/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: FailedJob.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:10 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol;

public class FailedJob extends FinishedJob {

    /**
     * Constructs a <code>FailedJob</code> with specified id and 
     * an error description.
     * @see Fail
     */
    public FailedJob(String id, Fail fail) {
        super( id );
        this.fail = fail;
    }

    /**
     * Accepts a <code>JobVisitor</code>.
     * @see JobVisitor
     */
    public void accept( JobVisitor v ) {
        v.visitFailedJob( this );
    }

    /**
     * Returns an error description.
     */
    public Fail getFail() { return fail;}


    /**
     * Sets an error description.
     */
    public void setFail(Fail fail) { this.fail = fail;}

    private Fail fail;
}