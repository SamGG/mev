/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: RequestSerializer.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:35:13 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol.serializer;

import java.io.OutputStream;

import org.tigr.util.ConfMap;
import org.tigr.remote.protocol.Request;

public abstract class RequestSerializer {
    /**
     * Must be overriden in concrete implementation.
     */
    public abstract void serializeRequest( Request req, OutputStream out ) throws SerializerException ;

    /**
     * Returns a <code>RequestSerializer</code> implementation with
     * specified configuration.
     */
    public static RequestSerializer createSerializer( ConfMap cfg ) throws SerializerException {
        return new RequestSerializerImpl();
    }
}

