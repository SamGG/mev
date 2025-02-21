/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: IFramework.java,v $
 * $Revision: 1.11 $
 * $Date: 2006-02-23 21:19:42 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Rectangle;
import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.ResultTree;
import org.tigr.microarray.mev.cgh.CGHDataModel.CytoBandsModel;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.resources.IMultiSupportFileDefinition;
import org.tigr.microarray.mev.resources.IResourceManager;
import org.tigr.microarray.mev.resources.ISupportFileDefinition;
import org.tigr.microarray.mev.resources.SupportFileAccessError;

/**
 * This class serves as an interface to the framework. Implementations of this
 * interface are used as 'conduits' for information exchange between the main
 * interface and the current viewer. Methods include signaling cluster storage
 * request, request for IData access, and other viewer triggered events.
 */
public interface IFramework {

	/**
	 * Returns instance of <code>IData</code> interface implementation.
	 */
	public IData getData();

	public JFrame getJFrame();

	/**
	 * Returns instance of <code>AlgorithmFactory</code> interface
	 * implementation.
	 */
	public AlgorithmFactory getAlgorithmFactory();

	/**
	 * Returns instance of <code>IDisplayMenu</code> interface
	 * implementation.
	 */
	public IDisplayMenu getDisplayMenu();

	/**
	 * Returns instance of <code>IDistanceMenu</code> interface
	 * implementation.
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
	 * Selects passed node in navigation tree, viewer in node is set into
	 * MultipleArrayViewer
	 */
	public void setTreeNode(DefaultMutableTreeNode node);

	/**
	 * Adds passed node in navigation tree, viewer in node is set into
	 * MultipleArrayViewer
	 */
	public void addNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode child);

	/**
	 * Returns the currently selected node.
	 */
	public DefaultMutableTreeNode getCurrentNode();

	/**
	 * Stores the indices into the cluster repository
	 */
	public Color storeCluster(int[] indices, Experiment experiment, int clusterType);

	/**
	 * Stores the indices into the cluster repository with a preselected color
	 */
	public Color storeCluster(int[] indices, Experiment experiment, int clusterType, Color color);

	/**
	 * Automatically stores clusters based on annotation
	 */
	public void autoStoreClusters(int clusterType, int index);

	/**
	 * Stores indices to a cluster in the manager but doesn't link to a
	 * particular viewer node.
	 */
	public void storeOperationCluster(String source, String clusterID, int[] indices, boolean geneCluster);

	/**
	 * Stores the indices into the cluster repository even if indices
	 * represent a subset of the displayed cluster.
	 */
	public Color storeSubCluster(int[] indices, Experiment experiment, int clusterType);

	/**
	 * Removes the cluster from the repository
	 */
	public boolean removeSubCluster(int[] indices, Experiment experiment, int clusterType);

	/**
	 * Removes the cluster from the repository
	 */
	public boolean removeCluster(int[] indices, Experiment experiment, int clusterType);

	/**
	 * Launches a new Multiple Array Viewer given indices
	 */
	public void launchNewMAV(int[] indices, Experiment experiment, String label, int clusterType);

	/**
	 * Opens viewer a parent node name and a cluster node name
	 */
	public void openClusterNode(String parentNodeName, String childNodeName);

	/**
	 * Returns the specified cluster repository
	 */
	public ClusterRepository getClusterRepository(int clusterType);

	/**
	 * Adds string to history node
	 */
	public void addHistory(String historyEvent);

	/**
	 * Returns the ResultTree object
	 */
	public ResultTree getResultTree();

	/**
	 * Adds result to the ResultTree
	 */
	public void addAnalysisResult(DefaultMutableTreeNode resultNode);

	/** Refreshes current viewer if it's an IViewer * */
	public void refreshCurrentViewer();

	/**
	 * Raktim Nov 02, 2005 CGH Specific methods
	 */

	/**
	 * Access to CGH Display Menu
	 */
	public ICGHDisplayMenu getCghDisplayMenu();

	/**
	 * Access to CGH Clones Menu
	 * 
	 * @return
	 */
	public ICGHCloneValueMenu getCghCloneValueMenu();

	/**
	 * 
	 * @return
	 */
	public Rectangle getViewerBounds();

	/**
	 * Returns the cytobandmodel associated with the CGH Data
	 * 
	 * @return
	 */
	public CytoBandsModel getCytoBandsModel();

	//TODO add comments
	public void broadcastGeneClusters(org.tigr.microarray.mev.cluster.clusterUtil.Cluster[] c);

	public void broadcastGeneCluster(Experiment experiment, int[] cluster, int[] expcluster);

	public void broadcastNamelist(org.tigr.microarray.mev.cluster.clusterUtil.Cluster[] c);

	public void broadcastNamelist(Experiment experiment, int[] cluster);

	public void broadcastNetwork(Vector<int[]> interactions, Vector<String> types, Vector<Boolean> directionals);

	public void broadcastGeneClusterToGenomeBrowser(Experiment experiment, int[] cluster, int[] expcluster);
	/**
     * Generic function for Gaggle to Broadcast any Network
     * @author raktim
     * @param nt Network as defined by Gaggle datatypes
     */
	public void broadcastNet(org.systemsbiology.gaggle.core.datatypes.Network nt);
	
	public boolean isGaggleConnected();

	public boolean requestGaggleConnect();

	public File getSupportFile(ISupportFileDefinition def, boolean getOnline) throws SupportFileAccessError;

	public Hashtable<ISupportFileDefinition, File> getMultipleSupportFiles(IMultiSupportFileDefinition def) throws SupportFileAccessError;

	public boolean hasSupportFile(ISupportFileDefinition def);

	public boolean isResourceManagerAvailable();

	public IResourceManager getResourceManager();

	public Hashtable<ISupportFileDefinition, File> getSupportFiles(Collection<ISupportFileDefinition> defs, boolean getOnline) throws SupportFileAccessError;

	public void storeClusterWithoutDialog(int[]clusterIndices, String source, String factor, String node, String label, String clusterDescription, int clusterType);

}
