/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ServletUtil.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:36:10 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.gateway.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

public class ServletUtil {

    /**
     * Reads all the data from input stream and writes their into output one.
     */
    public static void readTo( InputStream in, OutputStream out ) throws IOException {
        int c = 0;
        while (( c = in.read() ) != -1) {
            out.write( c );
        }
        out.flush();
    }

    /**
     * Reads all the data from the input stream.
     */
    public static void read( InputStream in ) throws IOException {
        int c = 0;
        while (( c = in.read() ) != -1);
    }

    /**
     * Reads all the data from input stream and writes their into output one,
     * using buffers.
     */
    public static void bufferedReadTo( InputStream in, OutputStream out ) throws IOException {
        BufferedInputStream b_in = new BufferedInputStream( in );
        BufferedOutputStream b_out = new BufferedOutputStream( out );
        readTo( b_in, b_out );
    }

    /**
     * Reads all the data from the input stream, using a buffer.
     */
    public static void bufferedRead( InputStream in ) throws IOException {
        BufferedInputStream b_in = new BufferedInputStream( in );
        read( b_in );
    }

}

