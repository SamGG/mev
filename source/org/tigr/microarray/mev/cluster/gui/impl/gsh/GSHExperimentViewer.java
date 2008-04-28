/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: GSHExperimentViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.gsh;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

public class GSHExperimentViewer extends ExperimentViewer {
    
    /**
     * Constructs a <code>KMCExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public GSHExperimentViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    /**
     * This constructor is used to re-create an ExperimentViewer from information
     * stored in a saved analysis file by XMLEncoder.  
     * 
     * @param experiment
     * @param clusters
     * @param samplesOrder
     * @param drawAnnotations
     * @param header
     * @param insets
     */
    public GSHExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder, boolean drawAnnotations) {
    	super(e, clusters, samplesOrder, drawAnnotations, new ExperimentHeader(e, clusters), new Insets(0, 10, 0,0));
    }
 
}
