package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;
/**
 * Creates the standard Mev centroid viewer
 * @author sarita
 *
 */
public class GSEACentroidsViewer extends CentroidsViewer {

	public GSEACentroidsViewer(Experiment experiment, int[][]clusters){
		super(experiment, clusters);
	}
	
	
}
