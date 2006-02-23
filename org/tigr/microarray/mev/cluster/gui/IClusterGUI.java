/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: IClusterGUI.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:47 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui;

import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;

/**
 * The interface of a class which is used to execute an analysis.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public interface IClusterGUI {
    
    /**
     * This method should return a tree with calculation results or
     * null, if analysis start was canceled.
     *
     * @param framework the reference to <code>IFramework</code> implementation,
     *        which is used to obtain an initial analysis data and parameters.
     * @throws AlgorithmException if calculation was failed.
     * @throws AbortException if calculation was canceled.
     * @see IFramework
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException;
}
