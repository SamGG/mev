/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ImageFilter.java,v $
 * $Revision: 1.5 $
 * $Date: 2006-02-23 20:59:41 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.tigr.util.swing.Utils;

public class ImageFilter extends FileFilter {
    
    // Accept all directories and all gif, jpg, or tiff files.
    public boolean accept(File f) {
	if (f.isDirectory()) {
	    return true;
	}
	
	String extension = Utils.getExtension(f);
	if (extension != null) {
	    if (extension.equals(Utils.txt) ||
	    extension.equals(Utils.dat) ||
	    extension.equals(Utils.res)) {
		return true;
	    } else {
		return false;
	    }
	}
	
	return false;
    }
    
    // The description of this filter
    public String getDescription() {
	return "Datasets (*.txt, *.dat, *.res)";
    }
}
