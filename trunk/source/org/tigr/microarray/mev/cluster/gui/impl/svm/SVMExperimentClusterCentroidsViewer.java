/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SVMExperimentClusterCentroidsViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-05-02 16:57:36 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.svm;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidsViewer;

public class SVMExperimentClusterCentroidsViewer extends ExperimentClusterCentroidsViewer {
    
    /**
     * Constructs a <code>SVMCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public SVMExperimentClusterCentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    /**
     * @inheritDoc
     */
    public SVMExperimentClusterCentroidsViewer(ExperimentClusterCentroidViewer cv) {
    	super(cv);
    }    

}



