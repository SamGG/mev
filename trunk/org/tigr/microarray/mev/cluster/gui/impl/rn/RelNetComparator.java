/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: RelNetComparator.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:23:44 $
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
