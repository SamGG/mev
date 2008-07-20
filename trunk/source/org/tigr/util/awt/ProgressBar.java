/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ProgressBar.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 21:00:04 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.util.awt;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class ProgressBar extends JDialog {
    private JFrame parent;
    private String title;
    private Color barColor1, barColor2, textColor;
    private int units = 0, value = 0;
    private Font progressBarFont;
    private ProgressBarCanvas canvas;
    private Container contentPane;
    private GBA gba;
    
    public ProgressBar(JFrame parent, String title, Color barColor1, Color barColor2, Color textColor, int units) {
	super(parent, title, false);
	
	this.parent = parent;
	this.title = title;
	this.barColor1 = barColor1;
	this.barColor2 = barColor2;
	this.textColor = textColor;
	this.units = units;
	progressBarFont = new Font("monospaced", Font.BOLD, 20);
	contentPane = getContentPane();
	gba = new GBA();
	
	addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		hide();
	    }
	});
	
	canvas = new ProgressBarCanvas(200, 50, 5, 5, 5, 5);
	contentPane.setLayout(new GridBagLayout());
	gba.add(contentPane, canvas, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C);
	
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	Dimension barSize = getSize();
	setLocation((screenSize.width - barSize.width) / 2, (screenSize.height - barSize.height) / 2);
	setResizable(true);
	pack();
	show();
	requestFocus();
    }
    
    public ProgressBar(JFrame parent, String title, int units) {
	this(parent, title, Color.green, Color.green, Color.black, units);
    }
    
    public void drawProgressBar() {
	if (value < units) setVisible(true);
    }
    
    public void increment(int increment) {
	if (value < units) {
	    value = value + increment;
	    
	    if ((value * 100 / units) != ((value - increment) * 100 / units)) {
		canvas.paint(canvas.getGraphics());
	    }
	}
	if (value >= units) {
	    complete();
	}
    }
    
    public void complete() {
	hide();
	dispose();
    }
    
    private class ProgressBarCanvas extends Drawable {
	private int width, height;
	private int preXSpacing, postXSpacing, preYSpacing, postYSpacing;
	
	public ProgressBarCanvas(int width, int height, int preXSpacing, int postXSpacing, int preYSpacing, int postYSpacing) {
	    super(0, preXSpacing + width + postXSpacing, 0, preYSpacing + height + postYSpacing);
	    
	    this.width = width;
	    this.height = height;
	    this.preXSpacing = preXSpacing;
	    this.postXSpacing = postXSpacing;
	    this.preYSpacing = preYSpacing;
	    this.postYSpacing = postYSpacing;
	    
	    setPreferredSize(new Dimension(preXSpacing + width + postXSpacing, preYSpacing + height + postYSpacing));
	    setBackground(Color.white);
	}
	
	public void controlPaint(Graphics g1D) {
	    Graphics2D g = (Graphics2D) g1D;
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
	    g.setPaint(new GradientPaint(0, 0, barColor1, width, 0, barColor2));
	    g.fillRect(preXSpacing, preYSpacing, (value * width / units), height);
	    drawRect(g, preXSpacing, preYSpacing, width, height, textColor);
	    drawString(g, "" + (value * 100 / units) + "%", 90, 36, textColor, progressBarFont);
	}
    }
}
