/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: StartJob.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:10 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol;

public class StartJob extends StartingJob {
    
    /**
     * Constructs a <code>StartJob</code> with specified id, data and
     * name of an algorithm.
     */
    public StartJob(String id, JobData data, String name) {
	super( id );
	this.data = data;
	this.name = name;
    }
    
    /**
     * Returns stored data.
     */
    public JobData getData() { return data;}
    
    /**
     * Returns name of the algorithm.
     */
    public String getType() {return name;}
    
    /**
     * Sets an algorithm data.
     */
    public void setData( JobData data ) { this.data = data;}
    
    /**
     * Accepts a <code>StartingJobVisitor</code>.
     * @see StartingJobVisitor
     */
    public void accept( StartingJobVisitor v ) {
	v.visitStartJob( this );
    }
    
    private String name;
    private JobData data;
}
