/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: MergeJoinBag.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:02 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl.terrain; 

import java.util.Arrays;

public class MergeJoinBag {
    private int[][]   m_pVectToInd;  // indeces matrix (packed by rows)	| all syncronous
    private float[][] m_pDistToVal;  // distances matrix (packed by rows)| all syncronous	

    // statistics
    private float m_fltMin;
    private float m_fltMax;

    // helpers
    public int[] getIndVector(int row) {
        return this.m_pVectToInd[row];
    }

    public float[] getValVector(int row) {
        return this.m_pDistToVal[row];
    }

    public MergeJoinBag(int iRowCount, int iColumnCount) {
        this(iRowCount, iColumnCount, false);
    }

    public MergeJoinBag(int iRowCount, int iColumnCount, boolean bNoIndMatr) {
        if (bNoIndMatr)
            m_pVectToInd = null;
        else {
            m_pVectToInd = new int[iRowCount][iColumnCount];
            for (int i=0; i<m_pVectToInd.length; i++)
                Arrays.fill(m_pVectToInd[i], -1); 
        }
        m_pDistToVal = new float[iRowCount][iColumnCount];
        for (int i=0; i<m_pDistToVal.length; i++)
            Arrays.fill(m_pDistToVal[i], Float.MAX_VALUE); 
        m_fltMin =  Float.MAX_VALUE;
        m_fltMax = -Float.MAX_VALUE;
    }

    public int getRowCount() {
        return m_pDistToVal.length;
    }

    public int getColumnCount() {
        if (m_pDistToVal.length <= 0 || m_pDistToVal[0] == null)
            return 0;
        return m_pDistToVal[0].length;
    }

    public void Clear() {
        m_pVectToInd = null;
        m_pDistToVal = null;
    }

    public void AppendTo(int iIndFrom, int iIndTo, float fltDist) {
        // Append the pair iIndTo and fltDist
        int[] pFirstInt = getIndVector(iIndFrom);
        float[] pFirstDist = getValVector(iIndFrom);

        // find the index to insert to..
        // TODO: Change the following code to binary_search-like procedure//
        int iIndToInsert=0;                                               //
        for (; iIndToInsert<pFirstDist.length; iIndToInsert++)            //
            if (fltDist <= pFirstDist[iIndToInsert])                      //
                break;                                                    //
        ////////////////////////////////////////////////////////////////////

        if (iIndToInsert<pFirstDist.length) { // if found
            // then shift the tail (RtlMoveMemory(Destination,Source,Length))
            if (pFirstDist.length-iIndToInsert-1 > 0) {
                System.arraycopy(pFirstDist, iIndToInsert, pFirstDist, iIndToInsert+1, pFirstDist.length-iIndToInsert-1);
                System.arraycopy(pFirstInt, iIndToInsert, pFirstInt, iIndToInsert+1, pFirstInt.length-iIndToInsert-1);
            }
            pFirstInt[iIndToInsert]  = iIndTo;
            pFirstDist[iIndToInsert] = fltDist;
        }
    }

    public void Assert(int iIndFrom, int iIndTo, float fltDist) {
        m_fltMin = Math.min(m_fltMin, fltDist);
        m_fltMax = Math.max(m_fltMax, fltDist);
        AppendTo(iIndFrom, iIndTo, fltDist);
        AppendTo(iIndTo, iIndFrom, fltDist);
    }

    public void Normalize(float fltLow) {
        float delta=(m_fltMax-m_fltMin);

        for (int i=0; i<m_pDistToVal.length; i++)
            for (int j=0; j<m_pDistToVal[i].length; j++)
            m_pDistToVal[i][j] = (1f-(m_pDistToVal[i][j]-m_fltMin)/delta)*(1f-fltLow)+fltLow;
    }
}
