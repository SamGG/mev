/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SOMExperimentCentroidViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-05-02 16:57:04 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.som;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;

public class SOMExperimentCentroidViewer extends ExperimentClusterCentroidViewer {    
    
    /**
     * Construct a <code>SOMCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public SOMExperimentCentroidViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }

    /**
     * @inheritDoc
     */
    public SOMExperimentCentroidViewer(Experiment experiment, int[][] clusters, Integer clusterIndex, float[][] means, float[][] variances, float[][] codes){
    	super(experiment, clusters, clusterIndex, means, variances, codes);
    }
    
}

