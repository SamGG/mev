/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: TTestVolcanoPlotViewer.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.ttest;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.tigr.graph.*;
import org.tigr.util.*;
import org.tigr.util.awt.*;

import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;

/**
 *
 * @author  nbhagaba
 * @version 
 */
public class TTestVolcanoPlotViewer extends JPanel implements IViewer/*, MouseMotionListener */{
    private double[] yArray, xArray;
    private boolean[] isSig;
    int originX, originY;  
    int currentMouseX, currentMouseY;
    private boolean useRefLines;
    private JPopupMenu popup;  
    private JCheckBoxMenuItem useRefLinesBox;

    /** Creates new TTestVolcanoPlotViewer */
    public TTestVolcanoPlotViewer(double[] xArray, double[] yArray, boolean[] isSig) {
        this.xArray = xArray;
        this.yArray = yArray;
        this.isSig = isSig;
        useRefLines = true;
        currentMouseX = 0;
        currentMouseY = 0;
        this.setBorder(new EtchedBorder());
        this.setBackground(Color.white);
        
        this.addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(java.awt.event.MouseEvent mouseEvent) {
                if (useRefLines) {
                    currentMouseX = mouseEvent.getX();
                    currentMouseY = mouseEvent.getY();
                    //System.out.println("X = " + currentMouseX + ", Y = " + currentMouseY);
                    TTestVolcanoPlotViewer.this.repaint();
                }
            }
            public void mouseDragged(java.awt.event.MouseEvent mouseEvent) {
            }            
        });  
        
        popup = new JPopupMenu();
        useRefLinesBox = new JCheckBoxMenuItem("Use reference lines", true);

        useRefLinesBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    useRefLines = true;
                } else {
                    useRefLines = false;
                    TTestVolcanoPlotViewer.this.repaint();
                }
            }
        });
        popup.add(useRefLinesBox);
        
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }
            
            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(),
                    e.getX(), e.getY());
                }
            }            
        });        

    }
    
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2D = (Graphics2D)g;
        int panelWidth = this.getWidth();
        int panelHeight = this.getHeight();
        g2D.setStroke(new BasicStroke(2.0f));
        //g2D.setColor(Color.lightGray.darker());
        //g2D.drawLine(10, 10, this.getWidth() - 10, 10);
        //g2D.drawLine(10, this.getHeight() - 10, this.getWidth() - 10, this.getHeight() - 10);
        g2D.drawLine(40, 10, this.getWidth() - 10, 10);
        g2D.drawLine(this.getWidth() - 10, 10, this.getWidth() - 10, this.getHeight() - 50);
        originX = (int)Math.round((double)(this.getWidth()/2));
        originY = (int)Math.round((double)(this.getHeight() - 50));  
        
        //draw axes
        g2D.setColor(Color.black);
        g2D.setStroke(new BasicStroke(2.0f));
        g2D.drawLine(40, (int)Math.round((double)(this.getHeight() - 50)), this.getWidth() - 10, (int)Math.round((double)(this.getHeight() - 50)));
        g2D.drawLine((int)Math.round((double)(this.getWidth()/2)), 10, (int)Math.round((double)(this.getWidth()/2)), this.getHeight() - 50);
        //g2D.setStroke(new BasicStroke(1.0f));
        g2D.drawLine(40, 10, 40, this.getHeight() - 50);  
        
        g2D.setStroke(new BasicStroke(1.0f));
        if ((useRefLines) && (currentMouseX >= 40) && (currentMouseX <= this.getWidth() - 10) && (currentMouseY >= 10) && (currentMouseY <= (this.getHeight() - 50))) {
            g2D.setColor(Color.magenta);
            g2D.drawLine(40, currentMouseY, this.getWidth() - 10, currentMouseY);
            g2D.drawLine(currentMouseX, 10, currentMouseX, this.getHeight() - 50);
        }
        g2D.setColor(Color.black);
        g2D.rotate(-Math.PI/2);
        //g2D.drawString("-log10(p)", 20, this.getHeight()/2 + 10);
        //g2D.drawString("-log10(p)", -1 * this.getWidth()/2, this.getHeight()/2);
        g2D.drawString("-log10(p)", -1*this.getHeight()/2, 25);
        g2D.rotate(Math.PI/2);
        
        double[] maxXAndY = getMaxXAndY();
        double[] minXAndY = getMinXAndY();
        
        double origMaxXValue = maxXAndY[0];
        double origMaxYValue = maxXAndY[1];
        double origMinXValue = minXAndY[0];
        double origMinYValue = minXAndY[1];       
        /*
        double origMaxXValue = getMax(xArray);
        double origMaxYValue = getMax(yArray);
        double origMinXValue = getMin(xArray);
        double origMinYValue = getMin(yArray);
         */

        double xScalingFactor = getXScalingFactor(origMaxXValue, origMinXValue); // relative to originX and originY        
        double[] xIntervalArray = new double[6];
        double xIncrement = 0.0d;
        
        if (Math.abs(origMaxXValue) > Math.abs(origMinXValue)) {
            xIncrement = Math.abs((double)(origMaxXValue/5.0d));
        } else {
            xIncrement = Math.abs((double)(origMinXValue/5.0d));
        }  
        double xCounter = 0.0d;
        
        for (int i = 0; i < xIntervalArray.length; i++) {
            xIntervalArray[i] = xCounter;
            xCounter = xCounter + xIncrement;
            //yIntervalArray[i] = yCounter;
            //yCounter = yCounter + yIncrement;
        }  
        
        //draw x tick marks
        g2D.setStroke(new BasicStroke(2.0f));
        for (int i = 1; i < xIntervalArray.length; i++) {
            g2D.drawLine((int)Math.round(xIntervalArray[i]*xScalingFactor) +this.getWidth()/2, this.getHeight() - 50 - 5, (int)Math.round(xIntervalArray[i]*xScalingFactor) +this.getWidth()/2, this.getHeight() - 50 + 5);
        }
        
        for (int i = 1; i < xIntervalArray.length; i++) {
            g2D.drawLine(this.getWidth()/2 - (int)Math.round(xIntervalArray[i]*xScalingFactor), this.getHeight() - 50 - 5, this.getWidth()/2 - (int)Math.round(xIntervalArray[i]*xScalingFactor), this.getHeight() - 50 + 5);
        }  
        
        DecimalFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits(2);
        
        //tick labels
        for (int i = 1; i < xIntervalArray.length; i++) {
            g2D.drawString(nf.format((double)xIntervalArray[i]), (int)Math.round(xIntervalArray[i]*xScalingFactor) +this.getWidth()/2 - 10, this.getHeight() - 30);
        }
        
        for (int i = 1; i < xIntervalArray.length; i++) {
            g2D.drawString("-" + nf.format((double)xIntervalArray[i]), this.getWidth()/2 - (int)Math.round(xIntervalArray[i]*xScalingFactor) - 10, this.getHeight() - 30);
        }        
        
        g2D.drawString("Mean(GroupB) - Mean(GroupA)", this.getWidth()/2 - 85, this.getHeight() - 15);
        
        int maxYInt = (int)(Math.round(origMaxYValue));
        
        int currY = 1;
        double yScalingFactor = getYScalingFactor(origMaxYValue, origMinYValue); // relative to originX and originY  
        //draw horizontal lines
        g2D.setColor(Color.gray);
        g2D.setStroke(new BasicStroke(1.0f));
        
        while ((currY <= maxYInt) && ((this.getHeight() - 50 - (int)Math.round(currY*yScalingFactor)) >= 10)) {
            g2D.drawLine(40, this.getHeight() - 50 - (int)Math.round(currY*yScalingFactor), this.getWidth() - 10, this.getHeight() - 50 - (int)Math.round(currY*yScalingFactor));
            currY++;
        } 
        g2D.setColor(Color.black);
        for (int i = 1; i <= maxYInt; i++) {
            if ((this.getHeight() - 50 - (int)Math.round(i*yScalingFactor)) >= 10) {
                g2D.drawString(nf.format(i), 30, this.getHeight() - 50 - (int)Math.round(i*yScalingFactor));
            }
        }   
        
        //draw data points
        for (int i = 0; i < xArray.length; i++) {
            if ((!Double.isNaN(xArray[i])) && (!Double.isNaN(yArray[i])) && (!Double.isInfinite(xArray[i])) && (!Double.isInfinite(yArray[i]))) {
                if (isSig[i]) {
                    g2D.setColor(Color.red);
                } else {
                    g2D.setColor(Color.black);
                }
                drawPoint(g2D, xArray[i], yArray[i], getXScalingFactor(origMaxXValue, origMinXValue), getYScalingFactor(origMaxYValue, origMinYValue), 5);
            }
        }
        
        g2D.setColor(Color.black);
        
    }
    
    private void drawPoint(Graphics2D g2D, double xValue, double yValue, double xScale, double yScale, int diameter) {
        int xRaw = (int)Math.round(xValue*xScale);
        int yRaw = (int)Math.round(yValue*yScale);
        //System.out.println("xValue = " + xValue + " , yValue = " + yValue + ", xRaw = " + xRaw + ", yRaw  = " + yRaw);
        
        int xCoord = 0;
        int yCoord = 0;
        
        xCoord = (int)Math.round((double)(this.getWidth()/2)) + xRaw;
        yCoord = (int)Math.round((double)(this.getHeight() - 50)) - yRaw;
       
        g2D.fillOval(xCoord, yCoord, diameter, diameter);
        
    }    
   
    /*
    private double getMax(double[] array) {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < array.length; i++) {
            if (!Double.isInfinite(array[i])) {
                if (max < array[i]) {
                    max = array[i];
                }
            }
        }
        return max;
    }
    
    private double getMin(double[] array) {
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < array.length; i++) {
            if (!Double.isInfinite(array[i])) {
                if (min > array[i]) {
                    min = array[i];
                }
            }
        }
        return min;
    } 
     */ 
    
    private double[] getMaxXAndY() { // used to scale the graph using only points that have valid values for both x and y
        double[] maxXAndY = new double[2];
        
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < xArray.length; i++) {
            if ((!Double.isInfinite(xArray[i])) && (!Double.isNaN(xArray[i])) && (!Double.isInfinite(yArray[i])) && (!Double.isNaN(yArray[i]))) {
                if (maxX < xArray[i]) {
                    maxX = xArray[i];
                }
                if (maxY < yArray[i]) {
                    maxY = yArray[i];
                }
            }
        }        
        
        maxXAndY[0] = maxX;
        maxXAndY[1] = maxY;
        
        return maxXAndY;
    }
    
    private double[] getMinXAndY() { // used to scale the graph using only points that have valid values for both x and y
        double[] minXAndY = new double[2];
        
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        for (int i = 0; i < xArray.length; i++) {
            if ((!Double.isInfinite(xArray[i])) && (!Double.isNaN(xArray[i])) && (!Double.isInfinite(yArray[i])) && (!Double.isNaN(yArray[i]))) {
                if (minX > xArray[i]) {
                    minX = xArray[i];
                }
                if (minY > yArray[i]) {
                    minY = yArray[i];
                }
            }
        }        
        
        minXAndY[0] = minX;
        minXAndY[1] = minY;
        
        return minXAndY;        
    }
    
    
    private double getXScalingFactor(double maxValue, double minValue) {
        double largest = 1;
        if ((maxValue > 0)&&(minValue > 0)) {
            largest = maxValue;
        } else if ((maxValue > 0)&&(minValue < 0)) {
            if (maxValue > Math.abs(minValue)) {
                largest = maxValue;
            } else {
                largest = Math.abs(minValue);
            }
        } else if (maxValue <= 0) {
            largest = Math.abs(minValue);
        } else if (minValue == 0) {
            largest = maxValue;
        }
        
        //double scalingFactor =0;

        double scalingFactor = (this.getWidth()/2 - 50)/largest;
        
        
        return scalingFactor;
    }    
    
    private double getYScalingFactor(double maxValue, double minValue) {
        double largest = 1;
        if ((maxValue > 0)&&(minValue > 0)) {
            largest = maxValue;
        } else if ((maxValue > 0)&&(minValue < 0)) {
            if (maxValue > Math.abs(minValue)) {
                largest = maxValue;
            } else {
                largest = Math.abs(minValue);
            }
        } else if (maxValue <= 0) {
            largest = Math.abs(minValue);
        } else if (minValue == 0) {
            largest = maxValue;
        } 
        double scalingFactor = ((this.getHeight() - 50) - 50)/largest;  
        return scalingFactor;
    }
    
    public void showReferenceLines() {
    }

    /**
     * Invoked by the framework when this viewer was deselected.
     */
    public void onDeselected() {
    }
    
    /**
     * Invoked by the framework when data is changed,
     * if this viewer is selected.
     * @see IData
     */
    public void onDataChanged(IData data) {
    }
    
    /**
     * Invoked when the framework is going to be closed.
     */
    public void onClosed() {
    }
    
    /**
     * Returns a component to be inserted into scroll pane view port.
     */
    public JComponent getContentComponent() {
        return this;
    }
    
    /**
     * Invoked by the framework to save or to print viewer image.
     */
    public BufferedImage getImage() {
        return null;
    }
    
    /**
     * Invoked by the framework when this viewer is selected.
     */
    public void onSelected(IFramework framework) {
    }
    
    /**
     * Invoked by the framework when display menu is changed,
     * if this viewer is selected.
     * @see IDisplayMenu
     */
    public void onMenuChanged(IDisplayMenu menu) {
    }
    
    /**
     * Returns a component to be inserted into scroll pane header.
     */
    public JComponent getHeaderComponent() {
        return null;
    }
    
    /*
    public void mouseDragged(java.awt.event.MouseEvent mouseEvent) {
    }
    
    public void mouseMoved(java.awt.event.MouseEvent mouseEvent) {
        currentMouseX = mouseEvent.getX();
        currentMouseY = mouseEvent.getY();
        System.out.println("X = " + currentMouseX + ", Y = " + currentMouseY);
        this.repaint();
    }
     */
    
}
