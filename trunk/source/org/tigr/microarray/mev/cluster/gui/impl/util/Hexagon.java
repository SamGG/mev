/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: Hexagon.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:36:56 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.util;

import java.awt.Polygon;

public class Hexagon extends Polygon {
    
    private int width;
    private int angle;
    private double c;
    private double a;
    private double b;
    
    public Hexagon(int width, int angle) {
	this.width = width;
	this.angle = angle;
	a = width/2.0;
	b = Math.tan(Math.PI/6.0)*a;
	c = a/(Math.cos(Math.PI/6.0));
	addPoint(0, 0);
	if (angle==0 || angle==180) {
	    addPoint((int)Math.round(b),-(int)Math.round(a));
	    addPoint((int)Math.round(b)+(int)Math.round(c),-(int)Math.round(a));
	    addPoint((int)Math.round(b)*2+(int)Math.round(c),0);
	    addPoint((int)Math.round(b)+(int)Math.round(c),+(int)Math.round(a));
	    addPoint((int)Math.round(b),+(int)Math.round(a));
	} else {
	    addPoint(-(int)Math.round(a),-(int)Math.round(b));
	    addPoint(-(int)Math.round(a),-(int)Math.round(b)-(int)Math.round(c));
	    addPoint(0,-(int)Math.round(b)*2-(int)Math.round(c));
	    addPoint((int)Math.round(a),-(int)Math.round(b)-(int)Math.round(c));
	    addPoint((int)Math.round(a),-(int)Math.round(b));
	}
	if (angle==180) translate(-(int)Math.round(b)*2-(int)Math.round(c),0);
	if (angle==270) translate(0,(int)Math.round(b)*2+(int)Math.round(c));
    }
    
    public int getA() {
	return(int)Math.round(a);
    }
    
    public int getB() {
	return(int)Math.round(b);
    }
    
    public int getC() {
	return(int)Math.round(c);
    }
    
    public int getHeight() {
	if (angle==90 || angle==270) {
	    return((int)Math.round(b)*2+(int)Math.round(c));
	} else {
	    return width;
	}
    }
    
    public int getWidth() {
	if (angle==0 || angle==180) {
	    return((int)Math.round(b)*2+(int)Math.round(c));
	} else {
	    return width;
	}
    }
    
}
