/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: OneWayANOVA.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-11-07 17:27:39 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.util.Random;
import java.util.Vector;

import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.gui.impl.owa.OneWayANOVAInitBox;
import org.tigr.util.FloatMatrix;
import org.tigr.util.QSort;

import JSci.maths.statistics.FDistribution;

/**
 *
 * @author  nbhagaba
 * @version 
 */
public class OneWayANOVA extends AbstractAlgorithm {
    
    public static final int FALSE_NUM = 12;
    public static final int FALSE_PROP = 13;    
    
    private boolean stop = false;
    
    private int function;
    private float factor;
    private boolean absolute, calculateAdjFDRPVals;
    private FloatMatrix expMatrix;
    
    private Vector[] clusters;
    private int k; // # of clusters
    
    private int numGenes, numExps, numGroups;
    private float alpha, falseProp;
    private boolean usePerms, drawSigTreesOnly;
    private int numPerms, falseNum;
    private int correctionMethod;    
    int[] groupAssignments; 
    private double[] origPVals;
    
    float currentP = 0.0f;
    //float currentF = 0.0f;
    int currentIndex = 0; 
    double constant;
    
    AlgorithmEvent event;
    
    Vector fValuesVector = new Vector();
    Vector rawPValuesVector = new Vector();
    Vector adjPValuesVector = new Vector();
    //Vector pValuesVector = new Vector(); 
    Vector dfNumVector = new Vector();
    Vector dfDenomVector = new Vector();
    Vector ssGroupsVector = new Vector();
    Vector ssErrorVector = new Vector();
    private boolean[] isSig;
    
    private int hcl_function;
    private boolean hcl_absolute;
    
    private boolean useFastFDRApprox = true;
    
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
        usePerms = map.getBoolean("usePerms", false);
        numPerms = map.getInt("numPerms", 0);

        hcl_function = map.getInt("hcl-distance-function", EUCLIDEAN);
        hcl_absolute = map.getBoolean("hcl-distance-absolute", false);        
        
	boolean hierarchical_tree = map.getBoolean("hierarchical-tree", false);
        if (hierarchical_tree) {
            drawSigTreesOnly = map.getBoolean("draw-sig-trees-only");
        }        
	int method_linkage = map.getInt("method-linkage", 0);
	boolean calculate_genes = map.getBoolean("calculate-genes", false);
	boolean calculate_experiments = map.getBoolean("calculate-experiments", false);
	
	this.expMatrix = data.getMatrix("experiment");
	
	numGenes = this.expMatrix.getRowDimension();
	numExps = this.expMatrix.getColumnDimension();
	alpha = map.getFloat("alpha", 0.01f);
	correctionMethod = map.getInt("correction-method", OneWayANOVAInitBox.JUST_ALPHA);        
        numGroups = map.getInt("numGroups", 3);
        
        calculateAdjFDRPVals = false;
        
        if (correctionMethod == FALSE_NUM) {
            falseNum = map.getInt("falseNum", 10);
        }
        
        if (correctionMethod == FALSE_PROP) {
            falseProp = map.getFloat("falseProp", 0.05f);
        }        
        
        getFDfSSValues();
        if ((correctionMethod == FALSE_NUM) || (correctionMethod == FALSE_PROP)) {
            rawPValuesVector = getRawPValuesFromFDist();
            origPVals = new double[rawPValuesVector.size()];
            for (int i = 0; i < origPVals.length; i++) {
                origPVals[i] = ((Float)(rawPValuesVector.get(i))).doubleValue();
            }
            adjPValuesVector = new Vector();
            for (int i = 0; i < origPVals.length; i++) {
                adjPValuesVector.add(new Float(0f));
            }
        }  else {        
            if (!usePerms) {
                rawPValuesVector = getRawPValuesFromFDist();
            } else {
                rawPValuesVector = getRawPValsFromPerms();
            }
            
        }
        
        
        //Vector clusterVector = sortGenesBySignificance();
        Vector clusterVector = new Vector();
        Vector sigGenes = new Vector();
        Vector nonSigGenes = new Vector();
        
        if ( (correctionMethod == OneWayANOVA.FALSE_NUM) || (correctionMethod == OneWayANOVA.FALSE_PROP) ) {
            boolean[] isGeneSig = new boolean[1];
            if (correctionMethod == OneWayANOVA.FALSE_NUM) {
                isGeneSig = isGeneSigByFDRNum();
            } else {
                isGeneSig = isGeneSigByFDRPropNew2();
            }
            /*            
            if (!calculateAdjFDRPVals) {
                adjustedPVals = new double[numGenes];
            }
             */
            //System.out.println("isGeneSig.length = " + isGeneSig.length);
            for (int i = 0; i < numGenes; i++) {
                if (isGeneSig[i]) {
                    sigGenes.add(new Integer(i));
                } else {
                    nonSigGenes.add(new Integer(i));
                }
            }
            
        }  else {      
            
            adjPValuesVector = getAdjPVals(rawPValuesVector, correctionMethod);
            event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numGenes);
            fireValueChanged(event);
            event.setId(AlgorithmEvent.PROGRESS_VALUE);
            
            for (int i = 0; i < numGenes; i++) {
                if (stop) {
                    throw new AbortException();
                }
                event.setIntValue(i);
                event.setDescription("Finding significant genes: Current gene = " + (i + 1));
                fireValueChanged(event);
                
                float currAdjP = ((Float)(adjPValuesVector.get(i))).floatValue();
                //System.out.println("currAdjP: gene" + i + " = " + currAdjP);
                if (correctionMethod == OneWayANOVAInitBox.ADJ_BONFERRONI) {// because we have to break out of the loop if a non-sig value is encountered
                    if (isSig[i]) {
                        sigGenes.add(new Integer(i));
                    } else {
                        nonSigGenes.add(new Integer(i));
                    }
                } else {
                    if (currAdjP <= alpha) {
                        sigGenes.add(new Integer(i));
                    } else {
                        nonSigGenes.add(new Integer(i));
                    }
                }
            }
        }
        
        clusterVector.add(sigGenes);
        clusterVector.add(nonSigGenes);
        k = clusterVector.size();

        FloatMatrix fValuesMatrix = new FloatMatrix(fValuesVector.size(), 1);  
        //FloatMatrix pValuesMatrix = new FloatMatrix(pValuesVector.size(), 1);   
        FloatMatrix rawPValuesMatrix = new FloatMatrix(rawPValuesVector.size(), 1);
        FloatMatrix adjPValuesMatrix = new FloatMatrix(adjPValuesVector.size(), 1);
        FloatMatrix dfNumMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix dfDenomMatrix = new FloatMatrix(numGenes, 1);

        for (int i = 0; i < fValuesVector.size(); i++) {
            fValuesMatrix.A[i][0] = ((Float)(fValuesVector.get(i))).floatValue();
        }  
        
        for (int i = 0; i < rawPValuesVector.size(); i++) {
            rawPValuesMatrix.A[i][0] = ((Float)(rawPValuesVector.get(i))).floatValue();
            adjPValuesMatrix.A[i][0] = ((Float)(adjPValuesVector.get(i))).floatValue();
        } 
        
        for (int i = 0; i < numGenes; i++) {
            //dfNumMatrix.A[i][0] = (float)(getDfNum(i));
            //dfDenomMatrix.A[i][0] = (float)(getDfDenom(i));            
            dfNumMatrix.A[i][0] = ((Integer)(dfNumVector.get(i))).floatValue();
            dfDenomMatrix.A[i][0] = ((Integer)(dfDenomVector.get(i))).floatValue();
            if (dfNumMatrix.A[i][0] <= 0) dfNumMatrix.A[i][0] = Float.NaN;
            if (dfDenomMatrix.A[i][0] <= 0) dfDenomMatrix.A[i][0] = Float.NaN;
        }
        
        FloatMatrix ssGroupsMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix ssErrorMatrix = new FloatMatrix(numGenes, 1);
        
        for (int i = 0; i < ssGroupsMatrix.getRowDimension(); i++) {
            //float[] currentGene = getGene(i);
            //constant = getConstant(currentGene);
            /*
            if (i == 11) {
                System.out.println("Gene A3: groupsSS = " + getGroupsSS(currentGene));
            }
             */
            ssGroupsMatrix.A[i][0] = ((Double)(ssGroupsVector.get(i))).floatValue();//(float)(getGroupsSS(currentGene));
            /*
            if (i == 11) {
                System.out.println("Gene A3: ssGroupsMatrix.A[11][0] = " + ssGroupsMatrix.A[i][0]);
            }
             */
            ssErrorMatrix.A[i][0] = ((Double)(ssErrorVector.get(i))).floatValue();//(float)(getTotalSS(currentGene) - getGroupsSS(currentGene));
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
                if (drawSigTreesOnly) {
                    if (i == 0) {
                        node.setValues(calculateHierarchicalTree(features, method_linkage, calculate_genes, calculate_experiments));
                        event.setIntValue(i+1);
                        fireValueChanged(event);                       
                    }                    
                } else {                
                    node.setValues(calculateHierarchicalTree(features, method_linkage, calculate_genes, calculate_experiments));
                    event.setIntValue(i+1);
                    fireValueChanged(event);
                }                
	    }
	}
        
	// prepare the result
	AlgorithmData result = new AlgorithmData();
	result.addCluster("cluster", result_cluster);
	result.addParam("number-of-clusters", String.valueOf(clusters.length));    
	result.addMatrix("clusters_means", means);
	result.addMatrix("clusters_variances", variances); 
        //result.addMatrix("pValues", pValuesMatrix);
        result.addMatrix("rawPValues", rawPValuesMatrix);
        result.addMatrix("adjPValues", adjPValuesMatrix);
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
        data.addParam("hcl-distance-function", String.valueOf(this.hcl_function));
        data.addParam("hcl-distance-absolute", String.valueOf(this.hcl_absolute));
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
    
    private boolean[] isGeneSigByFDRPropNew2() throws AlgorithmException {
        double[] nonNanPVals = new double[origPVals.length];
        for (int i = 0; i < origPVals.length; i++) { //gets rid of NaN's for sorting
            if (Double.isNaN(origPVals[i])) {
                nonNanPVals[i] = Double.POSITIVE_INFINITY;
            } else {
                nonNanPVals[i] = origPVals[i];
            }
        } 
        
        QSort sortOrigPVals = new QSort(nonNanPVals, QSort.ASCENDING);
        double[] sortedOrigPVals = sortOrigPVals.getSortedDouble();
        int[] sortedIndices = sortOrigPVals.getOrigIndx();  
        boolean[] isGeneSig = new boolean[numGenes];
        for (int i = 0; i < isGeneSig.length; i++) {
            isGeneSig[i] = false;
        } 
        
        double[] yKArray = getYKArray();
        
        if (sortedOrigPVals[0] >= yKArray[0]) {
            return isGeneSig;
            
        } else {
            isGeneSig[sortedIndices[0]] = true;
            if (useFastFDRApprox) {
                
                for (int i = 1; i < sortedOrigPVals.length; i++) {
                    int rGamma = (int)(Math.floor((i+1)*falseProp));
                    int rMinusOneGamma = (int)(Math.floor(i*falseProp));
                    double yKRGamma = yKArray[rGamma];
                    //System.out.println("rGamma = " + rGamma + ", (r - 1)Gamma = " + rMinusOneGamma + ", yKRGamma = " + yKRGamma);
                    if ((rGamma > rMinusOneGamma) || (sortedOrigPVals[i] < yKRGamma)) {
                       isGeneSig[sortedIndices[i]] = true; 
                    } else {
                        break;
                    }
                }
                
            }
        }
        
        return isGeneSig;
    }    
    
    private double[] getYKArray() throws AlgorithmException {
        AlgorithmEvent event2 = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numPerms);
        fireValueChanged(event2);
        event2.setId(AlgorithmEvent.PROGRESS_VALUE);  
        int maxRGamma = (int)(Math.floor(numGenes*falseProp));
        double[][] pValArray =new double[maxRGamma + 1][numPerms];     
        
        Vector validExpts = new Vector();
        for (int i = 0; i < groupAssignments.length; i++) {
            if (groupAssignments[i] != 0) validExpts.add(new Integer(i));
        }
        
        int[] validArray = new int[validExpts.size()];
        for (int j = 0; j < validArray.length; j++) {
            validArray[j] = ((Integer)(validExpts.get(j))).intValue();
        }          
        
        for (int i = 0; i < numPerms; i++) {
            if (stop) {
                throw new AbortException();
            }
            event2.setIntValue(i);
            event2.setDescription("Permuting matrix: Current permutation = " + (i + 1));
            fireValueChanged(event2);            
            int[] permutedExpts = getPermutedValues(numExps, validArray);
            FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
            float[] currPermFVals = getPermutedFVals(permutedMatrix);
            int[][] dfs = getDfs(permutedMatrix);
            double[] currPermPVals = getParametricPVals(currPermFVals, dfs[0], dfs[1]);
            
            for (int j = 0; j < currPermPVals.length; j++) {
                if (Double.isNaN(currPermPVals[j])) currPermPVals[j] = Double.POSITIVE_INFINITY;
                // this is to push NaNs to the end of the sorted array, since otherwise they would mess up quantile calculations
            }
            
            QSort sortCurrPVals = new QSort(currPermPVals, QSort.ASCENDING);
            double[] sortedCurrPVals = sortCurrPVals.getSortedDouble();
            //uPlusOneSmallestPVector.add(new Double(sortedCurrPVals[u]));    
            for (int j = 0; j < pValArray.length; j++) {
                pValArray[j][i] = sortedCurrPVals[j];
            }         
        }  
        
        double[] yKArray = new double[pValArray.length];
        
        for (int i = 0; i < pValArray.length; i++) {
            double[] currRow = new double[pValArray[i].length];
            
            for (int j = 0; j < currRow.length; j++) {
                currRow[j] = pValArray[i][j];
            }
            
            for (int j = 0; j < currRow.length; j++) {
                if (Double.isNaN(currRow[j])) currRow[j] = Double.POSITIVE_INFINITY;
                // this is to push NaNs to the end of the sorted array, since otherwise they would mess up quantile calculations 
            }
            
            QSort sortCurrRow = new QSort(currRow, QSort.ASCENDING);
            double[] sortedCurrRow = sortCurrRow.getSortedDouble();
            int selectedIndex = (int)Math.floor(sortedCurrRow.length*alpha) - 1;
            if (selectedIndex < 0) selectedIndex = 0;
            yKArray[i] = sortedCurrRow[selectedIndex]; 
            //System.out.println("i= " + i + ", selectedIndex = " + selectedIndex + ", yKArray[i] = " + yKArray[i]);
        }
        
        return yKArray;        
    }
    
    private boolean[] isGeneSigByFDRNum() throws AlgorithmException {
        double[] nonNanPVals = new double[origPVals.length];
        for (int i = 0; i < origPVals.length; i++) { //gets rid of NaN's for sorting
            if (Double.isNaN(origPVals[i])) {
                nonNanPVals[i] = Double.POSITIVE_INFINITY;
            } else {
                nonNanPVals[i] = origPVals[i];
            }
        }
        QSort sortOrigPVals = new QSort(nonNanPVals, QSort.ASCENDING);
        double[] sortedOrigPVals = sortOrigPVals.getSortedDouble();
        int[] sortedIndices = sortOrigPVals.getOrigIndx();        
        boolean[] isGeneSig = new boolean[numGenes];
        for (int i = 0; i < isGeneSig.length; i++) {
            isGeneSig[i] = false;
        }
        for (int i = 0; i < falseNum; i++) {
            isGeneSig[sortedIndices[i]] = true;          
        }
        if (useFastFDRApprox) {
            double yK = getYConservative(alpha, falseNum); 
            //System.out.println("yK = " + yK);
            for (int i = falseNum; i < sortedOrigPVals.length; i++) {
                if (sortedOrigPVals[i] < yK) {
                    isGeneSig[sortedIndices[i]] = true;
                } else {
                    break;
                }
            }
            
        } else {// if (!useFastFDRAprpox)
            for (int i = falseNum; i < origPVals.length; i++) {
                
            }
        }
        //return null; //for now
        return isGeneSig;
    }    
    

    private double getYConservative(double alphaQuantile, int u) throws AlgorithmException {
        AlgorithmEvent event2 = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numPerms);
        fireValueChanged(event2);
        event2.setId(AlgorithmEvent.PROGRESS_VALUE);
        Vector uPlusOneSmallestPVector = new Vector(); 
        
        Vector validExpts = new Vector();
        for (int i = 0; i < groupAssignments.length; i++) {
            if (groupAssignments[i] != 0) validExpts.add(new Integer(i));
        }
        
        int[] validArray = new int[validExpts.size()];
        for (int j = 0; j < validArray.length; j++) {
            validArray[j] = ((Integer)(validExpts.get(j))).intValue();
        }        
        for (int i = 0; i < numPerms; i++) {
            if (stop) {
                throw new AbortException();
            }
            event.setIntValue(i);
            event.setDescription("Permuting matrix: Current permutation = " + (i + 1));
            fireValueChanged(event);            
            int[] permutedExpts = getPermutedValues(numExps, validArray);
            FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
            float[] currPermFVals = getPermutedFVals(permutedMatrix);
            int[][] dfs = getDfs(permutedMatrix);
            double[] currPermPVals = getParametricPVals(currPermFVals, dfs[0], dfs[1]);
            
            for (int j = 0; j < currPermPVals.length; j++) {
                if (Double.isNaN(currPermPVals[j])) currPermPVals[j] = Double.POSITIVE_INFINITY;
                // this is to push NaNs to the end of the sorted array, since otherwise they would mess up quantile calculations
            }
            
            QSort sortCurrPVals = new QSort(currPermPVals, QSort.ASCENDING);
            double[] sortedCurrPVals = sortCurrPVals.getSortedDouble();
            uPlusOneSmallestPVector.add(new Double(sortedCurrPVals[u]));           
        }
        
        double[] uPlusOneSmallestArray = new double[uPlusOneSmallestPVector.size()];
        for(int i = 0; i < uPlusOneSmallestPVector.size(); i++) {
            uPlusOneSmallestArray[i] = ((Double)(uPlusOneSmallestPVector.get(i))).doubleValue();
        }
        
        QSort sortUPlusOneArray = new QSort(uPlusOneSmallestArray, QSort.ASCENDING);
        double[] sortedUPlusOneArray = sortUPlusOneArray.getSortedDouble();
        
        int selectedIndex = (int)Math.floor(sortedUPlusOneArray.length*alphaQuantile) - 1;
        //System.out.println("Selected index (before setting to zero) = " + selectedIndex);
        if (selectedIndex < 0) selectedIndex = 0;        
        
        return sortedUPlusOneArray[selectedIndex];        
        
        //return 0d; //for now;
    }
    
    private int[][] getDfs(FloatMatrix permutedMatrix) {
        int[][] dfs = new int[2][numGenes];
        for (int gene = 0; gene < numGenes; gene++) {
            dfs[0][gene] = getDfNum(gene, permutedMatrix); 
            dfs[1][gene] = getDfDenom(gene, permutedMatrix);
        }
        return dfs;
    }
    
    private double[] getParametricPVals(float[] fVals, int[] dfNums, int[] dfDenoms) {
        double[] pVals = new double[numGenes];
        for (int i = 0; i < numGenes; i++) {
            double currF = (double)fVals[i];
            int currDfNum = dfNums[i];
            int currDfDenom = dfDenoms[i];
            if (Double.isNaN(currF) || (currDfNum <= 0) || (currDfDenom <= 0) ) {
                pVals[i] = Double.NaN;
            } else {
                FDistribution fDist = new FDistribution(currDfNum, currDfDenom);     
                //System.out.println("i (gene) = " + i + ", currF = " + currF + ", currDfNum = " + currDfNum + ", currDfDenom = " + currDfDenom);
                double cumulProb = fDist.cumulative(currF);
                double pValue = 1 - cumulProb; //                
                if (pValue > 1) {
                    pValue = 1.0d;
                }   
                pVals[i] = pValue;
            }
        }
        return pVals;
    }
    
    private void getFDfSSValues() throws AlgorithmException {
        event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numGenes);
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);   
        
        for (int gene = 0; gene < numGenes; gene++) {
            float currentF = 0.0f;
            if (stop) {
                throw new AbortException();
            }
            event.setIntValue(gene);
            event.setDescription("Calculating F and df: Current gene = " + (gene + 1));
            fireValueChanged(event);            
            //boolean sig = false;
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
                //currentP = Float.NaN;
                //return false;
            }
            
            constant = getConstant(geneValues);
            
            double totalSS = getTotalSS(geneValues);
            double groupsSS = getGroupsSS(geneValues);
            double errorSS = totalSS - groupsSS;
            
            if ((Double.isNaN(totalSS))||(Double.isNaN(groupsSS))||(Double.isNaN(errorSS))) {
                currentF = Float.NaN;
                //currentP = Float.NaN;
                //return false;
            }
            
            //int totalDF = validN - 1;
            int groupsDF = getDfNum(gene);
            int errorDF = getDfDenom(gene);
            
            double groupsMS = groupsSS / groupsDF;
            double errorMS = errorSS / errorDF;
                        
            if (!Float.isNaN(currentF)) {
                double fValue = groupsMS/errorMS;
                currentF = (float)(fValue);
            }
            
            if (Float.isInfinite(currentF)) {
                currentF = Float.NaN;
            }
            //System.out.println("currentF for gene " + gene + " = " + currentF);
            
            fValuesVector.add(new Float(currentF));
            dfNumVector.add(new Integer(groupsDF));
            dfDenomVector.add(new Integer(errorDF));
            ssGroupsVector.add(new Double(groupsSS));
            ssErrorVector.add(new Double(errorSS));
        }
    }
    
    private Vector getRawPValuesFromFDist() {
        Vector rawPVals = new Vector();
        for (int i = 0; i < numGenes; i++) {
            double currF = ((Float)(fValuesVector.get(i))).doubleValue();
            int currDfNum = ((Integer)(dfNumVector.get(i))).intValue(); 
            int currDfDenom = ((Integer)(dfDenomVector.get(i))).intValue();
            
            if (Double.isNaN(currF) || (currDfNum <= 0) || (currDfDenom <= 0) ) {
                rawPVals.add(new Float(Float.NaN));
            } else {
                FDistribution fDist = new FDistribution(currDfNum, currDfDenom);     
                //System.out.println("i (gene) = " + i + ", currF = " + currF + ", currDfNum = " + currDfNum + ", currDfDenom = " + currDfDenom);
                double cumulProb = fDist.cumulative(currF);
                double pValue = 1 - cumulProb; //                
                if (pValue > 1) {
                    pValue = 1.0d;
                }                
                rawPVals.add(new Float(pValue));                
            }
        }
        return rawPVals;
    }
    
    private Vector getRawPValsFromPerms() throws AlgorithmException {
        event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numPerms);
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);
        
        float[] permPValues = new float[numGenes];
        for (int i = 0; i < numGenes; i++) {
            permPValues[i] = 0f;
        }
        float[] origFValues = new float[fValuesVector.size()];
        for (int i = 0; i < fValuesVector.size(); i++) {
            origFValues[i] = ((Float)(fValuesVector.get(i))).floatValue();
        }
        
        Vector validExpts = new Vector();
        for (int i = 0; i < groupAssignments.length; i++) {
            if (groupAssignments[i] != 0) validExpts.add(new Integer(i));
        }
        
        int[] validArray = new int[validExpts.size()];
        for (int j = 0; j < validArray.length; j++) {
            validArray[j] = ((Integer)(validExpts.get(j))).intValue();
        }  
        for (int i = 0; i < numPerms; i++) {
            if (stop) {
                throw new AbortException();
            }
            event.setIntValue(i);
            event.setDescription("Permuting matrix: Current permutation = " + (i + 1));
            fireValueChanged(event);            
            int[] permutedExpts = getPermutedValues(numExps, validArray);
            FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
            float[] currPermFVals = getPermutedFVals(permutedMatrix);
            for (int j = 0; j < numGenes; j++) {
                if (currPermFVals[j] > origFValues[j]) permPValues[j]++;
            }
        }
        
        for (int i = 0; i < numGenes; i++) {
            if (Float.isNaN(origFValues[i])) {
                permPValues[i] = Float.NaN;
            } else {
                permPValues[i] = (float)permPValues[i]/(float)numPerms;
            }
        }
        
        Vector permPValsVector = new Vector();
        
        for (int i = 0; i < permPValues.length; i++) {
            permPValsVector.add(new Float(permPValues[i]));
        }
        
        return permPValsVector;
    }
    
    private FloatMatrix getPermutedMatrix(FloatMatrix inputMatrix, int[] permExpts) {
        FloatMatrix permutedMatrix = new FloatMatrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());
        for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
            for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                permutedMatrix.A[i][j] = inputMatrix.A[i][permExpts[j]];
            }
        }
        return permutedMatrix;
    }    
    
    private float[] getPermutedFVals(FloatMatrix permMatrix) {        
        float[] permFVals  = new float[numGenes];
        
        for (int gene = 0; gene < numGenes; gene++) {
            float currentF = 0.0f;
            //boolean sig = false;
            float[] geneValues = new float[numExps];
            int n = 0;
            for (int i = 0; i < numExps; i++) {
                geneValues[i] = permMatrix.A[gene][i];
                if (!Float.isNaN(geneValues[i])) {
                    n++;
                }
            }
            
            if (n == 0) {
                currentF = Float.NaN;
                //currentP = Float.NaN;
                //return false;
            }
            
            constant = getConstant(geneValues);
            
            double totalSS = getTotalSS(geneValues);
            double groupsSS = getGroupsSS(geneValues);
            double errorSS = totalSS - groupsSS;
            
            if ((Double.isNaN(totalSS))||(Double.isNaN(groupsSS))||(Double.isNaN(errorSS))) {
                currentF = Float.NaN;
                //currentP = Float.NaN;
                //return false;
            } 
            
            int groupsDF = getDfNum(gene, permMatrix);
            int errorDF = getDfDenom(gene, permMatrix);            
            
            double groupsMS = groupsSS / groupsDF;
            double errorMS = errorSS / errorDF;
            
            if (!Float.isNaN(currentF)) {
                double fValue = groupsMS/errorMS;
                currentF = (float)(fValue);
            }
            
            //System.out.println("currentF for gene " + gene + " = " + currentF);
            permFVals[gene] = currentF;
        }  
        return permFVals;
    }
    
    private int[] getPermutedValues(int arrayLength, int[] validArray) {//returns an integer array of length "arrayLength", with the valid values (the currently included experiments) permuted
        int[] permutedValues = new int[arrayLength];
        for (int i = 0; i < permutedValues.length; i++) {
            permutedValues[i] = i;
        }
        
        int[] permutedValidArray = new int[validArray.length];
        for (int i = 0; i < validArray.length; i++) {
            permutedValidArray[i] = validArray[i];
        }
        
        for (int i = permutedValidArray.length; i > 1; i--) {
            Random generator2 =new Random();
            //Random generator2 = new Random(randomSeeds[i - 2]);
            int randVal = generator2.nextInt(i - 1);
            int temp = permutedValidArray[randVal];
            permutedValidArray[randVal] = permutedValidArray[i - 1];
            permutedValidArray[i - 1] = temp;
        }  
        
        for (int i = 0; i < validArray.length; i++) {
            //permutedValues[validArray[i]] = permutedValues[permutedValidArray[i]];
            permutedValues[validArray[i]] = permutedValidArray[i];
        }
        
        try {
            Thread.sleep(10);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        
        
        return permutedValues;        
    }    
    
    private Vector getAdjPVals(Vector rawPVals, int adjMethod) throws AlgorithmException {
        event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numGenes);
	fireValueChanged(event);
	event.setId(AlgorithmEvent.PROGRESS_VALUE); 
        
        Vector adjPVals = new Vector();
        if (adjMethod == OneWayANOVAInitBox.JUST_ALPHA) {
            adjPVals = (Vector)(rawPVals.clone());
        } 
        if (adjMethod == OneWayANOVAInitBox.STD_BONFERRONI) {
            for (int i = 0; i < numGenes; i++) {
		if (stop) {
		    throw new AbortException();
		}
		event.setIntValue(i);
		event.setDescription("Computing adjusted p-values: Current gene = " + (i + 1));  
		fireValueChanged(event);  
                float currP = ((Float)(rawPVals.get(i))).floatValue();
                float currAdjP = (float)(currP*numGenes);
                if (currAdjP > 1.0f) currAdjP = 1.0f;
                adjPVals.add(new Float(currAdjP));
            }
        }
        if (adjMethod == OneWayANOVAInitBox.ADJ_BONFERRONI) {
            adjPVals = getAdjBonfPVals(rawPVals);
        }
        if (adjMethod == OneWayANOVAInitBox.MAX_T) {
            adjPVals = getMaxTPVals();
        }
        
        return adjPVals;
    }
    
    private Vector getMaxTPVals() throws AlgorithmException {
        double[] origFValues = new double[numGenes];
        double[] descFValues = new double[numGenes];
        int[] descGeneIndices = new int[numGenes];
        double[] adjPValues = new double[numGenes];
        double[][] permutedRankedFValues = new double[numPerms][numGenes];
        double[][] uMatrix = new double[numGenes][numPerms];     
        
        event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numPerms);
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);    
        
        for (int i = 0; i < numGenes; i++) {
            origFValues[i] = ((Float)(fValuesVector.get(i))).doubleValue();
        }
        
        QSort sortDescFValues = new QSort(origFValues, QSort.DESCENDING);
        descFValues = sortDescFValues.getSortedDouble();
        descGeneIndices = sortDescFValues.getOrigIndx(); 
        
        Vector validExpts = new Vector();
        for (int i = 0; i < groupAssignments.length; i++) {
            if (groupAssignments[i] != 0) validExpts.add(new Integer(i));
        }
        
        int[] validArray = new int[validExpts.size()];
        for (int j = 0; j < validArray.length; j++) {
            validArray[j] = ((Integer)(validExpts.get(j))).intValue();
        }        
        
        for (int i = 0; i < numPerms; i++) {
            if (stop) {
                throw new AbortException();
            }
            event.setIntValue(i);
            event.setDescription("Permuting matrix: Current permutation = " + (i+1));
            fireValueChanged(event);   
            
            int[] permutedExpts = getPermutedValues(numExps, validArray);
            FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
            float[] currPermFVals = getPermutedFVals(permutedMatrix);  
            
            if (Double.isNaN(currPermFVals[descGeneIndices[numGenes - 1]])) {
                uMatrix[numGenes - 1][i] = Double.NEGATIVE_INFINITY;
            } else {
                uMatrix[numGenes - 1][i] = currPermFVals[descGeneIndices[numGenes - 1]];
            }   
            
            for (int j = numGenes - 2; j >= 0; j--) {
                if (Double.isNaN(currPermFVals[descGeneIndices[j]])) {
                    uMatrix[j][i] = uMatrix[j+1][i];
                } else {
                    uMatrix[j][i] = Math.max(uMatrix[j+1][i], currPermFVals[descGeneIndices[j]]);
                }
                //System.out.println("uMatrix[" + j + "][" + i + "] = " + uMatrix[j][i]);
            }            
            
        }
        
        for (int i = 0; i < numGenes; i++) {
            int pCounter = 0;
            for (int j = 0; j < numPerms; j++) {

                if (uMatrix[i][j] >= descFValues[i]) {
                    pCounter++;
                }
            }
            adjPValues[descGeneIndices[i]] = (double)pCounter/(double)numPerms;
        }  
        
        int NaNPCounter = 0;
        for (int i = 0; i < numGenes; i++) {
            if (Double.isNaN(origFValues[i])) {
                adjPValues[i] = Double.NaN;
                NaNPCounter++;
                //System.out.println("NaN index = " + i);
            }             
        } 
        //double[] pStarValues = new double[adjPValues];
        //pStartValues[descGeneIndices[0]]
        for (int i = 1; i < numGenes - NaNPCounter; i++) { // enforcing monotonicity
            adjPValues[descGeneIndices[i]] = Math.max(adjPValues[descGeneIndices[i]], adjPValues[descGeneIndices[i - 1]]); 
        }        
        
        Vector adPVector = new Vector();
        
        for (int i = 0; i < adjPValues.length; i++) {
            adPVector.add (new Float(adjPValues[i]));
        }
        return adPVector;
    }
    
    
    private Vector getAdjBonfPVals(Vector rawPVals) {
        float[] rawPValArray = new float[rawPVals.size()];
        isSig = new boolean[rawPValArray.length];
        for (int i = 0; i < isSig.length; i++) {
            isSig[i] = false;
        }        
        for (int i = 0; i < rawPValArray.length; i++) {
            rawPValArray[i] = ((Float)(rawPVals.get(i))).floatValue();
        }
        float[] adjPValArray = new float[rawPValArray.length];
        
        QSort sortRawPs = new QSort(rawPValArray, QSort.ASCENDING);
        float[] sortedRawPVals = sortRawPs.getSorted();
        int[] origIndices = sortRawPs.getOrigIndx();
        int n = numGenes;
        adjPValArray[origIndices[0]] = (float)(sortedRawPVals[0]*n);        
        for (int i = 1; i < numGenes; i++) {
            if (sortedRawPVals[i - 1] < sortedRawPVals[i]) n--;   
            if (n <= 0) n = 1;
            adjPValArray[origIndices[i]] = (float)(sortedRawPVals[i]*n);
        }
        
        for (int i = 0; i < adjPValArray.length; i++) {
            if (adjPValArray[i] > 1.0f) adjPValArray[i] = 1.0f;        
        }
        
        for (int i = 0; i < origIndices.length; i++) {// break out of loop as soon as non-significant value is encountered 
            if (adjPValArray[origIndices[i]] > (double)alpha) {
                break;
            } else {
                if (adjPValArray[origIndices[i]] <= (double)alpha) {
                    isSig[origIndices[i]] = true;
                }
            }
        }        
        
        Vector adjBonPVals = new Vector();
        for (int i = 0; i < adjPValArray.length; i++) {
            adjBonPVals.add(new Float(adjPValArray[i]));
        }
        return adjBonPVals;
    }
    
    /*
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
                    //pValuesVector.add(new Float(currentP));
		} else {
		    nonSigGenes.add(new Integer(i));
                    fValuesVector.add(new Float(currentF));
                    //pValuesVector.add(new Float(currentP));                    
		}                
            }
        }
        
	Vector sortedGenes = new Vector();
	sortedGenes.add(sigGenes);
	sortedGenes.add(nonSigGenes);   
	
        return sortedGenes;        
    }
     */
    
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
    
    private int getDfNum(int gene, FloatMatrix permMatrix) {
	//float[] geneValues = new float[numExps];
        int n = 0;
	for (int i = 0; i < numExps; i++) {
	    //geneValues[i] = expMatrix.A[gene][i];
            if ((!Float.isNaN(permMatrix.A[gene][i])) && (groupAssignments[i] != 0)) {
                n++;
            }
	} 
         if (n == 0) {
             return (-1); // will be exported as Float.NaN to OWAGUI
         }
        
         return (numGroups - 1);
    }
    
    private int getDfDenom(int gene, FloatMatrix permMatrix) {
        int n = 0;
	for (int i = 0; i < numExps; i++) {
	    //geneValues[i] = expMatrix.A[gene][i];
            if ((!Float.isNaN(permMatrix.A[gene][i])) && (groupAssignments[i] != 0)) {
                n++;
            }
	} 
         if (n == 0) {
             return (-1); // will be exported as Float.NaN to OWAGUI
         } 
        return (n - numGroups);
    }    
    
    /*
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
        double pValue = 1 - cumulProb; //
        
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
         /
        //System.out.println("Gene " + gene + ": GroupsSS = " + groupsSS + ", errorSS = " + errorSS + ", groupsDF = " + groupsDF + ", errorDF = " + errorDF + ", F = " + fValue + ", p = " + pValue);
        
        return sig;
    }
         */

    
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
