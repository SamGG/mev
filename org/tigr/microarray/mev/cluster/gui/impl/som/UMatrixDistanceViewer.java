/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: UMatrixDistanceViewer.java,v $
 * $Revision: 1.3 $
 * $Date: 2004-02-05 21:11:04 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.som;

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.RenderingHints;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;

import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JComponent;

import org.tigr.util.FloatMatrix;

import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;

import org.tigr.microarray.mev.cluster.gui.impl.util.Hexagon;

public class UMatrixDistanceViewer extends JPanel implements IViewer, java.io.Serializable {
    
    private int[][] clusters;
    private FloatMatrix u_matrix;
    private int dim_x, dim_y;
    private String topology;
    
    private boolean isAntiAliasing = true;
    
    /**
     * Constructs a <code>UMatrixDistanceViewer</code> with specified parameters.
     */
    public UMatrixDistanceViewer(int[][] clusters, FloatMatrix u_matrix, int dim_x, int dim_y, String topology) {
	this.clusters = clusters;
	this.u_matrix = u_matrix;
	this.dim_x = dim_x;
	this.dim_y = dim_y;
	this.topology = topology;
	
	setBackground(Color.white);
	setFont(new Font("monospaced", Font.PLAIN, 10));
	addComponentListener(new Listener());
    }
    
    /**
     * Sets the anti aliasing attribute.
     */
    public void setAntialiasing(boolean value) {
	this.isAntiAliasing = value;
    }
    
    /**
     * Paints the component into specified graphics.
     */
    public void paint(Graphics g1) {
	super.paint(g1);
	Graphics2D g = (Graphics2D)g1;
	if (isAntiAliasing) {
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
	    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}
	final int width = getWidth();
	final int height = getHeight();
	float stepX;
	float stepY;
	int xValue, yValue;
	Hexagon hexagon;
	g.setColor(Color.lightGray);
	if (this.topology.equals("hexagonal")) {
	    stepX = width/(dim_x+0.5f);
	    stepY = height/(dim_y+0.5f);
	    hexagon = new Hexagon((int)Math.round(stepX), 270);
	    hexagon.translate(0, 0);
	    for (int y=0; y<dim_y; y++) {
		if (y%2 == 0) {
		    hexagon.translate(-hexagon.getA(), 0);
		} else {
		    hexagon.translate(0, 0);
		}
		for (int x=0; x<dim_x; x++) {
		    hexagon.translate((int)Math.round(stepX), 0);
		    if (this.clusters[x*dim_y+y].length == 0) {
			g.setColor(new Color(230,230,230));
			g.fillPolygon(hexagon);
			g.setColor(Color.lightGray);
		    }
		    g.drawPolygon(hexagon);
		}
		if (y%2 == 0) {
		    hexagon.translate(-(int)Math.round(stepX)*dim_x+hexagon.getA(), hexagon.getB()+hexagon.getC());
		} else {
		    hexagon.translate(-(int)Math.round(stepX)*dim_x, hexagon.getB()+hexagon.getC());
		}
	    }
	    Hexagon aHexagon;
	    g.setColor(Color.black);
	    for (int y=0; y<dim_y; y++) {
		for (int x=0; x<dim_x; x++) {
		    xValue = (int)(stepX*this.u_matrix.get(x, y));
		    aHexagon = new Hexagon(xValue, 270);
		    aHexagon.translate(0, 0);
		    aHexagon.translate((int)Math.round(stepX)*x, (int)Math.round((hexagon.getB()+hexagon.getC())*y+(hexagon.getHeight()-aHexagon.getHeight())/2));
		    if ((y%2)==0) {
			aHexagon.translate(hexagon.getA(), 0);
		    } else {
			aHexagon.translate((int)stepX, 0);
		    }
		    g.drawPolygon(aHexagon);
		}
	    }
	} else {
	    stepX = (float)width/(float)dim_x;
	    stepY = (float)height/(float)dim_y;
	    g.setColor(new Color(230,230,230));
	    for (int y=0; y<dim_y; y++) {
		for (int x=0; x<dim_x; x++) {
		    if (this.clusters[x*dim_y+y].length == 0) {
			g.fillRect((int)Math.round(x*stepX)+1, (int)Math.round(y*stepY)+1, (int)Math.round(stepX)-1, (int)Math.round(stepY)-1);
		    }
		}
	    }
	    g.setColor(Color.lightGray);
	    for (int i=0; i<dim_x; i++) {
		g.drawLine((int)Math.round(i*stepX), 0,(int)Math.round(i*stepX), height);
	    }
	    for (int i=0; i<dim_y; i++) {
		g.drawLine(0, (int)Math.round(i*stepY), width, (int)Math.round(i*stepY));
	    }
	    for (int y=0; y<dim_y; y++) {
		for (int x=0; x<dim_x; x++) {
		    xValue=(int)Math.round(stepX*this.u_matrix.get(x, y));
		    yValue=(int)Math.round(stepY*this.u_matrix.get(x, y));
		    g.setColor(Color.black);
		    g.drawRect((int)Math.round(x*stepX+(stepX-xValue)/2), (int)Math.round(y*stepY+((stepY-yValue)/2)), (int)xValue, (int)yValue);
		}
	    }
	}
    }
    
    /**
     * @return null.
     */
    public JComponent getHeaderComponent() {
	return null;
    }
    
    /**
     * Returns this component.
     */
    public JComponent getContentComponent() {
	return this;
    }
    
    /**
     * @return null.
     */
    public BufferedImage getImage() {
	return null;
    }
    
    /**
     * Updates anti aliasing attributes when the viewer is selected.
     */
    public void onSelected(IFramework framework) {
	setAntialiasing(framework.getDisplayMenu().isAntiAliasing());
    }
    
    public void onDataChanged(IData data) {}
    
    /**
     * Updates anti aliasing attributes when the display menu is changed.
     */
    public void onMenuChanged(IDisplayMenu menu) {
	setAntialiasing(menu.isAntiAliasing());
    }
    
    public void onDeselected() {}
    public void onClosed() {}
    
    /**
     * Updates the viewer sizes.
     */
    private void updateSize() {
	if (this.topology.equals("hexagonal")) {
	    float stepX = (float)getWidth()/((float)dim_x+0.5f);
	    Hexagon hexagon = new Hexagon((int)Math.round(stepX), 270);
	    int height = dim_y*(hexagon.getB()+hexagon.getC())+hexagon.getB();
	    setPreferredSize(new Dimension(100, height));
	} else {
	    setPreferredSize(new Dimension(100, 100));
	}
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }
    
    /** Returns the corner component corresponding to the indicated corner,
     * posibly null
     */
    public JComponent getCornerComponent(int cornerIndex) {
        return null;
    }
    
    /**
     * The class to listen to the component events.
     */
    private class Listener extends ComponentAdapter {
	
	private boolean isRevalidate = true;
	
	public void componentResized(ComponentEvent e) {
	    if (!this.isRevalidate) {
		this.isRevalidate = true;
		return;
	    }
	    // reset flag to ignore the next event
	    this.isRevalidate = false;
	    updateSize();
	    revalidate();
	}
    }
}
