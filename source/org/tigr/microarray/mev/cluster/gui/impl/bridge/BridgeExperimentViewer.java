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

import java.beans.Expression;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
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
    /**
     * State-saving constructor for MeV v4.4.
     * @param experiment
     * @param clusters
     * @param samplesOrder
     * @param drawAnnotations
     */
    public BridgeExperimentViewer(Experiment experiment, ClusterWrapper clusters) {
    	this(experiment, clusters.getClusters());
    }
    
    @Override
    public Expression getExpression() {
    	return new Expression(this, this.getClass(), "new", 
        		new Object[]{getExperiment(), ClusterWrapper.wrapClusters(getClusters())});
    }
}
