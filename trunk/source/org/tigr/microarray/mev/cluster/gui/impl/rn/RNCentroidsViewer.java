/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: RNCentroidsViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-05-02 16:57:04 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.rn;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;

public class RNCentroidsViewer extends CentroidsViewer {
       
    /**
     * Constructs a <code>KMCCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public RNCentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    
    
    public RNCentroidsViewer(CentroidViewer cv) {
   		super(cv);
    }
}
