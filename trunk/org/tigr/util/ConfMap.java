/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ConfMap.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.util;

import java.net.URL;
import java.net.MalformedURLException;
import java.text.ParseException;

public class ConfMap extends java.util.Properties {
    
    public String getString(String key) {
	return getProperty(key);
    }
    
    public boolean getBoolean(String key) {
	return Boolean.valueOf(getProperty(key)).booleanValue();
    }
    
    public boolean getBoolean(String key, boolean defValue) {
	String bool = getProperty(key);
	if (bool == null)
	    return defValue;
	return Boolean.valueOf(bool).booleanValue();
    }
    
    public int getInt(String key) {
	return Integer.parseInt(getProperty(key));
    }
    
    public int getInt(String key, int defValue) {
	int value;
	try {
	    value = Integer.parseInt(getProperty(key));
	} catch (Exception nfe) {
	    return defValue;
	}
	return value;
    }
    
    public long getLong(String key) {
	return Long.parseLong(getProperty(key));
    }
    
    public long getLong(String key, long defValue) {
	long value;
	try {
	    value = Long.parseLong(getProperty(key));
	} catch (Exception nfe) {
	    return defValue;
	}
	return value;
    }
    
    public float getFloat(String key) {
	return Float.parseFloat( getProperty(key) );
    }
    
    public float getFloat(String key, float defValue) {
	float value;
	try {
	    value = Float.parseFloat( getProperty(key) );
	} catch (Exception nfe) {
	    return defValue;
	}
	return value;
    }
    
    public URL getURL(String key) throws MalformedURLException {
	return new URL(getProperty(key));
    }
    
}
