/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: IExperimentHeader.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:07 $
 * $Author: braistedj $
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
     * Sets the flag to use double gradient 
     */
    public void setUseDoubleGradient(boolean useDouble);    
    
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

