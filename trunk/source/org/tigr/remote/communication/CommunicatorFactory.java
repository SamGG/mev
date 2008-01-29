/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: CommunicatorFactory.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 21:00:00 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.remote.communication;

import org.tigr.remote.RemoteException;
import org.tigr.remote.protocol.communication.http.Communicator;
import org.tigr.util.ConfMap;

public class CommunicatorFactory {
    
    /**
     * Initialize factory.
     */
    public static void init(ConfMap map) {
	config = map;
    }
    
    /**
     * Returns a new instance of the ClientCommunicator.
     */
    public static ClientCommunicator getCommunicator() throws RemoteException {
	return new Communicator(config);
    }
    
    private static ConfMap config;
}

