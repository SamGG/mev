/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ExperimentClusterHeader.java,v $
 * $Revision: 1.4 $
 * $Date: 2005-03-10 15:56:09 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Font;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JPanel;
import javax.swing.JComponent;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;


public class ExperimentClusterHeader extends JPanel implements IExperimentHeader{
    
    private static final int RECT_HEIGHT = 15;
    private static final int COLOR_BAR_HEIGHT = 10;
    private Experiment experiment;
    private IData data;
    private int[][] samplesOrder;
    private int elementWidth;
    private boolean isAntiAliasing = true;
    private float maxValue = 3f;
    private float minValue = -3f;
    private float midValue = 0.0f;    
    int clusterIndex = 0;
    private String centroidName;
    private boolean hasCentroid;
    
    private BufferedImage negColorImage;
    private BufferedImage posColorImage;
    
    private Insets insets = new Insets(0, 10, 0, 0);
    
    private boolean useDoubleGradient = true;
    
    
    /**
     * Construct an <code>ExperimentClusterHeader</code> with specified experiment
     * and samples order.
     */
    public ExperimentClusterHeader(Experiment experiment, int[] samplesOrder) {
        this.experiment = experiment;        
        this.samplesOrder = new int[1][];
        this.samplesOrder[0] = samplesOrder;
        this.hasCentroid = false;
        setBackground(Color.white);
    }
    
    /**
     * Construct an <code>ExperimentClusterHeader</code> with specified experiment
     * and samples order.
     */
    public ExperimentClusterHeader(Experiment experiment, int[][] samplesOrder) {
        this.experiment = experiment;
        this.samplesOrder = samplesOrder;
        this.hasCentroid = false;
        setBackground(Color.white);
    }
    
    
    /**
     * Construct an <code>ExperimentClusterHeader</code> with specified experiment
     * and samples order.
     */
    public ExperimentClusterHeader(Experiment experiment, int[][] samplesOrder, String centroidName) {
        this.experiment = experiment;
        this.samplesOrder = samplesOrder;
        this.centroidName = centroidName;
        this.hasCentroid = true;
        setBackground(Color.white);
    }
        /**
     * Returns a component to be inserted into scroll pane view port.
     */
    public JComponent getContentComponent(){
        return this;
    }
    
    /**
     * Sets data.
     */
    public void setData(IData data) {
        this.data = data;
    }
    
    public void setClusterIndex(int index){
        this.clusterIndex = index;
    }
    
    public void setUseDoubleGradient(boolean useDouble) {
        useDoubleGradient = useDouble;
    }
    
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
     * Sets anti-aliasing property.
     */
    public void setAntiAliasing(boolean isAntiAliasing) {
        this.isAntiAliasing = isAntiAliasing;
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
     * Sets the left margin for the header
     */
    public void setLeftInset(int leftMargin){
        insets.left = leftMargin;
    }
    
    /**
     * Updates size of this header.
     */
    public void updateSizes(int contentWidth, int elementWidth) {
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
        
        final int size = this.samplesOrder[clusterIndex].length;
        
        for (int feature = 0; feature < size; feature++) {
            name = data.getSampleName(experiment.getSampleIndex(samplesOrder[clusterIndex][feature]));
            maxHeight = Math.max(maxHeight, hfm.stringWidth(name));
        }
        if(hasCentroid)
            maxHeight = Math.max(maxHeight, hfm.stringWidth(this.centroidName));
        
        maxHeight += RECT_HEIGHT + hfm.getHeight() + 10;
        maxHeight += getColorBarHeight();
        if(!hasCentroid){
            setSize(contentWidth, maxHeight);
            setPreferredSize(new Dimension(contentWidth, maxHeight));
       }
        else{
            setSize(contentWidth, maxHeight);
            setPreferredSize(new Dimension(contentWidth, maxHeight));
        }

    }
    
    /**
     * Returns height of color bar for experiment clustering
     */
    private int getColorBarHeight(){
        for( int sample = 0; sample < samplesOrder[clusterIndex].length ; sample++){
            if(data.getExperimentColor(experiment.getSampleIndex(this.samplesOrder[clusterIndex][sample])) != null)
                return COLOR_BAR_HEIGHT;
        }
        return 0;
    }
    
    /**
     * Paints the header into specified graphics.
     */
    public void paint(Graphics g1D) {
        super.paint(g1D);
        if (data == null) {
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
        final int samples = this.samplesOrder[clusterIndex].length;
        if (samples == 0) {
            return;
        }
        
        int width = samples*elementWidth;
        if(this.hasCentroid)
            width += this.elementWidth + 5;
        if(useDoubleGradient) {
        	g.drawImage(this.negColorImage, insets.left, 0, (int)(width/2f), RECT_HEIGHT, null);
        	g.drawImage(this.posColorImage, (int)((width/2f)+insets.left), 0, (int)(width/2f), RECT_HEIGHT, null);
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
        	g.drawString(String.valueOf(midValue), (int)((width+insets.left)/2f)-textWidth/2, RECT_HEIGHT+fHeight);
        textWidth = hfm.stringWidth(String.valueOf(this.maxValue));
        g.drawString(String.valueOf(this.maxValue), (width-textWidth)+insets.left, RECT_HEIGHT+fHeight);

        // draw feature names
        g.rotate(-Math.PI/2);
        String name;
        int h = -getSize().height + 5;
        boolean hasColorBar = false;
        int centroidNameOffset = 0;
        
        if(this.getColorBarHeight() > 0){
            h += COLOR_BAR_HEIGHT;
            hasColorBar = true;
        }
        
        if(this.hasCentroid){
            centroidNameOffset = elementWidth + 5;
            g.drawString(this.centroidName, h, descent + elementWidth/2 + insets.left);
        }
        
        for (int sample = 0; sample < samples; sample++) {
            name = data.getSampleName(experiment.getSampleIndex(this.samplesOrder[clusterIndex][sample]));
            g.drawString(name, h, descent + elementWidth*sample + elementWidth/2 + insets.left + centroidNameOffset);
        }
        g.rotate(Math.PI/2);
        if(hasColorBar){
            Color color;
            for(int sample = 0; sample < samples; sample++){
                color = data.getExperimentColor(experiment.getSampleIndex(this.samplesOrder[clusterIndex][sample]));
                if(color != null)
                    g.setColor(color);
                else
                    g.setColor(Color.white);
                g.fillRect(sample*elementWidth + insets.left + centroidNameOffset, getSize().height - COLOR_BAR_HEIGHT - 2, elementWidth, COLOR_BAR_HEIGHT);
            }
        }
    }
        
    private void writeObject(ObjectOutputStream oos) throws IOException {       
        oos.writeObject(experiment);            
        oos.writeObject(samplesOrder);
        oos.writeInt(elementWidth);
        oos.writeObject(insets);
        oos.writeBoolean(this.hasCentroid);
        if(this.hasCentroid){
            oos.writeObject(this.centroidName);
        }            
    }
    
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {     
        experiment = (Experiment)ois.readObject();
        samplesOrder = (int[][])ois.readObject();
        elementWidth = ois.readInt();
        insets = (Insets)ois.readObject();
        if(ois.readBoolean()){
            this.hasCentroid = true;
            this.centroidName = (String)ois.readObject();
        }
    }
    
}

