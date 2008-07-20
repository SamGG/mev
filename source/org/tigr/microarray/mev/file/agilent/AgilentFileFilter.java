/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: AgilentFileFilter.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-03-10 15:39:40 $
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