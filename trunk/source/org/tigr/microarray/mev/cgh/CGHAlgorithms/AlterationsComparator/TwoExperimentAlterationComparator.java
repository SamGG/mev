/*
 * TwoExperimentAlterationComparator.java
 *
 * Created on November 22, 2003, 3:23 AM
 */

package org.tigr.microarray.mev.cgh.CGHAlgorithms.AlterationsComparator;

//import org.abramson.microarray.cgh.CGHFcdObj.CGHMultipleArrayDataFcd;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.util.IntArray;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */
public class TwoExperimentAlterationComparator {
    public static final int DELETION = -1;
    public static final int AMPLIFICATION = 1;

    //CGHMultipleArrayDataFcd fcd;
    IData data;

    int experimentA, experimentB;
    int comparisonType;

    int[] aOnly, bOnly, aAndB;

    /** Creates a new instance of TwoExperimentAlterationComparator */
    public TwoExperimentAlterationComparator() {
    }

    public void compareExperiments(/*CGHMultipleArrayDataFcd fcd*/IData data, int experimentA, int experimentB, int comparisonType){
        int copyNumberA;
        int copyNumberB;

        IntArray iaAonly = new IntArray(10);
        IntArray iaBonly = new IntArray(10);
        IntArray iaAandB = new IntArray(10);

        for(int clone = 0; clone < data.getFeaturesSize(); clone++){
            copyNumberA = data.getCopyNumberDetermination(experimentA, clone);
            copyNumberB = data.getCopyNumberDetermination(experimentB, clone);

            if(copyNumberA == -1 || copyNumberA == -2){
                copyNumberA = DELETION;
            }

            if(copyNumberA == 1 || copyNumberA == 2){
                copyNumberA = AMPLIFICATION;
            }

            if(copyNumberB == -1 || copyNumberB == -2){
                copyNumberB = DELETION;
            }

            if(copyNumberB == 1 || copyNumberB == 2){
                copyNumberB = AMPLIFICATION;
            }

            if(copyNumberA == comparisonType){
                if(copyNumberB == comparisonType){
                    iaAandB.add(clone);
                }else{
                    iaAonly.add(clone);
                }
            }else if(copyNumberB == comparisonType){
                iaBonly.add(clone);
            }
        }

        aOnly = iaAonly.toArray();
        bOnly = iaBonly.toArray();
        aAndB = iaAandB.toArray();

    }

    /** Getter for property aOnly.
     * @return Value of property aOnly.
     */
    public int[] getAOnly() {
        return this.aOnly;
    }

    /** Setter for property aOnly.
     * @param aOnly New value of property aOnly.
     */
    public void setAOnly(int[] aOnly) {
        this.aOnly = aOnly;
    }

    /** Getter for property bOnly.
     * @return Value of property bOnly.
     */
    public int[] getBOnly() {
        return this.bOnly;
    }

    /** Setter for property bOnly.
     * @param bOnly New value of property bOnly.
     */
    public void setBOnly(int[] bOnly) {
        this.bOnly = bOnly;
    }

    /** Getter for property aAndB.
     * @return Value of property aAndB.
     */
    public int[] getAAndB() {
        return this.aAndB;
    }

    /** Setter for property aAndB.
     * @param aAndB New value of property aAndB.
     */
    public void setAAndB(int[] aAndB) {
        this.aAndB = aAndB;
    }

}
