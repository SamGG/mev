/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: IntArray.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:45:30 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl.util;

public class IntArray {

    private int[] elementData;
    private int size;


    public IntArray() {
        this(10);
    }
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

    public void clear() {
        size = 0;
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
