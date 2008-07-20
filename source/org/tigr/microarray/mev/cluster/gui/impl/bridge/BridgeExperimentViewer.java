/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Dec 6, 2005
 */
package org.tigr.microarray.mev.cluster.gui.impl.bridge;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

/**
 * @author iVu
 */
public class BridgeExperimentViewer extends ExperimentViewer {
	//
	
	
	public BridgeExperimentViewer( Experiment exp, int[][] clusters ) {
		super( exp, clusters );
	}
}
