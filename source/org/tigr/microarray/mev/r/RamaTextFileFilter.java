/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Feb 7, 2005
 */
package org.tigr.microarray.mev.r;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * @author vu
 */
public class RamaTextFileFilter extends FileFilter {

	/**
	 *
	 */

	public boolean accept(File f) {
		if( f.isDirectory() ) {
			return true;
		} else if( f.getPath().toLowerCase().endsWith( ".txt" ) ) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 *
	 */

	public String getDescription() {
		return "Text (.txt) Files";
	}

}
