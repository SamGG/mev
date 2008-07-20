/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: PlotVectorViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-03-24 15:51:05 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.pca;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.Expression;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;
import org.tigr.util.FloatMatrix;

public class PlotVectorViewer extends ViewerAdapter implements java.io.Serializable {
    
    private JComponent content;
    private int current = -1;
    FloatMatrix T;
    
    /**
     * Constructs a <code>PlotVectorViewer</code> for specified T-matrix.
     */
    public PlotVectorViewer(FloatMatrix T) {
	content = createContent(T);
    	this.T = T;
    }
    /**
     * @inheritDoc
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{T});
    }
    /**
     * Returns the viewer content.
     */
    public JComponent getContentComponent() {
	return content;
    }
    
    /**
     * Updates the viewer for a selected framework node.
     */
    public void onSelected(IFramework framework) {
	Object userObject = framework.getUserObject();
	if (userObject instanceof Integer) {
	    current = ((Integer)userObject).intValue();
	    return;
	}
	current = -1;
    }
    
    /**
     * Creates the viewer content.
     */
    private JComponent createContent(FloatMatrix T) {
	return new Plot(T);
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
	private FloatMatrix T;
	private Font font = new Font("monospaced", Font.BOLD, 10);
	
	/**
	 * Constructs a <code>Plot</code> for specified T-matrix.
	 */
	public Plot(FloatMatrix T) {
	    setBackground(Color.white);
	    setFont(font);
	    this.T = T;
	}
	
	/**
	 * Pains the content into specified graphics.
	 */
	public void paint(Graphics g) {
	    super.paint(g);
	    final int rows = T.getRowDimension();
	    if (rows < 1) {
		return;
	    }
	    int plotWidth  = getWidth()  - 80;
	    int plotHeight = getHeight() - 80;
	    if (current == -1 || plotWidth < 5 || plotHeight < 5) {
		return;
	    }
	    double stepY = 1;
	    double factor = plotHeight;
	    double stepX = plotWidth/(rows-1.0);
	    double maxValue = T.get(0, current);
	    if (Double.isNaN(maxValue)) {
		return;
	    }
	    for (int i=0; i<rows; i++) {
		if (Math.abs(T.get(i, current)) > maxValue) {
		    maxValue = Math.abs(T.get(i, current));
		}
	    }
	    double scale = 1.0;
	    int counter=1;
	    int steps=1;
	    if (maxValue>=1) {
		while (maxValue>=10) {
		    maxValue/=10.0;
		    counter*=10;
		}
		steps=((int)Math.round(maxValue+0.5));
		stepY = plotHeight/steps;
		factor /= (scale*2.0);
	    } else {
		while (maxValue <= 1) {
		    maxValue *= 10.0;
		    counter *= 10.0;
		}
		scale = (int)(Math.round(maxValue+0.5));
		steps = ((int)Math.round(maxValue+0.5));
		stepY = plotHeight/(steps*2);
	    }
	    factor = factor/(scale*2.0)*counter;
	    int curValue;
	    int zeroValue = top + plotHeight/2;
	    g.setColor(Color.black);
	    g.drawRect(left, top, plotWidth, plotHeight);
	    g.drawLine(left, top+plotHeight/2, left+plotWidth, top+plotHeight/2);
	    int width;
	    FontMetrics metrics = g.getFontMetrics();
	    int height = metrics.getHeight();
	    for (int i=1; i<steps; i++) {
		g.drawLine(left, top+(int)Math.round(i*stepY), left+5, top+(int)Math.round(i*stepY));
	    }
	    for (int i=steps+1; i<steps*2; i++) {
		g.drawLine(left, top+(int)Math.round(i*stepY), left+5, top+(int)Math.round(i*stepY));
	    }
	    for (int i=1; i<rows; i++) {
		g.drawLine(left+(int)Math.round(i*stepX), top+plotHeight-5, left+(int)Math.round(i*stepX), top+plotHeight);
	    }
	    g.setColor(Color.magenta);
	    g.clipRect(left, top, plotWidth, plotHeight);
	    int prevValue = -(int)Math.round(T.get(0, current)*factor);
	    for (int i=1; i<rows; i++) {
		curValue = -(int)Math.round(T.get(i, current)*factor);
		g.drawLine(left+(int)Math.round((i-1)*stepX), zeroValue+prevValue, left+(int)Math.round(i*stepX), zeroValue+curValue);
		prevValue = curValue;
	    }
	    g.setColor(new Color(0,0,128));
	    for (int i=0; i<rows; i++) {
		curValue = -(int)Math.round(T.get(i, current)*factor);
		g.fillOval(left+(int)Math.round(i*stepX)-3, zeroValue+curValue-3,6,6);
	    }
	    g.setClip(0, 0, getWidth(), getHeight());
	    String str;
	    for (int i=0; i<=steps*2; i++) {
		str = String.valueOf((scale-i)/counter);
		width = metrics.stringWidth(str);
		g.drawString(str, left-10-width, top+(int)Math.round(i*stepY)+5);
	    }
	    ((Graphics2D)g).rotate(-Math.PI/2.0);
	    for (int i=0; i<rows; i++) {
		str = String.valueOf(i+1);
		width = metrics.stringWidth(str);
		g.drawString(str, -top-plotHeight-10-width, left+5+(int)Math.round(i*stepX));
	    }
	    ((Graphics2D)g).rotate(Math.PI/2.0);
	}
    }
}
