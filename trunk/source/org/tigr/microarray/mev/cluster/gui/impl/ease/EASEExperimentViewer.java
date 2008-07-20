/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: EASEExperimentViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2007-11-29 16:30:11 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.ease;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Expression;


import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

public class EASEExperimentViewer extends ExperimentViewer {
        
    /**
     * Constructs a <code>EASExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public EASEExperimentViewer(Experiment experiment, int[][] clusters) {
		super(experiment, clusters);
    }
    /**
     * @inheritDoc
     */ 
    public EASEExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder, boolean drawAnnotations, ExperimentHeader header, Insets insets) {
    	super(e, clusters, samplesOrder, drawAnnotations, header, insets);
    }
    /**
     * State-saving constructor.
     * @param e
     * @param clusters
     * @param samplesOrder
     * @param drawAnnotations
     */
    public EASEExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder, boolean drawAnnotations) {
    	super(e, clusters, samplesOrder, drawAnnotations, 10);
    }
    
}
