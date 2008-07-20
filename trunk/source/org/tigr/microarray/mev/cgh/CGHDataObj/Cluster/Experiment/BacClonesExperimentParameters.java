/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
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

public class BacClonesExperimentParameters {
    public static final int ALL_CHROMOSOMES = -1;

    int[] chromosomeIndices;
    boolean includeMissingBacs;

    /** Creates a new instance of BacClonesExperimentParameters */
    public BacClonesExperimentParameters() {
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

    /** Getter for property includeMissingBacs.
     * @return Value of property includeMissingBacs.
     */
    public boolean isIncludeMissingBacs() {
        return includeMissingBacs;
    }

    /** Setter for property includeMissingBacs.
     * @param includeMissingBacs New value of property includeMissingBacs.
     */
    public void setIncludeMissingBacs(boolean includeMissingBacs) {
        this.includeMissingBacs = includeMissingBacs;
    }

}
