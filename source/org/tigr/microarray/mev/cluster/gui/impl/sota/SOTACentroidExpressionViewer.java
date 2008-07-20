/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SOTACentroidExpressionViewer.java,v $
 * $Revision: 1.9 $
 * $Date: 2006-03-24 15:51:44 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.sota;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.Expression;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLCluster;
import org.tigr.util.FloatMatrix;


public class SOTACentroidExpressionViewer extends JPanel implements IViewer {
     
    private int numberOfCentroids;
    private int [] clusterPopulation;
    private FloatMatrix clusterDiversity;
    
    
    private int TEXT_LEFT_MARGIN = 20;
    private int CLUSTER_POP_SPACER = 20;
    private int POP_DIV_SPACER = 20;
    private static final float INITIAL_MAX_VALUE = 3f;
    private static final float INITIAL_MIN_VALUE = -3f;
    private static final String NO_GENES_STR = "No Genes in Cluster!";
    private static final Font ERROR_FONT = new Font("monospaced", Font.BOLD, 20);
    
    private ExperimentHeader header;
    private Experiment experiment;

    private IFramework framework;
    private IData data;
    private int clusterIndex;
    private int[][] clusters;
    private int[] samplesOrder;
    private Dimension elementSize = new Dimension(40, 20);
    private boolean isAntiAliasing = true;
    private boolean isDrawBorders = true;
    public static Color missingColor = new Color(128, 128, 128);
    public static Color maskColor = new Color(255, 255, 255, 128);
    private float maxValue = INITIAL_MAX_VALUE;
    private float minValue = INITIAL_MIN_VALUE;
    private float midValue = 0.0f;
    private int firstSelectedRow = -1;
    private int lastSelectedRow  = -1;
    private ArrayList selectedClusterList;
    public BufferedImage posColorImage = createGradientImage(Color.black, Color.red);
    public BufferedImage negColorImage = createGradientImage(Color.green, Color.black);
    private int maxUniqueIDWidth, maxGeneNameWidth;
    private Listener listener;
    private boolean useDoubleGradient = true;
    private int exptID = 0;
    
    /**
     * Constructs an <code>SOTACentroidEpressionViewer</code> with specified
     * experiment, clusters, samples order and draw annotations attribute.
     *
     * @param centroidData, contains the values of the cluster centroids.
     * @param clusters the two dimensional array with gene indices.
     * @param samplesOrder the one dimensional array with samples indices.
     * @param clusterPop array containing genes per cluster
     * @param clusterDiv diversity measure of each cluster
     * @param selClusterList, ArrayList shared with parent viewer which accumulates selected clusters
     */
    public SOTACentroidExpressionViewer(Experiment centroidData, int[][] clusters, int[] samplesOrder, int[] clusterPop, FloatMatrix clusterDiv, ArrayList selClusterList) {
        if (centroidData == null) {
            throw new IllegalArgumentException("experiment == null");
        }
        
        this.experiment = centroidData;
        this.exptID = experiment.getId();
        this.clusterPopulation = clusterPop;
        this.clusterDiversity = clusterDiv;
        this.numberOfCentroids = clusterPopulation.length;
        this.selectedClusterList = selClusterList;
        this.clusters = clusters == null ? defGenesOrder(experiment.getNumberOfGenes()) : clusters;
        this.samplesOrder = samplesOrder == null ? defSamplesOrder(experiment.getNumberOfSamples()) : samplesOrder;
        this.header = new ExperimentHeader(this.experiment, this.clusters, this.samplesOrder);
        this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
        setBackground(Color.white);
        
        listener = new Listener();
        this.addMouseMotionListener(listener);
        this.addMouseListener(listener);
    }
    
    //These methods are used only for compatibility with IViewer
    public Expression getExpression(){return null;}
    public int getExperimentID(){return exptID;}
    public void setExperimentID(int i){this.exptID = i;}
    public void setExperiment(Experiment e){
    	this.experiment = e;
    	this.exptID = e.getId();
    	this.header.setExperiment(e);
    }
    
    /**
     * Constucts a default sample order, sequential
     */
    private static int[] defSamplesOrder(int size) {
        int[] order = new int[size];
        for (int i=0; i<order.length; i++) {
            order[i] = i;
        }
        return order;
    }
    
    /**
     * Constucts a default gene order, sequential
     */
    private static int[][] defGenesOrder(int size) {
        int[][] order = new int[1][size];
        for (int i=0; i<order[0].length; i++) {
            order[0][i] = i;
        }
        return order;
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
     * Initializes appropriate attributes of this viewer and its header.
     * @see IViewer#onSelected
     */
    public void onSelected(IFramework framework) {
        this.framework = framework;
        this.data = framework.getData();
        header.setData(data);
        
        Integer userObject = (Integer)framework.getUserObject();
        setClusterIndex(userObject == null ? 0 : userObject.intValue());
        IDisplayMenu menu = framework.getDisplayMenu();        
        useDoubleGradient = menu.getUseDoubleGradient();
        header.setUseDoubleGradient(useDoubleGradient);
        
        this.maxValue = menu.getMaxRatioScale();
        this.minValue = menu.getMinRatioScale();
        this.midValue = menu.getMidRatioValue();
        setElementSize(menu.getElementSize());
        setAntialiasing(menu.isAntiAliasing());
        setDrawBorders(menu.isDrawingBorder());
        
        updateSize();
        this.posColorImage = menu.getPositiveGradientImage();
        this.negColorImage = menu.getNegativeGradientImage();
        this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
        header.setValues(minValue, midValue, maxValue);
        header.setAntiAliasing(menu.isAntiAliasing());
        header.updateSizes(getSize().width, elementSize.width);
        
    }
    
    
    
    /**
     * Updates appropriate attributes of this viewer and its header.
     * @see IViewer#onMenuChanged
     */
    public void onMenuChanged(IDisplayMenu menu) {
    	useDoubleGradient = menu.getUseDoubleGradient();
    	header.setUseDoubleGradient(useDoubleGradient);
    	setDrawBorders(menu.isDrawingBorder());
        this.maxValue = menu.getMaxRatioScale();
        this.minValue = menu.getMinRatioScale();
        this.midValue = menu.getMidRatioValue();
        header.setValues(minValue, midValue, maxValue);
        this.posColorImage = menu.getPositiveGradientImage();
        this.negColorImage = menu.getNegativeGradientImage();
        this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
        if (this.elementSize.equals(menu.getElementSize()) &&
        this.isAntiAliasing == menu.isAntiAliasing()) {
            return;
        }
        setElementSize(menu.getElementSize());
        setAntialiasing(menu.isAntiAliasing());
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
    
    private int getRow(int row) {
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
    protected IData getData() {
        return data;
    }
    
    /**
     * Sets public color for the current cluster.
     */
    public void setClusterColor(Color color) {
        this.data.setProbesColor(getCluster(), color);
    }
    
    /**
     * Saves all the clusters.
     */
//    public void saveClusters(Frame frame) throws Exception {
//        frame = frame == null ? JOptionPane.getFrameForComponent(this) : frame;
//        ExperimentUtil.saveExperiment(frame, getExperiment(), getData(), getClusters());
//    }
    
    /**
     * Saves current cluster.
     */
//    public void saveCluster(Frame frame) throws Exception {
//        frame = frame == null ? JOptionPane.getFrameForComponent(this) : frame;
//        ExperimentUtil.saveExperiment(frame, getExperiment(), getData(), getCluster());
//    }
    
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
        BufferedImage image = (BufferedImage)java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(256, 1);        

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
        int width = elementSize.width*experiment.getNumberOfSamples()+1;
        
        width += getClusterTextWidth(g) + TEXT_LEFT_MARGIN + CLUSTER_POP_SPACER + POP_DIV_SPACER;
        
        int height = elementSize.height*numberOfCentroids+1;
        setSize(width, height);
        setPreferredSize(new Dimension(width, height));
    }
    
    
    private int getClusterTextWidth(Graphics2D g){
        return getPopulationTextWidth(g) + getDiversityTextWidth(g) + getClusterNumberTextWidth(g);
    }
    
    private int getPopulationTextWidth(Graphics2D g){
        int maxWidth = 0;
        int currWidth = 0;
        
        if(this.clusterPopulation == null) return 0;
        
        FontMetrics fm = g.getFontMetrics();
        for(int i = 0; i < clusterPopulation.length; i++){
            
            currWidth = fm.stringWidth(String.valueOf(clusterPopulation[i]));
            
            if(currWidth > maxWidth)
                maxWidth = currWidth;
        }
        
        return maxWidth;
    }
    
    
    private int getDiversityTextWidth(Graphics2D g){
        int maxWidth = 0;
        int currWidth = 0;
        int n;
        
        if(this.clusterDiversity == null) return 0;
        n = clusterDiversity.getRowDimension();
        
        FontMetrics fm = g.getFontMetrics();
        for(int i = 0; i < n; i++){
            
            currWidth = fm.stringWidth(String.valueOf(clusterDiversity.get(i,0)));
            
            if(currWidth > maxWidth)
                maxWidth = currWidth;
        }
        return maxWidth;
    }
    
    private int getClusterNumberTextWidth(Graphics2D g){
        
        FontMetrics fm = g.getFontMetrics();
        
        return  fm.stringWidth(String.valueOf(numberOfCentroids));
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
        // if (this.data == null) {
        //    return;
        //}
        if(this.elementSize.getHeight() < 1)
            return;
        
        if (this.isAntiAliasing) {
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        else{
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        }
        
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
        
        g.setColor(Color.black);
        int clusterNumX = elementSize.width*samples + TEXT_LEFT_MARGIN;
        int popX = clusterNumX + CLUSTER_POP_SPACER + getClusterNumberTextWidth((Graphics2D)g);
        int divX = popX + POP_DIV_SPACER + getPopulationTextWidth((Graphics2D)g);
        int centroidY;
        
        
        //draw the cluster information
        for (int row=top; row<bottom; row++) {
            centroidY = (row+1)*elementSize.height;
            g.drawString(Integer.toString(row+1), clusterNumX,centroidY);
            g.drawString(Integer.toString(clusterPopulation[row]), popX, centroidY);
            g.drawString(Float.toString(clusterDiversity.get(row,0)), divX, centroidY);
        }
        
        //draw cluster colors
        if(!selectedClusterList.isEmpty()){
            HCLCluster cluster;
            Color currColor = g.getColor();
            
            for(int i = 0; i < selectedClusterList.size(); i++){
                cluster = (HCLCluster)selectedClusterList.get(i);
                
                g.setColor(cluster.color);
                g.fillRect( elementSize.width*samples + 2, elementSize.height*cluster.root, 15, elementSize.height);
            }
            
        }
    }
    
    /**
     * Fills rect with specified row and colunn.
     */
    private void fillRectAt(Graphics g, int row, int column) {
        int x = column*elementSize.width;
        int y = row*elementSize.height;
        boolean mask = this.firstSelectedRow >= 0 && this.lastSelectedRow >= 0 && (row < this.firstSelectedRow || row > this.lastSelectedRow);
        g.setColor(getColor(this.experiment.get(getRow(row), getColumn(column))));
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
     * Draws rect with specified row, column and color.
     */
    private void drawRectAt(Graphics g, int row, int column, Color color) {
        g.setColor(color);
        g.drawRect(column*elementSize.width, row*elementSize.height, elementSize.width-1, elementSize.height-1);
    }
    
    private void drawCentroidRectangle(Graphics g, int centroidNumber, Color color){
        Color initColor = g.getColor();
        g.setColor(color);
        g.drawRect(0, centroidNumber * elementSize.height, samplesOrder.length * elementSize.width - 1, elementSize.height - 1);
        g.setColor(initColor);
    }
    
    private void fillCentroid(Graphics g, int centroidNumber){
        for(int col = 0; col < samplesOrder.length; col++)
            fillRectAt(g, centroidNumber, col);
    }
    
    private int getTopIndex(int top) {
        if (top < 0) {
            return 0;
        }
        return top/elementSize.height;
    }
    
    private int getLeftIndex(int left) {
        if (left < 0) {
            return 0;
        }
        return left/elementSize.width;
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
        if (targetx >= xSize || targetx < 0) {
            return -1;
        }
        return targetx/elementSize.width;
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
    
    public int getCurrentCentroidNumber(){
        return this.listener.getCurrCentroidNumber();
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
        return org.tigr.microarray.mev.cluster.clusterUtil.Cluster.GENE_CLUSTER;
    }
    
    private class Listener extends MouseAdapter implements ActionListener, MouseMotionListener{
        
        int x = 0;
        int y = 0;
        int currCentroidNumber;
        int oldCentroidNumber;
        Point origin;
        int originX;
        int originY;
        Rectangle expBounds;
        float xDim;
        float yDim;
        Graphics g;
        Color rectangleColor = Color.white;
        
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            
            
            
        }
        
        public void mouseDragged(java.awt.event.MouseEvent mouseEvent) {
        }
        
        public void mouseMoved(java.awt.event.MouseEvent mouseEvent) {
            x = (int)mouseEvent.getX();
            y = (int)mouseEvent.getY();
            
            currCentroidNumber = cursorOverCentroid(x,y);
            
            if(currCentroidNumber != oldCentroidNumber){
                g = getGraphics();
                
                if( currCentroidNumber != -1){
                    if(oldCentroidNumber != -1){
                        for(int col = 0; col < samplesOrder.length; col++)
                            fillRectAt(g, oldCentroidNumber, col);
                    }
                    
                    drawCentroidRectangle(g, currCentroidNumber, rectangleColor);
                    framework.setStatusText("Cluster # " +(currCentroidNumber+1)+",  Population: "+clusterPopulation[currCentroidNumber]+",  Diversity: "+clusterDiversity.get(currCentroidNumber,0));
                    
                }
                if(currCentroidNumber == -1 && oldCentroidNumber != -1){
                    for(int col = 0; col < samplesOrder.length; col++)
                        fillRectAt(g, oldCentroidNumber, col);
                    framework.setStatusText(" ");
                }
                
                oldCentroidNumber = currCentroidNumber;
            }
        }
        
        public void mouseExited(java.awt.event.MouseEvent mouseEvent){
            Graphics g = getGraphics();
            if(currCentroidNumber != -1)
                fillCentroid(g, currCentroidNumber);
            if(oldCentroidNumber != -1)
                fillCentroid(g, oldCentroidNumber);
            currCentroidNumber = -1;
        }
        
        
        private int cursorOverCentroid(int x, int y){
            currCentroidNumber = -1;
            
            originX = 0;
            originY = 0;
            
            xDim = (float)samplesOrder.length * elementSize.width;
            yDim = (float)numberOfCentroids * elementSize.height;
            
            if( (originX < x && x < originX + xDim) &&
            (originY < y && y < originY + yDim)){
                
                currCentroidNumber = (int)(numberOfCentroids * (y - originY)/yDim);
                
            }
            
            
            return currCentroidNumber;
        }
        
        public int getCurrCentroidNumber(){
            return currCentroidNumber;
            
        }
        
    }
    
    
    
    
    
}
