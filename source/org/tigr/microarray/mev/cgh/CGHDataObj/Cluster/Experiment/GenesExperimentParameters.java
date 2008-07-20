/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * GenesExperimentParameters.java
 *
 * Created on June 3, 2003, 1:51 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataObj.Cluster.Experiment;

import java.io.File;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class GenesExperimentParameters {
    File genesFile;
    /** Creates a new instance of GenesExperimentParameters */
    public GenesExperimentParameters() {
    }

    /** Getter for property genesFile.
     * @return Value of property genesFile.
     */
    public java.io.File getGenesFile() {
        return genesFile;
    }

    /** Setter for property genesFile.
     * @param genesFile New value of property genesFile.
     */
    public void setGenesFile(java.io.File genesFile) {
        this.genesFile = genesFile;
    }

}
