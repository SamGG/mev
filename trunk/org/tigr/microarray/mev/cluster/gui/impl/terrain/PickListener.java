/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: PickListener.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:10 $
 * $Author: braistedj $
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
