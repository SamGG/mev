/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: JobData.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:24 $
 * $Author: braistedj $
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