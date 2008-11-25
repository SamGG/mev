/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ExperimentClusterViewer.java,v $
 * $Revision: 1.13 $
 * $Date: 2007-12-20 22:12:44 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Font;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.Expression;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;
import java.util.ArrayList;

import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

import org.tigr.microarray.mev.cluster.clusterUtil.*;

public class ExperimentClusterViewer extends JPanel implements IViewer {
    
    private static final float INITIAL_MAX_VALUE = 3f;
    private static final float INITIAL_MIN_VALUE = -3f;
    
    private static final String NO_EXPERIMENT_STR = "No Experiments in Cluster!";
    private static final Font ERROR_FONT = new Font("monospaced", Font.BOLD, 20);
    protected static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
    protected static final String SET_DEF_COLOR_CMD = "set-def-color-cmd";
    protected static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
    protected static final String AUTO_STORE_GENE_CLUSTERS_CMD = "auto-store-gene-cluster-cmd";
    protected static final String AUTO_STORE_SAMPLE_CLUSTERS_CMD = "auto-store-sample-cluster-cmd";
    protected static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";
    protected static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";
    public static final String BROADCAST_MATRIX_GAGGLE_CMD = "broadcast-matrix-to-gaggle";
    public static final String BROADCAST_NAMELIST_GAGGLE_CMD = "broadcast-namelist-to-gaggle";
    
    private ExperimentClusterHeader header;
    private Experiment experiment;
    private IFramework framework;
    private IData data;
    private int clusterIndex;
    private int[][] clusters;
    private int[] genesOrder;
    private Dimension elementSize = new Dimension(20, 5);
    private int labelIndex = -1;
    private boolean isAntiAliasing = true;
    private boolean isDrawBorders = true;
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
    private boolean hasCentroid = false;
    private float [][] centroids;
    private int contentWidth;
    
    private boolean showClusters = true;
    private boolean haveColorBar = false;
    
    private boolean useDoubleGradient = true;
    private int exptID = 0;
    
    private static ArrayList<Color> storedGeneColors=new ArrayList<Color>();
    public static ArrayList<Color> savedGeneColorOrder = new ArrayList<Color>();
    private int activeCluster = 0;
    private static int[] ColorOverlaps = new int[100000];
    private boolean isCompact = false;
    private boolean isAutoArrangeColors = true;
    private boolean isShowRects = true;
    private int colorWidth = 0;
    private int maxColorWidth = 0;
	boolean mouseOnMap = false;
	int mouseRow = 0;
	int mouseColumn = 0;
    private boolean clickedCell = false;
    private int clickedColumn = 0;
    private int clickedRow = 0;
    
    private boolean inDrag = false;
    private int dragRow = 0;
    private int dragColumn = 0;
    private JPopupMenu popup;
    
    /**
     * Constructs an <code>ExperimentClusterViewer</code> with specified
     * experiment and clusters.
     *
     * @param experiment the experiment data.
     * @param clusters the two dimensional array with spots indices.
     */
    public ExperimentClusterViewer(Experiment experiment, int[][] clusters) {
        this(experiment, clusters, true);
    }
    
    /**
     * Constructs an <code>ExperimentClusterViewer</code> with specified
     * experiment, clusters and draw annotations attribute.
     *
     * @param experiment the experiment data.
     * @param clusters the two dimensional array with spots indices.
     * @param drawAnnotations true if this viewer must draw annotations.
     */
    public ExperimentClusterViewer(Experiment experiment, int[][] clusters, boolean drawAnnotations) {
        this(experiment, clusters, null, drawAnnotations);
    }
    
    /**
     * Constructs an <code>ExperimentClusterViewer</code> with specified
     * experiment, clusters, samples order and draw annotations attribute.
     *
     * @param experiment the experiment data.
     * @param clusters the two dimensional array with spots indices.
     * @param samplesOrder the one dimensional array with samples indices.
     * @param drawAnnotations true if this viewer must draw annotations.
     */
    public ExperimentClusterViewer(Experiment experiment, int[][] clusters, int[] genesOrder, boolean drawAnnotations) {
        if (experiment == null) {
            throw new IllegalArgumentException("experiment == null");
        }
        this.experiment = experiment;
        this.exptID = experiment.getId();
        this.clusters = clusters == null ? defSamplesOrder(experiment.getNumberOfSamples()) : clusters;
        this.genesOrder = genesOrder == null ? defGenesOrder(experiment.getNumberOfGenes()) : genesOrder;
        this.isDrawAnnotations = drawAnnotations;
        this.header = new ExperimentClusterHeader(this.experiment, this.clusters, this.storedGeneColors);
        this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
        setBackground(Color.white);
        Listener listener = new Listener();
        addMouseListener(listener);
        addMouseMotionListener(listener);
        

		PopupListener popupListener = new PopupListener();
		this.popup = createJPopupMenu(popupListener);
		getContentComponent().addMouseListener(popupListener);
		getHeaderComponent().addMouseListener(popupListener);
    }
    /*
    copy-paste this constructor into descendent classes
    /**
     * @inheritDoc
     *
    public ExperimentClusterViewer(int[][] clusters, int[] genesOrder, Boolean drawAnnotations, 
    		Integer offset, ExperimentClusterHeader header, Boolean hasCentroid, float[][] centroids, 
			Dimension elementSize, Integer labelIndex, Integer exptID) {
    		super(clusters, genesOrder, drawAnnotations, offset, header, hasCentroid, centroids, elementSize, labelIndex, exptID);
    }
    */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new",
    			new Object[]{experiment, clusters, genesOrder, new Boolean(isDrawAnnotations), new Integer(this.insets.left)});
    } 
    public ExperimentClusterViewer(Experiment experiment, int[][] clusters, int[] genesOrder, Boolean drawAnnotations, Integer offset){
    	this(experiment, clusters, genesOrder, drawAnnotations.booleanValue(), offset.intValue());
    }
    /**
     * Constructs an <code>ExperimentClusterViewer</code> with specified
     * experiment, clusters, samples order and draw annotations attribute.
     *
     * @param experiment the experiment data.
     * @param clusters the two dimensional array with spots indices.
     * @param samplesOrder the one dimensional array with samples indices.
     * @param drawAnnotations true if this viewer must draw annotations.
     */
    public ExperimentClusterViewer(Experiment experiment, int[][] clusters, int[] genesOrder, boolean drawAnnotations, int offset) {
        if (experiment == null) {
            throw new IllegalArgumentException("experiment == null");
        }
        this.experiment = experiment;
        this.exptID = experiment.getId();
        this.clusters = clusters == null ? defSamplesOrder(experiment.getNumberOfSamples()) : clusters;
        this.genesOrder = genesOrder == null ? defGenesOrder(experiment.getNumberOfGenes()) : genesOrder;
        this.insets.left = offset;
        this.isDrawAnnotations = drawAnnotations;
        this.header = new ExperimentClusterHeader(this.experiment, this.clusters, this.storedGeneColors);
        this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
        this.header.setLeftInset(offset);
        setBackground(Color.white);
        Listener listener = new Listener();
        addMouseListener(listener);
        addMouseMotionListener(listener);

		PopupListener popupListener = new PopupListener();
		this.popup = createJPopupMenu(popupListener);
		getContentComponent().addMouseListener(popupListener);
		getHeaderComponent().addMouseListener(popupListener);
    }
    
    
    /**
     * Constructs an <code>ExperimentClusterViewer</code> with specified
     * experiment, clusters, samples order and draw annotations attribute.
     *
     * @param experiment the experiment data.
     * @param clusters the two dimensional array with spots indices.
     * @param samplesOrder the one dimensional array with samples indices.
     * @param drawAnnotations true if this viewer must draw annotations.
     */
    public ExperimentClusterViewer(Experiment experiment, int[][] clusters, String centroidName, Vector vector) {
        this(experiment, clusters, centroidName, getCentroidArray(clusters, vector));
    }
    
    /**
     * Constructs an <code>ExperimentClusterViewer</code> with specified
     * experiment, clusters, samples order and draw annotations attribute.
     *
     * @param experiment the experiment data.
     * @param clusters the two dimensional array with spots indices.
     * @param samplesOrder the one dimensional array with samples indices.
     * @param drawAnnotations true if this viewer must draw annotations.
     */
    public ExperimentClusterViewer(Experiment experiment, int[][] clusters, String centroidName, float [][] centroids) {
        if (experiment == null) {
            throw new IllegalArgumentException("experiment == null");
        }
        this.experiment = experiment;
        this.exptID = experiment.getId();
        this.clusters = clusters == null ? defSamplesOrder(experiment.getNumberOfSamples()) : clusters;
        this.genesOrder = genesOrder == null ? defGenesOrder(experiment.getNumberOfGenes()) : genesOrder;
        this.insets.left = 10;
        this.isDrawAnnotations = true;
        if(centroidName != null){
            hasCentroid = true;
            this.centroids = centroids;
        }
        this.header = new ExperimentClusterHeader(this.experiment, this.clusters, centroidName, this.storedGeneColors);
        this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
        setBackground(Color.white);
        Listener listener = new Listener();
        addMouseListener(listener);
        addMouseMotionListener(listener);

		PopupListener popupListener = new PopupListener();
		this.popup = createJPopupMenu(popupListener);
		getContentComponent().addMouseListener(popupListener);
		getHeaderComponent().addMouseListener(popupListener);
    }
 
    /**
     * Builds an ExperimentClusterViewer in the state specified by an xml file.  Used by XMLDecoder to restore the saved
     * state of an ExperimentClusterViewer.  This constructor must work in concert with the setExperiment() 
     * method.  
     * 
     * @param clusters
     * @param genesOrder
     * @param drawAnnotations
     * @param offset
     * @param header
     * @param hasCentroid
     * @param centroids
     * @param elementSize
     * @param labelIndex
     * @param exptID
     
    public ExperimentClusterViewer(int[][] clusters, int[] genesOrder, Boolean drawAnnotations, 
    		Integer offset, ExperimentClusterHeader header, Boolean hasCentroid, float[][] centroids, 
			Dimension elementSize, Integer labelIndex, Integer exptID) {
    	this.clusters = clusters;
        this.genesOrder = genesOrder;
        this.insets.left = offset.intValue();
        this.isDrawAnnotations = drawAnnotations.booleanValue();
        this.hasCentroid = hasCentroid.booleanValue();
        this.centroids = centroids;
        this.elementSize = elementSize;
        this.labelIndex = labelIndex.intValue();
        this.header = header;
        this.header.setData(data);
        this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
        this.header.setLeftInset(offset.intValue());
        this.exptID = exptID.intValue();
        setBackground(Color.white);
        Listener listener = new Listener();
        addMouseListener(listener);
        addMouseMotionListener(listener);
    }
*/
    public void setExperiment(Experiment e) {
    	this.experiment = e;
        this.exptID = e.getId();
        this.header.setExperiment(e);
    }

    private static int[][] defSamplesOrder(int size) {
        int[][] order = new int[1][size];
        for (int i=0; i<order[0].length; i++) {
            order[0][i] = i;
        }
        return order;
    }
    
    private static int[] defGenesOrder(int size) {
        int[] order = new int[size];
        for (int i=0; i<order.length; i++) {
            order[i] = i;
        }
        return order;
    }
    
    private static float [][] getCentroidArray(int [][] clusters, Vector vector){
        int len = vector.size();
        float [][] array = new float[clusters.length][len];
        for(int i = 0; i < clusters.length; i++){
            for(int j = 0; j < len; j++){
                array[i][j] = ((Float)(vector.elementAt(j))).floatValue();
            }
        }
        return array;
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
     * Get current number of experiments
     */
    public int getCurrentNumberOfExperiments(){
        return getClusters()[clusterIndex].length;
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
        this.useDoubleGradient = menu.getUseDoubleGradient();
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
        setAutoArrangeColors(!menu.isAutoArrangeColors());
        setShowRects(menu.isShowRects());
        header.isShowRects = isShowRects;
        header.setCompactClusters(menu.isCompactClusters());
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
        //header.updateSizes(header.getSize().width, elementSize.width);
        header.setData(data);
        //onMenuChanged(menu);
        //header.setValues(maxValue, minValue);
        header.setValues(minValue, midValue, maxValue);
        header.setAntiAliasing(menu.isAntiAliasing());
        header.updateSizes(getSize().width, elementSize.width);
        header.repaint();
    }
    
    /**
     * Updates appropriate attributes of this viewer and its header.
     * @see IViewer#onMenuChanged
     */
    public void onMenuChanged(IDisplayMenu menu) {
    	header.clusterViewerClicked = false;
    	header.clickedCell = false;
    	clickedCell = false;
    	boolean isCompactChanged= isCompact;
    	boolean isAutoArrangeChanged=isAutoArrangeColors;
    	this.useDoubleGradient = menu.getUseDoubleGradient();
        header.setUseDoubleGradient(useDoubleGradient);    	
        setDrawBorders(menu.isDrawingBorder());
        setCompactClusters(menu.isCompactClusters());
        setAutoArrangeColors(!menu.isAutoArrangeColors());
        setShowRects(menu.isShowRects());
        header.isShowRects = isShowRects;
        header.setCompactClusters(menu.isCompactClusters());
        if(((!isAutoArrangeColors==isAutoArrangeChanged))||isAutoArrangeColors){
        storedGeneColors.clear();
        	header.clearStoredSampleColors();
        }
        this.maxValue = menu.getMaxRatioScale();
        this.minValue = menu.getMinRatioScale();
        this.midValue = menu.getMidRatioValue();
        //header.setValues(maxValue, minValue);
        header.setValues(minValue, midValue, maxValue);

        header.updateSizes(getSize().width, elementSize.width);

        updateSize();
        this.posColorImage = menu.getPositiveGradientImage();
        this.negColorImage = menu.getNegativeGradientImage();
        this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
        if (this.elementSize.equals(menu.getElementSize()) &&
        labelIndex == menu.getLabelIndex() &&
        this.isAntiAliasing == menu.isAntiAliasing()) {
            return;
        }
        setElementSize(menu.getElementSize());
        setAntialiasing(menu.isAntiAliasing());
        labelIndex = menu.getLabelIndex();
        updateSize();
        header.setAntiAliasing(menu.isAntiAliasing());
        header.updateSizes(getSize().width, elementSize.width);
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
     * returns true if a probe in the current viewer has color
     */
    protected  boolean areProbesColored() {
        int [] indices = this.genesOrder;
        for(int i = 0; i < indices.length; i++){
            if( this.data.getProbeColor(this.getMultipleArrayDataRow(i)) != null){
                return true;
            }
        }
        return false;
    }
    
    /**
     *	Returns the row (index) within the main iData which corresponds to
     *  the passed index to the clusters array
     */
    private int getMultipleArrayDataRow(int clusterArrayRow) {
        return experiment.getGeneIndexMappedToData(genesOrder[clusterArrayRow]);
    }
    
    /**
     *	Returns the row index in the experiment's <code>FloatMatrix<\code>
     *  corresponding to the passed index to the clusters array
     */
    private int getExperimentRow(int row){
        return genesOrder[row];
    }
    
    
    
    private int getColumn(int column) {
        return this.clusters[this.clusterIndex][column];
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
    protected IData getData() {
        return data;
    }
    
    /**
     * Sets public color for the current cluster.
     */
    public void setClusterColor(Color color) {
        if(color == null){  //indicates removal of cluster
            framework.removeCluster(getCluster(), experiment, ClusterRepository.EXPERIMENT_CLUSTER);
        } 
    }
    
    /**
     * Sets public color for the current cluster.
     */
    public void storeCluster() {
        framework.storeCluster(getCluster(), experiment, ClusterRepository.EXPERIMENT_CLUSTER);
        header.updateSizes(getSize().width, elementSize.width);
        this.onDataChanged(this.data);
    }
    
    /**
     * Sets public color for the current cluster.
     */
    public Color setHCLClusterColor(int [] clusterIndices, Color color, boolean isGeneHCLCluster) {
     //   if(isGeneHCLCluster)
    //        this.data.setProbesColor(clusterIndices, color);
    //    else
    //        this.data.setExperimentColor(clusterIndices, color);
        
        Color clusterColor = null;
        if(isGeneHCLCluster)
            clusterColor = framework.storeCluster(clusterIndices, experiment, ClusterRepository.GENE_CLUSTER);
        else
            clusterColor = framework.storeCluster(clusterIndices, experiment, ClusterRepository.EXPERIMENT_CLUSTER);

        
        this.selectColumns(-1,-1);
        this.selectRows(-1,-1);
        header.updateSizes(getSize().width, elementSize.width);
        this.header.repaint();
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
     * Automatically stores all clusters by annotation
     */
    public void autoStoreClusters(int clusterType, int index){
    	framework.autoStoreClusters(clusterType, index);
    }
    /**
     * Sets auto-arrange attribute
     */
    private void setAutoArrangeColors(boolean value){
    	this.isAutoArrangeColors = value;
    }
    /**
     * Saves all the clusters.
     */
    public void saveClusters(Frame frame) throws Exception {
        frame = frame == null ? JOptionPane.getFrameForComponent(this) : frame;
        ExperimentUtil.saveAllExperimentClusters(frame, getExperiment(), getData(), getClusters());
    }
    
    /**
     * Saves current cluster.
     */
    public void saveCluster(Frame frame) throws Exception {
        frame = frame == null ? JOptionPane.getFrameForComponent(this) : frame;
        ExperimentUtil.saveExperimentCluster(frame, getExperiment(), getData(), getCluster());
    }
    
    /**
     * Launches a new <code>MultipleExperimentViewer</code> containing the current cluster
     */
    public void launchNewSession(){
        framework.launchNewMAV(getCluster(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.EXPERIMENT_CLUSTER);
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

    private void clearColorOverlaps(){
    	for (int i=0; i<ColorOverlaps.length; i++){
    		ColorOverlaps[i] = i;
    	}
    }

	/**
	 * Sets show Rects attribute.
	 */
	private void setShowRects(boolean value) {
	    this.isShowRects = !value;
	    header.isShowRects = !value;
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
            int width = metrics.stringWidth(NO_EXPERIMENT_STR)+10;
            int height = metrics.getHeight()+30;
            setSize(width, height);
            setPreferredSize(new Dimension(width, height));
            return;
        }
        setFont(new Font("monospaced", Font.PLAIN, elementSize.height));
        Graphics2D g = (Graphics2D)getGraphics();
        int width = elementSize.width*getCluster().length+1 + insets.left;
        if (isDrawAnnotations) {
            this.annotationWidth = getMaxWidth(g);
            width += 20+this.annotationWidth;
        }
        
        if(this.hasCentroid)  //width of experiment centroid
            width += elementSize.width + 5;
        if (maxColorWidth < colorWidth){
        	maxColorWidth = colorWidth;
        }
        //colorWidth = maxColorWidth;
        if(haveColorBar){
            width += (colorWidth)*(this.elementSize.width) + 10;
            if (colorWidth==0)
            	width +=elementSize.width +10;
        }
        
        int height = elementSize.height*genesOrder.length+1;
        
        this.contentWidth = width;
        if (header.getSize().width> width){
        	setSize(header.getSize().width, height);
            setPreferredSize(new Dimension(header.getSize().width, height));
        }else{
        setSize(width, height);
        setPreferredSize(new Dimension(width, height));
        	header.fixHeaderWidth(getSize().width);
        }
        if (isCompact){
        	setSize(width, height);
        	setPreferredSize(new Dimension(width, height));
        }
    }
    
    public int getContentWidth(){
        return this.contentWidth;
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
        for (int i=0; i<genesOrder.length; i++) {
            str = data.getElementAttribute(getMultipleArrayDataRow(i), labelIndex);
            max = Math.max(max, fm.stringWidth(str));
        }
        return max;
    }
        
   
    /**
     * Calculates color for passed value.
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
			colorIndex = colorIndex < 0   ? 0 : colorIndex;
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
    	return this.storedGeneColors;
    }
    
    /**
     * Paint component into specified graphics.
     */
    public void paint(Graphics g) {
        super.paint(g);
        header.setStoredColors(storedGeneColors);

        header.repaint();
        if (this.data == null) {
            return;
        }
        int oldWidth = colorWidth;
        if(this.elementSize.getHeight() < 1)
            return;
        final int samples = getCluster().length;
        
        if (this.clusters == null || getCluster().length == 0) {
            g.setColor(new Color(0, 0, 128));
            g.setFont(ERROR_FONT);
            g.drawString(NO_EXPERIMENT_STR, 10, 30);
            return;
        }
        
        Rectangle bounds = g.getClipBounds();
        final int top = getTopIndex(bounds.y);
        final int bottom = getBottomIndex(bounds.y+bounds.height, genesOrder.length);
        final int left = getLeftIndex(bounds.x);
        final int right = getRightIndex(bounds.x+bounds.width, samples);
        
        int x, y;
        
        if(hasCentroid){
            for( int row=top; row<bottom; row++){
                fillCentroidRectAt(g, row);
            }
        }
        
        // draw rectangles
        for (int column=left; column<right; column++) {
            for (int row=top; row<bottom; row++) {
                fillRectAt(g, row, column);
            }
        }
        
        Color initColor = g.getColor();
        
        int expWidth = samples * this.elementSize.width + 5;
        int spacesOver=0;
        if(haveColorBar){
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
	                expWidth = samples * this.elementSize.width + 5 + this.elementSize.width*spacesOver;
	                if (this.hasCentroid)
	                	expWidth += this.elementSize.width +5;
	                fillClusterRectAt(g, row, expWidth, colors[clusters]);
                }
            }
        }
        
        g.setColor(initColor);
        
        // draw annotations
        if(this.isDrawAnnotations){
            if (this.isAntiAliasing) {
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
            if (right >= samples) {
                String label = "";
                g.setColor(Color.black);
                int uniqX = elementSize.width*samples+10;
                if(this.hasCentroid)
                    uniqX += elementSize.width + 5;
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
                        label = data.getElementAttribute(getMultipleArrayDataRow(row), labelIndex);
                       // System.out.println("ExperimentClusterViewer:getElementAttrinute() "+ label);
                        
                    	if(labelIndex <= data.getFieldNames().length-1) {
                    		annot[0] = data.getElementAttribute(getMultipleArrayDataRow(row), labelIndex);
                    		//System.out.println("Extra Field selected is:"+annot[0]);
                    	}
                    	else {
//                    		System.out.println("Annotation selected is:"+MevAnnotation.getFieldNames()[labelIndex-fieldNamesLength-1]);
                    		annot= data.getElementAnnotation(getMultipleArrayDataRow(row), MevAnnotation.getFieldNames()[labelIndex-fieldNamesLength-1]);
                    	}
                        
                        
                   
                    }
                    annY = (row+1)*elementSize.height;
                    
                    //g.drawString(label, uniqX + insets.left, annY);
                    g.drawString(annot[0], uniqX + insets.left, annY);
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
    		g.drawRect(dragColumn*elementSize.width + insets.left +5-1, -1, (elementSize.width), elementSize.height*experiment.getNumberOfGenes()+1);
        	header.drawClusterHeaderRectsAt(dragColumn, Color.blue, true);
        }
        if (colorWidth!=oldWidth){
        	updateSize();
        }
    }
    
    /**
     * Fills rectangle with specified row and column.
     */
    private void fillRectAt(Graphics g, int row, int column) {
    	if (column > (getCluster().length -1))
        	return;
    	int x = column*elementSize.width + insets.left;
        int y = row*elementSize.height;
        
        if(hasCentroid)
            x += elementSize.width + 5;
        
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
    
    private void fillCentroidRectAt(Graphics g, int row){
        int x = insets.left;
        int y = row*elementSize.height;
        
        boolean mask = this.firstSelectedRow >= 0 && this.lastSelectedRow >= 0 && (row < this.firstSelectedRow || row > this.lastSelectedRow);
        
        g.setColor(getColor(this.centroids[this.clusterIndex][row]));
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
     * Fills cluster colors
     */
    private void fillClusterRectAt(Graphics g, int row, int xLoc, Color color) {
        //Color geneColor = data.getProbeColor(getMultipleArrayDataRow(row));
        
    	if(color == null)
            color = Color.white;
        
        g.setColor(color);
        g.fillRect(xLoc + insets.left, row*elementSize.height, elementSize.width-1, elementSize.height);
    }
    
    /**
     * Draws rect with specified row, column and color.
     */
    private void drawRectAt(Graphics g, int row, int column, Color color) {
    	if (column>=getCluster().length)
        	return;
    	
    	g.setColor(color);
        if(!this.hasCentroid)
            g.drawRect(column*elementSize.width + insets.left, row*elementSize.height, elementSize.width-1, elementSize.height-1);
        else
            g.drawRect(column*elementSize.width + insets.left + elementSize.width + 5, row*elementSize.height, elementSize.width-1, elementSize.height-1);
        
    }
    /**
     * Draws rectangle around specified cluster colors
     */
    private void drawClusterRectsAt(Graphics g, int row, int column, Color color){
    	int centroidOffset = 0;
    	if(this.hasCentroid)
    		centroidOffset = this.elementSize.width +5;
    	g.setColor(color);
    	//g.drawRect((getCluster().length*elementSize.width + insets.left +5-1) + centroidOffset, row*elementSize.height-1, (elementSize.width)*colorWidth, elementSize.height+1);

    	if (column>=getCluster().length){
    		g.drawRect(getCluster().length*elementSize.width + insets.left +5-1 + centroidOffset, row*elementSize.height-1, (elementSize.width)*(colorWidth)+this.annotationWidth +8, elementSize.height+1);
    		if (isCompact) return;
    		g.drawRect(column*elementSize.width + insets.left +5-1, -1, (elementSize.width), elementSize.height*experiment.getNumberOfGenes()+1);
        	header.drawClusterHeaderRectsAt(column, color, true);
        	
    	}
    	else{
    		g.drawRect(getCluster().length*elementSize.width + insets.left +5-1 + centroidOffset, row*elementSize.height-1, (elementSize.width)*(colorWidth)+this.annotationWidth +8, elementSize.height+1);
    		header.drawClusterHeaderRectsAt(column, color, false);
    	}
    }
    
    protected boolean haveClusterColor(){
        int samples = this.getCluster().length;
        for(int i = 0; i < samples; i++){
            if( data.getExperimentColor(getColumn(i)) != null)
                return true;
        }
        return false;
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
        
        if(!hasCentroid){
            return (left - insets.left)/elementSize.width;
        }
        else{
            if(left < insets.left + elementSize.width + 5)
                return 0;
            return (left - insets.left - elementSize.width - 5)/elementSize.width;
        }
    }
    
    private int getRightIndex(int right, int limit) {
        if (right < 0) {
            return 0;
        }
        int result = 0;
        
        if(!hasCentroid)
            result = right/elementSize.width+1;
        else
            result = (right - (insets.left + elementSize.width + 5) )/elementSize.width + 1;
        
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
        int xSize = getCluster().length*elementSize.width;
        
        if(this.hasCentroid){
            xSize += elementSize.width + 5;
            if (targetx >= (xSize + insets.left) || targetx < insets.left + elementSize.width + 5) {
                return -1;
            }
            return  (targetx - insets.left - elementSize.width  - 5)/elementSize.width;
        }
        
        if (targetx >= (xSize + insets.left+this.elementSize.width*colorWidth + 10) || targetx < insets.left) {
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
        int ySize = genesOrder.length*elementSize.height;
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
        if (column < 0 || column > getCluster().length -1 + colorWidth)
            return false;
        return true;
    }
    
    private boolean isLegalRow(int row) {
        if (row < 0 || row > genesOrder.length -1)
            return false;
        return true;
    }
    
    /**
     * Adds viewer specific menu items.
     */
    protected void addMenuItems(JPopupMenu menu, ActionListener listener) {
        JMenuItem menuItem;
        menuItem = new JMenuItem("Store cluster", GUIFactory.getIcon("new16.gif"));
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
        
        menuItem = new JMenuItem("Launch new session", GUIFactory.getIcon("lanuch_new_mav.gif"));
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
    
    public void broadcastClusterGaggle() {
    	framework.broadcastGeneCluster(getExperiment(), null, getCluster());
	}
    public void broadcastNamelistGaggle() {
    	framework.broadcastNamelist(getExperiment(), getExperiment().getRows());
    }
    
    public void setFramework(IFramework framework) {
    	this.framework = framework;
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
        return Cluster.EXPERIMENT_CLUSTER;
    }
    
    /**
     * The class to listen to mouse events.
     */
    private class Listener extends MouseAdapter implements MouseMotionListener {
        
        private String oldStatusText = "";
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

        	if (column>getCluster().length-1){
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
            //mouse on different rectangle, but still on the map
            if (!isCurrentPosition(row, column)&&isLegalPosition(row, column)){
            	mouseOnMap = true;
            	mouseRow = row;
            	mouseColumn = column;
            	repaint();
            }
            //mouse on heat map
            if (isLegalPosition(row, column)&& (column < (getCluster().length -1))) {
                drawRectAt(g, row, column, Color.white);
                if (isShowRects)
                drawClusterRectsAt(g, row, column, Color.gray);
                framework.setStatusText("Gene: "+data.getUniqueId(getMultipleArrayDataRow(row))+" Sample: "+data.getSampleName(experiment.getSampleIndex(getColumn(column)))+" Value: "+experiment.get(getExperimentRow(row), getColumn(column)));
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
            oldStatusText = framework.getStatusText();
        }
        
        public void mouseExited(MouseEvent event) {
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
        	if (column>=getCluster().length){
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
        
	        if (endColumn < getCluster().length)
	        	return;
	        int endRow = findRow(event.getY());
	        if (!isLegalPosition(startRow, startColumn)) {
	            return;
	        }
	      	if (!isCompact){
		      	Color inter = (Color)storedGeneColors.get(startColumn-getCluster().length);
		      	storedGeneColors.remove(startColumn-getCluster().length);
		      	storedGeneColors.add(endColumn-getCluster().length, inter);
		      	repaint();
	      	}else{
	      		for (int j=0; j<storedGeneColors.size(); j++){
	      			if (ColorOverlaps[j]==startColumn-getCluster().length)
	      				ColorOverlaps[j]=-1;
	      			if (ColorOverlaps[j]==endColumn-getCluster().length)
	      				ColorOverlaps[j]=startColumn-getCluster().length;
	      			if(ColorOverlaps[j]== -1)
	      				ColorOverlaps[j]=endColumn-getCluster().length;
	      		}
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
		return this.exptID;
	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperimentID(int)
	 */
	public void setExperimentID(int id) {
		this.exptID = id;
	}
	
    
    /**
     * Creates a popup menu.
     */
	protected JPopupMenu createJPopupMenu(PopupListener listener) {
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
    private class PopupListener extends MouseAdapter implements ActionListener {
	
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
	    } else if(command.equals(BROADCAST_MATRIX_GAGGLE_CMD)){
            broadcastClusterGaggle();
	    } else if(command.equals(BROADCAST_NAMELIST_GAGGLE_CMD)){
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

