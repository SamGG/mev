/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: AlgorithmListener.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:25 $
 * $Author: braisted $
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
