/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ParserUtil.java,v $
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

class ParserUtil {
    /**
     * Creates a xml reader by specified configuration.
     */
    public static XMLReader createReader( ConfMap cfg ) throws SAXException {
        String driver = cfg.getProperty("org.xml.sax.driver", "org.apache.xerces.parsers.SAXParser" );
        return XMLReaderFactory.createXMLReader( driver );
    }
}

