/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: XMLMAGEParser.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.remote.protocol.parser;

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

import org.tigr.util.ConfMap;
import org.tigr.remote.RemoteException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;

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

