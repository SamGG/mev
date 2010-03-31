/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: IDisplayMenu.java,v $
 * $Revision: 1.5 $
 * $Date: 2007-12-19 21:39:36 $
 * $Author: saritanair $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;

/**
 * This interface is used to access to framework display menu.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public interface IDisplayMenu {
    // palette styles
    public static final int BLUETORED  = 1; //SingleArrayViewer color alternative
    public static final int GREENRED   = 2; //displays bars for cy3 and cy5, fraction of each
    public static final int OVERLAY    = 3; //color overlay
    public static final int RATIOSPLIT = 4; //continuous range through black
    
    // color schemes
    public static final int GREEN_RED_SCHEME = 5;
    public static final int BLUE_YELLOW_SCHEME = 6;
    public static final int CUSTOM_COLOR_SCHEME = 7;
    public static final int RAINBOW_COLOR_SCHEME = 8;
    //Added by Sarita
    public static final int ACCESSIBLE_COLOR_SCHEME=10;
    
    /**
     * Returns a palette style.
     */
    public int getPaletteStyle();
    
    /**
     * Returns true if "Green/Red" item is selected.
     */
    public boolean isGRScale();
    
    /**
     * Returns true if "Draw Borders" item is selected.
     */
    public boolean isDrawingBorder();
    
    /**
     * Returns true if "Compact Color Clusters" item is selected.
     */
    public boolean isCompactClusters();
    /**
     * Returns true if "Show Rects" item is selected.
     */
    public boolean isShowRects();
    /**
     * Returns true if "Auto-Arrange Colors" item is selected.
     */
    public boolean isAutoArrangeColors();
    /**
     * Returns true if "Tracing" item is selected.
     */
    public boolean isTracing();
    
    /**
     * Returns true if "Use Anti-Aliasing" item is selected.
     */
    public boolean isAntiAliasing();
    
    /**
     * Returns a shape size.
     */
    public Dimension getElementSize();
    
    /**
     * Returns index of a selected label item.
     */
    public int getLabelIndex();
    
    /**
     * Returns max value of the ratio scale.
     */
    public float getMaxRatioScale();
    
    /**
     * Returns min value of the ratio scale.
     */
    public float getMinRatioScale();
    
    /**
     *  Returns mid (selected "mid-point") of the ratio scale.
     * @return
     */
    public float getMidRatioValue();
    
    /**
     * Returns max value of the CY3 scale.
     */
    public float getMaxCY3Scale();
    
    /**
     * Returns max value of the CY5 scale.
     */
    public float getMaxCY5Scale();
    
    /**
     * Returns current negative gradient image
     */
    public BufferedImage getNegativeGradientImage();
    
    public BufferedImage createGradientImage(Color color1, Color color2);
    
       /**
     * Returns current positive gradient image
     */
    public BufferedImage getPositiveGradientImage();
    
    /**
     * Returns current selection for using a gradient in expression graphs
     */
    public boolean getColorGradientState();

    /**
     * Returns the current color scheme index
     */
    public int getColorScheme();
    
    /**
     * Returns true if the gradient style is double 
     */
    public boolean getUseDoubleGradient();

	public String getUserFont();
    
    

}
