/*
 * ScriptParameterException.java
 *
 * Created on March 29, 2004, 12:09 PM
 */

package org.tigr.microarray.mev.script.util;

/**
 *
 * @author  braisted
 */
public class ScriptParameterException extends java.lang.Exception {
    
    private String algName;
    private int algIndex;
    private int dataRef;
    private String key;
    private String value;
    private String message;

    public static final int INVALID_KEY_EXCEPTION = 0;
    public static final int MISSING_REQUIRED_PARAMETER_EXCEPTION = 1;
    public static final int INVALID_VALUE_TYPE_EXCEPTION = 2;

    /**
     * Creates a new instance of <code>ScriptParameterException</code> without detail message.
     */
    public ScriptParameterException(String algName, int algIndex, int dataRef, String key, String value, String message) {
        this.algName = algName;
        this.algIndex = algIndex;
        this.dataRef = dataRef;
        this.key = key;
        this.message = message;
    }
        
    /**
     * Constructs an instance of <code>ScriptParameterException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ScriptParameterException(String msg) {
        super(msg);
    }
    
    public String getAlgoritmName() {
        return algName;
    }
    
    public int getAlgorithmIndex() {
        return algIndex;
    }
    
    public int getDataReference() {
        return dataRef;
    }

    public String getKey() {
        return key;
    }

    public String getMessage() {
        return message;
    }
    
}
