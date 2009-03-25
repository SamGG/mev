/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
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
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;

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
    private int[] samplesOrder;
    private int[][] clusters;
    private static ArrayList<Color> storedGeneColors;
    private static ArrayList<Color> storedSampleColors = new ArrayList<Color>();
    private static ArrayList<Color> savedSampleColorOrder = new ArrayList<Color>();
    private int compactedColorBarHeight = 0;
    private int clusterIndex;
    private int elementWidth;
    private boolean isAntiAliasing = true;
    private boolean isCompact = false;
    public boolean isShowRects = true;
    private boolean enableMoveable = true;
    private float maxValue = 3f;
    private float minValue = -3f;
    private float midValue = 0.0f;
    private Insets insets = new Insets(0, 10, 0, 0);
    private BufferedImage negColorImage;
    private BufferedImage posColorImage;
    private int activeCluster = 0;
    private int maxSampleLabelLength = 0;
    private static int[] ColorOverlaps = new int[1000000];
    private boolean mouseOnMap=false;
	private int mouseRow = 0;
	private int mouseColumn = 0;
	private int clickedRow = 0;
    private int clickedColumn = 0;
    public boolean clickedCell = false;
    private boolean isDrag = false;
    private boolean headerDrag = false;
    private boolean isSampleDrag = false;
    private boolean isShift = false;
    private boolean isShiftMove = false;

    private int sampleDragColumn = 0;
	private int headerDragRow = 0;
    private int dragColumn = 0;
	private int dragRow = 0;

    private int startColumn = 0;
    private int startShift = 0;
    private int endShift = 0;
    private int startRow = 0;
    private int labelLength = 0;
    private int startShiftMove = 0;
    private int endShiftMove = 0;

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
        this.setSamplesOrder(samplesOrder == null ? createSamplesOrder(experiment) : samplesOrder);
        setBackground(Color.white);
	    Listener listener = new Listener();
	    addMouseListener(listener);
	    addMouseMotionListener(listener);
    }
    public ExperimentHeader(Experiment experiment, int [][] clusters, int[] samplesOrder, ArrayList<Color> storedGeneColors) {
        this.experiment = experiment;
        this.clusters = clusters;
        this.storedGeneColors = storedGeneColors;
        this.setSamplesOrder(samplesOrder == null ? createSamplesOrder(experiment) : samplesOrder);
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
     * clears the arraylist containing cluster color location info
     */
    public void clearStoredSampleColors(){
    	storedSampleColors.clear();
    	
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
     * Sets positive and negative images
     */
    public void setNegAndPosColorImages(BufferedImage neg, BufferedImage pos){
        this.negColorImage = neg;
        this.posColorImage = pos;
    }
    /**
     * Sets stored Color Array List
     */
    public void setStoredColors(ArrayList<Color> storedColors){
    	storedGeneColors = storedColors;

    }
    
    public void setClusters(int[][] mat){
    	clusters = new int[mat.length][mat[0].length];
    	for (int i=0; i<mat.length; i++){
    		for (int j=0; j<mat[i].length; j++){
    			this.clusters[i][j]=mat[i][j];
    		}
    	}
        this.repaint();
        this.updateUI();  
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
        for( int sample = 0; sample < getSamplesOrder().length ; sample++){
            if(data.getExperimentColor(experiment.getSampleIndex(this.getSamplesOrder()[sample])) != null)
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
     *//*
    public void updateSizesx(int contentWidth, int elementWidth) {
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
        int rewriteWidth=0;
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
    }    */
    /**
     * DS re-write
     */
    
    public void updateSizes(int useless, int setElementWidth) {
        if(data == null)
            return;
        setElementWidth(setElementWidth);
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
        int contentWidth=0;
        final int size = this.experiment.getNumberOfSamples();
        for (int feature = 0; feature < size; feature++) {
            name = data.getSampleName(experiment.getSampleIndex(feature));
            maxHeight = Math.max(maxHeight, hfm.stringWidth(name));
        }
        contentWidth = (experiment.getNumberOfSamples()+storedGeneColors.size())*elementWidth + insets.left + 4;
        maxSampleLabelLength = maxHeight;
        maxHeight += RECT_HEIGHT + hfm.getHeight() + 10;
        if (!isCompact){
        	maxHeight += (getColorBarHeight()*storedSampleColors.size());
            String sampleLabel;
            labelLength=0;
            for (int feature = 0; feature < storedSampleColors.size(); feature++) {
                sampleLabel = data.getClusterLabel(feature, false);
                if (sampleLabel != null)
                	labelLength = Math.max(labelLength, hfm.stringWidth(sampleLabel));
            }
            contentWidth = contentWidth + labelLength+10;
            if (labelLength<60)
            	contentWidth = contentWidth + 70;
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
            drawHeader(g);
    		return;
    	}
        
        setSize(contentWidth, maxHeight);
        setPreferredSize(new Dimension(contentWidth, maxHeight));
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
            name = data.getSampleName(experiment.getSampleIndex(this.getSamplesOrder()[sample]));
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
	        	//System.out.println("data.getClusterLabel(cluster, true))="+data.getClusterLabel(cluster, true));
	        	
	        	//if (data.getClusterLabel(cluster, true)==null) break;
	        	int index = data.getVisibleCluster((Color)storedGeneColors.get(cluster), true);
	        	if (index==0)
	        		break;
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
	        	//if (data.getClusterLabel(cluster, false)==null) break;
	        	int index = data.getVisibleCluster((Color)storedSampleColors.get(cluster), false);
	        	if (index==-1) continue;
	        	clusterName = data.getClusterLabel(index, false);
		        g.drawString(clusterName, elementWidth*(samples+visibleClusters) + insets.left+10, -h + COLOR_BAR_HEIGHT*(numberOfClusters-(cluster)));
	        }
        }
        
        int spacesOver=0;
        if(hasColorBar){
            int sscLength= storedSampleColors.size();
            for(int sample = 0; sample < samples; sample++){
                Color[] colors = data.getSampleColorArray(experiment.getSampleIndex(this.getSamplesOrder()[sample]));
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
	    			                	if (data.isColorOverlap(experiment.getSampleIndex(this.getSamplesOrder()[sample]), colors[clusters], (Color)storedSampleColors.get(j), false)){
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
        if (mouseOnMap&&isShowRects){
        	if (mouseColumn != clickedColumn)
        		drawClusterHeaderRectsAt(mouseColumn, Color.gray, false);
        	if (mouseRow != clickedRow)	
        		drawHorizontalRect(mouseRow, Color.gray);
        }
        mouseOnMap = false;
        if (clickedCell){
            g.setColor(Color.red);
            if (!isCompact){
            	g.drawRect(insets.left, this.getHeight()-COLOR_BAR_HEIGHT*(storedSampleColors.size() - clickedRow)-3, elementWidth*experiment.getNumberOfSamples() + 5 + elementWidth*storedGeneColors.size() + 5 + labelLength + 2, COLOR_BAR_HEIGHT);
            	g.drawRect(clickedColumn*elementWidth + insets.left, getSize().height - (COLOR_BAR_HEIGHT*(storedSampleColors.size()) + maxSampleLabelLength + 7) , (elementWidth), (COLOR_BAR_HEIGHT)*storedSampleColors.size()+maxSampleLabelLength+4);
        	 }
        }    
        if (clusterViewerClicked&&!isCompact){
        	g.setColor(Color.red);
        	g.drawRect(clusterViewerClickedColumn*elementWidth + insets.left + 4, getSize().height - (COLOR_BAR_HEIGHT*(storedSampleColors.size()) + maxSampleLabelLength + 7) , (elementWidth), (COLOR_BAR_HEIGHT)*storedSampleColors.size()+maxSampleLabelLength+4 + 10);
        }
        if (isDrag){
        	g.setColor(Color.blue);
        	if (!isCompact)
        		g.drawRect(dragColumn*elementWidth + insets.left + 4, getSize().height - (COLOR_BAR_HEIGHT*(storedSampleColors.size()) + maxSampleLabelLength + 7) , (elementWidth), (COLOR_BAR_HEIGHT)*storedSampleColors.size()+maxSampleLabelLength+4 + 10);
        	if (isCompact)
        		g.drawRect(dragColumn*elementWidth + insets.left + 4, getSize().height - (COLOR_BAR_HEIGHT*(compactedColorBarHeight) + maxSampleLabelLength + 7), (elementWidth), (COLOR_BAR_HEIGHT)*compactedColorBarHeight+maxSampleLabelLength+4 + 10);
        	
        }
        if (headerDrag){
        	g.setColor(Color.blue);
        	if (!isCompact)
        		g.drawRect(insets.left, this.getHeight()-COLOR_BAR_HEIGHT*(storedSampleColors.size() - headerDragRow)-3, elementWidth*experiment.getNumberOfSamples() + 5 + elementWidth*storedGeneColors.size() + 5 + labelLength + 2, COLOR_BAR_HEIGHT);
        	if (isCompact)
        		g.drawRect(insets.left, this.getHeight()-COLOR_BAR_HEIGHT*(this.compactedColorBarHeight - headerDragRow)-3, elementWidth*experiment.getNumberOfSamples(), COLOR_BAR_HEIGHT);
        	
        }
        if (isSampleDrag){
        	g.setColor(Color.blue);
        	g.drawRect(this.sampleDragColumn*elementWidth + insets.left, getSize().height - (COLOR_BAR_HEIGHT*(storedSampleColors.size()) + maxSampleLabelLength + 7) , (elementWidth), (COLOR_BAR_HEIGHT)*storedSampleColors.size()+maxSampleLabelLength+4 + 10);
                 	
        }
        if (isShift){
        	g.setColor(new Color(175,175,175,100));
        	if (startShift<endShift)
        		g.fillRect(startShift*elementWidth + insets.left, getSize().height - (COLOR_BAR_HEIGHT*(storedSampleColors.size()) + maxSampleLabelLength + 7) , (elementWidth*(endShift-startShift+1)), (COLOR_BAR_HEIGHT)*storedSampleColors.size()+maxSampleLabelLength+4 + 10);
        	if (startShift>endShift)
        		g.fillRect(endShift*elementWidth + insets.left, getSize().height - (COLOR_BAR_HEIGHT*(storedSampleColors.size()) + maxSampleLabelLength + 7) , (elementWidth*(startShift-endShift+1)), (COLOR_BAR_HEIGHT)*storedSampleColors.size()+maxSampleLabelLength+4 + 10);
        	if (startShift==endShift)
        		g.fillRect(startShift*elementWidth + insets.left, getSize().height - (COLOR_BAR_HEIGHT*(storedSampleColors.size()) + maxSampleLabelLength + 7) , (elementWidth), (COLOR_BAR_HEIGHT)*storedSampleColors.size()+maxSampleLabelLength+4 + 10);
        }
        if (isShiftMove){
        	g.setColor(Color.blue);
        	int move = startShiftMove-endShiftMove;
        	g.drawRect((Math.min(startShift, endShift)-move)*elementWidth + insets.left, getSize().height - (COLOR_BAR_HEIGHT*(storedSampleColors.size()) + maxSampleLabelLength + 7) , (elementWidth*(Math.abs(endShift-startShift)+1)), (COLOR_BAR_HEIGHT)*storedSampleColors.size()+maxSampleLabelLength+4 + 10);
        }
    }  
    
    private void drawHorizontalRect(int row, Color color){
    	Graphics2D g = (Graphics2D)getGraphics();
        if (g == null) {
            return;
        }
                    g.setColor(color);
        if (!isCompact){
        	g.drawRect(insets.left, this.getHeight()-COLOR_BAR_HEIGHT*(storedSampleColors.size() - row)-3, elementWidth*experiment.getNumberOfSamples() + 5 + elementWidth*storedGeneColors.size() + 5 + labelLength + 2, COLOR_BAR_HEIGHT);
        }
    }
    
    private class Listener extends MouseAdapter implements MouseMotionListener {
        
        private int oldRow = -1;
        private int oldColumn = -1;
        
        public void mouseClicked(MouseEvent event) {
            int column = findColumn(event.getX());
            if (SwingUtilities.isRightMouseButton(event)||column==-1) {
            	isShiftMove=false;
            	isSampleDrag = false;
                return;
            }
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
            }
            setOldPosition(row, column);
            if (g != null) {
                g.dispose();
            }
        }
        public void mouseDragged(MouseEvent event) {
        	repaint();
            if (SwingUtilities.isRightMouseButton(event)) {
            	isShiftMove=false;
            	isSampleDrag = false;
                return;
            }
            if(event.isShiftDown()){
            	isSampleDrag = false;
            	return;
            }
            int column = findColumn(event.getX());
            int row = findRow(event.getY());
            if (column==-1){
            	isShiftMove=false;
            	isSampleDrag = false;
            	return;
            }
            if (isShift&&enableMoveable){
        		isShiftMove=true;
            	endShiftMove = column;
            	if (startShiftMove-endShiftMove>Math.min(startShift, endShift))
            		endShiftMove = startShiftMove-Math.min(startShift, endShift);
            	if (endShiftMove-startShiftMove>experiment.getNumberOfSamples()-Math.max(startShift, endShift)-1)
            		endShiftMove = startShiftMove+experiment.getNumberOfSamples()-Math.max(startShift, endShift)-1;
            	return;
            }
            sampleDragColumn = column;
        	isSampleDrag = true;

        	if (!enableMoveable){
            	isShiftMove=false;
            	isSampleDrag = false;
        	}
            if (!isLegalPosition(row, column)) {
            	headerDrag = false;
                return;
            }
            if (!headerDrag)
            	return;
            headerDragRow = row;
        	if (column<experiment.getNumberOfSamples()){
        		
        	} else{
        		headerDrag = false;
        		isSampleDrag = false;
        	}
        }
        /** Called when the mouse has been pressed. */
        public void mousePressed(MouseEvent event) {
            startColumn = findColumn(event.getX());
            if (SwingUtilities.isRightMouseButton(event)||startColumn==-1) {
            	isShiftMove=false;
            	isSampleDrag = false;
                return;
            }

            sampleDragColumn = startColumn;
            startRow = findRow(event.getY());
            if(event.isShiftDown()){
            	if (!isShift)
            		startShift = startColumn;
            	endShift = startColumn;
            	isShift=true;
            }else{
            	if (isShift&&(startColumn>=Math.min(startShift,endShift)&&startColumn<=Math.max(startShift,endShift))){
            		startShiftMove = startColumn;
            	}else{
            		isShift = false;
            		isShiftMove = false;
            	}
            }
            if (!isLegalPosition(startRow, startColumn)) {
                return;
            }
            headerDrag = true;
            headerDragRow = startRow;
        }
        
        /** Called when the mouse has been released. */
        public void mouseReleased(MouseEvent event) {
        	 
	        int endColumn = findColumn(event.getX());
	        
	        if (SwingUtilities.isRightMouseButton(event)||endColumn==-1) {
             	isShiftMove=false;
             	isSampleDrag = false;
                 return;
            }
	        if (isShiftMove){
	        	int lowerShift=Math.min(endShift,startShift);
	        	int upperShift=Math.max(endShift,startShift);;
	        	int numMovedSamples=upperShift-lowerShift+1;
	        	int numSpacesMoved=endShiftMove-startShiftMove;
	        	int[] samplesMoved = new int[numMovedSamples];
	        	for (int i=0; i<samplesMoved.length; i++){
	        		samplesMoved[i]=getSamplesOrder()[lowerShift+i];
	        	}
	        	ArrayList<Integer> tempSamplesOrder = new ArrayList<Integer>();
	        	for (int i=0; i<getSamplesOrder().length; i++){
	        		tempSamplesOrder.add(getSamplesOrder()[i]);
	        	}
	        	for (int i=0; i<numMovedSamples; i++){
	        		tempSamplesOrder.remove(lowerShift);
	        	}
	        	for (int i=0; i<numMovedSamples; i++){
	        		tempSamplesOrder.add(lowerShift+numSpacesMoved+i, samplesMoved[i]);
	        	}
	        	for (int i=0; i<getSamplesOrder().length; i++){
	        		getSamplesOrder()[i]=tempSamplesOrder.get(i);
	        	}
	        	isShiftMove = false;
	        	isShift = false;
	        	return;
	        }
	        if (isSampleDrag){
		        if (endColumn>startColumn){
		        	int startSample = getSamplesOrder()[startColumn];
		        	for (int i=0; i<endColumn-startColumn; i++){
		        		getSamplesOrder()[startColumn+i]=getSamplesOrder()[startColumn+i+1];
		        	}
		        	getSamplesOrder()[endColumn]=startSample;
		      		repaint();
		        }
		        if (endColumn<startColumn){
		        	int startSample = getSamplesOrder()[startColumn];
		        	for (int i=0; i<startColumn-endColumn; i++){
		        		getSamplesOrder()[startColumn-i]=getSamplesOrder()[startColumn-(i+1)];
		        	}
		        	getSamplesOrder()[endColumn]=startSample;
		      		repaint();
		        }
	        }
        	isSampleDrag = false;
	        
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


	public void setSamplesOrder(int[] samplesOrder) {
		this.samplesOrder = samplesOrder;
	}


	public void setEnableMoveable(boolean enableMoveable) {
		this.enableMoveable = enableMoveable;
	}
}
