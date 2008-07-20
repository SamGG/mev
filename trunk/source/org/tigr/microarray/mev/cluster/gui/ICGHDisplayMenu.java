/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.cluster.gui;

import java.awt.Color;

/**
 * This interface is used to access to framework display menu.
 *
 * @version 1.0
 * @author Raktim Sinha
 * 
 */
public interface ICGHDisplayMenu {
    
    /**
     * Raktim Sept 29, 05
     * CGH Display Menu Constants
     */
    public static final double FIT_SIZE = -1;
    
    public static final int DISPLAY_TYPE_COMBINED = 0;
    public static final int DISPLAY_TYPE_SEPARATED = 1;
    
    public static final int COPY_DETERMINATION_BY_SD = 0;
    public static final int COPY_DETERMINATION_BY_THRESHOLD = 1;
    
    public static final int DISPLAY_WSL_ID = 0;
    public static final int DISPLAY_ALIAS = 1;
    public static final int DISPLAY_ID1 = 2;
      
    /**
     * Raktim Sept 29, 05
     * CGH Display Functions
     */
    public boolean isShowFlankingRegions();
    
    public void setShowFlankingRegions(boolean showFlankingRegions);
    
    public double getUnitLength();
    
    public int getElementWidth();
    
    public int getDisplayType();
    
    public int getDisplayLabelType();
    
    public void setCircleViewerBackgroundColor(Color circleViewerBackgroundColor);
    
    public Color getCircleViewerBackgroundColor();
    
}
