/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: StartingJobVisitor.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:10 $
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