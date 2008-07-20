/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: TXTFileFilter.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 21:00:04 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.util.swing;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class TXTFileFilter extends FileFilter {
    
    public boolean accept(File file) {
	String s = file.getName();
	if (file.isFile() && !s.toUpperCase().endsWith("TXT"))
	    return false;
	return true;
    }
    
    public String getDescription() {
	return "Tab Delimited Text (*.txt)";
    }
}

