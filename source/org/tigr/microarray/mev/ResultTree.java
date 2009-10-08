/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * ResultTree.java
 *
 * Created on December 22, 2003, 9:28 AM
 */

package org.tigr.microarray.mev;

import java.beans.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import org.tigr.graph.GraphViewer;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.util.FloatMatrix;

/**
 *
 * @author  braisted
 */
public class ResultTree extends JTree implements java.io.Serializable {
//    public static final long serialVersionUID = 100010201070001L;
    
    
    /** Root node
     */
    private DefaultMutableTreeNode root;
    /** analysis node
     */
    private DefaultMutableTreeNode analysisNode;
    
    Vector dumpVector;
    
    public ResultTree(){
        // super();
        this.setCellRenderer(new ResultTreeNodeRenderer());
    }
    
    /** Creates a new instance of ResultTree */
    public ResultTree(DefaultMutableTreeNode root) {
        super(root);
        this.root = root;
        this.setCellRenderer(new ResultTreeNodeRenderer());
        
        dumpVector = new Vector();
    }
    public ResultTree(DefaultMutableTreeNode root, DefaultMutableTreeNode analysisNode) {
        super(root);
        this.root = root;
        this.setCellRenderer(new ResultTreeNodeRenderer());
        this.analysisNode = analysisNode;
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
        Enumeration _enum = root.depthFirstEnumeration();
        DefaultMutableTreeNode node;
        while(_enum.hasMoreElements()){
            node = (DefaultMutableTreeNode)_enum.nextElement();
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
    
    /**
    * EH - for state-saving
    * Returns a hashtable of all the experiments contained within the result tree,
    * keyed on their ExptID.  Used by state-saving methods.  
    */
    public Hashtable getAllExperiments() {
    	Hashtable allExpts = new Hashtable();
		Enumeration allTreeNodes = ((DefaultMutableTreeNode)analysisNode).breadthFirstEnumeration();
		DefaultMutableTreeNode dmtn;
		while(allTreeNodes.hasMoreElements()){
			dmtn = (DefaultMutableTreeNode)allTreeNodes.nextElement();
			Object o = dmtn.getUserObject();
			if(o instanceof LeafInfo) {
				LeafInfo l = (LeafInfo)o;
				if(l.getViewer() != null){
					Experiment e = l.getViewer().getExperiment();
					if(e != null){
						allExpts.put(new Integer(e.getId()), l.getViewer().getExperiment());
        }
        }
    }
                    }
		return allExpts;
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
        Enumeration _enum;
        
        for(int i = 0; i < childCount; i++){
            analysisRoot = ((DefaultMutableTreeNode)(analysisNode.getChildAt(i)));
            object = analysisRoot.getUserObject();
            if(object != null){
                if(object instanceof LeafInfo){
                    algName = ((LeafInfo)object).toString();
                } else if(object instanceof String) {
                    algName = (String)object;
                }
                
                _enum = analysisRoot.depthFirstEnumeration();
                while (!stop && _enum.hasMoreElements()){
                    currentNode = (DefaultMutableTreeNode)_enum.nextElement();
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
    
    /** Support for gene or experiment search, retrieval of nodes for the
     * SearchResultDialg
     */
    
    public Vector findViewerCollection(int [] indices, boolean geneSearch) {
        Vector result = new Vector();
        
        
        
        Vector analysisNodes = new Vector();
        Hashtable expViewHash = new Hashtable();
        Hashtable tabViewHash = new Hashtable();
        
        Vector childNodes = new Vector();
        DefaultMutableTreeNode node;
        
        
        int childCount = analysisNode.getChildCount();
        boolean hasResult = false;
        
        for(int analysis = 0; analysis < childCount ; analysis++) {
            
            node = (DefaultMutableTreeNode)(analysisNode.getChildAt(analysis));
            
            //for each analysis node accumulate result nodes
            DefaultMutableTreeNode aNode = getSearchResults(indices, node, expViewHash, tabViewHash, geneSearch);
         
            if(aNode != null)
                analysisNodes.addElement(aNode);
        }
        
        
        result.add(analysisNodes);
        result.add(expViewHash);
        result.add(tabViewHash);
        
        if(expViewHash.size() == 0 && expViewHash.size() == 0)
            return null;
        
        return result;
    }
    
    private DefaultMutableTreeNode getSearchResults(int [] indices, DefaultMutableTreeNode analRoot, Hashtable expHash, Hashtable tabHash, boolean geneSearch) {
        boolean result = false;
        
        Enumeration _enum = analRoot.depthFirstEnumeration();
        DefaultMutableTreeNode currNode;
        Object currUserObject;
        IViewer currViewer;
        LeafInfo currLeafInfo;
        Experiment currExperiment;
        int [][] clusters;
        int clusterIndex;
        Object currIndexObject;
        boolean containsIndex;
        
        Vector expViewers = new Vector();
        Vector tabViewers = new Vector();
        
        DefaultMutableTreeNode newNode;
        
        DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode(analRoot.getUserObject());
        
      
        while(_enum.hasMoreElements()) {
            
            currNode = (DefaultMutableTreeNode)(_enum.nextElement());
            currUserObject = currNode.getUserObject();
            
            if(currUserObject instanceof LeafInfo) {
                currLeafInfo = (LeafInfo)currUserObject;
                currViewer = (currLeafInfo).getViewer();
                
                if(currViewer != null) {
                    
                    if(geneSearch) {                    
                        if( currViewer instanceof ExperimentViewer ) {
                            currIndexObject = ((LeafInfo)currUserObject).getUserObject();
                            if(currIndexObject != null && currIndexObject instanceof Integer) {
                                clusterIndex = ((Integer)currIndexObject).intValue();
                                
                                clusters = currViewer.getClusters();
                                currExperiment = currViewer.getExperiment();
                                
                                if(clusters != null && currExperiment != null) {
                                    if(containsGeneIndices(indices, clusters[clusterIndex], currExperiment)) {
                                        result = true;
                                        expViewers.addElement(currNode);
                                        //newNode = new DefaultMutableTreeNode(new LeafInfo(currLeafInfo.getName(), currLeafInfo.getViewer(), currLeafInfo.getJPopupMenu(), currLeafInfo.getUserObject()));
                                        //expViewers.addElement(newNode);
                                    }
                                }
                            }
                        } else if(currViewer instanceof ClusterTableViewer) {
                            currIndexObject = ((LeafInfo)currUserObject).getUserObject();
                            if(currIndexObject != null && currIndexObject instanceof Integer) {
                                clusterIndex = ((Integer)currIndexObject).intValue();
                                
                                clusters = currViewer.getClusters();
                                currExperiment = currViewer.getExperiment();
                                
                                if(clusters != null && currExperiment != null) {
                                    if(containsGeneIndices(indices, clusters[clusterIndex], currExperiment)) {
                                        result = true;                                                                                
                                        tabViewers.addElement(currNode);
                                        //newNode = new DefaultMutableTreeNode(new LeafInfo(currLeafInfo.getName(), currLeafInfo.getViewer(), currLeafInfo.getJPopupMenu(), currLeafInfo.getUserObject()));                                                                                
                                        //tabViewers.addElement(newNode);
                                    }
                                }
                            }             
                        }
                        
           
                        
                        
                    } else { //experiment search
                        
                        if(currViewer instanceof ExperimentClusterViewer) {
                            currIndexObject = ((LeafInfo)currUserObject).getUserObject();
                            if(currIndexObject != null && currIndexObject instanceof Integer) {
                                clusterIndex = ((Integer)currIndexObject).intValue();
                                
                                clusters = currViewer.getClusters();
                                currExperiment = currViewer.getExperiment();
                                
                                if(clusters != null && currExperiment != null) {
                                    if(containsExperimentIndices(indices, clusters[clusterIndex], currExperiment)) {
                                        result = true;                 
                                        expViewers.addElement(currNode);
                                        //newNode = new DefaultMutableTreeNode(new LeafInfo(currLeafInfo.getName(), currLeafInfo.getViewer(), currLeafInfo.getJPopupMenu(), currLeafInfo.getUserObject()));
                                        //expViewers.addElement(newNode);
                                    }
                                }
                            }
                        } else if(currViewer instanceof ExperimentClusterTableViewer){
                            currIndexObject = ((LeafInfo)currUserObject).getUserObject();
                            if(currIndexObject != null && currIndexObject instanceof Integer) {
                                clusterIndex = ((Integer)currIndexObject).intValue();
                                
                                clusters = currViewer.getClusters();
                                currExperiment = currViewer.getExperiment();
                                
                                if(clusters != null && currExperiment != null) {
                                    if(containsExperimentIndices(indices, clusters[clusterIndex], currExperiment)) {
                                        result = true;
                                        tabViewers.addElement(currNode);
                                        //newNode = new DefaultMutableTreeNode(new LeafInfo(currLeafInfo.getName(), currLeafInfo.getViewer(), currLeafInfo.getJPopupMenu(), currLeafInfo.getUserObject()));
                                        //tabViewers.addElement(newNode);
                                    }
                                }
                            }
                            
                            
                            
                        }
                        
                    }
                }
            } 
            
        }
        
        if(result) {
            expHash.put(newRoot, expViewers);
            tabHash.put(newRoot, tabViewers);
            return newRoot;
        }
        
        return null;
    }
    
    
    private boolean containsGeneIndices(int [] indices, int [] clusterIndices, Experiment experiment) {
        for(int i = 0; i < indices.length; i++) {
            for(int j = 0; j < clusterIndices.length; j++) {
                if(indices[i] == experiment.getGeneIndexMappedToData(clusterIndices[j]))
                    return true;
            }
        }        
        return false;
    }
    
    private boolean containsExperimentIndices(int [] indices, int [] clusterIndices, Experiment experiment) {
        int [] colIndices = experiment.getColumnIndicesCopy();
        
        for(int i = 0; i < indices.length; i++) {
            for(int j = 0; j < clusterIndices.length; j++) {
                if(indices[i] == colIndices[clusterIndices[j]])
                    return true;
            }
        }
        
        return false;
    }    
    
    /**
     *  Clears data selection over the tree
     */
    public void clearDataSelection() {
        Enumeration  _enum = this.root.depthFirstEnumeration();
        DefaultMutableTreeNode node;
        while(_enum.hasMoreElements()) {
            node = (DefaultMutableTreeNode)(_enum.nextElement());
            if(node.getUserObject() != null && node.getUserObject() instanceof LeafInfo)
                ((LeafInfo)node.getUserObject()).setSelectedDataSource(false);
        }
    }
    
    /** Renders the <CODE>ResultTree</CODE>.
     */
    public class ResultTreeNodeRenderer extends DefaultTreeCellRenderer {
        
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
        /** P Value graph Icon
         */
        private Icon pValueIcon = GUIFactory.getIcon("TableViewerResult.gif");
        /** Test statistic viewer icon
         */
        private Icon testStatValueIcon = GUIFactory.getIcon("TestStatViewer.gif");
        
        
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
        /**
         * Search Icon         
         */
        private Icon searchIcon = GUIFactory.getIcon("search_16.gif");   
        /**
         * GO hierarchy viewer Icon         
         */
        private Icon goHierarchyViewerIcon = GUIFactory.getIcon("go_hierarchy_viewer.gif"); 
        /**
         * Data Selection Icon         
         */
        private Icon dataSelectionIcon = GUIFactory.getIcon("data_selection_icon.gif");         
        /**
         * Data Filter Icon
         */
        private Icon dataFilterIcon = GUIFactory.getIcon("DataFilterResult.gif");
        /**
         * LEM Result Node
         */
        private Icon lemViewerIcon = GUIFactory.getIcon("LEM_result.gif");   
        /**
         * CGH Chromosome Views Icon
         */
        private Icon chrViewIcon = GUIFactory.getIcon("cgh_chrom.gif");
        /**
         * CGH Experiment Views Icon
         */
        private Icon exprViewIcon = GUIFactory.getIcon("cgh_circular.gif");
        private Icon cirViewIcon = GUIFactory.getIcon("Chr_Circular.gif");
        /**
         * CGH Sex Chromosomes Icon
         */
        private Icon chrXYIcon = GUIFactory.getIcon("Chr_XY.gif");
        /**
         * CGH Autosomes Icon
         */
        private Icon chrIcon = GUIFactory.getIcon("Chr_Auto.gif");
        /**
         * CGH Amplification Deletion Icon
         */
        private Icon ampDelIcon = GUIFactory.getIcon("Cgh_Amp_Del.gif");       
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
        public ResultTreeNodeRenderer(){
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
                        
            this.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
            
            if(!isLeaf){
                
                //set the text
                if(userObj instanceof String){
                    text = (String)userObj;
                    setText(text);
                } else if(userObj instanceof LeafInfo){
                    text = ((LeafInfo)userObj).toString();
                    setText(text);        
                    if(((LeafInfo)userObj).isSelectedDataSource())       
                        setBorder(BorderFactory.createLineBorder(Color.green, 2));
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
                } else if(text.equals("Hierarchical Trees")||text.startsWith("Consensus")||parentText.startsWith("Consensus")){
                    setIcon(hclIcon);
                } else if(text.equals("SOM Visualization")){
                    setIcon(SOMColorIcon);
                } else if(text.indexOf("Network") != -1) {
                    setIcon(networkIcon);
                } else if(text.indexOf("Script (") != -1) {
                    setIcon(scriptIcon);
                } else if(text.equals("Search Result Shortcuts")) {
                    setIcon(searchIcon);                    
                } else if(text.indexOf("Data Filter") != -1) {
                        setIcon(dataFilterIcon);
                } else if(text.equals("Original Data")){
                    setIcon(mainViewIcon);
                } else if(text.equals("Experiment Viewer")){
                    setIcon(mainViewIcon);
                } else if(text.equals("Geneset p-value graph")){
                	setIcon(pValueIcon);
                } else if(text.equals("Test statistics graph")){
                	setIcon(testStatValueIcon);
                } 	/* CGH Icons */ 
                else if(text.equals("Chromosome Views")){
                
                    setIcon(chrViewIcon);
                } else if(text.equals("Chromosome 1") | text.equals("Chromosome 2") | text.equals("Chromosome 3")
                		 |text.equals("Chromosome 4") | text.equals("Chromosome 5") | text.equals("Chromosome 6")
                		 |text.equals("Chromosome 7") | text.equals("Chromosome 8") | text.equals("Chromosome 9")
                		 |text.equals("Chromosome 10") | text.equals("Chromosome 11") | text.equals("Chromosome 12")
                		 |text.equals("Chromosome 13") | text.equals("Chromosome 14") | text.equals("Chromosome 15")
                		 |text.equals("Chromosome 16") | text.equals("Chromosome 17") | text.equals("Chromosome 18")
                		 |text.equals("Chromosome 19") | text.equals("Chromosome 20") | text.equals("Chromosome 21")
                		 |text.equals("Chromosome 22")){
                    setIcon(chrIcon);
                } else if(text.equals("Chromosome X") | text.equals("Chromosome Y")){
                    setIcon(chrXYIcon);
                } else if(text.equals("Experiment Views")){
                    setIcon(exprViewIcon);
                } else if(text.equals("Results")| text.equals("A and B") | text.equals("B Only") | text.equals("A Only")) {
                    setIcon(tableIcon);
                } else if(text.indexOf("Amplifications") != -1 | text.indexOf("Deletions") != -1) {
                    setIcon(ampDelIcon);
                } /* CGH Icons */
                
            } else {  //it's a leaf
                setIcon(leafIcon);
                if(userObj instanceof String){
                    text = ((String)userObj);
                    setText(text);
                    setIcon(dotTerminalIcon);
                } else if(userObj instanceof LeafInfo) {
                    text = ((LeafInfo)userObj).toString();
                    setText(text);
                    
                    if(((LeafInfo)userObj).isSelectedDataSource())       
                        setBorder(BorderFactory.createLineBorder(Color.green, 2));                    
                    
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
                    } else if(text.equals("Original Data")){
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
                    || text.indexOf("HCL Tree") != -1 || text.indexOf("Support Tree") != -1||text.startsWith("Consensus")||parentText.startsWith("Consensus")){
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
                    } else if(text.indexOf("Script Tree") != -1) {
                        setIcon(scriptTreeViewerIcon);
                    } else if(text.equals("Script XML Viewer")) {
                        setIcon(scriptXMLViewerIcon);
                    } else if(text.indexOf("GO") != -1) {
                        setIcon(goHierarchyViewerIcon);
                    } else if(text.equals("Data Source Selection")) {
                        setIcon(dataSelectionIcon);
                    } else if(text.indexOf("LEM Viewer") != -1) {
                    	setIcon(lemViewerIcon);
                    } else if(text.indexOf("Geneset p-value graph") != -1){
                        setIcon(pValueIcon);
                    }else if(text.indexOf("Test statistics graph") != -1){
                        setIcon(testStatValueIcon);
                    } /* CGH Icons */else if (text.equals("Experiment Views")) {
                        setIcon(exprViewIcon);
                    } else if(parentText.equals("Experiment Views")) {
                        setIcon(cirViewIcon);
                    } else if (text.equals("Chromosome Views")) {
                        setIcon(chrViewIcon);
                    } else if(text.equals("Chromosome 1") | text.equals("Chromosome 2") | text.equals("Chromosome 3")
	                		 |text.equals("Chromosome 4") | text.equals("Chromosome 5") | text.equals("Chromosome 6")
	                		 |text.equals("Chromosome 7") | text.equals("Chromosome 8") | text.equals("Chromosome 9")
	                		 |text.equals("Chromosome 10") | text.equals("Chromosome 11") | text.equals("Chromosome 12")
	                		 |text.equals("Chromosome 13") | text.equals("Chromosome 14") | text.equals("Chromosome 15")
	                		 |text.equals("Chromosome 16") | text.equals("Chromosome 17") | text.equals("Chromosome 18")
	                		 |text.equals("Chromosome 19") | text.equals("Chromosome 20") | text.equals("Chromosome 21")
	                		 |text.equals("Chromosome 22") ){
                    	setIcon(chrIcon);
                    } else if(text.equals("Chromosome X") | text.equals("Chromosome Y")){
                        setIcon(chrXYIcon);
                    } else if(text.equals("Results") | text.equals("A and B") | text.equals("B Only") | text.equals("A Only")) {
                        setIcon(tableIcon);
                    } else if(text.indexOf("Amplifications") != -1 | text.indexOf("Deletions") != -1) {
                        setIcon(ampDelIcon);
                    } /* CGH Icons */
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
            } else {
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
