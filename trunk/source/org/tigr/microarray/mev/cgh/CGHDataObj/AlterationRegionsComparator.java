/*
 * AlterationRegionsComparator.java
 *
 * Created on March 24, 2003, 1:57 AM
 */

package org.tigr.microarray.mev.cgh.CGHDataObj;

import java.util.Comparator;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class AlterationRegionsComparator implements Comparator{

    /** Creates a new instance of AlterationRegionsComparator */
    public AlterationRegionsComparator() {
    }

    public int compare(Object obj, Object obj1) {
        AlterationRegion region1 = (AlterationRegion)obj;
        AlterationRegion region2 = (AlterationRegion)obj1;

        return region2.getNumAlterations() - region1.getNumAlterations();
    }

}
