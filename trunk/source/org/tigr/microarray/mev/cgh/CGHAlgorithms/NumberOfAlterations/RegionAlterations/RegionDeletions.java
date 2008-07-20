/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * RegionDeletions.java
 *
 * Created on May 19, 2003, 7:08 PM
 */

package org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.RegionAlterations;

import org.tigr.microarray.mev.cgh.CGHDataObj.FlankingRegion;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class RegionDeletions extends RegionAlterations{

    /** Creates a new instance of RegionDeletions */
    public RegionDeletions() {
        nodeName = "RegionDeletions";
    }

    public int getFlankingRegionType() {
        return FlankingRegion.DELETION;
    }

}
