/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: IntSorter.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:45:30 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl.util;

public class IntSorter {

    /**
     * Sorts an array with specified comparator.
     *
     * @param a the array of integers to be sorted.
     * @param c the <code>IntComparator</code> interface implementation.
     * @see IntComparator
     */
    public static void sort(int[] a, IntComparator c) {
        int[] aux = cloneArray(a);
        mergeSort(aux, a, 0, a.length, c);
    }

    private static int[] cloneArray(int[] a) {
        int[] clone = new int[a.length];
        for (int i = a.length; --i >= 0;) {
            clone[i] = a[i];
        }
        return clone;
    }

    private static void swap(int[] x, int a, int b) {
        int t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    private static void mergeSort(int[] src, int[] dest, int low, int high, IntComparator c) {
        int length = high - low;

        // Insertion sort on smallest arrays
        if (length < 7) {
            for (int i=low; i<high; i++)
                for (int j=i; j>low && c.compare(dest[j-1], dest[j])>0; j--)
                    swap(dest, j, j-1);
            return;
        }

        // Recursively sort halves of dest into src
        int mid = (low + high)/2;
        mergeSort(dest, src, low, mid, c);
        mergeSort(dest, src, mid, high, c);

        // If list is already sorted, just copy from src to dest.  This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (c.compare(src[mid-1], src[mid]) <= 0) {
            System.arraycopy(src, low, dest, low, length);
            return;
        }

        // Merge sorted halves (now in src) into dest
        for (int i = low, p = low, q = mid; i < high; i++) {
            if (q>=high || p<mid && c.compare(src[p], src[q]) <= 0)
                dest[i] = src[p++];
            else
                dest[i] = src[q++];
        }
    }
}
