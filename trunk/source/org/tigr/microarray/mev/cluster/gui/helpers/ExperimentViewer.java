/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
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
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.annotation.MevAnnotation;

/**
 * This class is used to render an experiment values.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public class ExperimentViewer extends JPanel implements IViewer {
    
    private static final float INITIAL_MAX_VALUE = 3f;
    private static final float INITIAL_MIN_VALUE = -3f;
    private static final String NO_GENES_STR = "No Genes in Cluster!";
    private static final Font ERROR_FONT = new Font("monospaced", Font.BOLD, 20);
    protected static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
    protected static final String SET_DEF_COLOR_CMD = "set-def-color-cmd";
    protected static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
    protected static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";
    protected static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";
    
    //EH Gaggle testing
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
        this.header = new ExperimentHeader(this.experiment, this.clusters, this.samplesOrder);
        this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
        setBackground(Color.white);
        Listener listener = new Listener();
        addMouseListener(listener);
        addMouseMotionListener(listener);
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
        this.header = new ExperimentHeader(this.experiment, this.clusters, this.samplesOrder);
        this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
        this.insets.left = offset;
        this.header.setLeftInset(offset);
        setBackground(Color.white);
        Listener listener = new Listener();
        addMouseListener(listener);
        addMouseMotionListener(listener);
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
    		this.header = new ExperimentHeader(this.experiment, this.clusters, this.samplesOrder);
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
        this.framework = framework;
        this.data = framework.getData();
        IDisplayMenu menu = framework.getDisplayMenu();
        useDoubleGradient = menu.getUseDoubleGradient();
        Integer userObject = (Integer)framework.getUserObject();
        setClusterIndex(userObject == null ? 0 : userObject.intValue());
        this.header.setClusterIndex(this.clusterIndex);
        labelIndex = menu.getLabelIndex();
        this.maxValue = menu.getMaxRatioScale();
        this.minValue = menu.getMinRatioScale();
        this.midValue = menu.getMidRatioValue();
        setElementSize(menu.getElementSize());
        setAntialiasing(menu.isAntiAliasing());
        setDrawBorders(menu.isDrawingBorder());
        if(showClusters)
            haveColorBar = areProbesColored();
        else
            haveColorBar = false;
        updateSize();        
        header.updateSizes(getSize().width, elementSize.width);
        header.setData(data);
        onMenuChanged(menu);
        //header.setValues(maxValue, minValue);
        header.setValues(minValue, midValue, maxValue);
        header.setAntiAliasing(menu.isAntiAliasing());
        header.updateSizes(getSize().width, elementSize.width);
        header.setUseDoubleGradient(useDoubleGradient);
    }
    
    /**
     * Updates appropriate attributes of this viewer and its header.
     * @see IViewer#onMenuChanged
     */
    public void onMenuChanged(IDisplayMenu menu) {
        setDrawBorders(menu.isDrawingBorder());
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
        updateSize();
        header.setAntiAliasing(menu.isAntiAliasing());
        header.updateSizes(getSize().width, elementSize.width);
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
        
        if(haveColorBar)
            width += this.elementSize.width + 10;
        
        this.contentWidth = width;
        
        int height = elementSize.height*getCluster().length+1;
        setSize(width, height);
        setPreferredSize(new Dimension(width, height));
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
       		 str = data.getElementAttribute(getMultipleArrayDataRow(i), labelIndex);
       		
     	}
       	else {
       	 str =  (data.getElementAnnotation(getMultipleArrayDataRow(i), MevAnnotation.getFieldNames()[labelIndex-(data.getFieldNames().length)-1])).toString();
       	}
           
            max = Math.max(max, fm.stringWidth(str));
        }
        return max;
    }
    
    /**
     * Returns content width
     */
    public int getContentWidth(){
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
    
    /**
     * Paint component into specified graphics.
     */
    public void paint(Graphics g) {
        super.paint(g);
        
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
        
        int expWidth = samples * this.elementSize.width + 5;
        
        if(haveColorBar){
            for (int row=top; row<bottom; row++) {
                fillClusterRectAt(g, row, expWidth);
            }
        }
        
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
                    uniqX += this.elementSize.width;
                
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
//                    		System.out.println("Annotation selected is:"+MevAnnotation.getFieldNames()[labelIndex-fieldNamesLength-1]);
                    		annot= data.getElementAnnotation(getMultipleArrayDataRow(row), MevAnnotation.getFieldNames()[labelIndex-fieldNamesLength-1]);
                    	}
                        
                    }
                    annY = (row+1)*elementSize.height;
                  //  g.drawString(label, uniqX + insets.left, annY);
                    
                   
                    g.drawString(annot[0], uniqX + insets.left, annY);
                }
            }
        }
    }
    
    /**
     * Fills rect with specified row and colunn.
     */
    private void fillRectAt(Graphics g, int row, int column) {
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
    private void fillClusterRectAt(Graphics g, int row, int xLoc) {
        Color geneColor = data.getProbeColor(getMultipleArrayDataRow(row));
        if(geneColor == null)
            geneColor = Color.white;
        
        g.setColor(geneColor);
        g.fillRect(xLoc + insets.left, row*elementSize.height, elementSize.width-1, elementSize.height);
    }
    
    /**
     * Draws rect with specified row, column and color.
     */
    private void drawRectAt(Graphics g, int row, int column, Color color) {
        g.setColor(color);
        g.drawRect(column*elementSize.width + insets.left, row*elementSize.height, elementSize.width-1, elementSize.height-1);
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
        if (targetx >= (xSize + insets.left) || targetx < insets.left) {
            return -1;
        }
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
        if (column < 0 || column > experiment.getNumberOfSamples() -1)
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
        menuItem = new JMenuItem("Store cluster", GUIFactory.getIcon("new16.gif"));
        menuItem.setActionCommand(STORE_CLUSTER_CMD);
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
        
        //TODO
        //EH Gaggle testing
        menuItem = new JMenuItem("Broadcast Matrix to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
        menuItem.setActionCommand(BROADCAST_MATRIX_GAGGLE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Broadcast Gene List to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
        menuItem.setActionCommand(BROADCAST_NAMELIST_GAGGLE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        //end Gaggle testing
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
        
        public void mouseClicked(MouseEvent event) {
            if (SwingUtilities.isRightMouseButton(event)) {
                return;
            }
            int column = findColumn(event.getX());
            int row = findRow(event.getY());
            if (!isLegalPosition(row, column)) {
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
            if (isCurrentPosition(row, column)) {
                return;
            }
            Graphics g = null;
            if (isLegalPosition(row, column)) {
                g = getGraphics();
                drawRectAt(g, row, column, Color.white);
                framework.setStatusText("Gene: "+data.getUniqueId(getMultipleArrayDataRow(row))+" Sample: "+data.getSampleName(experiment.getSampleIndex(getColumn(column)))+" Value: "+experiment.get(getExperimentRow(row), getColumn(column)));
            } else {
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
            if (isLegalPosition(oldRow, oldColumn)) {
                Graphics g = getGraphics();
                fillRectAt(g, oldRow, oldColumn);
                g.dispose();
            }
            setOldPosition(-1, -1);
            framework.setStatusText(oldStatusText);
        }
        
        public void mouseDragged(MouseEvent event) {}
        
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

	//EH Gaggle test
    public void broadcastClusterGaggle() {
    	int[] temp = getCluster();
    	Experiment e = getExperiment();
    	if (temp == null)
    		System.out.println("getCluster returns null");
    	if(e == null)
    		System.out.println("getExperiment returns null");
    	if(framework == null)
    		System.out.println(this.toString() + ": framework is null");
//    	System.out.println("cluster is size " + getCluster().length);
    	framework.broadcastGeneCluster(getExperiment(), getCluster());
	}
    public void broadcastNamelistGaggle() {
    	framework.broadcastNamelist(getExperiment(), getCluster());
    }
    public void setFramework(IFramework framework) {
    	this.framework = framework;
    }

}
