/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: GraphCanvas.java,v $
 * $Revision: 1.5 $
 * $Date: 2006-02-23 20:59:40 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Vector;

public class GraphCanvas extends Drawable {
    
    public final static int SYSTEM_QUADRANT1_ONLY = 1000;
    public final static int SYSTEM_QUADRANT12_ONLY = 1001;
    public final static int SYSTEM_ALL_QUADRANTS = 1002;
    public final static int SYSTEM_BOUNDS = 1100;
    public final static int HISTOGRAM_BAR_OUTLINE = 2000;
    public final static int HISTOGRAM_BAR_SOLID = 2001;
    public final static int GRAPH_POINTS_SEPERATE = 3000;
    public final static int GRAPH_POINTS_CONNECT = 3001;
    
    protected double graphStartX, graphStopX, graphStartY, graphStopY;
    protected int preXSpacing, postXSpacing, preYSpacing, postYSpacing;
    protected double xAxisValue, yAxisValue;
    
    public boolean fixedHeight, fixedWidth;
    public int graphHeight, graphWidth;
    
    public boolean referenceLinesOn = true;
    public String title, xLabel, yLabel;
    protected Font tickFont, labelFont, titleFont;
    protected int tickFontHeight, tickFontWidth, labelFontHeight, labelFontWidth, titleFontHeight, titleFontWidth;
    
    protected Vector graphElements;
    
    public GraphCanvas() {
	initialize();
    }
    
    public GraphCanvas(int graphHeight, int graphWidth) {
	this.fixedHeight = true;
	this.fixedWidth = true;
	this.graphHeight = graphHeight;
	this.graphWidth = graphWidth;
	
	initialize();
    }
    
    public void initialize() {
	graphElements = new Vector();
	setDoubleBuffered(true);
	setBackground(Color.white);
	
	setTickFont("monospaced", Font.PLAIN, 10);
	setLabelFont("monospaced", Font.PLAIN, 12);
	setTitleFont("monospaced", Font.PLAIN, 16);
    }
    
    public void controlPaint(Graphics g1D) {
	Graphics2D g = (Graphics2D) g1D;
	drawGraph(g);
    }
    
    public void setTickFont(String fontName, int fontStyle, int fontSize) {
	tickFont = new Font(fontName, fontStyle, fontSize);
	tickFontWidth = (int) (.6 * fontSize);
	tickFontHeight = fontSize;
    }
    
    public void setLabelFont(String fontName, int fontStyle, int fontSize) {
	labelFont = new Font(fontName, fontStyle, fontSize);
	labelFontWidth = (int) (.6 * fontSize);
	labelFontHeight = fontSize;
    }
    
    public void setTitleFont(String fontName, int fontStyle, int fontSize) {
	titleFont = new Font(fontName, fontStyle, fontSize);
	titleFontWidth = (int) (.6 * fontSize);
	titleFontHeight = fontSize;
    }
    
    public void drawGraph(Graphics2D g) {
	
	drawSystem(g, SYSTEM_BOUNDS);
	
	for (int i = 0; i < graphElements.size(); i++) {
	    drawGraphElement(g, (GraphElement) graphElements.elementAt(i));
	}
	
	if (referenceLinesOn) { //Grid tracing is active
	    //	drawReferenceLines(g);
	}
	
	drawXLabel(g, xLabel, Color.black);
	drawYLabel(g, yLabel, Color.black);
	drawTitle(g, title, Color.black);
    }
    
    public void drawGraphElement(Graphics2D g, GraphElement e) {
	if (e instanceof GraphPoint) drawPoint(g, (GraphPoint) e);
	else if (e instanceof GraphBar) drawBar(g, (GraphBar) e);
	else if (e instanceof GraphTick) drawTick(g, (GraphTick) e);
	else if (e instanceof GraphLine) drawLine(g, (GraphLine) e);
	else if (e instanceof GraphPointGroup) drawPointGroup(g, (GraphPointGroup) e);
    }
    
	/*
	public void drawReferenceLines(Graphics2D g) {
		drawReferenceLines(g, getXOldEvent(), getYOldEvent());
	}
	 */
    
    public void drawReferenceLines(Graphics2D g, int x, int y) {
	if ((x <= convertX(graphStopX)) && (x >= convertX(graphStartX))) {
	    g.setColor(Color.magenta);
	    g.drawLine(x, convertY(graphStartY), x, convertY(graphStopY));
	}
	
	if ((y >= convertY(graphStopY)) && (y <= convertY(graphStartY))) {
	    g.setColor(Color.magenta);
	    g.drawLine(convertX(graphStartX), y, convertX(graphStopX), y);
	}
    }
    
    public void addGraphElement(GraphElement e) {
	graphElements.addElement(e);
    }
    
    public void removeAllGraphElements() {
	graphElements = new Vector();
    }
    
    public void clearAll(Graphics2D g) {
	fillRect(g, 0, 0, getSize().width, getSize().height, getBackground());
    }
    
    public void setGraphStartX(double graphStartX) {this.graphStartX = graphStartX;}
    public double getGraphStartX() {return this.graphStartX;}
    public void setGraphStartY(double graphStartY) {this.graphStartY = graphStartY;}
    public double getGraphStartY() {return this.graphStartY;}
    public void setGraphStopX(double graphStopX) {this.graphStopX = graphStopX;}
    public double getGraphStopX() {return this.graphStopX;}
    public void setGraphStopY(double graphStopY) {this.graphStopY = graphStopY;}
    public double getGraphStopY() {return this.graphStopY;}
    public void setGraphBounds(double graphStartX, double graphStopX, double graphStartY, double graphStopY) {
	this.graphStartX = graphStartX;
	this.graphStopX = graphStopX;
	this.graphStartY = graphStartY;
	this.graphStopY = graphStopY;
    }
    
    public void setPreXSpacing(int preXSpacing) {this.preXSpacing = preXSpacing;}
    public int getPreXSpacing() {return this.preXSpacing;}
    public void setPostXSpacing(int postXSpacing) {this.postXSpacing = postXSpacing;}
    public int getPostXSpacing() {return this.postXSpacing;}
    public void setPreYSpacing(int preYSpacing) {this.preYSpacing = preYSpacing;}
    public int getPreYSpacing() {return this.preYSpacing;}
    public void setPostYSpacing(int postYSpacing) {this.postYSpacing = postYSpacing;}
    public int getPostYSpacing() {return this.postYSpacing;}
    public void setGraphSpacing(int preXSpacing, int postXSpacing, int preYSpacing, int postYSpacing) {
	this.preXSpacing = preXSpacing;
	this.postXSpacing = postXSpacing;
	this.preYSpacing = preYSpacing;
	this.postYSpacing = postYSpacing;
    }
    
    public void setXAxisValue(double x) {this.xAxisValue = x;}
    public double getXAxisValue() {return this.xAxisValue;}
    public void setYAxisValue(double y) {this.yAxisValue = y;}
    public double getYAxisValue() {return this.yAxisValue;}
    
    public void drawSystem(Graphics2D g, int systemStyle) {
	switch (systemStyle) {
	    case SYSTEM_BOUNDS:
		drawLine(g, new GraphPoint(graphStartX, xAxisValue), new GraphPoint(graphStopX, xAxisValue), Color.black);
		drawLine(g, new GraphPoint(yAxisValue, graphStartY), new GraphPoint(yAxisValue, graphStopY), Color.black);
		break;
	    case SYSTEM_QUADRANT1_ONLY:
		break;
	    case SYSTEM_QUADRANT12_ONLY:
		break;
	    case SYSTEM_ALL_QUADRANTS:
		break;
	}
    }
    
    public int getWidth() {
	if (fixedWidth) {
	    return graphWidth;
	} else {
	    return getSize().width;
	}
    }
    
    public int getHeight() {
	if (fixedHeight) {
	    return graphHeight;
	} else {
	    return getSize().height;
	}
    }
    
    protected double getXScale() {
	return ((getWidth() - preXSpacing - postXSpacing) / (graphStopX - graphStartX));
    }
    
    protected double getYScale() {
	return ((getHeight() - preYSpacing - postYSpacing) / (graphStopY - graphStartY));
    }
    
    protected int convertX(double x) {
	return (int) ((x - graphStartX) * getXScale() + preXSpacing);
    }
    
    protected int convertY(double y) {
	return (int) ((graphStopY - y) * getYScale() + preYSpacing);
    }
    
    public void drawTick(Graphics2D g, GraphTick e) {
	if (e.getOrientation() == GC.HORIZONTAL) {
	    if (e.getLabel() != "") {
		drawVerticalTick(g, e.getLocation(), e.getHeight(), e.getAlignment(), e.getColor(), e.getLabel(), e.getLabelColor());
	    } else {
		drawVerticalTick(g, e.getLocation(), e.getHeight(), e.getAlignment(), e.getColor());
	    }
	    
	} else if (e.getOrientation() == GC.VERTICAL) {
	    if (e.getLabel() != "") {
		drawHorizontalTick(g, e.getLocation(), e.getHeight(), e.getAlignment(), e.getColor(), e.getLabel(), e.getLabelColor());
	    } else {
		drawHorizontalTick(g, e.getLocation(), e.getHeight(), e.getAlignment(), e.getColor());
	    }
	}
    }
    
    public void drawVerticalTick(Graphics2D g, double x, int length, int alignment, Color color) {
	if (x < graphStartX || x > graphStopX) return;
	
	switch (alignment) {
	    case GC.C:
		drawLine(g, convertX(x), convertY(xAxisValue) - (int) (length / 2), convertX(x), convertY(xAxisValue) + (int) (length / 2), color);
		break;
	    case GC.N:
		drawLine(g, convertX(x), convertY(xAxisValue), convertX(x), convertY(xAxisValue) - length, color);
		break;
	    case GC.S:
		drawLine(g, convertX(x), convertY(xAxisValue), convertX(x), convertY(xAxisValue) + length, color);
		break;
	}
    }
    
    public void drawVerticalTick(Graphics2D g, double x, int length, int alignment, Color color, String label, Color tickColor) {
	drawVerticalTick(g, x, length, alignment, color);
	
	if (true) { //Rotate labels
	    g.rotate(- Math.PI / 2);
	    drawString(g, label, - getHeight() + postYSpacing - (label.length() * tickFontWidth) - length,
	    convertX(x) + (tickFontHeight / 2), tickColor, tickFont);
	    g.rotate(Math.PI / 2);
	} else {
	    drawString(g, label, convertX(x) - (label.length() * tickFontWidth / 2),
	    getHeight() - postYSpacing + length + 10, tickColor, tickFont);
	}
    }
    
    public void drawHorizontalTick(Graphics2D g, double y, int length, int alignment, Color color) {
	if (y < graphStartY || y > graphStopY) return;
	
	switch (alignment) {
	    case GC.C:
		drawLine(g, convertX(yAxisValue) - (int) (length / 2), convertY(y), convertX(yAxisValue) + (int) (length / 2), convertY(y), color);
		break;
	    case GC.E:
		drawLine(g, convertX(yAxisValue), convertY(y), convertX(yAxisValue) + length, convertY(y), color);
		break;
	    case GC.W:
		drawLine(g, convertX(yAxisValue), convertY(y), convertX(yAxisValue) - length, convertY(y), color);
		break;
	}
    }
    
    // Modifications are necessary...
    public void drawHorizontalTick(Graphics2D g, double y, int length, int alignment, Color color, String label, Color tickColor) {
	drawHorizontalTick(g, y, length, alignment, color);
	
	drawString(g, label, preXSpacing - length - (label.length() * tickFontWidth),
	convertY(y) + (tickFontHeight / 2), tickColor, tickFont);
    }
    
    public void drawTitle(Graphics2D g, String title, Color titleColor) {
	if (title == null) return;
	drawString(g, title, getWidth() / 2 - (title.length() * titleFontWidth / 2), titleFontHeight * 2, titleColor, titleFont);
    }
    
    public void drawXLabel(Graphics2D g, String label, Color labelColor) {
	if (label == null) return;
	drawString(g, label, getWidth() / 2 - (label.length() * labelFontWidth / 2), convertY(graphStartY) + postYSpacing - labelFontHeight, labelColor, labelFont);
    }
    
    public void drawYLabel(Graphics2D g, String label, Color labelColor) {
	if (label == null) return;
	g.rotate(- Math.PI / 2);
	drawString(g, label, - postYSpacing + preXSpacing - (getHeight() / 2) - (label.length() * labelFontWidth / 2), labelFontHeight, labelColor, labelFont);
	g.rotate(Math.PI / 2);
    }
    
    public void drawPoint(Graphics2D g, GraphPoint graphPoint) {
	drawPointAt(g, graphPoint.getX(), graphPoint.getY(), graphPoint.getColor(), graphPoint.getPointSize());
    }
    
    public void drawPointAt(Graphics2D g, double x, double y, Color pointColor, int pointSize) {
	if ((x < graphStartX || x > graphStopX) || (y < graphStartY || y > graphStopY)) return;
	
	fillRect(g, convertX(x) - (pointSize / 2), convertY(y) - (pointSize / 2), pointSize, pointSize, pointColor);
    }
    
    public void drawPoints(Graphics2D g, Vector graphPoints, int graphPointStyle) {
	GraphPoint graphPoint, graphPoint2 = null;
	
	switch (graphPointStyle) {
	    case GRAPH_POINTS_SEPERATE:
		for (int i = 0; i < graphPoints.size(); i++) {
		    graphPoint = (GraphPoint) graphPoints.elementAt(i);
		    drawPoint(g, graphPoint);
		}
		break;
	    case GRAPH_POINTS_CONNECT:
		for (int i = 0; i < graphPoints.size(); i++) {
		    graphPoint = (GraphPoint) graphPoints.elementAt(i);
		    if (i == 0) graphPoint2 = graphPoint;
		    drawLine(g, graphPoint2, graphPoint, Color.black);
		    drawPoint(g, graphPoint2);
		    drawPoint(g, graphPoint);
		    graphPoint2 = graphPoint;
		}
		break;
	}
    }
    
    public void drawPointGroup(Graphics2D g, GraphPointGroup gpg) {
	
	double[] x = gpg.getX();
	double[] y = gpg.getY();
	Color pointColor = gpg.getColor();
	int pointSize = gpg.getPointSize();
	
	for (int i = 0; i < Math.min(x.length, y.length); i++) {
	    drawPointAt(g, x[i], y[i], pointColor, pointSize);
	}
    }
    
    public void drawLine(Graphics2D g, GraphLine e) {
	drawLine(g, convertX(e.getX1()), convertY(e.getY1()), convertX(e.getX2()), convertY(e.getY2()), e.getColor());
    }
    
    public void drawLine(Graphics2D g, GraphPoint graphPoint1, GraphPoint graphPoint2, Color lineColor) {
	drawLine(g, convertX(graphPoint1.getX()), convertY(graphPoint1.getY()),
	convertX(graphPoint2.getX()), convertY(graphPoint2.getY()), lineColor);
    }
    
    public void drawBar(Graphics2D g, GraphBar e) {
	if (e.getStyle() == GraphBar.VERTICAL) {
	    drawVerticalHistogramBar(g, e.getLower(), e.getUpper(), e.getValue(), e.getColor(), e.getStyle());
	} else if (e.getStyle() == GraphBar.HORIZONTAL) {
	    //Nothing yet
	}
    }
    
    public void drawVerticalHistogramBar(Graphics2D g, double low, double high, double value, Color barColor, int style) {
	if ((low < graphStartX || low > graphStopX) || (high < graphStartX || high > graphStopX)) return;
	if (value < graphStartY || value > graphStopY) return;
	
	if (style == GraphBar.OUTLINE) {
	    drawRect(g, convertX(low), convertY(value), (int) ((high - low) * getXScale()), (int) (value * getYScale()) - 1, barColor);
	} else if (style == GraphBar.SOLID) {
	    fillRect(g, convertX(low), convertY(value), (int) ((high - low) * getXScale()) + 1, (int) (value * getYScale()) + 1, barColor);
	}
    }
    
} //End GraphCanvas
