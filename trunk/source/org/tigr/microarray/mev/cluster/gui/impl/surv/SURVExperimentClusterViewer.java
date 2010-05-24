/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: KMCExperimentClusterViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-08-22 18:01:26 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.surv;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;

public class SURVExperimentClusterViewer extends ExperimentClusterViewer {
        

    
    /**
     * Constructs a <code>KMCExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public SURVExperimentClusterViewer(Experiment experiment, int[][] clusters) {
		super(experiment, clusters);
    }
    public SURVExperimentClusterViewer(Experiment experiment, ClusterWrapper clusters) {
		super(experiment, clusters.getClusters());
    }
    /**
     * State-saving constructor for MeV v4.4 and higher.
     * @param e
     * @param clusters
     * @param genesOrder
     * @param drawAnnotations
     * @param offset
     */
    public SURVExperimentClusterViewer(Experiment e, ClusterWrapper clusters, ClusterWrapper genesOrder, Boolean drawAnnotations, Integer offset){//, ExperimentClusterHeader header, Boolean hasCentroid, float[][] centroids, Dimension elementSize, Integer labelIndex) {
    	super(e, clusters.getClusters(), genesOrder.getClusters()[0], drawAnnotations, offset);

    }
}
