/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: NodeValue.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster;

/**
 * This structure is used to store a cluster node value.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public class NodeValue {
    
    /**
     * Constructs a <code>NodeValue</code> with specified name and value.
     *
     * @param name the name of the value.
     * @param value the reference to an value object.
     */
    public NodeValue(String name, Object value) {
	this(name, value, null);
    }
    
    /**
     * Constructs a <code>NodeValue</code> with specified name, value and
     * description.
     *
     * @param name the name of the value.
     * @param value the reference to an value object.
     * @param description the value description.
     */
    public NodeValue(String name, Object value, String description) {
	this.name = name;
	this.value = value;
	this.description = description;
    }
    
    public String name;
    public Object value;
    public String description;
    
}
