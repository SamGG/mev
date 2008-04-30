/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: GSHExperimentCentroidsViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-08-22 17:57:27 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.gsh;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidsViewer;

public class GSHExperimentCentroidsViewer extends ExperimentClusterCentroidsViewer {
    
    /**
     * Constructs a <code>HJCCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public GSHExperimentCentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    /**
     * Constructs a <code>CentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public GSHExperimentCentroidsViewer(ExperimentClusterCentroidViewer cv) {
    	super(cv);
    }
    

}

