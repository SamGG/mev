/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * DAMExperimentCentriodViewer.java
 *
 */

package org.tigr.microarray.mev.cluster.gui.impl.dam;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;

public class DAMExperimentCentroidViewer extends ExperimentClusterCentroidViewer {
    
    /**
     * Construct a <code>KMCCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public DAMExperimentCentroidViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    
    /**
     * @inheritDoc
     */
    public DAMExperimentCentroidViewer(Experiment experiment, int[][] clusters, Integer clusterIndex, float[][] means, float[][] variances, float[][] codes){
    	super(experiment, clusters, clusterIndex, means, variances, codes);
    }
    
    
}


