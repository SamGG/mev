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

package org.tigr.microarray.mev.cluster.gui.helpers;

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
import org.tigr.graph.GraphLine;
import org.tigr.graph.GraphTick;
import org.tigr.graph.GraphViewer;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.util.QSort;

public class KMGraphViewer extends JPanel implements IViewer {
    KMGraph viewer;
    float minX;
    float maxX;
    float minY;
    float maxY;
    boolean firstView = true;
    DecimalFormat format;

	Vector<float[]> timeses;
    Vector<boolean[]> statuses;
    Vector<Color> colors;
    String eventAnnLabel;

    Vector<Vector<Float>> timesIn;
    Vector<Vector<Boolean>> statusIn;
    
    /**
     * Creates a viewer for a single, basic survival curve. No censoring markers are possible and only one curve is drawn. 
     * @param datapoints 
     * @param colors
     * @param eventAnnLabel
     */
	public KMGraphViewer(Vector<Vector<Float>> timesIn, Vector<Color> colors, String eventAnnLabel) {
		this.timeses = new Vector<float[]>();
		this.eventAnnLabel = eventAnnLabel;
		this.colors = colors; 
		this.statuses = null;
		this.timesIn = timesIn;
		try {
		timeses = new Vector<float[]>();
		float[][] datapoints = new float[2][timesIn.get(0).size()];
		for(int i=0; i<datapoints[0].length; i++) {
			datapoints[0][i] = timesIn.get(0).get(i);
			datapoints[1][i] = timesIn.get(1).get(i);
		}
		timeses.add(datapoints[0]);
		initializeViewer();
		enterGraphData(datapoints, Color.black);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    /**
     * This constructor is used by XMLEncoder
     *
     */
	public KMGraphViewer(Vector<Vector<Float>> timesIn, Vector<Vector<Boolean>> statusIn, Vector<Color> colors, String eventAnnLabel) {
		this.timeses = new Vector<float[]>();
		this.statuses = new Vector<boolean[]>();
		this.timesIn = timesIn;
		this.statusIn = statusIn;
		this.eventAnnLabel = eventAnnLabel;
		
		for (int i = 0; i < timesIn.size(); i++) {
			Vector<Float> thisfloatList = timesIn.get(i);
			Vector<Boolean> thisstatusList = statusIn.get(i);
			timeses.add(i, new float[thisfloatList.size()]);
			statuses.add(i, new boolean[thisstatusList.size()]);
			for(int j=0; j<thisfloatList.size(); j++) {
				timeses.get(i)[j] = thisfloatList.get(j);
				statuses.get(i)[j] = thisstatusList.get(j);
			}
		}
		this.colors = colors;
		initializeViewer();
    	
    	assert(statuses.size() == timeses.size());
        for(int i=0; i<timeses.size(); i++) {
        	QSort qs = new QSort(timeses.get(i));
        	float[] sortedTimes = qs.getSorted();
            int[] originalIndex = qs.getOrigIndx();
        	enterGraphData(originalIndex, sortedTimes, statuses.get(i), colors.get(i));
        }
	}	
	
    /**
     * @inheritDoc
     * @author eleanorahowe
     * 
     */
    public Expression getExpression(){
    	if(this.statuses == null) {
    		Expression e = new Expression(this, this.getClass(), "new", 
	    			new Object[]{timesIn, this.colors, this.eventAnnLabel});
    		System.out.println(e.toString());
	     	return e;
	    } else {
    		return new Expression(this, this.getClass(), "new", 
	    			new Object[]{timesIn, statusIn, this.colors, this.eventAnnLabel});
    	}
    }


    private void initializeViewer(){
    	
        minX = findMinDistance(timeses);
        maxX = findMaxDistance(timeses);
        minY = 0;
        maxY = 1;
        viewer = new KMGraph();
        
        viewer.setYAxisValue(minX);
        viewer.setXAxisValue(minY);
        viewer.setShowCoordinates(true);


    }

        /**
     *  finds min dist in tree, initializes zeroThreshold
     */
    private float findMinDistance(Vector<float[]> timeses){
        float min = Float.POSITIVE_INFINITY;
        for(int j=0; j<timeses.size(); j++) {
	        float[] alltimes = timeses.get(j);
        	for(int i = 0; i < alltimes.length;i++){
	            min = Math.min(min, alltimes[i]);
	        }
        }
        return min;
    }
    
    /**
     * Returns the largest value of time.
     */
    private float findMaxDistance(Vector<float[]> timeses) {
        float max = 0;
        for(int j=0; j<timeses.size(); j++) {
        	float[] alltimes = timeses.get(j);
        	for (int i=0; i<alltimes.length; i++) {
        		max = Math.max(max, alltimes[i]);
        	}
        }
        return max;
    }
    

    /**
     * Draw one survival curve from a baselineSurv dataset.
     * @param datapoints two columns, each row representing one event. Column 1 is the drop in survival likelihood since the previous timepoint. Column 2 is the time coordinate.
     * @param color The color to draw this curve in.
     */
    private void enterGraphData(float[][] datapoints, Color color){
    	int numEvents = datapoints[0].length;
        
        float lastX = 0f;
        float lastY = 1f;
    	//First event (horizontal line from y-axis to first event timepoint).
        for(int i = 0; i < numEvents; i++){
//        	System.out.println("printing X: " + datapoints[0][i] + " Y: " + datapoints[1][i]);
        	float thisX = datapoints[0][i];
        	float thisY = datapoints[1][i];
            viewer.addGraphElement(new GraphLine(lastX, lastY, lastX, thisY, color));
            viewer.addGraphElement(new GraphLine(lastX, thisY, thisX, thisY, color));
            lastX = thisX;
            lastY = thisY;
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
           viewer.addGraphElement(new GraphTick(((float)i)/10f, 5, Color.black, GC.VERTICAL, GC.C, temp, Color.black));
           
        }
                
    }
    /**
     * Draw one survival curve from a set of sorted indexes, times and status flags. Draw it in the provided color.
     * @param originalIndex
     * @param sortedTimes
     * @param status
     * @param color
     */
    private void enterGraphData(int[] originalIndex, float[] sortedTimes, boolean[] status, Color color){
    	int numEvents = sortedTimes.length;
        float cumulativeProb = 1.0f;
        int numberAtRisk = numEvents;

    	//First event (horizontal line from y-axis to first event timepoint).
        if(numEvents>1){
            viewer.addGraphElement(new GraphLine(minX, maxY, sortedTimes[0], maxY, color));
        }

        //Assumes no duplicate timepoints.
        for (int i = 0; i < numEvents; i++) {
        	if(status[originalIndex[i]]) {
        		//mark a hash on the plot
                viewer.addGraphElement(new GraphLine(sortedTimes[i], cumulativeProb+.01, sortedTimes[i], cumulativeProb-.01, color));
        	} else {
        		//calculate daily probability
        		float dailyProb = (float)(numberAtRisk-1) / (float)numberAtRisk;
        		//calculate cumulative (multiplicative) probability
        		float oldProb = cumulativeProb;
        		cumulativeProb = cumulativeProb * dailyProb;
        		//draw vertcal line to new probability
            	viewer.addGraphElement(new GraphLine(sortedTimes[i], oldProb, sortedTimes[i], cumulativeProb, color));
        	}
    		numberAtRisk = numberAtRisk -1;
    		//draw horizontal line to next timepoint
    		if(i < numEvents-1) {
        		viewer.addGraphElement(new GraphLine(sortedTimes[i], cumulativeProb, sortedTimes[i+1], cumulativeProb, color));
        	} 
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
           viewer.addGraphElement(new GraphTick(((float)i)/10f, 5, Color.black, GC.VERTICAL, GC.C, temp, Color.black));
           
        }
                
    }
    
    /**
     * Print all graph data to console.
     */
    private void printGraphData() {
        for(int i=0; i<timeses.size(); i++) {
        	QSort qs = new QSort(timeses.get(i));
        	float[] sortedTimes = qs.getSorted();
            int[] originalIndex = qs.getOrigIndx();
    		boolean[] statuses = this.statuses.get(i);
	    	int numEvents = sortedTimes.length;
	        float cumulativeProb = 1.0f;
	        int numberAtRisk = numEvents;
	        //Assumes no duplicate timepoints.
	        System.out.println("n.risk\tsurvival\tdailyrisk");

	        for (int j = 0; j < numEvents; j++) {
	        	if(statuses[originalIndex[j]]) {
	        	} else {
	        		//calculate daily probability
	        		float dailyProb = (float)(numberAtRisk-1) / (float)numberAtRisk;
	        		//calculate cumulative (multiplicative) probability
	        		float oldProb = cumulativeProb;
	        		cumulativeProb = cumulativeProb * dailyProb;
	            	System.out.println(numberAtRisk + "\t" + cumulativeProb + "\t" + dailyProb);
	        	}
	    		numberAtRisk = numberAtRisk -1;

	        }
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

		        for(int i=0; i<timeses.size(); i++) {
		        	QSort qs = new QSort(timeses.get(i));
		        	float[] sortedTimes = qs.getSorted();
		            int[] originalIndex = qs.getOrigIndx();
		    		boolean[] statuses = this.statuses.get(i);
			    	int numEvents = sortedTimes.length;
			        float cumulativeProb = 1.0f;
			        int numberAtRisk = numEvents;
			        //Assumes no duplicate timepoints.
			        System.out.println("n.risk\tsurvival\tdailyrisk");
		
			        for (int j = 0; j < numEvents; j++) {
			        	if(statuses[originalIndex[j]]) {
			        	} else {
			        		//calculate daily probability
			        		float dailyProb = (float)(numberAtRisk-1) / (float)numberAtRisk;
			        		//calculate cumulative (multiplicative) probability
			        		float oldProb = cumulativeProb;
			        		cumulativeProb = cumulativeProb * dailyProb;
			            	System.out.println(numberAtRisk + "\t" + cumulativeProb + "\t" + dailyProb);
			        	}
			    		numberAtRisk = numberAtRisk -1;
		
			        }
					
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
    
    public void setViewer(KMGraph val){
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
    
    public class KMGraph extends GraphViewer {
    	public KMGraph() {
    		
    		super(null, (int)minX, (int)maxX, (int)minY, (int)maxY, minX, maxX, minY, maxY,
                    50, 50, 50, 50, "Survival over Time", "Time (" + eventAnnLabel + ")", "Survival Likelihood");

    		//modify menu to suport output values
 
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
