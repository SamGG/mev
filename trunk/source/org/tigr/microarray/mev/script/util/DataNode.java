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

/** Contains attibutes and data object related to a data node in
 * the <CODE>ScriptTree</CODE>.  Extends <CODE>ScriptNode</CODE>.
 * @author braisted
 */
public class DataNode extends ScriptNode {
    
    /** Node name
     */    
    String name;
    /** Node ID.
     */    
    int id;
    /** Data node type
     */    
    String data_type;
    /** Output class. (Types are specified in <CODE>ScriptConstants</CODE>)
     */    
    String output_class;
    
    /** Creates a new instance of AlgorithmNode */
    public DataNode() {
    }
    
    /** Constructs a new DataNode
     * @param id ID
     * @param name Title
     * @param outputClass output class
     */    
    public DataNode(int id, String name, String outputClass) {
        this(id, name, outputClass, null);
    }
    
    /** Constructs a DataNode.
     * @param id Node ID
     * @param name Node title
     * @param outputClass Output class (category)
     * @param dataType Data type
     */    
    public DataNode(int id, String name, String outputClass, String dataType) {
        this.id = id;
        this.name = name;
        this.output_class = outputClass;
        this.data_type = dataType;
    }
    
    /** converts to String representation based on Name.
     */    
    public String toString() {
        return name+" ["+id+"] ";
    }
    
    /** Sets the ID attribute.
     * @param id id
     */    
    public void setID(int id) {
        this.id = id;
    }
    
    /** Returns the node ID.
     */    
    public int getID() {
        return id;
    }
    
    /** Sets the data type.
     * @param dataType  */    
    public void setDataType(String dataType) {
        data_type = dataType;
    }
    
    /** Returns the data type.
     * @return  */    
    public String getDataType() {
        return data_type;
    }
    
    /** Sets the output class attribute.
     * @param outputClass  */    
    public void setDataOutputClass(String outputClass) {
        this.output_class = outputClass;
    }
    
    /** Returns the output class.
     */    
    public String getDataOutputClass() {
        return output_class;
    }
}
