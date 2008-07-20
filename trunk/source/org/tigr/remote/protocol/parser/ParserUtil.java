/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ParserUtil.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 21:00:02 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.remote.protocol.parser;

import org.tigr.util.ConfMap;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

class ParserUtil {
    /**
     * Creates a xml reader by specified configuration.
     */
    public static XMLReader createReader( ConfMap cfg ) throws SAXException {
        String driver = cfg.getProperty("org.xml.sax.driver", "org.apache.xerces.parsers.SAXParser" );
        return XMLReaderFactory.createXMLReader( driver );
    }
}

