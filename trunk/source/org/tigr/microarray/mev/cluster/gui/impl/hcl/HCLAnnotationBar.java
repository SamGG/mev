/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: HCLAnnotationBar.java,v $
 * $Revision: 1.5 $
 * $Date: 2006-03-24 15:50:40 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;

public class HCLAnnotationBar extends JPanel {
    
    private IData data;
    private int[] rowsOrder;
    private boolean isAntiAliasing = true;
    private int elementHeight = 5;
    private int maxUniqueIDWidth, maxGeneNameWidth;
    
    /**
     * Constructs a <code>HCLAnnotationBar</code>.
     */
    public HCLAnnotationBar(int[] rowsOrder) {
        this.rowsOrder = rowsOrder;
        setBackground(Color.white);
        setSizes(10, 10);
    }
        
        /**
         * Paints the bar into specified graphics.
         */
        public void paint(Graphics g) {
            super.paint(g);
            if (this.isAntiAliasing) {
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
            
            Rectangle bounds = g.getClipBounds();
            final int top = getTopIndex(bounds.y);
            final int bottom = getBottomIndex(bounds.y+bounds.height, this.rowsOrder.length);
            
            g.setColor(Color.black);
            int uniqX = 10;
            int nameX = uniqX+10+this.maxUniqueIDWidth;
            int annY;
            for (int row=top; row<bottom; row++) {
                annY = (row+1)*this.elementHeight;
                g.drawString(data.getUniqueId(this.rowsOrder[row]), uniqX, annY);
                g.drawString(data.getGeneName(this.rowsOrder[row]), nameX, annY);
            }
        }
        
        private int getTopIndex(int top) {
            if (top < 0) {
                return 0;
            }
            return top/this.elementHeight;
        }
        
        private int getBottomIndex(int bottom, int limit) {
            if (bottom < 0) {
                return 0;
            }
            int result = bottom/this.elementHeight+1;
            return result > limit ? limit : result;
        }
        
        /**
         * Updates its attributies when the viewer was selected.
         */
        public void onSelected(IFramework framework) {
            this.data = framework.getData();
            IDisplayMenu menu = framework.getDisplayMenu();
            this.isAntiAliasing = menu.isAntiAliasing();
            setElementHeight(menu.getElementSize().height);
            updateSize();
        }
        
        /**
         * Updates its attributies when the framework display menu was changed.
         */
        public void onMenuChanged(IDisplayMenu menu) {
            if (this.elementHeight == menu.getElementSize().height &&
            this.isAntiAliasing == menu.isAntiAliasing()) {
                return;
            }
            this.isAntiAliasing = menu.isAntiAliasing();
            setElementHeight(menu.getElementSize().height);
            updateSize();
        }
        
        /**
         * Sets a new element height.
         */
        private void setElementHeight(int height) {
            this.elementHeight = height;
            setFont(new Font("monospaced", Font.PLAIN, height));
        }
        
        /**
         * Updates the bar sizes.
         */
        private void updateSize() {
            Graphics2D g = (Graphics2D)getGraphics();
            this.maxGeneNameWidth = getMaxWidth(g, true);
            this.maxUniqueIDWidth = getMaxWidth(g, false);
            int width = 20+this.maxGeneNameWidth+this.maxUniqueIDWidth;
            int height = this.elementHeight*this.rowsOrder.length+1;
            setSizes(width, height);
        }
        
        /**
         * Calculates max annotation width.
         */
        private int getMaxWidth(Graphics2D g, boolean genename) {
            if (g == null || this.data == null) {
                return 0;
            }
            if (this.isAntiAliasing) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
            FontMetrics fm = g.getFontMetrics();
            int max = 0;
            String str;
            final int size = this.data.getFeaturesSize();
            for (int i=0; i<size; i++) {
                str = genename ? this.data.getGeneName(i) : this.data.getUniqueId(i);
                max = Math.max(max, fm.stringWidth(str));
            }
            return max;
        }
        
        /**
         * Sets the component sizes.
         */
        private void setSizes(int width, int height) {
            setSize(width, height);
            setPreferredSize(new Dimension(width, height));
        }
    }
