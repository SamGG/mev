/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: MinMaxHeap.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:40:35 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.motif;

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
	this.hpsz=hpsz;
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

