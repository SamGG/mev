/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ExpressionFileFilter.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:48 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.helpers;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ExpressionFileFilter extends FileFilter {
    
    public boolean accept(File f) {
	if (f.isDirectory()) {
	    return true;
	}
	String extension = getExtension(f);
	if (extension != null) {
	    if (extension.equals("txt")) {
		return true;
	    } else {
		return false;
	    }
	}
	return false;
    }
    
    // The description of this filter
    public String getDescription() {
	return "Expression file (*.txt)";
    }
    
    /** Get the extension of a file. */
    private String getExtension(File f) {
	String ext = null;
	String s = f.getName();
	int i = s.lastIndexOf('.');
	if (i > 0 &&  i < s.length() - 1) {
	    ext = s.substring(i+1).toLowerCase();
	}
	return ext;
    }
}
