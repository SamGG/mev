/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: IViewer.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui;

import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * The interface of a class, which can be displayed
 * in the framework scroll pane.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public interface IViewer {
    
    /**
     * Returns a component to be inserted into scroll pane view port.
     */
    public JComponent getContentComponent();
    
    /**
     * Returns a component to be inserted into scroll pane header.
     */
    public JComponent getHeaderComponent();
    
    /**
     * Invoked by the framework when this viewer is selected.
     */
    public void onSelected(IFramework framework);
    
    /**
     * Invoked by the framework when data is changed,
     * if this viewer is selected.
     * @see IData
     */
    public void onDataChanged(IData data);
    
    /**
     * Invoked by the framework when display menu is changed,
     * if this viewer is selected.
     * @see IDisplayMenu
     */
    public void onMenuChanged(IDisplayMenu menu);
    
    /**
     * Invoked by the framework when this viewer was deselected.
     */
    public void onDeselected();
    
    /**
     * Invoked when the framework is going to be closed.
     */
    public void onClosed();
    
    /**
     * Invoked by the framework to save or to print viewer image.
     */
    public BufferedImage getImage();
}
