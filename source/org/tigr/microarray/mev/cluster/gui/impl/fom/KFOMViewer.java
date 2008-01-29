/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: KFOMViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-03-24 15:50:09 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.fom;

import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

public class KFOMViewer extends ViewerAdapter implements java.io.Serializable {
    
    private FOMContentComponent content;
    
    public KFOMViewer(float[] fom_values, float[] variances) {
	this.content = new FOMContentComponent(fom_values, variances);
    }
    
    public KFOMViewer(FOMContentComponent content, JComponent header){
    	this.content = content;
    }
    
    public void setFOMIterationValues(float [][] values) {
        content.setFOMIterationValues(values);
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
    
    public BufferedImage getImage() {
	return this.content.getImage();
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }
    
}
