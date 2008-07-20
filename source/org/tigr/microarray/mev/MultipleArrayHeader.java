/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: MultipleArrayHeader.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:44:13 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.IData;

public class MultipleArrayHeader extends JPanel {
    
    private IData data;
    private Insets insets;
    private int contentWidth;
    private int elementWidth = 20;
    private boolean isAntiAliasing = true;
    private boolean isTracing = false;
    private int tracespace;
    private final int RECT_HEIGHT = 15;
    
    BufferedImage negColorImage;
    BufferedImage posColorImage;
    private float maxValue;
    private float minValue;
    private float midValue;
    private boolean useDoubleGradient = true;
    
    /**
     * Constructs a <code>MultipleArrayHeader</code> with specified
     * insets and trace space.
     */
    public MultipleArrayHeader(Insets insets, int tracespace) {
        setBackground(Color.white);
        this.insets = insets;
        this.tracespace = tracespace;
        this.maxValue = 3.0f;
        this.minValue = -3.0f;
        this.midValue = 0.0f;
    }
    
    /**
     * Sets the component data.
     */
    public void setData(IData data) {
        this.data = data;
        updateSize();
        this.repaint();
    }
    
    /**
     * Sets the anti-aliasing attribute.
     */
    public void setAntiAliasing(boolean isAntiAliasing) {
        this.isAntiAliasing = isAntiAliasing;
    }
    
    /**
     * Sets the element width attribute.
     */
    void setElementWidth(int width) {
        this.elementWidth = width;
        setFontSize(width);
        updateSize();
        this.repaint();
    }
    
    /**
     * Sets the content width attribute.
     */
    void setContentWidth(int width) {
        this.contentWidth = width;
        this.repaint();
    }
    
    public void setUseDoubleGradient(boolean useDouble) {
    	this.useDoubleGradient = useDouble;    
    }
    
    /**
     * Sets min and max ratio values
     */
    public void setMinAndMaxRatios(float min, float max){
        this.minValue = min;
        this.maxValue = max;
        this.repaint();
    }
    
    /**
     * Sets min and max ratio values
     */
    public void setMinAndMaxAndMidRatios(float min, float mid, float max){
        this.minValue = min;
        this.midValue = mid;
        this.maxValue = max;        
        this.repaint();
    }
    
    /**
     * Sets positive and negative color images
     */
    public void setNegativeAndPositiveColorImages(BufferedImage neg, BufferedImage pos){
        this.negColorImage = neg;
        this.posColorImage = pos;
    }
    
    /**
     * Sets the isTracing attribute.
     */
    void setTracing(boolean isTracing) {
        this.isTracing = isTracing;
    }
    
    /**
     * Returns a trace space value.
     */
    private int getSpacing() {
        if (isTracing) {
            return tracespace;
        }
        return 0;
    }
    
    /**
     * Sets the component font size.
     */
    private void setFontSize(int width) {
        if (width > 12) {
            width = 12;
        }
        setFont(new Font("monospaced", Font.PLAIN, width));
    }
    
    /**
     * Updates the header size.
     */
    void updateSize() {
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
        final int size = data.getFeaturesCount();
        for (int feature = 0; feature < size; feature++) {
            name = data.getSampleName(feature);
            maxHeight = Math.max(maxHeight, hfm.stringWidth(name));
        }
        
    //    if(!this.isTracing)
     //   contentWidth = this.data.getFeaturesCount() * this.elementWidth;
    //    else
    //   contentWidth = (this.data.getFeaturesCount() - 1) * (this.elementWidth + getSpacing()) + this.elementWidth ;    
        
        setSize(contentWidth, maxHeight+10+this.RECT_HEIGHT+hfm.getHeight());
        setPreferredSize(new Dimension(contentWidth, maxHeight+10+this.RECT_HEIGHT+hfm.getHeight()));
    }
    
    /**
     * Paints the header into specified graphics.
     */
    public void paint(Graphics g1D) {
        super.paint(g1D);
        if (this.data == null || this.data.getFeaturesCount() == 0) {
            return;
        }

        Graphics2D g = (Graphics2D)g1D;
        int width;
        if(!this.isTracing)
            width = this.data.getFeaturesCount() * this.elementWidth;
        else
            width = (this.data.getFeaturesCount() - 1) * (this.elementWidth + getSpacing()) + this.elementWidth ;    
        if(useDoubleGradient) {
        	g.drawImage(this.negColorImage, insets.left, 0, (int)(width/2f), RECT_HEIGHT, null);
        	g.drawImage(this.posColorImage, (int)(width/2f)+insets.left, 0, (int)(width/2.0), RECT_HEIGHT, null);
        } else {        	
	        g.drawImage(this.posColorImage, insets.left, 0, width, RECT_HEIGHT, null);
        }
        
        FontMetrics hfm = g.getFontMetrics();
        int descent = hfm.getDescent();
        int fHeight = hfm.getHeight();
        
        g.setColor(Color.black);
        if (isAntiAliasing) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        int textWidth;
        g.drawString(String.valueOf(this.minValue), insets.left, RECT_HEIGHT+fHeight);
        textWidth = hfm.stringWidth(String.valueOf(midValue));
        if(useDoubleGradient)
        	g.drawString(String.valueOf(midValue), (int)(width/2f)-textWidth/2 + insets.left, RECT_HEIGHT+fHeight);
        textWidth = hfm.stringWidth(String.valueOf(this.maxValue));
        g.drawString(String.valueOf(this.maxValue), (width-textWidth)+insets.left, RECT_HEIGHT+fHeight);
                
        drawColumnHeaders(g);
    }
    
    /**
     * Draws microarrays names.
     */
    private void drawColumnHeaders(Graphics2D g) {
        final int size = data.getFeaturesCount();
        if (size == 0) {
            return;
        }
        FontMetrics hfm = g.getFontMetrics();
        int descent = hfm.getDescent();
        g.rotate(-Math.PI/2);
        String name;
        for (int feature = 0; feature < size; feature++) {
            name = data.getSampleName(feature);
            g.drawString(name, insets.bottom - getSize().height +5, insets.left + descent + (elementWidth+getSpacing())*feature + elementWidth/2);
        }
        g.rotate(Math.PI/2);
    }
}
