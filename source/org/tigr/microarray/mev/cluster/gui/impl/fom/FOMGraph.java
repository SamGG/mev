/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: FOMGraph.java,v $
 * $Revision: 1.9 $
 * $Date: 2006-03-24 15:50:09 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.fom;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

public class FOMGraph extends JPanel implements java.io.Serializable {
    public static final long serialVersionUID = 202003060001L;
    
    private float[] values;
    private float [][] iValues;
    private float[] variances;
    private String[] xItems;
    private String[] yItems;
    private String title, xLabel, yLabel;
    private Insets insets;
    private boolean isAntiAliasing = false;
    
    private int maxXItem, maxYItem;
    private float maxYValue = 1f;
    private Color pointColor = Color.red;
    private Color valuesLineColor = Color.blue;
    private Color iterationLineColor = Color.lightGray;
    private Color sdLineColor = Color.darkGray;
    private Color mouseLineColor = Color.magenta;
    private Color gridLineColor = Color.yellow;
    private Color axisLineColor = Color.black;
    private int pointSize = 5;
    
    private boolean haveIValues;
    private boolean showVariance;
    private boolean showIValues;
    
    private JPopupMenu menu;
    private JCheckBoxMenuItem menuItem;
    
    private MouseHandler mouseHandler;
    
    public FOMGraph(float[] values, float [] variances, String title, String xLabel, String yLabel, boolean showVariance) {
        if (values == null) {
            throw new IllegalArgumentException("values == null");
        }
        setBackground(Color.white);
        this.insets = new Insets(60, 60, 60, 60);
        this.showVariance = showVariance;
        this.values = values;
        this.variances = variances;
        this.title = title;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        this.mouseHandler = new MouseHandler();
        addMouseMotionListener(mouseHandler);
        addMouseListener(mouseHandler);
    }
    public FOMGraph(){
        setBackground(Color.white);
        this.mouseHandler = new MouseHandler();
        addMouseMotionListener(mouseHandler);
        addMouseListener(mouseHandler);
    }
    
    public void setItems(String[] xItems, String[] yItems) {
        this.xItems = xItems;
        this.yItems = yItems;
        this.maxXItem = getMaxWidth(xItems);
        this.maxYItem = getMaxWidth(yItems);
    }
    
    public void setFOMIterationValues(float [][] values) {
        iValues = values;
        if(iValues != null) {
            haveIValues = true;
            showIValues = false;
            createJPopupMenu();
        }
    }
    
    public void createJPopupMenu() {
        MouseHandler mh = new MouseHandler();
        menuItem = new JCheckBoxMenuItem("Show Iteration Values", false);
        menuItem.addActionListener(mh);
        menuItem.setFocusPainted(false);
        menu = new JPopupMenu();
        menu.add(menuItem);
    }
    
    public void setMaxYValue(float value) {
        this.maxYValue = value;
    }
    
    public BufferedImage getImage() {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_3BYTE_BGR);  //need to use this type for image creation
        Graphics2D g = image.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        paint(g);
        return image;
    }
    
    /**
     * Sets the anti-aliasing attribute.
     */
    public void setAntiAliasing(boolean value) {
        this.isAntiAliasing = value;
    }
    
    public void paint(Graphics g1D) {
        super.paint(g1D);
        Graphics2D g = (Graphics2D)g1D;
        FontMetrics metrics = g.getFontMetrics();
        int descent = metrics.getDescent();
        if (this.isAntiAliasing) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        
        Dimension size = getSize();
        final int left = this.insets.left+this.maxYItem;
        final int top = this.insets.top;
        final int width = size.width - left - this.insets.right;
        final int height = size.height - this.insets.bottom - top - this.maxXItem;
        final float xScale = (float)width/(float)(this.values.length-1);
        final float yScale = (float)height/this.maxYValue;
        
        int strWidth;
        // draw title
        strWidth = metrics.stringWidth(this.title);
        g.drawString(this.title, left+(width-strWidth)/2, (this.insets.top+descent)/2);
        // draw xLabel
        strWidth = metrics.stringWidth(this.xLabel);
        g.drawString(this.xLabel, left+(width-strWidth)/2, this.insets.top+height+this.maxXItem+(this.insets.top+descent)/2);
        // draw yLabel
        strWidth = metrics.stringWidth(this.yLabel);
        g.rotate(-Math.PI/2.0);
        g.drawString(this.yLabel, -(this.insets.top+(height+strWidth)/2), (this.insets.left+descent)/2);
        g.rotate(Math.PI/2.0);
        // draw grid
        g.setColor(this.gridLineColor);
        if (this.yItems != null) {
            final float yItemsStep = height/(float)(this.yItems.length-1);
            for (int i=0; i<this.yItems.length; i++) {
                if (this.yItems[this.yItems.length-i-1] != null) {
                    g.drawLine(left, top+(int)Math.round(i*yItemsStep), left+width, top+(int)Math.round(i*yItemsStep));
                }
            }
        }
        if (this.xItems != null) {
            final float xItemsStep = width/(float)(this.xItems.length-1);
            for (int i=0; i<this.xItems.length; i++) {
                if (this.xItems[i] != null) {
                    g.drawLine(left+(int)Math.round(i*xItemsStep), top, left+(int)Math.round(i*xItemsStep), top+height);
                }
            }
        }
        // draw vertical line
        g.setColor(this.axisLineColor);
        g.drawLine(left, top, left, top+height);
        // draw y items
        if (this.yItems != null) {
            final float yItemsStep = height/(float)(this.yItems.length-1);
            for (int i=0; i<this.yItems.length; i++) {
                g.drawLine(left-5, top+(int)Math.round(i*yItemsStep), left+5, top+(int)Math.round(i*yItemsStep));
                if (this.yItems[this.yItems.length-i-1] != null) {
                    g.drawString(this.yItems[this.yItems.length-i-1], left-7-metrics.stringWidth(this.yItems[this.yItems.length-i-1]), top+(int)Math.round(i*yItemsStep)+descent);
                }
            }
        }
        // draw x items
        if (this.xItems != null) {
            final float xItemsStep = width/(float)(this.xItems.length-1);
            for (int i=0; i<this.xItems.length; i++) {
                g.drawLine(left+(int)Math.round(i*xItemsStep), top+height-5, left+(int)Math.round(i*xItemsStep), top+height+5);
            }
            g.rotate(-Math.PI/2.0);
            final int bottom = top+height+7;
            for (int i=0; i<this.xItems.length; i++) {
                if (this.xItems[i] != null) {
                    g.drawString(this.xItems[i], -(bottom+metrics.stringWidth(this.xItems[i])), left+(int)Math.round(i*xItemsStep)+descent);
                }
            }
            g.rotate(Math.PI/2.0);
        }
        // draw horizontal line
        g.drawLine(left, top+height, left+width, top+height);
        
        // draw value lines
        g.setColor(this.valuesLineColor);
        int x1, y1, x2, y2, sdY, sdX;
        
        //show the iteration fom values
        if(showIValues) {            
            g.setColor(this.iterationLineColor);
            for (int i=0; i<this.iValues.length; i++) {
                for(int j = 0; j < iValues[i].length-1; j++) {                    
                    x1 = left+Math.round(j*xScale);
                    y1 = top+height-Math.round(this.iValues[i][j]*yScale);
                    x2 = left+Math.round((j+1)*xScale);
                    y2 = top+height-Math.round(this.iValues[i][j+1]*yScale);
                    g.drawLine(x1, y1, x2, y2);
                }
            }
        }
        
        for (int i=0; i<this.values.length-1; i++) {
            g.setColor(this.valuesLineColor);
            x1 = left+Math.round(i*xScale);
            y1 = top+height-Math.round(this.values[i]*yScale);
            x2 = left+Math.round((i+1)*xScale);
            y2 = top+height-Math.round(this.values[i+1]*yScale);
            g.drawLine(x1, y1, x2, y2);
            g.drawLine(x1, y1-1, x2, y2-1);
            
            if(showVariance) {
                g.setColor(this.sdLineColor);
                sdX = x2;
                sdY = y2;
                
                //get coordinates
                x1 = left+Math.round((i+1)*xScale) - 3;
                x2 = x2 + 3;
                y1 = top+height-Math.round((this.values[i+1]+this.variances[i+1])*yScale);
                y2 = top+height-Math.round((this.values[i+1]-this.variances[i+1])*yScale);
                //draw caps
                g.drawLine(x1, y1, x2, y1);
                g.drawLine(x1, y2, x2, y2);
                //draw sd lines
                g.drawLine(sdX, sdY, sdX, y1);
                g.drawLine(sdX, sdY, sdX, y2);
            }
            
        }
        // draw value points
        g.setColor(this.pointColor);
        for (int i=0; i<this.values.length; i++) {
            g.fillOval(left+Math.round(i*xScale)-this.pointSize/2, top+height-Math.round(this.values[i]*yScale)-this.pointSize/2, this.pointSize, this.pointSize);
        }
        this.mouseHandler.validate();
    }
    
    private int getMaxWidth(String[] items) {
        if (items == null) {
            return 0;
        }
        FontMetrics metrics = getFontMetrics(getFont());
        int width = 0;
        for (int i=0; i<items.length; i++) {
            if (items[i] != null) {
                width = Math.max(width, metrics.stringWidth(items[i]));
            }
        }
        return width;
    }
    
    private void drawMouseCross(int x, int y) {
        Graphics2D g = (Graphics2D)getGraphics();
        g.setColor(this.mouseLineColor);
        g.setXORMode(getBackground());
        Dimension size = getSize();
        final int left = insets.left + this.maxYItem;
        final int right = size.width - this.insets.right;
        final int top = insets.top;
        final int bottom = size.height - this.insets.bottom - this.maxXItem;
        g.drawLine(x, top, x, bottom);
        g.drawLine(left, y, right, y);
        g.setPaintMode();
        g.dispose();
    }
    
    private class MouseHandler extends MouseMotionAdapter implements ActionListener, MouseListener, java.io.Serializable {
        
        private Point prevCoords = new Point(-1, -1);
        
        public void validate() {
            if (isCoordsValid(prevCoords.x, prevCoords.y)) {
                drawMouseCross(prevCoords.x, prevCoords.y);
            }
            prevCoords.setLocation(-1, -1);
        }
        
        public void mouseMoved(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            if (isCoordsValid(prevCoords.x, prevCoords.y)) {
                drawMouseCross(prevCoords.x, prevCoords.y);
            }
            if (isCoordsValid(x, y)) {
                drawMouseCross(x, y);
            }
            prevCoords.setLocation(x, y);
        }
        
        private boolean isCoordsValid(int x, int y) {
            Dimension size = getSize();
            final int left = insets.left+maxYItem;
            final int top = insets.top;
            final int right = size.width - insets.right;
            final int bottom = size.height - insets.bottom - maxXItem;
            return(left < x && x < right) && (top < y && y < bottom);
        }
        
        public void mousePressed(MouseEvent e) {
            if(menu != null && e.isPopupTrigger())
                menu.show(FOMGraph.this, e.getX(), e.getY());
        }
         
        public void mouseReleased(MouseEvent e) {
            if(menu != null && e.isPopupTrigger())
                menu.show(FOMGraph.this, e.getX(), e.getY());
        }
        
        public void actionPerformed(ActionEvent ae) {
            if(menuItem.isSelected()) {
                showIValues = true;
                repaint();
            } else {
                showIValues = false;
                repaint();
            }
        }
                   
        public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        }
        
        public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
        }
        
        public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
        }
        
    }
    
	/**
	 * @return Returns the haveIValues.
	 */
	public boolean isHaveIValues() {
		return haveIValues;
	}
	/**
	 * @param haveIValues The haveIValues to set.
	 */
	public void setHaveIValues(boolean haveIValues) {
		this.haveIValues = haveIValues;
	}
	/**
	 * @return Returns the insets.
	 */
	public Insets getInsets() {
		return insets;
	}
	/**
	 * @param insets The insets to set.
	 */
	public void setInsets(Insets insets) {
		this.insets = insets;
	}
	/**
	 * @return Returns the iValues.
	 */
	public float[][] getIValues() {
		return iValues;
	}
	/**
	 * @param values The iValues to set.
	 */
	public void setIValues(float[][] values) {
		iValues = values;
	}
	/**
	 * @return Returns the maxXItem.
	 */
	public int getMaxXItem() {
		return maxXItem;
	}
	/**
	 * @param maxXItem The maxXItem to set.
	 */
	public void setMaxXItem(int maxXItem) {
		this.maxXItem = maxXItem;
	}
	/**
	 * @return Returns the maxYItem.
	 */
	public int getMaxYItem() {
		return maxYItem;
	}
	/**
	 * @param maxYItem The maxYItem to set.
	 */
	public void setMaxYItem(int maxYItem) {
		this.maxYItem = maxYItem;
	}
	/**
	 * @return Returns the showIValues.
	 */
	public boolean isShowIValues() {
		return showIValues;
	}
	/**
	 * @param showIValues The showIValues to set.
	 */
	public void setShowIValues(boolean showIValues) {
		this.showIValues = showIValues;
	}
	/**
	 * @return Returns the showVariance.
	 */
	public boolean isShowVariance() {
		return showVariance;
	}
	/**
	 * @param showVariance The showVariance to set.
	 */
	public void setShowVariance(boolean showVariance) {
		this.showVariance = showVariance;
	}
	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return Returns the values.
	 */
	public float[] getValues() {
		return values;
	}
	/**
	 * @param values The values to set.
	 */
	public void setValues(float[] values) {
		this.values = values;
	}
	/**
	 * @return Returns the variances.
	 */
	public float[] getVariances() {
		return variances;
	}
	/**
	 * @param variances The variances to set.
	 */
	public void setVariances(float[] variances) {
		this.variances = variances;
	}
	/**
	 * @return Returns the xItems.
	 */
	public String[] getXItems() {
		return xItems;
	}
	/**
	 * @param items The xItems to set.
	 */
	public void setXItems(String[] items) {
		xItems = items;
	}
	/**
	 * @return Returns the xLabel.
	 */
	public String getXLabel() {
		return xLabel;
	}
	/**
	 * @param label The xLabel to set.
	 */
	public void setXLabel(String label) {
		xLabel = label;
	}
	/**
	 * @return Returns the yItems.
	 */
	public String[] getYItems() {
		return yItems;
	}
	/**
	 * @param items The yItems to set.
	 */
	public void setYItems(String[] items) {
		yItems = items;
	}
	/**
	 * @return Returns the yLabel.
	 */
	public String getYLabel() {
		return yLabel;
	}
	/**
	 * @param label The yLabel to set.
	 */
	public void setYLabel(String label) {
		yLabel = label;
	}
	/**
	 * @return Returns the isAntiAliasing.
	 */
	public boolean isAntiAliasing() {
		return isAntiAliasing;
	}
	/**
	 * @return Returns the maxYValue.
	 */
	public float getMaxYValue() {
		return maxYValue;
	}
}
