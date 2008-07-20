/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ScriptExperimentCentroidsViewer.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:39:55 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.script.scriptGUI;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidsViewer;

public class ScriptExperimentCentroidsViewer extends ExperimentClusterCentroidsViewer {
    
    /**
     * Constructs a <code>KMCCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public ScriptExperimentCentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    
    
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException { }
    
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
        ExperimentClusterCentroidsViewer.PopupListener listener = new ExperimentClusterCentroidsViewer.PopupListener();
        this.popup = createJPopupMenu(listener);
        getContentComponent().addMouseListener(listener);
    }

}

