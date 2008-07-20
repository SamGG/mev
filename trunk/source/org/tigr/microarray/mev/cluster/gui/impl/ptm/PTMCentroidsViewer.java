/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: PTMCentroidsViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-03-24 15:51:08 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.ptm;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;

public class PTMCentroidsViewer extends CentroidsViewer { //PTMSubCentroidsViewer {
    
    private String[] auxTitles;
    private Object[][] auxData;    

    /**
     * Constructs a <code>PTMCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public PTMCentroidsViewer(Experiment experiment, int[][] clusters, Vector templateVector, String[] auxTitles, Object[][] auxData) {
	super(experiment, clusters);
        this.auxTitles = auxTitles;
        this.auxData = auxData;       
    }
    /**
     * Constructs a <code>CentroidsViewer</code> for specified experiment
     * and clusters.
     */    
	public PTMCentroidsViewer(CentroidViewer cv) {
		super(cv);
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
    
            private void setAllYRanges(int yRangeOption){
            int numClusters = getClusters().length;
            for(int i = 0; i < numClusters; i++){
                centroidViewer.setClusterIndex(i);
                centroidViewer.setYRangeOption(yRangeOption);
            }
        }
    
}
