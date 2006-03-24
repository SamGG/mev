/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: CentroidUserObject.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-03-24 15:49:54 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.helpers;

public class CentroidUserObject implements java.io.Serializable {
    public static final long serialVersionUID = 201030001L;    
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
    
    //EH 06/14/05 beanifying
    public CentroidUserObject() {
    	
    }
    public void setClusterIndex(int c) {
    	clusterIndex = c;
    }
    public void setMode(int m) {
    	this.mode = m;
    }
    //EH end beanifying
        
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
