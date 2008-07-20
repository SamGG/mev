/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * IDataRegionSelectionListener.java
 *
 * Created on March 26, 2003, 1:18 AM
 */

package org.tigr.microarray.mev.cgh.CGHListenerObj;

import java.util.EventObject;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public interface IDataRegionSelectionListener {
    public void onShowBrowser(EventObject eventObj);
    public void onDisplayDataValues(EventObject eventObj);
    public void onShowGenes(EventObject eventObj);
    public void onAnnotationsSelected(EventObject eventObj);
}
