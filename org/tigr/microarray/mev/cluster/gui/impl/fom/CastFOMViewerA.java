/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: CastFOMViewerA.java,v $
 * $Revision: 1.5 $
 * $Date: 2006-02-23 20:59:51 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.fom;

import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

public class CastFOMViewerA extends ViewerAdapter implements java.io.Serializable {
    public static final long serialVersionUID = 202003010001L;
    
    private CastFOMContentComponentA content;
    
    public CastFOMViewerA(float[] fom_values, float interval, int[] numOfCastClusters) {
	this.content = new CastFOMContentComponentA(fom_values, interval, numOfCastClusters);
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
    
    public BufferedImage getImageA() {
	return this.content.getImageA();
    }
    
    public BufferedImage getImageB() {
	return this.content.getImageB();
	
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }
    
}
