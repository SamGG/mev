/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: HCLCentroidViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-08-22 17:58:50 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;

public class HCLCentroidViewer extends CentroidViewer implements java.io.Serializable {
    public static final long serialVersionUID = 202007010001L;
    
    /**
     * 
     * MeV v4.4 and higher state-saving constructor
     * @param e
     * @param clusters
     * @param variances
     * @param means
     * @param codes
     */
    public HCLCentroidViewer(Experiment e, ClusterWrapper clusters) {
    	this(e, clusters.getClusters());
    }
    /**
     * State-saving constructor for loading saved analyses from MeV v4.0-4.3
     * @param clusters
     * @param variances
     * @param means
     * @param codes
     */
    public HCLCentroidViewer(Experiment e, int[][] clusters, float[][] variances, float[][] means, float[][] codes) {
    	super(e, clusters, variances, means, codes);
    }
    /**
     * Construct a <code>HCLCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public HCLCentroidViewer(Experiment experiment, int[][] clusters) {
		super(experiment, clusters);
    }
    
}
