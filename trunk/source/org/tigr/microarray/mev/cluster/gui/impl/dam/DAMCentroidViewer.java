/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * DAMCentroidViewer.java
 *
 */

package org.tigr.microarray.mev.cluster.gui.impl.dam;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;

public class DAMCentroidViewer extends CentroidViewer {
    
    /** Creates a new instance of DAMCentroidViewer */
    public DAMCentroidViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);   
    }
    /**
     * @inheritDoc
     */
    public DAMCentroidViewer(Experiment e, int[][] clusters, float[][] variances, float[][] means, float[][] codes) {
    	super(e, clusters, variances, means, codes);
    }
    
}
