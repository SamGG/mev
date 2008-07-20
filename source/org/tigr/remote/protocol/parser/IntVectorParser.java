/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: IntVectorParser.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:22 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol.parser;

import java.util.StringTokenizer;

import org.tigr.util.IntArray;

class IntVectorParser {

    /**
     * Constructs a <code>IntVectorParser</code>
     */
    public IntVectorParser() {}

    /**
     * Parses space separated string of integers.
     */
    public int[] parse( String str ) throws ParserException {
        IntArray array = new IntArray(100);
        StringTokenizer st = new StringTokenizer(str);
        String token = null;
        try {
            while (st.hasMoreTokens()) {
                token = st.nextToken();
                array.add(Integer.parseInt(token));
            }
        } catch (NumberFormatException ex) {
            throw new ParserException( "Cannot parse " + token + " as integer value ", ex );
        }
        return array.toArray();
    }
}

