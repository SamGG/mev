/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ExperimentClusterHeader.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Font;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;


public class ExperimentClusterHeader extends JPanel implements IExperimentHeader{
    
    private static final int RECT_HEIGHT = 15;
    private static final int COLOR_BAR_HEIGHT = 15;
    private Experiment experiment;
    private IData data;
    private int[][] samplesOrder;
    private ArrayList<Color> storedGeneColors;
    private ArrayList<Color> storedSampleColors = new ArrayList<Color>();
    private ArrayList<Color> savedSampleColorOrder = new ArrayList<Color>();
    private int compactedColorBarHeight = 0;

    private int elementWidth;
    private boolean isAntiAliasing = true;
    private boolean isCompact = false;
    public boolean isShowRects = true;
    private float maxValue = 3f;
    private float minValue = -3f;
    private float midValue = 0.0f;    
    int clusterIndex = 0;
    private String centroidName;
    private boolean hasCentroid;
    private int activeCluster = 0;
    private static int[] ColorOverlaps = new int[1000000];
    
    private boolean mouseOnMap=false;
	private int mouseRow = 0;
	private int mouseColumn = 0;
	private int clickedRow = 0;
    private int clickedColumn = 0;
    public boolean clickedCell = false;
    private int maxSampleLabelLength = 0;
    private int oldLabelLength = 0;
	public boolean clusterViewerClicked = false;
	public int clusterViewerClickedColumn = 0;
	
    private boolean isDrag = false;
    private boolean headerDrag = false;
    private int headerDragColumn = 0;
	private int headerDragRow = 0;
    private int dragColumn = 0;
	private int dragRow = 0;
    private int startColumn = 0;
    private int startRow = 0;
    private BufferedImage negColorImage;
    private BufferedImage posColorImage;
    
    private Insets insets = new Insets(0, 10, 0, 0);
    
    private boolean useDoubleGradient = true;
	private String userFont;
	private int userFontSize;
    
    public static String[] getPersistenceDelegateArgs(){
//    	return new String[]{"samplesOrder", "centroidName", "elementWidth", "hasCentroid", "insets"};
    	return new String[]{"experiment", "samplesOrder"};
    }
    public String getCentroidName() {return centroidName;}
    public int[][] getSamplesOrder() {return samplesOrder;}
    public int getElementWidth() {return elementWidth;}
    public boolean getHasCentroid() {return hasCentroid;}
    public Insets getInsets() {return insets;}
    public void setExperiment(Experiment e) {
    	this.experiment = e;
    }
    public ExperimentClusterHeader(int[][] samplesOrder, String centroidName, int elementWidth, boolean hasCentroid, Insets insets){
    	this(null, samplesOrder, centroidName);
    	this.elementWidth = elementWidth;
    	this.hasCentroid = hasCentroid;
    	this.insets = insets;
        Listener listener = new Listener();
	    addMouseListener(listener);
	    addMouseMotionListener(listener);
    }
    /**
     * Construct an <code>ExperimentClusterHeader</code> with specified experiment
     * and samples order.
     */
    public ExperimentClusterHeader(Experiment experiment, int[] samplesOrder) {
        this.experiment = experiment;        
        this.samplesOrder = new int[1][];
        this.samplesOrder[0] = samplesOrder;
        this.hasCentroid = false;
        setBackground(Color.white);
        Listener listener = new Listener();
	    addMouseListener(listener);
	    addMouseMotionListener(listener);
    }
    
    /**
     * Construct an <code>ExperimentClusterHeader</code> with specified experiment
     * and samples order.
     */
    public ExperimentClusterHeader(Experiment experiment, int[][] samplesOrder) {
        this.experiment = experiment;
        this.samplesOrder = samplesOrder;
        this.hasCentroid = false;
        setBackground(Color.white);
        Listener listener = new Listener();
	    addMouseListener(listener);
	    addMouseMotionListener(listener);
    }
    public ExperimentClusterHeader(Experiment experiment, int[][] samplesOrder, ArrayList storedGeneColors) {
        this.experiment = experiment;
        this.samplesOrder = samplesOrder;
        this.hasCentroid = false;
        this.storedGeneColors = storedGeneColors;
        setBackground(Color.white);
        Listener listener = new Listener();
	    addMouseListener(listener);
	    addMouseMotionListener(listener);
    }
    
    
    /**
     * Construct an <code>ExperimentClusterHeader</code> with specified experiment
     * and samples order.
     */
    public ExperimentClusterHeader(Experiment experiment, int[][] samplesOrder, String centroidName) {
        this.experiment = experiment;
        this.samplesOrder = samplesOrder;
        this.centroidName = centroidName;
        this.hasCentroid = true;
        setBackground(Color.white);
        Listener listener = new Listener();
	    addMouseListener(listener);
	    addMouseMotionListener(listener);
    }
    public ExperimentClusterHeader(Experiment experiment, int[][] samplesOrder, String centroidName, ArrayList<Color> strdGeneColors) {
        this.experiment = experiment;
        this.samplesOrder = samplesOrder;
        this.centroidName = centroidName;
        this.hasCentroid = true;
        storedGeneColors = strdGeneColors;
        setBackground(Color.white);
        Listener listener = new Listener();
	    addMouseListener(listener);
	    addMouseMotionListener(listener);
    }
        /**
     * Returns a component to be inserted into scroll pane view port.
     */
    public JComponent getContentComponent(){
        return this;
    }
    
    /**
     * Sets stored Color Array List
     */
    public void setStoredColors(ArrayList<Color> storedColors){
    	storedGeneColors = storedColors;
    }
    
    /**
     * Sets data.
     */
    public void setData(IData data) {
        this.data = data;
    }
    
    public void setClusterIndex(int index){
        this.clusterIndex = index;
    }
    
    public void setUseDoubleGradient(boolean useDouble) {
        useDoubleGradient = useDouble;
    }
    
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
     * Sets values when a cluster is being dragged in the experiment viewer
     * @param setting
     * @param dragColumn
     * @param dragRow
     */
    public void setDrag(boolean setting, int dragColumn, int dragRow){
    	this.isDrag=setting;
    	this.dragColumn = dragColumn;
    	this.dragRow  = dragRow;
    }
    /**
     * clears the arraylist containing cluster color location info
     */
    public void clearStoredSampleColors(){
    	storedSampleColors.clear();
    
    }
    /**
     * Sets whether the color-coded clustering display is compact
     */
    
    public void setCompactClusters(boolean value){
    	if (value==isCompact)
    		return;
    	//savedGeneColorOrder is for keeping the same cluster color order when clusters are "un-compacted"
    	if (value){
	    	savedSampleColorOrder.clear();  
    		for (int i=0; i<storedSampleColors.size(); i++){
		    	savedSampleColorOrder.add((Color)storedSampleColors.get(i));
	    	}
    	storedSampleColors.clear();
    	}else{
	    	storedSampleColors.clear();
	    	clearColorOverlaps();
    		for (int i=0; i<savedSampleColorOrder.size(); i++){
		    	storedSampleColors.add((Color)savedSampleColorOrder.get(i));
    		}
    	}
	    this.isCompact = value;
    }
    
    /**
     * clears the array that holds the compacted location of each colorbar
     */
    
    private void clearColorOverlaps(){
    	for (int i=0; i<ColorOverlaps.length; i++){
    		ColorOverlaps[i] = i;
    	}
    }
    
    /**
     * draws rectangle around specified sample cluster colors
     * @param column
     * @param color
     */
    public void drawClusterHeaderRectsAt(int column, Color color){
    	Graphics2D g = (Graphics2D)getGraphics();
        if (g == null) {
            return;
        }
        g.setColor(color);
        int centroidOffset = 0;
        if (hasCentroid)
        	centroidOffset=this.elementWidth +5;
    	if (!isCompact)
    		g.drawRect(column*elementWidth + insets.left+centroidOffset, getSize().height - (COLOR_BAR_HEIGHT*(storedSampleColors.size())) - 2, (elementWidth-1), (COLOR_BAR_HEIGHT)*storedSampleColors.size());
    	if (isCompact)
    		g.drawRect(column*elementWidth + insets.left+centroidOffset, getSize().height - (COLOR_BAR_HEIGHT*(compactedColorBarHeight)) - 2, (elementWidth-1), (COLOR_BAR_HEIGHT)*compactedColorBarHeight);
    }
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
        int centroidOffset = 0;
        if (hasCentroid){
        	centroidOffset=this.elementWidth +5;
        }
        if (column > (this.samplesOrder[clusterIndex].length)-1)
        		side = 10;
        if (column > (experiment.getNumberOfSamples() -1)&&isCompact)
        	return;
    	if (!isCompact){
    		g.drawRect(column*elementWidth + insets.left + centroidOffset + inset, getSize().height - (COLOR_BAR_HEIGHT*(storedSampleColors.size()) + maxSampleLabelLength + 7), (elementWidth)  , (COLOR_BAR_HEIGHT)*storedSampleColors.size()+maxSampleLabelLength+4 + side);
    	}
    	if (isCompact)
    		g.drawRect(column*elementWidth + insets.left + centroidOffset + inset, getSize().height - (COLOR_BAR_HEIGHT*(compactedColorBarHeight) +   maxSampleLabelLength + 7), (elementWidth-1), (COLOR_BAR_HEIGHT)*compactedColorBarHeight + maxSampleLabelLength+4 + side);
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
     * Sets an element width.
     */
    private void setElementWidth(int width) {
        this.elementWidth = width;
        if (userFontSize==0){
	        if (width > 16) {
	            width = 16;
	        }
        }else if(userFontSize==-1){
        	//fit, so width = width
        } else {
        	width = userFontSize;
        }
        setFont(new Font(userFont, Font.PLAIN, width));
    }
    
    /**
     * Sets the left margin for the header
     */
    public void setLeftInset(int leftMargin){
        insets.left = leftMargin;
    }
    public void fixHeaderWidth(int minSize){
    	if (getSize().width<minSize)
    		setSize(minSize, getSize().height);
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
        FontMetrics hfm = g.getFontMetrics();
        int maxHeight = 0;
        String name;
        contentWidth = (experiment.getNumberOfSamples()+storedGeneColors.size())*elementWidth + insets.left + 4;
        final int size = this.samplesOrder[clusterIndex].length;
        
        for (int feature = 0; feature < size; feature++) {
            name = data.getSampleName(experiment.getSampleIndex(samplesOrder[clusterIndex][feature]));
            maxHeight = Math.max(maxHeight, hfm.stringWidth(name));
        }
        maxSampleLabelLength = maxHeight;
        if(hasCentroid)
            maxHeight = Math.max(maxHeight, hfm.stringWidth(this.centroidName));
        
        maxHeight += RECT_HEIGHT + hfm.getHeight() + 10;
        if (!isCompact){
        	maxHeight += (getColorBarHeight()*storedSampleColors.size());
            String sampleLabel;
            int labelLength=0;
            for (int feature = 0; feature < storedSampleColors.size(); feature++) {
                sampleLabel = data.getClusterLabel(feature, false);
                if (sampleLabel != null)
                labelLength = Math.max(labelLength, hfm.stringWidth(sampleLabel));
            }
            if (labelLength<60)
            	labelLength = 60;
            contentWidth = contentWidth + labelLength+10;
            	oldLabelLength = labelLength;
            }
        if(isCompact){
    		int maxSpacesOver=-1;
    		for (int i=0; i<storedSampleColors.size(); i++){
    			if ((ColorOverlaps[i])>maxSpacesOver)
    				maxSpacesOver=ColorOverlaps[i];
    		}
    		maxHeight += getColorBarHeight()*(maxSpacesOver+1);
    		compactedColorBarHeight= maxSpacesOver+1;

            setSize(getWidth(), maxHeight);
            setPreferredSize(new Dimension(getWidth(), maxHeight));
    		return;
    	}
        if(!hasCentroid){
            setSize(contentWidth, maxHeight);
            setPreferredSize(new Dimension(contentWidth, maxHeight));
       }
        else{
            setSize(contentWidth, maxHeight);
            setPreferredSize(new Dimension(contentWidth, maxHeight));
        }
        drawHeader(g);
    }
    
    /**
     * Returns height of color bar for experiment clustering
     */
    private int getColorBarHeight(){
        for( int sample = 0; sample < samplesOrder[clusterIndex].length ; sample++){
            if(data.getExperimentColor(experiment.getSampleIndex(this.samplesOrder[clusterIndex][sample])) != null)
                return COLOR_BAR_HEIGHT;
        }
        return 0;
    }
    
    public void setClusters(int[][] mat){
    	samplesOrder = new int[mat.length][mat[0].length];
    	for (int i=0; i<mat.length; i++){
    		for (int j=0; j<mat[i].length; j++){
    			this.samplesOrder[i][j]=mat[i][j];
    		}
    	}
        this.repaint();
        this.updateUI();  
    }
    
    /**
     * Paints the header into specified graphics.
     */
    public void paint(Graphics g1D) {
        super.paint(g1D);
        if (data == null) {
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
    	
        final int samples = this.samplesOrder[clusterIndex].length;
        if (samples == 0) {
            return;
        }
        
        int width = samples*elementWidth;
        if(this.hasCentroid)
            width += this.elementWidth + 5;
        if(useDoubleGradient) {
        	g.drawImage(this.negColorImage, insets.left, 0, (int)(width/2f), RECT_HEIGHT, null);
        	g.drawImage(this.posColorImage, (int)((width/2f)+insets.left), 0, (int)(width/2f), RECT_HEIGHT, null);
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
        	g.drawString(String.valueOf(midValue), (int)((width+insets.left)/2f)-textWidth/2, RECT_HEIGHT+fHeight);
        textWidth = hfm.stringWidth(String.valueOf(this.maxValue));
        g.drawString(String.valueOf(this.maxValue), (width-textWidth)+insets.left, RECT_HEIGHT+fHeight);

        // draw feature names
        g.rotate(-Math.PI/2);
        String name;
        int h = -getSize().height + 5;
        boolean hasColorBar = false;
        
        if(this.getColorBarHeight() > 0){
        	if (isCompact)
        		h += COLOR_BAR_HEIGHT*compactedColorBarHeight;
        	if (!isCompact)
        		h += COLOR_BAR_HEIGHT*storedSampleColors.size();
            hasColorBar = true;
        }
        int centroidNameOffset = 0;
        if(this.hasCentroid){
            centroidNameOffset = elementWidth + 5;
            g.drawString(this.centroidName, h, descent + elementWidth/2 + insets.left);
        }
        
        for (int sample = 0; sample < samples; sample++) {
            name = data.getSampleName(experiment.getSampleIndex(this.samplesOrder[clusterIndex][sample]));
            g.drawString(name, h, descent + elementWidth*sample + elementWidth/2 + insets.left + centroidNameOffset);
        }
        g.rotate(Math.PI/2);
        
        int visibleClusters = 0;
        if (!isCompact){
	        String clusterName;
	        g.rotate(-Math.PI/2);
	        int numberOfClusters = storedGeneColors.size();
	        for (int cluster = 0; cluster < numberOfClusters; cluster++) {
	        	//if (data.getClusterLabel(cluster, true)==null) break;
	        	int index = data.getVisibleCluster((Color)storedGeneColors.get(cluster), true);
	        	clusterName = data.getClusterLabel(index, true);
	        	if (clusterName!=null)
	        		g.drawString(clusterName, h, descent + elementWidth*(samples+cluster) + elementWidth/2 + insets.left+5 + centroidNameOffset);
	        	visibleClusters++;
	        }
	        g.rotate(Math.PI/2);
        }

        if (storedSampleColors!=null&&!isCompact){
	        String clusterName;
	        int numberOfClusters = storedSampleColors.size();
	        for (int cluster = 0; cluster < numberOfClusters; cluster++) {
	        	//if (data.getClusterLabel(cluster, false)==null) break;
	        	int index = data.getVisibleCluster((Color)storedSampleColors.get(cluster), false);
	        	clusterName = data.getClusterLabel(index, false);
	        	if (clusterName!=null)
	        		g.drawString(clusterName, elementWidth*(samples+visibleClusters) + insets.left+10 + centroidNameOffset, -h + COLOR_BAR_HEIGHT*(numberOfClusters-(cluster)));
	        }
        }
        
        
        
        int spacesOver=0;
        if(hasColorBar){
            int sscLength= storedSampleColors.size();
            for(int sample = 0; sample < samples; sample++){
                Color[] colors = data.getSampleColorArray(experiment.getSampleIndex(this.samplesOrder[clusterIndex][sample]));
                if (colors==null) { continue;}
                for (int clusters=0; clusters<colors.length; clusters++){
	            	if (colors[clusters]==null) {continue;} 
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
	    			                	if (data.isColorOverlap(experiment.getSampleIndex(this.samplesOrder[clusterIndex][sample]), colors[clusters], (Color)storedSampleColors.get(j), false)){
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
                    g.fillRect(sample*elementWidth + insets.left + centroidNameOffset, getSize().height - (COLOR_BAR_HEIGHT*(1+spacesOver)) - 2, elementWidth, COLOR_BAR_HEIGHT);
                }
            }
            int sizeTest = getSize().width;
            if (sscLength!= storedSampleColors.size()){
            	updateSizes(getSize().width, elementWidth);
            }
            if (sizeTest != getSize().width) repaint();
        }
        if (mouseOnMap&&isShowRects){
        	if (mouseColumn != clickedColumn)
        		drawClusterHeaderRectsAt(mouseColumn, Color.gray, false);
        	if (mouseRow != clickedRow)	
        		drawHorizontalRect(mouseRow, Color.gray);
        }
        mouseOnMap = false;
        if (clickedCell){
            g.setColor(Color.red);
            int centroidOffset = 0;
            if (hasCentroid)
            	centroidOffset = elementWidth + 5;
            if (!isCompact){
            	g.drawRect(centroidOffset + insets.left, this.getHeight()-COLOR_BAR_HEIGHT*(storedSampleColors.size() - clickedRow)-3, elementWidth*experiment.getNumberOfSamples() + 5 + elementWidth*storedGeneColors.size() + 5 + oldLabelLength + 2, COLOR_BAR_HEIGHT);
            	g.drawRect(centroidOffset + clickedColumn*elementWidth + insets.left, getSize().height - (COLOR_BAR_HEIGHT*(storedSampleColors.size()) + maxSampleLabelLength + 7) , (elementWidth), (COLOR_BAR_HEIGHT)*storedSampleColors.size()+maxSampleLabelLength+4);
        	}
        }    
        if (clusterViewerClicked&&!isCompact){
        	g.setColor(Color.red);
        	g.drawRect(clusterViewerClickedColumn*elementWidth + insets.left + 4, getSize().height - (COLOR_BAR_HEIGHT*(storedSampleColors.size()) + maxSampleLabelLength + 7) , (elementWidth), (COLOR_BAR_HEIGHT)*storedSampleColors.size()+maxSampleLabelLength+4 + 10);
        	//g.drawRect(column*elementWidth + insets.left + centroidOffset + inset, getSize().height - (COLOR_BAR_HEIGHT*(compactedColorBarHeight) +   maxSampleLabelLength + 7), (elementWidth-1), (COLOR_BAR_HEIGHT)*compactedColorBarHeight + maxSampleLabelLength+4 + side);
        	
        }

        if (isDrag){
        	g.setColor(Color.blue);
        	if (!isCompact)
        		g.drawRect(dragColumn*elementWidth + insets.left + 4, getSize().height - (COLOR_BAR_HEIGHT*(storedSampleColors.size()) + maxSampleLabelLength + 7) , (elementWidth), (COLOR_BAR_HEIGHT)*storedSampleColors.size()+maxSampleLabelLength+4 + 10);
        	if (isCompact)
        		g.drawRect(dragColumn*elementWidth + insets.left + 4, getSize().height - (COLOR_BAR_HEIGHT*(compactedColorBarHeight) + maxSampleLabelLength + 7), (elementWidth-1), (COLOR_BAR_HEIGHT)*compactedColorBarHeight+maxSampleLabelLength+4 + 10);
        	
        }
        if (headerDrag){
        	g.setColor(Color.blue);
        	if (!isCompact)
        		g.drawRect(insets.left, this.getHeight()-COLOR_BAR_HEIGHT*(storedSampleColors.size() - headerDragRow)-3, elementWidth*this.samplesOrder[clusterIndex].length + 5 + elementWidth*storedGeneColors.size() + 5 + oldLabelLength + 2, COLOR_BAR_HEIGHT);
        	if (isCompact)
        		g.drawRect(insets.left, this.getHeight()-COLOR_BAR_HEIGHT*(this.compactedColorBarHeight - headerDragRow)-3, elementWidth*this.samplesOrder[clusterIndex].length, COLOR_BAR_HEIGHT);
        	
        }
    }  
    
    private void drawHorizontalRect(int row, Color color){
    	Graphics2D g = (Graphics2D)getGraphics();
        if (g == null) {
            return;
        }
        int centroidOffset = 0;
        if (hasCentroid)
        	centroidOffset = elementWidth + 5;
                    g.setColor(color);
        if (!isCompact)
        	g.drawRect(centroidOffset + insets.left, this.getHeight()-COLOR_BAR_HEIGHT*(storedSampleColors.size() - row)-3, elementWidth*this.samplesOrder[clusterIndex].length + 5 + elementWidth*storedGeneColors.size() + 5 + oldLabelLength + 2, COLOR_BAR_HEIGHT);
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
        	if (!isShowRects)
        		return;
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
        
        public void mouseDragged(MouseEvent event) {
        	repaint();
            if (SwingUtilities.isRightMouseButton(event)) {
                return;
            }
            int column = findColumn(event.getX());
            int row = findRow(event.getY());
            if (!isLegalPosition(row, column)) {
            	headerDrag = false;
                return;
            }
            if (!headerDrag)
            	return;
            headerDragColumn = column;
            headerDragRow = row;
        	if (column<experiment.getNumberOfSamples()){
        		//Graphics g = getGraphics();
        		//g.drawRect((experiment.getNumberOfSamples())*elementSize.width + insets.left +5-1, row*elementSize.height-1, (elementSize.width)*(colorWidth)+annotationWidth +8, elementSize.height+1);
        		//if (isCompact) return;
        		//g.setColor(Color.blue);
        		//g.drawRect(column*elementSize.width + insets.left +5-1, -1, (elementSize.width), elementSize.height*getCluster().length+1);
            	//header.drawClusterHeaderRectsAt(column, Color.blue, true);
        	} else{
        		headerDrag = false;
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
            headerDrag = true;
            headerDragColumn = startColumn;
            headerDragRow = startRow;
        }

        /** Called when the mouse has been released. */
        public void mouseReleased(MouseEvent event) {
	        if (!headerDrag)
	        	return;
        	headerDrag = false;
	        int endRow = findRow(event.getY());
	        if (!isLegalPosition(startRow, startColumn)) {
	            return;
	        }
	      	if (!isCompact){
		      	Color inter = (Color)storedSampleColors.get(storedSampleColors.size()-1-startRow);
		      	storedSampleColors.remove(storedSampleColors.size()-1-startRow);
		      	storedSampleColors.add(storedSampleColors.size()-endRow, inter);
		      	repaint();
	      	}
	      	else{
	      		for (int j=0; j<storedSampleColors.size(); j++){
	      			if (ColorOverlaps[j]==compactedColorBarHeight-1-startRow)
	      				ColorOverlaps[j]=-1;
	      			if (ColorOverlaps[j]==compactedColorBarHeight-1-endRow)
	      				ColorOverlaps[j]=compactedColorBarHeight-1-startRow;
	      			if (ColorOverlaps[j]== -1)
	      				ColorOverlaps[j]=compactedColorBarHeight-1-endRow;
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
    /**
     * Finds column for specified x coordinate.
     * @return -1 if column was not found.
     */
    private int findColumn(int targetx) {
    	
        int xSize = this.samplesOrder[clusterIndex].length*elementWidth;     
        if(this.hasCentroid){
            xSize += elementWidth + 5;
            if (targetx >= (xSize + insets.left) || targetx < insets.left + elementWidth + 5) {
                return -1;
            }
            return  (targetx - insets.left - elementWidth  - 5)/elementWidth;
        }
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
	public void setUserFont(String userFont) {
		this.userFont = userFont;
	}
	public void setUserFontSize(int userFontSize) {
		this.userFontSize = userFontSize;
	}
    
    
    
    
    
    
    
}
      /*
    private void writeObject(ObjectOutputStream oos) throws IOException {       
        oos.writeObject(experiment);            
        oos.writeObject(samplesOrder);
        oos.writeInt(elementWidth);
        oos.writeObject(insets);
        oos.writeBoolean(this.hasCentroid);
        if(this.hasCentroid){
            oos.writeObject(this.centroidName);
        }            
    }
    
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {     
        experiment = (Experiment)ois.readObject();
        samplesOrder = (int[][])ois.readObject();
        elementWidth = ois.readInt();
        insets = (Insets)ois.readObject();
        if(ois.readBoolean()){
            this.hasCentroid = true;
            this.centroidName = (String)ois.readObject();
        }
    }
    */


