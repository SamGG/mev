/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Apr 19, 2004
 */
package org.tigr.microarray.mev.file.agilent;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

/**
 * MeVotation reads an Agilent Pattern File, parses it, and returns a tab-delim String
 * representing the parsed .ann file.
 * 
 * @author vu
 */
public class MeVotation {
	static String TAB = "\t";
	static String END_LINE = "\r\n";
	
	private Vector vLine;
	private AgilentFile af;
	
	
	/**
	 * Constructor.  Creates Reader object to read 'agilentPattern' File, replacing
	 * blank cells with 'blankReplacement' and storing the Vector of Lines locally
	 * @param agilentPattern	The Agilent Pattern File to be parsed
	 * @param blankReplacement	A String to be used as a placeholder for blanks
	 * @throws IOException	
	 */
	public MeVotation() {
		//
	}//end constructor
	
	
	/**
	 * Creates an AgilentFile object which does the parsing
	 * @throws UnrecognizedPatternException	If the Pattern is an unknown format
	 */
	public void parseAgilentPattern(File agilentPattern, String blankReplacement) 
	throws UnrecognizedPatternException, IOException  {
		//read the file
		Reader r = new Reader();
		r.readFile(agilentPattern);
		//this.vLine = r.getVNullLine(blankReplacement);
		this.vLine = r.getVNullLineBasedOnHeader( blankReplacement );
		this.af = new AgilentFile(this.vLine);
	}
	
	
	/**
	 * Compiles a tab-delim String that can be written to a new File.  At this time
	 * (20040419), there are only 2 types of Agilent Oligo pattern files.  There are
	 * 8-column patterns and 9-col patterns.  The 9-col contains 1 extra column of
	 * TopHit.
	 * @return	Tab-delimited String representing the new .ann File to be written
	 */
	public String getFileString() {
		StringBuffer sbReturn = new StringBuffer();
		
		Vector v = this.af.getVAgilentFeature();
		if(this.af.getPatternType() == AgilentFile.EIGHT_COL) {
			sbReturn.append("UID\tR\tC\tGeneName\tDescription\r\n");
			for(int i = 0; i < v.size(); i ++) {
				EightFeature ef = (EightFeature) v.elementAt(i);
				if(! ef.getControlType().equals(AgilentFeature.sIgnore)) {
					sbReturn.append(ef.getRefNumber());
					sbReturn.append(MeVotation.TAB);
					sbReturn.append(ef.getRow());
					sbReturn.append(MeVotation.TAB);
					sbReturn.append(ef.getCol());
					sbReturn.append(MeVotation.TAB);
					sbReturn.append(this.nullify(ef.getGeneName()));
					sbReturn.append(MeVotation.TAB);
					sbReturn.append(this.nullify(ef.getDesc()));
					sbReturn.append(MeVotation.END_LINE);
				}
			}//end i
		} else if(this.af.getPatternType() == AgilentFile.NINE_COL) {
			sbReturn.append("UID\tR\tC\tGeneName\tDescription\tTopHit\r\n");
			for(int i = 0; i < v.size(); i ++) {
				TopFeature tf = (TopFeature) v.elementAt(i);
				if(! tf.getControlType().equals(AgilentFeature.sIgnore)) {
					sbReturn.append(tf.getRefNumber());
					sbReturn.append(MeVotation.TAB);
					sbReturn.append(tf.getRow());
					sbReturn.append(MeVotation.TAB);
					sbReturn.append(tf.getCol());
					sbReturn.append(MeVotation.TAB);
					sbReturn.append(this.nullify(tf.getName()));
					sbReturn.append(MeVotation.TAB);
					sbReturn.append(this.nullify(tf.getDesc()));
					sbReturn.append(MeVotation.TAB);
					sbReturn.append(this.nullify(TopFeature.parseAccession(tf.getTopHit())));
					sbReturn.append(MeVotation.END_LINE);
				}
				
				if( i == 100 ) {
					//System.out.println(sbReturn.toString());
				}
			}//end i
		} else {
			//something really funky
			System.out.println("SomethingReallyFunky");
		}
		
		return sbReturn.toString();
	}
	
	
	private String nullify(String s) {
		if(s == null) {
			return "null";
		} else if(s.equals("")){
			return "null";
		} else if(s.equals("NA")) {
			return "null";
		} else {
			return s;
		}
	}
}//end class