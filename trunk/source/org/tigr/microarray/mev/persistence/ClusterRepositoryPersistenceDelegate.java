/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.persistence;

import java.beans.*;

import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;


public class ClusterRepositoryPersistenceDelegate extends PersistenceDelegate {
	public Expression instantiate(Object oldInstance, Encoder encoder) {
		ClusterRepository cr = (ClusterRepository) oldInstance;
		return new Expression((ClusterRepository) oldInstance, oldInstance.getClass(), "new",
				new Object[]{new Boolean(cr.isGeneClusterRepository()), new Integer(cr.getNumberOfElements()),
				new Integer(cr.getClusterSerialCounter()), cr.getElementClusters(), cr.getClusterColors()});
	}
}












































 