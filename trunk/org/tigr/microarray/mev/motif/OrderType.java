/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: OrderType.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.motif;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.tigr.microarray.mev.cluster.*;
/************************ ADT Order ***********************************
 * Defines a model for the orderings of ntyp different types of linear
 * elements having n (or npos) total positions in each of nseq sequences
 * (of elements).
 *
 * pos:           0     1     2     3     4     n-1    n
 * order:        ---[1]---[2]---[3]---[4]---....---[n]---
 *
 * seq[1]:	    A     B     A     C            B
 * seq[2]:	    C     B     A     B            A
 * :
 * :
 * seq[nseq]:	    B     C     A     B            A
 *
 * model[pos]:     ---[1]---[2]---[3]---....---[n]---
 * A		  #A[1] #A[2] #A[3]  ....  #A[n]
 * B		  #B[1] #B[2] #B[3]  ....  #B[n]
 * C		  #C[1] #C[2] #C[3]  ....  #C[n]
 *
 * pseudo:	   A*N0  ...etc...  (all positions have N0 of each element)
 * B*N0
 * C*N0
 * N0 defines the prior probability N0 (in pseudocounts) that any
 * element type will occur at any position in the ordering.
 **********************************************************************/

public class OrderType {
    long ntyp;        /* number of types of elements */
    long npos;        /* total number of postions for elements */
    long nseq;        /* number of sequences in the model */
    double N0;        /* number of pseudo elements at each pos */
    double[][] model;         /* model[npos][ntyp] = # each type @ pos */
    
    public OrderType() {
    }
    
/* Remove a sequence with ordering *order from the model;
   assumes that order is an array of npos elements (integers). */
    
    void  RmOrder(long[] order) {
	long i;
	nseq--;
	for (i=0; i<npos; i++) {
	    model[(int)i][(int)order[(int)i]] -= 1.0;
	}
    }
    
    
    
    /**********************************************************
   Return the relative probability that the order (given by inserting
   an element of type t at position i in *order) belongs to the
   model.  (*order is assumed to be an array of n-1 elements.)
 
  pos=2:		    t=A
  type:		    B     A     C     B             A
     *order:	---[1]---[2]---[3]---[4]---....---[n-1]---
  position:   	 0     1     2     3     4     n-2      n-1
 
  creates:	---[1]---[2]---[3]---[4]---[5]....---[n]---
  type:		    B     A     A     C     B         A
 
     ***********************************************************/
    public double RelProbOrder(long[] order, long t, long pos) {
	long i;
	double P; /* probability */
	
	if (pos >= npos) System.out.println("not that many positions in order");
	for (P=1.0,i=0; i < npos; i++) {
	    if (i == pos) {
		P *= model[(int)i][(int)t];
	    } else if (i < pos) {
		P *= model[(int)i][(int)order[(int)i]];
	    } else {  /* i-1 > pos */
		P *= model[(int)i][(int)order[(int)(i-1)]];
	    }
	}
	return P;
    }
    
/* add a sequence with ordering  *order to the model; assumes that
   order is an array of npos elements (integers). */
    
    void Add2Order(long[] order) {
	long i;
	nseq++;
	for (i=0; i<npos; i++) {
	    model[(int)i][(int)order[(int)i]] += 1.0;
	}
    }
}