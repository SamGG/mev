/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: Message.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:21 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol.communication;

import org.tigr.util.ConfMap;

public class Message {

    /**
     * Constructs a <code>Message</code> with specified from and to parameters.
     */
    public Message( String from, String to ) {
        m_from = from;
        m_to = to;
        m_properties = new ConfMap();
    }

    /**
     * Constructs a <code>Message</code> with specified from, to parameters
     * and configuration.
     */
    public Message( String from, String to, ConfMap properties ) {
        m_from = from;
        m_to = to;
        m_properties = properties;
    }

    public String  getFrom() { return m_from;}
    public String  getTo() { return m_to;}
    public ConfMap getProperties() { return m_properties;}

    private String  m_from;
    private String  m_to;
    private ConfMap m_properties;
}
