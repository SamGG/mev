/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Jul 5, 2005
 */
package org.tigr.microarray.mev.r;

import javax.swing.JRadioButton;

/**
 * This class keeps track of each of the user's loaded slides.
 * 
 * @author iVu
 */
public class RHyb {
	private String hybName;
	private int hybIndex;
	private int dataType;
	private JRadioButton oneButton;
	
	
	/**
	 * Constructor
	 * @param hybIndexP	Index of this slide in MeV's set of loaded slides
	 * @param hybNameP	Filename of this slide
	 * @param oneButtonP	JRadioButton to see if this slide was Cy3/Cy5/Treatment/Control
	 * @param dataTypeP	IData.dataType
	 */
	public RHyb( int hybIndexP, String hybNameP, JRadioButton oneButtonP, 
			int dataTypeP ) {
		this.hybIndex = hybIndexP;
		this.hybName = hybNameP;
		this.oneButton = oneButtonP;
		this.dataType = dataTypeP;
	}//constructor
	
	/**
	 * Returns the filename of this hyb (slide)
	 * @return
	 */
	public String getHybName() {
		return this.hybName;
	}
	/**
	 * Returns the index of this hyb in the array of hybs loaded into MeV
	 * @return
	 */
	public int getHybIndex() {
		return this.hybIndex;
	}
	/**
	 * Returns the IData.dataType integer of this hyb
	 * @return
	 */
	public int getDataType() {
		return this.dataType;
	}
	/**
	 * Used for 2 Intensity Data.  Returns true, if this hyb's control sample 
	 * was dyed with Cy3.  false if the treated sample was dyed with Cy3.  Gives 
	 * you enough data to know which sample is dyed with which dye
	 * @return
	 */
	public boolean controlCy3() {
		return this.oneButton.isSelected();
	}
	/**
	 * Used for Affy Data.  Returns true if this hyb was a treated sample.
	 * @return
	 */
	public boolean oneIsTreated() {
		return this.oneButton.isSelected();
	}
}//end class