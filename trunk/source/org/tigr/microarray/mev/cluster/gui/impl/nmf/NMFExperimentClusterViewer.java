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
package org.tigr.microarray.mev.cluster.gui.impl.nmf;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;

public class NMFExperimentClusterViewer extends ExperimentClusterViewer {
        

    
    /**
     * Constructs a <code>KMCExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public NMFExperimentClusterViewer(Experiment experiment, int[][] clusters) {
		super(experiment, clusters);
    }
    public NMFExperimentClusterViewer(Experiment experiment, ClusterWrapper clusters) {
		super(experiment, clusters.getClusters());
    }
    /**
     * Used to reconstruct a KMCExperimentClusterViewer from saved xml data written 
     * by XMLEncoder.  Retained for backwards compatibility with saved analysis files
     * from MeV v4.0-4.3.
     * 
     * @param clusters
     * @param genesOrder
     * @param drawAnnotations
     * @param offset
     * @param header
     * @param exptID
     * @deprecated
     */
    public NMFExperimentClusterViewer(Experiment e, int[][] clusters, int[] genesOrder, Boolean drawAnnotations, Integer offset){//, ExperimentClusterHeader header, Boolean hasCentroid, float[][] centroids, Dimension elementSize, Integer labelIndex) {
    	super(e, clusters, genesOrder, drawAnnotations, offset);

    }
    /**
     * State-saving constructor for MeV v4.4 and higher.
     * @param e
     * @param clusters
     * @param genesOrder
     * @param drawAnnotations
     * @param offset
     */
    public NMFExperimentClusterViewer(Experiment e, ClusterWrapper clusters, ClusterWrapper genesOrder, Boolean drawAnnotations, Integer offset){//, ExperimentClusterHeader header, Boolean hasCentroid, float[][] centroids, Dimension elementSize, Integer labelIndex) {
    	this(e, clusters.getClusters(), genesOrder.getClusters()[0], drawAnnotations, offset);

    }
}
