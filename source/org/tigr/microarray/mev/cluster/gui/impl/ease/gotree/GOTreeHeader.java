/*
Copyright @ 1999-2006, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * GOTreeHeader.java
 *
 * Created on August 17, 2004, 10:18 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.ease.gotree;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.LineMetrics;
import java.text.DecimalFormat;

import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.helpers.ktree.ITreeNodeRenderer;


/**
 *
 * @author  braisted
 */
public class GOTreeHeader extends JPanel {
    
    private GONode displayNode;
    private GOTreeViewer parent;
    private double lowerThr = 0.01;
    private double upperThr = 0.05;
    private String lowerStr = "0.01";
    private String upperStr = "0.05";
    private String nonsigStr = "";
    
    /** Creates a new instance of GOTreeHeader */
    public GOTreeHeader(GONode node, GOTreeViewer parentViewer, double upper, double lower) {
        super();
        super.setBackground(Color.white);
        setBackground(Color.white);
        //setBorder(BorderFactory.createLineBorder(Color.black));
        displayNode = new GONode(node);
        displayNode.setRenderingHint(ITreeNodeRenderer.RENDERING_HINT_VERBOSE);
        parent = parentViewer;
        setThresholds(upper, lower);
    }
    
    
    public void updateSize(int x, int y) {
        //set preferred size, note added extra width for possible verbose tool tip    	
        setPreferredSize(new Dimension(x+125,y));        
        setSize(x+125,y);
    }
    
   
    /* jcb 10/9/06 stopped putting the selected node in the header
    public void updateInfo(GONode selectedNode) {
        displayNode = selectedNode;
        displayNode.setRenderingHint(ITreeNodeRenderer.RENDERING_HINT_VERBOSE);
        updateSize( parent.getViewerWidth(), displayNode.getHeight()+10);
        repaint();
    }
    */
   
    
    public void update() {
        displayNode.setRenderingHint(ITreeNodeRenderer.RENDERING_HINT_VERBOSE);
        updateSize( parent.getViewerWidth(), 35);
        repaint();
    }
   
    
    public void setThresholds(double upper, double lower) {
        this.upperThr = upper;
        this.lowerThr = lower;
        
        
        DecimalFormat format;
        
        if (upperThr < 0.0001){
            format = new DecimalFormat("0.#####E00");
            upperStr = format.format(upperThr);
        } else {
            format = new DecimalFormat("0.#####");
            upperStr = format.format(upperThr);
        }
        
        //upperStr = "Upper Thr. p <= "+upperStr+ " ";
        upperStr = "p <= "+upperStr+ " ";
        
        if (lowerThr < 0.0001){
            format = new DecimalFormat("0.#####E00");
            lowerStr = format.format(lowerThr);
        } else {
            format = new DecimalFormat("0.#####");
            lowerStr = format.format(lowerThr);
        }
       // lowerStr = "Lower Thr. p <= " +lowerStr+ " ";
        lowerStr = "p <= " +lowerStr+ " ";
        
        if (upperThr < 0.0001){
            format = new DecimalFormat("0.#####E00");
            nonsigStr = format.format(upperThr);
        } else {
            format = new DecimalFormat("0.#####");
            nonsigStr = format.format(upperThr);
        }
        nonsigStr = "p > "+nonsigStr;
    }
    
    public void paint(Graphics g) {
        
        super.paint(g);
        Graphics2D g2 = (Graphics2D)g;

        // jcb 10/9/06 stop rendering node in header
        //((ITreeNodeRenderer)displayNode).renderNode(g2, 5, 5, ITreeNodeRenderer.STANDARD_NODE);
        // jcb 10/9/06 modified header rendering
        
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        FontMetrics fm = g.getFontMetrics();
        
        LineMetrics lm = fm.getLineMetrics(lowerStr, g);
        
        g.drawRect( 25, 5+(int)lm.getHeight()-(int)lm.getAscent(), 30, (int)lm.getAscent());
        g.setColor(Color.red);
        g.fillRect(26, 5+(int)lm.getHeight()-(int)lm.getAscent()+1, 30-1, (int)lm.getAscent()-1);
        g.setColor(Color.black);
        g.drawString(lowerStr, 60, 5+(int)lm.getHeight());
        
        int textWidth = g.getFontMetrics().stringWidth(lowerStr);
        int currX = 60+textWidth;
        
        lm = fm.getLineMetrics(upperStr, g);
         
        g.drawRect( currX+25, 5+(int)lm.getHeight()-(int)lm.getAscent(), 30, (int)lm.getAscent());
        g.setColor(Color.orange);
        g.fillRect( currX+26, 5+(int)lm.getHeight()-(int)lm.getAscent()+1, 30-1, (int)lm.getAscent()-1);
        g.setColor(Color.black);
        g.drawString(upperStr, currX+60, 5+(int)lm.getHeight());
        
        textWidth = g.getFontMetrics().stringWidth(upperStr);
        currX += 60+textWidth;
        
        lm = fm.getLineMetrics(nonsigStr, g);
        
        g.drawRect( currX+25, 5+(int)lm.getHeight()-(int)lm.getAscent(), 30, (int)lm.getAscent());
        g.setColor(Color.green);
        g.fillRect( currX+26, 5+(int)lm.getHeight()-(int)lm.getAscent()+1, 30-1, (int)lm.getAscent()-1);
        g.setColor(Color.black);
        g.drawString(nonsigStr, currX+60, 5+(int)lm.getHeight());
    }
    
}
