/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Transport.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.remote.protocol.communication;

import org.tigr.util.ConfMap;
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