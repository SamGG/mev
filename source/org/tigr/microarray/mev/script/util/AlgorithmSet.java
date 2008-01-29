/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * AlgorithmSet.java
 *
 * Created on March 17, 2004, 5:47 PM
 */

package org.tigr.microarray.mev.script.util;

import java.util.Vector;

import org.tigr.microarray.mev.cluster.gui.Experiment;

/**
 *
 * @author  braisted
 */
public class AlgorithmSet {
    
    /** Set ID
     */    
    private int id;
    /** Base (source) data node (<CODE>DataNode</CODE>).
     * Each AlgorithmSet has a unique source data node.
     */    
    private DataNode dataNode;
    /** Vector of related <CODE>AlgorithmNode</CODE> objects.
     */    
    private Vector algNodes;
    /** Base Experiment object on which to work.  This is common to the dataNode and is
     * usually populated in real time as the source data is actually created at
     * script execution time.
     */    
    private Experiment experiment;    

    //In the case where algNodes are cluster selection we need to hold
    //the cluster object and the cluster type
    /** Optional cluster indices.  This applies to auto-cluster-selection class algorithms.
     */    
    private int [][] clusters;
    /** Describes the nature of the input data.
     */    
    private int clusterType;
    
    /** Creates a new instance of AlgorithmSet */
    public AlgorithmSet() {
        algNodes = new Vector();
    }
    
    /** Sets the source <CODE>DataNode</CODE>.
     * @param node source node
     */    
    public void setDataNode(DataNode node) {
        dataNode = node;
    }
    
    /** Adds a new algorithm node.
     * @param node Node to add.
     * @return
     */    
    public boolean addAlgorithmNode(AlgorithmNode node) {
        //algorithm is added if the algorithm doesn't exist
        //and either the data node is null or the data node ID
        //matches the algorithm's data ref.
        
        //Might institute a test for matching data ref's in all set algorithms
        if(!algNodes.contains(node)) {
            if(dataNode == null || dataNode.getID() == node.getDataNodeRef()) {
                algNodes.add(node);
                return true;
            }
        }
        return false;
    }
    
    /** Sets the experiment.  (Usually at run time.)
     * @param exp source experiment object.
     */    
    public void setExperiment(Experiment exp) {
        experiment = exp;
    }
    
    /** Returns the set's Experiment.
     */    
    public Experiment getExperiment() {
        return experiment;
    }
    
    /** Sets the ID attribute.
     * @param setID id
     */    
    public void setID(int setID) {
        id = setID;
    }
    
    /** Sets the optional cluster indices.  Used for
     * cluster-selection algorithms.
     * @param clusters cluster indices
     */    
    public void setClusters(int [][] clusters) {
        this.clusters = clusters;
    }
    
    /** Sets the cluster type attribute.  Note that values are
     * held in the <CODE>ScriptConstants</CODE> class.
     * @param type Cluster type attribute, see <CODE>ScriptConstants</CODE> for
     * possible cluster types.
     */    
    public void setClusterType(int type) {
        this.clusterType = type;
    }
    
    /** Returns the set's ID.
     * @return  */    
    public int getID() {
        return id;
    }
    
    /** Returns the number of algorithms in the set.
     */    
    public int getAlgorithmCount() {
        return algNodes.size();
    }
    
    /** Returns an <CODE>AlgorithmNode</CODE> given an index.
     * @param index index
     * @return
     */    
    public AlgorithmNode getAlgorithmNodeAt(int index) {
        return (AlgorithmNode)algNodes.elementAt(index);
    }
    
    /** Returns the set's source <CODE>DataNode</CODE>
     */    
    public DataNode getDataNode() {
        return dataNode;
    }
    
    /** Returns the set's clusters object, possibly null if N/A.
     */    
    public int [][] getClusters() {
        return clusters;
    }
    
    /** Returns the cluster type attribute of the
     * cluster indices.
     * @return  */    
    public int  getClusterType() {
        return clusterType;
    }
}
