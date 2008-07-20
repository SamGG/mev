/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: XMLMAGEParser.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:22 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol.parser;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.util.ConfMap;
import org.xml.sax.XMLReader;

class XMLMAGEParser {

    /**
     * Constructs a <code>XMLMAGEParser</code> with specified configuration.
     */
    public XMLMAGEParser( ConfMap cfg ) {
        this.m_config = cfg;
    }

    /**
     * Parse a resource with specified id.
     */
    public void parse( String id ) throws Exception {
        XMLReader reader = ParserUtil.createReader( m_config );
        m_handler = new MAGEResponseHandler( m_config );

        reader.setContentHandler( m_handler );
        reader.setErrorHandler( m_handler );
        reader.parse( id );
    }

    /**
     * Returns parse result as an <code>AlgorithmData</code>.
     */
    public AlgorithmData getResult() { return m_handler.getResult() ;}

    private MAGEResponseHandler m_handler;
    private ConfMap m_config;
}

