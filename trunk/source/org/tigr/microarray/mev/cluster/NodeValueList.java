/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: NodeValueList.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:16:48 $
 * $Author: braistedj $
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

