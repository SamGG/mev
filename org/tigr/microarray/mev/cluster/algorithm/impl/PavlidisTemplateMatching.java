/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: PavlidisTemplateMatching.java,v $
 * $Revision: 1.2 $
 * $Date: 2003-12-11 15:43:20 $
 * $Author: nbhagaba $
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

import JSci.maths.statistics.TDistribution;

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

public class PavlidisTemplateMatching extends AbstractAlgorithm {
    private boolean stop = false;
    
    private int function;
    private float factor;
    private boolean absolute;
    
    private int number_of_genes;
    private int number_of_samples;
    
    private FloatMatrix expMatrix;
    private FloatMatrix newMatrix;
    
    private long StartTime;
    private long CalculationTime;
    private boolean ptmGenes;
    
    private double[] pValues, rValues;
    
    
    boolean useAbsolute; // true = use absolute value of correlation; false = use the signed value of the correlation
    boolean useR;
    JButton abortButton;
    
    Vector templateVector;
    FloatMatrix templateVectorMatrix; //NEED TO IMPORT TEMPLATE VECTOR AS A MATRIX BECAUSE "ALGORITHMDATA" CLASS ONLY HAS getMatrix() METHOD
    //NO WAY TO IMPORT A VECTOR DIRECTLY, EITHER USING CONFMAP OR ALGORITHMDATA CLASS
    float[] geneTemplate;    
    float threshold;    
    int origNumGenes, numSamples;
    
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
	
	
	AlgorithmParameters map = data.getParams();
	
	function = map.getInt("distance-function", EUCLIDEAN);
	factor   = map.getFloat("distance-factor", 1.0f);
	absolute = map.getBoolean("distance-absolute", false);
	
        ptmGenes = map.getBoolean("ptm-cluster-genes", true);
	threshold = map.getFloat("threshold", 0.8f);
	useAbsolute = map.getBoolean("use-absolute", false);
	useR = map.getBoolean("useR", false);
	templateVectorMatrix = data.getMatrix("templateVectorMatrix");
	templateVector = convertToVector(templateVectorMatrix);
	
	boolean hierarchical_tree = map.getBoolean("hierarchical-tree", false);
	int method_linkage = map.getInt("method-linkage", 0);
	boolean calculate_genes = map.getBoolean("calculate-genes", false);
	boolean calculate_experiments = map.getBoolean("calculate-experiments", false);
	
	this.expMatrix = data.getMatrix("experiment");
	
	number_of_genes   = this.expMatrix.getRowDimension();
	number_of_samples = this.expMatrix.getColumnDimension();
	
	origNumGenes = this.expMatrix.getRowDimension();
	numSamples = this.expMatrix.getColumnDimension();
	
	geneTemplate = new float[templateVector.size()];
        
        pValues = new double[number_of_genes];
        rValues = new double[number_of_genes];
	
	for (int i = 0; i < templateVector.size(); i++) {
	    geneTemplate[i] = ((Float)templateVector.get(i)).floatValue();
	}
	
	
	Vector clusters[] = calculate();
	
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
        
        FloatMatrix rValuesMatrix = new FloatMatrix(number_of_genes, 1);
        FloatMatrix pValuesMatrix = new FloatMatrix(number_of_genes, 1);     
        
        for (int i = 0; i < pValues.length; i++) {
            rValuesMatrix.A[i][0] = (float)(rValues[i]);
            pValuesMatrix.A[i][0] = (float)(pValues[i]);
        }
	
	// prepare the result
	AlgorithmData result = new AlgorithmData();
	result.addCluster("cluster", result_cluster);
	result.addMatrix("clusters_means", means);
	result.addMatrix("clusters_variances", variances);
        result.addMatrix("rValuesMatrix", rValuesMatrix);
        result.addMatrix("pValuesMatrix", pValuesMatrix);
	return result;
    }
    
    
    private NodeValueList calculateHierarchicalTree(int[] features, int method, boolean genes, boolean experiments) throws AlgorithmException {
	NodeValueList nodeList = new NodeValueList();
	AlgorithmData data = new AlgorithmData();
	FloatMatrix experiment;
        
        if(ptmGenes)
            experiment = getSubExperiment(this.expMatrix, features);
        else
            experiment = this.getSubExperimentReducedCols(this.expMatrix, features);
        
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
     *  Creates a matrix with reduced columns (samples) as during experiment clustering
     */
    private FloatMatrix getSubExperimentReducedCols(FloatMatrix experiment, int[] features) {
        FloatMatrix copyMatrix = experiment.copy();
        FloatMatrix subExperiment = new FloatMatrix(features.length, copyMatrix.getColumnDimension());
        for (int i=0; i<features.length; i++) {
            subExperiment.A[i] = copyMatrix.A[features[i]];
        }
        subExperiment = subExperiment.transpose();
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
	    int_matrix[i] = ((Integer) source.get(i)).intValue();
	}
	return int_matrix;
    }
    
    public void abort() {
	stop = true;
    }
    
    private Vector convertToVector(FloatMatrix tempMatrix) {
	Vector temp = new Vector();
	for (int i = 0; i < tempMatrix.A[0].length; i++) {
	    temp.add(new Float(tempMatrix.A[0][i]));
	}
	return temp;
    }
    
    
    public Vector itf(Vector integerVector) {
	Vector floatVector = new Vector();
	
	for (int i = 0; i < integerVector.size(); i++) {
	    floatVector.addElement(new Float(((Integer) integerVector.elementAt(i)).intValue()));
	}
	
	return floatVector;
    }
    
    
    Vector[] calculate() {
	
	Vector allUniqueIDIndices = new Vector();
	for(int i = 0; i < origNumGenes; i++) {
	    allUniqueIDIndices.add(new Integer(i));
	}
	
	Vector remainingGenes = (Vector)(allUniqueIDIndices).clone();
	StartTime=System.currentTimeMillis();
	Vector similarGenes = findSimilarGenes();
	remainingGenes.removeAll(similarGenes);
	CalculationTime=System.currentTimeMillis()-StartTime;
	
	Vector[] clusters = new Vector[2];
	clusters[0] = similarGenes;
	clusters[1] = remainingGenes;
	
	return clusters;
    }
    
    
    FloatMatrix addTemplateToexpMatrix() {
	FloatMatrix newMatrix = new FloatMatrix(origNumGenes + 1, numSamples);
	for (int i = 0; i < origNumGenes; i++) { //copy all the elements of expMatrix into newMatrix
	    for (int j = 0; j < numSamples; j++) {
		newMatrix.A[i][j] = expMatrix.A[i][j];
	    }
	}
	
	for (int k = 0; k < geneTemplate.length; k++) {
	    newMatrix.A[origNumGenes][k] = geneTemplate[k];
	}
	/*
	for (int i = 0; i < origNumGenes + 1; i++) {
	    for (int j = 0; j < numSamples; j++) {
		
	    }
	}
         */
	
	return newMatrix;
    }
    
    
    Vector findSimilarGenes() {
	Vector similarGenes = new Vector();
	double pearsonR;
	newMatrix = addTemplateToexpMatrix();
	
	
	Vector allUniqueIDIndices = new Vector();
	for(int i = 0; i < origNumGenes; i++) {
	    allUniqueIDIndices.add(new Integer(i));
	}
	
	for (int i = 0; i < origNumGenes; i++){
	    pearsonR = ExperimentUtil.genePearson(newMatrix, null, i, origNumGenes, factor);
            rValues[i] = pearsonR;
            pValues[i] = getProb(pearsonR);
	    if(useR) {
		
		if (useAbsolute == true) {
		    pearsonR = Math.abs(pearsonR);
		    if (pearsonR >= threshold) {
			similarGenes.add(allUniqueIDIndices.elementAt(i));
		    }
		} else {
		    if (pearsonR >= threshold) {
			similarGenes.add(allUniqueIDIndices.elementAt(i));
		    }
		}
		
	    } else { // if (!useR)
		
		if (useAbsolute == true) {
		    if (getProb(pearsonR)<= threshold) {
			similarGenes.add(allUniqueIDIndices.elementAt(i));
		    }
		    
		} else {
		    if ((pearsonR > 0)&&(getProb(pearsonR)<= threshold)) {
			similarGenes.add(allUniqueIDIndices.elementAt(i));
		    }
		}
	    }
	    
	}
	
	return similarGenes;
    }
    
    
    double getProb(double pearsonR){
	
	double prob;
	double tValue = getTValue(Math.abs(pearsonR));
	int df = geneTemplate.length - 2;
	
	TDistribution tDist = new TDistribution(df);
	//prob = tDist.probability(tValue);
        prob = 2*(1-tDist.cumulative(tValue));
        
        if (prob > 1.0d) {
            prob = 1.0d;
        }
	
	return prob;
    }
    //
    
    double getTValue(double pearsonR){ //as explained on pg 333, Jaccard and Becker
	double tValue;
	double stdError;
	int n = geneTemplate.length;
	
	stdError = Math.sqrt((1-pearsonR*pearsonR)/(n - 2));
	
	tValue = pearsonR/stdError;
	
	return tValue;
    }
    
    
    private FloatMatrix getMeans(Vector[] clusters) {
	FloatMatrix means = new FloatMatrix(clusters.length, number_of_samples);
	FloatMatrix mean;
	for (int i=0; i<clusters.length; i++) {
	    mean = getMean(clusters[i]);
	    means.A[i] = mean.A[0];
	}
	return means;
    }
    
    private FloatMatrix getMean(Vector cluster) {
	FloatMatrix mean = new FloatMatrix(1, number_of_samples);
	float currentMean;
	int n = cluster.size();
	int denom = 0;
	float value;
	for (int i=0; i<number_of_samples; i++) {
	    currentMean = 0f;
	    denom  = 0;
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
	validN = 0;
	float value;
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
    
}
