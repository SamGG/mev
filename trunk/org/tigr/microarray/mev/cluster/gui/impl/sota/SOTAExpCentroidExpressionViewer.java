/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SOTAExpCentroidExpressionViewer.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.sota;

import java.util.Arrays;
import java.util.ArrayList;

import java.awt.Font;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Insets;
import java.awt.event.*;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionListener;

import javax.swing.event.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLCluster;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;



public class SOTAExpCentroidExpressionViewer extends JPanel implements IViewer{
    
    private int numberOfCentroids;
    private int [] clusterPopulation;
    private FloatMatrix clusterDiversity;
    
    private int TEXT_LEFT_MARGIN = 20;
    private int CLUSTER_POP_SPACER = 20;
    private int POP_DIV_SPACER = 20;
    private static final float INITIAL_MAX_VALUE = 3f;
    private static final float INITIAL_MIN_VALUE = -3f;
    private static final String NO_GENES_STR = "No Experiments in Cluster!";
    private static final Font ERROR_FONT = new Font("monospaced", Font.BOLD, 20);
    
    private ColorBarHeader header;
    private Experiment experiment;
    private Experiment experimentMap;
    
    private IFramework framework;
    private IData data;
    private int clusterIndex;
    private int labelIndex;
    private int[][] clusters;
    private int[] samplesOrder;
    private Dimension elementSize = new Dimension(40, 20);
    private boolean isAntiAliasing = true;
    private boolean isDrawBorders = true;
    public static Color missingColor = new Color(128, 128, 128);
    public static Color maskColor = new Color(255, 255, 255, 128);
    private float maxValue = INITIAL_MAX_VALUE;
    private float minValue = INITIAL_MIN_VALUE;
    private int firstSelectedRow = -1;
    private int lastSelectedRow  = -1;
    private ArrayList selectedClusterList;
    public BufferedImage posColorImage = createGradientImage(Color.black, Color.red);
    public BufferedImage negColorImage = createGradientImage(Color.green, Color.black);
    private int maxUniqueIDWidth, maxGeneNameWidth;
    private Listener listener;
    private Insets insets = new Insets(0,10,0,0);
    private int numberOfGenes;
    private boolean haveColorBar = false;
    
    
    /**
     * Constructs an <code>SOTAExpCentroidEpressionViewer</code> with specified
     * experiment, clusters, samples order and draw annotations attribute.
     *
     * @param centroidData, contains the values of the cluster centroids.
     * @param clusters the two dimensional array with gene indices.
     * @param samplesOrder the one dimensional array with samples indices.
     * @param clusterPop array containing genes per cluster
     * @param clusterDiv diversity measure of each cluster
     * @param selClusterList, ArrayList shared with parent viewer which accumulates selected clusters
     */
    public SOTAExpCentroidExpressionViewer(Experiment centroidData, Experiment experimentMap, int[][] clusters, int[] samplesOrder, int[] clusterPop, FloatMatrix clusterDiv, ArrayList selClusterList) {
        if (centroidData == null) {
            throw new IllegalArgumentException("experiment == null");
        }
        this.experiment = centroidData;
        this.experimentMap = experimentMap;
        this.numberOfGenes = this.experiment.getNumberOfGenes();
        this.clusterPopulation = clusterPop;
        this.clusterDiversity = clusterDiv;
        this.numberOfCentroids = clusterPopulation.length;
        this.selectedClusterList = selClusterList;
        this.clusters = clusters == null ? defGenesOrder(experiment.getNumberOfGenes()) : clusters;
        this.samplesOrder = samplesOrder == null ? defSamplesOrder(experiment.getNumberOfSamples()) : samplesOrder;
        this.header = new ColorBarHeader(this.numberOfCentroids);
        this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
        setBackground(Color.white);
        
        listener = new Listener();
        this.addMouseMotionListener(listener);
        this.addMouseListener(listener);
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
        
        Integer userObject = (Integer)framework.getUserObject();
        setClusterIndex(userObject == null ? 0 : userObject.intValue());
        IDisplayMenu menu = framework.getDisplayMenu();
        this.labelIndex = menu.getLabelIndex();
        this.maxValue = Math.abs(menu.getMaxRatioScale());
        this.minValue = -Math.abs(menu.getMinRatioScale());
        setElementSize(menu.getElementSize());
        setAntialiasing(menu.isAntiAliasing());
        setDrawBorders(menu.isDrawingBorder());
        
        updateSize();
        this.posColorImage = menu.getPositiveGradientImage();
        this.negColorImage = menu.getNegativeGradientImage();
        if(this.selectedClusterList.size() > 0)
            this.insets.top = elementSize.height+1;
        else
            this.insets.top = 0;
        this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
        //header.setValues(maxValue, minValue);
        header.setValues(minValue, maxValue);
        header.setAntiAliasing(menu.isAntiAliasing());
        header.updateSizes(getSize().width, elementSize.width);
    }
    
    
    
    /**
     * Updates appropriate attributes of this viewer and its header.
     * @see IViewer#onMenuChanged
     */
    public void onMenuChanged(IDisplayMenu menu) {
        setDrawBorders(menu.isDrawingBorder());
        this.labelIndex = menu.getLabelIndex();
        this.maxValue = Math.abs(menu.getMaxRatioScale());
        this.minValue = -Math.abs(menu.getMinRatioScale());
        header.setValues(minValue, maxValue);
        this.posColorImage = menu.getPositiveGradientImage();
        this.negColorImage = menu.getNegativeGradientImage();
        this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
        setElementSize(menu.getElementSize());
        if(this.selectedClusterList.size() > 0)
            this.insets.top = elementSize.height+1;
        setAntialiasing(menu.isAntiAliasing());
        haveColorBar = areProbesColored();
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
        if(this.selectedClusterList.size() > 0)
            this.insets.top = elementSize.height + 1;
        else
            this.insets.top = 0;
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
    protected Experiment getExperiment() {
        return experiment;
    }
    
    /**
     *	Returns the row (index) within the main iData which corresponds to
     *  the passed index to the clusters array
     */
    private int getMultipleArrayDataRow(int clusterArrayRow) {
        return experimentMap.getGeneIndexMappedToData(clusterArrayRow);
    }
    
    /**
     * Converts cluster indicies from the experiment to IData rows which could be different
     */
    private int [] getIDataRowIndices(int [] expIndices){
        int [] dataIndices = new int[expIndices.length];
        for(int i = 0; i < expIndices.length; i++){
            dataIndices[i] = experimentMap.getGeneIndexMappedToData(expIndices[i]);
        }
        return dataIndices;
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
        this.data.setExperimentColor(getIDataRowIndices(getCluster()), color);
        this.insets.top = elementSize.height+1;
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
        int width = elementSize.width*numberOfCentroids+insets.left+1;
        
        width += getAnnotationWidth(g) + 30;
        
        int height = elementSize.height*this.experiment.getNumberOfGenes()+insets.top+1;
        setSize(width, height);
        setPreferredSize(new Dimension(width, height));
    }
    
    private int getAnnotationWidth(Graphics g){
        int maxWidth = 0;
        FontMetrics fm = g.getFontMetrics();
        for(int i = 0; i < numberOfGenes; i++){
            maxWidth = Math.max(maxWidth, fm.stringWidth(data.getElementAttribute(getMultipleArrayDataRow(i), labelIndex)));
        }
        return maxWidth;
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
        float maximum = value < 0 ? this.minValue : this.maxValue;
        int colorIndex = (int)(255*value/maximum);
        colorIndex = colorIndex > 255 ? 255 : colorIndex;
        int rgb = value < 0 ? negColorImage.getRGB(255-colorIndex, 0) : posColorImage.getRGB(colorIndex, 0);
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
        
        if (this.isAntiAliasing) {
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        else{
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        }
        
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
        final int right = getRightIndex(bounds.x+bounds.width, numberOfCentroids);
        
        int x, y;
        // draw rectangles
        for (int column=left; column<right; column++) {
            for (int row=top; row<bottom; row++) {
                fillRectAt(g, row, column);
            }
        }
        
        g.setColor(Color.black);
        //	int clusterNumX = elementSize.width*samples + TEXT_LEFT_MARGIN;
        //	int popX = clusterNumX + CLUSTER_POP_SPACER + getClusterNumberTextWidth((Graphics2D)g);
        //	int divX = popX + POP_DIV_SPACER + getPopulationTextWidth((Graphics2D)g);
        //	int centroidY;
        
        
        //draw the cluster information
/*	for (int row=top; row<bottom; row++) {
            centroidY = (row+1)*elementSize.height;
            g.drawString(Integer.toString(row+1), clusterNumX,centroidY);
            g.drawString(Integer.toString(clusterPopulation[row]), popX, centroidY);
            g.drawString(Float.toString(clusterDiversity.get(row,0)), divX, centroidY);
        }
 */
        //draw cluster colors
        if(!selectedClusterList.isEmpty()){
            HCLCluster cluster;
            Color currColor = g.getColor();
            
            for(int i = 0; i < selectedClusterList.size(); i++){
                cluster = (HCLCluster)selectedClusterList.get(i);
                g.setColor(cluster.color);
                //	g.fillRect( elementSize.width*numberOfCentroids + 2, elementSize.height*cluster.root, 15, elementSize.height);
                g.fillRect( elementSize.width*cluster.root+insets.left, 0, elementSize.width, elementSize.height );
            }
        }
       
        
        if (right >= numberOfCentroids) {
            String label = "";
            g.setColor(Color.black);
            int uniqX = elementSize.width*numberOfCentroids+10;
            int annY;
            for (int row=top; row<bottom; row++) {
                if (labelIndex >= 0) {
                    label = data.getElementAttribute(getMultipleArrayDataRow(row), labelIndex);
                }
                annY = (row+1)*elementSize.height+insets.top;
                g.drawString(label, uniqX + insets.left, annY);
            }
        }
    }
    
    /**
     * Fills rect with specified row and colunn.
     */
    private void fillRectAt(Graphics g, int row, int column) {
        int x = column*elementSize.width+insets.left;
        int y = row*elementSize.height + insets.top;
        boolean mask = this.firstSelectedRow >= 0 && this.lastSelectedRow >= 0 && (row < this.firstSelectedRow || row > this.lastSelectedRow);
        if(clusterPopulation[column] > 0)
            g.setColor(getColor(this.experiment.get(getRow(row), getColumn(column))));
        else
            g.setColor(missingColor);
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
        g.drawRect(column*elementSize.width+insets.left, row*elementSize.height+insets.top, elementSize.width-1, elementSize.height-1);
    }
    
    private void drawCentroidRectangle(Graphics g, int centroidNumber, Color color){
        Color initColor = g.getColor();
        g.setColor(color);
        g.drawRect(centroidNumber * elementSize.width + insets.left, insets.top, elementSize.width-1,numberOfGenes * elementSize.height - 1);
        g.setColor(initColor);
    }
    
    private void fillCentroid(Graphics g, int centroidNumber){
        for(int row = 0; row < numberOfGenes; row++)
            fillRectAt(g, row, centroidNumber);
    }
    
    private int getTopIndex(int top) {
        if (top < insets.top) {
            return 0;
        }
        return (top - insets.top)/elementSize.height;
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
        int result = (bottom - insets.top)/elementSize.height+1;
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
        int ySize = getCluster().length*elementSize.height+insets.top;
        if (targety >= ySize || targety < insets.top)
            return -1;
        return (targety-insets.top)/elementSize.height;
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
    
    
    
    /*
    public void setPopupLocation(int x, int y){
        this.infoPopup.setLocation(getLocationOnScreen().x+x,getLocationOnScreen().y+y);
    }
     
     */
    
    
    
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
                        for(int row = 0; row < numberOfGenes; row++)
                            fillRectAt(g, row, oldCentroidNumber);
                    }
                    drawCentroidRectangle(g, currCentroidNumber, rectangleColor);
                    framework.setStatusText("Cluster # " +(currCentroidNumber+1)+",  Population: "+clusterPopulation[currCentroidNumber]+",  Diversity: "+clusterDiversity.get(currCentroidNumber,0));                    
                }
                if(currCentroidNumber == -1 && oldCentroidNumber != -1){
                    for(int row = 0; row < numberOfGenes; row++)
                        fillRectAt(g, row, oldCentroidNumber);
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
            
            xDim = (float)numberOfCentroids * elementSize.width;
            yDim = (float)numberOfGenes * elementSize.height;
            //   xDim = (float)samplesOrder.length * elementSize.width;
            //   yDim = (float)numberOfCentroids * elementSize.height;
            
            if( (insets.left < x && x < insets.left + xDim) &&
            (originY < y && y < originY + yDim)){
                currCentroidNumber = (int)(numberOfCentroids * (x - insets.left)/xDim);
            }
  /*          if(currCentroidNumber!=-1){
                infoPopup.setValues(currCentroidNumber, clusterPopulation[currCentroidNumber], experiment.get(5,currCentroidNumber));
                setPopupLocation(x,y);
                infoPopup.setVisible(true);
            }
            else
                infoPopup.setVisible(false);
   */
            return currCentroidNumber;
        }
        
        public int getCurrCentroidNumber(){
            return currCentroidNumber;
            
        }
        
    }
    
    public class ColorBarHeader extends JPanel {
        
        private static final int RECT_HEIGHT = 15;
        private int elementWidth;
        private boolean isAntiAliasing = true;
        private float maxValue = 3f;
        private float minValue = -3f;
        private Insets insets = new Insets(0, 10, 0, 0);
        private int numberOfElements;
        private BufferedImage negColorImage;
        private BufferedImage posColorImage;
        
        
        /**
         * Construct an <code>ExperimentHeader</code> with specified experiment.
         */
        public ColorBarHeader(int NumberOfElements) {
            numberOfElements = NumberOfElements;
            setBackground(Color.white);
        }
        
        /**
         * Sets max and min experiment values.
         */
        public void setValues(float minValue, float maxValue) {
            this.maxValue = maxValue;
            this.minValue = minValue;
        }
        
        /**
         * Sets positive and negative images
         */
        public void setNegAndPosColorImages(BufferedImage neg, BufferedImage pos){
            this.negColorImage = neg;
            this.posColorImage = pos;
        }
        
        /**
         * Sets anti-aliasing property.
         */
        public void setAntiAliasing(boolean isAntiAliasing) {
            this.isAntiAliasing = isAntiAliasing;
        }
        
        /**
         * Sets the left margin for the header
         */
        public void setLeftInset(int leftMargin){
            insets.left = leftMargin;
        }
        
        /**
         * Sets an element width.
         */
        private void setElementWidth(int width) {
            this.elementWidth = width;
            if (width > 12) {
                width = 12;
            }
            setFont(new Font("monospaced", Font.PLAIN, width));
        }
        
        /**
         * Updates size of this header.
         */
        public void updateSizes(int contentWidth, int elementWidth) {
            
            setElementWidth(elementWidth);
            Graphics2D g = (Graphics2D)getGraphics();
            if (g == null) {
                return;
            }
            if (isAntiAliasing) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
            FontMetrics fm = g.getFontMetrics();
            int maxHeight = RECT_HEIGHT + 10 + fm.getHeight();
            int width = numberOfElements * elementWidth;
            setSize(width, maxHeight);
            setPreferredSize(new Dimension(width, maxHeight));
        }
        
        /**
         * Paints the header into specified graphics.
         */
        public void paint(Graphics g1D) {
            super.paint(g1D);
            Graphics2D g = (Graphics2D)g1D;
            if (isAntiAliasing) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
            drawHeader(g);
        }
        
        /**
         * Draws the header into specified graphics.
         */
        private void drawHeader(Graphics2D g) {
            
            int width = numberOfElements*elementWidth;
            g.drawImage(this.negColorImage, insets.left, 0, (int)(width/2f), RECT_HEIGHT, null);
            g.drawImage(this.posColorImage, (int)((width)/2f + insets.left), 0, (int)(width/2.0), RECT_HEIGHT, null);
            FontMetrics hfm = g.getFontMetrics();
            int descent = hfm.getDescent();
            int fHeight = hfm.getHeight();
            g.setColor(Color.black);
            int textWidth;
            g.drawString(String.valueOf(this.minValue), insets.left, RECT_HEIGHT+fHeight);
            textWidth = hfm.stringWidth("1:1");
            g.drawString("1:1", (int)(width/2f)-textWidth/2 + insets.left, RECT_HEIGHT+fHeight);
            textWidth = hfm.stringWidth(String.valueOf(this.maxValue));
            g.drawString(String.valueOf(this.maxValue), width-textWidth + insets.left, RECT_HEIGHT+fHeight);
        }
        
    }
    
  /*      public class InfoPopup extends JFrame{
    //    JMenuItem clusterNumber;
    //    JMenuItem clusterPop;
    //    JMenuItem clusterDiv;
            InfoPanel panel;
        int num = 1;
        int pop = 0;
        float div = 0;
   
        public InfoPopup(){
            super();
            panel = new InfoPanel();
            setSize(new Dimension(70, 50));
            setBackground(Color.white);
            getContentPane().add(panel);
          //  clusterNumber = new JMenuItem();
          //  clusterNumber.setBackground(Color.white);
          //  clusterPop = new JMenuItem();
         //   clusterPop.setBackground(Color.white);
        //    clusterDiv = new JMenuItem();
        //    clusterDiv.setBackground(Color.white);
        //    add(clusterNumber);
        //    add(clusterPop);
        //    add(clusterDiv);
        }
   
        public void setValues(int num, int pop, float div){
           // clusterNumber.setText("Cluster #: "+num);
          //  clusterPop.setText("Pop: "+pop);
          //  clusterDiv.setText("Div: "+div);
            this.num = num;
          this.pop = pop;
          this.div =div;
        }
   
        public class InfoPanel extends JPanel{
   
            public InfoPanel(){
            super();
            setSize(70, 50);
            setPreferredSize(new Dimension(70, 50));
            setBackground(Color.white);
            }
   
        public void paint(Graphics g){
            super.paint(g);
            g.drawString("Cluster #: " +(num+1), 5, 15);
            g.drawString("Pop.: " +pop, 5, 30);
            g.drawString("Div.: " +div, 5, 45);
        }
        }
    }
   */
}
