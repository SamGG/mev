/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: HCLExperimentCentroidViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-08-22 17:58:50 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;

public class HCLExperimentCentroidViewer extends ExperimentClusterCentroidViewer {
    
    /**
     * @inheritDoc
     * @author eleanorahowe
     * XMLEncoder/XMLDecoder constructor.
     *
     */ 
    public HCLExperimentCentroidViewer(Experiment experiment, int[][] clusters, Integer clusterIndex, float[][] means, float[][] variances, float[][] codes){
    	super(experiment, clusters, clusterIndex, means, variances, codes);
    }
    
    /**
     * Construct a <code>HCLCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public HCLExperimentCentroidViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
 
}

