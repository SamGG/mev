/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: NodeList.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:16:48 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster;

import java.util.ArrayList;

/**
 * This class is used to store cluster child nodes.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public class NodeList {
    
    private ArrayList list;
    
    /**
     * Constructs a <code>NodeList</code>
     */
    public NodeList() {
	list = new ArrayList();
    }
    
    /**
     * Returns a node by specified index.
     * @param index the node index.
     */
    public Node getNode(int index) {
	return(Node)list.get(index);
    }
    
    /**
     * Adds specified node to the list.
     * @param node the node to be added.
     */
    public void addNode(Node node) {
	list.add(node);
    }
    
    /**
     * Removes node from the list by specified index.
     * @param index the index of node to be removed.
     */
    public Node remove(int index) {
	return(Node)list.remove(index);
    }
    
    /**
     * Returns size of the node list.
     */
    public int getSize() {
	return list.size();
    }
    
    /**
     * Increases the capacity of this <code>NodeList</code> instance, if
     * necessary, to ensure that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity.
     */
    public void ensureCapacity(int minCapacity) {
	list.ensureCapacity(minCapacity);
    }
}
