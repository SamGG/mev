/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: NonparCentroidViewer.java,v $
 * $Revision: 1.2 $
 * $Date: 2007-12-17 22:10:37 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.nonpar;

import java.awt.Frame;
import java.beans.Expression;

import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;

public class NonparCentroidViewer extends CentroidViewer {
    
    private String [] auxTitles;
    private String [][] auxData;
    
    /**
     * Construct a <code>KMCCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public NonparCentroidViewer(Experiment experiment, int[][] clusters, String [] auxTitles, String [][] auxData) {
		super(experiment, clusters);
	    this.auxTitles = auxTitles;
	    this.auxData = auxData;
    }
    /**
     * 
     * MeV v4.4 and higher state-saving constructor
     * @param e
     * @param clusters
     * @param variances
     * @param means
     * @param codes
     */
    public NonparCentroidViewer(Experiment e, ClusterWrapper clusters, float[][] variances, float[][] means, float[][] codes, String [] auxTitles, String [][] auxData) {
    	this(e, clusters.getClusters(), variances, means, codes, auxTitles, auxData);
    }
    /**
     * State-saving constructor for loading saved analyses from MeV v4.0-4.3
     * @param clusters
     * @param variances
     * @param means
     * @param codes
     */
    public NonparCentroidViewer(Experiment e, int[][] clusters, float[][] variances, float[][] means, float[][] codes, String [] auxTitles, String [][] auxData) {
    	super(e, clusters, variances, means, codes);
        this.auxTitles = auxTitles;
        this.auxData = auxData;
    }
    public Expression getExpression() {
    	return new Expression(this, this.getClass(), "new",
				new Object[]{experiment, clusters, variances, means, codes, auxTitles, auxData});
    }
       
    /**
     * Saves all clusters.
     */
    protected void onSaveClusters() {
	Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
	try {
	    ExperimentUtil.saveAllGeneClustersWithAux(frame, getExperiment(), getData(), getClusters(), auxTitles, auxData);
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
	    ExperimentUtil.saveGeneClusterWithAux(frame, getExperiment(), getData(), getCluster(), auxTitles, auxData);
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
	    e.printStackTrace();
	}
    }
    
}
