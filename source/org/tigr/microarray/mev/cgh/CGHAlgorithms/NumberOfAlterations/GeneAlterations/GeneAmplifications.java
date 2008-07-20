/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * GeneAmplifications.java
 *
 * Created on May 21, 2003, 10:21 PM
 */

package org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.GeneAlterations;

import org.tigr.microarray.mev.cgh.CGHDataObj.FlankingRegion;
import org.tigr.microarray.mev.cluster.gui.IFramework;
/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class GeneAmplifications extends GeneAlterations{

    /** Creates a new instance of GeneAmplifications */
    public GeneAmplifications() {
        nodeName = "GeneAmplifications";
    }

    public GeneAmplifications(IFramework framework) {
    	super(framework);
        nodeName = "GeneAmplifications";
    }

    protected int getAlterationType(){
        return FlankingRegion.AMPLIFICATION;
    }
}
