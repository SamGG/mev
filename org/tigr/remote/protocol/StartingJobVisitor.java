/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: StartingJobVisitor.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
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