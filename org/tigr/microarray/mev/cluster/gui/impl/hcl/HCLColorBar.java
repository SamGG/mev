/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: HCLColorBar.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:25 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.RenderingHints;

import java.util.ArrayList;

import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;

public class HCLColorBar extends JPanel {
    
    private static final int BAR_WIDTH = 10;
    
    private ArrayList clusters;
    
    private int featuresSize;
    private boolean isAntiAliasing = true;
    private int elementHeight = 5;
    
    /**
     * Constructs a <code>HCLColorBar</code> for specified hcl clusters.
     */
    public HCLColorBar(ArrayList clusters, int featuresSize) {
	setBackground(Color.white);
	setFont(new Font("monospaced",Font.PLAIN, 20));
	this.clusters = clusters;
	this.featuresSize = featuresSize;
    }
    
    /**
     * Paints the component into specified graphics.
     */
    public void paint(Graphics g) {
	super.paint(g);
	if (this.isAntiAliasing) {
	    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}
	HCLCluster cluster;
	int x = 10, y, size;
	final int COUNT_CLUSTERS = this.clusters.size();
	for (int i=0; i<COUNT_CLUSTERS; i++) {
	    cluster = (HCLCluster)this.clusters.get(i);
	    y    = cluster.firstElem*this.elementHeight+this.elementHeight/2;
	    size = (cluster.lastElem-cluster.firstElem)*this.elementHeight;
	    g.setColor(cluster.color);
	    g.fillRect(x, y, x+BAR_WIDTH, size);
	    if (cluster.text != null) {
		g.drawString(cluster.text, x+BAR_WIDTH+10, y+size/2+7);
	    }
	}
    }
    
    /**
     * Updates attributies when component was selected by the framework.
     */
    public void onSelected(IFramework framework) {
	IDisplayMenu menu = framework.getDisplayMenu();
	this.isAntiAliasing = menu.isAntiAliasing();
	setElementHeight(menu.getElementSize().height);
	updateSize();
    }
    
    /**
     * Updates attributies, if the framework display menu was changed.
     */
    public void onMenuChanged(IDisplayMenu menu) {
	if (this.elementHeight == menu.getElementSize().height &&
	this.isAntiAliasing == menu.isAntiAliasing()) {
	    return;
	}
	this.isAntiAliasing = menu.isAntiAliasing();
	setElementHeight(menu.getElementSize().height);
	updateSize();
    }
    
    /**
     * Updates the component when clusters were changed.
     */
    public void onClustersChanged(ArrayList clusters) {
	this.clusters = clusters;
	updateSize();
	repaint();
    }
    
    /**
     * Sets a new element height.
     */
    private void setElementHeight(int height) {
	this.elementHeight = height;
    }
    
    /**
     * Updates the component sizes.
     */
    private void updateSize() {
	Graphics2D g = (Graphics2D)getGraphics();
	int width = 10+BAR_WIDTH+10+getMaxWidth(g);
	int height = this.elementHeight*this.featuresSize+1;
	setSizes(width, height);
    }
    
    /**
     * Calculates max description width.
     */
    private int getMaxWidth(Graphics2D g) {
	if (g == null || this.clusters == null) {
	    return 0;
	}
	if (this.isAntiAliasing) {
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}
	FontMetrics fm = g.getFontMetrics();
	int max = 0;
	String str;
	final int size = this.clusters.size();
	HCLCluster cluster;
	for (int i=0; i<size; i++) {
	    cluster = (HCLCluster)this.clusters.get(i);
	    if (cluster != null) {
		str = cluster.text == null ? "" : cluster.text;
		max = Math.max(max, fm.stringWidth(str));
	    }
	}
	return max;
    }
    
    /**
     * Sets the component sizes.
     */
    private void setSizes(int width, int height) {
	setSize(width, height);
	setPreferredSize(new Dimension(width, height));        
    }
    

}
