/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: KNNCCentroidsViewer.java,v $
 * $Revision: 1.5 $
 * $Date: 2006-03-24 15:50:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
/*
 * KNNCCentroidsViewer.java
 *
 * Created on October 3, 2003, 3:07 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.knnc;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;

public class KNNCCentroidsViewer extends CentroidsViewer {
    
    /** Creates a new instance of KNNCCentroidsViewer */
    public KNNCCentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);  
        
    }
	/**
	 * @inheritDoc
	 */
	public KNNCCentroidsViewer(CentroidViewer cv) {
		super(cv);
	}    
    
} 
