/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SVMExperimentClusterViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-05-02 16:57:36 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.svm;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;

public class SVMExperimentClusterViewer extends ExperimentClusterViewer {
    
    
    /**
     * Constructs a <code>SVMExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public SVMExperimentClusterViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    /**
     * State-saving constructor for MeV v4.4 and higher.
     * @param experiment
     * @param clusters
     * @param genesOrder
     * @param drawAnnotations
     * @param offset
     */
    public SVMExperimentClusterViewer(Experiment experiment, ClusterWrapper clusters, ClusterWrapper genesOrder, Boolean drawAnnotations, Integer offset){
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
    public SVMExperimentClusterViewer(Experiment e, int[][] clusters, int[] genesOrder, Boolean drawAnnotations, 
    		Integer offset){
    		super(e, clusters, genesOrder, drawAnnotations, offset);
    }
    
}


