/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ParserException.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:08 $
 * $Author: braistedj $
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

