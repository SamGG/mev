/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: Ttest.java,v $
 * $Revision: 1.3 $
 * $Date: 2004-01-13 17:31:02 $
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
    
    boolean hierarchical_tree;
    int method_linkage;
    boolean calculate_genes;
    boolean calculate_experiments;
    
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
        
        hierarchical_tree = map.getBoolean("hierarchical-tree", false);
        method_linkage = map.getInt("method-linkage", 0);
        calculate_genes = map.getBoolean("calculate-genes", false);
        calculate_experiments = map.getBoolean("calculate-experiments", false);
        
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
        
        if (significanceMethod == TtestInitDialog.MIN_P) {
            return executeMinP();
        } else if (significanceMethod == TtestInitDialog.MAX_T) {
            return executeMaxT();
        }
        
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
    
    public AlgorithmData executeMaxT() throws AlgorithmException {
        double[] origTValues = new double[numGenes];
        double[] descTValues = new double[numGenes];
        int[] descGeneIndices = new int[numGenes];
        double[] adjPValues = new double[numGenes];
        double[][] permutedRankedTValues = new double[numCombs][numGenes];
        double[][] uMatrix = new double[numGenes][numCombs];
        AlgorithmEvent event2 = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numCombs);
        fireValueChanged(event2);
        event2.setId(AlgorithmEvent.PROGRESS_VALUE);        
        if (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) {
            for (int i = 0; i < numGenes; i++) {
                origTValues[i] = Math.abs(getTValue(i));
            }
            
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
                        if (groupAssignments[j] != TtestInitDialog.NEITHER_GROUP) {
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
                    if (groupAssignments[i] != TtestInitDialog.NEITHER_GROUP) {
                        usedExptsVector.add(new Integer(i));
                    }
                    if (groupAssignments[i] == TtestInitDialog.GROUP_A) {
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
        } else if (tTestDesign == TtestInitDialog.ONE_CLASS) { // if tTestDesign == TtestInitDialog.ONE_CLASS
            for (int i = 0; i < numGenes; i++) {
                origTValues[i] = Math.abs(getOneClassTValue(i));
            }  
            
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
        
        adjPValues = new double[numGenes];
        
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
      
        Vector clusterVector = new Vector();
        Vector sigGenes = new Vector();
        Vector nonSigGenes = new Vector();
        for (int i = 0; i < numGenes; i++) {
            if (Double.isNaN(adjPValues[i])) {
                nonSigGenes.add(new Integer(i));
            } else if ((float)adjPValues[i] <= alpha) {
                sigGenes.add(new Integer(i));
            } else {
                nonSigGenes.add(new Integer(i));
            }
        }
        
        clusterVector.add(sigGenes);
        clusterVector.add(nonSigGenes);
        
        k = clusterVector.size();
        
        
        FloatMatrix isSigMatrix = new FloatMatrix(numGenes, 1);
        
        for (int i = 0; i < isSigMatrix.getRowDimension(); i++) {
            isSigMatrix.A[i][0] = 0.0f;
        }
        
        //Vector sigGenes = (Vector)(clusterVector.get(0));
        
        for (int i = 0 ; i < sigGenes.size(); i++) {
            int currentGene = ((Integer)(sigGenes.get(i))).intValue();
            isSigMatrix.A[currentGene][0] = 1.0f;
        }        
        
        FloatMatrix tValuesMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix pValuesMatrix = new FloatMatrix(numGenes, 1);
        //System.out.println("pValuesVector.size() = " + pValuesVector.size());
        FloatMatrix dfMatrix = new FloatMatrix(numGenes, 1);     
        FloatMatrix meansAMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix meansBMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix sdAMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix sdBMatrix = new FloatMatrix(numGenes, 1); 
        FloatMatrix oneClassMeansMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix oneClassSDsMatrix = new FloatMatrix(numGenes, 1);        

        if (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) {
            for (int i = 0; i < numGenes; i++) {
                tValuesMatrix.A[i][0] = (float)(origTValues[i]);
                pValuesMatrix.A[i][0] = (float)(adjPValues[i]);
                dfMatrix.A[i][0] = (float)(getDF(i));
            }
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
        }  else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
            for (int i = 0; i < numGenes; i++) {
                float[] currentGeneValues = getOneClassGeneValues(i);
                tValuesMatrix.A[i][0] = (float)(origTValues[i]);
                pValuesMatrix.A[i][0] = (float)(adjPValues[i]);
                dfMatrix.A[i][0] = (float)(getOneClassDFValue(currentGeneValues));
                oneClassMeansMatrix.A[i][0] = getMean(currentGeneValues);
                oneClassSDsMatrix.A[i][0] = (float)(Math.sqrt(getVar(currentGeneValues)));
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
        //return null; // for now
    }
    
    public AlgorithmData executeMinP() throws AlgorithmException {
        double[] origTValues = new double[numGenes];
        double[] rawPValues = new double[numGenes];
        double[] adjPValues = new double[numGenes];
        double[] sortedRawPValues = new double[1];
        double[][] origTMatrix = new double[numGenes][numCombs];
        double[][] qMatrix = new double[numGenes + 1][numCombs];
        double[][] sortedTMatrix = new double[numGenes][numCombs];
        double[][] pMatrix = new double[numGenes][numCombs];
        int[] sortedRawPValueIndices = new int[1];
        
        for (int i = 0; i < numCombs; i++) {
            qMatrix[numGenes][i] = 1.0d; // initialize q(m+1,b) = 1 from Ge, Dudoit and Speed 2003
        }
        
        AlgorithmEvent event2 = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numCombs);
        fireValueChanged(event2);
        event2.setId(AlgorithmEvent.PROGRESS_VALUE);
        
        if (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) {
            for (int i = 0; i < numGenes; i++) {
                origTValues[i] = Math.abs(getTValue(i));
            }            
            if (!useAllCombs) {
                for (int i = 0; i < numCombs; i++) {
                    //System.out.print("Permutation " + (i + 1) +" : ");
                    if (stop) {
                        throw new AbortException();
                    }
                    event2.setIntValue(i);
                    event2.setDescription("Permuting matrix: Current permutation = " + (i+1));
                    fireValueChanged(event2); 
                    
                    int[] permutedExpts = new int[1];
                    Vector validExpts = new Vector();
                    
                    for (int j = 0; j < groupAssignments.length; j++) {
                        if (groupAssignments[j] != TtestInitDialog.NEITHER_GROUP) {
                            validExpts.add(new Integer(j));
                        }
                    }
                    
                    int[] validArray = new int[validExpts.size()];
                    for (int j = 0; j < validArray.length; j++) {
                        validArray[j] = ((Integer)(validExpts.get(j))).intValue();
                    }
                    
                    permutedExpts = getPermutedValues(numExps, validArray); //returns an int array of size "numExps", with the valid values permuted 
                    
                    /*
                    for (int j = 0; j < permutedExpts.length; j++) {
                        System.out.print(" " + permutedExpts[j]);
                    }
                    System.out.println();            
                    */
                    FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
                    double[] currentPermTValues = getTwoClassUnpairedTValues(permutedMatrix);                    
                    for (int j = 0; j < numGenes; j++) {
                        origTMatrix[j][i] = currentPermTValues[j];
                    } 
                }
                /*
                for (int i = 0; i < numGenes; i++) {
                    double currentTValue = (double)getTValue(i);
                    if (Double.isNaN(currentTValue)) {
                        rawPValues[i] = Double.NaN;
                    } else {
                        int pCounter = 0;                        
                        for (int j = 0; j < numCombs; j++) {
                            if (origTMatrix[i][j] >= currentTValue) {
                                pCounter++;
                            }
                        }
                        rawPValues[i] = (double)pCounter/(double)numCombs;
                    }
                }
                
                QSort sortRawPValues = new QSort(rawPValues, QSort.ASCENDING);
                sortedRawPValues = sortRawPValues.getSortedDouble();
                sortedRawPValueIndices = sortRawPValues.getOrigIndx();
                
                for (int i = 0; i < numGenes; i++) {
                    for (int j = 0; j < numCombs; j++) {
                        sortedTMatrix[i][j] = origTMatrix[sortedRawPValueIndices[i]][j];
                    }
                } 
                */
                
            } else { //if (useAllCombs)
                int[] permutedExpts = new int[numExps];
                
                for (int i = 0; i < numExps; i++) {
                    permutedExpts[i] = i;
                }    
                
                Vector usedExptsVector = new Vector();
                int numGroupAValues = 0;
                for (int i = 0; i < groupAssignments.length; i++) {
                    if (groupAssignments[i] != TtestInitDialog.NEITHER_GROUP) {
                        usedExptsVector.add(new Integer(i));
                    }
                    if (groupAssignments[i] == TtestInitDialog.GROUP_A) {
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
                    //System.out.print("Permutation " + (permCounter + 1) +" : ");
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
                    
                    /*
                    for (int i = 0; i < permutedExpts.length; i++) {
                        System.out.print(" " + permutedExpts[i]);
                    }
                    System.out.println();
                    */
                    FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
                    double[] currentPermTValues = getTwoClassUnpairedTValues(permutedMatrix);                    
                    for (int j = 0; j < numGenes; j++) {
                        origTMatrix[j][permCounter] = currentPermTValues[j];
                    }  
                    
                    permCounter++;
                }               
                
                //System.out.println("permCounter = " + permCounter + ", numCombs = " + numCombs);
            }
            
            for (int i = 0; i < numGenes; i++) {
                double currentTValue = (double)getTValue(i);
                if (Double.isNaN(currentTValue)) {
                    rawPValues[i] = Double.NaN;
                } else {
                    int pCounter = 0;
                    for (int j = 0; j < numCombs; j++) {
                        if (origTMatrix[i][j] >= currentTValue) {
                            pCounter++;
                        }
                    }
                    rawPValues[i] = (double)pCounter/(double)numCombs;
                }
            }
            
            QSort sortRawPValues = new QSort(rawPValues, QSort.ASCENDING);
            sortedRawPValues = sortRawPValues.getSortedDouble();
            sortedRawPValueIndices = sortRawPValues.getOrigIndx();
            
            for (int i = 0; i < numGenes; i++) {
                for (int j = 0; j < numCombs; j++) {
                    sortedTMatrix[i][j] = origTMatrix[sortedRawPValueIndices[i]][j];
                }
            }           
            
            
        } else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
            for (int i = 0; i < numGenes; i++) {
                origTValues[i] = Math.abs(getOneClassTValue(i));
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
                    for (int j = 0; j < numGenes; j++) {
                        origTMatrix[j][i] = currentPermTValues[j];
                    }                    
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
                    for (int j = 0; j < numGenes; j++) {
                        origTMatrix[j][i] = currentPermTValues[j];
                    } 
                }
            }
            
            for (int i = 0; i < numGenes; i++) {
                double currentTValue = (double)getOneClassTValue(i);
                if (Double.isNaN(currentTValue)) {
                    rawPValues[i] = Double.NaN;
                } else {
                    int pCounter = 0;
                    for (int j = 0; j < numCombs; j++) {
                        if (origTMatrix[i][j] >= currentTValue) {
                            pCounter++;
                        }
                    }
                    rawPValues[i] = (double)pCounter/(double)numCombs;
                }
            }
            
            QSort sortRawPValues = new QSort(rawPValues, QSort.ASCENDING);
            sortedRawPValues = sortRawPValues.getSortedDouble();
            sortedRawPValueIndices = sortRawPValues.getOrigIndx();
            
            for (int i = 0; i < numGenes; i++) {
                for (int j = 0; j < numCombs; j++) {
                    sortedTMatrix[i][j] = origTMatrix[sortedRawPValueIndices[i]][j];
                }
            }            
        }
        
        double[] sortedAdjPValues = new double[numGenes];
        
        int currentGeneCounter = 0;
        AlgorithmEvent event3 = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numGenes);
        fireValueChanged(event3);
        event3.setId(AlgorithmEvent.PROGRESS_VALUE);        
        for (int i = numGenes - 1; i >= 0; i--) {
            //currentGeneCounter++;            
            event3.setIntValue(currentGeneCounter);
            event3.setDescription("Calculating p-values: Current gene = " + (i+1));
            fireValueChanged(event3);         
            double[] currentGeneTVals = new double[numCombs];
            for (int j = 0; j < numCombs; j++) {
                currentGeneTVals[j] = sortedTMatrix[i][j];                
            }
            QSort sortCurrentGeneTVals = new QSort(currentGeneTVals, QSort.DESCENDING);
            double[] sortedCurrentGeneTVals = sortCurrentGeneTVals.getSortedDouble();
            int[] currentGeneTValsSortedIndices = sortCurrentGeneTVals.getOrigIndx();
            /*
            System.out.print("Gene " + i + ": sortedCurrentTVals = ");
            
            for (int j = 0; j < 20; j++) {
                System.out.print(" " + sortedCurrentGeneTVals[j]);
            }
             
            System.out.println();
            */
            System.out.print("Gene " + i + ": ");
            double[] currentGeneSortedPVals = getPValsFromOrderStats(sortedCurrentGeneTVals);    
            System.out.println();
            //DONE UP TO HERE O1/05/2004
            for (int j = 0; j < pMatrix[i].length; j++) {
                pMatrix[i][j] = currentGeneSortedPVals[currentGeneTValsSortedIndices[j]];
            }
            
            for (int j = 0; j < qMatrix[i].length; j++) {
                qMatrix[i][j] = Math.min(qMatrix[i+1][j], pMatrix[i][j]);
            }
            
            int adjPCounter = 0;
            
            for (int j = 0; j < qMatrix[i].length; j++) {
                if (qMatrix[i][j] <= sortedRawPValues[i]) {
                    adjPCounter++;
                }
            }
            
            sortedAdjPValues[i] = (double)adjPCounter/(double)numCombs;
            currentGeneCounter++;
        }
        
        for (int i = 1; i < sortedAdjPValues.length; i++) { // enforcing monotonicity of p-values
            sortedAdjPValues[i] = Math.max(sortedAdjPValues[i - 1], sortedAdjPValues[i]);
        }
        
        for (int i = 0; i < sortedAdjPValues.length; i++) {
            adjPValues[i] = sortedAdjPValues[sortedRawPValueIndices[i]];
            if (Double.isNaN(rawPValues[i])) {
                adjPValues[i] = Double.NaN;
            }
        }
        
        Vector clusterVector = new Vector();
        Vector sigGenes = new Vector();
        Vector nonSigGenes = new Vector();
        for (int i = 0; i < numGenes; i++) {
            if (Double.isNaN(adjPValues[i])) {
                nonSigGenes.add(new Integer(i));
            } else if ((float)adjPValues[i] <= alpha) {
                sigGenes.add(new Integer(i));
            } else {
                nonSigGenes.add(new Integer(i));
            }
        }    
        
        clusterVector.add(sigGenes);
        clusterVector.add(nonSigGenes);
        
        k = clusterVector.size();
        
        
        FloatMatrix isSigMatrix = new FloatMatrix(numGenes, 1);
        
        for (int i = 0; i < isSigMatrix.getRowDimension(); i++) {
            isSigMatrix.A[i][0] = 0.0f;
        }
        
        //Vector sigGenes = (Vector)(clusterVector.get(0));
        
        for (int i = 0 ; i < sigGenes.size(); i++) {
            int currentGene = ((Integer)(sigGenes.get(i))).intValue();
            isSigMatrix.A[currentGene][0] = 1.0f;
        }    
        
        FloatMatrix tValuesMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix pValuesMatrix = new FloatMatrix(numGenes, 1);
        //System.out.println("pValuesVector.size() = " + pValuesVector.size());
        FloatMatrix dfMatrix = new FloatMatrix(numGenes, 1);     
        FloatMatrix meansAMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix meansBMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix sdAMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix sdBMatrix = new FloatMatrix(numGenes, 1); 
        FloatMatrix oneClassMeansMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix oneClassSDsMatrix = new FloatMatrix(numGenes, 1);    
        
        if (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) {
            for (int i = 0; i < numGenes; i++) {
                tValuesMatrix.A[i][0] = (float)(origTValues[i]);
                pValuesMatrix.A[i][0] = (float)(adjPValues[i]);
                dfMatrix.A[i][0] = (float)(getDF(i));
            }
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
        }  else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
            for (int i = 0; i < numGenes; i++) {
                float[] currentGeneValues = getOneClassGeneValues(i);
                tValuesMatrix.A[i][0] = (float)(origTValues[i]);
                pValuesMatrix.A[i][0] = (float)(adjPValues[i]);
                dfMatrix.A[i][0] = (float)(getOneClassDFValue(currentGeneValues));
                oneClassMeansMatrix.A[i][0] = getMean(currentGeneValues);
                oneClassSDsMatrix.A[i][0] = (float)(Math.sqrt(getVar(currentGeneValues)));
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
        
        //return null; // for now
    }
    
    public void abort() {
        stop = true;
    }
    
    private double[] getPValsFromOrderStats(double[] sortedTVals) {
        double[] pVals = new double[sortedTVals.length];
        int[] ranksArray = new int[sortedTVals.length];
        
        if (Double.isNaN(sortedTVals[0])) {
            for (int i = 0; i < pVals.length; i++) {
                pVals[i] = Double.NaN;
            }
            return pVals;
        }
        
        Vector ranksVector = new Vector();
        Vector ranksCounterVector = new Vector();
        ranksVector.add(new Integer(1));
        //int rankCounter = 1;
        ranksArray[0] = 1;
        
        for (int i = 1; i < sortedTVals.length; i++) {
            if (Double.isNaN(sortedTVals[i])) {
                ranksArray[i] = -1;
            } else {
                if (sortedTVals[i - 1] > sortedTVals[i]) {
                    ranksArray[i] = ranksArray[i - 1] + 1;
                    ranksVector.add(new Integer(ranksArray[i - 1] + 1));
                    //ranksCounterVector.add(new Integer(rankCounter));
                } else {
                   ranksArray[i] = ranksArray[i - 1];                   
                }
            }
        }
        
        /*
        for (int i = 0; i < ranksArray.length; i++) {
            System.out.println("ranksArray[" + i + "] = " + ranksArray[i]);
        }
        
        for (int i = 0; i < ranksVector.size(); i++) {
            System.out.println("ranksVector(" + i + ") = " + ((Integer)(ranksVector.get(i))).intValue());
        }
         */
         
 
        int currCounter = 0;
        
        for (int i = 0; i < ranksVector.size(); i++) {
            int currRank = ((Integer)(ranksVector.get(i))).intValue();
            int currRankCounter = 0;
            for (int j = currCounter; j < ranksArray.length; j++) {
                if (currRank == ranksArray[j]) {
                    currRankCounter++;
                    currCounter++;
                } else {
                    ranksCounterVector.add(new Integer(currRankCounter));
                    break;
                }
            }
            
            if (i == ranksVector.size() - 1) {
                ranksCounterVector.add(new Integer(currRankCounter));
            }
        }
        /* 
        System.out.println("ranksVector.size() = " + ranksVector.size() + ", ranksCounterVector.size() = " + ranksCounterVector.size());
        
        for (int i = 0; i < ranksCounterVector.size(); i++) {
            System.out.println("ranksCounterVector(" + i + ") = " + ((Integer)(ranksCounterVector.get(i))).intValue());
        } 
         */
               
        
        int[] numerators = new int[ranksArray.length];
        
        int currentNumerator = 0;
        int currentIndex = 0;
        for (int i = 0; i < ranksVector.size(); i++) {
            currentNumerator = currentNumerator + ((Integer)(ranksCounterVector.get(i))).intValue();
            
            for (int j = currentIndex; j < currentNumerator; j++) {
                numerators[j] = currentNumerator;
                currentIndex++;
            }
        }
        /*
        for (int i = 0; i < numerators.length; i++) {
            System.out.println("numerators[" + i + "] = " + numerators[i]);
        }
        */
        for (int i = 0; i < numerators.length; i++) {
            if (Double.isNaN(sortedTVals[i])) {
                pVals[i] = Double.NaN;
            } else {
                pVals[i] = (double)numerators[i]/(double)numCombs;
            }
        }
        
        for (int i = 0; i < pVals.length; i++) {
            System.out.print("  " + pVals[i]);
        }
        
        return pVals;
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
    
    private FloatMatrix getOrderedByTMatrix(FloatMatrix inputMatrix, int[] rowOrder) {
        FloatMatrix orderedMatrix = new FloatMatrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());
        
        for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
            for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                orderedMatrix.A[i][j] = inputMatrix.A[rowOrder[i]][j];
            }
        }
        
        return orderedMatrix;        
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
    
    private double getOneClassTValue(int gene) {
        float[] currentGene = getOneClassGeneValues(gene);
        return getOneClassTValue(currentGene); 
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
    
    double[] getOneClassTValues(FloatMatrix inputMatrix) {
        double[] tValsFromMatrix = new double[numGenes];
        for (int i = 0; i < numGenes; i++) {
            tValsFromMatrix[i] = Math.abs(getOneClassTValue(i, inputMatrix));
        }
        
        return tValsFromMatrix;
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
    
    private float getTValue(int gene, FloatMatrix inputMatrix) {
        float[] geneValues = new float[numExps];
        for (int i = 0; i < numExps; i++) {
            geneValues[i] = inputMatrix.A[gene][i];
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
