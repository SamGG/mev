package org.tigr.microarray.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class LineCount {

	
	public LineCount() {
		
	}
	
	
	/**
	 * 
	 * @param targetFile
	 * @return Number of lines in the file 
	 */
	public static int getNumberOfLines(File targetFile) {
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
	
	
	
	
	
	
}
