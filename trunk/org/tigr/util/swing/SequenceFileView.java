/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SequenceFileView.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:11 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.util.swing;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

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
