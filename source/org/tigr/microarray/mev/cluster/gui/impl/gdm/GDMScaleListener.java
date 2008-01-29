/*
 * GDMScaleListener.java
 *
 * Created on September 11, 2003, 11:36 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.gdm;

/**
 *
 * @author  braisted
 */
public abstract class GDMScaleListener {
    
    /** Creates a new instance of GDMScaleListener */
    public GDMScaleListener() {
    }
    
    public abstract void scaleChanged(float lower, float upper);
}
