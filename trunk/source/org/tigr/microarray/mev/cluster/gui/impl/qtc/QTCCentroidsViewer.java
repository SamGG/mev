/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*//*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*//*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: QTCCentroidsViewer.java,v $
 * $Revision: 1.5 $
 * $Date: 2006-03-24 15:51:22 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.qtc;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;

public class QTCCentroidsViewer extends CentroidsViewer {
    
    /**
     * Constructs a <code>QTCCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public QTCCentroidsViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    /**
     * Constructs a <code>CentroidsViewer</code> for specified experiment
     * and clusters.
     */    
	public QTCCentroidsViewer(CentroidViewer cv) {
		super (cv);
    }
}
