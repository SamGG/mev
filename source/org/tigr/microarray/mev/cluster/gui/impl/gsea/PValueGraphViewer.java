package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.tigr.graph.GC;
import org.tigr.graph.GraphBar;
import org.tigr.graph.GraphElement;
import org.tigr.graph.GraphLine;
import org.tigr.graph.GraphPoint;
import org.tigr.graph.GraphTick;

import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.util.awt.ActionInfoEvent;
import org.tigr.util.awt.ActionInfoListener;
import org.tigr.util.awt.BoundariesDialog;
import org.tigr.util.awt.Drawable;
import org.tigr.util.awt.GBA;

public class PValueGraphViewer extends JPanel implements IViewer {
	private GraphPoint gp;
	private GraphLine gl;
	private GraphTick gt;
	private String title;
	private String xLabel;
	private String yLabel;
	private String subTitle;
	private EventListener eventListener;
	private GBA gba;
	private Vector graphElements;
	private double graphstartx;
	private double graphstopx;

	// The maximum and minimum range of the Y axis would be 0 and 1.
	// The range of possible p values. The graph would be centered at 0.05
	private double graphstarty;
	private double graphstopy;
    private String[][]pVals;
    
    
   //Variables for functions from PValueGraphViewer 
    protected int preXSpacing, postXSpacing, preYSpacing, postYSpacing, startx, starty, stopx, stopy;
    protected double xAxisValue = 0, yAxisValue = 0;
    protected int pointSize;
    protected Font tickFont, labelFont, titleFont;
    protected int tickFontHeight, tickFontWidth, labelFontHeight, labelFontWidth, titleFontHeight, titleFontWidth;
    public final static int GRAPH_POINTS_SEPERATE = 3000;
    public final static int GRAPH_POINTS_CONNECT = 3001;
    public final static int SYSTEM_QUADRANT1_ONLY = 1000;
    public final static int SYSTEM_QUADRANT12_ONLY = 1001;
    public final static int SYSTEM_ALL_QUADRANTS = 1002;
    public final static int SYSTEM_BOUNDS = 1100;
    protected boolean referenceLinesOn = true;
    protected JPopupMenu popup;
    protected boolean showCoordinates = true;
    protected DecimalFormat coordinateFormat;
    //Variables end 
   
    
    //variables from Viewer
    protected int xOldEvent;
    protected int yOldEvent;
    public boolean mouseInside = false;
   //Variables end
    
    
    //Variable for the functions from ExperimentViewer. Changed from 20, 5
    private Dimension elementSize = new Dimension(20, 20);
    private Insets insets = new Insets(0, 10, 0, 0);
    private int contentWidth = 0;
    
    
	public PValueGraphViewer(double graphstartx, double graphstopx,
			double graphstarty, double graphstopy, String title, String xLabel, String yLabel,String[][]pValues) {
		this.graphstartx = graphstartx;
		this.graphstopx = graphstopx;
		this.graphstarty = graphstarty;
		this.graphstopy = graphstopy;
		this.xLabel=xLabel;
		this.yLabel=yLabel;
		this.title=title;
		this.pVals=pValues;
		eventListener = new EventListener();
		this.addMouseMotionListener(eventListener);
		this.addMouseListener(eventListener);
		 coordinateFormat = new DecimalFormat();
	     coordinateFormat.setMaximumFractionDigits(3);
	   
		
		initComponents();
		initializePopupMenu();
		// TODO Auto-generated constructor stub
	}

	private void initComponents() {

		//setLayout(new java.awt.BorderLayout());
		setBackground(java.awt.Color.white);
		super.setBackground(java.awt.Color.white);
		//Using default  "monospace" font set in upDateSize() causes wierd spacing
		setTickFont("SansSerif", Font.BOLD, 10);
	    setLabelFont("SansSerif", Font.BOLD, 12);
	   setTitleFont("SansSerif", Font.BOLD, 16);
	    setTitle(this.title);
	    setXLabel(this.xLabel);
	    setYLabel(this.yLabel);
	    graphElements=new Vector();
	       

	}
	
	
	 protected void initializePopupMenu(){
	        popup = new JPopupMenu();
	        JMenuItem item = new JMenuItem("Set Bounds");
	        item.setActionCommand("Set Boundaries");
	        item.addActionListener(eventListener);
	        popup.add(item);
	        popup.addSeparator();
	        item = new JMenuItem("Reference Lines");
	        item.setActionCommand("Reference Lines");
	        item.addActionListener(eventListener);
	        popup.add(item);
	    }
	   

	public void paint(Graphics g) {
	
		super.paint(g);
		Graphics2D g2 =  (Graphics2D)g;
		GraphElement e;
		FontMetrics metrics = g2.getFontMetrics();
		//Try setting these values here. You have pretty much all required info now.
		this.preXSpacing=getLeftMargin(g2);
		this.postYSpacing=getBottomMargin(g2);
		this.postXSpacing=20;
		//Space where the Title of the graph is displayed
		this.preYSpacing=50;
		updateSize();
			
		/*Rectangle bounds=g.getClipBounds();
		this.startx=
		this.stopx=
		this.starty=
		this.stopy=*/
		
	
		gl = new GraphLine(0, 0, graphstopx, 0, Color.BLACK);
		graphElements.add(gl);

		gl = new GraphLine(0, 0, 0, graphstopy, Color.BLACK);
		graphElements.add(gl);
		
		for (double i = 1; i <= graphstopx; i = i + 1) {
			// GraphLine is used here to construct the yellow colored grid
			// (vertical bars)
			gl = new GraphLine(i, graphstarty, i, graphstopy, Color.yellow);
			graphElements.add(gl);
		}

		for (double i = 0; i < graphstopy; i = i + 0.01) {
			// if (i != 0) {
			// GraphLine is used here to construct the yellow colored grid
			// (horizontal bars)
			if (i == 0.05)
				gl = new GraphLine(graphstartx, i, graphstopx, i, Color.black);
			else
				gl = new GraphLine(graphstartx, i, graphstopx, i, Color.yellow);
			graphElements.add(gl);
			// }
		}

		for (int i = 0; i < this.pVals.length; i++) {
			// GraphPoint is used to draw the actual data points in the graph
			// (blue colored dots)
		//	System.out.println("GraphPoint:"+pVals[i][1]);
			gp = new GraphPoint(i + 1, Double.parseDouble(this.pVals[i][1]),
					Color.blue, 3);
			graphElements.add(gp);

		}

		for (double i = graphstarty; i < graphstopy; i = i + 0.05) {

			// NumberFormat helps set the number of points after the fraction to
			// whatever we want it to be
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(2);

			// System.out.println(nf.format(i));
			gt = new GraphTick(i, 8, Color.black, GC.VERTICAL, GC.C, nf
					.format(i), Color.black);
			graphElements.add(gt);
		}

		for (int i = 0; i < graphstopx; i++) {
		//	System.out.println("Graphtick:"+pVals[i][0]);
			gt = new GraphTick(i + 1, 8, Color.black, GC.HORIZONTAL, GC.C,
					pVals[i][0], Color.black);
			graphElements.add(gt);
			
		}


	//	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		//		RenderingHints.VALUE_ANTIALIAS_OFF);
	
		//drawSystem draws x and y axis in an L shape
		drawSystem(g2, SYSTEM_BOUNDS);
		
		for (int i = 0; i < graphElements.size(); i++) {
			e = (GraphElement) graphElements.elementAt(i);
			if (e instanceof GraphPoint)
				drawPoint(g2, (GraphPoint) e);
			else if (e instanceof GraphBar)
				drawBar(g2, (GraphBar) e);
			else if (e instanceof GraphTick)
				drawTick(g2, (GraphTick) e);
			else if (e instanceof GraphLine)
				drawLine(g2, (GraphLine) e);
		}

	/**	
	 * Commented for now by Sarita
	 */ 
	 if (referenceLinesOn) { // Grid tracing is active
			int x = getXOldEvent();
			int y = getYOldEvent();
			//System.out.println("value of x is:"+x);
			//System.out.println("value of y is:"+y);
			int coordinateWidth;
			double xVal = 0;
			double yVal = 0;

			boolean onGraph = (x <= convertX(graphstopx))
					&& (x >= convertX(graphstartx))
					&& (y >= convertY(graphstopy))
					&& (y <= convertY(graphstarty));

			if (onGraph) {

				this.setCursor(Cursor.CROSSHAIR_CURSOR);

				xVal = ((x - convertX(this.graphstartx)) / this.getXScale())
						+ this.graphstartx;
				yVal = ((convertY(this.graphstarty) - y) / this.getYScale())
						+ this.graphstarty;

				g2.setColor(Color.magenta);
				g2.drawLine(x, convertY(graphstarty), x, convertY(graphstopy));
				g2.drawLine(convertX(graphstartx), y, convertX(graphstopx), y);

				if (this.showCoordinates) {
					
					coordinateWidth = metrics.stringWidth(coordinateFormat.format(xVal)
							+ ", " + coordinateFormat.format(yVal));
					Composite comp = g2.getComposite();
					g2.setComposite(AlphaComposite.getInstance(
							AlphaComposite.SRC_OVER, 0.2f));
					g2.setColor(Color.blue);
					g2.fillRect(x, y - 15, coordinateWidth + 20, 25);
					g2.setComposite(comp);
					g2.setColor(Color.black);
					g2.drawString("Geneset: "+this.pVals[(int)xVal][0] + ", "
								+ "Pvalue:"+this.pVals[(int)xVal][1], x + 5, y - 3);
					
					//g2.drawString(coordinateFormat.format(xVal) + ", "
						//	+ coordinateFormat.format(yVal), x + 5, y - 3);
				}
			} else {
				this.setCursor(Cursor.DEFAULT_CURSOR);
			}
		}

		// enforceGraphBounds(g);

	//	Commented by sarita
		drawXLabel(g2, xLabel, Color.black);
		drawYLabel(g2, yLabel, Color.black);
		drawTitle(g2, title, Color.black);
		

		
	}
	
	/************************Functions added from ExperimentViewer and modified*********************/
	 /**
     * Updates size of this viewer.
     */
    private void updateSize() {
        setFont(new Font("monospaced", Font.PLAIN, elementSize.height));
        Graphics2D g = (Graphics2D)getGraphics();
        //Wonder if i need to add the size of Y label here?
       // int width = elementSize.width*this.pVals.length + 1 + insets.left;--commented by Sarita
        int width = this.pVals.length*10 + 1 + insets.left;
      /*  if (isDrawAnnotations) {
            this.annotationWidth = getMaxWidth(g);
            width += 20+this.annotationWidth;
        }
        if (maxColorWidth < colorWidth){
        	maxColorWidth = colorWidth;
        }
        if(haveColorBar)
            width += this.elementSize.width*colorWidth + 10;*/
        this.contentWidth = width;
        
        //Height would be more importtant here, since the gene set labels are shown vertically
        //int height = elementSize.height*this.pVals.length+1;---commented by Sarita
        int height = getBottomMargin(g)+100*5+1+20;
        setSize(width, height);
     
       	setPreferredSize(new Dimension(width, height));
       	
    }
    
    
    
    
    
    /**
     * This should compute the amount of space to leave from the bottome of the JPanel.
     * This will be = height of the gene set names+XLabel height. SInce i want to display the labels 
     * in a vertical fashion, max width == max height or the pre Y spacing
     * @param g
     * @return
     */
    private int getBottomMargin(Graphics g){
    	   if (g == null ) {
               return 0;
           }
        /*   if (isAntiAliasing) {
               g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
               g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
           }*/
           FontMetrics fm = g.getFontMetrics();
           
           int max = 0;
           String str;
           
          for (int i=0; i<this.pVals.length; i++) {//For loop commented temporarily by sarita
          	 str =  pVals[i][0];
              max = Math.max(max, fm.stringWidth(str));
           }
          
          
           return max;
      
    }
    /**
     * This returns the width of the Y label as the margin
     * @param g
     * @return
     */
    
    private int getLeftMargin(Graphics g){
    	if(g == null){
    		return 0;
    	}
    	
    	
    	FontMetrics fm=g.getFontMetrics();
    	return (fm.stringWidth(getYLabel()));
    	
    }
    
    
    /**
     * Returns content width
     */
    public int getContentWidth(){
    	updateSize();
        return contentWidth;
    }

    
    

	/************************Functions added from ExperimentViewer and modified*********************/
	
	
	
	/********************Functions copied from GraphViewer************************/
	
	 public void setSubTitle(String subTitle){this.subTitle = subTitle;}
	 public String getSubTitle(){return this.subTitle;}
	 public void setTitle(String title){this.title = title;}
	 public String getTitle(){return this.title;}
	 public String getXLabel(){ return this.xLabel; }
	 public void setXLabel(String xlabel) {this.xLabel = xlabel;}
	 public String getYLabel(){ return this.yLabel; }
	 public void setYLabel(String ylabel) {this.yLabel = ylabel;}
	 public void setXAxisValue(double x) {this.xAxisValue = x;}
     public double getXAxisValue() {return this.xAxisValue;}
    public void setYAxisValue(double y) {this.yAxisValue = y;}
	    public double getYAxisValue() {return this.yAxisValue;}
	   
	    public void toggleReferenceLines(){
	        this.referenceLinesOn = (! this.referenceLinesOn);
	    }
	    
	    public void setShowCoordinates(boolean showCoordinates){
	        this.showCoordinates = showCoordinates;
	    }
	   
	   

	   
	 public void drawSystem(Graphics2D g, int systemStyle) {
	        switch (systemStyle) {
	            case SYSTEM_QUADRANT1_ONLY:
	                g.drawLine(startx + preXSpacing, stopy - postYSpacing, stopx - postXSpacing, stopy - postYSpacing);
	                g.drawLine(startx + preXSpacing, stopy - postYSpacing, startx + preXSpacing, starty + preYSpacing);
	                break;
	            case SYSTEM_QUADRANT12_ONLY:
	                g.drawLine(startx + preXSpacing, stopy - postYSpacing, stopx - postXSpacing, stopy - postYSpacing);
	                g.drawLine(startx + preXSpacing, stopy - postYSpacing, startx + preXSpacing, starty + preYSpacing);
	                g.drawLine((stopx - postXSpacing - startx + preXSpacing) / 2, stopy - postYSpacing,
	                (stopx - postXSpacing - startx + preXSpacing) / 2, starty + preYSpacing);
	                break;
	            case SYSTEM_BOUNDS:
	                drawLine(g, new GraphPoint(graphstartx, xAxisValue), new GraphPoint(graphstopx, xAxisValue), Color.black);
	                drawLine(g, new GraphPoint(yAxisValue, graphstarty), new GraphPoint(yAxisValue, graphstopy), Color.black);
	                break;
	            case SYSTEM_ALL_QUADRANTS:
	                break;
	        }
	    }
	    
	
	
	  public void drawTitle(Graphics2D g, String title, Color titleColor) {
	        if(subTitle == null)
	            this.drawString(g, title, this.getSize().width / 2 - (title.length() * titleFontWidth / 2), titleFontHeight * 2, titleColor, titleFont);
	        else {
	            this.drawString(g, title, this.getSize().width / 2 - (title.length() * titleFontWidth / 2), (int)(titleFontHeight * 1.5), titleColor, titleFont);
	            this.drawString(g, subTitle, this.getSize().width / 2 - (subTitle.length() * titleFontWidth / 2), titleFontHeight * 3, titleColor, titleFont);           
	        }
	    }
	    
	    public void drawXLabel(Graphics2D g, String label, Color labelColor) {
	        this.drawString(g, label, this.getSize().width / 2 - (label.length() * labelFontWidth / 2), convertY(graphstarty) + postYSpacing - labelFontHeight, labelColor, labelFont);
	    }
	    
	    public void drawYLabel(Graphics2D g, String label, Color labelColor) {
	        g.rotate(- Math.PI / 2);
	        this.drawString(g, label, startx - postYSpacing + preXSpacing - (this.getSize().height / 2) - (label.length() * labelFontWidth / 2), labelFontHeight, labelColor, labelFont);
	       
	        g.rotate(Math.PI / 2);
	    }
	    
	    public void drawPoint(Graphics2D g, GraphPoint graphPoint) {
	        drawPointAt(g, graphPoint.getX(), graphPoint.getY(), graphPoint.getColor(), graphPoint.getPointSize());
	    }
	    
	    public void drawPointAt(Graphics2D g, double x, double y, Color pointColor, int pointSize) {
	        if ((x < graphstartx || x > graphstopx) || (y < graphstarty || y > graphstopy)) /*System.out.println("X/Y OOB")*/;
	        else this.fillRect(g, convertX(x) - (pointSize / 2), convertY(y) - (pointSize / 2), pointSize, pointSize, pointColor);
	    }
	    
	    public void drawPoints(Graphics2D g, Vector graphPoints, int graphPointStyle) {
	        GraphPoint graphPoint, graphPoint2 = null;
	        
	        switch (graphPointStyle) {
	            case GRAPH_POINTS_SEPERATE:
	                for (int i = 0; i < graphPoints.size(); i++) {
	                    graphPoint = (GraphPoint) graphPoints.elementAt(i);
	                    drawPoint(g, graphPoint);
	                }
	                break;
	            case GRAPH_POINTS_CONNECT:
	                for (int i = 0; i < graphPoints.size(); i++) {
	                    graphPoint = (GraphPoint) graphPoints.elementAt(i);
	                    if (i == 0) graphPoint2 = graphPoint;
	                    drawLine(g, graphPoint2, graphPoint, Color.black);
	                    drawPoint(g, graphPoint2);
	                    drawPoint(g, graphPoint);
	                    graphPoint2 = graphPoint;
	                }
	                break;
	        }
	    }
	    
	    public void drawLine(Graphics2D g, GraphLine e) {
	        if(e.getX1() < graphstartx || e.getX1() > graphstopx ||
	            e.getY1() < graphstarty || e.getY1() > graphstopy ||
	            e.getX2() < graphstartx || e.getX2() > graphstopx ||
	            e.getY2() < graphstarty || e.getY2() > graphstopy);
	        else
	            this.drawLine(g, convertX(e.getX1()), convertY(e.getY1()), convertX(e.getX2()), convertY(e.getY2()), e.getColor());
	        	
	    }
	    
	    public void drawLine(Graphics2D g, GraphPoint graphPoint1, GraphPoint graphPoint2, Color lineColor) {
	            this.drawLine(g, convertX(graphPoint1.getX()), convertY(graphPoint1.getY()),
	            convertX(graphPoint2.getX()), convertY(graphPoint2.getY()), lineColor);
	    }
	    
	    //Do not think we will need drawBar--remove
	    public void drawBar(Graphics2D g, GraphBar e) {
	        if (e.getStyle() == GraphBar.VERTICAL) {
	            drawVerticalHistogramBar(g, e.getLower(), e.getUpper(), e.getValue(), e.getColor(), e.getStyle());
	        } else if (e.getStyle() == GraphBar.HORIZONTAL) {
	            //Nothing yet
	        }
	    }
	   
	    //Do not think we will need drawVerticalHistogramBar eitjher------remove
	    public void drawVerticalHistogramBar(Graphics2D g, double low, double high, double value, Color barColor, int style) {
	        if ((low < graphstartx || low > graphstopx) || (high < graphstartx || high > graphstopx)) /*System.out.println("Range OOB")*/;
	        else if (value < graphstarty || value > graphstopy) /*System.out.println("Value OOB")*/;
	        else {
	            if (style == GraphBar.OUTLINE) {
	                this.drawRect(g, convertX(low), convertY(value), (int) ((high - low) * getXScale()), (int) (value * getYScale()) - 1, barColor);
	            } else if (style == GraphBar.SOLID) {
	                this.fillRect(g, convertX(low), convertY(value), (int) ((high - low) * getXScale()) + 1, (int) (value * getYScale()) + 1, barColor);
	            }
	        }
	    }
	    public void drawTick(Graphics2D g, GraphTick e) {
	        if (e.getOrientation() == GC.HORIZONTAL) {
	            if (e.getLabel() != "") {
	                drawVerticalTick(g, e.getLocation(), e.getHeight(), e.getAlignment(), e.getColor(), e.getLabel(), e.getLabelColor());
	            } else drawVerticalTick(g, e.getLocation(), e.getHeight(), e.getAlignment(), e.getColor());
	        } else if (e.getOrientation() == GC.VERTICAL) {
	            if (e.getLabel() != "") {
	                drawHorizontalTick(g, e.getLocation(), e.getHeight(), e.getAlignment(), e.getColor(), e.getLabel(), e.getLabelColor());
	            } else drawHorizontalTick(g, e.getLocation(), e.getHeight(), e.getAlignment(), e.getColor());
	        }
	    }
	    public void drawVerticalTick(Graphics2D g, double x, int length, int alignment, Color color) {
	        if (x < graphstartx || x > graphstopx) /*System.out.println("X OOB")*/;
	        else if (length > postYSpacing) /*System.out.println("Length OOB")*/;
	        else {
	            switch (alignment) {
	                case GC.C: this.drawLine(g, convertX(x), convertY(xAxisValue) - (int) (length / 2), convertX(x), convertY(xAxisValue) + (int) (length / 2), color); break;
	                case GC.N: this.drawLine(g, convertX(x), convertY(xAxisValue), convertX(x), convertY(xAxisValue) - length, color); break;
	                case GC.S: this.drawLine(g, convertX(x), convertY(xAxisValue), convertX(x), convertY(xAxisValue) + length, color); break;
	            }
	        }
	    }
	    
	    public void drawVerticalTick(Graphics2D g, double x, int length, int alignment, Color color, String label, Color tickColor) {
	        drawVerticalTick(g, x, length, alignment, color);
	        
	        if (true) { //Rotate labels
	            g.rotate(- Math.PI / 2);
	            //this.drawString(g, label, postYSpacing - length - (label.length() * tickFontWidth), convertX(x), labelColor, tickFont);
	            //this.drawString(g, label, 750, convertX(x) + this.getSize().width, tickColor, tickFont);
	            this.drawString(g, label, - this.getSize().height + postYSpacing - (label.length() * tickFontWidth) - length,
	            convertX(x) + (tickFontHeight / 2), tickColor, tickFont);
	            g.rotate(Math.PI / 2);
	        } else {
	            this.drawString(g, label, convertX(x) - (label.length() * tickFontWidth / 2),
	            this.getSize().height - postYSpacing + length + 10, tickColor, tickFont);
	        }
	    }
	    
	    public void drawHorizontalTick(Graphics2D g, double y, int length, int alignment, Color color) {
	        if (y < graphstarty || y > graphstopy) /*System.out.println("Y OOB")*/;
	        else if (length > preXSpacing) /*System.out.println("Length OOB")*/;
	        else {
	            switch (alignment) {
	                case GC.C: this.drawLine(g, convertX(yAxisValue) - (int) (length / 2), convertY(y), convertX(yAxisValue) + (int) (length / 2), convertY(y), color); break;
	                case GC.E: this.drawLine(g, convertX(yAxisValue), convertY(y), convertX(yAxisValue) + length, convertY(y), color); break;
	                case GC.W: this.drawLine(g, convertX(yAxisValue), convertY(y), convertX(yAxisValue) - length, convertY(y), color); break;
	            }
	        }
	    }
	    
	    public void drawHorizontalTick(Graphics2D g, double y, int length, int alignment, Color color, String label, Color tickColor) {
	        drawHorizontalTick(g, y, length, alignment, color);
	        this.drawString(g, label, startx + preXSpacing - length - (label.length() * tickFontWidth),
     
	        convertY(y) + (tickFontHeight / 2), tickColor, tickFont);
	    }
	    
	   
	
	    public void setTickFont(String fontName, int fontStyle, int fontSize) {
	        tickFont = new Font(fontName, fontStyle, fontSize);
	        tickFontWidth = (int) (.6 * fontSize);
	        tickFontHeight = fontSize;
	    }
	    
	    public void setLabelFont(String fontName, int fontStyle, int fontSize) {
	        labelFont = new Font(fontName, fontStyle, fontSize);
	        labelFontWidth = (int) (.6 * fontSize);
	        labelFontHeight = fontSize;
	    }
	    
	    public void setTitleFont(String fontName, int fontStyle, int fontSize) {
	        titleFont = new Font(fontName, fontStyle, fontSize);
	        titleFontWidth = (int) (.6 * fontSize);
	        titleFontHeight = fontSize;
	    }
	    
	    protected double getXScale() {return((this.getSize().width - preXSpacing - postXSpacing) / (graphstopx - graphstartx));}
	    protected double getYScale() {return((this.getSize().height - preYSpacing - postYSpacing) / (graphstopy - graphstarty));}
	    protected int convertX(double x) {
	        if (true) { //Use log scale
	            return(int) ((x - graphstartx) * getXScale() + preXSpacing);
	            
	            //return (int) ((Math.log(x) - graphstartx) * getXScale() + preXSpacing);
	            
	        } else return(int) ((x - graphstartx) * getXScale() + preXSpacing);
	    }
	    
	    protected int convertY(double y) {
	        if (true) { //Use log scale
	            return(int) ((graphstopy - y) * getYScale() + preYSpacing);
	            
	            //return (int) ((graphstopy - Math.log(y)) * getYScale() + preYSpacing);
	            
	        }
	        return(int) ((graphstopy - y) * getYScale() + preYSpacing);
	    }
	   
	
	
	
	
	/***********************Functions from GraphViewer Ends***********************/
	
	
	

	/** ***Method copied from Drawable******* */
	public void drawPoint(Graphics2D g, Point point, Color color) {
		drawPoint(g, point.x, point.y, color);
	}

	public void drawPoint(Graphics2D g, int x, int y, Color color) {
		g.setColor(color);
		g.drawLine(x, y, x, y);
	}

	public void drawPoint(Graphics2D g, Point point) {
		drawPoint(g, point, getBackground());
	}

	public void drawPoint(Graphics2D g, int x, int y) {
		drawPoint(g, x, y, getBackground());
	}

	public void drawLine(Graphics2D g, int x1, int y1, int x2, int y2,
			Color color) {
		g.setColor(color);
		g.drawLine(x1, y1, x2, y2);
	}

	public void drawLine(Graphics2D g, int x1, int y1, int x2, int y2) {
		drawLine(g, x1, y1, x2, y2, getBackground());
	}

	public final void drawRect(Graphics2D g, int x, int y, int width,
			int height, Color color) {
		g.setColor(color);
		g.drawRect(x, y, width, height);
	}

	public final void fillRect(Graphics2D g, int x, int y, int width,
			int height, Color color) {
		g.setColor(color);
		g.fillRect(x, y, width, height);
	}

	public void drawString(Graphics2D g, String string, int x, int y,
			Color color, Font font) {
		g.setFont(font);
		g.setColor(color);
		g.drawString(string, x, y);
	}

	public void drawString(Graphics2D g, String string, int x, int y,
			Color color) {
		g.setColor(color);
		g.drawString(string, x, y);
	}

	/** ***Methods from Drawable Ends******* */

	/** ******Methods inherited from IViewer***** */
	
	public int[][] getClusters() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public JComponent getContentComponent() {
		return this;
	}

	
	public JComponent getCornerComponent(int cornerIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Experiment getExperiment() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public int getExperimentID() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public Expression getExpression() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public JComponent getHeaderComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public BufferedImage getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public JComponent getRowHeaderComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public int getViewerType() {
		return -1;
	}

	
	public void onClosed() {
		// TODO Auto-generated method stub

	}

	
	public void onDataChanged(IData data) {
		// TODO Auto-generated method stub

	}

	
	public void onDeselected() {
		// TODO Auto-generated method stub

	}

	
	public void onMenuChanged(IDisplayMenu menu) {
		// TODO Auto-generated method stub

	}


	public void onSelected(IFramework framework) {
      repaint();

	}

	
	public void setExperiment(Experiment e) {
		// TODO Auto-generated method stub

	}

	
	public void setExperimentID(int id) {
		// TODO Auto-generated method stub

	}
	/** ********Methods inherited from IViewer end****** */
	
	 private class EventListener implements ActionListener, KeyListener, MouseListener, MouseMotionListener, java.io.Serializable {
	        public void actionPerformed(ActionEvent event) {
	            popup.setVisible(false);
	           
	           if (event.getActionCommand() == "Set Boundaries") {
	                BoundariesDialog bd = new BoundariesDialog(new JFrame(), graphstartx, graphstopx, graphstarty, graphstopy);
	                bd.addActionInfoListener(new ActionInfoListener() {
	                    public void actionInfoPerformed(ActionInfoEvent event) {
	                        Hashtable hash = event.getHashtable();
	                        
	                        graphstartx = Double.parseDouble((String) hash.get("lowerx"));
	                        graphstopx = Double.parseDouble((String) hash.get("upperx"));
	                        graphstarty = Double.parseDouble((String) hash.get("lowery"));
	                        graphstopy = Double.parseDouble((String) hash.get("uppery"));
	                        
	                        setXAxisValue(graphstarty);
	                        setYAxisValue(graphstartx);
	                        
	                        repaint();
	                    }
	                });
	                bd.show();
	            } else if (event.getActionCommand() == "Reference Lines") {
	                referenceLinesOn = (! referenceLinesOn);
	                repaint();
	            }
	        }
	        public void keyPressed(KeyEvent event) {;}
	        public void keyReleased(KeyEvent event) {;}
	        public void keyTyped(KeyEvent event) {;}
	        public void mouseClicked(MouseEvent event) {
	            if(event.getModifiers() == MouseEvent.BUTTON3_MASK){
	            popup.show(event.getComponent(), event.getX(), event.getY());   
	            }
	        }
	        public void mouseDragged(MouseEvent event) {
	        //    if(clickAndDragZoom){
	                int x = event.getX();
	                int y = event.getY();
	                
	        //    }
	        }
	        public void mouseEntered(MouseEvent event) {;}
	        public void mouseExited(MouseEvent event) {
	            setXOldEvent(-1);
	            setYOldEvent(-1);
	            repaint();
	        }
	        public void mouseMoved(MouseEvent event) {
	            int x = event.getX();
	            int y = event.getY();
	            setXOldEvent(x);
	            setYOldEvent(y);
	            repaint();
	        }
	        public void mousePressed(MouseEvent event) {;}
	        public void mouseReleased(MouseEvent event) {;}
	    }

	
	  public void setCursor(int cursor) {setCursor(Cursor.getPredefinedCursor(cursor));}
	    public void setXOldEvent(int xEvent) {this.xOldEvent = xEvent;}
	    public int getXOldEvent() {return this.xOldEvent;}
	    public void setYOldEvent(int yEvent) {this.yOldEvent = yEvent;}
	    public int getYOldEvent() {return this.yOldEvent;}
	  
	
}
