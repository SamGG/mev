/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: StanfordFileFilter.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.file;

import java.io.File;
import java.util.Vector;

public class StanfordFileFilter extends ExpressionFileFilter {
    
    public boolean accept(File f) {
	String extension = "";
	if (f.isDirectory()) return true;
	
	return true;
    }
    
    public String getDescription() {
	return "Stanford Format Files (*.*)";
    }
    
    public Vector loadExpressionFile(File file) {
	Vector dataVector = new Vector();
	
	dataVector.add(new String("ST1"));
	dataVector.add(new String("ST2"));
	dataVector.add(new String("ST3"));
	
	return dataVector;
    }
}