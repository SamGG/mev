package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
/**
 * Creates standard MeV centroid viewer
 * @author sarita
 *
 */
public class GSEACentroidViewer extends CentroidViewer{
	
	public GSEACentroidViewer(Experiment experiment, int[][]clusters){
		super(experiment, clusters);
	}
    /**
     * 
     * MeV v4.4 state-saving constructor
     * @param e
     * @param clusters
     * @param variances
     * @param means
     * @param codes
     */
    public GSEACentroidViewer(Experiment e, ClusterWrapper clusters) {
    	this(e, clusters.getClusters());
    }
    
}
