/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
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