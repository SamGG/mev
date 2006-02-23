/*

Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).

All rights reserved.

*/

/*

 * $RCSfile: ViewerAdapter.java,v $

 * $Revision: 1.8 $

 * $Date: 2006-02-23 20:59:49 $

 * $Author: caliente $

 * $State: Exp $

 */

package org.tigr.microarray.mev.cluster.gui.impl;



import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;



public class ViewerAdapter implements IViewer {

    public BufferedImage getImage() {return null;}

    public void onSelected(IFramework framework) {}

    public void onDataChanged(IData data) {}

    public void onMenuChanged(IDisplayMenu menu) {}

    public void onDeselected() {}

    public void onClosed() {}

    public JComponent getContentComponent() {return null;}

    public JComponent getHeaderComponent() {return null;}

    

    /** Returns a component to be inserted into the scroll pane row header

     */

    public JComponent getRowHeaderComponent() {return null;}

    

    /** Returns the corner component corresponding to the indicated corner,

     * posibly null

     */

    public JComponent getCornerComponent(int cornerIndex) {

        return null;

    }

    public int[][] getClusters() {
        return null;
    }
    
    public Experiment getExperiment() {
        return null;
    }
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return -1;
    }    

}

