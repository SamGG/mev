package org.tigr.microarray.mev.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




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
	private ArrayList<String>columnHeaders;
	
	
	
	

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
		return columnHeaders;
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
			while(patternFound==false) {
				currentLine=reader.readLine();
				headerLinesCount=headerLinesCount+1;
				Matcher m = pattern.matcher(currentLine);
				if(m.find()) {
					patternFound=true;
					
					splitter.init(currentLine);
					while(splitter.hasMoreTokens()) {
						this.columnHeaders.add(splitter.nextToken());
					}
			
					break;
				}
						
			}
			
			if(AgilentFileParser.validate(currentLine)==AgilentFileParser.VALID_AGILENT_FILE) {
				this.isAgilentFileValid=true;
			}else
				this.isAgilentFileValid=false;
			
			//Loop through the file and fill datamatrix
			
			String[][]dataMatrix=new String[lines_in_file-headerLinesCount][getColumnHeaders().size()];
			
			//write a function to capture all file headers
			//write a function that will set the arraylist columnheaders to the predefined list above
			//Read the lines in the file, split the line in to tokens. construct a for loop that would compare
			//actual file headers with required headers, if they match put the token into the data matrix array.
			//If a column in the pre definedlist is not present in the file, just replace it with NA
			
			
			
			
			
			reader.close();
			
		}catch(Exception e) {
			e.printStackTrace();
			this.isAgilentFileValid=false;
		}
		
	
		
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
	
	
	
	
	
	public boolean isAgilentFileValid() {
		return isAgilentFileValid;
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
