/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: OneWayANOVA.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import JSci.maths.statistics.FDistribution;

import org.tigr.util.ConfMap;
import org.tigr.util.FloatMatrix;

import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.NodeValueList;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.gui.impl.owa.OneWayANOVAInitBox;
/**
 *
 * @author  nbhagaba
 * @version 
 */
public class OneWayANOVA extends AbstractAlgorithm {
    private boolean stop = false;
    
    private int function;
    private float factor;
    private boolean absolute;
    private FloatMatrix expMatrix;
    
    private Vector[] clusters;
    private int k; // # of clusters
    
    private int numGenes, numExps, numGroups;
    private float alpha;
    private int correctionMethod;    
    int[] groupAssignments; 
    
    float currentP = 0.0f;
    float currentF = 0.0f;
    int currentIndex = 0; 
    double constant;
    
    Vector fValuesVector = new Vector();
    Vector pValuesVector = new Vector();    

    /**
     * This method should interrupt the calculation.
     */
    public void abort() {
        stop = true;        
    }
    
    /**
     * This method execute calculation and return result,
     * stored in <code>AlgorithmData</code> class.
     *
     * @param data the data to be calculated.
     */
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
	groupAssignments = data.getIntArray("group-assignments");
	
	AlgorithmParameters map = data.getParams();
	function = map.getInt("distance-function", EUCLIDEAN);
	factor   = map.getFloat("distance-factor", 1.0f);
	absolute = map.getBoolean("distance-absolute", false);
	
	boolean hierarchical_tree = map.getBoolean("hierarchical-tree", false);
	int method_linkage = map.getInt("method-linkage", 0);
	boolean calculate_genes = map.getBoolean("calculate-genes", false);
	boolean calculate_experiments = map.getBoolean("calculate-experiments", false);
	
	this.expMatrix = data.getMatrix("experiment");
	
	numGenes = this.expMatrix.getRowDimension();
	numExps = this.expMatrix.getColumnDimension();
	alpha = map.getFloat("alpha", 0.01f);
	correctionMethod = map.getInt("correction-method", OneWayANOVAInitBox.JUST_ALPHA);        
        numGroups = map.getInt("numGroups", 3);
        Vector clusterVector = sortGenesBySignificance();
        k = clusterVector.size();

        FloatMatrix fValuesMatrix = new FloatMatrix(fValuesVector.size(), 1);  
        FloatMatrix pValuesMatrix = new FloatMatrix(pValuesVector.size(), 1);   
        FloatMatrix dfNumMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix dfDenomMatrix = new FloatMatrix(numGenes, 1);

        for (int i = 0; i < fValuesVector.size(); i++) {
            fValuesMatrix.A[i][0] = ((Float)(fValuesVector.get(i))).floatValue();
        }  
        
        for (int i = 0; i < pValuesVector.size(); i++) {
            pValuesMatrix.A[i][0] = ((Float)(pValuesVector.get(i))).floatValue();
        } 
        
        for (int i = 0; i < numGenes; i++) {
            dfNumMatrix.A[i][0] = (float)(getDfNum(i));
            dfDenomMatrix.A[i][0] = (float)(getDfDenom(i));
        }
        
        FloatMatrix ssGroupsMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix ssErrorMatrix = new FloatMatrix(numGenes, 1);
        
        for (int i = 0; i < ssGroupsMatrix.getRowDimension(); i++) {
            float[] currentGene = getGene(i);
            constant = getConstant(currentGene);
            /*
            if (i == 11) {
                System.out.println("Gene A3: groupsSS = " + getGroupsSS(currentGene));
            }
             */
            ssGroupsMatrix.A[i][0] = (float)(getGroupsSS(currentGene));
            /*
            if (i == 11) {
                System.out.println("Gene A3: ssGroupsMatrix.A[11][0] = " + ssGroupsMatrix.A[i][0]);
            }
             */
            ssErrorMatrix.A[i][0] = (float)(getTotalSS(currentGene) - getGroupsSS(currentGene));
        }
        
	clusters = new Vector[k];
	
	for (int i = 0; i < k; i++) {
	    clusters[i] = (Vector)(clusterVector.get(i));
	}
	
	FloatMatrix means = getMeans(clusters);
	FloatMatrix variances = getVariances(clusters, means);        

	AlgorithmEvent event = null;
	if (hierarchical_tree) {
	    event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, clusters.length, "Calculate Hierarchical Trees");
	    fireValueChanged(event);
	    event.setIntValue(0);
	    event.setId(AlgorithmEvent.PROGRESS_VALUE);
	    fireValueChanged(event);
	}        

	Cluster result_cluster = new Cluster();
	NodeList nodeList = result_cluster.getNodeList();
	int[] features;        
	for (int i=0; i<clusters.length; i++) {
	    if (stop) {
		throw new AbortException();
	    }
	    features = convert2int(clusters[i]);
	    Node node = new Node(features);
	    nodeList.addNode(node);
	    if (hierarchical_tree) {
		node.setValues(calculateHierarchicalTree(features, method_linkage, calculate_genes, calculate_experiments));
		event.setIntValue(i+1);
		fireValueChanged(event);
	    }
	}
        
	// prepare the result
	AlgorithmData result = new AlgorithmData();
	result.addCluster("cluster", result_cluster);
	result.addParam("number-of-clusters", String.valueOf(clusters.length));    
	result.addMatrix("clusters_means", means);
	result.addMatrix("clusters_variances", variances); 
        result.addMatrix("pValues", pValuesMatrix);
        result.addMatrix("fValues", fValuesMatrix);
        result.addMatrix("dfNumMatrix", dfNumMatrix);
        result.addMatrix("dfDenomMatrix", dfDenomMatrix);
        result.addMatrix("ssGroupsMatrix", ssGroupsMatrix);
        result.addMatrix("ssErrorMatrix", ssErrorMatrix);
        result.addMatrix("geneGroupMeansMatrix", getAllGeneGroupMeans());
        result.addMatrix("geneGroupSDsMatrix", getAllGeneGroupSDs());
	return result;        
        //return null; //for now
    }

    private NodeValueList calculateHierarchicalTree(int[] features, int method, boolean genes, boolean experiments) throws AlgorithmException {
	NodeValueList nodeList = new NodeValueList();
	AlgorithmData data = new AlgorithmData();
	FloatMatrix experiment = getSubExperiment(this.expMatrix, features);
	data.addMatrix("experiment", experiment);
	data.addParam("distance-function", String.valueOf(this.function));
	data.addParam("distance-absolute", String.valueOf(this.absolute));
	data.addParam("method-linkage", String.valueOf(method));
	HCL hcl = new HCL();
	AlgorithmData result;
	if (genes) {
	    data.addParam("calculate-genes", String.valueOf(true));
	    result = hcl.execute(data);
	    validate(result);
	    addNodeValues(nodeList, result);
	}
	if (experiments) {
	    data.addParam("calculate-genes", String.valueOf(false));
	    result = hcl.execute(data);
	    validate(result);
	    addNodeValues(nodeList, result);
	}
	return nodeList;
    }
    
    private void addNodeValues(NodeValueList target_list, AlgorithmData source_result) {
	target_list.addNodeValue(new NodeValue("child-1-array", source_result.getIntArray("child-1-array")));
	target_list.addNodeValue(new NodeValue("child-2-array", source_result.getIntArray("child-2-array")));
	target_list.addNodeValue(new NodeValue("node-order", source_result.getIntArray("node-order")));
	target_list.addNodeValue(new NodeValue("height", source_result.getMatrix("height").getRowPackedCopy()));
    }
    
    private FloatMatrix getSubExperiment(FloatMatrix experiment, int[] features) {
	FloatMatrix subExperiment = new FloatMatrix(features.length, experiment.getColumnDimension());
	for (int i=0; i<features.length; i++) {
	    subExperiment.A[i] = experiment.A[features[i]];
	}
	return subExperiment;
    }
    
    /**
     * Checking the result of hcl algorithm calculation.
     * @throws AlgorithmException, if the result is incorrect.
     */
    private void validate(AlgorithmData result) throws AlgorithmException {
	if (result.getIntArray("child-1-array") == null) {
	    throw new AlgorithmException("parameter 'child-1-array' is null");
	}
	if (result.getIntArray("child-2-array") == null) {
	    throw new AlgorithmException("parameter 'child-2-array' is null");
	}
	if (result.getIntArray("node-order") == null) {
	    throw new AlgorithmException("parameter 'node-order' is null");
	}
	if (result.getMatrix("height") == null) {
	    throw new AlgorithmException("parameter 'height' is null");
	}
    }
    
    private int[] convert2int(Vector source) {
	int[] int_matrix = new int[source.size()];
	for (int i=0; i<int_matrix.length; i++) {
	    int_matrix[i] = (int)((Integer)source.get(i)).intValue();
	}
	return int_matrix;
    }
    
    private FloatMatrix getMeans(Vector[] clusters) {
	FloatMatrix means = new FloatMatrix(clusters.length, numExps);
	FloatMatrix mean;
	for (int i=0; i<clusters.length; i++) {
	    mean = getMean(clusters[i]);
	    means.A[i] = mean.A[0];
	}
	return means;
    }
    
    private FloatMatrix getMean(Vector cluster) {
	FloatMatrix mean = new FloatMatrix(1, numExps);
	float currentMean;
	int n = cluster.size();
	int denom = 0;
	float value;
	for (int i=0; i<numExps; i++) {
	    currentMean = 0f;
	    denom = 0;
	    for (int j=0; j<n; j++) {
		value = expMatrix.get(((Integer) cluster.get(j)).intValue(), i);
		if (!Float.isNaN(value)) {
		    currentMean += value;
		    denom++;
		}
	    }
	    mean.set(0, i, currentMean/(float)denom);
	}
	
	return mean;
    }
    
    private FloatMatrix getVariances(Vector[] clusters, FloatMatrix means) {
	final int rows = means.getRowDimension();
	final int columns = means.getColumnDimension();
	FloatMatrix variances = new FloatMatrix(rows, columns);
	for (int row=0; row<rows; row++) {
	    for (int column=0; column<columns; column++) {
		variances.set(row, column, getSampleVariance(clusters[row], column, means.get(row, column)));
	    }
	}
	return variances;
    }
    
    int validN;
    
    private float getSampleNormalizedSum(Vector cluster, int column, float mean) {
	final int size = cluster.size();
	float sum = 0f;
	float value;
	validN = 0;
	for (int i=0; i<size; i++) {
	    value = expMatrix.get(((Integer) cluster.get(i)).intValue(), column);
	    if (!Float.isNaN(value)) {
		sum += Math.pow(value-mean, 2);
		validN++;
	    }
	}
	return sum;
    }
    
    private float getSampleVariance(Vector cluster, int column, float mean) {
	return(float)Math.sqrt(getSampleNormalizedSum(cluster, column, mean)/(float)(validN-1));
	
    }    
    
    private Vector sortGenesBySignificance() throws AlgorithmException {
	Vector sigGenes = new Vector();
	Vector nonSigGenes = new Vector();
        
	AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numGenes);
	fireValueChanged(event);
	event.setId(AlgorithmEvent.PROGRESS_VALUE);    

	if ((correctionMethod == OneWayANOVAInitBox.JUST_ALPHA)||(correctionMethod == OneWayANOVAInitBox.STD_BONFERRONI)) {
	    sigGenes = new Vector();
	    nonSigGenes = new Vector();   
	    for (int i = 0; i < numGenes; i++) {
		if (stop) {
		    throw new AbortException();
		}
		event.setIntValue(i);
		event.setDescription("Current gene = " + (i + 1));
		fireValueChanged(event);  
		if (isSignificant(i)) {
		    sigGenes.add(new Integer(i));
                    fValuesVector.add(new Float(currentF));
                    pValuesVector.add(new Float(currentP));
		} else {
		    nonSigGenes.add(new Integer(i));
                    fValuesVector.add(new Float(currentF));
                    pValuesVector.add(new Float(currentP));                    
		}                
            }
        }
        
	Vector sortedGenes = new Vector();
	sortedGenes.add(sigGenes);
	sortedGenes.add(nonSigGenes);   
	
        return sortedGenes;        
    }
    
    private float[] getGene(int gene) {
        float[] currentGene = new float[expMatrix.getColumnDimension()];
        for (int i = 0; i < currentGene.length; i++) {
            currentGene[i] = expMatrix.A[gene][i];
        }
        return currentGene;
    }
    
    private int getDfNum(int gene) {
	//float[] geneValues = new float[numExps];
        int n = 0;
	for (int i = 0; i < numExps; i++) {
	    //geneValues[i] = expMatrix.A[gene][i];
            if ((!Float.isNaN(expMatrix.A[gene][i])) && (groupAssignments[i] != 0)) {
                n++;
            }
	} 
         if (n == 0) {
             return (-1); // will be exported as Float.NaN to OWAGUI
         }
        
         return (numGroups - 1);
    }
    
    private int getDfDenom(int gene) {
        int n = 0;
	for (int i = 0; i < numExps; i++) {
	    //geneValues[i] = expMatrix.A[gene][i];
            if ((!Float.isNaN(expMatrix.A[gene][i])) && (groupAssignments[i] != 0)) {
                n++;
            }
	} 
         if (n == 0) {
             return (-1); // will be exported as Float.NaN to OWAGUI
         } 
        return (n - numGroups);
    }
    
    private boolean isSignificant(int gene) {
        boolean sig = false;
	float[] geneValues = new float[numExps];
        int n = 0;
	for (int i = 0; i < numExps; i++) {
	    geneValues[i] = expMatrix.A[gene][i];
            if (!Float.isNaN(geneValues[i])) {
                n++;
            }
	}  
        
        if (n == 0) {
            currentF = Float.NaN;
            currentP = Float.NaN;
            return false;
        }
        
        constant = getConstant(geneValues);
        
        double totalSS = getTotalSS(geneValues);
        double groupsSS = getGroupsSS(geneValues);
        double errorSS = totalSS - groupsSS;
        
        if ((Double.isNaN(totalSS))||(Double.isNaN(groupsSS))||(Double.isNaN(errorSS))) {
            currentF = Float.NaN;
            currentP = Float.NaN;
            return false;
        }
        
        //int totalDF = validN - 1;
        int groupsDF = getDfNum(gene);
        int errorDF = getDfDenom(gene);
        
        double groupsMS = groupsSS / groupsDF;
        double errorMS = errorSS / errorDF;
        
        double fValue = groupsMS/errorMS;
        currentF = (float)(fValue);
        FDistribution fDist = new FDistribution(groupsDF, errorDF);
        //System.out.println("Gene " + gene + ": fValue = " + fValue + ", dfNum = " + groupsDF + ", dfDenom = " + errorDF);        
        double cumulProb = fDist.cumulative(fValue);
        double pValue = 2*(1 - cumulProb); // (1 - cumulProb) is the one-tailed test p-value
        
        if (pValue > 1) {
            pValue = 1.0d;
        }
        
        currentP = (float)pValue;
        
        double criticalPValue = 0;
        
        if (correctionMethod == OneWayANOVAInitBox.JUST_ALPHA) {
            criticalPValue = (double)alpha;
        } else if (correctionMethod == OneWayANOVAInitBox.STD_BONFERRONI) {
            criticalPValue = (double)alpha/numGenes;
        }
        
        //double criticalFValue = fDist.inverse(criticalPValue);
        
        //System.out.println("critical P = " + criticalPValue + ", dfNum = " + groupsDF + ", dfDenom = " + errorDF + ", critical F = " + criticalFValue);
        
        if (pValue <= criticalPValue) {
            sig = true;
        } else {
            sig = false;
        }
        
        /*
        if (currentF >= criticalFValue) {
            sig = true;
        } else {
            sig = false;
        }
         */
        /*
        FDistribution testF = new FDistribution(11, 15);
        double testP = testF.cumulative(2.51);
        
        System.out.println("F(11, 15)  = 2.51, cum. probability = " + (1 - testP));
         */
        //System.out.println("Gene " + gene + ": GroupsSS = " + groupsSS + ", errorSS = " + errorSS + ", groupsDF = " + groupsDF + ", errorDF = " + errorDF + ", F = " + fValue + ", p = " + pValue);
        
        return sig;
    }
    
    private double getConstant(float[] geneValues) {
        double sum = 0.0d;
        double cons;
        int n = 0;
        for (int i = 0; i < geneValues.length; i++) {
            if ((!Float.isNaN(geneValues[i])) && (groupAssignments[i] != 0)) {
                sum = sum + geneValues[i];
                n++;
            }
        }
        
        if (n == 0) {
            return Double.NaN;
        } else {
            cons = (Math.pow((double)sum, 2d))/n;
        }
        return cons;
       
    }
    
    private double getTotalSS(float[] geneValues) {
        double ss = 0;
        int n = 0;
        for (int i = 0; i < geneValues.length; i++) {
            if ((!Float.isNaN(geneValues[i])) && (groupAssignments[i] != 0)) {
                ss = ss + Math.pow(geneValues[i], 2);
                n++;
            }
        }  
        
        if (n == 0) {
            return Double.NaN;
        } else {
            ss = ss - constant;
        }
                       
        return ss;
    }
    
    private double getGroupsSS(float[] geneValues) {
        float[][] geneValuesByGroups = new float[numGroups][];
        
        for (int i = 0; i < numGroups; i++) {
            geneValuesByGroups[i] = getGeneValuesForGroup(geneValues, i+1);
        }
        
        double[] avSquareArray = new double[numGroups];
        
        for (int i = 0; i < numGroups; i++) {
            avSquareArray[i] = getAvSquare(geneValuesByGroups[i]);
        }
        
        double ss = 0;
        
        for (int i = 0; i < numGroups; i++) {
            ss = ss + avSquareArray[i];
        }
        
        return (ss - constant);
        
        //double ss = 0;
        //return ss;
    }
    
    private float[] getGeneGroupMeans(int gene) {
	float[] geneValues = new float[numExps];
        for (int i = 0; i < numExps; i++) {
	    geneValues[i] = expMatrix.A[gene][i];
	} 
        
        float[][] geneValuesByGroups = new float[numGroups][];
        
        for (int i = 0; i < numGroups; i++) {
            geneValuesByGroups[i] = getGeneValuesForGroup(geneValues, i+1);
        } 
        
        float[] geneGroupMeans = new float[numGroups];
        for (int i = 0; i < numGroups; i++) {
            geneGroupMeans[i] = getMean(geneValuesByGroups[i]);
        }
        
        return geneGroupMeans;
    }
    
    private float[] getGeneGroupSDs(int gene) {
	float[] geneValues = new float[numExps];        
        for (int i = 0; i < numExps; i++) {
	    geneValues[i] = expMatrix.A[gene][i];
	}   
        
        float[][] geneValuesByGroups = new float[numGroups][];
        
        for (int i = 0; i < numGroups; i++) {
            geneValuesByGroups[i] = getGeneValuesForGroup(geneValues, i+1);
        }  
        
        float[] geneGroupSDs = new float[numGroups];
        for (int i = 0; i < numGroups; i++) {
            geneGroupSDs[i] = getStdDev(geneValuesByGroups[i]);
        }
        
        return geneGroupSDs;        
    }
    
    private float[] getGeneValuesForGroup(float[] geneValues, int group) {
        Vector groupValuesVector = new Vector();
        
        for (int i = 0; i < groupAssignments.length; i++) {
            if (groupAssignments[i] == group) {
                groupValuesVector.add(new Float(geneValues[i]));
            }
        }
        
        float[] groupGeneValues = new float[groupValuesVector.size()];
        
        for (int i = 0; i < groupValuesVector.size(); i++) {
            groupGeneValues[i] = ((Float)(groupValuesVector.get(i))).floatValue();
        }
        
        return groupGeneValues;
    }
    
    private FloatMatrix getAllGeneGroupMeans() {
        FloatMatrix means = new FloatMatrix(numGenes, numGroups);
        for (int i = 0; i < means.getRowDimension(); i++) {
            means.A[i] = getGeneGroupMeans(i);
        }
        return means;
    }
    
    private FloatMatrix getAllGeneGroupSDs() {
        FloatMatrix sds = new FloatMatrix(numGenes, numGroups);
        for (int i = 0; i < sds.getRowDimension(); i++) {
            sds.A[i] = getGeneGroupSDs(i);
        }
        return sds;        
    }
    
    private double getAvSquare(float[] values) {
        double ss = 0;
        double sum = 0;
        int n = 0;
        for (int i = 0; i < values.length; i++) {
            if (!Float.isNaN(values[i])) {
                sum  = sum + values[i];
                n++;
            }
        }
        
        if (n == 0) {
            return Double.NaN;
        } else {
            ss = (Math.pow(sum, 2)) / n;
        }
        
        return ss;
    }
    
    private float getMean(float[] group) {
	float sum = 0;
	int n = 0;
	
	for (int i = 0; i < group.length; i++) {
	    //System.out.println("getMean(): group[" + i + "] = " + group[i]);
	    if (!Float.isNaN(group[i])) {
		sum = sum + group[i];
		n++;
	    }
	}
	
	//System.out.println("getMean(): sum = " +sum);
	if (n == 0) {
            return Float.NaN;
        }
	float mean =  sum / (float)n;
        
        if (Float.isInfinite(mean)) {
            return Float.NaN;
        }
        
	return mean;
    }
    
    private float getStdDev(float[] group) {
	float mean = getMean(group);
	int n = 0;
	
	float sumSquares = 0;
	
	for (int i = 0; i < group.length; i++) {
	    if (!Float.isNaN(group[i])) {
		sumSquares = (float)(sumSquares + Math.pow((group[i] - mean), 2));
		n++;
	    }
	}
        
        if (n < 2) {
            return Float.NaN;
        }
	
	float var = sumSquares / (float)(n - 1);
	if (Float.isInfinite(var)) {
            return Float.NaN;
        }
	return (float)(Math.sqrt((double)var));
    }    
    
}
