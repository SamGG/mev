/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SVMCentroidsViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-03-24 15:51:53 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.svm;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;

public class SVMCentroidsViewer extends CentroidsViewer {
    
    /**
     * Constructs a <code>SVMCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public SVMCentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
	/**
	 * @inheritDoc
	 */
	public SVMCentroidsViewer(CentroidViewer cv) {
		super(cv);
    }
}

