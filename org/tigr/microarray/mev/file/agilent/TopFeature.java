/*
 * Created on Jan 22, 2004
 */
package org.tigr.microarray.mev.file.agilent;

import java.util.StringTokenizer;

/**
 * Subclass of AgilentFeature.  Represents a NINE_COLUMN Agilent Feature (has a TopHit column).
 * Beyond the base capabilities of AgilentFeature, this class deals with the TopHit column.
 * The TopHit can have none, any, or all of the following ids: GenbankPrimate, GenPept, SwissProt.
 * We want to use GenbankPrimate when it is available.  Else, just use the geneName.
 * 
 * @author vu
 */
public class TopFeature extends AgilentFeature {	
	static final String sGB = "GB";
	static final String sGBPRI = "GBPri";
	static final String sGP = "GP";
	static final String sSP = "SP";
	
	private String topHit;
	private String accession;
	
	
	/**
	 * Constructor.  Parses a line representing a single feature and stores as local variables.
	 * @param line One line from an Agilent Pattern File NOT containing a TopHit column
	 */
	public TopFeature(String line) {
		StringTokenizer st = new StringTokenizer(line,"\t");
		int kount = st.countTokens();
		if( kount != 9 ) {
			//big problems here
			System.out.println("kount(" + kount + ") != 9:" + line);
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
			String nine = st.nextToken();
			
			this.setCol(this.s2int(one));
			this.setRow(this.s2int(two));
			this.setName(three);
			this.setSysName(four);
			this.setRefNumber(this.s2int(five));
			this.setControlType(six);
			this.setGeneName(seven);
			this.setTopHit(eight);
			this.setDesc(nine);
			
			this.assignFeatureType();
			if(this.hasGbPri(this.getTopHit())) {
				this.setAccession(TopFeature.parseAccession(this.topHit));
				//System.out.println(this.getAccession() + "\t" + this.topHit);
			} else {
				this.setAccession(this.getGeneName());
			}
		}
	}//end constructor
	
	
	/**
	 * Tests String s to see if it contains a GBPri accession number.
	 * @param s	A String from the TopHit column in an Agilent Pattern.
	 * @return	True if there is a GBPri accession number.
	 */
	private boolean hasGbPri(String s) {
		boolean toReturn = false;
		if(s.toLowerCase().startsWith(TopFeature.sGBPRI.toLowerCase())) {
			toReturn = true;
		} else if(s.toLowerCase().startsWith(TopFeature.sGB.toLowerCase())) {
			toReturn = true;
		} else {
			int iPipe = s.indexOf("|");
			while(iPipe != -1) {
				int lastPipe = iPipe;
				String sub = s.substring(iPipe + 1);
				if(sub.toLowerCase().startsWith(TopFeature.sGBPRI.toLowerCase())) {
					toReturn = true;
				}
				iPipe = s.indexOf("|",(lastPipe+1));
			}
		}
		return toReturn;
	}//end hasGbPri
	
	
	/**
	 * Parses the main accession number.  Acc #'s sometimes have a build number
	 * appended.
	 * @param s	The Accession Number
	 * @return	The parsed Accession Number
	 */
	static String parseAccession(String s) {
		String toReturn;
		String temp;
		int iGb = s.toLowerCase().indexOf(TopFeature.sGB.toLowerCase());		
		int iPipe = s.indexOf("|",iGb);
		int iColon = s.indexOf(":",iGb);
		
		int iNextPipe = s.indexOf("|",(iPipe + 1));
		int iNextColon = s.indexOf(":",(iColon + 1));
		
		if(iPipe != -1) {
			//we know it's using pipes, not colons
			if(iNextPipe == -1) {
				//there are no other pipes, just use the rest of the string
				temp = s.substring((iPipe + 1));
			} else {
				//backtrack off next pipe
				temp = s.substring((iPipe + 1),(iNextPipe - 2));
			}
		} else {
			//it's using colons
			if(iNextColon == -1) {
				//there are no other colons, just use the rest of the string
				temp = s.substring((iColon + 1));
			} else {
				//backtrack off next colon
				temp = s.substring((iColon + 1), (iNextColon - 2));
			}
		}
		
		int iDot = temp.indexOf(".");
		int iDash = temp.indexOf("-");
		if(iDot != -1) {
			//has a dot ending
			toReturn = temp.substring(0,iDot);
		} else if(iDash != -1) {
			//has a dash ending
			toReturn = temp.substring(0,iDash);
		} else {
			//has no ending
			toReturn = temp;
		}
		
		return toReturn;
	}//end parseAccession
	
	
	/**
	 * Counts the number of times char c occurs in String s
	 * @param s
	 * @param c
	 * @return	
	 */
	private int kountChar(String s, char c) {
		int toReturn = 0;
		
		for(int i = 0; i < s.length(); i ++) {
			char at = s.charAt(i);
			if(at == c) {
				toReturn++;
			}
		}
		
		return toReturn;
	}//end kountPipes
	
	
	/**
	 * Converts a String to an int
	 * @param s	String value representing an int
	 * @return	int
	 */
	public int s2int(String s) {
		Integer I = new Integer(s);
		return I.intValue();
	}

	private void setTopHit(String sTopHit) {
		this.topHit = sTopHit;
	}

	public String getTopHit() {
		return topHit;
	}

	private void setAccession(String accession) {
		this.accession = accession;
	}

	public String getAccession() {
		return accession;
	}
}
