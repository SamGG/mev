/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Ttest.java,v $
 * $Revision: 1.2 $
 * $Date: 2003-12-08 18:50:36 $
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

import org.tigr.util.ConfMap;
import org.tigr.util.FloatMatrix;
import org.tigr.util.QSort;
import org.tigr.util.Combinations;

import JSci.maths.statistics.TDistribution;

import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.NodeValueList;

import org.tigr.microarray.mev.cluster.gui.impl.ttest.TtestInitDialog;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;

public class Ttest extends AbstractAlgorithm {
    
    private boolean stop = false;
    
    private int function;
    private float factor;
    private boolean absolute;
    private FloatMatrix expMatrix;
    
    private Vector[] clusters;
    private int k; // # of clusters
    
    private int numGenes, numExps;
    private float alpha;
    private int significanceMethod;
    private boolean isPermut;
    int[] groupAssignments;
    private int numCombs;
    boolean useAllCombs;
    int tTestDesign;
    float oneClassMean = 0.0f;
    
    double currentP = 0.0f;
    double currentT = 0.0f;
    int currentIndex = 0;
    Vector sigTValues = new Vector();
    Vector sigPValues = new Vector();
    Vector nonSigTValues = new Vector();
    Vector nonSigPValues = new Vector();
    Vector tValuesVector = new Vector();
    Vector pValuesVector = new Vector();
    
    /**
     * This method should interrupt the calculation.
     */
    
    
    /**
     * This method execute calculation and return result,
     * stored in <code>AlgorithmData</code> class.
     *
     * @param data the data to be calculated.
     */
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
        /*
        TDistribution testT = new TDistribution(11);
        double cumulP = testT.cumulative(1.796);
        double testP = 2*(1 - cumulP);
        
        System.out.println("t(11) = 1.796 at p = " + testP);
         */
        
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
        tTestDesign = map.getInt("tTestDesign", TtestInitDialog.BETWEEN_SUBJECTS);
        if (tTestDesign == TtestInitDialog.ONE_CLASS) {
            oneClassMean = map.getFloat("oneClassMean", 0.0f);
        }
	alpha = map.getFloat("alpha", 0.01f);
	significanceMethod = map.getInt("significance-method", TtestInitDialog.JUST_ALPHA);
	isPermut = map.getBoolean("is-permut", false);
	numCombs = map.getInt("num-combs", 100);
	useAllCombs = map.getBoolean("use-all-combs", false);
	
	Vector clusterVector = new Vector();
        if (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) {
            if (isPermut) {
                clusterVector = sortGenesByPermutationSignificance();
            } else {
                clusterVector = sortGenesBySignificance();
            }
        } else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
            clusterVector = sortGenesForOneClassDesign();
        }
        
        k = clusterVector.size();

        
        FloatMatrix isSigMatrix = new FloatMatrix(numGenes, 1);
        
        for (int i = 0; i < isSigMatrix.getRowDimension(); i++) {
            isSigMatrix.A[i][0] = 0.0f;
        }
        
        Vector sigGenes = (Vector)(clusterVector.get(0));
        
        for (int i = 0 ; i < sigGenes.size(); i++) {
            int currentGene = ((Integer)(sigGenes.get(i))).intValue();
            isSigMatrix.A[currentGene][0] = 1.0f;
        }
        
        Vector oneClassDFVector = new Vector();
        Vector oneClassGeneMeansVector = new Vector();
        Vector oneClassGeneSDsVector = new Vector();
        
        if (tTestDesign == TtestInitDialog.ONE_CLASS) {
            AlgorithmEvent event2 = null;
            /*
            if (isPermut) {
                event2 = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numGenes);
                fireValueChanged(event2);
                event2.setId(AlgorithmEvent.PROGRESS_VALUE);  
            }
             */
            //if ((significanceMethod == TtestInitDialog.JUST_ALPHA) || (significanceMethod == TtestInitDialog.STD_BONFERRONI)) {
                tValuesVector = new Vector();
                //pValuesVector = new Vector();
                oneClassDFVector = new Vector();
                
                for (int i = 0; i < numGenes; i++) {
                    float[] currentGeneValues = getOneClassGeneValues(i);
                    float currentOneClassT = (float)getOneClassTValue(currentGeneValues);
                    //System.out.println("t = " + currentOneClassT);
                    tValuesVector.add(new Float(currentOneClassT));
                    float currentOneClassDF = (float)getOneClassDFValue(currentGeneValues);
                    oneClassDFVector.add(new Float(currentOneClassDF));
                    //float currentOneClassProb = 0.0f;
                    /*
                    if (!isPermut) {
                       currentOneClassProb = getProb(currentOneClassT, (int)currentOneClassDF);
                    } else {                        
                        event2.setIntValue(i);
                        event2.setDescription("Reporting gene statistics: Current gene = " + (i + 1));
                        fireValueChanged(event2); 
                        
                        if (useAllCombs) {
                            currentOneClassProb = getAllCombsOneClassProb(i);
                        } else {
                            currentOneClassProb = getSomeCombsOneClassProb(i);
                        }
                         
                    }
                     */
                    //pValuesVector.add(new Float(currentOneClassProb));
                    float currentOneClassMean = getMean(currentGeneValues);
                    oneClassGeneMeansVector.add(new Float(currentOneClassMean));
                    float currentOneClassSD = (float)(Math.sqrt(getVar(currentGeneValues)));
                    oneClassGeneSDsVector.add(new Float(currentOneClassSD));
                }
            //}
        }        
        
        FloatMatrix tValuesMatrix = new FloatMatrix(tValuesVector.size(), 1);  
        FloatMatrix pValuesMatrix = new FloatMatrix(pValuesVector.size(), 1); 
        //System.out.println("pValuesVector.size() = " + pValuesVector.size());
        FloatMatrix dfMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix oneClassMeansMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix oneClassSDsMatrix = new FloatMatrix(numGenes, 1);

        if (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) {
            for (int i = 0; i < tValuesVector.size(); i++) {
                tValuesMatrix.A[i][0] = Math.abs(((Float)(tValuesVector.get(i))).floatValue());
            }
        } else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
            for (int i = 0; i < tValuesVector.size(); i++) {            
                tValuesMatrix.A[i][0] = ((Float)(tValuesVector.get(i))).floatValue();
            }
        }
        
        for (int i = 0; i < pValuesVector.size(); i++) {
            pValuesMatrix.A[i][0] = ((Float)(pValuesVector.get(i))).floatValue();
        }
        if (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) {
            for (int i = 0; i < numGenes; i++) {
                dfMatrix.A[i][0] = (float)(getDF(i));
            }
        } else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
            for (int i = 0; i < numGenes; i++) {
                dfMatrix.A[i][0] = ((Float)(oneClassDFVector.get(i))).floatValue();
                oneClassMeansMatrix.A[i][0] = ((Float)(oneClassGeneMeansVector.get(i))).floatValue();
                oneClassSDsMatrix.A[i][0] = ((Float)(oneClassGeneSDsVector.get(i))).floatValue();
            }            
        }
        
        FloatMatrix meansAMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix meansBMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix sdAMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix sdBMatrix = new FloatMatrix(numGenes, 1);    

        if (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) {
            Vector meansAndSDs = getMeansAndSDs();
            float[] meansA = (float[])(meansAndSDs.get(0));
            float[] meansB = (float[])(meansAndSDs.get(1));
            float[] sdA = (float[])(meansAndSDs.get(2));
            float[] sdB = (float[])(meansAndSDs.get(3));
        
            for (int i = 0; i < numGenes; i++) {
                meansAMatrix.A[i][0] = meansA[i];
                meansBMatrix.A[i][0] = meansB[i];
                sdAMatrix.A[i][0] = sdA[i];
                sdBMatrix.A[i][0] = sdB[i];
            }
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
	//result.addParam("unassigned-genes-exist", String.valueOf(unassignedExists));
	result.addMatrix("clusters_means", means);
	result.addMatrix("clusters_variances", variances);
        //result.addMatrix("sigPValues", sigPValuesMatrix);
        //result.addMatrix("sigTValues", sigTValuesMatrix);
        //result.addMatrix("nonSigPValues", nonSigPValuesMatrix);
        //result.addMatrix("nonSigTValues", nonSigTValuesMatrix);  
        result.addMatrix("pValues", pValuesMatrix);
        result.addMatrix("tValues", tValuesMatrix);
        result.addMatrix("dfValues", dfMatrix);
        result.addMatrix("meansAMatrix", meansAMatrix);
        result.addMatrix("meansBMatrix", meansBMatrix);
        result.addMatrix("sdAMatrix", sdAMatrix);
        result.addMatrix("sdBMatrix", sdBMatrix);
        result.addMatrix("isSigMatrix", isSigMatrix);
        result.addMatrix("oneClassMeansMatrix", oneClassMeansMatrix);
        result.addMatrix("oneClassSDsMatrix", oneClassSDsMatrix);
	return result;
	
    }
    
    public void abort() {
	stop = true;
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
    
    private Vector sortGenesForOneClassDesign() throws AlgorithmException {
        Vector sigGenes = new Vector();
        Vector nonSigGenes = new Vector();
        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numGenes);
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);
        pValuesVector = new Vector();
        if (!isPermut) {            
            if ((significanceMethod == TtestInitDialog.JUST_ALPHA)||(significanceMethod == TtestInitDialog.STD_BONFERRONI)) {
                for (int i = 0; i < numGenes; i++) {
                    if (stop) {
                        throw new AbortException();
                    }
                    event.setIntValue(i);
                    event.setDescription("Current gene = " + (i + 1));
                    fireValueChanged(event);
                    if (isSigOneClass(i)) {
                        sigGenes.add(new Integer(i));
                    } else {
                        nonSigGenes.add(new Integer(i));
                    }
                }
            } else if (significanceMethod == TtestInitDialog.ADJ_BONFERRONI) {
                float[] pValues = new float[numGenes];
                for (int i = 0; i < numGenes; i++) {
                    if (stop) {
                        throw new AbortException();
                    }
                    event.setIntValue(i);
                    event.setDescription("Current gene = " + (i + 1));
                    fireValueChanged(event);  
                    float[] currentGeneValues = getOneClassGeneValues(i);
                    float currentOneClassT = (float)getOneClassTValue(currentGeneValues);
                    //System.out.println("t = " + currentOneClassT);
                    //tValuesVector.add(new Float(currentOneClassT));
                    float currentOneClassDF = (float)getOneClassDFValue(currentGeneValues);
                    //oneClassDFVector.add(new Float(currentOneClassDF));
                    float currentOneClassProb = getProb(currentOneClassT, (int)currentOneClassDF); 
                    pValues[i] = currentOneClassProb;
                }
                
                for (int i = 0; i < pValues.length; i++) {
                    pValuesVector.add(new Float(pValues[i]));
                }
                //double adjAlpha = alpha;
                int denomAlpha = numGenes;
                //int dF = 0;
                double adjAlpha = alpha/(double)denomAlpha;   
                
                QSort sortPVals = new QSort(pValues);
                float[] sortedPValues = sortPVals.getSorted();
                int[] sortedIndices = sortPVals.getOrigIndx();
                
                for (int i = (sortedPValues.length - 1); i >= 0; i--) {
                    if (sortedPValues[i] <= adjAlpha) {
                        sigGenes.add(new Integer(sortedIndices[i]));
                    } else {
                        nonSigGenes.add(new Integer(sortedIndices[i]));
                    }
                    
                    if (i < sortedPValues.length - 1) {
                        if (sortedPValues[i] < sortedPValues[i + 1]) {
                            denomAlpha--;
                            //System.out.println(" i = " + i + ", denomAlpha = " + denomAlpha);
                            if (denomAlpha < 1) {
                                System.out.println("Warning: denomAlpha = " + denomAlpha);
                            }                                
                        } else {
                            //System.out.println("Equal p-values: i = " + i + " and " + (i+1) + ", denomAlpha = " + denomAlpha);
                        }
                    } else {
                        if (denomAlpha < 1) {
                            System.out.println("Warning: denomAlpha = " + denomAlpha);
                        }
                    }
                    
                    adjAlpha = alpha / denomAlpha;
                    
                }
            }
            
        } else { // if (isPermut)
            if (useAllCombs) {
                if ((significanceMethod == TtestInitDialog.JUST_ALPHA) || (significanceMethod == TtestInitDialog.STD_BONFERRONI)) {
                    for (int i = 0; i < numGenes; i++) {
                        if (stop) {
                            throw new AbortException();
                        }
                        event.setIntValue(i);
                        event.setDescription("Current gene = " + (i + 1));
                        fireValueChanged(event);
                        
                        if (significanceMethod == TtestInitDialog.JUST_ALPHA) {
                            float currentProb = getAllCombsOneClassProb(i);
                            pValuesVector.add(new Float(currentProb));
                            if (currentProb <= alpha) {
                                sigGenes.add(new Integer(i));
                            } else {
                                nonSigGenes.add(new Integer(i));
                            }
                        } else if (significanceMethod == TtestInitDialog.STD_BONFERRONI) {
                            float currentProb = getAllCombsOneClassProb(i);
                            pValuesVector.add(new Float(currentProb));
                            float thresh = (float)(alpha/(double)numGenes);
                            if (currentProb <= thresh) {
                                sigGenes.add(new Integer(i));
                            } else {
                                nonSigGenes.add(new Integer(i));
                            }
                        }
                        
                    }
                } else if (significanceMethod == TtestInitDialog.ADJ_BONFERRONI) {
                    float[] pValues = new float[numGenes];
                    for (int i = 0; i < numGenes; i++) {
                        if (stop) {
                            throw new AbortException();
                        }
                        event.setIntValue(i);
                        event.setDescription("Current gene = " + (i + 1));
                        fireValueChanged(event);
                        pValues[i] = getAllCombsOneClassProb(i);
                    }
                    for (int i = 0; i < pValues.length; i++) {
                        pValuesVector.add(new Float(pValues[i]));
                    }
                    //double adjAlpha = alpha;
                    int denomAlpha = numGenes;
                    //int dF = 0;
                    double adjAlpha = alpha/(double)denomAlpha;
                    
                    QSort sortPVals = new QSort(pValues);
                    float[] sortedPValues = sortPVals.getSorted();
                    int[] sortedIndices = sortPVals.getOrigIndx();
                    
                    for (int i = (sortedPValues.length - 1); i >= 0; i--) {
                        if (sortedPValues[i] <= adjAlpha) {
                            sigGenes.add(new Integer(sortedIndices[i]));
                        } else {
                            nonSigGenes.add(new Integer(sortedIndices[i]));
                        }
                        
                        if (i < sortedPValues.length - 1) {
                            if (sortedPValues[i] < sortedPValues[i + 1]) {
                                denomAlpha--;
                                //System.out.println(" i = " + i + ", denomAlpha = " + denomAlpha);
                                if (denomAlpha < 1) {
                                    System.out.println("Warning: denomAlpha = " + denomAlpha);
                                }
                            } else {
                                //System.out.println("Equal p-values: i = " + i + " and " + (i+1) + ", denomAlpha = " + denomAlpha);
                            }
                        } else {
                            if (denomAlpha < 1) {
                                System.out.println("Warning: denomAlpha = " + denomAlpha);
                            }
                        }
                        
                        adjAlpha = alpha / denomAlpha;
                        
                    }
                }
                
            } else {// if !useAllCombs
                if ((significanceMethod == TtestInitDialog.JUST_ALPHA) || (significanceMethod == TtestInitDialog.STD_BONFERRONI)) {
                    for (int i = 0; i < numGenes; i++) {
                        if (stop) {
                            throw new AbortException();
                        }
                        event.setIntValue(i);
                        event.setDescription("Current gene = " + (i + 1));
                        fireValueChanged(event);
                        
                        float currentProb = getSomeCombsOneClassProb(i);
                        pValuesVector.add(new Float(currentProb));
                        
                        if (significanceMethod == TtestInitDialog.JUST_ALPHA) {
                           if (currentProb <= alpha) {
                               //System.out.println("currentProb = " + currentProb + ", alpha = " + alpha);
                                sigGenes.add(new Integer(i));
                            } else {
                                nonSigGenes.add(new Integer(i));
                            }                            
                        } else if (significanceMethod == TtestInitDialog.STD_BONFERRONI) {                            
                            float thresh = (float)(alpha/(double)numGenes);
                            if (currentProb <= thresh) {
                                sigGenes.add(new Integer(i));
                            } else {
                                nonSigGenes.add(new Integer(i));
                            }
                        } 
                        
                    }
                } else if (significanceMethod == TtestInitDialog.ADJ_BONFERRONI) {
                    float[] pValues = new float[numGenes];
                    for (int i = 0; i < numGenes; i++) {
                        if (stop) {
                            throw new AbortException();
                        }
                        event.setIntValue(i);
                        event.setDescription("Current gene = " + (i + 1));
                        fireValueChanged(event);
                        pValues[i] = getSomeCombsOneClassProb(i);
                    }
                    for (int i = 0; i < pValues.length; i++) {
                        pValuesVector.add(new Float(pValues[i]));
                    }
                    //double adjAlpha = alpha;
                    int denomAlpha = numGenes;
                    //int dF = 0;
                    double adjAlpha = alpha/(double)denomAlpha;
                    
                    QSort sortPVals = new QSort(pValues);
                    float[] sortedPValues = sortPVals.getSorted();
                    int[] sortedIndices = sortPVals.getOrigIndx();
                    
                    for (int i = (sortedPValues.length - 1); i >= 0; i--) {
                        if (sortedPValues[i] <= adjAlpha) {
                            sigGenes.add(new Integer(sortedIndices[i]));
                        } else {
                            nonSigGenes.add(new Integer(sortedIndices[i]));
                        }
                        
                        if (i < sortedPValues.length - 1) {
                            if (sortedPValues[i] < sortedPValues[i + 1]) {
                                denomAlpha--;
                                //System.out.println(" i = " + i + ", denomAlpha = " + denomAlpha);
                                if (denomAlpha < 1) {
                                    System.out.println("Warning: denomAlpha = " + denomAlpha);
                                }
                            } else {
                                //System.out.println("Equal p-values: i = " + i + " and " + (i+1) + ", denomAlpha = " + denomAlpha);
                            }
                        } else {
                            if (denomAlpha < 1) {
                                System.out.println("Warning: denomAlpha = " + denomAlpha);
                            }
                        }
                        
                        adjAlpha = alpha / denomAlpha;
                        
                    }                    
                }
            }
        }
        
        
        Vector sortedGenes = new Vector();
        sortedGenes.add(sigGenes);
        sortedGenes.add(nonSigGenes);
        
        return sortedGenes; 
    }
    
    private float getSomeCombsOneClassProb(int gene) {
        
        int validNumExps = getNumValidOneClassExpts();
        //int numAllPossOneClassPerms = (int)(Math.pow(2, validNumExps));        
        float[] currentGene = expMatrix.A[gene];
        float[] origGeneValues = getOneClassGeneValues(gene);
        float origOneClassT = (float)Math.abs(getOneClassTValue(origGeneValues));  
        if (Float.isNaN(origOneClassT)) {
            return Float.NaN;
        }

        Random rand  = new Random();
        long[] randomSeeds  = new long[numCombs];
        for (int i = 0; i < numCombs; i++) {
            randomSeeds[i] = rand.nextLong();
        }    
        
        int exceedCount = 0;
        for (int i = 0; i < numCombs; i++) {
            boolean[] changeSign = getSomeCombsPermutArray(randomSeeds[i]);
            float[] randomizedGene = new float[origGeneValues.length];
            
            for (int l = 0; l < changeSign.length; l++) {
                if (changeSign[l]) {
                    randomizedGene[l] = (float)(origGeneValues[l] - 2.0f*(origGeneValues[l] - oneClassMean));
                } else {
                    randomizedGene[l] = origGeneValues[l];
                }
            }            
          
            double randTValue = Math.abs(getOneClassTValue(randomizedGene));
            if (randTValue > origOneClassT) {
                exceedCount++;
            }            
        }
        
        //System.out.println();
        
        double prob = (double)exceedCount / (double)numCombs;
        
        return (float)prob;        
    }
    
    private boolean[] getSomeCombsPermutArray(long seed) {
        boolean[] boolArray = new boolean[getNumValidOneClassExpts()];
        for (int i = 0; i < boolArray.length; i++) {
            boolArray[i] = false;
        }

        Random generator2 =new Random(seed);
        for (int i = 0; i < boolArray.length; i++) {
            
            boolArray[i] = generator2.nextBoolean();
            //System.out.print(boolArray[i] + " ");
            /*
            try {
                Thread.sleep(10);
            } catch (Exception exc) {
                exc.printStackTrace();
            }  
             */
              
        }
        
        //System.out.println();
        return boolArray;        
    }
    
    private float getAllCombsOneClassProb(int gene) {
        
        int validNumExps = getNumValidOneClassExpts();
        int numAllPossOneClassPerms = (int)(Math.pow(2, validNumExps));        
        float[] currentGene = expMatrix.A[gene];
        float[] origGeneValues = getOneClassGeneValues(gene);
        float origOneClassT = (float)Math.abs(getOneClassTValue(origGeneValues));
        if (Float.isNaN(origOneClassT)) {
            return Float.NaN;
        }
        int exceedCount = 0;
        
        for (int j = 0; j < numAllPossOneClassPerms; j++) {
            boolean[] changeSign = getOneClassPermutArray(j);
            float[] randomizedGene = new float[currentGene.length];
            
            for (int l = 0; l < changeSign.length; l++) {
                if (changeSign[l]) {
                    randomizedGene[l] = (float)(currentGene[l] - 2.0f*(currentGene[l] - oneClassMean));
                } else {
                    randomizedGene[l] = currentGene[l];
                }
            }
            
            float[] reducedRandGene = new float[validNumExps];
            int count = 0;
            for (int l = 0; l < groupAssignments.length; l++) {
                if (groupAssignments[l] == 1) {
                    reducedRandGene[count] = randomizedGene[l];
                    count++;
                }
            }
            
            double randTValue = Math.abs(getOneClassTValue(reducedRandGene));
            if (randTValue > origOneClassT) {
                exceedCount++;
            }
            
        }
        
        //System.out.println();
        double prob = (double)exceedCount / (double)numAllPossOneClassPerms;
        
        return (float)prob;
    }
    
    boolean[] getOneClassPermutArray(int num) {
        boolean[] oneClassPermutArray = new boolean[numExps];
        
        for (int i = 0; i < oneClassPermutArray.length; i++) {
            oneClassPermutArray[i] = false;
        }
        
        int validNumExps = getNumValidOneClassExpts();
        
        String binaryString = Integer.toBinaryString(num);
        //System.out.println(binaryString);
        char[] binArray = binaryString.toCharArray();
        if (binArray.length < validNumExps) {
            Vector binVector = new Vector();
            for (int i = 0; i < (validNumExps - binArray.length); i++) {
                binVector.add(new Character('0'));
            }
            
            for (int i = 0; i < binArray.length; i++) {
                binVector.add(new Character(binArray[i]));
            }
            binArray = new char[binVector.size()]; 
            
            for (int i = 0; i < binArray.length; i++) {
                binArray[i] = ((Character)(binVector.get(i))).charValue();
            }
        } 
        /*
        for (int i = 0; i < binArray.length; i++) {
            System.out.print(binArray[i]);
        }
        System.out.println();
         */
        int counter = 0;
        
        for (int i = 0; i < oneClassPermutArray.length; i++) {
            if (groupAssignments[i] == 1) {
                if (binArray[counter] == '1') {
                    oneClassPermutArray[i] = true;
                } else {
                    oneClassPermutArray[i] = false;
                }
                counter++;
            }
        }
        /*
        for (int i = 0; i < oneClassPermutArray.length; i++) {
            System.out.print(oneClassPermutArray[i] + " ");
        }
        System.out.println();
        */
        return oneClassPermutArray;
    }
    
    public int getNumValidOneClassExpts() {
        int validNum = 0;
        
        for (int i =0; i < groupAssignments.length; i++) {
            if (groupAssignments[i] == 1) {
                validNum++;
            }
        }
        
        return validNum;
    }    
    
    private float[] getOneClassGeneValues(int gene) {
        Vector currentGene = new Vector();
        
        for (int i = 0; i < numExps; i++) {
            if (groupAssignments[i] == 1) {
                currentGene.add(new Float(expMatrix.A[gene][i]));
            }
        }
        
        float[] currGeneArray = new float[currentGene.size()];
        
        for (int i = 0; i < currGeneArray.length; i++) {
            currGeneArray[i] = ((Float)(currentGene.get(i))).floatValue();
        }
        
        return currGeneArray;
    }
    
    private boolean isSigOneClass(int gene) {
        boolean isSig = false;
        Vector currentGene = new Vector();
        
        for (int i = 0; i < numExps; i++) {
            if (groupAssignments[i] == 1) {
                currentGene.add(new Float(expMatrix.A[gene][i]));
            }
        }
        
        float[] currGeneArray = new float[currentGene.size()];
        
        for (int i = 0; i < currGeneArray.length; i++) {
            currGeneArray[i] = ((Float)(currentGene.get(i))).floatValue();
        }
        
        double tValue = getOneClassTValue(currGeneArray);
        
        if (Double.isNaN(tValue)) {
            pValuesVector.add(new Float(Float.NaN));
            return false;
        }
        
        int validNum = 0;
        for (int i = 0; i < currGeneArray.length; i++) {
            if (!Float.isNaN(currGeneArray[i])) {
                validNum++;
            }
        }        
        
        int df = validNum -1;
        double prob;
        TDistribution tDist = new TDistribution(df);
        double cumulP = tDist.cumulative(Math.abs(tValue));
        prob = 2*(1 - cumulP); // two-tailed test
        if (prob > 1) {
            prob = 1;
        } 
        
        pValuesVector.add(new Float((float)prob));
        
        if (significanceMethod == TtestInitDialog.JUST_ALPHA) {
            if (prob <= alpha) {
                isSig = true;
            } else {
                isSig = false;
            }
        } else if (significanceMethod == TtestInitDialog.STD_BONFERRONI) {
            double thresh = alpha/(double)numGenes;
            if (prob <= thresh) {
                isSig = true;
            } else {
                isSig = false;
            }
            
        }
        
        return isSig;
    }
    
    private double getOneClassTValue(float[] geneArray) {
        double tValue;
        
        float mean = getMean(geneArray);
        double stdDev = Math.sqrt((double)(getVar(geneArray)));
        
        int validNum = 0;
        for (int i = 0; i < geneArray.length; i++) {
            if (!Float.isNaN(geneArray[i])) {
                validNum++;
            }
        }
        
        double stdErr = stdDev / (Math.sqrt(validNum));
        
        tValue = ((double)(mean - oneClassMean))/stdErr;
        
     
        
        return tValue;
    }
    
    private int getOneClassDFValue(float[] geneArray) {
        int validNum = 0;
        for (int i = 0; i < geneArray.length; i++) {
            if (!Float.isNaN(geneArray[i])) {
                validNum++;
            }
        }        
        
        int df = validNum -1;  
        
        return df;
    }
    
    private float getProb(float tValue, int df) {
        TDistribution tDist = new TDistribution(df);
        double cumulP = tDist.cumulative(Math.abs((double)tValue));
        double prob = 2*(1 - cumulP); // two-tailed test
        if (prob > 1) {
            prob = 1;
        }  
        
        return (float)prob;
    }
    
    private Vector sortGenesBySignificance() throws AlgorithmException {
	Vector sigGenes = new Vector();
	Vector nonSigGenes = new Vector();
	
	AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numGenes);
	fireValueChanged(event);
	event.setId(AlgorithmEvent.PROGRESS_VALUE);
	
	//System.out.println("alpha = " + alpha);
	
	if ((significanceMethod == TtestInitDialog.JUST_ALPHA)||(significanceMethod == TtestInitDialog.STD_BONFERRONI)) {
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
                    sigTValues.add(new Float(currentT));
                    sigPValues.add(new Float(currentP));
                    tValuesVector.add(new Float(currentT));
                    pValuesVector.add(new Float(currentP));
		} else {
		    nonSigGenes.add(new Integer(i));
                    nonSigTValues.add(new Float(currentT));
                    nonSigPValues.add(new Float(currentP)); 
                    tValuesVector.add(new Float(currentT));
                    pValuesVector.add(new Float(currentP));                    
		}
		
	    }
	} else if (significanceMethod == TtestInitDialog.ADJ_BONFERRONI) {
	    sigGenes = new Vector();
	    nonSigGenes = new Vector();
	    float[] tValues = new float[numGenes];
	    for (int i = 0; i < numGenes; i++) {
		if (stop) {
		    throw new AbortException();
		}
		
		event.setIntValue(i);
		event.setDescription("Current gene = " + (i + 1));
		fireValueChanged(event);
		
		tValues[i] = Math.abs(getTValue(i));
		//System.out.println("Unsorted: tValues[" + i + "] = " + tValues[i]);
		
	    }
	    
	    QSort sortTValues = new QSort(tValues);
	    float[] sortedTValues = sortTValues.getSorted();
	    int[] sortedUniqueIDs = sortTValues.getOrigIndx();
	    
            /*
	    for (int i = 0; i < sortedTValues.length; i++) {
		//System.out.println("sortedTValues[" + i + "] =" + sortedTValues[i]);
	    }
	    
	    for (int i = 0; i < sortedUniqueIDs.length; i++) {
		//System.out.println("sortedUniqueIDs[" + i + "] =" + sortedUniqueIDs[i]);
	    }
             */
	    
	    double adjAlpha = alpha;
	    int denomAlpha = numGenes;
	    int dF = 0;
	    double prob = Double.POSITIVE_INFINITY;
	    
            double[] tValuesArray = new double[numGenes];
            double[] pValuesArray = new double[numGenes];
            
	    for (int i = (sortedTValues.length - 1); i > 0; i--) {
		dF = getDF(sortedUniqueIDs[i]);
		if((Float.isNaN(sortedTValues[i])) || (Float.isNaN((new Integer(dF)).floatValue())) || (dF <= 0)) {
		    nonSigGenes.add(new Integer(sortedUniqueIDs[i]));
                    nonSigTValues.add(new Float(sortedTValues[i]));
                    nonSigPValues.add(new Float(Float.NaN));
                    tValuesArray[sortedUniqueIDs[i]] = sortedTValues[i];
                    pValuesArray[sortedUniqueIDs[i]] = Float.NaN;
                    
		    /*
		    System.out.print("sortedTValues[" + i + "] = " + sortedTValues[i] + "....");
		    System.out.print(" dF = " + dF + "....");
		    System.out.print("prob = " + prob + "....");
		    System.out.print("denomAlpha = " + denomAlpha + "....");
		    System.out.print("adjAlpha = " + adjAlpha);
		    System.out.println("... non-significant");
		     */
		} else {
		    TDistribution tDist = new TDistribution(dF);
                    double cumulP = tDist.cumulative(sortedTValues[i]);
                    prob = 2*(1 - cumulP); // two-tailed test
                    if (prob > 1) {
                        prob = 1;
                    }
		    //prob = tDist.probability(sortedTValues[i]);
		    adjAlpha = alpha/(double)denomAlpha;
		    /*
		    System.out.print("sortedTValues[" + i+ "] = " + sortedTValues[i] + "....");
		    System.out.print(" dF = " + dF + "....");
		    System.out.print("prob = " + prob + "....");
		    System.out.print("denomAlpha = " + denomAlpha + "....");
		    System.out.print("adjAlpha = " + adjAlpha);
		     */
		    if (prob <= adjAlpha) {
			sigGenes.add(new Integer(sortedUniqueIDs[i]));
                        sigTValues.add(new Float(sortedTValues[i]));
                        sigPValues.add(new Float(prob)); 
                        tValuesArray[sortedUniqueIDs[i]] = sortedTValues[i];
                        pValuesArray[sortedUniqueIDs[i]] = prob;                        
			//System.out.println("... significant");
		    } else {
			nonSigGenes.add(new Integer(sortedUniqueIDs[i]));
                        nonSigTValues.add(new Float(sortedTValues[i]));
                        nonSigPValues.add(new Float(prob)); 
                        tValuesArray[sortedUniqueIDs[i]] = sortedTValues[i];
                        pValuesArray[sortedUniqueIDs[i]] = prob;                        
			//System.out.println("... non-significant");
		    }
		    
		    if (sortedTValues[i] > sortedTValues[i - 1]) {
			//System.out.println("denomAlpha = " + denomAlpha);
			denomAlpha--;
			if (denomAlpha < 1) {
			    System.out.println("Warning: denomAlpha = " + denomAlpha);
			}
		    }
		}
		
	    }
	    
	    dF = getDF(sortedUniqueIDs[0]);
	    if((Float.isNaN(sortedTValues[0])) || (Float.isNaN((new Integer(dF)).floatValue())) || (dF <= 0)) {
		nonSigGenes.add(new Integer(sortedUniqueIDs[0]));
                nonSigTValues.add(new Float(sortedTValues[0]));
                nonSigPValues.add(new Float(Float.NaN));
                tValuesArray[sortedUniqueIDs[0]] = sortedTValues[0];
                pValuesArray[sortedUniqueIDs[0]] = Float.NaN;                
		    /*
		    System.out.print("sortedTValues[0] = " + sortedTValues[0] + "....");
		    System.out.print(" dF = " + dF + "....");
		    System.out.print("prob = " + prob + "....");
		    System.out.print("denomAlpha = " + denomAlpha + "....");
		    System.out.print("adjAlpha = " + adjAlpha);
		    System.out.println("... non-significant");
		     */
		
	    } else {
		TDistribution tDist = new TDistribution(dF);
                double cumulP = tDist.cumulative(sortedTValues[0]);
                prob = 2*(1 - cumulP); // two-tailed test
                if (prob > 1) {
                    prob = 1;
                }              
		//prob = tDist.probability(sortedTValues[0]);
		adjAlpha = alpha/(double)denomAlpha;
		    /*
		    System.out.print("sortedTValues[0] = " + sortedTValues[0] + "....");
		    System.out.print(" dF = " + dF + "....");
		    System.out.print("prob = " + prob + "....");
		    System.out.print("denomAlpha = " + denomAlpha + "....");
		    System.out.print("adjAlpha = " + adjAlpha);
		     */
		if (prob <= adjAlpha) {
		    sigGenes.add(new Integer(sortedUniqueIDs[0]));
                    sigTValues.add(new Float(sortedTValues[0]));
                    sigPValues.add(new Float(prob));
                    tValuesArray[sortedUniqueIDs[0]] = sortedTValues[0];
                    pValuesArray[sortedUniqueIDs[0]] = prob;                     
		    //System.out.println("... significant");
		} else {
		    nonSigGenes.add(new Integer(sortedUniqueIDs[0]));
                    nonSigTValues.add(new Float(sortedTValues[0]));
                    nonSigPValues.add(new Float(prob));
                    tValuesArray[sortedUniqueIDs[0]] = sortedTValues[0];
                    pValuesArray[sortedUniqueIDs[0]] = prob;                     
		    //System.out.println("... non-significant");
		}
		
	    }
            
            tValuesVector = new Vector();
            pValuesVector = new Vector();
            
            for (int i = 0; i < tValuesArray.length; i++) {
                tValuesVector.add(new Float(tValuesArray[i]));
                pValuesVector.add(new Float(pValuesArray[i])); 
                               
            }
            
            /*
            for (int i = 0; i < pValuesArray.length; i++) {
                System.out.println("pValuesArray[" + i + "] = " + pValuesArray[i]); 
            }
            
            for (int i = 0; i < tValuesArray.length; i++) {
                System.out.println("tValuesArray[" + i + "] = " + tValuesArray[i]); 
            }  
             */          
	}
	
	Vector sortedGenes = new Vector();
	sortedGenes.add(sigGenes);
	sortedGenes.add(nonSigGenes);
	
	return sortedGenes;
    }
    
    private Vector sortGenesByPermutationSignificance() throws AlgorithmException {
	Vector sigGenes = new Vector();
	Vector nonSigGenes = new Vector();
	
	AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numGenes);
	fireValueChanged(event);
	event.setId(AlgorithmEvent.PROGRESS_VALUE);
	
	if ((significanceMethod == TtestInitDialog.JUST_ALPHA)||(significanceMethod == TtestInitDialog.STD_BONFERRONI)) {
	    sigGenes = new Vector();
	    nonSigGenes = new Vector();
	    for (int i = 0; i < numGenes; i++) {
		if (stop) {
		    throw new AbortException();
		}
		
		event.setIntValue(i);
		event.setDescription("Current gene = " + (i + 1));
		fireValueChanged(event);
		if (isSignificantByPermutation(i)) {
		    sigGenes.add(new Integer(i));
                    sigTValues.add(new Float(currentT));
                    sigPValues.add(new Float(currentP));
                    tValuesVector.add(new Float(currentT));
                    pValuesVector.add(new Float(currentP));                    
		} else {
		    nonSigGenes.add(new Integer(i));
                    nonSigTValues.add(new Float(currentT));
                    nonSigPValues.add(new Float(currentP)); 
                    tValuesVector.add(new Float(currentT));
                    pValuesVector.add(new Float(currentP));                   
		}
	    }
	} else if (significanceMethod == TtestInitDialog.ADJ_BONFERRONI) {
	    sigGenes = new Vector();
	    nonSigGenes = new Vector();
	    float[] tValues = new float[numGenes];
	    for (int i = 0; i < numGenes; i++) {
		if (stop) {
		    throw new AbortException();
		}
		
		event.setIntValue(i);
		event.setDescription("Current gene = " + (i + 1));
		fireValueChanged(event);
		
		tValues[i] = Math.abs(getTValue(i));
		//System.out.println("Unsorted: tValues[" + i + "] = " + tValues[i]);
		
	    }
	    
	    QSort sortTValues = new QSort(tValues);
	    float[] sortedTValues = sortTValues.getSorted();
	    int[] sortedUniqueIDs = sortTValues.getOrigIndx();
	    /*
	    for (int i = 0; i < sortedTValues.length; i++) {
		//System.out.println("sortedTValues[" + i + "] =" + sortedTValues[i]);
	    }
	     
	    for (int i = 0; i < sortedUniqueIDs.length; i++) {
		//System.out.println("sortedUniqueIDs[" + i + "] =" + sortedUniqueIDs[i]);
	    }
	     */
	    
	    double adjAlpha = alpha;
	    int denomAlpha = numGenes;
	    //int dF = 0;
	    double prob = Double.POSITIVE_INFINITY;
            
            double[] tValuesArray = new double[numGenes];
            double[] pValuesArray = new double[numGenes];            
	    
	    for (int i = (sortedTValues.length - 1); i > 0; i--) {
		//dF = getDF(sortedUniqueIDs[i]);
		if(Float.isNaN(sortedTValues[i]) ) {
		    nonSigGenes.add(new Integer(sortedUniqueIDs[i]));
                    nonSigTValues.add(new Float(sortedTValues[i]));
                    nonSigPValues.add(new Float(Float.NaN));
                    tValuesArray[sortedUniqueIDs[i]] = sortedTValues[i];
                    pValuesArray[sortedUniqueIDs[i]] = Float.NaN;                    
		    /*
		    System.out.print("sortedTValues[" + i + "] = " + sortedTValues[i] + "....");
		    System.out.print(" dF = " + dF + "....");
		    System.out.print("prob = " + prob + "....");
		    System.out.print("denomAlpha = " + denomAlpha + "....");
		    System.out.print("adjAlpha = " + adjAlpha);
		    System.out.println("... non-significant");
		     */
		} else {
		    //TDistribution tDist = new TDistribution(dF);
		    //prob = tDist.probability(sortedTValues[i]);
		    prob = getPermutedProb(sortedUniqueIDs[i]);
		    adjAlpha = alpha/(double)denomAlpha;
		    /*
		    System.out.print("sortedTValues[" + i+ "] = " + sortedTValues[i] + "....");
		    System.out.print(" dF = " + dF + "....");
		    System.out.print("prob = " + prob + "....");
		    System.out.print("denomAlpha = " + denomAlpha + "....");
		    System.out.print("adjAlpha = " + adjAlpha);
		     */
		    if (prob <= adjAlpha) {
			sigGenes.add(new Integer(sortedUniqueIDs[i]));
                        sigTValues.add(new Float(currentT));
                        sigPValues.add(new Float(prob));
                        tValuesArray[sortedUniqueIDs[i]] = sortedTValues[i];
                        pValuesArray[sortedUniqueIDs[i]] = prob;                         
			//System.out.println("... significant");
		    } else {
			nonSigGenes.add(new Integer(sortedUniqueIDs[i]));
                        nonSigTValues.add(new Float(currentT));
                        nonSigPValues.add(new Float(prob));
                        tValuesArray[sortedUniqueIDs[i]] = sortedTValues[i];
                        pValuesArray[sortedUniqueIDs[i]] = prob;                         
			//System.out.println("... non-significant");
		    }
		    
		    if (sortedTValues[i] > sortedTValues[i - 1]) {
			//System.out.println("denomAlpha = " + denomAlpha);
			denomAlpha--;
			if (denomAlpha < 1) {
			    System.out.println("Warning: denomAlpha = " + denomAlpha);
			}
		    }
		}
		
	    }
	    
	    //dF = getDF(sortedUniqueIDs[0]);
	    if(Float.isNaN(sortedTValues[0])) {
		nonSigGenes.add(new Integer(sortedUniqueIDs[0]));
                nonSigTValues.add(new Float(sortedTValues[0]));
                nonSigPValues.add(new Float(Float.NaN));
                tValuesArray[sortedUniqueIDs[0]] = sortedTValues[0];
                pValuesArray[sortedUniqueIDs[0]] = Float.NaN;                  
		    /*
		    System.out.print("sortedTValues[0] = " + sortedTValues[0] + "....");
		    System.out.print(" dF = " + dF + "....");
		    System.out.print("prob = " + prob + "....");
		    System.out.print("denomAlpha = " + denomAlpha + "....");
		    System.out.print("adjAlpha = " + adjAlpha);
		    System.out.println("... non-significant");
		     */
		
	    } else {
		//TDistribution tDist = new TDistribution(dF);
		prob = getPermutedProb(sortedUniqueIDs[0]);
		adjAlpha = alpha/(double)denomAlpha;
		    /*
		    System.out.print("sortedTValues[0] = " + sortedTValues[0] + "....");
		    System.out.print(" dF = " + dF + "....");
		    System.out.print("prob = " + prob + "....");
		    System.out.print("denomAlpha = " + denomAlpha + "....");
		    System.out.print("adjAlpha = " + adjAlpha);
		     */
		if (prob <= adjAlpha) {
		    sigGenes.add(new Integer(sortedUniqueIDs[0]));
                    sigTValues.add(new Float(currentT));
                    sigPValues.add(new Float(prob));
                    tValuesArray[sortedUniqueIDs[0]] = sortedTValues[0];
                    pValuesArray[sortedUniqueIDs[0]] = prob;                    
		    //System.out.println("... significant");
		} else {
		    nonSigGenes.add(new Integer(sortedUniqueIDs[0]));
                    nonSigTValues.add(new Float(currentT));
                    nonSigPValues.add(new Float(prob)); 
                    tValuesArray[sortedUniqueIDs[0]] = sortedTValues[0];
                    pValuesArray[sortedUniqueIDs[0]] = prob;                     
		    //System.out.println("... non-significant");
		}
		
	    }
            
            tValuesVector = new Vector();
            pValuesVector = new Vector();
            
            for (int i = 0; i < tValuesArray.length; i++) {
                tValuesVector.add(new Float(tValuesArray[i]));
                pValuesVector.add(new Float(pValuesArray[i]));                
            } 
            
            /*
            for (int i = 0; i < pValuesArray.length; i++) {
                System.out.println("pValuesArray[" + i + "] = " + pValuesArray[i]); 
            }
            
            for (int i = 0; i < tValuesArray.length; i++) {
                System.out.println("tValuesArray[" + i + "] = " + tValuesArray[i]); 
            } 
             */           
            
	    
	}
	
	Vector sortedGenes = new Vector();
	sortedGenes.add(sigGenes);
	sortedGenes.add(nonSigGenes);
	
	return sortedGenes;
	
    }
    
    private double getPermutedProb(int gene) {
	float[] geneValues = new float[numExps];
	for (int i = 0; i < numExps; i++) {
	    geneValues[i] = expMatrix.A[gene][i];
	}
	
	int groupACounter = 0;
	int groupBCounter = 0;
	
	for (int i = 0; i < groupAssignments.length; i++) {
	    if (groupAssignments[i] == TtestInitDialog.GROUP_A) {
		groupACounter++;
	    } else if (groupAssignments[i] == TtestInitDialog.GROUP_B) {
		groupBCounter++;
	    }
	}
	
	float[] groupAValues = new float[groupACounter];
	float[] groupBValues = new float[groupBCounter];
	int[] groupedExpts = new int[(groupACounter + groupBCounter)];
	
	groupACounter = 0;
	groupBCounter = 0;
	int groupedExptsCounter = 0;
	
	for (int i = 0; i < groupAssignments.length; i++) {
	    if (groupAssignments[i] == TtestInitDialog.GROUP_A) {
		groupAValues[groupACounter] = geneValues[i];
		groupACounter++;
		groupedExpts[groupedExptsCounter] = i;
		groupedExptsCounter++;
	    } else if (groupAssignments[i] == TtestInitDialog.GROUP_B) {
		groupBValues[groupBCounter] = geneValues[i];
		groupBCounter++;
		groupedExpts[groupedExptsCounter] = i;
		groupedExptsCounter++;
	    }
	}
	/*
	for (int i = 0; i < groupedExpts.length; i++) {
	    System.out.println("groupedExpts[" + i + "] = " + groupedExpts[i]);
	}
	 */
	
	float tValue = Math.abs(calculateTValue(groupAValues, groupBValues));
        currentT = tValue;
	double permutProb;
	permutProb = 0;
	//criticalP = 0;
	/*
	if (significanceMethod == TtestInitDialog.JUST_ALPHA) {
	    criticalP = alpha;
	} else if (significanceMethod == TtestInitDialog.STD_BONFERRONI) {
	    criticalP = alpha / (double)numGenes;
	}
	 */
	
	// if (Float.isNaN(tValue)) {
	//    sig = false;
	//    return sig;
	if (useAllCombs) {
	    int numCombsCounter = 0;
	    int[] combArray = new int[groupAValues.length];
	    for (int i = 0; i < combArray.length; i++) {
		combArray[i] = -1;
	    }
	    while (Combinations.enumerateCombinations(groupedExpts.length, groupAValues.length, combArray)) {
		float[] resampGroupA = new float[groupAValues.length];
		float[] resampGroupB = new float[groupBValues.length];
		int[] notInCombArray = new int[groupBValues.length];
		int notCombCounter = 0;
		for (int i = 0; i < groupedExpts.length; i++) {
		    if(!belongsInArray(i, combArray)) {
			notInCombArray[notCombCounter] = i;
			notCombCounter++;
		    }
		}
		    /*
		    for (int i = 0; i < groupedExpts.length; i++) {
			for (int j = 0; j < combArray.length; j++) {
			    if (combArray[j] == groupedExpts[i]) {
				continue;
			    }
			}
		    }
		     */
		    /*
		    System.out.print("combArray: ");
		     
		    for (int i = 0; i < combArray.length; i++) {
			System.out.print("" + combArray[i]);
		    }
		     
		    System.out.println();
		     
		    System.out.print("notInCombArray: ");
		     
		    for (int i = 0; i < notInCombArray.length; i++) {
			System.out.print("" + notInCombArray[i]);
		    }
		     
		    System.out.println();
		     */
		
		for(int i = 0; i < combArray.length; i++) {
		    resampGroupA[i] = geneValues[groupedExpts[combArray[i]]];
		}
		
		for(int i = 0; i < notInCombArray.length; i++) {
		    resampGroupB[i] = geneValues[groupedExpts[notInCombArray[i]]];
		}
		
		float resampTValue = Math.abs(calculateTValue(resampGroupA, resampGroupB));
		if (tValue < resampTValue) {
		    permutProb++;
		}
		numCombsCounter++;
	    }
	    
	    permutProb = permutProb/(double)numCombsCounter;
	    
	} else {//if (!useAllCombs)
	    int randomCounter = 0;
	    permutProb = 0;
	    for (int i = 0; i < numCombs; i++) {
		//int[] randomGroupA = new int[groupAValues.length];
		//int[] randomGroupB = new int[groupBValues.length];
		float[][] randomGroups = randomlyPermute(geneValues, groupedExpts, groupAValues.length, groupBValues.length);
		float randomizedTValue = Math.abs(calculateTValue(randomGroups[0], randomGroups[1]));
		if (tValue < randomizedTValue) {
		    permutProb++;
		}
		randomCounter++;
	    }
	    
	    permutProb = permutProb/(double)randomCounter;
	}
        
        currentP = permutProb;
	return permutProb;
	
    }
    
    
    private boolean isSignificantByPermutation(int gene) {
	boolean sig = false;
	float[] geneValues = new float[numExps];
	for (int i = 0; i < numExps; i++) {
	    geneValues[i] = expMatrix.A[gene][i];
	}
	
	int groupACounter = 0;
	int groupBCounter = 0;
	
	for (int i = 0; i < groupAssignments.length; i++) {
	    if (groupAssignments[i] == TtestInitDialog.GROUP_A) {
		groupACounter++;
	    } else if (groupAssignments[i] == TtestInitDialog.GROUP_B) {
		groupBCounter++;
	    }
	}
	
	float[] groupAValues = new float[groupACounter];
	float[] groupBValues = new float[groupBCounter];
	int[] groupedExpts = new int[(groupACounter + groupBCounter)];
        int numbValidValuesA = 0;
        int numbValidValuesB = 0;
	
	groupACounter = 0;
	groupBCounter = 0;
	int groupedExptsCounter = 0;
	
	for (int i = 0; i < groupAssignments.length; i++) {
	    if (groupAssignments[i] == TtestInitDialog.GROUP_A) {
		groupAValues[groupACounter] = geneValues[i];
                if (!Float.isNaN(geneValues[i])) {
                    numbValidValuesA++;
                }
		groupACounter++;
		groupedExpts[groupedExptsCounter] = i;
		groupedExptsCounter++;
	    } else if (groupAssignments[i] == TtestInitDialog.GROUP_B) {
		groupBValues[groupBCounter] = geneValues[i];
                if (!Float.isNaN(geneValues[i])) {
                    numbValidValuesB++;
                }                
		groupBCounter++;
		groupedExpts[groupedExptsCounter] = i;
		groupedExptsCounter++;
	    }
	}
        
        if ((numbValidValuesA < 2) || (numbValidValuesB < 2)) {
            currentP = Float.NaN;
            currentT = Float.NaN;
            return false;
        }
	/*
	for (int i = 0; i < groupedExpts.length; i++) {
	    System.out.println("groupedExpts[" + i + "] = " + groupedExpts[i]);
	}
	 */
	
	float tValue = Math.abs(calculateTValue(groupAValues, groupBValues));
        currentT = tValue;
	double permutProb, criticalP;
	permutProb = 0;
	criticalP = 0;
	
	if (significanceMethod == TtestInitDialog.JUST_ALPHA) {
	    criticalP = alpha;
	} else if (significanceMethod == TtestInitDialog.STD_BONFERRONI) {
	    criticalP = alpha / (double)numGenes;
	}
	
	if (Float.isNaN(tValue)) {
	    sig = false;
            currentP = Float.NaN;
	    return sig;
	} else if (useAllCombs) {
	    int numCombsCounter = 0;
	    int[] combArray = new int[groupAValues.length];
	    for (int i = 0; i < combArray.length; i++) {
		combArray[i] = -1;
	    }
	    while (Combinations.enumerateCombinations(groupedExpts.length, groupAValues.length, combArray)) {
		float[] resampGroupA = new float[groupAValues.length];
		float[] resampGroupB = new float[groupBValues.length];
		int[] notInCombArray = new int[groupBValues.length];
		int notCombCounter = 0;
		for (int i = 0; i < groupedExpts.length; i++) {
		    if(!belongsInArray(i, combArray)) {
			notInCombArray[notCombCounter] = i;
			notCombCounter++;
		    }
		}
		    /*
		    for (int i = 0; i < groupedExpts.length; i++) {
			for (int j = 0; j < combArray.length; j++) {
			    if (combArray[j] == groupedExpts[i]) {
				continue;
			    }
			}
		    }
		     */
		    /*
		    System.out.print("combArray: ");
		     
		    for (int i = 0; i < combArray.length; i++) {
			System.out.print("" + combArray[i]);
		    }
		     
		    System.out.println();
		     
		    System.out.print("notInCombArray: ");
		     
		    for (int i = 0; i < notInCombArray.length; i++) {
			System.out.print("" + notInCombArray[i]);
		    }
		     
		    System.out.println();
		     */
		
		for(int i = 0; i < combArray.length; i++) {
		    resampGroupA[i] = geneValues[groupedExpts[combArray[i]]];
		}
		
		for(int i = 0; i < notInCombArray.length; i++) {
		    resampGroupB[i] = geneValues[groupedExpts[notInCombArray[i]]];
		}
		
		float resampTValue = Math.abs(calculateTValue(resampGroupA, resampGroupB));
		if (tValue < resampTValue) {
		    permutProb++;
		}
		numCombsCounter++;
	    }
	    
	    permutProb = permutProb/(double)numCombsCounter;
            currentP = permutProb;
	    if (permutProb <= criticalP) {
		sig = true;
	    }
	    return sig;
	    
	} else {//if (!useAllCombs)
	    int randomCounter = 0;
	    permutProb = 0;
	    for (int i = 0; i < numCombs; i++) {
		//int[] randomGroupA = new int[groupAValues.length];
		//int[] randomGroupB = new int[groupBValues.length];
		float[][] randomGroups = randomlyPermute(geneValues, groupedExpts, groupAValues.length, groupBValues.length);
		float randomizedTValue = Math.abs(calculateTValue(randomGroups[0], randomGroups[1]));
		if (tValue < randomizedTValue) {
		    permutProb++;
		}
		randomCounter++;
	    }
	    
	    permutProb = permutProb/(double)randomCounter;
            currentP = permutProb;
	    if (permutProb <= criticalP) {
		sig = true;
	    }
	}
	
	return sig;
	
    }
    
    private float[][] randomlyPermute(float[] gene, int[] groupedExpts, int groupALength, int groupBLength) {
	float[][] groupedValues = new float[2][];
	groupedValues[0] = new float[groupALength];
	groupedValues[1] = new float[groupBLength];
	if (groupALength > groupBLength) {
	    groupedValues[0] = new float[groupBLength];
	    groupedValues[1] = new float[groupALength];
	}
	
	Vector groupedExptsVector  = new Vector();
	for (int i = 0; i < groupedExpts.length; i++) {
	    groupedExptsVector.add(new Integer(groupedExpts[i]));
	}
	
	//System.out.print("In randomly permute: random expts groupA: ");
	
	for (int i = 0; i < groupedValues[0].length; i++) {
	    //Random rand = new Random();
	    //int randInt = (int)Math.round(rand.nextDouble()*(groupedExptsVector.size()-1));
	    int randInt = (int)Math.round(Math.random()*(groupedExptsVector.size()-1));
	    int randIndex = ((Integer)groupedExptsVector.remove(randInt)).intValue();
	    //System.out.print(" " + randIndex);
	    groupedValues[0][i] = gene[randIndex];
	}
	
	//System.out.println();
	
	//System.out.print("In randomly permute: random expts groupB: ");
	
	for (int i = 0; i < groupedValues[1].length; i++) {
	    int index = ((Integer)groupedExptsVector.get(i)).intValue();
	    //System.out.print(" " + index);
	    groupedValues[1][i] = gene[index];
	}
	
	//System.out.println("\n");
	
	return groupedValues;
	
    }
    
    private float[][] randomlyPermute2(float[] gene, int[] groupedExpts, int groupALength, int groupBLength) {
	// System.out.print("In randomlyPermute: geneValues: ");
	/*
	for (int i = 0; i < gene.length; i++) {
	    System.out.print(" " + gene[i]);
	}
	System.out.println();
	 */
	
	float[][] groupedValues = new float[2][];
	groupedValues[0] = new float[groupALength];
	groupedValues[1] = new float[groupBLength];
	boolean[] assignedToGroupA = new boolean[groupedExpts.length];
	
	for (int i = 0; i < assignedToGroupA.length; i++) {
	    assignedToGroupA[i] = false;
	}
	
	
	
	int groupACounter = 0;
	int groupBCounter = 0;
	
	while (groupACounter < groupALength) {
	    Random rand = new Random();
	    int randInt = rand.nextInt(groupedExpts.length);
	    if (assignedToGroupA[randInt]) {
		continue;
	    } else {
		groupedValues[0][groupACounter] = gene[groupedExpts[randInt]];
		assignedToGroupA[randInt] = true;
		groupACounter++;
	    }
	}
	
	for (int i = 0; i < groupedExpts.length; i++) {
	    if (assignedToGroupA[i]) {
		continue;
	    } else {
		groupedValues[1][groupBCounter] = gene[groupedExpts[i]];
		groupBCounter++;
	    }
	}
	/*
	System.out.print("randomly permuted group A :");
	for (int i = 0; i < groupedValues[0].length; i++) {
	    System.out.print(" " + groupedValues[0][i]);
	}
	 
	System.out.println();
	 
	System.out.print("randomly permuted group B :");
	for (int i = 0; i < groupedValues[1].length; i++) {
	    System.out.print(" " + groupedValues[1][i]);
	}
	 
	System.out.println();
	 */
	
	return groupedValues;
    }
    
    private boolean belongsInArray(int i, int[] arr) {
	boolean belongs = false;
	
	for (int j = 0; j < arr.length; j++) {
	    if (i == arr[j]) {
		belongs = true;
		break;
	    }
	}
	
	return belongs;
    }
    
    private float getTValue(int gene) {
	
	float[] geneValues = new float[numExps];
	for (int i = 0; i < numExps; i++) {
	    geneValues[i] = expMatrix.A[gene][i];
	}
	
	int groupACounter = 0;
	int groupBCounter = 0;
	
	for (int i = 0; i < groupAssignments.length; i++) {
	    if (groupAssignments[i] == TtestInitDialog.GROUP_A) {
		groupACounter++;
	    } else if (groupAssignments[i] == TtestInitDialog.GROUP_B) {
		groupBCounter++;
	    }
	}
	
	float[] groupAValues = new float[groupACounter];
	float[] groupBValues = new float[groupBCounter];
	
	groupACounter = 0;
	groupBCounter = 0;
	
	for (int i = 0; i < groupAssignments.length; i++) {
	    if (groupAssignments[i] == TtestInitDialog.GROUP_A) {
		groupAValues[groupACounter] = geneValues[i];
		groupACounter++;
	    } else if (groupAssignments[i] == TtestInitDialog.GROUP_B) {
		groupBValues[groupBCounter] = geneValues[i];
		groupBCounter++;
	    }
	}
	
	float tValue = calculateTValue(groupAValues, groupBValues);
	return tValue;
    }
    
    private int getDF(int gene) {
	float[] geneValues = new float[numExps];
	for (int i = 0; i < numExps; i++) {
	    geneValues[i] = expMatrix.A[gene][i];
	}
	
	int groupACounter = 0;
	int groupBCounter = 0;
	
	for (int i = 0; i < groupAssignments.length; i++) {
	    if (groupAssignments[i] == TtestInitDialog.GROUP_A) {
		groupACounter++;
	    } else if (groupAssignments[i] == TtestInitDialog.GROUP_B) {
		groupBCounter++;
	    }
	}
	
	float[] groupAValues = new float[groupACounter];
	float[] groupBValues = new float[groupBCounter];
	
	groupACounter = 0;
	groupBCounter = 0;
	
	for (int i = 0; i < groupAssignments.length; i++) {
	    if (groupAssignments[i] == TtestInitDialog.GROUP_A) {
		groupAValues[groupACounter] = geneValues[i];
		groupACounter++;
	    } else if (groupAssignments[i] == TtestInitDialog.GROUP_B) {
		groupBValues[groupBCounter] = geneValues[i];
		groupBCounter++;
	    }
	}
	
	int df = calculateDf(groupAValues, groupBValues);
	
	return df;
    }
    
    private Vector getMeansAndSDs() {
        float[] meansA = new float[numGenes];
        float[] meansB = new float[numGenes];
        float[] sdA = new float[numGenes];
        float[] sdB = new float[numGenes];
        for (int i = 0; i < numGenes; i++) {
            float[] geneValues = new float[numExps];
            for (int j = 0; j < numExps; j++) {
                geneValues[j] = expMatrix.A[i][j];
            }
            
            int groupACounter = 0;
            int groupBCounter = 0;
            
            for (int j = 0; j < groupAssignments.length; j++) {
                if (groupAssignments[j] == TtestInitDialog.GROUP_A) {
                    groupACounter++;
                } else if (groupAssignments[j] == TtestInitDialog.GROUP_B) {
                    groupBCounter++;
                }
            }
            
            float[] groupAValues = new float[groupACounter];
            float[] groupBValues = new float[groupBCounter];
            
            groupACounter = 0;
            groupBCounter = 0;
            
            for (int j = 0; j < groupAssignments.length; j++) {
                if (groupAssignments[j] == TtestInitDialog.GROUP_A) {
                    groupAValues[groupACounter] = geneValues[j];
                    groupACounter++;
                } else if (groupAssignments[j] == TtestInitDialog.GROUP_B) {
                    groupBValues[groupBCounter] = geneValues[j];
                    groupBCounter++;
                }
            } 
            
            meansA[i] = getMean(groupAValues);
            meansB[i] = getMean(groupBValues);
            sdA[i] = (float)(Math.sqrt(getVar(groupAValues)));
            sdB[i] = (float)(Math.sqrt(getVar(groupBValues)));
        }
        
        Vector meansAndSDs = new Vector();
        meansAndSDs.add(meansA);
        meansAndSDs.add(meansB);
        meansAndSDs.add(sdA);
        meansAndSDs.add(sdB);
        
        return meansAndSDs;
    }
    
    private boolean isSignificant(int gene) {
	boolean sig = false;
	float[] geneValues = new float[numExps];
	for (int i = 0; i < numExps; i++) {
	    geneValues[i] = expMatrix.A[gene][i];
	}
	
	int groupACounter = 0;
	int groupBCounter = 0;
	
	for (int i = 0; i < groupAssignments.length; i++) {
	    if (groupAssignments[i] == TtestInitDialog.GROUP_A) {
		groupACounter++;
	    } else if (groupAssignments[i] == TtestInitDialog.GROUP_B) {
		groupBCounter++;
	    }
	}
	
	float[] groupAValues = new float[groupACounter];
	float[] groupBValues = new float[groupBCounter];
	
        int numbValidValuesA = 0;
        int numbValidValuesB = 0;        
        
	groupACounter = 0;
	groupBCounter = 0;
	
	for (int i = 0; i < groupAssignments.length; i++) {
	    if (groupAssignments[i] == TtestInitDialog.GROUP_A) {
		groupAValues[groupACounter] = geneValues[i];
                if (!Float.isNaN(geneValues[i])) {
                    numbValidValuesA++;
                }                
		groupACounter++;
	    } else if (groupAssignments[i] == TtestInitDialog.GROUP_B) {
		groupBValues[groupBCounter] = geneValues[i];
                if (!Float.isNaN(geneValues[i])) {
                    numbValidValuesB++;
                }                 
		groupBCounter++;
	    }
	}
        
        if ((numbValidValuesA < 2) || (numbValidValuesB < 2)) {
            currentP = Float.NaN;
            currentT = Float.NaN;
            return false;
        }        
	
	float tValue = calculateTValue(groupAValues, groupBValues);
        currentT = tValue;
	int df = calculateDf(groupAValues, groupBValues);
	double prob;
	
	if (!isPermut) {
	    if((Float.isNaN(tValue)) || (Float.isNaN((new Integer(df)).floatValue())) || (df <= 0)) {
		sig = false;
                currentP = Float.NaN;
	    } else {
		TDistribution tDist = new TDistribution(df);
                double cumulP = tDist.cumulative(tValue);
                prob = 2*(1 - cumulP); // two-tailed test
                if (prob > 1) {
                    prob = 1;
                }                
		//prob = tDist.probability(tValue);
                currentP = prob;
		
		if (significanceMethod == TtestInitDialog.JUST_ALPHA) {
		    if (prob <= alpha) {
			sig = true;
		    } else {
			sig = false;
		    }
		} else if (significanceMethod == TtestInitDialog.STD_BONFERRONI) {
		    double thresh = alpha/(double)numGenes;
		    if (prob <= thresh) {
			sig = true;
		    } else {
			sig = false;
		    }
		    
		}
	    }
	}
	
	
	return sig;
	
    }
    
    private float calculateTValue(float[] groupA, float[] groupB) {
	int kA = groupA.length;
	int kB = groupB.length;
	float meanA = getMean(groupA);
	float meanB = getMean(groupB);
	float varA = getVar(groupA);
	float varB = getVar(groupB);
        
        int numbValidGroupAValues = 0; 
        int numbValidGroupBValues = 0;
        
        for (int i = 0; i < groupA.length; i++) {
            if (!Float.isNaN(groupA[i])) {
                numbValidGroupAValues++;
            }
        }
        
        for (int i = 0; i < groupB.length; i++) {
            if (!Float.isNaN(groupB[i])) {
                numbValidGroupBValues++;
            }
        } 
        
        if ((numbValidGroupAValues < 2) || (numbValidGroupBValues < 2)) {
            return Float.NaN;
        }
	
	float tValue = (float)((meanA - meanB) / Math.sqrt((varA/kA) + (varB/kB)));
	
        /*
	if (Float.isNaN(tValue)) {
	    tValue = 0;
	}*/
        
        
	
	return Math.abs(tValue);
    }
    
    private int calculateDf(float[] groupA, float[] groupB) {
	int kA = 0;
	int kB = 0;
	for (int i =0; i < groupA.length; i++) {
	    if (!Float.isNaN(groupA[i])) {
		kA++;
	    }
	}
	
	for (int i =0; i < groupB.length; i++) {
	    if (!Float.isNaN(groupB[i])) {
		kB++;
	    }
	}
	
	float meanA = getMean(groupA);
	float meanB = getMean(groupB);
	float varA = getVar(groupA);
	float varB = getVar(groupB);
	/*
	System.out.println("kA = " +kA);
	System.out.println("kB = " +kB);
	System.out.println("meanA = " +meanA);
	System.out.println("meanB = " +meanB);
	System.out.println("varA = " +varA);
	System.out.println("varB = " +varB);
	 */
	float numerator = (float) (Math.pow(((varA/kA) + (varB/kB)), 2));
	//System.out.println("numerator = " + numerator);
	float denom = (float)((Math.pow((varA/kA), 2)/(kA - 1)) + (Math.pow((varB/kB), 2)/(kB - 1)));
	//System.out.println("denominator = " + denom);
	
	//System.out.print(".. df(unrounded) = " +  (numerator / denom) + " ... ");
	
	int df = Math.round(numerator / denom);
	
	return df;
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
    
    private float getVar(float[] group) {
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
	return var;
    }
    
}
