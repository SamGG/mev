/*
 * AnalysisFileFilter.java
 *
 * Created on January 29, 2004, 1:24 PM
 */

package org.tigr.microarray.mev;

import java.io.File;

/**
 *
 * @author  braisted
 */
public class AnalysisFileFilter extends javax.swing.filechooser.FileFilter {
    
    /** Creates a new instance of AnalysisFileFilter */
    public AnalysisFileFilter() {
        
    }
    
        // Accept all directories and all anl files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals("anl")) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    // The description of this filter
    public String getDescription() {
        return "MeV Analysis File (*.anl)";
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
