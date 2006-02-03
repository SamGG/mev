/*
 * IDataRegionSelectionListener.java
 *
 * Created on March 26, 2003, 1:18 AM
 */

package org.tigr.microarray.mev.cgh.CGHListenerObj;

import java.util.EventObject;

/**
 *
 * @author  Adam Margolin
 */

public interface IDataRegionSelectionListener {
    public void onShowBrowser(EventObject eventObj);
    public void onDisplayDataValues(EventObject eventObj);
    public void onShowGenes(EventObject eventObj);
    public void onAnnotationsSelected(EventObject eventObj);
}
