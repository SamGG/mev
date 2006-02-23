/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: AlgorithmParameters.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:44 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class AlgorithmParameters implements Serializable {

    private Properties properties;

    public AlgorithmParameters() {
        this.properties = new Properties();
    }

    public void setProperty(String name, String value) {
        properties.setProperty(name, value);
    }

    public String getString(String key) {
        return properties.getProperty(key);
    }
    
    public String getString(String key, String defValue) {
        String value = properties.getProperty(key);
        if(value == null)
            return defValue;
        return value;
    }

    public boolean getBoolean(String key) {
        return Boolean.valueOf(properties.getProperty(key)).booleanValue();
    }

    public boolean getBoolean(String key, boolean defValue) {
        String bool = properties.getProperty(key);
        if (bool == null)
            return defValue;
        return Boolean.valueOf(bool).booleanValue();
    }

    public int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public int getInt(String key, int defValue) {
        int value;
        try {
            value = Integer.parseInt(properties.getProperty(key));
        } catch (Exception nfe) {
            return defValue;
        }
        return value;
    }

    public long getLong(String key) {
        return Long.parseLong(properties.getProperty(key));
    }

    public long getLong(String key, long defValue) {
        long value;
        try {
            value = Long.parseLong(properties.getProperty(key));
        } catch (Exception nfe) {
            return defValue;
        }
        return value;
    }

    public float getFloat(String key) {
        return Float.parseFloat(properties.getProperty(key));
    }

    public float getFloat(String key, float defValue) {
        float value;
        try {
            value = Float.parseFloat(properties.getProperty(key));
        } catch (Exception nfe) {
            return defValue;
        }
        return value;
    }

    public URL getURL(String key) throws MalformedURLException {
        return new URL(properties.getProperty(key));
    }

    // util methods 

    public Map getMap() {
        return properties;
    }

    public Set entrySet() {
        return properties.entrySet();
    }
}
