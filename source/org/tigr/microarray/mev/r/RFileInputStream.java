/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.r;

import java.io.*;

/** <b>RFileInputStream</b> is an {@link InputStream} to transfer files
    from <b>Rserve</b> server to the client. It is used very much like
    a {@link FileInputStream}. Currently mark and seek is not supported.
    The current implementation is also "one-shot" only, that means the file
    can be read only once.
    @version $Id: RFileInputStream.java,v 1.3 2006-03-07 19:00:35 caliente Exp $
*/
public class RFileInputStream extends InputStream {
    /** Rtalk class to use for communication with the Rserve */
    Rtalk rt;
    /** set to <code>true</code> when {@link #close} was called.
	Any subsequent read requests on closed stream  result in an 
	{@link IOException} or error result */
    boolean closed;
    /** set to <code>true</code> once EOF is reached - or more specifically
	the first time remore fread returns OK and 0 bytes */
    boolean eof;

    /** tries to open file on the R server, using specified {@link Rtalk} object
	and filename. Be aware that the filename has to be specified in host
	format (which is usually unix). In general you should not use directories
	since Rserve provides an own directory for every connection. Future Rserve
	servers may even strip all directory navigation characters for security
	purposes. Therefore only filenames without path specification are considered
	valid, the behavior in respect to absolute paths in filenames is undefined. */
    RFileInputStream(Rtalk rti, String fn) throws IOException {
	rt=rti;
	Rpacket rp=rt.request(Rtalk.CMD_openFile,fn);
	if (rp==null || !rp.isOk())
	    throw new IOException((rp==null)?"Connection to Rserve failed":("Request return code: "+rp.getStat()));
	closed=false; eof=false;
    }

    /** reads one byte from the file. This function should be avoided, since
	{@link RFileInputStream} provides no buffering. This means that each
	call to this function leads to a complete packet exchange between
	the server and the client. Use {@link #read(byte[],int,int)} instead
	whenever possible. In fact this function calls <code>#read(b,0,1)</code>.
	@return -1 on any failure, or the acquired byte (0..255) on success */
    public int read() throws IOException {
	byte[] b=new byte[1];
	if (read(b,0,1)<1) return -1;
	return b[0];
    }

    /** Reads specified number of bytes (or less) from the remote file.
	@param b buffer to store the read bytes
	@param off offset where to strat filling the buffer
	@param len maximal number of bytes to read
	@return number of bytes read or -1 if EOF reached
    */
    public int read(byte[] b, int off, int len) throws IOException {
	if (closed) throw new IOException("File is not open");
	if (eof) return -1;
	Rpacket rp=rt.request(Rtalk.CMD_readFile,len);
	if (rp==null || !rp.isOk())
	    throw new IOException((rp==null)?"Connection to Rserve failed":("Request return code: "+rp.getStat()));
	byte[] rd=rp.getCont();
	if (rd==null) {
	    eof=true;
	    return -1;
	};
	int i=0;
	while(i<rd.length) { b[off+i]=rd[i]; i++; };
	return rd.length;
    }

    /** close stream - is not related to the actual Rconnection, calling
	close does not close the Rconnection
    */
    public void close() throws IOException {
	Rpacket rp=rt.request(Rtalk.CMD_closeFile,(byte[])null);
	if (rp==null || !rp.isOk())
	    throw new IOException((rp==null)?"Connection to Rserve failed":("Request return code: "+rp.getStat()));
	closed=true;
    }
}
