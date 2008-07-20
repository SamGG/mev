/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: RelNetExperimentClusterViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-08-22 18:02:32 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.rn;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;

public class RelNetExperimentClusterViewer extends ExperimentClusterViewer {


    /**
     * Constructs a <code>RelNetExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public RelNetExperimentClusterViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    /**
     * State-saving constructor
     * @param clusters
     * @param genesOrder
     * @param drawAnnotations
     * @param offset
     * @param header
     * @param hasCentroid
     * @param centroids
     * @param elementSize
     * @param labelIndex
     * @param exptID
     */
    public RelNetExperimentClusterViewer(Experiment e, int[][] clusters, int[] genesOrder, Boolean drawAnnotations, 
    		Integer offset){
    	super(e, clusters, genesOrder, drawAnnotations, offset);
    }
    

}
