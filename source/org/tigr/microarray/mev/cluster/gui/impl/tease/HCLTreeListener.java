/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: HCLTreeListener.java,v $
 * $Revision: 1.1 $
 * $Date: 2006-02-08 18:17:16 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.tease;

public interface HCLTreeListener {
    /**
     * Invoked when a new cluster is selected.
     */
    void valueChanged(HCLTree source, HCLCluster cluster);
}

