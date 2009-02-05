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
    
    private int progress;
    private FloatMatrix expMatrix;
    private boolean stop = false;
    private int[] timeAssignments;
    private int[] conditionAssignments;
    private int[][] conditionsMatrix;  
    private int[][] sigGenesArrays= new int[2][];
    private int[] errorGenesArray;

    private int numGenes, numExps, numTimePoints;
    private boolean  drawSigTreesOnly;
    private int hcl_function;
    private boolean hcl_absolute;
    private boolean hcl_genes_ordered;  
    private boolean hcl_samples_ordered; 
    
    private int dataDesign;
    
    
    float currentP = 0.0f;
    int currentIndex = 0; 
    double constant;
    
    
    
    
    private AlgorithmEvent event;
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
        hcl_function = map.getInt("hcl-distance-function", EUCLIDEAN);
        hcl_absolute = map.getBoolean("hcl-distance-absolute", false);     
        hcl_genes_ordered = map.getBoolean("hcl-genes-ordered", false);  
        hcl_samples_ordered = map.getBoolean("hcl-samples-ordered", false);     
        numTimePoints = map.getInt("numTimePoints");
        if (dataDesign==1)
        	numTimePoints--;
        numGenes= expMatrix.getRowDimension();
        numExps = expMatrix.getColumnDimension();
        alpha = map.getFloat("alpha-value");
    	boolean hierarchical_tree = map.getBoolean("hierarchical-tree", false);
            if (hierarchical_tree) {
                drawSigTreesOnly = map.getBoolean("draw-sig-trees-only");
            }        
    	int method_linkage = map.getInt("method-linkage", 0);
    	boolean calculate_genes = map.getBoolean("calculate-genes", false);
    	boolean calculate_experiments = map.getBoolean("calculate-experiments", false);
    	


    	progress=0;
    	event = null;
    	event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Initializing...");
    	// set progress limit
    	fireValueChanged(event);
    	event.setId(AlgorithmEvent.PROGRESS_VALUE);
    	event.setIntValue(0);
    	fireValueChanged(event);
    	
    	initializeExperiments();
    	runAlg();
    
    	AlgorithmData result = new AlgorithmData();
    	FloatMatrix means =getMeans(sigGenesArrays);       
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
	private int numTreatReps = 3;// = number of treatment replicates;
	private int numConReps = 3;// = number of control replicates;
	//private static int numGenes; //number of genes		
	private float p=.05f;// p-value.	
	private float alpha;// significance level.
	private float[] I;
	//-Initialize two 3-Dimensional float arrays of length n (number of genes), width and height k (number of time-points).
	private FloatMatrix[] varianceError;// is the error variance.
	private FloatMatrix[] varianceMean;// is the mean variance.
	private FloatMatrix[] Shmg;
	private FloatMatrix[] Sheg;
 	private Experiment XTreat = new Experiment(null, null, null);
 	private Experiment XControl = new Experiment(null, null, null);
 	private Experiment XTreatAverage = new Experiment(null, null, null);
 	private Experiment XControlAverage = new Experiment(null, null, null);
 	private Experiment Y = new Experiment(null, null, null);
 	
	
	public void initializeExperiments(){
		if (dataDesign==1){
		
			FloatMatrix XTreatAve=new FloatMatrix(numGenes, numTimePoints);
			for (int i=0; i<numTimePoints; i++){  //i=1 to skip t0.
				for (int j=0; j<numGenes; j++){
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
			FloatMatrix XControlAve=new FloatMatrix(numGenes, numTimePoints);
			for (int i=0; i<numTimePoints; i++){
				for (int j=0; j<numGenes; j++){
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
			FloatMatrix XTreatAve=new FloatMatrix(numGenes, numTimePoints);
			for (int i=0; i<numTimePoints; i++){
				for (int j=0; j<numGenes; j++){
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
			XTreat.fillMatrix(expMatrix.getMatrix(0, numGenes-1, conditionsMatrix[0]));
			XControl.fillMatrix(expMatrix.getMatrix(0, numGenes-1, conditionsMatrix[1]));
		
			FloatMatrix XTreatAve=new FloatMatrix(numGenes, numTimePoints);
			for (int i=0; i<numTimePoints; i++){
				for (int j=0; j<numGenes; j++){
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
			FloatMatrix XControlAve=new FloatMatrix(numGenes, numTimePoints);
			for (int i=0; i<numTimePoints; i++){
				for (int j=0; j<numGenes; j++){
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
		
		progress++;
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue(10);
    	fireValueChanged(event);

	 	//-Estimate Variance components:
	 	float seg = 0; 
	 	varianceError = new FloatMatrix[numGenes];
	 	varianceMean = new FloatMatrix[numGenes];
	 	Sheg = new FloatMatrix[numGenes];
	 	Shmg = new FloatMatrix[numGenes];
	 	for (int i=0; i<numGenes; i++) {
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
	 		    JOptionPane.showMessageDialog(null, "Invalid Data!", "Error", JOptionPane.WARNING_MESSAGE);
                stop = true;
	 		}
	 		for (int ii=0; ii<numTimePoints; ii++) {
	 			varianceError[i].set(ii, ii, seg);
	 		}
	 		seg = 0f;
	 	}


 		//Estimate gene-specific variance components, ~Seg.
		//Find SBar:
		FloatMatrix SeBar = new FloatMatrix(numTimePoints,numTimePoints);
		float sume = 0;
		for(int ii=0; ii<numTimePoints; ii++){
			for(int iii=0; iii<numTimePoints; iii++){
				for(int i=0; i<numGenes; i++){
					sume = sume + varianceError[i].get(ii,iii);
				} 
				SeBar.set(ii,iii, sume/(float)numGenes);	
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
		float[] eg = new float[numGenes];
		float dg=0f;
		if (dataDesign==1)
			dg= (float)(  (numTreatReps-1)*(numTimePoints+1));
		if (dataDesign==2)
			dg= (float)(2*(numTreatReps-1)* numTimePoints);
		if (dataDesign==3)
			dg= (float)(  (numTreatReps-1)* numTimePoints);
		
		//time intensive loop
		for (int i=0; i<numGenes; i++){
			eg[i] = (float)( Math.log(varianceError[i].get(0, 0)) - diGamma(dg/2) + Math.log(dg/2) );
			
		} 
		for (int i=0;i<numGenes; i++){
			ebar = ebar+eg[i];
		}
		

		ebar=ebar/(float)numGenes;
		
		//VERY time intensive loop
		for (int i=0;i<numGenes; i++){
			totalmti=totalmti+((float)Math.pow(eg[i]-ebar, 2)*numGenes/(float)(numGenes-1))-triGamma(dg/2);
		}

		
		d0=2*trigammaInverse((float)totalmti/numGenes);
		s0 = (float)Math.exp(ebar + diGamma(d0/2)- Math.log(d0/2));
		float[] segtilde = new float[numGenes];
		for (int i=0; i<numGenes; i++){
			segtilde[i]=(float)((d0*s0)+dg*varianceError[i].get(0, 0))/(d0+dg);
		
		}

		updateProgressBar();
    	fireValueChanged(event);
		

		for (int i=0; i<numGenes; i++){
			Sheg[i] = new FloatMatrix(numTimePoints, numTimePoints);
			for (int j = 0; j<numTimePoints; j++){
				Sheg[i].set(j, j, segtilde[i]);
			}
		}
		
		for (int t=0; t<numTimePoints; t++){
			totalnue = totalnue+d0; 
		}
		 	//Estimate gene-specific variance components diagonal matrix ~seg with d0 and s0(page 8).
		 		//from previous step, we have the values for d0 and s0.
		 		//float ~seg = (d0*s0^2 + dg*sg^2)/(d0 + dg);
		 		
		
		 	//Estimate gene-specific multivariate normal density for the null hypothesis
		 	//	store values in float array of length n.
	 	float[] f0mvnd = new float[numGenes];
	 	for (int i=0; i<numGenes; i++) {
	 		FloatMatrix Yg = new FloatMatrix(numTimePoints,1);
	 		for (int yi = 0; yi<numTimePoints; yi++){
	 			Yg.set(yi,0, Y.get(i, yi));
	 		}
			
			f0mvnd[i] = multivariateNormalFunction(Sheg[i].times((float)(numConReps+numTreatReps)/(numConReps*numTreatReps)), Yg);
			
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
 			event.setDescription("Running Shrinkage Loop. Iteration #"+iteration);
			updateProgressBar();
	    	fireValueChanged(event);
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
				ebarm=ebarm/(float)numGenes;
			
				
				for (int i=0;i<diffGenes.size(); i++){
					totalmtim=totalmtim+((float)Math.pow((egm[i]-ebarm), 2)*diffGenes.size()/(float)(diffGenes.size()-1))-triGamma(dgm/2);
				}
					
				d0t[k]=2*trigammaInverse((float)totalmtim/diffGenes.size());
 			}
			
			for (int k=0; k<numTimePoints; k++){
				totalnu = totalnu+d0t[k]; 
			}
			float nu = totalnu/numTimePoints;
			FloatMatrix lambda;
			float tempnu = Math.max(nu, numTimePoints+6);
			lambda = SBar.times((tempnu-numTimePoints-1)/tempnu);

		
			//Estimate gene-specific variance components:
			//System.out.println("nu="+ nu);
			for (int i=0; i<numGenes; i++){
				Shmg[i] = (varianceMean[i].plus(lambda.times(nu)).times((float)1/(1+nu)));
			}
 			//f1(Yg) = (1/(Math.pow((2*Math.pi),numTimePoints/2)*Math.sqrt(determinant of (Shmg + (numConReps+numTreatReps)/(numConReps*numTreatReps))varianceError[gene])))*Math.exp((-1/2)Yg.transpose*(~Smg+(numConReps+numTreatReps)/(numConReps*numTreatReps))VarianceError[gene].invert*Yg);
 			//store values in float array of length n.
 			float[] f1mvnd = new float[numGenes];
 			FloatMatrix Yg = new FloatMatrix(numTimePoints, 1);
 			for (int i=0; i<numGenes; i++) {
	 			//FloatMatrix Yg = new FloatMatrix(numTimePoints, 1);
	 			for (int yi = 0; yi<numTimePoints; yi++){
	 				Yg.set(yi, 0, Y.get(i, yi));
	 			}
 				f1mvnd[i] = multivariateNormalFunction(Shmg[i].plus(Sheg[i].times((float)(numConReps+numTreatReps)/(numConReps*numTreatReps))), Yg);
 			}
 			for (int yi = 0; yi<numTimePoints; yi++){
 				Yg.set(yi, 0, Y.get(0, yi));
 			}
 			
 			//Determine next group of differentially expressed genes-
 			I = new float[numGenes];// = probability of differential expression.
 			
 			for (int i=0; i<numGenes; i++) {
 				I[i] = f1mvnd[i]*p/(f0mvnd[i]*(1-p)+f1mvnd[i]*p);
 			}
 			//Re-evaluate diffGenes.
 			
 			diffGenes.clear();
 			nonDiffGenes.clear();
 			errorGenes.clear();

 			for (int i=0; i<numGenes; i++) {
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
 			boolean sameresult=false;
 			if (diffGenes.size()==diffGenesOld.size()){
	 			for (int i=0; i<diffGenes.size();i++){
	 				sameresult = true;
	 				//System.out.print("i="+i+" "+diffGenes.get(i)+ " : "+diffGenesOld.get(i)+ " answer=");System.out.println(diffGenes.get(i)!=diffGenesOld.get(i));
	 				if (!diffGenes.get(i).equals(diffGenesOld.get(i))){
	 					sameresult = false;
	 					break;
	 				}
	 			}
 			}
 			if (sameresult){
 				break;
 			}
 			if (stop)
 				break;
 			
 			diffGenesOld.clear();
 			for (int i=0; i<diffGenes.size(); i++) {
 					diffGenesOld.add(diffGenes.get(i));
 			}
 			p=(float)diffGenesOld.size()/(float)numGenes;
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
 		
 		
 		
	}
	public void updateProgressBar(){

		progress++;
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue((100*progress)/(progress+7));
	}
	public float multivariateNormalFunction(FloatMatrix coMat, FloatMatrix Yg){
		
		
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

    public float trigammaInverse(float x) {
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
 