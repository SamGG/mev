/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ExpressionFileView.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 20:59:59 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.util.swing;

import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;

import org.tigr.util.swing.Utils;

public class ExpressionFileView extends FileView {
    Icon ExpressionIcon = new ImageIcon(org.tigr.microarray.util.swing.ExpressionFileView.class.getResource("/org/tigr/images/GeneIcon.gif"));
    
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
	    if (extension.equals(Utils.txt)) {
		type = "Expression file";
	    }
	}
	return type;
    }
    
    public Icon getIcon(File f) {
	String extension = Utils.getExtension(f);
	Icon icon = null;
	if (extension != null) {
	    if (extension.equals(Utils.txt)) {
		icon = ExpressionIcon;
	    }
	}
	return icon;
    }
}
