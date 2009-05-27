/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: RelNetExperimentViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-05-02 16:57:04 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.rn;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

public class RelNetExperimentViewer extends ExperimentViewer {

    private static final String SET_COLOR_CMD = "set-color-cmd";
    private static final String SET_DEF_COLOR_CMD = "set-def-color-cmd";
    private static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
    private static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";

    /**
     * Constructs a <code>RelNetExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public RelNetExperimentViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    /**
     * This constructor is used to re-create an ExperimentViewer from information
     * stored in a saved analysis file by XMLEncoder.  
     * 
     * @param experiment
     * @param clusters
     * @param samplesOrder
     * @param drawAnnotations
     * @param header
     * @param insets
     */
    public RelNetExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder, boolean drawAnnotations, ExperimentHeader header, Insets insets) {
    	super(e, clusters, samplesOrder, drawAnnotations, header, insets);
    }

    /**
     * State-saving constructor for MeV v4.4.
     **/
    public RelNetExperimentViewer(Experiment e, ClusterWrapper clusters, ClusterWrapper samplesOrder, boolean drawAnnotations) {
    	super(e, clusters.getClusters(), samplesOrder.getClusters()[0], drawAnnotations, new ExperimentHeader(e, clusters.getClusters()), new Insets(0,10,0,0));
    }
    public RelNetExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder, boolean drawAnnotations) {
    	super(e, clusters, samplesOrder, drawAnnotations, new ExperimentHeader(e, clusters), new Insets(0,10,0,0));
    }

}
