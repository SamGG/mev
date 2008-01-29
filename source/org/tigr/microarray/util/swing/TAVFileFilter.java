/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: TAVFileFilter.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 20:59:59 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.util.swing;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class TAVFileFilter extends FileFilter {
    
    public boolean accept(File file) {
	String s = file.getName();
	if (file.isFile() && !s.toUpperCase().endsWith("TAV"))
	    return false;
	return true;
    }
    
    public String getDescription() {
	return "TAV Files (*.tav)";
    }
}

