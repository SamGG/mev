/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ExecutedJob.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:24 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;

public class ExecutedJob extends FinishedJob {
    
    /**
     * Constructs an <code>ExecutedJob</code> with specified id and
     * an algorithm event.
     */
    public ExecutedJob(String id, AlgorithmEvent ev ) {
	super( id );
	setEvent( ev );
    }
    
    /**
     * Accepts a <code>JobVisitor</code>.
     * @see JobVisitor
     */
    public void accept( JobVisitor v ) {
	v.visitExecutedJob( this );
    }
    
    /**
     * Returns an algorithm event.
     */
    public AlgorithmEvent getEvent() {
	return m_event;
    }
    
    /**
     * Sets an algorithm event.
     */
    public void setEvent(AlgorithmEvent e) {
	this.m_event = e;
    }
    
    private AlgorithmEvent m_event;
}