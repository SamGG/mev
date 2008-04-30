/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: EASECentroidViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-08-22 17:56:41 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.ease;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;

public class EASECentroidViewer extends CentroidViewer {
    
    /**
     * Construct a <code>EASECentroidViewer</code> with specified experiment
     * and clusters.
     */
    public EASECentroidViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    
    /**
     * @inheritDoc
     */
    public EASECentroidViewer(Experiment e, int[][] clusters, float[][] variances, float[][] means, float[][] codes) {
    	super(e, clusters, variances, means, codes);
    }
    
}
