/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Hexagon.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
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
