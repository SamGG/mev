/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: IExperimentHeader.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:25 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.helpers;

import org.tigr.microarray.mev.cluster.gui.IData;

public interface IExperimentHeader {

        /**
     * Returns a component to be inserted into scroll pane view port.
     */
    public javax.swing.JComponent getContentComponent();
    
    /**
     * Sets data.
     */
    public void setData(IData data);
    
    /**
     * Sets max and min experiment values.
     */
    public void setValues(float minValue, float maxValue);
    
    /**
     * Sets positive and negative images 
     */
    public void setNegAndPosColorImages(java.awt.image.BufferedImage neg, java.awt.image.BufferedImage pos);
    
    /**
     * Sets anti-aliasing property.
     */
    public void setAntiAliasing(boolean isAntiAliasing);
    
    /**
     * Sets the left margin for the header
     */
    public void setLeftInset(int leftMargin);
    
    /**
     * Sets current cluster index 
     */
    public void setClusterIndex(int index);
    
    /**
     * Updates size of this header.
     */
    public void updateSizes(int contentWidth, int elementWidth);    
}

