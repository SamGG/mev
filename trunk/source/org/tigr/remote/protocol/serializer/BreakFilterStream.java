/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: BreakFilterStream.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 21:00:02 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.remote.protocol.serializer;

import java.io.IOException;
import java.io.OutputStream;

import de.mnl.java.io.base64.Base64OutputStream;

public class BreakFilterStream extends OutputStream {

    private final static int BREAK_AFTER = 57 * 1024 * 10; // break after 540K of source data

    /**
     * Constructs a <code>BreakFilterStream</code> for specified output stream.
     */
    public BreakFilterStream( OutputStream out ) {
        m_out = out;
        m_buffer = new byte[BREAK_AFTER];
    }

    /**
     * Writes an integer into the wrapped output stream.
     */
    public void write(int b) throws IOException {
        m_buffer[m_counter++] = (byte)(b & 0xff);
        if (m_counter == BREAK_AFTER)
            doBreak();
    }

    /**
     * This method is to write all buffered data to its destination.
     */
    public void flush() throws IOException {
        internalFlush();
    }

    /**
     * Writes the break xml command to wrapped output stream.
     */
    private void doBreak() throws IOException {
        internalFlush();
        m_out.write( s_break );
    }

    /**
     * Writes all base64-encoded buffered data into wrapped output stream.
     */
    private void internalFlush() throws IOException {
        Base64OutputStream o = new Base64OutputStream(m_out, false);
        o.write(m_buffer, 0, m_counter);
        m_counter = 0;
        o.flush();
        o = null;
    }

    private OutputStream m_out;
    private int m_counter = 0;
    private byte[] m_buffer;
    private static final byte[] s_break = "<?break?>\r\n".getBytes();
}
