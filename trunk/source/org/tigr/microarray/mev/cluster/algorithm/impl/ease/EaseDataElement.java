/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: EaseDataElement.java,v $
 * $Revision: 1.1 $
 * $Date: 2004-02-06 22:55:36 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl.ease;

import java.util.Vector;
/** This class encapsulates data structurs for encapsulating mappings between gene indices.
 * @author braisted
 */
public class EaseDataElement {
    
    /** The element's index value
     */    
    private int index;
    /** This is a single key provided from mev annotation.
     */    
    private String mevKey;
    /** This is a set of values associated with the mev side key.  The structure
     * permits one to many associations.
     */    
    private Vector<String> easeKeys;
    
    /** Creates a new instance of EaseDataElement
     * @param index Index value
     * @param mevKey annotation key
     */
    public EaseDataElement(int index, String mevKey) {
        this.index = index;
        this.mevKey = mevKey;
        this.easeKeys = new Vector<String>();
    }
    
    /** Returns all Ease Keys as a <CODE>Vector</CODE>
     * @return
     */    
    public Vector<String> getEaseKeys(){
        return easeKeys;
    }
    
    /** Adds an ease key (value) if the element's
     * key exists.
     * @param key mev key
     * @param value ease annotation index (ease key)
     * @return
     */    
    public boolean ifEqualsAdd(String key, String value){
        if(this.mevKey.equals(key)){
//            System.out.println("equal key = ***"+key+"*** value = ***"+value+"***");
            easeKeys.addElement(value);
            return true;
        }
        return false;        
    }
    
    /** Returns Vector of values if the key matches. (else null)
     * @param key
     * @return  */    
    public Vector<String> ifEqualsGetEaseKeys(String key){
        if(mevKey.equals(key) && easeKeys.size() > 0)
            return easeKeys;
        return null;       
    }
    
    /** Returns an element's index.
     * @return  */    
    public int getIndex(){
        return index;
    }
    
    /** Returns the element's key value
     * @return  */    
    public String getMevKey(){
        return this.mevKey;
    }
    
    /** Returns the element's key value if it contains the argument as a value.
     */    
    public int ifContainsValueGetIndex(String value){
        if(this.easeKeys.contains(value))
            return index;
        return -1;
    }
    
    /** Adds the value passed if the EaseDataElement doesn't contain the value.
     * This maintains a unique value list.
     * @param value
     */    
    public void addValue(String value){
        if(!this.easeKeys.contains(value))
            this.easeKeys.addElement(value);
    }
}
