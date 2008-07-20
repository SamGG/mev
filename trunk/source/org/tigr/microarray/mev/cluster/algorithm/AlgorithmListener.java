/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: AlgorithmListener.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:46:10 $
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
