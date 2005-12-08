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
}//end class