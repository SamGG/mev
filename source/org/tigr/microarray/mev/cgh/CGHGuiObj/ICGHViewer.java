/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * ICGHViewer.java
 *
 * Created on June 15, 2003, 1:35 AM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj;

import org.tigr.microarray.mev.cluster.gui.ICGHCloneValueMenu;
import org.tigr.microarray.mev.cluster.gui.ICGHDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IViewer;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public interface ICGHViewer extends IViewer {
    public void onMenuChanged(ICGHDisplayMenu menu);
    public void onThresholdsChanged(ICGHDisplayMenu menu);
    public void onCloneValuesChanged(ICGHCloneValueMenu menu);
}
