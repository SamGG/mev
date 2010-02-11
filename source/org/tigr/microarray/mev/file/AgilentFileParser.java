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




public class AgilentFileParser {
	
	public static final int VALID_AGILENT_FILE=1;
	public static final int INVALID_AGILENT_FILE=0;
	public static final String PROBENAME ="ProbeName"; 
	public static final String RPROCESSEDSIGNAL ="rProcessedSignal";
	public static final String GPROCESSEDSIGNAL ="gProcessedSignal";
	public static final String ROW ="Row"; 
	public static final String COLUMN = "Col";
	
	public static final String RMEDIANSIGNAL ="rMedianSignal";
	public static final String GMEDIANSIGNAL ="gMedianSignal";
	public static final String SYSTEMATICNAME ="SystematicName"; 
	public static final String GENENAME = "GeneName"; 
	
	private boolean isAgilentFileValid=false;
	private ArrayList<String>columnHeaders=new ArrayList<String>();
	private HashMap<String, Integer>requiredHeaders=new HashMap<String, Integer>();
	private String[][]dataMatrix=new String[1][1];
	
	

	
	public AgilentFileParser() {
		
	}
	
/**
	Scans the specified header line and returns validity code.
	Validity check 1: "ProbeName" present
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

			if (token.equalsIgnoreCase(AgilentFileCodes.PROBENAME.toString())) {
				valid1 = true;
				
			}

			if (token.equalsIgnoreCase(AgilentFileParser.GPROCESSEDSIGNAL)) {
				valid2 = true;
			}
			
			if (token.equalsIgnoreCase(AgilentFileParser.RPROCESSEDSIGNAL)) {
				valid3 = true;
			}

			if (token.equalsIgnoreCase(AgilentFileCodes.ROW.toString()))
				valid4 = true;

			if (token.equalsIgnoreCase(AgilentFileCodes.COLUMN.toString())) {
				valid5 = true;
			}

		}

		//A file must at the bare minimum satisfy all these five conditions
		if (valid1 && valid2 && valid3 &&valid4 && valid5) 
			return AgilentFileParser.VALID_AGILENT_FILE;

		else
			return AgilentFileParser.INVALID_AGILENT_FILE;

	}
	
	
	
	public ArrayList<String> getColumnHeaders() {
		return this.columnHeaders;
	}
	
	
	/**
	 * 
	 * @param columnName
	 * @return returns the position of desired column in the file
	 */
	public int getColumn(String columnName) {
		
		int columnSize=getColumnHeaders().size();
		
		for(int index=0; index<columnSize; index++) {
			if(columnName.equalsIgnoreCase(getColumnHeaders().get(index)))
				return index;
		}
		
		
		
		return -1;
	}
	/**
	 * 
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
		this.requiredHeaders.put(AgilentFileParser.ROW, new Integer(0) );
		this.requiredHeaders.put(AgilentFileParser.COLUMN, new Integer(1) );
		this.requiredHeaders.put(AgilentFileParser.PROBENAME,new Integer(2) );
		this.requiredHeaders.put(AgilentFileParser.GENENAME, new Integer(3));
		this.requiredHeaders.put(AgilentFileParser.SYSTEMATICNAME, new Integer(4));
		this.requiredHeaders.put(AgilentFileParser.GPROCESSEDSIGNAL, new Integer(5));
		this.requiredHeaders.put(AgilentFileParser.RPROCESSEDSIGNAL, new Integer(6));
		this.requiredHeaders.put(AgilentFileParser.GMEDIANSIGNAL, new Integer(7));
		this.requiredHeaders.put(AgilentFileParser.RMEDIANSIGNAL, new Integer(8));
	}
	

	
	
	
	
	public void loadFile(File targetFile) {
		
		StringSplitter splitter=new StringSplitter('\t');
		String currentLine = new String();
		BufferedReader reader = null;
		Pattern pattern=Pattern.compile("FEATURE",Pattern.CASE_INSENSITIVE );
		//Get number of lines in the file
		int lines_in_file=getNumberOfLines(targetFile);
		int headerLinesCount=0;
		
		try {
			reader=new BufferedReader(new FileReader(targetFile));
			boolean patternFound=false;
		
		//Looks for the line in the file starting with FEATURE which contains column headers	
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
					this.columnHeaders=new ArrayList<String>(Arrays.asList(currentLine.split("\t")));
					
					//All Agilent files *mostly* have Row, Column, Probename, rProcessedSignal and gProcessedSignal
					//However required headers hashmap has a couple more headers. If these headers are not in the file, no big deal..just remove them
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
			
			
			//Loop through the file and fill datamatrix
			
			this.dataMatrix=new String[lines_in_file-headerLinesCount][getRequiredHeaders().size()];
			
			//Read the lines in the file, split the line in to tokens. Get the position of required 
			//headers from the hashmap, fetch from token array and fill in datamatrix  
			
			int rowIndex=0;
			java.util.Iterator<String> keyIterator=getRequiredHeaders().keySet().iterator();
			while ((currentLine = reader.readLine()) != null) {
				int columnIndex = 0;
				String[] tokens = currentLine.split("\t");

				if (getRequiredHeaders().keySet().contains(
						AgilentFileParser.COLUMN)) {
					this.dataMatrix[rowIndex][++columnIndex] = tokens[getRequiredHeaders()
							.get(AgilentFileParser.COLUMN).intValue()];
				}
				if (getRequiredHeaders().keySet().contains(
						AgilentFileParser.ROW)) {
					this.dataMatrix[rowIndex][++columnIndex] = tokens[getRequiredHeaders()
							.get(AgilentFileParser.ROW).intValue()];
				}
				if (getRequiredHeaders().keySet().contains(
						AgilentFileParser.PROBENAME)) {
					this.dataMatrix[rowIndex][++columnIndex] = tokens[getRequiredHeaders()
							.get(AgilentFileParser.PROBENAME).intValue()];
				}
				if (getRequiredHeaders().keySet().contains(
						AgilentFileParser.GENENAME)) {
					this.dataMatrix[rowIndex][++columnIndex] = tokens[getRequiredHeaders()
							.get(AgilentFileParser.GENENAME).intValue()];
				}
				if (getRequiredHeaders().keySet().contains(
						AgilentFileParser.GPROCESSEDSIGNAL)) {
					this.dataMatrix[rowIndex][++columnIndex] = tokens[getRequiredHeaders()
							.get(AgilentFileParser.GPROCESSEDSIGNAL).intValue()];
				}
				if (getRequiredHeaders().keySet().contains(
						AgilentFileParser.RPROCESSEDSIGNAL)) {
					this.dataMatrix[rowIndex][++columnIndex] = tokens[getRequiredHeaders()
							.get(AgilentFileParser.RPROCESSEDSIGNAL).intValue()];
				}
				if (getRequiredHeaders().keySet().contains(
						AgilentFileParser.GMEDIANSIGNAL)) {
					this.dataMatrix[rowIndex][++columnIndex] = tokens[getRequiredHeaders()
							.get(AgilentFileParser.GMEDIANSIGNAL).intValue()];
				}
				if (getRequiredHeaders().keySet().contains(
						AgilentFileParser.RMEDIANSIGNAL)) {
					this.dataMatrix[rowIndex][++columnIndex] = tokens[getRequiredHeaders()
							.get(AgilentFileParser.RMEDIANSIGNAL).intValue()];
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

	

	/**
	 * 
	 * @param targetFile
	 * @return Number of lines in the file 
	 */
	public int getNumberOfLines(File targetFile) {
		int numberOfLines = 0;
		LineNumberReader lineCounter = null;
		try {
			lineCounter = new LineNumberReader(new FileReader(targetFile));
			while ((lineCounter.readLine()) != null) { 
				continue;
			}
			numberOfLines = lineCounter.getLineNumber();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return numberOfLines;
	}

	
	private enum AgilentFileCodes {

		PROBENAME ("ProbeName"), 
		LOGRATIO ("LogRatio"), 
		SYSTEMATICNAME ("SystematicName"), 
		GENENAME ("GeneName"), 
		ROW ("Row"), 
		COLUMN ("Col");

		private String description;

		private AgilentFileCodes(String description) {
			this.description = description;
		}

		private static boolean checkHeaderByName(String headerName) {

			for (AgilentFileCodes codes : AgilentFileCodes.values())
				if (codes.description.equalsIgnoreCase(headerName))
					return true;
			return false;

		}

	}
	
	
	
	
	
	
	
	
}
