/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: GDMExpGradientLabelPanel.java,v $
 * $Revision: 1.1 $
 * $Date: 2004-02-06 22:53:42 $
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

import java.awt.image.BufferedImage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;



import org.tigr.util.FloatMatrix;

import org.tigr.microarray.mev.cluster.gui.IData;

public class GDMExpGradientLabelPanel extends JScrollPane {
    
    private final int OFFSET = 2;
    private final int RECT_HEIGHT = 10;
    private final int RECT_WIDTH = 200;
    
    private IData expData;
    private Insets insets;
    
    private int contentWidth;
    private int contentHeight;
    private int elementWidth;
    private int elementHeight;
    
    private int tracespace;
    private int maxExpNameLength;
    private int num_experiments;
    
    private int[] indices;
    private int labelIndex;
    
    private boolean isAntiAliasing = false;
    private boolean isTracing = true;
    private boolean isColumnHeader;
    
    private int annotationSize;
    
    private GDMLabelPanel gdmLabelPanel;
    private GDMGradientPanel gdmGradientPanel;
    
    private float minValue = 0.0f;
    private float maxValue = 1.0f;
    
    private int maxTextWidth = 0;
    private int maxTextHeight = 0;
    
    /**
     * Constructs a <code>MultipleArrayHeader</code> with specified
     * insets and trace space.
     */
    public GDMExpGradientLabelPanel(Insets insets, int tracespace, boolean colHdr, IData gData,
    int width, int height, Dimension eSize, int maxExperimentLen, int num_experiments,
    int[] indices) {
        
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
                   
        this.insets = new Insets(0, 0, 0, 0);
        
        this.insets.left = insets.left;
        this.insets.right = insets.right;
        this.insets.top = insets.top;
        this.insets.bottom = insets.bottom;
        
        this.tracespace = tracespace;
        this.expData = gData;
        this.elementWidth = eSize.width;
        this.elementHeight = eSize.height;
        this.isColumnHeader = colHdr;
        this.contentWidth = width;
        this.contentHeight = height;
        this.indices = indices;
        
        this.num_experiments = num_experiments;
        
        this.maxExpNameLength = maxExperimentLen * elementWidth;
        
        gdmLabelPanel = new GDMLabelPanel();
        
        setViewportView(gdmLabelPanel);
        setViewport(getViewport());
        
        gdmGradientPanel = new GDMGradientPanel();
        if (isColumnHeader == true) {
            setColumnHeaderView(gdmGradientPanel);
            setColumnHeader(getColumnHeader());
        } else {
            setRowHeaderView(gdmGradientPanel);
            setRowHeader(getRowHeader());
        }
        
        updateSize();
        
        if (isColumnHeader == true) {
            setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        } else {
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }
        
        getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        
        Listener listener = new Listener();
        addMouseListener(listener);
        
        setBackground(Color.white);
        setOpaque(true);        
    }
    
    private class Listener extends MouseAdapter implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
        }
    }
    
    /**
     * GDMExpGradientLabelPanel: set the min and max values
     */
    public void setValues(float minValue, float maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    /**
     * GDMExpGradientLabelPanel: Sets the component data.
     */
    public void setData(IData expData) {
        this.expData = expData;
    }
    
    /**
     * GDMExpGradientLabelPanel: Sets the number of experiments.
     */
    public void setNumExperiments(int val) {
        this.num_experiments = val;
    }
    
    /**
     * GDMExpGradientLabelPanel: Sets the label index
     */
    public void setLabelIndex(int label) {
        this.labelIndex = label;
    }
    
    
    public void setIndices(int [] indexes) {
        this.indices = indexes;
    }
    
    public int [] getIndices() {
        return this.indices;
    }
    
    /**
     * GDMExpGradientLabelPanel: Gets the label index
     */
    public int getLabelIndex() {
        return this.labelIndex;
    }
    
    public Graphics getLabelPanelGraphics(){
        return this.gdmLabelPanel.getGraphics();
    }
    
    public JComponent getLabelPanel(){
        return this.gdmLabelPanel;
    }
    
    /**
     * GDMExpGradientLabelPanel: Sets the anti-aliasing attribute.
     */
    public void setAntiAliasing(boolean isAntiAliasing) {
        this.isAntiAliasing = isAntiAliasing;
    }
    
    /**
     * GDMExpGradientLabelPanel: Sets the element width attribute.
     */
    void setElementWidth(int width) {
        this.elementWidth = width;
        setFontSize(width);
        gdmLabelPanel.setFontSize(width);
    }
    
    /**
     * GDMExpGradientLabelPanel: Gets the element width attribute.
     */
    int getElementWidth() {
        return this.elementWidth;
    }
    
    /**
     * GDMExpGradientLabelPanel: Sets the annotation size attribute.
     */
    void setAnnotationSize(int size) {
        this.annotationSize = size;
    }
    
    /**
     * GDMExpGradientLabelPanel: Sets the content width attribute.
     */
    void setContentWidth(int width) {
        this.contentWidth = width;
    }
    
    /**
     * GDMExpGradientLabelPanel: Sets the element height attribute.
     */
    void setElementHeight(int height) {
        this.elementHeight = height;
        setFontSize(height);
        gdmLabelPanel.setFontSize(height);
    }
    /**
     * GDMExpGradientLabelPanel: Sets the content height attribute.
     */
    void setContentHeight(int height) {
        this.contentHeight = height;
    }
    
    /**
     * GDMExpGradientLabelPanel: Sets the isTracing attribute.
     */
    void setTracing(boolean isTracing) {
        this.isTracing = isTracing;
    }
    
    /**
     * GDMExpGradientLabelPanel: Returns a trace space value.
     */
    private int getSpacing() {
        if (isTracing) {
            return tracespace;
        }
        return 0;
    }
    
       
    
    /**
     * GDMExpGradientLabelPanel: Sets the component font size.
     */
    private void setFontSize(int size) {
        if (size > 12) {
            size = 12;
        }
        setFont(new Font("monospaced", Font.PLAIN, size));
    }
    
    /**
     * GDMExpGradientLabelPanel: Updates the max experiment name attribute.
     */
    private void updateMaxExpNameLength() {
        Graphics2D g = (Graphics2D)getLabelPanelGraphics();
        if (g == null) {
            return;
        }
        if (isAntiAliasing) {	//Anti-aliasing is on
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        FontMetrics fm = g.getFontMetrics();
        
        
        String expName;
        double maxLength = 0;
        int i = 0;
        for (i = 0; i < num_experiments; i++) {
            expName = expData.getSampleName(i);
            maxLength = Math.max(maxLength, fm.stringWidth(expName));
        }
        maxExpNameLength = (int)maxLength;
    }
    
    
    /**
     * GDMExpGradientLabelPanel: Returns max experiment name width.
     */
    private int getMaxExpNameLength() {
        return maxExpNameLength;
    }
    
    /**
     * GDMExpGradientLabelPanel: Sets max experiment name width.
     */
    private void setMaxExpNameLength(int val) {
        maxExpNameLength = val;
    }
    
    /**
     * GDMExpGradientLabelPanel: returns true if a probe in the current viewer has color
     */
    protected  boolean areExperimentsColored() {
        if (indices == null) return false;
        
        for(int i = 0; i < indices.length; i++){
            if( this.expData.getExperimentColor(indices[i]) != null){
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * GDMExpGradientLabelPanel: Updates the GDMHeader size.
     */
    public void updateSize() {
        
        updateMaxExpNameLength();
        
        gdmGradientPanel.updateSize();
        gdmLabelPanel.updateSize();
        
        if (isColumnHeader == true ) {
            getVerticalScrollBar().setValues(190, 10, 100, 200);
        }
        
        if (isColumnHeader == true) {
            int w = contentWidth + (elementWidth/2);
            int h = RECT_HEIGHT + maxTextHeight + annotationSize + 2*OFFSET + 10;
            
            this.setSize(w, h);
            this.setPreferredSize(new Dimension(w, h));
            
        } else {
            
            int w = RECT_HEIGHT + maxTextWidth + annotationSize + 2*OFFSET + 10;
            int h = contentHeight + (elementHeight/2);
            
            this.setSize(w, h);
            this.setPreferredSize(new Dimension(w, h));
        }       
        validate();
    }
    
    
    /**
     * GDMExpGradientLabelPanel: Sets gradient images.
     */
    public void setPosColorImages(BufferedImage posColorImage) {
        gdmGradientPanel.setPosColorImages(posColorImage);
        gdmGradientPanel.repaint();
    }
    
    /**
     * GDMExpGradientLabelPanel: Sets left margin
     */
    public void setLeftInset(int leftMargin){
        insets.left = leftMargin;
    }
    
    /**
     * GDMExpGradientLabelPanel: Sets top margin
     */
    public void setTopInset(int topMargin){
        insets.top = topMargin;
    }
    
    public Graphics getColorScaleGraphics(){
        return this.gdmGradientPanel.getGraphics();
    }
    
    private class GDMLabelPanel extends JPanel {
        
        public GDMLabelPanel() {
            setBackground(Color.white);
            setOpaque(true);
        }
        
        /**
         *  GDMLabelPanel: Sets the component font size.
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
                w = num_experiments * elementWidth;
                h = maxExpNameLength + 10;
                
                this.setSize(w, h);
                this.setPreferredSize(new Dimension(w, h));
            } else {
                w = maxExpNameLength + 10;
                h = num_experiments * elementHeight;
                
                this.setSize(w, h);
                this.setPreferredSize(new Dimension(w, h));
            }
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
         * GDMLabelPanel: Draws experiments names.
         */
        private void drawLabelHeader(Graphics2D g) {
            int tmp;
            if (num_experiments == 0) {
                return;
            }
            FontMetrics hfm = g.getFontMetrics();
            int descent = hfm.getDescent();
            
            if (isColumnHeader == true) {
                g.rotate(-Math.PI/2);
            }
            
            String expName = "";
            int x, y, w, h;
            int maxLength = 0;
            
            final int samples = expData.getFeaturesCount();
            if (samples == 0) {
                return;
            }
            
            for (int i = 0; i<samples; i++) {
                if (indices != null) {
                    expName = expData.getSampleName(indices[i]);
                } else {
                    expName = expData.getSampleName(i);
                }
                
                maxLength = Math.max(maxLength, hfm.stringWidth(expName));
                if (isColumnHeader == true) {
                    x = insets.top - getSize().height + 1;
                    y = insets.left + descent + (elementWidth+getSpacing())*i + elementWidth/2 + 1;
                    
                } else {
                    x = insets.left + 1;
                    y = insets.top + descent + (elementHeight+getSpacing())*i + elementHeight/2;
                }
                g.drawString(expName, x, y);
            }
            
            if (isColumnHeader == true) {
                g.rotate(Math.PI/2);
            }
            setMaxExpNameLength(maxLength);
        }
        
    }
    
    /**
     * The component to display gdm label vector.
     */
    private class GDMGradientPanel extends JPanel {
        
        private boolean drawBorders = false;
        private Color missingColor = new Color(128, 128, 128);
        private BufferedImage posColorImage;
        private DecimalFormat decFormat;        
        /**
         * Constructs a <code>GDMGradientPanel</code> with specified codes.
         */
        public GDMGradientPanel() {
            setBackground(Color.white);
            decFormat = new DecimalFormat();
            decFormat.setMinimumFractionDigits(1);
            decFormat.setMaximumFractionDigits(3);
        }
        
        /**
         * GDMGradientPanel: Sets the component font size.
         */
        private void setFontSize(int width) {
            if (width > 10) {
                width = 10;
            }
            setFont(new Font("monospaced", Font.PLAIN, width));
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
            
            float midValue = (minValue + maxValue)/2f;
            
            String midString = decFormat.format((double)midValue);  
            String maxString = decFormat.format((double)maxValue);
            String minString = decFormat.format((double)minValue);
            
            //midValue = (float) (new BigDecimal(midValue).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            //minValue = (float) (new BigDecimal(minValue).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            //maxValue = (float) (new BigDecimal(maxValue).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            
            int textWidth1;
            int textWidth2;
            int textWidth3;
            
            if (g != null) {
                hfm = g.getFontMetrics();
                maxTextHeight = hfm.getHeight();
                
                textWidth1 = hfm.stringWidth(midString);
                textWidth2 = hfm.stringWidth(maxString);
                textWidth3 = hfm.stringWidth(minString);
                maxTextWidth = Math.max(textWidth1, textWidth2);
                maxTextWidth = Math.max(maxTextWidth, textWidth3);
            } else {
                maxTextHeight = 4;
                maxTextWidth = 4;
            }
            
            if (isColumnHeader == true) {
                w = contentWidth + (int) (elementWidth/2f);
                h = 2*OFFSET + RECT_HEIGHT + maxTextHeight;
                
                this.setSize(w, h);
                this.setPreferredSize(new Dimension(w, h));
            } else {
                w = 2*OFFSET + RECT_HEIGHT + maxTextWidth;
                h = contentWidth + (int) (elementWidth/2f);
                
                this.setSize(w, h);
                this.setPreferredSize(new Dimension(w, h));
            }
        }
        
        /**
         *  GDMGradientPanel paint.
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
            textWidth2 = hfm.stringWidth(midString);
            textWidth3 = hfm.stringWidth(maxString);
            
            if (isColumnHeader == true) {
                g.drawString(minString, insets.left, RECT_HEIGHT + maxTextHeight);
                g.drawString(midString, insets.left + (int)(RECT_WIDTH/2f)-textWidth1/2, RECT_HEIGHT + maxTextHeight);
                g.drawString(maxString, insets.left + RECT_WIDTH-textWidth2, RECT_HEIGHT + maxTextHeight);
            } else {
                g.rotate(-Math.PI/2);
                g.drawString(minString, insets.left + RECT_HEIGHT, insets.top + maxTextHeight);
                g.drawString(midString, insets.left + RECT_HEIGHT, insets.top + (int)(RECT_WIDTH/2f) + maxTextHeight/2.0f);
                g.drawString(maxString, insets.left + RECT_HEIGHT, insets.top + RECT_WIDTH);
            }
            
        }
        
    }
}
