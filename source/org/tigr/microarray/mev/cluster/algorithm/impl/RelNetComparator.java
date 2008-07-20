/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: RelNetComparator.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:45:20 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl;

import org.tigr.microarray.mev.cluster.algorithm.impl.util.IntComparator;

public class RelNetComparator implements IntComparator {

    private double[] entropy;

    /**
     * Constructs a <code>RelNetComparator</code> with specified entropy.
     */
    public RelNetComparator(double[] entropy) {
        this.entropy = entropy;
    }

    /** 
     * Compare entropy with specified indices.
     */
    public int compare(int index1, int index2) {
        double e1 = entropy[index1];
        double e2 = entropy[index2];
        return e1 < e2 ? 1 : (e1 > e2 ? -1 : 0);
    }
}
