/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: AffymetrixFileFilter.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:39:39 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.file;

import java.io.File;
import java.util.Vector;

public class AffymetrixFileFilter extends ExpressionFileFilter {
    
    public boolean accept(File f) {
	String extension = "";
	if (f.isDirectory()) return true;
	
	//if (f.getName().endsWith(".tav")) return true;
	//else if (f.getName().endsWith(".pref")) return true;
	//else return false;
	
	return true;
    }
    
    public String getDescription() {
	return "Affymetrix Files (*.*)";
    }
    
    public Vector loadExpressionFile(File file) {
	return new Vector();
    }
}
