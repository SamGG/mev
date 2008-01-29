/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: RequestMessage.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:21 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol.communication;

import java.io.OutputStream;

import org.tigr.util.ConfMap;

public class RequestMessage extends Message {

    /**
     * Constructs a <code>RequestMessage</code> with specified
     * from and to parameters.
     */
    public RequestMessage( String from, String to ) {
        super( from, to );
    }

    /**
     * Constructs a <code>RequestMessage</code> with specified
     * from, to parameters and configuration.
     */
    public RequestMessage( String from, String to, ConfMap properties ) {
        super( from, to, properties );
    }

    /**
     * Returns reference to a wrapped output stream.
     */
    public OutputStream getStream() {
        return m_stream;   
    }

    /**
     * Sets output stream to be wrapped.
     */
    public void setStream( OutputStream out ) {
        m_stream = out;   
    }

    private OutputStream m_stream = null;
}