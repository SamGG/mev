/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: Fail.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:24 $
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
