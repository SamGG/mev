/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * ScriptDocument.java
 *
 * Created on February 27, 2004, 9:59 PM
 */

package org.tigr.microarray.mev.script;

import org.tigr.microarray.mev.script.util.DocumentBase;

/** The ScriptDocument class contains script attributes
 * such as script name and script description.  ScriptDocument
 * extends <CODE>DocumentBase</CODE> which handles the script
 * content as a DOM.
 *
 * @author braisted
 */
public class ScriptDocument extends DocumentBase {
    
    /** Script id
     */    
    private int id;
    /** Script name
     */    
    private String name;
    /** Script file name, if set
     */    
    private String fileName;    
    /** Script description
     */    
    private String description;
    /** Creation date
     */    
    private String date;
    
    /** Constructs a new ScriptDocument.
     * @param manager script manager
     * @param sid Script ID
     * @param fileName script file input file name
     */    
    public ScriptDocument(int sid, String fileName, ScriptManager manager) {
        super(manager);
        id = sid;
        this.fileName = fileName;
    }

    /** Creates a new instance of ScriptDocument
     * @param manager script manager
     * @param id Script ID
     * @param name Script name attribute
     * @param description Script Description
     * @param date Creation data
     */
    public ScriptDocument(int id, String name, String description, String date, ScriptManager manager) {
        super(date, name, description, manager);
        this.id = id;
        this.name = name;
        this.description = description;
        this.date = date;
    }
    
    
    
    public ScriptDocument(ScriptDocument doc) {
        super(doc);
        this.id = doc.getDocumentID();
        this.name = doc.getDocumentName();
        this.description = doc.getDescription();        
    }
    
    /** Returns the document's name, or null if not set.
     * @return  */  
    
    public String getDocumentName() {
        return name;
    }
    
    /** Sets the document's file name.
     * @param name file name
     */    
    public void setDocumentFileName(String name) {
        fileName = name;
    }
    
    /** Returns the document's file name.
     * @return  */    
    public String getDocumentFileName() {
        if(fileName != null)
            return fileName;
        return "Not Assigned";
    }
    
    /** Returns the document ID.
     * @return  */    
    public int getDocumentID() {
        return id;
    }
    
    /** Returns the script description, or null if not set.
     */    
    public String getDescription() {
        return description;
    }
    
    /** Sets the script ID.
     * @param docID document id
     */        
    public void setID(int docID) {
        id = docID;
    }
        
    /** Sets the script name
     * @param docName document name
     */    
    public void setName(String docName) {
        name = docName;
    }
                
    /** Sets script description.
     * @param docDesc script description
     */    
    public void setDescription(String docDesc) {
        description = docDesc;
    }

}
