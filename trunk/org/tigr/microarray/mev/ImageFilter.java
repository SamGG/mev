/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ImageFilter.java,v $
 * $Revision: 1.2 $
 * $Date: 2003-12-08 21:52:11 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;
import org.tigr.util.swing.*;

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
