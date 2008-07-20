/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * GDMScaleListener.java
 *
 * Created on September 11, 2003, 11:36 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.gdm;

/**
 *
 * @author  braisted
 */
public abstract class GDMScaleListener {
    
    /** Creates a new instance of GDMScaleListener */
    public GDMScaleListener() {
    }
    
    public abstract void scaleChanged(float lower, float upper);
}
