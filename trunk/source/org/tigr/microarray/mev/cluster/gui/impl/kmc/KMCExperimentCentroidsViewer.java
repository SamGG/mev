/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: KMCExperimentCentroidsViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-08-22 18:01:26 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.kmc;


import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidsViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

public class KMCExperimentCentroidsViewer extends ExperimentClusterCentroidsViewer {
    
    /**
     * Constructs a <code>KMCCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public KMCExperimentCentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    /**
     * Constructor used by IViewerPersistenceDelegate to reconstruct a given
     * KMCExperimentCentroidsViewer from a saved XML file using XMLEncoder/Decoder
     * @param eccv the ExperimentClusterCentroidViewer associated with this object.
     * @param exptID the ID number of the Experiment object associated with this viewer
     */
    public KMCExperimentCentroidsViewer(ExperimentClusterCentroidViewer eccv) {
    	super(eccv);
    }

}

