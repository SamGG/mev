/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: GDMGeneHeader.java,v $
 * $Revision: 1.6 $
 * $Date: 2005-03-10 20:22:00 $
 * $Author: braistedj $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.gdm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import javax.swing.JPanel;
import javax.swing.JScrollBar;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;

public class GDMGeneHeader extends JPanel {
    
    private static final String GDM_VECTOR_STRING = "GDM Vector";
    
    private int [] indices;
    private int labelIndex;
    private Insets insets;
    private int num_genes;
    private boolean showClusters = true;
    private boolean hasColorBar = false;
    private boolean isColumnHeader;
    
    private IData expData;
    private int contentWidth;
    private int contentHeight;
    private int elementWidth;
    private int elementHeight;
    private int tracespace;
    private int maxGeneNameLength;
    private Experiment experiment;
    
    private boolean isAntiAliasing = false;
    private boolean isTracing = true;
    
    private final int OFFSET = 5;
    private final int MIN_LABEL_PANEL_SIZE = 1;
    private final int MAX_LABEL_PANEL_SIZE = 200;
    private static final int NOT_UPDATE_ANNOTATION_SIZE = -1;
    
    private int annotationSize;
    
    private GDMGeneGradientLabelPanel gdmGradientLabelPanel;
    private GDMColorBarPanel gdmColorBarPanel;
    
    private float minValue = 0.0f;
    private float maxValue = 1.0f;
    
    private final int RECT_HEIGHT = 10;
    private final int RECT_WIDTH = 200;
    private int maxColorScaleTextWidth = 0;
    private int maxColorScaleTextHeight = 0;
    
    private int probes;
    
    private int headerWidth;
    private int headerHeight;
    
    private int gradientPanelWidth;
    private int gradientPanelHeight;
    
    private int labelPanelWidth;
    private int labelPanelHeight;
    
    private int colorBarWidth;
    private int colorBarHeight;
    private DecimalFormat decFormat;
    private boolean sortByGeneProximity = true;
    private ActionListener matrixListener;
    
    private int currentIndex = 0;
    
    /**
     * Constructs a <code>MultipleArrayHeader</code> with specified
     * insets and trace space.
     */
    public GDMGeneHeader(Insets insets, int tracespace, boolean colHdr, Experiment experiment,
    int width, int height, Dimension eSize, int maxGeneLen, int num_genes,
    int [] indexes) {
        
        this.setLayout(new BorderLayout());
        
        this.insets = new Insets(0, 0, 0, 0);

        this.tracespace = tracespace;
        this.experiment = experiment;
        this.elementWidth = eSize.width;
        this.elementHeight = eSize.height;
        this.isColumnHeader = colHdr;
        this.contentWidth = width;
        this.contentHeight = height;
        this.indices = indexes;
        this.probes = experiment.getNumberOfGenes();
        this.num_genes = num_genes;
        this.maxGeneNameLength = maxGeneLen * elementWidth;
        
        gdmGradientLabelPanel = new GDMGeneGradientLabelPanel(insets, tracespace, colHdr, experiment, width,
        height, eSize, maxGeneLen, num_genes, indexes);
        
        gdmColorBarPanel = new GDMColorBarPanel();
        
        setFontSize(elementWidth);

        if (isColumnHeader == true) {
            add(gdmGradientLabelPanel, BorderLayout.NORTH);
            if(hasColorBar)
                add(gdmColorBarPanel, BorderLayout.CENTER);
        } else {
            add(gdmGradientLabelPanel, BorderLayout.WEST);
            if(hasColorBar)
                add(gdmColorBarPanel, BorderLayout.CENTER);
        }
        
        decFormat = new DecimalFormat();
        decFormat.setMaximumFractionDigits(3);
        decFormat.setMinimumFractionDigits(1);
        
        Listener listener = new Listener();
        gdmGradientLabelPanel.getLabelPanel().addMouseListener(listener);
        gdmGradientLabelPanel.getLabelPanel().addMouseMotionListener(listener);
        
        setBackground(Color.white);
        setOpaque(true);
    }
    
    
    private class Listener extends MouseAdapter implements ActionListener, MouseMotionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
        }
        
        public void mouseClicked(MouseEvent evt){
            if(sortByGeneProximity){
                int loc = 0;
                if(GDMGeneHeader.this.isColumnHeader)
                    loc = evt.getX();
                else
                    loc = evt.getY();
                int index = (int)((loc - insets.left)/elementWidth);
                fireGDMSortEvent(indices[index]);
            }
        }
        
        public void mouseMoved(MouseEvent event) {
            if(sortByGeneProximity){
                Graphics2D g2D = (Graphics2D)gdmGradientLabelPanel.getLabelPanelGraphics();
                if(g2D == null)
                    return;
                
                int x = event.getX();
                int y = event.getY();
                int elementIndex;
                if(isColumnHeader){
                    elementIndex = x/elementWidth;
                    if(elementIndex >= num_genes){
                        repaint();
                        return;
                    }
                    if(elementIndex != currentIndex){
                        g2D.setColor(Color.white);
                        g2D.drawRect(currentIndex*elementWidth, 0, elementWidth, gdmGradientLabelPanel.getLabelPanel().getHeight()-1);
                    }
                    g2D.setColor(Color.blue);
                    g2D.drawRect(elementIndex*elementWidth, 0, elementWidth, gdmGradientLabelPanel.getLabelPanel().getHeight()-1);
                } else {
                    elementIndex = y/elementWidth;
                    if(elementIndex >= num_genes){
                        repaint();
                        return;
                    }
                    if(elementIndex != currentIndex){
                        g2D.setColor(Color.white);
                        g2D.drawRect( 0, currentIndex*elementWidth, gdmGradientLabelPanel.getLabelPanel().getWidth()-1, elementHeight);
                    }
                    g2D.setColor(Color.blue);
                    g2D.drawRect(0, elementIndex*elementWidth, gdmGradientLabelPanel.getLabelPanel().getWidth()-1, elementHeight);
                }               
                currentIndex = elementIndex;
            }
        }
        
        public void mouseExited(MouseEvent event) {
            repaint();
        }
        public void mouseEntered(MouseEvent event) {}
        public void mouseDragged(MouseEvent event) {}
    }
    
    /**
     * Sets the label index
     */
    public void setLabelIndex(int label) {
        this.labelIndex = label;
        this.gdmGradientLabelPanel.setLabelIndex(label);
    }
    
    /**
     * Sets matrix listener
     */
    public void setMatrixListener(ActionListener aL){
        this.matrixListener = aL;
    }
    
    public void setSortByGeneProximity(boolean allowSort){
        this.sortByGeneProximity = allowSort;
    }
    
    /**
     *  GDMGeneHeader: updateSize.
     */
    public void updateSize(int size) {
        
        int geneNameLength;
        updateMaxGeneNameLength();
        geneNameLength = getMaxGeneNameLength();
        
        int maxTextSize=0;
        
        if (size == NOT_UPDATE_ANNOTATION_SIZE) {
            if (geneNameLength < MIN_LABEL_PANEL_SIZE) {
                setAnnotationSize(MIN_LABEL_PANEL_SIZE);
            } else if (geneNameLength > MAX_LABEL_PANEL_SIZE) {
                setAnnotationSize(MAX_LABEL_PANEL_SIZE);
            } else {
                setAnnotationSize(geneNameLength);
            }
        } else {
            setAnnotationSize(size);
        }
        
        boolean tempBool;
        if(showClusters) {
            tempBool = areProbesColored();
            if(tempBool && !hasColorBar){
                this.add(gdmColorBarPanel, BorderLayout.CENTER);
                this.validate();
            } else if(!tempBool && hasColorBar){
                this.remove(gdmColorBarPanel);
                this.validate();
            }
            hasColorBar = tempBool;
        }
        else {
            hasColorBar = false;
        }
        
        if (isColumnHeader == true) {
            headerWidth = contentWidth + (int) (elementWidth/2);
            gradientPanelWidth = headerWidth;
            labelPanelWidth = headerWidth;
            colorBarWidth = headerWidth;
            colorBarHeight = 0;
            
        } else {
            headerHeight = contentHeight + (int) (elementHeight/2);
            gradientPanelHeight = headerHeight;
            labelPanelHeight = headerHeight;
            colorBarWidth = 0;
            colorBarHeight = headerHeight;
            
        }
        
        Graphics2D g = (Graphics2D)getColorScaleGraphics();
        FontMetrics hfm;
        int textHeight = 0;
        
        if (g != null) {
            hfm = g.getFontMetrics();
            maxColorScaleTextHeight = hfm.getHeight();
            
            float midValue = (minValue + maxValue)/2f;
            String minString = decFormat.format(minValue);
            String maxString = decFormat.format(maxValue);
            String midString = decFormat.format(midValue);
            // midValue = (float) (new BigDecimal(midValue).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            //  minValue = (float) (new BigDecimal(minValue).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            //  maxValue = (float) (new BigDecimal(maxValue).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            int textWidth1 = hfm.stringWidth(minString);
            int textWidth2 = hfm.stringWidth(maxString);
            int textWidth3 = hfm.stringWidth(midString);
            
            maxColorScaleTextWidth = Math.max(Math.max(textWidth1, textWidth2), textWidth3);
            textHeight = hfm.getHeight();
        }
        
        maxTextSize = Math.max(maxColorScaleTextWidth, maxColorScaleTextHeight);
        
        
        if (isColumnHeader == true) {
            
            gradientPanelHeight = RECT_HEIGHT + textHeight + 2*OFFSET;
            labelPanelHeight = annotationSize + 10;// + (elementHeight/2);
            
            if (hasColorBar) {
                colorBarHeight = OFFSET + (elementHeight >= 10 ? elementHeight : 10) ;
            } else {
                colorBarHeight = 0;
            }
            
            headerHeight =  gradientPanelHeight + labelPanelHeight + colorBarHeight + 4;
            
            this.setSize(headerWidth, headerHeight);
            this.setPreferredSize(new Dimension(headerWidth, headerHeight));
        } else {
            
            gradientPanelWidth = RECT_HEIGHT + maxColorScaleTextWidth + 2*OFFSET;
            labelPanelWidth = annotationSize + 10;// + (elementHeight/2);
            
            if (hasColorBar) {
                colorBarWidth = OFFSET + (elementWidth >= 10 ? elementWidth : 10);
            } else {
                colorBarWidth = 0;
            }
            
            headerWidth = gradientPanelWidth + labelPanelWidth + colorBarWidth + 4;
            
            this.setSize(headerWidth, headerHeight);
            this.setPreferredSize(new Dimension(headerWidth, headerHeight));
        }
        
        gdmGradientLabelPanel.updateSize();
        gdmColorBarPanel.updateSize();
        validate();
    }
    
    private Graphics getColorScaleGraphics(){
        return this.gdmGradientLabelPanel.getColorScaleGraphics();
    }
    
    
    
    private void updateMaxGeneNameLength() {
        Graphics2D g = (Graphics2D)this.gdmGradientLabelPanel.getLabelPanelGraphics();
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
                    geneName = expData.getElementAttribute(experiment.getGeneIndexMappedToData(i), labelIndex);
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
    
    public void setNumGenes(int num_genes){
        this.num_genes = num_genes;
        this.gdmGradientLabelPanel.setNumGenes(num_genes);
    }
    
    public void setIndices(int [] indexes) {
        this.indices = indexes;
        this.gdmGradientLabelPanel.setIndices(indexes);
    }
    
    public int [] getIndices() {
        return this.indices;
    }
    
    /**
     * Sets gradient images.
     */
    public void setPosColorImages(BufferedImage posColorImage) {
        this.gdmGradientLabelPanel.setPosColorImages(posColorImage);
        this.gdmGradientLabelPanel.repaint();
    }
    
    public JScrollBar getVerticalScrollBar() {
        return gdmGradientLabelPanel.getVerticalScrollBar();
    };
    
    public JScrollBar getHorizontalScrollBar() {
        return gdmGradientLabelPanel.getHorizontalScrollBar();
    };
    
    /**
     * Sets left margin
     */
    public void setLeftInset(int leftMargin){
        this.insets.left = leftMargin;
        this.setLeftInset(leftMargin);
        this.gdmGradientLabelPanel.setLeftInset(leftMargin);
    }
    
    /**
     * Sets top margin
     */
    public void setTopInset(int topMargin){
        insets.top = topMargin;
        this.gdmGradientLabelPanel.setTopInset(topMargin);
    }
    
    /**
     * Sets the component data.
     */
    public void setData(IData data) {
        this.expData = data;
        this.gdmGradientLabelPanel.setData(data);
    }
    
    
    /**
     * Sets the anti-aliasing attribute.
     */
    public void setAntiAliasing(boolean isAntiAliasing) {
        this.isAntiAliasing = isAntiAliasing;
        this.gdmGradientLabelPanel.setAntiAliasing(isAntiAliasing);
    }
    
    /**
     * Sets the element width attribute.
     */
    void setElementWidth(int width) {
        this.elementWidth = width;
        setFontSize(width);
        this.gdmGradientLabelPanel.setElementWidth(width);
    }
    
    /**
     * Sets the element height attribute.
     */
    void setElementHeight(int height) {
        this.elementHeight = height;
        setFontSize(height);
        this.gdmGradientLabelPanel.setElementHeight(height);
    }
    
    int getElementWidth() {
        return this.elementWidth;
    }
    
    /**
     * Sets the max header width attribute.
     */
    void setAnnotationSize(int size) {
        this.annotationSize = size;
        this.gdmGradientLabelPanel.setAnnotationSize(size);
    }
    
    /**
     * Sets the content width attribute.
     */
    void setContentWidth(int width) {
        this.contentWidth = width;
        this.gdmGradientLabelPanel.setContentWidth(width);
    }
    
    /**
     * Sets the content height attribute.
     */
    void setContentHeight(int height) {
        this.contentHeight = height;
        this.gdmGradientLabelPanel.setContentHeight(height);
    }
    
    /**
     * Sets the isTracing attribute.
     */
    void setTracing(boolean isTracing) {
        this.isTracing = isTracing;
        this.gdmGradientLabelPanel.setTracing(isTracing);
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
    
    public void setValues(float minValue, float maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.gdmGradientLabelPanel.setValues(minValue, maxValue);
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
    
    /**
     * signals the matrix to sort on proximity about the provided index
     */
    private void fireGDMSortEvent(int index){
        ActionEvent ae = new ActionEvent(this, index, GDMGeneViewer.SORT_BY_GENE_PROXIMITY_CMD);
        if(matrixListener != null){
            matrixListener.actionPerformed(ae);
        }
    }
    
    
    private class GDMColorBarPanel extends JPanel {
 
        public GDMColorBarPanel() {
            setBackground(Color.white);
            setSize(0,0);
            setPreferredSize(new Dimension(0,0));
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
         * GDMColorBarPanel: updateSize
         */
        public void updateSize() {
            int w, h;
            
/*
            if (isColumnHeader == true) {
                w = headerWidth;
                h = colorBarHeight;
            } else {
                h = headerHeight;
                w = colorBarHeight;
            }
 
            this.setSize(w, h);
            this.setPreferredSize(new Dimension(w, h));
 */
            this.setSize(colorBarWidth, colorBarHeight);
            this.setPreferredSize(new Dimension(colorBarWidth, colorBarHeight));
        }
        
        /**
         * GDMColorBarPanel: paint
         */
        public void paint(Graphics g1D) {
            super.paint(g1D);
            if(!GDMGeneHeader.this.hasColorBar)
                return;
            if (expData == null) {
                return;
            }
            Graphics2D g = (Graphics2D)g1D;
            if (isAntiAliasing) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
            
            if (isColumnHeader == true) {
                drawColumnColorBar(g);
            } else {
                drawRowColorBar(g);
            }
        }
        
        
        private void drawRowColorBar(Graphics2D g) {
            Rectangle bounds = g.getClipBounds();
            final int top = getTopIndex(bounds.y);
            int bottom =0;
            
            bottom = getBottomIndex(bounds.y+bounds.height, num_genes);
            
            for (int row=top; row<bottom; row++) {
                fillRowColorRectAt(g, OFFSET/2, row);
            }
        }
        
        /**
         * fills cluster colors
         */
        private void fillRowColorRectAt(Graphics g, int xLoc, int row) {
            
            Color geneColor = null;
            
            geneColor = expData.getProbeColor(experiment.getGeneIndexMappedToData(indices[row]));
            
            if(geneColor == null)
                geneColor = Color.white;

            g.setColor(geneColor);
            g.fillRect(xLoc, row*elementHeight, colorBarWidth-OFFSET, elementHeight);
        }
        
        private void drawColumnColorBar(Graphics2D g) {
            Rectangle bounds = g.getClipBounds();
            final int left = getLeftIndex(bounds.x);
            int right = 0;
            
            right = getRightIndex(bounds.x+bounds.width, num_genes);
            
            for (int column = left; column < right; column++) {
                fillColumnColorRectAt(g, column, OFFSET/2 );
            }
        }
        
        /**
         * fills cluster colors
         */
        private void fillColumnColorRectAt(Graphics g, int column, int yLoc) {
            
            Color geneColor = null;
            
            geneColor = expData.getProbeColor(experiment.getGeneIndexMappedToData(indices[column]));
            
            if(geneColor == null)
                geneColor = Color.white;
            
            g.setColor(geneColor);
            g.fillRect(column*elementWidth, yLoc, elementWidth, colorBarHeight-OFFSET);
        }
        
        
        /**
         * Returns index of top row.
         */
        private int getTopIndex(int top) {
            if (top<0) {
                return 0;
            }
            return top/elementHeight;
        }
        
        /**
         * Returns index of left column.
         */
        private int getLeftIndex(int left) {
            if (left<0) {
                return 0;
            }
            return left/(elementWidth+getSpacing());
        }
        
        /**
         * Returns index of right column.
         */
        private int getRightIndex(int right, int limit) {
            if (right<0) {
                return 0;
            }
            int result = right/(elementWidth+getSpacing())+1;
            return result > limit ? limit : result;
        }
        
        /**
         * Returns index of bottom row.
         */
        private int getBottomIndex(int bottom, int limit) {
            if (bottom<0) {
                return 0;
            }
            int result = bottom/elementHeight+1;
            return result > limit ? limit : result;
        }
    }
    
}
