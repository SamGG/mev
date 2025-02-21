/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: KMCSuppExperimentClusterViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.kmcs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;

public class KMCSuppExperimentClusterViewer extends ExperimentClusterViewer {
    
    
    /**
     * Constructs a <code>KMCSuppExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public KMCSuppExperimentClusterViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    
    /**
     * Used to reconstruct a KMCExperimentClusterViewer from saved xml data written 
     * by XMLEncoder.  
     * 
     * @param clusters
     * @param genesOrder
     * @param drawAnnotations
     * @param offset
     * @param header
     * @param exptID
     */
    public KMCSuppExperimentClusterViewer(Experiment e, int[][] clusters, int[] genesOrder, Boolean drawAnnotations, Integer offset){//, ExperimentClusterHeader header, Boolean hasCentroid, float[][] centroids, Dimension elementSize, Integer labelIndex) {
    	super(e, clusters, genesOrder, drawAnnotations, offset);
    }
    /**
     * State-saving constructor for MeV v4.4 and higher.
     * @param e
     * @param clusters
     * @param genesOrder
     * @param drawAnnotations
     * @param offset
     */
    public KMCSuppExperimentClusterViewer(Experiment e, ClusterWrapper clusters, ClusterWrapper genesOrder, Boolean drawAnnotations, Integer offset){//, ExperimentClusterHeader header, Boolean hasCentroid, float[][] centroids, Dimension elementSize, Integer labelIndex) {
    	this(e, clusters.getClusters(), genesOrder.getClusters()[0], drawAnnotations, offset);

    }
    
    

}
