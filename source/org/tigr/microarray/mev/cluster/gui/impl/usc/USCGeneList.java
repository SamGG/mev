/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Oct 29, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

/**
 * @author vu
 */
public class USCGeneList {
	private String[] genes;
	/**
	 * Extra annotation fields
	 * [ iGeneIndex ][ extraFieldIndex ]
	 */
	private String[][] extraFields;
	private int[] indices;
	
	
	/**
	 * 
	 * @param genesP
	 */
	/*
	public USCGeneList( String[] genesP ) {
		this.genes = genesP;
	}//end constructor
	*/
	public USCGeneList( String[] genesP, String[][] extraFieldsP, int[] indicesP ) {
		this.genes = genesP;
		this.extraFields = extraFieldsP;
		this.indices = indicesP;
	}
	
	
	public String[] getGenes() {
		return this.genes;
	}
	public String getGene( int i ) {
		return this.genes[ i ];
	}
	public String[][] getExtraFields() {
		return this.extraFields;
	}
	public String[] getExtraFields( int iGeneIndex ) {
		return this.extraFields[ iGeneIndex ];
	}
	public int[] getIndices() {
		return this.indices;
	}
	public int getIndex( int geneIndex ) {
		return this.indices[ geneIndex ];
	}
	public int getGeneKount() {
		return this.genes.length;
	}
}