/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Request.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:10 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol;


public class Request {
    
    /**
     * Constructs a <code>Request</code> with specified <code>StartingJob</code>.
     */
    public Request(StartingJob job) { setJob( job );}
    
    /**
     * Returns reference to a <code>StartingJob</code>.
     */
    public StartingJob getJob() { return job;}
    
    /**
     * Sets a specified <code>StartingJob</code>.
     */
    public void setJob( StartingJob job ) { this.job = job;}
    
    private StartingJob job;
}
