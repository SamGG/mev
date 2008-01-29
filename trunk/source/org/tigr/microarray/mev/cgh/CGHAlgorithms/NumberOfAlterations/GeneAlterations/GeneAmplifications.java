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
