/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: CASTExperimentViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.cast;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;


public class CASTExperimentViewer extends ExperimentViewer {    
    
    /**
     * Constructs a <code>CASTExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public CASTExperimentViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    public CASTExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder, boolean drawAnnotations, ExperimentHeader header, Insets insets) {
    	super(e, clusters, samplesOrder, drawAnnotations, header, insets);
    }
    public CASTExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder, boolean drawAnnotations) {
    	super(e, clusters, samplesOrder, drawAnnotations, new ExperimentHeader(e, clusters), new Insets(0, 10, 0, 0));
    }
    
}
