/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: PVMResponseAcceptStrategy.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:12 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.gateway;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tigr.remote.gateway.util.IQueue;
import org.tigr.util.ConfMap;

class PVMResponseAcceptStrategy {
    private static final int MAX_PACKET_SIZE = 1024 * 100; // 100K packet
    private static final int MAX_MESSAGES_IN_QUEUE = 10; // Maximum messages not sent
    
    /**
     * Constructs a <code>PVMResponseAcceptStrategy</code> with specified
     * session and configuration.
     */
    public PVMResponseAcceptStrategy( SessionState session, ConfMap cfg ) {
	m_session = session;
	m_maxSize = cfg.getInt("gateway.cache.max-packet-size", MAX_PACKET_SIZE);
	m_maxMessages = cfg.getInt("gateway.cache.max-messages-in-queue", MAX_MESSAGES_IN_QUEUE );
    }
    
    /**
     * Accepts a PVM response.
     */
    public void acceptPVMResponse( HttpServletRequest req, HttpServletResponse resp) throws IOException, InterruptedException {
	int packetSize = req.getContentLength();
	if (req.getContentLength() < m_maxSize) {
	    ByteArrayOutputStream tmp = new ByteArrayOutputStream( packetSize );
	    BufferedInputStream   in  = new BufferedInputStream( req.getInputStream() );
	    int counter = 0;
	    int c = 0;
	    while (( ( c = in.read() ) != -1 ) && ( counter < packetSize )) {
		++counter;
		tmp.write( c );
	    }
	    String response = tmp.toString();
	    IQueue queue = m_session.getMessageQueue();
	    if (queue.getSize() >= m_maxMessages) queue.getHead();
	    queue.addTail( response );
	} else { //
	    synchronized( m_session ) {
		m_session.createMonitor();
		m_session.wait();
	    }
	    BufferedInputStream in = new BufferedInputStream( req.getInputStream() );
	    OutputStream out_pipe = m_session.getOutputPipe();
	    
	    byte[] b = new byte[1024*100];
	    int cnt;
	    while ((cnt = in.read(b)) >= 0) {
		out_pipe.write(b, 0, cnt);
	    }
	    out_pipe.flush();
	    out_pipe.close();
	}
	IQueue queue = m_session.getRequestQueue();
	if (! queue.isEmpty()) {
	    String request = (String)queue.getHead();
	    PrintWriter p = new PrintWriter( resp.getOutputStream() );
	    p.print( request );
	    p.flush();
	    p = null;
	}
    }
    
    SessionState m_session;
    int m_maxSize;
    int m_maxMessages;
}
