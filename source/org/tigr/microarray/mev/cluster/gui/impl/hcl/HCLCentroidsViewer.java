/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: HCLCentroidsViewer.java,v $
 * $Revision: 1.5 $
 * $Date: 2006-08-22 17:58:50 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;

public class HCLCentroidsViewer extends CentroidsViewer {
    
    /**
     * XMLEncoder/Decoder Constructor.  See superclass.
     * 
     * @param hcv the wrapped HCLCentroidViewer. 
     */
    public HCLCentroidsViewer(HCLCentroidViewer hcv) {
    	super(hcv);
    }
    /**
     * Constructs a <code>HCLCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public HCLCentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
}
