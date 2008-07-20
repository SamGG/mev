/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: QTCCentroidViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-05-02 16:57:04 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.qtc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;


public class QTCCentroidViewer extends CentroidViewer {
    
    /**
     * Construct a <code>QTCCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public QTCCentroidViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    /**
     * This constructor is used by XMLEncoder/Decoder to store and retreive a 
     * CentroidViewer object to/from and xml file.  This constructor must 
     * always exist, with its current method signature, for purposes of 
     * backwards-compatability in loading old save-files from MeV versions 
     * of v3.2 and later.  
     * 
     * @param clusters
     * @param variances
     * @param means
     * @param codes
     * @param id
     */
    public QTCCentroidViewer(Experiment e, int[][] clusters, float[][] variances, float[][] means, float[][] codes) {
    	super(e, clusters, variances, means, codes);
    }
    
}
