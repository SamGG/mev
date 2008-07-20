/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SOMExperimentClusterViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-08-22 18:03:06 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.som;

import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;
import org.tigr.util.FloatMatrix;

public class SOMExperimentClusterViewer extends ExperimentClusterViewer {

    
    private JPopupMenu popup;
    
    /**
     * Constructs a <code>SOMExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public SOMExperimentClusterViewer(Experiment experiment, int[][] clusters, String centroidName, FloatMatrix codes) {
	super(experiment, clusters, centroidName, codes.getArrayCopy());
    }
    /**
     * @inheritDoc
     */
    public SOMExperimentClusterViewer(Experiment e, int[][] clusters, int[] genesOrder, Boolean drawAnnotations, 
    		Integer offset){
    	super(e, clusters, genesOrder, drawAnnotations, offset);
    }
    
}
