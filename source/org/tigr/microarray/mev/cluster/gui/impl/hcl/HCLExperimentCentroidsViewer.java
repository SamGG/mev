/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: HCLExperimentCentroidsViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-08-22 17:58:50 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidsViewer;

public class HCLExperimentCentroidsViewer extends ExperimentClusterCentroidsViewer {
    
    /**
     * Constructs a <code>HCLCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public HCLExperimentCentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    
    /**
     * @inheritDoc
     * Creates a new HCLExperimentCentroidsViewer.  Used by XMLEncoder/Decoder to restore
     * the state of this class.  parameters match the objects provided by 
     * ExperimentClusterCentroidsViewer.getExpression().
     * @param cv
     * @param exptID
     */
    public HCLExperimentCentroidsViewer(ExperimentClusterCentroidViewer cv) {
    	super(cv);
    }
}

