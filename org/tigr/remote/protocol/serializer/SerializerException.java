/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SerializerException.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.remote.protocol.serializer;

import org.tigr.remote.RemoteException;

public class SerializerException extends RemoteException {

    /**
     * Constructs a <code>SerializerException</code> with specified description.
     */
    public SerializerException( String message ) {
        super( message );
    }

    /**
     * Constructs a <code>SerializerException</code> with specified description
     * and an original exception.
     */
    public SerializerException( String message, Exception src ) {
        super( message, src );
    }
}

