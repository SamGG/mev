/*
 * Created on Nov 16, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

/**
 * This is a lightweight class used to keep track of gene order.  iOriginal is this gene's 
 * index in its original sort order when the USC algorithm was called.<br>
 * <br>
 * iSorted is the gene's index after it has been sorted on Beta.<br>
 * isRelevant true if this gene is found to be relevant<br>
 * isCorrelated true if this gene's corr < rho (remove if true)<br>
 * beta is the greatest dikShrunk value for this gene<br>
 * <br>
 * Note: The genes are sorted AFTER removing irrelavant genes.  Consequently, 
 * only the subset of relevant genes will have an iSorted value.
 * @author vu
 */
public class USCOrder implements java.io.Serializable {
	private int iOriginal;
	private int iRelevant;

	private boolean isRelevant;
	private boolean isCorrelated;
	private double beta;
	
	public USCOrder(){
		this.isRelevant = false;
		this.isCorrelated = false;
	}
	
	public USCOrder( int iOriginal ) {
		this.iOriginal = iOriginal;
		this.isRelevant = false;
		this.isCorrelated = false;
	}
	
	
	public void setIRelevant( int iSorted ) {
		this.iRelevant = iSorted;
	}
	public void setRelevant( boolean isRelevant ) {
		this.isRelevant = isRelevant;
	}
	public void setBeta( double maxDik ) {
		this.beta = maxDik;
	}
	public int getIOriginal() {
		return this.iOriginal;
	}
	public int getIRelevant() {
		return this.iRelevant;
	}
	public boolean isRelevant() {
		return this.isRelevant;
	}
	public double getBeta() {
		return this.beta;
	}
	public void setCorrelated(boolean isCorrelated) {
		this.isCorrelated = isCorrelated;
	}
	public boolean isCorrelated() {
		return isCorrelated;
	}
	public boolean use() {
		if( this.isRelevant && ! this.isCorrelated ) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * @param original The iOriginal to set.
	 */
	public void setIOriginal(int original) {
		iOriginal = original;
	}
	
	public static void main( String[] args ) {
		boolean one = true;
		boolean two = false;
		if( one && ! two ) {
			System.out.println( "true" );
		} else {
			System.out.println( "false" );
		}
	}
}//end class
