/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: MEVRequestAcceptStrategy2.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.remote.gateway;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.tigr.util.ConfMap;

import org.tigr.remote.gateway.util.IQueue;


class MEVRequestAcceptStrategy2 {
    private static final int MAX_MESSAGES_IN_QUEUE = 10;
    
    /**
     * Constructs a <code>MEVRequestAcceptStrategy2</code> with specified session.
     */
    public MEVRequestAcceptStrategy2( SessionState s  ) {
	m_session = s;
    }
    
    /**
     * Accepts a MEV request.
     */
    public void acceptMEVRequest( HttpServletRequest req, HttpServletResponse resp )
    throws IOException {
	int packetSize = req.getContentLength();
	if (packetSize < 1)
	    throw new IOException("Bad content length: " + req.getContentLength() );
	ByteArrayOutputStream tmp = new ByteArrayOutputStream( packetSize );
	BufferedInputStream   in  = new BufferedInputStream( req.getInputStream() );
	int counter = 0;
	byte[] b = new byte[1024*100];
	int cnt;
	while (((cnt = in.read(b)) >= 0) && ( counter < packetSize )) {
	    tmp.write(b, 0, cnt);
	    counter += cnt;
	}
	String response = tmp.toString();
	IQueue queue = m_session.getRequestQueue();
	if (queue.getSize() >= MAX_MESSAGES_IN_QUEUE) queue.getHead();
	queue.addTail( response );
    }
    
    private SessionState m_session;
}
