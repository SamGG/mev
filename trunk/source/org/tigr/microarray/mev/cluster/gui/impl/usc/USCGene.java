/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Nov 16, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

/**
 * @author vu
 */
public class USCGene {
	private String geneName;
	private String[] extraFields;
	
	/**
	 * EH - Null constructor added to allow state-saving.
	 *
	 */
	public USCGene(){}
	
	public USCGene( String geneNameP, String[] extraFieldsP ) {
		this.geneName = geneNameP;
		this.extraFields = extraFieldsP;
	}
	
	
	public String getGeneName() {
		return this.geneName;
	}
	public String[] getExtraFields() {
		return this.extraFields;
	}
	public String getExtraField( int iField ) {
		return this.extraFields[ iField ];
	}
	public int getExtraFieldSize() {
		if( this.extraFields == null ) {
			return 0;
		} else {
			return this.extraFields.length;
		}
	}
	//EH added accessor methods to make this class a JavaBean
	/**
	 * @param extraFields The extraFields to set.
	 */
	public void setExtraFields(String[] extraFields) {
		this.extraFields = extraFields;
	}
	/**
	 * @param geneName The geneName to set.
	 */
	public void setGeneName(String geneName) {
		this.geneName = geneName;
	}
}//end class