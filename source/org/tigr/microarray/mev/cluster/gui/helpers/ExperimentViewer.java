/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ExperimentViewer.java,v $
 * $Revision: 1.14 $
 * $Date: 2007-12-20 19:55:10 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GradientPaint;
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
import java.beans.Expression;
import java.util.ArrayList;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.tigr.microarray.mev.annotation.AnnoAttributeObj;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

/**
 * This class is used to render a heatmap of experiment values. 
 * 
 * Subclasses of this class in which adding to the right-click context menu would 
 * be useful will need to make the following modifications: 
 * 
 * Subclass the inner class PopupListener. Override its method public void actionPerformed(ActionEvent e).
 * Call super(e) and then add additional command handlers. 
 * 
 * Override the method protected void addMenuItems(JPopupMenu menu, ActionListener listener). Call 
 * the overridden method first, then add additional menu items afterwards. They will be appended to the 
 * bottom of the menu.
 * 
 * Add the following code block to subclass constructors: 
 *         PopupListener popListener = new PopupListener();
 *         this.popup = createJPopupMenu(popListener);
 *         getContentComponent().addMouseListener(popListener);
 *         getHeaderComponent().addMouseListener(popListener);
 *
 * @author Aleksey D.Rezantsev
 */
public class ExperimentViewer extends JPanel implements IViewer {
    
    private static final float INITIAL_MAX_VALUE = 3f;
    private static final float INITIAL_MIN_VALUE = -3f;
    private static final String NO_GENES_STR = "No Genes in Cluster!";
    private static final Font ERROR_FONT = new Font("monospaced", Font.BOLD, 20);
    protected static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
    protected static final String AUTO_STORE_GENE_CLUSTERS_CMD = "auto-store-gene-cluster-cmd";
    protected static final String AUTO_STORE_SAMPLE_CLUSTERS_CMD = "auto-store-sample-cluster-cmd";
    protected static final String SET_DEF_COLOR_CMD = "set-def-color-cmd";
    protected static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
    protected static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";
    protected static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";
    public static final String BROADCAST_MATRIX_GAGGLE_CMD = "broadcast-matrix-to-gaggle";
    public static final String BROADCAST_NAMELIST_GAGGLE_CMD = "broadcast-namelist-to-gaggle";
    
    private ExperimentHeader header;
    private Experiment experiment;
    private IFramework framework;
    private IData data;
    private int clusterIndex = 0;
    private int[][] clusters;
    private int[] samplesOrder;
    private Dimension elementSize = new Dimension(20, 5);
    private int labelIndex = -1;
    private boolean isAntiAliasing = true;
    private boolean isDrawBorders = true;
    private boolean isCompact = false;
    private boolean isAutoArrangeColors = true;
    private boolean isShowRects = true;
    private boolean clickedCell = false;
    private int clickedColumn = 0;
    private int clickedRow = 0;
    private boolean isDrawAnnotations = true;
    public static Color missingColor = new Color(128, 128, 128);
    public static Color maskColor = new Color(255, 255, 255, 128);
    private float maxValue = INITIAL_MAX_VALUE;
    private float minValue = INITIAL_MIN_VALUE;
    private float midValue = 0.0f;
    private int firstSelectedRow = -1;
    private int lastSelectedRow  = -1;
    private int firstSelectedColumn = -1;
    private int lastSelectedColumn  = -1;
    public BufferedImage posColorImage = createGradientImage(Color.black, Color.red);
    public BufferedImage negColorImage = createGradientImage(Color.green, Color.black);
    private int annotationWidth;
    private Insets insets = new Insets(0, 10, 0, 0);
    private int contentWidth = 0;
    private boolean useDoubleGradient = true;
    private boolean showClusters = true;
    private boolean haveColorBar = false;
    protected int exptID = 0;
    //Added by Sarita
    protected String LabelName;
    
    private int activeCluster = 0;
    public static ArrayList<Color> storedGeneColors = new ArrayList<Color>();
    public static ArrayList<Color> savedGeneColorOrder = new ArrayList<Color>();
    private static int[] ColorOverlaps = new int[100000];
    private int colorWidth = 0;
    private int maxColorWidth = 0;
	private boolean mouseOnMap = false;
	private int mouseRow = 0;
	private int mouseColumn = 0;
	protected JPopupMenu popup;
	private boolean inDrag = false;
    private int dragRow = 0;
    private int dragColumn = 0;
    
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    		new Object[]{this.experiment, this.clusters, this.samplesOrder, new Boolean(this.isDrawAnnotations)});
    }
    /**
     * Constructs an <code>ExperimentViewer</code> with specified
     * experiment and clusters.
     *
     * @param experiment the experiment data.
     * @param clusters the two dimensional array with spots indices.
     */
    public ExperimentViewer(Experiment experiment, int[][] clusters) {
        this(experiment, clusters, true);
    }
    
    /**
     * Constructs an <code>ExperimentViewer</code> with specified
     * experiment, clusters and draw annotations attribute.
     *
     * @param experiment the experiment data.
     * @param clusters the two dimensional array with spots indices.
     * @param drawAnnotations true if this viewer must draw annotations.
     */
    public ExperimentViewer(Experiment experiment, int[][] clusters, boolean drawAnnotations) {
        this(experiment, clusters, null, drawAnnotations);
    }
    /**
     * State-saving constructor
     * @param experiment
     * @param clusters
     * @param samplesOrder
     * @param drawAnnotations
     */
    public ExperimentViewer(Experiment experiment, int[][] clusters, int[] samplesOrder, Boolean drawAnnotations) {
    	this(experiment, clusters, samplesOrder, drawAnnotations.booleanValue());
    }
    /**
     * Constructs an <code>ExperimentViewer</code> with specified
     * experiment, clusters, samples order and draw annotations attribute.
     *
     * @param experiment the experiment data.
     * @param clusters the two dimensional array with spots indices.
     * @param samplesOrder the one dimensional array with samples indices.
     * @param drawAnnotations true if this viewer must draw annotations.
     */
    public ExperimentViewer(Experiment experiment, int[][] clusters, int[] samplesOrder, boolean drawAnnotations) {
        if (experiment == null) {
            throw new IllegalArgumentException("experiment == null");
        }
        this.experiment = experiment;
        this.exptID = experiment.getId();
        this.clusters = clusters == null ? defGenesOrder(experiment.getNumberOfGenes()) : clusters;
        this.samplesOrder = samplesOrder == null ? defSamplesOrder(experiment.getNumberOfSamples()) : samplesOrder;
        this.isDrawAnnotations = drawAnnotations;
        this.header = new ExperimentHeader(this.experiment, this.clusters, this.samplesOrder, this.storedGeneColors);
        this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
        setBackground(Color.white);
        Listener listener = new Listener();
        addMouseListener(listener);
        addMouseMotionListener(listener);
        
        PopupListener popListener = new PopupListener();
        this.popup = createJPopupMenu(popListener);
        getContentComponent().addMouseListener(popListener);
        getHeaderComponent().addMouseListener(popListener);
    }
    
    
    /**
     * Constructs an <code>ExperimentViewer</code> with specified
     * experiment, clusters, samples order and draw annotations attribute.
     *
     * @param experiment the experiment data.
     * @param clusters the two dimensional array with spots indices.
     * @param samplesOrder the one dimensional array with samples indices.
     * @param drawAnnotations true if this viewer must draw annotations.
     */
    public ExperimentViewer(Experiment experiment, int[][] clusters, int[] samplesOrder, boolean drawAnnotations, int offset) {
        if (experiment == null) {
            throw new IllegalArgumentException("experiment == null");
        }
        this.experiment = experiment;
        this.exptID = experiment.getId();
        this.clusters = clusters == null ? defGenesOrder(experiment.getNumberOfGenes()) : clusters;
        this.samplesOrder = samplesOrder == null ? defSamplesOrder(experiment.getNumberOfSamples()) : samplesOrder;
        this.isDrawAnnotations = drawAnnotations;
        this.header = new ExperimentHeader(this.experiment, this.clusters, this.samplesOrder, this.storedGeneColors);
        this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
        this.insets.left = offset;
        this.header.setLeftInset(offset);
        setBackground(Color.white);
        Listener listener = new Listener();
        addMouseListener(listener);
        addMouseMotionListener(listener);
        
        PopupListener popListener = new PopupListener();
        this.popup = createJPopupMenu(popListener);
        getContentComponent().addMouseListener(popListener);
        getHeaderComponent().addMouseListener(popListener);
    }
    
    public ExperimentViewer(){  }
    public void setInsets(Insets i) {
    	this.insets = i;
    }    
    /**
     * This constructor is used to re-create an ExperimentViewer from information
     * stored in a saved analysis file by XMLEncoder.  
     * @param experiment TODO
     * @param clusters
     * @param samplesOrder
     * @param drawAnnotations
     * @param header
     * @param insets
     * @param experiment
     */
    public ExperimentViewer(Experiment experiment, int[][] clusters, int[] samplesOrder, boolean drawAnnotations, ExperimentHeader header, Insets insets) {
	    this.insets = insets;
	    this.experiment = experiment;
	    this.header = header;
	    this.clusters = clusters;
	    this.samplesOrder = samplesOrder;
	    this.isDrawAnnotations = drawAnnotations;
	    this.header = header;

	    setBackground(Color.white);
	    
	    Listener listener = new Listener();
	    addMouseListener(listener);
	    addMouseMotionListener(listener);

        PopupListener popListener = new PopupListener();
        this.popup = createJPopupMenu(popListener);
        getContentComponent().addMouseListener(popListener);
        getHeaderComponent().addMouseListener(popListener);
    }

    /*
    copy-paste this constructor into descendent classes
    /**
     * @inheritDoc
     * 
    public ExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder, boolean drawAnnotations, ExperimentHeader header, Insets insets) {
    	super(e, clusters, samplesOrder, drawAnnotations, header, insets);
    } 
    */
    
    /**
     * @inheritdoc
     */
//    public Expression getExpression(){
//    	return new Expression(this, this.getClass(), "new",
//				new Object[]{this.clusters, this.samplesOrder, new Boolean(this.isDrawAnnotations), this.header, this.insets, new Integer(this.exptID)});  
//    }
    
    public void setExperiment(Experiment e) {
    	this.experiment = e;
    	this.exptID = experiment.getId();
    	if(this.header !=null){
    		this.header.setExperiment(e);
    	} else{
    		this.header = new ExperimentHeader(this.experiment, this.clusters, this.samplesOrder, this.storedGeneColors);
            this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
    	}
   		this.header.setIData(data);
    }
    /*
    public ExperimentHeader getHeader() {
    	return header;
    }
    public int[] getSamplesOrder(){
    	return samplesOrder;
    }
    public boolean getIsDrawAnnotations(){return isDrawAnnotations;}

    public Insets getInsets() {return insets;}

    public void setInsets(Insets i) {
    	this.insets = i;
    }    
    */
    private static int[] defSamplesOrder(int size) {
        int[] order = new int[size];
        for (int i=0; i<order.length; i++) {
            order[i] = i;
        }
        return order;
    }
    
    private static int[][] defGenesOrder(int size) {
        int[][] order = new int[1][size];
        for (int i=0; i<order[0].length; i++) {
            order[0][i] = i;
        }
        return order;
    }
    
    /**
     * Sets the left margin for the viewer
     */
    public void setLeftInset(int leftMargin){
        insets.left = leftMargin;
        this.header.setLeftInset(leftMargin);
    }
    
    /**
     * Returns component to render the experiment header.
     */
    public JComponent getHeaderComponent() {
        return header;
    }
    
    /**
     * Returns component to render the experiment values.
     */
    public JComponent getContentComponent() {
        return this;
    }
    
    /**
     * Returns null.
     */
    public BufferedImage getImage() {
        return null;
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
     * Returns a color assigned for a NaN value.
     */
    public Color getMissingColor() {
        return missingColor;
    }
    
    /**
     * Selects rows from start to end.
     */
    public void selectRows(int start, int end) {
        firstSelectedRow = start;
        lastSelectedRow  = end;
        repaint();
    }
    
    /**
     * Selects columns from start to end.
     */
    public void selectColumns(int start, int end) {
        firstSelectedColumn = start;
        lastSelectedColumn  = end;
        repaint();
    }
    
    /**
     * Initializes appropriate attributes of this viewer and its header.
     * @see IViewer#onSelected
     */
    public void onSelected(IFramework framework) {
    	header.clusterViewerClicked = false;
    	header.clickedCell = false;
    	clickedCell = false;
        this.framework = framework;
        this.data = framework.getData();
        IDisplayMenu menu = framework.getDisplayMenu();
        useDoubleGradient = menu.getUseDoubleGradient();
        header.setUseDoubleGradient(useDoubleGradient);
        Integer userObject = (Integer)framework.getUserObject();
        setClusterIndex(userObject == null ? 0 : userObject.intValue());
        header.setClusterIndex(this.clusterIndex);
        labelIndex = menu.getLabelIndex();
        this.maxValue = menu.getMaxRatioScale();
        this.minValue = menu.getMinRatioScale();
        this.midValue = menu.getMidRatioValue();
        setElementSize(menu.getElementSize());
        setAntialiasing(menu.isAntiAliasing());
        setDrawBorders(menu.isDrawingBorder());
        setCompactClusters(menu.isCompactClusters());
        setShowRects(menu.isShowRects());
        setAutoArrangeColors(!menu.isAutoArrangeColors());
        header.setCompactClusters(menu.isCompactClusters());
        header.setStoredColors(storedGeneColors);
        if (isAutoArrangeColors||isCompact){
        storedGeneColors.clear();
        	header.clearStoredSampleColors();
        }
        if(showClusters)
            haveColorBar = areProbesColored();
        else
            haveColorBar = false;
        updateSize();        
        this.maxValue = menu.getMaxRatioScale();
        this.minValue = menu.getMinRatioScale();
        this.midValue = menu.getMidRatioValue();
        this.posColorImage = menu.getPositiveGradientImage();
        this.negColorImage = menu.getNegativeGradientImage();
        this.useDoubleGradient = menu.getUseDoubleGradient();
        this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
        this.header.setUseDoubleGradient(useDoubleGradient);
        //header.setValues(maxValue, minValue);
        header.setValues(minValue, midValue, maxValue);
        header.updateSizes(header.getSize().width, elementSize.width);
        header.setData(data);
        //onMenuChanged(menu);
        //header.setValues(maxValue, minValue);
        header.setValues(minValue, midValue, maxValue);
        header.setAntiAliasing(menu.isAntiAliasing());
        header.updateSizes(header.getSize().width, elementSize.width);
    }
    
    /**
     * Updates appropriate attributes of this viewer and its header.
     * @see IViewer#onMenuChanged
     */
    public void onMenuChanged(IDisplayMenu menu) {
    	header.clusterViewerClicked = false;
    	header.clickedCell = false;
    	clickedCell = false;
    	boolean isAutoArrangeChanged=isAutoArrangeColors;
        setDrawBorders(menu.isDrawingBorder());
        setCompactClusters(menu.isCompactClusters());
        setAutoArrangeColors(!menu.isAutoArrangeColors());
        setShowRects(menu.isShowRects());
        header.isShowRects = isShowRects;
        header.setCompactClusters(menu.isCompactClusters());
        header.setStoredColors(storedGeneColors);
        if((isAutoArrangeColors!=isAutoArrangeChanged)||isAutoArrangeColors){
        storedGeneColors.clear();
        	header.clearStoredSampleColors();
        }
        header.updateSizes(header.getSize().width, elementSize.width);
        updateSize();
        this.maxValue = menu.getMaxRatioScale();
        this.minValue = menu.getMinRatioScale();
        this.midValue = menu.getMidRatioValue();
        this.posColorImage = menu.getPositiveGradientImage();
        this.negColorImage = menu.getNegativeGradientImage();
        this.useDoubleGradient = menu.getUseDoubleGradient();
        this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
        this.header.setUseDoubleGradient(useDoubleGradient);
        //header.setValues(maxValue, minValue);
        header.setValues(minValue, midValue, maxValue);
        if (this.elementSize.equals(menu.getElementSize()) &&
        labelIndex == menu.getLabelIndex() &&
        this.isAntiAliasing == menu.isAntiAliasing()) {
            return;
        }
        setElementSize(menu.getElementSize());
        setAntialiasing(menu.isAntiAliasing());
        labelIndex = menu.getLabelIndex();
        if(showClusters)
            haveColorBar = areProbesColored();
        else
            haveColorBar = false;
        header.updateSizes(header.getSize().width, elementSize.width);
        updateSize();
        header.setAntiAliasing(menu.isAntiAliasing());
        header.updateSizes(header.getSize().width, elementSize.width);
        onSelected(framework);

        
    }
    
    /**
     * Sets data for this viewer and its header.
     * @see IViewer#onDataChanged
     */
    public void onDataChanged(IData data) {
        this.data = data;
        this.header.setData(data);
        if(showClusters)
            haveColorBar = areProbesColored();
        else
            haveColorBar = false;
        updateSize();
        repaint();
    }
    
    public void onDeselected() {}
    public void onClosed() {}
    
    /**
     * Sets cluster index to be displayed.
     */
    public void setClusterIndex(int clusterIndex) {
        this.clusterIndex = clusterIndex;
    }
    
    /**
     * Returns index of current cluster.
     */
    public int getClusterIndex() {
        return clusterIndex;
    }
    
    /**
     * Returns indices of current cluster.
     */
    public int[] getCluster() {
        return clusters[this.clusterIndex];
    }
    
    /**
     * Returns all the clusters.
     */
    public int[][] getClusters() {
        return clusters;
    }
    
    /**
     *	Returns the row (index) within the main iData which corresponds to
     *  the passed index to the clusters array
     */
    private int getMultipleArrayDataRow(int clusterArrayRow) {
        return experiment.getGeneIndexMappedToData(this.clusters[this.clusterIndex][clusterArrayRow]);
    }
    
    /**
     *	Returns the row index in the experiment's <code>FloatMatrix<\code>
     *  corresponding to the passed index to the clusters array
     */
    private int getExperimentRow(int row){
        return this.clusters[this.clusterIndex][row];
    }
    
    private int getColumn(int column) {
        return samplesOrder[column];
    }
    
    /**
     * Returns wrapped experiment.
     */
    public Experiment getExperiment() {
        return experiment;
    }
    
    /**
     * Returns the data.
     */
    public IData getData() {
        return data;
    }
    
    /**
     * returns true if a probe in the current viewer has color
     */
    protected  boolean areProbesColored() {
        int [] indices = this.getCluster();
        for(int i = 0; i < indices.length; i++){
            if( this.data.getProbeColor(this.getMultipleArrayDataRow(i)) != null){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Sets public color for the current cluster.
     */
    public void setClusterColor(Color color) {
        if(color ==null){  //indicates removal of cluster
            framework.removeCluster(getIDataRowIndices(getCluster()), experiment, ClusterRepository.GENE_CLUSTER);
        }
    }
    
    /**
     *  Sets cluster color
     */
    public void storeCluster(){
        framework.storeCluster(getIDataRowIndices(getCluster()), experiment, ClusterRepository.GENE_CLUSTER);
        onDataChanged(this.data);
        updateSize();
    }
    /**
     * Automatically stores all clusters by annotation
     */
    public void autoStoreClusters(int clusterType, int index){
    	framework.autoStoreClusters(clusterType, index);
    }
    
    
    /**
     * Sets public color for the current cluster related to genes or experiment indices.
     */
    public Color setHCLClusterColor(int [] clusterIndices, Color color, boolean areGeneIndices) {
        Color clusterColor = null;
        if(areGeneIndices)
            clusterColor = framework.storeSubCluster(clusterIndices, experiment, ClusterRepository.GENE_CLUSTER);
        else
            clusterColor = framework.storeSubCluster(clusterIndices, experiment, ClusterRepository.EXPERIMENT_CLUSTER);
        
        this.selectColumns(-1,-1);
        this.selectRows(-1,-1);
        header.updateSizes(getSize().width, elementSize.width);
        this.header.repaint();
        updateSize();
        this.repaint();
        
        return clusterColor;
    }
    
    /**
     * Converts cluster indicies from the experiment to IData rows which could be different
     */
    private int [] getIDataRowIndices(int [] expIndices){
        int [] dataIndices = new int[expIndices.length];
        for(int i = 0; i < expIndices.length; i++){
            dataIndices[i] = this.getMultipleArrayDataRow(i);
        }
        return dataIndices;
    }
    
    /**
     * Saves all the clusters.
     */
    public void saveClusters(Frame frame) throws Exception {
        frame = frame == null ? JOptionPane.getFrameForComponent(this) : frame;
        ExperimentUtil.saveExperiment(frame, getExperiment(), getData(), getClusters());
    }
    
    /**
     * Saves current cluster.
     */
    public void saveCluster(Frame frame) throws Exception {
        frame = frame == null ? JOptionPane.getFrameForComponent(this) : frame;
        ExperimentUtil.saveExperiment(frame, getExperiment(), getData(), getCluster());
    }
    
    /**
     * Launches a new <code>MultipleExperimentViewer</code> containing the current cluster
     */
    public void launchNewSession(){
        framework.launchNewMAV(getIDataRowIndices(getCluster()), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.GENE_CLUSTER);
    }
    
    /**
     * Sets a shape size.
     */
    private void setElementSize(Dimension elementSize) {
        this.elementSize = new Dimension(elementSize);
    }
    
    /**
     * Sets anti-aliasing attribute.
     */
    private void setAntialiasing(boolean value) {
        this.isAntiAliasing = value;
    }
    
    /**
     * Sets draw borders attribute.
     */
    private void setDrawBorders(boolean value) {
        this.isDrawBorders = value;
    }
    
    /**
     * Sets compact clusters attribute.
     */
    private void setCompactClusters(boolean value) {
    	if (value==isCompact)
    		return;
    	//savedGeneColorOrder is for keeping the same cluster color order when clusters are "un-compacted"
    	if (value){
	    	savedGeneColorOrder.clear();  
    		for (int i=0; i<storedGeneColors.size(); i++){
		    	savedGeneColorOrder.add((Color)storedGeneColors.get(i));
	    	}
    		storedGeneColors.clear();
    	}else{
	    	storedGeneColors.clear();
	    	clearColorOverlaps();
    		for (int i=0; i<savedGeneColorOrder.size(); i++){
		    	storedGeneColors.add((Color)savedGeneColorOrder.get(i));
    		}
    }
	    this.isCompact = value;
    
    } 
    /**
     * Sets show Rects attribute.
     */
    private void setShowRects(boolean value) {
        this.isShowRects = !value;
        header.isShowRects = !value;
    }
    /**
     * Sets auto-arrange attribute
     */
    private void setAutoArrangeColors(boolean value){
    	this.isAutoArrangeColors = value;
    }
    private void clearColorOverlaps(){
    	for (int i=0; i<ColorOverlaps.length; i++){
    		ColorOverlaps[i] = i;
    	}
    }
    /**
     * Creates a gradient image with specified initial colors.
     */
    public BufferedImage createGradientImage(Color color1, Color color2) {
        BufferedImage image = (BufferedImage)java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(256,1);
        Graphics2D graphics = image.createGraphics();
        GradientPaint gp = new GradientPaint(0, 0, color1, 255, 0, color2);
        graphics.setPaint(gp);
        graphics.drawRect(0, 0, 255, 1);
        return image;
    }
    
    /**
     * Updates size of this viewer.
     */
    private void updateSize() {
        if (this.clusters == null || getCluster().length == 0) {
            setFont(ERROR_FONT);
            Graphics2D g = (Graphics2D)getGraphics();
            FontMetrics metrics = g.getFontMetrics();
            int width = metrics.stringWidth(NO_GENES_STR)+10;
            int height = metrics.getHeight()+30;
            setSize(width, height);
            setPreferredSize(new Dimension(width, height));
            return;
        }
        setFont(new Font("monospaced", Font.PLAIN, elementSize.height));
        Graphics2D g = (Graphics2D)getGraphics();
        int width = elementSize.width*experiment.getNumberOfSamples() + 1 + insets.left;
        if (isDrawAnnotations) {
            this.annotationWidth = getMaxWidth(g);
            width += 20+this.annotationWidth;
        }
        if (maxColorWidth < colorWidth){
        	maxColorWidth = colorWidth;
        }
        if(haveColorBar)
            width += this.elementSize.width*colorWidth + 10;
        this.contentWidth = width;
        
        int height = elementSize.height*getCluster().length+1;
        setSize(width, height);


        if (header.getSize().width < width){
        	setSize(width, height);
        setPreferredSize(new Dimension(width, height));
        }else{
        	setSize(new Dimension(header.getSize().width, getSize().height));
        	setPreferredSize(new Dimension(header.getSize().width, getSize().height));
        }

        if (isCompact){
        	setSize(width, height);
        	setPreferredSize(new Dimension(width, height));
        }
    }
    
    /**
     * Returns max width of annotation strings.
     */
    private int getMaxWidth(Graphics2D g) {
        if (g == null || data == null || getCluster() == null) {
            return 0;
        }
        if (isAntiAliasing) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        FontMetrics fm = g.getFontMetrics();
        int max = 0;
        String str;
        
       for (int i=0; i<getCluster().length; i++) {//For loop commented temporarily by sarita
        	//Added by Sarita to check for Label width in both instances,
    	   //namely if label is a "ExtraField" OR if label is actually
    	   //added using the new annotation model 
    	 if(labelIndex <= data.getFieldNames().length-1) {
    		 //using new annotation model
       		 str = data.getElementAttribute(getMultipleArrayDataRow(i), labelIndex);
       		
     	} else {
     		//using new annotation model
       	 str =  (data.getElementAnnotation(getMultipleArrayDataRow(i), MevAnnotation.getFieldNames()[labelIndex-(data.getFieldNames().length)])).toString();
       	}
            max = Math.max(max, fm.stringWidth(str));
        }
        return max;
    }
    
    /**
     * Returns content width
     */
    public int getContentWidth(){
    	updateSize();
        return contentWidth;
    }

    
    /**
     * Calculates color for passed value.
     */
    /*
      
     private Color getColor(float value) {
        if (Float.isNaN(value)) {
            return missingColor;
        }
        
        float maximum;
        int colorIndex, rgb;
        
        if(useDoubleGradient) {
        	maximum = value < 0 ? this.minValue : this.maxValue;
			colorIndex = (int) (255 * value / maximum);
			colorIndex = colorIndex > 255 ? 255 : colorIndex;
			rgb = value < 0 ? negColorImage.getRGB(255 - colorIndex, 0)
					: posColorImage.getRGB(colorIndex, 0);
        } else {
        	float span = this.maxValue - this.minValue;
        	if(value <= minValue)
        		colorIndex = 0;
        	else if(value >= maxValue)
        		colorIndex = 255;
        	else
        		colorIndex = (int)(((value - this.minValue)/span) * 255);
         	
        	rgb = posColorImage.getRGB(colorIndex,0);
        }
        return new Color(rgb);
    }
    */
    
    private Color getColor(float value) {
        if (Float.isNaN(value)) {
            return missingColor;
        }
        
        float maximum;
        int colorIndex, rgb;
        
        if(useDoubleGradient) {
        	maximum = value < midValue ? this.minValue : this.maxValue;
			colorIndex = (int) (255 * (value-midValue) / (maximum - midValue));
			colorIndex = colorIndex > 255 ? 255 : colorIndex;
			rgb = value < midValue ? negColorImage.getRGB(255 - colorIndex, 0)
					: posColorImage.getRGB(colorIndex, 0);
        } else {
        	float span = this.maxValue - this.minValue;
        	if(value <= minValue)
        		colorIndex = 0;
        	else if(value >= maxValue)
        		colorIndex = 255;
        	else
        		colorIndex = (int)(((value - this.minValue)/span) * 255);
         	
        	rgb = posColorImage.getRGB(colorIndex,0);
        }
        return new Color(rgb);
    }
    
    public ArrayList getStoredColors(){
    	return storedGeneColors;
    }
    
    /**
     * Paint component into specified graphics.
     */
    public void paint(Graphics g) {
        super.paint(g);
        if (header!=null){
        header.setStoredColors(storedGeneColors);
        header.updateSizes(0, elementSize.width);
        header.repaint();
    	}
        int oldWidth=colorWidth;
        if (this.data == null) {
            return;
        }
        if(this.elementSize.getHeight() < 1)
            return;
        final int samples = experiment.getNumberOfSamples();
        
        if (this.clusters == null || getCluster().length == 0) {
            g.setColor(new Color(0, 0, 128));
            g.setFont(ERROR_FONT);
            g.drawString(NO_GENES_STR, 10, 30);
            return;
        }
        
        Rectangle bounds = g.getClipBounds();
        final int top = getTopIndex(bounds.y);
        final int bottom = getBottomIndex(bounds.y+bounds.height, getCluster().length);
        final int left = getLeftIndex(bounds.x);
        final int right = getRightIndex(bounds.x+bounds.width, samples);
        
        int x, y;
        // draw rectangles
        for (int column=left; column<right; column++) {
            for (int row=top; row<bottom; row++) {
                fillRectAt(g, row, column);
            }
        }
        Color initColor = g.getColor();
        
        if(haveColorBar)
        	fillClusterColorPositions(g);
        
        // draw annotations
        if (this.isDrawAnnotations) {
            if (this.isAntiAliasing) {
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
            if (right >= samples) {
                String label = "";
                g.setColor(Color.black);
                int uniqX = elementSize.width*samples+10;
                
                if(haveColorBar)
	                if (!isCompact){
	                	uniqX += this.elementSize.width*storedGeneColors.size();
	                	colorWidth=storedGeneColors.size();
	                }
	                if(isCompact){
	            		int maxSpacesOver=-1;
	            		for (int i=0; i<storedGeneColors.size(); i++){
	            			if ((ColorOverlaps[i])>maxSpacesOver)
	            				maxSpacesOver=ColorOverlaps[i];
	            		}
	            		colorWidth=maxSpacesOver+1;
	            		uniqX += this.elementSize.width*(maxSpacesOver+1);
                
	            	}
                int annY;
                String[]annot=new String[] {""};
                int fieldNamesLength=data.getFieldNames().length-1;
                for (int row=top; row<bottom; row++) {
                    if (labelIndex >= 0) {
                      //  label = data.getElementAttribute(getMultipleArrayDataRow(row), labelIndex);
                       
                    	/*Added by Sarita
                         * These two if loops were introduced to check if the annotation
                         * selected from the Display Menu was a part of the "Extrafields"
                         * or the new annotation Model. The functions are called accordingly
                         * 
                         */
                    	
                    	if(labelIndex <= data.getFieldNames().length-1) {
                    		annot[0] = data.getElementAttribute(getMultipleArrayDataRow(row), labelIndex);
                    		//System.out.println("Extra Field selected is:"+annot[0]);
                    	}
                    	else {
                    	//	System.out.println("Annotation selected is:"+MevAnnotation.getFieldNames()[labelIndex-fieldNamesLength-1]);
                    		annot= data.getElementAnnotation(getMultipleArrayDataRow(row), MevAnnotation.getFieldNames()[labelIndex-fieldNamesLength-1]);
                    	}
                    }
                    annY = (row+1)*elementSize.height;
                    g.drawString(annot[0], uniqX + insets.left, annY-1);
                }
            }
        }

        if (mouseOnMap){
            drawRectAt(g, mouseRow, mouseColumn, Color.white);
            if (haveColorBar&&isShowRects)
            drawClusterRectsAt(g, mouseRow, mouseColumn, Color.gray);
        }
        mouseOnMap=false;
        if (clickedCell){
            g.setColor(Color.red);
            if (!isCompact){
            	drawClusterRectsAt(g,clickedRow,clickedColumn, Color.red);
            }
        }  
        if (inDrag){
        	g.setColor(Color.blue);
    		g.drawRect(dragColumn*elementSize.width + insets.left +5-1, -1, (elementSize.width), elementSize.height*getCluster().length+1);
        	header.drawClusterHeaderRectsAt(dragColumn, Color.blue, true);
        }
        
        if (colorWidth!=oldWidth)
        	updateSize();
    }
    
    /**
     * Fills rect with specified row and column.
     */
    private void fillRectAt(Graphics g, int row, int column) {
        if (column > (experiment.getNumberOfSamples() -1))
        	return;
        int x = column*elementSize.width + insets.left;
        int y = row*elementSize.height;
        boolean mask = this.firstSelectedRow >= 0 && this.lastSelectedRow >= 0 && (row < this.firstSelectedRow || row > this.lastSelectedRow);
        mask = (mask || this.firstSelectedColumn >= 0 && this.lastSelectedColumn >= 0 && (column < this.firstSelectedColumn || column > this.lastSelectedColumn));
        
        g.setColor(getColor(this.experiment.get(getExperimentRow(row), getColumn(column))));
        g.fillRect(x, y, elementSize.width, elementSize.height);
        if (mask) {
            g.setColor(maskColor);
            g.fillRect(x, y, elementSize.width, elementSize.height);
        }
        if (this.isDrawBorders) {
            g.setColor(Color.black);
            g.drawRect(x, y, elementSize.width-1, elementSize.height-1);
        }
    }
      
    /**
     * fills cluster colors
     */
    private void fillClusterRectAt(Graphics g, int row, int xLoc, Color color) {
        //Color geneColor = data.getProbeColor(getMultipleArrayDataRow(row));
        //if(geneColor == null)
        //    geneColor = Color.white; 
    	
    	//g.setColor(geneColor);
    	if(color == null)
            color = Color.white;
    	
    	g.setColor(color);
        
        g.fillRect(xLoc + insets.left, row*elementSize.height, elementSize.width-1, elementSize.height);
    }
    /**
     * Determines the location of the cluster colors for either compact
     * or non-compact settings and then fills the appropriate rectangles
     */
    private void fillClusterColorPositions(Graphics g){
        final int samples = experiment.getNumberOfSamples();
        Rectangle bounds = g.getClipBounds();
        final int top = getTopIndex(bounds.y);
        final int bottom = getBottomIndex(bounds.y+bounds.height, getCluster().length);

    	
    	int spacesOver=0;
    	for (int row=top; row<bottom; row++) {
    		Color[] colors = data.getGeneColorArray(getMultipleArrayDataRow(row));
    		if (colors==null) { continue;}
            for (int clusters=0; clusters<colors.length; clusters++){
            	if (colors[clusters]==null) {continue;}
            	if(storedGeneColors.contains(colors[clusters])) {
                	activeCluster=storedGeneColors.indexOf(colors[clusters]);
                }
                else{
	                storedGeneColors.add(colors[clusters]);
	                activeCluster=(storedGeneColors.size()-1);
                	ColorOverlaps[activeCluster]= activeCluster;
                	//compacts the cluster color display
                	boolean foundit= false;
                	if (!isCompact)foundit=true;
                	while (!foundit){
                		for (int i=0; i<storedGeneColors.size(); i++){
                			boolean allClear = true;
                			for (int j=0; j<storedGeneColors.size(); j++){
                				if (ColorOverlaps[j]==i){
    			                	if (data.isColorOverlap(getMultipleArrayDataRow(row), colors[clusters], (Color)storedGeneColors.get(j), true)){
    			                		allClear=false;
    			                		break;
    			                		}
    		                			allClear=true;
    		                		}	
                				}
                			if (allClear){
                				ColorOverlaps[activeCluster]= i;
                				foundit=true;
                				break;
                			}
                			}
                			if (foundit) break;
                		}
                }
                spacesOver=ColorOverlaps[activeCluster];
                int expWidth = samples * this.elementSize.width + 5 + this.elementSize.width*spacesOver;
                fillClusterRectAt(g, row, expWidth, colors[clusters]);
            }
        }
    }
    
    /**
     * Draws rect with specified row, column and color.
     */
    private void drawRectAt(Graphics g, int row, int column, Color color) {
        g.setColor(color);
        if (column>=experiment.getNumberOfSamples()){
        	return;
        }
        else{
        g.drawRect(column*elementSize.width + insets.left, row*elementSize.height, elementSize.width-1, elementSize.height-1);
    }
    }
    private void drawClusterRectsAt(Graphics g, int row, int column, Color color){
    	g.setColor(color);
    	if (column>=experiment.getNumberOfSamples()){
    		g.drawRect((experiment.getNumberOfSamples())*elementSize.width + insets.left +5-1, row*elementSize.height-1, (elementSize.width)*(colorWidth)+this.annotationWidth +8, elementSize.height+1);
    		if (isCompact) return;
    		g.drawRect(column*elementSize.width + insets.left +5-1, -1, (elementSize.width), elementSize.height*getCluster().length+1);
        	header.drawClusterHeaderRectsAt(column, color, true);
        	
    	}
    	else{
    		g.drawRect((experiment.getNumberOfSamples())*elementSize.width + insets.left +5-1, row*elementSize.height-1, (elementSize.width)*(colorWidth)+this.annotationWidth +8, elementSize.height+1);
    		header.drawClusterHeaderRectsAt(column, color, false);
    	}
    }
    
    private int getTopIndex(int top) {
        if (top < 0) {
            return 0;
        }
        return top/elementSize.height;
    }
    
    private int getLeftIndex(int left) {
        if (left < insets.left) {
            return 0;
        }
        return (left - insets.left)/elementSize.width;
    }
    
    private int getRightIndex(int right, int limit) {
        if (right < 0) {
            return 0;
        }
        int result = right/elementSize.width+1;
        return result > limit ? limit : result;
    }
    
    private int getBottomIndex(int bottom, int limit) {
        if (bottom < 0) {
            return 0;
        }
        int result = bottom/elementSize.height+1;
        return result > limit ? limit : result;
    }
    
    /**
     * Finds column for specified x coordinate.
     * @return -1 if column was not found.
     */
    private int findColumn(int targetx) {
        int xSize = experiment.getNumberOfSamples()*elementSize.width;
        if (targetx < insets.left) {
            return -1;
        }
        if (targetx >= (xSize + insets.left) && (targetx < (xSize + insets.left+this.elementSize.width*colorWidth + 10)))
        	return (targetx - insets.left-5)/elementSize.width;
        return (targetx - insets.left)/elementSize.width;
    }
    
    /**
     * Finds row for specified y coordinate.
     * @return -1 if row was not found.
     */
    private int findRow(int targety) {
        int ySize = getCluster().length*elementSize.height;
        if (targety >= ySize || targety < 0)
            return -1;
        return targety/elementSize.height;
    }
    
    private boolean isLegalPosition(int row, int column) {
        if (isLegalRow(row) && isLegalColumn(column))
            return true;
        return false;
    }
    
    private boolean isLegalColumn(int column) {
        if (column < 0 || column > (experiment.getNumberOfSamples() -1+colorWidth))
            return false;
        return true;
    }
    
    private boolean isLegalRow(int row) {
        if (row < 0 || row > getCluster().length -1)
            return false;
        return true;
    }
    
    /**
     * Adds viewer specific menu items.
     */
    protected void addMenuItems(JPopupMenu menu, ActionListener listener) {
        JMenuItem menuItem;
        menuItem = new JMenuItem("Store entire cluster", GUIFactory.getIcon("new16.gif"));
        menuItem.setActionCommand(STORE_CLUSTER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("Store clusters by gene annotation", GUIFactory.getIcon("new16.gif"));
        menuItem.setActionCommand(AUTO_STORE_GENE_CLUSTERS_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Store clusters by sample annotation", GUIFactory.getIcon("new16.gif"));
        menuItem.setActionCommand(AUTO_STORE_SAMPLE_CLUSTERS_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Launch new session", GUIFactory.getIcon("launch_new_mav.gif"));
        menuItem.setActionCommand(LAUNCH_NEW_SESSION_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Delete public cluster", GUIFactory.getIcon("delete16.gif"));
        menuItem.setActionCommand(SET_DEF_COLOR_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Save cluster...", GUIFactory.getIcon("save16.gif"));
        menuItem.setActionCommand(SAVE_CLUSTER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Save all clusters...", GUIFactory.getIcon("save16.gif"));
        menuItem.setActionCommand(SAVE_ALL_CLUSTERS_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Broadcast Matrix to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
        menuItem.setActionCommand(BROADCAST_MATRIX_GAGGLE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Broadcast Gene List to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
        menuItem.setActionCommand(BROADCAST_NAMELIST_GAGGLE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
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
    
    
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return Cluster.GENE_CLUSTER;
    }
    
    /**
     * The class to listen to mouse events.
     */
    private class Listener extends MouseAdapter implements MouseMotionListener {
        
        private String oldStatusText;
        private int oldRow = -1;
        private int oldColumn = -1;
        private int startColumn = 0;
        private int startRow = 0;
        
        public void mouseClicked(MouseEvent event) {
            if (SwingUtilities.isRightMouseButton(event)) {
                return;
            }
            int column = findColumn(event.getX());
            int row = findRow(event.getY());
            if (!isLegalPosition(row, column)) {
                return;
            }
            if (column>experiment.getNumberOfSamples()-1){
	            if (row==clickedRow&&column==clickedColumn){
	            	clickedCell = !clickedCell;
	            	header.clusterViewerClicked = clickedCell;
	            	return;
	            }
	            clickedRow = row;
	            clickedColumn = column;
	            clickedCell = true;
	            header.clusterViewerClickedColumn = column;
	            header.clusterViewerClicked = true;
	            if (isCompact){
	            	clickedCell = false;
	        		header.clusterViewerClicked = false;
	            }
	            repaint();
        		return;
        	}
            if (event.isControlDown()) { // single array viewer
                framework.displaySingleArrayViewer(experiment.getSampleIndex(getColumn(column)));
                return;
            }
            if (!event.isShiftDown()) { // element info
                framework.displaySlideElementInfo(experiment.getSampleIndex(getColumn(column)), getMultipleArrayDataRow(row));
                return;
            }
        }
        
        public void mouseMoved(MouseEvent event) {
            if (experiment.getNumberOfSamples() == 0 || event.isShiftDown())
                return;
            int column = findColumn(event.getX());
            int row = findRow(event.getY());
            Graphics g = null;
            g = getGraphics();
            //mouse on same rectangle
            if (isCurrentPosition(row, column)) {
                if (isLegalPosition(row, column)&&isShowRects)
                	drawClusterRectsAt(g, oldRow, oldColumn, Color.gray);
                return;
            }
            //mouse on heat map
            if (isLegalPosition(row, column)&& (column < experiment.getNumberOfSamples())) {
                drawRectAt(g, row, column, Color.white);
                if (isShowRects)
                	drawClusterRectsAt(g, row, column, Color.gray);
                framework.setStatusText("Gene: "+data.getUniqueId(getMultipleArrayDataRow(row))+" Sample: "+data.getSampleName(experiment.getSampleIndex(getColumn(column)))+" Value: "+experiment.get(getExperimentRow(row), getColumn(column)));
            }
            //mouse on different rectangle, but still on the map
            if (!isCurrentPosition(row, column)&&isLegalPosition(row, column)){
            	mouseOnMap = true;
            	mouseRow = row;
            	mouseColumn = column;
            	repaint();
            }
            //mouse on cluster part of map
            else {
            	repaint();
                framework.setStatusText(oldStatusText);
            }
            if (isLegalPosition(oldRow, oldColumn)) {
                g = g != null ? g : getGraphics();
                fillRectAt(g, oldRow, oldColumn);
            }
            setOldPosition(row, column);
            if (g != null) {
                g.dispose();
            }
        }
        
        public void mouseEntered(MouseEvent event) {
        	try {
            oldStatusText = framework.getStatusText();
        	} catch (NullPointerException npe) {
        		npe.printStackTrace();
        	}
        }
        
        public void mouseExited(MouseEvent event) {

        	mouseOnMap = false;
        	header.setDrag(false, 0, 0);
        	inDrag = false;
        	repaint();
        	if (isLegalPosition(oldRow, oldColumn)) {
                Graphics g = getGraphics();
                fillRectAt(g, oldRow, oldColumn);
                g.dispose();
            }
            setOldPosition(-1, -1);
            framework.setStatusText(oldStatusText);
            repaint();
        }
        
        public void mouseDragged(MouseEvent event) {
        	repaint();
            if (SwingUtilities.isRightMouseButton(event)) {
                return;
            }
            int column = findColumn(event.getX());
            int row = findRow(event.getY());
            if (!isLegalPosition(row, column)) {
            	inDrag = false;
            	header.setDrag(false, 0, 0);
                return;
            }
            if (!inDrag)
            	return;
            dragColumn = column;
            dragRow = row;
            header.setDrag(true, dragColumn, dragRow);
        	if (column>=experiment.getNumberOfSamples()){
        		//Graphics g = getGraphics();
        		//g.drawRect((experiment.getNumberOfSamples())*elementSize.width + insets.left +5-1, row*elementSize.height-1, (elementSize.width)*(colorWidth)+annotationWidth +8, elementSize.height+1);
        		//if (isCompact) return;
        		//g.setColor(Color.blue);
        		//g.drawRect(column*elementSize.width + insets.left +5-1, -1, (elementSize.width), elementSize.height*getCluster().length+1);
            	//header.drawClusterHeaderRectsAt(column, Color.blue, true);
        	} else{
        		inDrag = false;
        		header.setDrag(false, 0, 0);
        	}
        }
        /** Called when the mouse has been pressed. */
        public void mousePressed(MouseEvent event) {
            if (SwingUtilities.isRightMouseButton(event)) {
                return;
            }

            startColumn = findColumn(event.getX());
            startRow = findRow(event.getY());
            if (!isLegalPosition(startRow, startColumn)) {
                return;
            }
            inDrag = true;

            dragColumn = startColumn;
            dragRow = startRow;
            header.setDrag(true, startColumn, startRow);
        }

        /** Called when the mouse has been released. */
        public void mouseReleased(MouseEvent event) {
	        if (!inDrag)
	        	return;
        	inDrag = false;
	        header.setDrag(false, 0,0);
	        int endColumn = findColumn(event.getX());
	        if (endColumn < experiment.getNumberOfSamples())
	        	return;
	        int endRow = findRow(event.getY());
	        if (!isLegalPosition(startRow, startColumn)) {
	            return;
	        }
	      	if (!isCompact){
		      	Color inter = (Color)storedGeneColors.get(startColumn-experiment.getNumberOfSamples());
		      	storedGeneColors.remove(startColumn-experiment.getNumberOfSamples());
		      	storedGeneColors.add(endColumn-experiment.getNumberOfSamples(), inter);
		      	repaint();
	      	}else{
	      		for (int j=0; j<storedGeneColors.size(); j++){
	      			if (ColorOverlaps[j]==startColumn-experiment.getNumberOfSamples())
	      				ColorOverlaps[j]=-1;
	      			if (ColorOverlaps[j]==endColumn-experiment.getNumberOfSamples())
	      				ColorOverlaps[j]=startColumn-experiment.getNumberOfSamples();
	      			if(ColorOverlaps[j]== -1)
	      				ColorOverlaps[j]=endColumn-experiment.getNumberOfSamples();
	      		}
	      		repaint();
	      	}
        }
        
        private void setOldPosition(int row, int column) {
            oldColumn = column;
            oldRow = row;
        }
        
        private boolean isCurrentPosition(int row, int column) {
            return(row == oldRow && column == oldColumn);
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

	/**
	 * Broadcasts the current cluster's expression values to the Gaggle network.
	 *
	 */
    public void broadcastClusterGaggle() {
    	int[] temp = getCluster();
    	Experiment e = getExperiment();
    	if (temp == null)
    		System.out.println("getCluster returns null");
    	if(e == null)
    		System.out.println("getExperiment returns null");
    	if(framework == null)
    		System.out.println(this.toString() + ": framework is null");
    	framework.broadcastGeneCluster(getExperiment(), getCluster(), null);
	}
    public void broadcastNamelistGaggle() {
    	framework.broadcastNamelist(getExperiment(), getCluster());
    }
    public void setFramework(IFramework framework) {
    	this.framework = framework;
    }
	/**
	 * Creates a popup menu.
	 */
	protected JPopupMenu createJPopupMenu(ActionListener listener) {
	    JPopupMenu popup = new JPopupMenu();
	    addMenuItems(popup, listener);
	    return popup;
	}

	/**
	 * Saves clusters.
	 */
	protected void onSaveClusters() {
	    Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
	    try {
	        saveClusters(frame);
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(frame, "Can not save clusters!", e.toString(), JOptionPane.ERROR_MESSAGE);
	        e.printStackTrace();
	    }
	}
	/**
	 * Save the viewer cluster.
	 */
	protected void onSaveCluster() {
	    Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
	    try {
	        saveCluster(frame);
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
	        e.printStackTrace();
	    }
	}
	/**
	 * Sets a public color.
	 */
	protected void onSetColor() {
	    Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
	    Color newColor = JColorChooser.showDialog(frame, "Choose color", CentroidViewer.DEF_CLUSTER_COLOR);
	    if (newColor != null) {
	        setClusterColor(newColor);
	    }
	}
	/**
	 * Removes a public color.
	 */
	protected void onSetDefaultColor() {
	    setClusterColor(null);
	}

	/**
	 * The class to listen to mouse and action events.
	 */
	public class PopupListener extends MouseAdapter implements ActionListener {
	    
	    public void actionPerformed(ActionEvent e) {
	        String command = e.getActionCommand();
	        if (command.equals(SAVE_CLUSTER_CMD)) {
	            onSaveCluster();
	        } else if (command.equals(SAVE_ALL_CLUSTERS_CMD)) {
	            onSaveClusters();
	        } else if (command.equals(STORE_CLUSTER_CMD)) {
	            storeCluster();
	        } else if (command.equals(AUTO_STORE_GENE_CLUSTERS_CMD)) {
	            autoStoreClusters(ClusterRepository.GENE_CLUSTER, labelIndex);
	        } else if (command.equals(AUTO_STORE_SAMPLE_CLUSTERS_CMD)) {
	            autoStoreClusters(ClusterRepository.EXPERIMENT_CLUSTER, 0);
	        } else if (command.equals(SET_DEF_COLOR_CMD)) {
	            onSetDefaultColor();
	        } else if(command.equals(LAUNCH_NEW_SESSION_CMD)){
	            launchNewSession();
	        } else if (command.equals(BROADCAST_MATRIX_GAGGLE_CMD)) {
	            broadcastClusterGaggle();
	        } else if (command.equals(BROADCAST_NAMELIST_GAGGLE_CMD)) {
	            broadcastNamelistGaggle();
	        }
	    }
	    
	    public void mouseReleased(MouseEvent event) {
	        maybeShowPopup(event);
	    }
	    
	    public void mousePressed(MouseEvent event) {
	        maybeShowPopup(event);
	    }
	    
	    private void maybeShowPopup(MouseEvent e) {
	        
	        if (!e.isPopupTrigger() || getCluster() == null || getCluster().length == 0) {
	            return;
	        }
	        popup.show(e.getComponent(), e.getX(), e.getY());
	    }
	}
}

