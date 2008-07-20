/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
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
