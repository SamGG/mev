/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: IGUIFactory.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:38:25 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui;

/**
 * Each gui distribution must contain class, which
 * implements this interface.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public interface IGUIFactory {
    
    /**
     * Returns the array of analysis descriptions.
     * @see AnalysisDescription
     */
    public AnalysisDescription[] getAnalysisDescriptions();
}
