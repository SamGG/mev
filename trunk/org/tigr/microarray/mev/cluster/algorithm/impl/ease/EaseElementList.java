/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: EaseElementList.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:46:49 $
 * $Author: braistedj $
 * $State: Exp $
 */
/*
 * EaseElementList.java
 *
 * Created on August 28, 2003, 5:00 PM
 */

package org.tigr.microarray.mev.cluster.algorithm.impl.ease;

import java.util.Hashtable;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.Vector;

/** The EaseElementList class is used to hold and manage <CODE>EaseDataElements</CODE>
 * and to assist in mapping primary annotation indices (from MeV's <CODE>IData</CODE> data structure
 * to annotation keys used in ease files to map to biological themes.
 * @author braisted
 */
public class EaseElementList extends Vector{
    
    /** Creates a new instance of EaseElementList */
    public EaseElementList() {
        super();
    }
    
    /** Constructs an ease element list with the passed indices
     * and associated array of keys.  The indices map the element
     * to a particular genes in the <CODE>Experiment</CODE> data structure.
     * @param indices Gene indices (to identify a gene within an data set.
     * @param keys Key values for the EaseDataElement objects. */    
    public EaseElementList(int [] indices, String [] keys){
        super(Math.min(indices.length, keys.length));
        int numberOfElements = Math.min(indices.length, keys.length);
        for(int i = 0; i < numberOfElements; i++){
            addElement(new EaseDataElement(indices[i], keys[i]));
        }
    }
    
    /** Creates a list using the provided keys.
     * Note that the elements will be sequentially numbered.
     * @param keys Key values
     */    
    public EaseElementList(String [] keys){
        super(keys.length);
        int numberOfElements = keys.length;
        for(int i = 0; i < numberOfElements; i++){
            addElement(new EaseDataElement(i, keys[i]));
        }
    }
    
    /** Returns the indicated data element.
     * @param index Index to retrieve
     * @return
     */    
    public EaseDataElement dataElementAt(int index){
        return (EaseDataElement)super.elementAt(index);
    }
    
    /** Inserts a new key and single value into the list.
     * @param key
     * @param value  */    
    public void setValue(String key, String value){
        int size = size();
        for(int i = 0; i < size; i++){
            dataElementAt(i).ifEqualsAdd(key,value);
        }
    }
    
    /** Returns all values within the list.
     * (note that each element could have several values)
     */    
    public Vector getValueList(){
        Vector list = new Vector();
        int size = size();
        EaseDataElement element;
        Vector values;
        
        for(int i = 0; i < size; i++){
            values = dataElementAt(i).getEaseKeys();
            //System.out.println("values size = "+values.size());
            for(int j = 0; j < values.size(); j++){
               // if(!list.contains((String)values.elementAt(j)))
                    list.add((String)values.elementAt(j));
            }
        }        
        return list;
    }
    
    /** Returns a list of the values contained in the list in which each value is represented once.
     */    
    public Vector getUniqueValueList(){
        Vector list = new Vector();
        int size = size();
        EaseDataElement element;
        Vector values;
        
        for(int i = 0; i < size; i++){
            values = dataElementAt(i).getEaseKeys();
            for(int j = 0; j < values.size(); j++){
                if(!list.contains((String)values.elementAt(j))){
                    list.addElement((String)values.elementAt(j));
                }
            }
        }        
        return list;
    }
    
    /** Povided a list of values this method returns all associated element indices.
     * @param valuesList List of values on which to search.
     * @return
     */    
    public int [] getIndices(String [] valuesList){
        Vector indices = new Vector();
        int index;
        String value;
        Integer intObj;
        int size = size();
        for(int i = 0; i < valuesList.length; i++){
            value = valuesList[i];
            for(int j = 0; j < size; j++){
                index = dataElementAt(j).ifContainsValueGetIndex(value);
                if(index > -1){
                    intObj = new Integer(index);
                    if(!indices.contains(intObj))
                        indices.addElement(intObj);
                }
            }
        }
        size = indices.size();
        int [] list = new int[size];
        for(int i = 0 ; i < size; i++){
            list[i] = ((Integer)indices.elementAt(i)).intValue();
        }
        return list;
    }
    
    /** Loads keys and values from file.
     * @param fileName File containing values.
     * @throws FileNotFoundException
     * @throws IOException
     */    
    public void loadValues(String fileName) throws FileNotFoundException, IOException {
        BufferedReader in = new BufferedReader(new FileReader(fileName));
        String line = "", key = "", value = "";
        int delIndex = 0;
        
        int cnt = 0;
        
        //First load the file into a hash
        Hashtable ht = new Hashtable();
        while( (line = in.readLine()) != null ){        
            delIndex = line.indexOf('\t');
            if(delIndex < 0)
                continue;
            value = line.substring(0, delIndex).trim();             
            key = line.substring(delIndex+1).trim();
            ht.put(key,value);
        }
        
        //Now get Values
        int size = this.size();
        EaseDataElement ede;
        for(int i = 0; i < size; i++){
            ede = this.dataElementAt(i);
            value = ((String)ht.get(ede.getMevKey()));
            if(value != null)
                ede.addValue(value);
        }
        in.close();
    }
    
    /** Sets default values in which element values are set
     * equal to the entered keys.  This handles the case where
     * indices need not map through an intermediate annotation step.
     */    
    public void setDefaultValues() {
        int size = size();
        EaseDataElement ede;
        for(int i = 0; i < size; i++){
            ede = dataElementAt(i);
            ede.addValue(ede.getMevKey());
        }
    }
}
