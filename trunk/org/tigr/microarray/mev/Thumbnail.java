/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Thumbnail.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:44:16 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.BorderLayout;

import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;

public class Thumbnail extends JFrame {
    
    private Canvas canvas;
    
    /**
     * Constructs a <code>Thumbnail</code> for specified framework.
     */
    public Thumbnail(IFramework framework, WindowListener windowListener) {
	super("Thumbnail");
	addWindowListener(windowListener);
	Listener listener = new Listener();
	this.canvas = createCanvas(framework, listener);
	getContentPane().add(this.canvas, BorderLayout.CENTER);
	setSize(100, 200);
    }
    
    /**
     * Updates the canvas data.
     */
    public void onDataChanged(IData data) {
	this.canvas.onDataChanged(data);
    }
    
    /**
     * Delegates the call to its canvas.
     */
    public void onMenuChanged(IDisplayMenu menu) {
	this.canvas.onMenuChanged(menu);
    }
    
    /**
     * Creates the tumbnail canvas.
     */
    private Canvas createCanvas(IFramework framework, MouseListener listener) {
	Canvas canvas = new Canvas(framework);
	canvas.setBackground(Color.white);
	canvas.addMouseListener(listener);
	return canvas;
    }
    
    /**
     * The class to draw microarrays data.
     */
    private class Canvas extends JPanel {
	private IFramework framework;
	private IData data;
	private int elementHeight = 5;
	private float maxRatio;
	private float minRatio;
	
	/**
	 * Constructs a <code>Canvas</code> with specified reference to the framework.
	 */
	public Canvas(IFramework framework) {
	    this.framework = framework;
	    this.data = framework.getData();
	    this.maxRatio = framework.getDisplayMenu().getMaxRatioScale();
	    this.minRatio = framework.getDisplayMenu().getMinRatioScale();
	}
	
	/**
	 * Updates the canvas data.
	 */
	public void onDataChanged(IData data) {
	    this.data = data;
	    repaint();
	}
	
	/**
	 * Updates the canvas attributies.
	 */
	public void onMenuChanged(IDisplayMenu menu) {
	    this.elementHeight = menu.getElementSize().height;
	    this.maxRatio = menu.getMaxRatioScale();
	    this.minRatio = menu.getMinRatioScale();
	    repaint();
	}
	
	/**
	 * Sets the framework content position into a specified y coordinate.
	 */
	private void setContentPosition(int mouseY) {
	    framework.setContentLocation(0, (this.elementHeight*this.data.getFeaturesSize()*mouseY)/getHeight());
	}
	
	/**
	 * Paints the canvas into specified graphics.
	 */
	public void paint(Graphics g) {
	    super.paint(g);
	    final int COLUMNS = this.data.getFeaturesCount();
	    final int ROWS    = this.data.getFeaturesSize();
	    float width  = (float)getWidth()/(float)COLUMNS;
	    float height = (float)getHeight()/(float)ROWS;
	    int eHeight = (int)height > 0 ? (int)height : 1;
	    int[] indices;
	    int x, y;
	    float ratio;
	    for (int column=0; column<COLUMNS; column++) {
		indices = data.getSortedIndices(column);
		for (int row=0; row<ROWS; row++) {
		    x = Math.round(width*column);
		    y = Math.round(height*row);
		    ratio = data.getRatio(column, indices[row], IData.LOG);
		    drawElement(g, x, y, Math.round(width), eHeight, ratio);
		}
	    }
	}
	
	/**
	 * Draws an element with specified parameters.
	 */
	private void drawElement(Graphics g, int x, int y, int width, int height, float ratio) {
	    Color holdColor;
	    if (ratio > 0) {
		ratio = (float)Math.abs(ratio/this.maxRatio);
		ratio = ratio > 1f ? 1f : ratio;
		holdColor = new Color(ratio, 0, 0);
	    } else if (ratio < 0) {
		ratio = (float)Math.abs(ratio/this.minRatio);
		ratio = ratio > 1f ? 1f : ratio;
		holdColor = new Color(0, ratio, 0);
	    } else {
		//holdColor = new Color(0, 0, 0);
		holdColor = new Color(128, 128, 128);
	    }
	    g.setColor(holdColor);
	    g.fillRect(x, y, width, height);
	}
    }
    
    /**
     * Mouse events listener.
     */
    private class Listener extends MouseAdapter {
	public void mouseClicked(MouseEvent event) {
	    canvas.setContentPosition(event.getY());
	}
    }
}