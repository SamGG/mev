/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: HCLTreeListener.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:22:02 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.hcl;

public interface HCLTreeListener {
    /**
     * Invoked when a new cluster is selected.
     */
    void valueChanged(HCLTree source, HCLCluster cluster);
}
