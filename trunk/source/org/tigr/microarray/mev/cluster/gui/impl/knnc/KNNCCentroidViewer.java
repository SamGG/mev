/*
 * KNNCCentroidViewer.java
 *
 * Created on October 3, 2003, 3:01 PM
 */
/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: KNNCCentroidViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.knnc;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;

/**
 *
 * @author  nbhagaba
 */
public class KNNCCentroidViewer extends CentroidViewer {
    
    /** Creates a new instance of KNNCCentroidViewer */
    public KNNCCentroidViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);       
    }
    /**
     * @inheritDoc
     */
    public KNNCCentroidViewer(Experiment e, int[][] clusters, float[][] variances, float[][] means, float[][] codes) {
    	super(e, clusters, variances, means, codes);
    }    
    
}
