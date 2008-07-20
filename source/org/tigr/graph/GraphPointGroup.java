/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: GraphPointGroup.java,v $
 * $Revision: 1.2 $
 * $Date: 2004-02-06 22:46:02 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.graph;

import java.awt.Color;

public class GraphPointGroup extends GraphElement {
    
    private double[] x, y; //Arrays of x and y coordinates; should be same size
    private Color color;
    private int pointSize;
    
    public GraphPointGroup(double[] x, double[] y, Color color, int pointSize) {
	
	this.x = x;
	this.y = y;
	this.color = color;
	this.pointSize = pointSize;
    }
    
    public GraphPointGroup(double[] x, double[] y) {
	
	this.x = x;
	this.y = y;
	this.color = Color.black;
	this.pointSize = 1;
    }
    
    public void setX(double[] x) {this.x = x;}
    public double[] getX() {return this.x;}
    public void setY(double[] y) {this.y = y;}
    public double[] getY() {return this.y;}
    public void setColor(Color color) {this.color = color;}
    public Color getColor() {return this.color;}
    public void setPointSize(int pointSize) {this.pointSize = pointSize;}
    public int getPointSize() {return this.pointSize;}
}
