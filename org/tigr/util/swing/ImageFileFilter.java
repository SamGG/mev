/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ImageFileFilter.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.util.swing;

import com.sun.media.jai.codec.ImageEncodeParam;

public interface ImageFileFilter {
    public String getFileFormat();
    public ImageEncodeParam getImageEncodeParam();
}
