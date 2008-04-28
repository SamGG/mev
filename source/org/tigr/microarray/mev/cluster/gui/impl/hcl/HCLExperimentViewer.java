/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: HCLExperimentViewer.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-03-24 15:50:40 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

public class HCLExperimentViewer extends ExperimentViewer {
    
    /**
     * XMLEncoder/Decoder constructor
     * @param experiment
     * @param clusters
     */
    public HCLExperimentViewer(){
    	super();
    }
    /**
     * Constructs a <code>HCLExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public HCLExperimentViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    
}
