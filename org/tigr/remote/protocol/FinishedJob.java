/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: FinishedJob.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:10 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol;

public abstract class FinishedJob {
    
    private String id;
    
    /**
     * Constructs a <code>FinishedJob</code> with specified job id.
     */
    public FinishedJob(String id) {
	this.id = id;
    }
    
    /**
     * Must be overriden to accept the specified JobVisitor.
     */
    public abstract void accept( JobVisitor v );
    
    /**
     * Returns a job id.
     */
    public String getId() { return id;}
    
    /**
     * Sets a job id.
     */
    public void setId(String id) { this.id = id;}
}