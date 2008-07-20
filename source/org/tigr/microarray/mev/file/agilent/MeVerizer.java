/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.file.agilent;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

/*
 * Created on Apr 5, 2004
 */

/**
 * MeVerizer reformats an Agilent Feature Extractor (".txt") output file into an MeV
 * formatted file (".mev").
 * 
 * @author vu
 */
public class MeVerizer {
	/**
	 * The String that characterizes the Header Line in the Agilent File --> "FEATURES"
	 */
	public static String HEADER_HEADER = "FEATURES";
	/**
	 * The String used in lieu of an actual column Header when none are available.
	 */
	public static String FUDGE = "FUDGE";
	/**
	 * The String used as a placeholder in the tab-delimited file so that blank cells are
	 * accounted for.
	 */
	public static String PLACE_HOLDER = "MOTHRA";
	
	public static boolean PARSE_BY_HEADERS = true;
	public static boolean PARSE_BY_INDICES = false;
	
	
	//The Header line of the .mev File to be created.
	private String mHeaders;
	//The Agilent equivalent of the desired mHeaders
	private String columnDef;
	//The actual header line of the Agilent file
	private String headerLine;
	
	private int iHeaders;
	
	//Vector of Extract objects
	private Vector vExtract;
	
	
	/**
	 * Constructor
	 * @param mHeadersP	tab-delim String of MeV's Headers
	 * @param aHeadersP	tab-delim String of Agilent Headers equivalent to MeV's
	 * @throws ColumnMismatchException	If the number of columns in the header strings don't match
	 */
	public MeVerizer(String mHeadersP, String columnDefP) throws 
	ColumnMismatchException {
		this.mHeaders = mHeadersP;
		this.columnDef = columnDefP;
		
		StringTokenizer st1 = new StringTokenizer(mHeadersP, "\t");
		StringTokenizer st2 = new StringTokenizer(columnDefP, "\t");
		
		if(st1.countTokens() != st2.countTokens()) {
			throw new ColumnMismatchException("Header columns are mismatched");
		}
	}//end constructor
	
	
	/**
	 * Parses the Agilent file into a Vector of Extract objects
	 * @param f
	 * @throws IOException
	 */
	public void parseExtract(File f, boolean parseByHeaders, String replacement)
	throws IOException {
		
		//get file as Vector of rows where null cells have been replaced
		Vector vLine = this.readFileAsVector(f);
		
		//figure out where the data starts and get the header line
		this.iHeaders = this.findHeaderRow(vLine, MeVerizer.HEADER_HEADER);
		this.headerLine = this.findHeaderString(vLine, MeVerizer.HEADER_HEADER);
		
		//create Extract objects to represent each feature
		this.vExtract = this.createExtract(vLine, headerLine, this.iHeaders, 
		parseByHeaders, replacement);
	}//end parseExtract()
	
	
	/**
	 * Loops through the Vector of Lines, creating an Extract object to represent
	 * @param vLine	Vector of Lines from Agilent File (Nulls replaced with static var)
	 * @param headerLine	The tab-delimited Header line from the Agilent file
	 * @param iHeader	The index of the Header line in the vLine Vector
	 * @return	Returns a Vector of Extract objects
	 */
	private Vector createExtract(Vector vLine, String headerLine, int iHeader,
	boolean parseByHeaders, String replacement) {
		Vector vReturn = new Vector();
		
		//loop through rows of data, starting at the first line of actual data (follows Header)
		for(int i = iHeader + 1; i < vLine.size(); i ++) {
			String line = (String) vLine.elementAt(i);
			
			//create Extract object to represent each row.  add to vExtract Vector
			Extract e = new Extract();
			if(parseByHeaders) {
				e.parseLineByHeaders(headerLine, line, this.columnDef, replacement);
			} else {
				e.parseLineByIndex(this.columnDef, line, replacement);
			}
			vReturn.add(e);
		}//end i		
		
		return vReturn;
	}//end createExtract()
	
	
	/**
	 * Loops through the Vector of Lines looking for the Header line.
	 * @param vLine	Vector of Lines from Agilent file
	 * @param sDefine	The String that defines this line as the Header
	 * @return	Returns the Header line from the Agilent file
	 */
	private String findHeaderString(Vector vLine, String sDefine) {
		String sReturn = null;
		
		for(int i = 0; i < vLine.size(); i ++) {
			String line = (String) vLine.elementAt(i);
			if(line.startsWith(sDefine)) {
				sReturn = line;
			}
		}
		
		return sReturn;
	}//end findHeaderString()
	/**
	 * Loops through the Vector of Lines looking for the index of the Header line
	 * @param vLine	Vector of Lines from the Agilent file
	 * @param sDefine	The String that defines this line as the Header
	 * @return	Returns the index of the Header line from the Agilent file
	 */
	private int findHeaderRow(Vector vLine, String sDefine)  {
		int iReturn = 0;
		
		for(int i = 0; i < vLine.size(); i ++) {
			String line = (String) vLine.elementAt(i);
			if(line.startsWith(sDefine)) {
				iReturn = i;
			}
		}
			
		return iReturn;
	}//end findHeaderRow()
	
	
	/**
	 * Creates and uses a Reader object to read a file into a Vector of Lines where any
	 * null cells of the tab-delimited File are replaced with a String denoted by the static
	 * variable MeVerizer.PLACE_HOLDER.
	 * @param f	The File to be read
	 * @return	Returns a Vector where each element is a String representation of a
	 * Line from the File
	 * @throws IOException
	 */
	private Vector readFileAsVector(File f) throws IOException {
		Reader r = new Reader();
		r.readFile(f);
		return r.getVNullLine(MeVerizer.PLACE_HOLDER);
	}//end readFileASVector()
	
	
	/**
	 * Get the new .mev File String.
	 * @return	Returns a tab-delimited String representing a formatted .mev File
	 */
	public String getFileString() {
		StringBuffer sbReturn = new StringBuffer(this.mHeaders + "\r\n");
		
		for(int i = 0; i < this.vExtract.size(); i ++) {
			Extract e = (Extract) this.vExtract.elementAt(i);
			String s = e.getParsedLine();
			sbReturn.append(s);
			sbReturn.append("\r\n");
		}
		
		return sbReturn.toString();
	}
	
	
	/**
	 * Compiles a list of the 'columnDef' that aren't found in 'headerLine'
	 * @return Vector of 'columnDef' not found in 'headerLine'
	 */
	public Vector checkHeaders() {
		Vector vReturn = new Vector();
		Vector vWant = new Vector();
		Vector vHave = new Vector();
		
		StringTokenizer stWant = new StringTokenizer(this.columnDef, "\t");
		while(stWant.hasMoreTokens()) {
			vWant.add(stWant.nextToken());
		}
		
		StringTokenizer stHave = new StringTokenizer(this.headerLine, "\t");
		while(stHave.hasMoreTokens()) {
			vHave.add(stHave.nextToken());
		}
		
		for(int i = 0; i < vWant.size(); i ++) {
			String want = (String) vWant.elementAt(i);
			
			boolean wantFound = false;
			
			for(int j = 0; j < vHave.size(); j ++) {
				String have = (String) vHave.elementAt(j);
				if(want.toLowerCase().equals(have.toLowerCase())) {
					wantFound = true;
				}
			}
			
			if(!wantFound) {
				vReturn.add(want);
			}
		}
		
		return vReturn;
	}//end checkHeaders()
}//end class