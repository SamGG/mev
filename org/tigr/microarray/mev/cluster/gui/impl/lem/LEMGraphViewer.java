/*
 Copyright @ 1999-2006, The Institute for Genomic Research (TIGR).
 All rights reserved.
 */

package org.tigr.microarray.mev.cluster.gui.impl.lem;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JViewport;

import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;


/**
 * @author braisted
 * 
 * Basic JPanel extension for rendering a graph
 * 
 */
public class LEMGraphViewer extends JPanel implements IViewer {

	protected LEMGraphHeader header;
	
	protected Experiment experiment;
	protected IFramework framework;
	protected IDisplayMenu displayMenu;
	protected IData data;
	protected int clusterIndex;

	protected int yRangeOption;

	protected float[][] means;
	protected String [] locusNames;
	protected int [] start;
	protected int [] end;

	protected float minValue, maxValue, midValue = 0.0f;
	//protected float minDisplayValue, maxDisplayValue;
	protected float ticInterval;
	
	protected boolean drawReferenceBlock = true;
	protected int xref = 0;
	protected int yref = 0;
	protected int currLocusIndex;
	protected boolean showRefLine = false;
	
	protected boolean [] showSample;
	protected Color [] sampleColors;
	protected boolean showLocusInfo = false;
	protected boolean shadeLocusRanges;
	
	protected Vector sampleLineColors;
	protected Vector sampleMarkerColors;
	protected String title;
	protected int numberOfSamples;
	protected boolean overlay = false;
	protected FontMetrics fm;
	protected int MIN_GRAPH_WIDTH = 400;
	
	//y Range variables
	protected int yRangeMode;
	protected boolean useSymetricYRange;
	protected boolean showXAxis = true;
	protected BasicStroke xAxisStroke;
	protected Color xAxisColor = Color.lightGray;
	protected float xAxisCrossPoint = 0f;
	
	protected Vector yLabels;
	protected int xGraphInset = 40;
	protected boolean xGraphInsetInitialized = false;
	
	//related to offset mode
	protected boolean offsetLinesMode = true;
	protected Color posColor = Color.red;
	protected Color negColor = Color.green;
	protected float offsetGraphMidpoint = 0f;
	
	//related to digitized option
	protected boolean showOverlay = false;
	protected float upperCutoff;
	protected float lowerCutoff;
	protected float neutralPoint = 0f;
	
	protected boolean cursorOn = false;
	
	//to support zoom feature
	boolean inDragMode = false;	
	protected int startIndex;
	protected int endIndex;
	protected int dragStartX;
	protected int dragStopX;
	
	private int exptID = 0;
	
	public LEMGraphViewer() { }
	
	public LEMGraphViewer(Experiment experiment, float [][] data, String title, Hashtable properties, String [] locusNames, int [] start, int [] end) {
		if (experiment == null) {
			throw new IllegalArgumentException("experiment == null");
		}
		this.experiment = experiment;
		this.exptID = experiment.getId();
		numberOfSamples = this.experiment.getNumberOfSamples();
		this.means = data;
		this.showSample = new boolean[experiment.getNumberOfSamples()];
		this.title = title;
		this.locusNames = locusNames;
		this.start = start;
		this.end = end;
		
		this.header = new LEMGraphHeader();
		
		startIndex = 0;
		endIndex = data.length-1;
		
		setBackground(Color.white);
		setFont(new Font("monospaced", Font.BOLD, 10));
		//this.maxExperimentValue = experiment.getMaxAbsValue();

		this.yRangeMode = ((Integer)(properties.get("y-range-mode"))).intValue();
		this.useSymetricYRange = ((Boolean)(properties.get("y-axis-symetry"))).booleanValue();
		
		this.showXAxis = ((Boolean)(properties.get("show-x-axis"))).booleanValue();
		this.xAxisColor = (Color) (properties.get("x-axis-color"));
		this.xAxisStroke = (BasicStroke)(properties.get("x-axis-stroke"));
		
		this.overlay = ((Boolean)(properties.get("is-overlay-mode"))).booleanValue();        
		this.offsetLinesMode = ((Boolean)(properties.get("offset-lines-mode"))).booleanValue();
		this.offsetGraphMidpoint = ((Float)(properties.get("offset-graph-midpoint"))).floatValue();
		this.neutralPoint = offsetGraphMidpoint;		
		this.lowerCutoff = ((Float)(properties.get("offset-graph-min"))).floatValue();
		this.upperCutoff = ((Float)(properties.get("offset-graph-max"))).floatValue();
		this.showOverlay = ((Boolean)(properties.get("show-discrete-overlay"))).booleanValue();
		
		constructYAxisLabels();

		GraphListener listener = new GraphListener();
		this.addMouseMotionListener(listener);                  
		this.addMouseListener(listener);		
	}
		
//	public LEMGraphViewer(float[][] means, String title, String[] locusNames, int[] start, int[] end){
//		this.means = means;
//		this.title = title;
//		this.locusNames = locusNames;
//		this.start = start;
//		this.end = end;
//	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperiment(org.tigr.microarray.mev.cluster.gui.Experiment)
	 */
	public void setExperiment(Experiment e) {
		this.experiment = e;
		this.exptID = e.getId();
		numberOfSamples = this.experiment.getNumberOfSamples();
		this.showSample = new boolean[experiment.getNumberOfSamples()];
		this.header = new LEMGraphHeader();
		startIndex = 0;
		endIndex = means.length-1;

		GraphListener listener = new GraphListener();
		this.addMouseMotionListener(listener);                  
		this.addMouseListener(listener);		
	}
	/**
	 * Sets means values.
	 */
	public void setMeans(float[][] means) {
		this.means = means;
	}
	
	//public void toggleGradient() {
//		this.gradientToggle = !this.gradientToggle;
	//}
	
	
	/**
	 * Sets overlay mode
	 * @param overlayEnabled value to set overlay mode
	 */
	public void enableOverlay(boolean overlayEnabled) {
		overlay = overlayEnabled;
		if(!overlay) {
			setSize(MIN_GRAPH_WIDTH, numberOfSamples*190+40);
			setPreferredSize(new Dimension(getWidth(), numberOfSamples*190+40));
		} else {
			setSize(MIN_GRAPH_WIDTH, 400);
			setPreferredSize(new Dimension(getWidth(), 400));    		
		}    	
	}
	
		
	/**
	 * Updates data, mode and some the viewer attributes.
	 */
	public void onSelected(IFramework framework) {
		this.framework = framework;
		this.displayMenu = framework.getDisplayMenu();
		setData(framework.getData());
				
		//update scale if it comes from the display menu
		if(yRangeMode == 0) {  //display mode
			this.maxValue = framework.getDisplayMenu().getMaxRatioScale();
			this.minValue = framework.getDisplayMenu().getMinRatioScale();
		}

		refreshGraph();
	}
	
	/**
	 * Sets data.
	 */
	public void setData(IData data) {
		this.data = data;
	}
	
	/**
	 * Returns a current cluster.
	 */
	public int[] getCluster() {
		return null;
		//return this.clusters[this.clusterIndex];
	}
	
	/**
	 * Returns all clusters.
	 */
	public int[][] getClusters() {
		return null;
		//return clusters;
	}
	
	/**
	 * refreshes graph to current y limits
	 */
	public void refreshGraph() {
		this.constructYAxisLabels();
		repaint();
	}

	/**
	 * Sets the y range mode, custom or menu driven
	 * @param mode
	 */
	public void setYAxisRangeMode(int mode) {
		this.yRangeMode = mode;
		if(mode == 0) {  //display menu
			maxValue = displayMenu.getMaxRatioScale();
			minValue = displayMenu.getMinRatioScale();
			refreshGraph();
		}
	}
	
	private void updateYRangeAutoScale() {
		if(this.useSymetricYRange) {
			maxValue = experiment.getMaxAbsValue();
			minValue = maxValue*-1f;
		} else {
			float [] minAndMax = experiment.getMinAndMax(); 
			minValue = minAndMax[0];
			maxValue = minAndMax[1];
		}
	}
	
	/**
	 * sets y range limits
	 * @param min min limit
	 * @param max max limit
	 */
	public void setYRange(float min, float max) {
		minValue = min;
		maxValue = max;
	}
	
	/**
	 * Sets tick interval
	 * @param ticInterval tick interval
	 */
	public void setTicInterval(float ticInterval) {
		this.ticInterval = ticInterval;
	}
	
	/**
	 * True if selected to show x axis line.
	 * @param showXAxis
	 */
	public void setShowXAxis(boolean showXAxis) {
		this.showXAxis = showXAxis;
	}
	
	/**
	 * Sets x axis stroke options
	 * @param stroke basic stroke
	 */
	public void setXAxisStroke(BasicStroke stroke) {
		this.xAxisStroke = stroke;
	}
	
	/**
	 * sets  the y-value that the x axix crosses (often 0)
	 * @param xAxisCrossPoint
	 */
	public void setXAxisCrossPoint(float xAxisCrossPoint) {
		this.xAxisCrossPoint = xAxisCrossPoint;
	}
	
	/**
	 * sets the x axis color
	 * @param axisColor
	 */
	public void setXAxisColor(Color axisColor) {
		this.xAxisColor = axisColor;
	}
	
	/**
	 * sets y axis symitric for auto-scale mode
	 * 
	 * (Auto scale mode is not utilized as an option
	 * at this time)
	 * 
	 * @param isSymetric
	 */
	public void setYAxisSymetry(boolean isSymetric) {
		this.useSymetricYRange = isSymetric;
	}
		
	/**
	 * enables offset line mode for viewer mode option
	 * alternative is the connected points option
	 * @param enable
	 */
	public void enableOffsetLinesMode(boolean enable) {
		this.offsetLinesMode = enable;
	}
	
	/**
	 * Sets the midpoint or 'anchor value' for offset lines option
	 * @param val 'anchor' values
	 */
	public void setOffsetLinesMidpoint(float val) {
		this.offsetGraphMidpoint = val;
	}
	
	/**
	 * sets the limits for rendering offset line color
	 * @param val
	 */
	public void setOffsetLinesMin(float val) {
		this.lowerCutoff = val;
	}
	
	/**
	 * Sets the offset lines option's max offset to render line color
	 * @param val
	 */
	public void setOffsetLinesMax(float val) {
		this.upperCutoff = val;
	}

	/**
	 * Enables or disables discrete value overlay mode
	 * @param enable
	 */
	public void enableDiscreteValueOverlay(boolean enable) {
		this.showOverlay = enable;
	}
		
	/**
	 * Toggles the reference line option
	 */
		public void toggleReferenceLine() {
		showRefLine = !showRefLine;
		if(showRefLine)
			this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		else
			this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		repaint();
	}
	
	/**
	 * Sets the current graph to be displayed
	 * @param index
	 */
	public void setCurrentGraph(int index) {
		//dump graphs
		clearGraphs();
		//set current graph
		this.showSample[index] = true;
	}
	
	/**
	 * sets the graphs to display as a list of graph indices
	 * @param graphIndexList list o graph indices.
	 */
	public void setGraphsToDisplay(int [] graphIndexList) {
		clearGraphs();
		for(int i = 0; i < graphIndexList.length; i++) {
			showSample[graphIndexList[i]] = true;
		}
		repaint();
	}
	
	/**
	 * Resets the x range to show all loci points
	 * (Zoom out)
	 */
	public void resetXRange() {
		startIndex = 0;
		endIndex = means.length-1;
		header.resetLimits();
		repaint();
	}
	
	/**
	 * Returns a <code>Hashtable</code> of graph properties
	 * @return hash of current proerties
	 */
	public Hashtable getGraphProperties() {
		Hashtable props = new Hashtable();
		
		props.put("is-overlay-mode", new Boolean(overlay));
		props.put("y-range-mode", new Integer(this.yRangeMode));    	
		props.put("y-axis-min", new Float(this.minValue));
		props.put("y-axis-max", new Float(this.maxValue));
		props.put("y-axis-tic-interval", new Float(this.ticInterval));
		props.put("y-axis-symetry", new Boolean(this.useSymetricYRange));
		
		props.put("show-x-axis", new Boolean(this.showXAxis));    	
		props.put("x-axis-color", this.xAxisColor);
		props.put("x-axis-stroke", this.xAxisStroke);
		props.put("x-axis-cross-point", new Float(this.xAxisCrossPoint));
		
		props.put("offset-lines-mode", new Boolean(this.offsetLinesMode));
		props.put("offset-graph-midpoint", new Float(this.offsetGraphMidpoint));
		props.put("offset-graph-min", new Float(this.lowerCutoff));
		props.put("offset-graph-max", new Float(this.upperCutoff));
		props.put("show-discrete-overlay", new Boolean(this.showOverlay));		    	

		return props;
	}
	
	/**
	 * resets showSample field to all false
	 */
	public void clearGraphs() {
		for(int i = 0; i < showSample.length; i++)
			showSample[i] = false;
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
		return this;
	}
	
	/**
	 * Paints chart into specified graphics.  Splits on overlay vs. tile option
	 * 
	 */
	public void paint(Graphics g) {
		super.paint(g);
		
		g.setFont(new Font("Monospaced", Font.BOLD, 18));        		
		fm = g.getFontMetrics();
		
		//use FontMetrics to determine graph left inset
		if(!xGraphInsetInitialized)
			this.setXGraphInset();
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		if(overlay)
			overlayPaint(g);
		else
			multiGraphPaint(g);
	}
	
	/** Defines a view rectangle and clip rectangle for rendering graphs in
	 * overlay mode.
	 * 
	 * @param g <code>Graphics</code> object
	 */
	public void overlayPaint(Graphics g) {
		Rectangle rect = new Rectangle(xGraphInset, 40, getWidth()-xGraphInset-40, getHeight()-50);

		Rectangle clipRect = ((JViewport)(this.getParent())).getViewRect();
		g.setColor(Color.black);
		g.drawString(title, getWidth()/2 - fm.stringWidth(title)/2, rect.y - 15);
		paint((Graphics2D)g, rect, true, true, clipRect);    	
	}
	
	/** Defines a viewer rectangle and a clip rectangle for rendering graphs
	 * in a tile view.  Iterates over several Samples to tile the view.
	 * 
	 * @param g <code>Graphics</code> object
	 */
	public void multiGraphPaint(Graphics g) {
		int yOffset = 40;    
		int yGraphH = 150;
		
		Rectangle clipRect = ((JViewport)(this.getParent().getParent())).getViewRect();
		
		Component c = this.getParent();
		
		for(int plot = 0; plot < numberOfSamples; plot++) {
			setCurrentGraph(plot);    		
			
			Rectangle rect = new Rectangle(xGraphInset, (plot*(yGraphH+40))+40, getWidth()-xGraphInset-40, yGraphH); 
			//g.clearRect(40, (plot*(yGraphH+40))+1, getWidth()-80, yGraphH+40);
			g.setColor(Color.black);
			g.drawString(data.getSampleName(plot)+" -- "+title, xGraphInset + getWidth()/2 - fm.stringWidth(data.getSampleName(plot)+" -- "+title)/2, rect.y - 15);
			
			if(clipRect.intersects(rect)) {				
				//paint((Graphics2D)g, rect, true, clipRect.contains(rect), clipRect);    		    	
				paint((Graphics2D)g, rect, true, true, clipRect);    		    	    	    	
			}    		
		}
	}
	
	/**
	 * Sets the line colors
	 * @param lineColors line colors
	 */
	public void setSampleLineColors(Vector lineColors) {
		this.sampleLineColors = lineColors;
	}
	
	/**
	 * sets the marker colors
	 * @param markerColors marker colors
	 */
	public void setSampleMarkerColors(Vector markerColors) {
		this.sampleMarkerColors = markerColors;
	}
	
	/**
	 * Paints chart into specified graphics and with specified bounds.
	 */
	public void paint(Graphics2D g, Rectangle rect, boolean drawMarks, boolean applyClip, Rectangle originalClip) {
		
		final int left = rect.x;
		final int top = rect.y;
		final int width  = rect.width;
		final int height = rect.height;
		
		if (width < 5 || height < 5) {
			return;
		}
		
		final int numberOfSamples  = experiment.getNumberOfSamples();				
		final float factor = height/(maxValue - minValue);		
		final int zeroValue = top + (int)Math.round(factor*(maxValue-0f));        		
		final float stepX  = width/(float)((endIndex-startIndex)-1);		
		final int   stepsY = (int)maxValue+1;		
		float fValue, sValue = 0, yInterval, lineHeight;
		Color lineColor = Color.gray;
		Color markerColor = Color.blue;
		
		//clip computation        
		int clipX = Math.max(rect.x-2, originalClip.x);
		int clipY = Math.max(rect.y, originalClip.y);
		int clipRectWidth = Math.min(rect.width+5, originalClip.width-Math.max(0,rect.x-2-originalClip.x));//-rect.width-clipX);        
		int clipRectHeight = Math.min(rect.height+rect.y-clipY, originalClip.y+originalClip.height-clipY);
		
		if(applyClip)
			g.setClip(clipX, clipY, clipRectWidth, clipRectHeight);
		
		Graphics2D g2 = (Graphics2D)g;
		Composite defaultComp = g2.getComposite();		
		Composite overlayComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
		Stroke defaultStroke = g2.getStroke();
		BasicStroke overlayStroke = new BasicStroke(2f);
		
		g2.setComposite(defaultComp);
		g2.setColor(Color.black);
		
		int x1 = 0, x2 = 0;
		float prevY = this.offsetGraphMidpoint;
		float currY = this.offsetGraphMidpoint;
		
		for(int sample = 0; sample < numberOfSamples; sample++) {
			
			if(overlay) {
				lineColor = (Color)sampleLineColors.get(sample);        	
				markerColor = (Color)sampleMarkerColors.get(sample);
			}
			
			//short circuit if the sample is not to be displayed
			if(!this.showSample[sample])
				continue;
			
			if(!this.offsetLinesMode) {
				for(int gene = startIndex; gene < endIndex-1; gene++) {
					
					fValue = means[gene][sample];
					sValue = means[gene+1][sample];
					
					if(Float.isNaN(fValue)) {
						continue;
					}
					
					g.setColor((Color)(lineColor));
					
					if(!Float.isNaN(sValue)) {
						g.drawLine(left+(int)Math.round((gene-startIndex)*stepX), zeroValue - (int)Math.round(fValue*factor),
								left+(int)Math.round(((gene-startIndex)+1)*stepX), zeroValue - (int)Math.round(sValue*factor));
						
						if(showOverlay)
							prevY = drawOverlayLine(g2, prevY, sValue, left+(int)Math.round((gene-startIndex)*stepX), left+(int)Math.round(((gene-startIndex)+1)*stepX), factor, zeroValue, 
									defaultStroke, overlayStroke, overlayComp, defaultComp);							
					}
					
					g.setColor((Color)(markerColor));
					g.fillOval(left+(int)Math.round((gene-startIndex)*stepX)-2, zeroValue - (int)Math.round(fValue*factor)-2, 4,4);                    
				}
				
				// last point, must be a valid number and there has to be at least point rendered
				if(!Float.isNaN(sValue) && startIndex > endIndex-1)
					    g.fillOval(left+(int)Math.round((endIndex-startIndex-1)*stepX)-1, zeroValue - (int)Math.round(sValue*factor)-2, 4,4);		
			} else {  //offset lines mode
				
				for(int gene = startIndex; gene < endIndex; gene++) {
					
					fValue = means[gene][sample];
					if(gene+1 < means.length)
						sValue = means[gene+1][sample];
					else
						sValue = Float.NaN;
					
					if(Float.isNaN(fValue)) {
						continue;
					}
					
					g.setColor((Color)(lineColor));
					
					if(fValue >= upperCutoff)
						g.setColor(posColor);
					else if(fValue <= lowerCutoff)
						g.setColor(negColor);
					else
						g.setColor(Color.black);					
					
					g.drawLine(left+(int)Math.round((gene-startIndex)*stepX), zeroValue - (int)Math.round(fValue*factor),
							left+(int)Math.round((gene-startIndex)*stepX), zeroValue - (int)Math.round(this.offsetGraphMidpoint*factor));
					
					g.setColor(Color.darkGray);
					g.fillOval(left+(int)Math.round((gene-startIndex)*stepX)-2, zeroValue - (int)Math.round(fValue*factor)-2, 4,4);                    
					
					if(showOverlay && !Float.isNaN(sValue))
						prevY = drawOverlayLine(g2, prevY, sValue, left+(int)Math.round((gene-startIndex)*stepX), left+(int)Math.round(((gene-startIndex)+1)*stepX), factor, zeroValue, defaultStroke, overlayStroke, overlayComp, defaultComp);					
					
				}				
			}			
		}
		
		//reset stroke
		g2.setStroke(defaultStroke);
		//reset composite
		g2.setComposite(defaultComp);
		
		//reset clip		
		if(applyClip)
			g.setClip(originalClip);

		// draw rectangle
		g.setColor(Color.black);
		g.drawRect(left, top, width, height);
			
		String str;
		int strWidth;
		
		float val;
		int yOffset;
		for(int i = 0; i < yLabels.size(); i++) {
			str = (String)(yLabels.get(i));
			val = Float.parseFloat(str);
			strWidth = fm.stringWidth(str);
			g.drawString(str, left-10-strWidth, zeroValue+5-(int)Math.round(val*factor));
			g.drawLine(left-5, zeroValue - (int)Math.round(val*factor), left, zeroValue-(int)Math.round(val*factor));        		
		}
		
		
		//if show zero line
		if(this.showXAxis) {
			Stroke stroke = g.getStroke();
			Color initColor = g.getColor();
			
			g.setStroke(this.xAxisStroke);
			g.setColor(this.xAxisColor);
			g.drawLine(clipX, zeroValue - (int)Math.round(xAxisCrossPoint*factor), clipRectWidth+clipX, zeroValue - (int)Math.round(xAxisCrossPoint*factor));
			
			g.setColor(initColor);
			g.setStroke(stroke);
		}
		
		//drag shading
		if(inDragMode) {
			g.setColor(new Color(244, 250, 152));
			g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.8f));									
			//g.fillRect(Math.max(left, Math.min(dragStartX, dragStopX)), top, Math.min(Math.max(dragStopX-dragStartX, dragStartX - dragStopX), Math.max((left+width)-dragStopX,(left+width)-dragStartX)), height);				
			g.fillRect(Math.max(left+1, Math.min(dragStartX, dragStopX)), top, Math.max(dragStopX-dragStartX-1, dragStartX - dragStopX), height);								
			g.setComposite(defaultComp);		
		}
		
		
		//reference line
		if(this.showRefLine && this.drawReferenceBlock  && xref >= left && xref <= left+width){          
			
			//add locus and coordinates if in a graph
			if(rect.contains(xref, yref)) {
				
				g.setFont(new Font("Monospaced", Font.BOLD, 12));        		
				fm = g.getFontMetrics();
				
				g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.8f));					
				g.setColor(new Color(209, 213, 254));
				
				int boxWidth = Math.max(fm.stringWidth(locusNames[currLocusIndex]), Math.max(fm.stringWidth(String.valueOf(start[currLocusIndex])), fm.stringWidth(String.valueOf(start[currLocusIndex])))) + 10;
				int boxHeight = 3 * fm.getHeight() + 2;
				
				int boxX = xref;				
				int boxY = yref - boxHeight;
				
				if(left + width < xref + boxWidth)
					boxX = xref - boxWidth;
				
				if(top > yref - boxHeight)
					boxY = yref;
				
				g.fillRect(boxX, boxY, boxWidth, boxHeight);
				
				g.setColor(Color.black);
				g.setComposite(defaultComp);				
				g.drawString(locusNames[currLocusIndex], boxX+5, boxY- 2 + fm.getHeight());				
				g.drawString(String.valueOf(start[currLocusIndex]), boxX+5, boxY - 2 + 2*(fm.getHeight()));
				g.drawString(String.valueOf(end[currLocusIndex]), boxX+5, boxY - 2 + 3*(fm.getHeight()));
				
				
				g.setFont(new Font("Monospaced", Font.BOLD, 18));        		
				fm = g.getFontMetrics();
				
			}
			
			//vert ref line
			g.setColor(Color.blue);
			g.setComposite(defaultComp);
			g.drawLine(xref, top, xref, top+height);				
		}
		
	}
	
	/**
	 * Draws the overlay line while making the main view apha value lower
	 * 
	 * @param g graphics object on which to paint
	 * @param prevY previous rectangle y
	 * @param currY current rectangle y
	 * @param prevX previous rectangle x
	 * @param currX current rectangle x
	 * @param factor x scaling factor
	 * @param zeroValue zero factor also for scaling
	 * @param defaultStroke default <code>BasicStroke</code>
	 * @param overlayStroke overlay <code>BasicStroke</code>
	 * @param transparentComp semi-transparent <code>Composite</code>
	 * @param solidComp default alpha = 1, <code>Composite</code>
	 * @return returns the current y base to provide as prevY in the next iteration
	 */
	public float drawOverlayLine(Graphics2D g, float prevY, float currY, int prevX, int currX, float factor, int zeroValue, Stroke defaultStroke, BasicStroke overlayStroke, Composite transparentComp, Composite solidComp) {
				
		Color color = g.getColor();
		g.setColor(Color.black);
		g.setComposite(solidComp);
		g.setStroke(overlayStroke);
		
		//set current y position
		if(currY >= upperCutoff)
			currY = upperCutoff;
		else if(currY <= lowerCutoff)
			currY = lowerCutoff;
		else
			currY = this.neutralPoint;
		
		if(currY == prevY) {
			//draw a horizontal line from currY to prevY							
			g.drawLine(prevX, zeroValue - (int)Math.round(prevY*factor), currX, zeroValue - (int)Math.round(currY*factor));
		} else if(prevY == this.neutralPoint) {
			//draw horizontal on mid
			g.drawLine(prevX, zeroValue - (int)Math.round(prevY*factor), currX, zeroValue - (int)Math.round(prevY*factor));							
			//draw vertical line from mid to currY
			g.drawLine(currX, zeroValue - (int)Math.round(prevY*factor), currX, zeroValue - (int)Math.round(currY*factor));
		} else if(currY == this.neutralPoint) {
			//draw vertical line from prevY to mid							
			g.drawLine(prevX, zeroValue - (int)Math.round(prevY*factor), prevX, zeroValue - (int)Math.round(neutralPoint*factor));
			//draw horizontal line from mid to mid
			g.drawLine(prevX, zeroValue - (int)Math.round(neutralPoint*factor), currX, zeroValue - (int)Math.round(neutralPoint*factor));
		} else {
			//draw vertical line from prevY to mid
			g.drawLine(prevX, zeroValue - (int)Math.round(prevY*factor), prevX, zeroValue - (int)Math.round(neutralPoint*factor));
			//draw horizontal line from mid to mid
			g.drawLine(prevX, zeroValue - (int)Math.round(neutralPoint*factor), currX, zeroValue - (int)Math.round(neutralPoint*factor));
			//draw vertical line from mid to currY
			g.drawLine(currX, zeroValue - (int)Math.round(neutralPoint*factor), currX, zeroValue - (int)Math.round(currY*factor));
		}
				
		g.setColor(color);
		g.setStroke(defaultStroke);
		g.setComposite(transparentComp);
		return currY;		
	}
	
	
	/**
	 * Constructs the YAxisLabels and determins required inset.
	 */
	private void constructYAxisLabels() {
		yLabels = new Vector();
		int yInset = 40;
		float val = minValue;
		
		
		String str;
		int strWidth;
		
		DecimalFormat format = new DecimalFormat();
		format.setMaximumFractionDigits(1);
		
		if(yRangeMode == 1) { //custom mode    		
			
			while(val < maxValue) {
				str = format.format(val);    	
				yLabels.add(str);    			
				val += ticInterval;
			}
			str = format.format(maxValue);			
			yLabels.add(str);
			
		} else {
			
			int stepsY;
			float yRange = maxValue-minValue;
			if(yRange <= 8 && yRange >=4) {
				stepsY = (int)yRange+1;
				ticInterval = 1.0f;    		
			} else if(yRange < 4) {
				stepsY = (int)(yRange/0.5);
				ticInterval = 0.5f;
			} else {
				stepsY = 11;
				ticInterval = (yRange/10f);
			}
			
			val = minValue;
			
			
			for (int i=0; i<stepsY; i++) {
				str = format.format(val);
				val += ticInterval;
				yLabels.add(str);
			}        
		}
		setXGraphInset();    	
	}
	
	/**
	 * sets an X inset based on <code>FontMetrics</code> and the y labels
	 * start of the graph box has to permit full view of the y labels
	 *
	 */
	private void setXGraphInset() {
		if(fm != null) {
			this.xGraphInsetInitialized = true;
			this.xGraphInset = 0;
			for(int i = 0; i < yLabels.size(); i++) {
				xGraphInset = Math.max(xGraphInset, fm.stringWidth((String)yLabels.get(i)));
			}
			xGraphInset += 20; //buffer
		} 
	}
	
	
	/**
	 * @return returns the header
	 */
	public JComponent getHeaderComponent() {
		return header;
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
		this.displayMenu = menu;
		
		if(yRangeMode == 0) {  //display mode
			this.maxValue = menu.getMaxRatioScale();
			this.minValue = menu.getMinRatioScale();
			refreshGraph();			
		}
		repaint();
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
	private float calculateMaxValue(int[] probes) {
		float max = 0f;
		float value;
		final int samples = experiment.getNumberOfSamples();
		for (int sample=0; sample<samples; sample++) {
			for (int probe=0; probe<probes.length; probe++) {
				value = experiment.get(probes[probe], sample);
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
	 * Handles mouse event in viewer
	 */
	public class GraphListener extends java.awt.event.MouseAdapter implements java.awt.event.MouseMotionListener{
		int x = 0, y = 0;
	
		public void mouseReleased(MouseEvent me) {
			if(me.getModifiers() != MouseEvent.BUTTON1_MASK)
				return;

			if(inDragMode) {
				int initialStart = startIndex;
				startIndex = startIndex + Math.round((endIndex-startIndex - 1) * ((Math.min(dragStartX,dragStopX)-40f)/(getWidth()-80f)));				
				endIndex = initialStart + Math.round((endIndex-initialStart) * ((Math.max(dragStopX,dragStartX)-40f)/(getWidth()-80f)));								
				startIndex = Math.min(startIndex, endIndex);
				endIndex = Math.max(startIndex, endIndex);			
				if(startIndex < 0)
					startIndex = 0;
				if(endIndex > means.length-1)
					endIndex = means.length-1;
				header.setLimits( (float)startIndex/(float)means.length, (float)endIndex/(float)means.length);
			}
			inDragMode = false;
			repaint();
		}
		
		public void mouseDragged(java.awt.event.MouseEvent me) {	
			if(me.getModifiers() != MouseEvent.BUTTON1_MASK)
				return;
			
			if(!inDragMode) {
				dragStartX = me.getX();				
			}
			dragStopX = me.getX();
			
			if(dragStopX < 40)
				dragStopX = 41;
			if(dragStopX > getWidth()-40)
				dragStopX = getWidth()-39;
			
			inDragMode = true;			
			mouseMoved(me);
		}
		
		
		public void mouseMoved(java.awt.event.MouseEvent me) {
			
			int newX = me.getX();            
			int newY = me.getY();
			int refX = newX;
			
			if(inDragMode) {
				cursorOn = true;
				if(newX < 40)
					newX = 41;
				if(newX > getWidth()-40)
					newX = getWidth()-39;
				xref = newX; 
				yref = newY;
				currLocusIndex = startIndex + Math.round((endIndex-startIndex - 1) * ((newX-40f)/(getWidth()-80f)));
				repaint();
			}
			
			int numberOfSamples  = experiment.getNumberOfSamples();
			if(refX < 40 || refX > getWidth()- 40 || numberOfSamples <= 1){
				drawReferenceBlock = false;
				repaint();
				return;
			}
			
			drawReferenceBlock = true;    
			
			currLocusIndex = startIndex + Math.round((endIndex-startIndex - 1) * ((newX-40f)/(getWidth()-80f)));
			xref = newX;
			yref = newY;
			repaint();        
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

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExpression()
	 */
	public Expression getExpression() {
		// TODO Auto-generated method stub
		return null;
	} 
}
