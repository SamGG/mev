/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: RelNetComparator.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:14:06 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.rn;

import org.tigr.microarray.mev.cluster.gui.impl.util.IntComparator;

public class RelNetComparator implements IntComparator {

    private int[][] clusters;

    /**
     * Constructs a <code>RelNetComparator</code> with specified clusters.
     */
    public RelNetComparator(int[][] clusters) {
        this.clusters = clusters;
    }

    /**
     * Compare cluster sizes with specified indices.
     */
    public int compare(int index1, int index2) {
        int l1 = clusters[index1].length;
        int l2 = clusters[index2].length;
        return l1 < l2 ? 1 : (l1 > l2 ? -1 : 0);
    }
}
