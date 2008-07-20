/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.file.agilent;

/*
 * Created on Apr 14, 2004
 */

/**
 * Custom Exception for the case when the column Header Strings did not contain
 * the same number of columns
 * @author vu
 */
public class ColumnMismatchException extends Exception {
	public ColumnMismatchException(String s) {
		super(s);
	}
}