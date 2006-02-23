/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * ScriptNode.java
 *
 * Created on February 28, 2004, 4:21 PM
 */

package org.tigr.microarray.mev.script.util;

import javax.swing.tree.DefaultMutableTreeNode;

import org.w3c.dom.Element;

/** The script node is the base node for the nodes
 * used to represent the script within script tree.
 * Extenstions are: <CODE>DataNode</CODE> and
 * <CODE>AlgorithmNode</CODE>.
 * @author braisted
 */
public class ScriptNode extends DefaultMutableTreeNode {
    
    /** the enclosed document root element to the DOM
     */    
    Element docElement;

    /** Constructs a new ScriptNode
     */    
    public ScriptNode() {
        super();
    }
    
    /** Creates a new instance of ScriptNode
     * @param elem documents root element
     */
    public ScriptNode(Element elem) {
        docElement = elem;
    }
    
    /** sets the element field
     */    
    public void setElement(Element elem) { 
        docElement = elem;
    }
    
}
