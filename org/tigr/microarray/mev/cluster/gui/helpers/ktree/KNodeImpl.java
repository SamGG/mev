/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * GONode.java
 *
 * Created on August 11, 2004, 10:33 AM
 */

package org.tigr.microarray.mev.cluster.gui.helpers.ktree;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import java.io.Serializable;

import java.util.Vector;
//import awt.ktree.ITreeNode;
//import awt.ktree.ITreeNodeRenderer;

/**
 *
 * @author  braisted
 */
public class KNodeImpl implements ITreeNode, Serializable {
    
    protected int x = 0, y = 0;
    protected int w = 0, h = 0;
    
    protected Point location;
    
    protected boolean verboseRendering = true;
        
    protected ITreeNode [] children;
    protected ITreeNode [] parents;
    
    protected int level;
    
    /** Creates a new instance of GONode */
    public KNodeImpl() {
        children = new ITreeNode[0];
        parents = new ITreeNode[0];
    }
    
    /** Creates a new instance of GONode */
    public KNodeImpl(int xLoc, int yLoc) {
        location = new Point(xLoc, yLoc);
        x = xLoc;
        y = yLoc;
        children = new ITreeNode[0];
        parents = new ITreeNode[0];
    }
    
    /**************************************
     *          ITreeNode Methods
     */
    
    public boolean contains(int xLoc, int yLoc) {
        return (xLoc >= x && xLoc <= x+w && yLoc > y && yLoc <= y+h);
    }    
    
    public int getLevel() {
        return level;
    }
        
    public Point getTopAnchorPoint() {
        return new Point(x+w/2, y);
    }
    
    public Point getBottomAnchorPoint() {
        return new Point(x+w/2, y+h);
    }
    
    public ITreeNode[] getParents() {
        return parents;
    }
        
    public ITreeNode[] getChildren() {
        return children;
    }
        
    public Point getLocation() {
        return location;
    }
    
    public int getNodeHeight() {
        return level;
    }
    
    public boolean isVerboseRendering() {
        return verboseRendering;
    }
    
    public boolean addChild(ITreeNode child) {
        //check for conatains
        for(int i = 0; i < children.length; i++) {
            if(children[i] == child)
                return false;
        }
        ITreeNode [] newNodes = new ITreeNode [children.length+1];
        System.arraycopy(children, 0, newNodes, 0, children.length);
        newNodes[newNodes.length -1] = child;
        children = newNodes;
        return true;
    }
    
    public boolean addParent(ITreeNode parent) {
        //check for conatains
        for(int i = 0; i < parents.length; i++) {
            if(parents[i] == parent)
                return false;
        }
        ITreeNode [] newNodes = new ITreeNode [parents.length+1];
        System.arraycopy(parents, 0, newNodes, 0, parents.length);
        newNodes[newNodes.length -1] = parent;
        parents = newNodes;
        return true;        
    }
    
    public void clearChildren() {
        this.children = new ITreeNode[0];
    }
    
    public void clearParents() {
        this.parents = new ITreeNode[0];
    }
    
    public void setParents(Vector parentVector) {
        this.parents = new ITreeNode[parentVector.size()];
        for(int i = 0; i < parents.length; i++) {
            parents[i] = (ITreeNode)(parentVector.elementAt(i));            
        }        
    }

    public void setChildren(Vector childVector) {
        this.children = new ITreeNode[childVector.size()];
        for(int i = 0; i < children.length; i++) {
            children[i] = (ITreeNode)(childVector.elementAt(i));            
        }        
    }

    public int getMaxPathLengthToRoot() {
        int maxPathLength = 0;
        if(parents == null)
            return 0;
        else {            
            for(int i = 0; i < parents.length; i++) {
                maxPathLength = Math.max(maxPathLength, parents[i].getMaxPathLengthToRoot());                
            }            
        }
        
        return maxPathLength+1;
    }
    
    public int getMinPathLengthToRoot() {
        int minPathLength = Integer.MAX_VALUE;
        if(parents == null)
            return 0;        
        else {            
            for(int i = 0; i < parents.length; i++) {
                minPathLength = Math.min(minPathLength, parents[i].getMinPathLengthToRoot());
            }            
        }
        return minPathLength;
    }
        
    
    public Dimension getSize() {
        return new Dimension(w,h);
    }
    
    public int getWidth() {
        return w;        
    }
    
    public int getHeight() {
        return h;
    }

    public boolean hasChildren() {
        return children.length > 0;
    }
    
    public boolean hasParents() {
        return parents.length > 0;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
        
    public void getAncestors(Vector ancAccumulator) {
        if(this.parents.length == 0)
            return;
        else {
            //first add all parents
            for(int i = 0; i < parents.length; i++) {
                if(!ancAccumulator.contains(parents[i]))
                    ancAccumulator.addElement(parents[i]);
            }
            //recurse on all parents
            for(int i = 0; i < parents.length; i++) {
                parents[i].getAncestors(ancAccumulator);
            }
        }
    }
    
    public void getSuccessors(Vector sucAccumulator) {
        if(this.children.length == 0)
            return;
        else {
            //first add all parents
            for(int i = 0; i < children.length; i++) {
                if(!sucAccumulator.contains(children[i]))
                    sucAccumulator.addElement(children[i]);
            }
            //recurse on all children
            for(int i = 0; i < children.length; i++) {
                children[i].getSuccessors(sucAccumulator);
            }
        }
    }
    
    public boolean contains(Rectangle rect) {
        
//        return ( contains(rect.x, rect.y) || contains(rect.x, rect.y+rect.height)
  //      || contains(rect.x+rect.width, rect.y) || contains(rect.x+rect.width, rect.y+rect.height));
        return rect.contains(x,y,w,h);
    }
    
}
