/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: IFramework.java,v $
 * $Revision: 1.8 $
 * $Date: 2005-03-10 20:38:25 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui;

import java.awt.Color;
import java.awt.Frame;
import javax.swing.tree.DefaultMutableTreeNode;
import org.tigr.microarray.mev.ResultTree;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;

/**
 * This class serves as an interface to the framework.
 *
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
     * Returns the result node containing the supplied object
     */
    public DefaultMutableTreeNode getNode(Object object);
    /**
     * Selects passed node in navigation tree, viewer in node is set into MultipleArrayViewer
     */
    public void setTreeNode(DefaultMutableTreeNode node);    
    /**
     * Adds passed node in navigation tree, viewer in node is set into MultipleArrayViewer
     */
    public void addNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode child);
    
    /**
     * Returns the currently selected node.
     */       
    public DefaultMutableTreeNode getCurrentNode();

    /**
     * Stores the indices into the cluster repository
     */    
    public Color storeCluster(int [] indices, Experiment experiment, int clusterType);    

    /**
     *  Stores indices to a cluster in the manager but doesn't link to a particular viewer node.
     */
    public void storeOperationCluster(String source, String clusterID, int [] indices, boolean geneCluster);
    
    /**
     * Stores the indices into the cluster repository even if indices represent a subset
     * of the displayed cluster.
     */
    public Color storeSubCluster(int [] indices, Experiment experiment, int clusterType);

    /**
     * Removes the cluster from the repository
     */    
    public boolean removeSubCluster(int [] indices, Experiment experiment, int clusterType);
    
    /**
     * Removes the cluster from the repository
     */    
    public boolean removeCluster(int [] indices, Experiment experiment, int clusterType);
    
    /**
     * Launches a new Multiple Array Viewer given indices
     */
    public void launchNewMAV(int [] indices, Experiment experiment, String label, int clusterType);
    
    /**
     * Opens viewer a parent node name and a cluster node name
     */
    public void openClusterNode(String parentNodeName, String childNodeName);
    
    /**
     * Returns the specified cluster repository
     */
    public ClusterRepository getClusterRepository(int clusterType);
    
    /** Adds string to history node
     */
    public void addHistory(String historyEvent);
    
    /** Returns the ResultTree object
     */
    public ResultTree getResultTree();
    
           
    /** Adds result to the ResultTree    
     */
    public void addAnalysisResult(DefaultMutableTreeNode resultNode);
    
    /** Refreshes current viewer if it's an IViewer **/
    public void refreshCurrentViewer();
}
