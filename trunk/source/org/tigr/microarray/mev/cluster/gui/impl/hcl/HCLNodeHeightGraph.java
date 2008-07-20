/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: HCLNodeHeightGraph.java,v $
 * $Revision: 1.9 $
 * $Date: 2007-05-21 21:10:50 $
 * $Author: braistedj $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.tigr.graph.GC;
import org.tigr.graph.GraphLine;
import org.tigr.graph.GraphPoint;
import org.tigr.graph.GraphTick;
import org.tigr.graph.GraphViewer;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;

public class HCLNodeHeightGraph extends JPanel implements IViewer {
    HCLGraphViewer viewer;
    HCLTreeData treeData;
    float minX;
    float maxX;
    float minY;
    float maxY;
    boolean firstView = true;
    DecimalFormat format;

    /** Creates new HCLAvalancheViewer */
    public HCLNodeHeightGraph(HCLTreeData data, boolean gene) {   
        treeData = data;
        initializeViewer();        
    }
    public HCLNodeHeightGraph(HCLTreeData data){
    	this(data, false);
    }
    /**
     * This constructor is used by XMLEncoder
     *
     */
    public HCLNodeHeightGraph() { }
    
    /**
     * @inheritDoc
     * @author eleanorahowe
     * 
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{this.treeData});
    }
    
    private void initializeViewer(){
        minX = findMinDistance();
        maxX = findMaxDistance();
        minY = 0;
        maxY = treeData.node_order.length;
        viewer = new HCLGraphViewer();
        
        viewer.setYAxisValue(minX);
        viewer.setXAxisValue(minY);
        viewer.setShowCoordinates(true);
        
  /*          public GraphViewer(JFrame frame, int startx, int stopx, int starty, int stopy,
    double graphstartx, double graphstopx, double graphstarty, double graphstopy,
    int preXSpacing, int postXSpacing, int preYSpacing, int postYSpacing,
    String title, String xLabel, String yLabel)
    */
        enterGraphData();
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
     * Returns min height of the tree nodes.
     */
    private float findMaxDistance() {
        float max = Float.MIN_VALUE;
        for (int i=0; i<treeData.node_order.length-1; i++) {
            max = Math.max(max, treeData.height[treeData.node_order[i]]);
        }
        return max;
    }
    
    private void enterGraphData(){
        int [] nodeOrder = this.treeData.node_order;
        float [] height = this.treeData.height;
        double nodeHeight = 0d;
        double terminalNodes = nodeOrder.length;
        int numberOfNodes = (int)terminalNodes;
        
        if(nodeOrder.length>1){
            viewer.addGraphElement(new GraphLine(minX, terminalNodes, height[nodeOrder[0]], terminalNodes, Color.black));
            viewer.addGraphElement(new GraphLine(height[nodeOrder[0]], terminalNodes, height[nodeOrder[0]], terminalNodes-1, Color.blue));
        }
        
        for(int i = 0; i < nodeOrder.length; i++){
            if(nodeOrder[i] > -1){
            nodeHeight = height[nodeOrder[i]];
            terminalNodes--;
            viewer.addGraphElement(new GraphPoint(nodeHeight, terminalNodes, Color.blue, 2));
            if(i+1<nodeOrder.length && nodeOrder[i+1] > -1){
                viewer.addGraphElement(new GraphLine(nodeHeight, terminalNodes, height[nodeOrder[i+1]], terminalNodes, Color.black));
                viewer.addGraphElement(new GraphLine(height[nodeOrder[i+1]], terminalNodes, height[nodeOrder[i+1]], terminalNodes-1, Color.blue));

            }
                
            }
        }

        format = new java.text.DecimalFormat();
        format.setMaximumFractionDigits(2);
        
        for(int i = 0; i < 10 ; i++){
    
           if(minX >= 0)
                viewer.addGraphElement(new GraphTick((maxX-minX)/10 * i, 5, Color.black, GC.HORIZONTAL, GC.C, String.valueOf(format.format((maxX-minX)/10 * i)), Color.black));
            else
                viewer.addGraphElement(new GraphTick(minX + (maxX-minX)/10 * i, 5, Color.black, GC.HORIZONTAL, GC.C, String.valueOf(format.format(minX + (maxX-minX)/10 * i)), Color.black));

            viewer.addGraphElement(new GraphTick(nodeOrder.length/10 * i, 5, Color.black, GC.VERTICAL, GC.C, String.valueOf(nodeOrder.length/10 * i), Color.black));
    
        }
        
        
     /*   for(int i = height.length - nodeOrder.length; i < height.length; i++){
            nodeHeight = height[this.treeData.node_order[i]];
            terminalNodes -=i;
            viewer.addGraphPoint(nodeHeight, terminalNodes); 
        }
       */
                
    }
    
    private void printGraphData() {
    	
    	int [] nodeOrder = this.treeData.node_order;
    	float [] height = this.treeData.height;
    	double nodeHeight = 0d;
    	double terminalNodes = nodeOrder.length;
    	int numberOfNodes = (int)terminalNodes;
    	
    	if(nodeOrder.length>1){
    		System.out.println(height[nodeOrder[0]]+ "  " +terminalNodes);
    		
    	}
    	
    	for(int i = 0; i < nodeOrder.length; i++){
    		if(nodeOrder[i] > -1){
    			nodeHeight = height[nodeOrder[i]];
    			terminalNodes--;
    			if(i+1<nodeOrder.length && nodeOrder[i+1] > -1){
    				System.out.println(nodeHeight+"  "+terminalNodes);
    			}                
    		}    	
    	}    
    }
    
    private void onSaveGraphData(){
    	JFileChooser chooser = new JFileChooser();
    	if(chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
    		try {
    			PrintWriter bw = new PrintWriter(new FileWriter(chooser.getSelectedFile()));

    			//commnents
    			Date da = new Date(System.currentTimeMillis());    			
    			bw.println("# Node Height Data");
    			bw.println("# Save Date: "+da.toString());
    			
    			//header
    			bw.println("Lower Limit\tUpper Limit\tTerminal Nodes\tdelta (step width)");
    			
    	    	int [] nodeOrder = this.treeData.node_order;
    	    	float [] height = this.treeData.height;
    	    	double nodeHeight = 0d;
    	    	double terminalNodes = nodeOrder.length;
    	    	int numberOfNodes = (int)terminalNodes;
    	    	
    	    	if(nodeOrder.length>1){
    	    		bw.println("\t"+height[nodeOrder[0]]+ "\t" +terminalNodes+"\t");    	    		
    	    	}
    	    	
    	    	for(int i = 0; i < nodeOrder.length; i++){
    	    		if(nodeOrder[i] > -1){
    	    			nodeHeight = height[nodeOrder[i]];
    	    			if(nodeOrder[i] > -1 && i > 0){
    	    				bw.println(height[nodeOrder[i-1]]+"\t"+nodeHeight+"\t"+terminalNodes+"\t"+(nodeHeight-height[nodeOrder[i-1]]));
    	    			}
    	    			terminalNodes--;
    	    		} else {
    	    			//System.out.println("skip one level");
    	    		}
    	    	}
    	    	
    	    	bw.println(nodeHeight+"\t\t"+terminalNodes+"\t\t");
    			
    			
    			bw.flush();
    			bw.close();
    			
    		} catch (IOException ioe) {
    			ioe.printStackTrace();
    		}
    	}
    }
    
    /**
     * Invoked by the framework when this viewer was deselected.
     */
    public void onDeselected() {
    }
    
    /**
     * Invoked by the framework when data is changed,
     * if this viewer is selected.
     * @see IData
     */
    public void onDataChanged(IData data) {
    }
    
    /**
     * Invoked when the framework is going to be closed.
     */
    public void onClosed() {
    }
    
    /**
     * Returns a component to be inserted into scroll pane view port.
     */
    public JComponent getContentComponent() {
        return viewer;
    }
    
    
    /**
     * Invoked by the framework to save or to print viewer image.
     */
    public BufferedImage getImage() {
        return null;
    }
    
    /**
     * Invoked by the framework when this viewer is selected.
     */
    public void onSelected(IFramework framework) {
        if(firstView && this.viewer.isVisible()){
           Graphics g = viewer.getGraphics();
           FontMetrics metrics = g.getFontMetrics(new Font("SansSerif", Font.BOLD, 10));
           int preX = metrics.stringWidth(String.valueOf(format.format(this.maxX)));
           int preY = metrics.stringWidth(String.valueOf(format.format(this.maxY)));
           metrics = g.getFontMetrics(new Font("SansSerif", Font.BOLD, 12));
           preX += metrics.getHeight();
           preY += metrics.getHeight();
           this.viewer.setPreXSpacing(preY+10);
           this.viewer.setPostYSpacing(preX+20);
           firstView = false;
        }                            
    }
    
    /**
     * Invoked by the framework when display menu is changed,
     * if this viewer is selected.
     * @see IDisplayMenu
     */
    public void onMenuChanged(IDisplayMenu menu) {
    }
    
    /**
     * Returns a component to be inserted into scroll pane header.
     */
    public JComponent getHeaderComponent() {
        return null;
    }    
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }
    
    /** Returns the corner component corresponding to the indicated corner,
     * posibly null
     */
    public JComponent getCornerComponent(int cornerIndex) {
        return null;
    }
    
    public void setViewer(HCLGraphViewer val){
        viewer = val;
    }
    public GraphViewer getViewer(){
        return viewer;
    }
    
    /** Returns the viewer's clusters or null
     */
    public int[][] getClusters() {
        return null;
    }    
    
    /**  Returns the viewer's experiment or null
     */
    public Experiment getExperiment() {
        return null;
    }        
    
    public void setExperiment(Experiment e) {
 //   	this.exptID = e.getId();
    }
    public int getExperimentID() {return 0;}
    public void setExperimentID(int id) {}
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return -1;
    }
    
    public class HCLGraphViewer extends GraphViewer {
    	public HCLGraphViewer() {
    		
    		super(null, (int)minX, (int)maxX, (int)minY, (int)maxY, minX, maxX, minY, maxY,
                    50, 50, 50, 50, "Node Heights", "Distance", "Number of Terminal Nodes");

    		//modify menu to suport output values
 
    		JMenuItem dataOutputItem = new JMenuItem("Output Graph Data");
    		dataOutputItem.addActionListener(new ActionListener(){
    			public void actionPerformed(ActionEvent ae) {
    				//printGraphData();
    				onSaveGraphData();
    			}
    		});
    		popup.addSeparator();
    		popup.add(dataOutputItem);
    	}
    	
    }
    
}
