/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ParserException.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.remote.protocol.parser;

import org.tigr.remote.RemoteException;

public class ParserException extends RemoteException {

    /**
     * Constructs a <code>ParserException</code> with 
     * specified detail message.
     */
    public ParserException( String message ) {
        super( message );
    }

    /**
     * Constructs a <code>ParserException</code> with a
     * specified detail message and an intitial exception.
     */
    public ParserException( String message, Exception exception ) {
        super( message, exception );
    }
}

