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
