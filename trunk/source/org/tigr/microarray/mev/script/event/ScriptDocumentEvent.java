/*
 * ScriptDocumentEvent.java
 *
 * Created on March 14, 2004, 12:12 PM
 */

package org.tigr.microarray.mev.script.event;

import org.tigr.microarray.mev.script.util.DocumentBase;
/**
 *
 * @author  braisted
 */
public class ScriptDocumentEvent {
    
    private DocumentBase document;
    
    /** Creates a new instance of ScriptDocumentEvent */
    public ScriptDocumentEvent(DocumentBase doc) {
        document = doc;
    }
    
    public DocumentBase getDocument() {
        return document;
    }
    
}
