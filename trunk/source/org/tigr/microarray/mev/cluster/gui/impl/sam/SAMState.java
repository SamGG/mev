/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * SAMState.java
 *
 * Created on February 3, 2003, 10:31 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.sam;

import java.util.Vector;

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
    
	/**
	 * @return Returns the calculateQLowestFDR.
	 */
	public static boolean isCalculateQLowestFDR() {
		return calculateQLowestFDR;
	}
	/**
	 * @param calculateQLowestFDR The calculateQLowestFDR to set.
	 */
	public static void setCalculateQLowestFDR(boolean calculateQLowestFDR) {
		SAMState.calculateQLowestFDR = calculateQLowestFDR;
	}
	/**
	 * @return Returns the censored.
	 */
	public static boolean[] getCensored() {
		return censored;
	}
	/**
	 * @param censored The censored to set.
	 */
	public static void setCensored(boolean[] censored) {
		SAMState.censored = censored;
	}
	/**
	 * @return Returns the dArray.
	 */
	public static double[] getDArray() {
		return dArray;
	}
	/**
	 * @param array The dArray to set.
	 */
	public static void setDArray(double[] array) {
		dArray = array;
	}
	/**
	 * @return Returns the dBarValues.
	 */
	public static double[] getDBarValues() {
		return dBarValues;
	}
	/**
	 * @param barValues The dBarValues to set.
	 */
	public static void setDBarValues(double[] barValues) {
		dBarValues = barValues;
	}
	/**
	 * @return Returns the delta.
	 */
	public static double getDelta() {
		return delta;
	}
	/**
	 * @param delta The delta to set.
	 */
	public static void setDelta(double delta) {
		SAMState.delta = delta;
	}
	/**
	 * @return Returns the deltaGrid.
	 */
	public static double[] getDeltaGrid() {
		return deltaGrid;
	}
	/**
	 * @param deltaGrid The deltaGrid to set.
	 */
	public static void setDeltaGrid(double[] deltaGrid) {
		SAMState.deltaGrid = deltaGrid;
	}
	/**
	 * @return Returns the fDR90thPercentile.
	 */
	public static double[] getFDR90thPercentile() {
		return FDR90thPercentile;
	}
	/**
	 * @param percentile The fDR90thPercentile to set.
	 */
	public static void setFDR90thPercentile(double[] percentile) {
		FDR90thPercentile = percentile;
	}
	/**
	 * @return Returns the fDRmedian.
	 */
	public static double[] getFDRmedian() {
		return FDRmedian;
	}
	/**
	 * @param rmedian The fDRmedian to set.
	 */
	public static void setFDRmedian(double[] rmedian) {
		FDRmedian = rmedian;
	}
	/**
	 * @return Returns the firstRun.
	 */
	public static boolean isFirstRun() {
		return firstRun;
	}
	/**
	 * @param firstRun The firstRun to set.
	 */
	public static void setFirstRun(boolean firstRun) {
		SAMState.firstRun = firstRun;
	}
	/**
	 * @return Returns the groupAssignments.
	 */
	public static int[] getGroupAssignments() {
		return groupAssignments;
	}
	/**
	 * @param groupAssignments The groupAssignments to set.
	 */
	public static void setGroupAssignments(int[] groupAssignments) {
		SAMState.groupAssignments = groupAssignments;
	}
	/**
	 * @return Returns the imputedMatrix.
	 */
	public static FloatMatrix getImputedMatrix() {
		return imputedMatrix;
	}
	/**
	 * @param imputedMatrix The imputedMatrix to set.
	 */
	public static void setImputedMatrix(FloatMatrix imputedMatrix) {
		SAMState.imputedMatrix = imputedMatrix;
	}
	/**
	 * @return Returns the inSurvivalAnalysis.
	 */
	public static boolean[] getInSurvivalAnalysis() {
		return inSurvivalAnalysis;
	}
	/**
	 * @param inSurvivalAnalysis The inSurvivalAnalysis to set.
	 */
	public static void setInSurvivalAnalysis(boolean[] inSurvivalAnalysis) {
		SAMState.inSurvivalAnalysis = inSurvivalAnalysis;
	}
	/**
	 * @return Returns the medNumFalselyCalledGenesByDelta.
	 */
	public static double[] getMedNumFalselyCalledGenesByDelta() {
		return medNumFalselyCalledGenesByDelta;
	}
	/**
	 * @param medNumFalselyCalledGenesByDelta The medNumFalselyCalledGenesByDelta to set.
	 */
	public static void setMedNumFalselyCalledGenesByDelta(
			double[] medNumFalselyCalledGenesByDelta) {
		SAMState.medNumFalselyCalledGenesByDelta = medNumFalselyCalledGenesByDelta;
	}
	/**
	 * @return Returns the ninetiethPercentileFalselyCalledGenesByDelta.
	 */
	public static double[] getNinetiethPercentileFalselyCalledGenesByDelta() {
		return ninetiethPercentileFalselyCalledGenesByDelta;
	}
	/**
	 * @param ninetiethPercentileFalselyCalledGenesByDelta The ninetiethPercentileFalselyCalledGenesByDelta to set.
	 */
	public static void setNinetiethPercentileFalselyCalledGenesByDelta(
			double[] ninetiethPercentileFalselyCalledGenesByDelta) {
		SAMState.ninetiethPercentileFalselyCalledGenesByDelta = ninetiethPercentileFalselyCalledGenesByDelta;
	}
	/**
	 * @return Returns the numCombs.
	 */
	public static int getNumCombs() {
		return numCombs;
	}
	/**
	 * @param numCombs The numCombs to set.
	 */
	public static void setNumCombs(int numCombs) {
		SAMState.numCombs = numCombs;
	}
	/**
	 * @return Returns the numMultiClassGroups.
	 */
	public static int getNumMultiClassGroups() {
		return numMultiClassGroups;
	}
	/**
	 * @param numMultiClassGroups The numMultiClassGroups to set.
	 */
	public static void setNumMultiClassGroups(int numMultiClassGroups) {
		SAMState.numMultiClassGroups = numMultiClassGroups;
	}
	/**
	 * @return Returns the numNeighbors.
	 */
	public static int getNumNeighbors() {
		return numNeighbors;
	}
	/**
	 * @param numNeighbors The numNeighbors to set.
	 */
	public static void setNumNeighbors(int numNeighbors) {
		SAMState.numNeighbors = numNeighbors;
	}
	/**
	 * @return Returns the numSigGenesByDelta.
	 */
	public static int[] getNumSigGenesByDelta() {
		return numSigGenesByDelta;
	}
	/**
	 * @param numSigGenesByDelta The numSigGenesByDelta to set.
	 */
	public static void setNumSigGenesByDelta(int[] numSigGenesByDelta) {
		SAMState.numSigGenesByDelta = numSigGenesByDelta;
	}
	/**
	 * @return Returns the numUniquePerms.
	 */
	public static int getNumUniquePerms() {
		return numUniquePerms;
	}
	/**
	 * @param numUniquePerms The numUniquePerms to set.
	 */
	public static void setNumUniquePerms(int numUniquePerms) {
		SAMState.numUniquePerms = numUniquePerms;
	}
	/**
	 * @return Returns the oneClassMean.
	 */
	public static double getOneClassMean() {
		return oneClassMean;
	}
	/**
	 * @param oneClassMean The oneClassMean to set.
	 */
	public static void setOneClassMean(double oneClassMean) {
		SAMState.oneClassMean = oneClassMean;
	}
	/**
	 * @return Returns the pairedGroupAExpts.
	 */
	public static Vector getPairedGroupAExpts() {
		return pairedGroupAExpts;
	}
	/**
	 * @param pairedGroupAExpts The pairedGroupAExpts to set.
	 */
	public static void setPairedGroupAExpts(Vector pairedGroupAExpts) {
		SAMState.pairedGroupAExpts = pairedGroupAExpts;
	}
	/**
	 * @return Returns the pairedGroupBExpts.
	 */
	public static Vector getPairedGroupBExpts() {
		return pairedGroupBExpts;
	}
	/**
	 * @param pairedGroupBExpts The pairedGroupBExpts to set.
	 */
	public static void setPairedGroupBExpts(Vector pairedGroupBExpts) {
		SAMState.pairedGroupBExpts = pairedGroupBExpts;
	}
	/**
	 * @return Returns the pi0Hat.
	 */
	public static double getPi0Hat() {
		return pi0Hat;
	}
	/**
	 * @param pi0Hat The pi0Hat to set.
	 */
	public static void setPi0Hat(double pi0Hat) {
		SAMState.pi0Hat = pi0Hat;
	}
	/**
	 * @return Returns the qLowestFDR.
	 */
	public static double[] getQLowestFDR() {
		return qLowestFDR;
	}
	/**
	 * @param lowestFDR The qLowestFDR to set.
	 */
	public static void setQLowestFDR(double[] lowestFDR) {
		qLowestFDR = lowestFDR;
	}
	/**
	 * @return Returns the rArray.
	 */
	public static double[] getRArray() {
		return rArray;
	}
	/**
	 * @param array The rArray to set.
	 */
	public static void setRArray(double[] array) {
		rArray = array;
	}
	/**
	 * @return Returns the s0Percentile.
	 */
	public static double getS0Percentile() {
		return s0Percentile;
	}
	/**
	 * @param percentile The s0Percentile to set.
	 */
	public static void setS0Percentile(double percentile) {
		s0Percentile = percentile;
	}
	/**
	 * @return Returns the sNought.
	 */
	public static double getSNought() {
		return sNought;
	}
	/**
	 * @param nought The sNought to set.
	 */
	public static void setSNought(double nought) {
		sNought = nought;
	}
	/**
	 * @return Returns the sortedDArray.
	 */
	public static double[] getSortedDArray() {
		return sortedDArray;
	}
	/**
	 * @param sortedDArray The sortedDArray to set.
	 */
	public static void setSortedDArray(double[] sortedDArray) {
		SAMState.sortedDArray = sortedDArray;
	}
	/**
	 * @return Returns the sortedDArrayIndices.
	 */
	public static int[] getSortedDArrayIndices() {
		return sortedDArrayIndices;
	}
	/**
	 * @param sortedDArrayIndices The sortedDArrayIndices to set.
	 */
	public static void setSortedDArrayIndices(int[] sortedDArrayIndices) {
		SAMState.sortedDArrayIndices = sortedDArrayIndices;
	}
	/**
	 * @return Returns the studyDesign.
	 */
	public static int getStudyDesign() {
		return studyDesign;
	}
	/**
	 * @param studyDesign The studyDesign to set.
	 */
	public static void setStudyDesign(int studyDesign) {
		SAMState.studyDesign = studyDesign;
	}
	/**
	 * @return Returns the survivalTimes.
	 */
	public static double[] getSurvivalTimes() {
		return survivalTimes;
	}
	/**
	 * @param survivalTimes The survivalTimes to set.
	 */
	public static void setSurvivalTimes(double[] survivalTimes) {
		SAMState.survivalTimes = survivalTimes;
	}
	/**
	 * @return Returns the useAllUniquePerms.
	 */
	public static boolean isUseAllUniquePerms() {
		return useAllUniquePerms;
	}
	/**
	 * @param useAllUniquePerms The useAllUniquePerms to set.
	 */
	public static void setUseAllUniquePerms(boolean useAllUniquePerms) {
		SAMState.useAllUniquePerms = useAllUniquePerms;
	}
	/**
	 * @return Returns the useKNearest.
	 */
	public static boolean isUseKNearest() {
		return useKNearest;
	}
	/**
	 * @param useKNearest The useKNearest to set.
	 */
	public static void setUseKNearest(boolean useKNearest) {
		SAMState.useKNearest = useKNearest;
	}
	/**
	 * @return Returns the useTusherEtAlS0.
	 */
	public static boolean isUseTusherEtAlS0() {
		return useTusherEtAlS0;
	}
	/**
	 * @param useTusherEtAlS0 The useTusherEtAlS0 to set.
	 */
	public static void setUseTusherEtAlS0(boolean useTusherEtAlS0) {
		SAMState.useTusherEtAlS0 = useTusherEtAlS0;
    }
}
























