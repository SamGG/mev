/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SOMCentroidsViewer.java,v $
 * $Revision: 1.5 $
 * $Date: 2006-03-24 15:51:36 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.som;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;

public class SOMCentroidsViewer extends CentroidsViewer {
    
    /**
     * Constructs a <code>SOMCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public SOMCentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    
    /**
     * Constructs a <code>CentroidsViewer</code> for specified experiment
     * and clusters.
     */    
	public SOMCentroidsViewer(CentroidViewer cv) {
		super(cv);
    }
}
