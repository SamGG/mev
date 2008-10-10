/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: RP.java,v $
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
import org.tigr.microarray.mev.cluster.gui.impl.rp.RPInitBox;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;

//import JSci.maths.statistics.FDistribution;

import java.util.ArrayList;
import java.util.Random;
//import java.util.Random;
import java.util.Vector;

import javax.swing.JOptionPane;

public class RP extends AbstractAlgorithm{
    public static final int FALSE_NUM = 12;
    public static final int FALSE_PROP = 13;  
    
    private static int progress;
/*    private int function;
    private float factor;
    private boolean absolute, calculateAdjFDRPVals;*/
    private static FloatMatrix expMatrix;
    private FloatMatrix experimentData;
    private float alpha;
    private boolean stop = false;
    //private int[] timeAssignments;
    private int[] inGroupAssignments;
//    private int[][] conditionsMatrix;  
    private int[][] sigGenesArrays= new int[2][];
//    private int[] errorGenesArray;

    private int numGenes, numExps;
    private float falseProp;
    private boolean  drawSigTreesOnly;//usePerms,;
    private int numPerms, falseNum;
    private int correctionMethod;    
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
    	//timeAssignments = data.getIntArray("time_assignments");
    	inGroupAssignments = data.getIntArray("group_assignments");
    	int q =0;
    	for (int i=0; i<inGroupAssignments.length; i++){
    		if (inGroupAssignments[i] ==1)
    			q++;
    	}
    	int[] groupAssignments=new int[q];
    	q=0;
    	for (int i=0; i<inGroupAssignments.length; i++){
    		if (inGroupAssignments[i] ==1){
    			groupAssignments[q]=i;
    			q++;
    		}
    	}
    	experimentData = expMatrix.getMatrix(0, expMatrix.getRowDimension()-1, groupAssignments);
    	AlgorithmParameters map = data.getParams();
    	dataDesign = map.getInt("dataDesign");
    	
//    	conditionsMatrix = data.getIntMatrix("conditions-matrix");
//    	function = map.getInt("distance-function", EUCLIDEAN);
//    	factor   = map.getFloat("distance-factor", 1.0f);
//    	absolute = map.getBoolean("distance-absolute", false);
        numPerms = map.getInt("numPerms", 0);
        correctionMethod = map.getInt("correction-method");
        System.out.println("correction-method = " +correctionMethod);
        if (correctionMethod == RPInitBox.FALSE_NUM)
        	falseNum = map.getInt("falseNum");
        if (correctionMethod == RPInitBox.FALSE_PROP)
        	falseProp = map.getFloat("falseProp");
        System.out.println(falseNum);
        hcl_function = map.getInt("hcl-distance-function", EUCLIDEAN);
        hcl_absolute = map.getBoolean("hcl-distance-absolute", false);     
        hcl_genes_ordered = map.getBoolean("hcl-genes-ordered", false);  
        hcl_samples_ordered = map.getBoolean("hcl-samples-ordered", false);     
//        numTimePoints = map.getInt("numTimePoints");
//        if (dataDesign==1)
//        	numTimePoints--;
        numGenes1= experimentData.getRowDimension();
        numGenes= numGenes1;
        numExps = experimentData.getColumnDimension();
        alpha = map.getFloat("alpha");
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
    	event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Permuting Random Data...");
    	// set progress limit
    	fireValueChanged(event);
    	event.setId(AlgorithmEvent.PROGRESS_VALUE);
    	event.setIntValue(0);
    	fireValueChanged(event);
    	
    	initializeExperiments();

        System.out.println("RPinit");
    	runAlg();
    	//stop = true;

        System.out.println("RP0");
    	AlgorithmData result = new AlgorithmData();
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
    	System.out.println("RP3");
    	for (int i=0; i<sigGenesArrays.length; i++) {
    		System.out.println("RP4");
    		System.out.println(stop);
    	    if (stop) {
    		throw new AbortException();
    	    }
    	    features = sigGenesArrays[i];
    	    System.out.println("RP5");
    	    Node node = new Node(features);
    	    System.out.println("RP6");
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

        System.out.println("RP2");
    	// prepare the result
    	result.addIntMatrix("sigGenesArrays", sigGenesArrays);
    	//result.addParam("error-length", String.valueOf(errorGenesArray.length));
    	//result.addIntArray("error-genes", errorGenesArray);
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
//        if (dataDesign==2){
//        	result.addMatrix("geneConditionMeansMatrix", getAllGeneConditionMeans());
//        	result.addMatrix("geneConditionSDsMatrix", getAllGeneConditionSDs());       
//        }
        result.addMatrix("pValues", getPValues());
        result.addMatrix("qValues", getQValues());

        System.out.println("RP1");
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
    	FloatMatrix experiment = getSubExperiment(experimentData, features);
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
    		value = experimentData.get(((Integer) cluster[j]).intValue(), i);
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
    	    value = experimentData.get(((Integer) cluster[i]).intValue(), column);
    	    if (!Float.isNaN(value)) {
    		sum += Math.pow(value-mean, 2);
    		validN++;
    	    }
    	}
    	return sum;
    }
    

    

    
    
        private float[] getGeneGroupMeans(int gene) {     
        	float[] geneValues = new float[numExps];
            for (int i = 0; i < numExps; i++) {
    	    geneValues[i] = experimentData.A[gene][i];
            } 
            
            float[][] geneValuesByGroups = new float[1][];
            
            for (int i = 0; i < 1; i++) {
                geneValuesByGroups[i] = getGeneValuesForGroup(geneValues, i+1);
            } 
            
            float[] geneGroupMeans = new float[1];
            for (int i = 0; i < 1; i++) {
                geneGroupMeans[i] = getMean(geneValuesByGroups[i]);
            }
            
            return geneGroupMeans;
        }
        
//        private float[] getGeneConditionMeans(int gene) {
//        	float[] geneValues = new float[numExps];
//            for (int i = 0; i < numExps; i++) {
//            	geneValues[i] = experimentData.A[gene][i];
//            } 
//            
//            float[][] geneValuesByCondition = new float[2][];
//            
//            for (int i = 0; i < 2; i++) {
//                geneValuesByCondition[i] = getGeneValuesForGroup(geneValues, i+1);
//            } 
//            
//            float[] geneGroupMeans = new float[2];
//            for (int i = 0; i < 2; i++) {
//                geneGroupMeans[i] = getMean(geneValuesByCondition[i]);
//            }
//            
//            return geneGroupMeans;
//        }
        
        private float[] getGeneGroupSDs(int gene) {
        	float[] geneValues = new float[numExps];        
            for (int i = 0; i < numExps; i++) {
            	geneValues[i] = experimentData.A[gene][i];
            }   
            
            float[][] geneValuesByGroups = new float[1][];
            
            for (int i = 0; i < 1; i++) {
                geneValuesByGroups[i] = getGeneValuesForGroup(geneValues, i+1);
            }  
            
            float[] geneGroupSDs = new float[1];
            for (int i = 0; i < 1; i++) {
                geneGroupSDs[i] = getStdDev(geneValuesByGroups[i]);
            }
            
            return geneGroupSDs;        
        }
        
//        private float[] getGeneConditionSDs(int gene) {
//        	float[] geneValues = new float[numExps];        
//            for (int i = 0; i < numExps; i++) {
//            	geneValues[i] = experimentData.A[gene][i];
//            }   
//            
//            float[][] geneValuesByGroups = new float[2][];
//            
//            for (int i = 0; i < 2; i++) {
//                geneValuesByGroups[i] = getGeneValuesForCondition(geneValues, i+1);
//            }  
//            
//            float[] geneGroupSDs = new float[2];
//            for (int i = 0; i < 2; i++) {
//                geneGroupSDs[i] = getStdDev(geneValuesByGroups[i]);
//            }
//            
//            return geneGroupSDs;        
//        }
        
        private float[] getGeneValuesForGroup(float[] geneValues, int group) {
            Vector<Float> groupValuesVector = new Vector<Float>();
            
            for (int i = 0; i < geneValues.length; i++) {
                
                groupValuesVector.add(new Float(geneValues[i]));
                
            }
            
            float[] groupGeneValues = new float[groupValuesVector.size()];
            
            for (int i = 0; i < groupValuesVector.size(); i++) {
                groupGeneValues[i] = ((Float)(groupValuesVector.get(i))).floatValue();
            }
            
            return groupGeneValues;
        }
        
//        private float[] getGeneValuesForCondition(float[] geneValues, int condition) {
//            Vector<Float> groupValuesVector = new Vector<Float>();
//            
//            for (int i = 0; i < inGroupAssignments.length; i++) {
//                if (inGroupAssignments[i] == condition) {
//                    groupValuesVector.add(new Float(geneValues[i]));
//                }
//            }
//            
//            float[] groupGeneValues = new float[groupValuesVector.size()];
//            
//            for (int i = 0; i < groupValuesVector.size(); i++) {
//                groupGeneValues[i] = ((Float)(groupValuesVector.get(i))).floatValue();
//            }
//            
//            return groupGeneValues;
//        }
        
        private FloatMatrix getAllGeneTimeMeans() {
            FloatMatrix means = new FloatMatrix(numGenes, 1);
            for (int i = 0; i < means.getRowDimension(); i++) {
                means.A[i] = getGeneGroupMeans(i);
            }
            return means;
        }
        
//        private FloatMatrix getAllGeneConditionMeans() {
//            FloatMatrix means = new FloatMatrix(numGenes, 2);
//            for (int i = 0; i < means.getRowDimension(); i++) {
//                means.A[i] = getGeneConditionMeans(i);
//            }
//            return means;
//        }
        
        private FloatMatrix getAllGeneTimeSDs() {
            FloatMatrix sds = new FloatMatrix(numGenes, 1);
            for (int i = 0; i < sds.getRowDimension(); i++) {
                sds.A[i] = getGeneGroupSDs(i);
            }
            return sds;        
        }
        
//        private FloatMatrix getAllGeneConditionSDs() {
//            FloatMatrix sds = new FloatMatrix(numGenes, 2);
//            for (int i = 0; i < sds.getRowDimension(); i++) {
//                sds.A[i] = getGeneConditionSDs(i);
//            }
//            return sds;        
//        }
        
        private FloatMatrix getPValues(){
        	FloatMatrix pvals = new FloatMatrix(numGenes, 1);
        	for (int i=0; i<pvals.getRowDimension(); i++){
        		pvals.A[i][0] =pVals[i]; 
        	}
        	return pvals;
        }
        private FloatMatrix getQValues(){
        	FloatMatrix pvals = new FloatMatrix(numGenes, 1);
        	for (int i=0; i<pvals.getRowDimension(); i++){
        		pvals.A[i][0] =qVals[i]; 
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

	private static int numGenes1; //number of genes	
	private float[] qVals;
	private float[] pVals;
// 	private static Experiment XTreat = new Experiment(null, null, null);
// 	private static Experiment XControl = new Experiment(null, null, null);
// 	private static Experiment XTreatAverage = new Experiment(null, null, null);
// 	private static Experiment XControlAverage = new Experiment(null, null, null);
// 	private static Experiment Y = new Experiment(null, null, null);
 	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		doAlg();
	}
	private static void doAlg(){
		double[][] adata =makeXExperimentMatrix();
		double[] rankArray = new double[adata[0].length];
		for (int i=0; i<rankArray.length; i++){
			rankArray[i]=i;
		}
	    for (int i=0; i< (adata.length); i++){
	    	ExperimentUtil.sort2(adata[i], rankArray);
	    	
	    	for (int j=0; j<adata[i].length; j++){
	    		adata[i][j]=j;
	    	}
	    	ExperimentUtil.sort2(rankArray, adata[i]);
	    	//ExperimentUtil.sort2(adata[0], adata[i]);
	    }
	    for (int i=0; i<rankArray.length; i++){
	    	for (int j=0; j<adata.length; j++){
	    		if (adata[j][i]<10.0){
	    			System.out.print(adata[j][i] + "      ");
	    		}else{
	    			System.out.print(adata[j][i] + "     ");
	    		}
	    	}
	    	System.out.println(rankArray[i]);
		}
	    
	    
	    double[] rankProductArray = new double[rankArray.length];
	    double[] rankProductArray2 = new double[rankArray.length];
	    for (int i = 0; i<rankProductArray.length; i++){
	    	double rankProduct = 1;
	    	for (int j = 0; j<adata.length; j++){
	    		rankProduct = rankProduct*adata[j][i];
	    	}
	    	//System.out.println(rankProduct);
	    	//System.out.println((Math.pow(rankArray.length, adata.length)));
	    	rankProductArray[i] = rankProduct/(Math.pow(rankArray.length, adata.length));
	    	rankProductArray2[i] = (Math.pow(rankProduct, 1/(double)adata.length));
	    	rankProduct = 1;
	    	//System.out.println(rankProductArray[i]+"         " +rankProductArray2[i]);
	    	//System.out.println(rankProductArray2[i]);
	    }
	    System.out.println();
	    ExperimentUtil.sort2(rankProductArray, rankProductArray2);
	    for (int i = 0; i<rankProductArray.length; i++){
	    	//System.out.println(rankProductArray[i]+"         " +rankProductArray2[i]);
	    }
	    
	    
	    
	    //begin permutation-based estimation
	    int numberPerms =3;
	    double[][]rankProductMatrix = new double[3][];
	    for (int permutations=0; permutations<numberPerms; permutations++){
		    int[][] intRanksMatrix = new int[adata.length][];
		    int[][] permutedRanksMatrix = new int[adata.length][];
		    for (int i=0; i<adata.length; i++){
			    	intRanksMatrix[i] = new int[adata[i].length];
			    	permutedRanksMatrix[i] = new int[adata[i].length];
		    	for (int j=0; j<adata[i].length; j++){
		    		
			    	intRanksMatrix[i][j] = (int)adata[i][j];
			    }
		    }
		    for (int i=0; i<adata.length; i++){
		    	permutedRanksMatrix[i] = getPermutedValues(adata[i].length, intRanksMatrix[i]);
		    }
		    for (int i=0; i<rankArray.length; i++){
		    	for (int j=0; j<permutedRanksMatrix.length; j++){
		    		if (permutedRanksMatrix[j][i]<10.0){
		    			System.out.print(permutedRanksMatrix[j][i] + "      ");
		    		}else{
		    			System.out.print(permutedRanksMatrix[j][i] + "     ");
		    		}
		    	}
		    	System.out.println(rankArray[i]);
			}
		    
		    
		    //calculate rank products for random rankings
		    double[] permutedRankProductArray = new double[rankArray.length];
		    double[] permutedRankProductArray2 = new double[rankArray.length];
		    for (int i = 0; i<permutedRankProductArray.length; i++){
		    	double permutedRankProduct = 1;
		    	for (int j = 0; j<adata.length; j++){
		    		permutedRankProduct = permutedRankProduct*adata[j][i];
		    	}
		    	//System.out.println(rankProduct);
		    	//System.out.println((Math.pow(rankArray.length, adata.length)));
		    	permutedRankProductArray[i] = permutedRankProduct/(Math.pow(rankArray.length, adata.length));
		    	permutedRankProductArray2[i] = (Math.pow(permutedRankProduct, 1/(double)adata.length));
		    	permutedRankProduct = 1;
		    	//System.out.println(rankProductArray[i]+"         " +rankProductArray2[i]);
		    	//System.out.println(rankProductArray2[i]);
		    }
	    
	    }
		System.out.println("test");
	}
//	public static void initializeExperimentsLocal(){
//
//		System.out.println("Initializing...");
//		XTreat.fillMatrix(makeXTExperimentMatrix());
//		XTreatAverage.fillMatrix(makeXTAExperimentMatrix());
//		XControl.fillMatrix(makeXCExperimentMatrix());
//		XControlAverage.fillMatrix(makeXCAExperimentMatrix());
//		//Y.fillMatrix(makeYExperimentMatrix());
//	}
	public void initializeExperiments(){
		System.out.println("Initializing...");
	}
	public void runAlg(){
		System.out.println("Running algorithm...");
		double[][] adata = new double[experimentData.getColumnDimension()][experimentData.getRowDimension()];
		for (int i=0; i<experimentData.getColumnDimension(); i++){
			for (int j=0; j<experimentData.getRowDimension(); j++){
				adata[i][j] = experimentData.get(j, i);
			}
		}
		double[] rankArray = new double[adata[0].length];
		for (int i=0; i<rankArray.length; i++){
			rankArray[i]=i;
		}
	    for (int i=0; i< (adata.length); i++){
	    	ExperimentUtil.sort2(adata[i], rankArray);
	    	
	    	for (int j=0; j<adata[i].length; j++){
	    		adata[i][j]=j+1.0;
	    	}
	    	ExperimentUtil.sort2(rankArray, adata[i]);
	    	//ExperimentUtil.sort2(adata[0], adata[i]);
	    }
	    for (int i=0; i<rankArray.length; i++){
	    	for (int j=0; j<adata.length; j++){
	    		if (adata[j][i]<10.0){
	    			System.out.print(adata[j][i] + "      ");
	    		}else{
	    			System.out.print(adata[j][i] + "     ");
	    		}
	    	}
	    	System.out.println(rankArray[i]);
		}
	    double[] rankProductArray = new double[rankArray.length];
	    double[] rankProductArray2 = new double[rankArray.length];
	    for (int i = 0; i<rankProductArray.length; i++){
	    	double rankProduct = 1;
	    	for (int j = 0; j<adata.length; j++){
	    		rankProduct = rankProduct*adata[j][i];
	    	}
	    	//System.out.println(rankProduct);
	    	//System.out.println((Math.pow(rankArray.length, adata.length)));
	    	rankProductArray[i] = rankProduct/(Math.pow(rankArray.length, adata.length));
	    	rankProductArray2[i] = (Math.pow(rankProduct, 1/(double)adata.length));
	    	rankProduct = 1;
	    	System.out.println(rankProductArray[i]+"         " +rankProductArray2[i]);
	    	//System.out.println(rankProductArray2[i]);
	    }
//	    System.out.println();
//	    ExperimentUtil.sort2(rankProductArray, rankProductArray2);
//	    for (int i = 0; i<rankProductArray.length; i++){
//	    	System.out.println(rankProductArray[i]+"         " +rankProductArray2[i]);
//	    }
	    
	    //begin permutation-based estimation
	    double[][]rankProductMatrix = new double[numPerms][adata[0].length];
	    for (int permutations=0; permutations<numPerms; permutations++){
	    	if (stop)
	    		break;
	    	
	    	updateProgressBar();
		    int[][] intRanksMatrix = new int[adata.length][];
		    int[][] permutedRanksMatrix = new int[adata.length][];
		    for (int i=0; i<adata.length; i++){
			    	intRanksMatrix[i] = new int[adata[i].length];
			    	permutedRanksMatrix[i] = new int[adata[i].length];
		    	for (int j=0; j<adata[i].length; j++){
		    		
			    	intRanksMatrix[i][j] = (int)adata[i][j]-1;
			    }
		    }
		    for (int i=0; i<adata.length; i++){
		    	permutedRanksMatrix[i] = getPermutedValues(adata[i].length, intRanksMatrix[i]);
		    }
//		    for (int i=0; i<rankArray.length; i++){
//		    	for (int j=0; j<permutedRanksMatrix.length; j++){
//		    		if (permutedRanksMatrix[j][i]<10.0){
//		    			System.out.print(permutedRanksMatrix[j][i] + "      ");
//		    		}else{
//		    			System.out.print(permutedRanksMatrix[j][i] + "     ");
//		    		}
//		    	}
//		    	System.out.println(rankArray[i]);
//			}
		    
		    
		    //calculate rank products for random rankings
		    for (int i = 0; i<rankProductMatrix[permutations].length; i++){
		    	double permutedRankProduct = 1;
		    	for (int j = 0; j<adata.length; j++){
		    		permutedRankProduct = permutedRankProduct*(permutedRanksMatrix[j][i]+1);
		    	}
		    	//System.out.println(rankProduct);
		    	//System.out.println((Math.pow(rankArray.length, adata.length)));
		    	rankProductMatrix[permutations][i] = permutedRankProduct/(Math.pow(rankArray.length, adata.length));
		    	//rankProductMatrix2[permutations][i] = (Math.pow(permutedRankProduct, 1/(double)adata.length));
		    	permutedRankProduct = 1;
		    	//System.out.println(rankProductArray[i]+"         " +rankProductArray2[i]);
		    }
	    }
//	    for (int i=0; i<rankProductMatrix.length; i++){
//	    	for (int j=0; j<rankProductMatrix[i].length; j++){
//	    		System.out.print(rankProductMatrix[i][j]+ "  ");
//	    	}
//	    	System.out.println();
//	    }
	    double[] expectedP = new double[adata[0].length];
	    int totalC=0;
	    for (int h=0; h<expectedP.length;h++){
	    	totalC=0;
		    for (int i=0; i<rankProductMatrix.length; i++){
		    	for (int j=0; j<rankProductMatrix[i].length; j++){
		    		if (rankProductMatrix[i][j]<rankProductArray[h])
		    			totalC++;
		    		
		    	}
		    }
		    expectedP[h]=(double)totalC/numPerms;
	    }
	    System.out.println("expectedP[i]");
	    for (int i=0; i<expectedP.length; i++){
	    	System.out.println(expectedP[i]);
	    }
	    double[] qValues = new double[expectedP.length];
	    int[] rankg = new int[expectedP.length];
	    for(int i=0; i<qValues.length;i++){
	    	int rankTotal = 0;
	    	for(int j=0; j<qValues.length;j++){
		    	if (expectedP[j]<=expectedP[i])
		    		rankTotal++;
		    }
	    	rankg[i]=rankTotal;
	    }
	    System.out.println("   rankg");
	    for (int i=0; i<rankg.length; i++){
	    	System.out.println(rankg[i]);
	    }
	    //for(int j=0; j<qValues)
	    for(int i=0; i<qValues.length;i++){
	    	qValues[i] = expectedP[i]/rankg[i];
	    }
	    ExperimentUtil.sort2(expectedP, qValues);
	    System.out.println("   qValues");
	    for (int i=0; i<rankg.length; i++){
	    	System.out.println("expectedP "+expectedP[i] + "   qValue "+qValues[i]);
	    }

 		qVals =new float[qValues.length];
 		for (int i=0; i<qVals.length; i++){
 			qVals[i]=(float)qValues[i];
 		}
 		pVals=new float[qValues.length];
 		for (int i=0; i<pVals.length; i++){
 			pVals[i]=(float)expectedP[i]/this.numGenes;
 		}
	    int sigGenesCounter=0;
	    if (correctionMethod == RPInitBox.JUST_ALPHA){

		    for (int i=0; i<rankg.length; i++){
		    	if(pVals[i]<=alpha)
		    		sigGenesCounter++;
		    }
	    }
	    if (correctionMethod == RPInitBox.FALSE_PROP){
		    for (int i=0; i<rankg.length; i++){
		    	if(qVals[i]<=falseProp)
		    		sigGenesCounter++;
		    }
	    }
	    if (correctionMethod == RPInitBox.FALSE_NUM){
		    for (int i=0; i<rankg.length; i++){
		    	if(expectedP[i]<=falseNum)
		    		sigGenesCounter++;
		    }
	    }
	    	
	    
	    
	    
	    sigGenesArrays[0]=new int[sigGenesCounter];
 		sigGenesArrays[1]=new int[qValues.length-sigGenesCounter];
 		int counters=0;
 		int counteri=0;

	    if (correctionMethod == RPInitBox.JUST_ALPHA){
	    	for (int i=0; i<rankg.length; i++){
		    	if(pVals[i]<=alpha){
		    		sigGenesArrays[0][counters] = i;
		    		counters++;
		    	}
		    	if(pVals[i]>alpha){
		    		sigGenesArrays[1][counteri] = i;
		    		counteri++;
		    	}
		    }
	    }
	    if (correctionMethod == RPInitBox.FALSE_PROP){
			for (int i=0; i<rankg.length; i++){
		    	if(qValues[i]<=falseProp){
		    		sigGenesArrays[0][counters] = i;
		    		counters++;
		    	}
		    	if(qValues[i]>falseProp){
		    		sigGenesArrays[1][counteri] = i;
		    		counteri++;
		    	}
		    }
	    }
	    if (correctionMethod == RPInitBox.FALSE_NUM){
	    	for (int i=0; i<rankg.length; i++){
		    	if(expectedP[i]<=falseNum){
		    		sigGenesArrays[0][counters] = i;
		    		counters++;
		    	}
		    	if(expectedP[i]>falseNum){
		    		sigGenesArrays[1][counteri] = i;
		    		counteri++;
		    	}
		    }
	    }
		System.out.println("test");
	}

	protected static int[] getPermutedValues(int arrayLength, int[] validArray) {//returns an integer array of length "arrayLength", with the valid values (the currently included experiments) permuted
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
	
	private void updateProgressBar(){

		progress++;
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue((100*progress)/(numPerms));
    	fireValueChanged(event);
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
	public static double[][] makeXExperimentMatrix(){
		double[][] fmData = 
		
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

		
		return fmData;
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

}
 