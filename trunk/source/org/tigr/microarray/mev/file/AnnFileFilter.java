/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: AnnFileFilter.java,v $
 * $Revision: 1.3 $
 * $Date: 2006-05-02 20:52:48 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.file;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class AnnFileFilter extends FileFilter {
    
    public boolean accept(File f) {
        if (f.isDirectory()) return true;
        if (f.getName().endsWith(".ann")) return true;
        if (f.getName().endsWith(".dat")) return true;
        else return false;
    }
    
    public String getDescription() {
        return "MeV Annotation Files (*.ann, *.dat)";
    }    
}
