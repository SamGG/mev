package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;

public class GSEACentroidViewer extends CentroidViewer{
	
	public GSEACentroidViewer(Experiment experiment, int[][]clusters){
		super(experiment, clusters);
	}

}
