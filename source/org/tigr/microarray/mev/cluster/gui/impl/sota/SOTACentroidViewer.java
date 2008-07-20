/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SOTACentroidViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-05-02 16:57:35 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.sota;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;

public class SOTACentroidViewer extends CentroidViewer {
        
    /**
     * Construct a <code>SOTACentroidViewer</code> with specified experiment
     * and clusters.
     */
    public SOTACentroidViewer(Experiment experiment, int[][] clusters) {
    	super(experiment, clusters);
    }
    /**
     * This constructor is used by XMLEncoder/Decoder to store and retreive a 
     * CentroidViewer object to/from and xml file.  This constructor must 
     * always exist, with its current method signature, for purposes of 
     * backwards-compatability in loading old save-files from MeV versions 
     * of v3.2 and later.  
     * 
     * @param clusters
     * @param variances
     * @param means
     * @param codes
     * @param id
     */
    public SOTACentroidViewer(Experiment e, int[][] clusters, float[][] variances, float[][] means, float[][] codes) {
    	super(e, clusters, variances, means, codes);
    }
    
}
