package org.tigr.microarray.mev.cluster.gui;

import org.tigr.util.FloatMatrix;

/**
 * This class extends Experiment. It is used to store the modified expression values,
 * obtained after probes have been collapsed to genes
 * 
 * @author Sarita
 *
 */



public class GSEAExperiment extends Experiment{
	
	
	private FloatMatrix matrix; 
	// data indices 
	private int[] columns;




	/**
	 * Constructs an <code>Experiment</code> with specified
	 * matrix of ratio values and columns indices.
	 */
	public GSEAExperiment(FloatMatrix matrix, int[] columns) {
		super(matrix, columns);
	}
	
	
}
