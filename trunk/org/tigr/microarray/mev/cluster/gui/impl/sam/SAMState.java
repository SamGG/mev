/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * SAMState.java
 *
 * Created on February 3, 2003, 10:31 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.sam;
import org.tigr.util.FloatMatrix;
import java.util.Vector;
/**
 *
 * @author  nbhagaba
 * @version 
 */
public class SAMState {
    
    public static boolean firstRun = true, useAllUniquePerms; 
    public static int[] groupAssignments, numSigGenesByDelta, sortedDArrayIndices;
    public static int studyDesign, numCombs, numNeighbors, numMultiClassGroups, numUniquePerms;
    public static boolean useKNearest, /*isHierarchicalTree,*/ calculateQLowestFDR, useTusherEtAlS0;
    public static double delta, sNought, pi0Hat, s0Percentile, oneClassMean; 
    public static double[] dBarValues, sortedDArray, deltaGrid, medNumFalselyCalledGenesByDelta, dArray, rArray, survivalTimes; 
    public static double[] ninetiethPercentileFalselyCalledGenesByDelta, FDRmedian, FDR90thPercentile, qLowestFDR;
    public static boolean[] inSurvivalAnalysis, censored;
    public static FloatMatrix imputedMatrix;
    public static Vector pairedGroupAExpts, pairedGroupBExpts;
    
    /** Creates new SAMState */
    public SAMState() {
    }

}
