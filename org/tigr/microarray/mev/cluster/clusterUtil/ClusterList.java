/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: ClusterList.java,v $
 * $Revision: 1.4 $
 * $Date: 2004-07-27 19:58:13 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.clusterUtil;

import java.util.Vector;
import java.awt.Color;

/** The ClusterList class is used to collect and administer
 * a collection of clusters.  The ClusterList object typically is
 * used to collect clusters created from a particular analysis run.
 *
 */
public class ClusterList extends Vector implements java.io.Serializable {        
    public static final long serialVersionUID = 1000102010203020001L;
    /** a String identifing the algorithm origin.
     * (possibly the origin will not be an algorithm)
     */    
    String algorithmName;
    /** result index if applicable
     */    
    int algorithmIndex;
 
    /** Creates new ClusterList */
    public ClusterList() {
        super();
    }
    
    /** Creates a new ClusterList provided an initial
     * cluster count.
     */    
    public ClusterList(int numberOfClusters){
        super(numberOfClusters);
    }
    
    /** Creates a new ClusterList with the provided algorithm (source) name.
     */    
    public ClusterList(String AlgorithmName){
        this.algorithmName = AlgorithmName;
    }
    
    /** Adds a cluster to the list
     */
    public void addCluster(Cluster cluster){
        add(cluster);
    }
    
    /** removes a provided cluster from the list.
     */    
    public void removeCluster(Cluster cluster){
        this.remove(cluster);
    }
    
    /** removes a cluster a a provided position.
     */    
    public void removeClusterAt(int clusterIndex){
        if(isInRange(clusterIndex))
            this.removeElementAt(clusterIndex);
    }
    
    /** returns a cluster at the provied index.
     */
    public Cluster getClusterAt(int clusterIndex){
        if(isInRange(clusterIndex))
            return (Cluster)this.elementAt(clusterIndex);
        return null;
    }
    
    /** Returns the last cluster in the list.
     */    
    public Cluster lastCluster(){
        if(!this.isEmpty())
            return (Cluster)this.lastElement();
        return null;        
    }
    
    /** returns a cluster color given a cluster index.
     * @param clusterIndex
     */    
    public Color getClusterColor(int clusterIndex){
        if(isInRange(clusterIndex))
            return ((Cluster)this.elementAt(clusterIndex)).getClusterColor();
        return null;
    }
    
    /** Returns the cluster name for the specified
     * cluster index.
     *
     */    
    public String getClusterName(int clusterIndex){
        if(isInRange(clusterIndex))
            return ((Cluster)this.elementAt(clusterIndex)).getClusterLabel();
        return null;
    }

    /** Returns clusterID (cluster node) for the
     * specified cluster index.
     */    
    public String getClusterID(int clusterIndex){
        if(isInRange(clusterIndex))
            return ((Cluster)this.elementAt(clusterIndex)).getClusterID();
        return null;
    }
    
    /** Returns clsuter serial number for the specified cluster index
     */    
   public int getClusterSerialNumber(int clusterIndex){
        if(isInRange(clusterIndex))
            return ((Cluster)this.elementAt(clusterIndex)).getSerialNumber();
        return -1;
    }
    
    /** Returns the algorithm name (node name) for the generation of
     * clusters in the ClusterList
     */    
    public String getAlgorithmName(){
        return this.algorithmName;
    }
    
    /** Returns a result index for the ClusterList
     */    
    public int getAlgorithmIndex(){
        return this.algorithmIndex;
    } 
    
    /** Returns true if the index is in range.
     */    
    protected boolean isInRange(int index){
        return (index > -1 && index < size());
    }
    
    /** Returns true if the cluster from the specified clusterID
     * (node name) is already saved
     */    
    public boolean isClusterSaved(String clusterID, int [] indices){              
        for(int i = 0; i < size(); i++){
            if(this.getClusterID(i).equals(clusterID))
                if(this.getCluster(clusterID).doIndicesMatch(indices))
                    return true;                
        }
        return false;
    }
    
    /** Returns true if a cluster exists in the list with the
     * provided serila number.
     */    
        public boolean isClusterSaved(int serialNumber){              
        for(int i = 0; i < size(); i++){
            if(this.getClusterSerialNumber(i) == serialNumber)
                return true;
        }
        return false;
    }
    
    /** Returns the cluster given a clusterID
     */    
    public Cluster getCluster(String clusterID){
        Cluster curr;
        for(int i = 0; i < size(); i++){
            curr = getClusterAt(i);
            if((curr.getClusterID()).equals(clusterID))
                return curr;
        }
        return null;
    }
    
    /** Returns a cluster given a cluster serial number
     * @param serialNumber a cluster serial number
     * @return returned Cluster
     */    
    public Cluster getCluster(int serialNumber){
        Cluster curr;
        for(int i = 0; i < size(); i++){
            curr = getClusterAt(i);
            if(curr.getSerialNumber() == serialNumber)
                return curr;
        }
        return null;
    }
    
    /** Removes a cluster with the specified clusterID
     */    
    public void removeCluster(String clusterID){
        Cluster cluster = getCluster(clusterID);
        if(cluster != null)
            removeCluster(cluster);
    }
    
    /** Removes a cluster with specified serial number
     */    
    public void removeCluster(int serialNumber){
        Cluster cluster = getCluster(serialNumber);
        if(cluster != null)
            removeCluster(cluster);
    }
    
 
}
