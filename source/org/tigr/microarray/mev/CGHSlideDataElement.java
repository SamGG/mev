/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev;

import org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone;

public class CGHSlideDataElement extends SlideDataElement {
	public static final int CLONE_ID = 0;
	public static final int CHR = 1;
	public static final int START = 2;
	public static final int END = 3;
	public static final int DESC = 4;
		
	public  CGHSlideDataElement(String UID, int[] rows, int[] columns, float[] intensities, String[] values){
		super(UID, rows, columns, intensities, values);
	}
	
	/**
	 * @param rows
	 * @param cols
	 * @param extraFields
	 * @param uid
	 * @param isNull
	 * @param isNonZero
	 */
	public CGHSlideDataElement(int[] rows, int[] cols, String[] extraFields, String uid, boolean isNull, boolean isNonZero) {
		super(rows, cols, extraFields, uid, isNull, isNonZero);
	}

	public String getCloneID() {
		return extraFields[CLONE_ID];
	}
	
	public String getChromosome() {
		return extraFields[CHR];
	}
	
	public int getProbeStart() {
		return Integer.parseInt(extraFields[START]);
	}
	
	public int getProbeEnd() {
		return Integer.parseInt(extraFields[END]);
	}
	
	public int getProbeLength(){
		return getProbeEnd() - getProbeStart();
	}
	
	public String getDesc(){
		return extraFields[DESC];
	}
	public CGHClone getClone(int species) {
		//System.out.println("getClone(): " + getCloneID() + ", " + getChromosome() + ", " + getProbeStart() + ", " + getProbeEnd());
		return new CGHClone(getCloneID().trim(), getChromosome(), getProbeStart(), getProbeEnd(), species);
	}
}
