/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: NodeValueList.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster;

import java.util.ArrayList;

/**
 * The list of a cluster node values.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public class NodeValueList {
    
    private ArrayList list;
    
    /**
     * Constructs a <code>NodeValueList</code> with default capacity.
     */
    public NodeValueList() {
	this(10);
    }
    
    /**
     * Constructs a <code>NodeValueList</code> with specified capacity.
     *
     * @param initialCapacity the desired capacity.
     */
    public NodeValueList(int initialCapacity) {
	list = new ArrayList(initialCapacity);
    }
    
    /**
     * Returns node value by specified index.
     * @param index the index of node.
     */
    public NodeValue getNodeValue(int index) {
	return(NodeValue)list.get(index);
    }
    
    /**
     * Adds node value to the list.
     * @param nodeValue the value to be added.
     */
    public void addNodeValue(NodeValue nodeValue) {
	list.add(nodeValue);
    }
    
    /**
     * Removes node value by specified index.
     * @param index the index of node to be removed.
     */
    public NodeValue remove(int index) {
	return(NodeValue)list.remove(index);
    }
    
    /**
     * Returns size of the list.
     */
    public int getSize() {
	return list.size();
    }
    
    /**
     * Removes all of the values from this list.
     */
    public void clear() {
	list.clear();
    }
    
}

