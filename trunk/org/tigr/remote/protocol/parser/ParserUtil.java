/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ParserUtil.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:08 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol.parser;

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

import org.tigr.util.ConfMap;

class ParserUtil {
    /**
     * Creates a xml reader by specified configuration.
     */
    public static XMLReader createReader( ConfMap cfg ) throws SAXException {
        String driver = cfg.getProperty("org.xml.sax.driver", "org.apache.xerces.parsers.SAXParser" );
        return XMLReaderFactory.createXMLReader( driver );
    }
}

