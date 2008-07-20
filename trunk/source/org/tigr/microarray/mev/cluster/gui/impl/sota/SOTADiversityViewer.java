/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SOTADiversityViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-03-24 15:51:44 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
/*
 *Class provides a graphical representation of the overall tree
 *diversity during construction of a sot.
 */
package org.tigr.microarray.mev.cluster.gui.impl.sota;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.beans.Expression;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.tigr.graph.GC;
import org.tigr.graph.GraphPoint;
import org.tigr.graph.GraphTick;
import org.tigr.graph.GraphViewer;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.util.FloatMatrix;

public class SOTADiversityViewer extends GraphViewer implements IViewer {
    
    private int numPoints;
    // private int maxValue;
    private float initValue;
    private FloatMatrix values;
    public SOTADiversityViewer(FloatMatrix values){
     /*       public GraphViewer(JFrame frame, int startx, int stopx, int starty, int stopy,
		       double graphstartx, double graphstopx, double graphstarty, double graphstopy,
		       int preXSpacing, int postXSpacing, int preYSpacing, int postYSpacing,
		       String title, String xLabel, String yLabel)
      
      */
	
	super(new JFrame(), 0, values.getRowDimension() , 0, (int)(values.get(0,0)), 0, values.getRowDimension(), 0, values.get(0,0),
	75, 75, 75, 75, "SOTA Tree Diversity History",
	"Cycle Number", "Diversity (% of Initial)");
		//Stored only so that this class can be re-created using a PersistenceDelegate
		//See IViewerPersistenceDelegate
		this.values = values;
	numPoints = values.getRowDimension();
	//   maxValue = maxYVal(values,0);
	initValue = values.get(0,0);
	
	this.setBackground(Color.lightGray);
	
	referenceLinesOn = false;
		
	for(int i = 0; i < numPoints; i++){
	    
	    addGraphElement( new GraphPoint(i,(double)values.get(i,0), Color.black, 4));
	    
	    //xTicks
	    if(i%5 == 0){
		addGraphElement( new GraphTick((double)i, 10, Color.black, GC.HORIZONTAL, GC.S, String.valueOf(i), Color.black));
	    }
	    else{
		addGraphElement( new GraphTick(i, 5, Color.black, GC.HORIZONTAL, GC.S, "", Color.black));
	    }
	}
	
	
	double yTickIncrement = initValue/10.0;
	
	for(int i = 0; i <= 10; i++){
	    addGraphElement( new GraphTick(i*yTickIncrement, 10, Color.black, GC.VERTICAL, GC.W, String.valueOf(i*10), Color.black));
	}
    }
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new",
    			new Object[]{values});
    }
    
    //These methods are only here to satisfy the IViewer interface
    public void setExperiment (Experiment e){}
    public int getExperimentID(){return 0;}
    public void setExperimentID(int i){}
    
    //Over write super's method to rotatate text
    
    public void drawVerticalTick(Graphics2D g, double x, int length, int alignment, Color color, String label, Color tickColor) {
	drawVerticalTick(g, x, length, alignment, color);
	
	if (false) { //Rotate labels
	    // g.rotate(- Math.PI / 2);
	    //canvas.drawString(g, label, postYSpacing - length - (label.length() * tickFontWidth), convertX(x), labelColor, tickFont);
	    //canvas.drawString(g, label, 750, convertX(x) + canvas.getSize().width, tickColor, tickFont);
	    canvas.drawString(g, label, - canvas.getSize().height + postYSpacing - (label.length() * tickFontWidth) - length,
	    convertX(x) + (tickFontHeight / 2), tickColor, tickFont);
	    //  g.rotate(Math.PI / 2);
	} else {
	    canvas.drawString(g, label, convertX(x) - (label.length() * tickFontWidth / 2),
	    canvas.getSize().height - postYSpacing + length + 10, tickColor, tickFont);
	}
    }
    
    
/*
    public void drawVerticalTick(Graphics2D g, double x, int length, int alignment, Color color, String label, Color tickColor)
    {
	drawVerticalTick(g, x, length, alignment, color);
 
	if (true) { //Rotate labels
	    g.rotate(- Math.PI / 2);
	    //canvas.drawString(g, label, postYSpacing - length - (label.length() * tickFontWidth), convertX(x), labelColor, tickFont);
	    //canvas.drawString(g, label, 750, convertX(x) + canvas.getSize().width, tickColor, tickFont);
	    canvas.drawString(g, label, - canvas.getSize().height + postYSpacing - (label.length() * tickFontWidth) - length,
			      convertX(x) + (tickFontHeight / 2), tickColor, tickFont);
	    g.rotate(Math.PI / 2);
	} else {
	    canvas.drawString(g, label, convertX(x) - (label.length() * tickFontWidth / 2),
			      canvas.getSize().height - postYSpacing + length + 10, tickColor, tickFont);
	}
    }
 */
    
    
    
    
    
    float maxYVal(FloatMatrix data, int col){
	float max = Float.NEGATIVE_INFINITY;
	
	for(int i = 0; i < numPoints; i++){
	    if(data.get(i,col) > max)
		max = data.get(i,col);
	}
	
	return max;
    }
    
    /**
     * Invoked by the framework when this viewer was deselected.
     */
    public void onDeselected() {
    }
    
    /**
     * Invoked by the framework when this viewer is selected.
     */
    public void onSelected(IFramework framework) {
    }
    
    /**
     * Invoked by the framework when display menu is changed,
     * if this viewer is selected.
     * @see IDisplayMenu
     */
    public void onMenuChanged(IDisplayMenu menu) {
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
	return this;
    }
    
    /**
     * Invoked by the framework to save or to print viewer image.
     */
    public BufferedImage getImage() {
	return null;
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
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return -1;
    }
    
}




