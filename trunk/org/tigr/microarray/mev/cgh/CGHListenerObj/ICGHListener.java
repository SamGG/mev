/*
 * ICGHArrayViewer.java
 *
 * Created on June 1, 2003, 12:34 AM
 */

package org.tigr.microarray.mev.cgh.CGHListenerObj;

/**
 *
 * @author  Adam Margolin
 */

public interface ICGHListener {
    public void onDataChanged();
    public void onCloneValuesChanged();
    public void onChromosomeSelected(java.util.EventObject eventObj);
    public void onCloneDistributionsLoaded();
    //public void onExperimentsLoaded(java.util.EventObject eventObj);
    public void onExperimentsLoaded();
    //public void onExperimentsInitialized(java.util.EventObject eventObj);
}
