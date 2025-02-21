/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: HCLSupportViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2007-03-09 19:58:31 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.st;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Expression;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLCluster;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTree;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeListener;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLViewer;

public class HCLSupportViewer extends HCLViewer {
    
    protected static final String SET_CLUSTER_CMD = "set-cluster-cmd";
    protected static final String SET_CLUSTER_TEXT_CMD = "set-cluster-text-cmd";
    protected static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
    protected static final String DELETE_CLUSTER_CMD = "delete-cluster-cmd";
    protected static final String DELETE_ALL_CLUSTERS_CMD = "delete-all-clusters-cmd";
    protected static final String GENE_TREE_PROPERTIES_CMD = "gene-tree-properties-cmd";
    protected static final String SAMPLE_TREE_PROPERTIES_CMD = "sample-tree-properties-cmd";
    protected static final String SUPPORT_LEGEND_CMD = "support-legend-cmd";
    protected static final String SUPPORT_VALUES_CMD = "support-value-cmd";
    protected static final String TOGGLE_SUPPORT_COLORS_CMD = "support-color-cmd";
   
    
    Vector geneTreeSupportVector, exptTreeSupportVector;
    
    /**
     * MeV v4.4 and higher state-saving constructor.
     * @param experiment
     * @param features
     * @param genes_result
     * @param samples_result
     * @param geneTreeSupportVector
     * @param exptTreeSupportVector
     * @param node
     */
    public HCLSupportViewer(Experiment experiment, ClusterWrapper features, HCLTreeData genes_result, HCLTreeData samples_result, Vector geneTreeSupportVector, Vector exptTreeSupportVector, DefaultMutableTreeNode node) {
    	this(experiment, features.getClusters()[0], genes_result, samples_result, geneTreeSupportVector, exptTreeSupportVector, node);
    }
    
    public HCLSupportViewer(Experiment experiment, int[] Features, HCLTreeData genes_result, HCLTreeData samples_result, Vector geneTreeSupportVector, Vector exptTreeSupportVector, DefaultMutableTreeNode node) {
        super(experiment, Features, genes_result, samples_result, node); 
        setLayout(new GridBagLayout());
        setBackground(Color.white);
        
        this.geneTreeSupportVector = geneTreeSupportVector;
        this.exptTreeSupportVector = exptTreeSupportVector;

        if (genes_result != null && experiment.getNumberOfGenes() > 1) {
            this.genesTree = new HCLSupportTree(genes_result, HCLTree.HORIZONTAL, geneTreeSupportVector, exptTreeSupportVector);
            this.genesTree.addMouseListener(listener);
            this.genesTree.setListener(listener);
        }
        if (samples_result != null && experiment.getNumberOfSamples() > 1) {
            this.sampleTree = new HCLSupportTree(samples_result, HCLTree.VERTICAL, geneTreeSupportVector, exptTreeSupportVector);
            if(genes_result == null){
                offset = 10;
                this.sampleTree.setHorizontalOffset(offset);
            }
            this.sampleTree.setListener(listener);            
            this.sampleTree.addMouseListener(listener);
        }
        super.removeAll();
        super.validate();
        addComponents(this.sampleTree, this.genesTree, this.expViewer.getContentComponent(), this.colorBar, this.annotationBar);
        this.addMouseListener(listener);
        addSTMenuItems(popup);
    }
    /**
     * State-saving constructor for versions 4.0-4.3. 
     * @param e
     * @param features
     * @param genesResult
     * @param samplesResult
     * @param sampleClusters
     * @param isExperimentCluster
     * @param genesTree
     * @param sampleTree
     * @param offset
     * @param expViewer
     * @param geneTreeSupportVector
     * @param exptTreeSupportVector
     */
    public HCLSupportViewer(Experiment e, int[] features, HCLTreeData genesResult, HCLTreeData samplesResult, int [][] sampleClusters, boolean isExperimentCluster, HCLTree genesTree, HCLTree sampleTree, Integer offset, ExperimentViewer expViewer, Vector geneTreeSupportVector, Vector exptTreeSupportVector) {
    	super(e, features, genesResult, samplesResult, sampleClusters, isExperimentCluster, genesTree, sampleTree, offset, expViewer);

        this.geneTreeSupportVector = geneTreeSupportVector;
        this.exptTreeSupportVector = exptTreeSupportVector;
        
    }

    public Expression getExpression(){
    	if(features == null)
    		features = createDefaultFeatures(experiment);
    	return new Expression(this, this.getClass(), "new", new Object[]{experiment, ClusterWrapper.wrapClusters(new int[][]{features}), genes_result, samples_result, geneTreeSupportVector, exptTreeSupportVector, node});
		
    }

    public void setExperiment(Experiment e) {
    	super.setExperiment(e);
    }
    

    /**
     * Adds menu items to the specified popup menu.
     */
    private void addSTMenuItems(JPopupMenu menu) {
        Listener listener = new Listener();

        boolean haveResamplingData = true;
        if((this.geneTreeSupportVector == null || this.geneTreeSupportVector.isEmpty()) 
            && (this.exptTreeSupportVector == null || this.exptTreeSupportVector.isEmpty()))
            haveResamplingData = false;
            
        JMenuItem menuItem;
       
        menuItem = new JMenuItem("Support tree legend...");
        menuItem.setActionCommand(SUPPORT_LEGEND_CMD);
        menuItem.addActionListener(listener);
                
        if(!haveResamplingData)
            menuItem.setEnabled(false);

        JMenuItem hideSupportColorsItem = new JCheckBoxMenuItem("Hide support colors...", false);
        hideSupportColorsItem.setActionCommand(TOGGLE_SUPPORT_COLORS_CMD);
        hideSupportColorsItem.addActionListener(listener);
        
        Component [] comps = menu.getComponents();
        
        menu.removeAll();
        menu.add(menuItem);
        menu.addSeparator();

        menuItem = new JCheckBoxMenuItem("Show values...", false);
        menuItem.setActionCommand(SUPPORT_VALUES_CMD);
        menuItem.addActionListener(listener);

        if(!haveResamplingData)
            menuItem.setEnabled(false);

        menu.add(menuItem);
        menu.add(hideSupportColorsItem);
		menu.addSeparator();
        
        for(int i = 0; i < comps.length; i++) {
            menu.add(comps[i]);
        }        
    }
    
    protected void showPopup(MouseEvent e) {
        int node = HCLSupportViewer.this.selectedCluster == null ? -1 : HCLSupportViewer.this.selectedCluster.root;
        setEnableMenuItem(SET_CLUSTER_CMD, node >= 0);
        setEnableMenuItem(SET_CLUSTER_TEXT_CMD, doesClusterExist());
        setEnableMenuItem(DELETE_CLUSTER_CMD, doesClusterExist());
        setEnableMenuItem(DELETE_ALL_CLUSTERS_CMD, doesClusterExist());
        setEnableMenuItem(SAVE_CLUSTER_CMD, HCLSupportViewer.this.selectedCluster != null && HCLSupportViewer.this.selectedCluster.root != -1);
        popup.show(e.getComponent(), e.getX(), e.getY());
    }
    
    /**
     * Shows the legend for HCLSupportTree coloring
     * This method will no longer be supported when the legend
     * is displayed as part of the HCLSupportTree image
     */
    private void onShowSupportTreeLegend() {
        javax.swing.JDialog legendFrame = new javax.swing.JDialog(this.framework.getFrame(), "Support Tree Legend");
        JPanel legendPanel = HCLSupportTree.getColorLegendPanel();
        legendFrame.getContentPane().add(legendPanel);
        legendFrame.setSize(200, 300);
        legendFrame.setLocation(300, 100);
        legendFrame.setVisible(true);
    }    
    
    private void onShowSupportValues(boolean showValues) {
        if(this.genesTree != null)
            ((HCLSupportTree)this.genesTree).toggleShowSupportValues(showValues);
        if(this.sampleTree != null)
            ((HCLSupportTree)this.sampleTree).toggleShowSupportValues(showValues);        
        this.header.updateSize(getCommonWidth(), this.elementSize.width);       
        onSelected(framework);        
    }
  
    
    public void onHideSupportColors(boolean hideColors) {
        if(this.genesTree != null)
            ((HCLSupportTree)this.genesTree).hideSupportColors(hideColors);
        if(this.sampleTree != null)
            ((HCLSupportTree)this.sampleTree).hideSupportColors(hideColors);        
        this.header.updateSize(getCommonWidth(), this.elementSize.width);       
        onSelected(framework);  
    }
    
    /**
     * Delegates this invokation to wrapped viewers.
     */
    public void onMenuChanged(IDisplayMenu menu) {
        super.onMenuChanged(menu);
       // this.expViewer.onMenuChanged(menu);
        if (this.genesTree != null) {
            ((HCLSupportTree)genesTree).adjustPixelHeightsForValueDisplay();
            this.genesTree.onMenuChanged(menu);
        }
        if (this.sampleTree != null) {
            ((HCLSupportTree)sampleTree).adjustPixelHeightsForValueDisplay();
            this.sampleTree.onMenuChanged(menu);
        }                
        onSelected(framework);
    }
  
  /**
     * The class to listen to mouse, action and hcl tree events.
     */
    private class Listener extends MouseAdapter implements ActionListener, HCLTreeListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals(SET_CLUSTER_CMD)) {
                onSetCluster();
            } else if (command.equals(SET_CLUSTER_TEXT_CMD)) {
                onSetClusterText();
            } else if (command.equals(SAVE_CLUSTER_CMD)) {
                onSaveCluster();
            } else if (command.equals(DELETE_CLUSTER_CMD)) {
                onDeleteCluster();
            } else if (command.equals(DELETE_ALL_CLUSTERS_CMD)) {
                onDeleteAllClusters();
            } else if (command.equals(GENE_TREE_PROPERTIES_CMD)) {
                onGeneTreeProperties();
            } else if (command.equals(SAMPLE_TREE_PROPERTIES_CMD)) {
                onSampleTreeProperties();
            } else if (command.equals(SUPPORT_LEGEND_CMD)) {
                onShowSupportTreeLegend();
            } else if (command.equals(SUPPORT_VALUES_CMD)) {                
                onShowSupportValues(((JCheckBoxMenuItem)e.getSource()).isSelected());
            } else if (command.equals(TOGGLE_SUPPORT_COLORS_CMD)) {
            	onHideSupportColors(((JCheckBoxMenuItem)e.getSource()).isSelected());
            }
        }
        
        public void valueChanged(HCLTree source, HCLCluster cluster) {
            HCLSupportViewer.this.valueChanged(source, cluster);
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
            
            showPopup(e);
        }

        
        private void deselect(MouseEvent e){
            Object source = e.getSource();
            
            if(source instanceof HCLTree){  //in a tree don't deselect
                if(source == genesTree) { //if colloring rows (genes)
                    
                    
                    ((ExperimentViewer)expViewer).selectColumns(-1, -1);
                    
                    if(sampleTree != null)
                        sampleTree.deselectAllNodes();

                    ((ExperimentViewer)expViewer).selectRows(-1, -1);
                    
                    if(genesTree != null)
                        genesTree.deselectAllNodes();
                    
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
                    if(x > elementSize.width * numSamples + offset || x < offset){
                        deselectAllNodes();
                        repaint();
                    }
                }
                else if((source == expViewer) && x < offset){
                    deselectAllNodes();
                    repaint();
                }
            }
            
        }
        private void deselectAllNodes(){
            if(genesTree != null)
                genesTree.deselectAllNodes();
            if(sampleTree != null)
                sampleTree.deselectAllNodes();
            
            
            ((ExperimentViewer)expViewer).selectRows(-1, -1);
            ((ExperimentViewer)expViewer).selectColumns(-1, -1);
            
        }
    }
    
}
