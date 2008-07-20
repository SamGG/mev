/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * IScriptGUI.java
 *
 * Created on March 21, 2004, 12:46 AM
 */

package org.tigr.microarray.mev.script.scriptGUI;

import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IFramework;

/** IScriptGUI provides the methods required for an algorithm to
 * support scripting activities in MeV.
 *
 * These methods are to be implemented in the gui packages of algorithm
 * modules to implement scripting support and usually these are IClusterGUI
 * implementations.
 * @author braisted
 */
public interface IScriptGUI {
 
    
    /** Returns selected parameters for building a script.
     * @param framework Framework object to provide IData object.
     * @return
     */    
    public AlgorithmData getScriptParameters(IFramework framework) ;
    
    /** Excutes algorihtm provided an experiment, parameters, and the framework.
     * @param framework <code>IFramework</code> object.
     * @param algData Holds parameters
     * @param experiment <code>Experiment</code> object wraps <code>FloatMatrix</code>.
     *
     * @throws AlgorithmException
     * @return
     */    
    public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, 
                                             Experiment experiment) throws AlgorithmException;
 

    
    
}
