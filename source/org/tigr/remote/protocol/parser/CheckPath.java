/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: CheckPath.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 21:00:02 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.remote.protocol.parser;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.xml.sax.SAXException;

class CheckPath {

    /**
     * Constructs a <code>CheckPath</code> with soecified linked list.
     */
    public CheckPath(LinkedList stack) {
        m_stack = stack;
    }

    /**
     * Checkes elements path from top of the linked list.
     * @throws SAXException if path is uncorrect.
     */
    public void checkFromTopThrow( String[] elements ) throws SAXException  {
        if (!checkFromTop( elements ))
            throw new SAXException("Invalid document structure: " + m_stack.getLast() );
    }

    /**
     * Checkes elements path from bottom of the linked list.
     * @throws SAXException if path is uncorrect.
     */
    public void checkFromBottomThrow( String[] elements ) throws SAXException {
        if (!checkFromBottom( elements ))
            throw new SAXException("Invalid document structure: " + m_stack.getLast());
    }

    /**
     * Checkes elements path from top of the linked list.
     * @return true if specified path is correct.
     */
    public boolean checkFromTop( String[] elements ) {
        Iterator iter = m_stack.iterator();
        int i = 0;
        int size = elements.length;
        while (i < size && iter.hasNext()) {
            String el = (String)iter.next();
            if (!el.equals(elements[i])) return false;
            i++;
        }
        if (!iter.hasNext() && i < size)
            return false;
        else
            return true;
    }

    /**
     * Checkes elements path from bottom of the linked list.
     * @return true if specified path is correct.
     */
    public boolean checkFromBottom( String[] elements ) {
        ListIterator iter = m_stack.listIterator(  m_stack.size() );
        int size = elements.length;
        int i = 0;
        if (elements.length == 0) return false;

        while (i < size && iter.hasPrevious()) {
            String el = (String)iter.previous();
            if (!el.equals(elements[i])) return false;
            i++;
        }
        if (!iter.hasPrevious() && i < size)
            return false;
        else
            return true;
    }

    private LinkedList m_stack;
}

