/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: TempFile.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 21:00:03 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.remote.protocol.util;

import java.io.File;
import java.io.IOException;

import org.tigr.util.ConfMap;

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
