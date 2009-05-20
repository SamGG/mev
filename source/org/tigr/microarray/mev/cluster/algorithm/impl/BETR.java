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

import java.util.ArrayList;
import java.util.Vector;
import javax.swing.JOptionPane;

public class BETR extends AbstractAlgorithm{
    private int progress;
    private FloatMatrix expMatrix;
    private FloatMatrix filteredExpMatrix;
    private boolean stop = false;
    private int[] timeAssignments;
    private int[] conditionAssignments;
    private int[][] conditionsMatrix;  
    private int[][] sigGenesArrays= new int[2][];
    private int[] errorGenesArray = new int[0];
    private int[] errorGenesArray2 = new int[0];
    private int[] mapping, mapping2;

    private int numGenes, numExps, numTimePoints, iteration;
    private boolean  drawSigTreesOnly;
    private int hcl_function;
    private boolean hcl_absolute;
    private boolean hcl_genes_ordered;  
    private boolean hcl_samples_ordered; 
    
    private int dataDesign;
    
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
    	mapping = new int[expMatrix.getRowDimension()];
    	for (int i=0; i<mapping.length; i++){
    		mapping[i]=i;
    	}
    	mapping2 = new int[expMatrix.getRowDimension()];
    	for (int i=0; i<mapping2.length; i++){
    		mapping2[i]=i;
    	}
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
    	if (stop) {
    		throw new AbortException();
	    }
        numGenes= expMatrix.getRowDimension();
        numExps = expMatrix.getColumnDimension();

        if (dataDesign==1)
        	numTimePoints++;
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
    	//remap genes to expmatrix
    	int[][]sigReturn = new int[sigGenesArrays.length][];
    	for (int i=0; i<sigGenesArrays.length; i++){
    		sigReturn[i]=new int[sigGenesArrays[i].length];
    		for (int j=0; j<sigGenesArrays[i].length; j++){
    			sigReturn[i][j]=mapping2[mapping[sigGenesArrays[i][j]]];
    		}
    	}
    	ArrayList<Integer>tmpal = new ArrayList<Integer>();
    	
    	for (int i=0; i<errorGenesArray.length; i++){
    		if (!tmpal.contains(mapping2[errorGenesArray[i]]))
    			tmpal.add(mapping2[errorGenesArray[i]]);
    	}
    	for (int i=0; i<errorGenesArray2.length; i++){
    		if (!tmpal.contains(errorGenesArray2[i]))
    			tmpal.add(errorGenesArray2[i]);
    	}
    	int[]errReturn = new int[tmpal.size()];
    	for (int i=0; i<errReturn.length; i++){
    		errReturn[i]=tmpal.get(i);
    	}
    	
    	
    	// prepare the result
    	
    	
    	result.addIntMatrix("sigGenesArrays", sigReturn);
    	result.addParam("error-length", String.valueOf(errorGenesArray.length+errorGenesArray2.length));
    	result.addParam("iterations", String.valueOf(iteration-1));
    	result.addIntArray("error-genes", errReturn);
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
    	for (int i=0; i<mapping.length; i++){
    		
    		pvals.A[mapping2[mapping[i]]][0] =1-I[i]; 
    	}
    	return pvals;
    }
        
    private float getMean(float[] group) {
    	float sum = 0;
    	int n = 0;
    	
    	for (int i = 0; i < group.length; i++) {
    	    if (!Float.isNaN(group[i])) {
    		sum = sum + group[i];
    		n++;
    	    }
    	}
    	
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
		filteredExpMatrix = expMatrix.getMatrix(mapping2, 0, expMatrix.getColumnDimension()-1).getMatrix(mapping, 0, expMatrix.getColumnDimension()-1);
        numGenes= filteredExpMatrix.getRowDimension();
        numExps = filteredExpMatrix.getColumnDimension();
		if (dataDesign==1){
			FloatMatrix XTreatAve=new FloatMatrix(numGenes, numTimePoints);
			for (int i=0; i<numTimePoints; i++){  //i=1 to skip t0.
				for (int j=0; j<numGenes; j++){
					float s=0;
					float value;;
					int totals=0;
					for (int k=0; k<timeAssignments.length; k++){
						if ((timeAssignments[k]-1)==(i+1)){
							value = filteredExpMatrix.get(j,k); 
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
							value = filteredExpMatrix.get(j,k); 
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
							value = filteredExpMatrix.get(j,k); 
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
			XTreat.fillMatrix(filteredExpMatrix.getMatrix(0, numGenes-1, conditionsMatrix[0]));
			XControl.fillMatrix(filteredExpMatrix.getMatrix(0, numGenes-1, conditionsMatrix[1]));
			FloatMatrix XTreatAve=new FloatMatrix(numGenes, numTimePoints);
			for (int i=0; i<numTimePoints; i++){
				for (int j=0; j<numGenes; j++){
					float s=0;
					int totals=0;
					float value;
					for (int k=0; k<timeAssignments.length; k++){
						if ((timeAssignments[k]-1)==i&&(conditionAssignments[k]-1)==0){
							value = filteredExpMatrix.get(j,k); 
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
						if ((timeAssignments[k]-1)==i&&(conditionAssignments[k]-1)==1){
							value = filteredExpMatrix.get(j,k); 
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
		if (mapping.length<2){
			stop=true;
			JOptionPane.showMessageDialog(null, "Not enough valid genes", "Error", JOptionPane.WARNING_MESSAGE);
		}
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
	 	boolean foundBadData=false;
	 	ArrayList<Integer> badData = new ArrayList<Integer>();
	 	for (int i=0; i<numGenes; i++) {
	 		varianceError[i] = new FloatMatrix(numTimePoints,numTimePoints);
	 		varianceMean[i] = new FloatMatrix(numTimePoints,numTimePoints);
	 		for (int ii=0; ii<numTimePoints+1; ii++) {
	 			for (int sample=0; sample<numExps; sample++){
	 				
	 				if (timeAssignments[sample]-1==ii){
	 					float value =filteredExpMatrix.get(i, sample);
	 					if (Float.isNaN(value))
	 						continue;
	 					//1-Cond. scenario
	 					if (dataDesign==1){
		 					if (timeAssignments[sample]-1==0){
		 						seg = seg + (float)Math.pow((filteredExpMatrix.get(i,sample)-XControlAverage.get(i, ii)),2);
		 					}else{
		 						seg = seg + (float)Math.pow((filteredExpMatrix.get(i,sample)-XTreatAverage.get(i, ii-1)),2);
		 					}
	 					}
	 					//2-Cond. scenario
	 					if (dataDesign==2){
		 					if (conditionAssignments[sample]==1){
		 						seg = seg + (float)Math.pow((filteredExpMatrix.get(i,sample)-XTreatAverage.get(i, ii)),2);
		 					}
		 					if (conditionAssignments[sample]==2){
		 						seg = seg + (float)Math.pow((filteredExpMatrix.get(i,sample)-XControlAverage.get(i, ii)),2);
		 					}
	 					}
	 					//Paired-Data scenario
	 					if (dataDesign==3){
	 						seg = seg + (float)Math.pow((filteredExpMatrix.get(i,sample)-Y.get(i, ii)),2);
	 					}
	 				}
	 			}
	 		}
	 		for (int ii=0; ii<numTimePoints; ii++) {
	 			for (int iii=0; iii<numTimePoints; iii++) { 
	 				varianceMean[i].set(ii, iii, Y.get(i,ii)*Y.get(i,iii));
	 				if (Float.isNaN(Y.get(i,ii)*Y.get(i,iii))){
	 		 			badData.add(i);
	 		 			foundBadData=true;
	 		 		}
	 				
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
	 			badData.add(i);
	 			foundBadData=true;
	 		}
	 		
	 		for (int ii=0; ii<numTimePoints; ii++) {
	 			varianceError[i].set(ii, ii, seg);
	 		}
	 		seg = 0f;
	 	}
	 	if (foundBadData){
	 		    String[] options = {"Continue","Cancel"};
	 		    if(JOptionPane.showOptionDialog(null, "The data you are running contains groups of NaNs.\n" +
	 		    		"To remove these genes and restart the analysis, click 'Continue'",
	 		    		"Warning",1, JOptionPane.WARNING_MESSAGE, null,options,options[0])==1){
	 		    	stop=true;
	 		    }
	 		    if (stop)
	 		    	return;
	 		    
 				ArrayList<Integer> map = new ArrayList<Integer>();
 				for (int i=0; i<this.numGenes; i++){
 					map.add(i);
 				}
 				map.removeAll(badData);
 				mapping2 = new int[map.size()];
 				mapping = new int[mapping2.length];
 				for (int i=0; i<map.size(); i++){
 					mapping2[i]=map.get(i);
 					mapping[i]=i;
 				}
 		 		errorGenesArray2 = new int[badData.size()];
 		 		for (int i=0; i<errorGenesArray2.length; i++){
 		 			errorGenesArray2[i] = badData.get(i);
 		 		}
 		    	initializeExperiments();
 		    	runAlg();
 		    	return;
	 	}

		if (stop)
			return;

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
 		iteration = 1;
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
			for (int i=0; i<numGenes; i++){
				Shmg[i] = (varianceMean[i].plus(lambda.times(nu)).times((float)1/(1+nu)));
			}
 			//store values in float array of length n.
 			float[] f1mvnd = new float[numGenes];
 			FloatMatrix Yg = new FloatMatrix(numTimePoints, 1);
 			for (int i=0; i<numGenes; i++) {
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
 			boolean founderror=false;
 			for (int i=0; i<numGenes; i++) {
 				if (1-I[i]<alpha){
 					diffGenes.add(i);
 				}else{
 					if(Float.isNaN(I[i])){
 						errorGenes.add(i);
 						founderror=true;
 					}else{
 						nonDiffGenes.add(i);
 					}
 				}
 			}
 			if (founderror){
	 		    String[] options = {"Continue","Cancel"};
	 		    if(JOptionPane.showOptionDialog(null, "The data you are running contains invalid data.\n" +
	 		    		"To remove the invalid genes and re-run the analysis, click 'Continue'",
	 		    		"Warning",1, JOptionPane.WARNING_MESSAGE, null,options,options[0])==1){
	 		    	stop=true;
	 		    }
	 		    if (stop)
	 		    	return;
	 		    
 				ArrayList<Integer> map = new ArrayList<Integer>();
 				for (int i=0; i<this.numGenes; i++){
 					map.add(i);
 				}
 				map.removeAll(errorGenes);
 				mapping = new int[map.size()];
 				for (int i=0; i<map.size(); i++){
 					mapping[i]=map.get(i);
 				}
 		 		errorGenesArray = new int[errorGenes.size()];
 		 		for (int i=0; i<errorGenesArray.length; i++){
 		 			errorGenesArray[i] = errorGenes.get(i);
 		 		}
 		    	initializeExperiments();
 		    	runAlg();
 		    	return;
 			}
 			boolean sameresult=false;
 			if (diffGenes.size()==diffGenesOld.size()){
	 			for (int i=0; i<diffGenes.size();i++){
	 				sameresult = true;
	 				if (!diffGenes.get(i).equals(diffGenesOld.get(i))){
	 					sameresult = false;
	 					break;
	 				}
	 			}
 			}
 			if (diffGenes.size()==0)
 				break;
 			if (sameresult){
 				break;
 			}
 			if (stop)
 				break;
 			
 			diffGenesOld.clear();
 			for (int i=0; i<diffGenes.size(); i++) {
 					diffGenesOld.add(diffGenes.get(i));
 			}
 			p=Math.max((float)diffGenesOld.size()/(float)numGenes, 1f/(float)numGenes);
 		}
 		sigGenesArrays[0]=new int[diffGenes.size()];
 		sigGenesArrays[1]=new int[nonDiffGenes.size()];
 		
 		for (int i=0; i<sigGenesArrays[0].length; i++){
 			sigGenesArrays[0][i] = diffGenes.get(i);
 		}
 		for (int i=0; i<sigGenesArrays[1].length; i++){
 			sigGenesArrays[1][i] = nonDiffGenes.get(i);
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
	
	public static float diGamma(float in) {
		float ret;
		float x = in-1;
		ret = 1.111111111111f;
		for (float i = 1; i < 100000; i++){
			ret= ret + (1/i -1/(x+i));
		}
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
 