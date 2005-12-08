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
}//end class