/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * ScriptTree.java
 *
 * Created on February 28, 2004, 5:16 PM
 */

package org.tigr.microarray.mev.script.util;

import java.awt.Color;
import java.awt.Component;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JTree;

import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.plaf.basic.BasicTreeUI;
import org.tigr.util.FloatMatrix;

import org.tigr.microarray.mev.script.ScriptDocument;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Comment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import org.tigr.microarray.mev.script.ScriptManager;
import org.tigr.microarray.mev.script.scriptGUI.ScriptTreeRenderer;

import org.tigr.microarray.mev.cluster.gui.Experiment;
/**
 *
 * @author  braisted
 */
public class ScriptTree extends JTree {
    
    
    ScriptDocument document;
    ScriptManager manager;    
    DataNode primaryDataRoot;
    Hashtable dataNodeHash;
    
    boolean resetWidth = true;
    
    /** Creates a new instance of ScriptTree */
    public ScriptTree(ScriptDocument doc, ScriptManager manager) {
        super();
        super.setCellRenderer(new ScriptTreeRenderer());
        super.setForeground(Color.black);
        super.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        
        BasicTreeUI basicTreeUI = (BasicTreeUI) getUI();
        basicTreeUI.setRightChildIndent(50);
        super.putClientProperty("JTree.lineStyle", "Angled");
        
        this.document = doc;
        this.manager = manager;
        
        dataNodeHash = new Hashtable();
        constructTree(document.getDocument());
        scrollAllToVisible();
    }
    
    public ScriptDocument getDocument() {
        return document;
    }
    
    public void constructTree(Document doc) {
        Element root = doc.getDocumentElement();
        
        NodeList primaryData = doc.getElementsByTagName("primary_data");
        Element primaryDataElement;
        
        if(primaryData != null) {
            primaryDataElement = (Element)primaryData.item(0);
            primaryDataRoot = new DataNode(Integer.parseInt(primaryDataElement.getAttribute("id")), "Primary Data", "primary-data", primaryDataElement.getAttribute("type"));
            ((DefaultTreeModel)getModel()).setRoot(primaryDataRoot);
            dataNodeHash.put(primaryDataElement.getAttribute("id"), primaryDataRoot);
        }
        
        NodeList algSetList = doc.getElementsByTagName("alg_set");
        int numberOfSets = algSetList.getLength();
        
        //add all alg sets in order of id until all are added
        //numbering need not be continuous
        
        Element currAlgSet;
        int index = 0;
        int addedSets = 0;
        while(addedSets < numberOfSets && index < 100){
            //Retrieve Algorithm Sets in order by alg_set_id
            currAlgSet = getAlgSet(algSetList, index);
            index++;
            if(currAlgSet != null) {
                addAlgSet(currAlgSet);
                addedSets++;
            }
        }
    }
    
    
    public boolean addAlgSet(Element algSet) {
        NodeList algList = algSet.getElementsByTagName("algorithm");
        Element currAlgorithm;
        int listLength = algList.getLength();
        int addCnt = 0;
        int index = 0;
        while(addCnt < listLength && index < 100) {
            currAlgorithm = getAlgorithmElement(algList, index);
            index++;
            if(currAlgorithm != null) {
                addAlgorithm(currAlgorithm, algSet.getAttribute("input_data_ref"));
                addCnt++;
            }
        }
        return true;
    }
    
    
    public boolean addAlgorithm(Element algorithmElement, String dataRef) {
        int alg_id, alg_name, input_data_ref;
        String name = algorithmElement.getAttribute("alg_name");
        String  alg_type = algorithmElement.getAttribute("alg_type");
        
        try {
            alg_id = Integer.parseInt(algorithmElement.getAttribute("alg_id"));
            input_data_ref = Integer.parseInt(algorithmElement.getAttribute("input_data_ref"));
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return false;
        }
        
        //Make algorithm node
        AlgorithmNode algNode = new AlgorithmNode(name, alg_id, input_data_ref, alg_type);
        
        //Make AlgorithmData and set into algorithm node
        AlgorithmData data = constructAlgorithmData(algorithmElement);
        algNode.setAlgorithmData(data);
        
        //append output nodes to algorithm nodes, push data nodes into hash
        appendOutputNodes(algNode, algorithmElement);
        
        appendAlgorithmNode(algNode, dataRef);
        
        return true;
    }
    
    public void appendAlgorithmNode(AlgorithmNode node, String inputDataRef) {
        DataNode dataNode = (DataNode)dataNodeHash.get(inputDataRef);
        if(dataNode != null) {
            DefaultTreeModel model = (DefaultTreeModel)getModel();
            model.insertNodeInto(node, dataNode, dataNode.getChildCount());
            System.out.println("appended alg node to data node");
        } else {
            System.out.println("null data node can't append");
        }
    }
    
    public Element getAlgSet(NodeList setList, int id) {
        Element currElement;
        for(int i = 0; i < setList.getLength(); i++) {
            System.out.println("get alg set !!!!!!!!!!!! id ="+ id+"  algset attr = "+((Element)setList.item(i)).getAttribute("set_id"));
            if(((Element)setList.item(i)).getAttribute("set_id").equals(String.valueOf(id)))
                return (Element)setList.item(i);
        }
        return null;
    }
    
    public Element getAlgorithmElement(NodeList setList, int id) {
        Element currElement;
        for(int i = 0; i < setList.getLength(); i++) {
            if(((Element)setList.item(i)).getAttribute("alg_id").equals(String.valueOf(id)))
                return (Element)setList.item(i);
        }
        return null;
    }
    
    
    public int [] getAlgorithmSetList() {
        return null;
    }
    
    public AlgorithmNode [] getAlgorithmSet(int setID) {
        return null;
    }
    
    public DataNode getDataNode(int data_ref) {
        return null;
    }
    
    public AlgorithmSet [] getAlgorithmSets() {
/*
        AlgorithmSet set = new AlgorithmSet();
        DataNode root = (DataNode)(this.getModel().getRoot());
        set.setDataNode(root);
        int childCount = root.getChildCount();
        for(int i = 0; i < childCount; i++) {
            set.addAlgorithmNode((AlgorithmNode)(root.getChildAt(i)));
        }
 
 
 
        set.setExperiment(experiment);
 
        AlgorithmSet [] sets = new AlgorithmSet[1];
        sets[0] = set;
 */
        AlgorithmSet set;
        DataNode root = (DataNode)(this.getModel().getRoot());
        Experiment experiment = manager.getCurrentExperiment();
        
        //Algorithm Set collection
        Vector algSets = new Vector();
        Enumeration enum = root.breadthFirstEnumeration();
        ScriptNode node, childNode;
        while(enum.hasMoreElements()) {
            node = (ScriptNode)enum.nextElement();
            
            //if it's a data node with children (algs) make an alg set
            if(node instanceof DataNode && !node.isLeaf()) {
                set = new AlgorithmSet();
                if(node.getParent() == null)
                    set.setExperiment(experiment);
                set.setDataNode((DataNode)node);
                for(int i = 0; i < node.getChildCount(); i++) {
                    childNode = (ScriptNode)node.getChildAt(i);
                    if(childNode instanceof AlgorithmNode) {
                        set.addAlgorithmNode((AlgorithmNode)childNode);
                    }
                }
                algSets.add(set);
            }
        }
        
        AlgorithmSet [] sets = new AlgorithmSet[algSets.size()];
        for(int i = 0; i < sets.length ; i++) {
            sets[i] = ((AlgorithmSet)(algSets.elementAt(i)));
            
        }
        
        return sets;
    }
    
    
    public AlgorithmData constructAlgorithmData(Element algElement) {
        AlgorithmData data = new AlgorithmData();
        
        // parameter list
        NodeList list = algElement.getElementsByTagName("plist");
        if(list != null && list.getLength() > 0)
            appendParameters(data, (Element)list.item(0));
        
        // matrices and arrays
        list = algElement.getElementsByTagName("mlist");
        if(list != null && list.getLength() > 0)
            appendMatrices(data, (Element)list.item(0));
        
        return data;
    }
    
    public void appendParameters(AlgorithmData data, Element plist) {
        NodeList parameters = plist.getElementsByTagName("param");
        Element currParam;
        String key;
        String value;
        
        for(int i = 0; i < parameters.getLength(); i++) {
            currParam = (Element)parameters.item(i);
            key = currParam.getAttribute("key");
            value = currParam.getAttribute("value");
            System.out.println("key = "+key+" value = "+value);
            data.addParam(key, value);
        }
    }
    
    public void appendMatrices(AlgorithmData data, Element mlist) {
        NodeList matrices = mlist.getElementsByTagName("matrix");
        
        for(int i = 0; i < matrices.getLength(); i++) {
            addMatrix(data, (Element)matrices.item(i));
        }
    }
    
    public void addMatrix(AlgorithmData data, Element matrixElement) {
        String name = matrixElement.getAttribute("name");
        String type = matrixElement.getAttribute("type");
        String row_dim = matrixElement.getAttribute("row_dim");
        String col_dim = matrixElement.getAttribute("col_dim");
        int row_dim_int, col_dim_int;
        try {
            row_dim_int = Integer.parseInt(row_dim);
            col_dim_int = Integer.parseInt(col_dim);
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return;
        }
        
        NodeList elements = matrixElement.getElementsByTagName("element");
        int n = elements.getLength();
        Element element;
        
        if(type.equalsIgnoreCase("int-array")) {
            
            int [] values = new int [n];
            for(int i = 0; i < n ; i++) {
                element = (Element)elements.item(i);
                values[Integer.parseInt(element.getAttribute("row"))] = Integer.parseInt(element.getAttribute("value"));
            }
            
            data.addIntArray(name, values);
            
        } else if(type.equalsIgnoreCase("FloatMatrix")) {
            FloatMatrix fm = new FloatMatrix(row_dim_int, col_dim_int);
            float value;
            int x, y;
            if(n == row_dim_int*col_dim_int) {
                for(int i = 0; i < n; i++) {
                    element = (Element)elements.item(i);
                    x = Integer.parseInt(element.getAttribute("row"));
                    y = Integer.parseInt(element.getAttribute("col"));
                    value = Float.parseFloat(element.getAttribute("value"));
                    fm.set(x, y, value);
                }
                data.addMatrix(name, fm);
            }
        } else if(type.equalsIgnoreCase("String-array")) {
            String [] values = new String[n];
            for(int i = 0; i < n ; i++) {
                element = (Element)elements.item(i);
                values[Integer.parseInt(element.getAttribute("row"))] = element.getAttribute("value");
            }
            
            data.addStringArray(name, values);
        }
        
    }
    
    public void appendOutputNodes(AlgorithmNode node, Element algElement) {
        
        NodeList list = algElement.getElementsByTagName("output_data");
        
        if(list == null || list.getLength() < 1)
            return;
        
        Element outputElement = (Element)list.item(0);
        String output_class = outputElement.getAttribute("output_class");
        list = outputElement.getElementsByTagName("data_node");
        
        DataNode dataNode;
        int dataID;
        String dataType;
        String name;
        
        for(int i = 0; i < list.getLength(); i++) {
            System.out.println("put data node into hash");
            outputElement = (Element)list.item(i);
            dataID = Integer.parseInt(outputElement.getAttribute("data_node_id"));
            // dataType = outputElement.getAttribute("output_data_")
            dataNode = new DataNode(dataID, outputElement.getAttribute("name"), output_class);
            dataNodeHash.put(String.valueOf(dataID), dataNode);
            node.add(dataNode);
        }
        
    }
    
    
    
    
    
    
    /*****************
     *
     * Methods for script construction
     *
     */
    public boolean setDataRoot(DataNode node) {
        DefaultTreeModel model = (DefaultTreeModel)getModel();
        model.setRoot(node);
        return true;
    }
    
    public boolean addDataNode(DataNode node) {
        return true;
    }
    
    public boolean addAlgorithmNode(AlgorithmNode node, int dataRef) {
        return true;
    }
    
    public boolean addAlgorithmSet(AlgorithmNode [] nodes, int dataRef) {
        return true;
    }
    
    public ScriptNode getSelectedNode() {
        TreePath path =  this.getSelectionPath();
        if(path == null)
            return null;
        ScriptNode node = (ScriptNode)path.getLastPathComponent();
        return node;
    }
    
    public void scrollAllToVisible() {
        DefaultTreeModel model = (DefaultTreeModel)this.getModel();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)model.getRoot();
        Enumeration enum = node.depthFirstEnumeration();
        TreeNode [] visPath;
        TreePath path;
        while(enum.hasMoreElements()) {
            node = (DefaultMutableTreeNode)enum.nextElement();
            System.out.println("Element");
            if(node.isLeaf()) {
                System.out.println("leaf element");
                visPath = node.getPath();
                System.out.println("path length= "+visPath.length);
                path = new TreePath(visPath);
                //this.expandPath(path);
                //this.fireTreeExpanded(path);
                this.scrollPathToVisible(path);
            }
        }
    }
    
    public void addNewAlgorithmToDataNode(DataNode parentNode) {
        String outputClass = parentNode.getDataOutputClass();
        AlgorithmData data = manager.getAlgorithm(outputClass);
        
        if(data != null) {
            System.out.println("In Script tree have new alg data");
            //Builder bob = new Builder(parentNode, data);
            //Thread thread = new Thread(bob);
            //thread.start();
            
            if(document.appendAlgorithm(data, parentNode.getID())) {
                System.out.println("addnew appended alg");
                //  System.out.println("alg index = "+data.getParams().getString("algorithm-action-index"));;
                try {
                    updateTree();
                    
                    //  try{
                    //   java.lang.Thread.sleep(1000);
                    //  } catch (Exception e) { }
                    // childNode.setSelected(true);
                    //       DefaultMutableTreeNode n = (DefaultMutableTreeNode)childNode;
                    //     n.setSelected();
                    
                    // this.clearSelection();
                /*ScriptNode childNode = (ScriptNode)parentNode.getLastChild();
                TreePath path = new TreePath(((DefaultTreeModel)this.getModel()).getPathToRoot(childNode));
                System.out.println("tree path ="+path.toString());
                this.setSelectionPath(path);
                this.getSelectionModel().setSelectionPath(path);
                 */
                    scrollAllToVisible();
                    
                } catch (Exception e) {e.printStackTrace(); }
            } else {
                System.out.println("doc base didn't append");
            }
            
            
            
        }
        //Need to id data node, (id alg set), pass data to doc for append
        
    }
    
    /**
     * The class to allow run loading process in a separate thread.
     */
    private class Builder implements Runnable {
        DataNode parentNode;
        AlgorithmData data;
        public Builder(DataNode p, AlgorithmData d) {
            parentNode = p;
            data = d;
        }
        
        public void run() {
            if(document.appendAlgorithm(data, parentNode.getID())) {
                System.out.println("static addnew appended alg");
                try {
                    updateTree();
                    //  try{
                    //   java.lang.Thread.sleep(1000);
                    //  } catch (Exception e) { }
                    // childNode.setSelected(true);
                    //       DefaultMutableTreeNode n = (DefaultMutableTreeNode)childNode;
                    //     n.setSelected();
                    
                    //   clearSelection();
                    //  ScriptNode childNode = (ScriptNode)parentNode.getLastChild();
                    // TreePath path = new TreePath(((DefaultTreeModel)getModel()).getPathToRoot(parentNode));
                    //  System.out.println("tree path ="+path.toString());
                    // setSelectionPath(path);
                    // getSelectionModel().setSelectionPath(path);
                    scrollAllToVisible();
                    repaint();
                } catch (Exception e) {e.printStackTrace(); }
            } else {
                System.out.println("doc base didn't append");
            }
        }
    }
    
    public void updateTree() {
        ((DefaultTreeModel)this.getModel()).setRoot(null);
        this.constructTree(document.getDocument());
        
        // resetWidth = true;
        
        this.scrollAllToVisible();
    }
    
    public void replaceAlgorithm(AlgorithmNode node) {
        DataNode parentNode = (DataNode)node.getParent();
        AlgorithmData data;
        
        if(parentNode != null) {
            String outputClass = parentNode.getDataOutputClass();
            data = manager.getAlgorithm(outputClass);
        }
        
        //Need to id data node, (id alg set),
        
        //pass data to doc for append
        //remove node from tree and dom tree
        
        //put in the new one??
        
    }
    
    public void removeAlgorithm(AlgorithmNode node) {
        this.document.removeAlgorithm(node);
    }
    
    /*
    public int getMaxNodeWidth() {
        Enumeration enum = ((DefaultMutableTreeNode)((this.getModel()).getRoot())).breadthFirstEnumeration();
        int maxWidth = 0;
        TreeCellRenderer rend = super.getCellRenderer();
        Component comp;
        while (enum.hasMoreElements()) {
            Object obj = enum.nextElement();
            System.out.println("Node class = "+obj.getClass().getName());
            comp = rend.getTreeCellRendererComponent(this, obj, true, true, true, 0, false);
            maxWidth = Math.max(maxWidth, ((javax.swing.JPanel)comp).getComponent(0).getWidth());
            System.out.println("maxWidth = " + ((javax.swing.JPanel)comp).getComponent(0).getWidth());
            //System.out.println("Render comp count = "+ ((javax.swing.JPanel)(comp)).getComponent().get);
        //    javax.swing.JFrame frame = new javax.swing.JFrame();
         //   frame.getContentPane().add(comp);
          //  frame.setVisible(true);
        }
        return maxWidth;
    }
     
    public void paint(java.awt.Graphics g) {
        super.paint(g);
     
        if(resetWidth) {
                  int colWidth = getMaxNodeWidth();
        if(colWidth > 0) {
        BasicTreeUI basicTreeUI = (BasicTreeUI) getUI();
        basicTreeUI.setRightChildIndent(colWidth + 20);
        System.out.println("set indent");
        }
            resetWidth = false;
        }
     
    }
     **/
    
    
}
