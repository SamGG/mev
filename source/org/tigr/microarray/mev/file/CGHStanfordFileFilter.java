/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: CGHStanfordFileFilter.java,v $
 * $Revision: 1.1 $
 * $Date: 2006-02-02 19:56:28 $
 * $Author: raktim $
 * $State: Exp $
 */
package org.tigr.microarray.mev.file;

import java.io.File;
import java.util.Vector;

public class CGHStanfordFileFilter extends ExpressionFileFilter {

    public boolean accept(File f) {
	String extension = "";
	if (f.isDirectory()) return true;

	return true;
    }

    public String getDescription() {
	return "CGH Tab Delimited, Multiple Sample Files (TDMS) (*.*)";
    }

    public Vector loadExpressionFile(File file) {
	Vector dataVector = new Vector();

	dataVector.add(new String("ST1"));
	dataVector.add(new String("ST2"));
	dataVector.add(new String("ST3"));

	return dataVector;
    }
}
