/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: CASTExperimentClusterViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-08-22 17:54:29 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.cast;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;

public class CASTExperimentClusterViewer extends ExperimentClusterViewer {    
    
    
    /**
     * Constructs a <code>CASTExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public CASTExperimentClusterViewer(Experiment experiment, int[][] clusters) {
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
    public CASTExperimentClusterViewer(Experiment experiment, ClusterWrapper clusters, ClusterWrapper genesOrder, Boolean drawAnnotations, Integer offset){
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
    public CASTExperimentClusterViewer(Experiment e, int[][] clusters, int[] genesOrder, Boolean drawAnnotations, 
    		Integer offset){
    	super(e, clusters, genesOrder, drawAnnotations, offset);
    }
}
