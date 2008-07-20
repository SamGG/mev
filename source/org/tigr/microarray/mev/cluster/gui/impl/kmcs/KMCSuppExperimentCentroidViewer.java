/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: KMCSuppExperimentCentroidViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.kmcs;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;

public class KMCSuppExperimentCentroidViewer extends ExperimentClusterCentroidViewer {    
    
    /**
     * Construct a <code>KMCSuppCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public KMCSuppExperimentCentroidViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    /**
     * Used to recreate a KMCExperimentCentroidViewer from saved data written by 
     * XMLEncoder.  
     * 
     * @param clusters
     * @param exptID
     * @param clusterIndex
     * @param means
     * @param variances
     * @param codes
     */
    public KMCSuppExperimentCentroidViewer(Experiment e, int[][] clusters, Integer clusterIndex, float[][] means, float[][] variances, float[][] codes) {
    	super(e, clusters, clusterIndex, means, variances, codes);
    }
   
    
    
}

