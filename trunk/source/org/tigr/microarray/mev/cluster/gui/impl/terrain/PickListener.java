/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: PickListener.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:56 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.terrain;

import java.awt.event.MouseEvent;

import com.sun.j3d.utils.picking.PickCanvas;

public interface PickListener {
    void onMousePressed(MouseEvent event, PickCanvas canvas);
    void onMouseDragged(MouseEvent event, PickCanvas canvas);
    void onMouseReleased(MouseEvent event, PickCanvas canvas);
}
