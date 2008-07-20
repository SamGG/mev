/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Dec 7, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

/**
 * This class stores the results of testing a USCHyb[] against a training USCHyb[]
 * @author vu
 */
public class USCResult implements java.io.Serializable {
	private int iDeltaBin;
	private int iRhoBin;
	private double delta;
	private double rho;

	/**
	 * [ numTestHybs ][ numClasses ]
	 */
	private double[][] discScores;
	private int numGenesUsed;
	private USCOrder[] order;
	
	
	/**
	 * Constructor
	 * @param discScores
	 * @param numGenes
	 * @param delta
	 * @param rho
	 * @param order
	 */
	public USCResult( double[][] discScores, 
	int numGenes, double delta, double rho, USCOrder[] order ) {
		this.discScores = discScores;
		this.numGenesUsed = numGenes;
		this.delta = delta;
		this.rho = rho;
		this.order = order;
	}//end constructor
	/**
	 * EH - Null constructor added to allow state-saving
	 *
	 */
	public USCResult (){}
	
	public USCOrder[] getOrder() {
		return this.order;
	}

	public void setDiscScores( double[][] discScores ) {
		this.discScores = discScores;
	}
	public double[][] getDiscScores() {
		return this.discScores;
	}
	public int getAssignedClassIndex( int hybIndex ) {
		double fMin = 99999999;
		int iMin = 0;
		
		for( int i = 0; i < this.discScores[ hybIndex ].length; i ++ ) {
			if( this.discScores[ hybIndex ][ i ] < fMin ) {
				fMin = this.discScores[ hybIndex ][ i ];
				iMin = i;
			}
		}
		
		return iMin;
	}
	public int getNumGenesUsed() {
		return this.numGenesUsed;
	}
	public double getDelta() {
		return this.delta;
	}
	public double getRho() {
		return this.rho;
	}
	
	//EH - get/set methods added for state-saving
	/**
	 * @return Returns the iDeltaBin.
	 */
	public int getIDeltaBin() {
		return iDeltaBin;
	}
	/**
	 * @param deltaBin The iDeltaBin to set.
	 */
	public void setIDeltaBin(int deltaBin) {
		iDeltaBin = deltaBin;
	}
	/**
	 * @return Returns the iRhoBin.
	 */
	public int getIRhoBin() {
		return iRhoBin;
	}
	/**
	 * @param rhoBin The iRhoBin to set.
	 */
	public void setIRhoBin(int rhoBin) {
		iRhoBin = rhoBin;
	}
	/**
	 * @param delta The delta to set.
	 */
	public void setDelta(double delta) {
		this.delta = delta;
	}
	/**
	 * @param numGenesUsed The numGenesUsed to set.
	 */
	public void setNumGenesUsed(int numGenesUsed) {
		this.numGenesUsed = numGenesUsed;
	}
	/**
	 * @param rho The rho to set.
	 */
	public void setRho(double rho) {
		this.rho = rho;
	}
	public void setOrder(USCOrder[] u){
		this.order = u;
	}
}//end class