/*
 * Created on Sep 1, 2005
 */
package org.tigr.microarray.mev.cluster.gui.impl.bridge;

import javax.swing.JRadioButton;

/**
 * @author iVu
 */
public class BridgeHyb {
	private String hybName;
	private int hybIndex;
	private int dataType;
	private JRadioButton oneButton;
	

	/**
	 * Constructor
	 * @param h	Index of this hyb in the loaded data
	 * @param hybNameP	Name of the Hyb
	 * @param cy3Button	JRadioButton denoting whether or not this hyb was labeled with cy3
	 * @param data_type_affy_abs	DataType
	 */
	public BridgeHyb(int h, String hybNameP, JRadioButton cy3Button, int data_type_affy_abs) {
		this.hybIndex = h;
		this.hybName = hybNameP;
		this.oneButton = cy3Button;
		this.dataType = data_type_affy_abs;
	}
	
	/**
	 * Returns the File Name of this Hyb
	 * @return
	 */
	public String getHybName() {
		return this.hybName;
	}
	/**
	 * Returns the index of this hyb in the loaded data
	 * @return
	 */
	public int getHybIndex() {
		return this.hybIndex;
	}
	/**
	 * Returns the datatype of this hyb
	 * @return
	 */
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
}