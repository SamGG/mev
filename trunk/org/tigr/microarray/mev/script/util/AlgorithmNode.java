/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * AlgorithmNode.java
 *
 * Created on February 28, 2004, 4:25 PM
 */

package org.tigr.microarray.mev.script.util;


import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;

/** Extension of ScriptNode to represent an algorithm node.
 * @author braisted
 */
public class AlgorithmNode extends ScriptNode {
    
    /** Algorithm name
     */    
    String name;
    /** Alg ID
     */    
    int id;
    /** Parent data node referenct (ID)
     */    
    int dataNodeRef;
    /** Algorithm class
     */    
    String algType;
    /** Parameter container.
     */    
    AlgorithmData data;
        
    /** Next Algorithm node within an <CODE>AlgorithmSet</CODE>.
     * This is an optional thread through structures to connect
     * algorithms.
     */    
    AlgorithmNode nextNode;
    
    /** Creates a new instance of AlgorithmNode */
    public AlgorithmNode() {    
    }
    
    /** Creates a new AlgorithmNode
     * @param name Node name
     * @param id Algorithm node id.
     * @param dataNodeRef parent data node reference.
     * @param algType Algorithm Type attribute.
     */    
    public AlgorithmNode(String name, int id, int dataNodeRef, String algType) {
        this.name = name; 
        this.id = id; 
        this.dataNodeRef = dataNodeRef;
        this.algType = algType;
    }

    /** Returns the string of the node.
     * @return  */    
    public String toString() {
        return name;
    }
    
    /** Returns the algorithm id.
     * @return
     */    
    public int getID() {
        return id;
    }
    
    /** Sets the algorithm's ID.
     */    
    public void setID(int ID) {
        id = ID;
    }    
    
    /** Sets the algorithm node name.
     */    
    public void setName(String Name) {
        name = Name;
    }
    
    /** returns the algorithm name.
     * @return
     */    
    public String getAlgorithmName() {
        return name;
    }
    
    /** Returns the id of the parent data node.
     */    
    public int getDataNodeRef() {
        return dataNodeRef;
    }
    
    /** Sets the data node reference.
     */    
    public void setDataNodeRef(int ref) {
        dataNodeRef = ref;
    }
    
    /** Returns the AlgorithmData object.
     * @return
     */    
    public AlgorithmData getAlgorithmData() {
        return data;
    }
    
    /** Sets the data (paremeter) object.
     */    
    public void setAlgorithmData(AlgorithmData aData) {
        data = aData;
    }
    
    /** Sets a reference to the next algorithm node.
     */    
    public void setNextNode(AlgorithmNode node) {
        nextNode = node;
    }
    
    /** Returns the nodes next algorithm node.
     */    
    public AlgorithmNode nextNode() {
        return nextNode;
    }
    
    /** Sets the algorithm type attribute.
     */    
    public void setAlgorithmType(String type) {
        algType = type;
    }

    /** Returns the algorithm type.
     */    
    public String getAlgorithmType() {
        return algType;
    }
}
