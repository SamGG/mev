/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: GraphPoint.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:25 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.graph;

import java.awt.Color;

public class GraphPoint extends GraphElement {
    private double x, y;
    private Color color;
    private int pointSize;
    
    public GraphPoint(double x, double y, Color color, int pointSize) {
	this.x = x;
	this.y = y;
	this.color = color;
	this.pointSize = pointSize;
    }
    
    public GraphPoint(double x, double y) {
	this.x = x;
	this.y = y;
	this.color = Color.black;
	this.pointSize = 1;
    }
    
    public void setX(double x) {this.x = x;}
    public double getX() {return this.x;}
    public void setY(double y) {this.y = y;}
    public double getY() {return this.y;}
    public void setColor(Color color) {this.color = color;}
    public Color getColor() {return this.color;}
    public void setPointSize(int pointSize) {this.pointSize = pointSize;}
    public int getPointSize() {return this.pointSize;}
}