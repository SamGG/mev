/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: KMCCentroidViewer.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-08-22 18:01:26 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.kmc;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.util.FloatMatrix;

public class KMCCentroidViewer extends CentroidViewer {
    
    /**
     * Construct a <code>KMCCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public KMCCentroidViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    /**
     * Used by XMLDecoder when reconstructing this class from saved file. For
     * files saved by MeV v4.0 - 4.3.
     * 
     * @param experiment
     * @param clusters
     * @param variances
     */
    public KMCCentroidViewer(Experiment e, int[][] clusters, float[][] variances, float[][] means, float[][] codes) {
    	super(e, clusters, variances, means, codes);
    }
    
    /**
     * State-saving constructor for MeV versions 4.4 and up
     * @param experiment
     * @param clusters
     * @param variances
     * @param means
     * @param codes
     */
    public KMCCentroidViewer(Experiment experiment, ClusterWrapper clusters) {
    	super(experiment, clusters);
    }


    
}
