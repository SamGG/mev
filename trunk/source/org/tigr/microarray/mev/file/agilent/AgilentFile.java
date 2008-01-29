/*
 * Created on Jan 21, 2004
 */
package org.tigr.microarray.mev.file.agilent;

import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Object representing an Agilent Pattern File.
 * 
 * @author vu
 */
public class AgilentFile {
	static final int EIGHT_COL = 8;
	static final int NINE_COL = 9;
	
	private int patternType;
	//private Vector vLine;
	private Vector vAgilentFeature;
	private File file;
	
	
	/**
	 * Default Constructer
	 * 
	 * @param vLine	Vector of Strings where each String is a line in the file.
	 */	
	public AgilentFile(Vector vLine) throws UnrecognizedPatternException {
		//see if this pattern contains a TopHit column
		String header = (String) vLine.elementAt(0);
		this.patternType = this.characterizePattern(header);
		
		//create the Feature objects and add them to vFeature vector
		setVAgilentFeature(new Vector());
		
		//for(int i = 1; i < 50; i ++) {
		for(int i = 1; i < vLine.size(); i ++) {
			String line = (String) vLine.elementAt(i);
			if(this.patternType == AgilentFile.EIGHT_COL) {
				EightFeature ef = new EightFeature(line);
				this.getVAgilentFeature().add(ef);
			} else if(this.patternType == AgilentFile.NINE_COL) {
				TopFeature tf = new TopFeature(line);
				this.getVAgilentFeature().add(tf);
			}
		}//end i
	}//end constructor
	
	
	/**
	 * By reading the header line of the pattern, we should be able to determine what kind of
	 * Feature subclass to use to represent the features of this pattern.
	 * 
	 * For the sake of saving time, I am going to dangerously assume that all patterns with a
	 * given number of columns will be consistent with all others containing that given number
	 * of columns.  In other words, all patterns with 8 columns will contain exactly the same 
	 * columns in exactly the same order.  Moreover, all patterns with 9 columns will contain 
	 * the same 9 columns in the same order.
	 * 
	 * @param header The first line in the pattern containing column headings
	 */
	public int characterizePattern(String header) throws UnrecognizedPatternException {
		StringTokenizer st = new StringTokenizer(header, "\t");
		int kount = st.countTokens();
		if(kount == 8) {
			return AgilentFile.EIGHT_COL;
		} else if(kount == 9) {
			return AgilentFile.NINE_COL;
		} else {
			//some new kind, throw error
			throw new UnrecognizedPatternException(header + " unrecognized");
		}
	}

	public void setPatternType(int patternType) {
		this.patternType = patternType;
	}
	public int getPatternType() {
		return patternType;
	}
	public String getPatternName() {
		return this.file.getName();
	}

	public void setVAgilentFeature(Vector vFeature) {
		this.vAgilentFeature = vFeature;
	}

	public Vector getVAgilentFeature() {
		return vAgilentFeature;
	}
}//end class