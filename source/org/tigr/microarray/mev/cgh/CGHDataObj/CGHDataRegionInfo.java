/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * CGHDataRegionInfo.java
 *
 * Created on March 25, 2003, 11:46 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataObj;

//import org.tigr.microarray.mev.cgh.CGHDataObj.*;

/**
 * Simple class that is used to pass a CGHDataRegion and its
 * experiment index
 * @author  Adam Margolin
 * @author Raktim Sinha
 *
 */

public class CGHDataRegionInfo {
    ICGHDataRegion dataRegion;
    int experimentIndex;

    /** Creates a new instance of CGHDataRegionInfo */
    public CGHDataRegionInfo(ICGHDataRegion dataRegion, int experimentIndex) {
        this.dataRegion = dataRegion;
        this.experimentIndex = experimentIndex;
    }

    /** Getter for property dataRegion.
     * @return Value of property dataRegion.
     */
    public ICGHDataRegion getDataRegion() {
        return dataRegion;
    }

    /** Setter for property dataRegion.
     * @param dataRegion New value of property dataRegion.
     */
    public void setDataRegion(ICGHDataRegion dataRegion) {
        this.dataRegion = dataRegion;
    }

    /** Getter for property experimentIndex.
     * @return Value of property experimentIndex.
     */
    public int getExperimentIndex() {
        return experimentIndex;
    }

    /** Setter for property experimentIndex.
     * @param experimentIndex New value of property experimentIndex.
     */
    public void setExperimentIndex(int experimentIndex) {
        this.experimentIndex = experimentIndex;
    }

}
