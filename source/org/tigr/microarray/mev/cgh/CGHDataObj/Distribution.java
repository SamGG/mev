/*
 * Distribution.java
 *
 * Created on June 10, 2003, 9:53 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataObj;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class Distribution {
    double mean;
    double sd;

    /** Creates a new instance of Distribution */
    public Distribution() {
    }

    /** Getter for property mean.
     * @return Value of property mean.
     */
    public double getMean() {
        return mean;
    }

    /** Setter for property mean.
     * @param mean New value of property mean.
     */
    public void setMean(double mean) {
        this.mean = mean;
    }

    /** Getter for property sd.
     * @return Value of property sd.
     */
    public double getSd() {
        return sd;
    }

    /** Setter for property sd.
     * @param sd New value of property sd.
     */
    public void setSd(double sd) {
        this.sd = sd;
    }

    public double getZScore(double value){
        double z = (value - mean) / sd;
        return z;
    }

}
