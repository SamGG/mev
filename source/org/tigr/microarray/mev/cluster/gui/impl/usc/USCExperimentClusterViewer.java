/*
 * Created on Jun 18, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;

/**
 * @author vu
 */
public class USCExperimentClusterViewer extends ExperimentClusterViewer {
	

	/**
	 * @param experiment
	 * @param clusters
	 * @param centroidName
	 * @param centroids
	 */
	public USCExperimentClusterViewer(Experiment experiment, int[][] clusters, String centroidName, float[][] centroids) {
		super(experiment, clusters, centroidName, centroids);
	}//end constructor
	
	
	public USCExperimentClusterViewer( Experiment experiment, int[][] clusters ) {
		super( experiment, clusters );
	}
    /**
     * @inheritDoc
     */
    public USCExperimentClusterViewer(Experiment e, int[][] clusters, int[] genesOrder, Boolean drawAnnotations, 
    		Integer offset){
    		super(e, clusters, genesOrder, drawAnnotations, offset);
    }
	
}//end class
