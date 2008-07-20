/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: NonparCentroidsViewer.java,v $
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
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;

public class NonparCentroidsViewer extends CentroidsViewer {
    
    private String [] auxTitles;
    private String [][] auxData;
    
    /**
     * Constructs a <code>KMCCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public NonparCentroidsViewer(Experiment experiment, int[][] clusters, String [] auxTitles, String [][] auxData) {
        super(experiment, clusters);
        this.auxTitles = auxTitles;
        this.auxData = auxData;
    }
    /**
     * Used by XMLEncoder/Decoder and IViewerPersistenceDelegate to recreate
     * a KMCCentroidsViewer object from a saved xml file.  
     */
    public NonparCentroidsViewer(CentroidViewer cv, String [] auxTitles, String [][] auxData) {
    	super(cv);
        this.auxTitles = auxTitles;
        this.auxData = auxData;
    }
    
    public Expression getExpression() {
		return new Expression(this, this.getClass(), "new",
				new Object[]{getCentroidViewer(), auxTitles, auxData});
    }
    
    /**
     * Saves all clusters.
     */
    protected void onSaveClusters() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        try {
            ExperimentUtil.saveAllGeneClustersWithAux(frame, getExperiment(), getData(), getClusters(), auxTitles, auxData);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
