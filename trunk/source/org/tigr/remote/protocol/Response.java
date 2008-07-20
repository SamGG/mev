/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: Response.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:24 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol;

public class Response {
    
    /**
     * Constructs a <code>Response</code> with specified <code>FinishedJob</code>.
     * @see FinishedJob
     */
    public Response(FinishedJob job) { setJob( job );}
    
    /**
     * Returns reference to a <code>FinishedJob</code>.
     */
    public FinishedJob getJob() { return job;}
    
    /**
     * Sets a specified <code>FinishedJob</code>.
     */
    public void setJob( FinishedJob job ) { this.job = job;}
    
    private FinishedJob job;
}
