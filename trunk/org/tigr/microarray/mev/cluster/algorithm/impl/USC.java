/*
 * Created on Jun 4, 2004
 */
package org.tigr.microarray.mev.cluster.algorithm.impl;

import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.util.FloatMatrix;

/**
 * @author vu
 */
public class USC extends AbstractAlgorithm {

	
	public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
		FloatMatrix expMatrix = data.getMatrix("experiment");
		
		return null;
	}


	public void abort() {
		//
	}

}
