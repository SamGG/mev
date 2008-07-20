/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: StopJob.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:24 $
 * $Author: braistedj $
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
