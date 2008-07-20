/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ParserException.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:23 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol.parser;

import org.tigr.remote.RemoteException;

public class ParserException extends RemoteException {

    /**
     * Constructs a <code>ParserException</code> with 
     * specified detail message.
     */
    public ParserException( String message ) {
        super( message );
    }

    /**
     * Constructs a <code>ParserException</code> with a
     * specified detail message and an intitial exception.
     */
    public ParserException( String message, Exception exception ) {
        super( message, exception );
    }
}

