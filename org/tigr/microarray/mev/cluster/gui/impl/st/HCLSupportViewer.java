/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: HCLSupportViewer.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.st;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import java.awt.image.BufferedImage;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JPanel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JColorChooser;
import javax.swing.SwingUtilities;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLAnnotationBar;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLCluster;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLColorBar;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTree;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeListener;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLViewer;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;

public class HCLSupportViewer extends HCLViewer {
    
    protected static final String SET_CLUSTER_CMD = "set-cluster-cmd";
    protected static final String SET_CLUSTER_TEXT_CMD = "set-cluster-text-cmd";
    protected static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
    protected static final String DELETE_CLUSTER_CMD = "delete-cluster-cmd";
    protected static final String DELETE_ALL_CLUSTERS_CMD = "delete-all-clusters-cmd";
    protected static final String GENE_TREE_PROPERTIES_CMD = "gene-tree-properties-cmd";
    protected static final String SAMPLE_TREE_PROPERTIES_CMD = "sample-tree-properties-cmd";
    
    
    Vector geneTreeSupportVector, exptTreeSupportVector;
    
    
    public HCLSupportViewer(Experiment Experiment, int[] Features, HCLTreeData genes_result, HCLTreeData samples_result, Vector geneTreeSupportVector, Vector exptTreeSupportVector, DefaultMutableTreeNode node) {
        super(Experiment, Features, genes_result, samples_result, node); 
        setLayout(new GridBagLayout());
        setBackground(Color.white);

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
        //   super.numberOfSamples = experiment.getNumberOfSamples();
        super.removeAll();
        super.validate();
        addComponents(this.sampleTree, this.genesTree, this.expViewer.getContentComponent(), this.colorBar, this.annotationBar);
        this.addMouseListener(listener);
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
    private void addMenuItems(JPopupMenu menu, Listener listener) {
        JMenuItem menuItem;
        menuItem = new JMenuItem("Set cluster...", GUIFactory.getIcon("edit16.gif"));
        menuItem.setEnabled(false);
        menuItem.setActionCommand(SET_CLUSTER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Set cluster text...", GUIFactory.getIcon("edit16.gif"));
        menuItem.setEnabled(false);
        menuItem.setActionCommand(SET_CLUSTER_TEXT_CMD);
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
   /*
        private void deselect(MouseEvent e){
            Object source = e.getSource();
    
            if(source instanceof HCLSupportTree){  //in a tree don't deselect
                return;
            }
    
            int x = e.getX();
            int y = e.getY();
    
            //now know we are not on a tree
            if(!(source instanceof HCLSupportViewer) && (source != expViewer)){
                deselectAllNodes();
                repaint();
            }
            //know we are in the HCLViewer but not in the tree areas but above matrix
            else if(source instanceof HCLSupportViewer && sampleTree != null && y < sampleTree.getHeight()){
                deselectAllNodes();
                repaint();
            }
            else if(source == expViewer && x > (elementSize.width * numberOfSamples)){
                deselectAllNodes();
                repaint();
            }
            else if(source == expViewer && x < offset){
                deselectAllNodes();
                repaint();
            }
        }
    
        private void deselectAllNodes(){
            if(genesTree != null)
                genesTree.deselectAllNodes();
            if(sampleTree != null)
                sampleTree.deselectAllNodes();
           ((ExperimentViewer)expViewer).selectRows(-1, -1);
        }
    **/
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