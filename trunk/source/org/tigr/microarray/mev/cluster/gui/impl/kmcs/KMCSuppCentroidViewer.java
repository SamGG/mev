/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: KMCSuppCentroidViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.kmcs;



import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;


public class KMCSuppCentroidViewer extends CentroidViewer {
    
    /**
     * Construct a <code>HJCCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public KMCSuppCentroidViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    /**
     * Used by XMLDecoder when reconstructing this class from saved file.  Signature
     * specified by CentroidViewer.getPersistenceDelegateArgs().
     * 
     * @param experiment
     * @param clusters
     * @param variances
     */
    public KMCSuppCentroidViewer(Experiment e, int[][] clusters, float[][] variances, float[][] means, float[][] codes) {
    	super(e, clusters, variances, means, codes);
    }
    
}
