/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: AlgorithmListener.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:23:50 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm;

import java.util.EventListener;

/**
 * The interface of a class which is used to listen to algorithm events.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public interface AlgorithmListener extends EventListener {
    
    /**
     * Invoked when an algorithm progress value was changed.
     *
     * @param event a <code>AlgorithmEvent</code> object.
     */
    public void valueChanged(AlgorithmEvent event);
}
