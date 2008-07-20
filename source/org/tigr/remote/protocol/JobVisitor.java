/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: JobVisitor.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:24 $
 * $Author: braistedj $
 * $State: Exp $
 */

package org.tigr.remote.protocol;

public interface JobVisitor {
    /**
     * Invoked to accept an instance of <code>SuccessfulJob</code>.
     * @see SuccessfulJob
     */
    public void visitSuccessfulJob( SuccessfulJob job );
    
    /**
     * Invoked to accept an instance of <code>FailedJob</code>.
     * @see FailedJob
     */
    public void visitFailedJob( FailedJob job );
    
    /**
     * Invoked to accept an instance of <code>ExecutedJob</code>.
     * @see ExecutedJob
     */
    public void visitExecutedJob( ExecutedJob job );
}
