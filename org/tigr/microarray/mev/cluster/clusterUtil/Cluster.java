/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: Cluster.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:25 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.clusterUtil;

import java.awt.Color;
import java.util.HashSet;
import javax.swing.tree.DefaultMutableTreeNode;
import org.tigr.microarray.mev.cluster.gui.Experiment;

/** The Cluster class encapsulates information required to define a cluster.
 * Methods include standard set... and get... methods to access and alter
 * the cluster definition.
 */
public class Cluster {

    public static final int GENE_CLUSTER = 0;
    public static final int EXPERIMENT_CLUSTER = 1;
    
    /** Cluster color
     */    
    private Color clusterColor;
    /** Element indices in IData.
     */    
    private int [] indices;
    /** Cluster source type
     */    
    private String source;
    /** Cluster origin node name
     */    
    private String clusterID;
    /** User defined cluster label
     */    
    private String clusterLabel;
    /** Algorithm node name
     */    
    private String algorithmName;
    /** Cluster remarks/description
     */    
    private String clusterDescription;
    /** Result index on result tree.
     */    
    private int algorithmIndex;
    /** Cluster sequential serial numbe
     */    
    private int serialNumber;
    /** Cluster node of origin, possibly null.
     */    
    private DefaultMutableTreeNode node;
    /** Cluster's Experiment of origin
     */
    private Experiment experiment;

    /** Creates new cluster object
     */
    public Cluster(int [] indices, String source, String clusterLabel, String algorithmName, String clusterID, String clusterDescription, int index, int serialNumber, Color clusterColor, Experiment experiment) {
        this.indices = indices;
        this.source = source;
        this.clusterLabel = clusterLabel;
        this.clusterID = clusterID;
        this.algorithmName = algorithmName;
        this.algorithmIndex = index;
        this.clusterColor = clusterColor;
        this.clusterDescription = clusterDescription;
        this.serialNumber = serialNumber;
        this.experiment = experiment;
    }
    
        /** Creates new cluster object
         */
    public Cluster(int [] indices, String source, String clusterLabel, String algorithmName, String clusterID, String clusterDescription, int index, int serialNumber, Color clusterColor, DefaultMutableTreeNode node, Experiment experiment) {
        this.indices = indices;
        this.source = source;
        this.clusterLabel = clusterLabel;
        this.clusterID = clusterID;
        this.algorithmName = algorithmName;
        this.algorithmIndex = index;
        this.clusterColor = clusterColor;
        this.clusterDescription = clusterDescription;
        this.serialNumber = serialNumber;
        this.node = node;
        this.experiment = experiment;
    }
    
    /** Returns cluster color
     */    
    public Color getClusterColor(){ return this.clusterColor; }
    /** Returns cluster indices
     */    
    public int []  getIndices(){ return this.indices; }
    /** Returns source type
     */    
    public String getSource(){ return this.source; }
    /** Get user defined cluser label
     */    
    public String getClusterLabel(){ return this.clusterLabel; }
    /** Returns cluster node name.
     */    
    public String getClusterID(){ return this.clusterID; }
    /** Returns algorithm node name
     */    
    public String getAlgorithmName(){ return this.algorithmName; }
    /** Returns cluster description/remarks
     */    
    public String getClusterDescription(){ return this.clusterDescription; }
    /** Returns result index
     */    
    public int getAlgorithmIndex(){ return this.algorithmIndex; }
    /** Returns cluster serial number
     */    
    public int getSerialNumber(){ return this.serialNumber; }
    /** Returns the population of the cluster
     */    
    public int getSize(){ return this.indices.length; }
    /** Returns the node of origin or null if N/A
     */    
    public DefaultMutableTreeNode getNode(){ return node; }
    /** Returns the cluster's experiment
     */
    public Experiment getExperiment(){ return experiment; }
    
    /** Sets cluster color
     */    
    public void setClusterColor(Color color){ this.clusterColor = color; }
    /** Sets cluster label
     * @param clusterLabel Cluster label
     */    
    public void setClusterLabel(String clusterLabel){ this.clusterLabel = clusterLabel; }
    /** Sets cluster description
     */    
    public void setClusterDescription(String clusterDescription){ this.clusterDescription = clusterDescription; }
    /** Sets node of origin or reference node.
     * @param myNode node
     */    
    public void setNode(DefaultMutableTreeNode myNode){ node = myNode; }
    /** Sets the Experiment for the cluster
     */
    public void setExperiment(Experiment experiment){ this.experiment = experiment; }
    
    /** Returns true if supplied element index is a
     * member of the cluster
     * @param index Element index
     * @return Returns boolean indicating membership
     */    
    public boolean isMember(int index){
        for(int i = 0; i < this.indices.length; i++)
            if(index == indices[i])
                return true;
        return false;
    }
    
    /** Removes an element index from cluster
     * membership.
     * @param memberIndex Index of member to remove.
     */    
    public boolean removeMember(int memberIndex){

        if(!isMember(memberIndex))
            return false;
        
        int [] newIndices = new int[this.indices.length - 1];
        int cnt = 0;
        for(int i = 0; i < indices.length; i++){
            if(indices[i] != memberIndex){
                newIndices[cnt] = indices[i];
                cnt++;
            }
        }
        return true;
    }
    
    /** Returns a Hashset which contains the indices
     * of the cluster
     */    
    public HashSet getHashSet(){
        HashSet set = new HashSet(indices.length);
        for(int i = 0; i < indices.length; i++){
            set.add(new Integer(indices[i]));
        }
        return set;
    }  
    
    /** Returns true if indices are a match
     *
     */
    public boolean doIndicesMatch(int [] indices){
        if(indices.length != this.indices.length)
            return false;        
        HashSet clusterSet = getHashSet();
        HashSet testSet = makeHashSet(indices);
        return clusterSet.containsAll(testSet);
    }
    
    private HashSet makeHashSet(int [] indices){
        HashSet set = new HashSet(indices.length);
        for( int i = 0; i < indices.length; i++ ){
            set.add(new Integer(indices[i]));
        }
        return set;
    }
}
