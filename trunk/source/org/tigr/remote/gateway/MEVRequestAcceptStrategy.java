/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: MEVRequestAcceptStrategy.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:30:16 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.gateway;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;

import org.tigr.util.ConfMap;

class MEVRequestAcceptStrategy {
    private static final int MAX_PACKET_SIZE = 1024*1024; // 1M chunk
    
    /**
     * Constructs a <code>MEVRequestAcceptStrategy</code> with specified configuration.
     */
    public MEVRequestAcceptStrategy( ConfMap cfg ) {
	m_pvmProxyName = cfg.getString("gateway.pvm.proxy-name");
	m_pvmSlaveName = cfg.getString("gateway.pvm.slave-name");
	m_pvmURL       = cfg.getProperty("gateway.pvm.slave.http","").trim();
	m_pvmRoot      = cfg.getProperty("gateway.pvm.root-path","").trim();
    }
    
    /**
     * Checkes if specified input stream contains an error.
     */
    private static void checkError( InputStream in ) throws IOException {
	BufferedReader reader =  new BufferedReader(  new InputStreamReader( in ) );
	String result = reader.readLine();
	if (result != null) {
	    if (!result.startsWith("OK")) {
		result = reader.readLine();
		throw new IOException("Cannot send to PVM: " + result );
	    }
	} else
	    throw new IOException("Bad pvmproxy response.");
    }
    
    /**
     * Constructs PVM url to execute post response.
     */
    private void constructURL(HttpServletRequest req) throws IOException {
	if (m_pvmURL == null || ( m_pvmURL.trim().equals("") )) {
	    StringBuffer sb = HttpUtils.getRequestURL( req );
	    sb.append("?post-response");
	    m_pvmURL = sb.toString();
	}
    }
    
    /**
     * Starts pvmproxy and waits for it to finish.
     * The exact protocol is defined at TIGR-MEV/remote/src/pvmproxy/pvmproxy.cpp file
     */
    public void acceptMEVRequest( HttpServletRequest req, HttpServletResponse resp )
    throws IOException, InterruptedException {
	if (req.getContentLength() < 1)
	    throw new IOException("Bad content length: " + req.getContentLength() );
	constructURL( req );
	Runtime runtime = Runtime.getRuntime();
	String cmdarray[] = {m_pvmProxyName, m_pvmSlaveName, m_pvmURL, getSessionIdString( req )};
	Process process;
	if (!m_pvmRoot.equals("")) {
	    String envp[] = { "PVM_ROOT=" + m_pvmRoot};
	    process = runtime.exec( cmdarray, envp );
	} else
	    process = runtime.exec( cmdarray );
	
	// ad hoc:
	OutputStream out = new BufferedOutputStream( process.getOutputStream(), 1024*1024 );
	InputStream in   = new BufferedInputStream( process.getInputStream() );
	InputStream wwwIn = new BufferedInputStream( req.getInputStream() );
	
	checkError( in );
	
	int i = 0;
	byte[] b = new byte[1024*100];
	int cnt;
	while ((cnt = wwwIn.read(b)) >= 0) {
	    out.write(b, 0, cnt);
	    i += cnt;
	}
	if (i < 10) throw new IOException("read only : " + i);
	out.flush();
	out.close();
	checkError( in );
	
	process.waitFor();
	int code = process.exitValue();
	
	if (code != 0)
	    throw new IOException("pvmproxy exit code != 0");
    }
    
    /**
     *  Create a string, which identifies this session among others.
     *  Since we are not sure, what cookie is used exactly to identify this session,
     *  we will use all of them.
     *
     *  New:
     *  We use the folllowing: getSessionId as string.
     *  This should be used as JSESSIONID cookie value
     */
    private final String getSessionIdString(HttpServletRequest req) throws IOException {
	return "JSESSIONID=" + req.getSession().getId();
    }
    
    private String m_pvmProxyName;
    private String m_pvmSlaveName;
    private String m_pvmURL;
    private String m_pvmRoot;
}
