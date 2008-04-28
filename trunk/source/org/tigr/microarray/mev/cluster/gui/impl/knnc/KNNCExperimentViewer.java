/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: KNNCExperimentViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.knnc;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

public class KNNCExperimentViewer extends ExperimentViewer {
        
    /**
     * Constructs a <code>KMCExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public KNNCExperimentViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    
    /**
     * @inheritDoc
     */ 
    public KNNCExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder, boolean drawAnnotations, ExperimentHeader header, Insets insets) {
    	super(e, clusters, samplesOrder, drawAnnotations, header, insets);
    }
    
}

