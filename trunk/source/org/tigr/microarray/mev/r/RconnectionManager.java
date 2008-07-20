/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Aug 16, 2005
 */
package org.tigr.microarray.mev.r;

import java.awt.Frame;

import javax.swing.JOptionPane;

/**
 * Singleton type for managing Rconnections.  When getConnection() is called, 
 * the existing connection is returned.  If none exists, one is created and 
 * returned.
 * @author iVu
 */
public class RconnectionManager {
	private Rconnection rc;
	private Frame frame;
	private String sConn;
	private int iPort;
	
	
	public RconnectionManager( Frame frameP, String connPathP, int iPortP ) {
		this.frame = frameP;
		this.sConn = connPathP;
		this.iPort = iPortP;
	}
	
	
	/**
	 * As long as the user wants, recursively try to make a connection.
	 * @return
	 */
	public Rconnection getConnection() {
		if( this.rc == null ) {
			try {
				this.rc = new Rconnection( this.sConn, this.iPort );
				this.rc.setSendBufferSize( 100000000000l );
			} catch( RSrvException e ) {
				String s = e.getMessage();
				if( s.startsWith( "Cannot connect" ) ) {
					//inform user, allow them to start it, then connect
					RamaConnectionDialog rcd = new RamaConnectionDialog( this.frame );
					int i = rcd.showModal();
					if( i == JOptionPane.OK_OPTION ) {
						//continue
						this.getConnection();
					} else {
						return null;
					}
				}
			}
		}
		
		return this.rc;
	}//getConnection();
}//

/*
fraught with peril, removed

public int startRserve() {
	String sOs = System.getProperty( "os.name" );
	if( sOs.toLowerCase().indexOf( "mac" ) != -1 ) {
		try {            
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec("R CMD Rserve --no-save");
			InputStream stderr = proc.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			System.out.println("<ERROR>");
			while ( (line = br.readLine()) != null)
			    System.out.println(line);
			System.out.println("</ERROR>");
			int exitVal = proc.waitFor();
			System.out.println("Process exitValue: " + exitVal);
			return 1;
		} catch (Throwable t) {
			t.printStackTrace();
			return -1;
		}
	} else {
		return -1;
	}
}//startRserve()


public int pingRserve() {
	return -1;
}
*/