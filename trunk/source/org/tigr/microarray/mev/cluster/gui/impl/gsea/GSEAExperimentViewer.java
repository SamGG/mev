package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.GSEAExperiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

public class GSEAExperimentViewer extends ExperimentViewer{
	
	private String [] auxTitles;
	private String [][] auxData;
	
	
	 /**
	  * TO DO: Delete this, if not needed
     * Constructs a <code>GSEAExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public GSEAExperimentViewer(GSEAExperiment experiment) {
        super(experiment, null);
    	this.auxData = auxData;
    	this.auxTitles = auxTitles;
    }
   
    /**
     * 
     * @param experiment
     * @param clusters
     * Constructs a GSEA Experiment with given clusters and experiment
     */
    public GSEAExperimentViewer(Experiment experiment, int[][]clusters){
    	super(experiment, clusters);
    }
	
	
	

}
