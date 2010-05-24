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

package org.tigr.microarray.mev.cluster.gui.impl.surv;

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
import java.text.NumberFormat;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.tigr.graph.GC;
import org.tigr.graph.GraphElement;
import org.tigr.graph.GraphLine;
import org.tigr.graph.GraphPoint;
import org.tigr.graph.GraphTick;
import org.tigr.graph.GraphViewer;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.util.QSort;

public class BasehazGraphViewer extends JPanel implements IViewer {
    BasehazGraph viewer;
    float minX;
    float maxX;
    float minY;
    float maxY;
    boolean firstView = true;
    DecimalFormat format;

	Vector<Double> time;
    Vector<Double> hazards;
    String eventAnnLabel;
    
    /**
     * Draws a step graph indicating the hazard (y-axis) over time (x-axis).
     * @param time The time points at which the hazard changes.
     * @param hazards The level of hazard at the corresponding time point. Must be matched with time. 
     * @param eventAnnLabel The labe to be displayed on the X axis. Most likely a reference to "survival".
     */
	public BasehazGraphViewer(Vector<Double> xhazards, Vector<Double> yhazards, String eventAnnLabel) {
		this.time = xhazards;
		this.hazards = yhazards;
		this.eventAnnLabel = eventAnnLabel;

		initializeViewer();
	}

    /**
     * @inheritDoc
     * @author eleanorahowe
     * 
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{this.time, this.hazards, this.eventAnnLabel});
    }
	
    private void initializeViewer(){
    	
        minY = findMinDistance(time);
        maxY = findMaxDistance(time);
        minX = findMinDistance(hazards);
        maxX = findMaxDistance(hazards);
        viewer = new BasehazGraph();
        
        viewer.setYAxisValue(minY);
        viewer.setXAxisValue(minX);
        viewer.setShowCoordinates(true);
    	
    	assert(hazards.size() == time.size());
        	enterGraphData(hazards, time);

    }

        /**
     *  finds min dist in tree, initializes zeroThreshold
     */
    private float findMinDistance(Vector<Double> timeses){
        double min = Float.POSITIVE_INFINITY;
        for(int j=0; j<timeses.size(); j++) {
            min = Math.min(min, timeses.get(j));
        }
        return new Float(min).floatValue();
    }
    
    /**
     * Returns the largest value of time.
     */
    private float findMaxDistance(Vector<Double> timeses) {
        double max = 0;
        for(int j=0; j<timeses.size(); j++) {
            max = Math.max(max, timeses.get(j));
        }
        return new Float(max).floatValue();

    }
    
    
    /**
     * Draw one KM graph. 
     * @param basehazx
     * @param basehazy
     * @param status
     * @param color
     */
    private void enterGraphData(Vector<Double> basehazx, Vector<Double> basehazy){
    	int numEvents = basehazy.size();
        //Assumes no duplicate timepoints.
        for (int i = 0; i < numEvents; i++) {
        	GraphPoint gp = new GraphPoint(basehazx.get(i), basehazy.get(i));
        	gp.setPointSize(5);
            viewer.addGraphElement(gp); 
        }

        format = new java.text.DecimalFormat();
        format.setMaximumFractionDigits(2);
        
        for(int i = 0; i <= 10 ; i++) {
           if(minX >= 0) {
                viewer.addGraphElement(new GraphTick((maxX-minX)/10 * i, 5, Color.black, GC.HORIZONTAL, GC.C, String.valueOf(format.format((maxX-minX)/10 * i)), Color.black));
           } else {
                viewer.addGraphElement(new GraphTick(minX + (maxX-minX)/10 * i, 5, Color.black, GC.HORIZONTAL, GC.C, String.valueOf(format.format(minX + (maxX-minX)/10 * i)), Color.black));
           }
           
           String temp = NumberFormat.getInstance().format((float)maxY/10f * (float)i);
           viewer.addGraphElement(new GraphTick((float)maxY/10f * (float)i, 5, Color.black, GC.VERTICAL, GC.C, temp, Color.black));
           
        }
                
    }
    
    /**
     * Print all graph data to console.
     */
    private void printGraphData() {
//        System.out.println("Baseline Hazard X\tBaseline Hazard Y");
        for(int i=0; i<time.size(); i++) {
           	System.out.println(time.get(i) + "\t" + hazards.get(i));
    	}  
    }
    /**
     * Write graphing data to a flat file.
     */
    private void onSaveGraphData() {
    	JFileChooser chooser = new JFileChooser(TMEV.getDataPath());
    	if(chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
    		try {
    			PrintWriter bw = new PrintWriter(new FileWriter(chooser.getSelectedFile()));
		        bw.println("Baseline Hazard X\tBaseline Hazard Y");
    	        for(int i=0; i<time.size(); i++) {
    	        	bw.println(time.get(i) + "\t" + hazards.get(i));
    	    	}
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
    	//Re-draw with updated colors? 
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
    	//TODO implement
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
    
    public void setViewer(BasehazGraph val){
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
    }
    public int getExperimentID() {return 0;}
    public void setExperimentID(int id) {}
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return -1;
    }
    
    public class BasehazGraph extends GraphViewer {
    	public BasehazGraph() {
    		super(null, (int)minX, (int)maxX, (int)minY, (int)maxY, minX, maxX, minY, maxY,
                    50, 50, 50, 50, "Base Hazard", "Baseline Hazard", "Time (" + eventAnnLabel + ")");
    		
    		//modify menu to support output values
 
    		JMenuItem dataOutputItem = new JMenuItem("Output Graph Data");
    		dataOutputItem.addActionListener(new ActionListener(){
    			public void actionPerformed(ActionEvent ae) {
    				printGraphData();
    				onSaveGraphData();
    			}
    		});
    		popup.addSeparator();
    		popup.add(dataOutputItem);
    	}
    	
    }
    
}
