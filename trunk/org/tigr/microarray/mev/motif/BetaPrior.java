/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: BetaPrior.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.motif;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/*************************************************************************
 *
 * beta prior distribution for Bayesean statistics:
 *
 * mean = m = a/(a+b)
 * var = S^2 = a*b/((a+b+1)(a+b)^2)
 *
 * a = -m - m^2*(m-1)/s^2
 * b = -1 +m + m*(m-1)^2/s^2
 *
 *************************************************************************/

public class BetaPrior {
    
    double A;        /* total pseudo trials */
    double N;        /* total real trials */
    double T;        /* Total trials */
    double alpha;    /* #successful pseudo trials */
    double beta;     /* #failed pseudo trials */
    long success;    /* #successful real trials */
    long expect;     /* expected number of success */
    double p;        /* posterior probability of sucess */
    double weight;   /* A/N = w/(1-w) - fractional weight */
    boolean calc;
    
    /*******************************************************************
     * N = total sites; 	- Pseudo and Real sites.
     * tot_sites  = number of sites;
     * alpha = number site pseudo counts;
     * beta = number site pseudo counts;
     *******************************************************************/
    public BetaPrior(long Expect, double Weight, double n) {
	if (N <=0) System.out.println("total sites must be > 0");
	expect = Expect;
	weight = Weight;
	SetBPriorN(N);
	success = 0;
	p = (alpha + (double)expect)/T;
	calc = false;
    }
    
    
    public void ClearBPrior() {
	success = 0;
	p = (alpha + (double)expect)/T;
	calc = false;
    }
    
    public void SetBPriorS(long Success) {
	success = Success;
	calc = true;
    }
    
    public void SetBPriorN(double n) {
	double  ratio;
	ratio = (weight/(1.0 - weight));
	this.N = n;
	A =n*ratio;     /* A = N*(w/(1-w)) */
	alpha = (double) expect*ratio;
	beta= A - alpha;
	T=(A + N);
	calc = true;
    }
    
    public double PostProbBPrior() {
	if (calc) {
	    calc=false;
	    p=(alpha+(double)success)/(T-1);
	}
	return p;
    }
    
    /** Return the ratio of new to old. **/
    public double RatioBPrior(BetaPrior B1, BetaPrior B2) {
	double v1,v2;
	v1 = (double) B1.success;
	v2 = (double) B2.success;
	v1 = LnBeta(v1+B1.alpha,B1.N - v1 + B1.beta)- LnBeta(B1.alpha,B1.beta);
	v2 = LnBeta(v2+B2.alpha,B2.N - v2 + B2.beta)- LnBeta(B2.alpha,B2.beta);
	return Math.exp(v1 - v2);
    }
    
    public double LnBeta(double a, double b) {
	return(lgamma((float)(a))+lgamma((float)(b))-lgamma((float)(a+b)));
    }
    
    public float lgamma(float xx) {
	double x,y,tmp,ser;
	double[] cof=new double[6];
	cof[0]=76.18009172947146;
	cof[1]=-86.50532032941677;
	cof[2]=24.01409824083091;
	cof[3]=-1.231739572450155;
	cof[4]=0.1208650973866179e-2;
	cof[5]=-0.5395239384953e-5;
	int j;
	y=x=xx;
	tmp=x+5.5;
	tmp -= (x+0.5)*Math.log(tmp);
	ser=1.000000000190015;
	for (j=0;j<=5;j++) ser += cof[j]/++y;
	return(float)(-tmp+Math.log(2.5066282746310005*ser/x));
    }
}