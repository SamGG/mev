/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Presentation.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.remote.protocol.communication;

import org.tigr.util.ConfMap;

import org.tigr.remote.RemoteException;
import org.tigr.remote.protocol.Request;
import org.tigr.remote.protocol.Response;

import org.tigr.remote.protocol.parser.Parser;
import org.tigr.remote.protocol.serializer.RequestSerializer ;

public class Presentation {

    /**
     * Constructs a <code>Presentation</code> with specified 
     * configuration and transport.
     * @see Transport
     */
    public Presentation(ConfMap config, Transport transport) {
        m_transport = transport;
        m_config = config;
    }

    /**
     * Returns a <code>Response</code> of a service.
     */
    public Response getResponse() throws RemoteException {
        Response result = null;
        ResponseMessage msg = m_transport.getResponse();
        Parser parser = Parser.createParser(m_config);
        result = parser.parseResponse(msg.getStream());
        m_transport.finalizeReceive();
        return result;
    }

    /**
     * Sends a service request.
     */
    public void sendRequest(Request req) throws RemoteException {
        RequestMessage msg = new RequestMessage("TIGR-MEV", "Calculation service");
        RequestMessage msg2 = m_transport.sendRequest(msg);
        RequestSerializer serializer = RequestSerializer.createSerializer(m_config);
        serializer.serializeRequest(req, msg2.getStream());
        m_transport.finalizeSend();
    }

    private Transport m_transport;
    private ConfMap   m_config;
}