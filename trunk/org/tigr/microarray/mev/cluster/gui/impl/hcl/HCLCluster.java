/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: HCLCluster.java,v $
 * $Revision: 1.2 $
 * $Date: 2004-02-05 20:25:10 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import java.awt.Color;

public class HCLCluster implements java.io.Serializable {
    
    static final long serialVersionUID = 1L;
    
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
