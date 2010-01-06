package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

public class GSEAExperimentViewer extends ExperimentViewer {

	

	/**
	 * 
	 * @param experiment
	 * @param clusters
	 *            Constructs a GSEA Experiment with given clusters and
	 *            experiment
	 */
	public GSEAExperimentViewer(Experiment experiment, int[][] clusters) {
		super(experiment, clusters);
	}


	/**
	 * State-saving constructor for MeV v4.4.
	 * 
	 * @param experiment
	 * @param clusters
	 * @param samplesOrder
	 * @param drawAnnotations
	 */
	public GSEAExperimentViewer(Experiment experiment, ClusterWrapper clusters,
			ClusterWrapper samplesOrder, Boolean drawAnnotations) {
		super(experiment, clusters.getClusters(),
				samplesOrder.getClusters()[0], drawAnnotations.booleanValue());
	}


}
