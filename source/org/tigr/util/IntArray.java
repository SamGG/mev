/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: IntArray.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.util;

public class IntArray {
    
    private int[] elementData;
    private int size;
    
    /**
     * Constructs an empty array with the specified initial capacity.
     */
    public IntArray(int initialCapacity) {
	this.elementData = new int[initialCapacity];
    }
    
    /**
     * Returns an array containing all of the ints in this container
     * in the correct order.
     */
    public int[] toArray() {
	int[] result = new int[size];
	System.arraycopy(elementData, 0, result, 0, size);
	return result;
    }
    
    /**
     * Returns the int value at the specified position in this container.
     */
    public int get(int index) {
	RangeCheck(index);
	return elementData[index];
    }
    
    /**
     * Replaces the int value at the specified position in this container with
     * the specified one.
     */
    public void set(int index, int value) {
	RangeCheck(index);
	elementData[index] = value;
    }
    
    /**
     * Appends the specified value to the end of this container.
     */
    public boolean add(int i) {
	ensureCapacity(size + 1);
	elementData[size++] = i;
	return true;
    }
    
    /**
     * Check if the given index is in range. If not, throw an appropriate
     * runtime exception.
     */
    private void RangeCheck(int index) {
	if (index >= size || index < 0)
	    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
    }
    
    /**
     * Increases the capacity of this <tt>IntArray</tt> instance, if
     * necessary, to ensure that it can hold at least the number of integers
     * specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity.
     */
    private void ensureCapacity(int minCapacity) {
	int oldCapacity = elementData.length;
	if (minCapacity > oldCapacity) {
	    int[] oldData = elementData;
	    int newCapacity = (oldCapacity * 3)/2 + 1;
	    if (newCapacity < minCapacity)
		newCapacity = minCapacity;
	    elementData = new int[newCapacity];
	    System.arraycopy(oldData, 0, elementData, 0, size);
	}
    }
    
}
