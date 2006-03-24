/*

Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).

All rights reserved.

*/

/*

 * $RCSfile: ViewerAdapter.java,v $

 * $Revision: 1.9 $

 * $Date: 2006-03-24 15:49:57 $

 * $Author: eleanorahowe $

 * $State: Exp $

 */

package org.tigr.microarray.mev.cluster.gui.impl;



import java.awt.image.BufferedImage;
import java.beans.Expression;



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
    
    /**
     * Implemented only to satisfy IViewer interface requirements.
     */
    public Experiment getExperiment() {
        return null;
    }
    /**
     * Implemented only to satisfy IViewer interface requirements.
     */
    public void setExperiment(Experiment e) {
    }
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return -1;
    }    

    /**
     * Implemented only to satisfy IViewer interface requirements.
     */
    public int getExperimentID() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperimentID(int)
	 */
	public void setExperimentID(int id) {}    
	
	/**
	 * Provides the Expression required to express the state of this object
	 * in a saved file.
	 * @author eleanora
	 *
	 */
	public Expression getExpression(){
		return new Expression(this, this.getClass(), "new",
				new Object[]{this.getContentComponent(), this.getHeaderComponent()});
		
	}

}

