/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: IFramework.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui;

import java.awt.Color;
import java.awt.Frame;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;

/**
 * This class serves as an interface to the framework.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public interface IFramework {
    
    /**
     * Returns instance of <code>IData</code> interface implementation.
     */
    public IData getData();
    
    /**
     * Returns instance of <code>AlgorithmFactory</code> interface implementation.
     */
    public AlgorithmFactory getAlgorithmFactory();
    
    /**
     * Returns instance of <code>IDisplayMenu</code> interface implementation.
     */
    public IDisplayMenu getDisplayMenu();
    
    /**
     * Returns instance of <code>IDistanceMenu</code> interface implementation.
     */
    public IDistanceMenu getDistanceMenu();
    
    /**
     * Returns the framework's main frame.
     */
    public Frame getFrame();
    
    /**
     * Moves scroll pane content to the specified coordinaties.
     */
    public void setContentLocation(int x, int y);
    
    /**
     * Runs single array viewer for specified column.
     */
    public void displaySingleArrayViewer(int column);
    
    /**
     * Runs a dialog to display an element info.
     */
    public void displaySlideElementInfo(int column, int row);
    
    /**
     * Returns text of the framework status bar.
     */
    public String getStatusText();
    
    /**
     * Sets text to the framework status bar.
     */
    public void setStatusText(String text);
    
    /**
     * Returns a meta data from a selected tree node.
     */
    public Object getUserObject();
    
    /**
     * Selects passed node in navigation tree, viewer in node is set into MultipleArrayViewer
     */
    public void setTreeNode(DefaultMutableTreeNode node);
    
    /**
     * Adds passed node in navigation tree, viewer in node is set into MultipleArrayViewer
     */
    public void addNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode child);
    
    /**
     * Stores the indices into the cluster repository
     */
    public Color storeCluster(int [] indices, Experiment experiment, int clusterType);
    
    /**
     * Stores the indices into the cluster repository even if indices represent a subset
     * of the displayed cluster.
     */
    public Color storeSubCluster(int [] indices, Experiment experiment, int clusterType);

    /**
     * Removes the cluster from the repository
     */    
    public void removeSubCluster(int [] indices, Experiment experiment, int clusterType);
    
    /**
     * Removes the cluster from the repository
     */    
    public void removeCluster(int [] indices, Experiment experiment, int clusterType);
    
    /**
     * Launches a new Multiple Array Viewer given indices
     */
    public void launchNewMAV(int [] indices, Experiment experiment, String label, int clusterType);
    
    /**
     * Opens viewer a parent node name and a cluster node name
     */
    public void openClusterNode(String parentNodeName, String childNodeName);
}
