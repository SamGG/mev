/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: GDMGeneViewer.java,v $
 * $Revision: 1.12 $
 * $Date: 2007-08-24 21:46:05 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.gdm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.util.SlideDataSorter;
import org.tigr.util.FloatMatrix;
import org.tigr.util.QSort;

public class GDMGeneViewer extends JPanel implements IViewer {
    
    private JPanel content;
    private GDMGeneHeader geneColumnHeaderSP;
    private GDMGeneHeader geneRowHeaderSP;
    private JScrollBar upperRightCornerSB;
    private JScrollBar lowerLeftCornerSB;
    
    private FloatMatrix geneDistMatrix;
    private FloatMatrix rawMatrix;
    
    private IData expData;
    private String [] fieldNames;
    private IFramework framework;
    private JFrame mainframe;
    
    private static final int TRACE_SPACE = 50;
    private static final int MAX_MATRIX_WIDTH = 400;
    private static final int MAX_COL_WIDTH = MAX_MATRIX_WIDTH;
    private static final int MAX_COL_HEIGHT = 500;
    private static final int MAX_ROW_WIDTH = MAX_COL_HEIGHT;
    private static final int MAX_ROW_HEIGHT = MAX_COL_WIDTH;
    private static final int NOT_UPDATE_ANNOTATION_SIZE = -1;
    
    private int num_genes;
    private int maxGeneNameLength;
    private int probes;
    private int featuresCount;
    private int [] indices;
    private int label;
    private int [] clusterlength;
    
    private Insets insets;
    
    private int elementWidth;
    private int paletteStyle;
    private int labelIndex = -1;
    private int maxLabelWidth = 0;
    private boolean isGRScale;
    private boolean isDrawBorders;
    private boolean isDrawClusterBorders=true;
    private boolean isAntiAliasing = true;
    private boolean isTracing = false;
    private int tracespace;
    private int xWidth;
    private int xHeight;
    private Dimension elementSize = new Dimension(10,10);
    private boolean sortByGeneProximity = true;
    
    private boolean imposeClusterOrder = false;
    
    public static Color zeroColor = Color.black;
    public static Color NaNColor = Color.gray;
    public static Color diagColor = Color.white;
    
    private float maxValue;
    private float minValue;
    private float origMaxValue;
    private float origMinValue;
    
    private int colorScheme = IDisplayMenu.GREEN_RED_SCHEME;
    private BufferedImage negGreenColorImage = createGradientImage(Color.green, Color.black);
    private BufferedImage posRedColorImage = createGradientImage(Color.black, Color.red);
    private BufferedImage negBlueColorImage = createGradientImage(Color.blue, Color.black);
    private BufferedImage posYellowColorImage = createGradientImage(Color.black, Color.yellow);
    private BufferedImage negCustomColorImage;
    private BufferedImage posCustomColorImage;
    
    private BufferedImage posColorImage = posRedColorImage;
    private BufferedImage negColorImage = negGreenColorImage;
    private Color borderColor;
    private Color clusterBorderColor;
    
    private String distanceMetric;
    private JPopupMenu popup;
    private JCheckBoxMenuItem drawClusterBorderItem;
    private JMenu sortMenu;
    
    private int displayEvery = 1;
    
    private Experiment experiment;
    private int [][] clusters;
    private int numOfClusters;
    
    private Listener listener;
    
    private HashMap actions = new HashMap();
    
    private static final String PARAMETER = "command-parameter";
    private static final String BORDER_COLOR_CMD = "select-border-color-cmd";
    private static final String COLOR_SCALE_CMD = "set-color-scale-cmd";
    private static final String GREEN_RED_COLOR_SCHEME_CMD = "display-green-red-scheme-cmd";
    private static final String BLUE_YELLOW_COLOR_SCHEME_CMD = "display-blue-yellow-scheme-cmd";
    private static final String NO_LABEL = "No Label";
    private static final String LABEL_BY = "Label by ";
    private static final String DISPLAY_LABEL_ACTION = "display--label-action";
    private static final String DISPLAY_LABEL_CMD    = "display-label-cmd";
    private static final String CUSTOM_COLOR_SCHEME_CMD = "display-custom-color-scheme-cmd";
    private static final String DISPLAY_DRAW_BORDERS_CMD = "display-draw-borders-cmd";
    private static final String DISPLAY_2X2_CMD = "display-2x2-cmd";
    private static final String DISPLAY_5X5_CMD = "display-5x5-cmd";
    private static final String DISPLAY_10X10_CMD = "display-10x10-cmd";
    private static final String DISPLAY_15X15_CMD = "display-15x15-cmd";
    private static final String DISPLAY_OTHER_CMD = "display-other-cmd";
    private static final String SET_CLUSTER_BORDER_CMD = "set-cluster-border-cmd";
    private static final String SORT_BY = "Sort by ";
    private static final String SORT_BY_CLUSTER_ORDER_CMD = "sort-by-cluster-order-cmd";
    private static final String SORT_BY_LOCATION_CMD = "sort-by-location-cmd";
    private static final String SORT_BY_RATIO_CMD = "sort-by-ratio-cmd";
    private static final String SORT_LABEL_ACTION = "sort-label-action";
    private static final String SORT_LABEL_CMD    = "sort-label-cmd";
    public static final String SORT_BY_GENE_PROXIMITY_CMD = "sort-by-gene-proximity-cmd";
    private static final String ANNOTATION_WIDTH_ACTION = "annotation-width-action";
    private static final String ANNOTATION_WIDTH_CMD = "annotation-width-cmd";
    private static final String CHANGE_ANNOTATION_WIDTH = "Change Annotation Width";
    private static final String TOGGLE_PROXIMITY_SORT_CMD = "Toggle-proximity-cmd";
    private static final String SAVE_NEIGHBORS_CMD = "Save-neighbors-cmd";
    private static final String SAVE_MATRIX_CMD = "save-matrix";    

    

    
    /**
     * Constructs a <code>GDMGeneViewer</code> for specified results.
     */
    public GDMGeneViewer(IFramework fmwk, AlgorithmData aData, String distMetric, int displayEvery, int [][] clusters, int numOfClusters) {
        setBackground(Color.white);
        
        this.framework = fmwk;
        this.distanceMetric = distMetric;
        
        IDisplayMenu menu = framework.getDisplayMenu();
        setElementWidth(elementSize.width);
        
        this.expData = fmwk.getData();
        this.fieldNames = this.expData.getFieldNames();
        this.experiment = expData.getExperiment();
        
        //System.out.println("new GDMGeneViewer has exptID " + exptID);
        this.probes = expData.getFeaturesSize();
        this.featuresCount = expData.getFeaturesCount();
        
        this.geneDistMatrix = aData.getMatrix("gdMatrix");
        this.rawMatrix = aData.getMatrix("rawMatrix");
        
        AlgorithmParameters params = aData.getParams();
        
        this.minValue = params.getFloat("minDist");
        this.origMaxValue = maxValue;
        this.origMinValue = minValue;
        this.displayEvery = displayEvery;
        
        this.clusters = clusters;
        this.numOfClusters = numOfClusters;
        
        this.maxValue = params.getFloat("maxDist");
        this.maxGeneNameLength = params.getInt("maxGeneNameLength");
        
        this.num_genes = geneDistMatrix.getRowDimension()/displayEvery;
        
        this.borderColor = Color.black;
        this.clusterBorderColor = Color.white;
        
        this.insets = new Insets(1, 1, 1, 1);
        
        xWidth = getXSize();
        xHeight = getYSize();
        
        if(this.displayEvery==1) {
            setIndices(createIndices());
        } else if (this.displayEvery > 1) {
            setIndices(createIndices(this.displayEvery));
        }
        
        listener = new Listener();
        addMouseMotionListener(listener);
        addKeyListener(listener);
        
        initLabelActions(expData.getFieldNames());
        initSortActions(expData.getFieldNames());
        initAnnotationWidthActions();
        
        this.geneColumnHeaderSP = createHeader(TRACE_SPACE, true, xWidth, MAX_COL_HEIGHT, elementSize, experiment);
        this.geneColumnHeaderSP.setBorder(BorderFactory.createLineBorder(Color.white));
        
        this.geneRowHeaderSP = createHeader(TRACE_SPACE, false, MAX_ROW_WIDTH, xHeight, elementSize, experiment);
        this.content = createContent(MAX_MATRIX_WIDTH, MAX_MATRIX_WIDTH, listener);
        
        this.geneColumnHeaderSP.setMatrixListener(listener);
        this.geneRowHeaderSP.setMatrixListener(listener);
        
        this.upperRightCornerSB = createScrollBar(JScrollBar.VERTICAL);
        this.lowerLeftCornerSB = createScrollBar(JScrollBar.HORIZONTAL);
        
        setMaxWidth(content, geneColumnHeaderSP);
        setMaxHeight(content, geneRowHeaderSP);
        
        geneColumnHeaderSP.setPosColorImages(posColorImage);
        geneRowHeaderSP.setPosColorImages(posColorImage);
        
        geneColumnHeaderSP.setIndices(indices);
        geneRowHeaderSP.setIndices(indices);
        
        add(content);
        
        // Create a "pop-up" context menu for GDM Viewer
        popup = createJPopupMenu(listener);
        
        getContentComponent().addMouseListener(listener);
        
        setBackground(Color.white);
        setOpaque(true);
    }
    
    public GDMGeneViewer() {}

    /**
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExpression()
	 */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{this.experiment, geneDistMatrix, rawMatrix, 
    			new Integer(probes), new Integer(featuresCount), new Float(minValue), distanceMetric,
				new Float(origMaxValue), new Float(origMinValue), new Float(maxValue), new Integer(maxGeneNameLength), 
				fieldNames, 
				new Integer(displayEvery), clusters, new Integer(numOfClusters)});
    }
    
	/**
	 * 
     * Creates a new GDMExpViewer from saved state data.  
     * setExperiment must be called for the viewer to be fully initialized.
	 * @param exptID
	 * @param geneDistMatrix
	 * @param rawMatrix
	 * @param probes
	 * @param featuresCount
	 * @param minValue
	 * @param distMetric
	 * @param origMaxValue
	 * @param origMinValue
	 * @param maxValue
	 * @param maxGeneNameLength
	 * @param fieldNames
	 * @param displayEvery
	 * @param clusters
	 * @param numOfClusters
	 */
    public GDMGeneViewer(Experiment e, FloatMatrix geneDistMatrix, FloatMatrix rawMatrix, 
    		Integer probes, Integer featuresCount, Float minValue, String distMetric, 
			Float origMaxValue, Float origMinValue, Float maxValue, Integer maxGeneNameLength, 
			String[] fieldNames, 
			Integer displayEvery, int [][] clusters, Integer numOfClusters) {
        setBackground(Color.white);
        this.distanceMetric = distMetric;
        setElementWidth(elementSize.width);
        
        this.fieldNames = fieldNames;
        this.probes = probes.intValue();
        this.featuresCount = featuresCount.intValue();
        
        this.geneDistMatrix = geneDistMatrix;
        this.rawMatrix = rawMatrix;
        
        this.minValue = minValue.floatValue();
        this.origMaxValue = origMaxValue.floatValue();
        this.origMinValue = origMinValue.floatValue();
        this.displayEvery = displayEvery.intValue();
        
        this.clusters = clusters;
        this.numOfClusters = numOfClusters.intValue();
        
        this.maxValue = maxValue.floatValue();
        this.maxGeneNameLength = maxGeneNameLength.intValue();
        
        this.num_genes = geneDistMatrix.getRowDimension()/(displayEvery.intValue());
        
        this.borderColor = Color.black;
        this.clusterBorderColor = Color.white;
        
        this.insets = new Insets(1, 1, 1, 1);
        
        xWidth = getXSize();
        xHeight = getYSize();
        
        if(this.displayEvery==1) {
            setIndices(createIndices());
        } else if (this.displayEvery > 1) {
            setIndices(createIndices(this.displayEvery));
        }
		this.experiment = e;

        listener = new Listener();
        addMouseMotionListener(listener);
        addKeyListener(listener);
        
        this.geneColumnHeaderSP = createHeader(TRACE_SPACE, true, xWidth, MAX_COL_HEIGHT, elementSize, experiment);
        this.geneColumnHeaderSP.setBorder(BorderFactory.createLineBorder(Color.white));
        
        this.geneRowHeaderSP = createHeader(TRACE_SPACE, false, MAX_ROW_WIDTH, xHeight, elementSize, experiment);
        this.content = createContent(MAX_MATRIX_WIDTH, MAX_MATRIX_WIDTH, listener);
        
        this.geneColumnHeaderSP.setMatrixListener(listener);
        this.geneRowHeaderSP.setMatrixListener(listener);
        
        this.upperRightCornerSB = createScrollBar(JScrollBar.VERTICAL);
        this.lowerLeftCornerSB = createScrollBar(JScrollBar.HORIZONTAL);
        
        setMaxWidth(content, geneColumnHeaderSP);
        setMaxHeight(content, geneRowHeaderSP);
        
        geneColumnHeaderSP.setPosColorImages(posColorImage);
        geneRowHeaderSP.setPosColorImages(posColorImage);
        
        geneColumnHeaderSP.setIndices(indices);
        geneRowHeaderSP.setIndices(indices);
        
        add(content);
        
        // Create a "pop-up" context menu for GDM Viewer
        popup = createJPopupMenu(listener);
        
        getContentComponent().addMouseListener(listener);
        
        setBackground(Color.white);
        setOpaque(true);
        
    }
	/** 
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperiment(org.tigr.microarray.mev.cluster.gui.Experiment)
	 */
	public void setExperiment(Experiment e) {
		this.experiment = e;

    }
    
    /**
     * Initializes 'display/label' menu actions.
     */
    private void initLabelActions(String[] labels) {
        DefaultAction action = new DefaultAction(NO_LABEL, DISPLAY_LABEL_CMD);
        action.putValue(PARAMETER, String.valueOf(-1));
        actions.put(DISPLAY_LABEL_ACTION+String.valueOf(-1), action);
        for (int i=0; i<labels.length; i++) {
            action = new DefaultAction(LABEL_BY+labels[i], DISPLAY_LABEL_CMD);
            action.putValue(PARAMETER, String.valueOf(i));
            actions.put(DISPLAY_LABEL_ACTION+String.valueOf(i), action);
        }
    }
    
    /**
     * Initializes sorting actions.
     */
    private void initSortActions(String[] labels) {
        DefaultAction action;
        for (int i=0; i<labels.length; i++) {
            action = new DefaultAction(SORT_BY+labels[i], SORT_LABEL_CMD);
            action.putValue(PARAMETER, String.valueOf(i));
            actions.put(SORT_LABEL_ACTION+String.valueOf(i), action);
        }
    }
    
    /**
     * Initializes 'Annotation Width' menu actions.
     */
    private void initAnnotationWidthActions() {
        DefaultAction action = new DefaultAction(CHANGE_ANNOTATION_WIDTH, ANNOTATION_WIDTH_CMD);
        action.putValue(PARAMETER, String.valueOf(-1));
        actions.put(ANNOTATION_WIDTH_ACTION+String.valueOf(-1), action);
    }
    
    
    /**
     * Rturns an action by its name.
     */
    public Action getAction(String name) {
        return(Action)actions.get(name);
    }
    
    public void setMainFrame(JFrame mframe) {
        this.mainframe = mframe;
    }
    
    private void setIndices(int [] indexes) {
        this.indices = indexes;
    }
    
    private int [] getIndices() {
        return this.indices;
    }
    
    private int [] createIndices(int displayEvery) {
        
        int indicesSize = this.num_genes;
        int [] indices = new int [indicesSize];
        int i=0, total=0, index=0;
        
        for (index=0; index < this.probes; index++) {
            if ((total % this.displayEvery == 0) && (i<indicesSize)) {
                indices[i] = index;
                i++;
            }
            total++;
        }
        total = 0;
        if (this.numOfClusters > 0) {
            clusterlength = new int [numOfClusters];
            i=0;
            for (int j=0; j<this.numOfClusters; j++) {
                for(int k=0; k<clusters[j].length; k++) {
                    if ( (total % this.displayEvery == 0) && (i < indicesSize)) {
                        indices[i] = clusters[j][k];
                        i++;
                        clusterlength[j] ++;
                    }
                    total++;
                }
            }
        }
        
        return indices;
    }
    
    private int [] createIndices() {
        int [] indices = new int [this.num_genes];
        for (int i=0; i<indices.length; i++) {
            indices[i] = i;
        }
        
        
        if (this.numOfClusters > 0) {
            clusterlength = new int [this.num_genes];
            int i=0;
            
            for (int j=0; j<this.numOfClusters; j++) {
                for(int k=0; k<clusters[j].length; k++) {
                    indices[i] = clusters[j][k];
                    clusterlength[j] ++;
                    i++;
                }
            }
        }
        
        return indices;
    }
    
    private void setMaxWidth(JComponent content, JComponent header) {
        int c_width = content.getPreferredSize().width;
        int h_width = header.getPreferredSize().width;
        if (c_width > h_width) {
            header.setPreferredSize(new Dimension(c_width, header.getPreferredSize().height));
        } else {
            content.setPreferredSize(new Dimension(h_width, content.getPreferredSize().height));
        }
    }
    
    private void setMaxHeight(JComponent content, JComponent header) {
        int c_height = content.getPreferredSize().height;
        int h_height = header.getPreferredSize().height;
        if (c_height > h_height) {
            header.setPreferredSize(new Dimension(header.getPreferredSize().width, c_height));
        } else {
            content.setPreferredSize(new Dimension(content.getPreferredSize().width, h_height));
        }
    }
    
    /**
     * Returns a gradient image for positive values.
     */
    public BufferedImage getPosColorImage() {
        return posColorImage;
    }
    
    /**
     * Returns a gradient image for negative values.
     */
    public BufferedImage getNegColorImage() {
        return negColorImage;
    }
    
    
    /**
     * Returns a gradient image for positive values.
     */
    public void setPosColorImage(BufferedImage image) {
        this.posColorImage = image;
    }
    
    /**
     * Returns a gradient image for negative values.
     */
    public void setNegColorImage(BufferedImage image) {
        this.negColorImage = image;
    }
    
    public void setBorderColor(Color color) {
        this.borderColor = color;
    }
    
    private GDMGeneHeader createHeader(int tracespace, boolean colHdr,
    int width, int height, Dimension eSize, Experiment experiment) {
        
        GDMGeneHeader hdr = new GDMGeneHeader(this.insets, tracespace, colHdr, experiment, width,
        height, eSize, maxGeneNameLength, num_genes, getIndices());
        return hdr;
    }
    
    private JPanel createContent(int width, int height, Listener listener) {
        JPanel cPanel = new JPanel(new BorderLayout());
        cPanel.setBackground(Color.white);
        xWidth = getXSize();
        xHeight = getYSize();
        cPanel.setSize(xWidth+5, xHeight+5);
        cPanel.setPreferredSize(new Dimension(xWidth+5, xHeight+5));
        cPanel.setOpaque(true);
        cPanel.setVisible(true);
        return cPanel;
    }
    
    
    private JScrollBar createScrollBar(int orientation) {
        
        JScrollBar jsb = new JScrollBar(orientation);
        
        // VERTICAL Upper Right Corner ScrollBar for Column Header component
        if (orientation == JScrollBar.VERTICAL) {
            jsb.setModel(geneColumnHeaderSP.getVerticalScrollBar().getModel());
        } else {
            // HORIZONTAL Lower Left Corner ScrollBar for Row Header component
            jsb.setModel(geneRowHeaderSP.getHorizontalScrollBar().getModel());
        }
        return jsb;
    }
    
    /**
     * Returns index of top row.
     */
    private int getTopIndex(int top) {
        if (top<0) {
            return 0;
        }
        return (top-insets.top)/elementSize.height;
    }
    
    /**
     * Returns index of left column.
     */
    private int getLeftIndex(int left) {
        if (left<0) {
            return 0;
        }
        return (left-insets.left)/(elementSize.width+getSpacing());
    }
    
    /**
     * Returns index of right column.
     */
    private int getRightIndex(int right, int limit) {
        if (right<0) {
            return 0;
        }
        int result = (right-insets.left)/(elementSize.width+getSpacing())+1;
        return result > limit ? limit : result;
    }
    
    /**
     * Returns index of bottom row.
     */
    private int getBottomIndex(int bottom, int limit) {
        if (bottom<0) {
            return 0;
        }
        int result = (bottom-insets.top)/elementSize.height+1;
        return result > limit ? limit : result;
    }
    
    public void paint(Graphics g1D) {
        super.paint(g1D);
        if (num_genes == 0 || framework == null) { // empty data
            return;
        }
        
        Graphics2D g2D = (Graphics2D)g1D;
        
        if (isAntiAliasing) {//Anti-aliasing is on
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        
        drawColumns(g2D);
        
        if (isDrawClusterBorders && numOfClusters > 0 ) {
            drawClusterBorder(g2D);
        }
        
        drawPerimeter(g2D);
        
        return;
    }
    
    private void drawClusterBorder(Graphics2D g2d) {
        
        int xPosition = insets.left;
        int yPosition = insets.top;
        int width=0, height=0;
        
        Graphics g = (Graphics) g2d;
        
        g.setColor(clusterBorderColor);
        
        for (int i = 0; i < this.numOfClusters; i++) {
            height = this.clusterlength[i]* (this.elementSize.height + getSpacing());
            xPosition = insets.left;
            
            for (int j = 0; j < this.numOfClusters; j++) {
                width = this.clusterlength[j] * (this.elementSize.width + getSpacing());
                g.drawRect( xPosition, yPosition, width, height);
                xPosition += width;
            }
            yPosition += height;
        }
    }
    
    private void drawColumns(Graphics2D g) {
        
        Rectangle bounds = g.getClipBounds();
        final int top = getTopIndex(bounds.y);
        final int left = getLeftIndex(bounds.x);
        
        int bottom = 0;
        int right = 0;
        
        bottom = getBottomIndex(bounds.y+bounds.height, num_genes);
        right = getRightIndex(bounds.x+bounds.width, num_genes);
        
        for (int column = left; column < right; column++) {
            drawColumn(g, column, top, bottom);
        }
    }
    
    private void drawPerimeter(Graphics2D g){
        Color color = g.getColor();
        g.setColor(Color.black);
        if(this.isDrawClusterBorders && this.numOfClusters > 0)
            g.drawRect(0,0, getXSize()-insets.right+1, getYSize()-insets.bottom+1);
        else
            g.drawRect(0,0, getXSize()-insets.right, getYSize()-insets.bottom);
        g.setColor(color);
    }
    
    /**
     * Draws a specified column.
     */
    private void drawColumn(Graphics2D g, int column, final int top, final int bottom) {
        for (int row = top; row < bottom; row++) {
            drawSlideDataElement(g, row, column);
        }
    }
    
    /**
     * Draws an element rectangle to specified row and column.
     */
    private void drawSlideDataElement(Graphics g, final int row, final int column) {
        
        Color holdColor;
        
        float distance = geneDistMatrix.get(indices[row], indices[column]);
        if (Float.isNaN(distance)) {
            holdColor = NaNColor;
        } else if (distance == 0 && row == column) {
            holdColor = diagColor;
        } else if (distance == 0 && row != column) {
            holdColor = zeroColor;
        } else {
            holdColor = getColor(distance);
        }
        g.setColor(holdColor);
        g.fillRect( getXPos(column), getYPos(row), elementSize.width, elementSize.height);
        
        if (isDrawBorders && elementSize.width > 2) {
            g.setColor(borderColor);
            g.drawRect( getXPos(column), getYPos(row), elementSize.width -1, elementSize.height -1);
        }
    }
    
    /**
     * Creates a gradient image with specified initial colors.
     */
    private BufferedImage createGradientImage(Color color1, Color color2) {
        BufferedImage image = new BufferedImage(256, 1, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = image.createGraphics();
        GradientPaint gp = new GradientPaint(0, 0, color1, 255, 0, color2);
        graphics.setPaint(gp);
        graphics.drawRect(0, 0, 255, 1);
        return image;
    }
    
    private Color getColor(float value) {
        if (Float.isNaN(value) || posColorImage == null || negColorImage == null) {
            return NaNColor;
        }
        //float maximum = value < 0 ? this.minValue : this.maxValue;
        
        int colorIndex = (int)(255*(value-this.minValue)/this.maxValue);
        if(colorIndex > 255)
            colorIndex = 255;
        if(colorIndex < 0)
            colorIndex = 0;
        int rgb = value < 0 ? negColorImage.getRGB(255-colorIndex, 0) : posColorImage.getRGB(colorIndex, 0);
        
        return new Color(rgb);
    }
    
    private void setFontSize(int width) {
        if (width > 12)
            width = 12;
        setFont(new Font("monspaced", Font.PLAIN, width));
    }
    
    public void setElementWidth(int width) {
        this.elementWidth = width;
        setFontSize(width);
    }
    
    public int getElementWidth() {
        return this.elementWidth;
    }
    
    void setTracing(boolean isTracing) {
        this.isTracing = isTracing;
    }
    
    private int getSpacing() {
        if (isTracing)
            return tracespace;
        return 0;
    }
    
    public JComponent getContentComponent() {
        return this;
    }
    
    public JComponent getHeaderComponent() {
        return geneColumnHeaderSP;
    }
    
    public JComponent getColumnHeaderComponent() {
        return geneColumnHeaderSP;
    }
    
    public JComponent getRowHeaderComponent() {
        return geneRowHeaderSP;
    }
    
    public JComponent getUpperRightCornerSB() {
        return upperRightCornerSB;
    }
    
    public JComponent getLowerLeftCornerSB() {
        return lowerLeftCornerSB;
    }
    
    /**
     * Returns a width of the viewer.
     */
    private int getXSize() {
        int size = 0;
        size = (num_genes*elementSize.width)+((num_genes-1)*getSpacing())+insets.left+insets.right;
        
        return size;
    }
    
    /**
     * Returns a height of the viewer.
     */
    private int getYSize() {
        int size = 0;
        size = (num_genes*elementSize.height)+((num_genes-1)*getSpacing()+insets.top+insets.bottom);
        
        return size;
    }
    
    /**
     * Returns max label width.
     */
    private int getMaxGeneNameLength() {
        return maxGeneNameLength;
    }
    
    
    /**
     * Updates the viewer size.
     */
    private void updateSize(int annotationSize) {
        
        int width = getXSize();
        int height = getYSize();
        
        setSize(width, height);
        setPreferredSize(new Dimension(width, height));
        
        geneColumnHeaderSP.setNumGenes(num_genes);
        geneColumnHeaderSP.updateSize(annotationSize);
        
        geneRowHeaderSP.setNumGenes(num_genes);
        geneRowHeaderSP.updateSize(annotationSize);
        
        setMaxWidth(content, geneColumnHeaderSP);
        setMaxHeight(content, geneRowHeaderSP);
        
        geneColumnHeaderSP.setPosColorImages(posColorImage);
        geneRowHeaderSP.setPosColorImages(posColorImage);
        
        repaint();
    }
    
    public void onSelected(IFramework framework) {
        
        this.framework = framework;
        this.expData = framework.getData();
        
        onDataChanged(this.expData);
        
        this.probes = expData.getFeaturesSize();
        this.featuresCount = expData.getFeaturesCount();
        
        this.num_genes = geneDistMatrix.getRowDimension()/displayEvery;
        
        IDisplayMenu menu = framework.getDisplayMenu();
        setFontSize(elementSize.width);
        
        xWidth = getXSize();
        xHeight = getYSize();
        
        geneColumnHeaderSP.setData(this.expData);
        geneColumnHeaderSP.setContentWidth(xWidth+10);
        geneColumnHeaderSP.setElementWidth(elementSize.width);
        
        geneRowHeaderSP.setData(this.expData);
        geneRowHeaderSP.setContentHeight(xHeight+10);
        geneRowHeaderSP.setElementHeight(elementSize.height);
        
        geneColumnHeaderSP.repaint();
        geneRowHeaderSP.repaint();
        
        onMenuChanged(menu);
        onDataChanged(this.expData);
    }
    
    public void onMenuChanged(IDisplayMenu menu) {
        
        this.probes = expData.getFeaturesSize();
        this.featuresCount = expData.getFeaturesCount();
        this.num_genes = geneDistMatrix.getRowDimension()/displayEvery;
        
        paletteStyle = menu.getPaletteStyle();
        isGRScale = menu.isGRScale();
        if (menu.isTracing() == isTracing &&
        labelIndex == menu.getLabelIndex() &&
        isAntiAliasing == menu.isAntiAliasing()) {
            return;
        }
        
        isAntiAliasing = menu.isAntiAliasing();
        geneColumnHeaderSP.setAntiAliasing(isAntiAliasing);
        geneRowHeaderSP.setAntiAliasing(isAntiAliasing);
        labelIndex = menu.getLabelIndex();
        isTracing = menu.isTracing();
        setFont(new Font("monospaced", Font.BOLD, elementSize.height));
        
        xWidth = getXSize();
        xHeight = getYSize();
        
        geneColumnHeaderSP.setContentWidth(xWidth);
        geneColumnHeaderSP.setElementWidth(elementSize.width);
        geneColumnHeaderSP.setTracing(isTracing);
        
        geneRowHeaderSP.setContentHeight(xHeight);
        geneRowHeaderSP.setElementHeight(elementSize.height);
        geneRowHeaderSP.setTracing(isTracing);
        
        geneColumnHeaderSP.repaint();
        geneRowHeaderSP.repaint();
        
        updateSize(NOT_UPDATE_ANNOTATION_SIZE);
    }
    
    public void onDataChanged(IData data) {
        
        this.expData = data;
        
        this.probes = expData.getFeaturesSize();
        this.featuresCount = expData.getFeaturesCount();
        
        this.num_genes = geneDistMatrix.getRowDimension()/displayEvery;
        
        geneColumnHeaderSP.setData(data);
        geneRowHeaderSP.setData(data);
        
        updateSize(NOT_UPDATE_ANNOTATION_SIZE);
    }
    
    public void onDeselected() {
        return;
    }
    
    public void onClosed() {
        return;
    }
    
    public BufferedImage getImage() {
        return null;
    }
    
    /**
     * @return true, if specified row and column are exists.
     */
    private boolean isLegalPosition(int row, int column) {
        if (isLegalRow(row) && isLegalColumn(column)) {
            return true;
        }
        return false;
    }
    
    /**
     * @return true, if specified column is exists.
     */
    private boolean isLegalColumn(int column) {
        if (column < 0 || column > num_genes -1) {
            return false;
        }
        return true;
    }
    
    /**
     * @return true, if specified row is exists.
     */
    private boolean isLegalRow(int row) {
        if (row < 0 || row > num_genes -1) {
            return false;
        }
        return true;
    }
    
    /**
     * Finds column by specified x coordinate.
     * @return -1 if column was not found.
     */
    private int findColumn(int targetx) {
        int columnSize = elementSize.width + getSpacing();
        
        if (targetx > num_genes*columnSize - getSpacing() + insets.left || targetx < this.insets.left) {
            return -1;
        } else {
            return ((targetx)/columnSize);
        }
    }
    
    /**
     * Finds row by specified y coordinate.
     * @return -1 if row was not found.
     */
    private int findRow(int targety) {
        int rowSize = num_genes*elementSize.height;
        if (targety > rowSize + insets.top || targety < insets.top) {
            return -1;
        } else {
            return (targety)/elementSize.height;
        }
    }
    
    /**
     * Returns x coordinate of a column.
     */
    private int getXPos(int column) {
        return column*(elementSize.width + getSpacing())+ insets.left;
    }
    
    /**
     * Returns y coordinate of a row.
     */
    private int getYPos(int row) {
        return row * elementSize.height + insets.top;
    }
    
    /**
     * Draws a rectangle with specified color.
     */
    private void drawColoredBoxAt(Graphics g, int row, int column, Color color) {
        g.setColor(color);
        g.drawRect(insets.left + column*(elementSize.width+getSpacing()), insets.top + row*elementSize.height, elementSize.width-1, elementSize.height-1);
    }
    
    public void displayGDMSpotInfo(int colIndex, int rowIndex, int column, int row) {
        new GDMGeneSpotInfoDisplay(mainframe, this.experiment, this.expData, geneDistMatrix, rawMatrix, distanceMetric, colIndex, rowIndex, column, row);
    }
    
    /**
     * Creates a check box menu item with specified name, acton command and state.
     */
    private JCheckBoxMenuItem createJCheckBoxMenuItem(String name, String command, Listener listener, boolean isSelected) {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
        item.setActionCommand(command);
        item.addActionListener(listener);
        item.setSelected(isSelected);
        return item;
    }
    
    /**
     * Creates a check box menu item with specified name and acton command.
     */
    private JCheckBoxMenuItem createJCheckBoxMenuItem(String name, String command, Listener listener) {
        return createJCheckBoxMenuItem(name, command, listener, false);
    }
    
    /**
     * Creates a radio button menu item with specified name, acton command and state.
     */
    private JRadioButtonMenuItem createJRadioButtonMenuItem(String name, String command, Listener listener, ButtonGroup buttonGroup, boolean isSelected) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
        item.setActionCommand(command);
        item.addActionListener(listener);
        item.setSelected(isSelected);
        if (buttonGroup != null) {
            buttonGroup.add(item);
        }
        return item;
    }
    
    /**
     * Creates a radio button menu item with specified name, acton command and button group.
     */
    private JRadioButtonMenuItem createJRadioButtonMenuItem(String name, String command, Listener listener, ButtonGroup buttonGroup) {
        return createJRadioButtonMenuItem(name, command, listener, buttonGroup, false);
    }
    
    
    private JPopupMenu createJPopupMenu(Listener listener) {
        JPopupMenu popup = new JPopupMenu();
        addMenuItems(popup, listener);
        return popup;
    }
    
    private void addMenuItems(JPopupMenu menu, Listener listener) {
        JMenuItem item;
        ButtonGroup buttonGroup;
        
        JMenu colorSchemeMenu = new JMenu("Color Scheme");
        buttonGroup = new ButtonGroup();
        colorSchemeMenu.add(createJRadioButtonMenuItem("Black/Red Scheme", GREEN_RED_COLOR_SCHEME_CMD, listener, buttonGroup, true));
        colorSchemeMenu.add(createJRadioButtonMenuItem("Black/Yellow Scheme",BLUE_YELLOW_COLOR_SCHEME_CMD, listener, buttonGroup));
        colorSchemeMenu.add(createJRadioButtonMenuItem("Custom Color Scheme", CUSTOM_COLOR_SCHEME_CMD, listener, buttonGroup));
        menu.add(colorSchemeMenu);
        menu.addSeparator();
        
        JMenu sizeMenu = new JMenu("Element Size");
        buttonGroup = new ButtonGroup();
        sizeMenu.add(createJRadioButtonMenuItem("2 x 2", DISPLAY_2X2_CMD, listener, buttonGroup));
        sizeMenu.add(createJRadioButtonMenuItem("5 x 5", DISPLAY_5X5_CMD, listener, buttonGroup));
        sizeMenu.add(createJRadioButtonMenuItem("10 x 10", DISPLAY_10X10_CMD, listener, buttonGroup, true));
        sizeMenu.add(createJRadioButtonMenuItem("15 x 15", DISPLAY_15X15_CMD, listener, buttonGroup));
        sizeMenu.add(createJRadioButtonMenuItem("Other", DISPLAY_OTHER_CMD, listener, buttonGroup));
        menu.add(sizeMenu);
        menu.addSeparator();
        
        menu.add(createJCheckBoxMenuItem("Draw Borders", DISPLAY_DRAW_BORDERS_CMD, listener));
        if (numOfClusters > 0) {
            drawClusterBorderItem = createJCheckBoxMenuItem("Draw Cluster Borders", SET_CLUSTER_BORDER_CMD, listener, true);
            menu.add(drawClusterBorderItem);
        }
        
        menu.addSeparator();
        
        item = new JMenuItem("Select Border Color");
        item.setActionCommand(BORDER_COLOR_CMD);
        item.addActionListener(listener);
        menu.add(item);
        menu.addSeparator();
        
        item = new JMenuItem("Set Color Scale");
        item.setActionCommand(COLOR_SCALE_CMD);
        item.addActionListener(listener);
        menu.add(item);
        menu.addSeparator();
        
        item = new JMenuItem("Toggle Sort on Proximity");
        item.setActionCommand(TOGGLE_PROXIMITY_SORT_CMD);
        item.addActionListener(listener);
        menu.add(item);
        
        item = new JMenuItem("Save k Neighbors");
        item.setActionCommand(SAVE_NEIGHBORS_CMD);
        item.addActionListener(listener);
        menu.add(item);
        
        item = new JMenuItem("Save Distance Matrix");
        item.setActionCommand(SAVE_MATRIX_CMD);
        item.addActionListener(listener);
        menu.add(item);
        
        menu.addSeparator();
        
        sortMenu = new JMenu("Sort");
        buttonGroup = new ButtonGroup();
        if(numOfClusters > 0) {
            sortMenu.add(createJRadioButtonMenuItem("Sort by Location", SORT_BY_LOCATION_CMD, listener, buttonGroup, false));
        } else {
            sortMenu.add(createJRadioButtonMenuItem("Sort by Location", SORT_BY_LOCATION_CMD, listener, buttonGroup, false));
        }
        
        addSortMenuItems(sortMenu, buttonGroup);
        
        if(numOfClusters > 0) {
            addSortClusterMenuItems(sortMenu, buttonGroup, listener);
        }
        menu.add(sortMenu);
        
        menu.addSeparator();
        
        item = new JMenuItem("Impose Cluster Result");
        item.setActionCommand("impose-cluster-order");
        item.addActionListener(listener);
        menu.add(item);
        
        menu.addSeparator();
        
        JMenu annotationMenu = new JMenu("Change Annotation");
        buttonGroup = new ButtonGroup();
        addLabelMenuItems(annotationMenu, buttonGroup);
        menu.add(annotationMenu);
        
        menu.addSeparator();
        item = new JMenuItem("Change annotation Width");
        item.setActionCommand(ANNOTATION_WIDTH_CMD);
        item.addActionListener(listener);
        menu.add(item);
    }
    
    /**
     * Adds label menu items.
     */
    
    private void addLabelMenuItems(JMenu menu, ButtonGroup buttonGroup){
        int index = -1;
        JRadioButtonMenuItem item;
        Action action;
        while ((action = getAction(DISPLAY_LABEL_ACTION+String.valueOf(index)))!=null) {
            item = new JRadioButtonMenuItem(action);
            buttonGroup.add(item);
            if (index < 1) {
                item.setSelected(true);
                String indexStr = (String)action.getValue(PARAMETER);
                label = Integer.parseInt(indexStr);
            }
            menu.add(item);
            index++;
        }
    }
    
    /**
     * Adds sort menu items.
     */
    
    private void addSortMenuItems(JMenu menu, ButtonGroup buttonGroup) {
        int index = 0;
        JRadioButtonMenuItem item;
        Action action;
        while ((action = getAction(SORT_LABEL_ACTION+String.valueOf(index)))!=null) {
            item = new JRadioButtonMenuItem(action);
            buttonGroup.add(item);
            menu.add(item);
            index++;
        }
    }
    
    private void addSortClusterMenuItems(JMenu menu, ButtonGroup buttonGroup, Listener listener) {
        menu.add(createJRadioButtonMenuItem("Sort by Cluster Number", SORT_BY_CLUSTER_ORDER_CMD, listener, buttonGroup, true));
    }
    
    /**
     * Sets the user specified spot size.
     */
    
    private void onElementSizeChanged(int width, int height) {
        this.elementSize = new Dimension(width, height);
        xWidth = getXSize();
        xHeight = getYSize();
        
        geneColumnHeaderSP.setContentWidth(xWidth);
        geneColumnHeaderSP.setElementWidth(elementSize.width);
        geneColumnHeaderSP.setElementHeight(elementSize.height);
        
        geneRowHeaderSP.setContentHeight(xHeight);
        geneRowHeaderSP.setElementWidth(elementSize.width);
        geneRowHeaderSP.setElementHeight(elementSize.height);
        
        geneColumnHeaderSP.setPosColorImages(posColorImage);
        geneRowHeaderSP.setPosColorImages(posColorImage);
        
        updateSize(NOT_UPDATE_ANNOTATION_SIZE);
        this.validate();
    }
    
    /**
     * Sets the user specified spot size.
     */
    private void onElementSizeChanged() {
        GDMElementSizeDialog dialog = new GDMElementSizeDialog(mainframe, elementSize);
        if (dialog.showModal() == JOptionPane.OK_OPTION) {
            Dimension size = dialog.getElementSize();
            onElementSizeChanged(size.width, size.height);
        }
    }
    
    private void onDrawBordersChanged(boolean state) {
        isDrawBorders = state;
        geneColumnHeaderSP.repaint();
        repaint();
    }
    
    private void onDrawClusterBorderChange(boolean state) {
        if (numOfClusters > 0 ) {
            isDrawClusterBorders = state;
            geneColumnHeaderSP.repaint();
            repaint();
        }
    }
    
    /**
     * Sets the color pallete colors
     */
    private void onColorSchemeChange(int scheme){
        
        
        if (this.colorScheme == scheme && scheme != IDisplayMenu.CUSTOM_COLOR_SCHEME) {
            return;
        } else {
            if (scheme == IDisplayMenu.GREEN_RED_SCHEME) {
                setPosColorImage(posRedColorImage);
                this.colorScheme = scheme;
            } else if (scheme == IDisplayMenu.BLUE_YELLOW_SCHEME) {
                setPosColorImage(posYellowColorImage);
                this.colorScheme = scheme;
            } else {  // custom scheme request
                GDMColorSelectionDialog dialog = new GDMColorSelectionDialog((Frame)mainframe, true, getPosColorImage());
                
                if(dialog.showModal() != JOptionPane.OK_OPTION)
                    return;
                setPosColorImage(dialog.getPositiveGradient());
                this.colorScheme = scheme;
            }
            
        }
        
        geneColumnHeaderSP.setPosColorImages(posColorImage);
        geneRowHeaderSP.setPosColorImages(posColorImage);
        
        geneColumnHeaderSP.repaint();
        geneRowHeaderSP.repaint();
        
        repaint();
    }
    
    private void onBorderColorChanged() {
        
        GDMBorderColorDialog dialog = new GDMBorderColorDialog((Frame)mainframe, true, borderColor);
        if (dialog.showModal() != JOptionPane.OK_OPTION)
            return;
        setBorderColor(dialog.getBorderColor());
        geneColumnHeaderSP.repaint();
        geneRowHeaderSP.repaint();
        repaint();
    }
    
    private void onColorScaleChanged() {
        
        BufferedImage grad = getPosColorImage();
        Color lowerColor = new Color(grad.getRGB(0,0));
        Color upperColor = new Color(grad.getRGB(grad.getWidth()-1, 0));
        
        GDMColorScaleDialog dialog = new GDMColorScaleDialog((Frame)mainframe, minValue, maxValue, geneDistMatrix, num_genes, lowerColor, upperColor);
        dialog.setGDMScaleListener(new ScaleListener());
        int res = dialog.showModal();
        minValue = dialog.getLowerLimit();  // if reset it will return the original values.
        maxValue = dialog.getUpperLimit();
        geneColumnHeaderSP.setValues(minValue, maxValue);
        geneRowHeaderSP.setValues(minValue, maxValue);
        geneColumnHeaderSP.updateSize(NOT_UPDATE_ANNOTATION_SIZE);
        geneRowHeaderSP.updateSize(NOT_UPDATE_ANNOTATION_SIZE);
        // geneRowHeaderSP.setContentH;
        
        revalidate();
        // geneColumnHeaderSP.repaint();
        // geneRowHeaderSP.repaint();
        // repaint();
    }
    
    private void onSortByClusterChange() {
        if (numOfClusters > 0) {
            isDrawClusterBorders=true;
            //  drawClusterBorderItem.setState(true);
            
            if(this.displayEvery==1) {
                setIndices(createIndices());
            } else if (this.displayEvery > 1) {
                setIndices(createIndices(this.displayEvery));
            }
            
            geneColumnHeaderSP.setIndices(indices);
            geneRowHeaderSP.setIndices(indices);
            
            onDataChanged(this.expData);
        }
    }
    
    private void toggleSortByGeneProximity(){
        sortByGeneProximity = !sortByGeneProximity;
        this.geneColumnHeaderSP.setSortByGeneProximity(sortByGeneProximity);
        this.geneRowHeaderSP.setSortByGeneProximity(sortByGeneProximity);
    }
    
    private void onSortByGeneProximity(int baseIndex) {
        
        this.isDrawClusterBorders = false;
        
        QSort qsort = new QSort(this.geneDistMatrix.A[baseIndex]);
        int [] sortedIndices = qsort.getOrigIndx();
        
        //to handle random placement of base if sorting on NaN or have a tie
        if(sortedIndices[0] != baseIndex){
            boolean notFound = true;
            for(int i=0; i<sortedIndices.length && notFound; i++){
                if(sortedIndices[i] == baseIndex){
                    sortedIndices[i] = sortedIndices[0];
                    sortedIndices[0] = baseIndex;
                    notFound = false;
                }
            }
        }
        
        setIndices(sortedIndices);
        
        geneColumnHeaderSP.setIndices(sortedIndices);
        geneRowHeaderSP.setIndices(sortedIndices);
        
        onDataChanged(this.expData);
        validate();
        geneColumnHeaderSP.repaint();
        geneRowHeaderSP.repaint();
    }
    
    private void clearSortSelection(){
        ButtonGroup group = new ButtonGroup();
        for(int i = 0; i < sortMenu.getMenuComponentCount(); i++){
            ((JRadioButtonMenuItem)(sortMenu.getMenuComponent(i))).setSelected(false);
            group.add((JRadioButtonMenuItem)(sortMenu.getMenuComponent(i)));
        }
    }
    
    /**
     * Invoked when a sort menu item is changed.
     */
    private void onSort(Action action) {
        String index = (String)action.getValue(PARAMETER);
        this.isDrawClusterBorders = false;
        onSort(Integer.parseInt(index));
    }
    
    /**
     * Sorts the framework data.
     */
    private void onSort(int style) {
        //set initial indices
        setIndices(createIndices(this.displayEvery));
        
        isDrawClusterBorders=false;
        if(drawClusterBorderItem != null)
            drawClusterBorderItem.setState(false);
        sortIndices(style);
        onDataChanged(expData);
    }
    
    private void sortIndices(int style) {
        SlideDataSorter sorter = new SlideDataSorter();
        sorter.setSlideData(expData.getFeature(0));
        
        int [] indexes = getIndices();
        sorter.sort(indexes, style);
        setIndices(indexes);
        
        geneColumnHeaderSP.setIndices(indexes);
        geneRowHeaderSP.setIndices(indexes);
        
        geneColumnHeaderSP.setPosColorImages(posColorImage);
        geneRowHeaderSP.setPosColorImages(posColorImage);
    }
    
    /**
     * Sets the user specified label order.
     */
    
    private void onLabelChanged(Action action){
        String index = (String)action.getValue(PARAMETER);
        
        setLabelIndex(Integer.parseInt(index));
        
        geneColumnHeaderSP.setLabelIndex(label);
        geneRowHeaderSP.setLabelIndex(label);
        
        onDataChanged(expData);
        
        this.geneColumnHeaderSP.setPosColorImages(posColorImage);
        this.geneRowHeaderSP.setPosColorImages(posColorImage);
        
        geneColumnHeaderSP.updateSize(NOT_UPDATE_ANNOTATION_SIZE);
        geneRowHeaderSP.updateSize(NOT_UPDATE_ANNOTATION_SIZE);
        
        geneColumnHeaderSP.repaint();
        geneRowHeaderSP.repaint();
        
        updateSize(NOT_UPDATE_ANNOTATION_SIZE);
        repaint();
    }
    
    /**
     * Sets the user specified spot size.
     */
    private void onAnnotationWidthChanged() {
        
        GDMAnnotationSizeDialog dialog = new GDMAnnotationSizeDialog(mainframe);
        
        if (dialog.showModal() == JOptionPane.OK_OPTION) {
            int annotationSize = dialog.getAnnotationSize();
            
            geneColumnHeaderSP.setAnnotationSize(annotationSize);
            geneRowHeaderSP.setAnnotationSize(annotationSize);
            
            //geneColumnHeaderSP.updateSize(annotationSize);
            //geneRowHeaderSP.updateSize(annotationSize);
            
            geneColumnHeaderSP.setSize(geneColumnHeaderSP.getWidth(), annotationSize);
            geneColumnHeaderSP.setPreferredSize(new Dimension(geneColumnHeaderSP.getWidth(), annotationSize));
            
            geneRowHeaderSP.setSize(annotationSize, geneRowHeaderSP.getHeight());
            
            geneRowHeaderSP.setPreferredSize(new Dimension(annotationSize, geneRowHeaderSP.getHeight()));
            
            geneColumnHeaderSP.repaint();
            geneRowHeaderSP.repaint();
            
            updateSize(annotationSize);
            repaint();
        }
        
    }
    
    private void setLabelIndex(int style) {
        label = style;
    }
    
    private void onSaveNeighbors(){
        
        GDMMemberSelectionDialog dialog = new GDMMemberSelectionDialog(new JFrame(), this.num_genes);
        if(dialog.showModal() == JOptionPane.OK_OPTION){
            int k = dialog.getK();
            if(k <= 0)
                return;
            if(k > num_genes)
                k = num_genes;
            
            int [] rows = getRows(k); //getIDataRows(k);
            
            //get aux. data for ENTIRE set of genes relative to the base gene., rows will extract from here
            String [][] auxData = getAuxilaryData(num_genes);
            String [] auxHeaders = new String[3];
            auxHeaders[0] = "Scaled Dist.";
            auxHeaders[1] = "Actual Dist.";
            auxHeaders[2] = "Value Pairs";
            
            try{
                ExperimentUtil.saveGeneClusterWithAux(framework.getFrame(), this.experiment, this.expData, rows, auxHeaders, auxData);
            } catch (Exception e){
                e.printStackTrace();
                JOptionPane.showMessageDialog(framework.getFrame(), "Error saving file: "+e.getMessage(), "Output Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    
    private void onSaveMatrix(){
    	JFileChooser chooser = new JFileChooser(TMEV.getDataPath());
    	if(chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
    		try {
    			PrintWriter bw = new PrintWriter(new FileWriter(chooser.getSelectedFile()));
    			int rows = this.geneDistMatrix.getRowDimension();
    			int cols = this.geneDistMatrix.getColumnDimension();
    			//commnents
    			Date da = new Date(System.currentTimeMillis());    			
    			bw.println("# Save Date: "+da.toString());
    			bw.println("# Distance Metric: "+this.distanceMetric);
    			bw.println("#");
    			
    			//header    			
    			String line = "\t"; //skip one column
    			for(int i = 0; i < cols; i++) {
    				
    				line +=framework.getData().getElementAttribute(experiment.getGeneIndexMappedToData(indices[i]), this.labelIndex);
    				if(i < cols-1)
    					line += "\t";
    			}
    			bw.println(line);
				line = "";

    			for(int row = 0; row < rows; row++) {    				
    				line +=framework.getData().getElementAttribute(indices[row], this.labelIndex) + "\t";

    				for(int col = 0; col < cols; col++) {
    					line += Float.toString(this.rawMatrix.get(indices[row], indices[col]));
    					if(col < cols-1)
    						line += "\t";
    				}
    				
    				bw.println(line);
    				line = "";
    			}
    			
    			bw.flush();
    			bw.close();
    			
    		} catch (IOException ioe) {
    			ioe.printStackTrace();
    		}
    	}
    }
    
    private int [] getIDataRows(int k) {
        int [] rows = new int[k];
        for(int i = 0; i < k; i++){
            rows[i] = experiment.getGeneIndexMappedToData(indices[i]);
        }
        return rows;
    }

    private int [] getRows(int k) {
        int [] rows = new int[k];
        for(int i = 0; i < k; i++){
            rows[i] = indices[i];
        }
        return rows;
    }
    
    
    //get aux data for the entire gene set
    private String [][] getAuxilaryData(int k){
        String [][] data = new String[k][3];
        FloatMatrix matrix = this.experiment.getMatrix();
        for(int i = 0; i < k; i++){
            data[i][0] = Float.toString(this.geneDistMatrix.get(indices[0], i));
            data[i][1] = Float.toString(this.rawMatrix.get(indices[0], i));
            data[i][2] = getValuePairCount(matrix, indices[0], i);
        }
        return data;
    }
    
    private String getValuePairCount(FloatMatrix matrix, int row, int col){
        int cols = matrix.getColumnDimension();
        int count = 0;
        for(int i = 0; i < cols; i++){
            if(!Float.isNaN(matrix.get(row, i)) && !Float.isNaN(matrix.get(col, i)))
                count++;
        }
        return Integer.toString(count);
    }
    
    private void imposeClusterOrder() {
        
        Hashtable results = getResultHash();
        boolean noUseableResult = false;
        
        Hashtable goodResults = new Hashtable();
        
        Enumeration keys = results.keys();
        String key = "";
        Object [] result;
        
        while(keys.hasMoreElements()){
            key = (String)keys.nextElement();
            result = (Object [])(results.get(key));
            
            //need to handle HCL differently since it can be a gene or an experiemnt order
            if(key.indexOf("HCL") != -1) {
                int [][] clusters = new int[1][];
                clusters[0] = ((int[][])result[1])[0];
                
                if(clusters[0] == null)
                    continue;
                
                if((this.experiment == result[0]) && checkClustersSize(clusters)) {
                    goodResults.put(key, clusters);
                }             
            }
            
            //make sure it's the same experiment (same cutoffs), same number of genes (not exp. cluster)
            else if((this.experiment == result[0]) && checkClustersSize((int[][])result[1]) ) {
                goodResults.put(key, result[1]);
            }
        }
        
        if(goodResults.size() > 0) {
            GDMResultSelectionDialog dialog = new GDMResultSelectionDialog((JFrame)framework.getFrame(), goodResults.keys());
            if( dialog.showModal() == JOptionPane.OK_OPTION ) {
                int [][] clusters = ((int [][])goodResults.get(dialog.getSelectedResult()));
                imposeClusterOrder(clusters);
            }
        } else {
            JOptionPane.showMessageDialog(framework.getFrame(), "There are currently no appropriate clustering results to apply to this GDM.", "No Results Available", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private boolean checkClustersSize(int [][] clusters) {
        int cnt = 0;
        for(int i = 0; i < clusters.length; i++) {
            cnt += clusters[i].length;
        }
        
        if( ((int)(cnt/displayEvery)) == this.num_genes)
            return true;
        return false;
    }
    
    
    
    public Hashtable getResultHash(){
        Hashtable table = new Hashtable();
        DefaultMutableTreeNode analysisNode = framework.getResultTree().getAnalysisNode();
        DefaultMutableTreeNode analysisRoot;
        DefaultMutableTreeNode currentNode;
        Object object;
        Object [] vals;
        boolean stop = false;
        
        IViewer viewer;
        Experiment exp;
        int [][] clusters;
        
        int childCount = analysisNode.getChildCount();
        //String algTitles = new String[analysisNode.getChildCount()];
        String algName = "";
        Enumeration _enum;
        
        for(int i = 0; i < childCount; i++){
            analysisRoot = ((DefaultMutableTreeNode)(analysisNode.getChildAt(i)));
            object = analysisRoot.getUserObject();
            if(object != null){
                if(object instanceof LeafInfo){
                    algName = ((LeafInfo)object).toString();
                } else if(object instanceof String) {
                    algName = (String)object;
                }
                
                _enum = analysisRoot.depthFirstEnumeration();
                while (!stop && _enum.hasMoreElements()){
                    currentNode = (DefaultMutableTreeNode)_enum.nextElement();
                    if(currentNode.getUserObject() instanceof LeafInfo){
                        viewer = ((LeafInfo)currentNode.getUserObject()).getViewer();
                        if(viewer != null) {
                            exp = viewer.getExperiment();
                            clusters = viewer.getClusters();
                            if(exp != null && clusters != null) {
                                vals = new Object[2];
                                vals[0] = exp;
                                vals[1] = clusters;
                                table.put(algName, vals);
                                stop = true;
                            }
                        }
                    }
                }
                stop = false;
            }
        }
        return table;
    }
    
    
    private void imposeClusterOrder(int [][] newClusters) {
        this.clusters = newClusters;
        this.numOfClusters = clusters.length;
        onSortByClusterChange();
    }
    
    
    
    
    /**
     * The listener to listen to mouse, keyboard and window events.
     */
    
    private class Listener extends MouseAdapter implements ActionListener, MouseMotionListener, KeyListener, WindowListener {
        
        private int oldRow = -1;
        private int oldColumn = -1;
        private String oldStatusText;
        
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            
            if(command.equals(DISPLAY_LABEL_CMD)) {
                onLabelChanged((Action)event.getSource());
            } else if (command.equals(ANNOTATION_WIDTH_CMD)) {
                onAnnotationWidthChanged();
            } else if (command.equals(DISPLAY_2X2_CMD)) {
                onElementSizeChanged(2, 2);
            } else if (command.equals(DISPLAY_5X5_CMD)) {
                onElementSizeChanged(5, 5);
            } else if (command.equals(DISPLAY_10X10_CMD)) {
                onElementSizeChanged(10, 10);
            } else if (command.equals(DISPLAY_15X15_CMD)) {
                onElementSizeChanged(15, 15);
            } else if (command.equals(DISPLAY_OTHER_CMD)) {
                onElementSizeChanged();
            } else if (command.equals(GREEN_RED_COLOR_SCHEME_CMD)){
                onColorSchemeChange(IDisplayMenu.GREEN_RED_SCHEME);
            } else if (command.equals(BLUE_YELLOW_COLOR_SCHEME_CMD)){
                onColorSchemeChange(IDisplayMenu.BLUE_YELLOW_SCHEME);
            } else if (command.equals(CUSTOM_COLOR_SCHEME_CMD)){
                onColorSchemeChange(IDisplayMenu.CUSTOM_COLOR_SCHEME);
            } else if (command.equals(DISPLAY_DRAW_BORDERS_CMD)) {
                onDrawBordersChanged(((javax.swing.JCheckBoxMenuItem)(event.getSource())).isSelected());
            } else if (command.equals(BORDER_COLOR_CMD)) {
                onBorderColorChanged();
            } else if (command.equals(COLOR_SCALE_CMD)) {
                onColorScaleChanged();
            } else if (command.equals(SORT_BY_LOCATION_CMD)) {
                onSort(SlideDataSorter.SORT_BY_LOCATION);
            } else if (command.equals(SORT_BY_RATIO_CMD)) {
                onSort(SlideDataSorter.SORT_BY_RATIO);
            } else if (command.equals(SORT_LABEL_CMD)) {
                onSort((Action)event.getSource());
            } else if (command.equals(SORT_BY_CLUSTER_ORDER_CMD)) {
                onSortByClusterChange();
            } else if (command.equals(SORT_BY_GENE_PROXIMITY_CMD)) {
                onSortByGeneProximity(event.getID());
            } else if (command.equals(TOGGLE_PROXIMITY_SORT_CMD)) {
                toggleSortByGeneProximity();
            } else if (command.equals(SET_CLUSTER_BORDER_CMD)) {
                onDrawClusterBorderChange(((javax.swing.JCheckBoxMenuItem)(event.getSource())).isSelected());
            } else if (command.equals(SAVE_NEIGHBORS_CMD)){
                onSaveNeighbors();
            } else if (command.equals("impose-cluster-order")) {
                imposeClusterOrder();
            } else if (command.equals(SAVE_MATRIX_CMD)) {
                onSaveMatrix();
            }
        }
        
        
        public void mousePressed(MouseEvent event) {
            requestFocus();
        }
        
        public void mouseClicked(MouseEvent event) {
            if (SwingUtilities.isLeftMouseButton(event)) {
                int column = findColumn(event.getX());
                int row = findRow(event.getY());
                
                if (!isLegalPosition(row, column)) {
                    return;
                }
                
                displayGDMSpotInfo(indices[column], indices[row], column, row);
            }
            else {
                int column = findColumn(event.getX());
                int row = findRow(event.getY());
                
                popup.show(event.getComponent(), event.getX(), event.getY());
            }
        }
        
        public void mouseMoved(MouseEvent event) {
            if (num_genes == 0 || event.isShiftDown())
                return;
            int column = findColumn(event.getX());
            int row = findRow(event.getY());
            if (isCurrentPosition(row, column)) {
                return;
            }
            Graphics g = null;
            Graphics2D g2D = (Graphics2D) g;
            if (isLegalPosition(row, column)) {
                g = getGraphics();
                drawColoredBoxAt(g, row, column, Color.white);
                framework.setStatusText(
                " Column: " + (column+1) +
                "     " +  //some padding
                " Row: " + (row+1) +
                "     " +  //some padding
                " Scaled Distance: "    + geneDistMatrix.get(indices[column], indices[row]) +
                "     " +  //some padding
                " Actual Distance: " + rawMatrix.get(indices[column], indices[row]));
            } else {
                framework.setStatusText(oldStatusText);
            }
            if (isLegalPosition(oldRow, oldColumn)) {
                g2D = g != null ? (Graphics2D)g : (Graphics2D)getGraphics();
                drawSlideDataElement(g2D, oldRow, oldColumn);
            }
            setOldPosition(row, column);
            
            if(g != null) {
                if(isDrawClusterBorders && numOfClusters > 0)
                    drawClusterBorder((Graphics2D)g);
            } else if (g2D != null) {
                if(isDrawClusterBorders && numOfClusters > 0)
                    drawClusterBorder(g2D);
            }
            
            if (g != null) {
                g.dispose();
            }
        }
        
        public void mouseExited(MouseEvent event) {
            if (isLegalPosition(oldRow, oldColumn)) {
                Graphics g = getGraphics();
                Graphics2D g2D = (Graphics2D)g;
                drawSlideDataElement(g2D, oldRow, oldColumn);
                g2D.dispose();
                if(isDrawClusterBorders && numOfClusters > 0)
                    drawClusterBorder(g2D);
            }
            
            setOldPosition(-1, -1);
            framework.setStatusText("  ");	// blank Status bar
        }
        
        public void mouseEntered(MouseEvent event) {
            oldStatusText = framework.getStatusText();
        }
        
        private void setOldPosition(int row, int column) {
            oldColumn = column;
            oldRow = row;
        }
        
        private boolean isCurrentPosition(int row, int column) {
            return(row == oldRow && column == oldColumn);
        }
        
        public void mouseDragged(MouseEvent event) {}
        public void keyReleased(KeyEvent event) {}
        public void keyPressed(KeyEvent e) {}
        public void keyTyped(KeyEvent e) {}
        public void windowClosing(WindowEvent e) {}
        public void windowOpened(WindowEvent e) {}
        public void windowClosed(WindowEvent e) {}
        public void windowIconified(WindowEvent e) {}
        public void windowDeiconified(WindowEvent e) {}
        public void windowActivated(WindowEvent e) {}
        public void windowDeactivated(WindowEvent e) {}
    }
    
    private class ScaleListener extends GDMScaleListener{
        
        public void scaleChanged(float lower, float upper) {
            minValue = lower;
            maxValue = upper;
            geneColumnHeaderSP.setValues(minValue, maxValue);
            geneRowHeaderSP.setValues(minValue, maxValue);
            geneColumnHeaderSP.updateSize(NOT_UPDATE_ANNOTATION_SIZE);
            geneRowHeaderSP.updateSize(NOT_UPDATE_ANNOTATION_SIZE);
            geneColumnHeaderSP.validate();
            geneRowHeaderSP.validate();
            validate();
            geneColumnHeaderSP.repaint();
            geneRowHeaderSP.repaint();
            repaint();
        }
    }
    
    /**
     * returns true if a probe in the current viewer has color
     */
    protected  boolean areProbesColored() {
        for(int i = 0; i < indices.length; i++){
            if( this.expData.getProbeColor(indices[i]) != null){
                return true;
            }
        }
        return false;
    }
    
    /** Returns the corner component corresponding to the indicated corner,
     * posibly null
     */
    public JComponent getCornerComponent(int cornerIndex) {
        if(cornerIndex == IViewer.UPPER_RIGHT_CORNER)
            return this.upperRightCornerSB;
        else if(cornerIndex == IViewer.LOWER_LEFT_CORNER)
            return this.lowerLeftCornerSB;
        return null;
    }
    
    public int[][] getClusters() {
        return null;
    }
    
    public Experiment getExperiment() {
        return experiment;
    }
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return Cluster.GENE_CLUSTER;
    }
    
    private class DefaultAction extends AbstractAction {
        
        /**
         * Constructs <code>DefaultAction</code> with a specified name
         * and an action command.
         */
        public DefaultAction(String name, String command) {
            this(name, command, null);
        }
        
        /**
         * Constructs <code>DefaultAction</code> with a specified name,
         * an action command and a small icon.
         */
        public DefaultAction(String name, String command, ImageIcon smallIcon) {
            putValue(Action.NAME, name);
            putValue(Action.SHORT_DESCRIPTION, name);
            putValue(Action.ACTION_COMMAND_KEY, command);
            putValue(Action.SMALL_ICON, smallIcon);
        }
        
        /**
         * Delegates this invokation to a wrapped action manager.
         */
        public void actionPerformed(ActionEvent e) {
            listener.actionPerformed(new ActionEvent(this, e.getID(), (String)getValue(Action.ACTION_COMMAND_KEY)));
        }
    }



	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExperimentID()
	 */
	public int getExperimentID() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperimentID(int)
	 */
	public void setExperimentID(int id) {
		;
	}


}
