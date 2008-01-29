/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
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