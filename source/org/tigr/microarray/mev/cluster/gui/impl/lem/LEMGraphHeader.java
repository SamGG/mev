/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.impl.lem;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.JPanel;

/**
 * @author braisted
 *
 * Header to indicate zoom location and level of the graph viewer
 */
public class LEMGraphHeader extends JPanel {

	private float leftLimit, rightLimit;
	private String title;
	private int h, w, thinBarH, thickBarH, goalPostW;
	private FontMetrics fm;
	private Insets insets;
	
	public LEMGraphHeader() {
		super();
		setOpaque(true);
		setBackground(Color.white);
		
		title = "Current Viewer Range";
		leftLimit = 0f;
		rightLimit = 1f;
		Font font = this.getFont();
		thinBarH = 3;
		thickBarH = 10;
		goalPostW = 3;
		insets = new Insets(10,40,10,40);

		if(font != null) {
			w = 50;			
			fm = this.getFontMetrics(font);						
			h = fm.getHeight() + insets.top + insets.bottom + thickBarH;
			setPreferredSize(new Dimension(w, h));
		}
		
	}
	
	/**
	 * sets the range limits (0-1 range allowed on each)
	 * @param left left limit
	 * @param right right limit
	 */
	public void setLimits(float left, float right) {
		this.leftLimit = left;
		this.rightLimit = right;
		repaint();
	}
	
	/**
	 * Resets limit to full
	 */
	public void resetLimits() {
		this.leftLimit = 0f;
		this.rightLimit = 1f;
		repaint();
	}
	
	/**
	 * Renders header panel
	 */
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D)g;
		
		if(fm == null)
			fm = g2.getFontMetrics();
		
		h = getHeight();
		w = getWidth();
		
		g2.setColor(Color.blue);
		
		//goal posts
		g2.fillRect(insets.left, insets.top, goalPostW, h-insets.top-insets.bottom);		
		g2.fillRect(w-insets.right, insets.top, goalPostW, h-insets.top-insets.bottom);		

		//crossbar
		g2.fillRect(insets.left, h/2-thinBarH/2, w-insets.left-insets.right, thinBarH);
		
		//set thick background
		g2.setColor(Color.gray);
		Composite origComposite = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		
		//thickBar
		g2.fillRect(insets.left + (int)(((float)(w-insets.left-insets.right))*leftLimit), 
				h/2-thickBarH/2, (int)( ((float)(w-insets.left-insets.right))*(rightLimit-leftLimit)), thickBarH);		
		
		g2.setComposite(origComposite);
		g2.setColor(Color.blue);

		g2.drawRect(insets.left + (int)(((float)(w-insets.left-insets.right))*leftLimit), 
				h/2-thickBarH/2, (int)( ((float)(w-insets.left-insets.right))*(rightLimit-leftLimit)), thickBarH);		

	}
	
}
