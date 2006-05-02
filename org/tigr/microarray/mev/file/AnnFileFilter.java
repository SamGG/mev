/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
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