/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Jun 18, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;

/**
 * @author vu
 */
public class USCExperimentClusterViewer extends ExperimentClusterViewer {
	

	/**
	 * @param experiment
	 * @param clusters
	 * @param centroidName
	 * @param centroids
	 */
	public USCExperimentClusterViewer(Experiment experiment, int[][] clusters, String centroidName, float[][] centroids) {
		super(experiment, clusters, centroidName, centroids);
	}//end constructor
	
	
	public USCExperimentClusterViewer( Experiment experiment, int[][] clusters ) {
		super( experiment, clusters );
	}
    /**
     * State-saving constructor for MeV v4.4 and higher.
     * @param experiment
     * @param clusters
     * @param genesOrder
     * @param drawAnnotations
     * @param offset
     */
    public USCExperimentClusterViewer(Experiment experiment, ClusterWrapper clusters, ClusterWrapper genesOrder, Boolean drawAnnotations, Integer offset){
    	this(experiment, clusters.getClusters(), genesOrder.getClusters()[0], drawAnnotations.booleanValue(), offset.intValue());
    }
    /**
     * State-saving constructor for loading analyses saved by MeV versions v4.0-4.3.
     * @param e
     * @param clusters
     * @param genesOrder
     * @param drawAnnotations
     * @param offset
     */
    public USCExperimentClusterViewer(Experiment e, int[][] clusters, int[] genesOrder, Boolean drawAnnotations, 
    		Integer offset){
    		super(e, clusters, genesOrder, drawAnnotations, offset);
    }
	
}
