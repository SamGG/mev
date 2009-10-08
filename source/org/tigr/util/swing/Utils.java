/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: Utils.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.util.swing;

import java.io.File;

public class Utils {
    
    public final static String txt = "txt";
    public final static String dat = "dat";
    public final static String res = "res";
    public final static String svm = "svm";
    public final static String jpg = "jpg";
    public final static String svc = "svc";
    public final static String png = "png";
    public final static String seq = "seq";
    public final static String fna = "fna";
    public final static String bmp = "bmp";
    public final static String tree = "tree";
    public final static String tiff = "tiff";
    public final static String gmt = "gmt";
    public final static String gmx = "gmx";
    /*
     * Get the extension of a file.
     */
    public static String getExtension(File f) {
	String ext = null;
	String s = f.getName();
	int i = s.lastIndexOf('.');
	
	if (i > 0 &&  i < s.length() - 1) {
	    ext = s.substring(i+1).toLowerCase();
	}
	return ext;
    }
}
