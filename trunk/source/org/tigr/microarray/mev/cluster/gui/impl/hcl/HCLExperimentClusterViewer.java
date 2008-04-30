/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: HCLExperimentClusterViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-08-22 17:58:50 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;

public class HCLExperimentClusterViewer extends ExperimentClusterViewer {
        

    
    /**
     * Constructs a <code>HCLExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public HCLExperimentClusterViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    /**
     * @inheritDoc
     * @author eleanorahowe
     * 
     * @param clusters
     * @param genesOrder
     * @param drawAnnotations
     * @param offset
     * @param header
     * @param hasCentroid
     * @param centroids
     * @param elementSize
     * @param labelIndex
     * @param exptID
     */
    public HCLExperimentClusterViewer(Experiment e, int[][] clusters, int[] genesOrder, Boolean drawAnnotations, 
    		Integer offset){
    	super(e, clusters, genesOrder, drawAnnotations, offset);
    }
    
}
