/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: AlgorithmFactory.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:25 $
 * $Author: braisted $
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
