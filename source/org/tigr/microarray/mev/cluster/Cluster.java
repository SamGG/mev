/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: Cluster.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:16:48 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster;

/**
 * This class is used to store a cluster data.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public class Cluster {
    
    /**
     * Constructs a <code>Cluster</code>.
     */
    public Cluster() {
	nodes = new NodeList();
    }
    
    /**
     *  Returns the cluster node list.
     */
    public NodeList getNodeList() { return nodes;}
    
    /**
     * Sets a node list for this cluster.
     *
     * @param nodes the <code>NodeList</code>.
     */
    public void setNodeList( NodeList nodes ) { this.nodes = nodes;}
    
    private NodeList nodes;
}
