/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ExpressionFileFilter.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:08 $
 * $Author: braistedj $
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
