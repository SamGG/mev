/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: BETR.java,v $
 * $Revision: 1.8 $
 * $Date: 2008-8-28 17:27:39 $
 * $Author: dschlauch $
 * $State: Exp $
 */


package org.tigr.microarray.mev.cluster.algorithm.impl;


import org.tigr.util.FloatMatrix;
//import org.tigr.util.QSort;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;

//import JSci.maths.statistics.FDistribution;

import java.util.ArrayList;
//import java.util.Random;
import java.util.Vector;

import javax.swing.JOptionPane;

public class BETR extends AbstractAlgorithm{
    public static final int FALSE_NUM = 12;
    public static final int FALSE_PROP = 13;  
    
    private static int progress;
/*    private int function;
    private float factor;
    private boolean absolute, calculateAdjFDRPVals;*/
    private static FloatMatrix expMatrix;
    private boolean stop = false;
    private int[] timeAssignments;
    private int[] conditionAssignments;
    private int[][] conditionsMatrix;  
    private int[][] sigGenesArrays= new int[2][];
    private int[] errorGenesArray;

    private int numGenes, numExps, numTimePoints;
    //private float alpha, falseProp;
    private boolean  drawSigTreesOnly;//usePerms,;
  //  private int numPerms, falseNum;
  //  private int correctionMethod;    
    private int hcl_function;
    private boolean hcl_absolute;
    private boolean hcl_genes_ordered;  
    private boolean hcl_samples_ordered; 
    
    private int dataDesign;
    
    
    
  //  private int k; // # of clusters
    
  //  private float  falseProp;   
    //int[] groupAssignments; 
  //  private double[] origPVals;
    
    float currentP = 0.0f;
    //float currentF = 0.0f;
    int currentIndex = 0; 
    double constant;
    
    
  /*  Vector fValuesVector = new Vector();
    Vector rawPValuesVector = new Vector();
    Vector adjPValuesVector = new Vector();
    //Vector pValuesVector = new Vector(); 
    Vector dfNumVector = new Vector();
    Vector dfDenomVector = new Vector();
    Vector ssGroupsVector = new Vector();
    Vector ssErrorVector = new Vector();
    private boolean[] isSig;
    
    
    private boolean useFastFDRApprox = true;*/
    
    
    private static AlgorithmEvent event;
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
    	expMatrix = data.getMatrix("experiment");
    	timeAssignments = data.getIntArray("time_assignments");
    	conditionAssignments = data.getIntArray("condition_assignments");
    	AlgorithmParameters map = data.getParams();
    	dataDesign = map.getInt("dataDesign");
    	
    	conditionsMatrix = data.getIntMatrix("conditions-matrix");
/*    	function = map.getInt("distance-function", EUCLIDEAN);
    	factor   = map.getFloat("distance-factor", 1.0f);
    	absolute = map.getBoolean("distance-absolute", false);
        usePerms = map.getBoolean("usePerms", false);
        numPerms = map.getInt("numPerms", 0);*/
        hcl_function = map.getInt("hcl-distance-function", EUCLIDEAN);
        hcl_absolute = map.getBoolean("hcl-distance-absolute", false);     
        hcl_genes_ordered = map.getBoolean("hcl-genes-ordered", false);  
        hcl_samples_ordered = map.getBoolean("hcl-samples-ordered", false);     
        numTimePoints = map.getInt("numTimePoints");
        if (dataDesign==1)
        	numTimePoints--;
        numGenes1= expMatrix.getRowDimension();
        numGenes= numGenes1;
        numExps = expMatrix.getColumnDimension();
        alpha = map.getFloat("alpha-value");
    	boolean hierarchical_tree = map.getBoolean("hierarchical-tree", false);
            if (hierarchical_tree) {
                drawSigTreesOnly = map.getBoolean("draw-sig-trees-only");
            }        
    	int method_linkage = map.getInt("method-linkage", 0);
    	boolean calculate_genes = map.getBoolean("calculate-genes", false);
    	boolean calculate_experiments = map.getBoolean("calculate-experiments", false);
    	

    	
    /*	for (int i=0; i<conditionAssignments.length; i++){
    		if (conditionAssignments[i]==1){
    			condition1[nextEntry[0]]=i;
    			nextEntry[0]++;
    		}
    		if (conditionAssignments[i]==2)
    			condition2[nextEntry[1]]=i;
				nextEntry[1]++;
    	}*/
    	

    	progress=0;
    	event = null;
    	event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Finding Significant Genes...");
    	// set progress limit
    	fireValueChanged(event);
    	event.setId(AlgorithmEvent.PROGRESS_VALUE);
    	event.setIntValue(0);
    	fireValueChanged(event);
    	
    	initializeExperiments();
    	runAlg();
    	
    	

    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	AlgorithmData result = new AlgorithmData();
    	
    	
    	
    	/*getFDfSSValues();
    	
    	 FloatMatrix fValuesMatrix = new FloatMatrix(fValuesVector.size(), 1);   
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
             dfNumMatrix.A[i][0] = ((Integer)(dfNumVector.get(i))).floatValue();
             dfDenomMatrix.A[i][0] = ((Integer)(dfDenomVector.get(i))).floatValue();
             if (dfNumMatrix.A[i][0] <= 0) dfNumMatrix.A[i][0] = Float.NaN;
             if (dfDenomMatrix.A[i][0] <= 0) dfDenomMatrix.A[i][0] = Float.NaN;
         }
         
         FloatMatrix ssGroupsMatrix = new FloatMatrix(numGenes, 1);
         FloatMatrix ssErrorMatrix = new FloatMatrix(numGenes, 1);
         
         for (int i = 0; i < ssGroupsMatrix.getRowDimension(); i++) {
             ssGroupsMatrix.A[i][0] = ((Double)(ssGroupsVector.get(i))).floatValue();//(float)(getGroupsSS(currentGene));
             ssErrorMatrix.A[i][0] = ((Double)(ssErrorVector.get(i))).floatValue();//(float)(getTotalSS(currentGene) - getGroupsSS(currentGene));
         }*/
    	
    	
    	FloatMatrix means =getMeans(sigGenesArrays);        
    	System.out.println("siggenes");
    	for (int i=0; i<1; i++){
        	for (int j=0; j<sigGenesArrays[i].length; j++){
        		System.out.print(sigGenesArrays[i][j]+ "   ");
        	}
        	System.out.println(" **");
        }
    	
    	FloatMatrix variances = getVariances(sigGenesArrays, means); 
    	Cluster result_cluster = new Cluster();
    	NodeList nodeList = result_cluster.getNodeList();
    	int[] features;        
    	for (int i=0; i<sigGenesArrays.length; i++) {
    	    if (stop) {
    		throw new AbortException();
    	    }
    	    features = sigGenesArrays[i];
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
    	result.addIntMatrix("sigGenesArrays", sigGenesArrays);
    	result.addParam("error-length", String.valueOf(errorGenesArray.length));
    	result.addIntArray("error-genes", errorGenesArray);
    	result.addCluster("cluster", result_cluster);
    	result.addParam("number-of-clusters", "1"); //String.valueOf(clusters.length));    
    	result.addMatrix("clusters_means", means);
    	result.addMatrix("clusters_variances", variances); 
    /*  
        result.addMatrix("rawPValues", rawPValuesMatrix);
        result.addMatrix("adjPValues", adjPValuesMatrix);
        result.addMatrix("fValues", fValuesMatrix);
        result.addMatrix("dfNumMatrix", dfNumMatrix);
        result.addMatrix("dfDenomMatrix", dfDenomMatrix);
        result.addMatrix("ssGroupsMatrix", ssGroupsMatrix);
        result.addMatrix("ssErrorMatrix", ssErrorMatrix);*/
        result.addMatrix("geneTimeMeansMatrix", getAllGeneTimeMeans());
        result.addMatrix("geneTimeSDsMatrix", getAllGeneTimeSDs());       
        if (dataDesign==2){
        	result.addMatrix("geneConditionMeansMatrix", getAllGeneConditionMeans());
        	result.addMatrix("geneConditionSDsMatrix", getAllGeneConditionSDs());       
        }
        result.addMatrix("pValues", getPValues());
    	return result;   
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
    private NodeValueList calculateHierarchicalTree(int[] features, int method, boolean genes, boolean experiments) throws AlgorithmException {
    	NodeValueList nodeList = new NodeValueList();
    	AlgorithmData data = new AlgorithmData();
    	FloatMatrix experiment = getSubExperiment(expMatrix, features);
    	data.addMatrix("experiment", experiment);
            data.addParam("hcl-distance-function", String.valueOf(this.hcl_function));
            data.addParam("hcl-distance-absolute", String.valueOf(this.hcl_absolute));
    	data.addParam("method-linkage", String.valueOf(method));
    	HCL hcl = new HCL();
    	AlgorithmData result;
    	if (genes) {
    	    data.addParam("calculate-genes", String.valueOf(true));
    	    data.addParam("optimize-gene-ordering", String.valueOf(hcl_genes_ordered));
    	    result = hcl.execute(data);
    	    validate(result);
    	    addNodeValues(nodeList, result);
    	}
    	if (experiments) {
    	    data.addParam("calculate-genes", String.valueOf(false));
    	    data.addParam("optimize-sample-ordering", String.valueOf(hcl_samples_ordered));
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
    private FloatMatrix getMeans(int[][] clusters) {
    	FloatMatrix means = new FloatMatrix(clusters.length, numExps);
    	FloatMatrix mean;
    	for (int i=0; i<clusters.length; i++) {
    	    mean = getMean(clusters[i]);
    	    means.A[i] = mean.A[0];
    	}
    	return means;
        }
    private FloatMatrix getMean(int[] cluster) {
    	FloatMatrix mean = new FloatMatrix(1, numExps);
    	float currentMean;
    	int n = cluster.length;
    	int denom = 0;
    	float value;
    	for (int i=0; i<numExps; i++) {
    	    currentMean = 0f;
    	    denom = 0;
    	    for (int j=0; j<n; j++) {
    		value = expMatrix.get(((Integer) cluster[j]).intValue(), i);
    		if (!Float.isNaN(value)) {
    		    currentMean += value;
    		    denom++;
    		}
    	    }
    	    mean.set(0, i, currentMean/(float)denom);
    	}

    	return mean;
    }
    private FloatMatrix getVariances(int[][] clusters, FloatMatrix means) {
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
    private float getSampleVariance(int[] cluster, int column, float mean) {
    	return(float)Math.sqrt(getSampleNormalizedSum(cluster, column, mean)/(float)(validN-1));
    	
    }  
    private float getSampleNormalizedSum(int[] cluster, int column, float mean) {
    	final int size = cluster.length;
    	float sum = 0f;
    	float value;
    	validN = 0;
    	for (int i=0; i<size; i++) {
    	    value = expMatrix.get(((Integer) cluster[i]).intValue(), column);
    	    if (!Float.isNaN(value)) {
    		sum += Math.pow(value-mean, 2);
    		validN++;
    	    }
    	}
    	return sum;
    }
    

    

    
    
    
    
    
    //----------------------------------------------------------------------------------------------------------------------------------//
   /* 
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
            for (int i = 0; i < timeAssignments.length; i++) {
                if (timeAssignments[i] != 0) validExpts.add(new Integer(i));
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
        }*/
        /*
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
        */
/*
        private double getYConservative(double alphaQuantile, int u) throws AlgorithmException {
            AlgorithmEvent event2 = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numPerms);
            fireValueChanged(event2);
            event2.setId(AlgorithmEvent.PROGRESS_VALUE);
            Vector uPlusOneSmallestPVector = new Vector(); 
            
            Vector validExpts = new Vector();
            for (int i = 0; i < timeAssignments.length; i++) {
                if (timeAssignments[i] != 0) validExpts.add(new Integer(i));
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
     *//*   
        private int[][] getDfs(FloatMatrix permutedMatrix) {
            int[][] dfs = new int[2][numGenes];
            for (int gene = 0; gene < numGenes; gene++) {
                dfs[0][gene] = getDfNum(gene, permutedMatrix); 
                dfs[1][gene] = getDfDenom(gene, permutedMatrix);
            }
            return dfs;
        }
        *//*
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
        *//*
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
        *//*
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
            for (int i = 0; i < timeAssignments.length; i++) {
                if (timeAssignments[i] != 0) validExpts.add(new Integer(i));
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
        *//*
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
        }    */
        /*
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
        *//*
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
            for (int i = 0; i < timeAssignments.length; i++) {
                if (timeAssignments[i] != 0) validExpts.add(new Integer(i));
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
        */
        /*
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
        */
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
        /*
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
                if ((!Float.isNaN(expMatrix.A[gene][i])) && (timeAssignments[i] != 0)) {
                    n++;
                }
    	} 
             if (n == 0) {
                 return (-1); // will be exported as Float.NaN to OWAGUI
             }
            
             return (numTimePoints - 1);
        }
        
        private int getDfDenom(int gene) {
            int n = 0;
    	for (int i = 0; i < numExps; i++) {
    	    //geneValues[i] = expMatrix.A[gene][i];
                if ((!Float.isNaN(expMatrix.A[gene][i])) && (timeAssignments[i] != 0)) {
                    n++;
                }
    	} 
             if (n == 0) {
                 return (-1); // will be exported as Float.NaN to OWAGUI
             } 
            return (n - numTimePoints);
        }
        *//*
        private int getDfNum(int gene, FloatMatrix permMatrix) {
    	//float[] geneValues = new float[numExps];
            int n = 0;
    	for (int i = 0; i < numExps; i++) {
    	    //geneValues[i] = expMatrix.A[gene][i];
                if ((!Float.isNaN(permMatrix.A[gene][i])) && (timeAssignments[i] != 0)) {
                    n++;
                }
    	} 
             if (n == 0) {
                 return (-1); // will be exported as Float.NaN to OWAGUI
             }
            
             return (numTimePoints - 1);
        }
        
        private int getDfDenom(int gene, FloatMatrix permMatrix) {
            int n = 0;
    	for (int i = 0; i < numExps; i++) {
    	    //geneValues[i] = expMatrix.A[gene][i];
                if ((!Float.isNaN(permMatrix.A[gene][i])) && (timeAssignments[i] != 0)) {
                    n++;
                }
    	} 
             if (n == 0) {
                 return (-1); // will be exported as Float.NaN to OWAGUI
             } 
            return (n - numTimePoints);
        }    

        
        private double getConstant(float[] geneValues) {
            double sum = 0.0d;
            double cons;
            int n = 0;
            for (int i = 0; i < geneValues.length; i++) {
                if ((!Float.isNaN(geneValues[i])) && (timeAssignments[i] != 0)) {
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
                if ((!Float.isNaN(geneValues[i])) && (timeAssignments[i] != 0)) {
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
            float[][] geneValuesByGroups = new float[numTimePoints][];
            
            for (int i = 0; i < numTimePoints; i++) {
                geneValuesByGroups[i] = getGeneValuesForGroup(geneValues, i+1);
            }
            
            double[] avSquareArray = new double[numTimePoints];
            
            for (int i = 0; i < numTimePoints; i++) {
                avSquareArray[i] = getAvSquare(geneValuesByGroups[i]);
            }
            
            double ss = 0;
            
            for (int i = 0; i < numTimePoints; i++) {
                ss = ss + avSquareArray[i];
            }
            
            return (ss - constant);
            
            //double ss = 0;
            //return ss;
        }
        */
        private float[] getGeneGroupMeans(int gene) {
        	float[] geneValues = new float[numExps];
            for (int i = 0; i < numExps; i++) {
    	    geneValues[i] = expMatrix.A[gene][i];
            } 
            
            float[][] geneValuesByGroups = new float[numTimePoints][];
            
            for (int i = 0; i < numTimePoints; i++) {
                geneValuesByGroups[i] = getGeneValuesForGroup(geneValues, i+1);
            } 
            
            float[] geneGroupMeans = new float[numTimePoints];
            for (int i = 0; i < numTimePoints; i++) {
                geneGroupMeans[i] = getMean(geneValuesByGroups[i]);
            }
            
            return geneGroupMeans;
        }
        
        private float[] getGeneConditionMeans(int gene) {
        	float[] geneValues = new float[numExps];
            for (int i = 0; i < numExps; i++) {
            	geneValues[i] = expMatrix.A[gene][i];
            } 
            
            float[][] geneValuesByCondition = new float[2][];
            
            for (int i = 0; i < 2; i++) {
                geneValuesByCondition[i] = getGeneValuesForGroup(geneValues, i+1);
            } 
            
            float[] geneGroupMeans = new float[2];
            for (int i = 0; i < 2; i++) {
                geneGroupMeans[i] = getMean(geneValuesByCondition[i]);
            }
            
            return geneGroupMeans;
        }
        
        private float[] getGeneGroupSDs(int gene) {
        	float[] geneValues = new float[numExps];        
            for (int i = 0; i < numExps; i++) {
            	geneValues[i] = expMatrix.A[gene][i];
            }   
            
            float[][] geneValuesByGroups = new float[numTimePoints][];
            
            for (int i = 0; i < numTimePoints; i++) {
                geneValuesByGroups[i] = getGeneValuesForGroup(geneValues, i+1);
            }  
            
            float[] geneGroupSDs = new float[numTimePoints];
            for (int i = 0; i < numTimePoints; i++) {
                geneGroupSDs[i] = getStdDev(geneValuesByGroups[i]);
            }
            
            return geneGroupSDs;        
        }
        
        private float[] getGeneConditionSDs(int gene) {
        	float[] geneValues = new float[numExps];        
            for (int i = 0; i < numExps; i++) {
            	geneValues[i] = expMatrix.A[gene][i];
            }   
            
            float[][] geneValuesByGroups = new float[2][];
            
            for (int i = 0; i < 2; i++) {
                geneValuesByGroups[i] = getGeneValuesForCondition(geneValues, i+1);
            }  
            
            float[] geneGroupSDs = new float[2];
            for (int i = 0; i < 2; i++) {
                geneGroupSDs[i] = getStdDev(geneValuesByGroups[i]);
            }
            
            return geneGroupSDs;        
        }
        
        private float[] getGeneValuesForGroup(float[] geneValues, int group) {
            Vector<Float> groupValuesVector = new Vector<Float>();
            
            for (int i = 0; i < timeAssignments.length; i++) {
                if (timeAssignments[i] == group) {
                    groupValuesVector.add(new Float(geneValues[i]));
                }
            }
            
            float[] groupGeneValues = new float[groupValuesVector.size()];
            
            for (int i = 0; i < groupValuesVector.size(); i++) {
                groupGeneValues[i] = ((Float)(groupValuesVector.get(i))).floatValue();
            }
            
            return groupGeneValues;
        }
        
        private float[] getGeneValuesForCondition(float[] geneValues, int condition) {
            Vector<Float> groupValuesVector = new Vector<Float>();
            
            for (int i = 0; i < conditionAssignments.length; i++) {
                if (conditionAssignments[i] == condition) {
                    groupValuesVector.add(new Float(geneValues[i]));
                }
            }
            
            float[] groupGeneValues = new float[groupValuesVector.size()];
            
            for (int i = 0; i < groupValuesVector.size(); i++) {
                groupGeneValues[i] = ((Float)(groupValuesVector.get(i))).floatValue();
            }
            
            return groupGeneValues;
        }
        
        private FloatMatrix getAllGeneTimeMeans() {
            FloatMatrix means = new FloatMatrix(numGenes, numTimePoints);
            for (int i = 0; i < means.getRowDimension(); i++) {
                means.A[i] = getGeneGroupMeans(i);
            }
            return means;
        }
        
        private FloatMatrix getAllGeneConditionMeans() {
            FloatMatrix means = new FloatMatrix(numGenes, 2);
            for (int i = 0; i < means.getRowDimension(); i++) {
                means.A[i] = getGeneConditionMeans(i);
            }
            return means;
        }
        
        private FloatMatrix getAllGeneTimeSDs() {
            FloatMatrix sds = new FloatMatrix(numGenes, numTimePoints);
            for (int i = 0; i < sds.getRowDimension(); i++) {
                sds.A[i] = getGeneGroupSDs(i);
            }
            return sds;        
        }
        
        private FloatMatrix getAllGeneConditionSDs() {
            FloatMatrix sds = new FloatMatrix(numGenes, 2);
            for (int i = 0; i < sds.getRowDimension(); i++) {
                sds.A[i] = getGeneConditionSDs(i);
            }
            return sds;        
        }
        
        private FloatMatrix getPValues(){
        	FloatMatrix pvals = new FloatMatrix(numGenes, 1);
        	for (int i=0; i<pvals.getRowDimension(); i++){
        		pvals.A[i][0] =1-I[i]; 
        	}
        	return pvals;
        }
        /*
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
        }*/
        
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
        
    
    
    
    //----------------------------------------------------------------------------------------------------------------------------------//
    
    
    
    
    
/**	
	Initialization Parameters:
   	-User selects one of 3 types of Data:
   	1.) 2-Condition
   	2.) 1-Condition (time=0 is control)
   	3.) Paired data
  	-User selects significance level, float alpha = ?.
 	-User specifies time order of samples, replicates, control, etc..	
 
 	ANOVA is run
 	Gather p-values from AVOVA with significant and non-significant genes
 
 	Start BETR algorithm:
 	-Perform a log2 transform on the data, both control and treatment.
 	-Create a data subset (XTreat) of all replicates in all time-points of treatment samples.
 	-Create a data subset (XControl) of all replicates in all time-points of control samples.
 	-Create a data subset (XTreatAverage) of average replicates for each time-point and condition of treatment samples.
 	-Create a data subset (XControlAverage) of average replicates for each time-point and condition of the control.
 	-Create a data subset (Y) equal to the log ratio of treatment to control for each time-point of averaged replicates.
 **/
    /** Lanczos coefficients */
	private static float[] lanczos =
	{
		0.99999999999999709182f,
		57.156235665862923517f,
		-59.597960355475491248f,
		14.136097974741747174f,
		-0.49191381609762019978f,
		.33994649984811888699e-4f,
		.46523628927048575665e-4f,
		-.98374475304879564677e-4f,
		.15808870322491248884e-3f,
		-.21026444172410488319e-3f,
		.21743961811521264320e-3f,
		-.16431810653676389022e-3f,
		.84418223983852743293e-4f,
		-.26190838401581408670e-4f,
		.36899182659531622704e-5f,
   	};
    /** Avoid repeated computation of log of 2 PI in logGamma */
    private static final float HALF_LOG_2_PI = 0.5f * (float)Math.log(2.0 * Math.PI);
	/**
	 * @param args
	 */
	private static int numTreatReps = 3;// = number of treatment replicates;
	private static int numConReps = 3;// = number of control replicates;
	private static int numGenes1; //number of genes		
	private static float p = (float).05;// p-value.	
	private static float alpha;// significance level.
	private float[] I;
	//-Initialize two 3-Dimensional float arrays of length n (number of genes), width and height k (number of time-points).
	private static FloatMatrix[] varianceError;// is the error variance.
	private static FloatMatrix[] varianceMean;// is the mean variance.
	private static FloatMatrix[] Shmg;
	private static FloatMatrix[] Sheg;
 	private static Experiment XTreat = new Experiment(null, null, null);
 	private static Experiment XControl = new Experiment(null, null, null);
 	private static Experiment XTreatAverage = new Experiment(null, null, null);
 	private static Experiment XControlAverage = new Experiment(null, null, null);
 	private static Experiment Y = new Experiment(null, null, null);
 	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FloatMatrix a, b, c, d, varsM, varsY;
		float aa, bb, cc,dd, ab, ac, ad, bc,bd,cd;
		bb=.9346125f;
		aa=.06862506f;
		cc=.834962f;
		dd=.06513383f;
		ab=-.0497811470f;
		bc=.818980040f;
		bd=.022040663f;
		ac=-.046962418f;
		ad=-.000632187f;
		cd=.020613318f;
		
		float ya, yb, yc, yd;
		yb=-.98876286f;
		ya=.06222725f;
		yc=-.9279957f;
		yd=-.025420189f;
		
		float[][]vars =  {	{aa,ab,ac,ad},
							{ab,bb,bc,bd},
							{ac,bc,cc,cd},
							{ad,bd,cd,dd}};
		float[][]yvars = {	{ya},{yb},{yc},{yd}	};
				
		float[][]adata = {	{.9346125f		,.818980040f	,-.0497811470f	,.022040663f},
							{-.049781147f	,-.046962418f	,.06862506f		,-.000632187f},
							{.81898004f		,.834962f		,-.0469624180f	,.020613318f},
							{.022040663f	,.020613318f	,-.0006328187f	,.06513383f}};

		float[][]bdata = {	{-.98876286f},
							{-.9279957f},
							{.06222725f},
							{-.025420189f},	};

		float[][]cdata = {	{.9346125f		,-.0497811470f	,.818980040f	,.022040663f},
							{-.049781147f	,.06862506f		,-.046962418f	,-.000632187f},
							{.81898004f		,-.0469624180f	,.834962f		,.020613318f},
							{.022040663f	,-.0006328187f	,.020613318f	,.06513383f}};
		
		float[][]ddata = {	{-.98876286f},
							{.06222725f},
							{-.9279957f},
							{-.025420189f},	};
		
		a = new FloatMatrix(adata);
		b = new FloatMatrix(bdata);

		c = new FloatMatrix(cdata);
		d = new FloatMatrix(ddata);
		varsM = new FloatMatrix(vars);
		varsY = new FloatMatrix(yvars);
		System.out.println(multivariateNormalFunction(a,b));
		System.out.println(multivariateNormalFunction(c,d));
		System.out.println(multivariateNormalFunction(varsM,varsY));
		
		//initializeExperimentsLocal();
		//runAlg();
	}
	public static void initializeExperimentsLocal(){

		System.out.println("Initializing...");
		XTreat.fillMatrix(makeXTExperimentMatrix());
		XTreatAverage.fillMatrix(makeXTAExperimentMatrix());
		XControl.fillMatrix(makeXCExperimentMatrix());
		XControlAverage.fillMatrix(makeXCAExperimentMatrix());
		//Y.fillMatrix(makeYExperimentMatrix());
	}
	public void initializeExperiments(){
		System.out.println("Initializing...");
		//System.out.println("n = " +n);
		//for (int i=0; i<conditionsMatrix[0].length;i++){
		//	System.out.println("conditionsMatrix[0][i] = "+conditionsMatrix[0][i]);
		//}
		if (dataDesign==1){
		
			FloatMatrix XTreatAve=new FloatMatrix(numGenes1, numTimePoints);
			for (int i=0; i<numTimePoints; i++){  //i=1 to skip t0.
				for (int j=0; j<numGenes1; j++){
					float s=0;
					float value;;
					int totals=0;
					for (int k=0; k<timeAssignments.length; k++){
						if ((timeAssignments[k]-1)==(i+1)){
							value = expMatrix.get(j,k); 
							if (!Float.isNaN(value)) {
								s=s+value;
								totals++;
							}
						}
					}
					XTreatAve.set(j, i, (float)s/totals);
				}
			}
			FloatMatrix XControlAve=new FloatMatrix(numGenes1, numTimePoints);
			for (int i=0; i<numTimePoints; i++){
				for (int j=0; j<numGenes1; j++){
					float s=0;
					float value;
					int totals=0;
					for (int k=0; k<timeAssignments.length; k++){
						if ((timeAssignments[k]-1)==0){//gathers all t0 assignments, creates full matrix using only t0
							value = expMatrix.get(j,k); 
							if (!Float.isNaN(value)) {
								s=s+value;
								totals++;
							}
						}
					}
					XControlAve.set(j, i, (float)s/totals);
				}
			}
			XTreatAverage.fillMatrix(XTreatAve);
			XControlAverage.fillMatrix(XControlAve);
			Y.fillMatrix(XTreatAve.minus(XControlAve));
		}
		if (dataDesign==3){
			FloatMatrix XTreatAve=new FloatMatrix(numGenes1, numTimePoints);
			for (int i=0; i<numTimePoints; i++){
				for (int j=0; j<numGenes1; j++){
					float s=0;
					float value;
					int totals=0;
					for (int k=0; k<timeAssignments.length; k++){
						if ((timeAssignments[k]-1)==i){
							value = expMatrix.get(j,k); 
							if (!Float.isNaN(value)) {
								s=s+value;
								totals++;
							}
						}
					}
					XTreatAve.set(j, i, (float)s/totals);
				}
			}
			Y.fillMatrix(XTreatAve);
			
		}
		if (dataDesign==2){
			XTreat.fillMatrix(expMatrix.getMatrix(0, numGenes1-1, conditionsMatrix[0]));
			XControl.fillMatrix(expMatrix.getMatrix(0, numGenes1-1, conditionsMatrix[1]));
		
			FloatMatrix XTreatAve=new FloatMatrix(numGenes1, numTimePoints);
			for (int i=0; i<numTimePoints; i++){
				for (int j=0; j<numGenes1; j++){
					float s=0;
					int totals=0;
					float value;
					for (int k=0; k<timeAssignments.length; k++){
						//System.out.print("tA["+k+"]="+timeAssignments[k]+" ? i="+i+" cA[k]="+conditionAssignments[k]+" | ");
						if ((timeAssignments[k]-1)==i&&(conditionAssignments[k]-1)==0){
							value = expMatrix.get(j,k); 
							if (!Float.isNaN(value)) {
								s=s+value;
								totals++;
							}
						}
					}
					//System.out.println("totals = " + totals);
					XTreatAve.set(j, i, (float)s/totals);
				}
			}
			FloatMatrix XControlAve=new FloatMatrix(numGenes1, numTimePoints);
			for (int i=0; i<numTimePoints; i++){
				for (int j=0; j<numGenes1; j++){
					float s=0;
					float value;
					int totals=0;
					for (int k=0; k<timeAssignments.length; k++){
						if ((timeAssignments[k]-1)==i&&(conditionAssignments[k]-1)==1){
							value = expMatrix.get(j,k); 
							if (!Float.isNaN(value)) {
								s=s+value;
								totals++;
							}
						}
					}
					XControlAve.set(j, i, (float)s/totals);
				}
			}
			XTreatAverage.fillMatrix(XTreatAve);
			XControlAverage.fillMatrix(XControlAve);
			Y.fillMatrix(XTreatAve.minus(XControlAve));
		}
	}
	public void runAlg(){
		System.out.println("Running algorithm...");
	/*	System.out.println("XTreat");
		for (int i=0; i<XTreat.getNumberOfGenes(); i++){
			for (int j=0; j<XTreat.getNumberOfSamples(); j++){
				System.out.print(XTreat.get(i, j)+"   ");
				
			}
			System.out.println("");
		}
		System.out.println("XControl");
		for (int i=0; i<XControl.getNumberOfGenes(); i++){
			for (int j=0; j<XControl.getNumberOfSamples(); j++){
				System.out.print(XControl.get(i, j)+"   ");
				
			}
			System.out.println("");
		}

		System.out.println("XTreatAverage");
		for (int i=0; i<XTreatAverage.getNumberOfGenes(); i++){
			for (int j=0; j<XTreatAverage.getNumberOfSamples(); j++){
				System.out.print(XTreatAverage.get(i, j)+"   ");
				
			}
			System.out.println("");
		}
		System.out.println("Y");
		for (int i=0; i<Y.getNumberOfGenes(); i++){
			for (int j=0; j<Y.getNumberOfSamples(); j++){
				System.out.print(Y.get(i, j)+"   ");
				
			}
			System.out.println("");
		}*/
		progress++;
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue(10);
    	fireValueChanged(event);

	 	//-Estimate Variance components:
	 	float seg = 0; 
	 	varianceError = new FloatMatrix[numGenes1];
	 	varianceMean = new FloatMatrix[numGenes1];
	 	Sheg = new FloatMatrix[numGenes1];
	 	Shmg = new FloatMatrix[numGenes1];

	 	for (int i=0; i<numGenes1; i++) {
	 		varianceError[i] = new FloatMatrix(numTimePoints,numTimePoints);
	 		varianceMean[i] = new FloatMatrix(numTimePoints,numTimePoints);
	 		for (int ii=0; ii<numTimePoints+1; ii++) {
	 			for (int sample=0; sample<numExps; sample++){
	 				
	 				if (timeAssignments[sample]-1==ii){
	 					float value =expMatrix.get(i, sample);
	 					if (Float.isNaN(value))
	 						continue;
	 					//1-Cond. scenario
	 					if (dataDesign==1){
		 					if (timeAssignments[sample]-1==0){
		 						seg = seg + (float)Math.pow((expMatrix.get(i,sample)-XControlAverage.get(i, ii)),2);
		 					}else{
		 						seg = seg + (float)Math.pow((expMatrix.get(i,sample)-XTreatAverage.get(i, ii-1)),2);
		 					}
	 					}
	 					//2-Cond. scenario
	 					if (dataDesign==2){
		 					if (conditionAssignments[sample]==1){
		 						seg = seg + (float)Math.pow((expMatrix.get(i,sample)-XTreatAverage.get(i, ii)),2);
		 					}
		 					if (conditionAssignments[sample]==2){
		 						seg = seg + (float)Math.pow((expMatrix.get(i,sample)-XControlAverage.get(i, ii)),2);
		 					}
	 					}
	 					//Paired-Data scenario
	 					if (dataDesign==3){
	 						seg = seg + (float)Math.pow((expMatrix.get(i,sample)-Y.get(i, ii)),2);
	 					}
	 				}
	 			}
	 		}
	 		//System.out.println("dataDesign = " + dataDesign + " numTimePoints = " +numTimePoints + " columns = "+varianceMean[0].getColumnDimension());
	 		for (int ii=0; ii<numTimePoints; ii++) {
	 			for (int iii=0; iii<numTimePoints; iii++) { 
	 				varianceMean[i].set(ii, iii, Y.get(i,ii)*Y.get(i,iii));
	 				
	 				//change this to adjust to complete error variance matrix
	 				varianceError[i].set(ii, iii, 0);
	 			}
	 		}
	 		if (dataDesign==1){
	 			seg = seg/((numTimePoints+1)*(numTreatReps-1));
	 		}
	 		if (dataDesign==2){
	 			seg = seg/(numTimePoints*(2*(numTreatReps-1)));
	 		}
	 		if (dataDesign==3){
	 			seg = seg/(numTimePoints*((numTreatReps-1)));
	 		}
	 		if (seg==0){
	 			System.out.println("Bad Data, no variance for gene " + i);
	 		    JOptionPane.showMessageDialog(null, "Invalid Data!", "Error", JOptionPane.WARNING_MESSAGE);
                stop = true;
	 		}
	 		for (int ii=0; ii<numTimePoints; ii++) {
	 			varianceError[i].set(ii, ii, seg);
	 		}
	 		seg = 0f;
	 	}

		System.out.println("Sm");
		for (int i=0; i<varianceMean[0].getRowDimension(); i++){
			for (int j=0; j<varianceMean[0].getColumnDimension(); j++){
				System.out.print(varianceMean[0].get(i, j)+"   ");
				
			}
			System.out.println("");
		}
	 		
		System.out.println("Se");	
		for (int i=0; i<varianceError[0].getColumnDimension();i++){
			for (int j=0; j<varianceError[0].getRowDimension();j++){
				System.out.print(varianceError[0].get(i, j)+ " ");
			}
			System.out.println("  varianceError[0]");
		}

 		//Estimate gene-specific variance components, ~Seg.
		//Find SBar:
		FloatMatrix SeBar = new FloatMatrix(numTimePoints,numTimePoints);
		float sume = 0;
		for(int ii=0; ii<numTimePoints; ii++){
			for(int iii=0; iii<numTimePoints; iii++){
				for(int i=0; i<numGenes1; i++){
					sume = sume + varianceError[i].get(ii,iii);
				} 
				SeBar.set(ii,iii, sume/(float)numGenes1);	
				sume = 0f;
			}
		}

		//Find nu and lambda:
		float totalnue =0f;
		//find d0
		float d0;
		float s0;
		float totalmti=0f;
		float ebar=0f;
		float[] eg = new float[numGenes1];
		float dg=0f;
		if (dataDesign==1)
			dg= (float)(  (numTreatReps-1)*(numTimePoints+1));
		if (dataDesign==2)
			dg= (float)(2*(numTreatReps-1)* numTimePoints);
		if (dataDesign==3)
			dg= (float)(  (numTreatReps-1)* numTimePoints);
		
		//time intensive loop
		System.out.println("dg = "+dg);
		for (int i=0; i<numGenes1; i++){
			eg[i] = (float)( Math.log(varianceError[i].get(0, 0)) - diGamma(dg/2) + Math.log(dg/2) );
			//System.out.println("["+i+"] = "+varianceError[i].get(0, 0) + " diGamma(dg/2)="+diGamma(dg/2)+ " Math.log(dg/2)="+Math.log(dg/2));
			
			if (Float.isNaN(eg[i]))
				System.out.println("NaN varianceError["+i+"].get(0, 0) = "+varianceError[i].get(0, 0) + " diGamma(dg/2)="+diGamma(dg/2)+ " Math.log(dg/2)="+Math.log(dg/2));
			
		} 
		System.out.println("ebar init = "+ebar);
		for (int i=0;i<numGenes1; i++){
			ebar = ebar+eg[i];
			//System.out.print(" ebar"+i+"="+ebar + " eg[i]=" +eg[i]);
		}
		

		System.out.println("ebar1 = "+ebar);
		ebar=ebar/(float)numGenes1;
		System.out.println("ebar = "+ebar);
		System.out.println("n = "+numGenes1);
		//VERY time intensive loop
		for (int i=0;i<numGenes1; i++){
			totalmti=totalmti+((float)Math.pow(eg[i]-ebar, 2)*numGenes1/(float)(numGenes1-1))-triGamma(dg/2);
		}

		
		System.out.println("evar" + (float)totalmti/numGenes1);
		d0=2*trigammaInverse((float)totalmti/numGenes1);
		System.out.println("d0 = " +d0);
		s0 = (float)Math.exp(ebar + diGamma(d0/2)- Math.log(d0/2));
		System.out.println("s0 = "+s0);
		System.out.println("dg = " +dg);
		float[] segtilde = new float[numGenes1];
		for (int i=0; i<numGenes1; i++){
			segtilde[i]=(float)((d0*s0)+dg*varianceError[i].get(0, 0))/(d0+dg);
		
		}

		updateProgressBar();
    	fireValueChanged(event);
		

		for (int i=0; i<numGenes1; i++){
			Sheg[i] = new FloatMatrix(numTimePoints, numTimePoints);
			for (int j = 0; j<numTimePoints; j++){
				Sheg[i].set(j, j, segtilde[i]);
			}
			//System.out.println("seg~"+segtilde[i]);
		}
		
		for (int t=0; t<numTimePoints; t++){
			totalnue = totalnue+d0; 
		}
//		float nue = totalnue/numTimePoints;
//		FloatMatrix lambdae;
//		float tempnue = Math.max(nue, numTimePoints+6);
//		lambdae = SeBar.times((tempnue-numTimePoints-1)/tempnue);
//		FloatMatrix[] Sheg = new FloatMatrix[n];
//		for (int i=0; i<n; i++){
//			Sheg[i] = (varianceError[i].times((float)(timepointsT+timepointsC-2)).plus(lambdae.times(nue)).times(1/(float)(timepointsT+timepointsC-2)+nue));
//		}
		 	//Estimate gene-specific variance components diagonal matrix ~seg with d0 and s0(page 8).
		 		//from previous step, we have the values for d0 and s0.
		 		//float ~seg = (d0*s0^2 + dg*sg^2)/(d0 + dg);
		 		
		
		 	//Estimate gene-specific multivariate normal density for the null hypothesis
		 	//	store values in float array of length n.
	 	float[] f0mvnd = new float[numGenes1];
	 	for (int i=0; i<numGenes1; i++) {
	 		FloatMatrix Yg = new FloatMatrix(numTimePoints,1);
	 		for (int yi = 0; yi<numTimePoints; yi++){
	 			Yg.set(yi,0, Y.get(i, yi));
	 		}
			for (int q=0; q<varianceError[i].getColumnDimension();q++){
				for (int j=0; j<varianceError[i].getRowDimension();j++){
					//System.out.print(varianceError[i].times((float)(numConReps+numTreatReps)/(numConReps*numTreatReps)).get(q, j)+ " ");
				}
				//System.out.println("  varianceError");
			}
			f0mvnd[i] = multivariateNormalFunction(Sheg[i].times((float)(numConReps+numTreatReps)/(numConReps*numTreatReps)), Yg);
			//System.out.println("f0mvnd["+i+"] = "+ f0mvnd[i]);
	 	}
	 		
	 		
	 	//Main shrinkage loop.  Estimate covariance parameters and I's through shrinkage method.
 		ArrayList<Integer> diffGenes = new ArrayList<Integer>();
 		ArrayList<Integer> nonDiffGenes = new ArrayList<Integer>();
 		ArrayList<Integer> errorGenes = new ArrayList<Integer>();
 		diffGenes.add(1);
 		//diffGenes.add(2);
 		//diffGenes.add(6);
 		
 		ArrayList<Integer> diffGenesOld = new ArrayList<Integer>();
 		int iteration = 1;
 		while (true){
			updateProgressBar();
	    	fireValueChanged(event);
 			System.out.println();
 			System.out.println("=========================Main Shrinkage Loop====================================");
 			System.out.println("==============================Iteration "+iteration + "=======================================");
 			System.out.println();
 			iteration++;
 			//determine which genes are currently labeled as differentially expressed.
 			//Should be defined as integer array, ArrayList<Integer>,(or boolean array)of gene IDs, diffGenes[]. 
 				//Probably easiest way is to use format given to us by ANOVA or transfer that array into an ArrayList for easier manipulation.

 			//Estimate gene-specific variance components, ~Smg.
			//Find SBar:
			FloatMatrix SBar = new FloatMatrix(numTimePoints,numTimePoints);
			float sum = 0f;
			for(int ii=0; ii<numTimePoints; ii++){
				for(int iii=0; iii<numTimePoints; iii++){
					for(int i=0; i<diffGenes.size(); i++){
						sum = sum + varianceMean[diffGenes.get(i)].get(ii,iii);
					} 
				SBar.set(ii,iii, sum/diffGenes.size());	
				sum = 0f;
				}
			}
			//Find nu and lambda:
			float totalnu =0;
			float[] d0t=new float[numTimePoints];
			
			
			for (int k=0; k<numTimePoints; k++){
				updateProgressBar();
		    	fireValueChanged(event);
				float totalmtim=0;
				float ebarm=0;
				for (int i=0;i<diffGenes.size(); i++){
					ebarm = ebarm+varianceMean[i].get(k,k);
				}
				ebarm=ebarm/(float)diffGenes.size();

				float dgm=0f;
				if (dataDesign==1)
					dgm= (float)(  (numTreatReps-1)*(numTimePoints+1));
				if (dataDesign==2)
					dgm= (float)(2*(numTreatReps-1)* numTimePoints   );
				if (dataDesign==3)
					dgm= (float)(  (numTreatReps-1)* numTimePoints   );
			
				float[]egm = new float[diffGenes.size()];
				for (int i=0; i<diffGenes.size(); i++){
					egm[i] = (float)(Math.log(varianceMean[i].get(k, k))-diGamma(dgm/2)+Math.log(dgm/2));
				}
				for (int i=0;i<diffGenes.size(); i++){
					ebarm = ebarm+egm[i];
				}
				ebarm=ebarm/(float)numGenes1;
			
				
				
				for (int i=0;i<diffGenes.size(); i++){
					totalmtim=totalmtim+((float)Math.pow((egm[i]-ebarm), 2)*diffGenes.size()/(float)(diffGenes.size()-1))-triGamma(dgm/2);
				}
					
				d0t[k]=2*trigammaInverse((float)totalmtim/diffGenes.size());
				System.out.println("d0t["+k+"] = "+d0t[k]);
 			}
			
			
			
			
			
			for (int k=0; k<numTimePoints; k++){
				totalnu = totalnu+d0t[k]; 
			}
			float nu = totalnu/numTimePoints;
			FloatMatrix lambda;
			float tempnu = Math.max(nu, numTimePoints+6);
			lambda = SBar.times((tempnu-numTimePoints-1)/tempnu);
/*			for (int i=0; i<lambda.getColumnDimension(); i++){
				for (int j=0; j<lambda.getColumnDimension(); j++){
					System.out.print(SBar.get(i, j)+" ");
				}
				System.out.println(" SBar");
			}
*/			
			//Estimate gene-specific variance components:
			//System.out.println("nu="+ nu);
			for (int i=0; i<numGenes1; i++){
				Shmg[i] = (varianceMean[i].plus(lambda.times(nu)).times((float)1/(1+nu)));
			}
 			//f1(Yg) = (1/(Math.pow((2*Math.pi),numTimePoints/2)*Math.sqrt(determinant of (Shmg + (numConReps+numTreatReps)/(numConReps*numTreatReps))varianceError[gene])))*Math.exp((-1/2)Yg.transpose*(~Smg+(numConReps+numTreatReps)/(numConReps*numTreatReps))VarianceError[gene].invert*Yg);
 			//store values in float array of length n.
 			float[] f1mvnd = new float[numGenes1];
 			FloatMatrix Yg = new FloatMatrix(numTimePoints, 1);
 			for (int i=0; i<numGenes1; i++) {
	 			//FloatMatrix Yg = new FloatMatrix(numTimePoints, 1);
	 			for (int yi = 0; yi<numTimePoints; yi++){
	 				Yg.set(yi, 0, Y.get(i, yi));
	 			}
	 			/*
	 			for (int x=0; x<varianceMean[i].getColumnDimension();x++){
	 				for (int j=0; j<varianceMean[i].getRowDimension();j++){
	 					System.out.print(varianceMean[i].get(x, j));
	 					System.out.print(Shmg[i].get(x, j) + " ");
	 					
	 					System.out.print(Shmg[i].plus(varianceMean[i].times((numConReps+numTreatReps)/(numConReps*numTreatReps))).get(x, j)+ " ");
	 				}
	 				System.out.println("  premnf");
	 			}
 				System.out.println("  ");
	 			for (int x=0; x<varianceMean[i].getColumnDimension();x++){
	 				for (int j=0; j<varianceMean[i].getRowDimension();j++){
	 					System.out.print(varianceMean[i].get(x, j));
	 					System.out.print(varianceMean[i].get(x, j) + " ");
	 					
	 					System.out.print(Shmg[i].plus(varianceMean[i].times((numConReps+numTreatReps)/(numConReps*numTreatReps))).get(x, j)+ " ");
	 				}
	 				System.out.println("  premnf2");
	 			}*/
	 			if (i==0){
	 				System.out.println("i=0");
	 				for (int x=0; x< Sheg[0].getRowDimension(); x++){
	 	 		 		for (int j=0; j< Sheg[0].getColumnDimension(); j++){
	 	 		 			System.out.print(Sheg[0].get(x, j)+ "  ");
	 	 		 		}
	 	 		 		System.out.println("  Sheg[0] BETR");
	 	 		 	}

	 	 		 	for (int x=0; x< Shmg[0].getRowDimension(); x++){
	 	 		 		for (int j=0; j< Shmg[0].getColumnDimension(); j++){
	 	 		 			System.out.print(Shmg[0].get(x, j)+ "  ");
	 	 		 		}
	 	 		 		System.out.println("  Shmg[0] BETR");
	 	 		 	}
	 	 		 	
	 	 		 	System.out.println("end!");
	 			}
	 			
	 			
 				f1mvnd[i] = multivariateNormalFunction(Shmg[i].plus(Sheg[i].times((float)(numConReps+numTreatReps)/(numConReps*numTreatReps))), Yg);
 			
 			
 			}
 			for (int yi = 0; yi<numTimePoints; yi++){
 				Yg.set(yi, 0, Y.get(0, yi));
 			}
 			System.out.println(f1mvnd[0]+ " : "+ multivariateNormalFunction(Shmg[0].plus(Sheg[0].times((float)(numConReps+numTreatReps)/(numConReps*numTreatReps))), Yg));
 			for (int i=0; i< Sheg[0].getRowDimension(); i++){
 		 		for (int j=0; j< Sheg[0].getColumnDimension(); j++){
 		 			System.out.print(Sheg[0].get(i, j)+ "  ");
 		 		}
 		 		System.out.println("  Sheg[0] BETR");
 		 	}
 		 	for (int i=0; i< Shmg[0].getRowDimension(); i++){
 		 		for (int j=0; j< Shmg[0].getColumnDimension(); j++){
 		 			System.out.print(Shmg[0].get(i, j)+ "  ");
 		 		}
 		 		System.out.println("  Shmg[0] BETR");
 		 	}
 		 	
 			for (int i=0; i< Shmg[0].plus(Sheg[0].times((float)(numConReps+numTreatReps)/(numConReps*numTreatReps))).getRowDimension(); i++){
 		 		for (int j=0; j< Shmg[0].plus(Sheg[0].times((float)(numConReps+numTreatReps)/(numConReps*numTreatReps))).getColumnDimension(); j++){
 		 			System.out.print(Shmg[0].plus(Sheg[0].times((float)(numConReps+numTreatReps)/(numConReps*numTreatReps))).get(i, j)+ "  ");
 		 		}
 		 		System.out.println("  Shmg[0].plus(Sheg[0].times((float)(numConReps+numTreatReps)/(numConReps*numTreatReps)))");
 		 	}
 		 	
 		 	for (int i=0; i< Yg.getRowDimension(); i++){
 		 		for (int j=0; j< Yg.getColumnDimension(); j++){
 		 			System.out.print(Yg.get(i, j)+ "  ");
 		 		}
 		 		System.out.println("  Yg BETR");
 		 	}
 			
 			//Determine next group of differentially expressed genes-
 			I = new float[numGenes1];// = probability of differential expression.
 			
 			for (int i=0; i<numGenes1; i++) {
 				I[i] = f1mvnd[i]*p/(f0mvnd[i]*(1-p)+f1mvnd[i]*p);
 				System.out.println("1-I["+i+"] = " +(1-I[i])+"      f1mvnd[i] = "+f1mvnd[i]+"   f0mvnd[i] = "+f0mvnd[i]);
 			}
 			//Re-evaluate diffGenes.
 			
 			diffGenes.clear();
 			nonDiffGenes.clear();
 			errorGenes.clear();

 			for (int i=0; i<numGenes1; i++) {
 				if (1-I[i]<alpha){
 					diffGenes.add(i);
 				}else{
 					if(Float.isNaN(I[i])){
 						errorGenes.add(i);
 					}else{
 						nonDiffGenes.add(i);
 					}
 				}
 			}
 			for (int i=0; i< diffGenes.size(); i++){
 				if (diffGenesOld.size()>i)
 					System.out.print("oldGenes is " + diffGenesOld.get(i));
 				System.out.println(" - diffgenes is " + diffGenes.get(i));
 			}
 			boolean sameresult=false;
 			if (diffGenes.size()==diffGenesOld.size()){
 				//System.out.println("sizesame");
	 			for (int i=0; i<diffGenes.size();i++){
	 				sameresult = true;
	 				//System.out.print("i="+i+" "+diffGenes.get(i)+ " : "+diffGenesOld.get(i)+ " answer=");System.out.println(diffGenes.get(i)!=diffGenesOld.get(i));
	 				if (!diffGenes.get(i).equals(diffGenesOld.get(i))){
	 					sameresult = false;
	 					continue;
	 				}
	 				
	 			}
 			}
 			System.out.println("same result = " + sameresult);
 			if (sameresult){
 				System.out.println("break command");
 				break;
 			}
 			if (stop)
 				break;
 			
 			diffGenesOld.clear();
 			System.out.println("setting up oldgenesarraylist...   diffGenes.size() = "+diffGenes.size());
 			for (int i=0; i<diffGenes.size(); i++) {
 					diffGenesOld.add(diffGenes.get(i));
 			}
 		}
 		for (int i=0; i<diffGenes.size();i++){
 			//System.out.println(" "+diffGenes.get(i));
 		}
 		sigGenesArrays[0]=new int[diffGenes.size()];
 		sigGenesArrays[1]=new int[nonDiffGenes.size()];
 		errorGenesArray = new int[errorGenes.size()];
 		
 		for (int i=0; i<sigGenesArrays[0].length; i++){
 			sigGenesArrays[0][i] = diffGenes.get(i);
 		}
 		for (int i=0; i<sigGenesArrays[1].length; i++){
 			sigGenesArrays[1][i] = nonDiffGenes.get(i);
 		}
 		for (int i=0; i<errorGenesArray.length; i++){
 			errorGenesArray[i] = errorGenes.get(i);
 		}
 		
 		
 	/*	System.out.println("Final Shmg[0]");
		for (int i=0; i<Shmg[0].getRowDimension(); i++){
			for (int j=0; j<Shmg[0].getColumnDimension(); j++){
				System.out.print(Shmg[0].get(i, j)+"   ");
				
			}
			System.out.println("");
		}*/
 		
	}
	public static void updateProgressBar(){

		progress++;
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue((100*progress)/(progress+7));
	}
	public static float multivariateNormalFunction(FloatMatrix coMat, FloatMatrix Yg){
		/*for (int i=0; i<coMat.getColumnDimension();i++){
			for (int j=0; j<coMat.getRowDimension();j++){
				System.out.print(coMat.get(i, j)+ " ");
			}
			System.out.println("  mnf");
		}
		System.out.println("coMat.m="+coMat.m);
		System.out.println("Yg.m="+Yg.m+ " Yg.n="+Yg.n);*/
		
		return (float)((1/(Math.pow((2*Math.PI),Yg.getRowDimension()/2))  *  Math.pow(coMat.det(),-.5))
		*Math.exp((Yg.transpose().times(
				(coMat.inverse().times(Yg)))).get(0, 0)*(-1.0f/2)));
			
	}
	public static FloatMatrix makeXTExperimentMatrix(){
		float[][] fmData = 
		
		{		{11.93013822f	,	11.81087045f	,	11.05215758f	,	12.07948199f	,	11.63863935f	,	11.33517265f	,	11.06916488f	,	11.10281917f	,	11.02322754f	,	10.83908244f	,	10.75319141f	,	10.8626565f		},//		,	11.69343674f	,	11.74531286f	,	11.16773626f	,	12.98257843f	,	12.78077297f	,	12.07392597f	,	12.01909443f	,	12.19998337f	,	11.94242466f	,	11.06667809f	,	10.76366918f	,	10.70083955f},
				{9.35959598f	,	8.760408981f	,	6.088167733f	,	10.03520563f	,	8.925660259f	,	5.33171039f		,	4.275100859f	,	3.851032434f	,	3.174049442f	,	4.554373759f	,	4.690858153f	,	2.802381691f	},//		,	9.337043017f	,	8.564559926f	,	5.85821347f		,	10.03611282f	,	9.02960571f		,	5.49036451f		,	4.34127191f		,	3.939580617f	,	2.985505256f	,	4.291524138f	,	4.698888953f	,	2.773521532f},
				{7.507128515f	,	7.440905061f	,	8.094832762f	,	6.309574293f	,	7.065648854f	,	7.835821909f	,	7.80158668f		,	7.934641802f	,	8.448770257f	,	8.230546795f	,	8.327929413f	,	8.40732011f		},//		,	7.641430723f	,	7.711561528f	,	8.148287128f	,	6.278521588f	,	7.063936327f	,	7.808408609f	,	7.804759987f	,	7.931586437f	,	8.345790381f	,	8.004979135f	,	8.42300179f		,	8.513288952f},
				{8.931664075f	,	8.682351687f	,	8.873200135f	,	9.311405042f	,	9.390521017f	,	9.495282098f	,	9.117950468f	,	8.702012741f	,	9.142829145f	,	8.800749132f	,	8.642231369f	,	8.793491304f	},//		,	8.904163697f	,	8.803126454f	,	9.197024723f	,	8.439698647f	,	8.36421071f		,	8.526549021f	,	8.1011291f		,	7.622765801f	,	8.34051305f		,	8.700691856f	,	8.768719071f	,	8.910892173f},
				{6.606184004f	,	6.031608174f	,	6.374485461f	,	6.935391978f	,	7.635735671f	,	7.441293152f	,	6.046176512f	,	5.775605384f	,	6.091817146f	,	6.058802238f	,	7.109108951f	,	5.815400744f	},//		,	6.643619392f	,	5.934497999f	,	6.120400921f	,	6.935980993f	,	7.680969031f	,	7.373862462f	,	5.791238842f	,	5.670946512f	,	5.971533571f	,	6.032972756f	,	6.915650032f	,	5.839659974f},
				{12.03273835f	,	11.42834037f	,	11.46158477f	,	12.71779993f	,	11.78541887f	,	11.65119814f	,	11.51964774f	,	11.52208411f	,	11.86627842f	,	11.6430769f		,	11.40962606f	,	11.5209498f		},//		,	12.05478526f	,	11.60530117f	,	11.49771223f	,	12.89254178f	,	11.73854897f	,	11.50690158f	,	11.59019539f	,	11.74138873f	,	11.77885256f	,	11.65672021f	,	11.66269015f	,	11.43032294f},
				{6.776058938f	,	7.399316381f	,	7.628982848f	,	4.826754266f	,	6.029444569f	,	5.10644741f		,	7.266176487f	,	8.308253185f	,	8.207499742f	,	8.807762697f	,	9.31595014f		,	8.632927986f	},//		,	6.78522796f		,	7.394894124f	,	7.73539422f		,	4.742628293f	,	6.038828548f	,	5.282998273f	,	7.464624784f	,	8.400966442f	,	8.177028789f	,	8.843872908f	,	9.246121749f	,	8.677455576f},
				{11.42280491f	,	9.582390065f	,	7.75765297f		,	12.1011861f		,	10.9287389f		,	9.525329563f	,	8.159804732f	,	8.59187888f		,	7.363144201f	,	9.112050272f	,	8.768064849f	,	7.236725595f	},//		,	11.38685106f	,	9.527776094f	,	7.701360662f	,	12.10171021f	,	10.92961922f	,	9.523685263f	,	8.068689232f	,	8.602430378f	,	7.462518615f	,	9.247704274f	,	8.951307003f	,	7.435387406f},
				{8.753829058f	,	8.62937362f		,	8.62289039f		,	7.85045032f		,	7.803872393f	,	7.748554361f	,	8.619168716f	,	9.243966122f	,	9.149066059f	,	8.5543875f		,	9.109409398f	,	8.564689637f	},//		,	8.700511103f	,	8.550795977f	,	8.43423938f		,	7.957658717f	,	7.879857355f	,	7.763860613f	,	8.600552456f	,	9.233847873f	,	9.160250284f	,	8.659900595f	,	9.041112824f	,	8.476568494f},
				{10.68392312f	,	9.977140342f	,	9.859623852f	,	11.33564591f	,	11.00126526f	,	10.79958749f	,	10.03083625f	,	9.792075535f	,	10.0939871f		,	9.744832362f	,	9.996346789f	,	9.795779926f	}};//		,	10.53418251f	,	9.879490651f	,	9.765520484f	,	10.17316395f	,	9.949708744f	,	9.883138652f	,	8.968556956f	,	8.772690196f	,	8.858671137f	,	9.803628489f	,	9.8869672f		,	9.593488566f}	};

		
		FloatMatrix fm = new FloatMatrix(fmData);
		return fm;
	}

	public static FloatMatrix makeXCExperimentMatrix(){
		float[][] fmData = 
		
/*		{		{11.93013822f	,	11.81087045f	,	11.05215758f	,	12.07948199f	,	11.63863935f	,	11.33517265f	,	11.06916488f	,	11.10281917f	,	11.02322754f	,	10.83908244f	,	10.75319141f	,	10.8626565f		,*/	{	{	11.69343674f	,	11.74531286f	,	11.16773626f	,	12.98257843f	,	12.78077297f	,	12.07392597f	,	12.01909443f	,	12.19998337f	,	11.94242466f	,	11.06667809f	,	10.76366918f	,	10.70083955f},
/*				{9.35959598f	,	8.760408981f	,	6.088167733f	,	10.03520563f	,	8.925660259f	,	5.33171039f		,	4.275100859f	,	3.851032434f	,	3.174049442f	,	4.554373759f	,	4.690858153f	,	2.802381691f	,*/		{	9.337043017f	,	8.564559926f	,	5.85821347f		,	10.03611282f	,	9.02960571f		,	5.49036451f		,	4.34127191f		,	3.939580617f	,	2.985505256f	,	4.291524138f	,	4.698888953f	,	2.773521532f},
/*				{7.507128515f	,	7.440905061f	,	8.094832762f	,	6.309574293f	,	7.065648854f	,	7.835821909f	,	7.80158668f		,	7.934641802f	,	8.448770257f	,	8.230546795f	,	8.327929413f	,	8.40732011f		,*/		{	7.641430723f	,	7.711561528f	,	8.148287128f	,	6.278521588f	,	7.063936327f	,	7.808408609f	,	7.804759987f	,	7.931586437f	,	8.345790381f	,	8.004979135f	,	8.42300179f		,	8.513288952f},
/*				{8.931664075f	,	8.682351687f	,	8.873200135f	,	9.311405042f	,	9.390521017f	,	9.495282098f	,	9.117950468f	,	8.702012741f	,	9.142829145f	,	8.800749132f	,	8.642231369f	,	8.793491304f	,*/		{	8.904163697f	,	8.803126454f	,	9.197024723f	,	8.439698647f	,	8.36421071f		,	8.526549021f	,	8.1011291f		,	7.622765801f	,	8.34051305f		,	8.700691856f	,	8.768719071f	,	8.910892173f},
/*				{6.606184004f	,	6.031608174f	,	6.374485461f	,	6.935391978f	,	7.635735671f	,	7.441293152f	,	6.046176512f	,	5.775605384f	,	6.091817146f	,	6.058802238f	,	7.109108951f	,	5.815400744f	,*/		{	6.643619392f	,	5.934497999f	,	6.120400921f	,	6.935980993f	,	7.680969031f	,	7.373862462f	,	5.791238842f	,	5.670946512f	,	5.971533571f	,	6.032972756f	,	6.915650032f	,	5.839659974f},
/*				{12.03273835f	,	11.42834037f	,	11.46158477f	,	12.71779993f	,	11.78541887f	,	11.65119814f	,	11.51964774f	,	11.52208411f	,	11.86627842f	,	11.6430769f		,	11.40962606f	,	11.5209498f		,*/		{	12.05478526f	,	11.60530117f	,	11.49771223f	,	12.89254178f	,	11.73854897f	,	11.50690158f	,	11.59019539f	,	11.74138873f	,	11.77885256f	,	11.65672021f	,	11.66269015f	,	11.43032294f},
/*				{6.776058938f	,	7.399316381f	,	7.628982848f	,	4.826754266f	,	6.029444569f	,	5.10644741f		,	7.266176487f	,	8.308253185f	,	8.207499742f	,	8.807762697f	,	9.31595014f		,	8.632927986f	,*/		{	6.78522796f		,	7.394894124f	,	7.73539422f		,	4.742628293f	,	6.038828548f	,	5.282998273f	,	7.464624784f	,	8.400966442f	,	8.177028789f	,	8.843872908f	,	9.246121749f	,	8.677455576f},
/*				{11.42280491f	,	9.582390065f	,	7.75765297f		,	12.1011861f		,	10.9287389f		,	9.525329563f	,	8.159804732f	,	8.59187888f		,	7.363144201f	,	9.112050272f	,	8.768064849f	,	7.236725595f	,*/		{	11.38685106f	,	9.527776094f	,	7.701360662f	,	12.10171021f	,	10.92961922f	,	9.523685263f	,	8.068689232f	,	8.602430378f	,	7.462518615f	,	9.247704274f	,	8.951307003f	,	7.435387406f},
/*				{8.753829058f	,	8.62937362f		,	8.62289039f		,	7.85045032f		,	7.803872393f	,	7.748554361f	,	8.619168716f	,	9.243966122f	,	9.149066059f	,	8.5543875f		,	9.109409398f	,	8.564689637f	,*/		{	8.700511103f	,	8.550795977f	,	8.43423938f		,	7.957658717f	,	7.879857355f	,	7.763860613f	,	8.600552456f	,	9.233847873f	,	9.160250284f	,	8.659900595f	,	9.041112824f	,	8.476568494f},
/*				{10.68392312f	,	9.977140342f	,	9.859623852f	,	11.33564591f	,	11.00126526f	,	10.79958749f	,	10.03083625f	,	9.792075535f	,	10.0939871f		,	9.744832362f	,	9.996346789f	,	9.795779926f	,*/		{	10.53418251f	,	9.879490651f	,	9.765520484f	,	10.17316395f	,	9.949708744f	,	9.883138652f	,	8.968556956f	,	8.772690196f	,	8.858671137f	,	9.803628489f	,	9.8869672f		,	9.593488566f}	};

		
		FloatMatrix fm = new FloatMatrix(fmData);
		return fm;
	}
	public static FloatMatrix makeXTAExperimentMatrix(){
		float[][] fmData = 
		
		{		{(11.93013822f	+	11.81087045f	+	11.05215758f)/3	,	(12.07948199f	+	11.63863935f	+	11.33517265f)/3	,(	11.06916488f	+	11.10281917f	+	11.02322754f)/3	,	(10.83908244f	+	10.75319141f	+	10.8626565f)/3	},	//+	11.69343674f	+	11.74531286f	+	11.16773626f	+	12.98257843f	+	12.78077297f	+	12.07392597f	+	12.01909443f	+	12.19998337f	+	11.94242466f	+	11.06667809f	+	10.76366918f	+	10.70083955f}+
				{(9.35959598f	+	8.760408981f	+	6.088167733f)/3	,	(10.03520563f	+	8.925660259f	+	5.33171039f	)/3	,(	4.275100859f	+	3.851032434f	+	3.174049442f)/3	,	(4.554373759f	+	4.690858153f	+	2.802381691f)/3},	//+	9.337043017f	+	8.564559926f	+	5.85821347f		+	10.03611282f	+	9.02960571f		+	5.49036451f		+	4.34127191f		+	3.939580617f	+	2.985505256f	+	4.291524138f	+	4.698888953f	+	2.773521532f}+
				{(7.507128515f	+	7.440905061f	+	8.094832762f)/3	,	(6.309574293f	+	7.065648854f	+	7.835821909f)/3	,(	7.80158668f		+	7.934641802f	+	8.448770257f)/3	,	(8.230546795f	+	8.327929413f	+	8.40732011f	)/3},	//+	7.641430723f	+	7.711561528f	+	8.148287128f	+	6.278521588f	+	7.063936327f	+	7.808408609f	+	7.804759987f	+	7.931586437f	+	8.345790381f	+	8.004979135f	+	8.42300179f		+	8.513288952f}+
				{(8.931664075f	+	8.682351687f	+	8.873200135f)/3	,	(9.311405042f	+	9.390521017f	+	9.495282098f)/3	,(	9.117950468f	+	8.702012741f	+	9.142829145f)/3	,	(8.800749132f	+	8.642231369f	+	8.793491304f)/3},	//+	8.904163697f	+	8.803126454f	+	9.197024723f	+	8.439698647f	+	8.36421071f		+	8.526549021f	+	8.1011291f		+	7.622765801f	+	8.34051305f		+	8.700691856f	+	8.768719071f	+	8.910892173f}+
				{(6.606184004f	+	6.031608174f	+	6.374485461f)/3	,	(6.935391978f	+	7.635735671f	+	7.441293152f)/3	,(	6.046176512f	+	5.775605384f	+	6.091817146f)/3	,	(6.058802238f	+	7.109108951f	+	5.815400744f)/3},	//+	6.643619392f	+	5.934497999f	+	6.120400921f	+	6.935980993f	+	7.680969031f	+	7.373862462f	+	5.791238842f	+	5.670946512f	+	5.971533571f	+	6.032972756f	+	6.915650032f	+	5.839659974f}+
				{(12.03273835f	+	11.42834037f	+	11.46158477f)/3	,	(12.71779993f	+	11.78541887f	+	11.65119814f)/3	,(	11.51964774f	+	11.52208411f	+	11.86627842f)/3	,	(11.6430769f	+	11.40962606f	+	11.5209498f	)/3},	//+	12.05478526f	+	11.60530117f	+	11.49771223f	+	12.89254178f	+	11.73854897f	+	11.50690158f	+	11.59019539f	+	11.74138873f	+	11.77885256f	+	11.65672021f	+	11.66269015f	+	11.43032294f}+
				{(6.776058938f	+	7.399316381f	+	7.628982848f)/3	,	(4.826754266f	+	6.029444569f	+	5.10644741f	)/3	,(	7.266176487f	+	8.308253185f	+	8.207499742f)/3	,	(8.807762697f	+	9.31595014f		+	8.632927986f)/3},	//+	6.78522796f		+	7.394894124f	+	7.73539422f		+	4.742628293f	+	6.038828548f	+	5.282998273f	+	7.464624784f	+	8.400966442f	+	8.177028789f	+	8.843872908f	+	9.246121749f	+	8.677455576f}+
				{(11.42280491f	+	9.582390065f	+	7.75765297f	)/3	,	(12.1011861f	+	10.9287389f		+	9.525329563f)/3	,(	8.159804732f	+	8.59187888f		+	7.363144201f)/3	,	(9.112050272f	+	8.768064849f	+	7.236725595f)/3},	//+	11.38685106f	+	9.527776094f	+	7.701360662f	+	12.10171021f	+	10.92961922f	+	9.523685263f	+	8.068689232f	+	8.602430378f	+	7.462518615f	+	9.247704274f	+	8.951307003f	+	7.435387406f}+
				{(8.753829058f	+	8.62937362f		+	8.62289039f	)/3	,	(7.85045032f	+	7.803872393f	+	7.748554361f)/3	,(	8.619168716f	+	9.243966122f	+	9.149066059f)/3	,	(8.5543875f		+	9.109409398f	+	8.564689637f)/3},	//+	8.700511103f	+	8.550795977f	+	8.43423938f		+	7.957658717f	+	7.879857355f	+	7.763860613f	+	8.600552456f	+	9.233847873f	+	9.160250284f	+	8.659900595f	+	9.041112824f	+	8.476568494f}+
				{(10.68392312f	+	9.977140342f	+	9.859623852f)/3	,	(11.33564591f	+	11.00126526f	+	10.79958749f)/3	,(	10.03083625f	+	9.792075535f	+	10.0939871f	)/3	,	(9.744832362f	+	9.996346789f	+	9.795779926f)/3}};	//+	10.53418251f	+	9.879490651f	+	9.765520484f	+	10.17316395f	+	9.949708744f	+	9.883138652f	+	8.968556956f	+	8.772690196f	+	8.858671137f	+	9.803628489f	+	9.8869672f		+	9.593488566f}	};

		
		FloatMatrix fm = new FloatMatrix(fmData);
		return fm;
	}
	public static FloatMatrix makeXCAExperimentMatrix(){
		float[][] fmData = 

		/*		{		{11.93013822f	+	11.81087045f	+	11.05215758f	+	12.07948199f	+	11.63863935f	+	11.33517265f	+	11.06916488f	+	11.10281917f	+	11.02322754f	+	10.83908244f	+	10.75319141f	+	10.8626565f		+*/	{	{	(11.69343674f	+	11.74531286f	+	11.16773626f)/3	,	(12.98257843f	+	12.78077297f	+	12.07392597f)/3	,	(12.01909443f	+	12.19998337f	+	11.94242466f)/3	,	(11.06667809f	+	10.76366918f	+	10.70083955f)/3},
		/*				{9.35959598f	+	8.760408981f	+	6.088167733f	+	10.03520563f	+	8.925660259f	+	5.33171039f		+	4.275100859f	+	3.851032434f	+	3.174049442f	+	4.554373759f	+	4.690858153f	+	2.802381691f	+*/		{	(9.337043017f	+	8.564559926f	+	5.85821347f	)/3	,	(10.03611282f	+	9.02960571f		+	5.49036451f	)/3	,	(4.34127191f	+	3.939580617f	+	2.985505256f)/3	,	(4.291524138f	+	4.698888953f	+	2.773521532f)/3},
		/*				{7.507128515f	+	7.440905061f	+	8.094832762f	+	6.309574293f	+	7.065648854f	+	7.835821909f	+	7.80158668f		+	7.934641802f	+	8.448770257f	+	8.230546795f	+	8.327929413f	+	8.40732011f		+*/		{	(7.641430723f	+	7.711561528f	+	8.148287128f)/3	,	(6.278521588f	+	7.063936327f	+	7.808408609f)/3	,	(7.804759987f	+	7.931586437f	+	8.345790381f)/3	,	(8.004979135f	+	8.42300179f		+	8.513288952f)/3},
		/*				{8.931664075f	+	8.682351687f	+	8.873200135f	+	9.311405042f	+	9.390521017f	+	9.495282098f	+	9.117950468f	+	8.702012741f	+	9.142829145f	+	8.800749132f	+	8.642231369f	+	8.793491304f	+*/		{	(8.904163697f	+	8.803126454f	+	9.197024723f)/3	,	(8.439698647f	+	8.36421071f		+	8.526549021f)/3	,	(8.1011291f		+	7.622765801f	+	8.34051305f	)/3	,	(8.700691856f	+	8.768719071f	+	8.910892173f)/3},
		/*				{6.606184004f	+	6.031608174f	+	6.374485461f	+	6.935391978f	+	7.635735671f	+	7.441293152f	+	6.046176512f	+	5.775605384f	+	6.091817146f	+	6.058802238f	+	7.109108951f	+	5.815400744f	+*/		{	(6.643619392f	+	5.934497999f	+	6.120400921f)/3	,	(6.935980993f	+	7.680969031f	+	7.373862462f)/3	,	(5.791238842f	+	5.670946512f	+	5.971533571f)/3	,	(6.032972756f	+	6.915650032f	+	5.839659974f)/3},
		/*				{12.03273835f	+	11.42834037f	+	11.46158477f	+	12.71779993f	+	11.78541887f	+	11.65119814f	+	11.51964774f	+	11.52208411f	+	11.86627842f	+	11.6430769f		+	11.40962606f	+	11.5209498f		+*/		{	(12.05478526f	+	11.60530117f	+	11.49771223f)/3	,	(12.89254178f	+	11.73854897f	+	11.50690158f)/3	,	(11.59019539f	+	11.74138873f	+	11.77885256f)/3	,	(11.65672021f	+	11.66269015f	+	11.43032294f)/3},
		/*				{6.776058938f	+	7.399316381f	+	7.628982848f	+	4.826754266f	+	6.029444569f	+	5.10644741f		+	7.266176487f	+	8.308253185f	+	8.207499742f	+	8.807762697f	+	9.31595014f		+	8.632927986f	+*/		{	(6.78522796f	+	7.394894124f	+	7.73539422f	)/3	,	(4.742628293f	+	6.038828548f	+	5.282998273f)/3	,	(7.464624784f	+	8.400966442f	+	8.177028789f)/3	,	(8.843872908f	+	9.246121749f	+	8.677455576f)/3},
		/*				{11.42280491f	+	9.582390065f	+	7.75765297f		+	12.1011861f		+	10.9287389f		+	9.525329563f	+	8.159804732f	+	8.59187888f		+	7.363144201f	+	9.112050272f	+	8.768064849f	+	7.236725595f	+*/		{	(11.38685106f	+	9.527776094f	+	7.701360662f)/3	,	(12.10171021f	+	10.92961922f	+	9.523685263f)/3	,	(8.068689232f	+	8.602430378f	+	7.462518615f)/3	,	(9.247704274f	+	8.951307003f	+	7.435387406f)/3},
		/*				{8.753829058f	+	8.62937362f		+	8.62289039f		+	7.85045032f		+	7.803872393f	+	7.748554361f	+	8.619168716f	+	9.243966122f	+	9.149066059f	+	8.5543875f		+	9.109409398f	+	8.564689637f	+*/		{	(8.700511103f	+	8.550795977f	+	8.43423938f	)/3	,	(7.957658717f	+	7.879857355f	+	7.763860613f)/3	,	(8.600552456f	+	9.233847873f	+	9.160250284f)/3	,	(8.659900595f	+	9.041112824f	+	8.476568494f)/3},
		/*				{10.68392312f	+	9.977140342f	+	9.859623852f	+	11.33564591f	+	11.00126526f	+	10.79958749f	+	10.03083625f	+	9.792075535f	+	10.0939871f		+	9.744832362f	+	9.996346789f	+	9.795779926f	+*/		{	(10.53418251f	+	9.879490651f	+	9.765520484f)/3	,	(10.17316395f	+	9.949708744f	+	9.883138652f)/3	,	(8.968556956f	+	8.772690196f	+	8.858671137f)/3	,	(9.803628489f	+	9.8869672f		+	9.593488566f)/3}	};
		
		FloatMatrix fm = new FloatMatrix(fmData);
		return fm;
	}
	public static FloatMatrix makeXExperimentMatrix(){
		float[][] fmData = 
		
		{		{11.93013822f	,	11.81087045f	,	11.05215758f	,	12.07948199f	,	11.63863935f	,	11.33517265f	,	11.06916488f	,	11.10281917f	,	11.02322754f	,	10.83908244f	,	10.75319141f	,	10.8626565f		,	11.69343674f	,	11.74531286f	,	11.16773626f	,	12.98257843f	,	12.78077297f	,	12.07392597f	,	12.01909443f	,	12.19998337f	,	11.94242466f	,	11.06667809f	,	10.76366918f	,	10.70083955f},
				{9.35959598f	,	8.760408981f	,	6.088167733f	,	10.03520563f	,	8.925660259f	,	5.33171039f		,	4.275100859f	,	3.851032434f	,	3.174049442f	,	4.554373759f	,	4.690858153f	,	2.802381691f	,	9.337043017f	,	8.564559926f	,	5.85821347f		,	10.03611282f	,	9.02960571f		,	5.49036451f		,	4.34127191f		,	3.939580617f	,	2.985505256f	,	4.291524138f	,	4.698888953f	,	2.773521532f},
				{7.507128515f	,	7.440905061f	,	8.094832762f	,	6.309574293f	,	7.065648854f	,	7.835821909f	,	7.80158668f		,	7.934641802f	,	8.448770257f	,	8.230546795f	,	8.327929413f	,	8.40732011f		,	7.641430723f	,	7.711561528f	,	8.148287128f	,	6.278521588f	,	7.063936327f	,	7.808408609f	,	7.804759987f	,	7.931586437f	,	8.345790381f	,	8.004979135f	,	8.42300179f		,	8.513288952f},
				{8.931664075f	,	8.682351687f	,	8.873200135f	,	9.311405042f	,	9.390521017f	,	9.495282098f	,	9.117950468f	,	8.702012741f	,	9.142829145f	,	8.800749132f	,	8.642231369f	,	8.793491304f	,	8.904163697f	,	8.803126454f	,	9.197024723f	,	8.439698647f	,	8.36421071f		,	8.526549021f	,	8.1011291f		,	7.622765801f	,	8.34051305f		,	8.700691856f	,	8.768719071f	,	8.910892173f},
				{6.606184004f	,	6.031608174f	,	6.374485461f	,	6.935391978f	,	7.635735671f	,	7.441293152f	,	6.046176512f	,	5.775605384f	,	6.091817146f	,	6.058802238f	,	7.109108951f	,	5.815400744f	,	6.643619392f	,	5.934497999f	,	6.120400921f	,	6.935980993f	,	7.680969031f	,	7.373862462f	,	5.791238842f	,	5.670946512f	,	5.971533571f	,	6.032972756f	,	6.915650032f	,	5.839659974f},
				{12.03273835f	,	11.42834037f	,	11.46158477f	,	12.71779993f	,	11.78541887f	,	11.65119814f	,	11.51964774f	,	11.52208411f	,	11.86627842f	,	11.6430769f		,	11.40962606f	,	11.5209498f		,	12.05478526f	,	11.60530117f	,	11.49771223f	,	12.89254178f	,	11.73854897f	,	11.50690158f	,	11.59019539f	,	11.74138873f	,	11.77885256f	,	11.65672021f	,	11.66269015f	,	11.43032294f},
				{6.776058938f	,	7.399316381f	,	7.628982848f	,	4.826754266f	,	6.029444569f	,	5.10644741f		,	7.266176487f	,	8.308253185f	,	8.207499742f	,	8.807762697f	,	9.31595014f		,	8.632927986f	,	6.78522796f		,	7.394894124f	,	7.73539422f		,	4.742628293f	,	6.038828548f	,	5.282998273f	,	7.464624784f	,	8.400966442f	,	8.177028789f	,	8.843872908f	,	9.246121749f	,	8.677455576f},
				{11.42280491f	,	9.582390065f	,	7.75765297f		,	12.1011861f		,	10.9287389f		,	9.525329563f	,	8.159804732f	,	8.59187888f		,	7.363144201f	,	9.112050272f	,	8.768064849f	,	7.236725595f	,	11.38685106f	,	9.527776094f	,	7.701360662f	,	12.10171021f	,	10.92961922f	,	9.523685263f	,	8.068689232f	,	8.602430378f	,	7.462518615f	,	9.247704274f	,	8.951307003f	,	7.435387406f},
				{8.753829058f	,	8.62937362f		,	8.62289039f		,	7.85045032f		,	7.803872393f	,	7.748554361f	,	8.619168716f	,	9.243966122f	,	9.149066059f	,	8.5543875f		,	9.109409398f	,	8.564689637f	,	8.700511103f	,	8.550795977f	,	8.43423938f		,	7.957658717f	,	7.879857355f	,	7.763860613f	,	8.600552456f	,	9.233847873f	,	9.160250284f	,	8.659900595f	,	9.041112824f	,	8.476568494f},
				{10.68392312f	,	9.977140342f	,	9.859623852f	,	11.33564591f	,	11.00126526f	,	10.79958749f	,	10.03083625f	,	9.792075535f	,	10.0939871f		,	9.744832362f	,	9.996346789f	,	9.795779926f	,	10.53418251f	,	9.879490651f	,	9.765520484f	,	10.17316395f	,	9.949708744f	,	9.883138652f	,	8.968556956f	,	8.772690196f	,	8.858671137f	,	9.803628489f	,	9.8869672f		,	9.593488566f}	};

		
		FloatMatrix fm = new FloatMatrix(fmData);
		return fm;
	}
/*	public static FloatMatrix makeYExperimentMatrix(){
		FloatMatrix fm = new FloatMatrix(numGenes1,numTimePoints);
		
		for (int gene = 0; gene<numGenes1; gene++){
			for (int i=0; i<numTimePoints; i++){
				fm.set(gene, i, XTreatAverage.get(gene, i)-XControlAverage.get(gene, i));
			}
		}
		return fm;
	}*/
	public static float diGamma(float in) {
		float ret;
		float x = in-1;
		ret = 1.111111111111f;
		//System.out.println(in+ " x "+x);
		for (float i = 1; i < 100000; i++){
			ret= ret + (1/i -1/(x+i));

			//System.out.println("1/i " + 1/i + " sdf " +1/(x+i));
			//System.out.println("ret "+ret);
		}
		//System.out.println(in+ " x "+x);
		return ret;
	}
	public static float triGamma(float z) {
		float ret;
		ret = 0;
		for (float i = 0; i < 30000; i++){
			ret= ret+1/(float)(Math.pow((z+i),2));
		}
		return ret;
	}
	public static float tetraGamma(float z) {
		float ret;
		ret = 0;
		for (float i = 0; i < 10000; i++){
			ret= ret-2/(float)(Math.pow((z+i),3));
		}
		return ret;
	}
	
    public static float logGamma(float x) {
        float ret;

        if (Float.isNaN(x) || (x <= 0.0)) {
            ret = Float.NaN;
        } else {
            float g = 607.0f / 128.0f;
            
            float sum = 0.0f;
            for (int i = lanczos.length - 1; i > 0; --i) {
                sum = sum + (lanczos[i] / (x + i));
            }
            sum = sum + lanczos[0];

            float tmp = x + g + .5f;
            ret = ((x + .5f) * (float)Math.log(tmp)) - tmp +
                HALF_LOG_2_PI + (float)Math.log(sum / x);
        }

        return ret;
    }

    public static float trigammaInverse(float x) {
        if (Float.isNaN(x)) 
            return Float.NaN;
        float y = 0.5f + 1/x;
        for (int i=0; i<50; i++){
            float tri = triGamma(y);
            float dif = tri * (1 - tri/x)/tetraGamma(y);
            y = y + dif;
        }
        return y;
    }
}
 