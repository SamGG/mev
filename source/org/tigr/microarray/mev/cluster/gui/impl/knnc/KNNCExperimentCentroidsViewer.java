/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: KNNCExperimentCentroidsViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.knnc;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidsViewer;

public class KNNCExperimentCentroidsViewer extends ExperimentClusterCentroidsViewer {
    
    /**
     * Constructs a <code>KMCCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public KNNCExperimentCentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    /**
     * @inheritDoc
     */
    public KNNCExperimentCentroidsViewer(ExperimentClusterCentroidViewer cv) {
    	super(cv);
    }

}


