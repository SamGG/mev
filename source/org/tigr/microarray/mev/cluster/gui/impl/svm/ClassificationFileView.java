/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ClassificationFileView.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:21:56 $
 * $Author: braistedj $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.io.File;

import javax.swing.Icon;
import javax.swing.filechooser.FileView;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

public class ClassificationFileView extends FileView {

    private Icon ClassificationIcon = GUIFactory.getIcon("svcfileicon.gif");

    public String getName(File f) { 
        return null; // let the L&F FileView figure this out
    }

    public String getDescription(File f) {
        return null; // let the L&F FileView figure this out
    }

    public Boolean isTraversable(File f) {
        return null; // let the L&F FileView figure this out
    }

    /**
     * Returns type description for specified file.
     */
    public String getTypeDescription(File f) {
        String extension = getExtension(f);
        String type = null;
        if (extension != null) {
            if (extension.equals("svc")) {
                type = "Classification File";
            }
        }
        return type;
    }

    /**
     * Returns an icon for specified file.
     */
    public Icon getIcon(File f) {
        String extension = getExtension(f);
        Icon icon = null;
        if (extension != null) {
            if (extension.equals("svc")) {
                icon = ClassificationIcon;
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
