/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: CastFOMViewerB.java,v $
 * $Revision: 1.2 $
 * $Date: 2003-12-08 18:42:10 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.fom;

import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;

import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

public class CastFOMViewerB extends ViewerAdapter {
    
    private CastFOMContentComponentB content;
    
    public CastFOMViewerB(float[] fom_values, float interval, int[] numOfCastClusters) {
	this.content = new CastFOMContentComponentB(fom_values, interval, numOfCastClusters);
    }
    
    public JComponent getContentComponent() {
	return this.content;
    }
    
    public void onSelected(IFramework framework) {
	this.content.onSelected(framework);
    }
    
    public void onMenuChanged(IDisplayMenu menu) {
	this.content.onMenuChanged(menu);
    }
    
    /*
    public BufferedImage getImageA() {
	return this.content.getImageA();
    }
     */
    
    public BufferedImage getImageB() {
	return this.content.getImageB();
	
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }
    
}
