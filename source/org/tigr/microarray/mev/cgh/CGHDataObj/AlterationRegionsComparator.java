/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
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
