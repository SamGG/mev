/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: PTMExperimentHeader.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.ptm;

import java.util.Vector;

import java.awt.Font;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.BorderLayout;
import java.awt.RenderingHints;

import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JComponent;

/**
 *
 * @author  nbhagaba
 * @version
 */
public class PTMExperimentHeader extends javax.swing.JPanel {
    
    private PTMVectorPanel ptmVectorPanel;
    private static final String PTM_TEMPLATE_STRING = "Template";
    private Insets insets = new Insets(0, 10, 0, 0);
    /** Creates new PTMExperimentHeader */
    
    public PTMExperimentHeader(JComponent expHeader, Vector templateVector) {
	setLayout(new BorderLayout());
	setBackground(Color.white);
	this.ptmVectorPanel = new PTMVectorPanel(templateVector);
	add(expHeader, BorderLayout.NORTH);
	add(ptmVectorPanel, BorderLayout.SOUTH);
    }
    
    /**
     * The component to display ptm vector.
     */
    private class PTMVectorPanel extends JPanel {
	
	private Vector templateVector;
	private int cluster;
	private float maxValue = 3f;
	private float minValue = -3f;
	private Dimension elementSize;
	private boolean drawBorders = true;
	private boolean isAntiAliasing = false;
	private Color missingColor = new Color(128, 128, 128);
	private BufferedImage posColorImage;
	private BufferedImage negColorImage;
	
	/**
	 * Constructs a <code>PTMVectorPanel</code> with specified templateVector.
	 */
	public PTMVectorPanel(Vector templateVector) {
	    setBackground(Color.white);
	    this.templateVector = templateVector;
	}
	
	/**
	 * Sets gradient images.
	 */
	public void setColorImages(BufferedImage posColorImage, BufferedImage negColorImage) {
	    this.posColorImage = posColorImage;
	    this.negColorImage = negColorImage;
	}
	
	/**
	 * Sets color for NaN values.
	 */
	public void setMissingColor(Color color) {
	    this.missingColor = color;
	}
	
	/**
	 * Sets current index of a cluster.
	 */
	public void setCurrentCluster(int cluster) {
	    this.cluster = cluster;
	}
	
	/**
	 * Sets the draw borders attribute.
	 */
	public void setDrawBorders(boolean draw) {
	    this.drawBorders = draw;
	}
	
	/**
	 * Sets the anti aliasing attribute.
	 */
	public void setAntiAliasing(boolean value) {
	    this.isAntiAliasing = value;
	}
	
	/**
	 * Sets min and max values.
	 */
	public void setValues(float maxValue, float minValue) {
	    this.maxValue = maxValue;
	    this.minValue = minValue;
	}
        
        /**
         * Sets left margin
         */
        public void setLeftInset(int leftMargin){
            insets.left = leftMargin;
        }
	
	/**
	 * Updates the component size.
	 */
	public void updateSize(Dimension size) {
	    this.elementSize = new Dimension(size);
	    setFont(new Font("monospaced", Font.PLAIN, size.height));
	    Graphics2D g = (Graphics2D)getGraphics();
	    if (isAntiAliasing) {
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    }
	    FontMetrics metrics = g.getFontMetrics();
	    int strWidth = metrics.stringWidth(PTM_TEMPLATE_STRING)+10;
	    
	    int width  = size.width*this.templateVector.size() +strWidth;
	    int height = size.height+10;
	    setSize(width, height);
	    setPreferredSize(new Dimension(width, height));
	}
	
	/**
	 * Paints the component into specified graphics.
	 */
	public void paint(Graphics g) {
	    super.paint(g);
	    if (isAntiAliasing) {
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    }
	    final int samples = templateVector.size();
	    Vector scaledTemplateVector = new Vector();
	    float max = getMax(templateVector);
	    float min = getMin(templateVector);
	    
	    for (int i = 0; i < templateVector.size(); i++) {
		float current = ((Float)templateVector.get(i)).floatValue();
		if (current == max) {
		    scaledTemplateVector.add(new Float(3.0f));
		} else if (current == min) {
		    scaledTemplateVector.add(new Float(-3.0f));
		} else {
		    float scaledValue = 6*((current - min)/(max - min)) - 3;
		    scaledTemplateVector.add(new Float(scaledValue));
		}
	    }
	    
	    for (int i=0; i<samples; i++) {
		fillRectAt(g, i, getColor(((Float)scaledTemplateVector.get(i)).floatValue()));
		if (this.drawBorders) {
		    drawRectAt(g, i, Color.black);
		}
	    }
	    g.setColor(Color.black);
	    g.drawString(PTM_TEMPLATE_STRING, elementSize.width*samples+10 + insets.left, elementSize.height);
	}
	
	private float getMax(Vector gene) {
	    float max = Float.MIN_VALUE;
	    
	    for(int i = 0; i < gene.size(); i++) {
		if (! Float.isNaN(((Float)gene.get(i)).floatValue())) {
		    float current = ((Float)gene.get(i)).floatValue();
		    if (current > max) max = current;
		}
	    }
	    
	    return max;
	}
	
	
	private float getMin(Vector gene) {
	    float min = Float.MAX_VALUE;
	    
	    for(int i = 0; i < gene.size(); i++) {
		if (! Float.isNaN(((Float)gene.get(i)).floatValue())) {
		    float current = ((Float)gene.get(i)).floatValue();
		    if (current < min) min = current;
		}
	    }
	    
	    return min;
	}
	
	/**
	 * Fill in rect for the specified sample.
	 */
	private void fillRectAt(Graphics g, int sample, Color color) {
	    g.setColor(color);
	    g.fillRect(sample*elementSize.width + insets.left, 0, elementSize.width, elementSize.height);
	}
	
	/**
	 * Draw rect for the specified sample.
	 */
	private void drawRectAt(Graphics g, int sample, Color color) {
	    g.setColor(color);
	    g.drawRect(sample*elementSize.width, 0, elementSize.width-1, elementSize.height-1);
	}
	/**
	 * Calculates a color for the specified value.
	 */
	
	private Color getColor(float value) {
	    if (Float.isNaN(value) || posColorImage == null || negColorImage == null) {
		return missingColor;
	    }
	    float maximum = value < 0 ? this.minValue : this.maxValue;
	    int colorIndex = (int)(255*value/maximum);
	    colorIndex = colorIndex > 255 ? 255 : colorIndex;
	    int rgb = value < 0 ? negColorImage.getRGB(255-colorIndex, 0) : posColorImage.getRGB(colorIndex, 0);
	    return new Color(rgb);
	}
	
	
    }
    
    /**
     * Updates the viewer size.
     */
    public void updateSize(Dimension size) {
	ptmVectorPanel.updateSize(size);
    }
    
    /**
     * Sets the current index of a cluster.
     */
    public void setCurrentCluster(int cluster) {
	ptmVectorPanel.setCurrentCluster(cluster);
    }
    
    /**
     * Sets the draw borders attribute.
     */
    public void setDrawBorders(boolean draw) {
	ptmVectorPanel.setDrawBorders(draw);
    }
    
    /**
     * Sets min and max values.
     */
    public void setValues(float maxValue, float minValue) {
	ptmVectorPanel.setValues(maxValue, minValue);
    }
    
    /**
     * Sets gradient images.
     */
    public void setColorImages(BufferedImage posColorImage, BufferedImage negColorImage) {
	ptmVectorPanel.setColorImages(posColorImage, negColorImage);
    }
    
    /**
     * Sets color for NaN values.
     */
    public void setMissingColor(Color color) {
	ptmVectorPanel.setMissingColor(color);
    }
    
    /**
     * Sets the anti aliasing attribute.
     */
    public void setAntiAliasing(boolean value) {
	ptmVectorPanel.setAntiAliasing(value);
    }
    
}
