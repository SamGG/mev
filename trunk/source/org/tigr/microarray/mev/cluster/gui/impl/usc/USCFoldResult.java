/*
 * Created on Dec 7, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.util.BitSet;

/**
 * This class stores the results from 1 fold of testing
 * 
 * @author vu
 */
public class USCFoldResult {
    private int resultKount;
    private BitSet bsResult;
    private USCResult[] resultArray;
    private USCHyb[] testArray;
    private String[] uniqueClassArray;


    /**
     * 
     * @param resultKount
     */
    public USCFoldResult(int resultKount) {
        this.resultKount = resultKount;
        this.bsResult = new BitSet(this.resultKount);
        this.resultArray = new USCResult[this.resultKount];
    }//end constructor


    public void setResult(USCResult toSet, int resultIndex) {
        this.resultArray[resultIndex] = toSet;
        this.bsResult.set(resultIndex, true);
    }


    public USCResult getResult(int resultIndex) {
        if( this.bsResult.get(resultIndex) ) {
            return this.resultArray[resultIndex];
        } else {
            return null;
        }
    }


    public boolean hasResult(int resultIndex) {
        return this.bsResult.get(resultIndex);
    }


    public int getResultKount() {
        return this.resultKount;
    }


    public int getNonNullResultKount() {
        int toReturn = 0;

        for( int i = 0; i < resultArray.length; i++ ) {
            if( this.resultArray[i].getDiscScores() != null ) {
                toReturn++;
            }
        }

        return toReturn;
    }


    public void setTestArray(USCHyb[] testArray) {
        this.testArray = testArray;
    }


    public USCHyb[] getTestArray() {
        return this.testArray;
    }


    public void setUniqueClassArray(String[] uniqueClassArray) {
        this.uniqueClassArray = uniqueClassArray;
    }


    public String[] getUniqueClassArray() {
        return uniqueClassArray;
    }
}//end class
