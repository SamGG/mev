/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * ResultTree.java
 *
 * Created on December 22, 2003, 9:28 AM
 */

package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingConstants;

import javax.swing.border.EmptyBorder;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.UIManager;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.IViewer;


import java.beans.*;
/**
 *
 * @author  braisted
 */
public class ResultTree extends JTree implements java.io.Serializable {
    
    /** Root node
     */
    private DefaultMutableTreeNode root;
    /** analysis node
     */
    private DefaultMutableTreeNode analysisNode;
    
    Vector dumpVector;
    
    public ResultTree(){
        // super();
        this.setCellRenderer(new NodeRenderer());
    }
    
    /** Creates a new instance of ResultTree */
    public ResultTree(DefaultMutableTreeNode root) {
        super(root);
        this.root = root;
        this.setCellRenderer(new NodeRenderer());
        
        dumpVector = new Vector();
    }
    
    
    
    /** Returns the root node.
     * @return
     */
    public DefaultMutableTreeNode getRoot() {
        return root;
    }
    /** Sets the root node.
     */
    public void setRoot(DefaultMutableTreeNode r) {
        this.root = r;
    }
    /** Returns the analysis node.
     */
    public DefaultMutableTreeNode getAnalysisNode() {
        return analysisNode;
    }
    /** Sets the analysis node.
     */
    public void setAnalysisNode(DefaultMutableTreeNode n) {
        this.analysisNode = n;
    }
    
    /** Inserts a node.
     */
    public void insertNode(DefaultMutableTreeNode node, DefaultMutableTreeNode parentNode, int locationIndex){
        if(node == null || parentNode == null)
            return;
        DefaultTreeModel treeModel = (DefaultTreeModel)getModel();
        treeModel.insertNodeInto((DefaultMutableTreeNode)node, (DefaultMutableTreeNode)parentNode, locationIndex);
    }
    
    /** Removes a Node.
     */
    public void removeNode(DefaultMutableTreeNode node) {
        DefaultTreeModel treeModel = (DefaultTreeModel)getModel();
        treeModel.removeNodeFromParent(node);
    }
    
    /**
     * @param object
     * @return  */
    public DefaultMutableTreeNode getNode(Object object) {
        Enumeration enum = root.depthFirstEnumeration();
        DefaultMutableTreeNode node;
        while(enum.hasMoreElements()){
            node = (DefaultMutableTreeNode)enum.nextElement();
            if(node.getUserObject() == object)
                return node;
        }
        return null;
    }
    
    /** Returns the terminal node give a node name
     * path from root to termial node.
     * @param nodeNames
     * @return
     */
    public DefaultMutableTreeNode getNode(String [] nodeNames) {
        DefaultMutableTreeNode node = root;
        
        for(int i = 0; i < nodeNames.length; i++) {
            for(int j = 0; j < node.getChildCount(); j++){
                if( (node.getChildAt(j).toString()).equals(nodeNames[i])){
                    node = (DefaultMutableTreeNode)node.getChildAt(j);
                    continue;
                }
            }
        }
        
        //If final node string equals last node name return node
        if(node.toString().equals(nodeNames[nodeNames.length-1]))
            return node;
        
        return null;
    }
    
    
    /** Saves the analysis node state.
     */
    public void writeResults(ObjectOutputStream oos) throws IOException {
        
        DefaultMutableTreeNode timeNode = (DefaultMutableTreeNode)(analysisNode.getLastChild());
        
        boolean removedTimeNode = false;
        
        if(timeNode != null){
            //last analysis node is childless, indicates just a time stamp
            if(timeNode.getChildCount() == 0){
                DefaultTreeModel treeModel = (DefaultTreeModel)this.getModel();
                treeModel.removeNodeFromParent(timeNode);
                removedTimeNode = true;
            }
        }
        
        // work on analysis node
        writeTree(oos, analysisNode, 0);
        oos.writeInt(-1);
        if(removedTimeNode){
            DefaultTreeModel treeModel = (DefaultTreeModel)this.getModel();
            treeModel.insertNodeInto(timeNode, analysisNode, analysisNode.getChildCount());
        }
        
    }
    
    /** Writes the hitory node.
     */
    public void writeHistory(ObjectOutputStream oos, DefaultMutableTreeNode historyNode) throws IOException {
        // work on analysis node
        writeTree(oos, historyNode, 0);
        oos.writeInt(-1);
    }
    
    /** Writes the tree recursively.
     * @param oos ObjectOutputStream
     * @param node Current Node
     * @param level Level from highest node.
     * @throws IOException
     */
    private void writeTree(ObjectOutputStream oos, DefaultMutableTreeNode node, int level) throws IOException{
        int cnt = node.getChildCount();
        oos.writeInt(level);
        Object obj = ((DefaultMutableTreeNode)node).getUserObject();
        oos.writeObject(obj);
        for(int i = 0; i < cnt; i++){
            writeTree(oos, (DefaultMutableTreeNode) node.getChildAt(i), level+1);
        }
    }
    
    
    /** Loads a new analysis node from an <CODE>ObjectInputStream</CODE>.
     *
     */
    public DefaultMutableTreeNode loadResults(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        int currLevel = 0;
        int level = 0;
        int prevLevel = -1;
        Object obj;
        DefaultMutableTreeNode root = null, parent = null, child = null;
        int levelChange = 0;
        
        boolean end = false;
        while(!end){
            currLevel = ois.readInt();
            if(currLevel == 0){
                obj = ois.readObject();
                
                root = new DefaultMutableTreeNode(obj);
                parent = root;
                child = root;
                prevLevel = currLevel;
            } else if(currLevel == -1){
                end = true;
            } else {
                levelChange = currLevel - prevLevel;
                prevLevel = currLevel;
                if(levelChange > 0) {  //Deeper in tree
                    parent = child;
                } else if(levelChange < 0) { //Higer in tree
                    for(int i = 0; i > levelChange; i--){
                        parent = (DefaultMutableTreeNode)parent.getParent();
                    }
                }
                
                obj = ois.readObject();
                child = new DefaultMutableTreeNode(obj);
                parent.add(child);
            }
        }
        return root;
    }
    
    public Hashtable getResultHash(){
        Hashtable table = new Hashtable();
        DefaultMutableTreeNode analysisRoot;
        DefaultMutableTreeNode currentNode;
        Object object;
        Object [] vals;
        boolean stop = false;
        
        IViewer viewer;
        Experiment exp;
        int [][] clusters;
        
        int childCount = analysisNode.getChildCount();
        //String algTitles = new String[analysisNode.getChildCount()];
        String algName = "";
        Enumeration enum;
        
        for(int i = 0; i < childCount; i++){
            analysisRoot = ((DefaultMutableTreeNode)(analysisNode.getChildAt(i)));
            object = analysisRoot.getUserObject();
            if(object != null){
                if(object instanceof LeafInfo){
                    algName = ((LeafInfo)object).toString();
                } else if(object instanceof String) {
                    algName = (String)object;
                }
                
                enum = analysisRoot.depthFirstEnumeration();
                while (!stop && enum.hasMoreElements()){
                    currentNode = (DefaultMutableTreeNode)enum.nextElement();
                    if(currentNode.getUserObject() instanceof LeafInfo){
                        viewer = ((LeafInfo)currentNode.getUserObject()).getViewer();
                        if(viewer != null) {
                            exp = viewer.getExperiment();
                            clusters = viewer.getClusters();
                            if(exp != null && clusters != null) {
                                vals = new Object[2];
                                vals[0] = exp;
                                vals[1] = clusters;
                                table.put(algName, vals);
                                stop = true;
                            }
                        }
                    }
                    
                }
                stop = false;
            }
        }
        return table;
    }
    
    /** Renders the <CODE>ResultTree</CODE>.
     */
    private class NodeRenderer extends DefaultTreeCellRenderer {
        
        /** Default leaf icon to display
         */
        private Icon defaultLeafIcon;
        /** Icon for primary level results
         */
        private Icon primaryResultIcon = GUIFactory.getIcon("PrimaryResult.gif");
        /** Expression Image Icon
         */
        private Icon expressionImageIcon = GUIFactory.getIcon("ExpressionImageResult.gif");
        /** Centroid Graph Icon
         */
        private Icon centroidGraphIcon = GUIFactory.getIcon("CentroidResult.gif");
        /** Expression Graph Icon
         */
        private Icon expressionGraphIcon = GUIFactory.getIcon("ExpressionGraphResult.gif");
        /** HCL tree icon
         */
        private Icon hclIcon = GUIFactory.getIcon("HCLResult.gif");
        /** Node Height Graph Icon
         */
        private Icon nodeHeightIcon = GUIFactory.getIcon("NodeHeightResult.gif");
        /** SAM Graph Icon
         */
        private Icon samGraphIcon = GUIFactory.getIcon("SAMGraphResult.gif");
        /** Volcano Plot Icon
         */
        private Icon volcanoIcon = GUIFactory.getIcon("VolcanoPlotResult.gif");
        /** Network Icon
         */
        private Icon networkIcon = GUIFactory.getIcon("NetworkResult.gif");
        /** SOM Display Icon
         */
        private Icon SOMColorIcon = GUIFactory.getIcon("SOMColorResult.gif");
        /** SOM B/W Icon
         */
        private Icon SOMBWIcon = GUIFactory.getIcon("SOMBWResult.gif");
        /** GDM Matrix Icon
         */
        private Icon gdmMatrixIcon = GUIFactory.getIcon("GDMMatrixResult.gif");
        /** Cluster Info Icon
         */
        private Icon clusterInfoIcon = GUIFactory.getIcon("ClusterInformationResult.gif");
        /** General Information Icon
         */
        private Icon generalInfoIcon = GUIFactory.getIcon("Information16.gif");
        /** Table Icon
         */
        private Icon tableIcon = GUIFactory.getIcon("TableViewerResult.gif");
        /** PCA 3D icon
         */
        private Icon pca3DIcon = GUIFactory.getIcon("PCA3DResult.gif");
        /** Terrain Icon
         */
        private Icon trn3DIcon = GUIFactory.getIcon("TerrainResult.gif");
        
        /** MeV Root Icon
         */
        private Icon mevIcon = GUIFactory.getIcon("mev_mini_splash.gif");
        /** Main Expression View Icon
         */
        private Icon mainViewIcon = GUIFactory.getIcon("MainView.gif");
        /** Analysis Icon
         */
        private Icon analysisIcon = GUIFactory.getIcon("Analysis.gif");
        /** Cluster Manager Icon
         */
        private Icon clusterManagerIcon = GUIFactory.getIcon("ClusterManager.gif");
        /** History Icon
         */
        private Icon historyIcon = GUIFactory.getIcon("History.gif");
        
        /** String Terminal Icon
         */
        private Icon dotTerminalIcon = GUIFactory.getIcon("TerminalDot.gif");
        /**Script Manager Icon
         */
        private Icon scriptManagerIcon = GUIFactory.getIcon("ScriptManager.gif");
        /**
         * Script Object Viewer Icon
         */
        private Icon scriptIcon = GUIFactory.getIcon("ScriptIcon.gif");
        /**
         * Script Tree Viewer Icon
         */
        private Icon scriptTreeViewerIcon = GUIFactory.getIcon("ScriptTreeViewer.gif");
        /**
         * Script XML Viewer Icon
         */
        private Icon scriptXMLViewerIcon = GUIFactory.getIcon("ScriptXMLViewer.gif");        
        /** Parent node
         */
        private DefaultMutableTreeNode parent;
        /** Grandparent node
         */
        private DefaultMutableTreeNode  grandParent;
        
        /** node label
         */
        private JLabel label;
        
        /** Creats a new NodeRenderer.
         */
        public NodeRenderer(){
            super();
            
            this.setIcon(closedIcon);
            super.setOpaque(false);
            this.setIconTextGap(2);
        }
        
        
        /** Returns the component to display for a given
         * tree node.
         */
        public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean selected, boolean expanded, boolean isLeaf, int row, boolean hasFocus) {
            this.selected = selected;
            
            String text = "", parentText= "", grandParentText= "";
            Object userObj = ((DefaultMutableTreeNode)value).getUserObject();
            //setText("");
            //setIcon(null)
            //this.setBorder(null);
            this.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
            
            if(!isLeaf){
                
                //set the text
                if(userObj instanceof String){
                    text = (String)userObj;
                    setText(text);
                } else if(userObj instanceof LeafInfo){
                    text = ((LeafInfo)userObj).toString();
                    setText(text);
                }
                
                //assign default icons
                if(expanded){
                    setIcon(openIcon);
                } else
                    setIcon(closedIcon);
                
                //check for special Icons
                if(text.indexOf("Expression Image") != -1){
                    setIcon(expressionImageIcon);
                } else if(text.indexOf("Centroid") != -1){
                    setIcon(centroidGraphIcon);
                } else if(text.indexOf("Expression Graph") != -1){
                    setIcon(expressionGraphIcon);
                } else if(text.equals("Analysis Results")){
                    setIcon(analysisIcon);
                } else if(text.equals("Cluster Manager")){
                    setIcon(clusterManagerIcon);
                } else if (text.equals("Script Manager")){
                    setIcon(scriptManagerIcon);
                } else if(text.equals("History")){
                    setIcon(historyIcon);
                } else if(text.equals("General Information")){
                    setIcon(generalInfoIcon);
                } else if(text.indexOf('(') != -1 && ((DefaultMutableTreeNode)value).getParent() == root.getChildAt(2)){
                    setIcon(primaryResultIcon);
                } else if(text.indexOf("F-Ratio") != -1 ||
                text.indexOf("Statistic") != -1 || text.indexOf("Table") != -1){
                    setIcon(tableIcon);
                } else if(text.equals("Cluster Information")){
                    setIcon(clusterInfoIcon);
                } else if(text.equals("MultipleExperimentViewer")){
                    this.setText("");
                    this.setBorder(new EmptyBorder(3,2,3,0));
                    setIcon(mevIcon);
                } else if(text.equals("Hierarchical Trees")){
                    setIcon(hclIcon);
                } else if(text.equals("SOM Visualization")){
                    setIcon(SOMColorIcon);
                } else if(text.indexOf("Network") != -1) {
                    setIcon(networkIcon);
                } else if(text.indexOf("Script (") != -1) {
                    setIcon(scriptIcon);
                }
                
            } else {  //it's a leaf
                setIcon(leafIcon);
                if(userObj instanceof String){
                    text = ((String)userObj);
                    setText(text);
                    setIcon(dotTerminalIcon);
                } else if(userObj instanceof LeafInfo) {
                    text = ((LeafInfo)userObj).toString();
                    setText(text);
                    
                    parent = ((DefaultMutableTreeNode)((DefaultMutableTreeNode)value).getParent());
                    //get text
                    if(parent.getUserObject() instanceof String)
                        parentText = (String)(parent.getUserObject());
                    else if(parent.getUserObject() instanceof LeafInfo)
                        parentText = ((LeafInfo)(parent.getUserObject())).toString();
                    
                    grandParent = (DefaultMutableTreeNode)parent.getParent();
                    if(grandParent != null){
                        if(grandParent.getUserObject() instanceof String)
                            grandParentText = (String)(grandParent.getUserObject());
                        else if(grandParent.getUserObject() instanceof LeafInfo)
                            grandParentText = ((LeafInfo)(grandParent.getUserObject())).toString();
                    } else {
                        grandParentText = null;
                    }
                    
                    //assign icon
                    if(parentText.indexOf("Expression Image") != -1){
                        setIcon(expressionImageIcon);
                    } else if(parentText.indexOf("Centroid") != -1){
                        setIcon(centroidGraphIcon);
                    } else if(parentText.indexOf("Expression Graph") != -1){
                        setIcon(expressionGraphIcon);
                    } else if(parentText.indexOf("Cluster Manager") != -1){
                        setIcon(tableIcon);
                    } else if(parentText.indexOf("F-Ratio") != -1 ||
                    parentText.indexOf("Statistic") != -1) {
                        setIcon(tableIcon);
                    } else if(parentText.equals("Cluster Information") || text.equals("Classification Information")){
                        setIcon(clusterInfoIcon);
                    } else if(text.equals("MultipleExperimentViewer")){
                        setIcon(openIcon);
                    } else if(text.equals("Main View")){
                        setIcon(mainViewIcon);
                    } else if(text.equalsIgnoreCase("Analysis Results")){
                        setIcon(analysisIcon);
                    } else if(text.equalsIgnoreCase("Cluster Manager")){
                        setIcon(clusterManagerIcon);
                    } else if (text.equals("Script Manager")){
                        setIcon(scriptManagerIcon);
                    } else if(text.equalsIgnoreCase("History")){
                        setIcon(historyIcon);
                    } else if(text.indexOf("able") != -1){ //table viewer
                        setIcon(tableIcon);
                    } else if(parentText.indexOf("Hierarchical") != -1 || text.indexOf("Dendogram") != -1
                            || text.indexOf("HCL Tree") != -1 || text.indexOf("Support Tree") != -1){
                        setIcon(hclIcon);
                    } else if(text.equals("Expression Image")){
                        setIcon(expressionImageIcon);
                    } else if(text.equals("Expression Graph")){
                        setIcon(expressionGraphIcon);
                    } else if(text.equals("Centroid Graph")){
                        setIcon(centroidGraphIcon);
                    } else if(parentText.indexOf("Table") != -1 || text.indexOf("SVM") != -1) {
                        setIcon(tableIcon);
                    } else if(text.indexOf("Height Plot") != -1 || text.indexOf("SOTA Diversity") != -1 || text.indexOf("Graph - FOM") != -1){
                        setIcon(nodeHeightIcon);
                    } else if(text.equals("Volcano Plot")){
                        setIcon(volcanoIcon);
                    } else if(text.equals("SAM Graph")){
                        setIcon(samGraphIcon);
                    } else if(text.indexOf("Network") != -1){
                        setIcon(networkIcon);
                    } else if(text.equals("Matrix View")){
                        setIcon(gdmMatrixIcon);
                    } else if(text.equals("U-Matrix Color")){
                        setIcon(SOMColorIcon);
                    } else if(text.equals("U-Matrix Distance")){
                        setIcon(SOMBWIcon);
                    } else if(text.equals("3D view")) {
                        setIcon(pca3DIcon);
                    } else if(text.equals("Map") && parentText.indexOf("Terrain") != -1) {
                        setIcon(trn3DIcon);
                    } else if(text.equals("Script Tree Viewer")) {
                        setIcon(scriptTreeViewerIcon);
                    } else if(text.equals("Script XML Viewer")) {
                        setIcon(scriptXMLViewerIcon);
                    }
                    //add new icons here for leaf icons
                    
    
                    else if(grandParentText != null){
                        if(grandParentText.indexOf("Expression Image") != -1){
                            setIcon(expressionImageIcon);
                        } else if(grandParentText.indexOf("Centroid") != -1){
                            setIcon(centroidGraphIcon);
                        } else if(grandParentText.indexOf("Expression Graph") != -1){
                            setIcon(expressionGraphIcon);
                        }
                    }
                }
            }
            
            if(selected){
                setOpaque(true);
            } else{
                setOpaque(false);
            }
            
            return this;
        }
        
        /** Repaints the component.
         * @param g Graphics Object
         */
        public void paint(Graphics g){
            
            if(g == null)
                return;
            
            Color c;
            if (this.selected)
                c = this.getBackgroundSelectionColor();
            else
                c = this.getBackgroundNonSelectionColor();
            
            g.setColor(c);
            
            int width = this.getWidth();
            
            if(this.getIcon() != null)
                width -= this.getIcon().getIconWidth();
            
            g.fillRect((this.getWidth() - width)+1, 0, width+1, this.getHeight());
            
            super.setOpaque(false);
            super.paint(g);
        }
        
        
        
    }
    
}
