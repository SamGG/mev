/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: HCLTree.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-02-23 20:59:51 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;

public class HCLTree extends JPanel implements java.io.Serializable {
    
    public static final long serialVersionUID = 202006060001L;
    
    // These constants specify a horizontal or
    // vertical orientation.
    
    /** HORIZONTAL orientation */
    public static final int HORIZONTAL = 0;  //gene tree
    /** VERTICAL orientation */
    public static final int VERTICAL   = 1;  //sample tree
    
    protected HCLTreeListener treeListener;
    
    protected int orientation = HORIZONTAL;
    protected int min_pixels = 2;
    protected int max_pixels = 10;
    protected float zero_threshold = 0.05f;
    protected Color lineColor = new Color(0, 0, 128);
    protected Color belowThrColor = Color.lightGray;
    protected Color selectedLineColor = Color.magenta;
    
    // initial data
    protected IData data;
    // a result data
    protected HCLTreeData treeData;
    // helpers
    protected float minHeight;
    protected int stepSize;
    protected int[] pHeights;
    protected float[] positions;
    protected boolean[] selected;
    protected Color[] nodesColors;
    final protected int xOrigin = 10;
    
    protected int [] parentNodes;
    protected boolean [] terminalNodes;
    protected float maxHeight;
    protected boolean flatTree = false;
    protected int horizontalOffset = 0;
    
    protected IFramework framework;
    
    /**
     * Constructs a <code>HCLTree</code> for passed result and
     * with specified orientation.
     *
     * @param result the result of a hcl calculation.
     * @param orientation the tree orientation.
     */
    public HCLTree(HCLTreeData treeData, int orientation) {
        setBackground(Color.white);
        this.treeData = treeData;
        this.orientation = orientation;
        // helpers
        this.flatTree = flatTreeCheck(treeData.height);
        this.minHeight = getMinHeight(treeData.node_order, treeData.height);
        
        this.maxHeight = getMaxHeight(this.treeData.node_order, treeData.height);
        
        this.zero_threshold = minHeight;
        this.terminalNodes = new boolean[this.treeData.height.length];
        
        this.pHeights  = getPixelHeights(treeData.node_order, treeData.height);
        this.positions = getPositions(treeData.node_order, treeData.child_1_array, treeData.child_2_array);
        this.selected = new boolean[treeData.node_order.length*2];
        this.nodesColors = new Color[treeData.node_order.length*2];
        deselect(this.selected);
        if (treeData.node_order.length >= 1 && !flatTree) {
            switch (this.orientation) {
                case HORIZONTAL:
                    setSizes(this.pHeights[treeData.node_order[treeData.node_order.length-2]] + xOrigin, 0);
                    break;
                case VERTICAL:
                    setSizes(0, this.pHeights[treeData.node_order[treeData.node_order.length-2]]);
                    break;
            }
        } else {
            setSizes(0, 0);
        }
        
        initializeParentNodeArray();
        
        
        
        addMouseListener(new Listener());
    }
    
    private HCLTree() { }
    
    
    private void writeObject(java.io.ObjectOutputStream oos) throws IOException {
        oos.writeInt(orientation);
        oos.writeInt(min_pixels);
        oos.writeInt(max_pixels);
        oos.writeFloat(zero_threshold);
        
        oos.writeObject(this.lineColor);
        oos.writeObject(this.belowThrColor);
        oos.writeObject(this.selectedLineColor);
        
        oos.writeObject(treeData);
        oos.writeFloat(minHeight);
        oos.writeInt(stepSize);
        oos.writeObject(pHeights);
        oos.writeObject(positions);
        oos.writeObject(selected);
        oos.writeObject(nodesColors);
        oos.writeObject(parentNodes);
        oos.writeObject(terminalNodes);
        oos.writeFloat(maxHeight);
        oos.writeBoolean(flatTree);
        oos.writeInt(horizontalOffset);
    }
    
    private void readObject(java.io.ObjectInputStream ois) throws IOException, ClassNotFoundException {
        this.orientation = ois.readInt();
        this.min_pixels = ois.readInt();
        this.max_pixels = ois.readInt();
        this.zero_threshold = ois.readFloat();
        
        this.lineColor = (Color)ois.readObject();
        this.belowThrColor = (Color)ois.readObject();
        this.selectedLineColor = (Color)ois.readObject();
        
        this.treeData = (HCLTreeData)ois.readObject();
        this.minHeight = ois.readFloat();
        this.stepSize = ois.readInt();
        this.pHeights = (int [])ois.readObject();
        this.positions = (float [])ois.readObject();
        this.selected = (boolean [])ois.readObject();
        this.nodesColors = (Color [])ois.readObject();
        this.parentNodes = (int [])ois.readObject();
        this.terminalNodes = (boolean [])ois.readObject();
        this.maxHeight = ois.readFloat();
        this.flatTree = ois.readBoolean();
        this.horizontalOffset = ois.readInt();
        addMouseListener(new Listener());
    }
    
    /**
     * Sets specified listener to be notified by tree events.
     */
    public void setListener(HCLTreeListener treeListener) {
        this.treeListener = treeListener;
    }
    
    
    private void initializeParentNodeArray(){
        parentNodes = new int[this.treeData.height.length];
        
        for(int i = 0 ; i < this.treeData.node_order.length; i++){
            if(this.treeData.node_order[i] != -1)
                parentNodes[this.treeData.node_order[i]] =  findParent(i);
        }
    }
    
    private int findParent(int index){
        int node = this.treeData.node_order[index];
        for(int i = 0; i < this.treeData.child_1_array.length; i++){
            if(this.treeData.child_1_array[i] == node)
                return i;
        }
        for(int i = 0; i < this.treeData.child_2_array.length; i++){
            if(this.treeData.child_2_array[i] == node)
                return i;
        }
        return 0;
    }
    
    /**
     * Sets specified tree attributies.
     */
    public void setProperties(float zeroThreshold, int minDistance, int maxDistance) {
        this.zero_threshold = zeroThreshold;
        this.min_pixels = minDistance;
        this.max_pixels = maxDistance;
        this.pHeights   = getPixelHeights(treeData.node_order, treeData.height);
        if (treeData.node_order.length > 1) {
            switch (this.orientation) {
                case HORIZONTAL:
                    setSizes(this.pHeights[treeData.node_order[treeData.node_order.length-2]] + xOrigin, getHeight());
                    break;
                case VERTICAL:
                    setSizes(getWidth(), this.pHeights[treeData.node_order[treeData.node_order.length-2]]);
                    break;
            }
        }
    }
    
    /**
     * Set the tree threshold
     */
    public void setZeroThreshold(float zeroThreshold){
        this.zero_threshold = zeroThreshold;
    }
    
    /**
     * Returns the cluster row indices for the current distance threshold
     */
    public int [][] getClusterRowIndices(){
        
        int k = this.getNumberOfTerminalNodes();
        int index = 0;
        int [] endPoints;
        int [] rows;
        
        int testCnt = 0;
        int [] terminals;
        for(int i = 0; i < this.terminalNodes.length; i++){
            if(this.terminalNodes[i] == true)
                testCnt++;
        }
        terminals = new int[testCnt];
        int terminalCnt = 0;
        
        for(int i = 0; i < this.terminalNodes.length; i++){
            if(this.terminalNodes[i] == true){
                terminals[terminalCnt] = i;
                terminalCnt++;
            }
        }
        
        int [][] clusters = new int[terminals.length][];
        
        for(int i = 0; i < clusters.length; i++){
            index = terminals[i];
            if(index >= this.treeData.node_order.length){
                endPoints = this.getSubTreeEndPointElements(index);
                
            }
            else{
                endPoints = new int[2];
                endPoints[1] = (int)this.positions[index];
                endPoints[0] = (int)this.positions[index];
            }
            
            rows = new int[endPoints[1]-endPoints[0]+1];
            // rows = new int[endPoints[1]-endPoints[0]+1];
            
            for(int j = 0; j < rows.length; j++){
                rows[j] = endPoints[0]+j;
            }
            clusters[i] = rows;
        }
        return clusters;
    }
    
    /**
     * Sets the tree height based on min and max distances supplied
     */
    public void setPixelHeightLimits(int minDistance, int maxDistance){
        this.min_pixels = minDistance;
        this.max_pixels = maxDistance;
        this.pHeights   = getPixelHeights(treeData.node_order, treeData.height);
        if (treeData.node_order.length > 1) {
            switch (this.orientation) {
                case HORIZONTAL:
                    setSizes(this.pHeights[treeData.node_order[treeData.node_order.length-2]] + xOrigin, getHeight());
                    break;
                case VERTICAL:
                    setSizes(getWidth(), this.pHeights[treeData.node_order[treeData.node_order.length-2]]);
                    break;
            }
        }
    }
    
    
    /**
     * Sets horizontal margin (offset for sample tree)
     */
    public void setHorizontalOffset(int offset){
        this.horizontalOffset = offset;
    }
    
    /**
     *  finds min dist in tree, initializes zeroThreshold
     */
    private float findMinDistance(){
        float min = Float.POSITIVE_INFINITY;
        for(int i = 0; i < treeData.height.length;i++){
            min = Math.min(min, treeData.height[i]);
        }
        return min;
    }
    
    /**
     * Sets node color.
     */
    public void setNodeColor(int node, Color color) {
        setSubTreeColor(node, color);
        repaint();
    }
    
    private void setSubTreeColor(int node, Color color) {
        this.nodesColors[node] = color;
        if (treeData.child_1_array[node] != -1) {
            setNodeColor(treeData.child_1_array[node], color);
        }
        if (treeData.child_2_array[node] != -1) {
            setNodeColor(treeData.child_2_array[node], color);
        }
    }
    
    /**
     * Clears all the colored tree nodes.
     */
    public void resetNodeColors() {
        for (int i = this.nodesColors.length; --i >= 0;) {
            this.nodesColors[i] = null;
        }
        repaint();
    }
    
    /**
     * Returns the zero threshold attribute.
     */
    public float getZeroThreshold() {
        return zero_threshold;
    }
    
    /**
     * Returns the min distance attribute.
     */
    public int getMinDistance() {
        return min_pixels;
    }
    
    /**
     * Returns the max distance attribute.
     */
    public int getMaxDistance() {
        return max_pixels;
    }
    
    /**
     * Returns the minimum node distance
     */
    public float getMinNodeDistance(){
        return minHeight;
    }
    
    /**
     * Returns the maximum node distance
     */
    public float getMaxNodeDistance(){
        return maxHeight;
    }
    
    /**
     *
     */
    public int getNumberOfTerminalNodes(){
        int n = 0;
        int index = 0;
        float [] height = this.treeData.height;
        int [] nodeOrder = this.treeData.node_order;
        
        for(int i = 0; i < nodeOrder.length; i++){
            index = nodeOrder[i];
            if(index == -1 || height[index] < zero_threshold){
                continue;
            }
            n++;
        }
        return n+1;
    }
    
    /**
     * Returns a boolean array indicating which nodes are terminal
     */
    public boolean [] getTerminalNodes(){
        return terminalNodes;
    }
    
    /**
     * Returns min height of the tree nodes.
     */
    private float getMinHeight(int[] nodeOrder, float[] height) {
        float min = Float.MAX_VALUE;
        for (int i=0; i<nodeOrder.length-1; i++) {
            min = Math.min(min, height[nodeOrder[i]]);
        }
        return min;
    }
    
    /**
     * Returns true if tree is flat
     */
    private boolean flatTreeCheck(float [] height){
        if(height.length == 1)
            return false;
        
        for(int i = 0; i < height.length-1; i++){
            if(height[i] != height[i+1])
                return false;
        }
        return true;
    }
    
    /**
     * Returns min height of the tree nodes.
     */
    private float getMaxHeight(int[] nodeOrder, float[] height) {
        float max = Float.MIN_VALUE;
        for (int i=0; i<nodeOrder.length-1; i++) {
            max = Math.max(max, height[nodeOrder[i]]);
        }
        return max;
    }
    
    /**
     * Deselects the tree.
     */
    private void deselect(boolean[] selected) {
        for (int i = selected.length; --i >= 0;) {
            selected[i] = false;
        }
    }
    
    /**
     * Deselects all nodes
     */
    public void deselectAllNodes(){
        deselect(this.selected);
    }
    
    
    /**
     * Fills in an array by -1 values.
     */
    private void clear(int[] array) {
        for (int i = array.length; --i >= 0;) {
            array[i] = -1;
        }
    }
    
    /**
     * Calculates the current scale.
     */
    private float getScale() {
        // return this.min_pixels/Math.max(this.minHeight, this.zero_threshold);
        //return 1.0f;
        return this.max_pixels/this.maxHeight;
    }
    
    /**
     * Calculates tree node positions.
     */
    private float[] getPositions(int[] nodeOrder, int[] child1, int[] child2) {
        float[] positions = new float[child1.length];
        Arrays.fill(positions, -1);
        if (nodeOrder.length < 2) {
            return positions;
        }
        fillPositions(positions, child1, child2, 0, child1.length-2);
        int node;
        for (int i=0; i<nodeOrder.length-1; i++) {
            node = nodeOrder[i];
            positions[node] = (positions[child1[node]] + positions[child2[node]])/2f;
        }
        return positions;
    }
    
    private int fillPositions(float[] positions, int[] child1, int[] child2, int pos, int index) {
        if (child1[index] != -1) {
            pos = fillPositions(positions, child1, child2, pos, child1[index]);
        }
        if (child2[index] != -1) {
            pos = fillPositions(positions, child1, child2, pos, child2[index]);
        } else {
            positions[index] = pos;
            pos++;
        }
        return pos;
    }
    
    /**
     * Returns heights shifted by min distance, corrects for distance polarity change
     */
    private float[] shiftHeights(float [] height, float minH){
        for(int i = 0; i < height.length; i++)
            height[i] = height[i] - minH;
        return height;
    }
    
    
    /**
     * Returns nodes heights in pixels.
     */
    private int[] getPixelHeights(int[] nodeOrder, float[] height) {
        float scale = getScale();
        int[] pHeights = new int[nodeOrder.length*2];
        int node;
        int child_1, child_2;
        for (int i=0; i<nodeOrder.length-1; i++) {
            node = nodeOrder[i];
            child_1 = treeData.child_1_array[node];
            child_2 = treeData.child_2_array[node];
            
            pHeights[node] = Math.max(pHeights[child_1], pHeights[child_2]) + Math.max(Math.min((int)Math.round(height[node]*scale), max_pixels), min_pixels);
        }
        return pHeights;
    }
    
    /**
     * Paints the tree into specified graphics.
     */
    public void paint(Graphics g) {
        
        super.paint(g);
        if (this.treeData.node_order.length == 1){
            g.setColor(Color.black);
            g.drawLine(0,0,10,0);
        }
        
        for(int i = 0 ; i < this.terminalNodes.length; i++){
            terminalNodes[i] = false;
        }
        
        if (this.treeData.node_order.length < 2) {
            return;
        }
        int sign = 1;
        if (this.orientation == VERTICAL) {
            ((Graphics2D)g).rotate(-Math.PI/2.0);
            sign = -1;
        }
        int max_node_height = this.pHeights[this.treeData.node_order[this.treeData.node_order.length-2]];
        
        int node;
        int child_1, child_2;
        int child_1_x1, child_1_x2, child_1_y;
        int child_2_x1, child_2_x2, child_2_y;
        for (int i=0; i<this.treeData.node_order.length-1; i++) {
            node = this.treeData.node_order[i];
            child_1 = this.treeData.child_1_array[node];
            child_2 = this.treeData.child_2_array[node];
            child_1_x1 = (max_node_height-this.pHeights[node])*sign;
            child_1_x2 = (max_node_height-this.pHeights[child_1])*sign;
            child_1_y  = (int)(this.positions[child_1]*this.stepSize)+this.stepSize/2;
            child_2_x1 = (max_node_height-this.pHeights[node])*sign;
            child_2_x2 = (max_node_height-this.pHeights[child_2])*sign;
            child_2_y  = (int)(this.positions[child_2]*this.stepSize)+this.stepSize/2;
            
            
            
            if (this.nodesColors[node] == null){
                if(this.treeData.height[node] >= zero_threshold) {
                    g.setColor(lineColor);
                    this.terminalNodes[node] = false;
                    if(this.pHeights[child_1] == 0)
                        this.terminalNodes[child_1] = true;
                    if(this.pHeights[child_2] == 0)
                        this.terminalNodes[child_2] = true;
                } else{
                    g.setColor(belowThrColor);
                    this.terminalNodes[node] = false;
                    
                    if(this.treeData.height[parentNodes[node]] >= zero_threshold){
                        drawWedge(g, node, child_1_x1+xOrigin, child_2_x1+xOrigin, child_1_y, child_2_y);
                        this.terminalNodes[node] = true;
                        this.terminalNodes[child_1] = false;
                        this.terminalNodes[child_2] = false;
                    }
                }
            } else {
                g.setColor(this.nodesColors[node]);
                if(this.treeData.height[node] >= zero_threshold) {
                    //  g.setColor(lineColor);
                    this.terminalNodes[node] = false;
                    if(this.pHeights[child_1] == 0)
                        this.terminalNodes[child_1] = true;
                    if(this.pHeights[child_2] == 0)
                        this.terminalNodes[child_2] = true;
                } else{
                    //   g.setColor(belowThrColor);
                    this.terminalNodes[node] = false;
                    
                    if(this.treeData.height[parentNodes[node]] > zero_threshold){
                        drawWedge(g, node, child_1_x1+xOrigin, child_2_x1+xOrigin, child_1_y, child_2_y);
                        this.terminalNodes[node] = true;
                        this.terminalNodes[child_1] = false;
                        this.terminalNodes[child_2] = false;
                    }
                }
                
            }
            
            if (this.selected[node])
                g.setColor(selectedLineColor);
            
            /*
            if (this.selected[node]) {
                g.setColor(selectedLineColor);
            } else {
             
                if (this.nodesColors[node] == null){
                    if(this.treeData.height[node] >= zero_threshold) {
                        g.setColor(lineColor);
                        this.terminalNodes[node] = false;
                        if(this.pHeights[child_1] == 0)
                            this.terminalNodes[child_1] = true;
                        if(this.pHeights[child_2] == 0)
                            this.terminalNodes[child_2] = true;
                    } else{
                        g.setColor(belowThrColor);
                        this.terminalNodes[node] = false;
             
                        if(this.treeData.height[parentNodes[node]] >= zero_threshold){
                            drawWedge(g, node, child_1_x1+xOrigin, child_2_x1+xOrigin, child_1_y, child_2_y);
                            this.terminalNodes[node] = true;
                            this.terminalNodes[child_1] = false;
                            this.terminalNodes[child_2] = false;
                        }
                    }
                } else {
                    g.setColor(this.nodesColors[node]);
                }
            }
             
             */
            
            
            
            
            
            
            
            if(this.orientation == HORIZONTAL){
                g.drawLine(child_1_x1 + xOrigin, child_1_y, child_1_x2 + xOrigin, child_1_y);
                g.drawLine(child_2_x1 + xOrigin, child_2_y, child_2_x2 + xOrigin, child_2_y);
                g.drawLine(child_1_x1 + xOrigin, child_1_y, child_2_x1 + xOrigin, child_2_y);
            }
            else{
                g.drawLine(child_1_x1, child_1_y + horizontalOffset, child_1_x2, child_1_y+ horizontalOffset);
                g.drawLine(child_2_x1, child_2_y + horizontalOffset, child_2_x2, child_2_y+ horizontalOffset);
                g.drawLine(child_1_x1, child_1_y + horizontalOffset, child_2_x1, child_2_y+ horizontalOffset);
            }
        }
    }
    
    public void drawWedge(Graphics g, int node, int x1, int x2, int y1, int y2){
        int [] xs = new int[3];
        int [] ys = new int[3];
        
        
        int k = node;
        int k1 = node;
        while(this.treeData.child_1_array[k] != -1){
            k = this.treeData.child_1_array[k];
        }
        while(this.treeData.child_2_array[k1] != -1){
            k1 = this.treeData.child_2_array[k1];;
        }
        
        if(this.orientation == HORIZONTAL){
            ys[0] = (y2-y1)/2 + y1;
            ys[1] = (int)(this.positions[k]*this.stepSize)+this.stepSize/2;
            ys[2] = (int)(this.positions[k1]*this.stepSize)+this.stepSize/2;
            xs[0] = x1;
            xs[1] = this.pHeights[treeData.node_order[treeData.node_order.length-2]] + xOrigin;
            xs[2] = this.pHeights[treeData.node_order[treeData.node_order.length-2]] + xOrigin;
        } else {
            ys[0] = (y2-y1)/2 + y1 + horizontalOffset;
            ys[1] = (int)(this.positions[k]*this.stepSize)+this.stepSize/2 + horizontalOffset;
            ys[2] = (int)(this.positions[k1]*this.stepSize)+this.stepSize/2 + horizontalOffset;
            xs[0] = x1 - xOrigin;
            xs[1] = -1*this.pHeights[treeData.node_order[treeData.node_order.length-2]];
            xs[2] = -1*this.pHeights[treeData.node_order[treeData.node_order.length-2]];
        }
        
        Color color = g.getColor();
        Graphics2D g2 = (Graphics2D)g;
        Composite composite = g2.getComposite();
        
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g.setColor(Color.blue);
        g.fillPolygon( new Polygon(xs,ys,3));
        g.setColor(color);
        g2.setComposite(composite);
    }
    
    
    //THE FOLLOWING METHOD IS USED TO OVERRRIDE THE PAINT METHOD IN SUBCLASSES SUCH AS HCLSUPPORTTREE
    
    public void paintSubTree(Graphics g) {
        super.paint(g);
    }
    
    /**
     * Updates the tree size, if element size was changed.
     */
    public void onSelected(IFramework framework) {
        this.framework = framework;
        this.data = framework.getData();
        updateSize(framework.getDisplayMenu().getElementSize());
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
    protected void updateSize(Dimension elementSize) {
        switch (this.orientation) {
            case HORIZONTAL:
                if (flatTree || this.stepSize == elementSize.height) {
                    return;
                }
                this.stepSize = elementSize.height;
                setSizes(getPreferredSize().width, this.stepSize*this.treeData.node_order.length);
                break;
            case VERTICAL:
                if (flatTree || this.stepSize == elementSize.width) {
                    return;
                }
                this.stepSize = elementSize.width;
                setSizes(this.stepSize*this.treeData.node_order.length, getPreferredSize().height);
                break;
        }
    }
    
    /**
     * Sets the tree sizes.
     */
    public void setSizes(int width, int height) {
        if(orientation == HORIZONTAL){
            setSize(width, height);
            setPreferredSize(new Dimension(width, height));
        }
        else{
            setSize(this.stepSize*this.treeData.node_order.length, height);
            setPreferredSize(new Dimension(this.stepSize*this.treeData.node_order.length, height));
        }
    }
    
    /**
     * Returns the endpoint row indices for the subtree below a node
     */
    private int [] getSubTreeEndPointElements(int node){
        int [] endPoints = new int[2];
        endPoints[0] = (int)positions[node];
        endPoints[1] = (int)positions[node];
        int ptr = node;
        
        while(this.treeData.child_1_array[ptr] != -1)
            ptr = this.treeData.child_1_array[ptr];
        
        endPoints[0] = (int)positions[ptr];
        
        ptr = node;
        while(this.treeData.child_2_array[ptr] != -1)
            ptr = this.treeData.child_2_array[ptr];
        endPoints[1] = (int)positions[ptr];
        
        return endPoints;
    }
    
    
    /**
     * Selects node by specified x and y coordinaties.
     */
    private void selectNode(int x, int y) {
        deselect(this.selected);
        HCLCluster cluster = new HCLCluster(findNode(x, y), Integer.MAX_VALUE, Integer.MIN_VALUE);
        selectNode(cluster, cluster.root);
        fireEvent(cluster);
        repaint();
    }
    
    /**
     * Selects tree for specified root node.
     */
    private void selectNode(HCLCluster cluster, int node) {
        if (node == -1) {
            cluster.firstElem = -1;
            cluster.lastElem = -1;
            return;
        }
        this.selected[node] = true;
        if (this.treeData.child_1_array[node] != -1) {
            selectNode(cluster, this.treeData.child_1_array[node]);
        } else {
            if (this.positions[node] < cluster.firstElem) {
                cluster.firstElem = (int)this.positions[node];
            }
            if (this.positions[node] > cluster.lastElem) {
                cluster.lastElem = (int)this.positions[node];
            }
            cluster.setFinalSize();
        }
        if (this.treeData.child_2_array[node] != -1) {
            selectNode(cluster, this.treeData.child_2_array[node]);
        }
    }
    
    /**
     * Returns index of a node by specified x and y coordinaties.
     */
    private int findNode(int x, int y) {
        if(this.orientation == HORIZONTAL)
            x -= xOrigin; //add origin offset
        int max_node_height = this.pHeights[this.treeData.node_order[this.treeData.node_order.length-2]];
        int node;
        int child_1, child_2;
        int child_1_x1, child_1_x2, child_1_y;
        int child_2_x1, child_2_x2, child_2_y;
        for (int i=0; i<this.treeData.node_order.length-1; i++) {
            node = this.treeData.node_order[i];
            child_1 = this.treeData.child_1_array[node];
            child_2 = this.treeData.child_2_array[node];
            child_1_x1 = (max_node_height-this.pHeights[node]);
            child_1_y  = (int)(this.positions[child_1]*this.stepSize)+this.stepSize/2 + horizontalOffset;
            child_2_x1 = (max_node_height-this.pHeights[node]);
            child_2_y  = (int)(this.positions[child_2]*this.stepSize)+this.stepSize/2 + horizontalOffset;
            switch (this.orientation) {
                case HORIZONTAL:
                    if (child_1_y < y && child_2_y > y && x > child_1_x1) {
                        return node;
                    }
                    break;
                case VERTICAL:
                    if (child_1_y < x && child_2_y > x && y > child_1_x1) {
                        return node;
                    }
                    break;
            }
        }
        return -1;
    }
    
    public void saveGeneNodeHeights(){
        String line;
        int nodeIndex = 0;
        int child1, child2;
        
        File file = null;
        final JFileChooser fc = new JFileChooser(TMEV.getFile("data/"));
        int ret = fc.showSaveDialog(new javax.swing.JFrame());
        if (ret == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
        }
        
        try{
            PrintWriter out = new PrintWriter(new FileOutputStream(file));
            
            for(int i = 0; i < this.treeData.node_order.length-1; i++){
                line = "Node_"+String.valueOf(i)+"\t";
                child1 = this.treeData.child_1_array[this.treeData.node_order[i]];
                child2 = this.treeData.child_2_array[this.treeData.node_order[i]];
                
                if(child1 < this.treeData.height.length/2)
                    line += "Gene_" + String.valueOf(child1+1) + "\t";
                else
                    line += "Node_" + String.valueOf(child1-this.treeData.height.length/2) + "\t";
                
                if(child2 < this.treeData.height.length/2)
                    line += "Gene_" + String.valueOf(child2+1) + "\t";
                else
                    line += "Node_" + String.valueOf(child2-this.treeData.height.length/2) + "\t";
                
                line += String.valueOf(this.treeData.height[this.treeData.node_order[i]]);
                
                out.println(line);
            }
            out.flush();
            out.close();
        } catch (IOException ioe){
            JOptionPane.showMessageDialog(this, "Error saving node height file.", "Error", JOptionPane.WARNING_MESSAGE);
            ioe.printStackTrace();
        }
    }
    
    public void saveExperimentNodeHeights(){
        String line;
        int nodeIndex = 0;
        int child1, child2;
        
        File file = null;
        final JFileChooser fc = new JFileChooser(TMEV.getFile("data/"));
        int ret = fc.showSaveDialog(new javax.swing.JFrame());
        if (ret == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
        }
        
        try{
            PrintWriter out = new PrintWriter(new FileOutputStream(file));
            
            for(int i = 0; i < this.treeData.node_order.length-1; i++){
                line = "Node_"+String.valueOf(i)+"\t";
                child1 = this.treeData.child_1_array[this.treeData.node_order[i]];
                child2 = this.treeData.child_2_array[this.treeData.node_order[i]];
                
                if(child1 < this.treeData.height.length/2)
                    line += "Exp_" + String.valueOf(child1+1) + "\t";
                else
                    line += "Node_" + String.valueOf(child1-this.treeData.height.length/2) + "\t";
                
                if(child2 < this.treeData.height.length/2)
                    line += "Exp_" + String.valueOf(child2+1) + "\t";
                else
                    line += "Node_" + String.valueOf(child2-this.treeData.height.length/2) + "\t";
                
                line += String.valueOf(this.treeData.height[this.treeData.node_order[i]]);
                
                out.println(line);
            }
            out.flush();
            out.close();
        } catch (IOException ioe){
            JOptionPane.showMessageDialog(this, "Error saving node height file.", "Error", JOptionPane.WARNING_MESSAGE);
            ioe.printStackTrace();
        }
    }
    
    
    public void saveAsNewickFile() {
        NewickFileOutputDialog dialog;
        String newickString;
        String [] annKeys;
        
        if(this.orientation == HCLTree.HORIZONTAL) { //gene tree
            annKeys = data.getFieldNames();
        } else {  //sample tree
            Vector annKeyVector = data.getSampleAnnotationFieldNames();
            annKeys = new String[annKeyVector.size()];
            for(int i = 0; i < annKeys.length; i++) {
                annKeys[i] = (String)(annKeyVector.elementAt(i));
            }
        }
        
        dialog = new NewickFileOutputDialog(framework.getFrame(), annKeys, this.orientation);
        
        if(dialog.showModal() == JOptionPane.OK_OPTION) {

            if(this.orientation == HCLTree.HORIZONTAL)
                newickString = generateNewickStringForGeneTree(dialog.getAnnotationKey());
            else
                newickString = generateNewickStringForSampleTree(dialog.getAnnotationKey());
            
            saveNewickString(newickString, dialog.getOutputFile());            
        }        
    }
    
    private void saveNewickString(String s, File outputFile) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
            bw.write(s);
            bw.flush();
            bw.close();
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Error saving Newick file: "+outputFile.getAbsolutePath()+".<BR>"+
            "Please check that file location is valid and permissions are open.", "IO Error Saving Newick File", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String generateNewickStringForSampleTree(String annotationKey) {
        
        String s = new String("");
        
        int node, leftChild, rightChild, parent;
        
        float leftHeight, rightHeight;
        
        Hashtable treeHash = new Hashtable();
        String nodeName = "";
        
        for(int i = 0; i < treeData.node_order.length-1; i++) {
            node = treeData.node_order[i];
            
            leftChild = treeData.child_1_array[node];
            rightChild = treeData.child_2_array[node];
            
            leftHeight = treeData.height[leftChild];
            rightHeight = treeData.height[rightChild];
            
            parent = parentNodes[node];
            
            if(leftChild < this.treeData.height.length/2) {
                s = this.data.getSampleAnnotation(leftChild, annotationKey)+":"+String.valueOf(this.treeData.height[node]/2.0f);
                treeHash.put(String.valueOf(leftChild),s);
            }
            if(rightChild < this.treeData.height.length/2) {
                s = this.data.getSampleAnnotation(rightChild, annotationKey)+":"+String.valueOf(this.treeData.height[node]/2.0f);
                treeHash.put(String.valueOf(rightChild),s);
            }
            if(treeHash.containsKey(String.valueOf(leftChild)) && treeHash.containsKey(String.valueOf(rightChild))) {
                s = "("+treeHash.get(String.valueOf(leftChild))+","+treeHash.get(String.valueOf(rightChild))+"):"+String.valueOf(this.treeData.height[parentNodes[node]]/2.0f);
                treeHash.put(String.valueOf(node), s);
                
                //remove entries as they become obsolete
                treeHash.remove(String.valueOf(leftChild));
                treeHash.remove(String.valueOf(rightChild));
            }
        }        
        return s+";";        
    }
    
    
    private String generateNewickStringForGeneTree(String annotationKey) {
        
        String [] fieldNames = data.getFieldNames();
        
        int attIndex = 0;  //element attribute index, annotation field index
        for(int i = 0; i < fieldNames.length; i++) {
            if(fieldNames[i].equals(annotationKey)) {
                attIndex = i;
                break;
            }
        }
        
        String s = new String("");
        
        int node, leftChild, rightChild, parent;
        
        float leftHeight, rightHeight;
        
        Hashtable treeHash = new Hashtable();
        String nodeName = "";
        
        for(int i = 0; i < treeData.node_order.length-1; i++) {
            node = treeData.node_order[i];
            
            leftChild = treeData.child_1_array[node];
            rightChild = treeData.child_2_array[node];
            
            leftHeight = treeData.height[leftChild];
            rightHeight = treeData.height[rightChild];
            
            parent = parentNodes[node];
            
            if(leftChild < this.treeData.height.length/2) {
                s = this.data.getElementAttribute(leftChild, attIndex)+":"+String.valueOf(this.treeData.height[node]/2.0f);
                treeHash.put(String.valueOf(leftChild),s);
            }
            if(rightChild < this.treeData.height.length/2) {
                s = this.data.getElementAttribute(rightChild, attIndex)+":"+String.valueOf(this.treeData.height[node]/2.0f);
                treeHash.put(String.valueOf(rightChild),s);
            }
            if(treeHash.containsKey(String.valueOf(leftChild)) && treeHash.containsKey(String.valueOf(rightChild))) {
                s = "("+treeHash.get(String.valueOf(leftChild))+","+treeHash.get(String.valueOf(rightChild))+"):"+String.valueOf(this.treeData.height[parentNodes[node]]/2.0f);
                treeHash.put(String.valueOf(node), s);
                
                //remove entries as they become obsolete
                treeHash.remove(String.valueOf(leftChild));
                treeHash.remove(String.valueOf(rightChild));
            }
        }        
        return s+";";        
    }
    
    /**
     * Notifies the tree listener.
     * @see HCLCluster
     */
    private void fireEvent(HCLCluster cluster) {
        if (this.treeListener != null) {
            this.treeListener.valueChanged(this, cluster);
        }
    }
    
    /**
     * The class to listen to mouse events.
     */
    private class Listener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                return;
            }
            selectNode(e.getX(), e.getY());
        }
    }
}
