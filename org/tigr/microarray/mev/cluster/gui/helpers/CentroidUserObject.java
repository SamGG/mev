/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: CentroidUserObject.java,v $
 * $Revision: 1.2 $
 * $Date: 2004-02-05 22:53:06 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.helpers;

public class CentroidUserObject implements java.io.Serializable {
    
    // drawing modes
    public static final int VARIANCES_MODE = 0;
    public static final int VALUES_MODE = 1;
    
    private int clusterIndex;
    private int mode; // drawing mode
    
    /**
     * Constructs a <code>CentroidUserObject</code> with specified
     * cluster index and drawing mode.
     *
     * @param clusterIndex the cluster index.
     * @param mode the drawing mode.
     */
    public CentroidUserObject(int clusterIndex, int mode) {
	this.clusterIndex = clusterIndex;
	this.mode = mode;
    }
    
    /**
     * Returns a cluster index.
     */
    public int getClusterIndex() {
	return clusterIndex;
    }
    
    /**
     * Returns a drawing mode.
     */
    public int getMode() {
	return mode;
    }
}
