/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
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

