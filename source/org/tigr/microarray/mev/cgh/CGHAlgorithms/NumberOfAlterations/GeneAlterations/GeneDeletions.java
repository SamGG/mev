/*
 * GeneDeletions.java
 *
 * Created on May 21, 2003, 5:57 PM
 */

package org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.GeneAlterations;

import org.tigr.microarray.mev.cgh.CGHDataObj.FlankingRegion;
import org.tigr.microarray.mev.cluster.gui.IFramework;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class GeneDeletions extends GeneAlterations{

    /** Creates a new instance of GeneAmplifications */
    public GeneDeletions() {
        nodeName = "GeneDeletions";
    }

    public GeneDeletions(IFramework framework) {
    	super(framework);
        nodeName = "GeneDeletions";
    }

    protected int getAlterationType(){
        return FlankingRegion.DELETION;
    }
}
