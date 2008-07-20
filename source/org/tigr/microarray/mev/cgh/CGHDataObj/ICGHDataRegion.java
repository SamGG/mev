/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * CGHDataRegion.java
 *
 * Created on March 25, 2003, 3:24 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataObj;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public interface ICGHDataRegion {
    public String getName();
    public int getChromosomeIndex();
    public int getStart();
    public int getStop();
}
