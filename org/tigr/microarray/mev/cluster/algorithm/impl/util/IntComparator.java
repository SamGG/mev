/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: IntComparator.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:25 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl.util; 

public interface IntComparator {
    /** 
    * Returns a negative integer, zero, or a positive integer as the
    * first argument is less than, equal to, or greater than the second. 
    */
    int compare(int index1, int index2);
}
