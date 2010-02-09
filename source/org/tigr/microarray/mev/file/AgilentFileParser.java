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
	
	private static final int VALID_AGILENT_FILE=1;
	private static final int INVALID_AGILENT_FILE=0;
	private boolean isAgilentFileLoaded=false;
	private ArrayList<String>columnHeaders;
	
	
	
	

	public AgilentFileParser() {
		
	}
	
/**
	Scans the specified header line and returns validity code.
	Validity check 1: "ProbeName" present
	Validity check 2: "LogRatio" present
	Validity check 3: "Row" present --required for meta data, not 
	Validity check 4: "Col" present
		
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

		while (split.hasMoreTokens()) {
			String token = split.nextToken();

			if (token.equalsIgnoreCase(AgilentFileCodes.PROBENAME.toString())) {
				valid1 = true;
			}

			if (token.equalsIgnoreCase(AgilentFileCodes.LOGRATIO.toString())) {
				valid2 = true;
			}

			if (token.equalsIgnoreCase(AgilentFileCodes.ROW.toString()))
				valid3 = true;

			if (token.equalsIgnoreCase(AgilentFileCodes.COLUMN.toString())) {
				valid4 = true;
			}

		}

		if (valid1 && valid2)
			return AgilentFileParser.VALID_AGILENT_FILE;

		else
			return AgilentFileParser.INVALID_AGILENT_FILE;

	}
	
	
	
	public ArrayList<String> getColumnHeaders() {
		return columnHeaders;
	}

	public void setColumnHeaders(ArrayList<String> columnHeaders) {
		this.columnHeaders = columnHeaders;
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
				setAgilentFileLoaded(true);
			}else
				setAgilentFileLoaded(false);
			
			//Loop through the file and fill datamatrix
			
			String[][]dataMatrix=new String[lines_in_file-headerLinesCount][getColumnHeaders().size()];
			
			
			
			
			
			
			
			
			reader.close();
			
		}catch(Exception e) {
			e.printStackTrace();
			setAgilentFileLoaded(false);
		}
		
	
		
	}
	
	public boolean isAgilentFileLoaded() {
		return isAgilentFileLoaded;
	}

	public void setAgilentFileLoaded(boolean isAgilentFileLoaded) {
		this.isAgilentFileLoaded = isAgilentFileLoaded;
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
