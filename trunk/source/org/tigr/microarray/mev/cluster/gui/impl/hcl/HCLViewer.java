/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: HCLViewer.java,v $
 * $Revision: 1.19 $
 * $Date: 2007-09-13 19:07:31 $
 * $Author: braistedj $
 * $State: Exp $
 */
/*
 *Note from N. Bhagabati, 8/7/2002: I have commented out the statement in the addComponents() method that would add the HCL annotation bar to the viewer,
 *as we are now using the Labels as displayed by the ExperimentViewer class, giving us more flexibility to change the label displays as desired. I have,
 *however, left in the code dealing with the HCL annotation bar itself, as this was the least intrusive way to make changes to the code, so as not to set
 off a whole series of unanticipated (and perhaps undesired) consequences
 *
 */

package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.util.FloatMatrix;

public class HCLViewer extends JPanel implements IViewer {

    protected static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
    protected static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";
    protected static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
    protected static final String DELETE_CLUSTER_CMD = "delete-cluster-cmd";
    protected static final String DELETE_ALL_CLUSTERS_CMD = "delete-all-clusters-cmd";
    protected static final String GENE_TREE_PROPERTIES_CMD = "gene-tree-properties-cmd";
    protected static final String SAMPLE_TREE_PROPERTIES_CMD = "sample-tree-properties-cmd";
    protected static final String ROTATE_NODE_CMD = "rotate-node-cmd";
    
    protected static final String SAVE_GENE_ORDER_CMD = "save-gene-order-cmd";
    protected static final String SAVE_EXP_ORDER_CMD = "save-exp-order-cmd";
    protected static final String SAVE_GENE_HEIGHT_CMD = "save-gene-height-cmd";
    protected static final String SAVE_EXP_HEIGHT_CMD = "save-exp-height-cmd";
    
    protected static final String SAVE_GENE_NEWICK_CMD = "save-gene-newick-cmd";
    protected static final String SAVE_SAMPLE_NEWICK_CMD = "save-sample-newick-cmd";
    protected static final String SAVE_GENE_NEXUS_CMD = "save-gene-nexus-cmd";
    protected static final String SAVE_SAMPLE_NEXUS_CMD = "save-sample-nexus-cmd";
      
    // wrapped viewers
    /** component to draw an experiment data */
    protected IViewer expViewer;
    /** component to draw an experiment header */
    protected HCLExperimentHeader header;
    
    /** component to draw genes tree */
    protected HCLTree genesTree;
    
    /** component to draw samples tree */
    protected HCLTree sampleTree;
    
    /** component to draw hcl clusters colors and descriptions */
    protected HCLColorBar colorBar;
    /** component to draw an experiment annotations */
    protected HCLAnnotationBar annotationBar;
    
    protected HCLTreeData genes_result;
    protected HCLTreeData samples_result;
    
    protected IData data;
    protected Experiment experiment;
    protected ArrayList<HCLCluster> clusters = new ArrayList<HCLCluster>();
    protected ArrayList<HCLCluster> experimentClusters = new ArrayList<HCLCluster>();
    protected int [][] sampleClusters;
    public HCLCluster selectedCluster;
    protected int[] genesOrder;
    protected int[] samplesOrder;
    protected boolean isExperimentCluster = false;
    protected boolean featureListIsEmpty = false;
    protected Dimension elementSize;
    protected int numberOfSamples;
    protected int offset = 0;
    protected int clusterIndex = 0;
    protected Listener listener;
    protected JPopupMenu popup;
    private int exptID = 0;
    protected int[] features;
    
    protected DefaultMutableTreeNode node;
    protected IFramework framework;
    
    /**
     * Constructs a <code>HCLViewer</code> for specified results.
     */
    public HCLViewer(Experiment experiment, int[] features, HCLTreeData genes_result, HCLTreeData samples_result) {
    	setLayout(new GridBagLayout());
        setBackground(Color.white);
        this.experiment = experiment;
        this.exptID = experiment.getId();
        listener = new Listener();
        this.addMouseListener(listener);
        this.features = features == null ? createDefaultFeatures(experiment) : features;
        this.expViewer = createExperimentViewer(experiment, features, genes_result, samples_result);
        this.expViewer.getContentComponent().addMouseListener(listener);
        this.header = new HCLExperimentHeader(this.expViewer.getHeaderComponent());
        this.header.addMouseListener(listener);
        this.colorBar = new HCLColorBar(this.clusters, features.length);
        this.colorBar.addMouseListener(listener);
        this.genesOrder = createGenesOrder(experiment, features, genes_result);
        this.annotationBar = new HCLAnnotationBar(this.genesOrder);
        this.annotationBar.addMouseListener(listener);
        if (genes_result != null && experiment.getNumberOfGenes() > 1 && genes_result.node_order.length > 1) {
            this.genesTree = new HCLTree(genes_result, HCLTree.HORIZONTAL);
            this.genesTree.addMouseListener(listener);
            this.genesTree.setListener(listener);
            this.genes_result = genes_result;
        }
        if (samples_result != null && experiment.getNumberOfSamples() > 1 && samples_result.node_order.length > 1) {
            this.sampleTree = new HCLTree(samples_result, HCLTree.VERTICAL);
            this.samplesOrder = createSamplesOrder(samples_result);
            if(genes_result == null)
                this.sampleTree.setHorizontalOffset(10);
            this.sampleTree.addMouseListener(listener);
            this.sampleTree.setListener(listener);  //added for selection of experiment hcl nodes
            this.samples_result = samples_result;
        }
        this.isExperimentCluster = false;
        this.numberOfSamples = experiment.getNumberOfSamples(); //know this is correct for gene clustering constructor
        addComponents(this.sampleTree, this.genesTree, this.expViewer.getContentComponent(), this.colorBar, this.annotationBar);
        this.popup = createJPopupMenu(listener);
    }
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new",
				new Object[]{this.experiment, this.createDefaultFeatures(this.experiment), this.genes_result, this.samples_result, this.sampleClusters, new Boolean(this.isExperimentCluster), this.genesTree, this.sampleTree, new Integer(this.offset), (ExperimentViewer)this.expViewer});
    }
    /**
     * Constructs a <code>HCLViewer</code> for specified results
     * This is the XMLEncoder/Decoder constructor
     */
    public HCLViewer(Experiment e, int[] features, HCLTreeData genesResult, HCLTreeData samplesResult, int [][] sampleClusters, boolean isExperimentCluster, HCLTree genesTree, HCLTree sampleTree, Integer offset, ExperimentViewer expViewer) {
    	setLayout(new GridBagLayout());
        setBackground(Color.white);
        //this.exptID = exptID.intValue();
        this.offset = offset.intValue();        
        this.expViewer = expViewer;
        listener = new Listener();
        this.addMouseListener(listener);
        this.features = features;
        this.isExperimentCluster = isExperimentCluster;
        this.sampleClusters = sampleClusters;
        this.genes_result = genesResult;
        this.samples_result = samplesResult;
        this.genesTree = genesTree;
        this.sampleTree = sampleTree;
        if(genesTree != null) {
        	this.offset = 0;
        	expViewer.setLeftInset(0);
        	expViewer.setInsets(new Insets(0,0,0,0));        	
        } else {
        	sampleTree.setHorizontalOffset(10);
        }
        setExperiment(e);
    }

    
    /**
     * Constructs a <code>HCLViewer</code> for specified results
     */
    public HCLViewer(Experiment experiment, int[] features, HCLTreeData genes_result, HCLTreeData samples_result, int [][] sampleClusters, boolean isExperimentCluster) {
        setLayout(new GridBagLayout());
        setBackground(Color.white);
        this.experiment = experiment;
        this.exptID = experiment.getId();
        listener = new Listener();
        this.addMouseListener(listener);
        features = features == null ? createDefaultFeatures(experiment) : features;
        this.isExperimentCluster = isExperimentCluster;
        if(this.isExperimentCluster)
            this.numberOfSamples = sampleClusters[0].length;  //initial sample count of first cluster
        else
            this.numberOfSamples = experiment.getNumberOfSamples();
        this.genesOrder = createGenesOrder(experiment, createDefaultFeatures(experiment), genes_result);
        this.sampleClusters = sampleClusters;
        this.genes_result = genes_result;
        this.samples_result = samples_result;
        if(this.isExperimentCluster){
            if(genes_result != null){
                offset = 0;
                this.expViewer = new ExperimentClusterViewer(experiment, sampleClusters, genesOrder, true, offset);
            }
            else{
                offset = 10;
                this.expViewer = new ExperimentClusterViewer(experiment, sampleClusters, genesOrder, true, offset);
            }
        }
        else {
            this.expViewer = createExperimentViewer(experiment, features, genes_result, samples_result);
        }
        this.expViewer.getContentComponent().addMouseListener(listener);
        this.header = new HCLExperimentHeader(this.expViewer.getHeaderComponent());
        this.header.addMouseListener(listener);
        this.colorBar = new HCLColorBar(this.clusters, features.length);
        this.colorBar.addMouseListener(listener);
        this.annotationBar = new HCLAnnotationBar(this.genesOrder);
        this.annotationBar.addMouseListener(listener);
/*EH
        if (genes_result != null && experiment.getNumberOfGenes() > 1) {
            this.genesTree = new HCLTree(genes_result, HCLTree.HORIZONTAL);
            this.genesTree.addMouseListener(listener);
            this.genesTree.setListener(listener);
        }
        if (samples_result != null && experiment.getNumberOfSamples() > 1) {
            this.sampleTree = new HCLTree(samples_result, HCLTree.VERTICAL);
            //           this.samplesOrder = createSamplesOrder(samples_result, sampleClusters[0]);
            if(genes_result == null)
                this.sampleTree.setHorizontalOffset(10);
            this.sampleTree.addMouseListener(listener);
            this.sampleTree.setListener(listener);
        }
*/
        addComponents(this.sampleTree, this.genesTree, this.expViewer.getContentComponent(), this.colorBar, this.annotationBar);
        this.popup = createJPopupMenu(listener);
    }
    
    /**
     * Constructs a <code>HCLViewer</code> for specified results.
     */
    public HCLViewer(Experiment experiment, int[] features, HCLTreeData genes_result, HCLTreeData samples_result, DefaultMutableTreeNode node) {
    	setLayout(new GridBagLayout());
        setBackground(Color.white);
        this.experiment = experiment;
        this.exptID = experiment.getId();
        listener = new Listener();
        this.addMouseListener(listener);
        this.node = node;
        features = features == null ? createDefaultFeatures(experiment) : features;
        this.expViewer = createExperimentViewer(experiment, features, genes_result, samples_result);
        this.expViewer.getContentComponent().addMouseListener(listener);
        this.header = new HCLExperimentHeader(this.expViewer.getHeaderComponent());
        this.header.addMouseListener(listener);
        this.colorBar = new HCLColorBar(this.clusters, features.length);
        this.colorBar.addMouseListener(listener);
        this.genesOrder = createGenesOrder(experiment, features, genes_result);
        this.annotationBar = new HCLAnnotationBar(this.genesOrder);
        this.annotationBar.addMouseListener(listener);
        if (genes_result != null && experiment.getNumberOfGenes() > 1 && genes_result.node_order.length > 1) {
            this.genesTree = new HCLTree(genes_result, HCLTree.HORIZONTAL);
            this.genesTree.addMouseListener(listener);
            this.genesTree.setListener(listener);
            this.genes_result = genes_result;
        }
        if (samples_result != null && experiment.getNumberOfSamples() > 1 && samples_result.node_order.length > 1) {
            this.sampleTree = new HCLTree(samples_result, HCLTree.VERTICAL);
            this.samplesOrder = createSamplesOrder(samples_result);
            if(genes_result == null)
                this.sampleTree.setHorizontalOffset(10);
            this.sampleTree.addMouseListener(listener);
            this.sampleTree.setListener(listener);  //added for selection of experiment hcl nodes
            this.samples_result = samples_result;
        }
        this.isExperimentCluster = false;
        this.numberOfSamples = experiment.getNumberOfSamples(); //know this is correct for gene clustering constructor
        addComponents(this.sampleTree, this.genesTree, this.expViewer.getContentComponent(), this.colorBar, this.annotationBar);
        this.popup = createJPopupMenu(listener);
    }
    
    
    public void setExperiment(Experiment e) {
    	this.experiment = e;
    	this.exptID = e.getId();
    	setLayout(new GridBagLayout());
        setBackground(Color.white);
        listener = new Listener();
        this.addMouseListener(listener);
        features = features == null ? createDefaultFeatures(experiment) : features;
        this.expViewer.getContentComponent().addMouseListener(listener);
        this.expViewer.setExperiment(this.experiment);
        this.header = new HCLExperimentHeader(this.expViewer.getHeaderComponent());
        this.header.addMouseListener(listener);
        this.colorBar = new HCLColorBar(this.clusters, features.length);
        this.colorBar.addMouseListener(listener);
        this.genesOrder = createGenesOrder(experiment, features, genes_result);
        this.annotationBar = new HCLAnnotationBar(this.genesOrder);
        this.annotationBar.addMouseListener(listener);
        if (genes_result != null && experiment.getNumberOfGenes() > 1 && genes_result.node_order.length > 1) {
            this.genesTree.addMouseListener(listener);
            this.genesTree.setListener(listener);
            this.genesTree.deselectAllNodes();
        }
        if (samples_result != null && experiment.getNumberOfSamples() > 1 && samples_result.node_order.length > 1) {
            this.samplesOrder = createSamplesOrder(samples_result);
            if(genes_result == null)
                this.sampleTree.setHorizontalOffset(10);
            this.sampleTree.addMouseListener(listener);
            this.sampleTree.setListener(listener);  //added for selection of experiment hcl nodes
            this.sampleTree.deselectAllNodes();
        }
        this.isExperimentCluster = false;
        this.numberOfSamples = experiment.getNumberOfSamples(); //know this is correct for gene clustering constructor
        addComponents(this.sampleTree, this.genesTree, this.expViewer.getContentComponent(), this.colorBar, this.annotationBar);
        this.popup = createJPopupMenu(listener);
        
    }
    public void setExperimentID(int id) {
    	this.exptID = id;
    }
    public int getExperimentID() {return this.exptID;}
    
    /**
     * Adds wrapped viewers.
     */
    protected void addComponents(JComponent sTree, JComponent gTree, JComponent exp, JComponent cBar, JComponent aBar) {
        final int rows = sTree == null ? 1 : 2;
        final int cols = gTree == null ? 3 : 4;
        if (sTree != null) {
            add(sTree, new GridBagConstraints(cols-3, rows-2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        }
        if (gTree != null) {
            add(gTree, new GridBagConstraints(cols-4, rows-1, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        }
        add(exp,  new GridBagConstraints(cols-3, rows-1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }
    
    
    
    protected IViewer createExperimentViewer(Experiment experiment, int[] features, HCLTreeData genes_result, HCLTreeData samples_result) {
        int[][] clusters = createClusters(experiment, features, genes_result);
        int [] samples = getLeafOrder(samples_result, null);
        IViewer viewer;
        if(genes_result != null){
            offset = 0;
            viewer = new ExperimentViewer(experiment, clusters, samples, true, offset);
        }
        else{
            offset = 10;
            viewer = new ExperimentViewer(experiment, clusters, samples, true, offset);
        }
        ((ExperimentViewer)viewer).setEnableMoveable(false);
        return viewer;
    }
    
    
    protected int[] createDefaultFeatures(Experiment experiment) {
        int[] features = new int[experiment.getNumberOfGenes()];
        for (int i=0; i<features.length; i++) {
            features[i] = i;
        }
        return features;
    }
    
    /**
     * Creates a cluster for wrapped experiment viewer.
     */
    private int[][] createClusters(Experiment experiment, int[] features, HCLTreeData genes_result) {
        int [][] clusters = new int[1][features.length];
        clusters[0] = createGenesOrder(experiment, features, genes_result);
        return clusters;
    }
    
    protected int [] createSamplesOrder(HCLTreeData samples_result){
        return createSamplesOrder(samples_result, null);
    }
    
    protected int [] createSamplesOrder(HCLTreeData samples_result, int [] indices){
        return getLeafOrder(samples_result, indices);
    }
    
    protected int[] createGenesOrder(Experiment experiment, int[] features, HCLTreeData genes_result) {
        int[] order = getLeafOrder(genes_result, features);
        if (order == null) {
            order = features;
        }
        return order;
    }
    
    private int[] getLeafOrder(HCLTreeData result, int[] indices) {
        if (result == null || result.node_order.length < 2) {
            return null;
        }
        return getLeafOrder(result.node_order, result.child_1_array, result.child_2_array, indices);
    }
    
    private int[] getLeafOrder(int[] nodeOrder, int[] child1, int[] child2, int[] indices) {
        int[] leafOrder = new int[nodeOrder.length];
        Arrays.fill(leafOrder, -1);
        fillLeafOrder(leafOrder, child1, child2, 0, child1.length-2, indices);
        return leafOrder;
    }
    
    private int fillLeafOrder(int[] leafOrder, int[] child1, int[] child2, int pos, int index, int[] indices) {
        if (child1[index] != -1) {
            pos = fillLeafOrder(leafOrder, child1, child2, pos, child1[index], indices);
        }
        if (child2[index] != -1) {
            pos = fillLeafOrder(leafOrder, child1, child2, pos, child2[index], indices);
        } else {
            leafOrder[pos] = indices == null ? index : indices[index];
            pos++;
        }
        return pos;
    }
    
    /**
     * Returns this viewer to be inserted into the framework scroll pane.
     */
    public JComponent getContentComponent() {
        return this;
    }
    
    /**
     * Returns the viewer header.
     */
    public JComponent getHeaderComponent() {
        return this.header;
    }
    
    /**
     * Updates all wrapped viewers.
     */
    public void onSelected(IFramework framework) {
        this.framework = framework;
        this.data = framework.getData();
        this.expViewer.onSelected(framework);
        
        //   onDataChanged(this.data);
        
        Object userObject = framework.getUserObject();
        this.clusterIndex = (userObject != null ? ((Integer)userObject).intValue():0);
        if (this.genesTree != null) {
            this.genesTree.onSelected(framework);
        }
        if (this.sampleTree != null) {
            this.sampleTree.onSelected(framework);
        }
        this.annotationBar.onSelected(framework);
        this.colorBar.onSelected(framework);
        // set expression header position
        if (this.genesTree != null) {
            this.header.setHeaderPosition(this.genesTree.getWidth());
        }
        this.elementSize = framework.getDisplayMenu().getElementSize();
        this.header.updateSize(getCommonWidth(), this.elementSize.width);
        
        if(this.node == null)
            this.node = (DefaultMutableTreeNode)(framework.getCurrentNode().getParent());
        
        verifyClusterExistence(this.data);
        updateTrees();
        refreshViewer();
    }
    
    /**
     * Calculate the viewer width.
     */
    protected int getCommonWidth() {
        int width = 0;
        if (this.genesTree != null) {
            width += this.genesTree.getWidth();
        }
        if(this.isExperimentCluster)
            width += ((ExperimentClusterViewer)this.expViewer).getWidth();
        else
            width += ((ExperimentViewer)this.expViewer).getWidth();
        width += this.colorBar.getWidth();
        width += this.annotationBar.getWidth();
        return width + offset;
    }
    
    /**
     * Delegates this invokation to the wrapped experiment viewer.
     */
    public void onDataChanged(IData data) {
        this.expViewer.onDataChanged(data);
        this.data = data;
        updateTrees();
    }
    
    
    /**
     * Delegates this invokation to wrapped viewers.
     */
    public void onMenuChanged(IDisplayMenu menu) {
        this.expViewer.onMenuChanged(menu);
        if (this.genesTree != null) {
            this.genesTree.onMenuChanged(menu);
        }
        if (this.sampleTree != null) {
            this.sampleTree.onMenuChanged(menu);
        }
        this.annotationBar.onMenuChanged(menu);
        this.colorBar.onMenuChanged(menu);
        // expression header can change its size
        this.elementSize = menu.getElementSize();
        this.header.updateSize(getCommonWidth(), this.elementSize.width);
    }
    
    public void onDeselected() {}
    public void onClosed() {}
    
    public BufferedImage getImage() {
        return null;
    }
    
    /**
     * Creates a popup menu.
     */
    private JPopupMenu createJPopupMenu(Listener listener) {
        JPopupMenu popup = new JPopupMenu();
        addMenuItems(popup, listener);
        return popup;
    }
    
    /**
     * Adds menu items to the specified popup menu.
     */
    protected void addMenuItems(JPopupMenu menu, Listener listener) {
        JMenuItem menuItem;
        
        menuItem = new JMenuItem("Store Cluster", GUIFactory.getIcon("new16.gif"));
        menuItem.setEnabled(false);
        menuItem.setActionCommand(STORE_CLUSTER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Launch new session", GUIFactory.getIcon("launch_new_mav.gif"));
        menuItem.setEnabled(false);
        menuItem.setActionCommand(LAUNCH_NEW_SESSION_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Save cluster...", GUIFactory.getIcon("save_as16.gif"));
        menuItem.setEnabled(false);
        menuItem.setActionCommand(SAVE_CLUSTER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Delete cluster", GUIFactory.getIcon("delete16.gif"));
        menuItem.setEnabled(false);
        menuItem.setActionCommand(DELETE_CLUSTER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Delete all clusters", GUIFactory.getIcon("delete16.gif"));
        menuItem.setEnabled(false);
        menuItem.setActionCommand(DELETE_ALL_CLUSTERS_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("GeneTree properties...", GUIFactory.getIcon("edit16.gif"));
        menuItem.setEnabled(this.genesTree != null);
        menuItem.setActionCommand(GENE_TREE_PROPERTIES_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("SampleTree properties...", GUIFactory.getIcon("edit16.gif"));
        menuItem.setEnabled(this.sampleTree != null);
        menuItem.setActionCommand(SAMPLE_TREE_PROPERTIES_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);        

        menuItem = new JMenuItem("Rotate Selected Node", GUIFactory.getIcon("edit16.gif"));
        menuItem.setEnabled(false);
        menuItem.setActionCommand(ROTATE_NODE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Save Gene Node Heights", GUIFactory.getIcon("save_as16.gif"));
        menuItem.setEnabled(this.genesTree != null);
        menuItem.setActionCommand(SAVE_GENE_HEIGHT_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Save Gene Order", GUIFactory.getIcon("save_as16.gif"));
        menuItem.setEnabled(this.genesTree != null);
        menuItem.setActionCommand(SAVE_GENE_ORDER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Save Sample Node Heights", GUIFactory.getIcon("save_as16.gif"));
        menuItem.setEnabled(this.sampleTree != null);
        menuItem.setActionCommand(SAVE_EXP_HEIGHT_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Save Sample Order", GUIFactory.getIcon("save_as16.gif"));
        menuItem.setEnabled(this.sampleTree != null);
        menuItem.setActionCommand(SAVE_EXP_ORDER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        if(this.genesTree != null) {
            menuItem = new JMenuItem("Save Gene Tree To Newick File", GUIFactory.getIcon("save_as16.gif"));
            menuItem.setActionCommand(SAVE_GENE_NEWICK_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
        }
        
        if(this.sampleTree != null) {
            menuItem = new JMenuItem("Save Sample Tree to Newick File", GUIFactory.getIcon("save_as16.gif"));
            menuItem.setActionCommand(SAVE_SAMPLE_NEWICK_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
        }
        
        menu.addSeparator();
        
     /*   if(this.genesTree != null) {
            menuItem = new JMenuItem("Save Gene Tree To Nexus File", GUIFactory.getIcon("save_as16.gif"));
            menuItem.setActionCommand(SAVE_GENE_NEXUS_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
        }
       */
        
        if(this.sampleTree != null) {
            menuItem = new JMenuItem("Save Sample Tree to Nexus File", GUIFactory.getIcon("save_as16.gif"));
            menuItem.setActionCommand(SAVE_SAMPLE_NEXUS_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
        }   
        menuItem = new JMenuItem("Broadcast Matrix to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
        menuItem.setActionCommand(ExperimentViewer.BROADCAST_MATRIX_GAGGLE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Broadcast Gene List to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
        menuItem.setActionCommand(ExperimentViewer.BROADCAST_NAMELIST_GAGGLE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
    }
    
    public void broadcastClusterGaggle() {
    	int[] rows;
    	int[] cols;
    	if(selectedCluster != null) {
	    	if(selectedCluster.isGeneCluster) {
	    		rows = getSubTreeElements();
	    		cols = getExperiment().getColumnIndicesCopy();
	    	} else { //isExperimentCluster
	    		rows = getExperiment().getRows();
	    		cols = getSubTreeElements();
	    	}
	    	framework.broadcastGeneCluster(getExperiment(), rows, cols);
    	} else {
    		//Nothing selected by user, so broadcast complete experiment. 
    		framework.broadcastGeneCluster(getExperiment(), getExperiment().getRows(), null);
    	}
    }
    public void broadcastNamelistGaggle() {
    	int[] rows;
    	if(selectedCluster != null) {
	    	if(selectedCluster.isGeneCluster) {
	    		rows = getSubTreeElements();
	    	} else {
	    		rows = getExperiment().getRows();
	    	}
	    	framework.broadcastNamelist(getExperiment(), rows);
    	} else {
    		framework.broadcastNamelist(getExperiment(), getExperiment().getRows());
    	}
    }
    
    /**
     * Returns a menu item by specified action command.
     * @return null, if menu item was not found.
     */
    protected JMenuItem getJMenuItem(String command) {
        Component[] components = popup.getComponents();
        for (int i=0; i<components.length; i++) {
            if (components[i] instanceof JMenuItem) {
                if (((JMenuItem)components[i]).getActionCommand().equals(command))
                    return(JMenuItem)components[i];
            }
        }
        return null;
    }
    
    /**
     * Sets menu enabled flag.
     */
    protected void setEnableMenuItem(String command, boolean enable) {
        JMenuItem item = getJMenuItem(command);
        if (item == null) {
            return;
        }
        item.setEnabled(enable);
    }
    
    /**
     * Selects the specified cluster.
     */
    protected void setSelectedCluster(HCLCluster cluster) {
        this.selectedCluster = cluster;
        if(this.isExperimentCluster){  //viewer type,
            if(cluster.isGeneCluster)
                ((ExperimentClusterViewer)this.expViewer).selectRows(cluster.firstElem, cluster.lastElem);
            else
                ((ExperimentClusterViewer)this.expViewer).selectColumns(cluster.firstElem, cluster.lastElem);
        }
        else{
            if(cluster.isGeneCluster)
                ((ExperimentViewer)this.expViewer).selectRows(cluster.firstElem, cluster.lastElem);
            else
                ((ExperimentViewer)this.expViewer).selectColumns(cluster.firstElem, cluster.lastElem);
        }
    }
    
    /**
     * Returns a <code>HCLCluster</code> for specified root node.
     */
    protected HCLCluster getCluster(HCLCluster selCluster) {
        int root = selCluster.root;
        HCLCluster cluster;
        if(selCluster.isGeneCluster){
            for (int i=0; i<this.clusters.size(); i++) {
                cluster = (HCLCluster)this.clusters.get(i);
                if (cluster.root == root) {
                    return cluster;
                }
            }
        }
        else {
            for (int i=0; i<this.experimentClusters.size(); i++) {
                cluster = (HCLCluster)this.experimentClusters.get(i);
                if (cluster.root == root) {
                    return cluster;
                }
            }
        }
        return null;
    }
    
    public void updateTrees(){
        if(this.genesTree != null && this.data.getColors().length == 0){
            this.genesTree.deselectAllNodes();
            this.genesTree.resetNodeColors();
            this.clusters.clear();
        }
        if(this.sampleTree != null && this.data.getExperimentColors().length == 0){
            this.sampleTree.deselectAllNodes();
            this.sampleTree.resetNodeColors();
            this.experimentClusters.clear();
        }
    }
    
    /**
     * Returns a <code>HCLCluster</code> for specified root node.
     */
    protected HCLCluster getExperimentCluster(int root) {
        HCLCluster cluster;
        for (int i=0; i<this.experimentClusters.size(); i++) {
            cluster = (HCLCluster)this.experimentClusters.get(i);
            if (cluster.root == root) {
                return cluster;
            }
        }
        return null;
    }
    
    
    
    /**
     * Removes cluster for specified root.
     */
    protected void removeCluster(HCLCluster oldCluster) {
        HCLCluster cluster;
        if(oldCluster.isGeneCluster){
            for (int i=this.clusters.size(); --i>=0;) {
                cluster = (HCLCluster)this.clusters.get(i);
                if (cluster.root == oldCluster.root) {
                    this.clusters.remove(i);
                    return;
                }
            }
        }
        else {
            for (int i=this.experimentClusters.size(); --i>=0;) {
                cluster = (HCLCluster)this.experimentClusters.get(i);
                if (cluster.root == oldCluster.root) {
                    this.experimentClusters.remove(i);
                    return;
                }
            }
        }
    }
    
    /**
     * Returns true, if there is cluster for selected node.
     */
    protected boolean doesClusterExist() {
        if (this.selectedCluster == null) {
            return false;
        }
        return getCluster(this.selectedCluster) != null;
    }
    
    /**
     * Returns count of clusters.
     */
    protected int getClustersCount() {
        return this.clusters.size() + this.experimentClusters.size();
    }
    
    private Frame getFrame() {
        return JOptionPane.getFrameForComponent(this);
    }
    
    /**
     * Sets a public color for a selected cluster.
     */
    public void onSetCluster() {
        //Color newColor = JColorChooser.showDialog(getFrame(), "Choose color", new Color(128, 128, 128));
        Color newColor = Color.white;
        if (newColor == null || this.selectedCluster == null) {
            return;
        }
        HCLCluster cluster = getCluster(this.selectedCluster);
//        boolean notANewNode = true;
        
        if (cluster != null) {              //cluster already exists
            this.selectedCluster = cluster;
//            notANewNode = false;
        } else {                            //new cluster, add to list
            if(selectedCluster.isGeneCluster)
                this.clusters.add(this.selectedCluster);
            else
                this.experimentClusters.add(this.selectedCluster);
        }
        
        this.selectedCluster.color = newColor;
        
        if(!this.isExperimentCluster){
            if(!this.selectedCluster.isGeneCluster)
                this.selectedCluster.color = ((ExperimentViewer)(this.expViewer)).setHCLClusterColor(getSubTreeElements(),this.selectedCluster.color, this.selectedCluster.isGeneCluster);
            else
                this.selectedCluster.color = ((ExperimentViewer)(this.expViewer)).setHCLClusterColor(getArrayMappedToData(getSubTreeElements()),this.selectedCluster.color, this.selectedCluster.isGeneCluster);
        }
        else{
            if(!this.selectedCluster.isGeneCluster)
                this.selectedCluster.color = ((ExperimentClusterViewer)(this.expViewer)).setHCLClusterColor(getSubTreeElements(),this.selectedCluster.color, this.selectedCluster.isGeneCluster);
            else
                this.selectedCluster.color = ((ExperimentClusterViewer)(this.expViewer)).setHCLClusterColor(getArrayMappedToData(getSubTreeElements()),this.selectedCluster.color, this.selectedCluster.isGeneCluster);
        }
        
        //    if(!notANewNode)
        removeSubTreeClusters(this.selectedCluster); //need to remove clusters from the list if they are sub-trees
        
        if(selectedCluster.isGeneCluster)
            this.colorBar.onClustersChanged(this.clusters);
        
        refreshViewer();
        
        this.onDataChanged(data);
        this.header.updateSize(getCommonWidth(), this.elementSize.width);
        revalidate();
    }
    
    /**
     *  Removes subtree clusters when an enclosing tree
     */
    private void removeSubTreeClusters(HCLCluster parent){
        HCLCluster cluster;
        if(parent.isGeneCluster){
            //   for(int i = 0; i < this.clusters.size(); i++){
            for(int i = this.clusters.size()-1; i >= 0 ; i--){
                cluster = (HCLCluster)clusters.get(i);
                if( !parent.equals(cluster) && isSubTree(parent, cluster) ){
                    this.removeCluster(cluster);
                }
            }
        }
        else {
            for(int i = 0; i < this.experimentClusters.size(); i++){
                cluster = (HCLCluster)experimentClusters.get(i);
                if( parent != cluster && isSubTree(parent, cluster) )
                    this.removeCluster(cluster);
            }
        }
    }
    
    private boolean isSubTree(HCLCluster parent, HCLCluster child){
        if( parent.size < child.size )
            return false;
        if( child.firstElem >= parent.firstElem &&
        child.firstElem < parent.lastElem)
            return true;
        else
            return false;
    }
    
    
    /**
     * Sets cluster description.
     */
    public void onSetClusterText() {
        HCLCluster cluster = getCluster(this.selectedCluster);
        if (cluster == null) {
            JOptionPane.showMessageDialog(getFrame(), "Not a cluster!", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            String text = JOptionPane.showInputDialog(getFrame(), "Cluster text");
            if (text != null && text.length() > 0) {
                cluster.text = text;
                this.colorBar.onClustersChanged(this.clusters);
                this.header.updateSize(getCommonWidth(), this.elementSize.width);
                revalidate();
            }
        }
    }
    
    public void verifyClusterExistence(IData data){
        HCLCluster currCluster;
        int geneIndex;
        boolean [] clusterGone = new boolean[this.clusters.size()];
        boolean [] expClusterGone = new boolean[this.experimentClusters.size()];
        
        for( int i = 0; i < this.clusters.size() ; i++){
            currCluster = (HCLCluster)this.clusters.get(i);
            if(currCluster.firstElem == currCluster.lastElem)
                continue;
            geneIndex = experiment.getGeneIndexMappedToData(this.genesOrder[currCluster.firstElem]);
            if(data.getProbeColor(geneIndex) != currCluster.color){
                clusterGone[i] = true;
            }
        }
        for(int i = clusterGone.length-1; i >= 0; i--){
            if(clusterGone[i]){
                this.genesTree.setNodeColor(((HCLCluster)this.clusters.get(i)).root, null);
                //   this.clusters.remove(i);
            }
        }
        int expIndex;
        for( int i = 0; i < this.experimentClusters.size() ; i++){
            currCluster = (HCLCluster)this.experimentClusters.get(i);
            if(currCluster.firstElem == currCluster.lastElem)
                continue;
            if(this.isExperimentCluster)
                expIndex = experiment.getSampleIndex(this.sampleClusters[this.clusterIndex][currCluster.firstElem]);
            else
                expIndex = experiment.getSampleIndex(this.samplesOrder[currCluster.firstElem]);
            if(data.getExperimentColor(expIndex) != currCluster.color){
                expClusterGone[i] = true;
            }
        }
        for(int i = expClusterGone.length-1; i >= 0; i--){
            if(expClusterGone[i]){
                this.sampleTree.setNodeColor(((HCLCluster)this.experimentClusters.get(i)).root, null);
                //  this.experimentClusters.remove(i);
            }
        }
    }
    
    private int[] getSubTreeElements() {
        return getSubTreeElements(this.selectedCluster);
    }
    
    private int[] getSubTreeElements(HCLCluster cluster) {
        int size = cluster.lastElem-cluster.firstElem+1;
        int[] elements = new int[size];
        
        //cluster is a cluster of genes
        if(cluster.isGeneCluster){
            for (int i=0; i<size; i++) {
            		elements[i] = this.genesOrder[cluster.firstElem+i];
            }
        }
        
        //cluster is a cluster of experiments
        else {
            if(!this.isExperimentCluster){  //created by gene clustering algorithm
                for (int i= 0; i< size ; i++) {
                    elements[i] = this.samplesOrder[cluster.firstElem+i];
                }
            }
            else {  //created by experiment clustering algorthm
                for (int i= 0; i< size ; i++) {
                    elements[i] = this.sampleClusters[this.clusterIndex][cluster.firstElem+i];
                }
            }
        }
        return elements;
    }
    
    public void saveGenesOrder(){
        try{
            ExperimentUtil.saveExperiment(getFrame(), this.experiment, this.data, this.genesOrder);
        }catch (Exception e){
            JOptionPane.showMessageDialog(getFrame(), "Can not save data!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    public void saveExperimentOrder(){
        try{
            if(!this.isExperimentCluster)
                ExperimentUtil.saveExperimentCluster(getFrame(), this.experiment, this.data, this.samplesOrder);
            else{
                int elements[] = new int[this.sampleClusters[this.clusterIndex].length];
                for (int i= 0; i< this.sampleClusters[this.clusterIndex].length ; i++) {
                    elements[i] = this.sampleClusters[this.clusterIndex][i];
                }
                ExperimentUtil.saveExperimentCluster(getFrame(), this.experiment, this.data, elements);
            }
        }catch (Exception e){
            JOptionPane.showMessageDialog(getFrame(), "Can not save data!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Saves cluster.
     */
    public void onSaveCluster() {
        if(this.selectedCluster == null)
            return;
        try {
            if(this.selectedCluster.isGeneCluster)
                ExperimentUtil.saveExperiment(getFrame(), this.experiment, this.data, getSubTreeElements());
            else
                ExperimentUtil.saveExperimentCluster(getFrame(), this.experiment, this.data, getSubTreeElements());
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(getFrame(), "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Deletes the selected cluster.
     */
    public void onDeleteCluster() {
        HCLCluster cluster;
        removeCluster(this.selectedCluster);
        
        this.colorBar.onClustersChanged(this.clusters);
        if(this.selectedCluster.isGeneCluster){
            this.genesTree.setNodeColor(this.selectedCluster.root, null);
            this.data.setProbesColor(getArrayMappedToData(getSubTreeElements()), null);
            this.framework.removeSubCluster(getArrayMappedToData(getSubTreeElements()), this.experiment, Cluster.GENE_CLUSTER);
            // restore sub tree colors
            for (int i=0; i<this.clusters.size(); i++) {
                cluster = (HCLCluster)this.clusters.get(i);
                this.data.setProbesColor(getArrayMappedToData(getSubTreeElements(cluster)), cluster.color);
                this.genesTree.setNodeColor(cluster.root, cluster.color);
            }
        }
        else{  //working on sample tree
            this.sampleTree.setNodeColor(this.selectedCluster.root, null);
            this.data.setExperimentColor(getSubTreeElements(), null);
            this.framework.removeSubCluster(getSubTreeElements(), this.experiment, Cluster.EXPERIMENT_CLUSTER);
            
            // restore sub tree colors
            for (int i=0; i<this.experimentClusters.size(); i++) {
                cluster = (HCLCluster)this.experimentClusters.get(i);
                this.data.setExperimentColor(getSubTreeElements(cluster), cluster.color);
                this.sampleTree.setNodeColor(cluster.root, cluster.color);
            }
        }
        this.header.updateSize(getCommonWidth(), this.elementSize.width);
        this.onDataChanged(data);
        refreshViewer();
        revalidate();
    }
    
    private void refreshViewer(){
        HCLCluster cluster;
        if(this.selectedCluster == null)
            return;
        
        if(selectedCluster.isGeneCluster){
            for (int i=0; i<this.clusters.size(); i++) {
                cluster = (HCLCluster)this.clusters.get(i);
                this.genesTree.setNodeColor(cluster.root, cluster.color);
                this.genesTree.deselectAllNodes();
            }
        }
        else {
            for (int i=0; i<this.experimentClusters.size(); i++) {
                cluster = (HCLCluster)this.experimentClusters.get(i);
                this.sampleTree.setNodeColor(cluster.root, cluster.color);
                this.sampleTree.deselectAllNodes();
            }
        }
        
        if(this.isExperimentCluster){
            ((ExperimentClusterViewer)(this.expViewer)).selectRows(-1,-1);
            ((ExperimentClusterViewer)(this.expViewer)).selectColumns(-1,-1);
        } else {
            ((ExperimentViewer)(this.expViewer)).selectRows(-1,-1);
            ((ExperimentViewer)(this.expViewer)).selectColumns(-1,-1);
        }
    }
    
    private int [] getArrayMappedToData(int [] clusterIndices){
        if( clusterIndices == null || clusterIndices.length < 1)
            return clusterIndices;
        
        int [] dataIndices = new int [clusterIndices.length];
        for(int i = 0; i < clusterIndices.length; i++){
            dataIndices[i] = this.experiment.getGeneIndexMappedToData(clusterIndices[i]);
        }
        return dataIndices;
    }
    
    
    /**
     *  Created viewers for clusters and places them on the analysis tree
     */
    private void createAndAddClusterViews(HCLTree tree){
        int k = tree.getNumberOfTerminalNodes();
        
        DefaultMutableTreeNode newNode;
        if(tree == this.genesTree)
            newNode = new DefaultMutableTreeNode("Gene Tree Cut: "+String.valueOf(k)+" Clusters");
        else
            newNode = new DefaultMutableTreeNode("Sample Tree Cut: "+String.valueOf(k)+" Clusters");
        
        int [][] clusters = tree.getClusterRowIndices();
        
        if(tree == genesTree){
            for(int i = 0; i < clusters.length; i++){
                for(int j = 0; j < clusters[i].length; j++)
                    clusters[i][j] = this.genesOrder[clusters[i][j]];
            }
        } else {
            for(int i = 0; i < clusters.length; i++){
                for(int j = 0; j < clusters[i].length; j++)
                    clusters[i][j] = this.samplesOrder[clusters[i][j]];
            }
        }
        
        addExpressionImages(newNode, clusters, tree == this.genesTree);
        addCentroidViews(newNode, clusters, tree == this.genesTree);
        addClusterTableViews(newNode, clusters, tree == this.genesTree);
        addClusterInfo(newNode, clusters, tree == this.genesTree, tree.getZeroThreshold());
        addGeneralInfo(newNode, tree.getZeroThreshold(), k, tree == this.genesTree);
        this.framework.addNode(this.node, newNode);
    }
    
    private void addClusterTableViews(DefaultMutableTreeNode node, int [][] clusters, boolean geneClusters) {
        DefaultMutableTreeNode tabNode = new DefaultMutableTreeNode("Table Views");   
        IViewer tabViewer;
        if (geneClusters)
            tabViewer = new ClusterTableViewer(this.experiment, clusters, this.data);
        else
            tabViewer = new ExperimentClusterTableViewer(this.experiment, clusters, this.data);
            //return; // placeholder for ExptClusterTableViewer
        
        for(int i = 0; i < clusters.length; i++){
            tabNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), tabViewer, new Integer(i))));
        }
        node.add(tabNode);        
    }
    
    /**
     *  Adds cluster expression images
     */
    private void addExpressionImages(DefaultMutableTreeNode node, int [][] clusters, boolean geneClusters){
        DefaultMutableTreeNode expNode = new DefaultMutableTreeNode("Expression Images");
        
        IViewer expViewer;
        if(geneClusters)
            expViewer = new HCLExperimentViewer(this.experiment, clusters);
        else
            expViewer = new HCLExperimentClusterViewer(this.experiment, clusters);
        
        for(int i = 0; i < clusters.length; i++){
            expNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expViewer, new Integer(i))));
        }
        node.add(expNode);
    }
    
    
    
    /**
     * Adds nodes to display centroid charts.
     */
    private void addCentroidViews(DefaultMutableTreeNode root, int [][] clusters, boolean clusterGenes) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        FloatMatrix data = this.experiment.getMatrix();
        
        if(!clusterGenes){
            data = data.transpose();
        }
        
        FloatMatrix means = getMeans(data, clusters);
        FloatMatrix variances = getVariances(data, means, clusters);
        
        HCLCentroidViewer centroidViewer;
        ExperimentClusterCentroidViewer expCentroidViewer;
        if(clusterGenes){
            centroidViewer = new HCLCentroidViewer(this.experiment, clusters);
            centroidViewer.setMeans(means.A);
            centroidViewer.setVariances(variances.A);
            for (int i=0; i<clusters.length; i++) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            
            HCLCentroidsViewer centroidsViewer = new HCLCentroidsViewer(this.experiment, clusters);
            centroidsViewer.setMeans(means.A);
            centroidsViewer.setVariances(variances.A);
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
            
        }
        else{
            expCentroidViewer = new HCLExperimentCentroidViewer(this.experiment, clusters);
            
            expCentroidViewer.setMeans(means.A);
            expCentroidViewer.setVariances(variances.A);
            for (int i=0; i<clusters.length; i++) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            HCLExperimentCentroidsViewer expCentroidsViewer = new HCLExperimentCentroidsViewer(this.experiment, clusters);
            expCentroidsViewer.setMeans(means.A);
            expCentroidsViewer.setVariances(variances.A);
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", expCentroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", expCentroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        }
        root.add(centroidNode);
        root.add(expressionNode);
    }
    
    /**
     *  Calculates means for the clusters
     */
    private FloatMatrix getMeans(FloatMatrix data, int [][] clusters){
        FloatMatrix means = new FloatMatrix(clusters.length, data.getColumnDimension());
        for(int i = 0; i < clusters.length; i++){
            means.A[i] = getMeans(data, clusters[i]);
        }
        return means;
    }
    
    
    /**
     *  Returns a set of means for an element
     */
    private float [] getMeans(FloatMatrix data, int [] indices){
        int nSamples = data.getColumnDimension();
        float [] means = new float[nSamples];
        float sum = 0;
        float n = 0;
        float value;
        for(int i = 0; i < nSamples; i++){
            n = 0;
            sum = 0;
            for(int j = 0; j < indices.length; j++){
                value = data.get(indices[j],i);
                if(!Float.isNaN(value)){
                    sum += value;
                    n++;
                }
            }
            if(n > 0)
                means[i] = sum/n;
            else
                means[i] = Float.NaN;
        }
        return means;
    }
    
    /**
     * Returns a matrix of standard deviations grouped by cluster and element
     */
    private FloatMatrix getVariances(FloatMatrix data, FloatMatrix means, int [][] clusters){
        int nSamples = data.getColumnDimension();
        FloatMatrix variances = new FloatMatrix(clusters.length, nSamples);
        for(int i = 0; i < clusters.length; i++){
            variances.A[i] = getVariances(data, means, clusters[i], i);
        }
        return variances;
    }
    
    private float [] getVariances(FloatMatrix data, FloatMatrix means, int [] indices, int clusterIndex){
        int nSamples = data.getColumnDimension();
        float [] variances = new float[nSamples];
        float sse = 0;
        float mean;
        float value;
        int n = 0;
        for(int i = 0; i < nSamples; i++){
            mean = means.get(clusterIndex, i);
            n = 0;
            sse = 0;
            for(int j = 0; j < indices.length; j++){
                value = data.get(indices[j], i);
                if(!Float.isNaN(value)){
                    sse += (float)Math.pow((value - mean),2);
                    n++;
                }
            }
            if(n > 1)
                variances[i] = (float)Math.sqrt(sse/(n-1));
            else
                variances[i] = 0.0f;
        }
        return variances;
    }
    
    /**
     * Adds node with cluster information.
     */
    private void addClusterInfo(DefaultMutableTreeNode root, int [][] clusters, boolean clusterGenes, float zThr) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Cluster Information");
        if(clusterGenes)
            node.add(new DefaultMutableTreeNode(new LeafInfo("Genes in Clusters (#,%)", new HCLClusterInfoViewer(clusters, this.experiment.getNumberOfGenes(), zThr))));
        else
            node.add(new DefaultMutableTreeNode(new LeafInfo("Sammples in Clusters (#,%)", new HCLClusterInfoViewer(clusters, this.experiment.getNumberOfSamples(), false, zThr))));
        root.add(node);
    }
    
    /**
     *  Adds node with tree cut parameters
     */
    private void addGeneralInfo(DefaultMutableTreeNode root, float zThr, int k,boolean isGeneTree){
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Info");
        node.add(new DefaultMutableTreeNode("Cluster Type: "+(isGeneTree ? "Gene Clusters" : "Sample Clusters")));
        node.add(new DefaultMutableTreeNode("Distance Threshold: "+ String.valueOf(zThr)));
        node.add(new DefaultMutableTreeNode("Number of Clusters: "+String.valueOf(k)));
        root.add(node);
    }
    
    /**
     * Removes all clusters.
     */
    public void onDeleteAllClusters() {
        refreshViewer();
        this.clusters.clear();
        this.experimentClusters.clear();
        this.colorBar.onClustersChanged(this.clusters);
        this.header.updateSize(getCommonWidth(), this.elementSize.width);
        if(this.genesTree != null)
            this.genesTree.resetNodeColors();
        if(this.sampleTree != null)
            this.sampleTree.resetNodeColors();
        this.data.deleteColors();
        this.data.deleteExperimentColors();
        this.onDataChanged(data);
        revalidate();
        repaint();
    }
    
    /**
     * Sets genes tree properties.
     */
    public void onGeneTreeProperties() {
        setTreeProperties(this.genesTree);
        this.header.updateSize(getCommonWidth(), this.elementSize.width);
        this.header.setHeaderPosition(this.genesTree.getWidth());
        revalidate();
    }
    
    /**
     * Sets samples tree properties.
     */
    public void onSampleTreeProperties() {
        setTreeProperties(this.sampleTree);
        revalidate();
    }

    /**
     * Rotates the selected node.
     */
    public void onRotateNode() {
    	if(selectedCluster.isGeneCluster){
        	int i=genesTree.treeData.child_1_array[selectedCluster.getRoot()];
        	genesTree.treeData.child_1_array[selectedCluster.getRoot()]=genesTree.treeData.child_2_array[selectedCluster.getRoot()];
        	genesTree.treeData.child_2_array[selectedCluster.getRoot()]=i;
        	genes_result=genesTree.treeData;
        	genesTree.refreshPositions();
    	}
        else{
        	int i=sampleTree.treeData.child_1_array[selectedCluster.getRoot()];
        	sampleTree.treeData.child_1_array[selectedCluster.getRoot()]=sampleTree.treeData.child_2_array[selectedCluster.getRoot()];
        	sampleTree.treeData.child_2_array[selectedCluster.getRoot()]=i;
        	samples_result=sampleTree.treeData;
        	sampleTree.refreshPositions();
        }
    	
        if (genesTree!= null && experiment.getNumberOfGenes() > 1 && genesTree.treeData.node_order.length > 1) {
	        this.genesOrder = createGenesOrder(experiment, features, genesTree.treeData);
            ((ExperimentViewer)this.expViewer).setGenesOrder(genesOrder); 
        }
        if (sampleTree!= null && experiment.getNumberOfSamples() > 1 && sampleTree.treeData.node_order.length > 1) {
            this.samplesOrder = createSamplesOrder(sampleTree.treeData);
	        ((ExperimentHeader)((ExperimentViewer)this.expViewer).getHeaderComponent()).setSamplesOrder(samplesOrder);
	        ((ExperimentViewer)this.expViewer).setSamplesOrder(samplesOrder); 
        }
        
        expViewer.onDataChanged(data);
        onSelected(framework);
    }
    
    private void setTreeProperties(HCLTree tree) {
        Frame frame = JOptionPane.getFrameForComponent(this);
        HCLConfigDialog dialog = new HCLConfigDialog(frame, this, tree.getZeroThreshold(), tree.getMinDistance(), tree.getMaxDistance(), tree.getMinNodeDistance(), tree.getMaxNodeDistance(), tree);
        dialog.setTree(tree);
        if (dialog.showModal() == JOptionPane.OK_OPTION) {
            tree.setProperties(dialog.getZeroThreshold(), dialog.getMinDistance(), dialog.getMaxDistance());
            if(dialog.isCreateClusterViews())
                createAndAddClusterViews(tree);
        }
    }
    
    /**
     * Sets the specified cluster as selected.
     */
    public void valueChanged(HCLTree source, HCLCluster cluster) {
        setSelectedCluster(cluster);
    }
    
    /**
     * Revalidates viewer components
     */
    public void revalidateViewer(){
        if(this.genesTree != null){
            this.header.updateSize(getCommonWidth(), this.elementSize.width);
            this.header.setHeaderPosition(this.genesTree.getWidth());
        }
        revalidate();
    }
    
    /**
     * Launches a new <code>MultipleExperimentViewer</code> containing the current cluster
     */
    public void launchNewSession(){
        if(this.selectedCluster == null)
            return;
        if(this.selectedCluster.isGeneCluster){
            framework.launchNewMAV(getArrayMappedToData(getSubTreeElements()), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.GENE_CLUSTER);
        } else {
            framework.launchNewMAV(getSubTreeElements(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.EXPERIMENT_CLUSTER);
        }
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }
    
    /** Returns the corner component corresponding to the indicated corner,
     * posibly null
     */
    public JComponent getCornerComponent(int cornerIndex) {
        return null;
    }
    
    public int[][] getClusters() {       
        int [][] leafClusters = new int[2][];
        if(this.genesTree != null)
            leafClusters[0] = this.genesOrder;
        else
            leafClusters[0] = null;
        if(this.sampleTree != null)
            leafClusters[1] = this.samplesOrder;
        else
            leafClusters[1] = null;
            return leafClusters;
    }    
    
    public Experiment getExperiment() {
        return this.experiment;
    }
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return -1;
    }
    
  /**  Prototyping code for saving state as XML
   *
    public void writeXML(XMLEncoder enc) {
        enc.setPersistenceDelegate(HCLViewer.class,
        new DefaultPersistenceDelegate( new String[]{
            "experiment", "features", "genes_result", "samples_result", "node"}));
            
            enc.writeObject( new HCLViewer(experiment, features, genes_result, samples_result, node) );
            
    }
   */
    
    /**
     * The class to listen to mouse, action and hcl tree events.
     */
    private class Listener extends MouseAdapter implements ActionListener, HCLTreeListener, java.io.Serializable {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if(command.equals(STORE_CLUSTER_CMD)){
                onSetCluster();
            } else if(command.equals(LAUNCH_NEW_SESSION_CMD)) {
                launchNewSession();
            } if (command.equals(SAVE_CLUSTER_CMD)) {
                onSaveCluster();
            } else if (command.equals(DELETE_CLUSTER_CMD)) {
                onDeleteCluster();
            } else if (command.equals(DELETE_ALL_CLUSTERS_CMD)) {
                onDeleteAllClusters();
            } else if (command.equals(GENE_TREE_PROPERTIES_CMD)) {
                onGeneTreeProperties();
            } else if (command.equals(SAMPLE_TREE_PROPERTIES_CMD)) {
                onSampleTreeProperties();
            } else if (command.equals(ROTATE_NODE_CMD)) {
                onRotateNode();
            } else if (command.equals(SAVE_GENE_ORDER_CMD)){
                saveGenesOrder();
            } else if (command.equals(SAVE_GENE_HEIGHT_CMD)){
                genesTree.saveGeneNodeHeights();
            } else if (command.equals(SAVE_EXP_ORDER_CMD)){
                saveExperimentOrder();
            } else if (command.equals(SAVE_EXP_HEIGHT_CMD)){
                sampleTree.saveExperimentNodeHeights();
            } else if (command.equals(SAVE_GENE_NEWICK_CMD)) {
                genesTree.saveAsNewickFile();
            } else if (command.equals(SAVE_SAMPLE_NEWICK_CMD)) {
                sampleTree.saveAsNewickFile();                
            } else if (command.equals(SAVE_GENE_NEXUS_CMD)) {
                genesTree.saveAsNexusFile();
            } else if (command.equals(SAVE_SAMPLE_NEXUS_CMD)) {
                sampleTree.saveAsNexusFile();
            } else if (command.equals(ExperimentViewer.BROADCAST_MATRIX_GAGGLE_CMD)) {
                broadcastClusterGaggle();
            } else if (command.equals(ExperimentViewer.BROADCAST_NAMELIST_GAGGLE_CMD)) {
                broadcastNamelistGaggle();           
            }
        }
        
        public void valueChanged(HCLTree source, HCLCluster cluster) {
            if(source == sampleTree)
                cluster.isGeneCluster = false;
            HCLViewer.this.valueChanged(source, cluster);
        }
        
        public void mouseReleased(MouseEvent event) {
            maybeShowPopup(event);
        }
        
        public void mousePressed(MouseEvent event) {
            maybeShowPopup(event);
            if (SwingUtilities.isRightMouseButton(event)) {
                return;
            }
            deselect(event);
        }
        
        private void maybeShowPopup(MouseEvent e) {
            if (!e.isPopupTrigger()) {
                return;
            }
            int node = HCLViewer.this.selectedCluster == null ? -1 : HCLViewer.this.selectedCluster.root;
            setEnableMenuItem(STORE_CLUSTER_CMD, node >= 0);
            setEnableMenuItem(LAUNCH_NEW_SESSION_CMD, node >= 0);
            //    setEnableMenuItem(SET_CLUSTER_TEXT_CMD, doesClusterExist() && node != -1 && HCLViewer.this.selectedCluster.isGeneCluster);
            setEnableMenuItem(ROTATE_NODE_CMD, node >= 0);
            setEnableMenuItem(DELETE_CLUSTER_CMD, doesClusterExist());
            setEnableMenuItem(DELETE_ALL_CLUSTERS_CMD, doesClusterExist());
            setEnableMenuItem(SAVE_CLUSTER_CMD, HCLViewer.this.selectedCluster != null && HCLViewer.this.selectedCluster.root != -1);
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
        
        
        
        private void deselect(MouseEvent e){
        	selectedCluster = null;
        	Object source = e.getSource();
            
            if(source instanceof HCLTree){  //in a tree don't deselect
                if(source == genesTree) { //if colloring rows (genes)
                    
                    if(isExperimentCluster){
                        ((ExperimentClusterViewer)expViewer).selectColumns(-1, -1);
                    }
                    else{
                        ((ExperimentViewer)expViewer).selectColumns(-1, -1);
                    }
                    if(sampleTree != null)
                        sampleTree.deselectAllNodes();
                }
                else{  // coloring columns (experiment)
                    
                    if(isExperimentCluster){
                        ((ExperimentClusterViewer)expViewer).selectRows(-1, -1);
                    }
                    else{
                        ((ExperimentViewer)expViewer).selectRows(-1, -1);
                    }
                    if(genesTree != null)
                        genesTree.deselectAllNodes();
                }
                repaint();
                return;
            }
            
            int x = e.getX();
            int y = e.getY();
            
            //now know we are not on a tree
            if(!(source instanceof HCLViewer) && (source != expViewer)){
                deselectAllNodes();
                repaint();
            }
            //know we are in the HCLViewer but not in the tree areas but above matrix
            else if(source instanceof HCLViewer && sampleTree != null && y < sampleTree.getHeight()){
                deselectAllNodes();
                repaint();
            }
            else if(source == expViewer){
                int numSamples = numberOfSamples;
                if(source instanceof ExperimentClusterViewer)
                    numSamples = ((ExperimentClusterViewer)expViewer).getCurrentNumberOfExperiments();
                if(x > elementSize.width * numSamples + offset || x < offset){
                    deselectAllNodes();
                    repaint();
                }
            }
            else if(((source == expViewer) || (source instanceof ExperimentClusterViewer)) && x < offset){
                deselectAllNodes();
                repaint();
            }

        }
        
        
        private void deselectAllNodes(){
            if(genesTree != null)
                genesTree.deselectAllNodes();
            if(sampleTree != null)
                sampleTree.deselectAllNodes();
            if(isExperimentCluster){
                ((ExperimentClusterViewer)expViewer).selectRows(-1, -1);
                ((ExperimentClusterViewer)expViewer).selectColumns(-1, -1);
            }
            else{
                ((ExperimentViewer)expViewer).selectRows(-1, -1);
                ((ExperimentViewer)expViewer).selectColumns(-1, -1);
            }
        }
        
    }
}
