/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: PTMExperimentHeader.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-03-24 15:51:08 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.ptm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.helpers.CentroidExperimentHeader;

/**
 *
 * @author  nbhagaba
 * @version
 */
public class PTMExperimentHeader extends javax.swing.JPanel {
    
    private PTMVectorPanel ptmVectorPanel;
    private static final String PTM_TEMPLATE_STRING = "Template";
    private Insets insets = new Insets(0, 10, 0, 0);
    private BufferedImage posColorImage;
    private BufferedImage negColorImage;
    private boolean useDoubleGradient = true;
    private JComponent expHeader;
    private Vector templateVector;

    /** Creates new PTMExperimentHeader */
    
    public PTMExperimentHeader(JComponent expHeader, Vector templateVector) {
    	setLayout(new BorderLayout());
		setBackground(Color.white);
		this.expHeader = expHeader;
		this.templateVector = templateVector;
		this.ptmVectorPanel = new PTMVectorPanel(templateVector);
		add(expHeader, BorderLayout.NORTH);
		add(ptmVectorPanel, BorderLayout.SOUTH);
	}
    public static PersistenceDelegate getPersistenceDelegate(){
    	return new PTMExperimentHeaderPersistenceDelegate();
    }
    
    public Vector getTemplateVector(){return templateVector;}
    public JComponent getExpHeader(){return expHeader;}
    
    public void setUseDoubleGradient(boolean useDouble) {
    	useDoubleGradient = useDouble;
    }
    

    /**
     * The component to display ptm vector.
     */
    private class PTMVectorPanel extends JPanel {
	
	private Vector templateVector;
	private int cluster;
	private float maxValue = 3f;
	private float minValue = -3f;
	private float midValue = 0.0f;
	private Dimension elementSize;
	private boolean drawBorders = true;
	private boolean isAntiAliasing = false;
	private Color missingColor = new Color(128, 128, 128);
	
	/**
	 * Constructs a <code>PTMVectorPanel</code> with specified templateVector.
	 */
	public PTMVectorPanel(Vector templateVector) {
	    setBackground(Color.white);
	    this.templateVector = templateVector;
	}
	
        private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException {
            oos.defaultWriteObject();
            oos.writeObject(this.templateVector);
            oos.writeInt(this.cluster);
            oos.writeFloat(this.maxValue);
            oos.writeFloat(this.minValue);
            oos.writeObject(this.elementSize);
            oos.writeBoolean(this.drawBorders);
            oos.writeBoolean(this.isAntiAliasing);
            oos.writeObject(this.missingColor);
        }
        
        private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
            ois.defaultReadObject();
            this.templateVector = (Vector)ois.readObject();
            this.cluster = ois.readInt();
            this.maxValue = ois.readFloat();
            this.minValue = ois.readFloat();
            this.elementSize = (Dimension)ois.readObject();
            this.drawBorders = ois.readBoolean();
            this.isAntiAliasing = ois.readBoolean();
            this.missingColor = (Color)ois.readObject();
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
	public void setValues(float minValue, float maxValue) {
	    this.maxValue = maxValue;
	    this.minValue = minValue;
	}

	/**
	 * Sets min and max values.
	 */
	public void setValues(float minValue, float midValue, float maxValue) {
	    this.maxValue = maxValue;
	    this.minValue = minValue;
	    this.midValue = midValue;
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
     * Calculates color for passed value.
     */
	   private Color getColor(float value) {
        if (Float.isNaN(value)) {
            return missingColor;
        }
        
        float maximum;
        int colorIndex, rgb;
        
        if(useDoubleGradient) {
        	maximum = value < midValue ? this.minValue : this.maxValue;
			colorIndex = (int) (255 * (value-midValue) / (maximum - midValue));
			if(colorIndex<0)
				colorIndex=-colorIndex;
			colorIndex = colorIndex > 255 ? 255 : colorIndex;
			rgb = value < midValue ? negColorImage.getRGB(255 - colorIndex, 0)
					: posColorImage.getRGB(colorIndex, 0);
        } else {
        	float span = this.maxValue - this.minValue;
        	if(value <= minValue)
        		colorIndex = 0;
        	else if(value >= maxValue)
        		colorIndex = 255;
        	else
        		colorIndex = (int)(((value - this.minValue)/span) * 255);
         	
        	rgb = posColorImage.getRGB(colorIndex,0);
        }
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
    public void setValues(float minValue, float maxValue) {
    	ptmVectorPanel.setValues(minValue, maxValue);
    }

    /**
     * Sets min and max values.
     */
    public void setValues(float minValue, float midValue, float maxValue) {
    	ptmVectorPanel.setValues(minValue, midValue, maxValue);
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
	ptmVectorPanel.setMissingColor(color);
    }
    
    /**
     * Sets the anti aliasing attribute.
     */
    public void setAntiAliasing(boolean value) {
	ptmVectorPanel.setAntiAliasing(value);
    }
    
    private static class PTMExperimentHeaderPersistenceDelegate extends PersistenceDelegate {

		protected Expression instantiate(Object o, Encoder encoder) {
			PTMExperimentHeader oldInstance = (PTMExperimentHeader)o;
			return new Expression(oldInstance, oldInstance.getClass(), "new", 
					new Object[]{oldInstance.getExpHeader(), oldInstance.getTemplateVector()});
		}
		public void initialize(Class type, Object oldInstance, Object newInstance, Encoder encoder) {
			return;
		}
    	
    }
}
