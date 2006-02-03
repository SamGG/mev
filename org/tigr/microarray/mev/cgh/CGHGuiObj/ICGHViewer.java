/*
 * ICGHViewer.java
 *
 * Created on June 15, 2003, 1:35 AM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj;

import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.ICGHDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.ICGHCloneValueMenu;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public interface ICGHViewer extends IViewer {
    public void onMenuChanged(ICGHDisplayMenu menu);
    public void onThresholdsChanged(ICGHDisplayMenu menu);
    public void onCloneValuesChanged(ICGHCloneValueMenu menu);
}
