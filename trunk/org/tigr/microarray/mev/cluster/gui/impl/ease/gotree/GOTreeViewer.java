/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * GOTreeViewer.java
 *
 * Created on August 11, 2004, 10:31 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.ease.gotree;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.ktree.ITreeNode;
import org.tigr.microarray.mev.cluster.gui.helpers.ktree.ITreeNodeRenderer;
import org.tigr.microarray.mev.cluster.gui.helpers.ktree.Ktree;

/**
 *
 * @author  braisted
 */
public class GOTreeViewer extends JPanel implements IViewer, Serializable {
    public static final long serialVersionUID = 20200201010001L;
    
    private String category;
    private Ktree tree;
    private Vector nodes;
    private DefaultMutableTreeNode viewerNode;
    
    private int selectionPolarity = 0;
    private JPopupMenu popup;
    private boolean verbose = false;
    private GOTreeHeader header;
    
    private JMenu newTreeMenu;
    private JMenu launchMenu;
    
    private IFramework framework;
    
    private double upper = 0.05;
    private double lower = 0.01;
    
    private String baseFileSystem;
    
    /** Creates a new instance of GOTreeViewer */
    public GOTreeViewer() {
        
    }
    
    public GOTreeViewer(GONode root) {
        super(new GridBagLayout());
        tree = new Ktree(root);
        add(tree, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));
    }

    
    public GOTreeViewer(GONode [][] data, DefaultMutableTreeNode viewerNode, String baseFileSystem) {
        super(new GridBagLayout());
        this.baseFileSystem = baseFileSystem;
        tree = new Ktree(data);
        header = new GOTreeHeader(data[0][0], this, upper, lower);
        this.viewerNode = viewerNode;
        add(tree, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));
        nodes = new Vector();
        for(int i = 0; i < data.length; i++) {
            for(int j = 0; j < data[i].length; j++) {
                nodes.addElement(data[i][j]);
            }
        }
        Listener listener = new Listener();
        tree.addMouseListener(listener);
        this.addMouseListener(listener);
        createPopupMenu(listener);
    }

    
    public GOTreeViewer(String goCategory, String [] headerFields, String [][] data, DefaultMutableTreeNode viewerNode, String baseFileSystem) {
        super(new GridBagLayout());
        this.viewerNode = viewerNode;
        this.baseFileSystem = baseFileSystem;
        category = goCategory;
        GONode [][] nodes = constructTree(goCategory, headerFields, data);
        tree = new Ktree(nodes);
        header = new GOTreeHeader(nodes[0][0], this, upper, lower);
        add(tree, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));
        Listener listener = new Listener();
        tree.addMouseListener(listener);
        this.addMouseListener(listener);
        createPopupMenu(listener);
        setVerboseNodeStyle(false);
    }
    
    private GONode [][] constructTree(String goCategory, String [] header, String [][] data) {
        Hashtable termHash = new Hashtable(data.length);
        int goAccIndex= 0, termIndex = 0, listHitIndex = 0, popHitIndex = 0, statIndex = 0, catIndex = 0;
        String [] keys = { "Acc.", "Term", "List Hits", "Pop. Hits", "File"};
        boolean haveAcc = false;
        GONode currNode;
        //find the indices for the key fields
        int index = 0;
        for(int i = 0; i < keys.length; i++) {
            for(int j = 0; j < header.length; j++) {
                if(header[j].equals(keys[i])) {
                    index = j;
                }
            }
            if(i == 0)
                goAccIndex = index;
            else if(i == 1)
                termIndex = index;
            else if(i == 2)
                listHitIndex = index;
            else if(i == 3)
                popHitIndex = index;
            else
                catIndex = index;
        }
        
        if(goAccIndex < 4) {
            haveAcc = true;
            statIndex = 8;
        } else {
            statIndex = 7;
        }
        
        //accumulate primary nodes.
        nodes = new Vector(data.length);
        for(int i = 0; i < data.length; i++) {
            if(data[i][catIndex].indexOf(category) != -1) {   //row of interest
                GONode node = new GONode(data[i][goAccIndex], data[i][termIndex], category, Double.parseDouble(data[i][statIndex]), Integer.parseInt(data[i][listHitIndex]),
                Integer.parseInt(data[i][listHitIndex+1]), Integer.parseInt(data[i][popHitIndex]), Integer.parseInt(data[i][popHitIndex+1]), i);
                node.setRenderingHint(ITreeNodeRenderer.RENDERING_HINT_MINIMAL);
                node.setLowerThr(0.01d);
                node.setUpperThr(0.05d);
                nodes.addElement(node);
            }
        }
        
        makeAssociations(nodes);
        
        //nodes know associations and depth in tree
        //now construct the 2D GONode array structure
        
        //make a root, will need to set parameters later.
        GONode myRoot = new GONode("GO:00000001", category, category, 1.0, 100,100,100,100,-1);
        myRoot.setRenderingHint(ITreeNodeRenderer.RENDERING_HINT_MINIMAL);
        
        //?? if a node does not have a parent, should it connect to the main root ??
        
        //maybe look for nodes without parents and attach them to the root
        
        for(int i = 0; i < nodes.size(); i++){
            currNode = (GONode)(nodes.elementAt(i));
            if(!currNode.hasParents()) {
                currNode.addParent(myRoot);
                myRoot.addChild(currNode);
            }
        }
        
        //OK, everyonne is connected, hopefully, now set the levels
        nodes.addElement(myRoot);
        setLevelIndex(nodes);
        
        
        
        //get maximum depth
        
        int maxDepth = 0;
        for(int i = 0; i < nodes.size(); i++) {
            currNode = (GONode)(nodes.elementAt(i));
            maxDepth = Math.max(maxDepth, currNode.getLevel());
        }
      
        //max depth gives the height of the tree
        GONode [][] nodeData = new GONode[maxDepth+1][];
        
        for(int i = 0; i < nodeData.length; i++) {
            nodeData[i] = getLevelNodes(nodes, i);
        }

        //set real stats for the root
        
        //Set parmeters for the root
        ITreeNode [] children = myRoot.getChildren();
        
        if(children.length > 0) {
            myRoot.setListSize(((GONode)children[0]).getListSize());
            myRoot.setListHits(((GONode)children[0]).getListSize());  //root has size number of hits
            
            myRoot.setPopSize(((GONode)children[0]).getPopSize());
            myRoot.setPopHits(((GONode)children[0]).getPopSize());  //root has size number of hits
        }
        
        
        return nodeData;
    }
    
    private GONode [] getLevelNodes(Vector nodes, int level) {
        Vector levelNodes = new Vector();
        GONode node;
        for(int i = 0; i < nodes.size(); i++) {
            node = (GONode)(nodes.elementAt(i));
            if(node.getLevel() == level)
                levelNodes.addElement(node);
        }
        
        GONode [] n = new GONode[levelNodes.size()];
        
        for(int i = 0; i < n.length; i++) {
            n[i] = (GONode)(levelNodes.elementAt(i));
        }
        return n;
    }
    
    private boolean makeAssociations(Vector nodes) {
        //Get hash table of association vectors
        //key will be a term string, associations (parents) will be
        // terms in the value Vector key;
        
        Hashtable impliesTable = null;
        try {
            impliesTable= getAllAssociations();
        } catch (FileNotFoundException fnfe) {
            System.out.println("fnfe");
            fnfe.printStackTrace();
            return false;
        } catch (IOException ioe) {
            System.out.println("ioe");
            ioe.printStackTrace();
            return false;
        }
        
        if(impliesTable == null || impliesTable.size() == 0)
            return false;
        
        for(int i = 0; i < nodes.size(); i++) {
            makeAssociations(impliesTable, nodes, (GONode)(nodes.elementAt(i)));
        }
        return true;
    }
    
    
    private void makeAssociations(Hashtable impTable, Vector nodes, GONode baseNode) {
        String term = baseNode.getTerm();
        Vector termVector;
        if(!impTable.containsKey(term))
            return;
        else {
            termVector = (Vector)impTable.get(term);
            
            if(termVector == null)
                return;
            
            GONode currParent, parentNode;
            
            for(int i = 0; i < termVector.size(); i++) {
                parentNode = this.getNode(nodes, (String)(termVector.elementAt(i)));
                if(parentNode != null) {
                    //make the associations, note that !ifContains will apply on these adds
                    baseNode.addParent(parentNode);
                    parentNode.addChild(baseNode);
                    //recurse
                    makeAssociations(impTable, nodes, parentNode);
                }
            }
        }
    }
    
    public int getViewerWidth() {
        return tree.getTreePixelWidth();
    }
    
    private void setLevelIndex(Vector nodes) {
        GONode currNode;
        for(int i = 0; i < nodes.size(); i++) {
            currNode = (GONode)(nodes.elementAt(i));
            currNode.setLevel(currNode.getMaxPathLengthToRoot()-1);
        }
    }
    
    private Hashtable getAllAssociations() throws FileNotFoundException, IOException {
        //create hash table for implies using (implies_associator)
        //This will then be used to add implied categories
        int idx;
        String impliesFile, line;
        Hashtable implied_associations = new Hashtable(10000);
        
        File impliesFolder = TMEV.getFile(baseFileSystem+"/Data/Class/Implies/");
        String folderPath = impliesFolder.getPath();
        impliesFile = folderPath+"/"+category+".txt";
        
        File file = new File(impliesFile);
        if(!file.exists() || !file.isFile())  //if implies file is missing move on
            return null;
        
        BufferedReader in = new BufferedReader(new FileReader(impliesFile));
        
        while((line = in.readLine()) != null){
            idx = line.indexOf('\t');
            
            if(idx >= line.length() || idx < 1)  //must include a tab
                continue;
            
            if(!implied_associations.containsKey(line.substring(0,idx).trim())) {
                implied_associations.put(line.substring(0,idx).trim(), new Vector());
                ((Vector)(implied_associations.get(line.substring(0,idx).trim()))).addElement(line.substring(idx, line.length()).trim());
            } else {
                ((Vector)(implied_associations.get(line.substring(0,idx).trim()))).addElement(line.substring(idx, line.length()).trim());
            }
        }
        return implied_associations;
    }
    
    
    
    private GONode getNode(Vector nodes, String key) {
        Iterator iter = nodes.iterator();
        GONode currNode;
        GONode foundNode = null;
        boolean found = false;
        
        while(!found && iter.hasNext()) {
            currNode = (GONode)(iter.next());
            if(key.equals(currNode.getTerm())) {
                foundNode = currNode;
                found = true;
            }
        }
        return foundNode;
    }
    
    
    
  /*  
    public static void main(String [] args) {
        
        //        GONode node = new GONode("GO:0001234", "mitotic spindle formation, biological process", "biological process", 0.000012, 10, 12, 14, 5067);
        //          GONode node = new GONode("GO:0001234", "transmembrane receptor protein tyrosine kinase signaling pathway", "biological process", 0.000012, 10, 12, 14, 5067);
        GONode node = new GONode("GO:0001234", "G-protein signaling, coupled to IP3 second messenger (phospholipase C activating)", "biological process", 0.00124, 23, 33, 63, 25067,-1);
        GONode node1 = new GONode("GO:0001234", "transmembrane receptor protein tyrosine kinase signaling pathway", "biological process", 0.000012, 10, 12, 14, 5067, -1);
        GONode node2 = new GONode("GO:0001234", "transmembrane receptor protein tyrosine kinase signaling pathway", "biological process", 0.000012, 10, 12, 14, 5067, -1);
        GONode node3 = new GONode("GO:0001234", "transmembrane receptor protein tyrosine kinase signaling pathway", "biological process", 0.000012, 10, 12, 14, 5067, -1);
        
        GONode [][] nodes = new GONode[2][];
        GONode [] lev1 = new GONode[1];
        GONode [] lev2 = new GONode[3];
        
        lev1[0] = node;
        lev2[0] = node1;
        lev2[1] = node2;
        lev2[2] = node3;
        
        nodes[0] = lev1;
        nodes[1] = lev2;
        
        node.addChild(node1);
        node.addChild(node2);
        node.addChild(node3);
        
        node1.addParent(node);
        node2.addParent(node);
        node3.addParent(node);
      /*
       node.setRenderingHint(ITreeNodeRenderer.RENDERING_HINT_MINIMAL);
       node1.setRenderingHint(ITreeNodeRenderer.RENDERING_HINT_MINIMAL);
       node2.setRenderingHint(ITreeNodeRenderer.RENDERING_HINT_MINIMAL);
       node3.setRenderingHint(ITreeNodeRenderer.RENDERING_HINT_MINIMAL);
       
       */
    /*
        node.setRenderingHint(ITreeNodeRenderer.RENDERING_HINT_VERBOSE);
        node1.setRenderingHint(ITreeNodeRenderer.RENDERING_HINT_VERBOSE);
        node2.setRenderingHint(ITreeNodeRenderer.RENDERING_HINT_VERBOSE);
        node3.setRenderingHint(ITreeNodeRenderer.RENDERING_HINT_VERBOSE);
        
        GOTreeViewer viewer = new GOTreeViewer(nodes, new DefaultMutableTreeNode(), baseFileSystem);
        
        viewer.setVerboseNodeStyle(false);
        
        javax.swing.JFrame frame = new javax.swing.JFrame();
        frame.getContentPane().add(viewer);
        frame.setSize(200, 400);
        frame.setVisible(true);
    }
    */
    
    /** Returns the viewer's clusters or null
     */
    public int[][] getClusters() {
        return null;
    }
    
    /** Returns a component to be inserted into scroll pane view port.
     */
    public JComponent getContentComponent() {
        return this.tree;
    }
    
    /** Returns the corner component corresponding to the indicated corner,
     * posibly null
     */
    public JComponent getCornerComponent(int cornerIndex) {
        return null;
    }
    
    /**  Returns the viewer's experiment or null
     */
    public Experiment getExperiment() {
        return null;
    }
    
    /** Returns a component to be inserted into scroll pane header.
     */
    public JComponent getHeaderComponent() {
        //  if(!this.verbose)
        return header;
        //   return null;
    }
    
    /** Invoked by the framework to save or to print viewer image.
     */
    public BufferedImage getImage() {
        return null;
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }
    
    /** Invoked when the framework is going to be closed.
     */
    public void onClosed() {
    }
    
    /** Invoked by the framework when data is changed,
     * if this viewer is selected.
     * @see IData
     */
    public void onDataChanged(IData data) {
    }
    
    /** Invoked by the framework when this viewer was deselected.
     */
    public void onDeselected() {
    }
    
    /** Invoked by the framework when display menu is changed,
     * if this viewer is selected.
     * @see IDisplayMenu
     */
    public void onMenuChanged(IDisplayMenu menu) {
    }
    
    /** Invoked by the framework when this viewer is selected.
     */
    public void onSelected(IFramework framework) {
        this.framework = framework;
        header.update();
       
        if(this.viewerNode == null)
            this.viewerNode = (DefaultMutableTreeNode)(framework.getCurrentNode().getParent());
    }
    
    private void setStraitConnectorStyle(boolean isStrait) {
        tree.setStraitConnectorStyle(isStrait);
        tree.repaint();
    }
    
    private void setVerboseNodeStyle(boolean isVerbose) {
        int rendering;
        if(isVerbose) {
            rendering = ITreeNodeRenderer.RENDERING_HINT_VERBOSE;
            tree.setInterNodeHeight(60);
            tree.setInterNodeWidth(30);
        } else {
            rendering = ITreeNodeRenderer.RENDERING_HINT_MINIMAL;
            tree.setInterNodeHeight(40);
            tree.setInterNodeWidth(15);
        }
        
        verbose = isVerbose;
        
        for(int i =0; i < nodes.size(); i++) {
            ((GONode)(nodes.elementAt(i))).setRenderingHint(rendering);
        }
        
        tree.updateSize();  //update tree size
        header.update();    //THEN update header size
        tree.repaint();
    }
    
    
    private void setThresholds() {
        EaseThresholdDialog dialog = new EaseThresholdDialog((JFrame)framework.getFrame(), this.lower, this.upper);
        if(dialog.showModal() == JOptionPane.OK_OPTION) {
            
            setThresholds(dialog.getUpperThreshold(), dialog.getLowerThreshold());
            
            tree.repaint();
            header.repaint();
        }
    }
    
    
    public void setThresholds(double upper, double lower) {
        this.lower = lower;
        this.upper = upper;
        
        GONode currNode;
        for(int i = 0; i < nodes.size(); i++) {
            currNode = (GONode)(nodes.elementAt(i));
            currNode.setLowerThr(lower);
            currNode.setUpperThr(upper);
        }
        header.setThresholds(upper, lower);
    }
    
    
    private void setSelected(int x, int y) {
        if(tree.checkSelection(x, y, selectionPolarity)) {
            newTreeMenu.setEnabled(true);
            launchMenu.setEnabled(true);
            header.updateInfo(new GONode((GONode)tree.getSelectedNode()));
        } else {
            header.updateInfo((new GONode((GONode)this.tree.getRoot())));
            newTreeMenu.setEnabled(false);
            launchMenu.setEnabled(false);
        }
        tree.repaint();
    }
    
    private void launchNewGOTreeViewer() {
        Vector selectedNodes = tree.getSelectedPathNodes();
        if(selectedNodes.isEmpty())
            return;
        
        Vector nodes = new Vector();
        GONode node;
        //  int maxLevel = 0;
        int minLevel = Integer.MAX_VALUE;
        //get selected nodes, make a new set of nodes and get the max level.
        for(int i = 0 ; i < selectedNodes.size(); i++) {
            node = (GONode)(selectedNodes.elementAt(i));
            // maxLevel = Math.max(maxLevel, node.getLevel());
            minLevel = Math.min(minLevel, node.getLevel());
            nodes.add(new GONode(node));
        }
        
        int newDepth = 0;
        for(int i = 0; i < nodes.size(); i++) {
            node = (GONode)(nodes.elementAt(i));
            node.setLevel(node.getLevel()-minLevel);
            newDepth = Math.max(newDepth, node.getLevel());
            node.setRenderingHint(ITreeNodeRenderer.RENDERING_HINT_VERBOSE);
        }
        
        //take off unselected nodes from children and parents.
        pruneExtraNodes(nodes, selectedNodes, newDepth);
        
        Vector levelArrayVector = new Vector();
        
        for(int i = 0; i < newDepth+1; i++) {
            levelArrayVector.addElement(getLevelNodes(nodes, i));
        }
        
        //have nodes by level, need to compress levels
        GONode [] array;
        Vector toRemove = new Vector();
        for(int i = 0; i < levelArrayVector.size(); i++) {
            array = (GONode [])(levelArrayVector.elementAt(i));
            if( array.length < 1) {
                toRemove.add(array);
            }
        }
        
        //remove empty levels from the array
        for(int i = 0; i < toRemove.size(); i++) {
            levelArrayVector.remove(toRemove.elementAt(i));
        }
        
        //new data, and set level.
        GONode [][] data = new GONode[levelArrayVector.size()][];
        for(int i = 0; i < data.length; i++) {
            data[i] = (GONode [])(levelArrayVector.elementAt(i));
            for(int j = 0; j < data[i].length; j++) {
                data[i][j].setLevel(i);
            }
        }
        
        GOTreeViewer viewer = new GOTreeViewer(data, viewerNode, baseFileSystem);
        viewer.setThresholds(upper, lower);
        
        JFrame frame = new JFrame();
        
        JScrollPane pane = new JScrollPane(viewer.getContentComponent());
        pane.setColumnHeaderView(viewer.getHeaderComponent());
        frame.getContentPane().add(pane);
        
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        
        frame.setSize( (int)(screenDim.width/3), (int)(screenDim.height/2) );
        frame.setVisible(true);
        viewer.onSelected(framework);
    }
    
    private void pruneExtraNodes(Vector nodes, Vector selectedNodes, int depth) {
        GONode currNode, nodeToAdd;
        
        for(int i = 0; i < nodes.size(); i++) {
            currNode = (GONode)(nodes.elementAt(i));
            
            //handle parents first
            if(currNode.getLevel() == 0) {
                currNode.clearParents();
            } else {
                ITreeNode [] parents = (ITreeNode []) (currNode.getParents());
                Vector parentsToKeep = new Vector();
                for(int j = 0; j < parents.length; j++) {
                    nodeToAdd = null;
                    if(selectedNodes.contains(parents[j])) {
                        nodeToAdd = findNode(nodes, ((GONode)(parents[j])).getGOID());
                        if(nodeToAdd != null)
                            parentsToKeep.addElement(nodeToAdd);
                    }
                }
                currNode.setParents(parentsToKeep);
            }
            
            //now children
            if(currNode.getLevel() == depth) {
                currNode.clearChildren();
            } else {
                ITreeNode [] children = (ITreeNode[])(currNode.getChildren());
                Vector childrenToKeep = new Vector();
                for(int j = 0; j < children.length; j++) {
                    nodeToAdd = null;
                    if(selectedNodes.contains(children[j])) {
                        nodeToAdd = findNode(nodes, ((GONode)(children[j])).getGOID());
                        if(nodeToAdd != null)
                            childrenToKeep.addElement(nodeToAdd);
                    }
                }
                currNode.setChildren(childrenToKeep);
            }
        }
    }
    
    /** Handles opening cluster viewers.
     */
    private void onOpenViewer(String viewerType){
        
        GONode selNode = (GONode)(tree.getSelectedNode());
        
        if(selNode == null)
            return;
        
        int index = selNode.getClusterIndex();
        
        if(index == -1 || viewerNode == null)
            return;
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)viewerNode.getChildAt(1);
        if(node.getChildCount() < index)
            return;
        node = (DefaultMutableTreeNode)(node.getChildAt(index));
        
        if(viewerType.equals("expression image")){
            node = (DefaultMutableTreeNode)(node.getChildAt(0));
        } else if(viewerType.equals("centroid graph")){
            node = (DefaultMutableTreeNode)(node.getChildAt(1));
        } else if(viewerType.equals("expression graph")){
            node = (DefaultMutableTreeNode)(node.getChildAt(2));
        }
        
        if(framework != null)
            framework.setTreeNode(node);
    }
    
    private void createDockedGOTreeViewer() {
        Vector selectedNodes = tree.getSelectedPathNodes();
        if(selectedNodes.isEmpty())
            return;
        
        Vector nodes = new Vector();
        GONode node;
        //  int maxLevel = 0;
        int minLevel = Integer.MAX_VALUE;
        //get selected nodes, make a new set of nodes and get the max level.
        for(int i = 0 ; i < selectedNodes.size(); i++) {
            node = (GONode)(selectedNodes.elementAt(i));
            // maxLevel = Math.max(maxLevel, node.getLevel());
            minLevel = Math.min(minLevel, node.getLevel());
            nodes.add(new GONode(node));
        }
        
        //set levels
        int newDepth = 0;
        for(int i = 0; i < nodes.size(); i++) {
            node = (GONode)(nodes.elementAt(i));
            node.setLevel(node.getLevel()-minLevel);
            newDepth = Math.max(newDepth, node.getLevel());
            node.setRenderingHint(ITreeNodeRenderer.RENDERING_HINT_VERBOSE);
        }
        
        //take off unselected nodes from children and parents.
        pruneExtraNodes(nodes, selectedNodes, newDepth);
        
        Vector levelArrayVector = new Vector();
        
        for(int i = 0; i < newDepth+1; i++) {
            levelArrayVector.addElement(getLevelNodes(nodes, i));
        }
        
        //have nodes by level, need to compress levels
        GONode [] array;
        Vector toRemove = new Vector();
        for(int i = 0; i < levelArrayVector.size(); i++) {
            array = (GONode [])(levelArrayVector.elementAt(i));
            if( array.length < 1) {
                toRemove.add(array);
            }
        }
        
        //remove empty levels from the array
        for(int i = 0; i < toRemove.size(); i++) {
            levelArrayVector.remove(toRemove.elementAt(i));
        }
        
        //new data, and set level.
        GONode [][] data = new GONode[levelArrayVector.size()][];
        for(int i = 0; i < data.length; i++) {
            data[i] = (GONode [])(levelArrayVector.elementAt(i));
            for(int j = 0; j < data[i].length; j++) {
                data[i][j].setLevel(i);
            }
        }
        
        GOTreeViewer viewer = new GOTreeViewer(data, viewerNode, baseFileSystem);
        viewer.setThresholds(upper, lower);
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new LeafInfo("GO Subtree", viewer));
        
        framework.addNode(viewerNode, newNode);
        framework.setTreeNode(newNode);
    }
    
    
    
    private GONode findNode(Vector nodes, String goID) {
        for(int i = 0; i < nodes.size(); i++) {
            if( ((GONode)(nodes.elementAt(i))).getGOID().equals(goID))
                return (GONode)(nodes.elementAt(i));
        }
        return null;
    }
    
    private void createPopupMenu(ActionListener listener) {
        popup = new JPopupMenu();
        
        JMenuItem item;
        JCheckBoxMenuItem checkBoxItem;
        ButtonGroup bg;
        
        //node style
        JMenu menu = new JMenu("Node Style");
        
        bg = new ButtonGroup();
        checkBoxItem = new JCheckBoxMenuItem("Minimal", true);
        checkBoxItem.setActionCommand("simple-node-command");
        checkBoxItem.addActionListener(listener);
        bg.add(checkBoxItem);
        menu.add(checkBoxItem);
        
        checkBoxItem = new JCheckBoxMenuItem("Verbose");
        checkBoxItem.setActionCommand("verbose-node-command");
        checkBoxItem.addActionListener(listener);
        bg.add(checkBoxItem);
        menu.add(checkBoxItem);
        
        popup.add(menu);
        popup.addSeparator();
        
        
        //Connector style
        menu = new JMenu("Connector Style");
        bg = new ButtonGroup();
        
        checkBoxItem = new JCheckBoxMenuItem("Curved", true);
        checkBoxItem.setActionCommand("curved-connector-command");
        checkBoxItem.addActionListener(listener);
        bg.add(checkBoxItem);
        menu.add(checkBoxItem);
        
        checkBoxItem = new JCheckBoxMenuItem("Strait");
        checkBoxItem.setActionCommand("strait-connector-command");
        checkBoxItem.addActionListener(listener);
        bg.add(checkBoxItem);
        menu.add(checkBoxItem);
        
        popup.add(menu);
        popup.addSeparator();
        
        item = new JMenuItem("Set Thresholds");
        item.setActionCommand("set-thresholds-command");
        item.addActionListener(listener);
        
        popup.add(item);
        popup.addSeparator();
        
        menu = new JMenu("Selection Polarity");
        
        bg = new ButtonGroup();
        
        JCheckBoxMenuItem box = new JCheckBoxMenuItem("Select Ancestors");
        box.setActionCommand("ancestor-selection-command");
        box.addActionListener(listener);
        bg.add(box);
        menu.add(box);
        
        box = new JCheckBoxMenuItem("Select Successors");
        box.setActionCommand("successor-selection-command");
        box.addActionListener(listener);
        bg.add(box);
        menu.add(box);
        
        box = new JCheckBoxMenuItem("Bipolar Selection", true);
        box.setActionCommand("bipolar-selection-command");
        box.addActionListener(listener);
        bg.add(box);
        menu.add(box);
        
        popup.add(menu);
        popup.addSeparator();
        
        newTreeMenu = new JMenu("Create Subset Viewer");
        newTreeMenu.setEnabled(false);
        
        item = new JMenuItem("In New Window...");
        item.setActionCommand("new-subtree-command");
        item.addActionListener(listener);
        newTreeMenu.add(item);
        
        item = new JMenuItem("Docked in Result Tree...");
        item.setActionCommand("new-docked-subtree-command");
        item.addActionListener(listener);
        newTreeMenu.add(item);
        
        popup.add(newTreeMenu);
        popup.addSeparator();
        
        launchMenu = new JMenu("Open Viewer");
        launchMenu.setEnabled(false);
        
        item = new JMenuItem("Expression Image");
        item.setActionCommand("launch-expression-image-command");
        item.addActionListener(listener);
        launchMenu.add(item);
        
        item = new JMenuItem("Centroid Graph");
        item.setActionCommand("launch-centroid-graph-command");
        item.addActionListener(listener);
        launchMenu.add(item);
        
        item = new JMenuItem("Expression Graph");
        item.setActionCommand("launch-expression-graph-command");
        item.addActionListener(listener);
        launchMenu.add(item);
        
        popup.add(launchMenu);
    }
    
    private class Listener extends MouseAdapter implements ActionListener {
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            
            if(command.equals("verbose-node-command")) {
                setVerboseNodeStyle(true);
            } else if (command.equals("simple-node-command")) {
                setVerboseNodeStyle(false);
            } else if (command.equals("strait-connector-command")) {
                setStraitConnectorStyle(true);
            } else if (command.equals("curved-connector-command")) {
                setStraitConnectorStyle(false);
            } else if (command.equals("set-thresholds-command")) {
                setThresholds();
            } else if (command.equals("bipolar-selection-command")) {
                selectionPolarity = 0;
            } else if (command.equals("ancestor-selection-command")) {
                selectionPolarity = 1;
            } else if (command.equals("successor-selection-command")) {
                selectionPolarity = 2;
            } else if (command.equals("new-subtree-command")) {
                launchNewGOTreeViewer();
            } else if (command.equals("new-docked-subtree-command")) {
                createDockedGOTreeViewer();
            } else if(command.equals("launch-expression-image-command")){
                onOpenViewer("expression image");
            } else if(command.equals("launch-centroid-graph-command")){
                onOpenViewer("centroid graph");
            } else if(command.equals("launch-expression-graph-command")){
                onOpenViewer("expression graph");
            }
            
        }
        
        public void mousePressed(MouseEvent evt) {
            if(evt.isPopupTrigger()) {
                popup.show(tree, evt.getX(), evt.getY());
            } else {
                if(evt.getModifiers() == MouseEvent.BUTTON1_MASK)
                    setSelected(evt.getX(), evt.getY());
            }
        }
        
        public void mouseClicked(MouseEvent evt) {
            if(evt.isPopupTrigger()) {
                popup.show(tree, evt.getX(), evt.getY());
            } else {
                if(evt.getModifiers() == MouseEvent.BUTTON1_MASK)
                    setSelected(evt.getX(), evt.getY());
            }
        }
        
        public void mouseReleased(MouseEvent evt) {
            if(evt.isPopupTrigger()) {
                popup.show(tree, evt.getX(), evt.getY());
            } else {
                if(evt.getModifiers() == MouseEvent.BUTTON1_MASK)
                    setSelected(evt.getX(), evt.getY());
            }
        }
        
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeObject(baseFileSystem);
        oos.writeObject(category);
        oos.writeObject(tree);
        oos.writeObject(nodes);
        oos.writeInt(selectionPolarity);
        oos.writeBoolean(verbose);
        oos.writeObject(header);
        oos.writeDouble(upper);
        oos.writeDouble(lower);
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException{
        this.baseFileSystem = (String)ois.readObject();
        this.category = (String)ois.readObject();
        this.tree = (Ktree)ois.readObject();
        this.nodes = (Vector)ois.readObject();
        this.selectionPolarity = ois.readInt();
        this.verbose = ois.readBoolean();
        this.header = (GOTreeHeader)ois.readObject();
        this.upper = ois.readDouble();
        this.lower = ois.readDouble();
        
        Listener listener = new Listener();
        tree.addMouseListener(listener);
        this.addMouseListener(listener);
        createPopupMenu(listener);
    }
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return -1;
    }
    
}
