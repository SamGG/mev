/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: KMCSuppExperimentCentroidsViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.kmcs;


import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidsViewer;

public class KMCSuppExperimentCentroidsViewer extends ExperimentClusterCentroidsViewer {
    
    /**
     * Constructs a <code>KMCSuppCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public KMCSuppExperimentCentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    /**
     * Constructor used by IViewerPersistenceDelegate to reconstruct a given
     * KMCSuppExperimentCentroidsViewer from a saved XML file using XMLEncoder/Decoder
     * @param eccv the ExperimentClusterCentroidViewer associated with this object.
     * @param exptID the ID number of the Experiment object associated with this viewer
     */
    public KMCSuppExperimentCentroidsViewer(ExperimentClusterCentroidViewer eccv) {
    	super(eccv);
    }
 
}

