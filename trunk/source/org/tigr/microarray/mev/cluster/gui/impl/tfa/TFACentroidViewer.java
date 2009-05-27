/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * TFACentroidViewer.java
 *
 * Created on February 27, 2004, 11:43 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.tfa;

import java.awt.Frame;
import java.beans.Expression;

import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;

/**
 *
 * @author  nbhagaba
 */
public class TFACentroidViewer extends CentroidViewer {
    
    String[] auxTitles;
    Object[][] auxData;    
    
    /** Creates a new instance of TFACentroidViewer */
    public TFACentroidViewer(Experiment experiment, int[][] clusters, String[] auxTitles, Object[][] auxData) {
	super(experiment, clusters);
    	initialize(auxTitles, auxData);
    }
    /**
     * State-saving constructor for loading saved analyses from MeV v4.4 and higher
     * @param e
     * @param clusters
     * @param variances
     * @param means
     * @param codes
     * @param templateVector
     * @param auxTitles
     * @param auxData
     */
    public TFACentroidViewer(Experiment e, ClusterWrapper clusters, String[] auxTitles, Object[][] auxData) {
    	this(e, clusters.getClusters(), auxTitles, auxData);
    }    
    /**
     * State-saving constructor for loading saved analyses from MeV v4.0-4.3
     **/
    /**
     * @inheritDoc
     */
    public TFACentroidViewer(Experiment e, int[][] clusters, float[][] variances, float[][] means, float[][] codes, String[] auxTitles, Object[][] auxData) {
    	super(e, clusters, variances, means, codes);
    	initialize(auxTitles, auxData);
    }
	public Expression getExpression(){
		Object[] parentConstructorArgs = super.getExpression().getArguments();
		return new Expression(this, this.getClass(), "new", 
				new Object[]{parentConstructorArgs[0], parentConstructorArgs[1], 
				auxTitles, auxData});
	}
    public void initialize(String[] auxTitles, Object[][] auxData){	        
        this.auxTitles = auxTitles;
        this.auxData = auxData;             
    }
    
    /**
     * Saves all clusters.
     */
    protected void onSaveClusters() {
	Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
	try {
	    //ExperimentUtil.saveExperiment(frame, getExperiment(), getData(), getClusters());
            ExperimentUtil.saveAllGeneClustersWithAux(frame, this.getExperiment(), this.getData(), this.getClusters(), auxTitles, auxData);           
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
	    //ExperimentUtil.saveExperiment(frame, getExperiment(), getData(), getCluster());
            ExperimentUtil.saveGeneClusterWithAux(frame, this.getExperiment(), this.getData(), this.getCluster(), auxTitles, auxData);            
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
	    e.printStackTrace();
	}
    }    
    
}
