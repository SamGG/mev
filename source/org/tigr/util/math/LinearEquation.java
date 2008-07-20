/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: LinearEquation.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.util.math;

public class LinearEquation {
    private double slope, yIntercept, regressionCoefficient;
    
    public LinearEquation() {
	this.slope = 0;
	this.yIntercept = 0;
	this.regressionCoefficient = 0;
    }
    
    public LinearEquation(double slope, double yIntercept, double regressionCoefficient) {
	this.slope = slope;
	this.yIntercept = yIntercept;
	this.regressionCoefficient = regressionCoefficient;
    }
    
    public void setSlope(double slope) {this.slope = slope;}
    public double getSlope() {return this.slope;}
    public void setYIntercept(double yIntercept) {this.yIntercept = yIntercept;}
    public double getYIntercept() {return this.yIntercept;}
    public void setRegressionCoefficient(double regressionCoefficient) {
	this.regressionCoefficient = regressionCoefficient;
    }
    public double getRegressionCoefficient() {return this.regressionCoefficient;}
}
