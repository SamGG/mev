/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: CASTCentroidsViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-03-24 15:49:58 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.cast;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;

public class CASTCentroidsViewer extends CentroidsViewer {
    
    /**
     * Constructs a <code>CASTCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public CASTCentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
	public CASTCentroidsViewer(CentroidViewer cv) {
		super(cv);
	}
    
    /**
     * Saves all clusters.
     */
    protected void onSaveClusters() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        try {
            ExperimentUtil.saveExperiment(frame, getExperiment(), getData(), getClusters());
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
