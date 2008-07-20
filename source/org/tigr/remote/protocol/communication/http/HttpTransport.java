/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: HttpTransport.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 21:00:01 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.remote.protocol.communication.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.tigr.remote.RemoteException;
import org.tigr.remote.protocol.communication.RequestMessage;
import org.tigr.remote.protocol.communication.ResponseMessage;
import org.tigr.remote.protocol.communication.Transport;
import org.tigr.util.ConfMap;

class HttpTransport implements Transport {

    /**
     * Construct a <code>HttpTransport</code> with specified configuration.
     */
    public HttpTransport( ConfMap config ) throws RemoteException {
        m_config = config;
        try {
            m_communicator = new HttpCommunicator2(  config  );
        } catch (Exception ex) {
            throw new RemoteException("HttpTransport: bad server URL ", ex);
        }
    }

    /**
     * Send a request message to a server.
     */
    public RequestMessage sendRequest( RequestMessage msg ) throws RemoteException {
        try {
            OutputStream out =  m_communicator.send( msg.getProperties() );
            msg.setStream( out );
            return msg;
        } catch (IOException ex) {
            throw new RemoteException("HttpTransport: cannot send request: " + ex.toString(), ex);
        }
    }

    /**
     * Returns the response message from a server.
     */
    public ResponseMessage getResponse() throws RemoteException {
        try {
            InputStream in =  m_communicator.receive();
            ResponseMessage msg = new ResponseMessage("Server", "TIGR-MEV", in);
            return msg;
        } catch (IOException ex) {
            throw new RemoteException("HttpTransport: cannot receive response. " + ex.toString(), ex);
        }
    }

    /**
     * Clean up used resources after data was sent.
     */
    public void finalizeSend() throws RemoteException {
        try {
            m_communicator.cleanupAfterSend();
        } catch (IOException ex) {
            throw new RemoteException("HttpTransport: Server error. Cannot start job: " + ex.toString(), ex);
        }
    }

    /**
     * Clean up resources after data was received.
     */
    public void finalizeReceive() throws RemoteException {
        try {
            m_communicator.cleanupAfterReceive();
        } catch (IOException ex) {
            throw new RemoteException("HttpTransport: cannot finalize receiving: " + ex.toString(), ex);
        }
    }

    protected ConfMap m_config;
    protected HttpCommunicator2 m_communicator;
}
