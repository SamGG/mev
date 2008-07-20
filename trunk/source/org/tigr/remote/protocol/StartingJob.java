/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: StartingJob.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:23 $
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
