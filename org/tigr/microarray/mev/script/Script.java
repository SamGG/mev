/*
 * Script.java
 *
 * Created on March 18, 2004, 11:40 AM
 */

package org.tigr.microarray.mev.script;

import org.tigr.microarray.mev.script.util.*;
import org.tigr.microarray.mev.script.scriptGUI.*;

/**
 *
 * @author  braisted
 */
public class Script {
    
    private ScriptDocument document;
    private ScriptTree scriptTree;
    private ScriptXMLViewer xmlViewer;
    
    /** Creates a new instance of Script */
    public Script(ScriptDocument doc, ScriptTree tree, ScriptXMLViewer xViewer) {
        document = doc;
        scriptTree = tree;
        xmlViewer = xViewer;
    }

    public ScriptDocument getScriptDocument() {
        return document;
    }   

    public ScriptTree getScriptTree() {
        return scriptTree;
    }   

    public ScriptXMLViewer getXMLViewer() {
        return xmlViewer;
    }   
}
