/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: Heap.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:40:35 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.motif;

public class Heap {
    int N;           /* max number of items in heap */
    int n;           /* number of items in heap */
    int d;           /* base of heap */
    int[] h;         /* {h[1],...,h[n]} is set of items */
    int[] pos;           /* pos[i] gives position of i in h */
    int[] kvec;          /* kvec[i] is key of item i */
    
    Heap(int N,int D) {
	this.N = N;
	d = D;
	n = 0;
	h = new int[N];
	pos = new int[N];
	kvec=new int[N];
	for (int i=0; i<N; i++) pos[i] = 0;
    }
    /* insert item i with specified key */
    public void insrtHeap(int i,double k) {
	if (i<0) System.out.println("fatal! item to insert is < 0");
	if (pos[i]!=-1) rmHeap(i);
	kvec[i] = (int)k;
	n++;
	siftupHeap(i,(int)n);
    }
    
    /* Remove item i from heap. */
    int rmHeap(int i) {
	int j;
	if (pos[i]==-1) return -1;
	j = h[(int)n--];
	if (i != j && kvec[(int)j] <= kvec[i]) siftupHeap((int)j,(int)pos[i]);
	else if (i != j && kvec[(int)j]>kvec[i]) siftdownHeap((int)j,(int)pos[(int)i]);
	pos[i] = -1;
	return i;
    }
    
    /* delete and return item with smallest key */
    public int delminHeap() {
	int i;
	if (n == 0) return 0;
	i = h[0];
	rmHeap((int)h[0]);
	return i;
    }
    
    /* Shift i up from position x to restore heap order.*/
    void siftupHeap(int i ,int x) {
	int px = pHeap(x);
	while (x > 0 && kvec[(int)h[(int)px]]>kvec[i]) {
	    h[(int)x] = h[(int)px];
	    pos[(int)h[(int)x]] = x;
	    x = (int)px;
	    px = pHeap(x);
	}
	h[x] = i;
	pos[i] = x;
    }
    
    public int pHeap(int x) {
	return((x+(d-2))/d);
    }
    
    /* Shift i down from position x to restore heap order.*/
    public void siftdownHeap(int i,int x) {
	int cx = minchildHeap(x);
	while (cx != -1 && kvec[(int)h[(int)cx]] < kvec[i]) {
	    h[x] = h[(int)cx];
	    pos[(int)h[x]] = x;
	    x = (int)cx;
	    cx = minchildHeap(x);
	}
	h[x] = i;
	pos[i] = x;
    }
    
    /* Return the position of the child of the item at position x
       having minimum key. */
    int minchildHeap(int x) {
	int y, minc;
	if ((minc = leftHeap(x)) > n) return -1;
	for (y = minc + 1; y <= rightHeap(x) && y <= n; y++) {
	    if (kvec[(int)h[(int)y]] < kvec[(int)h[(int)minc]]) minc = y;
	}
	return minc;
    }
    
    public int leftHeap(int x) {
	return(d*((x)-1)+2);
    }
    
    public int rightHeap(int x) {
	return(d*(x)+1);
    }
}
