/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: JobData.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.remote.protocol;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;

public class JobData {
    
    /**
     * Constructs a <code>JobData</code> with a specified algorithm data.
     */
    public JobData( AlgorithmData data) {
	this.m_data = data;
    }
    
    /**
     * Returns an algorithm data.
     */
    public AlgorithmData getData() { return m_data;}
    
    /**
     * Sets an algorithm data.
     */
    public void setData( AlgorithmData data ) { m_data = data;}
    
    private AlgorithmData m_data;
    
}