/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: StopJob.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.remote.protocol;

public class StopJob extends StartingJob {
    
    /**
     * Constructs an instance of <code>StopJob</code> with specified id.
     */
    public StopJob(String id) {
	super( id );
    }
    
    /**
     * Accepts a <code>StartingJobVisitor</code>.
     * @see StartingJobVisitor#visitStopJob
     */
    public void accept( StartingJobVisitor v ) {
	v.visitStopJob( this );
    }
}
