/*
 * ScriptNode.java
 *
 * Created on February 28, 2004, 4:21 PM
 */

package org.tigr.microarray.mev.script.util;

import javax.swing.tree.DefaultMutableTreeNode;
import org.w3c.dom.Element;

/**
 *
 * @author  braisted
 */
public class ScriptNode extends DefaultMutableTreeNode {
    
    Element docElement;

    public ScriptNode() {
        super();
    }
    
    /** Creates a new instance of ScriptNode */
    public ScriptNode(Element elem) {
        docElement = elem;
    }
    
    public void setElement(Element elem) { 
        docElement = elem;
    }
    
}
