/*
 * Created on Oct 28, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

/**
 * Represents a hyb
 * 
 * @author vu
 */
public class USCHyb {
	private int indexInSet;				//it's index in the USCHybSet
	private String label;				//class label
	private String hybName;				//name
	private double[] ratios;			//ratios
	private int numGenes;				//
	private int uniqueLabelIndex;		//label's index in the uniqueLabel array
	
	
	//constructor
	public USCHyb( int index, String hybNameP ) {
		this.hybName = hybNameP;
		this.indexInSet = index;
	}
	public USCHyb( int index, String hybNameP, int geneKountP ) {
		this.hybName = hybNameP;
		this.indexInSet = index;
		this.ratios = new double[ geneKountP ];
		this.numGenes = this.ratios.length;
	}
	public USCHyb( int index, String labelP, String hybNameP, double[] ratiosP ) {
		this.hybName = hybNameP;
		this.indexInSet = index;
		this.label = labelP;
		this.ratios = ratiosP;
		this.numGenes = this.ratios.length;
	}//end constructor
	
	
	public int getIndexInFullSet() {
		return this.indexInSet;
	}
	public void setHybLabel( String labelP ) {
		this.label = labelP;
	}
	public String getHybLabel() { 
		return this.label;
	}
	public String getHybName() { 
		return this.hybName;
	}
	public void setRatios( double[] ratiosP ) {
		this.ratios = ratiosP;
	}
	public void setRatio( int i, double d ) {
		this.ratios[ i ] = d;
	}
	public double[] getRatios() {
		return this.ratios;
	}
	public double getRatio( int geneIndex ) {
		return this.ratios[ geneIndex ];
	}
	public int getNumGenes() {
		return this.numGenes;
	}
	public void setUniqueLabelIndex(int uniqueLabelIndex) {
		this.uniqueLabelIndex = uniqueLabelIndex;
	}
	public int getUniqueLabelIndex() {
		return uniqueLabelIndex;
	}
}//end class
