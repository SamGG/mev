/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: TavFileFilter.java,v $
 * $Revision: 1.2 $
 * $Date: 2004-02-27 22:16:51 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.Vector;

import org.tigr.microarray.mev.FloatSlideData;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.ISlideDataElement;
import org.tigr.microarray.mev.ISlideMetaData;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.mev.TMEV;

import org.tigr.util.StringSplitter;

public class TavFileFilter extends ExpressionFileFilter {
    
    private static final int BUFFER_SIZE = 1024*128;
    
    public boolean accept(File f) {
	String extension = "";
	if (f.isDirectory()) return true;
	
	if (f.getName().endsWith(".tav")) return true;
	else return false;
    }
    
    public String getDescription() {
	return "TIGR ArrayViewer Files (*.tav)";
    }
    
   
    public Vector loadExpressionFile(File file) throws IOException {
	Vector dataVector = new Vector();
	
	if (! accept(file)) return dataVector;
	
	ISlideDataElement slideDataElement;
	String currentLine;
	
	int maxRows = 0, maxColumns = 0;
	String avoidNullString;
	int p, q;
	
	//int coordinatePairCount = TMEV.getCoordinatePairCount();
	//int intensityCount = TMEV.getIntensityCount();
	//final int preSpotRows = TMEV.getHeaderRowCount();
	int coordinatePairCount = 3;
	int intensityCount = 2;
	final int preSpotRows = 0;
	
	int[] rows = new int[coordinatePairCount];
	int[] columns = new int[coordinatePairCount];
	float[] intensities = new float[intensityCount];
	//String[] moreFields = new String[TMEV.getFieldNames().length];
	String[] moreFields = new String[3];
	
	BufferedReader reader = new BufferedReader(new FileReader(file), BUFFER_SIZE);
	StringSplitter ss = new StringSplitter((char) 0x09);
	int currentRow, currentColumn;
	int header_row = 0;
	
	while ((currentLine = reader.readLine()) != null) {
	    if (header_row < preSpotRows) {
		header_row++;
		continue;
	    }
	    
	    ss.init(currentLine);
	    currentRow = ss.nextIntToken();
	    currentColumn = ss.nextIntToken();
	    if (currentRow > maxRows) maxRows = currentRow;
	    if (currentColumn > maxColumns) maxColumns = currentColumn;
	}
	
	SlideData slideData = new SlideData(maxRows, maxColumns);
	reader.close();
	reader = new BufferedReader(new FileReader(file));
	header_row = 0;
	int curpos = 0;
	
	while ((currentLine = reader.readLine()) != null) {
	    
	    if (header_row < preSpotRows) {
		header_row++;
		continue;
	    }
	    
	    ss.init(currentLine);
	    
	    for (int j = 0; j < coordinatePairCount; j++) {
		rows[j] = ss.nextIntToken();
		columns[j] = ss.nextIntToken();
	    }
	    
	    for (int j = 0; j < intensityCount; j++) {
		intensities[j] = ss.nextFloatToken(0.0f);
	    }
	    
	    for (int j = 0; j < moreFields.length; j++) {
		if (ss.hasMoreTokens()) {
		    avoidNullString = ss.nextToken();
		    if (avoidNullString.equals("null")) moreFields[j] = "";
		    else moreFields[j] = avoidNullString;
		} else {
		    moreFields[j] = "";
		}
	    }
	    
	    slideDataElement = new SlideDataElement(rows, columns, intensities, moreFields);
	    slideData.addSlideDataElement(slideDataElement);
	}
	
	reader.close();
	slideData.setSlideDataName(file.getName());
	slideData.setSlideFileName(file.getPath());
	
	dataVector.add(slideData);
	return dataVector;
    }
     
}