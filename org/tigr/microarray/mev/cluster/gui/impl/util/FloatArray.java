/*
Copyright @ 1999-2002, The Institute for Genomic Research (TIGR).
All rights reserved.

This software is provided "AS IS".  TIGR makes no warranties, express
or implied, including no representation or warranty with respect to
the performance of the software and derivatives or their safety,
effectiveness, or commercial viability.  TIGR does not warrant the
merchantability or fitness of the software and derivatives for any
particular purpose, or that they may be exploited without infringing
the copyrights, patent rights or property rights of others. TIGR shall
not be liable for any claim, demand or action for any loss, harm,
illness or other damage or injury arising from access to or use of the
software or associated information, including without limitation any
direct, indirect, incidental, exemplary, special or consequential
damages.

This software program may not be sold, leased, transferred, exported
or otherwise disclaimed to anyone, in whole or in part, without the
prior written consent of TIGR.
*/
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
