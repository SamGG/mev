/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ExperimentHeader.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;

/**
 * This class is used to render header of an experiment.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public class ExperimentHeader extends JPanel implements IExperimentHeader {
    
    
    private static final int RECT_HEIGHT = 15;
    private static final int COLOR_BAR_HEIGHT = 10;
    private Experiment experiment;
    private IData data;
    private int[] samplesOrder;
    private int[][] clusters;
    private int clusterIndex;
    private int elementWidth;
    private boolean isAntiAliasing = true;
    private float maxValue = 3f;
    private float minValue = -3f;
    private float midValue = 0.0f;
    private Insets insets = new Insets(0, 10, 0, 0);
    private BufferedImage negColorImage;
    private BufferedImage posColorImage;
    
    public void setExperiment(Experiment e) {
    	this.experiment = e;
    }
    
    private boolean useDoubleGradient = true;
    
    /**
     * Used for restoring an ExperimentHeader from saved xml file.
     * @param clusters
     * @param samplesOrder
     * @param insets
     
    public ExperimentHeader(int [][] clusters, int[] samplesOrder, Insets insets){
        this.clusters = clusters;
        this.samplesOrder = samplesOrder;
        this.insets = insets;
        setBackground(Color.white);
    }*/
    
    
    protected void setIData(IData d) {this.data = d;}
    
    
    /**
     * Construct an <code>ExperimentHeader</code> with specified experiment.
     */
    public ExperimentHeader(Experiment experiment, int [][] clusters) {
        this(experiment, clusters, null);
    }
    
    /**
     * Construct an <code>ExperimentHeader</code> with specified experiment
     * and samples order.
     */
    public ExperimentHeader(Experiment experiment, int [][] clusters, int[] samplesOrder) {
        this.experiment = experiment;
        this.clusters = clusters;
        this.samplesOrder = samplesOrder == null ? createSamplesOrder(experiment) : samplesOrder;
        setBackground(Color.white);
    }
    public Insets getInsets(){return insets;}
    public Experiment getExperiment() {
    	return experiment;
    }
    public int[][] getClusters() {
    	return clusters;
    }
    /**
     * Returns a component to be inserted into scroll pane view port.
     */
    public JComponent getContentComponent() {
        return this;
    }
    
    private static int[] createSamplesOrder(Experiment experiment) {
        int[] order = new int[experiment.getNumberOfSamples()];
        for (int i=0; i<order.length; i++) {
            order[i] = i;
        }
        return order;
    }
    public int[] getSamplesOrder(){return samplesOrder;}
    public BufferedImage getPosColorImage(){return posColorImage;}
    public BufferedImage getNegColorImage(){return negColorImage;}
    /**
     * Sets data.
     */
    public void setData(IData data) {
        this.data = data;
    }
    public IData getData(){return data;}
    
    /**
     * Sets max and min experiment values.
     */
    public void setValues(float minValue, float midValue, float maxValue) {
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.midValue = midValue;
    }

    /**
     * Sets max and min experiment values.
     */
    public void setValues(float minValue, float maxValue) {
        this.maxValue = maxValue;
        this.minValue = minValue;
    }
    
    /**
     * Sets positive and negative images
     */
    public void setNegAndPosColorImages(BufferedImage neg, BufferedImage pos){
        this.negColorImage = neg;
        this.posColorImage = pos;
    }
    
    /**
     * Sets flag to use a double gradient 
     */
    public void setUseDoubleGradient(boolean useDouble) {
    	this.useDoubleGradient = useDouble;
    }    
     
    /**
     * Sets anti-aliasing property.
     */
    public void setAntiAliasing(boolean isAntiAliasing) {
        this.isAntiAliasing = isAntiAliasing;
    }
    
    /**
     * Sets the left margin for the header
     */
    public void setLeftInset(int leftMargin){
        insets.left = leftMargin;
    }
    
    /**
     * Sets current cluster index
     */
    public void setClusterIndex(int index){
        clusterIndex = index;
    }
    
    /**
     *  Gets current cluster
     */
    private int [] getCluster(){
        return clusters[clusterIndex];
    }
    
    /**
     * Returns height of color bar for experiments
     */
    private int getColorBarHeight(){
        for( int sample = 0; sample < samplesOrder.length ; sample++){
            if(data.getExperimentColor(experiment.getSampleIndex(this.samplesOrder[sample])) != null)
                return COLOR_BAR_HEIGHT;
        }
        return 0;
    }
    
    /**
     * Sets an element width.
     */
    private void setElementWidth(int width) {
        this.elementWidth = width;
        if (width > 12) {
            width = 12;
        }
        setFont(new Font("monospaced", Font.PLAIN, width));
    }
    
    /**
     * Updates size of this header.
     */
    public void updateSizes(int contentWidth, int elementWidth) {
        if(data == null)
            return;
        setElementWidth(elementWidth);
        Graphics2D g = (Graphics2D)getGraphics();
        if (g == null) {
            return;
        }
        if (isAntiAliasing) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        FontMetrics hfm = g.getFontMetrics();
        int maxHeight = 0;
        String name;
        final int size = this.experiment.getNumberOfSamples();
        for (int feature = 0; feature < size; feature++) {
            name = data.getSampleName(experiment.getSampleIndex(feature));
            maxHeight = Math.max(maxHeight, hfm.stringWidth(name));
        }
        maxHeight += RECT_HEIGHT + hfm.getHeight() + 10;
        maxHeight += getColorBarHeight();
        setSize(contentWidth, maxHeight);
        setPreferredSize(new Dimension(contentWidth, maxHeight));
    }
    
    /**
     * Paints the header into specified graphics.
     */
    public void paint(Graphics g1D) {
        super.paint(g1D);
        if (data == null || (this.getCluster().length < 1)) {
            return;
        }
        Graphics2D g = (Graphics2D)g1D;
        if (isAntiAliasing) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        drawHeader(g);
    }
    
    /**
     * Draws the header into specified graphics.
     */
    private void drawHeader(Graphics2D g) {
        final int samples = this.experiment.getNumberOfSamples();
        
        if (samples == 0) {
            return;
        }
        
        int width = samples*elementWidth;
        
        if(useDoubleGradient) {
	        g.drawImage(this.negColorImage, insets.left, 0, (int)(width/2f), RECT_HEIGHT, null);
	        g.drawImage(this.posColorImage, (int)((width)/2f + insets.left), 0, (int)(width/2.0), RECT_HEIGHT, null);
        } else {
	        g.drawImage(this.posColorImage, insets.left, 0, width, RECT_HEIGHT, null);
        }

        FontMetrics hfm = g.getFontMetrics();
        int descent = hfm.getDescent();
        int fHeight = hfm.getHeight();
        
        g.setColor(Color.black);
        
        int textWidth;
        g.drawString(String.valueOf(this.minValue), insets.left, RECT_HEIGHT+fHeight);
        textWidth = hfm.stringWidth(String.valueOf(midValue));
        if(useDoubleGradient)
        	g.drawString(String.valueOf(midValue), (int)(width/2f)-textWidth/2 + insets.left, RECT_HEIGHT+fHeight);
        textWidth = hfm.stringWidth(String.valueOf(this.maxValue));
        g.drawString(String.valueOf(this.maxValue), width-textWidth + insets.left, RECT_HEIGHT+fHeight);
        
        //draw possible clusters
        int h = -getSize().height + 5;
        boolean hasColorBar = false;
        if(this.getColorBarHeight() > 0){
            h += COLOR_BAR_HEIGHT;
            hasColorBar = true;
        }
        
        // draw feature names
        String name;
        g.rotate(-Math.PI/2);
        for (int sample = 0; sample < samples; sample++) {
            name = data.getSampleName(experiment.getSampleIndex(this.samplesOrder[sample]));
            g.drawString(name, h, descent + elementWidth*sample + elementWidth/2 + insets.left);
        }
        g.rotate(Math.PI/2);
        
        if(hasColorBar){
            Color color;
            for(int sample = 0; sample < samples; sample++){
                color = data.getExperimentColor(experiment.getSampleIndex(this.samplesOrder[sample]));
                if(color != null)
                    g.setColor(color);
                else
                    g.setColor(Color.white);
                g.fillRect(sample*elementWidth + insets.left, getSize().height - COLOR_BAR_HEIGHT - 2, elementWidth, COLOR_BAR_HEIGHT);
            }
        } else {
        }
    }
    
}
