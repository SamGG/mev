/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Node.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster;

import org.tigr.util.ConfMap;

/**
 * This class presents a cluster node.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public class Node {
    
    /**
     * Constructs a <code>Node</code>.
     */
    public Node() {
	this(new NodeValueList());
    }
    
    /**
     * Constructs a <code>Node</code> with specified values.
     *
     * @param values the <code>NodeValueList</code>.
     */
    public Node(NodeValueList values) {
	this(null, values);
    }
    
    /**
     * Constructs a <code>Node</code> with specified values and child nodes list.
     *
     * @param nodeList the <code>NodeList</code>.
     * @param values the <code>NodeValueList</code>.
     */
    public Node(NodeList nodeList, NodeValueList values) {
	this(nodeList, values, null);
    }
    
    /**
     * Constructs a <code>Node</code> with specified values, child nodes list
     * and properties.
     *
     * @param nodeList the <code>NodeList</code>.
     * @param values the <code>NodeValueList</code>.
     * @param map the node properties.
     */
    public Node(NodeList nodeList, NodeValueList values, ConfMap map) {
	this.childNodes = nodeList;
	setProperties(map);
	setValues(values);
    }
    
    /**
     * Constructs a <code>Node</code> with specified indices.
     *
     * @param featuresIndexes the array of integer indices.
     */
    public Node(int[] featuresIndexes) {
	setFeaturesIndexes(featuresIndexes);
    }
    
    /**
     * Returns the node properties.
     */
    public ConfMap getProperties() { return properties;}
    
    /**
     * Sets the node properties.
     * @param map the properties to be set.
     */
    public void setProperties(ConfMap map) { properties = map;}
    
    /**
     * Sets property value by its name.
     *
     * @param name the name of a property.
     * @param value the property value.
     */
    public void setProperty(String name, String value) {
	if (this.properties == null) {
	    this.properties = new ConfMap();
	}
	this.properties.setProperty(name, value);
    }
    
    /**
     * Returns child nodes list.
     */
    public NodeList getChildNodes() { return childNodes;}
    
    /**
     * Sets child node list.
     * @param l the <code>NodeList</code>.
     */
    public void setChildNodes(NodeList l) { childNodes = l;}
    
    /**
     * Returns features indices.
     */
    public int[] getFeaturesIndexes() { return featuresIndexes;}
    
    /**
     * Sets features indices.
     * @param idx the indices.
     */
    public void setFeaturesIndexes( int[] idx ) { featuresIndexes = idx;}
    
    /**
     * Returns probes indices.
     */
    public int[] getProbesIndexes() { return probesIndexes;}
    
    /**
     * Sets probes indices.
     * @param idx the probes indices.
     */
    public void setProbesIndexes( int[] idx ) { probesIndexes = idx;}
    
    /**
     * Returns node values.
     */
    public NodeValueList getValues() { return values;}
    
    /**
     * Sets node values.
     * @param values the values to be set.
     */
    public void setValues(NodeValueList values) { this.values = values;}
    
    private ConfMap properties;
    private NodeList childNodes;
    private NodeValueList values;
    private int[] featuresIndexes;
    private int[] probesIndexes;
}
