/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SVMFileFilter.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:23:45 $
 * $Author: braistedj $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class SVMFileFilter extends FileFilter {

    // Accept all directories and all svm files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals("svm")) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    // The description of this filter
    public String getDescription() {
        return "SVM File (*.svm)";
    }

    /**
     * Get the extension of a file.
     */
    private String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}
