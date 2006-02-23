/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.LineMetrics;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
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
        setBorder(BorderFactory.createLineBorder(Color.black));
        displayNode = new GONode(node);
        displayNode.setRenderingHint(ITreeNodeRenderer.RENDERING_HINT_VERBOSE);
        parent = parentViewer;
        setThresholds(upper, lower);
    }
    
    public void updateSize(int x, int y) {
        setPreferredSize(new Dimension(x,y));
        setSize(x,y);
    }
    
    public void updateInfo(GONode selectedNode) {
        displayNode = selectedNode;
        displayNode.setRenderingHint(ITreeNodeRenderer.RENDERING_HINT_VERBOSE);
        updateSize( parent.getViewerWidth(), displayNode.getHeight()+10);
        repaint();
    }
    
    public void update() {
        displayNode.setRenderingHint(ITreeNodeRenderer.RENDERING_HINT_VERBOSE);
        updateSize( parent.getViewerWidth(), displayNode.getHeight()+10);
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
        
        Rectangle rect = g.getClip().getBounds();
        
        //   g.clearRect(rect.x, rect.y, rect.width, rect.height);
        // Rectangle rect = g.getClipRect();
        //  ((ITreeNodeRenderer)displayNode).renderNode(g2, rect.x+5, rect.y+5, ITreeNodeRenderer.STANDARD_NODE);
        
        //       if(displayNode.contains(rect))
        //   if(!( rect.height < this.getHeight() ))
        
        //((ITreeNodeRenderer)displayNode).renderNode(g2, rect.x+5, 5, ITreeNodeRenderer.STANDARD_NODE);
        
        ((ITreeNodeRenderer)displayNode).renderNode(g2, 5, 5, ITreeNodeRenderer.STANDARD_NODE);
        
        
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        FontMetrics fm = g.getFontMetrics();
        /*g.drawString(upperStr, rect.x+5+displayNode.getWidth()+ 20, 5+fm.getHeight());
        g.drawString(lowerStr, rect.x+5+displayNode.getWidth()+ 20, 5+2*fm.getHeight()+20);
        g.drawString(nonsigStr, rect.x+5+displayNode.getWidth()+ 20, 5+2*fm.getHeight()+20);
         
        g.drawRect( rect.x+5+displayNode.getWidth()+ 20, 5+fm.getHeight(), 30, 10);
        g.drawRect( rect.x+5+displayNode.getWidth()+ 20, fm.getHeight(), 30, 10);
         */
        
        LineMetrics lm = fm.getLineMetrics(lowerStr, g);
        
        g.drawRect( 5+displayNode.getWidth()+ 20, 5+(int)lm.getHeight()-(int)lm.getAscent(), 30, (int)lm.getAscent());
        g.setColor(Color.red);
        g.fillRect(5+displayNode.getWidth()+ 20+1, 5+(int)lm.getHeight()-(int)lm.getAscent()+1, 30-1, (int)lm.getAscent()-1);
        g.setColor(Color.black);
        g.drawString(lowerStr, 5+displayNode.getWidth()+ 55, 5+(int)lm.getHeight());
        
        lm = fm.getLineMetrics(upperStr, g);
        
        g.drawRect( 5+displayNode.getWidth()+ 20, 5+2*(int)lm.getHeight()-(int)lm.getAscent()+20, 30, (int)lm.getAscent());
        g.setColor(Color.orange);
        g.fillRect( 5+displayNode.getWidth()+ 20+1, 5+2*(int)lm.getHeight()-(int)lm.getAscent()+20+1, 30-1, (int)lm.getAscent()-1);
        g.setColor(Color.black);
        g.drawString(upperStr, 5+displayNode.getWidth()+ 55, 5+2*(int)lm.getHeight()+20);
        
        lm = fm.getLineMetrics(nonsigStr, g);
        
        g.drawRect( 5+displayNode.getWidth()+ 20, 5+3*(int)lm.getHeight()-(int)lm.getAscent()+40, 30, (int)lm.getAscent());
        g.setColor(Color.green);
        g.fillRect( 5+displayNode.getWidth()+ 20+1, 5+3*(int)lm.getHeight()-(int)lm.getAscent()+40+1, 30-1, (int)lm.getAscent()-1);
        g.setColor(Color.black);
        g.drawString(nonsigStr, 5+displayNode.getWidth()+ 55, 5+3*(int)lm.getHeight()+40);
    }
    
}
