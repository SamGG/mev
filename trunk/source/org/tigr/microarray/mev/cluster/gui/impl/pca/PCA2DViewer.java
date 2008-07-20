/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * PCA2DViewer.java
 *
 * Created on October 25, 2004, 2:41 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.pca;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.util.FloatMatrix;

/**
 *
 * @author  nbhagaba
 */
public class PCA2DViewer extends JPanel implements IViewer {
    
    private static final String SAVE_CMD    = "save-cmd";
    private static final String SHOW_TEXT_CMD = "show-text-cmd";
    private static final String SHOW_TICK_LABELS_CMD = "show-tick-labels-cmd";
    private static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
    private static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";  
    private static final String DISPLAY_EXPT_NAMES_CMD = "display-expt-names-cmd";
    private static final String SHOW_LARGER_POINTS_CMD = "show-larger-points-cmd";
    
    private float[] yArray, xArray;     
    //int originX, originY;
    private int axis1, axis2;  
    private FloatMatrix UMatrix;
    private Experiment experiment; 
    private IFramework framework;
    private Frame frame;
    private IData data; 
    private JPopupMenu popup; 
    private Ellipse2D.Double ellipse;
    private boolean displayExptNames, showLargePoints, geneViewer, showTickLabels;
    Rectangle currentRect = null;
    Rectangle rectToDraw = null;
    Rectangle previousRectDrawn = new Rectangle();    
    private int exptID = 0;
    
    /** Creates a new instance of PCA2DViewer */
    public PCA2DViewer(Experiment experiment, float[] xArray, float[] yArray, boolean geneViewer, int axis1, int axis2) {
       this.yArray = yArray;
        this.xArray = xArray;
        this.displayExptNames = false;
        this.showLargePoints = false;
        this.showTickLabels = true;
        this.geneViewer = geneViewer;
        this.axis1 = axis1;
        this.axis2 = axis2;
        this.experiment = experiment;
        this.exptID = experiment.getId();
        this.ellipse = new Ellipse2D.Double();
        this.setBackground(Color.white);   
        
        popup = createJPopupMenu();        
        
        GraphListener graphListener = new GraphListener();
        addMouseListener(graphListener);
        addMouseMotionListener(graphListener);      
    }
    
    /** Creates a new instance of PCA2DViewer */
    public PCA2DViewer(Experiment experiment, FloatMatrix UMatrix, boolean geneViewer, int axis1, int axis2) {
        this.UMatrix = UMatrix;
        this.axis1 = axis1;
        this.axis2 = axis2;        
        this.xArray = getFloatArray(UMatrix, axis1);
        this.yArray = getFloatArray(UMatrix, axis2);
        this.displayExptNames = false;
        this.showLargePoints = false;
        this.showTickLabels = true;
        this.geneViewer = geneViewer;
        this.experiment = experiment;
        this.exptID = experiment.getId();
        this.ellipse = new Ellipse2D.Double();
        this.setBackground(Color.white); 
        
        popup = createJPopupMenu();
        
        GraphListener graphListener = new GraphListener();
        addMouseListener(graphListener);
        addMouseMotionListener(graphListener);        
    }    
    /** Creates a new instance of PCA2DViewer */
    public PCA2DViewer(Experiment e, FloatMatrix UMatrix, Boolean geneViewer, Integer axis1, Integer axis2) {
    	this.UMatrix = UMatrix;
        this.axis1 = axis1.intValue();
        this.axis2 = axis2.intValue();
        this.xArray = getFloatArray(UMatrix, this.axis1);
        this.yArray = getFloatArray(UMatrix, this.axis2);
        this.displayExptNames = false;
        this.showLargePoints = false;
        this.showTickLabels = true;
        this.geneViewer = geneViewer.booleanValue();
        this.ellipse = new Ellipse2D.Double();
        this.setBackground(Color.white); 
    
        popup = createJPopupMenu();
    
        GraphListener graphListener = new GraphListener();
        addMouseListener(graphListener);
        addMouseMotionListener(graphListener);  
        setExperiment(e);
    }     
    /**
     * @inheritDoc
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{this.experiment, UMatrix, new Boolean(geneViewer), new Integer(axis1), new Integer(axis2)});
    }
   
    
   private float[] getFloatArray(FloatMatrix matrix, int column) {
       float[] array = new float[matrix.getRowDimension()];
       for (int i = 0; i < array.length; i++) {
           array[i] = matrix.A[i][column];
       }
       return array;
   }    
    
    
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2D = (Graphics2D)g;
        int panelWidth = this.getWidth();
        int panelHeight = this.getHeight();
        int originX = (int)Math.round((double)(this.getWidth()/2));
        int originY = (int)Math.round((double)(this.getHeight()/2));
        double origMaxXValue = getMax(xArray);
        double origMaxYValue = getMax(yArray);
        double origMinXValue = getMin(xArray);
        double origMinYValue = getMin(yArray);
        
        double xScalingFactor = getXScalingFactor(origMaxXValue, origMinXValue); // relative to originX and originY
        double yScalingFactor = getYScalingFactor(origMaxYValue, origMinYValue); // relative to originX and originY   
        
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
        
        if (this.showTickLabels) {
            //draw x tick marks
            
            for (int i = 1; i < xIntervalArray.length; i++) {
                g2D.drawLine((int)Math.round(xIntervalArray[i]*xScalingFactor) +this.getWidth()/2, this.getHeight()/2 - 5, (int)Math.round(xIntervalArray[i]*xScalingFactor) +this.getWidth()/2, this.getHeight()/2 + 5);
            }
            
            for (int i = 1; i < xIntervalArray.length; i++) {
                g2D.drawLine(this.getWidth()/2 - (int)Math.round(xIntervalArray[i]*xScalingFactor), this.getHeight()/2 - 5, this.getWidth()/2 - (int)Math.round(xIntervalArray[i]*xScalingFactor), this.getHeight()/2 + 5);
            }
            
            //draw y tick marks
            for (int i = 1; i < yIntervalArray.length; i++) {
                g2D.drawLine(this.getWidth()/2 - 5, this.getHeight()/2 + (int)Math.round(yIntervalArray[i]*yScalingFactor), this.getWidth()/2 + 5, this.getHeight()/2 + (int)Math.round(yIntervalArray[i]*yScalingFactor));
            }
            
            for (int i = 1; i < yIntervalArray.length; i++) {
                g2D.drawLine(this.getWidth()/2 - 5, this.getHeight()/2 - (int)Math.round(yIntervalArray[i]*yScalingFactor), this.getWidth()/2 + 5, this.getHeight()/2 - (int)Math.round(yIntervalArray[i]*yScalingFactor));
            }
            
            g2D.setStroke(new BasicStroke(2.0f));
            g2D.setColor(Color.black);
            
            DecimalFormat nf = new DecimalFormat();
            nf.setMaximumFractionDigits(2);
            
            //tick labels
            for (int i = 1; i < xIntervalArray.length; i++) {
                double stringWidth = (g2D.getFontMetrics()).stringWidth(nf.format(xIntervalArray[i]));
                g2D.drawString(nf.format((double)xIntervalArray[i]), (int)Math.round(xIntervalArray[i]*xScalingFactor) +this.getWidth()/2 - (int)Math.round(0.5d*stringWidth), this.getHeight()/2 + 20);
            }
            
            for (int i = 1; i < xIntervalArray.length; i++) {
                double stringWidth = (g2D.getFontMetrics()).stringWidth("-" + nf.format(xIntervalArray[i]));
                g2D.drawString("-" + nf.format((double)xIntervalArray[i]), this.getWidth()/2 - (int)Math.round(xIntervalArray[i]*xScalingFactor) - (int)Math.round(0.5d*stringWidth), this.getHeight()/2 + 20);
            }
            
            for (int i = 1; i < yIntervalArray.length; i++) {
                //double stringWidth = (g2D.getFontMetrics()).stringWidth(nf.format(yIntervalArray[i]));
                g2D.drawString(nf.format((double)yIntervalArray[i]), this.getWidth()/2 + 10, this.getHeight()/2 - (int)Math.round(yIntervalArray[i]*yScalingFactor) );
            }
            
            for (int i = 1; i < yIntervalArray.length; i++) {
                g2D.drawString("-" + nf.format((double)yIntervalArray[i]), this.getWidth()/2 + 10, this.getHeight()/2 + (int)Math.round(yIntervalArray[i]*yScalingFactor) );
            }
        }
        
        //Color[] pointColor = new Color[xArray.length];
        
        //if ((geneOrExpt == COAGUI.GENES) || (geneOrExpt == COAGUI.EXPTS)) {
            /*
            for (int i = 0; i < pointColor.length; i++) {
                pointColor[i] = Color.black; //just for now, will add cluster colors later
            }
             */
            //draw data points
            for (int i = 0; i < xArray.length; i++) {
                Color currPointColor = Color.black;
                if (this.geneViewer) {
                	this.experiment.toString();
                    currPointColor = this.data.getProbeColor(this.experiment.getGeneIndexMappedToData(i));
                    if (currPointColor == null) currPointColor = Color.black;
                } else {
                    currPointColor = this.data.getExperimentColor(i);
                    if (currPointColor == null) currPointColor = Color.black;
                }
                g2D.setColor(currPointColor);
                if (showLargePoints) {
                    drawRectPoint(g2D, xArray[i], yArray[i], getXScalingFactor(origMaxXValue, origMinXValue), getYScalingFactor(origMaxYValue, origMinYValue), 8);
                } else {
                    drawPoint(g2D, xArray[i], yArray[i], getXScalingFactor(origMaxXValue, origMinXValue), getYScalingFactor(origMaxYValue, origMinYValue), 5);
                }
                g2D.setColor(Color.black);
                //g2D.drawOval((midX + expectedXArray[i]),
            }
            g2D.drawString( "X axis = " + (axis1 + 1) + ", Y axis = " + (axis2 + 1), this.getWidth()/2 + 25, this.getHeight() - 25);
            
        //} 
        
        // display expt names
        
        if (!this.geneViewer) {
            if (this.displayExptNames) {
                FontMetrics fmet = g2D.getFontMetrics(g2D.getFont());
                double ascent = (double)(fmet.getAscent());
                double advance = (double)(fmet.getMaxAdvance());
                for (int i = 0; i < xArray.length; i++) {
                    double currCoords[] = getCoords(xArray[i], yArray[i]);
                    String currName = data.getSampleName(i);
                    double stringWidth = (double)(fmet.stringWidth(currName));
                    if ((currCoords[0] + 0.5d*advance + stringWidth) > (double)(this.getWidth())) {
                        g2D.drawString(currName, (float)(currCoords[0] - stringWidth - 0.25d*advance), (float)(currCoords[1] + 0.5d*ascent));
                    }
                    else {
                        g2D.drawString(currName, (float)(currCoords[0] + 0.5d*advance), (float)(currCoords[1] + 0.5d*ascent));
                    }                    
                    //g2D.drawString(currName, (float)(currCoords[0] + 0.5d*advance), (float)(currCoords[1] + 0.5d*ascent));
                }
            }
        }        
        
        if (currentRect != null) {
            g2D.setXORMode(Color.white);//Color of line varies
                                           //depending on image colors
            g2D.draw(ellipse);
        }
    }
    
    private void drawPoint(Graphics2D g2D, double xValue, double yValue, double xScale, double yScale, int diameter) {
        int xRaw = (int)Math.round(xValue*xScale);
        int yRaw = (int)Math.round(yValue*yScale);
        //System.out.println("xValue = " + xValue + " , yValue = " + yValue + ", xRaw = " + xRaw + ", yRaw  = " + yRaw);
        
        int xCoord = (int)Math.round((double)(this.getWidth()/2)) + xRaw;
        int yCoord = (int)Math.round((double)(this.getHeight()/2)) - yRaw;
        
        g2D.fillOval(xCoord, yCoord, diameter, diameter);   
    } 
    
    private void drawRectPoint(Graphics2D g2D, double xValue, double yValue, double xScale, double yScale, int dim) {
        int xRaw = (int)Math.round(xValue*xScale);
        int yRaw = (int)Math.round(yValue*yScale);
        //System.out.println("xValue = " + xValue + " , yValue = " + yValue + ", xRaw = " + xRaw + ", yRaw  = " + yRaw);
        
        int xCoord = (int)Math.round((double)(this.getWidth()/2)) + xRaw;
        int yCoord = (int)Math.round((double)(this.getHeight()/2)) - yRaw;
        
        g2D.fillRect(xCoord, yCoord, dim, dim);
    }
    
    private double getMax(float[] array) {
        float max = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < array.length; i++) {
            if (max < array[i]) {
                max = array[i];
            }
        }
        return (double)max;
    }
    
    private double getMin(float[] array) {
        float min = Float.POSITIVE_INFINITY;
        for (int i = 0; i < array.length; i++) {
            if (min > array[i]) {
                min = array[i];
            }
        }
        return (double)min;
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
        scalingFactor = (this.getWidth()/2 - 50)/largest;       
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
        scalingFactor = (this.getHeight()/2 - 50)/largest;
        return scalingFactor;
    }    
    
    /** Returns the viewer's clusters or null
     */
    public int[][] getClusters() {
        return null;
    }
    
    public int[] getSelectedPoints() {
        Vector selPointsVector = new Vector();
        for (int i = 0; i < UMatrix.getRowDimension(); i++) {
            double[] currCoords = getCoords(xArray[i], yArray[i]);
            if (ellipse.contains(currCoords[0], currCoords[1])) {
                if (this.geneViewer) {
                    selPointsVector.add(new Integer(experiment.getGeneIndexMappedToData(i)));
                } else {
                    selPointsVector.add(new Integer(experiment.getSampleIndex(i)));
                }
            }
        }
        
        int[] selPoints = new int[selPointsVector.size()];
        for (int i = 0; i < selPoints.length; i++) {
            selPoints[i] = ((Integer)(selPointsVector.get(i))).intValue();
        }
        
        return selPoints;
    }    
    
    private double[] getCoords(double xValue, double yValue) {
        double origMaxXValue = getMax(xArray);
        double origMaxYValue = getMax(yArray);
        double origMinXValue = getMin(xArray);
        double origMinYValue = getMin(yArray);
        
        double xScalingFactor = getXScalingFactor(origMaxXValue, origMinXValue); // relative to originX and originY
        double yScalingFactor = getYScalingFactor(origMaxYValue, origMinYValue); // relative to originX and originY  
        
        double xRaw = Math.round(xValue*xScalingFactor);
        double yRaw= Math.round(yValue*yScalingFactor);
        
        double xCoord = Math.round((double)(this.getWidth()/2)) + xRaw;
        double yCoord = Math.round((double)(this.getHeight()/2)) - yRaw;      
        
       double[] coords = {xCoord, yCoord};
       return coords;
    }
    
    /** Returns a component to be inserted into scroll pane view port.
     */
    public JComponent getContentComponent() {
        return this;
    }
    
    /** Returns the corner component corresponding to the indicated corner,
     * posibly null
     */
    public JComponent getCornerComponent(int cornerIndex) {
        return null;
    }
    
    /**  Returns the viewer's experiment or null
     */
    public Experiment getExperiment() {
        return experiment;
    }
    
    /** Returns a component to be inserted into scroll pane header.
     */
    public JComponent getHeaderComponent() {
        return null;
    }
    
    /** Invoked by the framework to save or to print viewer image.
     */
    public BufferedImage getImage() {
        return null;
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }
    
    /** Invoked when the framework is going to be closed.
     */
    public void onClosed() {
    }
    
    /** Invoked by the framework when data is changed,
     * if this viewer is selected.
     * @see IData
     */
    public void onDataChanged(IData data) {
        setData(data);        
    }
    
    /** Invoked by the framework when this viewer was deselected.
     */
    public void onDeselected() {
    }
    
    /** Invoked by the framework when display menu is changed,
     * if this viewer is selected.
     * @see IDisplayMenu
     */
    public void onMenuChanged(IDisplayMenu menu) {
    }
    
    /** Invoked by the framework when this viewer is selected.
     */
    public void onSelected(IFramework framework) {
        this.framework = framework;
        this.frame = framework.getFrame();
        setData(framework.getData());   
        
        //In case it is viewed after serialization
        if(popup == null){
            popup = createJPopupMenu(); 
            DefaultMutableTreeNode node = framework.getCurrentNode();
            if(node != null){
                if(node.getUserObject() instanceof LeafInfo){
                    LeafInfo leafInfo = (LeafInfo) node.getUserObject();
                    leafInfo.setPopupMenu(this.popup);
                }
            }
        }         
    }
    
    public void setData(IData data) {
        this.data = data;
    }    
    
    private class GraphListener extends MouseInputAdapter {
       
        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            currentRect = new Rectangle(x, y, 0, 0);
            updateDrawableRect(getWidth(), getHeight());
            repaint();
            onShowSelection();
        }
        
        public void mouseDragged(MouseEvent e) {                        
            updateSize(e);
        }   
        
        public void mouseReleased(MouseEvent e) {                       
            updateSize(e);
        }
        
        void updateSize(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            currentRect.setSize(x - currentRect.x,
            y - currentRect.y);
            updateDrawableRect(getWidth(), getHeight());
            Rectangle totalRepaint = rectToDraw.union(previousRectDrawn); 
            ellipse.setFrame(totalRepaint.getX(), totalRepaint.getY(), totalRepaint.getWidth(), totalRepaint.getHeight());
            PCA2DViewer.this.repaint();
            onShowSelection();
            //repaint(totalRepaint.x, totalRepaint.y,
            //totalRepaint.width, totalRepaint.height);
        }   
        
        private void updateDrawableRect(int compWidth, int compHeight) {
            int x = currentRect.x;
            int y = currentRect.y;
            int width = currentRect.width;
            int height = currentRect.height;
    
            //Make the width and height positive, if necessary.
            if (width < 0) {
                width = 0 - width;
                x = x - width + 1; 
                if (x < 0) {
                    width += x; 
                    x = 0;
                }
            }
            if (height < 0) {
                height = 0 - height;
                y = y - height + 1; 
                if (y < 0) {
                    height += y; 
                    y = 0;
                }
            }
    
            //The rectangle shouldn't extend past the drawing area.
            if ((x + width) > compWidth) {
                width = compWidth - x;
            }
            if ((y + height) > compHeight) {
                height = compHeight - y;
            }
          
            //Update rectToDraw after saving old value.
            if (rectToDraw != null) {
                previousRectDrawn.setBounds(
                            rectToDraw.x, rectToDraw.y, 
                            rectToDraw.width, rectToDraw.height);
                rectToDraw.setBounds(x, y, width, height);
            } else {
                rectToDraw = new Rectangle(x, y, width, height);
            }
        }        
        
    }
    
    
    /**
     * Returns the viewer popup menu.
     */
    public JPopupMenu getJPopupMenu() {
        return popup;
    }
    
    /**
     * Creates the viewer popup menu.
     */
    private JPopupMenu createJPopupMenu() {
        JPopupMenu popup = new JPopupMenu();
        addMenuItems(popup);
        return popup;
    }  
    
   
    /**
     * Adds the viewer specific menu items.
     */
    private void addMenuItems(JPopupMenu menu) {
        Listener listener = new Listener();
        JMenuItem menuItem;
        
        menuItem = new JMenuItem("Store cluster...", GUIFactory.getIcon("new16.gif"));
        menuItem.setEnabled(false);
        menuItem.setActionCommand(STORE_CLUSTER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Launch new session", GUIFactory.getIcon("launch_new_mav.gif"));
        menuItem.setEnabled(false);
        menuItem.setActionCommand(LAUNCH_NEW_SESSION_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Save cluster...", GUIFactory.getIcon("save16.gif"));
        menuItem.setEnabled(false);
        menuItem.setActionCommand(SAVE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        //menu.addSeparator();
           
        if (!this.geneViewer) {
            menu.addSeparator();
            menuItem = new JCheckBoxMenuItem("Show sample names");
            menuItem.setEnabled(true);
            menuItem.setActionCommand(DISPLAY_EXPT_NAMES_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);          
        }        
        
        menuItem = new JCheckBoxMenuItem("Larger point size");
        menuItem.setEnabled(true);
        menuItem.setActionCommand(SHOW_LARGER_POINTS_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);           
        
        menuItem = new JCheckBoxMenuItem("Show tick marks and labels");
        menuItem.setEnabled(true);
        menuItem.setSelected(true);
        menuItem.setActionCommand(SHOW_TICK_LABELS_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);        
        
    }   
    
    /**
     * Returns a menu item by specified action command.
     */
    private JMenuItem getJMenuItem(String command) {
        JMenuItem item;
        Component[] components = popup.getComponents();
        for (int i=0; i<components.length; i++) {
            if (components[i] instanceof JMenuItem) {
                if (((JMenuItem)components[i]).getActionCommand().equals(command))
                    return(JMenuItem)components[i];
            }
        }
        return null;
    }    
    
    /**
     * Sets a menu item state.
     */
    private void setEnableMenuItem(String command, boolean enable) {
        JMenuItem item = getJMenuItem(command);
        if (item == null) {
            return;
        }
        item.setEnabled(enable);
    }    
    
   /**
     * Saves selected genes.
     */
    private void onSave() {
        try {
            if(this.geneViewer)
                ExperimentUtil.saveExperiment(frame, experiment, data, getSelectedPoints());
            else 
                ExperimentUtil.saveExperimentCluster(frame, experiment, data, getSelectedPoints());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save matrix!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
   
    /**
     * Stores the selected cluster
     */
    private void storeCluster(){
        if(this.geneViewer)
            framework.storeSubCluster(getSelectedPoints(), experiment, Cluster.GENE_CLUSTER);
        else 
            framework.storeSubCluster(getSelectedPoints(), experiment, Cluster.EXPERIMENT_CLUSTER);
        //content.setSelection(false);
        //onHideSelection(); 
        this.onDataChanged(this.data);
        this.repaint();
        //content.updateScene();        
    }
    
    /**
     * Launches a new MultipleArrayViewer using selected elements
     */
    private void launchNewSession(){
        if(this.geneViewer)
            framework.launchNewMAV(getSelectedPoints(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.GENE_CLUSTER);
        else 
            framework.launchNewMAV(getSelectedPoints(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.EXPERIMENT_CLUSTER);        
    }
    
    /**
     * Handles the selection box state.
     */
    private void onShowSelection() {
       // if ((geneOrExpt == COAGUI.GENES) || (geneOrExpt == COAGUI.EXPTS)) {
            JMenuItem saveClusterItem = getJMenuItem(SAVE_CMD);
            JMenuItem storeClusterItem = getJMenuItem(STORE_CLUSTER_CMD);
            JMenuItem launchNewItem = getJMenuItem(LAUNCH_NEW_SESSION_CMD);
            if (!ellipse.isEmpty()) {
                //content.setSelection(true);
                //content.setSelectionBox(!hideBoxItem.isSelected());
                //selectionAreaItem.setEnabled(true);
                saveClusterItem.setEnabled(true);
                //hideBoxItem.setEnabled(true);
                storeClusterItem.setEnabled(true);
                launchNewItem.setEnabled(true);
            } else {
                //content.setSelection(false);
                //content.setSelectionBox(false);
                //selectionAreaItem.setEnabled(false);
                saveClusterItem.setEnabled(false);
                //hideBoxItem.setEnabled(false);
                storeClusterItem.setEnabled(false);
                launchNewItem.setEnabled(false);
            }
        //} 
        //content.updateScene();
    }  
    
    private void showExptNames() {
        displayExptNames  = !(displayExptNames);
        this.repaint();
    }
    
    private void displayLargePoints() {
        showLargePoints = !(showLargePoints);
        this.repaint();
    }
    
    private void displayTickLabels() {
        showTickLabels = !(showTickLabels);
        this.repaint();
    }
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return -1;
    }
    
    /**
     * The listener to listen to menu items events.
     */
    private class Listener extends MouseAdapter implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (command.equals(SAVE_CMD)) {
                onSave();
            } else if (command.equals(STORE_CLUSTER_CMD)){
                storeCluster();
            } else if (command.equals(LAUNCH_NEW_SESSION_CMD)){
                launchNewSession();
            } else if (command.equals(DISPLAY_EXPT_NAMES_CMD)) {
                showExptNames();
            } else if (command.equals(SHOW_LARGER_POINTS_CMD)) {
                displayLargePoints();
            } else if (command.equals(SHOW_TICK_LABELS_CMD)) {
                displayTickLabels();
            }
        }        
    }    
    

	/**
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperiment(org.tigr.microarray.mev.cluster.gui.Experiment)
	 */
	public void setExperiment(Experiment e) {
		this.experiment = e;
		this.exptID = experiment.getId();
	}

	/**
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExperimentID()
	 */
	public int getExperimentID() {
		return this.exptID;
	}

	/**
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperimentID(int)
	 */
	public void setExperimentID(int id) {
		this.exptID = id;
	}    
}
