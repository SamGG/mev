/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Util.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:35:13 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol.serializer;

import java.util.Date;
import java.text.SimpleDateFormat;

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
