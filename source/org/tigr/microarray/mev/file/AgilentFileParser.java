package org.tigr.microarray.mev.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.html.HTMLDocument.Iterator;

import org.tigr.microarray.util.LineCount;




public class AgilentFileParser {
	
	public static final int VALID_AGILENT_FILE=1;
	public static final int INVALID_AGILENT_FILE=0;
	public static final String PROBENAME ="ProbeName"; 
	public static final String RPROCESSEDSIGNAL ="rProcessedSignal";
	public static final String GPROCESSEDSIGNAL ="gProcessedSignal";
	public static final String ROW ="Row"; 
	public static final String COLUMN = "Col";
	public static final String FEATURENUMBER="FeatureNum";
	
	public static final String RMEDIANSIGNAL ="rMedianSignal";
	public static final String GMEDIANSIGNAL ="gMedianSignal";
	public static final String SYSTEMATICNAME ="SystematicName"; 
	public static final String GENENAME = "GeneName"; 
	
	private boolean isAgilentFileValid=false;

	private HashMap<String, Integer>requiredHeaders=new HashMap<String, Integer>();
	private String[][]dataMatrix=new String[1][1];
	
	

	
	public AgilentFileParser() {
		
	}
	
/**
	Scans the specified header line and returns validity code.
	Validity check 1: "FeatureNum" present
	Validity check 2: "gProcessedSignal" present
	Validity check 3: "rProcessedSignal" present
	Validity check 4: "Row" present  
	Validity check 5: "Col" present

		
	@param headerLine The header line contained in the expression data file
	
	@throws FileFormatException
	
	@return validity code
*/
	
	
	public static int validate(String headerLine) {

		ArrayList<String> columnHeaders = new ArrayList<String>();
		StringSplitter split = new StringSplitter('\t');
		split.init(headerLine);

		boolean valid1 = false;
		boolean valid2 = false;
		boolean valid3 = false;
		boolean valid4 = false;
		boolean valid5 = false;
	

		while (split.hasMoreTokens()) {
			String token = split.nextToken();

			if(token.equalsIgnoreCase(AgilentFileParser.FEATURENUMBER)) { //Validity test 1
				valid1=true;
			}
			
			if (token.equalsIgnoreCase(AgilentFileParser.GPROCESSEDSIGNAL)) {//Validity test 2
				valid2 = true;
			}
			
			if (token.equalsIgnoreCase(AgilentFileParser.RPROCESSEDSIGNAL)) { //Validity test 3
				valid3 = true;
			}

			if (token.equalsIgnoreCase(AgilentFileParser.ROW))//Validity test 4
				valid4 = true;

			if (token.equalsIgnoreCase(AgilentFileParser.COLUMN)) { //Validity test 5
				valid5 = true;
			}
			
			

		}

		//A file must at the bare minimum satisfy all these five conditions
		if (valid1 && valid2 && valid3 &&valid4 && valid5) 
			return AgilentFileParser.VALID_AGILENT_FILE;

		else
			return AgilentFileParser.INVALID_AGILENT_FILE;

	}
	

	
	/**
	 * Returns a bare minimum list of headers which must be present in the feature file 
	 * 
	 * @return
	 */
	public HashMap<String, Integer>getRequiredHeaders(){
		return this.requiredHeaders;
	}
	
	
	/**
	 * Sets the required headers to the predefined list of columns as described in public static instance fields
	 * 
	 */
	public void setRequiredHeaders() {
		this.requiredHeaders.put(AgilentFileParser.FEATURENUMBER, new Integer(0) );
		this.requiredHeaders.put(AgilentFileParser.ROW, new Integer(1) );
		this.requiredHeaders.put(AgilentFileParser.COLUMN, new Integer(2) );
		this.requiredHeaders.put(AgilentFileParser.PROBENAME,new Integer(3) );
		this.requiredHeaders.put(AgilentFileParser.GENENAME, new Integer(4));
		this.requiredHeaders.put(AgilentFileParser.SYSTEMATICNAME,new Integer(5) );
		this.requiredHeaders.put(AgilentFileParser.GPROCESSEDSIGNAL,new Integer(6) );
		this.requiredHeaders.put(AgilentFileParser.RPROCESSEDSIGNAL, new Integer(7));
		this.requiredHeaders.put(AgilentFileParser.GMEDIANSIGNAL,new Integer(8) );
		this.requiredHeaders.put(AgilentFileParser.RMEDIANSIGNAL, new Integer(9));
	}
	

	
	
	
	
	public void loadFile(File targetFile) {
		
		StringSplitter splitter=new StringSplitter('\t');
		String currentLine = new String();
		BufferedReader reader = null;
		Pattern pattern=Pattern.compile("FEATURE",Pattern.CASE_INSENSITIVE );
		//Get number of lines in the file
		int lines_in_file=LineCount.getNumberOfLines(targetFile);
		int headerLinesCount=0;
		
		try {
			reader=new BufferedReader(new FileReader(targetFile));
			boolean patternFound=false;
		
		//Looks for a line in the file starting with FEATURE which contains column headers	
			search:
			while(patternFound==false) {
				currentLine=reader.readLine();
				headerLinesCount=headerLinesCount+1;
				Matcher m = pattern.matcher(currentLine);
				if(m.find()) {
					patternFound=true;
					
					//Once header row is found, check if file has all required columns in it. If not, no point in reading further
					if(AgilentFileParser.validate(currentLine)==AgilentFileParser.VALID_AGILENT_FILE) {
						this.isAgilentFileValid=true;
					}else {
						this.isAgilentFileValid=false;
						return;
					}
					
				//Store all the header fields in the file
					ArrayList<String> columnHeaders=new ArrayList<String>(Arrays.asList(currentLine.split("\t")));
					
					//All Agilent files *mostly* have Row, Column, FeatureNum, rProcessedSignal and gProcessedSignal
					//However required headers hash map has a couple more headers. If these headers are not in the file, no big deal..just remove them
					//from the list of requiredHeaders. If these headers are present in the file, update the position of the header in required headers
					//hashmap
					for(int index=0; index<getRequiredHeaders().size(); index++) {
						String key=getRequiredHeaders().keySet().iterator().next();
						pattern=Pattern.compile(key,Pattern.CASE_INSENSITIVE );
						 m = pattern.matcher(currentLine);
						
						if(m.find()) {
							patternFound=true;
							requiredHeaders.put(key, new Integer(columnHeaders.indexOf(key)));
						}else
							requiredHeaders.remove(index);
							
					}
				
					break search;		
				}
						
			}
			
			
			//Loop through rest of the file and fill data matrix
			
			this.dataMatrix=new String[lines_in_file-headerLinesCount][getRequiredHeaders().size()];
			
			//Read the lines in the file, split the line in to array of tokens. Get the position of required 
			//headers from the hashmap, fetch from token array and fill in data matrix  
			
			int rowIndex=0;
			java.util.Iterator<String> keyIterator=getRequiredHeaders().keySet().iterator();
			while ((currentLine = reader.readLine()) != null) {
				int columnIndex = 0;
				String[] tokens = currentLine.split("\t");
				
				if (getRequiredHeaders().keySet().contains(
						AgilentFileParser.FEATURENUMBER)) {
					this.dataMatrix[rowIndex][0] = tokens[getRequiredHeaders()
							.get(AgilentFileParser.FEATURENUMBER).intValue()];
					++columnIndex;
				}

				
				if (getRequiredHeaders().keySet().contains(
						AgilentFileParser.ROW)) {
					this.dataMatrix[rowIndex][columnIndex++] = tokens[getRequiredHeaders()
							.get(AgilentFileParser.ROW).intValue()];
				}
				
				if (getRequiredHeaders().keySet().contains(
						AgilentFileParser.COLUMN)) {
					this.dataMatrix[rowIndex][columnIndex++] = tokens[getRequiredHeaders()
							.get(AgilentFileParser.COLUMN).intValue()];
				}
				
				if (getRequiredHeaders().keySet().contains(
						AgilentFileParser.PROBENAME)) {
					this.dataMatrix[rowIndex][columnIndex++] = tokens[getRequiredHeaders()
							.get(AgilentFileParser.PROBENAME).intValue()];
				}
				if (getRequiredHeaders().keySet().contains(
						AgilentFileParser.GENENAME)) {
					this.dataMatrix[rowIndex][columnIndex++] = tokens[getRequiredHeaders()
							.get(AgilentFileParser.GENENAME).intValue()];
				}
				
				if (getRequiredHeaders().keySet().contains(
						AgilentFileParser.RPROCESSEDSIGNAL)) {
					this.dataMatrix[rowIndex][columnIndex++] = tokens[getRequiredHeaders()
							.get(AgilentFileParser.RPROCESSEDSIGNAL).intValue()];
				}
				
				if (getRequiredHeaders().keySet().contains(
						AgilentFileParser.GPROCESSEDSIGNAL)) {
					this.dataMatrix[rowIndex][columnIndex++] = tokens[getRequiredHeaders()
							.get(AgilentFileParser.GPROCESSEDSIGNAL).intValue()];
				}
				
				if (getRequiredHeaders().keySet().contains(
						AgilentFileParser.RMEDIANSIGNAL)) {
					this.dataMatrix[rowIndex][columnIndex++] = tokens[getRequiredHeaders()
							.get(AgilentFileParser.RMEDIANSIGNAL).intValue()];
				}
				if (getRequiredHeaders().keySet().contains(
						AgilentFileParser.GMEDIANSIGNAL)) {
					this.dataMatrix[rowIndex][columnIndex++] = tokens[getRequiredHeaders()
							.get(AgilentFileParser.GMEDIANSIGNAL).intValue()];
				}
				
				rowIndex=rowIndex+1;
			
			}
				
		
			reader.close();
			
		}catch(Exception e) {
			e.printStackTrace();
			this.isAgilentFileValid=false;
		}
		
	
		
	}
	
	

	public String[][] getDataMatrix() {
		return this.dataMatrix;
	}

	
	
	
	public boolean isAgilentFileValid() {
		return this.isAgilentFileValid;
	}

	

	

	
	
	
	
	
	
	
	
	
}
