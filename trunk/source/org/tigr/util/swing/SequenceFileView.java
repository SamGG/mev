/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SequenceFileView.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 21:00:04 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.util.swing;

import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;

public class SequenceFileView extends FileView {
    Icon ExpressionIcon = new ImageIcon(SequenceFileView.class.getResource("/org/tigr/images/Sequence216.gif"));
    Icon DirectoryIcon = new ImageIcon(SequenceFileView.class.getResource("/org/tigr/images/Directory.gif"));
    
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
	    if (extension.equals(Utils.seq) || extension.equals(Utils.fna)) {
		type = "Sequence file";
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
	    if (extension.equals(Utils.seq) || extension.equals(Utils.fna)) {
		icon = ExpressionIcon;
	    }
	}
	return icon;
    }
}
