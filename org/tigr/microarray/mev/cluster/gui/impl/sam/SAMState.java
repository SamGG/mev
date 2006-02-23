/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * SAMState.java
 *
 * Created on February 3, 2003, 10:31 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.sam;

import java.util.Vector;

import org.tigr.util.FloatMatrix;
/**
 *
 * @author  nbhagaba
 * @version 
 */
public class SAMState implements java.io.Serializable {
    public static final long serialVersionUID = 202015040001L;
    
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
    
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException {
        oos.writeBoolean(SAMState.firstRun);
        oos.writeBoolean(SAMState.useAllUniquePerms);
        oos.writeObject(SAMState.groupAssignments);
        oos.writeObject(SAMState.numSigGenesByDelta);
        oos.writeObject(SAMState.sortedDArrayIndices);
        oos.writeInt(SAMState.studyDesign);
        oos.writeInt(SAMState.numCombs);
        oos.writeInt(SAMState.numNeighbors);
        oos.writeInt(SAMState.numMultiClassGroups);
        oos.writeInt(SAMState.numUniquePerms);
        oos.writeBoolean(SAMState.useKNearest);
        oos.writeBoolean(SAMState.calculateQLowestFDR);
        oos.writeBoolean(SAMState.useTusherEtAlS0);
        oos.writeDouble(SAMState.delta);
        oos.writeDouble(SAMState.sNought);
        oos.writeDouble(SAMState.pi0Hat);  
        oos.writeDouble(SAMState.s0Percentile);
        oos.writeDouble(SAMState.oneClassMean);
        oos.writeObject(SAMState.dBarValues);
        oos.writeObject(SAMState.sortedDArray);
        oos.writeObject(SAMState.deltaGrid);
        oos.writeObject(SAMState.medNumFalselyCalledGenesByDelta);
        oos.writeObject(SAMState.dArray);
        oos.writeObject(SAMState.rArray);
        oos.writeObject(SAMState.survivalTimes);
        oos.writeObject(SAMState.ninetiethPercentileFalselyCalledGenesByDelta);
        oos.writeObject(SAMState.FDRmedian);
        oos.writeObject(SAMState.FDR90thPercentile);
        oos.writeObject(SAMState.qLowestFDR);
        oos.writeObject(SAMState.inSurvivalAnalysis);
        oos.writeObject(SAMState.censored);
        oos.writeObject(SAMState.imputedMatrix);
        oos.writeObject(SAMState.pairedGroupAExpts);
        oos.writeObject(SAMState.pairedGroupBExpts);
    }
    
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
        SAMState.firstRun = ois.readBoolean();
        SAMState.useAllUniquePerms =ois.readBoolean();
        SAMState.groupAssignments = (int[])ois.readObject();
        SAMState.numSigGenesByDelta = (int[])ois.readObject();
        SAMState.sortedDArrayIndices = (int[])ois.readObject();
        SAMState.studyDesign = ois.readInt();
        SAMState.numCombs = ois.readInt();
        SAMState.numNeighbors = ois.readInt();
        SAMState.numMultiClassGroups = ois.readInt();
        SAMState.numUniquePerms = ois.readInt();
        SAMState.useKNearest =ois.readBoolean();
        SAMState.calculateQLowestFDR = ois.readBoolean();
        SAMState.useTusherEtAlS0 = ois.readBoolean();
        SAMState.delta = ois.readDouble();
        SAMState.sNought = ois.readDouble();
        SAMState.pi0Hat = ois.readDouble();  
        SAMState.s0Percentile = ois.readDouble();
        SAMState.oneClassMean = ois.readDouble();
        SAMState.dBarValues = (double[])ois.readObject();
        SAMState.sortedDArray = (double[])ois.readObject();
        SAMState.deltaGrid = (double[])ois.readObject();
        SAMState.medNumFalselyCalledGenesByDelta = (double[])ois.readObject();
        SAMState.dArray = (double[])ois.readObject();
        SAMState.rArray = (double[])ois.readObject();
        SAMState.survivalTimes = (double[])ois.readObject();
        SAMState.ninetiethPercentileFalselyCalledGenesByDelta = (double[])ois.readObject();
        SAMState.FDRmedian = (double[])ois.readObject();
        SAMState.FDR90thPercentile = (double[])ois.readObject();
        SAMState.qLowestFDR = (double[])ois.readObject();
        SAMState.inSurvivalAnalysis = (boolean[])ois.readObject();
        SAMState.censored = (boolean[])ois.readObject();
        SAMState.imputedMatrix = (FloatMatrix)ois.readObject();
        SAMState.pairedGroupAExpts = (Vector)ois.readObject();
        SAMState.pairedGroupBExpts = (Vector)ois.readObject();        
    }
}
























