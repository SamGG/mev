/*
 * Created on Jan 31, 2005
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

/**
 * @author vu
 */
public class USCXValResult {
	/**
	 * [ xValKount ][ foldKount ]
	 */
	private USCFoldResult[][] foldResults;
	
	
	/**
	 * Constructors
	 * @param xValKount
	 */
	public USCXValResult( int xValKount ) {
		this.foldResults = new USCFoldResult[ xValKount ][];
	}
	public USCXValResult( USCFoldResult[][] foldResultsP ) {
		this.foldResults = foldResultsP;
	}//constructor
	
	
	public USCFoldResult[][] getFoldResults() {
		return this.foldResults;
	}
	public USCFoldResult[] getFoldResult( int xValIndex ) {
		return this.foldResults[ xValIndex ];
	}
	public void setFoldResult( USCFoldResult[] results, int xValIndex ) { 
		this.foldResults[ xValIndex ] = results;
	}
	public void setFoldResults( USCFoldResult[][] results ) {
		this.foldResults = results;
	}
	public int getXValKount() {
		return this.foldResults.length;
	}
}//end class
