/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * ScriptTreeViewer.java
 *
 * Created on February 28, 2004, 4:36 PM
 */

package org.tigr.microarray.mev.script.scriptGUI;

import java.awt.Color;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.script.util.AlgorithmNode;
import org.tigr.microarray.mev.script.util.ScriptConstants;
import org.tigr.microarray.mev.script.util.ScriptNode;
import org.tigr.microarray.mev.script.util.DataNode;
import org.tigr.microarray.mev.script.util.ScriptTree;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

import org.tigr.microarray.mev.script.ScriptDocument;
import org.tigr.microarray.mev.script.ScriptManager;
import org.tigr.microarray.mev.script.event.ScriptDocumentEvent;
import org.tigr.microarray.mev.script.event.ScriptEventListener;
import org.tigr.microarray.mev.script.util.AlgorithmSet;


/** ScriptTreeViewer renders the script in an intuitive tree where
 * data nodes and algorithm nodes are identified.
 * @author braisted
 */
public class ScriptTreeViewer extends ViewerAdapter implements Serializable {
    public static final long serialVersionUID = 1000102010301040001L;
    
    /** Data for the tree rendering, scriptTree
     */
    ScriptTree scriptTree;
    
    JPopupMenu dataPopup;
    JPopupMenu algPopup;
    JPopupMenu defaultMenu;
    
    ScriptTreeListener listener;
    
    ScriptManager manager;
    
    private boolean isAlgSetViewer = false;
    private DataNode algSetRoot;
    
    boolean selected = false;
    /** Creates a new instance of ScriptTreeViewer
     * @param tree ScriptTree data structure
     * @param manager script manager to support mev-script interactions.
     */
    public ScriptTreeViewer(ScriptTree tree, ScriptManager manager) {
        scriptTree = tree;
        this.manager = manager;
        listener = new ScriptTreeListener();
        scriptTree.addMouseListener(listener);
        scriptTree.getDocument().addDocumentListener(new ScriptListener());
        //scriptTree.setBackground(new Color(255,255,220));
        dataPopup = createPopupMenu(listener, "data");
        algPopup = createPopupMenu(listener, "alg");
        defaultMenu = createPopupMenu(listener, "default");
        isAlgSetViewer = false;
    }
    
    
    /** Creates a new instance of ScriptTreeViewer
     * @param tree ScriptTree data structure
     * @param manager script manager to support mev-script interactions.
     */
    public ScriptTreeViewer(ScriptTree tree, ScriptManager manager, DataNode algSetRoot) {
        scriptTree = tree;
        this.manager = manager;
        //listener = new ScriptTreeListener();
        //scriptTree.addMouseListener(listener);
        //scriptTree.getDocument().addDocumentListener(new ScriptListener());
        
        this.algSetRoot = findLocalAlgSetRoot(algSetRoot.toString());
        
        if(this.algSetRoot != null){
            scriptTree.highlightAlgSet(this.algSetRoot);
            isAlgSetViewer = true;
        } else {
            isAlgSetViewer = false;
        }
        
        //dataPopup = createPopupMenu(listener, "data");
        //algPopup = createPopupMenu(listener, "alg");
        //defaultMenu = createPopupMenu(listener, "default");
        
        
    }
    
    private DataNode findLocalAlgSetRoot(String rootName) {
        AlgorithmSet [] sets = scriptTree.getAlgorithmSets();
        DataNode dataNode = null;
        for(int i = 0; i < sets.length; i++) {
            if(sets[i].getDataNode().toString().equals(rootName)) {
                dataNode = sets[i].getDataNode();
                break;
            }
        }
        return dataNode;
    }
    
    
    public void onSelected(IFramework framework) {
        selected = true;
        
        //sets it up to highlight an algorithm set
        if(isAlgSetViewer)
            scriptTree.highlightAlgSet(algSetRoot);
        else
            scriptTree.clearHighlights();
        
        scriptTree.validate();
    }
    
    public void onClosed() {
        selected = false;
    }
    
    public JComponent getContentComponent() {
        return scriptTree;
    }
    
    public JPopupMenu createPopupMenu(ScriptTreeListener listener, String type) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item;
        if(type.equals("alg")){
            //Select Algorithm
            item = new JMenuItem("Change Algorithm");
            item.setActionCommand("replace-algorithm-cmd");
            item.addActionListener(listener);
            //    menu.add(item);
            //Delete Algorithm
            item = new JMenuItem("Delete Algorithm");
            item.setActionCommand("delete-algorithm-cmd");
            item.addActionListener(listener);
            menu.add(item);
            //Modify Parameters
            item = new JMenuItem("Modify Paramaters");
            item.setActionCommand("modify-algorithm-cmd");
            item.addActionListener(listener);
            //    menu.add(item);
            
            menu.addSeparator();
            
            //view xml
            item = new JMenuItem("View XML Section");
            item.setActionCommand("view-xml-cmd");
            item.addActionListener(listener);
            menu.add(item);
            menu.addSeparator();
        } else {
            // Add algorithm
            item = new JMenuItem("Add Algorithm Node");
            item.setActionCommand("add-new-algorithm-cmd");
            item.addActionListener(listener);
            menu.add(item);
            menu.addSeparator();
        }
        
        item = new JMenuItem("Execute Script");
        item.setActionCommand("execute-script-cmd");
        item.addActionListener(listener);
        menu.add(item);
        menu.addSeparator();
        
        item = new JMenuItem("Save Script");
        item.setActionCommand("save-script-cmd");
        item.addActionListener(listener);
        menu.add(item);
        
        return menu;
    }
    
    
    public class ScriptTreeListener extends MouseAdapter implements ActionListener {
        
        ScriptNode node;
        
        public void mouseClicked(MouseEvent me) {
            if(isAlgSetViewer)
                return;
            if(me.isPopupTrigger()){
                node = scriptTree.getSelectedNode();
                if(node == null) {
                    defaultMenu.show(scriptTree, me.getX(), me.getY());
                } else if(node instanceof AlgorithmNode) {
                    algPopup.show(scriptTree, me.getX(), me.getY());
                } else {
                    ScriptNode parent = (ScriptNode)(node.getParent());
                    if(parent != null) {
                        if(((AlgorithmNode)parent).getAlgorithmType().equals(ScriptConstants.ALGORITHM_TYPE_VISUALIZATION)) {
                            dataPopup.getComponent(0).setEnabled(false);
                        } else
                            dataPopup.getComponent(0).setEnabled(true);
                    }
                    dataPopup.show(scriptTree, me.getX(), me.getY());
                }
            }
        }
        
        
        public void mouseReleased(MouseEvent me) {
            if(isAlgSetViewer)
                return;
            if(me.isPopupTrigger()){
                node = scriptTree.getSelectedNode();
                if(node == null)
                    return;
                if(node instanceof AlgorithmNode) {
                    algPopup.show(scriptTree, me.getX(), me.getY());
                }
                else {
                    ScriptNode parent = (ScriptNode)(node.getParent());
                    if(parent != null) {
                        if(((AlgorithmNode)parent).getAlgorithmType().equals(ScriptConstants.ALGORITHM_TYPE_VISUALIZATION)) {
                            dataPopup.getComponent(0).setEnabled(false);
                        } else
                            dataPopup.getComponent(0).setEnabled(true);
                    }
                    dataPopup.show(scriptTree, me.getX(), me.getY());
                }
            }            
        }

        
        public void mousePressed(MouseEvent me) {
                        if(isAlgSetViewer)
                return;
            if(me.isPopupTrigger()){
                node = scriptTree.getSelectedNode();
                if(node == null)
                    return;
                if(node instanceof AlgorithmNode) {
                    algPopup.show(scriptTree, me.getX(), me.getY());
                }
                else {
                    ScriptNode parent = (ScriptNode)(node.getParent());
                    if(parent != null) {
                        if(((AlgorithmNode)parent).getAlgorithmType().equals(ScriptConstants.ALGORITHM_TYPE_VISUALIZATION)) {
                            dataPopup.getComponent(0).setEnabled(false);
                        } else
                            dataPopup.getComponent(0).setEnabled(true);
                    }
                    dataPopup.show(scriptTree, me.getX(), me.getY());
                }
            }
        }
        
        
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            if(isAlgSetViewer)
                return;
            
            String command = actionEvent.getActionCommand();
            if(command.equals("add-new-algorithm-cmd") || command.equals("replace-algorithm-cmd")) {
                node = scriptTree.getSelectedNode();
                if(node == null){
                    return;
                }
                if(node instanceof DataNode)
                    scriptTree.addNewAlgorithmToDataNode((DataNode)node);
            } else if(command.equals("delete-algorithm-cmd")) {
                node = scriptTree.getSelectedNode();
                if(node != null) {
                    if(node instanceof AlgorithmNode)
                        scriptTree.removeAlgorithm((AlgorithmNode)node);
                }
            } else if(command.equals("modify-algorithm-cmd")) {
                
            } else if(command.equals("view-xml-cmd")) {
                node = scriptTree.getSelectedNode();
                if(node != null) {
                    manager.viewSelectedNodeXML(ScriptTreeViewer.this, node);
                }
            } else if(command.equals("save-script-cmd")) {
                manager.saveScript(scriptTree.getDocument());
            } else if(command.equals("execute-script-cmd")) {
                manager.runScript(scriptTree.getDocument());
            }
        }
    }
    
    
    public class ScriptListener implements ScriptEventListener {
        public void documentChanged(ScriptDocumentEvent event) {
            scriptTree.updateTree();
        }
    }
    
    
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException {
        oos.writeObject(manager);
        oos.writeObject(scriptTree.getDocument());
        oos.writeBoolean(isAlgSetViewer);
        if(isAlgSetViewer)
            oos.writeObject(algSetRoot);
        oos.writeBoolean(selected);
    }
    
    
    
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
        this.manager = (ScriptManager)ois.readObject();
        this.scriptTree = new ScriptTree((ScriptDocument)ois.readObject(), manager);
        this.isAlgSetViewer = ois.readBoolean();
        if(this.isAlgSetViewer) {
            this.algSetRoot = (DataNode)ois.readObject();
            this.algSetRoot = scriptTree.getDataNodeNamed(algSetRoot.toString());
            if(algSetRoot != null)
                this.scriptTree.highlightAlgSet(algSetRoot);
        }
        this.selected = ois.readBoolean();
    }
    
}
