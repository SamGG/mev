/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: MinMaxHeap.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:19 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.motif;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class MinMaxHeap {
    Heap heap;   /* minheap */
    Heap maxheap;    /* maxheap */
    int  hpsz;       /* heap size */
    int  nfree;      /* #items available */
    int[]    avail;      /* list of available item no */
    
    
    
    public MinMaxHeap(int hpsz, int d) {
	avail=new int[hpsz];
	heap=new Heap(hpsz,d);
	maxheap=new Heap(hpsz,d);
	hpsz=hpsz;
	nfree = hpsz;
	for (int i=0; i<hpsz; i++) avail[i] = i;
    }
    
/* NULL == not inserted; -1 == inserted but none deleted  */
    public void InsertMheap(double key) {
	int i=0;
	if (nfree > 0) {
	    i = avail[nfree-1];
	    nfree--;
	} else if ((maxheap.kvec[maxheap.h[0]]) < -key) {
	    i=maxheap.delminHeap();
	    heap.rmHeap(i);
	} //else return NULL;
	heap.insrtHeap(i,key);
	maxheap.insrtHeap(i,-key);
    }
    
}

