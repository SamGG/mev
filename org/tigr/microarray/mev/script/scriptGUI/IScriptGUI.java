/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * IScriptGUI.java
 *
 * Created on March 21, 2004, 12:46 AM
 */

package org.tigr.microarray.mev.script.scriptGUI;

import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;

/**
 *
 * @author  braisted
 */
public interface IScriptGUI {
 
public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, 
                                             Experiment experiment) throws AlgorithmException;
 
public AlgorithmData getScriptParameters(IFramework framework) ;
    
    
}
