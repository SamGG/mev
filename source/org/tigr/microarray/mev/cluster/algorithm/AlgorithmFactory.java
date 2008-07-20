/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: AlgorithmFactory.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:46:10 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm;

/**
 * Each distribution of calculation algorithms must contain class, which
 * implements this interface.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public interface AlgorithmFactory {
    
    /**
     * Returns an algorithm implementation by its name.
     *
     * @param name the name of an algorithm.
     * @throws <code>AlgorithmException</code> if an algorithm was not found.
     */
    public Algorithm getAlgorithm(String name) throws AlgorithmException;
}
