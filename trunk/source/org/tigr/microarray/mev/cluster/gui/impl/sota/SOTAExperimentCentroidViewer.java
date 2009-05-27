/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SOTAExperimentCentroidViewer.java,v $
 * $Revision: 1.5 $
 * $Date: 2006-05-02 16:57:35 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.sota;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
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
    /**
     * Used to recreate a KMCExperimentCentroidViewer from saved data written by 
     * XMLEncoder. For MeV v4.4 and higher
     * @param e
     * @param clusters
     * @param clusterIndex
     * @param means
     * @param variances
     * @param codes
     */
    public SOTAExperimentCentroidViewer(Experiment e, ClusterWrapper clusters, Integer clusterIndex, float[][] means, float[][] variances, float[][] codes) {
    	this(e, clusters.getClusters(), clusterIndex, means, variances, codes);
    }
    /**
     * Used to load saved analysis files from MeV v4.0-4.3.
     **/ 
    public SOTAExperimentCentroidViewer(Experiment e, int[][] clusters, Integer clusterIndex, float[][] means, float[][] variances, float[][] codes){
    	super(e, clusters, clusterIndex, means, variances, codes);
    }    

}

