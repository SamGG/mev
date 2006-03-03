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
	 * @param h
	 * @param string
	 * @param cy3Button
	 * @param data_type_affy_abs
	 */
	public BridgeHyb(int h, String string, JRadioButton cy3Button, int data_type_affy_abs) {
		this.hybIndex = h;
		this.hybName = string;
		this.oneButton = cy3Button;
		this.dataType = data_type_affy_abs;
	}
	
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
}