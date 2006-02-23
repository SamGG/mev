/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * ITreeNode.java
 *
 * Created on August 11, 2004, 9:52 AM
 */

package org.tigr.microarray.mev.cluster.gui.helpers.ktree;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;

/**
 *
 * @author  braisted
 */
public interface ITreeNode {
    
    public Dimension getSize();
    public Point getLocation();
    public Point getTopAnchorPoint();
    public Point getBottomAnchorPoint();
    
    public boolean contains(int x, int y);
    public boolean contains(Rectangle rect);
    
    public int getMaxPathLengthToRoot();
    public int getMinPathLengthToRoot();
    
    public int getWidth();
    public int getHeight();
    
    public int getNodeHeight();
    public int getLevel();  //alt
    
    public ITreeNode [] getParents();
    public ITreeNode [] getChildren();    
    public void getSuccessors(Vector sucAccumulator);
    public void getAncestors(Vector ancAccumulator);
    
    public boolean addChild(ITreeNode childNode);
    public boolean addParent(ITreeNode parentNode);
    public void setLevel(int level);
    
    public boolean hasParents();
    public boolean hasChildren();
        
}
