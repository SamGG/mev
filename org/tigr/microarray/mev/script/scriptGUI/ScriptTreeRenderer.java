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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import org.tigr.microarray.mev.script.util.AlgorithmNode;
import org.tigr.microarray.mev.script.util.DataNode;
import org.tigr.microarray.mev.script.util.ScriptNode;
import org.tigr.microarray.mev.script.util.ScriptConstants;


/** ScriptTreeRenderer renders the <CODE>ScriptTree</CODE> algorithm and data nodes.
 * @author braisted
 */

public class ScriptTreeRenderer implements TreeCellRenderer {
    
    private ScriptNodeLabel label;
  // private JLabel label;
    
    private JPanel labelPanel;
    private DataNode algSetRoot = null;
    private boolean highlightAlgSet = false;
    
    boolean showToolTips = true;
    
    private Icon scriptAnalysisIcon = GUIFactory.getIcon("ScriptAnalysis.gif");
    private Icon scriptGeneAnalysisIcon = GUIFactory.getIcon("ScriptGeneAlgorithm.gif");
    private Icon scriptExperimentAnalysisIcon = GUIFactory.getIcon("ScriptExperimentAlgorithm.gif");
    private Icon scriptClusterSelectionAnalysisIcon = GUIFactory.getIcon("ScriptClusterSelectionAlgorithm.gif");
    private Icon scriptAdjustmentAlgIcon = GUIFactory.getIcon("adjustment_algorithm.gif");
    private Icon scriptEmptyAnalysisIcon = GUIFactory.getIcon("TreeBallLeaf.gif");
    private Icon scriptVisAlgNodeIcon = GUIFactory.getIcon("ScriptVisAlgorithmNode.gif");
    
    private Icon scriptDataNodeIcon = GUIFactory.getIcon("ScriptDataNode.gif");
    private Icon scriptPrimaryDataNodeIcon = GUIFactory.getIcon("ScriptPrimaryDataNode.gif");
    private Icon scriptMultiDataNodeIcon = GUIFactory.getIcon("ScriptMultiDataNodeShaded.gif");
    
    private Color dataNodeColor;
    private Color algNodeColor;
    
    /** Creates a new instance of ScriptTreeRenderer */
    public ScriptTreeRenderer() {
        label = new ScriptNodeLabel();
   
        dataNodeColor = new Color(209, 248, 203);
        algNodeColor = new Color(255,255,195);

        labelPanel = new JPanel(new GridBagLayout());
        labelPanel.setBackground(Color.white);
        labelPanel.add(label, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(8,0,0,0), 5, 0));
    }
    
    public void clearHighlights() {
        this.highlightAlgSet = false;
        label.setScriptNodeHighlighted(false);
    }
    
    public void highlightAlgSet(DataNode algSetRoot) {
        this.highlightAlgSet = true;
        this.algSetRoot = algSetRoot;
    }
    
    
    /** Returns the component to display for a given
     * tree node.
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
    boolean selected, boolean expanded, boolean isLeaf, int row, boolean hasFocus) {
        String text;
        
        //never show node selection if it's an alg set viewer
        if(!highlightAlgSet)
            label.setScriptNodeSelected(selected);
        else {
            label.setScriptNodeSelected(false);
            checkHighlight((ScriptNode)value);
        }
       
        if(value instanceof DataNode) {

            label.setBackgroundColor(dataNodeColor);
            label.setRounded(false);
            DataNode dataNode= (DataNode)value;
            
            text = dataNode.toString();
            
            //set icon
            if(text.indexOf("Primary") != -1)
                label.setIcon(scriptPrimaryDataNodeIcon);
            else if(text.indexOf("Multi") != -1)
                label.setIcon(scriptMultiDataNodeIcon);
            else
                label.setIcon(scriptDataNodeIcon);

            //set text
            label.setText(text);

        } else if(value instanceof AlgorithmNode){

            label.setBackgroundColor(algNodeColor);
            label.setRounded(true);
            AlgorithmNode algNode = (AlgorithmNode)value;
            
            text = algNode.toString();
            if(text != null && !text.equals("") || !text.equals(" ")) {

                //set icon
                if( algNode.getAlgorithmType().equals(ScriptConstants.ALGORITHM_TYPE_CLUSTER))
                    label.setIcon(scriptAnalysisIcon);
                else if(algNode.getAlgorithmType().equals(ScriptConstants.ALGORITHM_TYPE_CLUSTER_GENES))
                    label.setIcon(scriptGeneAnalysisIcon);
                else if(algNode.getAlgorithmType().equals(ScriptConstants.ALGORITHM_TYPE_CLUSTER_EXPERIMENTS))
                    label.setIcon(scriptExperimentAnalysisIcon);
                else if(algNode.getAlgorithmType().equals(ScriptConstants.ALGORITHM_TYPE_ADJUSTMENT))
                    label.setIcon(scriptAdjustmentAlgIcon);
                else if(algNode.getAlgorithmType().equals(ScriptConstants.ALGORITHM_TYPE_VISUALIZATION))
                    label.setIcon(scriptVisAlgNodeIcon);
                else if(algNode.getAlgorithmType().equals(ScriptConstants.ALGORITHM_TYPE_CLUSTER_SELECTION))
                    label.setIcon(scriptClusterSelectionAnalysisIcon);
                    
                //set text
                label.setText(text+" ["+algNode.getDataNodeRef()+","+algNode.getID()+"] ");
  
            } else {
                label.setText("Empty Algorithm ");
                label.setBorder(BorderFactory.createLineBorder(Color.blue, 3));
                label.setIcon(scriptEmptyAnalysisIcon);
            }
        }
       
        label.validate();        
        labelPanel.validate();

        return labelPanel;
    }
    
    private void checkHighlight(ScriptNode node) {
        ScriptNode parent = (ScriptNode)(node.getParent());
        
        // safety
        if(algSetRoot == null) {
            return;
        }
        
        // direct identity, offspring, or grandchild
        else if(node == algSetRoot || (parent != null && parent == algSetRoot) || (parent != null && parent.getParent() != null && parent.getParent() == algSetRoot)) {
            label.setScriptNodeHighlighted(true);
        }
        
        //not in highlighted alg set
        else {
            label.setScriptNodeHighlighted(false);
        }
        
    }
    
    
    /**
     */
    private class ScriptNodeLabel extends JLabel implements java.io.Serializable {
        boolean showRound;
        boolean sel;
        boolean highlighted;
        
        Color backgroundColor;
        
        public ScriptNodeLabel() {
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 8));

            backgroundColor = new Color(249,249,112);
        }
        
        public void setBackgroundColor(Color bkg) {
            backgroundColor = bkg;
        }
        
        public void setScriptNodeSelected(boolean selected) {
            this.sel = selected;
        }
        
        public void setScriptNodeHighlighted(boolean isLit) {
            this.highlighted= isLit;
            if(isLit && this.sel)
                this.sel = false;
        }
        
        public void setRounded(boolean isRounded) {
            showRound = isRounded;
        }
        
        /**
         * @param g  */
        public void paintComponent(Graphics g) {
            int width = getWidth();
            int height = getHeight();
            g.setColor(backgroundColor);
            
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            
            if(showRound) {
                if(sel) {
                    g2.fillRoundRect(2, 2, width-4, height-4, 19, 19);
                    g2.setColor(Color.blue);
                    g2.drawRoundRect(1, 1, width-3, height-3, 19, 19);
                    g2.drawRoundRect(0, 0, width-1, height-1, 20, 20);
                    g2.setColor(Color.black);
                    super.paintComponent(g);
                } else if(highlighted) {
                    g2.fillRoundRect(2, 2, width-4, height-4, 19, 19);
                    g2.setColor(new Color(176, 23, 54));
                    g2.drawRoundRect(1, 1, width-3, height-3, 19, 19);
                    g2.drawRoundRect(0, 0, width-1, height-1, 20, 20);
                    g2.setColor(Color.black);
                    super.paintComponent(g);
                } else {
                    if(!highlightAlgSet) {
                        g2.fillRoundRect(1, 1, width-2, height-2, 19,19);
                        g2.setColor(Color.black);
                        g2.drawRoundRect(0, 0, width-1, height-1, 20, 20);
                        g2.setColor(Color.black);
                        super.paintComponent(g);
                    } else {
                        Color color = g.getColor();
                        
                        Composite composite = g2.getComposite();                        
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
              
                        g2.fillRoundRect(1, 1, width-2, height-2, 19,19);
                        g2.setColor(Color.black);
                        g2.drawRoundRect(0, 0, width-1, height-1, 20, 20);
                        
                        g2.setColor(Color.black);
                        super.paintComponent(g);
                        
                        g.setColor(color);
                        g2.setComposite(composite);
                    }
                }
            } else {
                g2.fillRect( 0, 0, width, height);
                if(sel) {
                    g2.setColor(Color.blue);
                    g2.drawRect(1, 1, width-3, height-3);
                    g2.drawRect(0, 0, width-1, height-1);
                    g2.setColor(Color.black);
                    super.paintComponent(g);
                } else if(highlighted) {
                    g2.setColor(new Color(176, 23, 54));
                    g2.drawRect(1, 1, width-3, height-3);
                    g2.drawRect(0, 0, width-1, height-1);
                    g2.setColor(Color.black);
                    super.paintComponent(g);
                } else {
                    if(!highlightAlgSet) {
                        g2.setColor(Color.black);
                        g2.drawRect(0, 0, width-1, height-1);
                        g2.setColor(Color.black);
                        super.paintComponent(g);
                    } else {
                        Color color = g.getColor();
                        
                        Composite composite = g2.getComposite();
                        
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
       
                        g2.setColor(Color.black);
                        g2.drawRect(0, 0, width-1, height-1);
                        
                        g2.setColor(Color.black);
                        super.paintComponent(g);
                        
                        g.setColor(color);
                        g2.setComposite(composite);
                    }
                }
            }
        }
    }
    
}
