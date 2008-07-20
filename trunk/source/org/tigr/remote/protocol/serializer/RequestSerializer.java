/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: RequestSerializer.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 21:00:02 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.remote.protocol.serializer;

import java.io.OutputStream;

import org.tigr.remote.protocol.Request;
import org.tigr.util.ConfMap;

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

