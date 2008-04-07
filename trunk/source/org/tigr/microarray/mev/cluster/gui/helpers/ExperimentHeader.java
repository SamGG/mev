/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ExperimentHeader.java,v $
 * $Revision: 1.9 $
 * $Date: 2006-07-13 16:08:37 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.clusterUtil.*;

/**
 * This class is used to render header of an experiment.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public class ExperimentHeader extends JPanel implements IExperimentHeader {
    private static final int RECT_HEIGHT = 15;
    private static final int COLOR_BAR_HEIGHT = 15;
    private Experiment experiment;
    private IData data;
    private IViewer iviewer;
    private int[] samplesOrder;
    private int[][] clusters;
    private ArrayList storedGeneColors;
    private ArrayList storedSampleColors = new ArrayList();
    private int compactedColorBarHeight = 0;
    private int clusterIndex;
    private int elementWidth;
    private boolean isAntiAliasing = true;
    private boolean isCompact = false;
    private float maxValue = 3f;
    private float minValue = -3f;
    private float midValue = 0.0f;
    private Insets insets = new Insets(0, 10, 0, 0);
    private BufferedImage negColorImage;
    private BufferedImage posColorImage;
    private int activeCluster = 0;
    private int maxSampleLabelLength = 0;
    private int[] ColorOverlaps = new int[1000];
    private int oldLabelLength = 0;
    private boolean mouseOnMap=false;
	private int mouseRow = 0;
	private int mouseColumn = 0;
	private int clickedRow = 0;
    private int clickedColumn = 0;
    public boolean clickedCell = false;

	public boolean clusterViewerClicked = false;
	public int clusterViewerClickedColumn = 0;
    
    public void setExperiment(Experiment e) {
    	this.experiment = e;
    }
    
    private boolean useDoubleGradient = true;
    
  
    protected void setIData(IData d) {this.data = d;}
    
    
    /**
     * Construct an <code>ExperimentHeader</code> with specified experiment.
     */
    public ExperimentHeader(Experiment experiment, int [][] clusters) {
        this(experiment, clusters, null);
    }
    
    /**
     * Construct an <code>ExperimentHeader</code> with specified experiment
     * and samples order.
     */
    public ExperimentHeader(Experiment experiment, int [][] clusters, int[] samplesOrder) {
        this.experiment = experiment;
        this.clusters = clusters;
        this.samplesOrder = samplesOrder == null ? createSamplesOrder(experiment) : samplesOrder;
        setBackground(Color.white);
	    Listener listener = new Listener();
	    addMouseListener(listener);
	    addMouseMotionListener(listener);
    }
    public ExperimentHeader(Experiment experiment, int [][] clusters, int[] samplesOrder, ArrayList storedGeneColors) {
        this.experiment = experiment;
        this.clusters = clusters;
        this.storedGeneColors = storedGeneColors;
        this.samplesOrder = samplesOrder == null ? createSamplesOrder(experiment) : samplesOrder;
        setBackground(Color.white);
	    Listener listener = new Listener();
	    addMouseListener(listener);
	    addMouseMotionListener(listener);
    }
    public Insets getInsets(){return insets;}
    public Experiment getExperiment() {
    	return experiment;
    }
    public int[][] getClusters() {
    	return clusters;
    }
    /**
     * Returns a component to be inserted into scroll pane view port.
     */
    public JComponent getContentComponent() {
        return this;
    }
    
    private static int[] createSamplesOrder(Experiment experiment) {
        int[] order = new int[experiment.getNumberOfSamples()];
        for (int i=0; i<order.length; i++) {
            order[i] = i;
        }
        return order;
    }
    public int[] getSamplesOrder(){return samplesOrder;}
    public BufferedImage getPosColorImage(){return posColorImage;}
    public BufferedImage getNegColorImage(){return negColorImage;}
    /**
     * Sets data.
     */
    public void setData(IData data) {
        this.data = data;
    }
    public IData getData(){return data;}
    
    /**
     * Sets max and min experiment values.
     */
    public void setValues(float minValue, float midValue, float maxValue) {
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.midValue = midValue;
    }

    /**
     * Sets max and min experiment values.
     */
    public void setValues(float minValue, float maxValue) {
        this.maxValue = maxValue;
        this.minValue = minValue;
    }
    /**
     * Sets whether the color-coded clustering display is compact
     */
    public void setCompactClusters(boolean isCompact){
    	this.isCompact=isCompact;
    	storedSampleColors.clear();
    }
    
    /**
     * Sets positive and negative images
     */
    public void setNegAndPosColorImages(BufferedImage neg, BufferedImage pos){
        this.negColorImage = neg;
        this.posColorImage = pos;
    }
    /**
     * Sets stored Color Array List
     */
    public void setStoredColors(ArrayList storedColors){
    	this.storedGeneColors = storedColors;
    }
    
    /**
     * Sets flag to use a double gradient 
     */
    public void setUseDoubleGradient(boolean useDouble) {
    	this.useDoubleGradient = useDouble;
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
     * Sets current cluster index
     */
    public void setClusterIndex(int index){
        clusterIndex = index;
    }
    
    /**
     *  Gets current cluster
     */
    private int [] getCluster(){
        return clusters[clusterIndex];
    }
    
    /**
     * Returns height of color bar for experiments
     */
    private int getColorBarHeight(){
        for( int sample = 0; sample < samplesOrder.length ; sample++){
            if(data.getExperimentColor(experiment.getSampleIndex(this.samplesOrder[sample])) != null)
                return COLOR_BAR_HEIGHT;
        }
        return 0;
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
     * draws rectangle around specified sample cluster colors
     * @param column
     * @param color
     * @param cluster refers to whether the rectangle surrounds the cluster display
     */
    public void drawClusterHeaderRectsAt(int column, Color color, boolean cluster){
    	Graphics2D g = (Graphics2D)getGraphics();
        if (g == null) {
            return;
        }
        int side = 0;
        int inset = 0;
        if (cluster)
        	inset = 4;
        g.setColor(color);
        if (column > (experiment.getNumberOfSamples() -1))
        		side = 10;
        if (column > (experiment.getNumberOfSamples() -1)&&isCompact)
        	return;
    	if (!isCompact)
    		g.drawRect(column*elementWidth + insets.left + inset, getSize().height - (COLOR_BAR_HEIGHT*(storedSampleColors.size()) + maxSampleLabelLength + 7) , (elementWidth), (COLOR_BAR_HEIGHT)*storedSampleColors.size()+maxSampleLabelLength+4 + side);
    	if (isCompact)
    		g.drawRect(column*elementWidth + insets.left + inset, getSize().height - (COLOR_BAR_HEIGHT*(compactedColorBarHeight) + maxSampleLabelLength + 7), (elementWidth-1), (COLOR_BAR_HEIGHT)*compactedColorBarHeight+maxSampleLabelLength+4 + side);
    	
    }
    
    /**
     * Updates size of this header.
     */
    public void updateSizes(int contentWidth, int elementWidth) {
        if(data == null)
            return;
        setElementWidth(elementWidth);
        Graphics2D g = (Graphics2D)getGraphics();
        if (g == null) {
            return;
        }
        if (isAntiAliasing) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        FontMetrics hfm = g.getFontMetrics();
        int maxHeight = 0;
        String name;
        final int size = this.experiment.getNumberOfSamples();
        for (int feature = 0; feature < size; feature++) {
            name = data.getSampleName(experiment.getSampleIndex(feature));
            maxHeight = Math.max(maxHeight, hfm.stringWidth(name));
        }
        maxSampleLabelLength = maxHeight;
        maxHeight += RECT_HEIGHT + hfm.getHeight() + 10;
        if (!isCompact){
        	maxHeight += (getColorBarHeight()*storedSampleColors.size());
            String sampleLabel;
            int labelLength=0;
            for (int feature = 0; feature < storedSampleColors.size(); feature++) {
                sampleLabel = data.getClusterLabel(feature, false);
                labelLength = Math.max(labelLength, hfm.stringWidth(sampleLabel));
            }
            if (labelLength != oldLabelLength) {
            	
            	contentWidth = contentWidth - oldLabelLength + labelLength; 
            	oldLabelLength = labelLength;
            }
        }
        if(isCompact){
    		int maxSpacesOver=-1;
    		for (int i=0; i<storedSampleColors.size(); i++){
    			if ((ColorOverlaps[i])>maxSpacesOver)
    				maxSpacesOver=ColorOverlaps[i];
    		}
    		maxHeight += getColorBarHeight()*(maxSpacesOver+1);
    		compactedColorBarHeight= maxSpacesOver+1;
    	}
        setSize(contentWidth, maxHeight);
        setPreferredSize(new Dimension(contentWidth, maxHeight));
        //System.out.println("contentWidth" + contentWidth);
        drawHeader(g);
    }
    
    /**
     * Paints the header into specified graphics.
     */
    public void paint(Graphics g1D) {
        super.paint(g1D);
        if (data == null || (this.getCluster().length < 1)) {
            return;
        }
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
        final int samples = this.experiment.getNumberOfSamples();
        
        if (samples == 0) {
            return;
        }
        
        int width = samples*elementWidth;
        
        if(useDoubleGradient) {
	        g.drawImage(this.negColorImage, insets.left, 0, (int)(width/2f), RECT_HEIGHT, null);
	        g.drawImage(this.posColorImage, (int)((width)/2f + insets.left), 0, (int)(width/2.0), RECT_HEIGHT, null);
        } else {
	        g.drawImage(this.posColorImage, insets.left, 0, width, RECT_HEIGHT, null);
        }

        FontMetrics hfm = g.getFontMetrics();
        int descent = hfm.getDescent();
        int fHeight = hfm.getHeight();
        
        g.setColor(Color.black);
        
        int textWidth;
        g.drawString(String.valueOf(this.minValue), insets.left, RECT_HEIGHT+fHeight);
        textWidth = hfm.stringWidth(String.valueOf(midValue));
        if(useDoubleGradient)
        	g.drawString(String.valueOf(midValue), (int)(width/2f)-textWidth/2 + insets.left, RECT_HEIGHT+fHeight);
        textWidth = hfm.stringWidth(String.valueOf(this.maxValue));
        g.drawString(String.valueOf(this.maxValue), width-textWidth + insets.left, RECT_HEIGHT+fHeight);
        
        //draw possible clusters
        int h = -getSize().height + 5;
        boolean hasColorBar = false;
        if(this.getColorBarHeight() > 0){
        	if (isCompact)
        		h += COLOR_BAR_HEIGHT*compactedColorBarHeight;
        	if (!isCompact)
        		h += COLOR_BAR_HEIGHT*storedSampleColors.size();
            hasColorBar = true;
        }
        
        // draw feature names
        String name;
        g.rotate(-Math.PI/2);
        for (int sample = 0; sample < samples; sample++) {
            name = data.getSampleName(experiment.getSampleIndex(this.samplesOrder[sample]));
            g.drawString(name, h, descent + elementWidth*sample + elementWidth/2 + insets.left);
        }
        //write the gene cluster names sideways
        g.rotate(Math.PI/2);
        int visibleClusters = 0;
        if (storedGeneColors!=null&&!isCompact){
	        String clusterName;
	        g.rotate(-Math.PI/2);
	        int numberOfClusters = storedGeneColors.size();
	        for (int cluster = 0; cluster < numberOfClusters; cluster++) {
	        	if (data.getClusterLabel(cluster, true)==null) break;
	        	int index = data.getVisibleCluster((Color)storedGeneColors.get(cluster), true);
	        	clusterName = data.getClusterLabel(index, true);
	        	g.drawString(clusterName, h, descent + elementWidth*(samples+cluster) + elementWidth/2 + insets.left+5);
		        visibleClusters++;
	        }
	        g.rotate(Math.PI/2);
        }
        //write the sample cluster names
        if (storedSampleColors!=null&&!isCompact){
	        String clusterName;
	        int numberOfClusters = storedSampleColors.size();
	        for (int cluster = 0; cluster < numberOfClusters; cluster++) {
	        	if (data.getClusterLabel(cluster, false)==null) break;
	        	int index = data.getVisibleCluster((Color)storedSampleColors.get(cluster), false);
	        	clusterName = data.getClusterLabel(index, false);
		        g.drawString(clusterName, elementWidth*(samples+visibleClusters) + insets.left+10, -h + COLOR_BAR_HEIGHT*(numberOfClusters-(cluster)));
	        }
        }
        
        int spacesOver=0;
        if(hasColorBar){
            int sscLength= storedSampleColors.size();
            for(int sample = 0; sample < samples; sample++){
                Color[] colors = data.getSampleColorArray(experiment.getSampleIndex(this.samplesOrder[sample]));
                if (colors==null) { continue;}
                for (int clusters=0; clusters<colors.length; clusters++){
	            	if (colors[clusters]==null) {System.out.println("colors null");continue;}
	            	if(storedSampleColors.contains(colors[clusters])) {
	                	activeCluster=storedSampleColors.indexOf(colors[clusters]);
	                }else{
		                storedSampleColors.add(colors[clusters]);
		                activeCluster=(storedSampleColors.size()-1);
	                	ColorOverlaps[activeCluster]= activeCluster;
	                	//compacts the cluster color display
	                	boolean compact= false;
	                	if (isCompact)compact=true;
	                	while (compact){
	                		for (int i=0; i<storedSampleColors.size(); i++){
	                			boolean allClear = true;
	                			for (int j=0; j<storedSampleColors.size(); j++){
	                				if (ColorOverlaps[j]==i){
	    			                	if (data.isColorOverlap(experiment.getSampleIndex(this.samplesOrder[sample]), colors[clusters], (Color)storedSampleColors.get(j), false)){
	    			                		allClear=false;
	    			                		break;
	    			                		}
	    		                			allClear=true;
	    		                		}	
	                				}
	                			if (allClear){
	                				ColorOverlaps[activeCluster]= i;
	                				compact=true;
	                				break;
	                			}
	                		}
	                		if (compact) break;
	                	}
	                }
	                spacesOver=ColorOverlaps[activeCluster];
	                g.setColor(colors[clusters]);
	                if (sscLength!= storedSampleColors.size()){
                    g.setColor(Color.white);
	                }
	                g.fillRect(sample*elementWidth + insets.left, getSize().height - (COLOR_BAR_HEIGHT*(1+spacesOver)) - 2, elementWidth, COLOR_BAR_HEIGHT);
                }
            }
            int sizeTest = getSize().width;
            if (sscLength!= storedSampleColors.size()){
            	updateSizes(getSize().width, elementWidth);
            }
            if (sizeTest != getSize().width) repaint();
        }
        if (mouseOnMap){
        	if (mouseColumn != clickedColumn)
        		drawClusterHeaderRectsAt(mouseColumn, Color.gray, false);
        	if (mouseRow != clickedRow)	
        		drawHorizontalRect(mouseRow, Color.gray);
        }
        mouseOnMap = false;
        if (clickedCell){
            g.setColor(Color.red);
            if (!isCompact){
            	g.drawRect(insets.left, this.getHeight()-COLOR_BAR_HEIGHT*(storedSampleColors.size() - clickedRow)-3, elementWidth*experiment.getNumberOfSamples() + 5 + elementWidth*this.storedGeneColors.size() + 5 + oldLabelLength + 2, COLOR_BAR_HEIGHT);
            	g.drawRect(clickedColumn*elementWidth + insets.left, getSize().height - (COLOR_BAR_HEIGHT*(storedSampleColors.size()) + maxSampleLabelLength + 7) , (elementWidth), (COLOR_BAR_HEIGHT)*storedSampleColors.size()+maxSampleLabelLength+4);
        	 }
        }    
        if (clusterViewerClicked&&!isCompact){
        	g.setColor(Color.red);
        	g.drawRect(clusterViewerClickedColumn*elementWidth + insets.left + 4, getSize().height - (COLOR_BAR_HEIGHT*(storedSampleColors.size()) + maxSampleLabelLength + 7) , (elementWidth), (COLOR_BAR_HEIGHT)*storedSampleColors.size()+maxSampleLabelLength+4 + 10);
        }
    }  
    
    private void drawHorizontalRect(int row, Color color){
    	Graphics2D g = (Graphics2D)getGraphics();
        if (g == null) {
            return;
        }
        //if (isCompact)
        //	g.drawRect(insets.left, this.getHeight()-COLOR_BAR_HEIGHT*(compactedColorBarHeight - row)-3, this.getWidth(), COLOR_BAR_HEIGHT);

                    g.setColor(color);
        if (!isCompact)
        	g.drawRect(insets.left, this.getHeight()-COLOR_BAR_HEIGHT*(storedSampleColors.size() - row)-3, elementWidth*experiment.getNumberOfSamples() + 5 + elementWidth*this.storedGeneColors.size() + 5 + oldLabelLength + 2, COLOR_BAR_HEIGHT);
    }
    
    private class Listener extends MouseAdapter implements MouseMotionListener {
        
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
            if (row==clickedRow&&column==clickedColumn){
            	clickedCell = false;
            	return;
            }
            clickedRow = row;
            clickedColumn = column;
            clickedCell = true;
            drawClusterHeaderRectsAt(clickedColumn, Color.red, false);
        	drawHorizontalRect(clickedRow, Color.red);
        }
        public void mouseExited(MouseEvent event){
        	mouseOnMap = false;
        	
        	repaint();
        	if (clickedCell&&!isCompact){
            	
            	drawClusterHeaderRectsAt(clickedColumn, Color.red, false);
                drawHorizontalRect(clickedRow, Color.red);
            }
        }
        public void mouseMoved(MouseEvent event) {
            if (experiment.getNumberOfSamples() == 0 || event.isShiftDown())
                return;
            int column = findColumn(event.getX());
            int row = findRow(event.getY());
            Graphics g = null;
            g = getGraphics();
            if (!isLegalPosition(row, column)){
            	repaint();
            	if (clickedCell&&!isCompact){
                	drawClusterHeaderRectsAt(clickedColumn, Color.red, false);
                    drawHorizontalRect(clickedRow, Color.red);
                }
            	return;
            }
            if (isCurrentPosition(row, column)) {
                if (isLegalPosition(row, column)){
                	drawClusterHeaderRectsAt(oldColumn, Color.gray, false);
                	drawHorizontalRect(row, Color.gray);
                }
                if (clickedCell&&!isCompact){
                	drawClusterHeaderRectsAt(clickedColumn, Color.red, false);
                    drawHorizontalRect(clickedRow, Color.red);
                }
                return;
            }
            if (!isCurrentPosition(row, column)&&isLegalPosition(row, column)){
            	mouseOnMap = true;
            	mouseRow = row;
            	mouseColumn = column;
            	repaint();
            	drawClusterHeaderRectsAt(oldColumn, Color.gray, false);
            	if (clickedCell&&!isCompact){
                	drawClusterHeaderRectsAt(clickedColumn, Color.red, false);
                    drawHorizontalRect(clickedRow, Color.red);
                }
            }/*
            if (isLegalPosition(row, column)&& (column < (experiment.getNumberOfSamples() -1))) {
                drawRectAt(g, row, column, Color.white);
                drawClusterRectsAt(g, row, column, Color.gray);
        } else {
            	repaint();
            }
            if (isLegalPosition(oldRow, oldColumn)) {
                g = g != null ? g : getGraphics();
                fillRectAt(g, oldRow, oldColumn);
            }*/
            setOldPosition(row, column);
            if (g != null) {
                g.dispose();
            }
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
    /**
     * Finds column for specified x coordinate.
     * @return -1 if column was not found.
     */
    private int findColumn(int targetx) {
        int xSize = experiment.getNumberOfSamples()*elementWidth;
        if (targetx < insets.left) {
            return -1;
        }
        if (targetx > (xSize + insets.left))
        	return -1;
        return (targetx - insets.left)/elementWidth;
    }
    
    /**
     * Finds row for specified y coordinate.
     * @return -1 if row was not found.
     */
    private int findRow(int targety) {
    	int length = 0;
    	if (!isCompact)
    		length = (this.getSize().height-getColorBarHeight()*storedSampleColors.size());
    		
    	if (isCompact)
    		length = this.getSize().height -compactedColorBarHeight*getColorBarHeight();
        if (targety >= this.getSize().height || targety < (length))
            return -1;
        return (targety - length)/COLOR_BAR_HEIGHT;
    }
    
    private boolean isLegalPosition(int row, int column) {
        if (isLegalRow(row) && isLegalColumn(column))
            return true;
        return false;
    }  
    
    private boolean isLegalColumn(int column) {
        if (column < 0 || column > (experiment.getNumberOfSamples() -1))
            return false;
        return true;
    }
    
    private boolean isLegalRow(int row) {
        if (row < 0 || row > storedSampleColors.size())
            return false;
        if (isCompact){
        	if (row < 0 || row > compactedColorBarHeight)
                return false;
        }
        return true;
    }
}
