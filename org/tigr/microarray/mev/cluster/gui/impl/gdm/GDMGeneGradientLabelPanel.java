/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: GDMGeneGradientLabelPanel.java,v $
 * $Revision: 1.2 $
 * $Date: 2004-02-13 21:36:44 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.gdm;

import java.io.File;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.*;

import java.awt.image.BufferedImage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.tigr.util.FloatMatrix;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.Experiment;

public class GDMGeneGradientLabelPanel extends JScrollPane {
    
    private final int OFFSET = 5;
    private final int RECT_HEIGHT = 10;
    private final int RECT_WIDTH = 200;
    
    private IData expData;
    private Insets insets;
    private int contentWidth;
    private int contentHeight;
    private int elementWidth;
    private int elementHeight;
    private int tracespace;
    private int maxGeneNameLength;
    private int num_genes;
    private int [] indices;
    private int labelIndex;
    private Experiment experiment;
    
    private boolean isAntiAliasing = false;
    private boolean isTracing = true;
    private boolean isColumnHeader;
    
    private int annotationSize;
    
    private GDMLabelPanel gdmLabelPanel;
    private GDMGradientPanel gdmGradientPanel;
    
    private float minValue = 0.0f;
    private float maxValue = 1.0f;
    
    private int maxColorScaleTextWidth = 0;
    private int maxColorScaleTextHeight = 0;
    
    private DecimalFormat decFormat;
    /**
     * Constructs a <code>MultipleArrayHeader</code> with specified
     * insets and trace space.
     */
    public GDMGeneGradientLabelPanel(Insets insets, int tracespace, boolean colHdr, Experiment experiment,
    int width, int height, Dimension eSize, int maxGeneLen, int num_genes,
    int [] indexes) {
        
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
        
        this.insets = new Insets(0, 0, 0, 0);

        this.tracespace = tracespace;
        this.experiment = experiment;
        this.elementWidth = eSize.width;
        this.elementHeight = eSize.height;
        this.isColumnHeader = colHdr;
        this.contentWidth = width;
        this.contentHeight = height;
        this.indices = indexes;
        
        this.num_genes = num_genes;
        
        this.maxGeneNameLength = maxGeneLen * elementWidth;
        
        gdmGradientPanel = new GDMGradientPanel();
        gdmLabelPanel = new GDMLabelPanel();
        
        setViewportView(gdmLabelPanel);
        setViewport(getViewport());
        
        
        
        if (isColumnHeader == true) {
            setColumnHeaderView(gdmGradientPanel);
            setColumnHeader(getColumnHeader());
            setViewportBorder(BorderFactory.createEmptyBorder(2,0,2,0));
        } else {
            setRowHeaderView(gdmGradientPanel);
            setRowHeader(getRowHeader());
            setViewportBorder(BorderFactory.createEmptyBorder(0,2,0,2));
        }
        
        updateSize();
        
        if (isColumnHeader == true) {
            setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        } else {
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        }
        
        getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        
        decFormat = new DecimalFormat();
        decFormat.setMinimumFractionDigits(1);
        decFormat.setMaximumFractionDigits(3);
             
        setBackground(Color.white);
        setOpaque(true);
    }
    
    private class Listener extends MouseAdapter implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
        }
        
        public void mouseMoved(MouseEvent event) {
            
        }
        public void mouseExited(MouseEvent event) {}
        public void mouseEntered(MouseEvent event) {}
        public void mouseDragged(MouseEvent event) {}
    }
    
    public void setValues(float minValue, float maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    /**
     * Sets the component data.
     */
    public void setData(IData data) {
        this.expData = data;
    }
    
    public void setNumGenes(int val) {
        this.num_genes = val;
    }
    
    public void setIndices(int [] indexes) {
        this.indices = indexes;
    }
    
    public int [] getIndices() {
        return this.indices;
    }
    
    /**
     * Sets the label index
     */
    public void setLabelIndex(int label) {
        this.labelIndex = label;
    }
    
    public int getLabelIndex() {
        return this.labelIndex;
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
        gdmLabelPanel.setFontSize(width);
    }
    
    int getElementWidth() {
        return this.elementWidth;
    }
    
    public JComponent getLabelPanel(){
        return this.gdmLabelPanel;
    }
    
    /**
     * Sets the max header width attribute.
     */
    void setAnnotationSize(int size) {
        this.annotationSize = size;
    }
    
    /**
     * Sets the content width attribute.
     */
    void setContentWidth(int width) {
        this.contentWidth = width;
    }
    
    /**
     * Sets the element height attribute.
     */
    void setElementHeight(int height) {
        this.elementHeight = height;
        setFontSize(height);
        gdmLabelPanel.setFontSize(height);
    }
    /**
     * Sets the content height attribute.
     */
    void setContentHeight(int height) {
        this.contentHeight = height;
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
    private void setFontSize(int size) {
        if (size > 12) {
            size = 12;
        }
        setFont(new Font("monospaced", Font.PLAIN, size));
    }
    
    private void updateMaxGeneNameLength() {
        Graphics2D g = (Graphics2D)getGraphics();
        if (g == null) {
            return;
        }
        if (isAntiAliasing) {	//Anti-aliasing is on
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        FontMetrics fm = g.getFontMetrics();
        String geneName="";
        int maxLength = 0;
        int [] indices = getIndices();
        
        for (int i = 0; i<num_genes; i++) {
            if (indices != null) {
                if (labelIndex >= 0) {
                    geneName = expData.getElementAttribute(experiment.getGeneIndexMappedToData(indices[i]), labelIndex);
                }
            } else {
                if (labelIndex >= 0) {
                    geneName = expData.getElementAttribute(i, labelIndex);
                }
            }
            maxLength = Math.max(maxLength, fm.stringWidth(geneName));
        }
        maxGeneNameLength = maxLength;
    }
    
    /**
     * Returns max label width.
     */
    private int getMaxGeneNameLength() {
        return maxGeneNameLength;
    }
    
    private void setMaxGeneNameLength(int val) {
        maxGeneNameLength = val;
    }
    
    /**
     *  GDMGeneGradientLabelPanel: updateSize.
     */
    public void updateSize() {
        
        updateMaxGeneNameLength();
        gdmGradientPanel.updateSize();
        gdmLabelPanel.updateSize();
        
        if (isColumnHeader == true ) {
            getVerticalScrollBar().setValues(190, 10, 100, 200);
            //this.getViewport().setViewPosition(new Point(0, this.getViewport().getHeight()));
            //  this.scrollRectToVisible(new Rectangle(0, 20, getWidth(), 10));
            
        } else {
            getHorizontalScrollBar().setValues(190, 10, 100, 200);
        }
        
        
        
        if (isColumnHeader == true) {
            int w = contentWidth + (int) (elementWidth/2);
            int h = RECT_HEIGHT + maxColorScaleTextHeight + annotationSize + 2*OFFSET + 10+4;
            
            this.setSize(w, h);
            this.setPreferredSize(new Dimension(w, h));
            
        } else {
            int w = RECT_HEIGHT + maxColorScaleTextWidth + annotationSize + 2*OFFSET + 10+4;
            int h = contentHeight + (elementHeight/2);
            
            this.setSize(w, h);
            this.setPreferredSize(new Dimension(w, h));
        }
        this.getViewport().setViewSize(gdmLabelPanel.getSize());
        //       this.getViewport().setSize(gdmLabelPanel.getSize());
        //     this.getViewport().setPreferredSize(new Dimension(gdmLabelPanel.getSize()));
        //this.doLayout();
        validate();
    }
    
    
    /**
     * Sets gradient images.
     */
    public void setPosColorImages(BufferedImage posColorImage) {
        gdmGradientPanel.setPosColorImages(posColorImage);
        gdmGradientPanel.repaint();
    }
    
    /**
     * Sets left margin
     */
    public void setLeftInset(int leftMargin){
        insets.left = leftMargin;
    }
    
    /**
     * Sets top margin
     */
    public void setTopInset(int topMargin){
        insets.top = topMargin;
    }
    
    /**
     * returns true if a probe in the current viewer has color
     */
    public boolean areProbesColored() {
        if (indices == null) return false;
        
        for(int i = 0; i < indices.length; i++){
            if( this.expData.getProbeColor(experiment.getGeneIndexMappedToData(indices[i])) != null){
                return true;
            }
        }
        return false;
    }
    
    
    private class GDMLabelPanel extends JPanel {
        
        public GDMLabelPanel() {
            setBackground(Color.white);
            setOpaque(true);
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
         * GDMLabelPanel: updateSize
         */
        public void updateSize() {
            int w, h;
            
            if (isColumnHeader == true) {
                w = contentWidth + (int) (elementWidth/2f);
                h = getMaxGeneNameLength() + 10;
                
            } else {
                w = getMaxGeneNameLength() + 10;
                h = contentHeight + (int) (elementHeight/2f);
            }
            
            this.setSize(w, h);
            this.setMinimumSize(new Dimension(w,h));
            this.setPreferredSize(new Dimension(w, h));
            
        }
        
        /**
         * GDMLabelPanel: paint
         */
        public void paint(Graphics g1D) {
            super.paint(g1D);
            if (expData == null || getElementWidth() <= 2) {
                return;
            }
            Graphics2D g = (Graphics2D)g1D;
            if (isAntiAliasing) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
            
            drawLabelHeader(g);
        }
        
        /**
         * GDMLabelPanel: Draws gene names.
         */
        private void drawLabelHeader(Graphics2D g) {
            int tmp;
            if (num_genes == 0) {
                return;
            }
            FontMetrics hfm = g.getFontMetrics();
            int descent = hfm.getDescent();
            
            if (isColumnHeader == true) {
                g.rotate(-Math.PI/2);
            }
            
            String geneName = "";
            int x, y, w, h;
            int maxLength = 0;
            int [] indices = getIndices();
            
            for (int i = 0; i<num_genes; i++) {
                if (indices != null) {
                    if (labelIndex >= 0) {
                        geneName = expData.getElementAttribute(experiment.getGeneIndexMappedToData(indices[i]), labelIndex);
                    }
                } else {
                    if (labelIndex >= 0) {
                        geneName = expData.getElementAttribute(i, labelIndex);
                    }
                }
                maxLength = Math.max(maxLength, hfm.stringWidth(geneName));
                if (isColumnHeader == true) {
                    //x = insets.top - getSize().height + 2;
                    x = insets.top - (getMaxGeneNameLength() + 10) + 5;
                    y = insets.left + descent + (elementWidth+getSpacing())*i + (int) (elementWidth/2) + 1;
                    
                } else {
                    x = insets.left + 5;
                    y = insets.top + descent + (elementHeight+getSpacing())*i + (int) (elementHeight/2);
                }
                g.drawString(geneName, x, y);
            }
            
            if (isColumnHeader == true) {
                g.rotate(Math.PI/2);
            }
            
            setMaxGeneNameLength(maxLength);
        }
    }
    
    public Graphics getColorScaleGraphics(){
        return this.gdmGradientPanel.getGraphics();
    }
    
    public Graphics getLabelPanelGraphics(){
        return this.gdmLabelPanel.getGraphics();
    }
    
    /**
     * The component to display gdm vector.
     */
    private class GDMGradientPanel extends JPanel {
        
        private boolean drawBorders = false;
        private Color missingColor = new Color(128, 128, 128);
        private BufferedImage posColorImage;
        private DecimalFormat decFormat;
        /**
         * GDMGradientPanel: Constructs a <code>GDMGradientPanel</code> with specified codes.
         */
        public GDMGradientPanel() {
            setBackground(Color.white);
            setOpaque(true);
            decFormat = new DecimalFormat();
            decFormat.setMinimumFractionDigits(1);
            decFormat.setMaximumFractionDigits(3);
        }
        
        /**
         * GDMGradientPanel: Sets gradient images.
         */
        public void setPosColorImages(BufferedImage posColorImage) {
            this.posColorImage = posColorImage;
        }
        
        /**
         * GDMGradientPanel: Sets color for NaN values.
         */
        public void setMissingColor(Color color) {
            this.missingColor = color;
        }
        
        /**
         * GDMGradientPanel: Sets the draw borders attribute.
         */
        public void setDrawBorders(boolean draw) {
            this.drawBorders = draw;
        }
        
        /**
         *  GDMGradientPanel: updateSize.
         */
        public void updateSize() {
            int w, h;
            
            Graphics2D g = (Graphics2D)getGraphics();
            FontMetrics hfm;
            int textHeight = 0;
            float midValue = (minValue + maxValue)/2f;
            
            String midString = decFormat.format((double)midValue);
            String maxString = decFormat.format((double)maxValue);
            String minString = decFormat.format((double)minValue);
            int textWidth1;
            int textWidth2;
            int textWidth3;
            
            if (g != null) {
                hfm = g.getFontMetrics();
                maxColorScaleTextHeight = hfm.getHeight();
                
                textWidth1 = hfm.stringWidth(midString);
                textWidth2 = hfm.stringWidth(maxString);
                textWidth3 = hfm.stringWidth(minString);
                
                maxColorScaleTextWidth = Math.max(textWidth1, textWidth2);
                maxColorScaleTextWidth = Math.max(maxColorScaleTextWidth, textWidth3);
                
            } else {
                maxColorScaleTextHeight = 4;
                maxColorScaleTextWidth = 4;
            }
            
            if (isColumnHeader == true) {
                w = contentWidth + (int) (elementWidth/2f);
                h = 2*OFFSET + RECT_HEIGHT + maxColorScaleTextHeight;
                
                this.setSize(w, h);
                this.setPreferredSize(new Dimension(w, h));
            } else {
                w = 2*OFFSET + RECT_HEIGHT + maxColorScaleTextWidth;
                h = contentHeight + (int) (elementHeight/2f);
                
                this.setSize(w, h);
                this.setPreferredSize(new Dimension(w, h));
            }
        }
        
        /**
         *  GDMGradientPanel: paint.
         */
        public void paint(Graphics g1D) {
            
            super.paint(g1D);
            if (expData == null || expData.getFeaturesCount() == 0) {
                return;
            }
            
            Graphics2D g = (Graphics2D)g1D;
            
            if (isColumnHeader == true) {
                g.drawImage(posColorImage, insets.left, insets.top, RECT_WIDTH, RECT_HEIGHT, null);
            } else {
                g.rotate(Math.PI/2);
                g.drawImage(posColorImage, insets.top, 0 - RECT_HEIGHT - insets.left, RECT_WIDTH, RECT_HEIGHT, null);
            }
            
            FontMetrics hfm = g.getFontMetrics();
            int descent = hfm.getDescent();
            
            g.setColor(Color.black);
            if (isAntiAliasing) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
            
            int textWidth1;
            int textWidth2;
            int textWidth3;
            
            float midValue = (minValue + maxValue)/2f;
            
            String midString = decFormat.format(midValue);
            String minString = decFormat.format(minValue);
            String maxString = decFormat.format(maxValue);
            
            textWidth1 = hfm.stringWidth(minString);
            textWidth2 = hfm.stringWidth(maxString);
            textWidth3 = hfm.stringWidth(midString);
            
            if (isColumnHeader == true) {
                g.drawString(minString, insets.left, RECT_HEIGHT + maxColorScaleTextHeight);
                g.drawString(midString, insets.left + (int)(RECT_WIDTH/2f)-textWidth3/2, RECT_HEIGHT + maxColorScaleTextHeight);
                g.drawString(maxString, insets.left + RECT_WIDTH-textWidth2, RECT_HEIGHT + maxColorScaleTextHeight);
            } else {
                g.rotate(-Math.PI/2);
                g.drawString(minString, insets.left + RECT_HEIGHT, insets.top + maxColorScaleTextHeight);
                g.drawString(midString, insets.left + RECT_HEIGHT, insets.top + (int)(RECT_WIDTH/2f) + maxColorScaleTextHeight/2.0f);
                g.drawString(maxString, insets.left + RECT_HEIGHT, insets.top + RECT_WIDTH);
            }
        }
        
    }
    
}
