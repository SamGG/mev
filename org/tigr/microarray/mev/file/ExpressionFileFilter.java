/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ExpressionFileFilter.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.file;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import javax.swing.filechooser.FileFilter;

import org.tigr.microarray.mev.ISlideData;

public abstract class ExpressionFileFilter extends FileFilter {
    
    public abstract Vector loadExpressionFile(File f) throws IOException;
    
    public Vector loadExpressionFile(Vector fileVector) throws IOException {
	Vector dataVector = new Vector();
	Vector singleDataVector;
	
	for (int i = 0; i < fileVector.size(); i++) {
	    singleDataVector = loadExpressionFile((File) fileVector.elementAt(i));
	    
	    for (int j = 0; j < singleDataVector.size(); j++) {
		dataVector.add(singleDataVector.elementAt(j));
	    }
	}
	
	return dataVector;
    }
}