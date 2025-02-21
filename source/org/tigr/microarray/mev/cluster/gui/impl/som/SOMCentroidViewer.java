/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SOMCentroidViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-05-02 16:57:04 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.som;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;

public class SOMCentroidViewer extends CentroidViewer {
      
    /**
     * Construct a <code>KMCCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public SOMCentroidViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    /**
     * State-saving constructor for loading saved analyses for MeV v4.4 and higher.
     * @param e
     * @param clusters
     */
    public SOMCentroidViewer(Experiment e, ClusterWrapper clusters) {
    	this(e, clusters.getClusters());
    }    
    /**
     * State-saving constructor for loading saved analyses from MeV v4.0-4.3
     **/
    public SOMCentroidViewer(Experiment e, int[][] clusters, float[][] variances, float[][] means, float[][] codes) {
    	super(e, clusters, variances, means, codes);
    }
    
}
