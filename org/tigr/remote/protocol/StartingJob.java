/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: StartingJob.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:10 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol;

public abstract class StartingJob {
    
    private String id;
    
    /**
     * Constructs a <code>StartingJob</code> with specified id.
     */
    public StartingJob( String id ) {
	this.id = id;
    }
    
    /**
     * Returns a job id.
     */
    public String getId() { return id;}
    
    /**
     * Must be overriden to accept a <code>StartingJobVisitor</code>.
     * @see StartingJobVisitor
     */
    public abstract void accept( StartingJobVisitor v );
}
