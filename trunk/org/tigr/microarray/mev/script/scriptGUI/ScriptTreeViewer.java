/*
 * ScriptTreeViewer.java
 *
 * Created on February 28, 2004, 4:36 PM
 */

package org.tigr.microarray.mev.script.scriptGUI;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.script.util.AlgorithmNode;
import org.tigr.microarray.mev.script.util.ScriptNode;
import org.tigr.microarray.mev.script.util.DataNode;
import org.tigr.microarray.mev.script.util.ScriptTree;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

import org.tigr.microarray.mev.script.ScriptManager;
import org.tigr.microarray.mev.script.event.ScriptDocumentEvent;
import org.tigr.microarray.mev.script.event.ScriptEventListener;
/**
 *
 * @author  braisted
 */
public class ScriptTreeViewer extends ViewerAdapter {
    
    ScriptTree scriptTree;
    
    JPopupMenu dataPopup;
    JPopupMenu algPopup;
    JPopupMenu defaultMenu;
    
    ScriptTreeListener listener;
    
    ScriptManager manager;
    
    boolean selected = false;
    /** Creates a new instance of ScriptTreeViewer */
    public ScriptTreeViewer(ScriptTree tree, ScriptManager manager) {
        scriptTree = tree; 
        this.manager = manager;
        listener = new ScriptTreeListener(); 
        scriptTree.addMouseListener(listener);
        scriptTree.getDocument().addDocumentListener(new ScriptListener());
        dataPopup = createPopupMenu(listener, "data");
        algPopup = createPopupMenu(listener, "alg");
        defaultMenu = createPopupMenu(listener, "default");
    }
    
    public void onSelected(IFramework framework) {
        selected = true;
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
            if(me.isPopupTrigger()){
                node = scriptTree.getSelectedNode();
                if(node == null) {
                    defaultMenu.show(scriptTree, me.getX(), me.getY());
                } else if(node instanceof AlgorithmNode) {
                    algPopup.show(scriptTree, me.getX(), me.getY());
                } else {
                    dataPopup.show(scriptTree, me.getX(), me.getY());
                }
            }
        }
        
        public void mouseReleased(MouseEvent me) {
            if(me.isPopupTrigger()){
                node = scriptTree.getSelectedNode();
                if(node == null)
                    return;
                if(node instanceof AlgorithmNode)
                    algPopup.show(scriptTree, me.getX(), me.getY());
                else
                    dataPopup.show(scriptTree, me.getX(), me.getY());
            }
        }
        
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            String command = actionEvent.getActionCommand();
            if(command.equals("add-new-algorithm-cmd") || command.equals("replace-algorithm-cmd")) {
                node = scriptTree.getSelectedNode();
                if(node == null){
                    return;     
                }
                if(node instanceof DataNode)
                    scriptTree.addNewAlgorithmToDataNode((DataNode)node);
                else
                    scriptTree.replaceAlgorithm((AlgorithmNode)node);
                
            
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
    
}
