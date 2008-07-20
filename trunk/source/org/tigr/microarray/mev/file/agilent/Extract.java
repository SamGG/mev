/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.file.agilent;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/*
 * Created on Apr 7, 2004
 */

/**
 * Represents a single data Line of an Agilent File.
 * @author vu
 */
public class Extract {
	private static String TAB = "\t";
	
	private String parsedLine;
	
	
	/**
	 * Builds a new tab-delim data line by looking up the column data for each of the
	 * column headers in 'aHeaders'.  Stores it in private variable 'parsedLine'.
	 * @param headers	The Header line of the Agilent File
	 * @param line	The Data line of the Agilent File
	 * @param columnDef	The columns to be pulled out of the Agilent File
	 * @param fudger	The String to be added as a "FUDGE"
	 */
	public void parseLineByHeaders(String headers, String line, String columnDef, String fudger) {
		//System.out.println("ParsingLine()");
		
		StringBuffer sb = new StringBuffer();
		
		//get a Hashtable where the column data can be found using the Header as a key
		Hashtable ht = this.hashify(headers, line);
		
		//Break the columnDef into Tokens
		StringTokenizer st = new StringTokenizer(columnDef, Extract.TAB);
		
		//Look up each header in Hashtable, fudge if necessary
		while(st.hasMoreTokens()) {
			String header = st.nextToken();
			
			//find it
			String element = (String) ht.get(header);
			if(element == null) {
				//couldn't find the header, so fudge it with 'fudger'
				sb.append(fudger);
				sb.append(Extract.TAB);
			} else {
				sb.append(element);
				sb.append(Extract.TAB);
			}
			
		}//end i
		
		this.parsedLine = sb.toString();
	}//end parseLineByHeader()
	
	
	/**
	 * 
	 * @param indices
	 * @param line
	 * @param fudger
	 */
	public void parseLineByIndex(String indices, String line, String fudger) {
		StringBuffer sb = new StringBuffer();
		
		StringTokenizer stLine = new StringTokenizer(line, Extract.TAB);
		Vector vToken = new Vector();
		while(stLine.hasMoreTokens()) {
			String token = stLine.nextToken();
			vToken.add(token);
		}
		
		StringTokenizer stIndex = new StringTokenizer(indices, Extract.TAB);
		while(stIndex.hasMoreTokens()) {
			String token = stIndex.nextToken();
			if(token.equals(MeVerizer.FUDGE)) {
				sb.append(fudger);
				sb.append(Extract.TAB);
			} else {
				Integer IToken = new Integer(token);
				int iToken = IToken.intValue();
				String s = (String) vToken.elementAt(iToken);
				sb.append(s);
				sb.append(Extract.TAB);
			}
		}//end while
		
		this.parsedLine = sb.toString();
	}//end parseLineByIndex()
	
	
	/**
	 * Creates a Hashtable where the column headers are the keys for the column data
	 * @param headers	Tab-delimited Header line of Agilent file
	 * @param line				Tab-delimited Data line of Agilent file
	 * @return	
	 */
	private Hashtable hashify(String headers, String line) {
		Hashtable htReturn = new Hashtable();
		
		StringTokenizer stLine = new StringTokenizer(line, "\t");
		StringTokenizer stHeader = new StringTokenizer(headers, "\t");
		
		while(stHeader.hasMoreTokens()) {
			String header = stHeader.nextToken();
			String element = stLine.nextToken();
			
			htReturn.put(header, element);
			//System.out.println(header + "\t" + element);
		}
		
		return htReturn;
	}
	
	
	/**
	 * Get the new tab-delim data line.
	 * @return	Returns the new tab-delim data line
	 */
	public String getParsedLine() {
		return this.parsedLine;
	}
}//end class