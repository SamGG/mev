/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: BMPFileFilter.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.util.swing;

import java.io.File;
import javax.swing.filechooser.FileFilter;

import com.sun.media.jai.codec.ImageEncodeParam;
import com.sun.media.jai.codec.BMPEncodeParam;

public class BMPFileFilter extends FileFilter implements ImageFileFilter {
    
    // Accept all directories and all gif, jpg, or tiff files.
    public boolean accept(File f) {
	if (f.isDirectory()) {
	    return true;
	}
	
	String extension = Utils.getExtension(f);
	if (extension != null) {
	    if (extension.equals(Utils.bmp)) {
		return true;
	    } else {
		return false;
	    }
	}
	
	return false;
    }
    
    // The description of this filter
    public String getDescription() {
	return "BMP image files (*.bmp)";
    }
    
    public ImageEncodeParam getImageEncodeParam() {
	return new BMPEncodeParam();
    }
    
    public String getFileFormat() {
	return "BMP";
    }
}
