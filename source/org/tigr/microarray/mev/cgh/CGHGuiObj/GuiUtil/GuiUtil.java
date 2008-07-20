/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * UtilGui.java
 *
 * Created on January 19, 2003, 12:15 AM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.GuiUtil;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class GuiUtil {

    /** Creates a new instance of UtilGui */
    public static void center(Window w){
        Dimension us = w.getSize();
        Dimension them = Toolkit.getDefaultToolkit().getScreenSize();

        int newX = (them.width - us.width) / 2;
        int newY = (them.height - us.height) / 2;
        w.setLocation(newX, newY);
    }

}
