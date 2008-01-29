/*
 * BacClonesExperimentParameters.java
 *
 * Created on June 2, 2003, 1:19 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataObj.Cluster.Experiment;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class DataRegionsExperimentParameters {
    public static final int ALL_CHROMOSOMES = -1;

    int[] chromosomeIndices;

    /** Creates a new instance of BacClonesExperimentParameters */
    public DataRegionsExperimentParameters() {
    }

    /** Getter for property chromosomeIndices.
     * @return Value of property chromosomeIndices.
     */
    public int[] getChromosomeIndices() {
        return this.chromosomeIndices;
    }

    /** Setter for property chromosomeIndices.
     * @param chromosomeIndices New value of property chromosomeIndices.
     */
    public void setChromosomeIndices(int[] chromosomeIndices) {
        this.chromosomeIndices = chromosomeIndices;
    }

}
