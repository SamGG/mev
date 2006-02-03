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
        nodeName = "Region Deletions";
    }

    public int getFlankingRegionType() {
        return FlankingRegion.DELETION;
    }

}
