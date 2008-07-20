/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: NonparExperimentViewer.java,v $
 * $Revision: 1.2 $
 * $Date: 2007-12-17 22:10:37 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.nonpar;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Expression;

import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
//import org.tigr.microarray.mev.cluster.gui.impl.kmc.KMCExperimentViewer.Listener;

public class NonparExperimentViewer extends ExperimentViewer {

	private String [] auxTitles;
	private String [][] auxData;
		
    /**
     * Reconstitute a saved instance of this class from an XML file.
     * 
     * TODO 
     * Save clusters as a long, tab-delimited string rather than an int[][].  Same for samplesOrder.
     * 
     * @param experiment
     * @param clusters
     * @param samplesOrder
     * @param drawAnnotations
     * @param header
     * @param insets
     * @param auxData results in String form //supports cluster saves
     * @param auxTitles String array of titles //supports cluster saves
     */
    public NonparExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder,
    		Boolean drawAnnotations, String [] auxTitles, String [][] auxData){//, ExperimentHeader header, Insets insets) {
    	super(e, clusters, samplesOrder, drawAnnotations.booleanValue());//, header, insets);
    	this.auxData = auxData;
    	this.auxTitles = auxTitles;
    }

    public Expression getExpression() {
    	Expression e = super.getExpression();
    	Object[] args = e.getArguments();
    	Object[] temp = new Object[args.length+2];
    	int i = 0;
    	for(; i<args.length; i++) {
    		temp[i] = args[i];
    	}
    	temp[i] = auxTitles;
    	temp[i+1] = auxData;
    	return new Expression(this, this.getClass(), "new", temp);
    }
    
    /**
     * Constructs a <code>KMCExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public NonparExperimentViewer(Experiment experiment, int[][] clusters, String [] auxTitles, String [][] auxData) {
        super(experiment, clusters);
    	this.auxData = auxData;
    	this.auxTitles = auxTitles;
    }
    
    /**
     * Saves all the clusters.
     */
    public void saveClusters(Frame frame) throws Exception {
        frame = frame == null ? JOptionPane.getFrameForComponent(this) : frame;
        ExperimentUtil.saveAllGeneClustersWithAux(frame, getExperiment(), getData(), getClusters(), auxTitles, auxData);
    }
    
    /**
     * Saves current cluster.
     */
    public void saveCluster(Frame frame) throws Exception {
        frame = frame == null ? JOptionPane.getFrameForComponent(this) : frame;
        ExperimentUtil.saveGeneClusterWithAux(frame, getExperiment(), getData(), getCluster(), auxTitles, auxData);
    }
    
    
}
