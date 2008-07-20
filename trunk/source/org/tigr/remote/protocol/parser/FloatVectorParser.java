/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: FloatVectorParser.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:23 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol.parser;

import java.util.StringTokenizer;

import org.tigr.util.FloatArray;

class FloatVectorParser {

    /**
     * Constructs a <code>FloatVectorParser</code>
     */
    public FloatVectorParser() {}

    /**
     * Parses space separated string of floats.
     */
    public float[] parse( String str ) throws ParserException {
        FloatArray array = new FloatArray(100);
        StringTokenizer st = new StringTokenizer(str);
        String token = null;
        try {
            while (st.hasMoreTokens()) {
                token = st.nextToken();
                array.add(Float.parseFloat(token));
            }
        } catch (NumberFormatException ex) {
            throw new ParserException( "Cannot parse " + token + " as float value ", ex );
        }
        return array.toArray();
    }
}
