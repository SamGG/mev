/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: HCLSupportTree.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.st;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;

import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import java.util.Arrays;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.tigr.util.FloatMatrix;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;

import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTree;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;

public class HCLSupportTree extends HCLTree {
    private Vector geneTreeSupportVector, exptTreeSupportVector;
    
    public HCLSupportTree(HCLTreeData treeData, int orientation, Vector geneTreeSupportVector, Vector exptTreeSupportVector) {
        super(treeData, orientation);
        this.geneTreeSupportVector = geneTreeSupportVector;
        this.exptTreeSupportVector = exptTreeSupportVector;
    }
    
   
    /**
     * Paints the tree into specified graphics.
     */
    public void paint(Graphics g) {
        super.paintSubTree(g);
        if (this.treeData.node_order.length < 2) {
            return;
        }
        
        for(int i = 0 ; i < terminalNodes.length; i++){
            terminalNodes[i] = false;
        }
        
        int sign = 1;
        if (this.orientation == VERTICAL) {
            ((Graphics2D)g).rotate(-Math.PI/2.0);
            sign = -1;
        }
        int max_node_height = this.pHeights[this.treeData.node_order[this.treeData.node_order.length-2]];
        int node;
        int child_1, child_2;
        int child_1_x1, child_1_x2, child_1_y;
        int child_2_x1, child_2_x2, child_2_y;
        for (int i=0; i<this.treeData.node_order.length-1; i++) {
            node = this.treeData.node_order[i];
            child_1 = this.treeData.child_1_array[node];
            child_2 = this.treeData.child_2_array[node];
            child_1_x1 = (max_node_height-this.pHeights[node])*sign;
            child_1_x2 = (max_node_height-this.pHeights[child_1])*sign;
            child_1_y  = (int)(this.positions[child_1]*this.stepSize)+this.stepSize/2;
            child_2_x1 = (max_node_height-this.pHeights[node])*sign;
            child_2_x2 = (max_node_height-this.pHeights[child_2])*sign;
            child_2_y  = (int)(this.positions[child_2]*this.stepSize)+this.stepSize/2;
            /*
            if (this.selected[node]) {
                g.setColor(selectedLineColor);
            } else {
                if (this.nodesColors[node] == null) {
                    g.setColor(lineColor);
                } else {
                    g.setColor(this.nodesColors[node]);
                }
            }
             */
            
            if ((geneTreeSupportVector != null) || (exptTreeSupportVector != null)) {
                int index = node - (this.treeData.node_order.length);
                Float supportPercentage = null;
                
                try {
                    if (orientation == HCLTree.HORIZONTAL) {
                        if (geneTreeSupportVector != null) supportPercentage = (Float) geneTreeSupportVector.get(index);
                    } else {
                        if (exptTreeSupportVector != null) supportPercentage = (Float) exptTreeSupportVector.get(index);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    
                }
                
                if (supportPercentage != null) {
                    g.setColor(getColorFromPercentage(supportPercentage.doubleValue())); //Parameter is the percentage of support for this node
                }
            }
            
            
            if(this.treeData.height[node] >= zero_threshold) {                    
                    this.terminalNodes[node] = false;
                    if(this.pHeights[child_1] == 0)
                        this.terminalNodes[child_1] = true;
                    if(this.pHeights[child_2] == 0)
                        this.terminalNodes[child_2] = true;
                } else{
                    
                    this.terminalNodes[node] = false;
                    
                     if(this.treeData.height[parentNodes[node]] >= zero_threshold){
                        drawWedge(g, node, child_1_x1+xOrigin, child_2_x1+xOrigin, child_1_y, child_2_y);
                        this.terminalNodes[node] = true;
                        this.terminalNodes[child_1] = false;
                        this.terminalNodes[child_2] = false;
                    }
                }
            
            selectedLineColor = Color.lightGray;
            
            if (this.selected[node]) {
                g.setColor(selectedLineColor);
            }
            if(this.orientation == HORIZONTAL){
                g.drawLine(child_1_x1 + xOrigin, child_1_y, child_1_x2 + xOrigin, child_1_y);
                g.drawLine(child_2_x1 + xOrigin, child_2_y, child_2_x2 + xOrigin, child_2_y);
                g.drawLine(child_1_x1 + xOrigin, child_1_y, child_2_x1 + xOrigin, child_2_y);
            }
            else{
                g.drawLine(child_1_x1, child_1_y + horizontalOffset, child_1_x2, child_1_y + horizontalOffset);
                g.drawLine(child_2_x1, child_2_y + horizontalOffset, child_2_x2, child_2_y + horizontalOffset);
                g.drawLine(child_1_x1, child_1_y + horizontalOffset, child_2_x1, child_2_y + horizontalOffset);
            }
        }
    }
    
    public static JPanel getColorLegendPanel() {
        JPanel legendPanel = new JPanel() {
            public void paintComponent(Graphics g1D) {
                
                Graphics2D g = (Graphics2D) g1D;
                super.paintComponent(g);
                
                int width = getSize().width;
                int height = getSize().height;
                int blockWidth = width;
                int blockHeight = height / 9;
                
                Font font = new Font("monospaced", Font.PLAIN, 12);
                g.setFont(font);
                FontMetrics fm = g.getFontMetrics();
                int stringHeight = fm.getHeight();
                int stringWidth = 0;
                String labelString = null;
                
                labelString = "100% Support";
                g.setColor(Color.black);
                g.fillRect(0, blockHeight * 0, blockWidth, blockHeight);
                g.setColor(Color.white);
                stringWidth = fm.stringWidth(labelString);
                g.drawString(labelString, 0 + (blockWidth / 2) - (stringWidth / 2), blockHeight * 0 + (blockHeight / 2) + (stringHeight / 2));
                
                labelString = "90-100% Support";
                g.setColor(Color.darkGray);
                g.fillRect(0, blockHeight * 1, blockWidth, blockHeight);
                g.setColor(Color.white);
                stringWidth = fm.stringWidth(labelString);
                g.drawString(labelString, 0 + (blockWidth / 2) - (stringWidth / 2), blockHeight * 1 + (blockHeight / 2) + (stringHeight / 2));
                
                labelString = "80-90% Support";
                g.setColor(Color.blue);
                g.fillRect(0, blockHeight * 2, blockWidth, blockHeight);
                g.setColor(Color.white);
                stringWidth = fm.stringWidth(labelString);
                g.drawString(labelString, 0 + (blockWidth / 2) - (stringWidth / 2), blockHeight * 2 + (blockHeight / 2) + (stringHeight / 2));
                
                labelString = "70-80% Support";
                g.setColor(Color.green);
                g.fillRect(0, blockHeight * 3, blockWidth, blockHeight);
                g.setColor(Color.white);
                stringWidth = fm.stringWidth(labelString);
                g.drawString(labelString, 0 + (blockWidth / 2) - (stringWidth / 2), blockHeight * 3 + (blockHeight / 2) + (stringHeight / 2));
                
                labelString = "60-70% Support";
                g.setColor(Color.yellow);
                g.fillRect(0, blockHeight * 4, blockWidth, blockHeight);
                g.setColor(Color.white);
                stringWidth = fm.stringWidth(labelString);
                g.drawString(labelString, 0 + (blockWidth / 2) - (stringWidth / 2), blockHeight * 4 + (blockHeight / 2) + (stringHeight / 2));
                
                labelString = "50-60% Support";
                g.setColor(Color.orange);
                g.fillRect(0, blockHeight * 5, blockWidth, blockHeight);
                g.setColor(Color.white);
                stringWidth = fm.stringWidth(labelString);
                g.drawString(labelString, 0 + (blockWidth / 2) - (stringWidth / 2), blockHeight * 5 + (blockHeight / 2) + (stringHeight / 2));
                
                labelString = "0-50% Support";
                g.setColor(Color.magenta);
                g.fillRect(0, blockHeight * 6, blockWidth, blockHeight);
                g.setColor(Color.white);
                stringWidth = fm.stringWidth(labelString);
                g.drawString(labelString, 0 + (blockWidth / 2) - (stringWidth / 2), blockHeight * 6 + (blockHeight / 2) + (stringHeight / 2));
                
                labelString = "0% Support";
                g.setColor(Color.red);
                g.fillRect(0, blockHeight * 7, blockWidth, blockHeight);
                g.setColor(Color.white);
                stringWidth = fm.stringWidth(labelString);
                g.drawString(labelString, 0 + (blockWidth / 2) - (stringWidth / 2), blockHeight * 7 + (blockHeight / 2) + (stringHeight / 2));
                
                labelString = "Unrecovered Node";
                g.setColor(Color.pink);
                g.fillRect(0, blockHeight * 8, blockWidth, blockHeight);
                g.setColor(Color.white);
                stringWidth = fm.stringWidth(labelString);
                g.drawString(labelString, 0 + (blockWidth / 2) - (stringWidth / 2), blockHeight * 8 + (blockHeight / 2) + (stringHeight / 2));
            }
        };
        
        return legendPanel;
    }
    
    public static Color getColorFromPercentage(double percent) {
        if (percent > 100) {
            return Color.gray;
        } else if (percent == 100) {
            return Color.black;
        } else if ((percent < 100) && (percent >= 90)) {
            return Color.darkGray;
        } else if ((percent < 90) && (percent >= 80)) {
            return Color.blue;
        } else if ((percent < 80) && (percent >= 70)) {
            return Color.green;
        } else if ((percent < 70) && (percent >= 60)) {
            return Color.yellow;
        } else if ((percent < 60) && (percent >= 50)) {
            return Color.orange;
        } else if ((percent < 50) && (percent > 0)) {
            return Color.magenta;
        } else if (percent == 0) {
            return Color.red;
        } else /* percent < 0 */ {
            return Color.pink;
        }
    }
    
}