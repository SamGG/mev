/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: GSHCentroidsViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-03-24 15:50:35 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.gsh;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;

public class GSHCentroidsViewer extends CentroidsViewer {
    
    /**
     * Constructs a <code>GSHCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public GSHCentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    /**
     * Constructs a <code>CentroidsViewer</code> for specified experiment
     * and clusters.
     */    
	public GSHCentroidsViewer(CentroidViewer cv) {
		super(cv);
    }
}
