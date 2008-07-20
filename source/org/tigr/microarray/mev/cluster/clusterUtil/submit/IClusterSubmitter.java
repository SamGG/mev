/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * IClusterSubmitter.java
 *
 * Created on July 2, 2004, 12:05 PM
 */

package org.tigr.microarray.mev.cluster.clusterUtil.submit;

import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.gui.IFramework;


/**
 *
 * @author  braisted
 */
public interface IClusterSubmitter {
    
    public boolean submit(Cluster cluster, IFramework framework, RepositoryConfigParser parser);

}
