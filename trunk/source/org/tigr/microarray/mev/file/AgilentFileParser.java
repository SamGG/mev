package org.tigr.microarray.mev.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



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

	private ArrayList<String>requiredHeaders=new ArrayList<String>();
	private String[][]dataMatrix=new String[1][1];
	
	

	
	public AgilentFileParser() {
		initializeHeaders();
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
		if (valid1 && valid2 && valid3 &&valid4 && valid5) {
			return AgilentFileParser.VALID_AGILENT_FILE;
		}

		else {
			return AgilentFileParser.INVALID_AGILENT_FILE;
		}

	}
	

	
	/**
	 * Returns a bare minimum list of headers which must be present in the feature file 
	 * 
	 * @return
	 */
	public ArrayList<String>getRequiredHeaders(){
		return this.requiredHeaders;
	}
	
	
	/**
	 * Sets the required headers to the predefined list of columns as described in public static instance fields
	 * 
	 */
	public void initializeHeaders() {
		this.requiredHeaders.add(AgilentFileParser.FEATURENUMBER );
		this.requiredHeaders.add(AgilentFileParser.ROW);
		this.requiredHeaders.add(AgilentFileParser.COLUMN);
		this.requiredHeaders.add(AgilentFileParser.PROBENAME );
		this.requiredHeaders.add(AgilentFileParser.GENENAME);
		this.requiredHeaders.add(AgilentFileParser.SYSTEMATICNAME);
		this.requiredHeaders.add(AgilentFileParser.GPROCESSEDSIGNAL);
		this.requiredHeaders.add(AgilentFileParser.RPROCESSEDSIGNAL);
		this.requiredHeaders.add(AgilentFileParser.GMEDIANSIGNAL);
		this.requiredHeaders.add(AgilentFileParser.RMEDIANSIGNAL);
	}
	

	public void setHeaderPositions(ArrayList<String>columnHeaders, String headerLine) {
		
		Pattern pattern=null;
		Matcher m =null;
		int colIndex=0;
		
		for(int index=0; index<columnHeaders.size(); index++) {
			String key=columnHeaders.get(index);
			
			pattern=Pattern.compile(key,Pattern.CASE_INSENSITIVE );
			m = pattern.matcher(headerLine);
			
			if(m.find()) {
				//do nothing
			}else
				requiredHeaders.remove(key);
				
		}
	}
	
	
	
	public void loadFile(File targetFile) {
		
		StringSplitter splitter=new StringSplitter('\t');
		String currentLine = new String();
		BufferedReader reader = null;
		Pattern pattern=Pattern.compile("FEATURES",Pattern.CASE_INSENSITIVE );
		//Get number of lines in the file
		int lines_in_file=LineCount.getNumberOfLines(targetFile);
		int headerLinesCount=0;
		ArrayList<String> columnHeaders=new ArrayList<String>();
		
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
					
					pattern=Pattern.compile("FeatureNum",Pattern.CASE_INSENSITIVE );
					m = pattern.matcher(currentLine);
					
					if(m.find()) {
					
					
					//Once header row is found, check if file has all required columns in it. If not, no point in reading further
					if(AgilentFileParser.validate(currentLine)==AgilentFileParser.VALID_AGILENT_FILE) {
						this.isAgilentFileValid=true;
					}else {
						this.isAgilentFileValid=false;
						return;
					}
					
				//Store all the header fields in the file
					columnHeaders=new ArrayList<String>(Arrays.asList(currentLine.split("\t")));
					
					//All Agilent files *mostly* have Row, Column, FeatureNum, rProcessedSignal and gProcessedSignal
					//However required headers hash map has a couple more headers. If these headers are not in the file, no big deal..just remove them
					//from the list of requiredHeaders. If these headers are present in the file, update the position of the header in required headers
					//hashmap
					setHeaderPositions(columnHeaders, currentLine);
				
					patternFound=true;
					break search;		
				}
				}
						
			}
			
			
			//Loop through rest of the file and fill data matrix
			
			this.dataMatrix=new String[lines_in_file-headerLinesCount][getRequiredHeaders().size()];
			
			//Read the lines in the file, split the line in to array of tokens. Get the position of required 
			//headers from the hashmap, fetch from token array and fill in data matrix  
			
			int rowIndex=0;
			
			while ((currentLine = reader.readLine()) != null) {
				int columnIndex = 0;
				String[] tokens = currentLine.split("\t");
				
				if (getRequiredHeaders().contains(
						AgilentFileParser.FEATURENUMBER)) {
					this.dataMatrix[rowIndex][0] = tokens[columnHeaders.indexOf(AgilentFileParser.FEATURENUMBER)];
					++columnIndex;
				}

				
				if (getRequiredHeaders().contains(
						AgilentFileParser.ROW)) {
					this.dataMatrix[rowIndex][columnIndex++] = tokens[columnHeaders.indexOf(AgilentFileParser.ROW)];
				}
				
				if (getRequiredHeaders().contains(
						AgilentFileParser.COLUMN)) {
					this.dataMatrix[rowIndex][columnIndex++] = tokens[columnHeaders.indexOf(AgilentFileParser.COLUMN)];
				}
				
				if (getRequiredHeaders().contains(
						AgilentFileParser.PROBENAME)) {
					this.dataMatrix[rowIndex][columnIndex++] = tokens[columnHeaders.indexOf(AgilentFileParser.PROBENAME)];
				}
				if (getRequiredHeaders().contains(
						AgilentFileParser.GENENAME)) {
					this.dataMatrix[rowIndex][columnIndex++] = tokens[columnHeaders.indexOf(AgilentFileParser.GENENAME)];
				}
				if (getRequiredHeaders().contains(
						AgilentFileParser.SYSTEMATICNAME)) {
					this.dataMatrix[rowIndex][columnIndex++] = tokens[columnHeaders.indexOf(AgilentFileParser.SYSTEMATICNAME)];
				}
				
				if (getRequiredHeaders().contains(
						AgilentFileParser.RPROCESSEDSIGNAL)) {
					this.dataMatrix[rowIndex][columnIndex++] = tokens[columnHeaders.indexOf(AgilentFileParser.RPROCESSEDSIGNAL)];
				}
				
				if (getRequiredHeaders().contains(
						AgilentFileParser.GPROCESSEDSIGNAL)) {
					this.dataMatrix[rowIndex][columnIndex++] = tokens[columnHeaders.indexOf(AgilentFileParser.GPROCESSEDSIGNAL)];
				}
				
				if (getRequiredHeaders().contains(
						AgilentFileParser.RMEDIANSIGNAL)) {
					this.dataMatrix[rowIndex][columnIndex++] = tokens[columnHeaders.indexOf(AgilentFileParser.RMEDIANSIGNAL)];
				}
				if (getRequiredHeaders().contains(
						AgilentFileParser.GMEDIANSIGNAL)) {
					this.dataMatrix[rowIndex][columnIndex++] = tokens[columnHeaders.indexOf(AgilentFileParser.GMEDIANSIGNAL)];
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
