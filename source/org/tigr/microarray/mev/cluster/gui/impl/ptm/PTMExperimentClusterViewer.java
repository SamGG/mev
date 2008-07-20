/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: PTMExperimentClusterViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.ptm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;


public class PTMExperimentClusterViewer extends ExperimentClusterViewer {
    
    private String[] auxTitles;
    private Object[][] auxData;    
    
    /**
     * Constructs a <code>PTMExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public PTMExperimentClusterViewer(Experiment experiment, int[][] clusters, String centroidName, Vector vector, String[] auxTitles, Object[][] auxData) {
    	super(experiment, clusters, centroidName, vector);
    	this.auxTitles = auxTitles;
    	this.auxData = auxData;         
    	setBackground(Color.red);
    }
    
    /**
     * @inheritDoc
     */
    public PTMExperimentClusterViewer(Experiment e, int[][] clusters, int[] genesOrder, Boolean drawAnnotations, 
    		Integer offset){
    		super(e, clusters, genesOrder, drawAnnotations, offset);
    }
    
    /**
     * Saves clusters.
     */
    protected void onSaveClusters() {
    	Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
    	try {
    		//saveClusters(frame);
    		ExperimentUtil.saveAllExperimentClustersWithAux(frame, getExperiment(), getData(), getClusters(), auxTitles, auxData);
    	} catch (Exception e) {
    		JOptionPane.showMessageDialog(frame, "Can not save clusters!", e.toString(), JOptionPane.ERROR_MESSAGE);
    		e.printStackTrace();
    	}
    }

    /**
     * Save the viewer cluster.
     */
    protected void onSaveCluster() {
    	Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
    	try {
    		ExperimentUtil.saveExperimentClusterWithAux(frame, getExperiment(), getData(), getCluster(), auxTitles, auxData);            
    	} catch (Exception e) {
    		JOptionPane.showMessageDialog(frame, "Can not save cluster.", e.toString(), JOptionPane.ERROR_MESSAGE);
    		e.printStackTrace();
    	}
    }
}
