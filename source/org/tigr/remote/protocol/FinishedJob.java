/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: FinishedJob.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:24 $
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
