/*
 * AlgorithmNode.java
 *
 * Created on February 28, 2004, 4:25 PM
 */

package org.tigr.microarray.mev.script.util;

/**
 *
 * @author  braisted
 */
public class DataNode extends ScriptNode {
    
    String name;
    int id;
    String data_type;
    String output_class;
    
    /** Creates a new instance of AlgorithmNode */
    public DataNode() {
    }
    
    public DataNode(int id, String name, String outputClass) {
        this(id, name, outputClass, null);
    }
    
    public DataNode(int id, String name, String outputClass, String dataType) {
        this.id = id;
        this.name = name;
        this.output_class = outputClass;
        this.data_type = dataType;
    }
    
    public String toString() {
        return name+" ["+id+"] ";
    }
    
    public void setID(int id) {
        this.id = id;
    }
    
    public int getID() {
        return id;
    }
    
    public void setDataType(String dataType) {
        data_type = dataType;
    }
    
    public String getDataType() {
        return data_type;
    }
    
    public void setDataOutputClass(String outputClass) {
        this.output_class = outputClass;
    }
    
    public String getDataOutputClass() {
        return output_class;
    }
}
