package org.tigr.microarray.mev.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tigr.microarray.util.LineCount;



public class AgilentAnnotationFileParser {

	public static final int INVALID_ANNOTATION_FILE = 0;
	public static final int VALID_ANNOTATION_FILE = 1;
	public static final String REF_NUM = "RefNumber";
	public static final String ROW ="Row"; 
	public static final String COLUMN = "Col";
	private String[][]annotationMatrix=new String[1][1];
	private int uniqueIdentifierColumn;
	private ArrayList<String>columnHeaders=new ArrayList<String>();
	private  boolean isAnnotationLoaded=false;
	

	public AgilentAnnotationFileParser() {
		
	}
	
	/**
	 * validates a given annotation file. Checks if the field ReferenceNum, which is the unique identifier is present
	 * @param headerLine
	 * @return INVALID_ANNOTATION_FILE or VALID_ANNOTATION_FILE
	 */
	
	public static int validate(String headerLine) {

		
		StringSplitter split = new StringSplitter('\t');

		split.init(headerLine);

		Pattern pattern=Pattern.compile(AgilentAnnotationFileParser.REF_NUM,Pattern.CASE_INSENSITIVE );
		Matcher m = pattern.matcher(headerLine);
		if(m.find()) {
			System.out.println("valid annotation file");
			return VALID_ANNOTATION_FILE;
		}else {
			System.out.println("invalid annotation file");
			return INVALID_ANNOTATION_FILE;
		}

		
			
	}
	
	
	
	/**
	 * 
	 * @param targetFile
	 */
	
	public void loadAnnotationFile(File targetFile) {
		
		StringSplitter splitter=new StringSplitter('\t');
		String currentLine = new String();
		int rowCount=LineCount.getNumberOfLines(targetFile);
		
		BufferedReader reader = null;
		Pattern pattern=Pattern.compile(AgilentAnnotationFileParser.REF_NUM,Pattern.CASE_INSENSITIVE );
		
		try {
			reader=new BufferedReader(new FileReader(targetFile));	
			currentLine=reader.readLine();
			//First line contains the header
			int result = AgilentAnnotationFileParser.validate(currentLine);
			
			//If file is invalid, no need to read further
			if(result==AgilentAnnotationFileParser.INVALID_ANNOTATION_FILE)
				return;
			
			//Add all column headers to the array list
			columnHeaders=new ArrayList<String>(Arrays.asList(currentLine.split("\t")));
			
			//Gets the column number of the unique identifier
			this.uniqueIdentifierColumn=columnHeaders.indexOf(AgilentAnnotationFileParser.REF_NUM);
			this.annotationMatrix=new String[rowCount-1][columnHeaders.size()];
			int rowIndex=0;
			
			//Read rest of the file
			while((currentLine=reader.readLine())!=null) {
				
				String[] tokens = currentLine.split("\t");
				
				this.annotationMatrix[rowIndex][0]=tokens[getUniqueIdentifierColumn()];
				int colIndex=1;
				for(int index=0; index<getColumnHeaders().size(); index++) {
					if(index!=getUniqueIdentifierColumn()) {
						this.annotationMatrix[rowIndex][colIndex]=tokens[index];
						colIndex=colIndex+1;
					}
				}
				
				
				
				rowIndex=rowIndex+1;
			}
			
			
			
			
			
		}catch(Exception e) {
			
		}
		
			
	}
	
	
	public boolean isAnnotationLoaded() {
		return isAnnotationLoaded;
	}
	
	
	
	
	/**
	 * 
	 * @returns annotation matrix
	 */
	public String[][] getAnnotationMatrix() {
		return this.annotationMatrix;
	}

	
	/**
	 * 
	 * @returns all the header columns in the annotation file
	 */
	public ArrayList<String> getColumnHeaders() {
		return this.columnHeaders;
	}
	
	
	
	
	/**
	 * 
	 * @returns the position of the column containing unique identifiers (ReferenceNum)
	 */
	public int getUniqueIdentifierColumn() {
		return this.uniqueIdentifierColumn;
	}

	
	
	
	
	
}
