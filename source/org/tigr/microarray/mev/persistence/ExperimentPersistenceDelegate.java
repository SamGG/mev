/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.persistence;

import java.beans.*;

import org.tigr.microarray.mev.cluster.gui.Experiment;



public class ExperimentPersistenceDelegate extends PersistenceDelegate {
	public Expression instantiate(Object oldInstance, Encoder encoder) {
		Experiment e = (Experiment) oldInstance;
		return new Expression((Experiment) oldInstance, oldInstance.getClass(), "new",
				new Object[]{e.getColumns(), e.getRows(), new Integer(e.getId()), e.getMatrix()});
	}
}












































 