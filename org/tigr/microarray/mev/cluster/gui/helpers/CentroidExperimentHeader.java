/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: CentroidExperimentHeader.java,v $
 * $Revision: 1.3 $
 * $Date: 2004-07-27 19:59:15 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.helpers;


import java.awt.Font;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JPanel;
import javax.swing.JComponent;

import org.tigr.util.FloatMatrix;

/**
 *  Creates a header with a centroid vector image and centroid label
 */
public class CentroidExperimentHeader extends JPanel implements java.io.Serializable {
    public static final long serialVersionUID = 201010001L;    
    
    private String vectorString;
    private CentroidVectorPanel centroidVectorPanel;
    private int [][] clusters;
    protected int clusterIndex = 0;
    private int currHeight;
    private int currWidth;
    private Insets insets = new Insets(0, 10, 0, 0);
    private ExperimentHeader expHeader;
    private BufferedImage posColorImage;
    private BufferedImage negColorImage;
    
    private FloatMatrix mainCentroidData;
    /**
     * Constructs a <code>CentroidExperimentHeader</code> with specified experiment
     * header, centroid data, and centroid vector label.
     * @param expHeader <code>ExperimentHeader</code> passed header component
     * @param centroidData, has data for all centroids that can be shown in the header
     * @param vectorString, label for centroid vector
     */
    public CentroidExperimentHeader(JComponent expHeader, FloatMatrix centroidData, int [][] clusters, String VectorString) {
        super();
        setLayout(new GridBagLayout());
        setBackground(Color.white);
        this.clusters = clusters;
        this.expHeader = (ExperimentHeader)expHeader;
        this.mainCentroidData = centroidData;
        this.centroidVectorPanel = new CentroidVectorPanel(centroidData, insets);
        add(expHeader, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        add(centroidVectorPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        vectorString = VectorString;
        currWidth = currHeight = 0;
    }
    
    public CentroidExperimentHeader(){ }
    
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeObject(expHeader);
        
        oos.writeObject(clusters);
        oos.writeObject(mainCentroidData);
        oos.writeInt(clusterIndex);
        oos.writeObject(insets);
        oos.writeObject(this.vectorString);
        oos.writeInt(currWidth);
        oos.writeInt(currHeight);
        oos.writeObject(centroidVectorPanel);
    }
    
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        this.expHeader = (ExperimentHeader)ois.readObject();
        
        this.clusters = (int[][])ois.readObject();
        this.mainCentroidData = (FloatMatrix)ois.readObject();
        this.clusterIndex = ois.readInt();
        this.insets = (Insets)ois.readObject();
        this.vectorString = (String)ois.readObject();
        this.currWidth = ois.readInt();
        this.currHeight = ois.readInt();
        this.centroidVectorPanel = (CentroidExperimentHeader.CentroidVectorPanel)ois.readObject();
    }
    
    
    /**
     *  Returns the current width of the header.
     */
    public int getCurrWidth(){
        return currWidth;
    }
    
    /**
     *  Returns the current height of the header.
     */
    public int getCurrHeight(){
        return currHeight + this.expHeader.getHeight();
    }
    
    /**
     *  Returns the current width of the header.
     */
    //  public int getWidth(){
    //      return currWidth;
    //  }
    
    /**
     * Sets index of current cluster to view
     */
    public void setClusterIndex(int index){
        this.clusterIndex = index;
        this.centroidVectorPanel.setCurrentCluster(index);
    }
    
    /**
     * Returns current cluster
     */
    public int [] getCluster(){
        return clusters[clusterIndex];
    }
    
    //  public void paint(Graphics g){
    //      this.expHeader.paint(g);
    //      this.centroidVectorPanel.paint(g);
    //  }
    
    /**
     * The component to display som vector.
     */
    public class CentroidVectorPanel extends JPanel implements java.io.Serializable {
        
        private FloatMatrix codes;
        private int cluster;
        private float maxValue = 3f;
        private float minValue = -3f;
        private Dimension elementSize = new Dimension(20,5);
        private boolean drawBorders = true;
        private boolean isAntiAliasing = false;
        private Color missingColor = new Color(128, 128, 128);
        private Insets insets = new Insets(0,0,0,0);
        
        
        /**
         * Constructs a <code>SOMVectorPanel</code> with specified codes.
         */
        public CentroidVectorPanel(FloatMatrix codes, Insets insets) {
            setBackground(Color.white);
            this.codes = codes;
            this.insets = insets;
        }
        
        public CentroidVectorPanel(){ }
        
        private void writeObject(ObjectOutputStream oos) throws IOException {
            oos.defaultWriteObject();
            oos.writeObject(codes);
            oos.writeInt(this.cluster);
            oos.writeFloat(this.minValue);
            oos.writeFloat(this.maxValue);
            oos.writeObject(elementSize);
            oos.writeBoolean(this.drawBorders);
            oos.writeBoolean(this.isAntiAliasing);
            oos.writeObject(insets);
        }
        
        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ois.defaultReadObject();
            this.codes = (FloatMatrix)ois.readObject();
            this.cluster = ois.readInt();
            this.minValue = ois.readFloat();
            this.maxValue = ois.readFloat();
            this.elementSize = (Dimension)ois.readObject();
            this.drawBorders = ois.readBoolean();
            this.isAntiAliasing = ois.readBoolean();
            this.insets = (Insets)ois.readObject();
        }
        
        
        /**
         * Sets gradient images.
         */
 /*       public void setColorImages(BufferedImage posColorImage, BufferedImage negColorImage) {
            CentroidExperimentHeader.this.posColorImage = posColorImage;
            CentroidExperimentHeader.this.negColorImage = negColorImage;
        }
  */      
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
        
        public int getCurrWidth(){
            return currWidth;
        }
        
        /**
         * Sets left margin
         */
        public void setLeftInset(int leftMargin){
            insets.left = leftMargin;
        }
        
        public int getCurrHeight(){
            return currHeight;
        }
        
        
        /**
         * Updates the component size.
         */
        public void updateSize(Dimension size) {
            this.elementSize = new Dimension(size);
            setFont(new Font("monospaced", Font.PLAIN, size.height));
            Graphics2D g = (Graphics2D)getGraphics();
            
            int strWidth = 0;
            
            if(g != null){
                if (isAntiAliasing) {
                    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                FontMetrics metrics = g.getFontMetrics();
                strWidth = metrics.stringWidth(vectorString)+10;
            }
            
            int width  = size.width*this.codes.getColumnDimension() + strWidth + insets.left + 10;
            int height = size.height+10;
            currWidth = width;
            currHeight = height;
            
            this.setMinimumSize(new Dimension(width, height));
            setPreferredSize(new Dimension(width, height));
            setSize(width, height);
            validate();
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
            g.drawString(vectorString, elementSize.width*samples+10 + insets.left, elementSize.height);
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
        centroidVectorPanel.updateSize(size);
        //expHeader.updateSizes(currWidth, this.centroidVectorPanel.elementSize.width);
    }
    
    /**
     * Sets the current index of a cluster.
     */
    public void setCurrentCluster(int cluster) {
        clusterIndex = cluster;
        centroidVectorPanel.setCurrentCluster(cluster);
    }
    
    /**
     * Sets the draw borders attribute.
     */
    public void setDrawBorders(boolean draw) {
        centroidVectorPanel.setDrawBorders(draw);
    }
    
    /**
     * Sets min and max values.
     */
    public void setValues(float maxValue, float minValue) {
        centroidVectorPanel.setValues(maxValue, minValue);
    }
    
    /**
     * Sets positive and negative images
     */
    public void setNegAndPosColorImages(BufferedImage neg, BufferedImage pos){
        CentroidExperimentHeader.this.negColorImage = neg;
        CentroidExperimentHeader.this.posColorImage = pos;
    }
    
    /**
     * Sets color for NaN values.
     */
    public void setMissingColor(Color color) {
        centroidVectorPanel.setMissingColor(color);
    }
    
    /**
     * Sets the anti aliasing attribute.
     */
    public void setAntiAliasing(boolean value) {
        centroidVectorPanel.setAntiAliasing(value);
    }
    
    
}
