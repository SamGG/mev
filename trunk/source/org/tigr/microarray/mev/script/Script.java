/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Script.java
 *
 * Created on March 18, 2004, 11:40 AM
 */

package org.tigr.microarray.mev.script;

import org.tigr.microarray.mev.script.scriptGUI.ScriptXMLViewer;
import org.tigr.microarray.mev.script.util.ScriptTree;

/** High level single script wrapper containing the basic script classes supporting
 * data storage and graphical representation.
 * The class encapsulates instances of <CODE>ScriptTree</CODE> <CODE>ScriptXMLViewer</CODE>
 * and <CODE>ScriptDocument</CODE> objects.
 * @author braisted
 */
public class Script {
    
    private ScriptDocument document;
    private ScriptTree scriptTree;
    private ScriptXMLViewer xmlViewer;
    
    /** Creates a new instance of Script
     * @param doc Enclosed ScriptDocument object.
     * @param tree ScriptTree data structure.
     * @param xViewer XMLViewer XML based viewer option.
     */
    public Script(ScriptDocument doc, ScriptTree tree, ScriptXMLViewer xViewer) {
        document = doc;
        scriptTree = tree;
        xmlViewer = xViewer;
    }

    /** Returns the enclosed <CODE>ScriptDocument</CODE>
     * @return
     */    
    public ScriptDocument getScriptDocument() {
        return document;
    }   

    /** Returns the <CODE>ScriptTree</CODE> object.
     * @return
     */    
    public ScriptTree getScriptTree() {
        return scriptTree;
    }   

    /** Returns the XML view (<CODE>ScriptXMLViewer</CODE>)of the script.
     */    
    public ScriptXMLViewer getXMLViewer() {
        return xmlViewer;
    }   
}
