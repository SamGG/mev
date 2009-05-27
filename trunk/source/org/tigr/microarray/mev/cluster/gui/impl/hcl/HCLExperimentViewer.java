/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: HCLExperimentViewer.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-03-24 15:50:40 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

public class HCLExperimentViewer extends ExperimentViewer {
    
    /**
     * XMLEncoder/Decoder constructor
     * @param experiment
     * @param clusters
     */
    public HCLExperimentViewer(){
    	super();
    }
    /**
     * Constructs a <code>HCLExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public HCLExperimentViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }

	/**
	 * MeV v4.4 and higher state-saving constructor.
	 **/
	public HCLExperimentViewer(Experiment experiment, ClusterWrapper clusters) {
		this(experiment, clusters.getClusters());
	}
}
