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
public class SAMState implements java.io.Serializable {
    
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
        this.firstRun = ois.readBoolean();
        this.useAllUniquePerms =ois.readBoolean();
        this.groupAssignments = (int[])ois.readObject();
        this.numSigGenesByDelta = (int[])ois.readObject();
        this.sortedDArrayIndices = (int[])ois.readObject();
        this.studyDesign = ois.readInt();
        this.numCombs = ois.readInt();
        this.numNeighbors = ois.readInt();
        this.numMultiClassGroups = ois.readInt();
        this.numUniquePerms = ois.readInt();
        this.useKNearest =ois.readBoolean();
        this.calculateQLowestFDR = ois.readBoolean();
        this.useTusherEtAlS0 = ois.readBoolean();
        this.delta = ois.readDouble();
        this.sNought = ois.readDouble();
        this.pi0Hat = ois.readDouble();  
        this.s0Percentile = ois.readDouble();
        this.oneClassMean = ois.readDouble();
        this.dBarValues = (double[])ois.readObject();
        this.sortedDArray = (double[])ois.readObject();
        this.deltaGrid = (double[])ois.readObject();
        this.medNumFalselyCalledGenesByDelta = (double[])ois.readObject();
        this.dArray = (double[])ois.readObject();
        this.rArray = (double[])ois.readObject();
        this.survivalTimes = (double[])ois.readObject();
        this.ninetiethPercentileFalselyCalledGenesByDelta = (double[])ois.readObject();
        this.FDRmedian = (double[])ois.readObject();
        this.FDR90thPercentile = (double[])ois.readObject();
        this.qLowestFDR = (double[])ois.readObject();
        this.inSurvivalAnalysis = (boolean[])ois.readObject();
        this.censored = (boolean[])ois.readObject();
        this.imputedMatrix = (FloatMatrix)ois.readObject();
        this.pairedGroupAExpts = (Vector)ois.readObject();
        this.pairedGroupBExpts = (Vector)ois.readObject();        
    }
}
























