/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: RNCentroidViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-05-02 16:57:04 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.rn;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;

public class RNCentroidViewer extends CentroidViewer {
    
    /**
     * Construct a <code>KMCCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public RNCentroidViewer(Experiment experiment, int[][] clusters) {
		super(experiment, clusters);
    }
    public RNCentroidViewer(Experiment e, int[][] clusters, float[][] variances, float[][] means, float[][] codes) {
    	super(e, clusters, variances, means, codes);
    }
    
}
