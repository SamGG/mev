/*
 * AlgorithmNode.java
 *
 * Created on February 28, 2004, 4:25 PM
 */

package org.tigr.microarray.mev.script.util;


import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;

/**
 *
 * @author  braisted
 */
public class AlgorithmNode extends ScriptNode {
    
    String name;
    int id;
    int dataNodeRef;
    String algType;
    AlgorithmData data;
    
    AlgorithmNode nextNode;
    
    /** Creates a new instance of AlgorithmNode */
    public AlgorithmNode() {    
    }
    
    public AlgorithmNode(String name, int id, int dataNodeRef, String algType) {
        this.name = name; 
        this.id = id; 
        this.dataNodeRef = dataNodeRef;
        this.algType = algType;
    }

    public String toString() {
        return name;
    }
    
    public int getID() {
        return id;
    }
    
    public void setID(int ID) {
        id = ID;
    }    
    
    public void setName(String Name) {
        name = Name;
    }
    
    public String getAlgorithmName() {
        return name;
    }
    
    public int getDataNodeRef() {
        return dataNodeRef;
    }
    
    public void setDataNodeRef(int ref) {
        dataNodeRef = ref;
    }
    
    public AlgorithmData getAlgorithmData() {
        return data;
    }
    
    public void setAlgorithmData(AlgorithmData aData) {
        data = aData;
    }
    
    public void setNextNode(AlgorithmNode node) {
        nextNode = node;
    }
    
    public AlgorithmNode nextNode() {
        return nextNode;
    }
    
    public void setAlgorithmType(String type) {
        algType = type;
    }

    public String getAlgorithmType() {
        return algType;
    }
}
