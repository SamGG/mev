/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: IViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-03-24 15:49:53 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui;

import java.awt.image.BufferedImage;
import java.beans.Expression;

import javax.swing.JComponent;

/**
 * The interface of a class, which can be displayed
 * in the framework scroll pane.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public interface IViewer {
    
    public static int UPPER_LEFT_CORNER = 0;
    public static int UPPER_RIGHT_CORNER = 1;
    public static int LOWER_LEFT_CORNER = 2;
    
    /**
     * Returns a component to be inserted into scroll pane view port.
     */
    public JComponent getContentComponent();
    
    /**
     * Returns a component to be inserted into scroll pane header.
     */
    public JComponent getHeaderComponent();
    
    /**
     * Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent();  

    /**
     * Returns the corner component corresponding to the indicated corner,
     * posibly null
     */
    public JComponent getCornerComponent(int cornerIndex);
    
    
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
    
    /**
     * Returns the viewer's clusters or null
     */
    public int[][] getClusters();
    
    
    /**
     *  Returns the viewer's experiment or null
     */
    public Experiment getExperiment();
    
    /**
     * Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType();
    
    /**
     * EH - state-saving
     * Sets the Experiment field for this Viewer.  Used when restoring state.  
     *
     */
    public void setExperiment(Experiment e);
    
    /**
    * EH - state-saving
    * Returns the ID value for the Experiment associated with this viewer.
    */
    public int getExperimentID();
    
    /**
    * EH - state-saving
    * Sets the ID value for the Experiment associated with this IViewer
    */
    public void setExperimentID(int id);
    
    /**
     * EH testing
     * @return An Expression that can be used to save the object's state.
     */
    public Expression getExpression();
}
