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
    
    private int id;
    private DataNode dataNode;
    private Vector algNodes;
    private Experiment experiment;    
    
    /** Creates a new instance of AlgorithmSet */
    public AlgorithmSet() {
        algNodes = new Vector();
    }
    
    public void setDataNode(DataNode node) {
        dataNode = node;
    }
    
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
    
    public void setExperiment(Experiment exp) {
        experiment = exp;
    }
    
    public Experiment getExperiment() {
        return experiment;
    }
    
    public void setID(int setID) {
        id = setID;
    }
    
    public int getID() {
        return id;
    }
    
    public int getAlgorithmCount() {
        return algNodes.size();
    }
    
    public AlgorithmNode getAlgorithmNodeAt(int index) {
        return (AlgorithmNode)algNodes.elementAt(index);
    }
    
    public DataNode getDataNode() {
        return dataNode;
    }
    
}
