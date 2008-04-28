/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: KMCExperimentViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-08-22 18:01:26 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.kmc;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

public class KMCExperimentViewer extends ExperimentViewer {
    
    /**
     * Reconstitute a saved instance of this class from an XML file.
     * 
     * TODO 
     * Save clusters as a long, tab-delimited string rather than an int[][].  Same for samplesOrder.
     * 
     * @param experiment
     * @param clusters
     * @param samplesOrder
     * @param drawAnnotations
     * @param header
     * @param insets
     */
    public KMCExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder,
    		Boolean drawAnnotations){
    	super(e, clusters, samplesOrder, drawAnnotations.booleanValue());
        
    } 

    /**
     * Constructs a <code>KMCExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public KMCExperimentViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
}
