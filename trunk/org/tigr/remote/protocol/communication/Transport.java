/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Transport.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:22 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol.communication;

import org.tigr.remote.RemoteException;

public interface Transport {
    /**
     * Sends a request message.
     */
    public RequestMessage sendRequest( RequestMessage msg ) throws RemoteException;

    /**
     * Invoked to clean up resources after data was sent.
     */
    public void finalizeSend() throws RemoteException;

    /**
     * Returns a response message.
     */
    public ResponseMessage getResponse() throws RemoteException;

    /**
     * Invoked to clean up resources after data was received.
     */
    public void finalizeReceive() throws RemoteException;
}