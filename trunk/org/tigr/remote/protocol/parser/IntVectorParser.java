/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: IntVectorParser.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.remote.protocol.parser;

import java.util.Iterator;
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

