/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ExpressionFileFilter.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:39:40 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.file;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;

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