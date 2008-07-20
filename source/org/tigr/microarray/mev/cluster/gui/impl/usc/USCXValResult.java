/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
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
