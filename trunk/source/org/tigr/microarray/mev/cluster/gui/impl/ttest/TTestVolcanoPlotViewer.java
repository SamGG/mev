/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: TTestVolcanoPlotViewer.java,v $
 * $Revision: 1.11 $
 * $Date: 2006-05-02 16:57:56 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.ttest;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileFilter;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileView;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.util.awt.ActionInfoDialog;

/**
 *
 * @author  nbhagaba
 * @version
 */
public class TTestVolcanoPlotViewer extends JPanel implements IViewer /*, MouseMotionListener */{
    
    private Experiment experiment;
    private IFramework framework;
    private IData data;
    
    private double[] yArray, xArray;
    private boolean[] isSig;
    int originX, originY;
    int currentMouseX, currentMouseY;
    private boolean useRefLines, projectClusters, usePosAndNeg, usePosOnly, useNegOnly;
    private JPopupMenu popup;
    private JCheckBoxMenuItem useRefLinesBox, projectClustersBox;
    int currentXSliderPosition, currentYSliderPosition;
    double currentP, currentMean, oneClassMean;
    int tTestDesign;
    private Vector rawPValues, adjPValues, tValues, dfValues, meansA, meansB, sdA, sdB, oneClassMeans, oneClassSDs;
    private int exptID;
    
    /** Creates new TTestVolcanoPlotViewer */
    public TTestVolcanoPlotViewer(Experiment experiment, double[] xArray, double[] yArray, 
    		boolean[] isSig, int tTestDesign, double oneClassMean, Vector oneClassMeans, 
			Vector oneClassSDs, Vector meansA, Vector meansB, Vector sdA, Vector sdB, 
			Vector rawPValues, Vector adjPValues, Vector tValues, Vector dfValues) {
        //System.out.println("Created Volcano plot");
        //this.tTestFrame = tTestFrame;
        this.experiment = experiment;
        setExperimentID(experiment.getId());
        initialize(xArray, yArray, isSig, tTestDesign, oneClassMean, oneClassMeans, 
        		oneClassSDs, meansA, meansB, sdA, sdB, 
				rawPValues, adjPValues, tValues, dfValues);
    }
 
    public TTestVolcanoPlotViewer(Experiment e, double[] xArray, double[] yArray, 
    		boolean[] isSig, Integer tTestDesign, Double oneClassMean, Vector oneClassMeans, 
			Vector oneClassSDs, Vector meansA, Vector meansB, Vector sdA, Vector sdB, 
			Vector rawPValues, Vector adjPValues, Vector tValues, Vector dfValues){
    	this(e, xArray, yArray, isSig, tTestDesign.intValue(), oneClassMean.doubleValue(), oneClassMeans, 
        		oneClassSDs, meansA, meansB, sdA, sdB, 
				rawPValues, adjPValues, tValues, dfValues);
    }
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{this.experiment, this.xArray, this.yArray,
    			this.isSig, new Integer(tTestDesign), new Double(oneClassMean), oneClassMeans, 
				oneClassSDs, meansA, meansB, sdA, sdB,
				rawPValues, adjPValues, tValues, dfValues});
    }
    
    private void initialize(double[] xArray, double[] yArray, 
    		boolean[] isSig, int tTestDesign, double oneClassMean, Vector oneClassMeans, 
			Vector oneClassSDs, Vector meansA, Vector meansB, Vector sdA, Vector sdB, 
			Vector rawPValues, Vector adjPValues, Vector tValues, Vector dfValues){
        this.xArray = xArray;
        this.yArray = yArray;
        this.isSig = isSig;
        this.rawPValues = rawPValues;
        this.adjPValues = adjPValues;
        this.tValues = tValues;
        this.dfValues = dfValues;
        this.meansA = meansA;
        this.meansB = meansB;
        this.tTestDesign = tTestDesign;
        this.oneClassMeans = oneClassMeans;
        this.oneClassMean = oneClassMean;
        this.oneClassSDs = oneClassSDs;
        this.sdA = sdA;
        this.sdB =sdB;
        //this.tTestDesign =TtestInitDialog.BETWEEN_SUBJECTS;  //for now
        useRefLines = true;
        projectClusters = false;
        usePosAndNeg = true;
        usePosOnly = false;
        useNegOnly = false;
        currentMouseX = 0;
        currentMouseY = 0;
        currentXSliderPosition = 0;
        currentYSliderPosition = 0;
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
        popup.addSeparator();
        
        projectClustersBox = new JCheckBoxMenuItem("Project previously stored cluster colors", false);
        projectClustersBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    projectClusters = true;
                    TTestVolcanoPlotViewer.this.repaint();
                } else {
                    projectClusters = false;
                    TTestVolcanoPlotViewer.this.repaint();
                }
            }
        });
        popup.add(projectClustersBox);
        popup.addSeparator();
        
        JMenuItem showCutoffLines = new JMenuItem("Use gene selection sliders");
        showCutoffLines.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                SelectionSliderPanel ssPanel = new SelectionSliderPanel(currentYSliderPosition, currentXSliderPosition);
                TTestVolcanoPlotViewer.this.repaint();
                ssPanel.setVisible(true);
            }
        });
        popup.add(showCutoffLines);
        
        popup.addSeparator();
        
        JMenuItem storeClusterItem = new JMenuItem("Store selected genes as cluster", GUIFactory.getIcon("new16.gif"));
        storeClusterItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                storeCluster();
            }
        });
        popup.add(storeClusterItem);
        
        popup.addSeparator();
        
        JMenuItem launchNewSessionItem = new JMenuItem("Launch new session with selected genes", GUIFactory.getIcon("launch_new_mav.gif"));
        launchNewSessionItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                launchNewSession();
            }
        });
        popup.add(launchNewSessionItem);
        
        popup.addSeparator();
        
        JMenuItem saveGenesItem = new JMenuItem("Save selected genes as cluster", GUIFactory.getIcon("save16.gif"));
        saveGenesItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onSaveCluster();
            }
        });
        popup.add(saveGenesItem);
        
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }
            
            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    if ((currentXSliderPosition == 0) && (currentYSliderPosition == 0) && usePosAndNeg) {
                        for (int i = 6; i < popup.getComponentCount(); i++) {
                            Component item = popup.getComponent(i);
                            if (item instanceof JMenuItem) {
                                item.setEnabled(false);
                            }
                        }
                    } else {
                        for (int i = 6; i < popup.getComponentCount(); i++) {
                            Component item = popup.getComponent(i);
                            if (item instanceof JMenuItem) {
                                item.setEnabled(true);
                            }
                        }
                        
                    }
                    popup.show(e.getComponent(),
                    e.getX(), e.getY());
                }
            }
        });
        
    }
    
        
        
	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperiment(org.tigr.microarray.mev.cluster.gui.Experiment)
	 */
	public void setExperiment(Experiment e) {
		this.experiment = e;
		this.exptID = e.getId();
    }
            
	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExperimentID()
	 */
	public int getExperimentID() {
		return exptID;
                        }
                        
	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperimentID(int)
	 */
	public void setExperimentID(int id) {
		this.exptID = id;
                    }
        
    
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2D = (Graphics2D)g;
        int panelWidth = this.getWidth();
        int panelHeight = this.getHeight();
        
        double[] maxXAndY = getMaxXAndY();
        double[] minXAndY = getMinXAndY();
        
        double origMaxXValue = maxXAndY[0];
        double origMaxYValue = maxXAndY[1];
        double origMinXValue = minXAndY[0];
        double origMinYValue = minXAndY[1];
        
        double xScalingFactor = getXScalingFactor(origMaxXValue, origMinXValue);
        double yScalingFactor = getYScalingFactor(origMaxYValue, origMinYValue);
        
        final float dash1[] = {10.0f};
        final BasicStroke dashed = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
        
        g2D.setStroke(new BasicStroke(2.0f));
        //g2D.setColor(Color.lightGray.darker());
        //g2D.drawLine(10, 10, this.getWidth() - 10, 10);
        //g2D.drawLine(10, this.getHeight() - 10, this.getWidth() - 10, this.getHeight() - 10);
        g2D.drawLine(40, 10, this.getWidth() - 10, 10);
        g2D.drawLine(this.getWidth() - 10, 10, this.getWidth() - 10, this.getHeight() - 50);
        originX = (int)Math.round((double)(this.getWidth()/2));
        originY = (int)Math.round((double)(this.getHeight() - 50));
        
        g2D.setColor(Color.black);
        /*
        if (currentYSliderPosition > 0) {
            g2D.setStroke(dashed);
            g2D.drawLine(40, (int)Math.round(this.getHeight() - 50 - currentP*yScalingFactor), this.getWidth() - 10, (int)Math.round(this.getHeight() - 50 - currentP*yScalingFactor));
        }
         
        if (currentXSliderPosition > 0) {
            g2D.setStroke(dashed);
            if (usePosAndNeg) {
              g2D.drawLine((int)Math.round(Math.round(this.getWidth()/2) - currentMean*xScalingFactor), 10, (int)Math.round(Math.round(this.getWidth()/2) - currentMean*xScalingFactor), (int)Math.round(this.getHeight() - 50));
              g2D.drawLine((int)Math.round(Math.round(this.getWidth()/2) + currentMean*xScalingFactor), 10, (int)Math.round(Math.round(this.getWidth()/2) + currentMean*xScalingFactor), (int)Math.round(this.getHeight() - 50));
            } else if (usePosOnly) {
                g2D.drawLine((int)Math.round(Math.round(this.getWidth()/2) + currentMean*xScalingFactor), 10, (int)Math.round(Math.round(this.getWidth()/2) + currentMean*xScalingFactor), (int)Math.round(this.getHeight() - 50));
            } else if (useNegOnly) {
                g2D.drawLine((int)Math.round(Math.round(this.getWidth()/2) - currentMean*xScalingFactor), 10, (int)Math.round(Math.round(this.getWidth()/2) - currentMean*xScalingFactor), (int)Math.round(this.getHeight() - 50));
            }
        }
         
        //draw axes
        //g2D.setColor(Color.black);
        g2D.setStroke(new BasicStroke(2.0f));
         */
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
        g2D.drawString("-log10(p)", -1*this.getHeight()/2, 15);
        g2D.rotate(Math.PI/2);
/*
        double[] maxXAndY = getMaxXAndY();
        double[] minXAndY = getMinXAndY();
 
        double origMaxXValue = maxXAndY[0];
        double origMaxYValue = maxXAndY[1];
        double origMinXValue = minXAndY[0];
        double origMinYValue = minXAndY[1];
 */
        /*
        double origMaxXValue = getMax(xArray);
        double origMaxYValue = getMax(yArray);
        double origMinXValue = getMin(xArray);
        double origMinYValue = getMin(yArray);
         */
        
        //double xScalingFactor = getXScalingFactor(origMaxXValue, origMinXValue); // relative to originX and originY
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
        
        if (tTestDesign == TtestInitDialog.ONE_CLASS) {
            g2D.drawString("Gene Mean - Hypothesized Mean (" + oneClassMean + ")", this.getWidth()/2 - 85, this.getHeight() - 15);
        } else {
            g2D.drawString("Mean(GroupB) - Mean(GroupA)", this.getWidth()/2 - 85, this.getHeight() - 15);
        }
        
        int maxYInt = (int)(Math.round(origMaxYValue));
        
        int currY = 1;
        //double yScalingFactor = getYScalingFactor(origMaxYValue, origMinYValue); // relative to originX and originY
        //draw horizontal lines
        g2D.setColor(Color.gray);
        g2D.setStroke(new BasicStroke(1.0f));
        
        while ((currY <= maxYInt) && ((this.getHeight() - 50 - (int)Math.round(currY*yScalingFactor)) >= 10)) {
            g2D.drawLine(40, this.getHeight() - 50 - (int)Math.round(currY*yScalingFactor), this.getWidth() - 10, this.getHeight() - 50 - (int)Math.round(currY*yScalingFactor));
            currY++;
        }
        g2D.setColor(Color.black);
        for (int i = 1; i <= maxYInt; i++) {
            int position = 25;
            if (i >= 10) {
                position = 20;
            }
            if ((this.getHeight() - 50 - (int)Math.round(i*yScalingFactor)) >= 10) {
                g2D.drawString(nf.format(i), position, this.getHeight() - 50 - (int)Math.round(i*yScalingFactor));
            }
        }
        
        //draw data points
        for (int i = 0; i < xArray.length; i++) {
            if ((!Double.isNaN(xArray[i])) && (!Double.isNaN(yArray[i])) && (!Double.isInfinite(xArray[i])) && (!Double.isInfinite(yArray[i]))) {
                if (!projectClusters){
                    if ((currentXSliderPosition == 0) && (currentYSliderPosition == 0) && (usePosAndNeg)) {
                        if (isSig[i]) {
                            g2D.setColor(Color.red);
                        } else {
                            g2D.setColor(Color.black);
                        }
                    } else {
                        if (usePosAndNeg) {
                            if ((Math.abs(xArray[i]) >= currentMean) && (yArray[i] >= currentP)) {
                                g2D.setColor(Color.green);
                            } else {
                                if (isSig[i]) {
                                    g2D.setColor(Color.red);
                                } else {
                                    g2D.setColor(Color.black);
                                }
                            }
                        } else if (usePosOnly) {
                            if ((xArray[i] >= currentMean) && (yArray[i] >= currentP)) {
                                g2D.setColor(Color.green);
                            } else {
                                if (isSig[i]) {
                                    g2D.setColor(Color.red);
                                } else {
                                    g2D.setColor(Color.black);
                                }
                            }
                        } else if (useNegOnly) {
                            if ((xArray[i] <= -1*currentMean) && (yArray[i] >= currentP)) {
                                g2D.setColor(Color.green);
                            } else {
                                if (isSig[i]) {
                                    g2D.setColor(Color.red);
                                } else {
                                    g2D.setColor(Color.black);
                                }
                            }
                        }
                    }
                    
                } else { // if (projectClusters)
                    Color pointColor = this.data.getProbeColor(this.experiment.getGeneIndexMappedToData(i));
                    if (pointColor == null) pointColor = Color.black;
                    g2D.setColor(pointColor);
                }
                drawPoint(g2D, xArray[i], yArray[i], getXScalingFactor(origMaxXValue, origMinXValue), getYScalingFactor(origMaxYValue, origMinYValue), 5);
            }
        }
        
        g2D.setColor(Color.black);
        
        if (currentYSliderPosition > 0) {
            g2D.setStroke(dashed);
            g2D.drawLine(40, (int)Math.round(this.getHeight() - 50 - currentP*yScalingFactor), this.getWidth() - 10, (int)Math.round(this.getHeight() - 50 - currentP*yScalingFactor));
        }
        
        if (currentXSliderPosition > 0) {
            g2D.setStroke(dashed);
            if (usePosAndNeg) {
                g2D.drawLine((int)Math.round(Math.round(this.getWidth()/2) - currentMean*xScalingFactor), 10, (int)Math.round(Math.round(this.getWidth()/2) - currentMean*xScalingFactor), (int)Math.round(this.getHeight() - 50));
                g2D.drawLine((int)Math.round(Math.round(this.getWidth()/2) + currentMean*xScalingFactor), 10, (int)Math.round(Math.round(this.getWidth()/2) + currentMean*xScalingFactor), (int)Math.round(this.getHeight() - 50));
            } else if (usePosOnly) {
                g2D.drawLine((int)Math.round(Math.round(this.getWidth()/2) + currentMean*xScalingFactor), 10, (int)Math.round(Math.round(this.getWidth()/2) + currentMean*xScalingFactor), (int)Math.round(this.getHeight() - 50));
            } else if (useNegOnly) {
                g2D.drawLine((int)Math.round(Math.round(this.getWidth()/2) - currentMean*xScalingFactor), 10, (int)Math.round(Math.round(this.getWidth()/2) - currentMean*xScalingFactor), (int)Math.round(this.getHeight() - 50));
            }
        }
        
        //draw axes
        //g2D.setColor(Color.black);
        g2D.setStroke(new BasicStroke(2.0f));
        
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
    
    /**
     * Sets public color for the current cluster.
     */
    public void setClusterColor(Color color) {
        if(color ==null){  //indicates removal of cluster
            framework.removeCluster(getArrayMappedToData(), experiment, ClusterRepository.GENE_CLUSTER);
        }
    }
    
    /**
     *  Sets cluster color
     */
    public void storeCluster(){
        framework.storeSubCluster(getArrayMappedToData(), experiment, ClusterRepository.GENE_CLUSTER);
        onDataChanged(this.data);
    }
    
    public void setData(IData data) {
        this.data = data;
    }
    
    private int [] getArrayMappedToData(){
        int [] clusterIndices = getCluster();
        if(clusterIndices == null || clusterIndices.length < 1)
            return clusterIndices;
        
        int [] dataIndices = new int [clusterIndices.length];
        for(int i = 0; i < clusterIndices.length; i++){
            dataIndices[i] = this.experiment.getGeneIndexMappedToData(clusterIndices[i]);
        }
        return dataIndices;
    }
    
    private int[] getCluster() {
        Vector clusterVector = new Vector();
        for (int i = 0; i < xArray.length; i++) {
            if (usePosAndNeg) {
                if ((Math.abs(xArray[i]) >= currentMean) && (yArray[i] >= currentP)) {
                    clusterVector.add(new Integer(i));
                }
            } else if (usePosOnly) {
                if ((xArray[i] >= currentMean) && (yArray[i] >= currentP)) {
                    clusterVector.add(new Integer(i));
                }
            } else if (useNegOnly) {
                if ((xArray[i] <= -1*currentMean) && (yArray[i] >= currentP)) {
                    clusterVector.add(new Integer(i));
                }
            }
        }
        
        int[] clust = new int[clusterVector.size()];
        for (int i = 0; i < clust.length; i++) {
            clust[i] = ((Integer)(clusterVector.get(i))).intValue();
        }
        //System.out.println("clust.length = " + clust.length);
        return clust;
    }
    
    public void launchNewSession(){
        framework.launchNewMAV(getArrayMappedToData(), this.experiment, "Multiple Experiment Viewer - Volcano Plot Selected Genes Viewer", Cluster.GENE_CLUSTER);
    }
    
    /**
     * Returns a file choosed by the user.
     */
    private static File getFile(Frame frame) {
        File file = null;
        final JFileChooser fc = new JFileChooser(TMEV.getFile("data/"));
        fc.addChoosableFileFilter(new ExpressionFileFilter());
        fc.setFileView(new ExpressionFileView());
        int ret = fc.showSaveDialog(frame);
        if (ret == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
        }
        return file;
    }
    
    /**
     * Save the viewer cluster.
     */
    private void onSaveCluster() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        try {
            saveExperiment(frame, getExperiment(), getData(), getCluster());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Returns the experiment data (ratio values).
     */
    public Experiment getExperiment() {
        return experiment;
    }
    
    /**
     * Returns data values.
     */
    public IData getData() {
        return data;
    }
    
    /**
     * Saves values from specified experiment and its rows.
     */
    public void saveExperiment(Frame frame, Experiment experiment, IData data, int[] rows) throws Exception {
        File file = getFile(frame);
        if (file != null) {
            saveCluster(file, experiment, data, rows);
        }
    }
    
    private void saveCluster(File file, Experiment experiment, IData data, int[] rows) throws Exception {
        PrintWriter out = new PrintWriter(new FileOutputStream(file));
        String[] fieldNames = data.getFieldNames();
        out.print("Original row");
        out.print("\t");
        for (int i = 0; i < fieldNames.length; i++) {
            out.print(fieldNames[i]);
            //if (i < fieldNames.length - 1) {
            out.print("\t");
            //}
        }
        if (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) {
            out.print("GroupA mean\t");
            out.print("GroupA std.dev.\t");
            out.print("GroupB mean\t");
            out.print("GroupB std.dev.\t");
            out.print("Absolute t value");
            
        } else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
            out.print("Gene mean\t");
            out.print("Gene std.dev.\t");
            out.print("t value");
        }
        //out.print("\t");
        
        out.print("\t");
        out.print("Degrees of freedom\t");
        out.print("Raw p value\t");
        out.print("Adj p value");
        
        //out.print("UniqueID\tName");
        for (int i=0; i<experiment.getNumberOfSamples(); i++) {
            out.print("\t");
            out.print(data.getSampleName(experiment.getSampleIndex(i)));
        }
        out.print("\n");
        for (int i=0; i<rows.length; i++) {
            out.print(Integer.toString(experiment.getGeneIndexMappedToData(rows[i]) + 1));
            //out.print(data.getUniqueId(rows[i]));
            out.print("\t");
            //out.print(data.getGeneName(rows[i]));
            for (int k = 0; k < fieldNames.length; k++) {
                out.print(data.getElementAttribute(experiment.getGeneIndexMappedToData(rows[i]), k));
                //if (k < fieldNames.length - 1) {
                out.print("\t");
                //}
            }
            if (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) {
                out.print(((Float)meansA.get(rows[i])).floatValue() + "\t");
                out.print(((Float)sdA.get(rows[i])).floatValue() + "\t");
                out.print(((Float)meansB.get(rows[i])).floatValue() + "\t");
                out.print(((Float)sdB.get(rows[i])).floatValue() + "\t");
            } else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
                out.print(((Float)oneClassMeans.get(rows[i])).floatValue() + "\t");
                out.print(((Float)oneClassSDs.get(rows[i])).floatValue() + "\t");
            }
            //out.print("\t");
            out.print("" + ((Float)tValues.get(rows[i])).floatValue());
            out.print("\t");
            out.print("" + ((Float)dfValues.get(rows[i])).intValue());
            out.print("\t");
            out.print("" + ((Float)rawPValues.get(rows[i])).floatValue());
            out.print("\t");
            out.print("" + ((Float)adjPValues.get(rows[i])).floatValue());            
            for (int j=0; j<experiment.getNumberOfSamples(); j++) {
                out.print("\t");
                out.print(Float.toString(experiment.get(rows[i], j)));
            }
            out.print("\n");
        }
        out.flush();
        out.close();
    }
    
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
        setData(data);
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
        this.framework = framework;
        setData(framework.getData());
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
    
    void buildConstraints(GridBagConstraints gbc, int gx, int gy,
    int gw, int gh, int wx, int wy) {
        
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }
    
    /** Returns the corner component corresponding to the indicated corner,
     * posibly null
     */
    public JComponent getCornerComponent(int cornerIndex) {
        return null;
    }

    /** Returns the viewer's clusters or null
     */
    public int[][] getClusters() {
        return null;
    }    
 
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return Cluster.GENE_CLUSTER;
    }    
    
    class SelectionSliderPanel extends ActionInfoDialog {
        JSlider pValueSlider, meanSlider;
        JTextField pValueField, meanField;
        JRadioButton posAndNeg, posOnly, negOnly;
        JButton closeButton, resetButton;
        int initP, initMean;
        double maxP, maxMean;
        
        SelectionSliderPanel(int initP, int initMean) {
            super((JFrame)(JOptionPane.getFrameForComponent(TTestVolcanoPlotViewer.this)), "Select genes", true);
            this.setBackground(Color.white);
            //super(new JFrame(), "Select genes", true);
            this.initP = initP;
            this.initMean = initMean;
            this.maxP = getMaxXAndY()[1];
            this.maxMean = Math.max(Math.abs(getMaxXAndY()[0]), Math.abs(getMinXAndY()[0]));
            setBounds(0, 0, 450, 200);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            //constraints.fill = GridBagConstraints.BOTH;
            JPanel pane = new JPanel();
            pane.setLayout(gridbag);
            
            JPanel sliderPanel = new JPanel();
            GridBagLayout grid1 = new GridBagLayout();
            sliderPanel.setLayout(grid1);
            
            JLabel pValueLabel = new JLabel("Select -log10(p) cutoff: ");
            buildConstraints(constraints, 0, 0, 1, 1, 30, 50);
            grid1.setConstraints(pValueLabel, constraints);
            sliderPanel.add(pValueLabel);
            
            pValueSlider = new JSlider(0, 100, initP);
            pValueField = new JTextField("" + getDisplayPValue(initP), 7);
            pValueField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int sliderValue;
                    try {
                        String s = pValueField.getText();
                        //double maxP = getMaxXAndY()[1];
                        double f = Double.parseDouble(s);
                        sliderValue = (int)Math.round(f*100/maxP);
                        if (sliderValue >= 100) {
                            pValueSlider.setValue(100);
                            pValueField.setText("" + (float)maxP);
                        } else if (sliderValue <= 0) {
                            pValueSlider.setValue(0);
                            pValueField.setText("" + 0.0f);
                        } else {
                            pValueSlider.setValue(sliderValue);
                            pValueField.setText("" + (float)f);
                        }
                    } catch (Exception exc) {
                        pValueSlider.setValue(0);
                        pValueField.setText("" + 0.0f);
                    }
                    
                    currentYSliderPosition = pValueSlider.getValue();
                    currentP = getDisplayPValue(currentYSliderPosition);
                    TTestVolcanoPlotViewer.this.repaint();
                }
            });
            pValueField.addFocusListener(new FocusListener() {
                public void focusLost(FocusEvent e) {
                    int sliderValue;
                    try {
                        String s = pValueField.getText();
                        //double maxP = getMaxXAndY()[1];
                        double f = Double.parseDouble(s);
                        sliderValue = (int)Math.round(f*100/maxP);
                        if (sliderValue >= 100) {
                            pValueSlider.setValue(100);
                            pValueField.setText("" + (float)maxP);
                        } else if (sliderValue <= 0) {
                            pValueSlider.setValue(0);
                            pValueField.setText("" + 0.0f);
                        } else {
                            pValueSlider.setValue(sliderValue);
                            pValueField.setText("" + (float)f);
                        }
                    } catch (Exception exc) {
                        pValueSlider.setValue(0);
                        pValueField.setText("" + 0.0f);
                    }
                    
                    currentYSliderPosition = pValueSlider.getValue();
                    currentP = getDisplayPValue(currentYSliderPosition);
                    TTestVolcanoPlotViewer.this.repaint();
                }
                
                public void focusGained(FocusEvent e) {
                }
            });
            pValueSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    int value = pValueSlider.getValue();
                    //double maxP = getMaxXAndY()[1];
                    double displayValue = (double)(value*maxP/100);
                    if (value == 0) {
                        displayValue = 0.0f;
                    }
                    pValueField.setText("" + (float)displayValue);
                    
                    currentYSliderPosition = pValueSlider.getValue();
                    currentP = getDisplayPValue(currentYSliderPosition);
                    TTestVolcanoPlotViewer.this.repaint();
                }
            });
            
            pValueSlider.setBackground(Color.white);
            buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
            grid1.setConstraints(pValueSlider, constraints);
            sliderPanel.add(pValueSlider);
            
            
            buildConstraints(constraints, 2, 0, 1, 1, 20, 0);
            grid1.setConstraints(pValueField, constraints);
            sliderPanel.add(pValueField);
            
            JLabel meanLabel = new JLabel("Select mean diff. cutoff: ");
            buildConstraints(constraints, 0, 1, 1, 1, 30, 50);
            grid1.setConstraints(meanLabel, constraints);
            sliderPanel.add(meanLabel);
            
            meanSlider = new JSlider(0, 100, initMean);
            meanField = new JTextField("" + getDisplayMean(initMean), 7);
            meanSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    int value = meanSlider.getValue();
                    //double maxP = getMaxXAndY()[1];
                    double displayValue = (double)(value*maxMean/100);
                    if (value == 0) {
                        displayValue = 0.0f;
                    }
                    meanField.setText("" + (float)displayValue);
                    
                    currentXSliderPosition = meanSlider.getValue();
                    currentMean = getDisplayMean(currentXSliderPosition);
                    TTestVolcanoPlotViewer.this.repaint();
                }
            });
            meanField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int sliderValue;
                    try {
                        String s = meanField.getText();
                        //double maxP = getMaxXAndY()[1];
                        double f = Double.parseDouble(s);
                        sliderValue = (int)Math.round(f*100/maxMean);
                        if (sliderValue >= 100) {
                            meanSlider.setValue(100);
                            meanField.setText("" + (float)maxMean);
                        } else if (sliderValue <= 0) {
                            meanSlider.setValue(0);
                            meanField.setText("" + 0.0f);
                        } else {
                            meanSlider.setValue(sliderValue);
                            meanField.setText("" + (float)f);
                        }
                    } catch (Exception exc) {
                        meanSlider.setValue(0);
                        meanField.setText("" + 0.0f);
                    }
                    
                    currentXSliderPosition = meanSlider.getValue();
                    currentMean = getDisplayMean(currentXSliderPosition);
                    TTestVolcanoPlotViewer.this.repaint();
                }
            });
            meanField.addFocusListener(new FocusListener() {
                public void focusLost(FocusEvent e) {
                    int sliderValue;
                    try {
                        String s = meanField.getText();
                        //double maxP = getMaxXAndY()[1];
                        double f = Double.parseDouble(s);
                        sliderValue = (int)Math.round(f*100/maxMean);
                        if (sliderValue >= 100) {
                            meanSlider.setValue(100);
                            meanField.setText("" + (float)maxMean);
                        } else if (sliderValue <= 0) {
                            meanSlider.setValue(0);
                            meanField.setText("" + 0.0f);
                        } else {
                            meanSlider.setValue(sliderValue);
                            meanField.setText("" + (float)f);
                        }
                    } catch (Exception exc) {
                        meanSlider.setValue(0);
                        meanField.setText("" + 0.0f);
                    }
                    
                    currentXSliderPosition = meanSlider.getValue();
                    currentMean = getDisplayMean(currentXSliderPosition);
                    TTestVolcanoPlotViewer.this.repaint();
                }
                
                public void focusGained(FocusEvent e) {
                }
            });
            
            meanSlider.setBackground(Color.white);
            buildConstraints(constraints, 1, 1, 1, 1, 50, 0);
            grid1.setConstraints(meanSlider, constraints);
            sliderPanel.add(meanSlider);
            
            
            buildConstraints(constraints, 2, 1, 1, 1, 20, 0);
            grid1.setConstraints(meanField, constraints);
            sliderPanel.add(meanField);
            
            JPanel chooseMeansPanel = new JPanel();
            GridBagLayout grid2 = new GridBagLayout();
            chooseMeansPanel.setLayout(grid2);
            
            ButtonGroup group = new ButtonGroup();
            posAndNeg = new JRadioButton("Both pos. and neg.");
            posOnly = new JRadioButton("Pos. only");
            negOnly = new JRadioButton("Neg. only");
            if (usePosAndNeg) {
                //usePosAndNeg = true;
                posAndNeg.setSelected(true);
                posOnly.setSelected(false);
                negOnly.setSelected(false);
            } else if (usePosOnly) {
                posAndNeg.setSelected(false);
                posOnly.setSelected(true);
                negOnly.setSelected(false);
            } else if (useNegOnly) {
                posAndNeg.setSelected(false);
                posOnly.setSelected(false);
                negOnly.setSelected(true);
            }
            posAndNeg.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    usePosAndNeg = true;
                    usePosOnly = false;
                    useNegOnly = false;
                    TTestVolcanoPlotViewer.this.repaint();
                }
            });
            posAndNeg.setBackground(Color.white);
            
            //posOnly = new JRadioButton("Pos. only", false);
            posOnly.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    usePosAndNeg = false;
                    usePosOnly = true;
                    useNegOnly = false;
                    TTestVolcanoPlotViewer.this.repaint();
                }
            });
            posOnly.setBackground(Color.white);
            
            //negOnly = new JRadioButton("Neg. only", false);
            negOnly.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    usePosAndNeg = false;
                    usePosOnly = false;
                    useNegOnly = true;
                    TTestVolcanoPlotViewer.this.repaint();
                }
            });
            negOnly.setBackground(Color.white);
            
            group.add(posAndNeg);
            group.add(posOnly);
            group.add(negOnly);
            
            buildConstraints(constraints, 0, 0, 1, 1, 33, 100);
            grid2.setConstraints(posAndNeg, constraints);
            chooseMeansPanel.add(posAndNeg);
            
            buildConstraints(constraints, 1, 0, 1, 1, 33, 0);
            grid2.setConstraints(posOnly, constraints);
            chooseMeansPanel.add(posOnly);
            
            buildConstraints(constraints, 2, 0, 1, 1, 34, 0);
            grid2.setConstraints(negOnly, constraints);
            chooseMeansPanel.add(negOnly);
            
            JPanel resetClosePanel = new JPanel();
            GridBagLayout grid3 = new GridBagLayout();
            resetClosePanel.setLayout(grid3);
            
            resetButton = new JButton("Reset");
            resetButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    pValueSlider.setValue(0);
                    pValueField.setText("0.0");
                    meanSlider.setValue(0);
                    meanField.setText("0.0");
                    posAndNeg.setSelected(true);
                    usePosAndNeg = true;
                    usePosOnly = false;
                    useNegOnly = false;
                    currentXSliderPosition = 0;
                    currentYSliderPosition = 0;
                }
            });
            buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
            constraints.anchor = GridBagConstraints.EAST;
            grid3.setConstraints(resetButton, constraints);
            resetClosePanel.add(resetButton);
            
            closeButton = new JButton("OK");
            closeButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    SelectionSliderPanel.this.dispose();
                }
            });
            buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.WEST;
            grid3.setConstraints(closeButton, constraints);
            resetClosePanel.add(closeButton);
            
            constraints.anchor = GridBagConstraints.CENTER;
            
            sliderPanel.setBorder(new EtchedBorder());
            chooseMeansPanel.setBorder(new EtchedBorder());
            resetClosePanel.setBorder(new EtchedBorder());
            
            sliderPanel.setBackground(Color.white);
            chooseMeansPanel.setBackground(Color.white);
            resetClosePanel.setBackground(Color.white);
            
            constraints.fill = GridBagConstraints.BOTH;
            
            buildConstraints(constraints, 0, 0, 1, 1, 100, 40);
            //constraints.fill = GridBagConstraints.VERTICAL;
            gridbag.setConstraints(sliderPanel, constraints);
            pane.add(sliderPanel);
            
            //constraints.fill = GridBagConstraints.NONE;
            
            buildConstraints(constraints, 0, 1, 1, 1, 0, 30);
            gridbag.setConstraints(chooseMeansPanel, constraints);
            pane.add(chooseMeansPanel);
            
            buildConstraints(constraints, 0, 2, 1, 1, 0, 30);
            gridbag.setConstraints(resetClosePanel, constraints);
            pane.add(resetClosePanel);
            
            setContentPane(pane);
        }
        
        private float getDisplayPValue(int pSliderVal) {
            double d = (double)(pSliderVal*maxP/100);
            
            return (float)d;
        }
        
        private float getDisplayMean(int meanSliderVal) {
            double d = (double)(meanSliderVal*maxMean/100);
            return (float)d;
        }
        
        public void setVisible(boolean visible) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
            
            super.setVisible(visible);
        }
        
        
    }
    
    
}
