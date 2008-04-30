/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SOTACentroidsViewer.java,v $
 * $Revision: 1.5 $
 * $Date: 2006-03-24 15:51:44 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.sota;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;

public class SOTACentroidsViewer extends CentroidsViewer {
    
    /**
     * Constructs a <code>SOTACentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public SOTACentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
	public SOTACentroidsViewer(CentroidViewer cv) {
		super(cv);
    }
}
