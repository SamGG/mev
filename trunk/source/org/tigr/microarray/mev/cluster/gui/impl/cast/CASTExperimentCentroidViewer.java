/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: CASTExperimentCentroidViewer.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.cast;
     
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JColorChooser;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;

public class CASTExperimentCentroidViewer extends ExperimentClusterCentroidViewer {
       
    /**
     * Construct a <code>CASTCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public CASTExperimentCentroidViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    /**
     * Used to recreate a KMCExperimentCentroidViewer from saved data written by 
     * XMLEncoder. For MeV v4.4 and higher
     * @param e
     * @param clusters
     * @param clusterIndex
     * @param means
     * @param variances
     * @param codes
     */
    public CASTExperimentCentroidViewer(Experiment e, ClusterWrapper clusters, Integer clusterIndex, float[][] means, float[][] variances, float[][] codes) {
    	this(e, clusters.getClusters(), clusterIndex, means, variances, codes);
    }
    /**
     * Used to load saved analysis files from MeV v4.0-4.3.
     **/
    public CASTExperimentCentroidViewer(Experiment experiment, int[][] clusters, Integer clusterIndex, float[][] means, float[][] variances, float[][] codes){
    	super(experiment, clusters, clusterIndex, means, variances, codes);
    }
    
    /**
     * Sets a public color.
     */
    private void onSetColor() {
	Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
	Color newColor = JColorChooser.showDialog(frame, "Choose color", DEF_CLUSTER_COLOR);
	if (newColor != null) {
	    setClusterColor(newColor);
	}
    }
    
    
}

