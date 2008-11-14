package org.tigr.microarray.mev.cluster.gui.impl.GSEA;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.GSEAExperiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

public class GSEAExperimentViewer extends ExperimentViewer{
	
	private String [] auxTitles;
	private String [][] auxData;

	
	 /**
     * Constructs a <code>GSEAExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public GSEAExperimentViewer(GSEAExperiment experiment) {
        super(experiment, null);
    	this.auxData = auxData;
    	this.auxTitles = auxTitles;
    }
   
	
	
	
	

}
