/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: NMFPlotViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-03-24 15:51:05 $
 * $Author: dschlauch $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.nmf;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.Expression;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;
import org.tigr.util.FloatMatrix;

public class NMFPlotViewer extends ViewerAdapter implements java.io.Serializable {
    
    private JComponent content;
    FloatMatrix S;
    /**
     * Constructs a <code>PlotViewer</code> for specified S-matrix.
     */
    public NMFPlotViewer(FloatMatrix S) {
	content = createContent(S);
    	this.S = S;
    }
    /**
     * @inheritDoc
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{S});
    }
    
    /**
     * Returns the viewer content.
     */
    public JComponent getContentComponent() {
	return content;
    }
    
    /**
     * Creates the viewer content.
     */
    private JComponent createContent(FloatMatrix S) {
	return new Plot(S);
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }
    
    /**
     * The class to draw the viewer content.
     */
    private class Plot extends JPanel {
	
	private static final int left = 40;
	private static final int top  = 40;
	private FloatMatrix S;
	private Font font = new Font("monospaced", Font.BOLD, 10);
	
	/**
	 * Constructs a <code>Plot</code> with specified S-matrix.
	 */
	public Plot(FloatMatrix S) {
	    setBackground(Color.white);
	    setFont(font);
	    this.S = S;
	}
	
	/**
	 * Paints the viewer content into specified graphics.
	 */
	public void paint(Graphics g) {
	    super.paint(g);
	    // drawing rectangle
	    int plotWidth  = getWidth()  - 80;
	    int plotHeight = getHeight() - 80;
	    if (plotWidth < 5 || plotHeight < 5) {
		return;
	    }
	    g.setColor(Color.black);
	    g.drawRect(left, top, plotWidth, plotHeight);
	    
	    double maxValue = S.get(0,0);
	    if (Double.isNaN(maxValue)) {
		return;
	    }
	    int counter = 1;
	    while (maxValue >= 10) {
		maxValue /= 10.0;
		counter *= 10;
	    }
	    int scale = ((int)Math.round(maxValue+0.5))*counter;
	    int steps = ((int)Math.round(maxValue+0.5));
	    // drawing left marks
	    double stepY = plotHeight/(double)(steps);
	    for (int i=1; i<steps; i++) {
		g.drawLine(left, top + (int)Math.round(i*stepY), left+5, top+(int)Math.round(i*stepY));
	    }
	    // drawing right marks
	    double stepX = plotWidth/(S.getColumnDimension()-1.0);
	    for (int i=1; i<S.getColumnDimension(); i++) {
		g.drawLine(left+(int)Math.round(i*stepX), top+plotHeight-5, left+(int)Math.round(i*stepX), top+plotHeight);
	    }
	    g.setColor(Color.magenta);
	    double factor = (double)plotHeight/(double)(scale);
	    int prevValue = -(int)Math.round(S.get(0,0)*factor);
	    int curValue;
	    int zeroValue = top+plotHeight;
	    // draw chart
	    for (int i=1; i<S.getColumnDimension(); i++) {
		curValue = -(int)Math.round(S.get(i, i)*factor);
		g.drawLine(left+(int)Math.round((i-1)*stepX), zeroValue+prevValue, left+(int)Math.round(i*stepX), zeroValue+curValue);
		prevValue = curValue;
	    }
	    // draw points
	    g.setColor(new Color(0,0,128));
	    for (int i=0; i<S.getColumnDimension(); i++) {
		curValue=-(int)Math.round(S.get(i,i)*factor);
		g.fillOval(left+(int)Math.round(i*stepX)-3, zeroValue+curValue-3, 6, 6);
	    }
	    // draw labels
	    int width;
	    String str;
	    FontMetrics metrics = g.getFontMetrics();
	    for (int i=0; i <= steps; i++) {
		str = String.valueOf(scale-counter*i);
		width  = metrics.stringWidth(str);
		g.drawString(str, left-10-width, top+(int)Math.round(i*stepY)+5);
	    }
	    ((Graphics2D)g).rotate(-Math.PI/2.0);
	    for (int i=0; i<S.getColumnDimension(); i++) {
		str = String.valueOf(i+1);
		width  = metrics.stringWidth(str);
		g.drawString(str,-top-plotHeight-10-width, left+5+(int)Math.round(i*stepX));
	    }
	    ((Graphics2D)g).rotate(Math.PI/2.0);
	}
    }
}
