/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: FloatArray.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:36:56 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.util;

public class FloatArray {

    private float[] elementData;
    private int size;

    /**
     * Constructs an empty array with the specified initial capacity.
     */
    public FloatArray(int initialCapacity) {
        this.elementData = new float[initialCapacity];
    }

    /**
     * Returns an array containing all of the floats in this container
     * in the correct order.
     */
    public float[] toArray() {
        float[] result = new float[size];
        System.arraycopy(elementData, 0, result, 0, size);
        return result;
    }

    /**
     * Returns the float value at the specified position in this container.
     */
    public float get(int index) {
        RangeCheck(index);
        return elementData[index];
    }

    /**
     * Replaces the float value at the specified position in this container with
     * the specified one.
     */
    public void set(int index, float value) {
        RangeCheck(index);
        elementData[index] = value;
    }

    /**
     * Appends the specified value to the end of this container.
     */
    public boolean add(float i) {
        ensureCapacity(size + 1);
        elementData[size++] = i;
        return true;
    }

    public int getSize() {
        return size;
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
     * Increases the capacity of this <tt>FloatArray</tt> instance, if
     * necessary, to ensure that it can hold at least the number of floats
     * specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity.
     */
    private void ensureCapacity(int minCapacity) {
        int oldCapacity = elementData.length;
        if (minCapacity > oldCapacity) {
            float[] oldData = elementData;
            int newCapacity = (oldCapacity * 3)/2 + 1;
            if (newCapacity < minCapacity)
                newCapacity = minCapacity;
            elementData = new float[newCapacity];
            System.arraycopy(oldData, 0, elementData, 0, size);
        }
    }

}
