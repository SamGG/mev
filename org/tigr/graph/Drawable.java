/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Drawable.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:25 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.graph;

import java.awt.*;
import javax.swing.*;

public abstract class Drawable extends JPanel {
    
    protected abstract void controlPaint(Graphics g);
    
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
    
    public void drawRect(Graphics2D g, int x, int y, int width, int height, Color color) {
	g.setColor(color);
	g.drawRect(x, y, width, height);
    }
    
    public void fillRect(Graphics2D g, int x, int y, int width, int height, Color color) {
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
}