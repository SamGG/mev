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
package org.tigr.microarray.mev.cluster.gui.impl.kmc;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;

public class KMCExperimentClusterViewer extends ExperimentClusterViewer {
        

    
    /**
     * Constructs a <code>KMCExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public KMCExperimentClusterViewer(Experiment experiment, int[][] clusters) {
		super(experiment, clusters);
    }
    /**
     * Used to reconstruct a KMCExperimentClusterViewer from saved xml data written 
     * by XMLEncoder.  
     * 
     * @param clusters
     * @param genesOrder
     * @param drawAnnotations
     * @param offset
     * @param header
     * @param exptID
     */
    public KMCExperimentClusterViewer(Experiment e, int[][] clusters, int[] genesOrder, Boolean drawAnnotations, Integer offset){//, ExperimentClusterHeader header, Boolean hasCentroid, float[][] centroids, Dimension elementSize, Integer labelIndex) {
    	super(e, clusters, genesOrder, drawAnnotations, offset);//, header, hasCentroid, centroids, elementSize, labelIndex);

    }

}
