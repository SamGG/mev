/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: QTCExperimentCentroidViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-05-02 16:57:04 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.qtc;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;

public class QTCExperimentCentroidViewer extends ExperimentClusterCentroidViewer {
    
    /**
     * Construct a <code>QTCCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public QTCExperimentCentroidViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    /*
     * This constructor is used by XMLEncoder/Decoder and IViewerPersistenceDelegate
     * to re-create an ExperimentClusterCentroidViewer from a saved xml file
     */
    public QTCExperimentCentroidViewer(Experiment experiment, int[][] clusters, Integer clusterIndex, float[][] means, float[][] variances, float[][] codes){
    	super(experiment, clusters, clusterIndex, means, variances, codes);
    }
    
    
}

