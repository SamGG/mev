/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Fail.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:10 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol;

public class Fail {
    
    /**
     * Constructs a <code>Fail</code> with specified id and description.
     */
    public Fail(String id, String reason) {
	this.id = id;
	this.description = reason;
    }
    
    /**
     * Returns the fail id.
     */
    public String getId() { return id;}
    
    /**
     * Returns the fail description.
     */
    public String getDescription() { return description;}
    
    /**
     * Sets the fail id.
     */
    public void setId(String id) { this.id = id;}
    
    /**
     * Sets the fail description.
     */
    public void setDescription(String data) { this.description = data;}
    
    private String id;
    private String description;
}