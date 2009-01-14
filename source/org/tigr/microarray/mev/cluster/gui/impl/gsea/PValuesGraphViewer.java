package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
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
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.tigr.graph.GC;
import org.tigr.graph.GraphBar;
import org.tigr.graph.GraphElement;
import org.tigr.graph.GraphLine;
import org.tigr.graph.GraphPoint;
import org.tigr.graph.GraphTick;
import org.tigr.graph.GraphViewer;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer.PopupListener;
import org.tigr.util.Xcon;
import org.tigr.util.awt.ActionInfoEvent;
import org.tigr.util.awt.ActionInfoListener;
import org.tigr.util.awt.BoundariesDialog;
import org.tigr.util.awt.Drawable;
import org.tigr.util.awt.GBA;

public class PValuesGraphViewer extends JPanel implements IViewer {
	public final static int SYSTEM_QUADRANT1_ONLY = 1000;
	public final static int SYSTEM_QUADRANT12_ONLY = 1001;
	public final static int SYSTEM_ALL_QUADRANTS = 1002;
	public final static int SYSTEM_BOUNDS = 1100;
	public final static int HISTOGRAM_BAR_OUTLINE = 2000;
	public final static int HISTOGRAM_BAR_SOLID = 2001;
	public final static int GRAPH_POINTS_SEPERATE = 3000;
	public final static int GRAPH_POINTS_CONNECT = 3001;

	private String[][] pVals;
	private JFrame graphFrame;
	private GraphViewer graph;
	private GraphPoint gp;
	private GraphLine gl;
	private GraphTick gt;
	private String title;
	private String xLabel;
	private String yLabel;
	private String subTitle;
	private JPopupMenu popup;
	private EventListener eventListener;
	private GBA gba;
	private Vector graphElements;

	// Number of x coordinates would be equal to the number of gene sets.
	// minX would be equal to 0 and maxX would be equal to number of gene sets

	private double graphstartx;
	private double graphstopx;

	// The maximum and minimum range of the Y axis would be 0 and 1.
	// The range of possible p values. The graph would be centered at 0.05
	private double graphstarty;
	private double graphstopy;

	private double xAxisValue = 0;
	private double yAxisValue = 0;

	private boolean showCoordinates = false;
	private DecimalFormat coordinateFormat;
	private FontMetrics metrics;
	private int preXSpacing, postXSpacing, preYSpacing, postYSpacing;

	private int pointSize;
	private Font tickFont, labelFont, titleFont;
	private int tickFontHeight, tickFontWidth, labelFontHeight, labelFontWidth,
			titleFontHeight, titleFontWidth;
	private int startx, stopx, starty, stopy;

	private boolean referenceLinesOn = true;

	private boolean redrawCachedImage = true;
	private BufferedImage cachedImage;

	private Drawable canvas;
	private JScrollPane scrollPane;
	
	private int xOldEvent;
    private int yOldEvent;
    
      

	public PValuesGraphViewer(int startx, int stopx, int starty, int stopy,
			double graphstartx, double graphstopx, double graphstarty,
			double graphstopy, int preXSpacing, int postXSpacing,
			int preYSpacing, int postYSpacing, String title, String xLabel,
			String yLabel, String[][] pValues) {

		this.setPVals(pValues);
		this.setgraphstartx(graphstartx);
		this.setgraphstopx(graphstopx);
		this.setgraphstarty(graphstarty);
		this.setgraphstopy(graphstopy);
		this.setTitle(title);
		this.setXLabel(xLabel);
		this.setYLabel(yLabel);
		
		
		initializeViewer();
        initializeCanvas();
        initializePopupMenu();
   

		eventListener = new EventListener();
		// this.popup = createJPopupMenu(listener);
		getContentComponent().addMouseListener(eventListener);
	}

	private void initializeViewer() {
		setLayout(new GridBagLayout());
		eventListener = new EventListener();
		gba = new GBA();

		graphElements = new Vector();

		setBackground(Color.white);
		// setTickFont("monospaced", Font.PLAIN, 10);
		// setLabelFont("monospaced", Font.PLAIN, 12);
		// setTitleFont("monospaced", Font.PLAIN, 16);
		// setTickFont("Arial", Font.PLAIN, 10);
		// setLabelFont("Arial", Font.PLAIN, 12);
		// setTitleFont("Arial", Font.PLAIN, 16);

		setTickFont("SansSerif", Font.BOLD, 10);
		setLabelFont("SansSerif", Font.BOLD, 12);
		setTitleFont("SansSerif", Font.BOLD, 16);
		

	   	setSize(stopx - startx + preXSpacing + postXSpacing, stopy - starty
				+ preYSpacing + postYSpacing);
		coordinateFormat = new DecimalFormat();
		coordinateFormat.setMaximumFractionDigits(3);
	}

	private void initializeCanvas() {
		canvas = new Drawable(startx, stopx, starty, stopy) {
			public void controlPaint(Graphics g) {
				display(g);
			}
		};

		canvas.setBackground(Color.white);
		canvas.addMouseListener(eventListener);
		canvas.addMouseMotionListener(eventListener);

		scrollPane = new JScrollPane(canvas,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.getViewport().setBackground(Color.white);

		gba.add(this, scrollPane, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C);
	}

	protected void initializePopupMenu() {
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

	public void display(Graphics g1D) {
		Graphics2D g = (Graphics2D) g1D;
		metrics = g.getFontMetrics();
		if (false /* Draw from cached image */) {
			if ((cachedImage == null) || (redrawCachedImage == true)) {
				constructImage();
				redrawCachedImage = false;
			}
			drawImage(g);
		}

		else{
			
			drawGraph(g);
		}
	}

	public void drawGraph(Graphics2D g) {
		GraphElement e;
		
		FontMetrics metrics = g.getFontMetrics();
		  Rectangle rect = new Rectangle(40, 20, getWidth()-80, getHeight() - 40- getNamesWidth(metrics));
		    this.startx = Double.valueOf(rect.getMinX()).intValue();
		    this.starty = Double.valueOf(rect.getMinY()).intValue();
		    this.stopx=Double.valueOf(rect.getMaxX()).intValue();
		    this.stopy= Double.valueOf(rect.getMaxY()).intValue();
		    this.preXSpacing=40;
		    this.preYSpacing=80;
		    this.postXSpacing=20;
		    this.postYSpacing=40;
			setSize(stopx - startx + preXSpacing + postXSpacing, stopy - starty
					+ preYSpacing + postYSpacing);    
		
		
		 
		
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

		for (double i = graphstarty; i < graphstopy; i = i + 0.01) {

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


		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
		// g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		drawSystem(g, SYSTEM_BOUNDS);
		
		for (int i = 0; i < graphElements.size(); i++) {
			e = (GraphElement) graphElements.elementAt(i);
			if (e instanceof GraphPoint)
				drawPoint(g, (GraphPoint) e);
			else if (e instanceof GraphBar)
				drawBar(g, (GraphBar) e);
			else if (e instanceof GraphTick)
				drawTick(g, (GraphTick) e);
			else if (e instanceof GraphLine)
				drawLine(g, (GraphLine) e);
		}

		if (referenceLinesOn) { // Grid tracing is active
			int x = getXOldEvent();
			int y = getYOldEvent();
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

				g.setColor(Color.magenta);
				g.drawLine(x, convertY(graphstarty), x, convertY(graphstopy));
				g.drawLine(convertX(graphstartx), y, convertX(graphstopx), y);

				if (this.showCoordinates) {
					coordinateWidth = metrics.stringWidth(coordinateFormat
							.format(xVal)
							+ ", " + coordinateFormat.format(yVal));
					Composite comp = g.getComposite();
					g.setComposite(AlphaComposite.getInstance(
							AlphaComposite.SRC_OVER, 0.2f));
					g.setColor(Color.blue);
					g.fillRect(x, y - 15, coordinateWidth + 10, 15);
					g.setComposite(comp);
					g.setColor(Color.black);
					g.drawString(coordinateFormat.format(xVal) + ", "
							+ coordinateFormat.format(yVal), x + 5, y - 3);
				}
			} else {
				this.setCursor(Cursor.DEFAULT_CURSOR);
			}
		}

	//	 enforceGraphBounds(g);

		drawXLabel(g, xLabel, Color.black);
		drawYLabel(g, yLabel, Color.black);
		drawTitle(g, title, Color.black);
	}
	
	
	  /**
     * Returns max width of experiment names.
     */
    protected int getNamesWidth(FontMetrics metrics) {
        int maxWidth = 0;
        for (int i=0; i<this.pVals.length; i++) {
            maxWidth = Math.max(maxWidth, metrics.stringWidth(pVals[i][0]));
        }
        return maxWidth;
    }
  
	
	
	
	
	
	

	public void constructImage() {
		Graphics2D g = (Graphics2D) this.getGraphics();
		BufferedImage tempImage = (BufferedImage) java.awt.GraphicsEnvironment
				.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration().createCompatibleImage(
						canvas.getSize().width, canvas.getSize().height);
		// BufferedImage tempImage = (BufferedImage)
		// createImage(canvas.getSize().width, canvas.getSize().height);
		g = tempImage.createGraphics();
		System.out.println("drawGraph called from constructImage");
		drawGraph(g);
		cachedImage = tempImage;
	}

	public void drawImage(Graphics2D g) {
		g.drawImage(cachedImage, 0, 0, this);
	}

	public void addGraphElement(GraphElement e) {
		graphElements.addElement(e);
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

	public void setPointSize(int pointSize) {
		this.pointSize = pointSize;
	}

	public int getPointSize() {
		return this.pointSize;
	}

	public void showAll() {
		canvas.repaint();
	}

	public void clearAll(Graphics2D g) {
		canvas.fillRect(g, startx, starty, getSize().width, getSize().height,
				Color.white);
	}

	public void drawSystem(Graphics2D g, int systemStyle) {
		switch (systemStyle) {
		case SYSTEM_QUADRANT1_ONLY:
			g.drawLine(startx + preXSpacing, stopy - postYSpacing, stopx
					- postXSpacing, stopy - postYSpacing);
			g.drawLine(startx + preXSpacing, stopy - postYSpacing, startx
					+ preXSpacing, starty + preYSpacing);
			break;
		case SYSTEM_QUADRANT12_ONLY:
			g.drawLine(startx + preXSpacing, stopy - postYSpacing, stopx
					- postXSpacing, stopy - postYSpacing);
			g.drawLine(startx + preXSpacing, stopy - postYSpacing, startx
					+ preXSpacing, starty + preYSpacing);
			g.drawLine((stopx - postXSpacing - startx + preXSpacing) / 2, stopy
					- postYSpacing,
					(stopx - postXSpacing - startx + preXSpacing) / 2, starty
							+ preYSpacing);
			break;
		case SYSTEM_BOUNDS:
			drawLine(g, new GraphPoint(graphstartx, xAxisValue),
					new GraphPoint(graphstopx, xAxisValue), Color.black);
			drawLine(g, new GraphPoint(yAxisValue, graphstarty),
					new GraphPoint(yAxisValue, graphstopy), Color.black);
			break;
		case SYSTEM_ALL_QUADRANTS:
			break;
		}
	}

	// protected double getXScale() {return ((stopx - preXSpacing -
	// postXSpacing) / (graphstopx - graphstartx));}
	// protected double getYScale() {return ((stopy - preYSpacing -
	// postYSpacing) / (graphstopy - graphstarty));}
	protected double getXScale() {
		return ((canvas.getSize().width - preXSpacing - postXSpacing) / (graphstopx - graphstartx));
	}

	protected double getYScale() {
		return ((canvas.getSize().height - preYSpacing - postYSpacing) / (graphstopy - graphstarty));
	}

	public void drawTick(Graphics2D g, GraphTick e) {
		if (e.getOrientation() == GC.HORIZONTAL) {
			if (e.getLabel() != "") {
				drawVerticalTick(g, e.getLocation(), e.getHeight(), e
						.getAlignment(), e.getColor(), e.getLabel(), e
						.getLabelColor());
			} else
				drawVerticalTick(g, e.getLocation(), e.getHeight(), e
						.getAlignment(), e.getColor());
		} else if (e.getOrientation() == GC.VERTICAL) {
			if (e.getLabel() != "") {
				drawHorizontalTick(g, e.getLocation(), e.getHeight(), e
						.getAlignment(), e.getColor(), e.getLabel(), e
						.getLabelColor());
			} else
				drawHorizontalTick(g, e.getLocation(), e.getHeight(), e
						.getAlignment(), e.getColor());
		}
	}

	public void drawVerticalTick(Graphics2D g, double x, int length,
			int alignment, Color color) {
		if (x < graphstartx || x > graphstopx) /* System.out.println("X OOB") */
			;
		else if (length > postYSpacing) /* System.out.println("Length OOB") */
			;
		else {
			switch (alignment) {
			case GC.C:
				canvas.drawLine(g, convertX(x), convertY(xAxisValue)
						- (int) (length / 2), convertX(x), convertY(xAxisValue)
						+ (int) (length / 2), color);
				break;
			case GC.N:
				canvas.drawLine(g, convertX(x), convertY(xAxisValue),
						convertX(x), convertY(xAxisValue) - length, color);
				break;
			case GC.S:
				canvas.drawLine(g, convertX(x), convertY(xAxisValue),
						convertX(x), convertY(xAxisValue) + length, color);
				break;
			}
		}
	}

	public void drawVerticalTick(Graphics2D g, double x, int length,
			int alignment, Color color, String label, Color tickColor) {
		drawVerticalTick(g, x, length, alignment, color);

		if (true) { // Rotate labels
			g.rotate(-Math.PI / 2);
			// canvas.drawString(g, label, postYSpacing - length -
			// (label.length() * tickFontWidth), convertX(x), labelColor,
			// tickFont);
			// canvas.drawString(g, label, 750, convertX(x) +
			// canvas.getSize().width, tickColor, tickFont);
			canvas.drawString(g, label, -canvas.getSize().height + postYSpacing
					- (label.length() * tickFontWidth) - length, convertX(x)
					+ (tickFontHeight / 2), tickColor, tickFont);
			g.rotate(Math.PI / 2);
		} else {
			canvas.drawString(g, label, convertX(x)
					- (label.length() * tickFontWidth / 2),
					canvas.getSize().height - postYSpacing + length + 10,
					tickColor, tickFont);
		}
	}

	public void drawHorizontalTick(Graphics2D g, double y, int length,
			int alignment, Color color) {
		if (y < graphstarty || y > graphstopy) /* System.out.println("Y OOB") */
			;
		else if (length > preXSpacing) /* System.out.println("Length OOB") */
			;
		else {
			switch (alignment) {
			case GC.C:
				canvas.drawLine(g, convertX(yAxisValue) - (int) (length / 2),
						convertY(y), convertX(yAxisValue) + (int) (length / 2),
						convertY(y), color);
				break;
			case GC.E:
				canvas.drawLine(g, convertX(yAxisValue), convertY(y),
						convertX(yAxisValue) + length, convertY(y), color);
				break;
			case GC.W:
				canvas.drawLine(g, convertX(yAxisValue), convertY(y),
						convertX(yAxisValue) - length, convertY(y), color);
				break;
			}
		}
	}

	public void drawHorizontalTick(Graphics2D g, double y, int length,
			int alignment, Color color, String label, Color tickColor) {
		drawHorizontalTick(g, y, length, alignment, color);
		canvas.drawString(g, label, startx + preXSpacing - length
				- (label.length() * tickFontWidth), convertY(y)
				+ (tickFontHeight / 2), tickColor, tickFont);
	}

	public void enforceGraphBounds(Graphics2D g) {
		enforceGraphBounds(g, Color.white);
	}

	public void enforceGraphBounds(Graphics2D g, Color color) {
		int height = canvas.getSize().height;
		int width = canvas.getSize().width;

		canvas.fillRect(g, 0, 0, width, preYSpacing, color);
		canvas
				.fillRect(g, 0, height - postYSpacing, width, postYSpacing,
						color);
		canvas.fillRect(g, 0, 0, preXSpacing, height, color);
		canvas
				.fillRect(g, width - postXSpacing, 0, postXSpacing, height,
						color);
	}

	public void drawTitle(Graphics2D g, String title, Color titleColor) {
		if (subTitle == null)
			canvas.drawString(g, title, canvas.getSize().width / 2
					- (title.length() * titleFontWidth / 2),
					titleFontHeight * 2, titleColor, titleFont);
		else {
			canvas.drawString(g, title, canvas.getSize().width / 2
					- (title.length() * titleFontWidth / 2),
					(int) (titleFontHeight * 1.5), titleColor, titleFont);
			canvas.drawString(g, subTitle, canvas.getSize().width / 2
					- (subTitle.length() * titleFontWidth / 2),
					titleFontHeight * 3, titleColor, titleFont);
		}
	}

	public void drawXLabel(Graphics2D g, String label, Color labelColor) {
		canvas.drawString(g, label, canvas.getSize().width / 2
				- (label.length() * labelFontWidth / 2), convertY(graphstarty)
				+ postYSpacing - labelFontHeight, labelColor, labelFont);
	}

	public void drawYLabel(Graphics2D g, String label, Color labelColor) {
		/*
		 * String[] headerStrings = new String[columnVector.size()]; Font
		 * headerFont = new Font("System", Font.PLAIN, 12);
		 * g.setFont(headerFont); FontMetrics hfm = g.getFontMetrics(); int
		 * fontHeight = hfm.getHeight(); int nudge = (int) ((double) fontHeight *
		 * .5); int maxHeight = 0;
		 * 
		 * for (int i = 0; i < columnVector.size(); i++) { headerStrings[i] =
		 * ((SlideData) columnVector.elementAt(i)).getSlideDataName(); maxHeight =
		 * Math.max(maxHeight, hfm.stringWidth(headerStrings[i])); }
		 * 
		 * setPreferredSize(new Dimension(canvas.getSize().width, maxHeight));
		 * 
		 * g.rotate(- Math.PI / 2);
		 * 
		 * for (int i = 0; i < headerStrings.length; i++) {
		 * g.drawString(headerStrings[i], postYSpacing - getSize().height,
		 * preXSpacing + nudge + (canvas.getXElementSize() / 2) +
		 * canvas.getXpos(i)); }
		 * 
		 * g.rotate(Math.PI / 2);
		 */
		g.rotate(-Math.PI / 2);
		canvas.drawString(g, label, startx - postYSpacing + preXSpacing
				- (canvas.getSize().height / 2)
				- (label.length() * labelFontWidth / 2), labelFontHeight,
				labelColor, labelFont);
		g.rotate(Math.PI / 2);
	}

	public void drawPoint(Graphics2D g, GraphPoint graphPoint) {
		drawPointAt(g, graphPoint.getX(), graphPoint.getY(), graphPoint
				.getColor(), graphPoint.getPointSize());
	}

	public void drawPointAt(Graphics2D g, double x, double y, Color pointColor,
			int pointSize) {
		if ((x < graphstartx || x > graphstopx)
				|| (y < graphstarty || y > graphstopy)) /*
														 * System.out.println("X/Y
														 * OOB")
														 */
			;
		else
			canvas.fillRect(g, convertX(x) - (pointSize / 2), convertY(y)
					- (pointSize / 2), pointSize, pointSize, pointColor);
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
				if (i == 0)
					graphPoint2 = graphPoint;
				drawLine(g, graphPoint2, graphPoint, Color.black);
				drawPoint(g, graphPoint2);
				drawPoint(g, graphPoint);
				graphPoint2 = graphPoint;
			}
			break;
		}
	}

	public void drawLine(Graphics2D g, GraphLine e) {
		if (e.getX1() < graphstartx || e.getX1() > graphstopx
				|| e.getY1() < graphstarty || e.getY1() > graphstopy
				|| e.getX2() < graphstartx || e.getX2() > graphstopx
				|| e.getY2() < graphstarty || e.getY2() > graphstopy)
			;
		else
			canvas.drawLine(g, convertX(e.getX1()), convertY(e.getY1()),
					convertX(e.getX2()), convertY(e.getY2()), e.getColor());
	}

	public void drawLine(Graphics2D g, GraphPoint graphPoint1,
			GraphPoint graphPoint2, Color lineColor) {
		canvas.drawLine(g, convertX(graphPoint1.getX()), convertY(graphPoint1
				.getY()), convertX(graphPoint2.getX()), convertY(graphPoint2
				.getY()), lineColor);
	}

	public void drawBar(Graphics2D g, GraphBar e) {
		if (e.getStyle() == GraphBar.VERTICAL) {
			drawVerticalHistogramBar(g, e.getLower(), e.getUpper(), e
					.getValue(), e.getColor(), e.getStyle());
		} else if (e.getStyle() == GraphBar.HORIZONTAL) {
			// Nothing yet
		}
	}

	public void drawVerticalHistogramBar(Graphics2D g, double low, double high,
			double value, Color barColor, int style) {
		if ((low < graphstartx || low > graphstopx)
				|| (high < graphstartx || high > graphstopx)) /*
																 * System.out.println("Range
																 * OOB")
																 */
			;
		else if (value < graphstarty || value > graphstopy) /*
															 * System.out.println("Value
															 * OOB")
															 */
			;
		else {
			if (style == GraphBar.OUTLINE) {
				canvas.drawRect(g, convertX(low), convertY(value),
						(int) ((high - low) * getXScale()),
						(int) (value * getYScale()) - 1, barColor);
			} else if (style == GraphBar.SOLID) {
				canvas.fillRect(g, convertX(low), convertY(value),
						(int) ((high - low) * getXScale()) + 1,
						(int) (value * getYScale()) + 1, barColor);
			}
		}
	}
	
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
	  

	
	
	public void setCursor(int cursor) {setCursor(Cursor.getPredefinedCursor(cursor));}
	public void setXOldEvent(int xEvent) {this.xOldEvent = xEvent;}
	public int getXOldEvent() {return this.xOldEvent;}
	public void setYOldEvent(int yEvent) {this.yOldEvent = yEvent;}
	public int getYOldEvent() {return this.yOldEvent;}
	  
	
	public void toggleReferenceLines() {
		this.referenceLinesOn = (!this.referenceLinesOn);
	}

	public void setShowCoordinates(boolean showCoordinates) {
		this.showCoordinates = showCoordinates;
	}

	@Override
	public int[][] getClusters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JComponent getContentComponent() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public JComponent getCornerComponent(int cornerIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Experiment getExperiment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getExperimentID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Expression getExpression() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JComponent getHeaderComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BufferedImage getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JComponent getRowHeaderComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getViewerType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onClosed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDataChanged(IData data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDeselected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMenuChanged(IDisplayMenu menu) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSelected(IFramework framework) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setExperiment(Experiment e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setExperimentID(int id) {
		// TODO Auto-generated method stub

	}
	
	
	 /**
     * Paints chart into specified graphics.
     *
    public void paint(Graphics g) {
        FontMetrics metrics = g.getFontMetrics();
        Rectangle rect = new Rectangle(40, 20, getWidth()-80, getHeight() - 40 - getNamesWidth(metrics));
        paint((Graphics2D)g, rect, true);
    }
    
    public void subPaint(Graphics2D g, Rectangle rect, boolean drawMarks) {
        super.paint(g);
    }
    
    /**
     * Paints chart into specified graphics and with specified bounds.
     *
    public void paint(Graphics2D g, Rectangle rect, boolean drawMarks) {
        super.paint(g);
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
  //      if (gradientToggle) {
  //          setGradient(checkGradient());
  //      } else {
  //          setGradient(false);
   //     }
        final int zeroValue = top + (int)Math.round(height/2f);
        final int numberOfSamples  = experiment.getNumberOfSamples();
        
        //do this outside paint once menu is set up
        if(this.yRangeOption == CentroidViewer.USE_EXPERIMENT_MAX)
            maxYValue = this.maxExperimentValue;
        else if(this.yRangeOption == CentroidViewer.USE_CLUSTER_MAX)
            maxYValue = this.maxClusterValue;
        
        if (maxYValue == 0.0f) {
            maxYValue = 1.0f;
        }
        
        final float factor = height/(2f*maxYValue);
        final float stepX  = width/(float)(numberOfSamples-1);
        final int   stepsY = (int)maxYValue+1;
        if (this.drawVariances) {
            // draw variances
            g.setColor(bColor);
            for (int i=0; i<numberOfSamples; i++) {
            	
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
            boolean coloredClusters = false;
            float fValue, sValue, yInterval, lineHeight;
            Color color = null;
            float maxLineHeight = (maxExperimentValue*factor) / 20;	//maxExperimentValue is an expression value - not a coordinate length
            int R=0, G=0, B = 0, intervalNumber;
            
            for (int sample=0; sample<numberOfSamples-1; sample++) {
                for (int probe=0; probe<getCluster().length; probe++) {
                    fValue = this.experiment.get(getProbe(probe), sample);
                    sValue = this.experiment.get(getProbe(probe), sample+1);
                    if (Float.isNaN(fValue) || Float.isNaN(sValue)) {
                        continue;
                    }
                    if(!gradientColors) {
                        color = this.data.getProbeColor(this.experiment.getGeneIndexMappedToData(getProbe(probe)));
                        color = color == null ? DEF_CLUSTER_COLOR : color;
                        g.setColor(color);
                        g.drawLine(left+(int)Math.round(sample*stepX), zeroValue - (int)Math.round(fValue*factor),
                        left+(int)Math.round((sample+1)*stepX), zeroValue - (int)Math.round(sValue*factor));
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
                            g.drawLine(left+(int)Math.round(sample*stepX + ((float)i/intervalNumber)*stepX), zeroValue - (int)Math.round(fValue*factor + (float)i*yInterval),
                            left+(int)Math.round((sample)*stepX + (((float)i+1)/intervalNumber)*stepX), zeroValue - (int)Math.round(fValue*factor + ((float)i+1)*yInterval));
                        }
                    }
                }
            }
        }
        if (this.drawCodes && this.codes != null && clusters[clusterIndex].length > 0) {
            g.setColor(Color.blue);
            for (int i=0; i<numberOfSamples-1; i++) {
                g.drawLine(left+(int)Math.round(i*stepX), zeroValue-(int)Math.round(this.codes[this.clusterIndex][i]*factor), left+(int)Math.round((i+1)*stepX), zeroValue-(int)Math.round(this.codes[this.clusterIndex][i+1]*factor));
            }
        }
        
        // draw zero line
        g.setColor(Color.black);
        g.drawLine(left, zeroValue, left+width, zeroValue);
        // draw magenta line
        if (getCluster() != null && getCluster().length > 0) {
            g.setColor(centroidColor);
            for (int i=0; i<numberOfSamples-1; i++) {
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
        for (int i=1; i<numberOfSamples-1; i++) {
            g.drawLine(left+(int)Math.round(i*stepX), top+height-5, left+(int)Math.round(i*stepX), top+height);
            g.drawLine(left+(int)Math.round(i*stepX), top, left+(int)Math.round(i*stepX), top+5);
        }
        //draw Y items
        for (int i=1; i<stepsY; i++) {
            g.drawLine(left, zeroValue-(int)Math.round(i*factor), left+5, zeroValue-(int)Math.round(i*factor));
            g.drawLine(left, zeroValue+(int)Math.round(i*factor), left+5, zeroValue+(int)Math.round(i*factor));
        }
        // draw genes info
        g.setColor(bColor);
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
            // draw X samples names
            g.rotate(-Math.PI/2.0);
            final int max_name_width = getNamesWidth(metrics);
            for (int i=0; i<numberOfSamples; i++) {
                g.drawString(data.getSampleName(experiment.getSampleIndex(i)), -height-top-10-max_name_width, left+(int)Math.round(i*stepX)+3);
            }
            g.rotate(Math.PI/2.0);
        }
        if (getCluster() != null && getCluster().length > 0 && this.drawVariances) {
            // draw points
            g.setColor(bColor);
            for (int i=0; i<numberOfSamples; i++) {
                if (Float.isNaN(this.means[this.clusterIndex][i])) {
                    continue;
                }
                g.fillOval(left+(int)Math.round(i*stepX)-3, zeroValue-(int)Math.round(this.means[this.clusterIndex][i]*factor)-3, 6, 6);
            }
        }
        if(this.showRefLine && this.drawReferenceBlock){          
           java.awt.Composite initComposite = g.getComposite();
           g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.3f));
           g.setColor(Color.yellow);
           g.fillRect(xref-3, 20, 7, height); 
           g.setComposite(initComposite);
           g.setColor(Color.blue);
           g.drawLine(xref, 20, xref, height+20);
           framework.setStatusText("Sample= "+data.getSampleName(experiment.getSampleIndex(currExpRefLine))+",   mean = "+ this.means[this.clusterIndex][currExpRefLine]+",   sd = "+ this.variances[this.clusterIndex][currExpRefLine]);
        }
        g.setColor(bColor);
        if (getCluster() == null || getCluster().length == 0) {
            g.drawString("No Genes", left+10, top+20);
        } else {
            g.drawString(getCluster().length+" Genes", left+10, top+20);
        }
    }*/
   

	public class EventListener implements ActionListener, KeyListener,
			MouseListener, MouseMotionListener, java.io.Serializable {
		public void actionPerformed(ActionEvent event) {
			// popup.setVisible(false);
			// if (event.getActionCommand() == "Close") getFrame().dispose();
			if (event.getActionCommand() == "Set Boundaries") {
				BoundariesDialog bd = new BoundariesDialog(new JFrame(),
						getMinX(), getMaxX(), getMinY(), getMaxY());
				bd.addActionInfoListener(new ActionInfoListener() {
					public void actionInfoPerformed(ActionInfoEvent event) {
						Hashtable hash = event.getHashtable();

						setgraphstartx(Double.parseDouble((String) hash
								.get("lowerx")));
						setgraphstopx(Double.parseDouble((String) hash
								.get("upperx")));
						setgraphstarty(Double.parseDouble((String) hash
								.get("lowery")));
						setgraphstopy(Double.parseDouble((String) hash
								.get("uppery")));

						setXAxisValue(getMinY());
						setYAxisValue(getMinX());

						repaint();
					}
				});
				bd.show();
			} else if (event.getActionCommand() == "Reference Lines") {
				// referenceLinesOn = (! referenceLinesOn);
				repaint();
			}
		}

		public void keyPressed(KeyEvent event) {
			;
		}

		public void keyReleased(KeyEvent event) {
			;
		}

		public void keyTyped(KeyEvent event) {
			;
		}

		public void mouseClicked(MouseEvent event) {
			if (event.getModifiers() == MouseEvent.BUTTON3_MASK) {
				// popup.show(event.getComponent(), event.getX(), event.getY());
			}
		}

		public void mouseDragged(MouseEvent event) {
			// if(clickAndDragZoom){
			int x = event.getX();
			int y = event.getY();

			// }
		}

		public void mouseEntered(MouseEvent event) {
			;
		}

		public void mouseExited(MouseEvent event) {
			// setXOldEvent(-1);
			// setYOldEvent(-1);
			repaint();
		}

		public void mouseMoved(MouseEvent event) {
			int x = event.getX();
			int y = event.getY();
			// setXOldEvent(x);
			// setYOldEvent(y);
			repaint();
		}

		public void mousePressed(MouseEvent event) {
			;
		}

		public void mouseReleased(MouseEvent event) {
			;
		}
	}

	public String[][] getPVals() {
		return pVals;
	}

	public void setPVals(String[][] vals) {
		pVals = vals;
		System.out.println("Length of p val array is:"+pVals.length);
	}

	public GraphPoint getGp() {
		return gp;
	}

	public void setGp(GraphPoint gp) {
		this.gp = gp;
	}

	public GraphLine getGl() {
		return gl;
	}

	public void setGl(GraphLine gl) {
		this.gl = gl;
	}

	public GraphTick getGt() {
		return gt;
	}

	public void setGt(GraphTick gt) {
		this.gt = gt;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getXLabel() {
		return xLabel;
	}

	public void setXLabel(String label) {
		xLabel = label;
	}

	public String getYLabel() {
		return yLabel;
	}

	public void setYLabel(String label) {
		yLabel = label;
	}

	public double getMinX() {
		return graphstartx;
	}

	public void setgraphstartx(double minX) {
		this.graphstartx = minX;
	}

	public double getMaxX() {
		return graphstopx;
	}

	public void setgraphstopx(double maxX) {
		this.graphstopx = maxX;
	}

	public double getMinY() {
		return graphstarty;
	}

	public void setgraphstarty(double minY) {
		this.graphstarty = minY;
	}

	public double getMaxY() {
		return graphstopy;
	}

	public void setgraphstopy(double maxY) {
		this.graphstopy = maxY;
	}

	public double getXAxisValue() {
		return xAxisValue;
	}

	public void setXAxisValue(double axisValue) {
		xAxisValue = axisValue;
	}

	public double getYAxisValue() {
		return yAxisValue;
	}

	public void setYAxisValue(double axisValue) {
		yAxisValue = axisValue;
	}

}
