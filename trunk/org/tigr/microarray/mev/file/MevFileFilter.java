/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: MevFileFilter.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-05-02 20:52:48 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.file;

import java.io.File;
import java.util.Vector;

public class MevFileFilter extends ExpressionFileFilter {
    
    public boolean accept(File f) {
	String extension = "";
	if (f.isDirectory()) return true;
	
	if (f.getName().endsWith(".mev")) return true;
	else return false;
    }
    
    public String getDescription() {
	return "MeV Files (*.mev)";
    }
    
    public Vector loadExpressionFile(File file) {
	Vector dataVector = new Vector();
	
	if (! accept(file)) return dataVector;
	
	dataVector.add(new String("MEV"));
	
	return dataVector;
    }
}