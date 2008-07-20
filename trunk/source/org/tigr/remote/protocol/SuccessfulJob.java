/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SuccessfulJob.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:24 $
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
