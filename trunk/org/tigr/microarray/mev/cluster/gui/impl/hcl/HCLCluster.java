/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: HCLCluster.java,v $
 * $Revision: 1.3 $
 * $Date: 2004-07-27 19:59:16 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import java.awt.Color;

public class HCLCluster implements java.io.Serializable {
    
    public static final long serialVersionUID = 202006020001L;
    
    public int root; // root index
    public int firstElem;
    public int lastElem;
    public Color color; // color of the cluster.
    public String text; // cluster description.
    public boolean isGeneCluster;
    public int size;
    /**
     * Constructs a <code>HCLCluster</code> with specified root,
     * firstRow and lastRow indices.
     */
    public HCLCluster(int root, int firstElem, int lastElem) {
	this.root = root;
	this.firstElem = firstElem;
	this.lastElem = lastElem;
        this.isGeneCluster = true;
        this.size = (lastElem - firstElem) + 1;
    }
    
    public void setFinalSize(){
        this.size = (this.lastElem - this.firstElem) + 1;
    }

    
}
