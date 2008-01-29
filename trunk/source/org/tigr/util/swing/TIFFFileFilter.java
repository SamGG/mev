/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: TIFFFileFilter.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 21:00:04 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.util.swing;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import com.sun.media.jai.codec.ImageEncodeParam;
import com.sun.media.jai.codec.TIFFEncodeParam;

public class TIFFFileFilter extends FileFilter implements ImageFileFilter {
    
    // Accept all directories and all gif, jpg, or tiff files.
    public boolean accept(File f) {
	if (f.isDirectory()) {
	    return true;
	}
	
	String extension = Utils.getExtension(f);
	if (extension != null) {
	    if (extension.equals(Utils.tiff)) {
		return true;
	    } else {
		return false;
	    }
	}
	
	return false;
    }
    
    // The description of this filter
    public String getDescription() {
	return "TIFF image files (*.tiff)";
    }
    
    public ImageEncodeParam getImageEncodeParam() {
	TIFFEncodeParam param = new TIFFEncodeParam();
	param.setCompression(TIFFEncodeParam.COMPRESSION_NONE);
	return param;
    }
    
    public String getFileFormat() {
	return "TIFF";
    }
}
