/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: Ttest.java,v $
 * $Revision: 1.6 $
 * $Date: 2005-02-24 20:23:47 $
 * $Author: braistedj $
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
import org.tigr.util.Combinations;
import org.tigr.util.FloatMatrix;
import org.tigr.util.QSort;

import JSci.maths.statistics.TDistribution;

public class Ttest extends AbstractAlgorithm {
    
    public static final int GROUP_A = 1;
    public static final int GROUP_B = 2;
    public static final int NEITHER_GROUP = 3;
    public static final int JUST_ALPHA = 4;
    public static final int STD_BONFERRONI = 5;
    public static final int ADJ_BONFERRONI = 6;
    public static final int BETWEEN_SUBJECTS = 7;
    public static final int ONE_CLASS = 8;
    public static final int MAX_T = 9;
    public static final int MIN_P = 10; 
    public static final int PAIRED = 11;
    public static final int FALSE_NUM = 12;
    public static final int FALSE_PROP = 13;    
    
    private boolean stop = false;
    
    private int function;
    private float factor;
    private boolean absolute;
    private FloatMatrix expMatrix;
    
    boolean hierarchical_tree, drawSigTreesOnly;
    int method_linkage;
    boolean calculate_genes;
    boolean calculate_experiments;
    
    private Vector[] clusters;
    private int k; // # of clusters
    
    private int numGenes, numExps, falseNum;
    private float alpha, falseProp;
    private int significanceMethod;
    private boolean isPermut, useWelchDf, calculateAdjFDRPVals, useFastFDRApprox;
    private int[] groupAssignments, pairedGroupAExpts, pairedGroupBExpts;
    private int numCombs;
    boolean useAllCombs;
    int tTestDesign;
    float oneClassMean = 0.0f;  
    
    AlgorithmEvent event;
    
    double[] tValues, oneClassMeans, groupAMeans, groupBMeans, oneClassSDs, groupASDs, groupBSDs, dfValues, origPVals, adjustedPVals; 
    private boolean[] isSig;

    private int hcl_function;
    private boolean hcl_absolute;
    
    /** This method should interrupt the calculation.
     */
    public void abort() {
        stop = true;
    }
    
    /** This method execute calculation and return result,
     * stored in <code>AlgorithmData</code> class.
     *
     * @param data the data to be calculated.
     */
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
        /*
        if ((tTestDesign == ONE_CLASS) || (tTestDesign == BETWEEN_SUBJECTS)) {
            groupAssignments = data.getIntArray("group-assignments");
        }
        
        if (tTestDesign == PAIRED) {
            FloatMatrix pairedAExptsMatrix = data.getMatrix("pairedAExptsMatrix");
            FloatMatrix pairedBExptsMatrix = data.getMatrix("pairedBExptsMatrix");
            pairedGroupAExpts = new int[pairedAExptsMatrix.getRowDimension()];
            pairedGroupBExpts = new int[pairedBExptsMatrix.getRowDimension()];
            for (int i = 0; i < pairedAExptsMatrix.getRowDimension(); i++) {
                pairedGroupAExpts[i] = (int)(pairedAExptsMatrix.A[i][0]);
                pairedGroupBExpts[i] = (int)(pairedBExptsMatrix.A[i][0]);
            }
        }  
         */      
        
        AlgorithmParameters map = data.getParams();
        function = map.getInt("distance-function", EUCLIDEAN);
        factor   = map.getFloat("distance-factor", 1.0f);
        absolute = map.getBoolean("distance-absolute", false);

        hcl_function = map.getInt("hcl-distance-function", EUCLIDEAN);
        hcl_absolute = map.getBoolean("hcl-distance-absolute", false);
        
        hierarchical_tree = map.getBoolean("hierarchical-tree", false);
        if (hierarchical_tree) {
            drawSigTreesOnly = map.getBoolean("draw-sig-trees-only");
        }
        method_linkage = map.getInt("method-linkage", 0);
        calculate_genes = map.getBoolean("calculate-genes", false);
        calculate_experiments = map.getBoolean("calculate-experiments", false);
        
        this.expMatrix = data.getMatrix("experiment");
        
        numGenes = this.expMatrix.getRowDimension();
        numExps = this.expMatrix.getColumnDimension();
        tTestDesign = map.getInt("tTestDesign", BETWEEN_SUBJECTS);
        if (tTestDesign == ONE_CLASS) {
            oneClassMean = map.getFloat("oneClassMean", 0.0f);
        }
        alpha = map.getFloat("alpha", 0.01f);
        significanceMethod = map.getInt("significance-method", JUST_ALPHA);
        isPermut = map.getBoolean("is-permut", false);
        useWelchDf = map.getBoolean("useWelchDf", true);
        numCombs = map.getInt("num-combs", 100);
        useAllCombs = map.getBoolean("use-all-combs", false);  
        
        if ((significanceMethod == FALSE_NUM) || (significanceMethod == FALSE_PROP)) {
            calculateAdjFDRPVals = map.getBoolean("calculateAdjFDRPVals", false);
            useFastFDRApprox = map.getBoolean("useFastFDRApprox", true);
        }
        
        if (significanceMethod == FALSE_NUM) {
            falseNum = map.getInt("falseNum", 10);
        }
        
        if (significanceMethod == FALSE_PROP) {
            falseProp = map.getFloat("falseProp", 0.05f);
        }
        
        if ((tTestDesign == ONE_CLASS) || (tTestDesign == BETWEEN_SUBJECTS)) {
            groupAssignments = data.getIntArray("group-assignments");
        }
        
        if (tTestDesign == PAIRED) {
            FloatMatrix pairedAExptsMatrix = data.getMatrix("pairedAExptsMatrix");
            FloatMatrix pairedBExptsMatrix = data.getMatrix("pairedBExptsMatrix");
            pairedGroupAExpts = new int[pairedAExptsMatrix.getRowDimension()];
            pairedGroupBExpts = new int[pairedBExptsMatrix.getRowDimension()];
            for (int i = 0; i < pairedAExptsMatrix.getRowDimension(); i++) {
                pairedGroupAExpts[i] = (int)(pairedAExptsMatrix.A[i][0]);
                pairedGroupBExpts[i] = (int)(pairedBExptsMatrix.A[i][0]);
            }
        }           
        
        if ((significanceMethod == FALSE_NUM) || (significanceMethod == FALSE_PROP)) {
            if (tTestDesign == BETWEEN_SUBJECTS) {
                computeBtnSubOrigVals();
            } else if (tTestDesign == ONE_CLASS) {
                computeOneClassOrigVals();
            } else if (tTestDesign == PAIRED) {
                computePairedOrigVals();
            }
            origPVals = getRawPValsFromTDist();
            
        }  else {     
        
            if (tTestDesign == BETWEEN_SUBJECTS) {
                computeBtnSubOrigVals();
                if (!isPermut) {
                    origPVals = getRawPValsFromTDist();
                } else {
                    origPVals = getTwoClassRawPValsFromPerms();
                }
            } else if (tTestDesign == ONE_CLASS) {
                computeOneClassOrigVals();
                if (!isPermut) {
                    origPVals = getRawPValsFromTDist();
                } else {
                    origPVals = getOneClassRawPValsFromPerms();
                }
            } else if (tTestDesign == PAIRED) {
                computePairedOrigVals();
                if (!isPermut) {
                    origPVals = getRawPValsFromTDist();
                } else {
                    origPVals = getPairedRawPValsFromPerms();
                }
                //return null; //for now
            }
        }
        
        Vector clusterVector = new Vector();
        Vector sigGenes = new Vector();
        Vector nonSigGenes = new Vector();   
        
        if ( (significanceMethod == Ttest.FALSE_NUM) || (significanceMethod == Ttest.FALSE_PROP) ) {
            boolean[] isGeneSig = new boolean[1];
            if (significanceMethod == Ttest.FALSE_NUM) {
                isGeneSig = isGeneSigByFDRNum();
            } else {
                isGeneSig = isGeneSigByFDRPropNew2();
            }
                        
            if (!calculateAdjFDRPVals) {
                adjustedPVals = new double[numGenes];
            }
            //System.out.println("isGeneSig.length = " + isGeneSig.length);
            for (int i = 0; i < numGenes; i++) {
                if (isGeneSig[i]) {
                    sigGenes.add(new Integer(i));
                } else {
                    nonSigGenes.add(new Integer(i));
                }
            }
            
        } else { // if ! ((significanceMethod == this.FALSE_NUM) || (significanceMethod == this.FALSE_PROP))
            
            adjustedPVals = getAdjPVals(origPVals, significanceMethod);
            
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
                
                float currAdjP = (float)(adjustedPVals[i]);
                //System.out.println("currAdjP: gene" + i + " = " + currAdjP);
                if (significanceMethod == ADJ_BONFERRONI) {// because we have to break out of the loop if a non-sig value is encountered
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
        
        FloatMatrix rawPValuesMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix adjPValuesMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix tValuesMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix dfMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix meansAMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix meansBMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix sdAMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix sdBMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix isSigMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix oneClassMeansMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix oneClassSDsMatrix = new FloatMatrix(numGenes, 1);
        
        for (int i = 0; i < isSigMatrix.getRowDimension(); i++) {
            isSigMatrix.A[i][0] = 0.0f;
        }        
        
        for (int i = 0 ; i < sigGenes.size(); i++) {
            int currentGene = ((Integer)(sigGenes.get(i))).intValue();
            isSigMatrix.A[currentGene][0] = 1.0f;
        }      
        
        for (int i = 0; i < numGenes; i++) {
            rawPValuesMatrix.A[i][0] = (float)(origPVals[i]);
            adjPValuesMatrix.A[i][0] = (float)(adjustedPVals[i]);
            tValuesMatrix.A[i][0] = (float)(tValues[i]);
            dfMatrix.A[i][0] = (float)(dfValues[i]);
            if ((tTestDesign == BETWEEN_SUBJECTS) ||(tTestDesign == PAIRED)){
                meansAMatrix.A[i][0] = (float)(groupAMeans[i]);
                meansBMatrix.A[i][0] = (float)(groupBMeans[i]);
                sdAMatrix.A[i][0] = (float)(groupASDs[i]);
                sdBMatrix.A[i][0] = (float)(groupBSDs[i]);
            } else if (tTestDesign == ONE_CLASS) {
                oneClassMeansMatrix.A[i][0] = (float)(oneClassMeans[i]);
                oneClassSDsMatrix.A[i][0] = (float)(oneClassSDs[i]);
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
        //result.addParam("unassigned-genes-exist", String.valueOf(unassignedExists));
        result.addMatrix("clusters_means", means);
        result.addMatrix("clusters_variances", variances);
        //result.addMatrix("sigPValues", sigPValuesMatrix);
        //result.addMatrix("sigTValues", sigTValuesMatrix);
        //result.addMatrix("nonSigPValues", nonSigPValuesMatrix);
        //result.addMatrix("nonSigTValues", nonSigTValuesMatrix);
        result.addMatrix("rawPValues", rawPValuesMatrix);
        result.addMatrix("adjPValues", adjPValuesMatrix);
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
    
    private boolean[] isGeneSigByFDRPropOld() throws AlgorithmException {
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
        
        double yKZero = getYConservative(alpha, 0);
        
        if (sortedOrigPVals[0] >= yKZero) {
            return isGeneSig;
            
        } else {
            isGeneSig[sortedIndices[0]] = true;
            if (useFastFDRApprox) {
                for (int i = 1; i < sortedOrigPVals.length; i++) {
                    int rGamma = (int)(Math.floor((i+1)*falseProp));
                    int rMinusOneGamma = (int)(Math.floor(i*falseProp));
                    double yKRGamma = getYConservative(alpha, rGamma);
                    System.out.println("rGamma = " + rGamma + ", (r - 1)Gamma = " + rMinusOneGamma + ", yKRGamma = " + yKRGamma);
                    if ((rGamma > rMinusOneGamma) || (sortedOrigPVals[i] < yKRGamma)) {
                       isGeneSig[sortedIndices[i]] = true; 
                    } else {
                        break;
                    }
                }
            }
        }
     
        return isGeneSig;
        //return null; // for now
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
    
    private boolean[] isGeneSigByFDRPropNew() throws AlgorithmException {
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
        
        FloatMatrix sortedExpMatrix = new FloatMatrix(expMatrix.getRowDimension(), expMatrix.getColumnDimension());
        for (int i = 0; i < expMatrix.getRowDimension(); i++) {
            for (int j = 0; j < expMatrix.getColumnDimension(); j++) {
                sortedExpMatrix.A[i][j] = expMatrix.A[sortedIndices[i]][j];
            }
        }
        
        double yKZero = getYConservative(alpha, 0);
        
        if (sortedOrigPVals[0] >= yKZero) {
            return isGeneSig;
            
        }  else {  
            isGeneSig[sortedIndices[0]] = true;
            if (useFastFDRApprox) {
                int currMinGene = 1;
                int currMaxGene = 100;
                if (currMaxGene > (numGenes - 1)) currMaxGene = (numGenes - 1);
                int rGammaPrev = (int)(Math.floor(2*falseProp));
                //double yKRGamma =  getYKFromPMatrix(pValMatrix, rGammaPrev);
                
                boolean sig = true;
                while (sig) {
                    System.out.println("Entering while, sig = " + sig);
                    if (currMinGene > (numGenes - 1)) {
                        break;
                    }
                    if (currMaxGene > (numGenes - 1)) {
                        currMaxGene = (numGenes - 1);
                        sig = false;
                    }
                    System.out.println("currMinGene = " + currMinGene + ", currMaxGene = " + currMaxGene);
                    FloatMatrix smallMatrix = sortedExpMatrix.getMatrix(0, currMaxGene, 0, sortedExpMatrix.getColumnDimension() - 1);
                    double[][] pValMatrix = getSortedPermPValMatrix(smallMatrix);
                    double yKRGamma =  getYKFromPMatrix(pValMatrix, rGammaPrev);
                    for (int i = currMinGene; i <= currMaxGene; i++) {
                        int rGamma = (int)(Math.floor((i+1)*falseProp));
                        int rMinusOneGamma = (int)(Math.floor(i*falseProp));
                        if (rGamma > currMaxGene) {
                            //currMinGene = currMaxGene + 1;
                            //currMaxGene = currMaxGene + 100;
                            break;
                        } else {
                            System.out.println("i = " + i +", rGamma = " + rGamma + ",  rMinusOneGamma = " + rMinusOneGamma + ", rGammaPrev = " + rGammaPrev);
                            //FloatMatrix smallMatrix = expMatrix.getMatrix(0, currMaxGene, 0, expMatrix.getColumnDimension() - 1);
                            //double[][] pValMatrix = getSortedPermPValMatrix(smallMatrix);  
                            if (rGamma != rGammaPrev) {
                                yKRGamma =  getYKFromPMatrix(pValMatrix, rGamma);
                                rGammaPrev = rGamma;
                            }
                            System.out.println("yKRGamma = " + yKRGamma);
                            //double yKRGamma =  getYKFromPMatrix(pValMatrix, rGamma);
                            if ((rGamma > rMinusOneGamma) || (sortedOrigPVals[i] < yKRGamma)) {
                                isGeneSig[sortedIndices[i]] = true;
                                System.out.println("true");
                            } else {
                                sig = false;
                                System.out.println("false");
                                break;
                            }
                        }
                        
                        currMinGene = currMaxGene + 1;
                        currMaxGene = currMaxGene + 100;                        
                        
                    }
                }
            }
        }
        
        //return null; // for now
        return isGeneSig;
    }
    
    private double getYKFromPMatrix(double[][] pMatrix, int u) {
        double[] uPlusOnePValArray = new double[pMatrix[u].length];
        for (int i = 0; i < pMatrix[u].length; i++) {
            uPlusOnePValArray[i] = pMatrix[u][i];
        }
        
        QSort sortPValArray = new QSort(uPlusOnePValArray, QSort.ASCENDING);
        double[] sortedPValArray = sortPValArray.getSortedDouble();
        int selectedIndex = (int)Math.floor(sortedPValArray.length*alpha) - 1;
        //System.out.println("Selected index (before setting to zero) = " + selectedIndex);
        if (selectedIndex < 0) selectedIndex = 0;        
        
        return sortedPValArray[selectedIndex];        
    }
    
    private double[][] getSortedPermPValMatrix(FloatMatrix inputMatrix) throws AlgorithmException {
        AlgorithmEvent event2 = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numCombs);
        fireValueChanged(event2);
        event2.setId(AlgorithmEvent.PROGRESS_VALUE);        
        double[][] pValMatrix = new double[inputMatrix.getRowDimension()][numCombs];
        if (tTestDesign == Ttest.BETWEEN_SUBJECTS) {
            if (!useAllCombs) {
                for (int i = 0; i < numCombs; i++) {
                    if (stop) {
                        throw new AbortException();
                    }
                    event2.setIntValue(i);
                    event2.setDescription("Permuting matrix: Current permutation = " + (i+1));
                    fireValueChanged(event2);   
                    
                    int[] permutedExpts = new int[1];
                    Vector validExpts = new Vector();
                    
                    for (int j = 0; j < groupAssignments.length; j++) {
                        if (groupAssignments[j] != Ttest.NEITHER_GROUP) {
                            validExpts.add(new Integer(j));
                        }
                    }
                    
                    int[] validArray = new int[validExpts.size()];
                    for (int j = 0; j < validArray.length; j++) {
                        validArray[j] = ((Integer)(validExpts.get(j))).intValue();
                    }
                    
                    permutedExpts = getPermutedValues(numExps, validArray); //returns an int array of size "numExps", with the valid values permuted
                    FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
                    double[] currentPermTValues = getTwoClassUnpairedTValues(permutedMatrix);
                    int[] currPermDfValues = getTwoClassDfs(permutedMatrix);
                    
                    double[] currPermPVals = getParametricPVals(currentPermTValues, currPermDfValues);
                    
                    for (int j = 0; j < currPermPVals.length; j++) {
                        if (Double.isNaN(currPermPVals[j])) currPermPVals[j] = Double.POSITIVE_INFINITY; 
                        // this is to push NaNs to the end of the sorted array, since otherwise they would mess up quantile calculations 
                    }
                    
                    QSort sortCurrPVals = new QSort(currPermPVals, QSort.ASCENDING);
                    double[] sortedCurrPVals = sortCurrPVals.getSortedDouble();                    
                    
                    for (int j = 0; j < pValMatrix.length; j++) {
                        pValMatrix[j][i] = sortedCurrPVals[i];
                    }
                    
                }
            }
        }
        return pValMatrix; 
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
    
    /*
    private double getY(double alphaQuantile, double[] T, int u) {
        QSort sortT = new QSort(T);
        double[] sortedT = sortT.getSortedDouble();
        double[] smallT = new double[u + 1];
        for (int i = 0; i < smallT.length; i++) {
            smallT[i] = sortedT[i];
        }
        int selectedIndex = (int)Math.floor(smallT.length*alphaQuantile) - 1;
        System.out.println("Selected index (before setting to zero) = " + selectedIndex);
        if (selectedIndex < 0) selectedIndex = 0;
        return smallT[selectedIndex];
    }
     */
    
    private double[] getYKArray() throws AlgorithmException {
        AlgorithmEvent event2 = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numCombs);
        fireValueChanged(event2);
        event2.setId(AlgorithmEvent.PROGRESS_VALUE);  
        int maxRGamma = (int)(Math.floor(numGenes*falseProp));
        double[][] pValArray =new double[maxRGamma + 1][numCombs];
        if (tTestDesign == ONE_CLASS) {
            if (!useAllCombs) {
                boolean[] changeSign = new boolean[1];
                
                Random rand  = new Random();
                long[] randomSeeds  = new long[numCombs];
                for (int i = 0; i < numCombs; i++) {
                    randomSeeds[i] = rand.nextLong();
                }
                
                for (int i = 0; i < numCombs; i++) {
                    if (stop) {
                        throw new AbortException();
                    }
                    event2.setIntValue(i);
                    event2.setDescription("Permuting matrix: Current permutation = " + (i+1));
                    fireValueChanged(event2);
                    
                    int[] permutedExpts = new int[1];
                    Vector validExpts = new Vector();
                    
                    for (int j = 0; j < groupAssignments.length; j++) {
                        if (groupAssignments[j] == 1) {
                            validExpts.add(new Integer(j));
                        }
                    }
                    
                    int[] validArray = new int[validExpts.size()];
                    for (int j = 0; j < validArray.length; j++) {
                        validArray[j] = ((Integer)(validExpts.get(j))).intValue();
                    }
                    
                    changeSign = getOneClassChangeSignArray(randomSeeds[i], validArray);
                    FloatMatrix permutedMatrix = getOneClassPermMatrix(expMatrix, changeSign);
                    
                    double[] currentPermTValues = getOneClassTValues(permutedMatrix);
                    int[] currPermDfValues = getOneClassDfs(permutedMatrix);
                    double[] currPermPVals = getParametricPVals(currentPermTValues, currPermDfValues);
                    
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
            } else {// if (useAllCombs)
                for (int i = 0; i < numCombs; i++) {
                    if (stop) {
                        throw new AbortException();
                    }
                    event2.setIntValue(i);
                    event2.setDescription("Permuting matrix: Current permutation = " + (i+1));
                    fireValueChanged(event2);
                    
                    Vector validExpts = new Vector();
                    for (int j = 0; j < groupAssignments.length; j++) {
                        if (groupAssignments[j] == 1) {
                            validExpts.add(new Integer(j));
                        }
                    }
                    
                    int[] validArray = new int[validExpts.size()];
                    for (int j = 0; j < validArray.length; j++) {
                        validArray[j] = ((Integer)(validExpts.get(j))).intValue();
                    }
                    
                    boolean[] changeSign = getOneClassChangeSignArrayAllUniquePerms(i, validArray);
                    FloatMatrix permutedMatrix = getOneClassPermMatrix(expMatrix, changeSign);
                    
                    double[] currentPermTValues = getOneClassTValues(permutedMatrix);  
                    int[] currPermDfValues = getOneClassDfs(permutedMatrix);
                    double[] currPermPVals = getParametricPVals(currentPermTValues, currPermDfValues);
                    
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
            }
            
        } else if (tTestDesign == Ttest.BETWEEN_SUBJECTS) {
            if (!useAllCombs) { 
                for (int i = 0; i < numCombs; i++) {
                    if (stop) {
                        throw new AbortException();
                    }
                    event2.setIntValue(i);
                    event2.setDescription("Permuting matrix: Current permutation = " + (i+1));
                    fireValueChanged(event2);
                    int[] permutedExpts = new int[1];
                    Vector validExpts = new Vector();
                    
                    for (int j = 0; j < groupAssignments.length; j++) {
                        if (groupAssignments[j] != Ttest.NEITHER_GROUP) {
                            validExpts.add(new Integer(j));
                        }
                    }
                    
                    int[] validArray = new int[validExpts.size()];
                    for (int j = 0; j < validArray.length; j++) {
                        validArray[j] = ((Integer)(validExpts.get(j))).intValue();
                    }
                    
                    permutedExpts = getPermutedValues(numExps, validArray); //returns an int array of size "numExps", with the valid values permuted
                    FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
                    double[] currentPermTValues = getTwoClassUnpairedTValues(permutedMatrix);
                    int[] currPermDfValues = getTwoClassDfs(permutedMatrix);
                    
                    double[] currPermPVals = getParametricPVals(currentPermTValues, currPermDfValues);
                    
                    for (int j = 0; j < currPermPVals.length; j++) {
                        if (Double.isNaN(currPermPVals[j])) currPermPVals[j] = Double.POSITIVE_INFINITY; 
                        // this is to push NaNs to the end of the sorted array, since otherwise they would mess up quantile calculations 
                    }
                    
                    QSort sortCurrPVals = new QSort(currPermPVals, QSort.ASCENDING);
                    double[] sortedCurrPVals = sortCurrPVals.getSortedDouble();  
                    for (int j = 0; j < pValArray.length; j++) {
                        pValArray[j][i] = sortedCurrPVals[j];
                    }
                }
            } else { //if (useAllCombs)
                int[] permutedExpts = new int[numExps];
                
                for (int i = 0; i < numExps; i++) {
                    permutedExpts[i] = i;
                }    
                
                Vector usedExptsVector = new Vector();
                int numGroupAValues = 0;
                for (int i = 0; i < groupAssignments.length; i++) {
                    if (groupAssignments[i] != Ttest.NEITHER_GROUP) {
                        usedExptsVector.add(new Integer(i));
                    }
                    if (groupAssignments[i] == Ttest.GROUP_A) {
                        numGroupAValues++;
                    }
                }
                int[] usedExptsArray = new int[usedExptsVector.size()];
                
                for (int i = 0; i < usedExptsArray.length; i++) {
                    usedExptsArray[i] = ((Integer)(usedExptsVector.get(i))).intValue();
                }
                
                int[] combArray = new int[numGroupAValues];
                for (int i = 0; i < combArray.length; i++) {
                    combArray[i] = -1;
                }
                
                int numGroupBValues = usedExptsArray.length - numGroupAValues;
                
                int permCounter = 0;     
                
              while (Combinations.enumerateCombinations(usedExptsArray.length, numGroupAValues, combArray)) {
                  if (stop) {
                        throw new AbortException();
                    }
                    event2.setIntValue(permCounter);
                    event2.setDescription("Permuting matrix: Current permutation = " + (permCounter+1));
                    fireValueChanged(event2);
                    
                    int[] notInCombArray = new int[numGroupBValues];
                    int notCombCounter = 0;
                    
                    for (int i = 0; i < usedExptsArray.length; i++) {
                        if(!belongsInArray(i, combArray)) {
                            notInCombArray[notCombCounter] = i;
                            notCombCounter++;
                        }
                    }
                    
                    for (int i = 0; i < combArray.length; i++) {
                        permutedExpts[usedExptsArray[i]] = usedExptsArray[combArray[i]];
                    }
                    for (int i = 0; i < notInCombArray.length; i++) {
                        permutedExpts[usedExptsArray[combArray.length + i]] = usedExptsArray[notInCombArray[i]];
                    }
                    
                    FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts); 
                    double[] currentPermTValues = getTwoClassUnpairedTValues(permutedMatrix); 
                    int[] currPermDfValues = getTwoClassDfs(permutedMatrix);
                    
                    double[] currPermPVals = getParametricPVals(currentPermTValues, currPermDfValues);
                    
                    for (int j = 0; j < currPermPVals.length; j++) {
                        if (Double.isNaN(currPermPVals[j])) currPermPVals[j] = Double.POSITIVE_INFINITY; 
                        // this is to push NaNs to the end of the sorted array, since otherwise they would mess up quantile calculations 
                    }
                    
                    QSort sortCurrPVals = new QSort(currPermPVals, QSort.ASCENDING);
                    //double[] sortedCurrPVals = sortCurrPVals.getSortedDouble();
                    //uPlusOneSmallestPVector.add(new Double(sortedCurrPVals[u]));                     
                    double[] sortedCurrPVals = sortCurrPVals.getSortedDouble();  
                    for (int j = 0; j < pValArray.length; j++) {
                        pValArray[j][permCounter] = sortedCurrPVals[j];
                    }                    
                    permCounter++;                    
              }               
                
            }
        } else if (tTestDesign == Ttest.PAIRED) {
            if (!useAllCombs) {
                Random rand  = new Random();
            /*
            long[] randomSeeds  = new long[numCombs];
            for (int i = 0; i < numCombs; i++) {
                randomSeeds[i] = rand.nextLong();
            }
             */
                for (int i = 0; i < numCombs; i++) {
                    event2.setIntValue(i);
                    event2.setDescription("Calculating raw p values: Current permutation = " + (i + 1));
                    fireValueChanged(event2);
                    long randomSeed = rand.nextLong();
                    if (stop) {
                        throw new AbortException();
                    }
                    int[] permutedExpts = permuteWithinPairs(randomSeed); //returns an int array with some paired experiment indices permuted
                    FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
                    double[] currentPermTValues = getPairedTValues(permutedMatrix);
                    int[] currPermDfValues = getPairedDfs(permutedMatrix);
                    
                    double[] currPermPVals = getParametricPVals(currentPermTValues, currPermDfValues);
                    
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
            } else {//if (useAllCombs)
                for (int i = 0; i < numCombs; i++) {
                    event2.setIntValue(i);
                    event2.setDescription("Calculating raw p values: Current permutation = " + (i + 1));
                    fireValueChanged(event2);                    
                    int[] permutedExpts = permuteWithinPairsAllPerms(i);
                    FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);   
                    
                    double[] currentPermTValues = getPairedTValues(permutedMatrix);
                    int[] currPermDfValues = getPairedDfs(permutedMatrix);
                    
                    double[] currPermPVals = getParametricPVals(currentPermTValues, currPermDfValues);
                    
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
            }
        } //end if (tTestDesign == this.PAIRED)
        
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
    
    private double getYConservative(double alphaQuantile, int u) throws AlgorithmException {
        AlgorithmEvent event2 = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numCombs);
        fireValueChanged(event2);
        event2.setId(AlgorithmEvent.PROGRESS_VALUE);
        Vector uPlusOneSmallestPVector = new Vector();
        
        if (tTestDesign == ONE_CLASS) {
            if (!useAllCombs) {
                boolean[] changeSign = new boolean[1];
                
                Random rand  = new Random();
                long[] randomSeeds  = new long[numCombs];
                for (int i = 0; i < numCombs; i++) {
                    randomSeeds[i] = rand.nextLong();
                }
                
                for (int i = 0; i < numCombs; i++) {
                    if (stop) {
                        throw new AbortException();
                    }
                    event2.setIntValue(i);
                    event2.setDescription("Permuting matrix: Current permutation = " + (i+1));
                    fireValueChanged(event2);
                    
                    int[] permutedExpts = new int[1];
                    Vector validExpts = new Vector();
                    
                    for (int j = 0; j < groupAssignments.length; j++) {
                        if (groupAssignments[j] == 1) {
                            validExpts.add(new Integer(j));
                        }
                    }
                    
                    int[] validArray = new int[validExpts.size()];
                    for (int j = 0; j < validArray.length; j++) {
                        validArray[j] = ((Integer)(validExpts.get(j))).intValue();
                    }
                    
                    changeSign = getOneClassChangeSignArray(randomSeeds[i], validArray);
                    FloatMatrix permutedMatrix = getOneClassPermMatrix(expMatrix, changeSign);
                    
                    double[] currentPermTValues = getOneClassTValues(permutedMatrix);
                    int[] currPermDfValues = getOneClassDfs(permutedMatrix);
                    double[] currPermPVals = getParametricPVals(currentPermTValues, currPermDfValues);
                    
                    for (int j = 0; j < currPermPVals.length; j++) {
                        if (Double.isNaN(currPermPVals[j])) currPermPVals[j] = Double.POSITIVE_INFINITY; 
                        // this is to push NaNs to the end of the sorted array, since otherwise they would mess up quantile calculations 
                    }
                    
                    QSort sortCurrPVals = new QSort(currPermPVals, QSort.ASCENDING);
                    double[] sortedCurrPVals = sortCurrPVals.getSortedDouble();
                    uPlusOneSmallestPVector.add(new Double(sortedCurrPVals[u]));
                }
                    
            } else { //if(useAllCombs)
                for (int i = 0; i < numCombs; i++) {
                    if (stop) {
                        throw new AbortException();
                    }
                    event2.setIntValue(i);
                    event2.setDescription("Permuting matrix: Current permutation = " + (i+1));
                    fireValueChanged(event2);
                    
                    Vector validExpts = new Vector();
                    for (int j = 0; j < groupAssignments.length; j++) {
                        if (groupAssignments[j] == 1) {
                            validExpts.add(new Integer(j));
                        }
                    }
                    
                    int[] validArray = new int[validExpts.size()];
                    for (int j = 0; j < validArray.length; j++) {
                        validArray[j] = ((Integer)(validExpts.get(j))).intValue();
                    }
                    
                    boolean[] changeSign = getOneClassChangeSignArrayAllUniquePerms(i, validArray);
                    FloatMatrix permutedMatrix = getOneClassPermMatrix(expMatrix, changeSign);
                    
                    double[] currentPermTValues = getOneClassTValues(permutedMatrix);  
                    int[] currPermDfValues = getOneClassDfs(permutedMatrix);
                    double[] currPermPVals = getParametricPVals(currentPermTValues, currPermDfValues);
                    
                    for (int j = 0; j < currPermPVals.length; j++) {
                        if (Double.isNaN(currPermPVals[j])) currPermPVals[j] = Double.POSITIVE_INFINITY; 
                        // this is to push NaNs to the end of the sorted array, since otherwise they would mess up quantile calculations 
                    }
                    
                    QSort sortCurrPVals = new QSort(currPermPVals, QSort.ASCENDING);
                    double[] sortedCurrPVals = sortCurrPVals.getSortedDouble();
                    uPlusOneSmallestPVector.add(new Double(sortedCurrPVals[u]));                    
                }
            }
        
        } else if (tTestDesign == Ttest.BETWEEN_SUBJECTS) {
            if (!useAllCombs) {
                for (int i = 0; i < numCombs; i++) {
                    if (stop) {
                        throw new AbortException();
                    }
                    event2.setIntValue(i);
                    event2.setDescription("Permuting matrix: Current permutation = " + (i+1));
                    fireValueChanged(event2);
                    int[] permutedExpts = new int[1];
                    Vector validExpts = new Vector();
                    
                    for (int j = 0; j < groupAssignments.length; j++) {
                        if (groupAssignments[j] != Ttest.NEITHER_GROUP) {
                            validExpts.add(new Integer(j));
                        }
                    }
                    
                    int[] validArray = new int[validExpts.size()];
                    for (int j = 0; j < validArray.length; j++) {
                        validArray[j] = ((Integer)(validExpts.get(j))).intValue();
                    }
                    
                    permutedExpts = getPermutedValues(numExps, validArray); //returns an int array of size "numExps", with the valid values permuted
                    FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
                    double[] currentPermTValues = getTwoClassUnpairedTValues(permutedMatrix);
                    int[] currPermDfValues = getTwoClassDfs(permutedMatrix);
                    
                    double[] currPermPVals = getParametricPVals(currentPermTValues, currPermDfValues);
                    
                    for (int j = 0; j < currPermPVals.length; j++) {
                        if (Double.isNaN(currPermPVals[j])) currPermPVals[j] = Double.POSITIVE_INFINITY; 
                        // this is to push NaNs to the end of the sorted array, since otherwise they would mess up quantile calculations 
                    }
                    
                    QSort sortCurrPVals = new QSort(currPermPVals, QSort.ASCENDING);
                    double[] sortedCurrPVals = sortCurrPVals.getSortedDouble();
                    uPlusOneSmallestPVector.add(new Double(sortedCurrPVals[u]));                    
                }
                
            } else {// if(useAllCombs)
                int[] permutedExpts = new int[numExps];
                
                for (int i = 0; i < numExps; i++) {
                    permutedExpts[i] = i;
                }    
                
                Vector usedExptsVector = new Vector();
                int numGroupAValues = 0;
                for (int i = 0; i < groupAssignments.length; i++) {
                    if (groupAssignments[i] != Ttest.NEITHER_GROUP) {
                        usedExptsVector.add(new Integer(i));
                    }
                    if (groupAssignments[i] == Ttest.GROUP_A) {
                        numGroupAValues++;
                    }
                }
                int[] usedExptsArray = new int[usedExptsVector.size()];
                
                for (int i = 0; i < usedExptsArray.length; i++) {
                    usedExptsArray[i] = ((Integer)(usedExptsVector.get(i))).intValue();
                }
                
                int[] combArray = new int[numGroupAValues];
                for (int i = 0; i < combArray.length; i++) {
                    combArray[i] = -1;
                }
                
                int numGroupBValues = usedExptsArray.length - numGroupAValues;
                
                int permCounter = 0;     
                
              while (Combinations.enumerateCombinations(usedExptsArray.length, numGroupAValues, combArray)) {
                  if (stop) {
                        throw new AbortException();
                    }
                    event2.setIntValue(permCounter);
                    event2.setDescription("Permuting matrix: Current permutation = " + (permCounter+1));
                    fireValueChanged(event2);
                    
                    int[] notInCombArray = new int[numGroupBValues];
                    int notCombCounter = 0;
                    
                    for (int i = 0; i < usedExptsArray.length; i++) {
                        if(!belongsInArray(i, combArray)) {
                            notInCombArray[notCombCounter] = i;
                            notCombCounter++;
                        }
                    }
                    
                    for (int i = 0; i < combArray.length; i++) {
                        permutedExpts[usedExptsArray[i]] = usedExptsArray[combArray[i]];
                    }
                    for (int i = 0; i < notInCombArray.length; i++) {
                        permutedExpts[usedExptsArray[combArray.length + i]] = usedExptsArray[notInCombArray[i]];
                    }
                    
                    FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts); 
                    double[] currentPermTValues = getTwoClassUnpairedTValues(permutedMatrix); 
                    int[] currPermDfValues = getTwoClassDfs(permutedMatrix);
                    
                    double[] currPermPVals = getParametricPVals(currentPermTValues, currPermDfValues);
                    
                    for (int j = 0; j < currPermPVals.length; j++) {
                        if (Double.isNaN(currPermPVals[j])) currPermPVals[j] = Double.POSITIVE_INFINITY; 
                        // this is to push NaNs to the end of the sorted array, since otherwise they would mess up quantile calculations 
                    }
                    
                    QSort sortCurrPVals = new QSort(currPermPVals, QSort.ASCENDING);
                    double[] sortedCurrPVals = sortCurrPVals.getSortedDouble();
                    uPlusOneSmallestPVector.add(new Double(sortedCurrPVals[u]));                     
                    
                    permCounter++;                    
              }                
                
            }
            
        } else if (tTestDesign == Ttest.PAIRED) {
            if (!useAllCombs) {
                Random rand  = new Random();
            /*
            long[] randomSeeds  = new long[numCombs];
            for (int i = 0; i < numCombs; i++) {
                randomSeeds[i] = rand.nextLong();
            }
             */
                for (int i = 0; i < numCombs; i++) {
                    event2.setIntValue(i);
                    event2.setDescription("Calculating raw p values: Current permutation = " + (i + 1));
                    fireValueChanged(event2);
                    long randomSeed = rand.nextLong();
                    if (stop) {
                        throw new AbortException();
                    }
                    int[] permutedExpts = permuteWithinPairs(randomSeed); //returns an int array with some paired experiment indices permuted
                    FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
                    double[] currentPermTValues = getPairedTValues(permutedMatrix);
                    int[] currPermDfValues = getPairedDfs(permutedMatrix);
                    
                    double[] currPermPVals = getParametricPVals(currentPermTValues, currPermDfValues);
                    
                    for (int j = 0; j < currPermPVals.length; j++) {
                        if (Double.isNaN(currPermPVals[j])) currPermPVals[j] = Double.POSITIVE_INFINITY; 
                        // this is to push NaNs to the end of the sorted array, since otherwise they would mess up quantile calculations 
                    }
                    
                    QSort sortCurrPVals = new QSort(currPermPVals, QSort.ASCENDING);
                    double[] sortedCurrPVals = sortCurrPVals.getSortedDouble();
                    uPlusOneSmallestPVector.add(new Double(sortedCurrPVals[u]));                    
                }
                
            } else { // if (useAllCombs)
                for (int i = 0; i < numCombs; i++) {
                    event2.setIntValue(i);
                    event2.setDescription("Calculating raw p values: Current permutation = " + (i + 1));
                    fireValueChanged(event2);                    
                    int[] permutedExpts = permuteWithinPairsAllPerms(i);
                    FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);   
                    
                    double[] currentPermTValues = getPairedTValues(permutedMatrix);
                    int[] currPermDfValues = getPairedDfs(permutedMatrix);
                    
                    double[] currPermPVals = getParametricPVals(currentPermTValues, currPermDfValues);
                    
                    for (int j = 0; j < currPermPVals.length; j++) {
                        if (Double.isNaN(currPermPVals[j])) currPermPVals[j] = Double.POSITIVE_INFINITY; 
                        // this is to push NaNs to the end of the sorted array, since otherwise they would mess up quantile calculations 
                    }
                    
                    QSort sortCurrPVals = new QSort(currPermPVals, QSort.ASCENDING);
                    double[] sortedCurrPVals = sortCurrPVals.getSortedDouble();
                    uPlusOneSmallestPVector.add(new Double(sortedCurrPVals[u]));                    
                }
            }
        }// end if (tTestDesign == this.PAIRED)
        
        //System.out.println("uPlusOneSmallestPVector.size() = " + uPlusOneSmallestPVector.size());
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
        //return null; //for now
    }
    
    private double getMinY(double alphaQuantile, int r, int u) {//this is for exhaustive sampling for false significant number calculation
        
        return 0.0d; // for now
    }
    
    private double[] getOneClassRawPValsFromPerms() throws AlgorithmException {
        event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numCombs);
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE); 
        double[] rawPVals = new double[numGenes];  
        for (int i = 0; i < numGenes; i++) {
            if (Double.isNaN(tValues[i])) {
                rawPVals[i] = Double.NaN;
            } else {
                rawPVals[i] = 0d;
            }
        }   
        
        if (!useAllCombs) {
            boolean[] changeSign = new boolean[1];
            
            Random rand  = new Random();
            long[] randomSeeds  = new long[numCombs];
            for (int i = 0; i < numCombs; i++) {
                randomSeeds[i] = rand.nextLong();
            }
                
            for (int i = 0; i < numCombs; i++) {
                if (stop) {
                    throw new AbortException();
                }
                event.setIntValue(i);
                event.setDescription("Permuting matrix: Current permutation = " + (i+1));
                fireValueChanged(event);
                
                int[] permutedExpts = new int[1];
                Vector validExpts = new Vector();
                
                for (int j = 0; j < groupAssignments.length; j++) {
                    if (groupAssignments[j] == 1) {
                        validExpts.add(new Integer(j));
                    }
                }
                
                int[] validArray = new int[validExpts.size()];
                for (int j = 0; j < validArray.length; j++) {
                    validArray[j] = ((Integer)(validExpts.get(j))).intValue();
                }
                
                changeSign = getOneClassChangeSignArray(randomSeeds[i], validArray);
                FloatMatrix permutedMatrix = getOneClassPermMatrix(expMatrix, changeSign);
                
                double[] currentPermTValues = getOneClassTValues(permutedMatrix);
                for (int gene = 0; gene < numGenes; gene++) {
                    if (tValues[gene] < currentPermTValues[gene]) {
                        rawPVals[gene] = rawPVals[gene] + 1d;
                    }
                }                
            }
            
            for (int i = 0; i < numGenes; i++) {
                rawPVals[i] = rawPVals[i]/(double)numCombs;
            }            
            
        } else { //if (useAllCombs)
            for (int i = 0; i < numCombs; i++) {
                if (stop) {
                    throw new AbortException();
                }
                event.setIntValue(i);
                event.setDescription("Permuting matrix: Current permutation = " + (i+1));
                fireValueChanged(event);
                
                Vector validExpts = new Vector();
                for (int j = 0; j < groupAssignments.length; j++) {
                    if (groupAssignments[j] == 1) {
                        validExpts.add(new Integer(j));
                    }
                }
                
                int[] validArray = new int[validExpts.size()];
                for (int j = 0; j < validArray.length; j++) {
                    validArray[j] = ((Integer)(validExpts.get(j))).intValue();
                }
                
                boolean[] changeSign = getOneClassChangeSignArrayAllUniquePerms(i, validArray);
                FloatMatrix permutedMatrix = getOneClassPermMatrix(expMatrix, changeSign);
                
                double[] currentPermTValues = getOneClassTValues(permutedMatrix);
                for (int gene = 0; gene < numGenes; gene++) {
                    if (tValues[gene] < currentPermTValues[gene]) {
                        rawPVals[gene] = rawPVals[gene] + 1d;
                    }
                }
            }
            for (int i = 0; i < numGenes; i++) {
                rawPVals[i] = rawPVals[i]/(double)numCombs;
            }                
        }
        /*
        if (useAllCombs) {
            for (int i = 0; i < numGenes; i++) {
                if (stop) {
                    throw new AbortException();
                }
                event.setIntValue(i);
                event.setDescription("Calculating raw p values: gene = " + (i + 1));
                fireValueChanged(event);                
                rawPVals[i] = (double)(getAllCombsOneClassProb(i));
            }

        } else {// if (!useAllCombs)
            for (int i = 0; i < numGenes; i++) {
                if (stop) {
                    throw new AbortException();
                }
                event.setIntValue(i);
                event.setDescription("Calculating raw p values: gene = " + (i + 1));
                fireValueChanged(event);                
                rawPVals[i] = getSomeCombsOneClassProb(i);                
            }            
        }
        */
        return rawPVals;
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
    
    private boolean[] getOneClassChangeSignArray(long seed, int[] validExpts) {
        boolean[] changeSignArray = new boolean[numExps];
        for (int i = 0; i < changeSignArray.length; i++) {
            changeSignArray[i] = false;            
        }
        
        Random generator2 = new Random(seed);
        for (int i = 0; i < validExpts.length; i++) {
            changeSignArray[validExpts[i]] = generator2.nextBoolean();
        }
        
        return changeSignArray;
    }  
    
    private FloatMatrix getOneClassPermMatrix(FloatMatrix inputMatrix, boolean[] changeSign) {
        FloatMatrix permutedMatrix = new FloatMatrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());

        for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
            for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                if (changeSign[j]) {
                    permutedMatrix.A[i][j] = (float)(inputMatrix.A[i][j] - 2.0f*(inputMatrix.A[i][j] - oneClassMean));
                } else {
                    permutedMatrix.A[i][j] = inputMatrix.A[i][j];
                }
            }
        }
        
        return permutedMatrix;
    } 
    
    private boolean[] getOneClassChangeSignArrayAllUniquePerms(int num, int[] validExpts) {
        boolean[] changeSignArray = new boolean[numExps];
        for (int i = 0; i < changeSignArray.length; i++) {
            changeSignArray[i] = false;            
        }
        
        //Random generator2 = new Random(seed);
        int numValidExps = validExpts.length;

        String binaryString = Integer.toBinaryString(num);
        //System.out.println(binaryString);
        char[] binArray = binaryString.toCharArray();
        if (binArray.length < numValidExps) {
            Vector binVector = new Vector();
            for (int i = 0; i < (numValidExps - binArray.length); i++) {
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
        
        for (int i = 0; i < validExpts.length; i++) {
            if (binArray[i] == '1') {
                changeSignArray[validExpts[i]] = true;
            } else {
                changeSignArray[validExpts[i]] = false;
            }
        }
        
        return changeSignArray;
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
    
    private double getOneClassTValue(int gene, FloatMatrix inputMatrix) {
        Vector currentGene = new Vector();
        
        for (int i = 0; i < numExps; i++) {
            if (groupAssignments[i] == 1) {
                currentGene.add(new Float(inputMatrix.A[gene][i]));
            }
        }
        
        float[] currGeneArray = new float[currentGene.size()];
        
        for (int i = 0; i < currGeneArray.length; i++) {
            currGeneArray[i] = ((Float)(currentGene.get(i))).floatValue();
        }     
        
        return getOneClassTValue(currGeneArray);
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
    
    private double[] getPairedRawPValsFromPerms() throws AlgorithmException {
        event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numCombs);
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE); 
        double[] rawPVals = new double[numGenes];  
        for (int i = 0; i < numGenes; i++) {
            if (Double.isNaN(tValues[i])) {
                rawPVals[i] = Double.NaN;
            } else {
                rawPVals[i] = 0d;
            }
        }
        if (!useAllCombs) {
            Random rand  = new Random();
            /*
            long[] randomSeeds  = new long[numCombs];
            for (int i = 0; i < numCombs; i++) {
                randomSeeds[i] = rand.nextLong();
            }   
             */        
            for (int i = 0; i < numCombs; i++) {
                long randomSeed = rand.nextLong();
                if (stop) {
                    throw new AbortException();
                }
                int[] permutedExpts = permuteWithinPairs(randomSeed); //returns an int array with some paired experiment indices permuted
                FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);     
                
                event.setIntValue(i);
                event.setDescription("Calculating raw p values: Current permutation = " + (i + 1));
                fireValueChanged(event);
                //System.out.println("Calculating raw p values: Current permutation = " + (i + 1));
                for (int gene = 0; gene < numGenes; gene++) {
                    double currT = getPairedTValue(gene, permutedMatrix);
                    if (tValues[gene] < currT) {
                        rawPVals[gene] = rawPVals[gene] + 1d;
                    }
                }
            }
        } else { //if (useAllCombs)
            for (int i = 0; i < numCombs; i++) {
                if (stop) {
                    throw new AbortException();
                } 
                int[] permutedExpts = permuteWithinPairsAllPerms(i);
                FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
                
                event.setIntValue(i);
                event.setDescription("Calculating raw p values: Current permutation = " + (i + 1));
                fireValueChanged(event);
                //System.out.println("Calculating raw p values: Current permutation = " + (i + 1));
                for (int gene = 0; gene < numGenes; gene++) {
                    double currT = getPairedTValue(gene, permutedMatrix);
                    if (tValues[gene] < currT) {
                        rawPVals[gene] = rawPVals[gene] + 1d;
                    }
                }                
            }
        }
        
        for (int i = 0; i < numGenes; i++) {
            rawPVals[i] = rawPVals[i]/(double)numCombs;
        }
        /*
        for (int gene = 0; gene < numGenes; gene++) {
            //System.out.println("Current gene = " + gene);
            if (stop) {
                throw new AbortException();
            }
            event.setIntValue(gene);
            event.setDescription("Calculating raw p values: Current gene = " + (gene + 1));
            fireValueChanged(event);
            if (Double.isNaN(tValues[gene])) {
                rawPVals[gene] = Double.NaN;
            } else {
                if (!useAllCombs) {
                    Random rand  = new Random();
                    long[] randomSeeds  = new long[numCombs];
                    for (int i = 0; i < numCombs; i++) {
                        randomSeeds[i] = rand.nextLong();
                    }
                    for (int i = 0; i < numCombs; i++) {
                        int[] permutedExpts = permuteWithinPairs(randomSeeds[i]); //returns an int array with some paired experiment indices permuted
                        FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
                        //double[] permTValues
                    }
                    
                } else { // if (useAllCombs)
                }
            }
        }
         */
        
        return rawPVals;
    }
    
    private int[] permuteWithinPairsAllPerms(int num) {
        int[] permutedValues = new int[numExps];
        for (int i = 0; i < permutedValues.length; i++) {
            permutedValues[i] = i;
        }
        
        int temp;
        //Random generator2 =new Random(seed);
        boolean[] changeSign = getChangeSignArrayForAllPairedPerms(num);
        for (int i = 0; i < pairedGroupAExpts.length; i++) {
            
            boolean swap = changeSign[i];
            //System.out.print(swap + " ");
            if (swap) {
                temp = permutedValues[pairedGroupBExpts[i]];
                permutedValues[pairedGroupBExpts[i]] = permutedValues[pairedGroupAExpts[i]];
                permutedValues[pairedGroupAExpts[i]] = temp;
            }
        }
        /*
        try {
            Thread.sleep(10);
        } catch (Exception exc) {
            exc.printStackTrace();
        } 
         */       
        
        return permutedValues;
    }    
    
    boolean[] getChangeSignArrayForAllPairedPerms(int num) {
        boolean[] permutArray = new boolean[pairedGroupAExpts.length];
        
        for (int i = 0; i < permutArray.length; i++) {
            permutArray[i] = false;
        }
        
        int numPairs = pairedGroupAExpts.length;
        
        String binaryString = Integer.toBinaryString(num);
        //System.out.println(binaryString);
        char[] binArray = binaryString.toCharArray();
        if (binArray.length < numPairs) {
            Vector binVector = new Vector();
            for (int i = 0; i < (numPairs - binArray.length); i++) {
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
        //int counter = 0;
        
        for (int i = 0; i < permutArray.length; i++) {

            if (binArray[i] == '1') {
                permutArray[i] = true;
            } else {
                permutArray[i] = false;
            }

        }
        /*
        for (int i = 0; i < oneClassPermutArray.length; i++) {
            System.out.print(oneClassPermutArray[i] + " ");
        }
        System.out.println();
        */
        return permutArray;
    }    
    
    private int[] permuteWithinPairs(long seed) {
        int[] permutedValues = new int[numExps];
        for (int i = 0; i < permutedValues.length; i++) {
            permutedValues[i] = i;
        }
        
        int temp;
        Random generator2 =new Random(seed);
        for (int i = 0; i < pairedGroupAExpts.length; i++) {
            
            boolean swap = generator2.nextBoolean();
            //System.out.print(swap + " ");
            if (swap) {
                temp = permutedValues[pairedGroupBExpts[i]];
                permutedValues[pairedGroupBExpts[i]] = permutedValues[pairedGroupAExpts[i]];
                permutedValues[pairedGroupAExpts[i]] = temp;
            }
        }
        
        try {
            Thread.sleep(10);
        } catch (Exception exc) {
            exc.printStackTrace();
        }        
        
        return permutedValues;
    }    
    
    private double[] getTwoClassRawPValsFromPerms() throws AlgorithmException {
        event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numCombs);
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE); 
        double[] rawPVals = new double[numGenes];
        for (int i = 0; i < numGenes; i++) {
            if (Double.isNaN(tValues[i])) {
                rawPVals[i] = Double.NaN;
            } else {
                rawPVals[i] = 0d;
            }
        }
        if (!useAllCombs) {
            for (int i = 0; i < numCombs; i++) {
                if (stop) {
                    throw new AbortException();
                }
                event.setIntValue(i);
                event.setDescription("Permuting matrix: Current permutation = " + (i+1));
                fireValueChanged(event);
                int[] permutedExpts = new int[1];
                Vector validExpts = new Vector();
                
                for (int j = 0; j < groupAssignments.length; j++) {
                    if (groupAssignments[j] != Ttest.NEITHER_GROUP) {
                        validExpts.add(new Integer(j));
                    }
                }
                
                int[] validArray = new int[validExpts.size()];
                for (int j = 0; j < validArray.length; j++) {
                    validArray[j] = ((Integer)(validExpts.get(j))).intValue();
                }
                
                permutedExpts = getPermutedValues(numExps, validArray); //returns an int array of size "numExps", with the valid values permuted
                FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
                double[] currentPermTValues = getTwoClassUnpairedTValues(permutedMatrix);
                for (int gene = 0; gene < numGenes; gene++) {
                    if (tValues[gene] < currentPermTValues[gene]) {
                        rawPVals[gene] = rawPVals[gene] + 1d;
                    }
                }
            }
            
            for (int i = 0; i < numGenes; i++) {
                rawPVals[i] = rawPVals[i]/(double)numCombs;
            }
            
        } else { // if (useAllCombs)
                int[] permutedExpts = new int[numExps];
                
                for (int i = 0; i < numExps; i++) {
                    permutedExpts[i] = i;
                }    
                
                Vector usedExptsVector = new Vector();
                int numGroupAValues = 0;
                for (int i = 0; i < groupAssignments.length; i++) {
                    if (groupAssignments[i] != Ttest.NEITHER_GROUP) {
                        usedExptsVector.add(new Integer(i));
                    }
                    if (groupAssignments[i] == Ttest.GROUP_A) {
                        numGroupAValues++;
                    }
                }
                int[] usedExptsArray = new int[usedExptsVector.size()];
                
                for (int i = 0; i < usedExptsArray.length; i++) {
                    usedExptsArray[i] = ((Integer)(usedExptsVector.get(i))).intValue();
                }
                
                int[] combArray = new int[numGroupAValues];
                for (int i = 0; i < combArray.length; i++) {
                    combArray[i] = -1;
                }
                
                int numGroupBValues = usedExptsArray.length - numGroupAValues;
                
                int permCounter = 0;    
                
                while (Combinations.enumerateCombinations(usedExptsArray.length, numGroupAValues, combArray)) {
                    if (stop) {
                        throw new AbortException();
                    }
                    event.setIntValue(permCounter);
                    event.setDescription("Permuting matrix: Current permutation = " + (permCounter+1));
                    fireValueChanged(event);
                    
                    int[] notInCombArray = new int[numGroupBValues];
                    int notCombCounter = 0;
                    
                    for (int i = 0; i < usedExptsArray.length; i++) {
                        if(!belongsInArray(i, combArray)) {
                            notInCombArray[notCombCounter] = i;
                            notCombCounter++;
                        }
                    }
                    
                    for (int i = 0; i < combArray.length; i++) {
                        permutedExpts[usedExptsArray[i]] = usedExptsArray[combArray[i]];
                    }
                    for (int i = 0; i < notInCombArray.length; i++) {
                        permutedExpts[usedExptsArray[combArray.length + i]] = usedExptsArray[notInCombArray[i]];
                    }
                    
                    FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts); 
                    double[] currentPermTValues = getTwoClassUnpairedTValues(permutedMatrix);
                    for (int gene = 0; gene < numGenes; gene++) {
                        if (tValues[gene] < currentPermTValues[gene]) {
                            rawPVals[gene] = rawPVals[gene] + 1d;
                        }
                    } 
                    permCounter++;
                } 
                
                for (int i = 0; i < numGenes; i++) {
                    rawPVals[i] = rawPVals[i]/(double)permCounter;
                }
            }
        /*
        for (int gene = 0; gene < numGenes; gene++) {
            //System.out.println("Current gene = " + gene);
            if (stop) {
                throw new AbortException();
            }
            event.setIntValue(gene);
            event.setDescription("Calculating raw p values: Current gene = " + (gene + 1));
            fireValueChanged(event);   
            if (Double.isNaN(tValues[gene])) {
                rawPVals[gene] = Double.NaN;
            } else {
                double origTValue = tValues[gene];
                float[] geneValues = new float[numExps];
                for (int i = 0; i < numExps; i++) {
                    geneValues[i] = expMatrix.A[gene][i];
                }
                
                int groupACounter = 0;
                int groupBCounter = 0;
                
                for (int i = 0; i < groupAssignments.length; i++) {
                    if (groupAssignments[i] == this.GROUP_A) {
                        groupACounter++;
                    } else if (groupAssignments[i] == this.GROUP_B) {
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
                    if (groupAssignments[i] == GROUP_A) {
                        groupAValues[groupACounter] = geneValues[i];
                        if (!Float.isNaN(geneValues[i])) {
                            numbValidValuesA++;
                        }
                        groupACounter++;
                        groupedExpts[groupedExptsCounter] = i;
                        groupedExptsCounter++;
                    } else if (groupAssignments[i] == GROUP_B) {
                        groupBValues[groupBCounter] = geneValues[i];
                        if (!Float.isNaN(geneValues[i])) {
                            numbValidValuesB++;
                        }
                        groupBCounter++;
                        groupedExpts[groupedExptsCounter] = i;
                        groupedExptsCounter++;
                    }
                }               
                
                if (useAllCombs) {
                    double permutProb = 0d;
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
                        
                        for(int i = 0; i < combArray.length; i++) {
                            resampGroupA[i] = geneValues[groupedExpts[combArray[i]]];
                        }
                        
                        for(int i = 0; i < notInCombArray.length; i++) {
                            resampGroupB[i] = geneValues[groupedExpts[notInCombArray[i]]];
                        }
                        
                        float resampTValue = Math.abs(calculateTValue(resampGroupA, resampGroupB));
                        //System.out.println("resampTValue = " + resampTValue);
                        if (origTValue < (double)(resampTValue)) {
                            permutProb++;
                        }
                        numCombsCounter++;
                    }
                    
                    permutProb = (double)(permutProb/(double)numCombsCounter);
                    //System.out.println("permutProb = " + permutProb);
                    rawPVals[gene] = permutProb;
                    
                } else { // if (!useAllCombs)
                    double permutProb = 0d;
                    int randomCounter = 0;
                    //permutProb = 0;
                    for (int i = 0; i < numCombs; i++) {
                        //int[] randomGroupA = new int[groupAValues.length];
                        //int[] randomGroupB = new int[groupBValues.length];
                        float[][] randomGroups = randomlyPermute(geneValues, groupedExpts, groupAValues.length, groupBValues.length);
                        float randomizedTValue = Math.abs(calculateTValue(randomGroups[0], randomGroups[1]));
                        if (origTValue < (double)(randomizedTValue)) {
                            permutProb++;
                        }
                        randomCounter++;
                    }
                    
                    permutProb = (double)(permutProb/(double)randomCounter);
                    rawPVals[gene] = permutProb;                    
                }
            }
        } */
        
        return rawPVals;
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
    
    private double[] getParametricPVals(double[] tVals, int dfs[]) {
        double[] pVals = new double[numGenes];
        
        for (int i = 0; i < numGenes; i++) {
            if (Double.isNaN(tVals[i])) {
                pVals[i] = Double.NaN;
            } else {
                TDistribution tDist = new TDistribution(dfs[i]);
                double cumulP = tDist.cumulative(tVals[i]);
                double prob = 2*(1 - cumulP); // two-tailed test
                if (prob > 1) {
                    prob = 1;
                } 
                pVals[i] = prob;                
            }
        }
        return pVals;
    }
    
    private double[] getRawPValsFromTDist() throws AlgorithmException {
        event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numGenes);
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);      
        
        double[] rawPVals = new double[numGenes];
        for (int i = 0; i < numGenes; i++) {
            if (stop) {
                throw new AbortException();
            }
            event.setIntValue(i);
            event.setDescription("Calculating raw p values: Current gene = " + (i + 1));
            fireValueChanged(event);  
            
            if (Double.isNaN(tValues[i])) {
                rawPVals[i] = Double.NaN;
            } else {
                TDistribution tDist = new TDistribution((int)(dfValues[i]));
                double cumulP = tDist.cumulative(tValues[i]);
                double prob = 2*(1 - cumulP); // two-tailed test
                if (prob > 1) {
                    prob = 1;
                } 
                rawPVals[i] = prob;
            }
        }
        
        return rawPVals;
    }
    
    private double[] getAdjPVals(double[] rawPVals, int adjMethod) throws AlgorithmException {
        event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numGenes);
	fireValueChanged(event);
	event.setId(AlgorithmEvent.PROGRESS_VALUE); 
        
        double[] adjPVals = new double[rawPVals.length];
        if (adjMethod == Ttest.JUST_ALPHA) {
            adjPVals = rawPVals;
        } 
        if (adjMethod == Ttest.STD_BONFERRONI) {
            for (int i = 0; i < numGenes; i++) {
		if (stop) {
		    throw new AbortException();
		}
		event.setIntValue(i);
		event.setDescription("Computing adjusted p-values: Current gene = " + (i + 1));  
		fireValueChanged(event);  
                double currP = rawPVals[i];
                double currAdjP = (double)(currP*numGenes);
                if (currAdjP > 1.0d) currAdjP = 1.0d;
                adjPVals[i] = currAdjP;
            }
        }
        if (adjMethod == Ttest.ADJ_BONFERRONI) {
            adjPVals = getAdjBonfPVals(rawPVals);
        }
        if (adjMethod == Ttest.MAX_T) {
            adjPVals = getMaxTPVals();
        }
        
        return adjPVals;
    }  
    
    private double[] getMaxTPVals() throws AlgorithmException {
        double[] origTValues = tValues;
        double[] descTValues = new double[numGenes];
        int[] descGeneIndices = new int[numGenes];
        double[] adjPValues = new double[numGenes];
        
        double[][] permutedRankedTValues = new double[numCombs][numGenes];
        double[][] uMatrix = new double[numGenes][numCombs];
        AlgorithmEvent event2 = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numCombs);
        fireValueChanged(event2);
        event2.setId(AlgorithmEvent.PROGRESS_VALUE);    
        
        if (tTestDesign == Ttest.BETWEEN_SUBJECTS) {
            QSort sortDescTValues = new QSort(origTValues, QSort.DESCENDING);
            descTValues = sortDescTValues.getSortedDouble();
            descGeneIndices = sortDescTValues.getOrigIndx();
            
            //FloatMatrix orderedByTMatrix = getOrderedByTMatrix(expMatrix, descGeneIndices);

            if (!useAllCombs) {
                for (int i = 0; i < numCombs; i++) {
                    if (stop) {
                        throw new AbortException();
                    }
                    event2.setIntValue(i);
                    event2.setDescription("Permuting matrix: Current permutation = " + (i+1));
                    fireValueChanged(event2);
                    int[] permutedExpts = new int[1];
                    Vector validExpts = new Vector();
                    
                    for (int j = 0; j < groupAssignments.length; j++) {
                        if (groupAssignments[j] != Ttest.NEITHER_GROUP) {
                            validExpts.add(new Integer(j));
                        }
                    }
                    
                    int[] validArray = new int[validExpts.size()];
                    for (int j = 0; j < validArray.length; j++) {
                        validArray[j] = ((Integer)(validExpts.get(j))).intValue();
                    }
                    
                    permutedExpts = getPermutedValues(numExps, validArray); //returns an int array of size "numExps", with the valid values permuted
                    FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
                    double[] currentPermTValues = getTwoClassUnpairedTValues(permutedMatrix);
                    
                    if (Double.isNaN(currentPermTValues[descGeneIndices[numGenes - 1]])) {
                        uMatrix[numGenes - 1][i] = Double.NEGATIVE_INFINITY;
                    } else {
                        uMatrix[numGenes - 1][i] = currentPermTValues[descGeneIndices[numGenes - 1]];
                    }
                    //System.out.println("uMatrix[" + (numGenes - 1) + "][" + i + "] = " + uMatrix[numGenes - 1][i]);
                    
                    for (int j = numGenes - 2; j >= 0; j--) {
                        if (Double.isNaN(currentPermTValues[descGeneIndices[j]])) {
                            uMatrix[j][i] = uMatrix[j+1][i];
                        } else {
                            uMatrix[j][i] = Math.max(uMatrix[j+1][i], currentPermTValues[descGeneIndices[j]]);
                        }
                        //System.out.println("uMatrix[" + j + "][" + i + "] = " + uMatrix[j][i]);
                    }
                    
                    /*
                    QSort sortDescCurrentPermTValues = new QSort(currentPermTValues, QSort.DESCENDING);
                    double[] descCurrentPermTValues = sortDescCurrentPermTValues.getSortedDouble();
                    
                    for (int j = 0; j < permutedRankedTValues[i].length; j++) {
                        permutedRankedTValues[i][j] = descCurrentPermTValues[j];
                    }*/
                } // end "for (int i = 0; i < numCombs; i++)"
                
            } else { // if (useAllCombs)
                int[] permutedExpts = new int[numExps];
                
                for (int i = 0; i < numExps; i++) {
                    permutedExpts[i] = i;
                }    
                
                Vector usedExptsVector = new Vector();
                int numGroupAValues = 0;
                for (int i = 0; i < groupAssignments.length; i++) {
                    if (groupAssignments[i] != Ttest.NEITHER_GROUP) {
                        usedExptsVector.add(new Integer(i));
                    }
                    if (groupAssignments[i] == Ttest.GROUP_A) {
                        numGroupAValues++;
                    }
                }
                int[] usedExptsArray = new int[usedExptsVector.size()];
                
                for (int i = 0; i < usedExptsArray.length; i++) {
                    usedExptsArray[i] = ((Integer)(usedExptsVector.get(i))).intValue();
                }
                
                int[] combArray = new int[numGroupAValues];
                for (int i = 0; i < combArray.length; i++) {
                    combArray[i] = -1;
                }
                
                int numGroupBValues = usedExptsArray.length - numGroupAValues;
                
                int permCounter = 0;     
                //System.out.println("All combs minP: up to here");
                while (Combinations.enumerateCombinations(usedExptsArray.length, numGroupAValues, combArray)) {
                    
                    if (stop) {
                        throw new AbortException();
                    }
                    event2.setIntValue(permCounter);
                    event2.setDescription("Permuting matrix: Current permutation = " + (permCounter+1));
                    fireValueChanged(event2);
                    
                    int[] notInCombArray = new int[numGroupBValues];
                    int notCombCounter = 0;
                    
                    for (int i = 0; i < usedExptsArray.length; i++) {
                        if(!belongsInArray(i, combArray)) {
                            notInCombArray[notCombCounter] = i;
                            notCombCounter++;
                        }
                    }
                    
                    for (int i = 0; i < combArray.length; i++) {
                        permutedExpts[usedExptsArray[i]] = usedExptsArray[combArray[i]];
                    }
                    for (int i = 0; i < notInCombArray.length; i++) {
                        permutedExpts[usedExptsArray[combArray.length + i]] = usedExptsArray[notInCombArray[i]];
                    }
                    
                    FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
                    /*
                    double[] permDArray = new double[permutedMatrix.getRowDimension()];
                    for (int j = 0; j < permutedMatrix.getRowDimension(); j++) {
                        permDArray[j] = getD(j, permutedMatrix);
                    }
                    
                    QSort sortPermDArray = new QSort(permDArray);
                    double[] sortedPermDArray = sortPermDArray.getSortedDouble();
                    
                    for (int j = 0; j < sortedPermDArray.length; j++) {
                        permutedDValues[permCounter][j] = sortedPermDArray[j];
                    }
                    */
                    double[] currentPermTValues = getTwoClassUnpairedTValues(permutedMatrix);
                    
                    if (Double.isNaN(currentPermTValues[descGeneIndices[numGenes - 1]])) {
                        uMatrix[numGenes - 1][permCounter] = Double.NEGATIVE_INFINITY;
                    } else {
                        uMatrix[numGenes - 1][permCounter] = currentPermTValues[descGeneIndices[numGenes - 1]];
                    }
                    //System.out.println("uMatrix[" + (numGenes - 1) + "][" + i + "] = " + uMatrix[numGenes - 1][i]);
                    
                    for (int j = numGenes - 2; j >= 0; j--) {
                        if (Double.isNaN(currentPermTValues[descGeneIndices[j]])) {
                            uMatrix[j][permCounter] = uMatrix[j+1][permCounter];
                        } else {
                            uMatrix[j][permCounter] = Math.max(uMatrix[j+1][permCounter], currentPermTValues[descGeneIndices[j]]);
                        }
                        //System.out.println("uMatrix[" + j + "][" + i + "] = " + uMatrix[j][i]);
                    }                    
                    /*
                    QSort sortDescCurrentPermTValues = new QSort(currentPermTValues, QSort.DESCENDING);
                    double[] descCurrentPermTValues = sortDescCurrentPermTValues.getSortedDouble();
                    
                    for (int j = 0; j < permutedRankedTValues[permCounter].length; j++) {
                        permutedRankedTValues[permCounter][j] = descCurrentPermTValues[j];
                    }  */                  
                    
                    permCounter++;
                }             
                
                //System.out.println("numCombs = " + numCombs + ", permCounter =" + permCounter);
            }
        } else if (tTestDesign == PAIRED) {
            QSort sortDescTValues = new QSort(origTValues, QSort.DESCENDING);
            descTValues = sortDescTValues.getSortedDouble();
            descGeneIndices = sortDescTValues.getOrigIndx();   
            if (!useAllCombs) {
                Random rand  = new Random();  
                for (int i = 0; i < numCombs; i++) {
                    long randomSeed = rand.nextLong();
                    if (stop) {
                        throw new AbortException();
                    }
                    int[] permutedExpts = permuteWithinPairs(randomSeed); //returns an int array with some paired experiment indices permuted
                    FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
                    
                    event2.setIntValue(i);
                    event2.setDescription("Permuting matrix: Current permutation = " + (i + 1));
                    fireValueChanged(event2); 
                    
                    double[] currentPermTValues = new double[numGenes];
                    for (int gene = 0; gene < numGenes; gene++) {
                        currentPermTValues[gene] = getPairedTValue(gene, permutedMatrix);
                    }     
                    
                    if (Double.isNaN(currentPermTValues[descGeneIndices[numGenes - 1]])) {
                        uMatrix[numGenes - 1][i] = Double.NEGATIVE_INFINITY;
                    } else {
                        uMatrix[numGenes - 1][i] = currentPermTValues[descGeneIndices[numGenes - 1]];
                    }
                    //System.out.println("uMatrix[" + (numGenes - 1) + "][" + i + "] = " + uMatrix[numGenes - 1][i]);
                    
                    for (int j = numGenes - 2; j >= 0; j--) {
                        if (Double.isNaN(currentPermTValues[descGeneIndices[j]])) {
                            uMatrix[j][i] = uMatrix[j+1][i];
                        } else {
                            uMatrix[j][i] = Math.max(uMatrix[j+1][i], currentPermTValues[descGeneIndices[j]]);
                        }
                        //System.out.println("uMatrix[" + j + "][" + i + "] = " + uMatrix[j][i]);
                    }                    
                }
            } else { // if (useAllCombs)
                for (int i = 0; i < numCombs; i++) {
                    if (stop) {
                        throw new AbortException();
                    }
                    int[] permutedExpts = permuteWithinPairsAllPerms(i);
                    FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
                    
                    event2.setIntValue(i);
                    event2.setDescription("Permuting matrix: Current permutation = " + (i + 1));
                    fireValueChanged(event2);
                    
                    double[] currentPermTValues = new double[numGenes];
                    for (int gene = 0; gene < numGenes; gene++) {
                        currentPermTValues[gene] = getPairedTValue(gene, permutedMatrix);
                    }     
                    
                    if (Double.isNaN(currentPermTValues[descGeneIndices[numGenes - 1]])) {
                        uMatrix[numGenes - 1][i] = Double.NEGATIVE_INFINITY;
                    } else {
                        uMatrix[numGenes - 1][i] = currentPermTValues[descGeneIndices[numGenes - 1]];
                    }
                    //System.out.println("uMatrix[" + (numGenes - 1) + "][" + i + "] = " + uMatrix[numGenes - 1][i]);
                    
                    for (int j = numGenes - 2; j >= 0; j--) {
                        if (Double.isNaN(currentPermTValues[descGeneIndices[j]])) {
                            uMatrix[j][i] = uMatrix[j+1][i];
                        } else {
                            uMatrix[j][i] = Math.max(uMatrix[j+1][i], currentPermTValues[descGeneIndices[j]]);
                        }
                        //System.out.println("uMatrix[" + j + "][" + i + "] = " + uMatrix[j][i]);
                    }                    
                }
            }
            
        } else if (tTestDesign == ONE_CLASS) {
            QSort sortDescTValues = new QSort(origTValues, QSort.DESCENDING);
            descTValues = sortDescTValues.getSortedDouble();
            descGeneIndices = sortDescTValues.getOrigIndx(); 
            
            if (!useAllCombs) {
                boolean[] changeSign = new boolean[1];
                
                Random rand  = new Random();
                long[] randomSeeds  = new long[numCombs];
                for (int i = 0; i < numCombs; i++) {
                    randomSeeds[i] = rand.nextLong();
                }  
                
                for (int i = 0; i < numCombs; i++) {
                    if (stop) {
                        throw new AbortException();
                    }
                    event2.setIntValue(i);
                    event2.setDescription("Permuting matrix: Current permutation = " + (i+1));
                    fireValueChanged(event2);
                    int[] permutedExpts = new int[1];
                    Vector validExpts = new Vector(); 
                    
                    for (int j = 0; j < groupAssignments.length; j++) {
                        if (groupAssignments[j] == 1) {
                            validExpts.add(new Integer(j));
                        }
                    }   
                    
                    int[] validArray = new int[validExpts.size()];
                    for (int j = 0; j < validArray.length; j++) {
                        validArray[j] = ((Integer)(validExpts.get(j))).intValue();
                    }   
                    
                    changeSign = getOneClassChangeSignArray(randomSeeds[i], validArray);
                    FloatMatrix permutedMatrix = getOneClassPermMatrix(expMatrix, changeSign);
                    
                    double[] currentPermTValues = getOneClassTValues(permutedMatrix);
                    
                    if (Double.isNaN(currentPermTValues[descGeneIndices[numGenes - 1]])) {
                        uMatrix[numGenes - 1][i] = Double.NEGATIVE_INFINITY;
                    } else {
                        uMatrix[numGenes - 1][i] = currentPermTValues[descGeneIndices[numGenes - 1]];
                    }
                    //System.out.println("uMatrix[" + (numGenes - 1) + "][" + i + "] = " + uMatrix[numGenes - 1][i]);
                    
                    for (int j = numGenes - 2; j >= 0; j--) {
                        if (Double.isNaN(currentPermTValues[descGeneIndices[j]])) {
                            uMatrix[j][i] = uMatrix[j+1][i];
                        } else {
                            uMatrix[j][i] = Math.max(uMatrix[j+1][i], currentPermTValues[descGeneIndices[j]]);
                        }
                        //System.out.println("uMatrix[" + j + "][" + i + "] = " + uMatrix[j][i]);
                    }                  
                    
                    /*
                    QSort sortDescCurrentPermTValues = new QSort(currentPermTValues, QSort.DESCENDING);
                    double[] descCurrentPermTValues = sortDescCurrentPermTValues.getSortedDouble();
                    
                    for (int j = 0; j < permutedRankedTValues[i].length; j++) {
                        permutedRankedTValues[i][j] = descCurrentPermTValues[j];
                    }  */                  
                }
            } else { // if (useAllCombs)
                for (int i = 0; i < numCombs; i++) {
                    if (stop) {
                        throw new AbortException();
                    }
                    event2.setIntValue(i);
                    event2.setDescription("Permuting matrix: Current permutation = " + (i+1));
                    fireValueChanged(event2);   
                    
                    Vector validExpts = new Vector();
                    for (int j = 0; j < groupAssignments.length; j++) {
                        if (groupAssignments[j] == 1) {
                            validExpts.add(new Integer(j));
                        }
                    }
                    
                    int[] validArray = new int[validExpts.size()];
                    for (int j = 0; j < validArray.length; j++) {
                        validArray[j] = ((Integer)(validExpts.get(j))).intValue();
                    }  
                    
                    boolean[] changeSign = getOneClassChangeSignArrayAllUniquePerms(i, validArray);                    
                    FloatMatrix permutedMatrix = getOneClassPermMatrix(expMatrix, changeSign); 
                    
                    double[] currentPermTValues = getOneClassTValues(permutedMatrix);
                    
                    if (Double.isNaN(currentPermTValues[descGeneIndices[numGenes - 1]])) {
                        uMatrix[numGenes - 1][i] = Double.NEGATIVE_INFINITY;
                    } else {
                        uMatrix[numGenes - 1][i] = currentPermTValues[descGeneIndices[numGenes - 1]];
                    }
                    //System.out.println("uMatrix[" + (numGenes - 1) + "][" + i + "] = " + uMatrix[numGenes - 1][i]);
                    
                    for (int j = numGenes - 2; j >= 0; j--) {
                        if (Double.isNaN(currentPermTValues[descGeneIndices[j]])) {
                            uMatrix[j][i] = uMatrix[j+1][i];
                        } else {
                            uMatrix[j][i] = Math.max(uMatrix[j+1][i], currentPermTValues[descGeneIndices[j]]);
                        }
                        //System.out.println("uMatrix[" + j + "][" + i + "] = " + uMatrix[j][i]);
                    }                   
                    
                    /*
                    QSort sortDescCurrentPermTValues = new QSort(currentPermTValues, QSort.DESCENDING);
                    double[] descCurrentPermTValues = sortDescCurrentPermTValues.getSortedDouble();
                    
                    for (int j = 0; j < permutedRankedTValues[i].length; j++) {
                        permutedRankedTValues[i][j] = descCurrentPermTValues[j];
                    }   */                 
                    
                }
            }            
        }
        
        for (int i = 0; i < numGenes; i++) {
            int pCounter = 0;
            for (int j = 0; j < numCombs; j++) {
                /*
                if (permutedRankedTValues[j][i] >= descTValues[i]) {
                    pCounter++;
                }
                 */
                if (uMatrix[i][j] >= descTValues[i]) {
                    pCounter++;
                }
            }
            adjPValues[descGeneIndices[i]] = (double)pCounter/(double)numCombs;
                //System.out.println("valid i = " + i + ", Valid index = " + descGeneIndices[i]);   
            /*
            if (Double.isNaN(origTValues[i])) {
                adjPValues[i] = Double.NaN;
                System.out.println("NaN index = " + i);
            } else {
                // UP TO HERE Dec 15 2003
                int pCounter = 0;
                for (int j = 0; j < numCombs; j++) {
                    if (permutedRankedTValues[j][i] >= descTValues[i]) {
                        pCounter++;
                    }
                }
                adjPValues[descGeneIndices[i]] = (double)pCounter/(double)numCombs;
                System.out.println("valid i = " + i + ", Valid index = " + descGeneIndices[i]);
            }
            */
        }
        
        int NaNPCounter = 0;
        for (int i = 0; i < numGenes; i++) {
            if (Double.isNaN(origTValues[i])) {
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
        
        return adjPValues;
    } 
    
    double[] getOneClassTValues(FloatMatrix inputMatrix) {
        double[] tValsFromMatrix = new double[numGenes];
        for (int i = 0; i < numGenes; i++) {
            tValsFromMatrix[i] = Math.abs(getOneClassTValue(i, inputMatrix));
        }
        
        return tValsFromMatrix;
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
    
    private float getTValue(int gene, FloatMatrix inputMatrix) {
        float[] geneValues = new float[numExps];
        for (int i = 0; i < numExps; i++) {
            geneValues[i] = inputMatrix.A[gene][i];
        }
        
        int groupACounter = 0;
        int groupBCounter = 0;
        
        for (int i = 0; i < groupAssignments.length; i++) {
            if (groupAssignments[i] == Ttest.GROUP_A) {
                groupACounter++;
            } else if (groupAssignments[i] == Ttest.GROUP_B) {
                groupBCounter++;
            }
        }
        
        float[] groupAValues = new float[groupACounter];
        float[] groupBValues = new float[groupBCounter];
        
        groupACounter = 0;
        groupBCounter = 0;
        
        for (int i = 0; i < groupAssignments.length; i++) {
            if (groupAssignments[i] == Ttest.GROUP_A) {
                groupAValues[groupACounter] = geneValues[i];
                groupACounter++;
            } else if (groupAssignments[i] == Ttest.GROUP_B) {
                groupBValues[groupBCounter] = geneValues[i];
                groupBCounter++;
            }
        }
        
        float tValue = calculateTValue(groupAValues, groupBValues);
        return tValue;        
    }    
    
    private double[] getTwoClassUnpairedTValues(FloatMatrix inputMatrix) {
        double[] tValsFromMatrix = new double[numGenes];
        for (int i = 0; i < numGenes; i++) {
            tValsFromMatrix[i] = Math.abs(getTValue(i, inputMatrix));
        }
        
        return tValsFromMatrix;
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
        
        /*
        long[] randomSeeds = new long[permutedValues.length - 1];
       
        
        for (int i = 0; i < randomSeeds.length; i++) {
            Random generator = new Random(i);
            randomSeeds[i] = generator.nextLong();
            //System.out.println("randomSeeds[" + i + "] =" + randomSeeds[i]);
        }
        */
        /*
        for (int i = permutedValues.length; i > 1; i--) {
            Random generator2 =new Random();
            //Random generator2 = new Random(randomSeeds[i - 2]);
            int randVal = generator2.nextInt(i - 1);
            int temp = permutedValues[randVal];
            permutedValues[randVal] = permutedValues[i - 1];
            permutedValues[i - 1] = temp;
        }
        */
        
        try {
            Thread.sleep(10);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        
        
        return permutedValues;
        
    }    
    
    private double[] getAdjBonfPVals(double[] rawPValArray) {
        double[] adjPValArray = new double[rawPValArray.length];
        isSig = new boolean[rawPValArray.length];
        for (int i = 0; i < isSig.length; i++) {
            isSig[i] = false;
        }
        QSort sortRawPs = new QSort(rawPValArray, QSort.ASCENDING);
        double[] sortedRawPVals = sortRawPs.getSortedDouble();
        int[] origIndices = sortRawPs.getOrigIndx();
        int n = numGenes;
        adjPValArray[origIndices[0]] = sortedRawPVals[0]*n;        
        for (int i = 1; i < numGenes; i++) {
            if (sortedRawPVals[i - 1] < sortedRawPVals[i]) n--;   
            if (n <= 0) n = 1;
            adjPValArray[origIndices[i]] = sortedRawPVals[i]*n;
        }
        
        for (int i = 0; i < adjPValArray.length; i++) {
            if (adjPValArray[i] > 1.0d) adjPValArray[i] = 1.0d;
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
        
        return adjPValArray;
    }   
    
    private int[] getPairedDfs(FloatMatrix inputMatrix) {
        int[] pairedDfs = new int[numGenes];
        for (int gene = 0; gene < numGenes; gene++) {
            
            float[] geneValues = new float[numExps];
            for (int i = 0; i < numExps; i++) {
                geneValues[i] = inputMatrix.A[gene][i];
            }
            
            float[] groupAValues = new float[pairedGroupAExpts.length];
            float[] groupBValues = new float[pairedGroupBExpts.length];
            
            int numbValidValuesA = 0;
            int numbValidValuesB = 0; 
            
            for (int i = 0; i < pairedGroupAExpts.length; i++) {                
                groupAValues[i] = geneValues[pairedGroupAExpts[i]];
                if (!Float.isNaN(geneValues[pairedGroupAExpts[i]])) {
                    numbValidValuesA++;
                }                
                groupBValues[i] = geneValues[pairedGroupBExpts[i]];
                if (!Float.isNaN(geneValues[pairedGroupBExpts[i]])) {
                    numbValidValuesB++;
                }
            }  
            
            if ((numbValidValuesA < 2) || (numbValidValuesB < 2)) {
                pairedDfs[gene] = -1;
            } else {
                int N = 0;                
                for (int i = 0; i < pairedGroupAExpts.length; i++) {
                    if ( (!Double.isNaN(inputMatrix.A[gene][pairedGroupAExpts[i]])) && (!Double.isNaN(inputMatrix.A[gene][pairedGroupBExpts[i]])) ) {                        
                        N++;
                    }
                }  
                pairedDfs[gene] = N - 1;
            }            
        }
        return pairedDfs;
    }
    
    private int[] getTwoClassDfs(FloatMatrix inputMatrix) {
        int[] twoClassDfs = new int[numGenes];
        for (int gene = 0; gene < numGenes; gene++) {
            float[] geneValues = new float[numExps];
            for (int i = 0; i < numExps; i++) {
                geneValues[i] = inputMatrix.A[gene][i];
            }  
            
            int groupACounter = 0;
            int groupBCounter = 0;
            
            for (int i = 0; i < groupAssignments.length; i++) {
                if (groupAssignments[i] == GROUP_A) {
                    groupACounter++;
                } else if (groupAssignments[i] == GROUP_B) {
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
                if (groupAssignments[i] == GROUP_A) {
                    groupAValues[groupACounter] = geneValues[i];
                    if (!Float.isNaN(geneValues[i])) {
                        numbValidValuesA++;
                    }
                    groupACounter++;
                } else if (groupAssignments[i] == GROUP_B) {
                    groupBValues[groupBCounter] = geneValues[i];
                    if (!Float.isNaN(geneValues[i])) {
                        numbValidValuesB++;
                    }
                    groupBCounter++;
                }
            } 
            
            if ((numbValidValuesA < 2) || (numbValidValuesB < 2)) {
                twoClassDfs[gene] = -1;
            } else {
                twoClassDfs[gene] = calculateDf(groupAValues, groupBValues);
            }            
            
        }
        
        return twoClassDfs;        
    }
    
    private int[] getOneClassDfs(FloatMatrix inputMatrix) {
        int[] oneClassDfValues = new int[numGenes];
        for (int gene = 0; gene < numGenes; gene++) {            
            Vector currentGene = new Vector();
            
            for (int i = 0; i < numExps; i++) {
                if (groupAssignments[i] == 1) {
                    currentGene.add(new Float(inputMatrix.A[gene][i]));
                }
            }    
            float[] currGeneArray = new float[currentGene.size()];
            
            for (int i = 0; i < currGeneArray.length; i++) {
                currGeneArray[i] = ((Float)(currentGene.get(i))).floatValue();
            } 
           int validNum = 0;
           for (int i = 0; i < currGeneArray.length; i++) {
               if (!Float.isNaN(currGeneArray[i])) {
                   validNum++;
               }
           }
           
           oneClassDfValues[gene] = validNum -1;            
        }
        
        return oneClassDfValues;
    }
    
    private void computeOneClassOrigVals() throws AlgorithmException {
        event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numGenes);
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);
        
        oneClassMeans = new double[numGenes];
        oneClassSDs = new double[numGenes];
        dfValues = new double[numGenes];
        tValues = new double[numGenes]; 
        
        for (int gene = 0; gene < numGenes; gene++) {
            if (stop) {
                throw new AbortException();
            }
            event.setIntValue(gene);
            event.setDescription("Calculating t values: Current gene = " + (gene + 1));
            fireValueChanged(event);
            
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
            
           tValues[gene] = getOneClassTValue(currGeneArray);  
           oneClassMeans[gene] = (double)(getMean(currGeneArray));
           oneClassSDs[gene] = Math.sqrt((double)(getVar(currGeneArray)));
           
           int validNum = 0;
           for (int i = 0; i < currGeneArray.length; i++) {
               if (!Float.isNaN(currGeneArray[i])) {
                   validNum++;
               }
           }
           
           dfValues[gene] = (double)(validNum -1);            
        }
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
        
        
        
        return Math.abs(tValue);
    }    
   
    private void computePairedOrigVals() throws AlgorithmException {
        event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numGenes);
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);     
        
        groupAMeans = new double[numGenes];
        groupBMeans = new double[numGenes];
        groupASDs = new double[numGenes];
        groupBSDs = new double[numGenes];
        dfValues = new double[numGenes];
        tValues = new double[numGenes];  
        for (int gene = 0; gene < numGenes; gene++) {
            if (stop) {
                throw new AbortException();
            }
            event.setIntValue(gene);
            event.setDescription("Calculating t values: Current gene = " + (gene + 1));
            fireValueChanged(event);
            
            float[] geneValues = new float[numExps];
            for (int i = 0; i < numExps; i++) {
                geneValues[i] = expMatrix.A[gene][i];
            }            
           
            float[] groupAValues = new float[pairedGroupAExpts.length];
            float[] groupBValues = new float[pairedGroupBExpts.length];
            
            int numbValidValuesA = 0;
            int numbValidValuesB = 0;
            
            for (int i = 0; i < pairedGroupAExpts.length; i++) {                
                groupAValues[i] = geneValues[pairedGroupAExpts[i]];
                if (!Float.isNaN(geneValues[pairedGroupAExpts[i]])) {
                    numbValidValuesA++;
                }                
                groupBValues[i] = geneValues[pairedGroupBExpts[i]];
                if (!Float.isNaN(geneValues[pairedGroupBExpts[i]])) {
                    numbValidValuesB++;
                }
            }  
            
            if ((numbValidValuesA < 2) || (numbValidValuesB < 2)) {
                tValues[gene] = Double.NaN;
                dfValues[gene] = Double.NaN;
                groupAMeans[gene] = Double.NaN;
                groupBMeans[gene] = Double.NaN;
                groupASDs[gene] = Double.NaN;
                groupBSDs[gene] = Double.NaN;
            } else {
                tValues[gene] = getPairedTValue(gene, expMatrix);
                
                int N = 0;                
                for (int i = 0; i < pairedGroupAExpts.length; i++) {
                    if ( (!Double.isNaN(expMatrix.A[gene][pairedGroupAExpts[i]])) && (!Double.isNaN(expMatrix.A[gene][pairedGroupBExpts[i]])) ) {                        
                        N++;
                    }
                }              
                
                dfValues[gene] = (double)(N - 1);
                groupAMeans[gene] = (double)(getMean(groupAValues));
                groupBMeans[gene] = (double)(getMean(groupBValues));
                groupASDs[gene] = (double)(Math.sqrt(getVar(groupAValues)));
                groupBSDs[gene] = (double)(Math.sqrt(getVar(groupBValues)));
            }            
        }       
    }
    
    private double getPairedTValue(int gene, FloatMatrix inputMatrix) {//Jaccard & Becker, 2nd ed. pg 250
        double sumDev = 0d;
        int N = 0;

        for (int i = 0; i < pairedGroupAExpts.length; i++) {
            if ( (!Double.isNaN(inputMatrix.A[gene][pairedGroupAExpts[i]])) && (!Double.isNaN(inputMatrix.A[gene][pairedGroupBExpts[i]])) ) {
                sumDev = sumDev + (double)(inputMatrix.A[gene][pairedGroupAExpts[i]] - inputMatrix.A[gene][pairedGroupBExpts[i]]);
                N++;
            }
        }

        if (N < 2) return Double.NaN;
        
        double meanDev = sumDev/(double)N;
        
        double sumSquaredDev = 0d;
        for (int i = 0; i < pairedGroupAExpts.length; i++) {
            if ( (!Double.isNaN(inputMatrix.A[gene][pairedGroupAExpts[i]])) && (!Double.isNaN(inputMatrix.A[gene][pairedGroupBExpts[i]])) ) {
                sumSquaredDev = sumSquaredDev + Math.pow((double)(inputMatrix.A[gene][pairedGroupAExpts[i]] - inputMatrix.A[gene][pairedGroupBExpts[i]]), 2);
            }
        }
        
        //int N = pairedGroupAExpts.length;
        
        double ssD = sumSquaredDev - Math.pow(sumDev, 2)/(double)N;
        double sHatDSquared = ssD/(double)(N - 1);
        double sHatDBar = Math.sqrt(sHatDSquared/(double)N);
        
        return Math.abs((meanDev/sHatDBar));
    }
    
    private double[] getPairedTValues(FloatMatrix inputMatrix) {
        double[] pairedTValues = new double[inputMatrix.getRowDimension()];
        for (int i = 0; i < pairedTValues.length; i++) {
            pairedTValues[i] = getPairedTValue(i, inputMatrix);
        }
        return pairedTValues;
    }
    
    private void computeBtnSubOrigVals() throws AlgorithmException {
        event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numGenes);
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);
        
        groupAMeans = new double[numGenes];
        groupBMeans = new double[numGenes];
        groupASDs = new double[numGenes];
        groupBSDs = new double[numGenes];
        dfValues = new double[numGenes];
        tValues = new double[numGenes];
        
        for (int gene = 0; gene < numGenes; gene++) {
            if (stop) {
                throw new AbortException();
            }
            event.setIntValue(gene);
            event.setDescription("Calculating t values: Current gene = " + (gene + 1));
            fireValueChanged(event);
            
            float[] geneValues = new float[numExps];
            for (int i = 0; i < numExps; i++) {
                geneValues[i] = expMatrix.A[gene][i];
            }
            
            int groupACounter = 0;
            int groupBCounter = 0;
            
            for (int i = 0; i < groupAssignments.length; i++) {
                if (groupAssignments[i] == GROUP_A) {
                    groupACounter++;
                } else if (groupAssignments[i] == GROUP_B) {
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
                if (groupAssignments[i] == GROUP_A) {
                    groupAValues[groupACounter] = geneValues[i];
                    if (!Float.isNaN(geneValues[i])) {
                        numbValidValuesA++;
                    }
                    groupACounter++;
                } else if (groupAssignments[i] == GROUP_B) {
                    groupBValues[groupBCounter] = geneValues[i];
                    if (!Float.isNaN(geneValues[i])) {
                        numbValidValuesB++;
                    }
                    groupBCounter++;
                }
            }  
            
            if ((numbValidValuesA < 2) || (numbValidValuesB < 2)) {
                tValues[gene] = Double.NaN;
                dfValues[gene] = Double.NaN;
                groupAMeans[gene] = Double.NaN;
                groupBMeans[gene] = Double.NaN;
                groupASDs[gene] = Double.NaN;
                groupBSDs[gene] = Double.NaN;
            } else {
                tValues[gene] = (double)(calculateTValue(groupAValues, groupBValues));
                dfValues[gene] = (double)(calculateDf(groupAValues, groupBValues));
                groupAMeans[gene] = (double)(getMean(groupAValues));
                groupBMeans[gene] = (double)(getMean(groupBValues));
                groupASDs[gene] = (double)(Math.sqrt(getVar(groupAValues)));
                groupBSDs[gene] = (double)(Math.sqrt(getVar(groupBValues)));
            }
            
        }
    }
    
    private float calculateTValue(float[] groupA, float[] groupB) {
        if (useWelchDf) {
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
            
            //float tValue = (float)((meanA - meanB) / Math.sqrt((varA/kA) + (varB/kB)));
            //changed to divide by valid n
            float tValue = (float)((meanA - meanB) / Math.sqrt((varA/numbValidGroupAValues) + (varB/numbValidGroupBValues)));
            
            return Math.abs(tValue);
            
        } else { // if(!useWelchDf)
            float tValue;
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
            
            float ssA = getSumSquares(groupA);
            float ssB = getSumSquares(groupB);
            
            float term1 = (float)(ssA + ssB);
            float term2 = (float)(numbValidGroupAValues + numbValidGroupBValues - 2);
            float term3 = (float)((1.0f/(float)numbValidGroupAValues) +(1.0f/(float)numbValidGroupBValues));
            
            float denom = (float)Math.sqrt(((term1*term3)/term2));
            //float denom = (float)Math.sqrt((((ssA + ssB)/(numbValidGroupAValues + numbValidGroupBValues - 2))*((1/numbValidGroupAValues) + (1/numbValidGroupBValues))));
            //System.out.println("ssA = " + ssA + ", ssB = " + ssB + "nA = " +  numbValidGroupAValues + ", nB = " + numbValidGroupBValues + ", denom = " + denom);
            float meanA = getMean(groupA);
            float meanB = getMean(groupB);  
            
            tValue = (float)((meanA - meanB)/denom);
            
            return Math.abs(tValue);
        }
    }   
    
    private float getSumSquares(float[] arr) {
        int N = 0;
        float sumX = 0f;
        float sumXSquared = 0f;
        for (int i = 0; i < arr.length; i++) {
            if (!Float.isNaN(arr[i])) {
                N++;
                sumX += arr[i];
                sumXSquared += arr[i]*arr[i];
            }
        }
        if (N == 0) {
            return Float.NaN;
        } else {
            float sumSq = sumXSquared - ((sumX*sumX)/N);
            return sumSq;
        }
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
        
        if (!useWelchDf) {
            int df = kA + kB - 2;
            if (df < 0) df = 0;
            return df;
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
        
        int df = (int)Math.floor(numerator / denom);
        
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
