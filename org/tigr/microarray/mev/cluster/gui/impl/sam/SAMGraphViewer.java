/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * SAMGraphViewer.java
 *
 * Created on January 28, 2003, 9:52 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.sam;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;

/**
 *
 * @author  nbhagaba
 * @version 
 */
public class SAMGraphViewer extends JPanel implements IViewer, java.io.Serializable {
    public static final long serialVersionUID = 202015010001L;

    private double[] yArray, xArray;
    private double delta; 
    int originX, originY;
    int studyDesign;

    /** Creates new SAMGraphViewer */
    public SAMGraphViewer(double[] xArray, double[] yArray, int studyDesign, double delta) {
        this.yArray = yArray;
        this.xArray = xArray;  
        this.delta = delta; 
        this.studyDesign = studyDesign;
        this.setBackground(Color.white);
    }
    
    public void paint(Graphics g) {
        super.paint(g);
        if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) || (studyDesign == SAMInitDialog.ONE_CLASS)) {
            paint1(g);
        } else if (studyDesign == SAMInitDialog.MULTI_CLASS) {
            paint2(g);
        }
        
    }
    
    private void paint1(Graphics g) {
        Graphics2D g2D = (Graphics2D)g;
        int panelWidth = this.getWidth();
        int panelHeight = this.getHeight();
        originX = (int)Math.round((double)(this.getWidth()/2));
        originY = (int)Math.round((double)(this.getHeight()/2));
        double origMaxXValue = getMax(xArray);
        double origMaxYValue = getMax(yArray);
        double origMinXValue = getMin(xArray);
        double origMinYValue = getMin(yArray);
        
        double xScalingFactor = getXScalingFactor(origMaxXValue, origMinXValue); // relative to originX and originY
        double yScalingFactor = getYScalingFactor(origMaxYValue, origMinYValue); // relative to originX and originY
        
        final float dash1[] = {10.0f};
        
        final BasicStroke dashed = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
        
        //System.out.println("xScalingFactor = " + xScalingFactor + ", yScalingFactor = " + yScalingFactor);
        
        //draw axes
        g2D.setStroke(new BasicStroke(2.0f));
        g2D.drawLine(0, (int)Math.round((double)(this.getHeight()/2)), this.getWidth(), (int)Math.round((double)(this.getHeight()/2)));
        g2D.drawLine((int)Math.round((double)(this.getWidth()/2)), 0, (int)Math.round((double)(this.getWidth()/2)), this.getHeight());
        
        double[] xIntervalArray = new double[6];
        double[] yIntervalArray = new double[6];
        
        double xIncrement = 0.0d;
        double yIncrement = 0.0d;
        
        if (Math.abs(origMaxXValue) > Math.abs(origMinXValue)) {
            xIncrement = Math.abs((double)(origMaxXValue/5.0d));
        } else {
            xIncrement = Math.abs((double)(origMinXValue/5.0d));
        }
        
        if (Math.abs(origMaxYValue) > Math.abs(origMinYValue)) {
            yIncrement = Math.abs((double)(origMaxYValue/5.0d));
        } else {
            yIncrement = Math.abs((double)(origMinYValue/5.0d));
        }
        
        double xCounter = 0.0d;
        double yCounter = 0.0d;
        for (int i = 0; i < xIntervalArray.length; i++) {
            xIntervalArray[i] = xCounter;
            xCounter = xCounter + xIncrement;
            yIntervalArray[i] = yCounter;
            yCounter = yCounter + yIncrement;
        }
        
        //draw x tick marks
        
        for (int i = 1; i < xIntervalArray.length; i++) {
            g2D.drawLine((int)Math.round(xIntervalArray[i]*xScalingFactor) +this.getWidth()/2, this.getHeight()/2 - 5, (int)Math.round(xIntervalArray[i]*xScalingFactor) +this.getWidth()/2, this.getHeight()/2 + 5);
        }
        
        for (int i = 1; i < xIntervalArray.length; i++) {
            g2D.drawLine(this.getWidth()/2 - (int)Math.round(xIntervalArray[i]*xScalingFactor), this.getHeight()/2 - 5, this.getWidth()/2 - (int)Math.round(xIntervalArray[i]*xScalingFactor), this.getHeight()/2 + 5);
        }
        
        
        //draw  reference line of slope = 1
        g2D.drawLine(this.getWidth()/2 - (int)Math.round(xIntervalArray[5]*xScalingFactor), this.getHeight()/2 + (int)Math.round(xIntervalArray[5]*yScalingFactor), this.getWidth()/2 + (int)Math.round(xIntervalArray[5]*xScalingFactor), this.getHeight()/2 - (int)Math.round(xIntervalArray[5]*yScalingFactor));
        
        g2D.setColor(Color.gray);
        
        g2D.setStroke(new BasicStroke(1.0f));
        
        //draw horizonal grid lines
        for (int i = 1; i < yIntervalArray.length; i++) {
            g2D.drawLine(0, this.getHeight()/2 + (int)Math.round(yIntervalArray[i]*yScalingFactor), this.getWidth(), this.getHeight()/2 + (int)Math.round(yIntervalArray[i]*yScalingFactor));
        }
        
        for (int i = 1; i < yIntervalArray.length; i++) {
            g2D.drawLine(0, this.getHeight()/2 - (int)Math.round(yIntervalArray[i]*yScalingFactor), this.getWidth(), this.getHeight()/2 - (int)Math.round(yIntervalArray[i]*yScalingFactor));
        }
        
        g2D.setStroke(new BasicStroke(2.0f));
        g2D.setColor(Color.black);
        
        DecimalFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits(2);
        
        //tick labels
        for (int i = 1; i < xIntervalArray.length; i++) {
            g2D.drawString(nf.format((double)xIntervalArray[i]), (int)Math.round(xIntervalArray[i]*xScalingFactor) +this.getWidth()/2 - 10, this.getHeight()/2 + 20);
        }
        
        for (int i = 1; i < xIntervalArray.length; i++) {
            g2D.drawString("-" + nf.format((double)xIntervalArray[i]), this.getWidth()/2 - (int)Math.round(xIntervalArray[i]*xScalingFactor) - 10, this.getHeight()/2 + 20);
        }
        
        for (int i = 1; i < yIntervalArray.length; i++) {
            g2D.drawString(nf.format((double)yIntervalArray[i]), this.getWidth()/2 - 30, this.getHeight()/2 - (int)Math.round(yIntervalArray[i]*yScalingFactor) - 5);
        }
        
        for (int i = 1; i < yIntervalArray.length; i++) {
            g2D.drawString("-" + nf.format((double)yIntervalArray[i]), this.getWidth()/2 - 30, this.getHeight()/2 + (int)Math.round(yIntervalArray[i]*yScalingFactor) - 5);
        }
        
        int deltaOffset = (int)Math.round(delta*yScalingFactor);
        
        //draw dashed lines for delta limits
        g2D.setStroke(dashed);
        
        g2D.drawLine(this.getWidth()/2 - (int)Math.round(xIntervalArray[5]*xScalingFactor), this.getHeight()/2 + (int)Math.round(xIntervalArray[5]*yScalingFactor) - deltaOffset, this.getWidth()/2 + (int)Math.round(xIntervalArray[5]*xScalingFactor), this.getHeight()/2 - (int)Math.round(xIntervalArray[5]*yScalingFactor) - deltaOffset);
        g2D.drawLine(this.getWidth()/2 - (int)Math.round(xIntervalArray[5]*xScalingFactor), this.getHeight()/2 + (int)Math.round(xIntervalArray[5]*yScalingFactor) + deltaOffset, this.getWidth()/2 + (int)Math.round(xIntervalArray[5]*xScalingFactor), this.getHeight()/2 - (int)Math.round(xIntervalArray[5]*yScalingFactor) + deltaOffset);
        
        g2D.setStroke(new BasicStroke(2.0f));
        
        boolean posSigEncountered = false;
        boolean negSigEncountered = false;
        
        int lowestPosSigIndex = 0;
        int highestNegSigIndex = 0;
        
        for (int i = 0; i < xArray.length; i++) {
            if ( (xArray[i] > 0.0d) && ((yArray[i] - xArray[i]) > delta) ) {
                lowestPosSigIndex = i;
                posSigEncountered = true;
                break;
            }
        }
        
        for (int i = 0; i < xArray.length; i++) {
            if ( (xArray[i] < 0.0d) && ((xArray[i] - yArray[i]) > delta) ) {
                highestNegSigIndex = i;
                negSigEncountered = true;
            }
        }
        
        Color[] pointColor = new Color[xArray.length];
        
        if ((posSigEncountered)&&(negSigEncountered)) {
            for (int i = 0; i < (highestNegSigIndex + 1); i++) {
                //negSigGenes.add(new Integer(sortedDArrayIndices[i]));
                pointColor[i] = Color.green.darker();
            }
            
            for (int i = (highestNegSigIndex + 1); i < lowestPosSigIndex; i++) {
                //nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                pointColor[i] = Color.black;
            }
            
            for (int i = lowestPosSigIndex; i < xArray.length; i++) {
                //posSigGenes.add(new Integer(sortedDArrayIndices[i]));
                pointColor[i] = Color.red;
            }
            
        } else if((posSigEncountered)&&(!negSigEncountered)) {
            for (int i = 0; i < lowestPosSigIndex; i++) {
                //nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                pointColor[i] = Color.black;
            }
            
            for (int i = lowestPosSigIndex; i < xArray.length; i++) {
                //posSigGenes.add(new Integer(sortedDArrayIndices[i]));
                pointColor[i] = Color.red;
            }
        } else if ((!posSigEncountered) && (negSigEncountered)) {
            for (int i = 0; i < (highestNegSigIndex + 1); i++) {
                //negSigGenes.add(new Integer(sortedDArrayIndices[i]));
                pointColor[i] = Color.green.darker();
            }
            for (int i = (highestNegSigIndex + 1); i < xArray.length; i++) {
                //nonSigGenes.add(new Integer[sortedDArrayIndices[i]]);
                pointColor[i] = Color.black;
            }
        } else if ((!posSigEncountered) && (!negSigEncountered)) {
            for (int i = 0; i < xArray.length; i++) {
                //nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                pointColor[i] = Color.black;
            }
        }
        
        //draw data points
        for (int i = 0; i < xArray.length; i++) {
                /*
                if (((yArray[i] - xArray[i]) > delta) && (yArray[i] > 0.0d) ) {
                    g2D.setColor(Color.red);
                } else if (((yArray[i] - xArray[i]) < (-1.0d)*delta) && (yArray[i] < 0.0d) ) {
                    g2D.setColor(Color.green.darker());
                }
                 */
            g2D.setColor(pointColor[i]);
            drawPoint(g2D, xArray[i], yArray[i], getXScalingFactor(origMaxXValue, origMinXValue), getYScalingFactor(origMaxYValue, origMinYValue), 5);
            g2D.setColor(Color.black);
            //g2D.drawOval((midX + expectedXArray[i]),
        }
        
        g2D.drawString( "X axis = Expected, Y axis = Observed", this.getWidth()/2 + 25, this.getHeight() - 25);
    }
    
    private void paint2(Graphics g) {
        Graphics2D g2D = (Graphics2D)g;
        int panelWidth = this.getWidth();
        int panelHeight = this.getHeight();
        originX = 30;
        originY = (int)Math.round((double)(this.getHeight() - 30));
        double origMaxXValue = getMax(xArray);
        double origMaxYValue = getMax(yArray);
        double origMinXValue = getMin(xArray);
        double origMinYValue = getMin(yArray);
        
        double xScalingFactor = getXScalingFactor(origMaxXValue, origMinXValue); // relative to originX and originY
        double yScalingFactor = getYScalingFactor(origMaxYValue, origMinYValue); // relative to originX and originY
        
        final float dash1[] = {10.0f};
        
        final BasicStroke dashed = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
        
        //System.out.println("xScalingFactor = " + xScalingFactor + ", yScalingFactor = " + yScalingFactor);
        
        //draw axes
        g2D.setStroke(new BasicStroke(2.0f));
        g2D.drawLine(0, (int)Math.round((double)(this.getHeight() - 30)), this.getWidth(), (int)Math.round((double)(this.getHeight() - 30)));
        g2D.drawLine(30, 0, 30, this.getHeight());
        
        double[] xIntervalArray = new double[6];
        double[] yIntervalArray = new double[6];
        
        double xIncrement = 0.0d;
        double yIncrement = 0.0d;
        
        if (Math.abs(origMaxXValue) > Math.abs(origMinXValue)) {
            xIncrement = Math.abs((double)(origMaxXValue/5.0d));
        } else {
            xIncrement = Math.abs((double)(origMinXValue/5.0d));
        }
        
        if (Math.abs(origMaxYValue) > Math.abs(origMinYValue)) {
            yIncrement = Math.abs((double)(origMaxYValue/5.0d));
        } else {
            yIncrement = Math.abs((double)(origMinYValue/5.0d));
        }
        
        double xCounter = 0.0d;
        double yCounter = 0.0d;
        for (int i = 0; i < xIntervalArray.length; i++) {
            xIntervalArray[i] = xCounter;
            xCounter = xCounter + xIncrement;
            yIntervalArray[i] = yCounter;
            yCounter = yCounter + yIncrement;
        }
        
        //draw x tick marks
        
        for (int i = 1; i < xIntervalArray.length; i++) {
            g2D.drawLine((int)Math.round(xIntervalArray[i]*xScalingFactor) + 30, (this.getHeight() - 30) - 5, (int)Math.round(xIntervalArray[i]*xScalingFactor) + 30, (this.getHeight() - 30) + 5);
        }
        
        
        
        //for (int i = 1; i < xIntervalArray.length; i++) {
        //    g2D.drawLine(this.getWidth()/2 - (int)Math.round(xIntervalArray[i]*xScalingFactor), this.getHeight()/2 - 5, this.getWidth()/2 - (int)Math.round(xIntervalArray[i]*xScalingFactor), this.getHeight()/2 + 5);
        //}
        
        
        //draw reference line of slope = 1
        g2D.drawLine(30, this.getHeight() - 30, 30 + (int)Math.round(xIntervalArray[5]*xScalingFactor), this.getHeight() - 30 - (int)Math.round(xIntervalArray[5]*yScalingFactor));
        
        g2D.setColor(Color.gray);
        
        g2D.setStroke(new BasicStroke(1.0f));
        
        //draw horizonal grid lines
        for (int i = 1; i < yIntervalArray.length; i++) {
            g2D.drawLine(0, this.getHeight() - 30 - (int)Math.round(yIntervalArray[i]*yScalingFactor), this.getWidth(), this.getHeight() - 30 - (int)Math.round(yIntervalArray[i]*yScalingFactor));
        }
        
        //for (int i = 1; i < yIntervalArray.length; i++) {
        //    g2D.drawLine(0, this.getHeight()/2 - (int)Math.round(yIntervalArray[i]*yScalingFactor), this.getWidth(), this.getHeight()/2 - (int)Math.round(yIntervalArray[i]*yScalingFactor));
        //}
        
        g2D.setStroke(new BasicStroke(2.0f));
        g2D.setColor(Color.black);
        
        DecimalFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits(2);
        
        //tick labels
        for (int i = 1; i < xIntervalArray.length; i++) {
            g2D.drawString(nf.format((double)xIntervalArray[i]), (int)Math.round(xIntervalArray[i]*xScalingFactor) + 10, this.getHeight() - 5);
        }
        
        //for (int i = 1; i < xIntervalArray.length; i++) {
        //    g2D.drawString("-" + nf.format((double)xIntervalArray[i]), this.getWidth()/2 - (int)Math.round(xIntervalArray[i]*xScalingFactor) - 10, this.getHeight()/2 + 20);
        //}
        
        for (int i = 1; i < yIntervalArray.length; i++) {
            g2D.drawString(nf.format((double)yIntervalArray[i]), 5, this.getHeight() - 30 - (int)Math.round(yIntervalArray[i]*yScalingFactor) - 5);
        }
        
        //for (int i = 1; i < yIntervalArray.length; i++) {
        //    g2D.drawString("-" + nf.format((double)yIntervalArray[i]), this.getWidth()/2 - 30, this.getHeight()/2 + (int)Math.round(yIntervalArray[i]*yScalingFactor) - 5);
        //}
        
        int deltaOffset = (int)Math.round(delta*yScalingFactor);
        
        //draw dashed lines for delta limits
        g2D.setStroke(dashed);
        
        g2D.drawLine(30, this.getHeight() - 30 - deltaOffset, 30 + (int)Math.round(xIntervalArray[5]*xScalingFactor), this.getHeight() - 30 - (int)Math.round(xIntervalArray[5]*yScalingFactor) - deltaOffset);
        g2D.drawLine(30, this.getHeight() - 30 + deltaOffset, 30 + (int)Math.round(xIntervalArray[5]*xScalingFactor), this.getHeight() - 30 - (int)Math.round(xIntervalArray[5]*yScalingFactor) + deltaOffset);
        
        g2D.setStroke(new BasicStroke(2.0f));
        
        boolean posSigEncountered = false;
        boolean negSigEncountered = false;
        
        int lowestPosSigIndex = 0;
        int highestNegSigIndex = 0;
        
        for (int i = 0; i < xArray.length; i++) {
            if ( (xArray[i] > 0.0d) && ((yArray[i] - xArray[i]) > delta) ) {
                lowestPosSigIndex = i;
                posSigEncountered = true;
                break;
            }
        }
        
        for (int i = 0; i < xArray.length; i++) {
            if ( (xArray[i] < 0.0d) && ((xArray[i] - yArray[i]) > delta) ) {
                highestNegSigIndex = i;
                negSigEncountered = true;
            }
        }
        
        Color[] pointColor = new Color[xArray.length];
        
        if ((posSigEncountered)&&(negSigEncountered)) {
            for (int i = 0; i < (highestNegSigIndex + 1); i++) {
                //negSigGenes.add(new Integer(sortedDArrayIndices[i]));
                pointColor[i] = Color.green.darker();
            }
            
            for (int i = (highestNegSigIndex + 1); i < lowestPosSigIndex; i++) {
                //nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                pointColor[i] = Color.black;
            }
            
            for (int i = lowestPosSigIndex; i < xArray.length; i++) {
                //posSigGenes.add(new Integer(sortedDArrayIndices[i]));
                pointColor[i] = Color.red;
            }
            
        } else if((posSigEncountered)&&(!negSigEncountered)) {
            for (int i = 0; i < lowestPosSigIndex; i++) {
                //nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                pointColor[i] = Color.black;
            }
            
            for (int i = lowestPosSigIndex; i < xArray.length; i++) {
                //posSigGenes.add(new Integer(sortedDArrayIndices[i]));
                pointColor[i] = Color.red;
            }
        } else if ((!posSigEncountered) && (negSigEncountered)) {
            for (int i = 0; i < (highestNegSigIndex + 1); i++) {
                //negSigGenes.add(new Integer(sortedDArrayIndices[i]));
                pointColor[i] = Color.green.darker();
            }
            for (int i = (highestNegSigIndex + 1); i < xArray.length; i++) {
                //nonSigGenes.add(new Integer[sortedDArrayIndices[i]]);
                pointColor[i] = Color.black;
            }
        } else if ((!posSigEncountered) && (!negSigEncountered)) {
            for (int i = 0; i < xArray.length; i++) {
                //nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                pointColor[i] = Color.black;
            }
        }
        
        //draw data points
        for (int i = 0; i < xArray.length; i++) {
            
            g2D.setColor(pointColor[i]);
            drawPoint(g2D, xArray[i], yArray[i], getXScalingFactor(origMaxXValue, origMinXValue), getYScalingFactor(origMaxYValue, origMinYValue), 5);
            g2D.setColor(Color.black);
            //g2D.drawOval((midX + expectedXArray[i]),
        }
        
        g2D.drawString( "X axis = Expected, Y axis = Observed", 40, 20);
        
        
    }
    
    private void drawPoint(Graphics2D g2D, double xValue, double yValue, double xScale, double yScale, int diameter) {
        int xRaw = (int)Math.round(xValue*xScale);
        int yRaw = (int)Math.round(yValue*yScale);
        //System.out.println("xValue = " + xValue + " , yValue = " + yValue + ", xRaw = " + xRaw + ", yRaw  = " + yRaw);
        
        int xCoord = 0;
        int yCoord = 0;
        
        //if (xValue >= 0) {
        if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) || (studyDesign == SAMInitDialog.ONE_CLASS)) {
            xCoord = (int)Math.round((double)(this.getWidth()/2)) + xRaw;
            //} else {
            //xCoord = Math.round((float)(this.getWidth()/2)) - xRaw;
            //}
            
            //if (yValue >= 0) {
            yCoord = (int)Math.round((double)(this.getHeight()/2)) - yRaw;
            //} else {
            //yCoord = Math.round((float)(this.getHeight()/2)) + yRaw;
            //}
        } else if (studyDesign == SAMInitDialog.MULTI_CLASS) {
            xCoord = 30 + xRaw;
            yCoord = (int)Math.round((double)(this.getHeight() - 30)) - yRaw;
        }
        
        g2D.fillOval(xCoord, yCoord, diameter, diameter);
        
    }
    
    private double getMax(double[] array) {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < array.length; i++) {
            if (max < array[i]) {
                max = array[i];
            }
        }
        return max;
    }
    
    private double getMin(double[] array) {
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < array.length; i++) {
            if (min > array[i]) {
                min = array[i];
            }
        }
        return min;
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
        
        double scalingFactor =0;
        if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) || (studyDesign == SAMInitDialog.ONE_CLASS)) {
            scalingFactor = (this.getWidth()/2 - 50)/largest;
        } else if (studyDesign == SAMInitDialog.MULTI_CLASS) {
            scalingFactor = ((this.getWidth() - 50) - 30)/largest;
        }
        
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
        
        double scalingFactor = 0;
        if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) || (studyDesign == SAMInitDialog.ONE_CLASS)) {
            scalingFactor = (this.getHeight()/2 - 50)/largest;
        } else if (studyDesign == SAMInitDialog.MULTI_CLASS) {
            scalingFactor = ((this.getHeight() - 30) - 50)/largest;
        }
        
        return scalingFactor;
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
    
    /** Returns the corner component corresponding to the indicated corner,
     * posibly null
     */
    public JComponent getCornerComponent(int cornerIndex) {
        return null;
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }
        
    public int[][] getClusters() {
        return null;
    }
    
    public Experiment getExperiment() {
        return null;
    }
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return -1;
    }
    
}
