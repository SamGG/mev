/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ResponseMessage.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:12 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol.communication;

import java.io.InputStream;

import org.tigr.util.ConfMap;

public class ResponseMessage extends Message {

    /**
     * Constructs a <code>ResponseMessage</code> with specified from, to
     * parameters and input stream to be wrapped.
     */
    public ResponseMessage( String from, String to, InputStream in ) {
        super( from, to );
        m_stream = in; 
    }

    /**
     * Constructs a <code>ResponseMessage</code> with specified from, to
     * parameters, configuration and input stream to be wrapped.
     */
    public ResponseMessage( String from, String to, ConfMap properties, InputStream in ) {
        super( from, to, properties );
        m_stream = in;
    }

    /**
     * Returns the wrapped input stream.
     */
    public InputStream getStream() {
        return m_stream;
    }

    private InputStream m_stream = null;
}