/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: GraphTick.java,v $
 * $Revision: 1.2 $
 * $Date: 2004-02-06 22:46:02 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.graph;

import java.awt.Color;

public class GraphTick extends GraphElement {
    private double location;
    private int height;
    private Color color;
    private int orientation;
    private int alignment;
    private String label;
    private Color labelColor;
    
    public GraphTick(double location, int height, Color color, int orientation, int alignment, String label, Color labelColor) {
	this.location = location;
	this.height = height;
	this.color = color;
	this.orientation = orientation;
	this.alignment = alignment;
	this.label = label;
	this.labelColor = labelColor;
    }
    
    public GraphTick(double location, int height, int orientation) {
	this.location = location;
	this.height = height;
	this.orientation = orientation;
	this.alignment = GC.C;
	this.color = Color.black;
	this.label = "";
	this.labelColor = Color.black;
    }
    
    public void setLocation(double location) {this.location = location;}
    public double getLocation() {return this.location;}
    public void setHeight(int height) {this.height = height;}
    public int getHeight() {return this.height;}
    public void setOrientation(int orientation) {this.orientation = orientation;}
    public int getOrientation() {return this.orientation;}
    public void setAlignment(int alignment) {this.alignment = alignment;}
    public int getAlignment() {return this.alignment;}
    public void setColor(Color color) {this.color = color;}
    public Color getColor() {return this.color;}
    public void setLabel(String label) {this.label = label;}
    public String getLabel() {return this.label;}
    public void setLabelColor(Color labelColor) {this.labelColor = labelColor;}
    public Color getLabelColor() {return this.labelColor;}
}
