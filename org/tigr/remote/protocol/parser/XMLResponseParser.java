/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: XMLResponseParser.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:08 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol.parser;

import org.tigr.util.ConfMap;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class XMLResponseParser extends Parser {

    /**
     * Creates a <code>XMLResponseParser</code> with specified configuration.
     */
    public XMLResponseParser( ConfMap cfg ) {
        this.cfg = cfg;
    }

    /**
     * Parses the specified input stream and returns a <code>Response</code>.
     */
    public org.tigr.remote.protocol.Response parseResponse( java.io.InputStream in ) throws ParserException {
        try {
            XMLReader reader = ParserUtil.createReader( cfg );
            SAXResponseHandler handler = new SAXResponseHandler( cfg );
            reader.setContentHandler( handler );
            reader.setErrorHandler( handler );
            reader.parse( new InputSource(in) );

            return handler.getResponse();
        } catch (Exception e) {
            throw new ParserException("XML response parsing error", e );
        }
    }

    private ConfMap cfg;
}

