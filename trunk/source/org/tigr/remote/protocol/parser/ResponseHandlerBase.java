/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ResponseHandlerBase.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:23 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol.parser;

import java.util.LinkedList;

import org.tigr.util.ConfMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

class ResponseHandlerBase extends DefaultHandler {

    // elements stack
    private LinkedList m_elements = new LinkedList();
    // XML elements ( path ) checker
    protected CheckPath m_path = new CheckPath( m_elements );
    //  Configuration info
    protected ConfMap m_config;

    /**
     * Constructs a <code>ResponseHandlerBase</code> with specified configuration.
     */
    public ResponseHandlerBase(ConfMap cfg) {
        m_config = cfg;
    }

    /**
     * An element is started.
     */
    public void startElement(String uri, String localName,
                             String qName, Attributes attributes) throws SAXException {
        m_elements.addLast( qName );
    }

    /**
     * An element is finished.
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        m_elements.removeLast();
    }

    // ErrorHandler interface
    public void warning(SAXParseException ex) throws SAXException {
        processError( new Exception("[Warning] "+
                                    getLocationString(ex)+": "+
                                    ex.getMessage()) );
    }

    public void error(SAXParseException ex) throws SAXException {
        processError( new Exception("[Error] "+
                                    getLocationString(ex)+": "+
                                    ex.getMessage()) );
    }

    public void fatalError(SAXParseException ex) throws SAXException {
        processError( new Exception("[FatalError] "+
                                    getLocationString(ex)+": "+
                                    ex.getMessage()));
    }

    /**
     * Returns location of an exception.
     */
    private String getLocationString(SAXParseException ex) {
        StringBuffer str = new StringBuffer();

        String systemId = ex.getSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1)
                systemId = systemId.substring(index + 1);
            str.append(systemId);
        }
        str.append(':');
        str.append(ex.getLineNumber());
        str.append(':');
        str.append(ex.getColumnNumber());

        return str.toString();
    }

    /**
     * Returns a current element.
     */
    protected String getCurrentElement() {
        return(String)m_elements.getLast();
    }

    /**
     * Converts an error into a <code>SAXException</code>.
     */
    protected void processError( Exception ex ) throws SAXException {
        throw new SAXException( new ParserException("Processing error", ex) );
    }
}

