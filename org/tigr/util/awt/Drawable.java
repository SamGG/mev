/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Drawable.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 21:00:04 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.util.awt;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JPanel;

public abstract class Drawable extends JPanel {
    protected int startX, stopX, startY, stopY;
    
    public Drawable(int startX, int stopX, int startY, int stopY) {
	this.startX = startX;
	this.stopX = stopX;
	this.startY = startY;
	this.stopY = stopY;
	//setSize(startX + stopX, startY + stopY);
	setDoubleBuffered(true);
    }
    
    protected void paintComponent(Graphics g) {
	super.paintComponent(g);
	controlPaint(g);
    }
    
    public void setCursor(int cursor) {
	setCursor(Cursor.getPredefinedCursor(cursor));
    }
    
    public void drawPoint(Graphics2D g, Point point, Color color) {
	drawPoint(g, point.x, point.y, color);
    }
    
    public void drawPoint(Graphics2D g, int x, int y, Color color) {
	g.setColor(color);
	g.drawLine(x, y, x, y);
    }
    
    public void drawPoint(Graphics2D g, Point point) {
	drawPoint(g, point, getBackground());
    }
    
    public void drawPoint(Graphics2D g, int x, int y) {
	drawPoint(g, x, y, getBackground());
    }
    
    public void drawLine(Graphics2D g, int x1, int y1, int x2, int y2, Color color) {
	g.setColor(color);
	g.drawLine(x1, y1, x2, y2);
    }
    
    public void drawLine(Graphics2D g, int x1, int y1, int x2, int y2) {
	drawLine(g, x1, y1, x2, y2, getBackground());
    }
    
    public final void drawRect(Graphics2D g, int x, int y, int width, int height, Color color) {
	g.setColor(color);
	g.drawRect(x, y, width, height);
    }
    
    public final void fillRect(Graphics2D g, int x, int y, int width, int height, Color color) {
	g.setColor(color);
	g.fillRect(x, y, width, height);
    }
    
    public void drawString(Graphics2D g, String string, int x, int y, Color color, Font font) {
	g.setFont(font);
	g.setColor(color);
	g.drawString(string, x, y);
    }
    
    public void drawString(Graphics2D g, String string, int x, int y, Color color) {
	g.setColor(color);
	g.drawString(string, x, y);
    }
    
    public abstract void controlPaint(Graphics g);
}