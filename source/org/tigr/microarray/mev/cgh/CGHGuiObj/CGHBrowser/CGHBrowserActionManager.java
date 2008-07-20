/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * CGHBrowserActionManager.java
 *
 * Created on July 5, 2003, 10:13 PM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.CGHBrowser;

import java.awt.event.ActionListener;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHBrowserActionManager {
    ActionListener listener;

    /** Creates a new instance of CGHBrowserActionManager */
    public CGHBrowserActionManager(ActionListener listener) {
        this.listener = listener;
    }

    public static final String CLONE_VALUES_DYE_SWAP = "clone-values-dye-swap";
    public static final String CLONE_VALUES_LOG_AVERAGE_INVERTED = "clone-values-log-invert-average";
    public static final String CLONE_VALUES_P_VALUES = "clone-values-p-values";
    public static final String CLONE_VALUES_LOG_DYE_SWAP = "clone-values-log-dye-swap";
    public static final String CLONE_VALUES_RATIOS = "clone-values-ratios";
    public static final String CLONE_VALUES_LOG_RATIOS = "clone-values-log-ratios";
}
