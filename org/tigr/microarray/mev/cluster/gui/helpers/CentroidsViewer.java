/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: CentroidsViewer.java,v $
 * $Revision: 1.4 $
 * $Date: 2004-02-13 19:15:03 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.helpers;

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

/**
 * This class is used to draw set of centroid charts.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public class CentroidsViewer extends JPanel implements IViewer, java.io.Serializable {
    
    /** Wrapped centroid viewer */
    protected CentroidViewer centroidViewer;
    
    /**
     * Constructs a <code>CentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public CentroidsViewer(Experiment experiment, int[][] clusters) {
		this(new CentroidViewer(experiment,clusters));
	//	this.centroidViewer = new CentroidViewer(experiment, clusters);
	//	setBackground(Color.white);
	//	setFont(new Font("monospaced", Font.BOLD, 10));
    }
	
    /**
     * Constructs a <code>CentroidsViewer</code> for specified experiment
     * and clusters.
     */    
	public CentroidsViewer(CentroidViewer cv) {
		this.centroidViewer = cv;
		setBackground(Color.white);
		setFont(new Font("monospaced", Font.BOLD, 10));
	}
	
    /**
     * Returns component to be inserted into the framework scroll pane.
     */
    public JComponent getContentComponent() {
		return this;
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
		this.centroidViewer.onSelected(framework);
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
		int cluster;
		Rectangle rect = new Rectangle();
		
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
    
}
