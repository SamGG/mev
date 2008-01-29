/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
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
