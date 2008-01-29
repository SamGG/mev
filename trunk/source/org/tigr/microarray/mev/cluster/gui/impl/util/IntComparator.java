/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
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
