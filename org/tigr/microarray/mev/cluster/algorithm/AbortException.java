/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: AbortException.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:23:50 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm;

/**
 * Signals that an algorithm has been aborted.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public class AbortException extends AlgorithmException {
    
    /**
     * Constructs an <code>AbortException</code>.
     */
    public AbortException() {
	super("abort");
    }
}
