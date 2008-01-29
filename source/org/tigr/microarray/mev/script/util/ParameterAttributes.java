/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * ParameterAttributes.java
 *
 * Created on May 27, 2004, 9:29 AM
 */

package org.tigr.microarray.mev.script.util;

/** Encapsulates the attributes of a given parameter.
 * ParameterAttributes is used in conjuction with the
 * parameter validation to exchange parameter attributes.
 * @author braisted
 */
public class ParameterAttributes {
    
    
    /** parameter key used in scripting to refer to the parameter
     */
    String parameterKey;
    /** Type of value (int, float, string, double, long)
     *
     */
    String valueType;
    /** Required or dependent on other settings.
     */
    String valueRequirementLevel;
    
    /** Returns true if the parameter has constraing limits.
     */
    boolean hasConstraints;
    /** If have constraints, min is storred.
     */
    String min;
    /** Min value
     */
    String max;
    
    
    /** Creates a new instance of ParameterAttributes
     * @param key key values
     * @param type value type
     * @param level level of requirement.
     */
    public ParameterAttributes(String key, String type, String level) {
        hasConstraints = false;
        
        parameterKey = key;
        valueType = type;
        valueRequirementLevel = level;
    }
    
    
    /** Creates a new instance of ParameterAttributes
     * @param key parameter key
     * @param type parameter type
     * @param level requirement level
     * @param min optional min
     * @param max optional max
     */
    
    public ParameterAttributes(String key, String type, String level, String min, String max) {
        hasConstraints = true;
        this.min = min;
        this.max = max;
        
        parameterKey = key;
        valueType = type;
        valueRequirementLevel = level;
    }
    
    /** returns true if it's a parameter match.
     * @param key Key
     * @return
     */
    public boolean isParameter(String key) {
        return (key.equals(parameterKey));
    }
    
    /** Returns true if parameter has constraints.
     */
    public boolean hasConstraints() {
        return hasConstraints;
    }
    
    /** Returns the parameter key.
     * @return
     */
    public String getParameterKey() {
        return parameterKey;
    }
    
    /** Returns the value type
     */
    public String getValueType() {
        return valueType;
    }
    
    /** Returns the requirement level.
     */
    public String getValueRequirementLevel() {
        return valueRequirementLevel;
    }
    
    /** Returns the min
     */
    public String getMin() {
        return min;
    }
    
    /** Returns the max
     */
    public String getMax() {
        return max;
    }
}
