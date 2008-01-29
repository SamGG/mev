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