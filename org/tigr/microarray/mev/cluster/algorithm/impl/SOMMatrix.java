/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SOMMatrix.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:23:47 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.util.ArrayList;

public class SOMMatrix {
    
    private ArrayList rowArrayList;
    
    public SOMMatrix(int width, int height, int dimension) {
	this.rowArrayList = new ArrayList(height);
	ArrayList list1, list2;
	for (int i=0; i<height; i++) {
	    list1 = new ArrayList(width);
	    for (int j=0; j<width; j++) {
		list2 = new ArrayList(dimension);
		for (int k=0; k<dimension; k++) {
		    list2.add(new Float(0));
		}
		list1.add(list2);
	    }
	    this.rowArrayList.add(list1);
	}
    }
    
    public void addValue(int x, int y, float value) {
	ArrayList list1 = (ArrayList)this.rowArrayList.get(y);
	ArrayList list2 = (ArrayList)list1.get(x);
	list2.add(new Float(value));
    }
    
    public void insertValue(int x, int y, int index, float value) {
	ArrayList list1 = (ArrayList)this.rowArrayList.get(y);
	ArrayList list2 = (ArrayList)list1.get(x);
	list2.add(index, new Float(value));
    }
    
    public int getDimension(int x, int y) {
	ArrayList list1 = (ArrayList)this.rowArrayList.get(y);
	ArrayList list2 = (ArrayList)list1.get(x);
	return list2.size();
    }
    
    public float getValue(int x, int y, int z) {
	ArrayList list1 = (ArrayList)this.rowArrayList.get(y);
	ArrayList list2 = (ArrayList)list1.get(x);
	return ((Float)list2.get(z)).floatValue();
    }
    
    public ArrayList getArrayList(int x, int y) {
	ArrayList list1 = (ArrayList)this.rowArrayList.get(y);
	ArrayList list2 = (ArrayList)list1.get(x);
	return list2;
    }
    
    public void setValue(int x, int y, int z, float value) {
	ArrayList list1 = (ArrayList)this.rowArrayList.get(y);
	ArrayList list2 = (ArrayList)list1.get(x);
	list2.set(z,new Float(value));
    }
    
    public void printArrayList(int x, int y) {
	ArrayList list1 = (ArrayList)this.rowArrayList.get(y);
	ArrayList list2 = (ArrayList)list1.get(x);
	System.out.println("ArrayList["+x+":"+y+"]="+list2);
    }
    
}