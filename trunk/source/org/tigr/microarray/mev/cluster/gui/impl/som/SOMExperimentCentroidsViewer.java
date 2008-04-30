/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SOMExperimentCentroidsViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-05-02 16:57:04 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.som;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidsViewer;

public class SOMExperimentCentroidsViewer extends ExperimentClusterCentroidsViewer {
    
    /**
     * Constructs a <code>SOMCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public SOMExperimentCentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    /**
     * @inheritDoc
     */
    public SOMExperimentCentroidsViewer(ExperimentClusterCentroidViewer cv) {
    	super(cv);
    }    

}

