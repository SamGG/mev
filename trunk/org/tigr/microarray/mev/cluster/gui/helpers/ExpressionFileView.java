/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ExpressionFileView.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:08 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.helpers;

import java.io.File;
import javax.swing.Icon;
import javax.swing.filechooser.FileView;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

public class ExpressionFileView extends FileView {
    
    private Icon ExpressionIcon = GUIFactory.getIcon("geneicon.gif");
    
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
	String extension = getExtension(f);
	String type = null;
	
	if (extension != null) {
	    if (extension.equals("txt")) {
		type = "Expression file";
	    }
	}
	return type;
    }
    
    public Icon getIcon(File f) {
	String extension = getExtension(f);
	Icon icon = null;
	if (extension != null) {
	    if (extension.equals("txt")) {
		icon = ExpressionIcon;
	    }
	}
	return icon;
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
