/*
 * TFA.java
 *
 * Created on February 12, 2004, 11:22 AM
 */

package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.util.Vector;
import java.util.Random;

import JSci.maths.statistics.FDistribution;

import org.tigr.util.FloatMatrix;
import org.tigr.util.ConfMap;
import org.tigr.util.QSort;
  
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.NodeValueList;

import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
/**
 *
 * @author  nbhagaba
 */
public class TFA extends AbstractAlgorithm {
    
    public static final int JUST_ALPHA = 4;
    public static final int STD_BONFERRONI = 5;
    public static final int ADJ_BONFERRONI = 6;    
    public static final int MAX_T = 9;
    public static final int MIN_P = 10;  
    
    public static final int HAS_EMPTY_CELL = 21;
    public static final int ALL_CELLS_HAVE_ONE_SAMPLE = 22;
    public static final int SOME_CELLS_HAVE_ONE_SAMPLE = 23;
    public static final int BALANCED_WITH_REPLICATION = 24;
    public static final int UNBALANCED_WITH_REPLICATION = 25;

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
    
    private int[] numFactorLevels, factorAAssignments, factorBAssignments;
    private boolean allCellsHaveOneSample, isBalancedDesign, usePerms;
    private int adjustmentMethod;
    private float alpha;
    //private Vector[][] bothFactorAssignments;
    private int numPerms;
    
    double[] origFactorAPValues, origFactorBPValues, origInteractionPValues, factorAFValues, factorBFValues, interactionFValues; 
    double[] factorADfValues, factorBDfValues, interactionDfValues, errorDfValues;
    double[] adjFactorAPValues, adjFactorBPValues, adjInteractionPValues;
    /**
     * This method should interrupt the calculation.
     *
     */
    public void abort() {
        stop = true;
    }
    
    /**
     * This method execute calculation and return result,
     *
     * stored in <code>AlgorithmData</code> class.
     *
     *
     *
     * @param data the data to be calculated.
     *
     */
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
        
        //bothFactorAssignments = (Vector[][])(data.getObjectMatrix("bothFactorAssignments"));
        numFactorLevels = data.getIntArray("numFactorLevels");
        factorAAssignments = data.getIntArray("factorAAssignments");
        factorBAssignments = data.getIntArray("factorBAssignments");
        
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
        
        allCellsHaveOneSample = map.getBoolean("allCellsHaveOneSample", false);
        isBalancedDesign = map.getBoolean("isBalancedDesign", false);
        usePerms = map.getBoolean("usePerms", true);
        adjustmentMethod = map.getInt("adjustmentMethod", JUST_ALPHA);
        alpha = map.getFloat("alpha", 0.01f);
        numPerms = map.getInt("numPerms", 1000);
        
        /*
        System.out.println("numFactorLevels[0] = " + numFactorLevels[0]);
        System.out.println("numFactorLevels[1] = " + numFactorLevels[1]);
        System.out.println("allCellsHaveOneSample = " + String.valueOf(allCellsHaveOneSample));
        System.out.println("isBalancedDesign = " + String.valueOf(isBalancedDesign));
        System.out.println("usePerms = " + String.valueOf(usePerms));
        System.out.println("adjustmentMethod = " + adjustmentMethod);
        System.out.println("alpha = " + alpha);
        System.out.println("numPerms = " + numPerms);
        for (int i = 0; i < bothFactorAssignments.length; i++) {
            for (int j = 0; j < bothFactorAssignments[i].length; j++) {
                System.out.println("bothFactorAssignments[" + i + "][" + j +"].size() = " + bothFactorAssignments[i][j].size());
            }
        }
         */
        
        origFactorAPValues = new double[numGenes];
        origFactorBPValues = new double[numGenes];
        origInteractionPValues = new double[numGenes];
        
        adjFactorAPValues = new double[numGenes];
        adjFactorBPValues = new double[numGenes];
        adjInteractionPValues = new double[numGenes];     
        
        factorAFValues = new double[numGenes];
        factorBFValues = new double[numGenes];
        interactionFValues = new double[numGenes];
        factorADfValues = new double[numGenes]; 
        factorBDfValues = new double[numGenes];
        interactionDfValues = new double[numGenes];
        errorDfValues = new double[numGenes];
        
        for (int i = 0; i < numGenes; i++) {
            origFactorAPValues[i] = 0d;
            origFactorBPValues[i] = 0d;
            origInteractionPValues[i] = 0d;
        }
        
        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numGenes);
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);      
        
        int nonMissingDataGenes = 0;
        boolean[] hasMissingValues = new boolean[numGenes];
        for (int i = 0; i < hasMissingValues.length; i++) {
            hasMissingValues[i] = false;
        }
        
        for (int i = 0; i < numGenes; i++) {
            boolean missing = false;
            for (int j = 0; j < numExps; j++) {
                if ( (factorAAssignments[j] != 0) && (factorBAssignments[j] != 0) && (Float.isNaN(expMatrix.A[i][j])) ) {
                    missing = true;
                    break;
                }
            }
            if (missing) {
                hasMissingValues[i] = true;
            } else {
                nonMissingDataGenes++;
            }
        }
        
        for (int i = 0; i < numGenes; i++) {
            if (stop) {
                throw new AbortException();
            }
            
            event.setIntValue(i);
            event.setDescription("Calculating unadjusted p-values: Current gene = " + (i + 1));
            fireValueChanged(event);   
            
            //Vector[][] currentGeneAssignments = getCurrentGeneAssignments(i);
            Vector[][] currentGeneFactorValues = getCurrentGeneFactorValues(i);
            int currGeneFactorCondition = getCurrGeneFactorCondition(currentGeneFactorValues);
            
            if (currGeneFactorCondition == HAS_EMPTY_CELL) {// not analyzed
                origFactorAPValues[i] = Double.NaN;
                origFactorBPValues[i] = Double.NaN;
                origInteractionPValues[i] = Double.NaN;
                
                adjFactorAPValues[i] = Double.NaN;
                adjFactorBPValues[i] = Double.NaN;
                adjInteractionPValues[i] = Double.NaN;                
                
                factorAFValues[i] = Double.NaN;
                factorBFValues[i] = Double.NaN;
                interactionFValues[i] = Double.NaN;
                factorADfValues[i] = Double.NaN;
                factorBDfValues[i] = Double.NaN;
                interactionDfValues[i] = Double.NaN;
                errorDfValues[i] = Double.NaN;  
                
            } else if (currGeneFactorCondition == SOME_CELLS_HAVE_ONE_SAMPLE) {// not analyzed; unclear on how to analyze
                origFactorAPValues[i] = Double.NaN;
                origFactorBPValues[i] = Double.NaN;
                origInteractionPValues[i] = Double.NaN;
                
                adjFactorAPValues[i] = Double.NaN;
                adjFactorBPValues[i] = Double.NaN;
                adjInteractionPValues[i] = Double.NaN;                
                
                factorAFValues[i] = Double.NaN;
                factorBFValues[i] = Double.NaN;
                interactionFValues[i] = Double.NaN;
                factorADfValues[i] = Double.NaN;
                factorBDfValues[i] = Double.NaN;
                interactionDfValues[i] = Double.NaN;
                errorDfValues[i] = Double.NaN; 
                
            } else if (currGeneFactorCondition == BALANCED_WITH_REPLICATION) {
                double[] fValuesAndDfs = getBalancedFValuesAndDfs(currentGeneFactorValues);
                factorAFValues[i] = fValuesAndDfs[0];
                factorBFValues[i] = fValuesAndDfs[1];
                interactionFValues[i] = fValuesAndDfs[2];
                factorADfValues[i] = fValuesAndDfs[3];
                factorBDfValues[i] = fValuesAndDfs[4];
                interactionDfValues[i] = fValuesAndDfs[5];
                errorDfValues[i] = fValuesAndDfs[6];
                if (!usePerms) {
                    origFactorAPValues[i] = getPValueFromFDist(factorAFValues[i], (int)(factorADfValues[i]), (int)(errorDfValues[i]));
                    origFactorBPValues[i] = getPValueFromFDist(factorBFValues[i], (int)(factorBDfValues[i]), (int)(errorDfValues[i]));
                    origInteractionPValues[i] = getPValueFromFDist(interactionFValues[i], (int)(interactionDfValues[i]), (int)(errorDfValues[i]));
                } else { // if (usePerms)
                    if (hasMissingValues[i]) {
                        //origFactorAPValues[i] = 0d;
                        //origFactorBPValues[i] = 0d;
                        //origInteractionPValues[i] = 0d;
                        for (int j = 0; j < numPerms; j++) {
                            Vector[][] currentGenePermutedFactorValues = getCurrentGenePermutedFactorValues(i);
                            double[] permFValuesAndDfs = getBalancedFValuesAndDfs(currentGenePermutedFactorValues);
                            double permFAValue = permFValuesAndDfs[0];
                            double permFBValue = permFValuesAndDfs[1];
                            double permInteractionFValue = permFValuesAndDfs[2];
                            
                            if (permFAValue >= factorAFValues[i]) origFactorAPValues[i] = origFactorAPValues[i] + 1d;
                            if (permFBValue >= factorBFValues[i]) origFactorBPValues[i] = origFactorBPValues[i] + 1d;
                            if (permInteractionFValue >= origInteractionPValues[i]) origInteractionPValues[i] = origInteractionPValues[i] + 1d;
                            // UP TO HERE 03/01/2004
                        }
                        //origFactorAPValues[i] = origFactorAPValues[i]/(double)numPerms;
                        //origFactorBPValues[i] = origFactorBPValues[i]/(double)numPerms;
                        //origInteractionPValues[i] = origInteractionPValues[i]/(double)numPerms;
                    }
                }
            }  else if (currGeneFactorCondition == UNBALANCED_WITH_REPLICATION) {
                double[] fValuesAndDfs = getUnbalancedFValuesAndDfs(currentGeneFactorValues);                
                factorAFValues[i] = fValuesAndDfs[0];
                factorBFValues[i] = fValuesAndDfs[1];
                interactionFValues[i] = fValuesAndDfs[2];
                factorADfValues[i] = fValuesAndDfs[3];
                factorBDfValues[i] = fValuesAndDfs[4];
                interactionDfValues[i] = fValuesAndDfs[5];
                errorDfValues[i] = fValuesAndDfs[6];
                if (!usePerms) {
                    origFactorAPValues[i] = getPValueFromFDist(factorAFValues[i], (int)(factorADfValues[i]), (int)(errorDfValues[i]));
                    origFactorBPValues[i] = getPValueFromFDist(factorBFValues[i], (int)(factorBDfValues[i]), (int)(errorDfValues[i]));
                    origInteractionPValues[i] = getPValueFromFDist(interactionFValues[i], (int)(interactionDfValues[i]), (int)(errorDfValues[i]));
                } else { // if (usePerms)
                    if (hasMissingValues[i]) {
                        //origFactorAPValues[i] = 0d;
                        //origFactorBPValues[i] = 0d;
                        //origInteractionPValues[i] = 0d;
                        for (int j = 0; j < numPerms; j++) {
                            Vector[][] currentGenePermutedFactorValues = getCurrentGenePermutedFactorValues(i);
                            double[] permFValuesAndDfs = getUnbalancedFValuesAndDfs(currentGenePermutedFactorValues);
                            double permFAValue = permFValuesAndDfs[0];
                            double permFBValue = permFValuesAndDfs[1];
                            double permInteractionFValue = permFValuesAndDfs[2];
                            
                            if (permFAValue >= factorAFValues[i]) origFactorAPValues[i] = origFactorAPValues[i] + 1d;
                            if (permFBValue >= factorBFValues[i]) origFactorBPValues[i] = origFactorBPValues[i] + 1d;
                            if (permInteractionFValue >= origInteractionPValues[i]) origInteractionPValues[i] = origInteractionPValues[i] + 1d;
                            // UP TO HERE 03/01/2004
                        }
                        //origFactorAPValues[i] = origFactorAPValues[i]/(double)numPerms;
                        //origFactorBPValues[i] = origFactorBPValues[i]/(double)numPerms;
                        //origInteractionPValues[i] = origInteractionPValues[i]/(double)numPerms;
                    }
                }                
                
            } else if (currGeneFactorCondition == ALL_CELLS_HAVE_ONE_SAMPLE) {
                double[] fValuesAndDfs = getBalancedMainEffectsFValuesAndDfs(currentGeneFactorValues);
                factorAFValues[i] = fValuesAndDfs[0];
                factorBFValues[i] = fValuesAndDfs[1]; 
                interactionFValues[i] = Double.NaN;
                factorADfValues[i] = fValuesAndDfs[2];
                factorBDfValues[i] = fValuesAndDfs[3];
                interactionDfValues[i] = Double.NaN;
                errorDfValues[i] = fValuesAndDfs[4]; 
                origInteractionPValues[i] = Double.NaN;
                if (!usePerms) {
                    origFactorAPValues[i] = getPValueFromFDist(factorAFValues[i], (int)(factorADfValues[i]), (int)(errorDfValues[i]));
                    origFactorBPValues[i] = getPValueFromFDist(factorBFValues[i], (int)(factorBDfValues[i]), (int)(errorDfValues[i]));
                    //origInteractionPValues[i] = Double.NaN;
                }    else { // if (usePerms)
                    if (hasMissingValues[i]) {
                        for (int j = 0; j < numPerms; j++) {
                            Vector[][] currentGenePermutedFactorValues = getCurrentGenePermutedFactorValues(i);
                            double[] permFValuesAndDfs = getBalancedMainEffectsFValuesAndDfs(currentGenePermutedFactorValues);
                            double permFAValue = permFValuesAndDfs[0];
                            double permFBValue = permFValuesAndDfs[1];
                            //double permInteractionFValue = permFValuesAndDfs[2];
                            
                            if (permFAValue >= factorAFValues[i]) origFactorAPValues[i] = origFactorAPValues[i] + 1d;
                            if (permFBValue >= factorBFValues[i]) origFactorBPValues[i] = origFactorBPValues[i] + 1d;
                            //if (permInteractionFValue >= origInteractionPValues[i]) origInteractionPValues[i] = origInteractionPValues[i] + 1d;
                            
                        }                        
                    }
                }
            }
            
        }
        
        if ((usePerms) && (nonMissingDataGenes > 0)) {// speeds up the calculation for genes with complete data, 
                                                      // as the entire matrix can be permuted at once instead of permuting each gene individually
            int firstNonMissingGene = -1;
            
            for (int i = 0; i < hasMissingValues.length; i++) {
                if (!hasMissingValues[i]) {
                    firstNonMissingGene = i;
                    break;
                }
            }
            
            Vector[][] geneFactorValues = getCurrentGeneFactorValues(firstNonMissingGene);
            int geneFactorCondition = getCurrGeneFactorCondition(geneFactorValues);            
            
            Vector validIndices = new Vector();
            for (int i = 0; i < numExps; i++) {
                if ((factorAAssignments[i] !=0)&&(factorBAssignments[i] != 0) ) {
                    validIndices.add(new Integer(i));
                }
            }
            int[] validArray = new int[validIndices.size()];
            for (int i = 0; i < validArray.length; i++) {
                validArray[i] = ((Integer)(validIndices.get(i))).intValue();
            }
            
            event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numPerms);
            fireValueChanged(event);
            event.setId(AlgorithmEvent.PROGRESS_VALUE);            
            
            for (int j = 0; j < numPerms; j++) {
                if (stop) {
                    throw new AbortException();
                }
                
                event.setIntValue(j);
                event.setDescription("Calculating unadjusted p-values for some genes: Current permutation = " + (j + 1));
                fireValueChanged(event);
                
                int[] permutedExpts = getPermutedValues(numExps, validArray); //returns an int array of size "numExps", with the valid values permuted
                FloatMatrix permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
                
                if (geneFactorCondition == BALANCED_WITH_REPLICATION) {
                    for (int i = 0; i < numGenes; i++) {
                        if (!hasMissingValues[i]) {
                            Vector[][] currentGenePermFactorValues = getCurrentGeneFactorValuesFromPermMatrix(permutedMatrix, i);
                            //int currGenePermFactorCondition = getCurrGeneFactorCondition(currentGenePermFactorValues);
                            double[] permFValuesAndDfs = getBalancedFValuesAndDfs(currentGenePermFactorValues);
                            double currGenePermFAValue = permFValuesAndDfs[0];
                            double currGenePermFBValue = permFValuesAndDfs[1];                            
                            double currGenePermInteractionFValue = permFValuesAndDfs[2];
                            
                            if (currGenePermFAValue >= factorAFValues[i]) origFactorAPValues[i] = origFactorAPValues[i] + 1d;
                            if (currGenePermFBValue >= factorBFValues[i]) origFactorBPValues[i] = origFactorBPValues[i] + 1d;
                            if (currGenePermInteractionFValue >= origInteractionPValues[i]) origInteractionPValues[i] = origInteractionPValues[i] + 1d;                                               
                        }
                    }
                } else if (geneFactorCondition == UNBALANCED_WITH_REPLICATION) {
                    for (int i = 0; i < numGenes; i++) {
                        if (!hasMissingValues[i]) {
                            Vector[][] currentGenePermFactorValues = getCurrentGeneFactorValuesFromPermMatrix(permutedMatrix, i);
                            //int currGenePermFactorCondition = getCurrGeneFactorCondition(currentGenePermFactorValues);
                            double[] permFValuesAndDfs = getUnbalancedFValuesAndDfs(currentGenePermFactorValues);
                            double currGenePermFAValue = permFValuesAndDfs[0];
                            double currGenePermFBValue = permFValuesAndDfs[1];                            
                            double currGenePermInteractionFValue = permFValuesAndDfs[2];
                            
                            if (currGenePermFAValue >= factorAFValues[i]) origFactorAPValues[i] = origFactorAPValues[i] + 1d;
                            if (currGenePermFBValue >= factorBFValues[i]) origFactorBPValues[i] = origFactorBPValues[i] + 1d;
                            if (currGenePermInteractionFValue >= origInteractionPValues[i]) origInteractionPValues[i] = origInteractionPValues[i] + 1d;                                               
                        }
                    }                    
                } else if (geneFactorCondition == ALL_CELLS_HAVE_ONE_SAMPLE) {
                    for (int i = 0; i < numGenes; i++) {
                        if (!hasMissingValues[i]) {
                            Vector[][] currentGenePermFactorValues = getCurrentGeneFactorValuesFromPermMatrix(permutedMatrix, i);
                            //int currGenePermFactorCondition = getCurrGeneFactorCondition(currentGenePermFactorValues);
                            double[] permFValuesAndDfs = getBalancedMainEffectsFValuesAndDfs(currentGenePermFactorValues);
                            double currGenePermFAValue = permFValuesAndDfs[0];
                            double currGenePermFBValue = permFValuesAndDfs[1];                            
                            //double currGenePermInteractionFValue = permFValuesAndDfs[2];
                            
                            if (currGenePermFAValue >= factorAFValues[i]) origFactorAPValues[i] = origFactorAPValues[i] + 1d;
                            if (currGenePermFBValue >= factorBFValues[i]) origFactorBPValues[i] = origFactorBPValues[i] + 1d;
                            //origInteractionPValues[i] = Double.NaN;
                            //if (currGenePermInteractionFValue >= origInteractionPValues[i]) origInteractionPValues[i] = origInteractionPValues[i] + 1d;                                               
                        }                        
                    }
                }
            }           

        }
        
        if (usePerms) {
            for (int i = 0; i < numGenes; i++) {
                origFactorAPValues[i] = origFactorAPValues[i]/(double)numPerms;
                origFactorBPValues[i] = origFactorBPValues[i]/(double)numPerms;                
                origInteractionPValues[i] = origInteractionPValues[i]/(double)numPerms;                
            }
        }
        
        if (adjustmentMethod == JUST_ALPHA) {
            for (int i = 0; i < numGenes; i++) {
                adjFactorAPValues[i] = origFactorAPValues[i];
                adjFactorBPValues[i] = origFactorBPValues[i];
                adjInteractionPValues[i] = origInteractionPValues[i];
            }
        } else {// for other adjustment methods
        }
        
        Vector clusterVector = new Vector();
        
        Vector sigAGenes = new Vector();
        Vector nonSigAGenes = new Vector();
        Vector sigBGenes = new Vector();
        Vector nonSigBGenes = new Vector();
        Vector sigInteractionGenes = new Vector();
        Vector nonSigInteractionGenes = new Vector();
        Vector nonSigAllGenes = new Vector();
        
        for (int i = 0; i < numGenes; i++) {
            if ((float)(adjFactorAPValues[i]) <= alpha) {
                sigAGenes.add(new Integer(i));
            } else if ((float)(adjFactorBPValues[i]) <= alpha) {
                sigBGenes.add(new Integer(i));
            } else if ((float)(adjInteractionPValues[i]) <= alpha) {
                sigInteractionGenes.add(new Integer(i));
            }
        };
        
        Vector allGenes = new Vector();        
        
        for (int i = 0; i < numGenes; i++) {
            allGenes.add(new Integer(i));
        }
        
        nonSigAGenes = (Vector)(allGenes.clone());
        nonSigAGenes.removeAll(sigAGenes);
        
        nonSigBGenes = (Vector)(allGenes.clone());
        nonSigBGenes.removeAll(sigBGenes);  
        
        nonSigInteractionGenes = (Vector)(allGenes.clone());
        nonSigInteractionGenes.removeAll(sigInteractionGenes);
        
        nonSigAllGenes = (Vector)(allGenes.clone());
        nonSigAllGenes.removeAll(sigAGenes);
        nonSigAllGenes.removeAll(sigBGenes);
        nonSigAllGenes.removeAll(sigInteractionGenes);
        
        clusterVector.add(sigAGenes);
        clusterVector.add(sigBGenes);
        clusterVector.add(sigInteractionGenes);
        clusterVector.add(nonSigAGenes);        
        clusterVector.add(nonSigBGenes);  
        clusterVector.add(nonSigInteractionGenes);
        clusterVector.add(nonSigAllGenes);       
        
        k = clusterVector.size();
        
        FloatMatrix factorAFValuesMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix factorBFValuesMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix interactionFValuesMatrix = new FloatMatrix(numGenes, 1);
        
        FloatMatrix factorADfValuesMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix factorBDfValuesMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix interactionDfValuesMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix errorDfValuesMatrix = new FloatMatrix(numGenes, 1);
        
        FloatMatrix origFactorAPValuesMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix origFactorBPValuesMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix origInteractionPValuesMatrix = new FloatMatrix(numGenes, 1);
        
        FloatMatrix adjFactorAPValuesMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix adjFactorBPValuesMatrix = new FloatMatrix(numGenes, 1);
        FloatMatrix adjInteractionPValuesMatrix = new FloatMatrix(numGenes, 1);        
        
        for (int i = 0; i < numGenes; i++) {
            factorAFValuesMatrix.A[i][0] = (float)(factorAFValues[i]);
            factorBFValuesMatrix.A[i][0] = (float)(factorBFValues[i]);
            interactionFValuesMatrix.A[i][0] = (float)(interactionFValues[i]);
            
            factorADfValuesMatrix.A[i][0] = (float)(factorADfValues[i]);
            factorBDfValuesMatrix.A[i][0] = (float)(factorBDfValues[i]);
            interactionDfValuesMatrix.A[i][0] = (float)(interactionDfValues[i]);
            errorDfValuesMatrix.A[i][0] = (float)(errorDfValues[i]);
            
            origFactorAPValuesMatrix.A[i][0] = (float)(origFactorAPValues[i]);
            origFactorBPValuesMatrix.A[i][0] = (float)(origFactorBPValues[i]);
            origInteractionPValuesMatrix.A[i][0] = (float)(origInteractionPValues[i]);            
            
            adjFactorAPValuesMatrix.A[i][0] = (float)(adjFactorAPValues[i]);
            adjFactorBPValuesMatrix.A[i][0] = (float)(adjFactorBPValues[i]);
            adjInteractionPValuesMatrix.A[i][0] = (float)(adjInteractionPValues[i]);
        }
        
	clusters = new Vector[k];
	
	for (int i = 0; i < k; i++) {
	    clusters[i] = (Vector)(clusterVector.get(i));
	}
	
	FloatMatrix means = getMeans(clusters);
	FloatMatrix variances = getVariances(clusters, means);         
        
	event = null;
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
        result.addMatrix("factorAFValuesMatrix", factorAFValuesMatrix);
        result.addMatrix("factorBFValuesMatrix", factorBFValuesMatrix);
        result.addMatrix("interactionFValuesMatrix", interactionFValuesMatrix);
        result.addMatrix("factorADfValuesMatrix", factorADfValuesMatrix);
        result.addMatrix("factorBDfValuesMatrix", factorBDfValuesMatrix);
        result.addMatrix("interactionDfValuesMatrix", interactionDfValuesMatrix);
        result.addMatrix("errorDfValuesMatrix", errorDfValuesMatrix);
        result.addMatrix("origFactorAPValuesMatrix", origFactorAPValuesMatrix);
        result.addMatrix("origFactorBPValuesMatrix", origFactorBPValuesMatrix);
        result.addMatrix("origInteractionPValuesMatrix", origInteractionPValuesMatrix);
        result.addMatrix("adjFactorAPValuesMatrix", adjFactorAPValuesMatrix);
        result.addMatrix("adjFactorBPValuesMatrix", adjFactorBPValuesMatrix);
        result.addMatrix("adjInteractionPValuesMatrix", adjInteractionPValuesMatrix);        
        
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
    
    /*
    private double getFactorAPValueBalanced(int gene) {
        //double FA = getBalancedFA(gene);
        
    }*/
    
    private double getPValueFromFDist(double fValue, int groupsDF, int errorDF) {
        FDistribution fDist = new FDistribution(groupsDF, errorDF);
        double cumulProb = fDist.cumulative(fValue);
        double pValue = 2*(1 - cumulProb); // (1 - cumulProb) is the one-tailed test p-value
        
        if (pValue > 1) {
            pValue = 1.0d;
        }     
        
        return pValue;
    }
    
    private double[] getUnbalancedFValuesAndDfs(Vector[][] currGeneFactorValues) {
        double[][] unadjCellSums = getCellSums(currGeneFactorValues);
        double[][] cellMeans = new double[currGeneFactorValues.length][currGeneFactorValues[0].length];
        
        for (int i = 0; i < currGeneFactorValues.length; i++) {
            for (int j = 0; j < currGeneFactorValues[i].length; j++) {
                cellMeans[i][j] = unadjCellSums[i][j]/(double)(currGeneFactorValues[i][j].size());
            }
        } 
        
        double sumRecipCellSizes = 0;
        int dfError = 0;
        
        for (int i = 0; i < currGeneFactorValues.length; i++) {
            for (int j = 0; j < currGeneFactorValues[i].length; j++) {
                sumRecipCellSizes = sumRecipCellSizes + 1d/(double)(currGeneFactorValues[i][j].size());
                dfError = dfError + (currGeneFactorValues[i][j].size() - 1);
            }
        }
        
        double harmonicCellSize = (double)(numFactorLevels[0]*numFactorLevels[1])/sumRecipCellSizes;
        
        double[][] adjustedCellSums = new double[currGeneFactorValues.length][currGeneFactorValues[0].length];
        for (int i = 0; i < currGeneFactorValues.length; i++) {
            for (int j = 0; j < currGeneFactorValues[i].length; j++) {
                adjustedCellSums[i][j] = (cellMeans[i][j])*harmonicCellSize;
            }
        }
        
        int dfA = numFactorLevels[0] - 1;
        int dfB = numFactorLevels[1] - 1;
        int dfInteraction = dfA*dfB;   
        
        
        double T = getTUnbalanced(adjustedCellSums, harmonicCellSize);
        double A = getAUnbalanced(adjustedCellSums, harmonicCellSize);
        double B = getBUnbalanced(adjustedCellSums, harmonicCellSize);
        double AB = getABUnbalanced(adjustedCellSums, harmonicCellSize);
        
        double msA = (A - T)/(double)dfA;
        double msB = (B - T)/(double)dfB;
        double msInteraction = (AB - A - B + T)/(double)dfInteraction;     
        
        double ssError = 0d;
        
        for (int i = 0; i < currGeneFactorValues.length; i++) {
            for (int j = 0; j < currGeneFactorValues[i].length; j++) {
                Vector vect = currGeneFactorValues[i][j];
                ssError = ssError + getSumSquaredDiffs(vect);
            }
        }
        
        double msError = ssError/(double)dfError;  
        
        double fA = msA / msError;
        double fB = msB / msError;
        double fInteraction = msInteraction/msError;        
        
        double[] fValuesAndDfs = new double[7];
        fValuesAndDfs[0] = fA;
        fValuesAndDfs[1] = fB;
        fValuesAndDfs[2] = fInteraction;
        fValuesAndDfs[3] = (double)dfA;
        fValuesAndDfs[4] = (double)dfB;
        fValuesAndDfs[5] = (double)dfInteraction;
        fValuesAndDfs[6] = (double)dfError;        
        return fValuesAndDfs;
    }
    
    private double[] getBalancedFValuesAndDfs(Vector[][] currGeneFactorValues) {
        double[][] cellSums = getCellSums(currGeneFactorValues);
        int sampleSizePerCell = currGeneFactorValues[0][0].size();
        
        int dfA = numFactorLevels[0] - 1;
        int dfB = numFactorLevels[1] - 1;
        int dfInteraction = dfA*dfB;
        int dfError = numFactorLevels[0]*numFactorLevels[1]*(sampleSizePerCell - 1);
        
        double Y = getYBalanced(currGeneFactorValues);
        double AB = getABBalanced(currGeneFactorValues, cellSums);
        double T = getTBalanced(currGeneFactorValues, cellSums);
        double A = getABalanced(currGeneFactorValues, cellSums);
        double B = getBBalanced(currGeneFactorValues, cellSums);
        
        double msA = (A - T)/(double)dfA;
        double msB = (B - T)/(double)dfB;
        double msInteraction = (AB - A - B + T)/(double)dfInteraction;
        double msError = (Y - AB)/(double)dfError;
        
        double fA = msA / msError;
        double fB = msB / msError;
        double fInteraction = msInteraction/msError;
        
        double[] fValuesAndDfs = new double[7];
        fValuesAndDfs[0] = fA;
        fValuesAndDfs[1] = fB;
        fValuesAndDfs[2] = fInteraction;
        fValuesAndDfs[3] = (double)dfA;
        fValuesAndDfs[4] = (double)dfB;
        fValuesAndDfs[5] = (double)dfInteraction;
        fValuesAndDfs[6] = (double)dfError;
        
        return fValuesAndDfs;
    }
    

    
    private double[] getBalancedMainEffectsFValuesAndDfs(Vector[][] currGeneFactorValues) {
        // implemented as in Biostatistical Analysis by Zar 4th ed. pg 249. 
        //Without replication in cells, only main effects are tested, and Remainder (Error) terms are the same as
        //the interaction terms in the case with equal replication.
        double[][] cellSums = getCellSums(currGeneFactorValues);
        //int sampleSizePerCell = currGeneFactorValues[0][0].size();
        
        int dfA = numFactorLevels[0] - 1;
        int dfB = numFactorLevels[1] - 1;  
        int dfRemainder = dfA*dfB;//numFactorLevels[0]*numFactorLevels[1]*(sampleSizePerCell - 1); 

        double Y = getYBalanced(currGeneFactorValues); 
        double AB = getABBalanced(currGeneFactorValues, cellSums);        
        double T = getTBalanced(currGeneFactorValues, cellSums);
        double A = getABalanced(currGeneFactorValues, cellSums);
        double B = getBBalanced(currGeneFactorValues, cellSums);  
        
        double msA = (A - T)/(double)dfA;
        double msB = (B - T)/(double)dfB;  
        double msRemainder = (AB - A - B + T)/(double)dfRemainder;//(Y - AB)/(double)dfError; 
        
        double fA = msA / msRemainder;
        double fB = msB / msRemainder;  
        
        double[] fValuesAndDfs = new double[5]; 
        fValuesAndDfs[0] = fA;
        fValuesAndDfs[1] = fB;
        fValuesAndDfs[2] = (double)dfA;
        fValuesAndDfs[3] = (double)dfB;
        fValuesAndDfs[4] = (double)dfRemainder;
        
        return fValuesAndDfs;        
    }
     
    
    /*
    private double getBalancedFA(Vector[][] currGeneFactorVals) {
        //Vector[][] currGeneFactorVals = getCurrentGeneFactorValues(gene);
        double A = getABalanced(currGeneFactorVals);
        double T = getTBalanced(currGeneFactorVals);
        
        double ssA = A - T;
        int dfA = numFactorLevels[0] - 1;
        
        double msA = ssA/(double)dfA;        
        double msError = getMSErrorBalanced(currGeneFactorVals);
        
        double fValue = msA/msError;
        
        return fValue;
    }
    
    private double getBalancedFB(Vector[][] currGeneFactorVals) {
        double B = getBBalanced(currGeneFactorVals);
        double T = getTBalanced(currGeneFactorVals);
        
        double ssB = B - T;
        int dfB = numFactorLevels[1] - 1;
        
        double msB = ssB/(double)dfB;        
        double msError = getMSErrorBalanced(currGeneFactorVals);
        
        double fValue = msB/msError;
        
        return fValue;        
    }
    
    private double getMSErrorBalanced(Vector[][] currGeneFactorVals) {
        double Y = 0;
        for (int i = 0; i < currGeneFactorVals.length; i++) {
            for (int j = 0; j < currGeneFactorVals[i].length; j++) {
                for (int k = 0; k < currGeneFactorVals[i][j].size(); i++) {
                    double currVal = ((Float)(currGeneFactorVals[i][j].get(k))).doubleValue();
                    Y = Y + currVal*currVal;
                }
            }
        }
        
        double sumSquaresCellSums = 0;
        double[][] cellSums = getCellSums(currGeneFactorVals);    
        
        for (int i = 0; i < cellSums.length; i++) {
            for (int j = 0; j < cellSums[i].length; j++) {
                sumSquaresCellSums = sumSquaresCellSums + cellSums[i][j]*cellSums[i][j];
            }
        }
        
        int sampleSizePerCell = currGeneFactorVals[0][0].size();
        
        double AB = sumSquaresCellSums/(double)(sampleSizePerCell);
        
        double ssError = Y - AB;
        int dfError = numFactorLevels[0]*numFactorLevels[1]*(sampleSizePerCell - 1);
        
        double msError = ssError/(double)(dfError);
        
        return msError;
    }
    */
    
    private double getYBalanced(Vector[][] currGeneFactorVals) {
        double Y = 0;
        for (int i = 0; i < currGeneFactorVals.length; i++) {
            for (int j = 0; j < currGeneFactorVals[i].length; j++) {
                for (int k = 0; k < currGeneFactorVals[i][j].size(); k++) {
                    double currVal = ((Float)(currGeneFactorVals[i][j].get(k))).doubleValue();
                    Y = Y + currVal*currVal;
                }
            }
        }
        
        return Y;
    }
    
    private double getABUnbalanced(double[][] adjCellSums, double adjSampleSize) {
        double sumSquaresCellSums = 0;
        
        for (int i = 0; i < adjCellSums.length; i++) {
            for (int j = 0; j < adjCellSums[i].length; j++) {
                sumSquaresCellSums = sumSquaresCellSums + adjCellSums[i][j]*adjCellSums[i][j];
            }
        }  
        
        double ABUnbalanced = sumSquaresCellSums/adjSampleSize;
        return ABUnbalanced;
    }
    
    private double getABBalanced(Vector[][] currGeneFactorVals, double[][] cellSums) {
        double sumSquaresCellSums = 0;
        //double[][] cellSums = getCellSums(currGeneFactorVals);    
        
        for (int i = 0; i < cellSums.length; i++) {
            for (int j = 0; j < cellSums[i].length; j++) {
                sumSquaresCellSums = sumSquaresCellSums + cellSums[i][j]*cellSums[i][j];
            }
        }
        
        int sampleSizePerCell = currGeneFactorVals[0][0].size();
        
        double AB = sumSquaresCellSums/(double)(sampleSizePerCell);
        
        return AB;
    }
    
    private double getAUnbalanced(double[][] adjCellSums, double adjSampleSize) {
        double sumSquaresA = 0d;
        
        for (int i = 0; i < adjCellSums.length; i++) {
            double currLevelASum = 0d;
            for (int j = 0; j < adjCellSums[i].length; j++) {
                currLevelASum = currLevelASum + adjCellSums[i][j];
            }
            
            sumSquaresA = sumSquaresA + currLevelASum*currLevelASum;
        }      
        
        double AUnbalanced = sumSquaresA/(double)(numFactorLevels[1]*adjSampleSize);
        return AUnbalanced;
    }
    
    private double getABalanced(Vector[][] currGeneFactorVals, double[][] cellSums) {
        //double[] sumsA = new double[numFactorLevels[0]];
        //double[][] cellSums = getCellSums(currGeneFactorVals);
        double sumSquaresA = 0d;
        
        for (int i = 0; i < cellSums.length; i++) {
            double currLevelASum = 0d;
            for (int j = 0; j < cellSums[i].length; j++) {
                currLevelASum = currLevelASum + cellSums[i][j];
            }
            
            sumSquaresA = sumSquaresA + currLevelASum*currLevelASum;
        }
        
        int sampleSizePerCell = currGeneFactorVals[0][0].size();
        
        double ABalanced = sumSquaresA/(double)(numFactorLevels[1]*sampleSizePerCell);
        
        return ABalanced;
    }
    
    private double getBUnbalanced(double[][] adjCellSums, double adjSampleSize) {
        double sumSquaresB = 0d;    
        
        for (int i = 0; i < adjCellSums[0].length; i++) {
            double currLevelBSum = 0d;
            for (int j = 0; j < adjCellSums.length; j++) {
                currLevelBSum = currLevelBSum + adjCellSums[j][i];
            }
            
            sumSquaresB = sumSquaresB + currLevelBSum*currLevelBSum;
        } 
        
        double BUnbalanced = sumSquaresB/(double)(numFactorLevels[0]*adjSampleSize); 
        return BUnbalanced;
    }
    
    private double getBBalanced(Vector[][] currGeneFactorVals, double[][] cellSums) {
        //double[][] cellSums = getCellSums(currGeneFactorVals);
        double sumSquaresB = 0d;    
        
        for (int i = 0; i < cellSums[0].length; i++) {
            double currLevelBSum = 0d;
            for (int j = 0; j < cellSums.length; j++) {
                currLevelBSum = currLevelBSum + cellSums[j][i];
            }
            
            sumSquaresB = sumSquaresB + currLevelBSum*currLevelBSum;
        }
        
        int sampleSizePerCell = currGeneFactorVals[0][0].size();
        
        double BBalanced = sumSquaresB/(double)(numFactorLevels[0]*sampleSizePerCell);    
        
        return BBalanced;
    }
    
    private double getTBalanced(Vector[][] currGeneFactorVals, double[][] cellSums) {
        //double[][] cellSums = getCellSums(currGeneFactorVals);
        double totalSum = 0;
        for (int i = 0; i < cellSums.length; i++) {
            for (int j = 0; j < cellSums[i].length; j++) {
                totalSum = totalSum + cellSums[i][j];
            }
        }
        
        int sampleSizePerCell = currGeneFactorVals[0][0].size();
        
        double TBalanced = (totalSum*totalSum)/(double)(numFactorLevels[0]*numFactorLevels[1]*sampleSizePerCell);
        return TBalanced;
    }
    
    private double getTUnbalanced(double[][] adjCellSums, double adjSampleSize) {
        double adjTotalSum = 0d;
        
        for (int i = 0; i < adjCellSums.length; i++) {
            for (int j = 0; j < adjCellSums[i].length; j++) {
                adjTotalSum = adjTotalSum + adjCellSums[i][j];
            }
        }  
        
        double TUnbalanced = (adjTotalSum*adjTotalSum)/(double)(numFactorLevels[0]*numFactorLevels[1]*adjSampleSize);
        return TUnbalanced;
    }
    
    private double[][] getCellSums(Vector[][] currGeneFactorVals) {
        double[][] cellSums = new double[currGeneFactorVals.length][currGeneFactorVals[0].length];
        
        //int cellSumsCounter = 0;
        for (int i = 0; i < currGeneFactorVals.length; i++) {
            for (int j = 0; j < currGeneFactorVals[i].length; j++) {
                cellSums[i][j] = getSum(currGeneFactorVals[i][j]);
                //cellSumsCounter++;
            }
        }
        
        return cellSums;
    }
    
    private double[][] getCellMeans(Vector[][] currGeneFactorVals) {
        double[][] cellMeans = new double[currGeneFactorVals.length][currGeneFactorVals[0].length];
        
        //int cellSumsCounter = 0;
        for (int i = 0; i < currGeneFactorVals.length; i++) {
            for (int j = 0; j < currGeneFactorVals[i].length; j++) {
                cellMeans[i][j] = getSum(currGeneFactorVals[i][j]);
                //cellSumsCounter++;
            }
        }
        
        for (int i = 0; i < currGeneFactorVals.length; i++) {
            for (int j = 0; j < currGeneFactorVals[i].length; j++) {
                cellMeans[i][j] = cellMeans[i][j]/(double)(currGeneFactorVals[i][j].size());
            }
        }
        
        return cellMeans;
    }    
    
    private double getSum(Vector vect) {
        double sum = 0d;        
        for (int i = 0; i < vect.size(); i++) {
            sum = sum + ((Float)(vect.get(i))).doubleValue();
        }
        
        return sum;
    }
    
    private double getSumSquares(Vector vect) {
        double sumSquares = 0d;
        for (int i = 0; i < vect.size(); i++) {
            sumSquares = sumSquares + (((Float)(vect.get(i))).doubleValue())*(((Float)(vect.get(i))).doubleValue());            
        }  
        
        return sumSquares;
    }
    
    private double getSumSquaredDiffs(Vector vect) {
        return ( getSumSquares(vect) - ((getSum(vect)*getSum(vect))/(double)(vect.size())) );
    }
    
    /*
    private Vector[][] getCurrentGeneAssignments(int gene) {
        Vector[][] currGeneAssignments = new Vector[numFactorLevels[0]][numFactorLevels[1]];
        
        for (int i = 0; i < currGeneAssignments.length; i++) {
            for (int j = 0; j < currGeneAssignments[i].length; j++) {
                currGeneAssignments[i][j] = new Vector();
            }
        }        
        
        for (int i = 0; i < factorAAssignments.length; i++) {
            if ((factorAAssignments[i] != 0)&&(factorBAssignments[i] != 0) && !Float.isNaN(expMatrix.A[gene][i])) {
                currGeneAssignments[factorAAssignments[i] - 1][factorBAssignments[i] - 1].add(new Integer(i));
            }
        }
        
        return currGeneAssignments;        
    }
     */
    
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
    
    private FloatMatrix getPermutedMatrix(FloatMatrix inputMatrix, int[] permExpts) {
        FloatMatrix permutedMatrix = new FloatMatrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());
        for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
            for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                permutedMatrix.A[i][j] = inputMatrix.A[i][permExpts[j]];
            }
        }
        return permutedMatrix;
    }    
    
    private Vector[][] getCurrentGenePermutedFactorValues(int gene) {
        Vector[][] permutedCurrGeneFactorValues = new Vector[numFactorLevels[0]][numFactorLevels[1]];
        Vector validIndices = new Vector();
        for (int i = 0; i < numExps; i++) {
            if ((factorAAssignments[i] !=0)&&(factorBAssignments[i] != 0) && !Float.isNaN(expMatrix.A[gene][i])) {
                validIndices.add(new Integer(i));
            }
        }
        int[] validArray = new int[validIndices.size()];
        for (int i = 0; i < validArray.length; i++) {
            validArray[i] = ((Integer)(validIndices.get(i))).intValue();
        }
        
        int[] permutedFactorAIndices = getPermutedValues(numExps, validArray);
        int[] permutedFactorBIndices = getPermutedValues(numExps, validArray);
        
        int[] permutedFactorAAssignments = new int[permutedFactorAIndices.length];
        int[] permutedFactorBAssignments = new int[permutedFactorBIndices.length];
        
        for (int i = 0; i < permutedFactorAIndices.length; i++) {
            permutedFactorAAssignments[i] = factorAAssignments[permutedFactorAIndices[i]];
            permutedFactorBAssignments[i] = factorBAssignments[permutedFactorBIndices[i]];
        }
        
        for (int i = 0; i < permutedCurrGeneFactorValues.length; i++) {
            for (int j = 0; j < permutedCurrGeneFactorValues[i].length; j++) {
                permutedCurrGeneFactorValues[i][j] = new Vector();
            }
        }   
        
        for (int i = 0; i < permutedFactorAAssignments.length; i++) {
            if ((permutedFactorAAssignments[i] != 0)&&(permutedFactorBAssignments[i] != 0) && !Float.isNaN(expMatrix.A[gene][i])) {
                permutedCurrGeneFactorValues[permutedFactorAAssignments[i] - 1][permutedFactorBAssignments[i] - 1].add(new Float(expMatrix.A[gene][i]));
            }
        } 
        
        return permutedCurrGeneFactorValues;
    }
    
    private Vector[][] getCurrentGeneFactorValues(int gene) {
        Vector[][] currGeneFactorValues = new Vector[numFactorLevels[0]][numFactorLevels[1]]; 
        
        for (int i = 0; i < currGeneFactorValues.length; i++) {
            for (int j = 0; j < currGeneFactorValues[i].length; j++) {
                currGeneFactorValues[i][j] = new Vector();
            }
        }  
        
        for (int i = 0; i < factorAAssignments.length; i++) {
            if ((factorAAssignments[i] != 0)&&(factorBAssignments[i] != 0) && !Float.isNaN(expMatrix.A[gene][i])) {
                currGeneFactorValues[factorAAssignments[i] - 1][factorBAssignments[i] - 1].add(new Float(expMatrix.A[gene][i]));
            }
        }  
        
        return currGeneFactorValues;
    }
    
    private Vector[][] getCurrentGeneFactorValuesFromPermMatrix(FloatMatrix permMatrix, int gene) {
        Vector[][] currGeneFactorValues = new Vector[numFactorLevels[0]][numFactorLevels[1]]; 
        
        for (int i = 0; i < currGeneFactorValues.length; i++) {
            for (int j = 0; j < currGeneFactorValues[i].length; j++) {
                currGeneFactorValues[i][j] = new Vector();
            }
        }  
        
        for (int i = 0; i < factorAAssignments.length; i++) {
            if ((factorAAssignments[i] != 0)&&(factorBAssignments[i] != 0) && !Float.isNaN(permMatrix.A[gene][i])) {
                currGeneFactorValues[factorAAssignments[i] - 1][factorBAssignments[i] - 1].add(new Float(permMatrix.A[gene][i]));
            }
        }  
        
        return currGeneFactorValues;        
    }
    
    private int getCurrGeneFactorCondition(Vector[][] currGeneFactorAssignments) {
        int[] cellSizes =new int[currGeneFactorAssignments.length*currGeneFactorAssignments[0].length];
        int cellCounter = 0;
        for (int i = 0; i < currGeneFactorAssignments.length; i++) {
            for (int j = 0; j < currGeneFactorAssignments[i].length; j++) {
                cellSizes[cellCounter] = currGeneFactorAssignments[i][j].size();
                cellCounter++;
            }
        }
        
        for (int i = 0; i < cellSizes.length; i++) {
            if (cellSizes[i] == 0) {
                return HAS_EMPTY_CELL;
            }
        }
        
        if (cellSizes[0] == 1) {
            boolean allOne = true;
            for (int i = 1; i < cellSizes.length; i++) {
                if (cellSizes[i] != 1) {
                    allOne = false;
                    break;
                }
            }
            
            if (allOne) {
                return ALL_CELLS_HAVE_ONE_SAMPLE;
            } 
        }
        
        for (int i = 0; i < cellSizes.length; i++) {
            if (cellSizes[i] == 1) return SOME_CELLS_HAVE_ONE_SAMPLE;
        }
        
        int firstCellCount = cellSizes[0];
        boolean balanced = true;
        for (int i = 1; i < cellSizes.length; i++) {
            if (cellSizes[i] != firstCellCount) {
                balanced = false;
                break;
            }
        }
        
        if (balanced) {
            return BALANCED_WITH_REPLICATION;
        } else {
            return UNBALANCED_WITH_REPLICATION;
        }        
        //return -1;
    }
    
}
