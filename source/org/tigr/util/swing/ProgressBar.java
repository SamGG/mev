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
package org.tigr.util.swing;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JPanel;

public class ProgressBar extends JDialog {
    private Frame parent;
    private String title;
    private Color barColor1, barColor2, textColor;
    private int units = 0, value = 0;
    private Applet progressBarApplet;
    private Font progressBarFont;
    private JPanel drawingArea;
    
    public ProgressBar(Frame parent, String title, Color barColor1, Color barColor2, Color textColor, int units) {
	super(parent, title, false);
	this.parent = parent;
	this.title = title;
	this.barColor1 = barColor1;
	this.barColor2 = barColor2;
	this.textColor = textColor;
	this.units = units;
	progressBarApplet = new Applet();
	progressBarFont = new Font("monospaced", Font.BOLD, 20);
	setResizable(false);
	addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		hide();
		dispose();
	    }
	});
	drawingArea = new JPanel() {
	    protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		OnPaint(g);
	    }
	};
	//		drawingArea.setBackground(Color.white);
	drawingArea.setPreferredSize(new Dimension(210,50));
	this.getContentPane().add(drawingArea, null);
	this.setSize(210,50);
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	Dimension barSize = this.getSize();
	this.setLocation((screenSize.width - barSize.width) / 2, (screenSize.height - barSize.height) / 2);
	//      this.setModal(true);
	this.pack();
	this.show();
	this.requestFocus();
	this.toFront();
    }
    
    public void OnPaint(Graphics g) {
	//      g.setColor(barColor);
	Graphics2D g2 = (Graphics2D) g;
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
	GradientPaint gp;
	gp=new GradientPaint(0,0,this.barColor1,200,0,this.barColor2);
	g2.setPaint(gp);
	g2.fillRect(5, 5, (value * 200 / units) , 40);
	g2.setColor(textColor);
	g2.drawRect(5,5,200,40);
	g2.setFont(progressBarFont);
	g2.drawString("" + (value * 100 / units) + "%", 90, 32);
	
    }
    
    public ProgressBar(Frame parent, String title, int units) {
	this(parent, title, new Color((int) 200, (int) 200, (int) 200),new Color((int) 200, (int) 200, (int) 200), Color.black, units);
    }
    
    public ProgressBar(Frame parent, String title, Color barColor, Color textColor, int units) {
	this(parent, title, barColor , barColor, Color.black, units);
    }
    
    public void drawProgressBar() {
	if (value<units) show();
	//      show();
    }
    
    public void increment(int increment) {
	if (value < units) {
	    value = value + increment;
	    this.repaint();
	}
	if (value >= units) {
	    this.hide();
	    this.dispose();
	}
    }
    
    public void set(int newvalue) {
	value=newvalue;
	repaint();
    }
}
