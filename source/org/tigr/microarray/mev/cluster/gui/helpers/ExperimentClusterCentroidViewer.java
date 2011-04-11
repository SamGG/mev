/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ExperimentClusterCentroidViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.Expression;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.util.FloatMatrix;



public class ExperimentClusterCentroidViewer extends JPanel implements IViewer {
    
    public static final Color DEF_CLUSTER_COLOR = Color.lightGray;
    protected static final Color bColor = new Color(0, 0, 128);

    protected JMenuItem setOverallMaxMenuItem;
    protected JMenuItem setClusterMaxMenuItem;
    protected static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
    protected static final String COLLAPSE_EXPAND = "collapse-expand-cmd";
    protected static final String SET_DEF_COLOR_CMD = "set-def-color-cmd";
    protected static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
    protected static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";
    protected static final String SET_Y_TO_EXPERIMENT_MAX_CMD = "set-y-to-exp-max-cmd";
    protected static final String SET_Y_TO_CLUSTER_MAX_CMD = "set-y-to-cluster-max-cmd";
    protected static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";   
    public static final String BROADCAST_MATRIX_GAGGLE_CMD = "broadcast-matrix-to-gaggle";
    public static final String BROADCAST_NAMELIST_GAGGLE_CMD = "broadcast-namelist-to-gaggle";
    public static final String BROADCAST_MATRIX_GENOME_BROWSER_CMD = "broadcast-matrix-to-genome-browser";
 
    protected Experiment experiment;
    protected IData data;
    protected IFramework framework;
    protected int clusterIndex = 0;
    protected int[][] clusters;
    protected float maxYValue;           //current max y range set for graph, from y = 0
    protected float maxClusterValue;     //max abs. value in current cluster
    protected float maxExperimentValue;  //max abs. value in all clusters
    protected float minValue, maxValue, midValue = 0.0f;
    
    
    protected int yRangeOption;
    public static int USE_EXPERIMENT_MAX = 0;
    public static int USE_CLUSTER_MAX = 1;
    
    protected boolean drawValues = true;
    protected boolean drawVariances = true;
    protected boolean drawCodes = true;
    public static Color missingColor = new Color(128, 128, 128);
    private boolean drawMarks = false;
	private boolean collapseExpand = true;
    protected boolean gradientColors;
    protected boolean isAntiAliasing = false;
    protected float[][] means;
    protected float[][] variances;
    protected float[][] codes;
    
    protected int numberOfGenes;
    public static BufferedImage posColorImage; // = createGradientImage(Color.black, Color.red);
    public static BufferedImage negColorImage; // = createGradientImage(Color.green, Color.black);
    protected boolean showRefLine = false;
    
    private boolean useDoubleGradient = true;
    private int exptID = 0;
	protected JPopupMenu popup;
    
	/**
	 * State-saving constructor for MeV v4.4 and higher. 
	 * @param e
	 * @param clusters
	 * @param clusterIndex
	 * @param means
	 * @param variances
	 * @param codes
	 */
    public ExperimentClusterCentroidViewer(Experiment e, ClusterWrapper clusters, Integer clusterIndex, float[][] means, float[][] variances, float[][] codes){
    	this(e,clusters.getClusters(), clusterIndex, means, variances, codes);
    }
   	/**
     * This constructor is used by XMLEncoder/Decoder and IViewerPersistenceDelegate
     * to re-create an ExperimentClusterCentroidViewer from a saved xml file. MeV versions v4.0-4.3
     * @param e 
     */
    public ExperimentClusterCentroidViewer(Experiment e, int[][] clusters, Integer clusterIndex, float[][] means, float[][] variances, float[][] codes){
    	this.clusters = clusters;
    	this.clusterIndex = clusterIndex.intValue();
    	this.experiment = e;
        numberOfGenes = experiment.getNumberOfGenes();
        setBackground(Color.white);
        setFont(new Font("monospaced", Font.BOLD, 10));
        this.means = means;
        this.variances = variances;
        this.codes = codes;
        this.maxExperimentValue = experiment.getMaxAbsValue();
        this.yRangeOption = ExperimentClusterCentroidViewer.USE_EXPERIMENT_MAX;

        this.resetSize();
        PopupListener listener = new PopupListener();
        this.popup = createJPopupMenu(listener);
        getContentComponent().addMouseListener(listener);
    }
        
    /**
     * Constructs a <code>CentroidViewer</code> for insertion into ClusterTable
     *
     * @param experiment the data of an experiment.
     */
    public ExperimentClusterCentroidViewer(IFramework framework, int mode) {
        this(framework.getData().getExperiment(), defSampsOrder(framework.getData().getExperiment().getNumberOfSamples()));
        this.means = getMeans(clusters).A;
        this.variances = getVariances(clusters, getMeans(clusters)).A;
        this.setMode(mode);
        this.onSelected(framework);
        this.resetSize();
        PopupListener listener = new PopupListener();
        this.popup = createJPopupMenu(listener);
        getContentComponent().addMouseListener(listener);   
        
    }
    private static int[][] defSampsOrder(int size) {
        int[][] order = new int[1][size];
        for (int i=0; i<order[0].length; i++) {
            order[0][i] = i;
        }
        return order;
    }
    private FloatMatrix getMeans(int[][] clusters) {
    	FloatMatrix means = new FloatMatrix(clusters.length, experiment.getNumberOfGenes());
    	FloatMatrix mean;
    	for (int i=0; i<clusters.length; i++) {
    	    mean = getMean(clusters[i]);
    	    means.A[i] = mean.A[0];
    	}
    	return means;
    }
    private FloatMatrix getMean(int[] cluster) {
    	FloatMatrix mean = new FloatMatrix(1, experiment.getNumberOfGenes());
    	float currentMean;
    	int n = cluster.length;
    	int denom = 0;
    	float value;
    	for (int i=0; i<experiment.getNumberOfGenes(); i++) {
    	    currentMean = 0f;
    	    denom = 0;
    	    for (int j=0; j<n; j++) {
	    		value = experiment.get(i, ((Integer) cluster[j]).intValue());
	    		if (!Float.isNaN(value)) {
	    		    currentMean += value;
	    		    denom++;
	    		}
    	    }
    	    mean.set(0, i, currentMean/(float)denom);
    	}

    	return mean;
    }
    private FloatMatrix getVariances(int[][] clusters, FloatMatrix means) {
    	final int rows = means.getRowDimension();
    	final int columns = means.getColumnDimension();
    	FloatMatrix variances = new FloatMatrix(rows, columns);
    	for (int row=0; row<rows; row++) {
    	    for (int column=0; column<columns; column++) {
    		variances.set(row, column, getSampleVariance(clusters[row], column, means.get(row, column)));
    	    }
    	}
    	return variances;
    }
    private float getSampleVariance(int[] cluster, int column, float mean) {
    	return(float)Math.sqrt(getSampleNormalizedSum(cluster, column, mean)/(float)(validN-1));
    	
    }    
    int validN;
    private float getSampleNormalizedSum(int[] cluster, int column, float mean) {
    	final int size = cluster.length;
    	float sum = 0f;
    	float value;
    	validN = 0;
    	for (int i=0; i<size; i++) {
    	    value = experiment.get(column, ((Integer) cluster[i]).intValue());
    	    if (!Float.isNaN(value)) {
    		sum += Math.pow(value-mean, 2);
    		validN++;
    	    }
    	}
    	return sum;
    }
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new",
    			new Object[]{this.experiment, ClusterWrapper.wrapClusters(this.clusters), new Integer(this.clusterIndex), this.means, this.variances, this.codes});  
    }

    
    /**
     * Constructs a <code>ExperimentClusterCentroidViewer</code> for specified
     * experiment and clusters.
     *
     * @param experiment the data of an experiment.
     * @param clusters the array of clusters.
     */
    public ExperimentClusterCentroidViewer(Experiment experiment, int[][] clusters) {
    	if (experiment == null) {
            throw new IllegalArgumentException("experiment == null");
        }
        this.experiment = experiment;
        numberOfGenes = experiment.getNumberOfGenes();
        this.clusters = clusters;
        setBackground(Color.white);
        setFont(new Font("monospaced", Font.BOLD, 10));
        this.maxExperimentValue = experiment.getMaxAbsValue();
        this.yRangeOption = ExperimentClusterCentroidViewer.USE_EXPERIMENT_MAX;
        
        this.means = getMeans(clusters).A;
        this.variances = getVariances(clusters, getMeans(clusters)).A;
        
        this.resetSize();
        PopupListener listener = new PopupListener();
        this.popup = createJPopupMenu(listener);
        getContentComponent().addMouseListener(listener);
    }

    /**
     * Determines whether any clusters are set
     */
    public boolean checkGradient() {
        boolean temp = true;
        for (int i = 0; i<clusters.length; i++) {
            for (int j = 0; j<clusters[i].length; j++) {
                try {
                    if (this.data.getExperimentColor(clusters[i][j]) != null) {
                        temp = false;
                    }
                } catch (Exception e) {
                }
            }
        }
        return temp;
    }
    
    /**
     * Calculates color for passed expression value.
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
			if(colorIndex<0)
				colorIndex=-colorIndex;
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
    
    public void setGradient(boolean g) {
        this.gradientColors = g;
    }
    
    /**
     * Sets means values.
     */
    public void setMeans(float[][] means) {
        this.means = means;
    }
    
    /**
     * Sets variances values.
     */
    public void setVariances(float[][] variances) {
        this.variances = variances;
    }
    
    /**
     * Sets codes.
     */
    public void setCodes(float[][] codes) {
        this.codes = codes;
    }
    
    /**
     * Sets the draw marks attribute.
     */
    public void setDrawMarks(boolean draw) {
        this.drawMarks = draw;
    }
    
    /**
     * Sets the draw variances attribute.
     */
    public void setDrawVariances(boolean draw) {
        this.drawVariances = draw;
    }
    
    /**
     * Sets the draw values attribute.
     */
    public void setDrawValues(boolean draw) {
        this.drawValues = draw;
    }
    
    /**
     * Sets the draw codes attribute.
     */
    public void setDrawCodes(boolean draw) {
        this.drawCodes = draw;
    }
    
    /**
     * Sets the anti-aliasing attribute.
     */
    public void setAntiAliasing(boolean value) {
        this.isAntiAliasing = value;
    }
    
    /**
     * Updates data, mode and some the viewer attributes.
     */
    public void onSelected(IFramework framework) {
        this.framework = framework;
        setData(framework.getData());
        setAntiAliasing(framework.getDisplayMenu().isAntiAliasing());
        Object userObject = framework.getUserObject();
        if (userObject!=null){
	        if (userObject instanceof CentroidUserObject) {
	            setClusterIndex(((CentroidUserObject)userObject).getClusterIndex());
	            setMode(((CentroidUserObject)userObject).getMode());
	        } else {
	            setClusterIndex(((Integer)userObject).intValue());
	        }
        }
        updateValues(getCluster());
        this.maxValue = framework.getDisplayMenu().getMaxRatioScale();
        this.minValue = framework.getDisplayMenu().getMinRatioScale();
        this.midValue = framework.getDisplayMenu().getMidRatioValue();
        ExperimentClusterCentroidViewer.posColorImage = framework.getDisplayMenu().getPositiveGradientImage();
        ExperimentClusterCentroidViewer.negColorImage = framework.getDisplayMenu().getNegativeGradientImage();
        this.gradientColors = framework.getDisplayMenu().getColorGradientState();
    }
    public void setClusters(int[][] mat){
    	clusters = new int[mat.length][mat[0].length];
    	for (int i=0; i<mat.length; i++){
    		for (int j=0; j<mat[i].length; j++){
    			this.clusters[i][j]=mat[i][j];
    		}
    	}        
    	this.means = getMeans(clusters).A;
        this.variances = getVariances(clusters, getMeans(clusters)).A;
        this.repaint();
        this.updateUI();  
    }
    /**
     * Sets data.
     */
    public void setData(IData data) {
        this.data = data;
    }
    
    /**
     * Sets a cluster index.
     */
    public void setClusterIndex(int clusterIndex) {
        this.clusterIndex = clusterIndex;
        updateValues(getCluster());
    }
    
    /**
     * Returns a current cluster.
     */
    public int[] getCluster() {
        return this.clusters[this.clusterIndex];
    }
    
    /**
     * Returns all clusters.
     */
    public int[][] getClusters() {
        return clusters;
    }
    
    /**
     * Returns index of a gene in the current cluster.
     */
    protected int getProbe(int row) {
        return this.experiment.getGeneIndexMappedToData(row);
    }
    
    /**
     * Sets the viewer mode.
     */
    public void setMode(int mode) {
        switch (mode) {
            case CentroidUserObject.VARIANCES_MODE:
                setDrawVariances(true);
                setDrawValues(false);
                break;
            case CentroidUserObject.VALUES_MODE:
                setDrawVariances(false);
                setDrawValues(true);
                break;
        }
    }
    
    /**
     * Updates max value.
     */
    private void updateValues(int[] cluster) {
        this.maxClusterValue = calculateMaxValue(cluster);
    }
    
    /**
     * Sets public color for the current cluster.
     */
    public void setClusterColor(Color color) {
        if(color ==null){  //indicates removal of cluster
            framework.removeCluster(getCluster(), experiment, ClusterRepository.EXPERIMENT_CLUSTER);
        }
    }
    
    /**
     *  Sets cluster color
     */
    public void storeCluster(){
        framework.storeCluster(getCluster(), experiment, ClusterRepository.EXPERIMENT_CLUSTER);       
        onDataChanged(this.data);
    }
    private void collapseExpand() {
    	collapseExpand = !collapseExpand;
    	this.resetSize();
    
	}
    
    private void resetSize() {
    	int width;
    	if (collapseExpand)
    		width = this.numberOfGenes*10+100;
    	else
    		width = this.numberOfGenes+100;
        this.setPreferredSize(new Dimension(width, getSize().height));
        repaint();
        this.updateUI();
		
	}
    /**
     * Launches a new <code>MultipleExperimentViewer</code> containing the current cluster
     */
    public void launchNewSession(){
        framework.launchNewMAV(getCluster(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.EXPERIMENT_CLUSTER);
    }
    
        
    
    /**
     * Sets Y range scaling option
     */
    public void setYRangeOption(int option){
        if(option != ExperimentClusterCentroidViewer.USE_EXPERIMENT_MAX && option != ExperimentClusterCentroidViewer.USE_CLUSTER_MAX)
            this.yRangeOption = USE_EXPERIMENT_MAX;
        else
            yRangeOption = option;
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
     * Returns component to be displayed in the framework scroll pane.
     */
    public JComponent getContentComponent() {
//    	JScrollPane jsp = new JScrollPane(this);
//        return jsp;
        return this;
    }
    
    /**
     * Paints chart into specified graphics.
     */
    public void paint(Graphics g) {
        FontMetrics metrics = g.getFontMetrics();
        Rectangle rect = new Rectangle(40, 20, this.getWidth()-80, getHeight() - 40 - getNamesWidth(metrics));
        paint((Graphics2D)g, rect, true);
    }
    
    public void subPaint(Graphics2D g, Rectangle rect, boolean drawMarks) {
        super.paint(g);
    }
    
//    public Dimension getSize(){
//    	Dimension dim = new Dimension();
//    	dim.height = this.getHeight();
//    	dim.width = 20000;
//    	return dim;
//    }
    /**
     * Paints chart into specified graphics and with specified bounds.
     */
    public void paint(Graphics2D g, Rectangle rect, boolean drawMarks) {
        super.paint(g);
//        System.out.println("this.getSize().width ="+this.getSize().width);
//        System.out.println("this.getPreferredSize().width ="+this.getPreferredSize().width);
//        System.out.println("getMaximumSize().width ="+this.getMaximumSize().width);
//        System.out.println("getMinimumSize().width ="+this.getMinimumSize().width);
        

        
        if (isAntiAliasing) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        
        final int left = rect.x;
        final int top = rect.y;
        final int width  = rect.width;
        final int height = rect.height;
        
        if (width < 5 || height < 5) {
            return;
        }
        
        final int zeroValue = top + (int)Math.round(height/2f);
        
        //do this outside paint once menu is set up
        if(this.yRangeOption == ExperimentClusterCentroidViewer.USE_EXPERIMENT_MAX)
            maxYValue = this.maxExperimentValue;
        else if(this.yRangeOption == ExperimentClusterCentroidViewer.USE_CLUSTER_MAX)
            maxYValue = this.maxClusterValue;
        
        if (maxYValue == 0.0f) {
            maxYValue = 1.0f;
        }
        final float factor = height/(2f*maxYValue);
        final float stepX  = width/(float)(numberOfGenes-1);
        final int   stepsY = (int)maxYValue+1;
        float fontHeight = g.getFontMetrics().getHeight()*2;  
        
        Rectangle bounds = g.getClipBounds();
//        System.out.println("bounds.height "+bounds.height);
//        System.out.println("bounds.width "+bounds.width);
//        System.out.println("bounds.y "+bounds.y);
//        System.out.println("bounds.x "+bounds.x);
        int geneBegin = (int)Math.max((bounds.x-100f)/stepX, 0f);
        int geneEnd = (int)Math.min(this.numberOfGenes, geneBegin + (bounds.width)/stepX+100);
//        System.out.println("geneBegin "+geneBegin);
//        System.out.println("geneEnd "+geneEnd);
        
        if (drawMarks) {
            FontMetrics metrics = g.getFontMetrics();
            String str;
            int strWidth;
            //draw Y digits
            for (int i=1; i<stepsY; i++) {
                str = String.valueOf(i);
                strWidth = metrics.stringWidth(str);
                g.drawString(str, left-10-strWidth, zeroValue+5-(int)Math.round(i*factor));
                str = String.valueOf(-i);
                strWidth = metrics.stringWidth(str);
                g.drawString(str, left-10-strWidth, zeroValue+5+(int)Math.round(i*factor));
            }
            // draw X gene names
            g.rotate(-Math.PI/2.0);
            final int max_name_width = getNamesWidth(metrics);
            for (int i=geneBegin; i<geneEnd; i++) {
            	if (this.collapseExpand)
            		g.drawString(data.getGeneName(i), -height-top-10-max_name_width, left+(int)Math.round(i*stepX)+3);
            }
            g.rotate(Math.PI/2.0);
        }
        if (this.drawVariances) {
            // draw variances
            g.setColor(bColor);
            for (int i=geneBegin; i<geneEnd; i++) {
                if(Float.isNaN(this.means[this.clusterIndex][i]) || Float.isNaN(this.variances[this.clusterIndex][i]) || (this.variances[this.clusterIndex][i] < 0.0f)) {
                    continue;
                }
                g.drawLine(left+(int)Math.round(i*stepX)  , zeroValue - (int)Math.round((this.means[this.clusterIndex][i]-this.variances[this.clusterIndex][i])*factor),
                left+(int)Math.round(i*stepX)  , zeroValue - (int)Math.round((this.means[this.clusterIndex][i]+this.variances[this.clusterIndex][i])*factor));
                g.drawLine(left+(int)Math.round(i*stepX)-3, zeroValue - (int)Math.round((this.means[this.clusterIndex][i]-this.variances[this.clusterIndex][i])*factor),
                left+(int)Math.round(i*stepX)+3, zeroValue - (int)Math.round((this.means[this.clusterIndex][i]-this.variances[this.clusterIndex][i])*factor));
                g.drawLine(left+(int)Math.round(i*stepX)-3, zeroValue - (int)Math.round((this.means[this.clusterIndex][i]+this.variances[this.clusterIndex][i])*factor),
                left+(int)Math.round(i*stepX)+3, zeroValue - (int)Math.round((this.means[this.clusterIndex][i]+this.variances[this.clusterIndex][i])*factor));
            }
        }
        if (this.drawValues) {
            // draw values
            float fValue, sValue, yInterval, lineHeight;
            Color color = null;
            float maxLineHeight = (maxExperimentValue*factor) / 20;	//maxExperimentValue is an expression value - not a coordinate length
            int intervalNumber;
            
            for (int sample=0; sample<getCluster().length; sample++) {
                for (int probe=geneBegin; probe<geneEnd-1; probe++) {
                    fValue = this.experiment.get(probe, getCluster()[sample]);
                    sValue = this.experiment.get(probe+1, getCluster()[sample]);
                    if (Float.isNaN(fValue) || Float.isNaN(sValue)) {
                        continue;
                    }
                    if(!gradientColors) {
                        color = this.data.getExperimentColor(getCluster()[sample]);
                        color = color == null ? DEF_CLUSTER_COLOR : color;
                        g.setColor(color);
                        g.drawLine(left+(int)Math.round(probe*stepX), zeroValue - (int)Math.round(fValue*factor),
                        left+(int)Math.round((probe+1)*stepX), zeroValue - (int)Math.round(sValue*factor));
                    } else {
                        lineHeight = (sValue - fValue)*factor;
                        if (Math.abs(lineHeight) > maxLineHeight) {
                            intervalNumber = Math.abs((int)(lineHeight / maxLineHeight));
                        } else {
                            intervalNumber = 1;
                        }
                        yInterval = lineHeight/intervalNumber;
                        for(int i=0; i<intervalNumber; i++) {
                            g.setColor(getColor(fValue + (float)(i)*yInterval/factor));
                            g.drawLine(left+(int)Math.round(probe*stepX + ((float)i/intervalNumber)*stepX), zeroValue - (int)Math.round(fValue*factor + (float)i*yInterval),
                            left+(int)Math.round((probe)*stepX + (((float)i+1)/intervalNumber)*stepX), zeroValue - (int)Math.round(fValue*factor + ((float)i+1)*yInterval));
                        }
                    }
                }
            }
        }
        if (this.drawCodes && this.codes != null && clusters[clusterIndex].length > 0) {
            g.setColor(Color.gray);
            for (int i=geneBegin; i<geneEnd-1; i++) {
                g.drawLine(left+(int)Math.round(i*stepX), zeroValue-(int)Math.round(this.codes[this.clusterIndex][i]*factor), left+(int)Math.round((i+1)*stepX), zeroValue-(int)Math.round(this.codes[this.clusterIndex][i+1]*factor));
            }
        }
        
        // draw zero line
        g.setColor(Color.black);
        g.drawLine(left, zeroValue, left+width, zeroValue);
        // draw magenta line
        if (getCluster() != null && getCluster().length > 0) {
            g.setColor(Color.magenta);
            for (int i=geneBegin; i<geneEnd-1; i++) {
                if (Float.isNaN(this.means[this.clusterIndex][i]) || Float.isNaN(this.means[this.clusterIndex][i+1])) {
                    continue;
                }
                g.drawLine(left+(int)Math.round(i*stepX), zeroValue-(int)Math.round(this.means[this.clusterIndex][i]*factor), left+(int)Math.round((i+1)*stepX), zeroValue-(int)Math.round(this.means[this.clusterIndex][i+1]*factor));
            }
        }
        // draw rectangle
        g.setColor(Color.black);
        g.drawRect(left, top, width, height);
        // draw X items
        for (int i=geneBegin+1; i<geneEnd-1; i++) {
            g.drawLine(left+(int)Math.round(i*stepX), top+height-5, left+(int)Math.round(i*stepX), top+height);
        }
        //draw Y items
        for (int i=1; i<stepsY; i++) {
            g.drawLine(left, zeroValue-(int)Math.round(i*factor), left+5, zeroValue-(int)Math.round(i*factor));
            g.drawLine(left, zeroValue+(int)Math.round(i*factor), left+5, zeroValue+(int)Math.round(i*factor));
        }
        // draw genes info
        g.setColor(bColor);
  
  
        if (getCluster() != null && getCluster().length > 0 && this.drawVariances) {
            // draw points
            g.setColor(bColor);
            for (int i=geneBegin; i<geneEnd; i++) {
                if (Float.isNaN(this.means[this.clusterIndex][i])) {
                    continue;
                }
                g.fillOval(left+(int)Math.round(i*stepX)-3, zeroValue-(int)Math.round(this.means[this.clusterIndex][i]*factor)-3, 6, 6);
            }
        }
        g.setColor(bColor);
        if (getCluster() == null || getCluster().length == 0) {
            g.drawString("No Experiments In Cluster", left+10, top+20);
        } else {
            if(getCluster().length == 1)
                g.drawString(1 + " Experiment", left+10, top+20);
            else
                g.drawString(getCluster().length+" Experiments", left+10, top+20);
        }
    }
    
    /**
     * @return null
     */
    public JComponent getHeaderComponent() {
        return null;
    }
    
    /**
     * Updates the viewer data.
     */
    public void onDataChanged(IData data) {
        setData(data);
    }
    
    /**
     * Updates some viewer attributes.
     */
    public void onMenuChanged(IDisplayMenu menu) {
        setAntiAliasing(menu.isAntiAliasing());
        this.maxValue = menu.getMaxRatioScale();
        this.minValue = menu.getMinRatioScale();
        this.midValue = menu.getMidRatioValue();
        ExperimentClusterCentroidViewer.posColorImage = menu.getPositiveGradientImage();
        ExperimentClusterCentroidViewer.negColorImage = menu.getNegativeGradientImage();
        this.gradientColors = menu.getColorGradientState();
    }
    
    public void onDeselected() {}
    public void onClosed() {}
    
    /**
     * @return null
     */
    public BufferedImage getImage() {
        return null;
    }
    
    /**
     * Calculate experiment max value for scale purpose.
     */
    private float calculateMaxValue(int[] samples) {
        float max = 0f;
        float value;
        final int numGenes = experiment.getNumberOfGenes();
        for (int gene=0; gene<numGenes; gene++) {
            for (int sample=0; sample<samples.length; sample++) {
                value = experiment.get(gene, samples[sample]);
                if (!Float.isNaN(value)) {
                    max = Math.max(max, Math.abs(value));
                }
            }
        }
        return max;
    }
    
    /**
     * Returns max width of experiment names.
     */
    protected int getNamesWidth(FontMetrics metrics) {
        int maxWidth = 0;
        for (int i=0; i<experiment.getNumberOfSamples(); i++) {
            maxWidth = Math.max(maxWidth, metrics.stringWidth(data.getSampleName(experiment.getSampleIndex(i))));
        }
        return maxWidth;
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
        
        menuItem = new JMenuItem("Collapse/Expand Labels", GUIFactory.getIcon("new16.gif"));
        menuItem.setActionCommand(COLLAPSE_EXPAND);
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
        
        setOverallMaxMenuItem = new JMenuItem("Set Y to overall max...", GUIFactory.getIcon("Y_range_expand.gif"));
        setOverallMaxMenuItem.setActionCommand(SET_Y_TO_EXPERIMENT_MAX_CMD);
        setOverallMaxMenuItem.addActionListener(listener);
        setOverallMaxMenuItem.setEnabled(false);
        menu.add(setOverallMaxMenuItem);
        
        setClusterMaxMenuItem = new JMenuItem("Set Y to cluster max...", GUIFactory.getIcon("Y_range_expand.gif"));
        setClusterMaxMenuItem.setActionCommand(SET_Y_TO_CLUSTER_MAX_CMD);
        setClusterMaxMenuItem.addActionListener(listener);
        menu.add(setClusterMaxMenuItem);
        
    //    menu.addSeparator();

    //    menuItem = new JMenuItem("Toggle reference line...");
    //    menuItem.setActionCommand(TOGGLE_REF_LINE_CMD);
    //    menuItem.addActionListener(listener);
    //    menu.add(menuItem);        

        menu.addSeparator();
        
        menuItem = new JMenuItem("Broadcast Matrix to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
        menuItem.setActionCommand(BROADCAST_MATRIX_GAGGLE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Broadcast Gene List to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
        menuItem.setActionCommand(BROADCAST_NAMELIST_GAGGLE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("Broadcast Matrix to Genome Browser", GUIFactory.getIcon("gaggle_icon_16.gif"));
        menuItem.setActionCommand(BROADCAST_MATRIX_GENOME_BROWSER_CMD);
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
        return Cluster.EXPERIMENT_CLUSTER;
    }
    
	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperiment(org.tigr.microarray.mev.cluster.gui.Experiment)
	 */
	public void setExperiment(Experiment e) {
        this.experiment = e;
        this.exptID = e.getId();
        numberOfGenes = experiment.getNumberOfGenes();
        this.maxExperimentValue = experiment.getMaxAbsValue();
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
	 * Saves all clusters.
	 */
	protected void onSaveClusters() {
	    Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
	    try {
	        ExperimentUtil.saveAllExperimentClusters(frame, getExperiment(), getData(), getClusters());
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(frame, "Can not save clusters!", e.toString(), JOptionPane.ERROR_MESSAGE);
	        e.printStackTrace();
	    }
	}
    public void broadcastGeneClusterToGenomeBrowser() {
    	framework.broadcastGeneClusterToGenomeBrowser(getExperiment(), getCluster(), null);
    }

	/**
	 * Removes a public color.
	 */
	public void onSetDefaultColor() {
	    setClusterColor(null);
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
	 * Save the viewer cluster.
	 */
	private void onSaveCluster() {
	    Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
	    try {
	        ExperimentUtil.saveExperimentCluster(frame, getExperiment(), getData(), getCluster());
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
	        e.printStackTrace();
	    }
	}
    public void broadcastClusterGaggle() {
    	framework.broadcastGeneCluster(getExperiment(), null, getCluster());
	}
    public void broadcastNamelistGaggle() {
    	framework.broadcastNamelist(getExperiment(), getExperiment().getRows());
    }
    

	/**
     * The class to listen to mouse and action events.
     */
    protected class PopupListener extends MouseAdapter implements ActionListener {
        public PopupListener() {
        	super();
//        	System.out.println("popup created");
        }
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals(SAVE_CLUSTER_CMD)) {
                onSaveCluster();
            } else if (command.equals(SAVE_ALL_CLUSTERS_CMD)) {
                onSaveClusters();
            } else if (command.equals(STORE_CLUSTER_CMD)) {
                storeCluster();
            } else if (command.equals(COLLAPSE_EXPAND)) {
                collapseExpand();
            } else if (command.equals(SET_DEF_COLOR_CMD)) {
                onSetDefaultColor();
            } else if(command.equals(SET_Y_TO_EXPERIMENT_MAX_CMD)){
                yRangeOption = ExperimentClusterCentroidViewer.USE_EXPERIMENT_MAX;
                setClusterMaxMenuItem.setEnabled(true);
                setOverallMaxMenuItem.setEnabled(false);
                repaint();
            } else if(command.equals(SET_Y_TO_CLUSTER_MAX_CMD)){
                yRangeOption = ExperimentClusterCentroidViewer.USE_CLUSTER_MAX;
                setClusterMaxMenuItem.setEnabled(false);
                setOverallMaxMenuItem.setEnabled(true);
                repaint();
            } else if(command.equals(LAUNCH_NEW_SESSION_CMD)){
                launchNewSession();
    	    } else if(command.equals(BROADCAST_MATRIX_GAGGLE_CMD)){
                broadcastClusterGaggle();
    	    } else if(command.equals(BROADCAST_NAMELIST_GAGGLE_CMD)){
                broadcastNamelistGaggle();
	        } else if (command.equals(BROADCAST_MATRIX_GENOME_BROWSER_CMD)) {
	        	broadcastGeneClusterToGenomeBrowser();
            }
            
        }
        
        public void mouseReleased(MouseEvent event) {
            maybeShowPopup(event);
        }
        
        public void mousePressed(MouseEvent event) {
//        	System.out.println("pressed");
            maybeShowPopup(event);
        }
        
        private void maybeShowPopup(MouseEvent e) {
//        	System.out.println("popup? ");
            if (!e.isPopupTrigger() || getCluster() == null || getCluster().length == 0) {
                return;
            }
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}

