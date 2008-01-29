/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * COA2DViewer.java
 *
 * Created on October 11, 2004, 3:24 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.coa;

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
public class COA2DViewer extends JPanel implements IViewer {
    
    private static final String SAVE_CMD    = "save-cmd";
    private static final String SAVE_GENE_CLUSTER_CMD    = "save-genes-cmd";
    private static final String SAVE_EXPT_CLUSTER_CMD    = "save-expts-cmd";
    private static final String SHOW_TEXT_CMD = "show-text-cmd";
    private static final String SHOW_TICK_LABELS_CMD = "show-tick-labels-cmd";    
    private static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
    private static final String STORE_GENE_CLUSTER_CMD = "store-gene-cluster-cmd";
    private static final String STORE_EXPT_CLUSTER_CMD = "store-expt-cluster-cmd";
    private static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";
    private static final String LAUNCH_NEW_GENE_SESSION_CMD = "launch-new-gene-session-cmd";
    private static final String LAUNCH_NEW_EXPT_SESSION_CMD = "launch-new-expt-session-cmd";   
    private static final String DISPLAY_EXPT_NAMES_CMD = "display-expt-names-cmd";
    private static final String SHOW_LARGER_POINTS_CMD = "show-larger-points-cmd";
    
    private float[] yArray, xArray;     
    //int originX, originY;
    private int geneOrExpt, axis1, axis2;  
    private FloatMatrix UMatrix, geneUMatrix, exptUMatrix, scaledGeneUMatrix, scaledExptUMatrix;
    private Experiment experiment; 
    private IFramework framework;
    private Frame frame;
    private IData data; 
    private JPopupMenu popup; 
    private Ellipse2D.Double ellipse;
    private boolean displayExptNames, showLargePoints, showTickLabels;
    Rectangle currentRect = null;
    Rectangle rectToDraw = null;
    Rectangle previousRectDrawn = new Rectangle();    
    private int exptID = 0;
    
    /** Creates a new instance of COA2DViewer */
    public COA2DViewer(Experiment experiment, float[] xArray, float[] yArray, int geneOrExpt, int axis1, int axis2) {
       this.yArray = yArray;
        this.xArray = xArray;
        this.displayExptNames = false;
        this.showLargePoints = false;
        this.showTickLabels = true;        
        this.geneOrExpt = geneOrExpt;
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
    
    /** Creates a new instance of COA2DViewer */
    public COA2DViewer(Experiment experiment, FloatMatrix UMatrix, int geneOrExpt, int axis1, int axis2) {
        this.UMatrix = UMatrix;
        this.axis1 = axis1;
        this.axis2 = axis2;        
        this.xArray = getFloatArray(UMatrix, axis1);
        this.yArray = getFloatArray(UMatrix, axis2);
        this.displayExptNames = false;
        this.showLargePoints = false;
        this.showTickLabels = true;        
        this.geneOrExpt = geneOrExpt;
        this.experiment = experiment;
        this.exptID = experiment.getId();
        this.ellipse = new Ellipse2D.Double();
        this.setBackground(Color.white); 
        
        popup = createJPopupMenu();
        
        GraphListener graphListener = new GraphListener();
        addMouseListener(graphListener);
        addMouseMotionListener(graphListener);        
    }    
    
    public COA2DViewer(Experiment experiment, FloatMatrix geneUMatrix, FloatMatrix exptUMatrix, int geneOrExpt, int axis1, int axis2) {
        this.geneUMatrix = geneUMatrix;
        this.exptUMatrix = exptUMatrix;
        //scaleMatrices(geneUMatrix, exptUMatrix);
        scaledGeneUMatrix = (FloatMatrix)(geneUMatrix.clone());
        scaledExptUMatrix = (FloatMatrix)(exptUMatrix.clone());        
        this.UMatrix = combineMatrices(scaledGeneUMatrix, scaledExptUMatrix);
        this.axis1 = axis1;
        this.axis2 = axis2;        
        this.xArray = getFloatArray(UMatrix, axis1);
        this.yArray = getFloatArray(UMatrix, axis2);
        this.displayExptNames = false;
        this.showLargePoints = false;
        this.showTickLabels = true;
        this.geneOrExpt = geneOrExpt;
        this.experiment = experiment;
        this.exptID = experiment.getId();
        this.ellipse = new Ellipse2D.Double();
        this.setBackground(Color.white);  
        
        popup = createJPopupMenu();
        
        GraphListener graphListener = new GraphListener();
        addMouseListener(graphListener);
        addMouseMotionListener(graphListener);        
    }
    /**
     * XMLEncoder/Decoder constructor
     * @param exptID
     * @param geneUMatrix
     * @param expUMatrix
     * @param xArray
     * @param yArray
     * @param geneOrExpt
     * @param axis1
     * @param axis2
     */
    public COA2DViewer(Experiment e, FloatMatrix geneUMatrix, FloatMatrix expUMatrix, float[] xArray, float[] yArray, Integer geneOrExpt, Integer axis1, Integer axis2) {
        this.xArray = xArray;
        this.yArray = yArray;
        this.axis1 = axis1.intValue();
        this.axis2 = axis2.intValue();        
        this.displayExptNames = false;
        this.showLargePoints = false;
        this.showTickLabels = true;
        this.geneOrExpt = geneOrExpt.intValue();
        if(geneUMatrix != null)
        	this.geneUMatrix = geneUMatrix;
        if(expUMatrix != null)
        	this.exptUMatrix = expUMatrix;
        if (this.geneOrExpt == COAGUI.BOTH) {
            scaledGeneUMatrix = (FloatMatrix)(geneUMatrix.clone());
            scaledExptUMatrix = (FloatMatrix)(exptUMatrix.clone());   
	    }     
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
    			new Object[]{this.experiment, geneUMatrix, exptUMatrix, xArray, yArray, new Integer(geneOrExpt), new Integer(axis1), new Integer(axis2)});
    }
    
	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperiment(org.tigr.microarray.mev.cluster.gui.Experiment)
	 */
	public void setExperiment(Experiment e) {
		this.experiment = e;
		this.exptID = experiment.getId();
	}

    private void scaleMatrices(FloatMatrix U1, FloatMatrix U2) { //brings them both to the same scale for plotting
        float max1 = 0f;
        float max2 = 0f;
        final int rows1 = U1.getRowDimension();
        final int rows2 = U2.getRowDimension();
        for (int i = rows1; --i >= 0;) {
            max1 = Math.max(max1, Math.max(Math.max(Math.abs(U1.get(i, 0)), Math.abs(U1.get(i, 1))), Math.abs(U1.get(i, 2))));
        }
        for (int i = rows2; --i >= 0;) {
            max2 = Math.max(max2, Math.max(Math.max(Math.abs(U2.get(i, 0)), Math.abs(U2.get(i, 1))), Math.abs(U2.get(i, 2))));
        }  
        
        float max = Math.max(max1, max2);
        
        if (max1 == max2) {
            scaledGeneUMatrix = (FloatMatrix)(geneUMatrix.clone());
            scaledExptUMatrix = (FloatMatrix)(exptUMatrix.clone());
        } else if (max1 > max2) {
            scaledGeneUMatrix = (FloatMatrix)(geneUMatrix.clone());
            float scalingFactor = (float)(max1/max2);
            scaledExptUMatrix = exptUMatrix.times(scalingFactor);
        } else {
            scaledExptUMatrix = (FloatMatrix)(exptUMatrix.clone());
            float scalingFactor = (float)(max2/max1);
            scaledGeneUMatrix = geneUMatrix.times(scalingFactor);
        }
    }    
    
    private FloatMatrix combineMatrices(FloatMatrix U1, FloatMatrix U2) {
        FloatMatrix combinedMatrix = new FloatMatrix(U1.getRowDimension() + U2.getRowDimension(), U1.getColumnDimension());
        
        for (int i = 0; i < U1.getRowDimension(); i++) {
            for (int j = 0; j < U1.getColumnDimension(); j++) {
                combinedMatrix.A[i][j] =U1.A[i][j];
            }
        }
        int counter = U1.getRowDimension();
        for (int i = 0; i < U2.getRowDimension(); i++) {
            for (int j = 0; j < U2.getColumnDimension(); j++) {
                combinedMatrix.A[counter + i][j] = U2.A[i][j];
            }
        }
        return combinedMatrix; 
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
            //nf.setMaximumFractionDigits(2);
            
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
                g2D.drawString(nf.format((double)yIntervalArray[i]), this.getWidth()/2 + 10, this.getHeight()/2 - (int)Math.round(yIntervalArray[i]*yScalingFactor) );
            }
            
            for (int i = 1; i < yIntervalArray.length; i++) {
                g2D.drawString("-" + nf.format((double)yIntervalArray[i]), this.getWidth()/2 + 10, this.getHeight()/2 + (int)Math.round(yIntervalArray[i]*yScalingFactor) );
            }
        }
        //Color[] pointColor = new Color[xArray.length];
        
        if ((geneOrExpt == COAGUI.GENES) || (geneOrExpt == COAGUI.EXPTS)) {
            /*
            for (int i = 0; i < pointColor.length; i++) {
                pointColor[i] = Color.black; //just for now, will add cluster colors later
            }
             */
            //draw data points
            for (int i = 0; i < xArray.length; i++) {
                Color currPointColor = Color.black;
                if (geneOrExpt == COAGUI.GENES) {
                    currPointColor = this.data.getProbeColor(this.experiment.getGeneIndexMappedToData(i));
                    if (currPointColor == null) currPointColor = Color.black;
                } else if (geneOrExpt == COAGUI.EXPTS) {
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
            
        } else { // if (geneOrExpt == COAGUI.BOTH)
            /*
            for (int i = 0; i < geneUMatrix.getRowDimension(); i++) {
                pointColor[i] = Color.black; //just for now, will add cluster colors later
            }    
            for (int i = geneUMatrix.getRowDimension(); i < pointColor.length; i++) {
                pointColor[i] = Color.gray;
            }
             */
            //draw data points
            Color currPointColor = Color.black;
            for (int i = 0; i < geneUMatrix.getRowDimension(); i++) {
                 currPointColor = this.data.getProbeColor(this.experiment.getGeneIndexMappedToData(i));
                 if (currPointColor == null) currPointColor = Color.gray;
                g2D.setColor(currPointColor);
                drawPoint(g2D, xArray[i], yArray[i], getXScalingFactor(origMaxXValue, origMinXValue), getYScalingFactor(origMaxYValue, origMinYValue), 5);
                g2D.setColor(Color.gray);
                //g2D.drawOval((midX + expectedXArray[i]),
            }  
            //g2D.setColor(Color.lightGray);
            for (int i = geneUMatrix.getRowDimension(); i < xArray.length; i++) {
                currPointColor = this.data.getExperimentColor(i - geneUMatrix.getRowDimension());
                if (currPointColor == null) currPointColor = Color.black;
                g2D.setColor(currPointColor);
                drawRectPoint(g2D, xArray[i], yArray[i], getXScalingFactor(origMaxXValue, origMinXValue), getYScalingFactor(origMaxYValue, origMinYValue), 8);
                g2D.setColor(Color.black);                
            }
            g2D.setColor(Color.black);
            g2D.drawString( "X axis = " + (axis1 + 1) + ", Y axis = " + (axis2 + 1), this.getWidth()/2 + 25, this.getHeight() - 25);            
        }
        
        // display expt names
        
        if (this.geneOrExpt == COAGUI.EXPTS) {
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
        
        if (this.geneOrExpt == COAGUI.BOTH) {
            if (this.displayExptNames) {
                FontMetrics fmet = g2D.getFontMetrics(g2D.getFont());
                double ascent = (double)(fmet.getAscent());
                double advance = (double)(fmet.getMaxAdvance());
                for (int i = geneUMatrix.getRowDimension(); i < xArray.length; i++) {
                    double currCoords[] = getCoords(xArray[i], yArray[i]);
                    String currName = data.getSampleName(i - geneUMatrix.getRowDimension());
                    double stringWidth = (double)(fmet.stringWidth(currName));
                    if ((currCoords[0] + 0.5d*advance + stringWidth) > (double)(this.getWidth())) {
                        g2D.drawString(currName, (float)(currCoords[0] - stringWidth - 0.25d*advance), (float)(currCoords[1] + 0.5d*ascent));
                    }
                    else {
                        g2D.drawString(currName, (float)(currCoords[0] + 0.5d*advance), (float)(currCoords[1] + 0.5d*ascent));
                    }
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
                if (geneOrExpt == COAGUI.GENES) {
                    selPointsVector.add(new Integer(experiment.getGeneIndexMappedToData(i)));
                } else if (geneOrExpt == COAGUI.EXPTS) {
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
    
    public int[] getSelectedGenesFromBoth() {
        Vector selPointsVector = new Vector();
        for (int i = 0; i < geneUMatrix.getRowDimension(); i++) {
            double[] currCoords = getCoords(xArray[i], yArray[i]);
            if (ellipse.contains(currCoords[0], currCoords[1])) {
                selPointsVector.add(new Integer(experiment.getGeneIndexMappedToData(i)));
            }
        }
        
        int[] selPoints = new int[selPointsVector.size()];
        for (int i = 0; i < selPoints.length; i++) {
            selPoints[i] = ((Integer)(selPointsVector.get(i))).intValue();
        }
        
        return selPoints;        
    }
    
    public int[] getSelectedExptsFromBoth() {
        Vector selPointsVector = new Vector();
        for (int i = geneUMatrix.getRowDimension(); i < UMatrix.getRowDimension(); i++) {
            double[] currCoords = getCoords(xArray[i], yArray[i]);
            if (ellipse.contains(currCoords[0], currCoords[1])) {
                selPointsVector.add(new Integer(experiment.getSampleIndex(i - geneUMatrix.getRowDimension())));
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
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return -1;
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
            COA2DViewer.this.repaint();
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
        
        if (this.geneOrExpt == COAGUI.BOTH) {
            menuItem = new JMenuItem("Store gene cluster...", GUIFactory.getIcon("new16.gif"));
            menuItem.setEnabled(false);
            menuItem.setActionCommand(STORE_GENE_CLUSTER_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
            
            menuItem = new JMenuItem("Launch new session with selected genes", GUIFactory.getIcon("launch_new_mav.gif"));
            menuItem.setEnabled(false);
            menuItem.setActionCommand(LAUNCH_NEW_GENE_SESSION_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
            
            menuItem = new JMenuItem("Save gene cluster...", GUIFactory.getIcon("save16.gif"));
            menuItem.setEnabled(false);
            menuItem.setActionCommand(SAVE_GENE_CLUSTER_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
            menu.addSeparator(); 
            
            menuItem = new JMenuItem("Store sample cluster...", GUIFactory.getIcon("new16.gif"));
            menuItem.setEnabled(false);
            menuItem.setActionCommand(STORE_EXPT_CLUSTER_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
            
            menuItem = new JMenuItem("Launch new session with selected samples", GUIFactory.getIcon("launch_new_mav.gif"));
            menuItem.setEnabled(false);
            menuItem.setActionCommand(LAUNCH_NEW_EXPT_SESSION_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
            
            menuItem = new JMenuItem("Save sample cluster...", GUIFactory.getIcon("save16.gif"));
            menuItem.setEnabled(false);
            menuItem.setActionCommand(SAVE_EXPT_CLUSTER_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
            //menu.addSeparator();            
        }
        else {
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
        }   
        if ((this.geneOrExpt == COAGUI.EXPTS)|| (this.geneOrExpt == COAGUI.BOTH)) {
            menu.addSeparator();
            menuItem = new JCheckBoxMenuItem("Show sample names");
            menuItem.setEnabled(true);
            menuItem.setActionCommand(DISPLAY_EXPT_NAMES_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);          
        }
        
        if ((this.geneOrExpt == COAGUI.EXPTS)|| (this.geneOrExpt == COAGUI.GENES)) {
            menuItem = new JCheckBoxMenuItem("Larger point size");
            menuItem.setEnabled(true);
            menuItem.setActionCommand(SHOW_LARGER_POINTS_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);              
        }
        
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
            if(geneOrExpt == COAGUI.GENES)
                ExperimentUtil.saveExperiment(frame, experiment, data, getSelectedPoints());
            else if (geneOrExpt == COAGUI.EXPTS)
                ExperimentUtil.saveExperimentCluster(frame, experiment, data, getSelectedPoints());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save matrix!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void onSaveGenesFromBoth() {
        try {
            ExperimentUtil.saveExperiment(frame, experiment, data, getSelectedGenesFromBoth());            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save matrix!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }    
    
    private void onSaveExptsFromBoth() {
        try {
            ExperimentUtil.saveExperimentCluster(frame, experiment, data, getSelectedExptsFromBoth());            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save matrix!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }    
    
    /**
     * Stores the selected cluster
     */
    private void storeCluster(){
        if(geneOrExpt == COAGUI.GENES)
            framework.storeSubCluster(getSelectedPoints(), experiment, Cluster.GENE_CLUSTER);
        else if (geneOrExpt == COAGUI.EXPTS)
            framework.storeSubCluster(getSelectedPoints(), experiment, Cluster.EXPERIMENT_CLUSTER);
        //content.setSelection(false);
        //onHideSelection(); 
        this.onDataChanged(this.data);
        this.repaint();
        //content.updateScene();        
    }
    
    private void storeGeneClusterFromBoth() {
        framework.storeSubCluster(getSelectedGenesFromBoth(), experiment, Cluster.GENE_CLUSTER);
        //content.setSelection(false);
        //onHideSelection(); 
        this.onDataChanged(this.data);
        this.repaint();
        //content.updateScene();        
    }  
    
    private void storeExptClusterFromBoth() {
        framework.storeSubCluster(getSelectedExptsFromBoth(), experiment, Cluster.EXPERIMENT_CLUSTER);     
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
        if(geneOrExpt == COAGUI.GENES)
            framework.launchNewMAV(getSelectedPoints(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.GENE_CLUSTER);
        else if (geneOrExpt == COAGUI.EXPTS)
            framework.launchNewMAV(getSelectedPoints(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.EXPERIMENT_CLUSTER);        
    }
    
    private void launchNewGeneSessionFromBoth() {
        framework.launchNewMAV(getSelectedGenesFromBoth(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.GENE_CLUSTER);
    }
    
    private void launchNewExptSessionFromBoth() {
       framework.launchNewMAV(getSelectedExptsFromBoth(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.EXPERIMENT_CLUSTER); 
    }    
    
    /**
     * Handles the selection box state.
     */
    private void onShowSelection() {
        if ((geneOrExpt == COAGUI.GENES) || (geneOrExpt == COAGUI.EXPTS)) {
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
        } else {
            JMenuItem saveGeneClusterItem = getJMenuItem(SAVE_GENE_CLUSTER_CMD);
            JMenuItem storeGeneClusterItem = getJMenuItem(STORE_GENE_CLUSTER_CMD);
            JMenuItem launchNewGeneItem = getJMenuItem(LAUNCH_NEW_GENE_SESSION_CMD);
            JMenuItem saveExptClusterItem = getJMenuItem(SAVE_EXPT_CLUSTER_CMD);
            JMenuItem storeExptClusterItem = getJMenuItem(STORE_EXPT_CLUSTER_CMD);
            JMenuItem launchNewExptItem = getJMenuItem(LAUNCH_NEW_EXPT_SESSION_CMD);
            if (!ellipse.isEmpty()) {
                //content.setSelection(true);
                //content.setSelectionBox(!hideBoxItem.isSelected());
                //selectionAreaItem.setEnabled(true);
                saveGeneClusterItem.setEnabled(true);
                saveExptClusterItem.setEnabled(true);
                //hideBoxItem.setEnabled(true);
                storeGeneClusterItem.setEnabled(true);
                storeExptClusterItem.setEnabled(true);
                launchNewGeneItem.setEnabled(true);  
                launchNewExptItem.setEnabled(true);
            } else {
                //content.setSelection(false);
                //content.setSelectionBox(false);
                //selectionAreaItem.setEnabled(false);
                saveGeneClusterItem.setEnabled(false);
                saveExptClusterItem.setEnabled(false);
                //hideBoxItem.setEnabled(false);
                storeGeneClusterItem.setEnabled(false);
                storeExptClusterItem.setEnabled(false);
                launchNewGeneItem.setEnabled(false);  
                launchNewExptItem.setEnabled(false);                
            }
        }
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
    
    /**
     * The listener to listen to menu items events.
     */
    private class Listener extends MouseAdapter implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (command.equals(SAVE_GENE_CLUSTER_CMD)) {
                onSaveGenesFromBoth();
            } else if (command.equals(SAVE_EXPT_CLUSTER_CMD)) {
                onSaveExptsFromBoth();
            } else if (command.equals(SAVE_CMD)) {
                onSave();
            } else if (command.equals(STORE_CLUSTER_CMD)){
                storeCluster();
            } else if (command.equals(STORE_GENE_CLUSTER_CMD)) {
                storeGeneClusterFromBoth();
            } else if (command.equals(STORE_EXPT_CLUSTER_CMD)) {
                storeExptClusterFromBoth();
            } else if (command.equals(LAUNCH_NEW_SESSION_CMD)){
                launchNewSession();
            } else if (command.equals(LAUNCH_NEW_GENE_SESSION_CMD)) {
                launchNewGeneSessionFromBoth();
            } else if (command.equals(LAUNCH_NEW_EXPT_SESSION_CMD)) {
                launchNewExptSessionFromBoth();
            } else if (command.equals(DISPLAY_EXPT_NAMES_CMD)) {
                showExptNames();
            } else if (command.equals(SHOW_LARGER_POINTS_CMD)) {
                displayLargePoints();
            } else if (command.equals(SHOW_TICK_LABELS_CMD)) {
                displayTickLabels();
            }
        }        
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
    
}
