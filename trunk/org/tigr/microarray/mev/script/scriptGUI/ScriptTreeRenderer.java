/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * ScriptTreeRenderer.java
 *
 * Created on February 28, 2004, 4:42 PM
 */

package org.tigr.microarray.mev.script.scriptGUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Icon;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.script.util.DataNode;
import org.tigr.microarray.mev.script.util.AlgorithmNode;
import org.tigr.microarray.mev.script.util.ScriptConstants;


/**
 *
 * @author  braisted
 */

public class ScriptTreeRenderer implements TreeCellRenderer {
    
    JLabel label;
    JPanel labelPanel;
    boolean showToolTips = true;
    
    private Icon scriptAnalysisIcon = GUIFactory.getIcon("ScriptAnalysis.gif");
    private Icon scriptAdjustmentAlgIcon = GUIFactory.getIcon("adjustment_algorithm.gif");
    private Icon scriptEmptyAnalysisIcon = GUIFactory.getIcon("TreeBallLeaf.gif");
    
    private Icon scriptDataNodeIcon = GUIFactory.getIcon("ScriptDataNode.gif");
    private Icon scriptPrimaryDataNodeIcon = GUIFactory.getIcon("ScriptPrimaryDataNode.gif");
    private Icon scriptMultiDataNodeIcon = GUIFactory.getIcon("ScriptMultiDataNodeShaded.gif");
    private Color dataNodeColor;
    
    
    /** Creates a new instance of ScriptTreeRenderer */
    public ScriptTreeRenderer() {
        label = new JLabel();       
        
                label.setToolTipText("I have a tool tip");
        
        dataNodeColor = new Color(209, 248, 203);
        label.setBorder(BorderFactory.createLineBorder(Color.black));    
        label.setOpaque(true);
        //label.setMinimumSize(new Dimension(100,30));        
        labelPanel = new JPanel(new GridBagLayout());
        labelPanel.setBackground(Color.white);
        labelPanel.add(label, new GridBagConstraints(0,0,1,1,0,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(8,0,0,5), 0,0));
    }
    
    /** Returns the component to display for a given
     * tree node.
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
    boolean selected, boolean expanded, boolean isLeaf, int row, boolean hasFocus) {
        String text;
        label.setToolTipText("I have a tool tip");
        if(selected)
            label.setBorder(BorderFactory.createLineBorder(Color.blue, 2)); 
        else
            label.setBorder(BorderFactory.createLineBorder(Color.black)); 
            
        if(value instanceof DataNode) {
            label.setBackground(dataNodeColor);
            DataNode dataNode= (DataNode)value;
            text = dataNode.toString();
            label.setText(text);
            if(text.indexOf("Primary") != -1)
                label.setIcon(scriptPrimaryDataNodeIcon);
            else if(text.indexOf("Multi") != -1)
                label.setIcon(scriptMultiDataNodeIcon);
            else 
                label.setIcon(scriptDataNodeIcon);
            
            if(showToolTips)
                label.setToolTipText("Data Node: id = "+dataNode.getID());
        } else if(value instanceof AlgorithmNode){
            label.setBackground(Color.white);
            AlgorithmNode algNode = (AlgorithmNode)value;
            text = algNode.toString();
            if(text != null && !text.equals("") || !text.equals(" ")) {
                label.setText(text+" ["+algNode.getDataNodeRef()+","+algNode.getID()+"] ");
                if(algNode.getAlgorithmType().equals(ScriptConstants.ALGORITHM_TYPE_CLUSTER))
                    label.setIcon(scriptAnalysisIcon);
                else
                    label.setIcon(scriptAdjustmentAlgIcon);
                if(showToolTips)
                    label.setToolTipText("Algorithm Node: id = "+algNode.getID()+", input_data_ref = "+algNode.getDataNodeRef());
            } else {
                label.setText("Empty Algorithm ");
                label.setBorder(BorderFactory.createLineBorder(Color.blue, 3));
                label.setIcon(scriptEmptyAnalysisIcon);
                if(showToolTips)
                    label.setToolTipText("Algorithm Node: no algorithm selected");
            }
        }
        return labelPanel;
    }
    
}
