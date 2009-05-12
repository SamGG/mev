/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.persistence;

import java.beans.*;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.util.FloatMatrix;



public class ExperimentPersistenceDelegate extends PersistenceDelegate {
	public Expression instantiate(Object oldInstance, Encoder encoder) {
		Experiment e = (Experiment) oldInstance;
		float[] rowindices = new float[e.getRows().length];
		for(int i=0; i<e.getRows().length; i++) {
			rowindices[i] = (float)e.getRows()[i];
		}
		FloatMatrix rows = new FloatMatrix(rowindices, rowindices.length);
		
		float[] colindices = new float[e.getRows().length];
		colindices = new float[e.getColumns().length];
		for(int i=0; i<e.getColumns().length; i++) {
			colindices[i] = (float)e.getColumns()[i];
		}
		FloatMatrix cols = new FloatMatrix(colindices, colindices.length);
		return new Expression((Experiment) oldInstance, oldInstance.getClass(), "new",
				new Object[]{cols, rows, new Integer(e.getId()), e.getMatrix()});
	}
} 