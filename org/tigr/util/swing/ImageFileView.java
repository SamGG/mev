/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ImageFileView.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:27:48 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.util.swing;

import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;

public class ImageFileView extends FileView {
    Icon ImageIcon = new ImageIcon(org.tigr.util.swing.ImageFileView.class.getResource("/org/tigr/images/JPGFileIcon.gif"));
    Icon DirectoryIcon = new ImageIcon(org.tigr.util.swing.ImageFileView.class.getResource("/org/tigr/images/Directory.gif"));
    
    public String getName(File f) {
	return null; // let the L&F FileView figure this out
    }
    
    public String getDescription(File f) {
	return null; // let the L&F FileView figure this out
    }
    
    public Boolean isTraversable(File f) {
	return null; // let the L&F FileView figure this out
    }
    
    public String getTypeDescription(File f) {
	String extension = Utils.getExtension(f);
	String type = null;
	
	if (extension != null) {
	    if (extension.equals(Utils.bmp)) {
		type = "BMP Image File";
	    }
	    if (extension.equals(Utils.jpg)) {
		type = "JPEG Image File";
	    }
	    if (extension.equals(Utils.png)) {
		type = "PNG Image File";
	    }
	    if (extension.equals(Utils.tiff)) {
		type = "TIFF Image File";
	    }
	}
	return type;
    }
    
    public Icon getIcon(File f) {
	String extension = Utils.getExtension(f);
	Icon icon = null;
	
	if (f.isDirectory()) {
	    icon = DirectoryIcon;
	}
	
	if (extension != null) {
	    if (extension.equals(Utils.bmp)) {
		icon = ImageIcon;
	    }
	    if (extension.equals(Utils.jpg)) {
		icon = ImageIcon;
	    }
	    if (extension.equals(Utils.png)) {
		icon = ImageIcon;
	    }
	    if (extension.equals(Utils.tiff)) {
		icon = ImageIcon;
	    }
	    
	}
	return icon;
    }
}
