/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: TempFile.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:34:45 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol.util;

import org.tigr.util.ConfMap;
import java.io.IOException;
import java.io.File;

public class TempFile {

    /**
     * Creates a <code>TempFile</code> from specified configuration.
     */
    public TempFile( ConfMap cfg ) {
        this.m_cfg = cfg;
    }

    /**
     * Returns a file name with specified suffix.
     */
    public String getName(String suffix) throws IOException {
        String s = '.' + suffix;
        File tmpDirectory = new File( m_cfg.getProperty("remote.parser.tmp-dir.path","DUMMY" ) );
        File file;
        if (!tmpDirectory.exists())
            file = File.createTempFile("tigr-mev", s );
        else
            file = File.createTempFile("tigr-mev", s, tmpDirectory);
        if (file == null) throw new IOException("TempFile: cannot create temp file");
        file.deleteOnExit();
        return file.getAbsolutePath();
    }

    /**
     * Returns a file name with 'tmp' suffix.
     */
    public String getName() throws IOException {
        return getName("tmp");
    }

    private ConfMap m_cfg;
}