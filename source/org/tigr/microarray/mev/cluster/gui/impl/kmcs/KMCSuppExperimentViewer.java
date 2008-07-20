/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: KMCSuppExperimentViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.kmcs;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;


public class KMCSuppExperimentViewer extends ExperimentViewer {
    
    /**
     * Reconstitute a saved instance of this class from an XML file.
     * 
     * 
     * @param experiment
     * @param clusters
     * @param samplesOrder
     * @param drawAnnotations
     * @param header
     * @param insets
     */
    public KMCSuppExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder,
    		Boolean drawAnnotations, ExperimentHeader header, Insets insets) {
    	super(e, clusters, samplesOrder, drawAnnotations.booleanValue(), header, insets);
    } 
    
    /**
     * Constructs a <code>HJCExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public KMCSuppExperimentViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    /**
     * Constructs a <code>HJCExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public KMCSuppExperimentViewer(Experiment experiment, int[][] clusters, int[] samplesOrder, Boolean drawAnnotations) {
	super(experiment, clusters, samplesOrder, drawAnnotations);
    }
    
}
