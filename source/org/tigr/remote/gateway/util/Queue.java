/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: Queue.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 21:00:01 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.remote.gateway.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Queue implements IQueue {

    /**
     * Constructs a <code>Queue</code>.
     */
    public Queue() {
        list = new ArrayList();
    }

    /**
     * Returns true if this queue is empty.
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * Returns first object from this queue.
     */
    public Object getHead() {
        if (isEmpty()) return null;
        Object head = list.get(0);
        list.remove(0);
        return head;
    }

    /**
     * Adds an object to the end of this queue.
     */
    public void addTail(Object o) {
        list.add(o);
    }

    /**
     * Removes all elements from this queue.
     */
    public void clear() {
        list.clear();
    }

    /**
     * Returns an iterator over the elements in this queue in proper
     * sequence.
     */
    public Iterator iterator() {
        return list.iterator();
    }

    /**
     * Adds collection to the end of this queue.
     */
    public void addTail(Collection col) {
        list.addAll(col);
    }

    /**
     * Adds collection to the head of this queue.
     */
    public void addHead(Collection col) {
        list.addAll(0, col);
    }

    /**
     * Creates clone of this queue.
     */
    public Collection makeCopy() {
        return(Collection)list.clone();
    }

    /**
     * Returns size of this queue.
     */
    public int getSize() { return list.size();}

    private ArrayList list;
}
