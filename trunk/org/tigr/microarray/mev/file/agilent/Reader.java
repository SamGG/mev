package org.tigr.microarray.mev.file.agilent;

/*
 * Created on Jan 21, 2004
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This is a simple utility class that reads a tab-delimited text file into a Vector of Strings
 * where each String is a line from the file.
 * 
 * It exists to deal with blank cells in a spreadsheet.  
 * 
 * Where there are null fields in the file, a String 'replacer' is added as a placeholder.
 * 
 * @author vu
 */
public class Reader {
	//private Vector vNullLine;
	private Vector vLine;
	
	
	public Reader() {
		//do nothing
	}//end constructor()
	
	
	public void readFile(File f) throws IOException {
		this.vLine = this.readLines(f);
	}


	public Vector getVNullLine(String s) {
		Vector vReturn = this.nullifyLines(this.vLine, s);
		return vReturn;
	}
	
	
	public Vector getVLine() {
		return this.vLine;
	}


	/**
	 * This method exists to deal with the StringTokenizer's inability to recognize a
	 * blank cell in a tab-delim file.  StringTokinzer just ignores it, so I wrote this to
	 * replace a blank cell with a 'replacer' String.
	 * @param vLines
	 * @param replacer
	 * @return	Vector of Lines from file where blank cells are replaced with 'replacer'
	 */
	public Vector nullifyLines (Vector vLines, String replacer) {
		Vector vReturn = new Vector();
		
		//loop through the lines
		for(int i=0; i<vLines.size(); i++) {
			String line = (String) vLines.get(i);

			//tokenize this line
			StringTokenizer st = new StringTokenizer(line, "\t", true);
			int kount = st.countTokens();

			StringBuffer sb = new StringBuffer();
			
			//keep track of the last token to compare to the current token
			String last = "";
			String current = null;
			
			//loop through the tokens
			for(int j=0; j<kount; j++) {
				current = st.nextToken();
				if(j == 0) {
					//first token in line
					if(current.equals("\t")) {
						//first cell was a blank cell, so replace with replacer
						sb.append(replacer);
						sb.append(current);
					}
					else {
						sb.append(current);
					}
				}
				else {
					//not first token
					if(j == kount-1) {
						//last token
						if(current.equals("\t")) {
							//last token was blank cell, so replace with replacer
							sb.append(current);
							sb.append(replacer);
						}
						else {
							sb.append(current);
						}
					}
					else {
						//not first or last token
						if(current.equals("\t")) {
							if(last.equals("\t")) {
								//2 tabs in a row, insert null
								sb.append(replacer);
								sb.append(current);
							}
							else {
								//tab here, skip
								sb.append(current);
							}
						}
						else {
							//data here
							sb.append(current);
						}
					}
				}
				last = current;
			}//end j
			vReturn.add(sb.toString());
		}//end i
		
		return vReturn;
	}//end parseLines()


	public Vector readLines(File f) throws IOException {
		Vector vReturn = new Vector();
		String s;

		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);

		while( (s = br.readLine()) != null ) {
			vReturn.add(s);
		}
		fr.close();
		br.close();
		
		return vReturn;
	}
}