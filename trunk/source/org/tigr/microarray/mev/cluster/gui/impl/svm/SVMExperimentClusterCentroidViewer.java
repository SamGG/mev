/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SVMExperimentClusterCentroidViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-05-02 16:57:36 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.svm;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;

public class SVMExperimentClusterCentroidViewer extends ExperimentClusterCentroidViewer {
    
    /**
     * Construct a <code>SVMCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public SVMExperimentClusterCentroidViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    /**
     * @inheritDoc
     */
    public SVMExperimentClusterCentroidViewer(Experiment e, int[][] clusters, Integer clusterIndex, float[][] means, float[][] variances, float[][] codes){
    	super(e, clusters, clusterIndex, means, variances, codes);
    }    
      
}



