/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: GSHExperimentClusterViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-08-22 17:57:27 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.gsh;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;

public class GSHExperimentClusterViewer extends ExperimentClusterViewer {

    
    /**
     * Constructs a <code>GSHExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public GSHExperimentClusterViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    public GSHExperimentClusterViewer(Experiment e, int[][] clusters, int[] genesOrder, Boolean drawAnnotations, 
    		Integer offset){
    	super(e, clusters, genesOrder, drawAnnotations, offset);
    }
    
    
}
