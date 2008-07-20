/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: KMCCentroidsViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-03-24 15:50:49 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.kmc;



import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;

public class KMCCentroidsViewer extends CentroidsViewer {
    
    /**
     * Constructs a <code>KMCCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public KMCCentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    /**
     * Used by XMLEncoder/Decoder and IViewerPersistenceDelegate to recreate
     * a KMCCentroidsViewer object from a saved xml file.  
     */
    public KMCCentroidsViewer(CentroidViewer cv) {
    	super(cv);
    }
}
