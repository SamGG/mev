/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: RelNetComparator.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
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
