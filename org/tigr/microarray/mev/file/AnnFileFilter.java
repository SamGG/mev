/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: AnnFileFilter.java,v $
 * $Revision: 1.1 $
 * $Date: 2005-02-24 20:23:50 $
 * $Author: braistedj $
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
        return "TIGR MeV Annotation Files (*.ann, *.dat)";
    }    
}