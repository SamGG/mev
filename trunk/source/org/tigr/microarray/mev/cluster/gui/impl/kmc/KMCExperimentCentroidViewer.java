/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: KMCExperimentCentroidViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-08-22 18:01:26 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.kmc;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;

public class KMCExperimentCentroidViewer extends ExperimentClusterCentroidViewer {
    
    /**
     * Construct a <code>KMCCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public KMCExperimentCentroidViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    /**
     * Used to recreate a KMCExperimentCentroidViewer from saved data written by 
     * XMLEncoder.  
     * 
     * @param clusters
     * @param exptID
     * @param clusterIndex
     * @param means
     * @param variances
     * @param codes
     */
    public KMCExperimentCentroidViewer(Experiment e, int[][] clusters, Integer clusterIndex, float[][] means, float[][] variances, float[][] codes) {
    	super(e, clusters, clusterIndex, means, variances, codes);
    }
    
    
}

