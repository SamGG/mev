/*
 * $RCSfile: AgilentFileFilter.java,v $
 * $Revision: 1.1 $
 * $Date: 2005-02-24 20:23:49 $
 * $Author: braistedj $
 */
package org.tigr.microarray.mev.file.agilent;

import java.io.File;
import java.util.Vector;

import org.tigr.microarray.mev.file.ExpressionFileFilter;

public class AgilentFileFilter extends ExpressionFileFilter {
    
    public boolean accept(File f) {
	String extension = "";
	if (f.isDirectory()) return true;
	
	return true;
    }
    
    public String getDescription() {
	return "Agilent Files (*.*)";
    }
    
    public Vector loadExpressionFile(File file) {
	return new Vector();
    }
}