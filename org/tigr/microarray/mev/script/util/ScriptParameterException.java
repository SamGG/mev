/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * ScriptParameterException.java
 *
 * Created on March 29, 2004, 12:09 PM
 */

package org.tigr.microarray.mev.script.util;

/** Encapsulates information about script parse errors or parameter
 * errors.
 * @author braisted
 */
public class ScriptParameterException extends java.lang.Exception {
    
    /** algorithm name
     */    
    private String algName;
    /** algorithm index, for possitive id with algName
     */    
    private int algIndex;
    /** Input data reference to the algorithm generating
     * the exception
     */    
    private int dataRef;
    /** parameter key
     */    
    private String key;
    /** parameter value
     */    
    private String value;
    /** Error message
     */    
    private String message;

    /** Exception class
     */    
    public static final int INVALID_KEY_EXCEPTION = 0;
    /** exception class
     */    
    public static final int MISSING_REQUIRED_PARAMETER_EXCEPTION = 1;
    /** exception class
     */    
    public static final int INVALID_VALUE_TYPE_EXCEPTION = 2;

    /** Creates a new instance of <code>ScriptParameterException</code> without detail message.
     * @param algName algorithm name
     * @param algIndex algorithm index
     * @param dataRef input data reference
     * @param key parameter key
     * @param value parameter value
     * @param message error message
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
    
    /** Returns algorithm name
     */    
    public String getAlgoritmName() {
        return algName;
    }
    
    /** Returns alg index
     */    
    public int getAlgorithmIndex() {
        return algIndex;
    }
    
    /** Returns data reference
     */    
    public int getDataReference() {
        return dataRef;
    }

    /** returns algorithm key
     */    
    public String getKey() {
        return key;
    }

    /** returns the error message
     */    
    public String getMessage() {
        return message;
    }
    
}
