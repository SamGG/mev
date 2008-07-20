/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * ICGHArrayViewer.java
 *
 * Created on June 1, 2003, 12:34 AM
 */

package org.tigr.microarray.mev.cgh.CGHListenerObj;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public interface ICGHListener {
    public void onDataChanged();
    public void onCloneValuesChanged();
    public void onChromosomeSelected(java.util.EventObject eventObj);
    public void onCloneDistributionsLoaded();
    //public void onExperimentsLoaded(java.util.EventObject eventObj);
    public void onExperimentsLoaded();
    //public void onExperimentsInitialized(java.util.EventObject eventObj);
}
