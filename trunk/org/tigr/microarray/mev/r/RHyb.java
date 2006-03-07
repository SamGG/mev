/*
 * Created on Jul 5, 2005
 */
package org.tigr.microarray.mev.r;

import javax.swing.JRadioButton;

/**
 * @author iVu
 */
public class RHyb {
	private String hybName;
	private int hybIndex;
	private int dataType;
	private JRadioButton oneButton;
	
	
	/**
	 * 
	 */
	public RHyb( int hybIndexP, String hybNameP, JRadioButton oneButtonP, 
			int dataTypeP ) {
		this.hybIndex = hybIndexP;
		this.hybName = hybNameP;
		this.oneButton = oneButtonP;
		this.dataType = dataTypeP;
	}//constructor
	
	public String getHybName() {
		return this.hybName;
	}
	public int getHybIndex() {
		return this.hybIndex;
	}
	public int getDataType() {
		return this.dataType;
	}
	/**
	 * Used for 2 Intensity Data
	 * @return
	 */
	public boolean controlCy3() {
		return this.oneButton.isSelected();
	}
	/**
	 * Used for Affy Data
	 * @return
	 */
	public boolean oneIsTreated() {
		return this.oneButton.isSelected();
	}
}//end class