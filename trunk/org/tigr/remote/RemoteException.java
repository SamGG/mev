/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: RemoteException.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:10 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote;

public class RemoteException extends Exception {
    
    /**
     * Constructs a <code>RemoteException</code> with specified message.
     *
     * @param message the detail message.
     */
    public RemoteException(String message) {
	super( message );
    }
    
    /**
     * Constructs a <code>RemoteException</code> with specified message
     * and exception.
     *
     * @param message the detail message.
     * @param exception the exception to be wrapped.
     */
    public RemoteException( String message, Exception exception ) {
	super( message );
	this.exception = exception;
    }
    
    /**
     * Prints this <code>RemoteException</code> and its backtrace to the
     * specified print stream.
     *
     * @param s <code>PrintStream</code> to use for output.
     */
    public void printStackTrace(java.io.PrintStream s) {
	super.printStackTrace(s);
	if (exception != null) {
	    exception.printStackTrace(s);
	}
    }
    
    /**
     * Field exception specifies a wrapped exception.
     */
    private Exception exception = null;
}
