/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SessionState.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.remote.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.tigr.remote.gateway.util.IQueue;
import org.tigr.remote.gateway.util.Queue;

class SessionState {
    
    /**
     * Construct a <code>SessionState</code>.
     */
    public SessionState() {
    }
    
    /**
     * Connects wrapped pipe streams.
     */
    public void connect() throws IOException {
	m_out = new PipedOutputStream();
	m_in  = new PipedInputStream( m_out );
    }
    
    /**
     * Checkes if wrapped pipe streams are connected.
     */
    public void checkConnected() {
	if (( m_out == null ) || ( m_in == null ))
	    throw new NullPointerException("Program error: pipe streams are not connected");
    }
    
    /**
     * Clean up pipe streams.
     */
    public void free() {
	m_in = null;
	m_out = null;
    }
    
    /**
     * Creates a monitor for this session state.
     */
    public void createMonitor() throws IOException {
	if (m_monitor != false)
	    throw new IOException("Sesion monitor exists");
	m_monitor = true;
    }
    
    /**
     * Releases a monitor for this session state.
     */
    public void releaseMonitor() { m_monitor = false;}
    
    /**
     * Returns true if request for this session state is ready.
     */
    public boolean isLocked() { return m_monitor == true;}
    
    public IQueue getMessageQueue() { return m_queue;}
    public IQueue getRequestQueue() { return m_requestQueue;}
    
    public InputStream   getInputPipe()  { return m_in;}
    public OutputStream  getOutputPipe() { return m_out;}
    
    private PipedInputStream  m_in;
    private PipedOutputStream m_out;
    
    private IQueue m_queue = new Queue();
    private IQueue m_requestQueue = new Queue();
    
    private boolean m_monitor = false;
}
