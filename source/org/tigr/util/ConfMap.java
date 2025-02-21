/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ConfMap.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:27:01 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class ConfMap extends java.util.Properties {
    
	public ConfMap() {
		super();
	}
	public ConfMap(Properties props) {
		super(props);
	}
	
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
