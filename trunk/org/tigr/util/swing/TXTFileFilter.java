/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: TXTFileFilter.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.util.swing;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class TXTFileFilter extends FileFilter {
    
    public boolean accept(File file) {
	String s = file.getName();
	if (file.isFile() && !s.toUpperCase().endsWith("TXT"))
	    return false;
	return true;
    }
    
    public String getDescription() {
	return "Tab Delimited Text (*.txt)";
    }
}

