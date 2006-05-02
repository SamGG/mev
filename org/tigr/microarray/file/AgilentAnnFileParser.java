/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: AgilentAnnFileParser.java,v $
 * $Revision: 1.3 $
 * $Date: 2006-05-02 20:52:47 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.file;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.StringReader;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.tigr.microarray.file.AnnFileParser;
import org.tigr.microarray.file.FieldNotFoundException;
import org.tigr.microarray.file.MevFileParser;

import org.tigr.microarray.mev.file.agilent.MeVotation;
import org.tigr.microarray.mev.file.agilent.UnrecognizedPatternException;

/**
	Parses and stores annotation (.ann, .dat, .txt) file data
	
	@author aisaeed
	@version "1.2, 3 June 2003"
*/

/*
	To do:
	
	1. Add support for duplicate UID checking in validate(java.io.File).
*/

public class AgilentAnnFileParser {
	
	public static final int INVALID_FILE = 0;
	public static final int ANN_FILE = 1;
	
	public static final String UNIQUE_ID_STRING = "UID";
	
	private Vector columnHeaders;
	private Vector rawLines;
	private IntVector dataLinesMap;
	
	private boolean annFileLoaded;
	
	
	/**
		Default and sole constructor
	*/
	public AgilentAnnFileParser() {
		//
	}
	
	
	/**
		Displays a JFileChooser with an ann file filter. The default directory 
		is <i>user.dir</i>.
		
		@param dialogParent Parent component of the JFileChooser
		
		@return The selected ann file
	*/
	public static File selectFile(Component dialogParent) {
		return selectFile(new File(System.getProperty("user.dir")), dialogParent);
	}
	
	/**
		Displays a JFileChooser with an ann file filter that opens to a specified 
		directory.
		
		@param defaultDirectory The default directory for the JFileChooser to 
		open to
		
		@param dialogParent Parent component of the JFileChooser
		
		@return The selected ann file
	*/
	public static File selectFile(File defaultDirectory, Component dialogParent) {
		
		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
		chooser.setDialogTitle("Select an annotation file");
		chooser.setCurrentDirectory(defaultDirectory);
		chooser.setMultiSelectionEnabled(false);
		chooser.addChoosableFileFilter(new FileFilter() {
			public boolean accept(File f) {
				String extension = "";
				if (f.isDirectory()) return true;
				
				if (f.getName().endsWith(".txt")) return true;
				else return false;
			}
			
			public String getDescription() {
				return "MeV Annotation Files (*.txt)";
			}
			
		});
		
		if (chooser.showOpenDialog(dialogParent) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		} else {
			return null;
		}
	}
	
	
	/**
		Scans the specified file and returns filetype/validity code.
		
		<p> Duplicate UID check not yet implemented.
		
		@param targetFile The ann file to validate
		
		@throws FileFormatException
		
		@return The filetype/validity code
	*/
	public static int validate(File targetFile) {
		
		IntVector dataLinesMap = new IntVector();
		Vector rawLines = new Vector();
		Vector columnHeaders = new Vector();
		
		String currentLine = new String();
		BufferedReader reader = null;
		boolean readHeaders = false;
		
		boolean valid1 = false; // Has a header containing UNIQUE_ID_STRING
		boolean valid2 = true; // No duplicate header fields
		boolean valid3 = false; // Dataset contains at least one row
		//boolean valid4 = false; // No duplicate UID values in dataset
		boolean valid4 = true; // Just until it's implemented...
		
		String s = null;
		try {
			MeVotation m = new MeVotation();
			m.parseAgilentPattern(targetFile, "null");
			s = m.getFileString();
		} catch (UnrecognizedPatternException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			//reader = new BufferedReader(new FileReader(targetFile));
			reader = new BufferedReader(new StringReader(s));
			for (int lineCount = 0; ((currentLine = reader.readLine()) != null); lineCount++) {
				rawLines.add(currentLine);
				if (! currentLine.startsWith("#")) { // Non-comment line
					if (! readHeaders) { // Read/load the column headers
						readHeaders = true;
						StringTokenizer st = new StringTokenizer(currentLine, "\t");
						while (st.hasMoreTokens()) {
							String token = st.nextToken();
							
							if (token.equals(MevFileParser.UNIQUE_ID_STRING)) { // Validity test 1
								valid1 = true;
							}
							
							for (int i = 0; i < columnHeaders.size(); i++) { // Validity test 2
								String headerValue = (String) columnHeaders.elementAt(i);
								if (token.equals(headerValue)) {
									valid2 = false;
									return MevFileParser.INVALID_FILE;
								}
							}
							
							columnHeaders.add(token);
						}
						
					} else {
						dataLinesMap.add(lineCount);
					}
				}
			}
			
			if (dataLinesMap.size() > 0) { // Validity test 3
				valid3 = true;
			}

		} catch (IOException ioe) {
			return MevFileParser.INVALID_FILE;
		}
		
		if (valid1 && valid2 && valid3 && valid4) {
			return MevFileParser.MEV_FILE;
		} else {
			return MevFileParser.INVALID_FILE;
		}
	}
	
																				
	/**
		Reads the specified ann file, then instantiates and populates the 
		appropriate data objects. <code>isAnnFileLoaded</code> will return true 
		if this method was successful in loading the ann file.
		
		@param targetFile The ann file to load
	*/
	public void loadFile(File targetFile) {
		File f = targetFile;
		
		dataLinesMap = new IntVector();
		rawLines = new Vector();
		columnHeaders = new Vector();
		
		String currentLine = new String();
		BufferedReader reader = null;
		boolean readHeaders = false;
		
		String s = null;
		try {
			MeVotation m = new MeVotation();
			m.parseAgilentPattern(f, "null");
			s = m.getFileString();
		} catch (UnrecognizedPatternException e) {
			//would like to ask for different file here, but can't get it to work
			e.printStackTrace();
			annFileLoaded = false;
			return;
		} catch (IOException e) {
			e.printStackTrace();
			annFileLoaded = false;
			return;
		}
		
		try {
			//reader = new BufferedReader(new FileReader(targetFile));
			reader = new BufferedReader(new StringReader(s));
			for (int lineCount = 0; ((currentLine = reader.readLine()) != null); lineCount++) {
				rawLines.add(currentLine);
				if (! currentLine.startsWith("#")) { // Non-comment line
					if (! readHeaders) { // Read/load the column headers
						readHeaders = true;
						StringTokenizer st = new StringTokenizer(currentLine, "\t");
						while (st.hasMoreTokens()) {
							columnHeaders.add(st.nextToken());
						}
					} else {
						dataLinesMap.add(lineCount);
					}
				}
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
			annFileLoaded = false;
			return;
		}
		
		annFileLoaded = true;
	}
	
	/**
		Returns true if the <code>loadFile</code> method was successful.
		
		@return The file load status
	*/
	public boolean isAnnFileLoaded() {
		return annFileLoaded;
	}
	
	/**
		Returns a Vector containing the required row of column headers. Each 
		element in the Vector is one of the tab-delimited tokens from the first 
		non-comment line in the mev file.
		
		@return The Vector of column headers
	*/
	public Vector getColumnHeaders() {
		return columnHeaders;
	}
	
	/**
		Returns a Vector containing the fields in the target column. All 
		comment lines will be ignored.
		
		@param targetColumn The index of the target column; valid values range 
		from 0 to n-1, where n is the number of columns in the ann file.
		
		@return The Vector of column data
	*/
	public Vector getColumnAt(int targetColumn) {
		return getColumnAt(targetColumn, false);
	}
	
	/**
		Returns a Vector containing the fields and an optional header in the 
		target column. If requested, the first element of the Vector will be the 
		column header value. All comment lines will be ignored.
		
		@param targetColumn The index of the target column; valid values range 
		from 0 to n-1, where n is the number of columns in the ann file.
		
		@param withHeaders If true, the first element in the return Vector will 
		be the column header for the target column.
		
		@return The Vector of column data
	*/
	public Vector getColumnAt(int targetColumn, boolean withHeaders) {
		
		Vector columnVector = new Vector(dataLinesMap.size() + (withHeaders ? 1 : 0));
		
		if ((targetColumn >= columnHeaders.size()) || (targetColumn < 0)) {
			throw new IndexOutOfBoundsException("Column Index out of bounds.");
		}
		
		if (withHeaders) columnVector.add(columnHeaders.elementAt(targetColumn));
		
		for (int i = 0; i < dataLinesMap.size(); i++) {
			StringTokenizer st = new StringTokenizer(getElementAtIndex(i));
			for (int j = 0; j < targetColumn; j++) {
				st.nextToken();
			}
			
			columnVector.add(st.nextToken());
		}
		
		return columnVector;
	}
	
	/**
		Returns a Vector containing the fields in the column which is 
		identified by the specified column header. All comment lines will be 
		ignored.
		
		@param columnName The column header of the target column
		
		@throws FieldNotFoundException
		
		@return The Vector of column data. If the specified column header is not 
		found, the return Vector will be null.
	*/
	public Vector getColumnNamed(String columnName) throws FieldNotFoundException {
		return getColumnNamed(columnName, false);
	}
	
	/**
		Returns a Vector containing the fields and an optional header in the 
		column which is identified by the specified column header. If requested, 
		the first element of the Vector will be the column header value. All 
		comment lines will be ignored.
		
		@param columnName The column header of the target column
		
		@param withHeaders If true, the first element in the return Vector will 
		be the column header for the target column.
		
		@throws FieldNotFoundException
		
		@return The Vector of column data.
	*/
	public Vector getColumnNamed(String columnName, boolean withHeaders) throws FieldNotFoundException {
		
		Vector columnHeaders = getColumnHeaders();
		
		if (columnHeaders.contains(columnName)) {
			return getColumnAt(columnHeaders.indexOf(columnName), withHeaders);
		} else {
			throw new FieldNotFoundException("Field " + columnName + " not found.");
		}
	}
	
	/**
		Returns the line from the ann file at the specified index.
		
		@param rawTargetline The index of the target line to be retrieved.
		
		@return The String containing the target line of text, as it appears in 
		the ann file. The trailing newline character, <code>\n</code>, if 
		present, is omitted.
	*/
	public String getLineAt(int rawTargetLine) {
		return (String) rawLines.elementAt(rawTargetLine);
	}
	
	/**
		Returns the spot element line from the ann file at the specified index. 
		The index should refer to the position of the element in the ann file, 
		such that an index of 0 refers to the first record in the file, an 
		index of 1 refers to the second record in the file, and so forth. The 
		header row and all comment lines do not count towards this index.
		
		@param rawTargetline The index of the target element to be retrieved.
		
		@return The String encapsulating the target element, as it appears in 
		the ann file. The trailing newline character, <code>\n</code>, if 
		present, is omitted.
	*/
	public String getElementAtIndex(int index) {
		return getLineAt(dataLinesMap.intElementAt(index));
	}
	
	/**
		Returns the record from the ann file that has a <i>UID</i> that 
		matches the specified id value. If there are multiple matches, only 
		the first matched record will be returned.
		
		<p> Note: There should not be multiple records with the same 
		<i>UID</i>, as defined in the annotation file format description.
		
		@param id The <i>id</i> of the target record to be retrieved.
		
		@throws FieldNotFoundException
		
		@return The String encapsulating the target record, as it appears in 
		the ann file. The trailing newline character, <code>\n</code>, if 
		present, is omitted.
	*/
	public String getElementById(String id) throws FieldNotFoundException {
		
		String element = null;
		
		try {
			element = getElementByField(AnnFileParser.UNIQUE_ID_STRING, id);
			return element;
		} catch (FieldNotFoundException fnfe) {
			throw new FieldNotFoundException("Unique Identifier field (" + MevFileParser.UNIQUE_ID_STRING + ") not found.");
		}
	}
	
	/**
		Returns the record from the ann file that contains the specified value 
		for the specified field. If there are multiple matches, only the first 
		record will be returned.
		
		@param fieldName The column header that identifies the column in which 
		to find the specified value of the target element to be retrieved.
		
		@param value The value in the specified column that identifies the 
		target record to be retrieved.
		
		@throws FieldNotFoundException
		
		@return The String encapsulating the target element, as it appears in 
		the ann file. The trailing newline character, <code>\n</code>, if 
		present, is omitted.
	*/
	public String getElementByField(String fieldName, String value) throws FieldNotFoundException {
		
		Vector targetColumn = getColumnNamed(fieldName);
		
		if (targetColumn == null) throw new FieldNotFoundException("Field " + fieldName + " not found.");
		
		for (int i = 0; i < targetColumn.size(); i++) {
			if (((String) targetColumn.elementAt(i)).equals(value)) {
				return getElementAtIndex(i);
			}
		}
		
		return null;
	}
	
	/**
		Returns a Vector of records from the ann file that contains the 
		specified value for the specified field.
		
		@param fieldName The column header that identifies the column in which 
		to find the specified value of the record to be retrieved.
		
		@param value The value in the specified column that identifies the 
		target element to be retrieved.
		
		@throws FieldNotFoundException
		
		@return The String encapsulating the target element, as it appears in 
		the ann file. The trailing newline character, <code>\n</code>, if 
		present, is omitted. If there are no matches, the return Vector will 
		be null.
	*/
	public Vector getElementsByField(String fieldName, String value) throws FieldNotFoundException {
		
		Vector targetColumn = getColumnNamed(fieldName);
		Vector matchesVector = null;
		
		if (targetColumn == null) throw new FieldNotFoundException("Field " + fieldName + " not found.");
		
		for (int i = 0; i < targetColumn.size(); i++) {
			if (((String) targetColumn.elementAt(i)).equals(value)) {
				if (matchesVector == null) matchesVector = new Vector();
				matchesVector.add(getElementAtIndex(i));
			}
		}
		
		return matchesVector;
	}
	
	/**
		Returns a two-dimensional String array containing every value for each 
		column header for every record in the ann file. The first dimension of 
		the array iterates over the columns, while the second dimension iterates 
		over the spots. All comment lines will be ignored.
		
		@return The String[][] containing all annotation data
	*/
	public String[][] getDataMatrix() {
		return getDataMatrix(false);
	}
	
	/**
		Returns a two-dimensional String array containing every value for each 
		column header for every record in the ann file. The first dimension of 
		the array iterates over the columns, while the second dimension iterates 
		over the spots. Optionally, the first element in the first dimension of 
		the array can be an array of all column headers. All comment lines will 
		be ignored.
		
		@param withHeaders If true, headers are included in the returned array
		
		@return The String[][] containing all annotation data
	*/
	public String[][] getDataMatrix(boolean withHeaders) {
		
		Vector columnHeaders = getColumnHeaders();
		int hc = withHeaders ? 1 : 0;
		
		String[][] matrix = new String[dataLinesMap.size() + hc][columnHeaders.size()];
		
		if (withHeaders) {
			for (int i = 0; i < columnHeaders.size(); i++) {
				matrix[0][i] = (String) columnHeaders.elementAt(i);
			}
		}
		
		for (int i = hc; i < matrix.length; i++) {
			
			String currentLine = getElementAtIndex(i - hc);
			StringTokenizer st = new StringTokenizer(currentLine, "\t");
			
			for (int j = 0; j < matrix[i].length; j++) {
                            try{
				matrix[i][j] = st.nextToken();
                            } catch (Exception e){
                                System.out.println("error with line number "+ i);
                                e.printStackTrace();
                            }
                        }
		}
		
		return matrix;
	}
	
	private static class IntVector extends Vector {

		public void add(int element) {
			super.add(new Integer(element));
		}
		
		public int intElementAt(int index) {
			return ((Integer) super.elementAt(index)).intValue();
		}
	}
}