/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: IntComparator.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:36:56 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.util;

public interface IntComparator {
    /**
     * Returns a negative integer, zero, or a positive integer as the
     * first argument is less than, equal to, or greater than the second.
     */
    int compare(int index1, int index2);
}
