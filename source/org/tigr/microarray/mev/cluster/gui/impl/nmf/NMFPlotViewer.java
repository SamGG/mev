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
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

public class NMFPlotViewer extends ViewerAdapter implements java.io.Serializable {
    
    private JComponent content;
    float[] data;
    String[] labels;
    /**
     * Constructs a <code>PlotViewer</code> for specified S-matrix.
     */
    public NMFPlotViewer(float[] data, String[] labels) {
    	float minValue = 1.0f;
    	float maxValue = 1.0f;
    	for (int i=0; i<data.length; i++){
    		if (data[i]<minValue)
    			minValue = data[i];
    	}
    	content = createContent(data, labels, minValue-.05f, maxValue);
    	this.data = data;
    	this.labels = labels;
    }
    /**
     * @inheritDoc
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{data});
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
    private JComponent createContent(float[] data, String[] labels, double minValue, double maxValue) {
    	return new Plot(data, labels, minValue, maxValue);
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

	    double maxValue = 1.0;
	    double minValue = 0.7;
		private static final int left = 40;
		private static final int top  = 40;
		private float[] data;
		private String[] labels;
		private Font font = new Font("monospaced", Font.BOLD, 10);
		
		/**
		 * Constructs a <code>Plot</code> with specified S-matrix.
		 */
		public Plot(float[] data, String[] labels, double minValue, double maxValue) {
		    setBackground(Color.white);
		    setFont(font);
		    this.data = data;
		    this.labels = labels;
		    this.minValue = minValue;
		    this.maxValue = maxValue;
		}
		
		/**
		 * Paints the viewer content into specified graphics.
		 */
		public void paint(Graphics g) {
		    super.paint(g);
		    // drawing rectangle
		    int plotWidth  = getWidth()  - 160;
		    int plotHeight = getHeight() - 200;
		    if (plotWidth < 5 || plotHeight < 5) 
		    	return;
		    g.setColor(Color.black);
		    g.drawRect(left, top, plotWidth, plotHeight);
		    
		    int steps = 10;
		    double stepValue = (maxValue-minValue)/steps;
		    if (Double.isNaN(maxValue)) {
			return;
		    }
		    int counter = 1;
		    while (maxValue >= 10) {
				maxValue /= 10.0;
				counter *= 10;
		    }
		    float scale = (float)(maxValue-minValue)/(float)maxValue;
		    System.out.println("scale "+scale);
		    // drawing left marks
		    double stepY = plotHeight/(double)(steps);
		    for (int i=1; i<steps; i++) {
		    	g.drawLine(left, top + (int)Math.round(i*stepY), left+5, top+(int)Math.round(i*stepY));
		    }
		    // drawing right marks
		    double stepX = plotWidth/(data.length-1.0);
		    for (int i=1; i<data.length; i++) {
		    	g.drawLine(left+(int)Math.round(i*stepX), top+plotHeight-5, left+(int)Math.round(i*stepX), top+plotHeight);
		    }
		    g.setColor(Color.magenta);
		    double factor = (double)plotHeight;///(double)(scale);
		    System.out.println("factor "+factor);
		    int prevValue = -(int)Math.round((data[0]-minValue)*factor/scale);
		    int curValue;
		    int zeroValue = top+plotHeight;
		    // draw chart
		    for (int i=1; i<data.length; i++) {
				curValue = -(int)Math.round((data[i]-minValue)*factor/scale);
				g.drawLine(left+(int)Math.round((i-1)*stepX), zeroValue+prevValue, left+(int)Math.round(i*stepX), zeroValue+curValue);
				prevValue = curValue;
		    }
		    // draw points
		    g.setColor(new Color(0,0,128));
		    for (int i=0; i<data.length; i++) {
				curValue=-(int)Math.round((data[i]-minValue)*factor/scale);
				g.fillOval(left+(int)Math.round(i*stepX)-3, zeroValue+curValue-3, 6, 6);
		    }
		    // draw labels
		    int width;
		    String str;
		    FontMetrics metrics = g.getFontMetrics();
		    for (int i=0; i <= steps; i++) {
				str = String.valueOf(Math.round(100.00*(maxValue-stepValue*i))/100.00);
				width  = metrics.stringWidth(str);
				g.drawString(str, left-10-width, top+(int)Math.round(i*stepY)+5);
		    }
		    ((Graphics2D)g).rotate(-Math.PI/2.0);
		    g.setColor(Color.blue);
		    for (int i=0; i<labels.length; i++) {
				str = String.valueOf(labels[i]);
				width  = metrics.stringWidth(str);
				g.drawString(str,-top-plotHeight-10-width, left+5+(int)Math.round(i*stepX));
		    }
		    ((Graphics2D)g).rotate(Math.PI/2.0);
		}
    }

    public static void main(String[] args){
    	float[][] fm = new float[20][20];
    	for (int i=0; i<fm.length; i++){
    		for (int j=0; j<fm[i].length; j++){
    			fm[i][j] = 0;
    		}
    	}
		fm[0][0] = 2000;
		fm[1][1] = 2000;
		fm[0][1] = 4000;
		fm[1][0] = 4000;
		for (int i=0; i<fm.length; i++){
			fm[i][i] = i;
		}
		fm[0][0] = 20;
		float[] fa = {.99f, .88f, .72f, .1f};
		String[]sa = {"2 Clusters", "3 Clusters", "4 Clusters", "5 Clusters"};
    	NMFPlotViewer pv = new NMFPlotViewer(fa, sa);
    	JDialog jd = new JDialog();
    	jd.add(pv.getContentComponent());
    	jd.setSize(800, 800);
    	jd.setModal(true);
    	jd.setVisible(true);
    	System.exit(0);
    	 
    }
}
