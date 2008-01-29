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