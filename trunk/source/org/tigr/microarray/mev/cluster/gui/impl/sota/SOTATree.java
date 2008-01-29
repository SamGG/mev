/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SOTATree.java,v $
 * $Revision: 1.5 $
 * $Date: 2006-03-24 15:51:44 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.sota;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;

public class SOTATree extends JPanel {
    
    
    private int TREE_X_ORIGIN = 10;
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL   = 1;
    private int orientation = 0;
    protected Color lineColor = new Color(0, 0, 128);
    protected Color extenderColor = Color.lightGray;
    
    private int numberOfClusters;
    private double  maxLeafToRootPath;
    private int treeHeight;
    int maxXPosition;
    
    private int maxNodeHeight;
    private int minNodeHeight;
    private int height;
    private int width;  //based on numClusters*element height
    private int elementHeight = 10;
    private int elementWidth = 20;
    private int paintElementHeight;
    private int FACTOR = 20;
    private int origX;
    private int origY;
    private int utilCounter;
    private SOTATreeData sotaTreeData;
    
    protected IData data;
    
    int sign = 1;
    
    float [] nodeHeights;
    int [] leftChild;
    int [] rightChild;
    int [] nodePopulation;
    public boolean getGeneTree(){
    	if(this.orientation == SOTATree.VERTICAL)
    		return true;
    	return false;
    }
    public SOTATreeData getSotaTreeData(){return sotaTreeData;}
    
    /**
     * Used by SOTATreePersistenceDelegate to recreate a SOTATree from saved data
     */
    public SOTATree(SOTATreeData sotaTreeData, Boolean geneTree){
    	this(sotaTreeData, geneTree.booleanValue());
    }
    
    /** Creates new SOTATree */
    public SOTATree(SOTATreeData sotaTreeData, boolean geneTree){
    	this.sotaTreeData = sotaTreeData;
        setBackground(Color.white);
        if(!geneTree)
            this.orientation = SOTATree.VERTICAL;
        this.nodeHeights = sotaTreeData.nodeHeights;
        this.leftChild = sotaTreeData.leftChild;
        this.rightChild = sotaTreeData.rightChild;
        this.nodePopulation = sotaTreeData.nodePopulation;
        
        numberOfClusters = nodeHeights.length/2;
        
        maxNodeHeight = 40;
        minNodeHeight = 5;
        
        width = getTreeHeight();
        height = getTreeWidth();
        
        if(orientation == SOTATree.HORIZONTAL)            
            setSizes(width, height);
        else
            setSizes(height, width);
    }
    
    
    public void paint(Graphics g){
        super.paint(g);
        height = numberOfClusters*elementHeight;
        width = getTreeHeight();  //sets maxXPosition
        paintSotaTree(g);
    }
    
    
    private void paintSotaTree(Graphics g){
        Color startColor = g.getColor();
        origX = TREE_X_ORIGIN;
        
        g.setColor(lineColor);
        this.paintElementHeight = elementHeight;
        if(this.orientation == SOTATree.VERTICAL){
            ((Graphics2D)g).rotate(-Math.PI/2.0);
            //     this.elementHeight = -elementWidth;
            this.paintElementHeight = elementWidth;
            sign = -1;
            origY = getSubTreeSize(rightChild[0]) * elementWidth;
            paintSotaTree(g, 0, origX, origY+10);
        }
        else{
            origY = getSubTreeSize(rightChild[0]) * elementHeight;
            paintSotaTree(g, 0, origX, origY);
        }
        g.setColor(startColor);
        sign = 1;
    }
    
    
    private void paintSotaTree(Graphics g, int index, int xPos, int yPosCenter){
        
        if(nodePopulation[index] == 0)
            return;
        
        int xPosition = xPos;
        int y1, y2;
        y1 = y2 = yPosCenter;
        
        //leaf, draw one line back to parent node lemvel
        if(leftChild[index] == -1){// && nodeHeights[index] > -1){
            xPosition += FACTOR * nodeHeights[index];
            if(xPosition - xPos < minNodeHeight)
                xPosition = xPos + minNodeHeight;
            else if(xPosition - xPos > maxNodeHeight)
                xPosition = xPos + maxNodeHeight;
            
            g.drawLine(xPos*sign, yPosCenter, xPosition*sign, yPosCenter);
            g.setColor(this.extenderColor);
            g.drawLine(xPosition*sign, yPosCenter, maxXPosition*sign, yPosCenter);
            g.setColor(this.lineColor);
            return;
        }
        
        //root node
        if(index == 0) //set the
            xPosition = xPos;      //origin x
        
        //this is not the root, nor a leaf
        else{
            xPosition += FACTOR * nodeHeights[index];
            if(xPosition - xPos < minNodeHeight)
                xPosition = xPos + minNodeHeight;
            else if(xPosition - xPos > maxNodeHeight)
                xPosition = xPos + maxNodeHeight;
            
            maxXPosition = max(maxXPosition, xPosition);
            g.drawLine(xPos*sign, yPosCenter, xPosition*sign, yPosCenter);
        }
        
        //know subroot has two children
        int widthLeft = 1;
        int widthRight = 1;
        
        if(rightChild[leftChild[index]] != -1 && nodePopulation[rightChild[leftChild[index]]] != -1){
            widthLeft = getSubTreeSize(rightChild[leftChild[index]]);
            widthLeft = (int) (widthLeft * paintElementHeight);
            y1 = yPosCenter + widthLeft;
        }
        else{
            y1 = yPosCenter + (int)(paintElementHeight/2.0);
        }
        
        if(leftChild[rightChild[index]] != -1 && nodePopulation[leftChild[rightChild[index]]] != -1){
            widthRight = getSubTreeSize(leftChild[rightChild[index]]);
            widthRight = (int)(widthRight * paintElementHeight);
            y2 = yPosCenter - widthRight;
        }
        else{
            y2 = yPosCenter - (int)(paintElementHeight/2.0);
        }
        g.drawLine(xPosition*sign, yPosCenter, xPosition*sign, y1);
        paintSotaTree(g, leftChild[index], xPosition ,y1 );
        
        g.drawLine(xPosition*sign, yPosCenter, xPosition*sign, y2);
        paintSotaTree(g, rightChild[index], xPosition, y2);
    }
    
    
    private int getSubTreeSize(int index){
        utilCounter = 0;
        getNumberOfSubtreeNodes(index);
        return utilCounter;
    }
    
    private void getNumberOfSubtreeNodes(int index){
        if(leftChild[index] != -1)
            getNumberOfSubtreeNodes(leftChild[index]);
        if(rightChild[index] != -1)
            getNumberOfSubtreeNodes(rightChild[index]);
        else
            utilCounter++;
    }
    
    private int max(int n, int m){
        if(n > m)
            return n;
        else
            return m;
    }
    
    public int getTreeWidth(){
        return elementHeight * numberOfClusters;
    }
    
    public int getTreeHeight(){
        maxXPosition = TREE_X_ORIGIN;
        getTreeHeight(0, TREE_X_ORIGIN);
        return maxXPosition;
    }
    
    private void getTreeHeight(int index, int xPos){
        
        if(index < 0) return;
        int xPosition = xPos;
        
        //leaf
        if(leftChild[index] == -1){  // && subRoot.parent != null){
            xPosition += FACTOR * nodeHeights[index];
            if(xPosition - xPos < minNodeHeight)
                xPosition = xPos + minNodeHeight;
            else if(xPosition - xPos > maxNodeHeight)
                xPosition = xPos + maxNodeHeight;
            maxXPosition = max(maxXPosition, xPosition);
            return;
        }
        
        //root node
        if(index == 0) //set the
            xPosition = xPos;      //origin x
        
        //this is not the root, nor a leaf
        else{
            xPosition += FACTOR * nodeHeights[index];
            if(xPosition - xPos < minNodeHeight)
                xPosition = xPos + minNodeHeight;
            else if(xPosition - xPos > maxNodeHeight)
                xPosition = xPos + maxNodeHeight;
            maxXPosition = max(maxXPosition, xPosition);
        }
        getTreeHeight(leftChild[index], xPosition);
        getTreeHeight(rightChild[index], xPosition);
    }
    
    /**
     * Updates the tree size, if element size was changed.
     */
    public void onSelected(IFramework framework) {
        this.data = framework.getData();
        updateSize(framework.getDisplayMenu().getElementSize());
        repaint();
    }
    
    /**
     * Updates the tree size, if element size was changed.
     */
    public void onMenuChanged(IDisplayMenu menu) {
        updateSize(menu.getElementSize());
    }
    
    /**
     * Updates the tree size with specified element size.
     */
    private void updateSize(Dimension elementSize) {
        
        elementHeight = elementSize.height;
        elementWidth = elementSize.width;
        
        switch (this.orientation) {
            case HORIZONTAL:
                if (this.elementHeight == elementSize.height) {
                    return;
                }
                this.elementHeight = elementSize.height;
                setSizes(getTreeHeight(), elementHeight * this.numberOfClusters);
                break;
            case VERTICAL:
                if (elementWidth == elementSize.width) {
                    return;
                }
                elementWidth = elementSize.width;
                setSizes(this.elementWidth * this.numberOfClusters, getTreeHeight());
                break;
        }
    }
    
    /**
     * Sets the tree sizes.
     */
    private void setSizes(int width, int height) {
        setSize(width, height);
        setPreferredSize(new Dimension(width, height));
    }
    
    public int getMinDistance(){
        return minNodeHeight;
    }
    
    public int getMaxDistance(){
        return maxNodeHeight;
    }
    
    public void setProperties(float zeroThreshold, int min, int max){
        minNodeHeight = min;
        maxNodeHeight = max;
        treeHeight = getTreeHeight();  //resets to current height
        if(orientation == SOTATree.HORIZONTAL)            
            setSizes(getTreeHeight(), getTreeWidth());
        else
            setSizes(getTreeWidth(), getTreeHeight());
    }
    
    
}
