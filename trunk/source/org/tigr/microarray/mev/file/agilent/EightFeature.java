/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Jan 22, 2004
 */
package org.tigr.microarray.mev.file.agilent;

import java.util.StringTokenizer;

/**
 * A basic extension of AgilentFeature to parse AgilentFeatures containing 8 columns.
 * 
 * @author vu
 */
public class EightFeature extends AgilentFeature {
	/**
	 * Constructor.  Parses a line representing a single feature and stores as local variables.
	 * @param line One line from an Agilent Pattern File NOT containing a TopHit column
	 */
	public EightFeature(String line) {
		StringTokenizer st = new StringTokenizer(line,"\t");
		int kount = st.countTokens();
		if( kount != 8 ) {
			//big problems here
			System.out.println("kount != 8:" + line);
		} else {
			//we're ok
			String one = st.nextToken();
			String two = st.nextToken();
			String three = st.nextToken();
			String four = st.nextToken();
			String five = st.nextToken();
			String six = st.nextToken();
			String seven = st.nextToken();
			String eight = st.nextToken();
			
			this.setCol(this.s2int(one));
			this.setRow(this.s2int(two));
			this.setName(three);
			this.setSysName(four);
			this.setRefNumber(this.s2int(five));
			this.setControlType(six);
			this.setGeneName(seven);
			this.setDesc(eight);
			
			this.assignFeatureType();
		}
	}//end constructor
	
	
	public String stripHylon(String s) {
		String toReturn;
		
		int iDot = s.indexOf(".");
		int iDash = s.indexOf("-");
		if(iDot != -1) {
			toReturn = s.substring(0,iDot);
		} else if(iDash != -1) {
			toReturn = s.substring(0,iDash);
		} else {
			toReturn = s;
		}
		
		return toReturn;
	}
	
	
	/**
	 * Converts a String to an int
	 * @param s	String value representing an int
	 * @return	int
	 */
	public int s2int(String s) {
		Integer I = new Integer(s);
		return I.intValue();
	}
}