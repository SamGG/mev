/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: MEVPollAcceptStrategy.java,v $
 * $Revision: 1.4 $
 * $Date: 2005-03-10 15:34:10 $
 * $Author: braistedj $
 */
package org.tigr.remote.gateway;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tigr.util.ConfMap;

class MEVPollAcceptStrategy {
    
    /**
     * Construct a <code>MEVPollAcceptStrategy</code> with specified session.
     */
    public MEVPollAcceptStrategy( SessionState session, ConfMap cfg ) {
	m_session = session;
    }
    
    /**
     * Accepts a polling request.
     */
    public void acceptPollRequest( HttpServletRequest req, HttpServletResponse resp ) throws IOException {
	if (!m_session.isLocked()) { // no PVM waiting for this poll
	    PrintWriter p = new PrintWriter( resp.getOutputStream() );
	    if (m_session.getMessageQueue().getSize() > 0) { // message queue is not empty
		String response = (String)m_session.getMessageQueue().getHead();
		p.print( response );
	    } else {
		p.println("<response/>");
	    }
	    p.flush();
	    p = null;
	} else { // PVM response arrived
	    // need to clear message queue
	    m_session.getMessageQueue().clear();
	    synchronized( m_session ) {
		m_session.connect();
		m_session.notify();
	    }
	    m_session.releaseMonitor();
	    BufferedInputStream in = new BufferedInputStream( m_session.getInputPipe() );
	    BufferedOutputStream out = new BufferedOutputStream( resp.getOutputStream() );
	    
	    byte[] b = new byte[1024*100];
	    int cnt;
	    while ((cnt = in.read(b)) >= 0) {
		out.write(b, 0, cnt);
	    }
	    out.flush();
	    out = null;
	    in.close();
	    m_session.getInputPipe().close();
	    m_session.free();
	}
    }
    
    SessionState m_session;
}
