/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: StartingJobVisitor.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:24 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol;

public interface StartingJobVisitor {
    /**
     * Visits a start job.
     */
    public void visitStartJob( StartJob job );
    
    /**
     * Visits a stop job.
     */
    public void visitStopJob( StopJob job );
}