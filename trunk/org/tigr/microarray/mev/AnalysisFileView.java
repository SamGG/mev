/*
 * AnalysisFileView.java
 *
 * Created on January 29, 2004, 1:32 PM
 */

package org.tigr.microarray.mev;

import java.io.File;

import javax.swing.Icon;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

/**
 *
 * @author  braisted
 */
public class AnalysisFileView extends javax.swing.filechooser.FileView {
    
    private Icon analysisIcon = GUIFactory.getIcon("analysis16.gif");

    
    /** Creates a new instance of AnalysisFileView */
    public AnalysisFileView() {
    }
    
    public String getName(File f) {
        return null; // let the L&F FileView figure this out
    }

    public String getDescription(File f) {
        return null; // let the L&F FileView figure this out
    }

    public Boolean isTraversable(File f) {
        return null; // let the L&F FileView figure this out
    }

    public String getTypeDescription(File f) {
        String extension = getExtension(f);
        String type = null;

        if (extension != null) {
            if (extension.equals("anl")) {
                type = "MeV Analysis File";
            }
        }
        return type;
    }

    public Icon getIcon(File f) {
        String extension = getExtension(f);
        Icon icon = null;
        if (extension != null) {
            if (extension.equals("anl")) {
                icon = analysisIcon;
            }
        }
        return icon;
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
