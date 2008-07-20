/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * ITreeNodeRenderer.java
 *
 * Created on August 11, 2004, 9:51 AM
 */

package org.tigr.microarray.mev.cluster.gui.helpers.ktree;

import java.awt.Graphics2D;

/**
 *
 * @author  braisted
 */
public interface ITreeNodeRenderer {
    
    public static int RENDERING_HINT_VERBOSE = 0;
    public static int RENDERING_HINT_MINIMAL = 1;
    
    public static int STANDARD_NODE = 0;
    public static int SELECTED_NODE = 1;
    public static int PATH_NODE = 2;
    public static int NON_PATH_NODE = 3;
        
    public void renderNode(Graphics2D g2, int x, int y, int modifier);
    
    //added method to render a node tool tip when in minimal node info mode.
    public void renderVerboseTip(Graphics2D g2);
}
