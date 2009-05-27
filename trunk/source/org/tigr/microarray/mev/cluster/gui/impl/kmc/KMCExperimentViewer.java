/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: KMCExperimentViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-08-22 18:01:26 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.kmc;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.microarray.mev.cluster.ClusterWrapper;

public class KMCExperimentViewer extends ExperimentViewer {
    
    /**
     * Reconstitute a saved instance of this class from an XML file. Used 
     * for backwards compatibility with files saved by MeV v4.0-4.3.
     * @param experiment
     * @param clusters
     * @param samplesOrder
     * @param drawAnnotations
     * @param header
     * @param insets
     * @deprecated
     */
    public KMCExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder,
    		Boolean drawAnnotations){
    	super(e, clusters, samplesOrder, drawAnnotations.booleanValue());
 
    } 

    /**
     * State-saving constructor for MEV v4.4 and higher.
     * @param e
     * @param clusters
     * @param samplesOrder
     * @param drawAnnotations
     */
    public KMCExperimentViewer(Experiment e, ClusterWrapper clusters, ClusterWrapper samplesOrder,
    		Boolean drawAnnotations){
    	super(e, clusters.getClusters(), samplesOrder.getClusters()[0], drawAnnotations.booleanValue());
    } 
    
    /**
     * Constructs a <code>KMCExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public KMCExperimentViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
}
