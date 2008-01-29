/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: IQueue.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 21:00:01 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.remote.gateway.util;

import java.util.Collection;
import java.util.Iterator;

public interface IQueue {
    /**
     * Returns true if queue is empty.
     */
    public boolean isEmpty();

    /**
     * Returns head object of queue.
     */
    public Object getHead();

    /**
     * Adds an object to tail of queue.
     */
    public void addTail(Object o);

    /**
     * Clears queue.
     */
    public void clear();

    /**
     * Returns queue iterator.
     */
    public Iterator iterator();

    /**
     * Adds specified collection to the tail of queue.
     */
    public void addTail(Collection col);

    /**
     * Adds specified collection to the head of queue.
     */
    public void addHead(Collection col);

    /**
     * Returns queue copy as a Collection.
     */
    public Collection makeCopy();

    /**
     * Returns queue size.
     */
    public int getSize();
}

