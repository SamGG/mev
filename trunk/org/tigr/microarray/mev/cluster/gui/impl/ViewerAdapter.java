/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ViewerAdapter.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl;

import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;

public class ViewerAdapter implements IViewer {
    public BufferedImage getImage() {return null;}
    public void onSelected(IFramework framework) {}
    public void onDataChanged(IData data) {}
    public void onMenuChanged(IDisplayMenu menu) {}
    public void onDeselected() {}
    public void onClosed() {}
    public JComponent getContentComponent() {return null;}
    public JComponent getHeaderComponent() {return null;}
}
