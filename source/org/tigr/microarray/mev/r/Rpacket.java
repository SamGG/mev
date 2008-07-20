/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.r;

/** small class encapsulating packets from/to Rserv
    @version $Id: Rpacket.java,v 1.2 2006-03-07 19:00:35 caliente Exp $
*/
public class Rpacket {
    int cmd;
    byte[] cont;

    /** construct new packet
	@param Rcmd command
	@param Rcont content */
    public Rpacket(int Rcmd, byte[] Rcont) {
	cmd=Rcmd; cont=Rcont;
    }
    
    /** get command
        @return command */
    public int getCmd() { return cmd; }
    
    /** check last response for RESP_OK
	@return <code>true</code> if last response was OK */
    public boolean isOk() { return ((cmd&15)==1); }
    
    /** check last response for RESP_ERR
	@return <code>true</code> if last response was ERROR */
    public boolean isError() { return ((cmd&15)==2); }
    
    /** get status code of last response
	@return status code returned on last response */
    public int getStat() { return ((cmd>>24)&127); }

    /** get content
	@return inner package content */
    public byte[] getCont() { return cont; }
}
