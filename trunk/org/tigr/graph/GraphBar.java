/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: GraphBar.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-02-24 20:24:07 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.graph;

import java.awt.Color;

public class GraphBar extends GraphElement {
    public final static int VERTICAL = 0;
    public final static int HORIZONTAL = 1;
    
    public final static int SOLID = 0;
    public final static int OUTLINE = 1;
    
    private double lower, upper, value;
    private Color color;
    private int orientation;
    private int style;
    
    public GraphBar(double lower, double upper, double value, Color color, int orientation, int style) {
	this.lower = lower;
	this.upper = upper;
	this.value = value;
	this.color = color;
	this.orientation = orientation;
	this.style = style;
    }
    
    public GraphBar(double lower, double upper, double value) {
	this.lower = lower;
	this.upper = upper;
	this.color = Color.black;
	this.orientation = GraphBar.VERTICAL;
	this.style = GraphBar.SOLID;
    }
    
    public void setLower(double lower) {this.lower = lower;}
    public double getLower() {return this.lower;}
    public void setUpper(double upper) {this.upper = upper;}
    public double getUpper() {return this.upper;}
    public void setValue(double value) {this.value = value;}
    public double getValue() {return this.value;}
    public void setColor(Color color) {this.color = color;}
    public Color getColor() {return this.color;}
    public void setOrientation(int range) {this.orientation = range;}
    public int getOrientation() {return this.orientation;}
    public void setStyle(int style) {this.style = style;}
    public int getStyle() {return this.style;}
}