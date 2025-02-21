/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ResponseMessage.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:22 $
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
