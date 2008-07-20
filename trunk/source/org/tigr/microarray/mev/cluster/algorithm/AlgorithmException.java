/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: AlgorithmException.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:46:10 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm;

/**
 * Signals that an algorithm exception of some sort has occurred. This
 * class is the general class of exceptions produced by failed or
 * interrupted calculation operations.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public class AlgorithmException extends Exception {
    
    /**
     * Field exception specifies a wrapped exception. May be null.
     */
    private Exception exception;
    
    /**
     * Constructs an <code>AlgorithmException</code> with the specified detail
     * message.
     *
     * @param s the detail message.
     */
    public AlgorithmException(String s) {
	super(s);
    }
    
    /**
     * Create a new <code>AlgorithmException</code> wrapping an existing exception.
     *
     * @param exception the exception to be wrapped.
     */
    public AlgorithmException(Exception exception) {
	this.exception = exception;
    }
    
    /**
     * Returns the error message string.
     */
    public String getMessage() {
	if (isInternal()) {
	    return getInternal().getMessage();
	}
	return super.getMessage();
    }
    
    /**
     * Returns true if the wrapped exception is not null.
     */
    public boolean isInternal() {
	return exception != null;
    }
    
    /**
     * Returns the wrapped exception.
     */
    public Exception getInternal() {
	return exception;
    }
    
    /**
     * Prints this <code>AlgorithmException</code> and its backtrace to the
     * specified print stream.
     *
     * @param s <code>PrintStream</code> to use for output.
     */
    public void printStackTrace(java.io.PrintStream s) {
	super.printStackTrace(s);
	if (isInternal()) {
	    getInternal().printStackTrace(s);
	}
    }
}
