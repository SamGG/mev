/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * DAMCentroidViewer.java
 *
 */

package org.tigr.microarray.mev.cluster.gui.impl.dam;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;

public class DAMCentroidViewer extends CentroidViewer {
    
    /** Creates a new instance of DAMCentroidViewer */
    public DAMCentroidViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);   
    }
    /**
     * @inheritDoc
     */
    public DAMCentroidViewer(Experiment e, int[][] clusters, float[][] variances, float[][] means, float[][] codes) {
    	super(e, clusters, variances, means, codes);
    }
    
}
