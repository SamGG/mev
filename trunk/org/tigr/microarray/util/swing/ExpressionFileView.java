/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ExpressionFileView.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.util.swing;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;
import org.tigr.util.swing.*;

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
