/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SOMExperimentHeader.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:22:04 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.som;

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

import org.tigr.util.FloatMatrix;

public class SOMExperimentHeader extends JPanel {
    
    private SOMVectorPanel somVectorPanel;
    private int [][] clusters;
    private int clusterIndex;
    private static final String SOM_VECTOR_STRING = "SOM Vector";
    private Insets insets = new Insets(0, 10, 0, 0);
    private boolean useDoubleGradient = true;
    
    /**
     * Construct a <code>SOMExperimentHeader</code> with specified experiment
     * header and codes.
     */
    public SOMExperimentHeader(JComponent expHeader, FloatMatrix codes, int [][] clusters) {
        setLayout(new BorderLayout());
        setBackground(Color.white);
        this.somVectorPanel = new SOMVectorPanel(codes);
        this.clusters = clusters;
        add(expHeader, BorderLayout.NORTH);
        add(somVectorPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Sets index of current cluster to view
     */
    public void setClusterIndex(int index){
        this.clusterIndex = index;
    }
    
    /**
     * Returns current cluster
     */
    private int [] getCluster(){
        return clusters[clusterIndex];
    }
    
    /**
     * sets flag to use double or single gradient
     * @param useDouble 
     */
    private void setUseDoubleGradient(boolean useDouble) {
    	useDoubleGradient = useDouble;    
    }
    
    /**
     * The component to display som vector.
     */
    private class SOMVectorPanel extends JPanel {
        
        private FloatMatrix codes;
        private int cluster;
        private float maxValue = 3f;
        private float minValue = -3f;
        private float midValue = 0.0f;
        private Dimension elementSize;
        private boolean drawBorders = true;
        private boolean isAntiAliasing = false;
        private Color missingColor = new Color(128, 128, 128);
        private BufferedImage posColorImage;
        private BufferedImage negColorImage;
        
        /**
         * Constructs a <code>SOMVectorPanel</code> with specified codes.
         */
        public SOMVectorPanel(FloatMatrix codes) {
            setBackground(Color.white);
            this.codes = codes;
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
            clusterIndex = cluster;
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
         * Sets left margin
         */
        public void setLeftInset(int leftMargin){
            insets.left = leftMargin;
        }
        
        /**
         * Sets min and max values.
         */
        public void setValues(float minValue, float maxValue) {
            this.maxValue = maxValue;
            this.minValue = minValue;
        }

        public void setValues(float minValue, float midValue, float maxValue) {
            this.maxValue = maxValue;
            this.minValue = minValue;
            this.midValue = midValue;
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
            int strWidth = metrics.stringWidth(SOM_VECTOR_STRING)+10;
            
            int width  = size.width*this.codes.getColumnDimension() +strWidth;
            int height = size.height+10;
            setSize(width, height);
            setPreferredSize(new Dimension(width, height));
        }
        
        /**
         * Paints the component into specified graphics.
         */
        public void paint(Graphics g) {
            super.paint(g);
            if(getCluster().length < 1)
                return;
            
            if (isAntiAliasing) {
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
            final int samples = codes.getColumnDimension();
            for (int i=0; i<samples; i++) {
                fillRectAt(g, i, getColor(codes.get(this.cluster, i)));
                if (this.drawBorders) {
                    drawRectAt(g, i, Color.black);
                }
            }
            g.setColor(Color.black);
            g.drawString(SOM_VECTOR_STRING, elementSize.width*samples+10 + insets.left, elementSize.height);
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
            g.drawRect(sample*elementSize.width + insets.left, 0, elementSize.width-1, elementSize.height-1);
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
        somVectorPanel.updateSize(size);
    }
    
    /**
     * Sets the current index of a cluster.
     */
    public void setCurrentCluster(int cluster) {
        somVectorPanel.setCurrentCluster(cluster);
    }
    
    /**
     * Sets the draw borders attribute.
     */
    public void setDrawBorders(boolean draw) {
        somVectorPanel.setDrawBorders(draw);
    }
    
    /**
     * Sets min and max values.
     */
    public void setValues(float minValue, float maxValue) {
        somVectorPanel.setValues(minValue, maxValue);
    }
    
    /**
     * Sets min, mid, and max values.
     */
    public void setValues(float minValue, float midValue, float maxValue) {
        somVectorPanel.setValues(minValue, midValue, maxValue);
    }    
    
    /**
     * Sets gradient images.
     */
    public void setColorImages(BufferedImage posColorImage, BufferedImage negColorImage) {
        somVectorPanel.setColorImages(posColorImage, negColorImage);
    }
    
    /**
     * Sets color for NaN values.
     */
    public void setMissingColor(Color color) {
        somVectorPanel.setMissingColor(color);
    }
    
    /**
     * Sets the anti aliasing attribute.
     */
    public void setAntiAliasing(boolean value) {
        somVectorPanel.setAntiAliasing(value);
    }
}
