/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: UMatrixColorViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-02-23 20:59:54 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.som;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.impl.util.Hexagon;
import org.tigr.util.FloatMatrix;

public class UMatrixColorViewer extends JPanel implements IViewer, java.io.Serializable {
    public static final long serialVersionUID = 202016030001L;
     
    private int[][] clusters;
    private FloatMatrix u_matrix;
    private int dim_x, dim_y;
    private String topology;
    
    private boolean isAntiAliasing = true;
    
    /**
     * Constructs a <code>UMatrixColorViewer</code> with specified parameters.
     */
    public UMatrixColorViewer(int[][] clusters, FloatMatrix u_matrix, int dim_x, int dim_y, String topology) {
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
	GradientPaint gp = new GradientPaint(0, 0, Color.black, width, 0, Color.white);
	g.setPaint(gp);
	g.fillRect(0, 0, width, 20);
	FontMetrics metrics = g.getFontMetrics();
	g.setColor(Color.black);
	g.drawRect(0, 0, width, 20);
	g.drawString("great distance", 5, 35);
	g.drawString("small distance", width-metrics.stringWidth("small distance") -5, 35);
	g.setColor(Color.lightGray);
	int value;
	float stepX;
	float stepY;
	if (this.topology.equals("hexagonal")) {
	    stepX = (float)width/((float)dim_x+0.5f);
	    stepY = (float)height/((float)dim_y+0.5f);
	    gp = new GradientPaint(0, 0, new Color(0,0,128), (int)Math.round(stepX), 0, new Color(0,128,255));
	    Hexagon hexagon = new Hexagon((int)Math.round(stepX), 270);
	    hexagon.translate(0, 40);
	    for (int y=0; y<dim_y; y++) {
		if (y%2 == 0) {
		    hexagon.translate(-hexagon.getA(), 0);
		} else {
		    hexagon.translate(0, 0);
		}
		for (int x=0; x<dim_x; x++) {
		    hexagon.translate((int)Math.round(stepX), 0);
		    if (this.clusters[x*dim_y+y].length == 0) {
			gp = new GradientPaint(hexagon.xpoints[5], 0, new Color(0,0,128), hexagon.xpoints[1], 0, new Color(0,128,255));
			g.setPaint(gp);
			g.fillPolygon(hexagon);
		    } else {
			value = 255-(int)Math.round(this.u_matrix.get(x, y)*255);
			g.setColor(new Color(value, value, value));
			g.fillPolygon(hexagon);
		    }
		    g.setColor(Color.lightGray);
		    g.drawPolygon(hexagon);
		}
		if (y%2 == 0) {
		    hexagon.translate(-(int)Math.round(stepX)*dim_x+hexagon.getA(), hexagon.getB()+hexagon.getC());
		} else {
		    hexagon.translate(-(int)Math.round(stepX)*dim_x, hexagon.getB()+hexagon.getC());
		}
	    }
	} else {
	    stepX = (float)width/(float)dim_x;
	    stepY = ((float)height-40f)/(float)dim_y;
	    for (int y=0; y<dim_y; y++) {
		for (int x=0; x<dim_x; x++) {
		    if (this.clusters[x*dim_y+y].length == 0) {
			gp = new GradientPaint((int)Math.round((x+1)*stepX), 0, new Color(0,0,128), (int)Math.round(x*stepX), 0, new Color(0,128,255));
			g.setPaint(gp);
		    } else {
			value = 255-(int)Math.round(this.u_matrix.get(x, y)*255);
			g.setColor(new Color(value, value, value));
		    }
		    g.drawRect((int)Math.round(x*stepX),(int)Math.round(y*stepY)+40, (int)stepX, (int)stepY);
		    g.fillRect((int)Math.round(x*stepX),(int)Math.round(y*stepY)+40, (int)stepX, (int)stepY);
		}
	    }
	    g.setColor(Color.lightGray);
	    for (int x=1; x<dim_x; x++) {
		g.drawLine((int)Math.round(x*stepX), 40, (int)Math.round(x*stepX), height+40);
	    }
	    for (int y=0; y<dim_y; y++) {
		g.drawLine(0, (int)Math.round(y*stepY)+40, width, (int)Math.round(y*stepY)+40);
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
	    int height = dim_y*(hexagon.getB()+hexagon.getC())+hexagon.getB()+40;
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
    
    /** Returns the viewer's clusters or null
     */
    public int[][] getClusters() {
        return null;
    }    
    
    /**  Returns the viewer's experiment or null
     */
    public Experiment getExperiment() {
        return null;
    }
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return -1;
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

