/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: GraphLine.java,v $
 * $Revision: 1.2 $
 * $Date: 2004-02-06 22:46:02 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.graph;

import java.awt.Color;

public class GraphLine extends GraphElement implements java.io.Serializable {
    private double x1, y1, x2, y2;
    private Color color;
    
    public GraphLine(double x1, double y1, double x2, double y2, Color color) {
	this.x1 = x1;
	this.y1 = y1;
	this.x2 = x2;
	this.y2 = y2;
	this.color = color;
    }
    
    public GraphLine(double x1, double y1, double x2, double y2) {
	this.x1 = x1;
	this.y1 = y1;
	this.x2 = x2;
	this.y2 = y2;
	this.color = Color.black;
    }
    
    public void setX1(double x1) {this.x1 = x1;}
    public double getX1() {return this.x1;}
    public void setX2(double x2) {this.x2 = x2;}
    public double getX2() {return this.x2;}
    public void setY1(double y1) {this.y1 = y1;}
    public double getY1() {return this.y1;}
    public void setY2(double y2) {this.y2 = y2;}
    public double getY2() {return this.y2;}
    public void setColor(Color color) {this.color = color;}
    public Color getColor() {return this.color;}
}