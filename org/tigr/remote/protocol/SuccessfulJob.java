/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SuccessfulJob.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:10 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol;

public class SuccessfulJob extends FinishedJob {
    
    /**
     * Constructs a <code>SuccessfulJob</code> with specified id and data.
     */
    public SuccessfulJob(String id, JobData data) {
	super( id );
	this.data = data;
    }
    
    /**
     * Accepts a <code>JobVisitor</code>.
     * @see JobVisitor
     */
    public void accept( JobVisitor v ) {
	v.visitSuccessfulJob( this );
    }
    
    /**
     * Returns an algorithm result.
     */
    public JobData getData() { return data;}
    
    /**
     * Sets an algorithm result.
     */
    public void setData(JobData data) { this.data = data;}
    
    private JobData data;
}