/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: GatewayServlet.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:12 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.gateway;
/*
 Gateway:
   1. redirects HTTP MEV query to one of the PVM cluster nodes
   2. redirects HTTP responses issued by C++ PVM module back to MEV.
 
 MEV issues calculation request via HTTP POST and when polls gateway for responses
 using HTTP GET. Then PVM module uses HTTP POST to send response to the gateway.
 The Gateway redirects that response to the next incoming MEV polling request.
 
 Gateway exploits HTTP session mechanism, provided by Servlet Engine.
 A session is created for each MEV calculation request.
 Servlet engine drops a session on a timeout basis.
 Important: Servlet engine SHOULD be configured to use cookies to maintain HTTP sessions.
 
 Gateway has 3 entry points:
 1. MEV posting start-job or stop-job request ( HTTP POST )
    URL?post-request
 2. MEV polling for responses ( HTTP GET )
    URL?get-response
 3. tigr-slave ( pvm slave module ) posting progress or result ( HTTP POST )
    URL?post-response
 
 There URL is the adress of this Gateway servlet.
 
 PVM slave HTTP POST redirection strategy:
 There could be 2 kinds of PVM module responses: a progress event and a result packet.
 Progress event is quite short, so Gateway could store it into it's RAM in a queue. This
 will increase the performance of the system.
 The result packet is quite large, so Gateway will lock PVM post until the next MEV polling request
 will arrive. When, it redirects PVM post into MEV polling request.
 
 Note: Gateway was developed and tested under Apache Tomcat 3.2 in Standalone mode
 Requirements: Java Servlet API 2.2 or higher.
 */

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.tigr.remote.gateway.util.ServletUtil;
import org.tigr.util.ConfMap;

public class GatewayServlet extends HttpServlet {
    
    /**
     * Constructs a <code>GatewayServlet</code>.
     */
    public GatewayServlet()  {
    }
    
    /**
     * Initialize this servlet from specified configuration.
     */
    public void init( ServletConfig cfg ) throws ServletException {
	m_config = cfg;
	m_ctx = cfg.getServletContext();
	configure();
    }
    
    /**
     * Configure this servlet.
     */
    private void configure() throws ServletException {
	m_properties = new ConfMap();
	String[] params   = { "gateway.pvm.proxy-name", "gateway.pvm.slave-name",  "gateway.pvm.root-path"};
	String[] defaults = { "", "tigr-slave", "/usr/local/pvm3"};
	String tmp;
	for (int  i = 0; i < params.length; i++) {
	    tmp = m_ctx.getInitParameter( params[i] );
	    if (tmp == null || "".equals( tmp )) {
		if (!"".equals( defaults[i] ))
		    m_properties.setProperty( params[i], defaults[i] );
		else
		    throw new ServletException("Undefined required parameter: " + params[i] );
	    } else
		m_properties.setProperty( params[i], tmp );
	    
	}
	m_properties.list( System.out );
    }
    
    /**
     * Executes HTTP GET request.
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
	logTrace( req, "GET log" );
	String requestId = req.getQueryString();
	if (requestId == null) return;
	if ("get-response".equals( requestId )) {
	    try {
		onMEVPollsForResponse( req, resp );
	    } catch (Exception e) {
		logError( req, resp, e, "MEV polling error" );
		sendError( resp, "MEV polling error: " +  e.toString() );
	    }
	}
    }
    
    /**
     * Executes HTTP POST request.
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
	logTrace( req,  "POST log" );
	String requestId = req.getQueryString();
	if (requestId == null) {
	    try {
		ServletUtil.bufferedRead( req.getInputStream() );
	    } catch (IOException ex) {
	    }
	    logError( req, resp, new Exception("Unrecognized POST"), "" );
	    sendError(resp, "Unrecognized POST");
	} else
	    if ("post-request".equals( requestId )) {
		try {
		    onMEVPostsRequest( req, resp );
		} catch (Exception e) {
		    try {
			ServletUtil.bufferedRead( req.getInputStream() );
		    } catch (IOException ex) {
		    }
		    logError( req, resp, e, "MEV POST error" );
		    sendError( resp, "MEV POST error: " +  e.toString() );
		}
	    } else if ("post-response".equals( requestId )) {
		try {
		    onPVMPostsResponse( req, resp );
		} catch (Exception e) {
		    try {
			ServletUtil.bufferedRead( req.getInputStream() );
		    } catch (IOException ex) {
		    }
		    logError( req, resp, e, "PVM POST error" );
		    sendError( resp, "PVM POST error: " +  e.toString() );
		}
	    }
    }
    
    /**
     * Executes MEV POST request.
     */
    protected void onMEVPostsRequest( HttpServletRequest req, HttpServletResponse resp ) throws Exception {
	HttpSession session = req.getSession( false );
	if (session == null) {    // that means that it is the first request
	    session = req.getSession();
	    synchronized( session ) {
		session.setAttribute("MEV_SESSION", new SessionState() );
		MEVRequestAcceptStrategy strategy = new MEVRequestAcceptStrategy( m_properties );
		strategy.acceptMEVRequest( req, resp );
	    }
	} else {
	    SessionState si = (SessionState)session.getAttribute("MEV_SESSION");
	    MEVRequestAcceptStrategy2 strategy = new MEVRequestAcceptStrategy2( si );
	    strategy.acceptMEVRequest( req, resp );
	}
    }
    
    /**
     * Executes MEV polling requests.
     */
    protected void onMEVPollsForResponse( HttpServletRequest req, HttpServletResponse resp ) throws Exception {
	HttpSession session = req.getSession( false );
	if (session == null) throw new NullPointerException("Illegal call. Session has not been created ");
	SessionState si = (SessionState)session.getAttribute("MEV_SESSION");
	if (si == null) throw new NullPointerException("Illegal call. Session info has not been created for the  current session");
	MEVPollAcceptStrategy strategy = new MEVPollAcceptStrategy( si, m_properties );
	strategy.acceptPollRequest( req, resp );
    }
    
    /**
     * Executes PVM POST response.
     */
    protected void onPVMPostsResponse( HttpServletRequest req, HttpServletResponse resp ) throws Exception {
	HttpSession session = req.getSession( false );
	if (session == null) throw new NullPointerException("Illegal call. Session has has not been created ");
	SessionState si = (SessionState)session.getAttribute("MEV_SESSION");
	if (si == null) throw new NullPointerException("Illegal call. Session info has not been created for the  current session");
	PVMResponseAcceptStrategy strategy = new PVMResponseAcceptStrategy( si, m_properties );
	strategy.acceptPVMResponse( req, resp );
    }
    
    /**
     * Sends HTTP error.
     */
    protected void sendError(HttpServletResponse resp, String error ) {
	try {
	    m_ctx.log("Sending HTTP 404");
	    resp.setContentType("text/plain");
	    resp.sendError( 404 );
	} catch (IOException ex) {
	    System.err.println("Error sending error code");
	    throw new RuntimeException( ex.toString() );
	}
    }
    
    /**
     * Returns true if servlet configuration is in debug mode.
     */
    private final boolean isDebugMode() {
	String debug = m_ctx.getInitParameter("gateway.debug");
	if (debug == null)
	    return false;
	if (debug.equalsIgnoreCase("true"))
	    return true;
	else
	    return false;
    }
    
    /**
     * Writes a specified string to the servlet log.
     */
    protected void logTrace( HttpServletRequest req, String str  ) {
	if (!isDebugMode()) return;
	try {
	    StringBuffer sb = new StringBuffer();
	    sb.append("\n---------- Gateway trace message:\n");
	    sb.append("Trace message:  " + str + "\n");
	    sb.append("Request method: " + req.getMethod() + "\n");
	    sb.append("Query string:   " + req.getQueryString() + "\n");
	    sb.append("Content length: " + req.getContentLength() + "\n");
	    sb.append("Cookies: ( session info )\n");
	    Cookie[] cookies = req.getCookies();
	    if (cookies == null || cookies.length == 0)
		sb.append("No cookies in the HTTP request\n");
	    else {
		for (int i = 0; i < cookies.length; i++) {
		    sb.append("\tName: " + cookies[i].getName() + "\t" + "Value: " + cookies[i].getValue() + "\n");
		}
	    }
	    sb.append("\n");
	    m_ctx.log( sb.toString() );
	} catch (Exception e) {
	    System.err.println("Error logging a message");
	}
    }
    
    /**
     * Writes a specified exception to the servlet log.
     */
    protected void logError( HttpServletRequest req, HttpServletResponse resp, Exception ex, String str  ) {
	try {
	    StringBuffer sb = new StringBuffer();
	    sb.append("\n---------- Gateway error:\n");
	    sb.append("Error message:  " + str + "\n");
	    sb.append("Request method: " + req.getMethod() + "\n");
	    sb.append("Query string:   " + req.getQueryString() + "\n");
	    sb.append("Content length: " + req.getContentLength() + "\n");
	    sb.append("Cookies: ( session info )\n");
	    Cookie[] cookies = req.getCookies();
	    if (cookies == null || cookies.length == 0)
		sb.append("No cookies in the HTTP request\n");
	    else {
		for (int i = 0; i < cookies.length; i++) {
		    sb.append("\tName: " + cookies[i].getName() + "\t" + "Value: " + cookies[i].getValue() + "\n");
		}
	    }
	    sb.append("\n");
	    m_ctx.log( sb.toString(), ex  );
	} catch (Exception e) {
	    System.err.println("Error logging a message");
	}
    }
    
    ServletConfig m_config;
    ServletContext m_ctx;
    ConfMap m_properties;
}
