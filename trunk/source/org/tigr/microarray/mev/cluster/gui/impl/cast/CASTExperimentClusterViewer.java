/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: CASTExperimentClusterViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-08-22 17:54:29 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.cast;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;

public class CASTExperimentClusterViewer extends ExperimentClusterViewer {    
    
    
    /**
     * Constructs a <code>CASTExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public CASTExperimentClusterViewer(Experiment experiment, int[][] clusters) {
		super(experiment, clusters);
    }
    
    public CASTExperimentClusterViewer(Experiment e, int[][] clusters, int[] genesOrder, Boolean drawAnnotations, 
    		Integer offset){
    	super(e, clusters, genesOrder, drawAnnotations, offset);
    }
}
