/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * DAMExperimentViewer.java
 * 
 */

package org.tigr.microarray.mev.cluster.gui.impl.dam;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Expression;


import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

public class DAMExperimentViewer extends ExperimentViewer {
        
    /**
     * Constructs a <code>KMCExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public DAMExperimentViewer(Experiment experiment, int[][] clusters) {
		super(experiment, clusters);
    }
    
    /**
     * @inheritDoc
     */ 
    public DAMExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder, boolean drawAnnotations, ExperimentHeader header, Insets insets) {
    	super(e, clusters, samplesOrder, drawAnnotations, header, insets);
    }
    /**
     * Persistence constructor.
     * @param e
     * @param clusters
     * @param samplesOrder
     * @param drawAnnotations
     */
    public DAMExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder, boolean drawAnnotations) {
    	super(e, clusters, samplesOrder, drawAnnotations, new ExperimentHeader(e, clusters), new Insets(0, 10, 0, 0));
    }
    
}

