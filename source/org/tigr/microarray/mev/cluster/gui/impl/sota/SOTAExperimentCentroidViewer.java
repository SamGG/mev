/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SOTAExperimentCentroidViewer.java,v $
 * $Revision: 1.5 $
 * $Date: 2006-05-02 16:57:35 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.sota;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;

public class SOTAExperimentCentroidViewer extends ExperimentClusterCentroidViewer {
    
    /**
     * Construct a <code>SOTACentroidViewer</code> with specified experiment
     * and clusters.
     */
    public SOTAExperimentCentroidViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    /*
     * This constructor is used by XMLEncoder/Decoder and IViewerPersistenceDelegate
     * to re-create an ExperimentClusterCentroidViewer from a saved xml file
     */
    public SOTAExperimentCentroidViewer(Experiment e, int[][] clusters, Integer clusterIndex, float[][] means, float[][] variances, float[][] codes){
    	super(e, clusters, clusterIndex, means, variances, codes);
    }    

}

