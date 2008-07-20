/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.impl.lem;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;

/**
 * @author braisted
 * 
 * LEMHeader displays sample names, the current gradient option either gradient
 * or bin colors
 */
public class LEMHeader extends JPanel {
	    
	    private static final int RECT_HEIGHT = 15;
	    private static final int COLOR_BAR_HEIGHT = 10;
	    private int MIN_WIDTH = 180;
	    private Experiment experiment;
	    private IData data;
	    private int[] samplesOrder;
		
	    private int columnSpacing;
	    private int arrowWidth;
	    private int wingWidth;
	    private boolean isAntiAliasing = true;
	    private float maxValue = 3f;
	    private float minValue = -3f;
	    private float midValue = 0.0f;
	    private Insets insets = new Insets(0, 10, 0, 0);

	    private int colorMode = LinearExpressionMapViewer.COLOR_MODE_GRADIENT;	   

	    private BufferedImage negColorImage;
	    private BufferedImage posColorImage;
	    
	    private Color c1 = new Color(10, 159, 1);
	    private Color c2 = new Color(187, 240, 181);
	    private Color midColor = Color.white;
	    private Color c3 = new Color(243, 169, 160);
	    private Color c4 = Color.red;
	    	    
		private float cutoff1 = -3f;
		private float cutoff2 = -1f;
		private float binMidValue = 0f;
		private float cutoff3 = 1f;
		private float cutoff4 = 3f;
	    
	    private boolean useDoubleGradient = true;
	    
	    /**
	     * Construct an <code>ExperimentHeader</code> with specified experiment.
	     */
	    public LEMHeader(Experiment experiment) {
	        this(experiment, null);
	    }
	    
	    /**
	     * Construct an <code>ExperimentHeader</code> with specified experiment
	     * and samples order.
	     */
	    public LEMHeader(Experiment experiment, int[] samplesOrder) {
	        this.experiment = experiment;
	        this.samplesOrder = samplesOrder == null ? createSamplesOrder(experiment) : samplesOrder;
	        setBackground(Color.white);
	    }
	    
	    /**
	     * Returns a component to be inserted into scroll pane view port.
	     */
	    public JComponent getContentComponent() {
	        return this;
	    }
	    
	    /**
	     * returns the default sample ordering
	     * @param experiment
	     * @return
	     */
	    private static int[] createSamplesOrder(Experiment experiment) {
	        int[] order = new int[experiment.getNumberOfSamples()];
	        for (int i=0; i<order.length; i++) {
	            order[i] = i;
	        }
	        return order;
	    }
	    
	    /**
	     * Sets data.
	     */
	    public void setData(IData data) {
	        this.data = data;
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
	     * sets the color bin policy, gradient, 3 bin, or 5 bin
	     * @param binOption gradient, 3 bin, or 5 bin as specified by
	     * <code>LinearExpressionMapViewer</code> static fields
	     */
	    public void setColorBinPolicy(int binOption) {
	    	colorMode = binOption;	    
	    }
	    
	    /**
	     * Sets bin colors
	     * @param c1
	     * @param c2
	     * @param c3
	     * @param c4
	     */
	    public void setBinColors(Color c1, Color c2, Color c3, Color c4) {
	    	this.c1 = c1;
	    	this.c2 = c2;
	    	this.c3 = c3;
	    	this.c4 = c4;	    	
	    }

	    /**
	     * sets bin cutoffs
	     * @param c1
	     * @param c2
	     * @param mid
	     * @param c3
	     * @param c4
	     */
	    public void setBinCutoffs(float c1, float c2, float mid, float c3, float c4) {
	    	this.cutoff1 = c1;
	    	this.cutoff2 = c2;
	    	this.binMidValue = mid;
	    	this.cutoff3 = c3;
	    	this.cutoff4 = c4;	    	
	    }
	    
	    /**
	     * Sets arrow width property to match the rendered arrow width (shaft width)
	     * @param arrowWidth
	     */
	    public void setArrowWidth(int arrowWidth) {
	    	this.arrowWidth = arrowWidth;
	    }
	    
	    /**
	     * Sets the width of the arrow 'wing' from shaft to secondary point
	     * @param arrowWingWidth
	     */
	    public void setArrowWingWidth(int arrowWingWidth) {
	    	this.wingWidth = arrowWingWidth;
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
	    public void setColumnSpacing(int width) {
	        this.columnSpacing = width;
	        if (width > 12) {
	            width = 12;
	        }
	        setFont(new Font("monospaced", Font.PLAIN, width));
	    }
	    
	    /**
	     * Updates size of this header.
	     */
	    public void updateSizes(int contentWidth, int columnSpacing) {
	        if(data == null)
	            return;
	        setColumnSpacing(columnSpacing);
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
	        if(contentWidth < MIN_WIDTH)
	        	contentWidth = MIN_WIDTH;
	        setSize(contentWidth, maxHeight);
	        setPreferredSize(new Dimension(contentWidth, maxHeight));
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
	        final int samples = this.experiment.getNumberOfSamples();
	        
	        if (samples == 0) {
	            return;
	        }
	        
	        int width = samples*columnSpacing;
	        if(width < MIN_WIDTH)
	        	width = MIN_WIDTH;
	        
	        int textWidth;
	        FontMetrics hfm = g.getFontMetrics();
	        int descent = hfm.getDescent();
	        int fHeight = hfm.getHeight();	        

	        //color image rep
	        
	        if(colorMode == LinearExpressionMapViewer.COLOR_MODE_GRADIENT){
	        	if(useDoubleGradient) {
	        		g.drawImage(this.negColorImage, insets.left, 0, (int)(width/2f), RECT_HEIGHT, null);
	        		g.drawImage(this.posColorImage, (int)((width)/2f + insets.left), 0, (int)(width/2.0), RECT_HEIGHT, null);
	        	} else {
	        		g.drawImage(this.posColorImage, insets.left, 0, width, RECT_HEIGHT, null);
	        	}
	        	
	        	//render limits
	        	g.setColor(Color.black);		        		        
		        g.drawString(String.valueOf(this.minValue), insets.left, RECT_HEIGHT+fHeight);
		        textWidth = hfm.stringWidth(String.valueOf(midValue));
		        if(useDoubleGradient)
		        	g.drawString(String.valueOf(midValue), (int)(width/2f)-textWidth/2 + insets.left, RECT_HEIGHT+fHeight);
		        textWidth = hfm.stringWidth(String.valueOf(this.maxValue));
		        g.drawString(String.valueOf(this.maxValue), width-textWidth + insets.left, RECT_HEIGHT+fHeight);

	        } else if(colorMode == LinearExpressionMapViewer.COLOR_MODE_2_BIN) {
	        	//fill bins
	        	g.setColor(c1);
	        	g.fillRect(insets.left, 0, (int)(width/4f), RECT_HEIGHT);
	        	g.setColor(midColor);
	        	g.fillRect(insets.left + (int)(width/4f), 0, (int)(width/4f), RECT_HEIGHT);
	        	g.fillRect(insets.left + 2*(int)(width/4f), 0, (int)(width/4f), RECT_HEIGHT);
	        	g.setColor(c4);
	        	g.fillRect(insets.left + 3*(int)(width/4f), 0, (int)(width/4f), RECT_HEIGHT);	        		        

	        	//outline bins
	        	g.setColor(Color.black);
	        	g.drawRect(insets.left, 0, (int)(width/4f), RECT_HEIGHT);
	        	g.drawRect(insets.left + (int)(width/4f), 0, (int)(width/4f), RECT_HEIGHT);
	        	g.drawRect(insets.left + 2*(int)(width/4f), 0, (int)(width/4f), RECT_HEIGHT);
	        	g.drawRect(insets.left + 3*(int)(width/4f), 0, (int)(width/4f), RECT_HEIGHT);	  

	        	//cutoff labels
	        	g.drawString(String.valueOf(cutoff1), insets.left + (int)(width/4f)- (hfm.stringWidth(String.valueOf(cutoff1))/2), RECT_HEIGHT + fHeight);
	        	g.drawString(String.valueOf(binMidValue), insets.left + 2*(int)(width/4f)- (hfm.stringWidth(String.valueOf(binMidValue))/2), RECT_HEIGHT + fHeight);
	        	g.drawString(String.valueOf(cutoff4), insets.left + 3*(int)(width/4f)- (hfm.stringWidth(String.valueOf(cutoff4))/2), RECT_HEIGHT + fHeight);
	        	
	        } else if(colorMode == LinearExpressionMapViewer.COLOR_MODE_4_BIN) {
	        	//fill bins
	        	g.setColor(c1);
	        	g.fillRect(insets.left, 0, (int)(width/6f), RECT_HEIGHT);
	        	g.setColor(c2);
	        	g.fillRect(insets.left + (int)(width/6f), 0, (int)(width/6f), RECT_HEIGHT);
	        	g.setColor(midColor);
	        	g.fillRect(insets.left + 2*(int)(width/6f), 0, (int)(width/6f), RECT_HEIGHT);
	        	g.fillRect(insets.left + 3*(int)(width/6f), 0, (int)(width/6f), RECT_HEIGHT);
	        	g.setColor(c3);
	        	g.fillRect(insets.left + 4*(int)(width/6f), 0, (int)(width/6f), RECT_HEIGHT);	        		        
	        	g.setColor(c4);
	        	g.fillRect(insets.left + 5*(int)(width/6f), 0, (int)(width/6f), RECT_HEIGHT);	        		        

	        	//outline bins
	        	g.setColor(Color.black);
	        	g.drawRect(insets.left, 0, (int)(width/6f), RECT_HEIGHT);
	        	g.drawRect(insets.left + (int)(width/6f), 0, (int)(width/6f), RECT_HEIGHT);
	        	g.drawRect(insets.left + 2*(int)(width/6f), 0, (int)(width/6f), RECT_HEIGHT);
	        	g.drawRect(insets.left + 3*(int)(width/6f), 0, (int)(width/6f), RECT_HEIGHT);
	        	g.drawRect(insets.left + 4*(int)(width/6f), 0, (int)(width/6f), RECT_HEIGHT);	        		        
	        	g.drawRect(insets.left + 5*(int)(width/6f), 0, (int)(width/6f), RECT_HEIGHT);	        		        	        	

	        	//cutoff labels
	        	g.drawString(String.valueOf(cutoff1), insets.left + (int)(width/6f) - (hfm.stringWidth(String.valueOf(cutoff1))/2), RECT_HEIGHT + fHeight);	        	
	        	g.drawString(String.valueOf(cutoff2), insets.left + 2*(int)(width/6f) - (hfm.stringWidth(String.valueOf(cutoff2))/2), RECT_HEIGHT + fHeight);
	        	g.drawString(String.valueOf(binMidValue), insets.left + 3*(int)(width/6f) - (hfm.stringWidth(String.valueOf(binMidValue))/2), RECT_HEIGHT + fHeight);	        	
	        	g.drawString(String.valueOf(cutoff3), insets.left + 4*(int)(width/6f)- (hfm.stringWidth(String.valueOf(cutoff3))/2), RECT_HEIGHT + fHeight);
	        	g.drawString(String.valueOf(cutoff4), insets.left + 5*(int)(width/6f)- (hfm.stringWidth(String.valueOf(cutoff4))/2), RECT_HEIGHT + fHeight);
	        }
	        
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
	        int currX = insets.left + arrowWidth/2 + wingWidth + descent;
	        for (int sample = 0; sample < samples; sample++) {
	            name = data.getSampleName(experiment.getSampleIndex(this.samplesOrder[sample]));
	            g.drawString(name, h, currX);
	            currX += columnSpacing;	            
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
	                g.fillRect(sample*columnSpacing + insets.left, getSize().height - COLOR_BAR_HEIGHT - 2, columnSpacing, COLOR_BAR_HEIGHT);
	            }
	        }
	    }
	    
	    /*
	    private void writeObject(ObjectOutputStream oos) throws IOException {       
	        oos.writeObject(experiment);              
	        oos.writeObject(samplesOrder);
	        oos.writeInt(columnSpacing);
	        oos.writeObject(insets);
	        oos.writeBoolean(useDoubleGradient);
	    }
	    
	    
	    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {     
	        experiment = (Experiment)ois.readObject();
	        samplesOrder = (int[])ois.readObject();
	        columnSpacing = ois.readInt();
	        insets = (Insets)ois.readObject();
	        this.useDoubleGradient = ois.readBoolean();
	    }
	    */
}
