/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SOTAExperimentTreeViewer.java,v $
 * $Revision: 1.9 $
 * $Date: 2006-05-02 16:57:35 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.sota;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLCluster;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTree;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.util.FloatMatrix;


public class SOTAExperimentTreeViewer extends JPanel implements IViewer{
    
    protected static String SET_CLUSTER_CMD = "set-cluster-cmd";
    protected static String SET_CLUSTER_TEXT_CMD = "set-cluster-text-cmd";
    protected static String SAVE_CLUSTER_CMD = "save-cluster-cmd";
    protected static String DELETE_CLUSTER_CMD = "delete-cluster-cmd";
    protected static String DELETE_ALL_CLUSTERS_CMD = "delete-all-clusters-cmd";
    protected static String SOTA_TREE_PROPERTIES_CMD = "gene-tree-properties-cmd";
    protected static String SAMPLE_TREE_PROPERTIES_CMD = "sample-tree-properties-cmd";
    
    
    private int numberOfCells;
    private int numberOfSamples;
    private Experiment centroidData;
    private SOTATree sotaTree;
    private FloatMatrix clusterDivFM;
    private int [] clusterPop;
    private int [][] clusterIndices;
    private ArrayList selectedClusterList = new ArrayList();
    
    private SOTAExpCentroidExpressionViewer expViewer;
    private Dimension elementSize;
    private IFramework framework;
    private IData data;
    private Experiment experiment;
    private SOTACentroidInfoDialog centroidDialog;
    private HCLExperimentHeader header;
    protected HCLTree sampleTree;
    private int elementHeight;
    private int elementWidth;
    private Listener listener;
    private int function;
    private int currClusterNum = -1;
    private DefaultMutableTreeNode expImageNode;
    private JPopupMenu popup;
    //EH
    private int exptID = 0;
    
    /**
     * Creates a new instance of SOTAExperimentTreeViewer
     * @param result result of SOTA clustering
     * @param sota SOTA tree data structure
     * @param hclSampleTree result from clustering samples by Hierarchical Clustering
     * @param clusters Cluster gene indicies
     */
    public SOTAExperimentTreeViewer(Experiment experiment, SOTATreeData sotaTreeData, int[][] clusters) {
        setLayout(new GridBagLayout());
        setBackground(Color.white);
        listener = new Listener();
        clusterPop = sotaTreeData.clusterPopulation;
        clusterDivFM = sotaTreeData.clusterDiversity;
        clusterIndices = clusters;
        function = sotaTreeData.function;
        numberOfSamples = experiment.getNumberOfSamples();
        numberOfCells = sotaTreeData.clusterPopulation.length;
        sotaTree = new SOTATree(sotaTreeData, false);
        if(sotaTree != null)
            sotaTree.addMouseListener(listener);
        int [] samplesOrder = null;
        centroidDialog = null;
        centroidData = new Experiment(sotaTreeData.centroidMatrix.transpose(), samplesOrder != null ? samplesOrder : getSamplesOrder(numberOfSamples));
        numberOfSamples = centroidData.getNumberOfSamples();
        expViewer = new SOTAExpCentroidExpressionViewer( centroidData, experiment, null, samplesOrder, sotaTreeData.clusterPopulation, sotaTreeData.clusterDiversity, selectedClusterList);
        expViewer.addMouseListener(listener);
        addComponents(sotaTree, expViewer);
        addMouseListener(listener);
        popup = this.createJPopupMenu(listener);
        this.experiment = experiment;
        this.exptID = experiment.getId();
    }

    /**
     * Creates a new instance of SOTAExperimentTreeViewer from xml-saved data
     * written by XMLEncoder/XMLDecoder and IViewerPersistenceDelegate
     * @param result result of SOTA clustering
     * @param sota SOTA tree data structure
     * @param hclSampleTree result from clustering samples by Hierarchical Clustering
     * @param clusters Cluster gene indicies
     * @deprecated
     */
    public SOTAExperimentTreeViewer(Integer exptID, SOTATreeData sotaTreeData, 
    		int[][] clusterIndices) {
        setLayout(new GridBagLayout());
        setBackground(Color.white);
        listener = new Listener();
        clusterPop = sotaTreeData.clusterPopulation;
        clusterDivFM = sotaTreeData.clusterDiversity;
        this.clusterIndices = clusterIndices;
        function = sotaTreeData.function;
        numberOfCells = sotaTreeData.clusterPopulation.length;
        sotaTree = new SOTATree(sotaTreeData, false);
        if(sotaTree != null)
            sotaTree.addMouseListener(listener);
        centroidDialog = null;
        this.exptID = exptID.intValue();
    }
    
    public SOTATreeData getSOTATreeData(){return sotaTree.getSotaTreeData();}
        
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{this.experiment, this.sotaTree.getSotaTreeData(), this.clusterIndices});
    }
    public int getExperimentID(){return exptID;}
    public void setExperimentID(int e){
    	exptID = e;
    	expViewer.setExperimentID(e);
    }
    public void setExperiment(Experiment e){
    	experiment = e;
    	exptID = e.getId();
        numberOfSamples = experiment.getNumberOfSamples();
    	centroidData = new Experiment(sotaTree.getSotaTreeData().centroidMatrix.transpose(), getSamplesOrder(numberOfSamples));

    	numberOfSamples = centroidData.getNumberOfSamples();
        this.expViewer = new SOTAExpCentroidExpressionViewer(centroidData, experiment, null, getSamplesOrder(numberOfSamples), sotaTree.getSotaTreeData().clusterPopulation, sotaTree.getSotaTreeData().clusterDiversity, selectedClusterList);
        this.expViewer.addMouseListener(listener);
    	this.expViewer.setExperiment(e);
    	
        addComponents(sotaTree, expViewer);
        addMouseListener(listener);
        popup = this.createJPopupMenu(listener);
    
    }
    /**
     *  sets a node to reference when jumping to expression images
     */
    public void associateExpressionImageNode(DefaultMutableTreeNode ExpressionImageNode){
        expImageNode = ExpressionImageNode;
    }
    
    /**
     * Returns a hcl tree data from the specified cluster node.
     */
    private HCLTreeData getResult(Node clusterNode, int pos) {
        HCLTreeData data = new HCLTreeData();
        NodeValueList valueList = clusterNode.getValues();
        data.child_1_array = (int[])valueList.getNodeValue(pos).value;
        data.child_2_array = (int[])valueList.getNodeValue(pos+1).value;
        data.node_order = (int[])valueList.getNodeValue(pos+2).value;
        data.height = (float[])valueList.getNodeValue(pos+3).value;
        return data;
    }
    
    
    int [] getSamplesOrder(int numSamples){
        int [] order = new int[numSamples];
        
        for(int i = 0; i < numSamples; i++){
            order[i] = i;
        }
        return order;
    }
    
    
    protected void addComponents( JComponent sotaTree, JComponent expViewer){// ,JComponent cBar) {
        
        if (sotaTree != null) {
            add(sotaTree, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        }
        add(expViewer,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }
    
    /**
     * Invoked by the framework when display menu is changed,
     * if this viewer is selected.
     * @see IDisplayMenu
     */
    public void onMenuChanged(IDisplayMenu menu) {
        this.sotaTree.onMenuChanged(menu);
        this.expViewer.onMenuChanged(menu);
        if(sampleTree != null)
            this.sampleTree.onMenuChanged(menu);
        verifyClusterExistence(data);
        
        //this.header.setHeaderPosition(this.sotaTree.getTreeHeight()-10); //move by inset of viewer
        //this.header.updateSize(getCommonWidth());
    }
    
    /**
     * Invoked by the framework when this viewer was deselected.
     */
    public void onDeselected() {
    }
    
    
    /**
     * Invoked by the framework when this viewer is selected.
     */
    public void onSelected(IFramework framework) {
        this.framework = framework;
        this.data = framework.getData();
        
        IDisplayMenu menu = framework.getDisplayMenu();
        elementSize = menu.getElementSize();
        if(this.expViewer.isVisible())
            this.expViewer.onSelected(framework);
        if(sampleTree != null)
            this.sampleTree.onSelected(framework);
        this.sotaTree.onSelected(framework);
        verifyClusterExistence(data);
        verifyClusterMembership(data);
        
        //expImageNode is null after de-serialization and must be reset
        if(this.expImageNode == null){
            DefaultMutableTreeNode node = framework.getCurrentNode();
            if(node != null)
                node = (DefaultMutableTreeNode)node.getParent();
            if(node != null)
                node = (DefaultMutableTreeNode)node.getChildAt(1);
            if(node != null)
                this.expImageNode = node;            
        }
    }
    
    /**
     * Calculate the viewer width.
     */
    public int getCommonWidth() {
        int width = 0;
        if (this.sotaTree != null) {
            width += this.sotaTree.getTreeHeight();
        }
        
        width += this.expViewer.getWidth();
        
        return width;
    }
    
    public int getCommonHeight(){
        int height = 0;
        if(this.sotaTree != null){
            height += this.sotaTree.getTreeWidth();
        }
        height += this.header.getHeight();
        return height;
    }
    
    /**
     * Invoked when the framework is going to be closed.
     */
    public void onClosed() {
    }
    
    /**
     * Returns a component to be inserted into scroll pane view port.
     */
    public JComponent getContentComponent() {
        return this;
    }
    
    /**
     * Invoked by the framework to save or to print viewer image.
     */
    public BufferedImage getImage() {
        return null;
    }
    
    /**
     * Invoked by the framework when data is changed,
     * if this viewer is selected.
     * @see IData
     */
    public void onDataChanged(IData data) {
        this.data = data;
        verifyClusterExistence(data);
    }
    
    
    private void verifyClusterExistence(IData data){
        Color [] colors = data.getExperimentColors();
        if(colors.length == 0){
            this.selectedClusterList.clear();
            this.expViewer.onDataChanged(data);
        }
    }
    
    private void verifyClusterMembership(IData data){
        Color [] colors = data.getExperimentColors();
        Color currColor = null;
        HCLCluster cluster;
        boolean aMemberChanged = false;
        boolean membershipChanged = false;
        boolean [] alteredMembership = new boolean[selectedClusterList.size()];
        
        int index;
        for(int c = 0; c < this.selectedClusterList.size(); c++){
            cluster = (HCLCluster)this.selectedClusterList.get(c);
            currColor = cluster.color;
            index = cluster.root;
            membershipChanged = false;
            for(int exp = 0; exp < this.clusterIndices[index].length ; exp++){
                if(!(currColor.equals(data.getExperimentColor(this.clusterIndices[index][exp])))){
                    aMemberChanged = true;
                    membershipChanged = true;
                    break;
                }
            }
            if(membershipChanged)
                alteredMembership[c] = true;
        }
        
        for(int i = selectedClusterList.size()-1; i >=0 ;i--){
            if(alteredMembership[i])
                selectedClusterList.remove(i);
        }
        if(aMemberChanged)
            this.expViewer.onDataChanged(data);
    }
    
    
    /**
     * Returns a component to be inserted into scroll pane header.
     */
    public JComponent getHeaderComponent() {
        return this.expViewer.getHeaderComponent();
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
    
    private Frame getFrame() {
        return JOptionPane.getFrameForComponent(this);
    }
    
    private void setSOTATreeProperties() {
        Frame frame = JOptionPane.getFrameForComponent(this);
        SOTAConfigDialog dialog = new SOTAConfigDialog(frame, 0, sotaTree.getMinDistance(), sotaTree.getMaxDistance());
        if (dialog.showModal() == JOptionPane.OK_OPTION) {
            sotaTree.setProperties(dialog.getZeroThreshold(), dialog.getMinDistance(), dialog.getMaxDistance());
        }
        revalidate();
    }
    
    public void onSampleTreeProperties() {
        setTreeProperties(this.sampleTree);
        revalidate();
    }
    
    private void setTreeProperties(HCLTree tree) {
        Frame frame = JOptionPane.getFrameForComponent(this);
        SOTAConfigDialog dialog = new SOTAConfigDialog(frame, tree.getZeroThreshold(), tree.getMinDistance(), tree.getMaxDistance());
        if (dialog.showModal() == JOptionPane.OK_OPTION) {
            tree.setProperties(dialog.getZeroThreshold(), dialog.getMinDistance(), dialog.getMaxDistance());
        }
    }
    
    
    private JPopupMenu createJPopupMenu(Listener listener) {
        JPopupMenu popup = new JPopupMenu();
        addMenuItems(popup, listener);
        return popup;
    }
    
    private void showClusterInfo(int clusterNumber){
        float neighborDist;
        int neighbor = getClosestCentroid(clusterNumber);
        
        if(neighbor == clusterNumber) return;
        
        neighborDist = org.tigr.microarray.mev.cluster.algorithm.impl.ExperimentUtil.geneDistance(centroidData.getMatrix(),
        null, clusterNumber, neighbor, function, (float)1.0, false);
        
        //Code to put selected viewer into scroll pane
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)(expImageNode.getChildAt(clusterNumber));
        
        if(this.framework != null)
            framework.setTreeNode(node);
    }
    
    
    
    private int getClosestCentroid(int centroidNum){
        float minDist = Float.POSITIVE_INFINITY;
        float currDist;
        int closestCentroid = centroidNum;
        for(int i = 0; i < this.numberOfCells ;i++){
            currDist = org.tigr.microarray.mev.cluster.algorithm.impl.ExperimentUtil.geneDistance(centroidData.getMatrix(),
            null, centroidNum, i, function, (float)1.0 , false);
            
            if(currDist < minDist && i != centroidNum){
                minDist = currDist;
                closestCentroid = i;
            }
        }
        return closestCentroid;
    }
    
    
    private void onSaveCluster(int cNum){
        try {
            org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil.saveExperimentCluster(getFrame(), this.experiment, this.data, clusterIndices[cNum]);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(getFrame(), "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Sets cluster color
     */
    private void onSetCluster(int currCluster){
        
        Color newColor = JColorChooser.showDialog(getFrame(), "Choose color", new Color(128, 128, 128));
        if (newColor == null || currCluster == -1) {
            return;
        }
        HCLCluster cluster = new HCLCluster(currCluster, currCluster-1, currCluster+1);
        selectedClusterList.add(cluster);
        cluster.color = newColor;
        revalidate();
        // this.expViewer.setClusterColor(newColor);
        this.data.setExperimentColor( getIDataRowIndices(clusterIndices[currCluster]), newColor);
        this.expViewer.onDataChanged(data);
        repaint();
    }
    
    /**
     * Converts cluster indicies from the experiment to IData rows which could be different
     */
    private int [] getIDataRowIndices(int [] expIndices){
        int [] dataIndices = new int[expIndices.length];
        for(int i = 0; i < expIndices.length; i++){
            dataIndices[i] = experiment.getGeneIndexMappedToData(expIndices[i]);
        }
        return dataIndices;
    }
    
    /**
     * Deletes indicated cluster
     */
    private void onDeleteCluster(int clusterIndex){
        HCLCluster currCluster;
        
        for(int i = 0; i < selectedClusterList.size(); i++){
            currCluster = (HCLCluster)selectedClusterList.get(i);
            if(currCluster.root == clusterIndex){
                this.data.setExperimentColor( clusterIndices[clusterIndex], null);
                selectedClusterList.remove(i);
            }
        }
        this.expViewer.onDataChanged(data);
        repaint();
    }
    
    /**
     *  Clears cluster colors
     */
    private void onDeleteAllClusters(){
        selectedClusterList.clear();
        this.data.deleteExperimentColors();
        this.expViewer.onDataChanged(data);
        repaint();
    }
    
    
    /**
     * Adds menu items to the specified popup menu.
     */
    protected void addMenuItems(JPopupMenu menu, Listener listener) {
        
        JMenuItem menuItem;
        
        //  menuItem = new JMenuItem("Set cluster...", GUIFactory.getIcon("edit16.gif"));
        //   menuItem.setEnabled(false);
        //   menuItem.setActionCommand(SET_CLUSTER_CMD);
        //   menuItem.addActionListener(listener);
        //   menu.add(menuItem);
        
        //   menuItem = new JMenuItem("Set cluster text...", GUIFactory.getIcon("edit16.gif"));
        //    menuItem.setEnabled(false);
        //    menuItem.setActionCommand(SET_CLUSTER_TEXT_CMD);
        //    menuItem.addActionListener(listener);
        //     menu.add(menuItem);
        
        menuItem = new JMenuItem("Save cluster...", GUIFactory.getIcon("save_as16.gif"));
        menuItem.setEnabled(false);
        menuItem.setActionCommand(SAVE_CLUSTER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        //    menuItem = new JMenuItem("Delete cluster", GUIFactory.getIcon("delete16.gif"));
        //     menuItem.setEnabled(false);
        //     menuItem.setActionCommand(DELETE_CLUSTER_CMD);
        //     menuItem.addActionListener(listener);
        //     menu.add(menuItem);
        
        //     menuItem = new JMenuItem("Delete all clusters", GUIFactory.getIcon("delete16.gif"));
        //     menuItem.setEnabled(false);
        //     menuItem.setActionCommand(DELETE_ALL_CLUSTERS_CMD);
        //    menuItem.addActionListener(listener);
        //     menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("SOTATree properties...", GUIFactory.getIcon("edit16.gif"));
        menuItem.setEnabled(this.sotaTree != null);
        menuItem.setActionCommand(SOTA_TREE_PROPERTIES_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("SampleTree properties...", GUIFactory.getIcon("edit16.gif"));
        menuItem.setEnabled(this.sampleTree != null);
        menuItem.setActionCommand(SAMPLE_TREE_PROPERTIES_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
    }
    
    
    /**
     * Returns a menu item by specified action command.
     * @return null, if menu item was not found.
     */
    protected JMenuItem getJMenuItem(String command) {
        JMenuItem item;
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
    
    private boolean isClusterSet(int clusterIndex){
        HCLCluster currCluster;
        
        for(int i = 0; i < selectedClusterList.size(); i++){
            currCluster = (HCLCluster)selectedClusterList.get(i);
            if(currCluster.root == clusterIndex)
                return true;
        }
        return false;
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
    
    /** Returns the viewer's clusters or null
     */
    public int[][] getClusters() {
        return null;
    }    
    
    /**  Returns the viewer's experiment or null
     */
    public Experiment getExperiment() {
        return null;
    }
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return -1;
    }    
    
    private class Listener extends MouseAdapter implements ActionListener{
        
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            String actionCmd = actionEvent.getActionCommand();
            if(actionCmd.equals(SOTAExperimentTreeViewer.SOTA_TREE_PROPERTIES_CMD)){
                setSOTATreeProperties();
            }
/*
            else if(actionCmd.equals(SOTAGeneTreeViewer.SAMPLE_TREE_PROPERTIES_CMD)){
                onSampleTreeProperties();
            }
 */
            else if(actionCmd.equals(SOTAExperimentTreeViewer.SET_CLUSTER_CMD)){
                if(currClusterNum != -1)
                    onSetCluster(currClusterNum);
            }
            else if(actionCmd.equals(SOTAExperimentTreeViewer.SAVE_CLUSTER_CMD)){
                if(currClusterNum != -1)
                    onSaveCluster(currClusterNum);
            }
            else if(actionCmd.equals(SOTAExperimentTreeViewer.DELETE_CLUSTER_CMD)){
                if(currClusterNum != -1)
                    onDeleteCluster(currClusterNum);
            }
            
            else if(actionCmd.equals(SOTAExperimentTreeViewer.DELETE_ALL_CLUSTERS_CMD)){
                onDeleteAllClusters();
            }
        }
        
        public void mouseReleased(MouseEvent event) {
            
            if(!maybeShowPopup(event) && SwingUtilities.isLeftMouseButton(event)){
                currClusterNum = expViewer.getCurrentCentroidNumber();
                
                if(currClusterNum != -1){
                    showClusterInfo(currClusterNum);
                }
            }
        }
        
        
        public void mousePressed(MouseEvent event) {
            
            if(!maybeShowPopup(event) && SwingUtilities.isLeftMouseButton(event)){
                currClusterNum = expViewer.getCurrentCentroidNumber();
                
                if(currClusterNum != -1){
                    showClusterInfo(currClusterNum);
                }
            }
        }
        
        
        
        private boolean maybeShowPopup(MouseEvent e) {
            if (!e.isPopupTrigger()) {
                return false;
            }
            currClusterNum = expViewer.getCurrentCentroidNumber();
            setEnableMenuItem(SET_CLUSTER_CMD, currClusterNum != -1);
            setEnableMenuItem(DELETE_CLUSTER_CMD, currClusterNum != -1 && isClusterSet(currClusterNum));
            setEnableMenuItem(DELETE_ALL_CLUSTERS_CMD, !selectedClusterList.isEmpty());
            setEnableMenuItem(SAVE_CLUSTER_CMD, currClusterNum != -1);
            
            popup.show(e.getComponent(), e.getX(), e.getY());
            return true;
        }
    }
    
    
    
}
