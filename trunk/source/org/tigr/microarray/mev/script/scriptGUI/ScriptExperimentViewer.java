/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ScriptExperimentViewer.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:39:55 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.script.scriptGUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

public class ScriptExperimentViewer extends ExperimentViewer {
    
    /**
     * Constructs a <code>KMCExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public ScriptExperimentViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
        ExperimentViewer.PopupListener listener = new ExperimentViewer.PopupListener();
        this.popup = createJPopupMenu(listener);
        getContentComponent().addMouseListener(listener);
        getHeaderComponent().addMouseListener(listener);        
    }
    
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException { }
}
