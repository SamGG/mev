/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: IDisplayMenu.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui;

import java.awt.Dimension;
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
    
       /**
     * Returns current positive gradient image
     */
    public BufferedImage getPositiveGradientImage();
    
    /**
     * Returns current selection for using a gradient in expression graphs
     */
    public boolean getColorGradientState();
}
