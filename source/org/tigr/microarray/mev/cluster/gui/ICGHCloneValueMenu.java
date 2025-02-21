/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * ICGHCloneValueMenu.java
 *
 * Created on Sept 29, 2005
 */

package org.tigr.microarray.mev.cluster.gui;

/**
 *
 * @author  Raktim Sinha
 */

public interface ICGHCloneValueMenu {
    public static final int CLONE_VALUE_DISCRETE_DETERMINATION = 0;
    public static final int CLONE_VALUE_LOG_AVERAGE_INVERTED = 1;
    public static final int CLONE_VALUE_LOG_CLONE_DISTRIBUTION = 2;
    public static final int CLONE_VALUE_THRESHOLD_OR_CLONE_DISTRIBUTION = 3;
    
    public static final int FLANKING_REGIONS_BY_THRESHOLD = 0;
    public static final int FLANKING_REGIONS_BY_LOG_CLONE_DISTRIBUTION = 1;
    public static final int FLANKING_REGIONS_BY_THRESHOLD_OR_CLONE_DISTRIBUTION = 2;
    
    public int getCloneValueType();
    public int getFlankingRegionType();
    public void setCloneValueType(int cloneValue);
    public float getClonePThresh();
    
    public float getAmpThresh();
    public float getDelThresh();
    public float getAmpThresh2Copy();
    public float getDelThresh2Copy();
    
    public void setAmpThresh(float thresh);
    public void setDelThresh(float thresh);
    public void setAmpThresh2Copy(float thresh);
    public void setDelThresh2Copy(float thresh);
    
}
