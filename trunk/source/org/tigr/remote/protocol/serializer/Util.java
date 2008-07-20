/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: Util.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 21:00:02 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.remote.protocol.serializer;

import java.text.SimpleDateFormat;
import java.util.Date;

class Util {
    public static final String NULL = "";

    /**
     * Escapes the following simbols: <>\&" from a string.
     */
    public static String escape( String s ) {
        if (s == null) return NULL;
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else if (c == '\'') {
                sb.append("&apos;");
            } else if (c == '&') {
                sb.append("&amp;");
            } else if (c == '"') {
                sb.append("&quote;");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Converts a specified date into a string.
     */
    public static String escape( Date date ) {
        if (date == null) return NULL;
        return s_xmlDateFormat.format( date );
    }

    /**
     * Converts an integer to a string.
     */
    public static String escape( int i ) {
        return String.valueOf( i );
    }

    /**
     * Converts a float to a string.
     */
    public static String escape( float f ) {
        return String.valueOf( f );
    }

    private static SimpleDateFormat s_xmlDateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'-00:00-'z");
}
