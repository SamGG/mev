/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: Monitor.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:50 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.dialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

public class Monitor extends JDialog {
    
    private JPanel drawingArea;
    private int position = 0;
    private int stepXFactor = 1;
    private double factor;
    private int[] values = new int[245];
    
    /**
     * Creates a <code>Monitor</code> with specified parameters.
     */
    public Monitor(Frame parent, String title, int x, int y, double factor) {
	super(parent, title);
	this.factor = factor;
	enableEvents(WindowEvent.WINDOW_EVENT_MASK);
	drawingArea = new JPanel() {
	    protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		onPaint(g);
	    }
	};
	drawingArea.setPreferredSize(new Dimension(275, 275));
	drawingArea.setOpaque(false);
	for (int i=1; i<200; i++) {
	    values[i]=0;
	}
	getContentPane().add(drawingArea);
	((JPanel)getContentPane()).setOpaque(false);
	ImageIcon backgroundImage = GUIFactory.getIcon("monitor.jpg");
	JLabel backgoundLabel = new JLabel(backgroundImage);
	drawingArea.add(backgoundLabel);
	getLayeredPane().add(backgoundLabel, new Integer(Integer.MIN_VALUE));
	backgoundLabel.setBounds(0, 0, backgroundImage.getIconWidth(), backgroundImage.getIconHeight());
	setLocation(x, y);
	pack();
    }
    
    /**
     * Updates the dialog view.
     */
    public void update(double value)  {
	int dummy = (int)Math.round(value*factor);
	if (dummy > 210) {
	    dummy = 210;
	}
	values[position] = 210 - dummy;
	position++;
	if (position*stepXFactor >= 245) {
	    position = 0;
	}
	drawingArea.repaint();
    }
    
    /**
     * Sets x step factor.
     */
    public void setStepXFactor(int stepXFactor) {
	this.stepXFactor = stepXFactor;
    }
    
    protected void processWindowEvent(WindowEvent e) {
	if (e.getID() == WindowEvent.WINDOW_CLOSING) {
	    dispose();
	}
	super.processWindowEvent(e);
    }
    
    /**
     * Paints a chart into specified graphics.
     */
    private void onPaint(Graphics g) {
	Graphics2D g2 = (Graphics2D)g;
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	g2.clipRect(15, 28, 245, 210);
	g2.setColor(new Color(118, 243, 254));
	for (int i=1; i<position; i++) {
	    g2.drawLine(15+(i-1)*stepXFactor, 28+values[i-1], 15+i*stepXFactor, 28+values[i]);
	}
    }
    
}
