/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: PTMExperimentSubCentroidsViewer.java,v $
 * $Revision: 1.4 $
 * $Date: 2004-02-13 19:15:05 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.ptm;

import java.util.Vector;

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JComponent;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;

/**
 *
 * @author  nbhagaba
 * @version
 */
public class PTMExperimentSubCentroidsViewer extends JPanel implements IViewer {
    
    //Vector templateVector;
    protected PTMExperimentCentroidViewer centroidViewer;
    
    
    /** Creates new PTMSubCentroidsViewer */
    public PTMExperimentSubCentroidsViewer(Experiment experiment, int[][] clusters, Vector templateVector, String[] auxTitles, Object[][] auxData) {
	//  super(experiment, clusters);
	//this.templateVector = templateVector;
	this.centroidViewer = new PTMExperimentCentroidViewer(experiment, clusters, templateVector, auxTitles, auxData);
	setBackground(Color.white);
	setFont(new Font("monospaced", Font.BOLD, 10));
    }
    
    /**
     * Returns component to be inserted into the framework scroll pane.
     */
    public JComponent getContentComponent() {
	return this;
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
    
    /**
     * There is no a header.
     * @return null
     */
    public JComponent getHeaderComponent() {
	return null;
    }
    
    
    /**
     * Updates data, drawing mode and some attributes of the wrapped viewer.
     */
    public void onSelected(IFramework framework) {
	this.centroidViewer.setData(framework.getData());
	this.centroidViewer.setMode(((Integer)framework.getUserObject()).intValue());
	this.centroidViewer.setAntiAliasing(framework.getDisplayMenu().isAntiAliasing());
    }
    
    /**
     * Updates data of the wrapped viewer.
     */
    public void onDataChanged(IData data) {
	this.centroidViewer.setData(data);
    }
    
    /**
     * Sets mean values to the wrapped viewer.
     */
    public void setMeans(float[][] means) {
	this.centroidViewer.setMeans(means);
    }
    
    /**
     * Sets variances values to the wrapped viewer.
     */
    public void setVariances(float[][] variances) {
	this.centroidViewer.setVariances(variances);
    }
    
    /**
     * Sets codes to the wrapped viewer.
     */
    public void setCodes(float[][] codes) {
	this.centroidViewer.setCodes(codes);
    }
    
    /**
     * Returns the experiment.
     */
    public Experiment getExperiment() {
	return this.centroidViewer.getExperiment();
    }
    
    /**
     * Returns the data.
     */
    protected IData getData() {
	return this.centroidViewer.getData();
    }
    
    /**
     * Returns clusters.
     */
    public int[][] getClusters() {
	return this.centroidViewer.getClusters();
    }
    
    /**
     * Updates some attributes of the wrapped viewer.
     */
    public void onMenuChanged(IDisplayMenu menu) {
	this.centroidViewer.onMenuChanged(menu);
    }
    
    public void onDeselected() {}
    public void onClosed() {}
    
    /**
     * @return null
     */
    public BufferedImage getImage() {
	return null;
    }
    
    /**
     * Paints centroid charts into specified graphics.
     */
    public void paint(Graphics g) {
	super.paint(g);
	
	final int gap = 10;
	final int imagesX = (int)Math.ceil(Math.sqrt(getClusters().length));
	final int imagesY = (int)Math.ceil((float)getClusters().length/(float)imagesX);
	
	final float stepX = (float)(getWidth()-gap)/(float)imagesX;
	final float stepY = (float)(getHeight()-gap)/(float)imagesY;
	Rectangle rect = new Rectangle();
	int cluster;
	for (int y=0; y<imagesY; y++) {
	    for (int x=0; x<imagesX; x++) {
		cluster = y*imagesX+x;
		if (cluster >= getClusters().length) {
		    break;
		}
		this.centroidViewer.setClusterIndex(cluster);
		rect.setBounds((int)Math.round(gap+x*stepX), (int)Math.round(gap+y*stepY), (int)Math.round(stepX-gap), (int)Math.round(stepY-gap));
		this.centroidViewer.paint((Graphics2D)g, rect, false);
	    }
	}
    }
    
    
}
