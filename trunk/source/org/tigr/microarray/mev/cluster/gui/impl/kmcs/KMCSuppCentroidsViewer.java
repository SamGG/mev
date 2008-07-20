/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: KMCSuppCentroidsViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-03-24 15:50:54 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.kmcs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;

public class KMCSuppCentroidsViewer extends CentroidsViewer {
    
    /**
     * Constructs a <code>HJCCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public KMCSuppCentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    /**
     * Used by XMLEncoder/Decoder and IViewerPersistenceDelegate to recreate
     * a KMCCentroidsViewer object from a saved xml file.  
     */
    public KMCSuppCentroidsViewer(CentroidViewer cv) {
    	super(cv);
    }
    }
    
    
    
