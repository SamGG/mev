/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: GraphViewer.java,v $
 * $Revision: 1.4 $
 * $Date: 2005-03-10 15:22:39 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.graph;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import org.tigr.util.awt.Drawable;
import org.tigr.util.awt.Viewer;
import org.tigr.util.awt.GBA;
import org.tigr.util.awt.BoundariesDialog;
import org.tigr.util.awt.ActionInfoListener;
import org.tigr.util.awt.ActionInfoEvent;

import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import java.text.DecimalFormat;

public class GraphViewer extends Viewer {
   public static final long serialVersionUID = 1000101030001L;    
    
    public final static int SYSTEM_QUADRANT1_ONLY = 1000;
    public final static int SYSTEM_QUADRANT12_ONLY = 1001;
    public final static int SYSTEM_ALL_QUADRANTS = 1002;
    public final static int SYSTEM_BOUNDS = 1100;
    public final static int HISTOGRAM_BAR_OUTLINE = 2000;
    public final static int HISTOGRAM_BAR_SOLID = 2001;
    public final static int GRAPH_POINTS_SEPERATE = 3000;
    public final static int GRAPH_POINTS_CONNECT = 3001;
    
    protected int startx, stopx, starty, stopy;
    protected double graphstartx, graphstopx, graphstarty, graphstopy;
    protected int preXSpacing, postXSpacing, preYSpacing, postYSpacing;
    protected double xAxisValue = 0, yAxisValue = 0;
    protected int pointSize;
    protected Font tickFont, labelFont, titleFont;
    protected int tickFontHeight, tickFontWidth, labelFontHeight, labelFontWidth, titleFontHeight, titleFontWidth;
    
    protected String title, xLabel, yLabel, subTitle;
    
    protected JScrollPane scrollPane;
    protected JMenuBar menuBar;
    protected Drawable canvas;
    protected EventListener eventListener;
    protected GBA gba;
    
    protected boolean referenceLinesOn = true;
    
    private boolean redrawCachedImage = true;
    private BufferedImage cachedImage;
    
    protected Vector graphElements;
    protected JPopupMenu popup;
    protected boolean showCoordinates = false;
    protected DecimalFormat coordinateFormat;
    protected FontMetrics metrics;
    
    public GraphViewer(JFrame frame, int startx, int stopx, int starty, int stopy,
    double graphstartx, double graphstopx, double graphstarty, double graphstopy,
    int preXSpacing, int postXSpacing, int preYSpacing, int postYSpacing,
    String title, String xLabel, String yLabel) {
        super(frame);
        
        this.startx = startx;
        this.stopx = stopx;
        this.starty = starty;
        this.stopy = stopy;
        this.graphstartx = graphstartx;
        this.graphstopx = graphstopx;
        this.graphstarty = graphstarty;
        this.graphstopy = graphstopy;
        this.preXSpacing = preXSpacing;
        this.postXSpacing = postXSpacing;
        this.preYSpacing = preYSpacing;
        this.postYSpacing = postYSpacing;
        
        this.title = title;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        
        initializeViewer();
        initializeCanvas();
        if(frame != null)
            initializeFrame();
        initializePopupMenu();
    }
    
    public void setSubTitle(String subTitle){
        this.subTitle = subTitle;
    }
    
    private void initializeViewer() {
        setLayout(new GridBagLayout());
        eventListener = new EventListener();
        gba = new GBA();
        
        graphElements = new Vector();
        
        setBackground(Color.white);
        //setTickFont("monospaced", Font.PLAIN, 10);
        //setLabelFont("monospaced", Font.PLAIN, 12);
        //setTitleFont("monospaced", Font.PLAIN, 16);
        // 	setTickFont("Arial", Font.PLAIN, 10);
        //setLabelFont("Arial", Font.PLAIN, 12);
        //setTitleFont("Arial", Font.PLAIN, 16);
        
        setTickFont("SansSerif", Font.BOLD, 10);
        setLabelFont("SansSerif", Font.BOLD, 12);
        setTitleFont("SansSerif", Font.BOLD, 16);
        
        setSize(stopx - startx + preXSpacing + postXSpacing, stopy - starty + preYSpacing + postYSpacing);
        coordinateFormat = new DecimalFormat();
        coordinateFormat.setMaximumFractionDigits(3);
    }
    
    private void initializeCanvas() {
        canvas = new Drawable(startx, stopx, starty, stopy) {
            public void controlPaint(Graphics g) {
                display(g);
            }};
            
            canvas.setBackground(Color.white);
            canvas.addMouseListener(eventListener);
            canvas.addMouseMotionListener(eventListener);
            
            scrollPane = new JScrollPane(canvas, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            scrollPane.getViewport().setBackground(Color.white);
            
            gba.add(this, scrollPane, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C);
    }
    
    private void initializeFrame() {
        frame.getContentPane().setLayout(new GridBagLayout());
        frame.setResizable(true);
        frame.setBackground(Color.white);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {close();}
        });
        initializeMenuBar(frame);
        
        gba.add(frame.getContentPane(), this, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C);
        frame.pack();
    }
    
    private void initializeMenuBar(JFrame frame) {
        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(eventListener);
        fileMenu.add(closeItem);
        
        JMenu controlMenu = new JMenu("Control");
        
        JMenuItem graphBoundariesItem = new JMenuItem("Set Boundaries");
        graphBoundariesItem.addActionListener(eventListener);
        controlMenu.add(graphBoundariesItem);
        graphBoundariesItem.setEnabled(false);
        
        JCheckBoxMenuItem referenceLinesItem = new JCheckBoxMenuItem("Reference Lines");
        referenceLinesItem.addActionListener(eventListener);
        controlMenu.add(referenceLinesItem);
        referenceLinesItem.setSelected(true);
        
        menuBar.add(fileMenu);
        menuBar.add(controlMenu);
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
    
    public void display(Graphics g1D) {
        Graphics2D g = (Graphics2D) g1D;
        metrics = g.getFontMetrics();
        if (false /*Draw from cached image*/) {
            if ((cachedImage == null) || (redrawCachedImage == true)) {
                constructImage();
                redrawCachedImage = false;
            }
            drawImage(g);
        }
        
        else drawGraph(g);
    }
    
    public void drawGraph(Graphics2D g) {
        GraphElement e;
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
        //g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        drawSystem(g, SYSTEM_BOUNDS);
        for (int i = 0; i < graphElements.size(); i++) {
            e = (GraphElement) graphElements.elementAt(i);
            if (e instanceof GraphPoint) drawPoint(g, (GraphPoint) e);
            else if (e instanceof GraphBar) drawBar(g, (GraphBar) e);
            else if (e instanceof GraphTick) drawTick(g, (GraphTick) e);
            else if (e instanceof GraphLine) drawLine(g, (GraphLine) e);
        }
        
        if (referenceLinesOn) { //Grid tracing is active
            int x = getXOldEvent();
            int y = getYOldEvent();
            int coordinateWidth;
            double xVal = 0;
            double yVal = 0;
            
            boolean onGraph =  (x <= convertX(graphstopx)) && (x >= convertX(graphstartx)) &&
            (y >= convertY(graphstopy)) && (y <= convertY(graphstarty));
            
            if (onGraph) {
                
                this.setCursor(Cursor.CROSSHAIR_CURSOR);
                
                xVal = ((x - convertX(this.graphstartx))/this.getXScale()) + this.graphstartx;
                yVal = ((convertY(this.graphstarty) - y)/this.getYScale()) + this.graphstarty;
                
                g.setColor(Color.magenta);
                g.drawLine(x, convertY(graphstarty), x, convertY(graphstopy));
                g.drawLine(convertX(graphstartx), y, convertX(graphstopx), y);
                
                if(this.showCoordinates){
                    coordinateWidth = metrics.stringWidth(coordinateFormat.format(xVal)+", "+coordinateFormat.format(yVal));
                    Composite comp = g.getComposite();
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
                    g.setColor(Color.blue);
                    g.fillRect(x ,y-15, coordinateWidth + 10, 15);
                    g.setComposite(comp);
                    g.setColor(Color.black);
                    g.drawString(coordinateFormat.format(xVal)+", "+coordinateFormat.format(yVal), x+5, y-3);
                }
            }
            else{
                this.setCursor(Cursor.DEFAULT_CURSOR);
            }
        }
        
        //enforceGraphBounds(g);
        
        drawXLabel(g, xLabel, Color.black);
        drawYLabel(g, yLabel, Color.black);
        drawTitle(g, title, Color.black);
    }
    
    public void constructImage() {
        Graphics2D g = (Graphics2D) this.getGraphics();
        BufferedImage tempImage = (BufferedImage)java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(canvas.getSize().width, canvas.getSize().height);        
  //      BufferedImage tempImage = (BufferedImage) createImage(canvas.getSize().width, canvas.getSize().height);
        g = tempImage.createGraphics();
        drawGraph(g);
        cachedImage = tempImage;
    }
    
    public void drawImage(Graphics2D g) {
        g.drawImage(cachedImage, 0, 0, this);
    }
    
    public void addGraphElement(GraphElement e) {
        graphElements.addElement(e);
    }
    
    public void setGraphStartX(double graphStartX) {this.graphstartx = graphStartX;}
    public double getGraphStartX() {return this.graphstartx;}
    public void setGraphStopX(double graphStopX) {this.graphstopx = graphStopX;}
    public double getGraphStopX() {return this.graphstopx;}
    public void setGraphStartY(double graphStartY) {this.graphstarty = graphStartY;}
    public double getGraphStartY() {return this.graphstarty;}
    public void setGraphStopY(double graphStopY) {this.graphstopy = graphStopY;}
    public double getGraphStopY() {return this.graphstopy;}
    
    public void setXAxisValue(double x) {this.xAxisValue = x;}
    public double getXAxisValue() {return this.xAxisValue;}
    public void setYAxisValue(double y) {this.yAxisValue = y;}
    public double getYAxisValue() {return this.yAxisValue;}
    
    public void setPreXSpacing(int x){ 
        this.preXSpacing = x; 
      //  ((GraphCanvas)(this.canvas)).setPreXSpacing(x);
    }
    public int getPreXSpacing(){ return this.preXSpacing; }
    public void setPreYSpacing(int y){ 
        this.preYSpacing = y; 
       // ((GraphCanvas)(this.canvas)).setPreYSpacing(y);
    }
    public int getPreYSpacing() { return this.preYSpacing; }
    public void setPostYSpacing(int y){ this.postYSpacing = y; }
    
    public String getXLabel(){ return this.xLabel; }
    public String getYLabel(){ return this.yLabel; }
    
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
    
    public void setPointSize(int pointSize) {this.pointSize = pointSize;}
    public int getPointSize() {return this.pointSize;}
    
    public void showAll() {canvas.repaint();}
    public void clearAll(Graphics2D g) {canvas.fillRect(g, startx, starty, getSize().width, getSize().height, Color.white);}
    
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
    
    //protected double getXScale() {return ((stopx - preXSpacing - postXSpacing) / (graphstopx - graphstartx));}
    //protected double getYScale() {return ((stopy - preYSpacing - postYSpacing) / (graphstopy - graphstarty));}
    protected double getXScale() {return((canvas.getSize().width - preXSpacing - postXSpacing) / (graphstopx - graphstartx));}
    protected double getYScale() {return((canvas.getSize().height - preYSpacing - postYSpacing) / (graphstopy - graphstarty));}
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
                case GC.C: canvas.drawLine(g, convertX(x), convertY(xAxisValue) - (int) (length / 2), convertX(x), convertY(xAxisValue) + (int) (length / 2), color); break;
                case GC.N: canvas.drawLine(g, convertX(x), convertY(xAxisValue), convertX(x), convertY(xAxisValue) - length, color); break;
                case GC.S: canvas.drawLine(g, convertX(x), convertY(xAxisValue), convertX(x), convertY(xAxisValue) + length, color); break;
            }
        }
    }
    
    public void drawVerticalTick(Graphics2D g, double x, int length, int alignment, Color color, String label, Color tickColor) {
        drawVerticalTick(g, x, length, alignment, color);
        
        if (true) { //Rotate labels
            g.rotate(- Math.PI / 2);
            //canvas.drawString(g, label, postYSpacing - length - (label.length() * tickFontWidth), convertX(x), labelColor, tickFont);
            //canvas.drawString(g, label, 750, convertX(x) + canvas.getSize().width, tickColor, tickFont);
            canvas.drawString(g, label, - canvas.getSize().height + postYSpacing - (label.length() * tickFontWidth) - length,
            convertX(x) + (tickFontHeight / 2), tickColor, tickFont);
            g.rotate(Math.PI / 2);
        } else {
            canvas.drawString(g, label, convertX(x) - (label.length() * tickFontWidth / 2),
            canvas.getSize().height - postYSpacing + length + 10, tickColor, tickFont);
        }
    }
    
    public void drawHorizontalTick(Graphics2D g, double y, int length, int alignment, Color color) {
        if (y < graphstarty || y > graphstopy) /*System.out.println("Y OOB")*/;
        else if (length > preXSpacing) /*System.out.println("Length OOB")*/;
        else {
            switch (alignment) {
                case GC.C: canvas.drawLine(g, convertX(yAxisValue) - (int) (length / 2), convertY(y), convertX(yAxisValue) + (int) (length / 2), convertY(y), color); break;
                case GC.E: canvas.drawLine(g, convertX(yAxisValue), convertY(y), convertX(yAxisValue) + length, convertY(y), color); break;
                case GC.W: canvas.drawLine(g, convertX(yAxisValue), convertY(y), convertX(yAxisValue) - length, convertY(y), color); break;
            }
        }
    }
    
    public void drawHorizontalTick(Graphics2D g, double y, int length, int alignment, Color color, String label, Color tickColor) {
        drawHorizontalTick(g, y, length, alignment, color);
        canvas.drawString(g, label, startx + preXSpacing - length - (label.length() * tickFontWidth),
        convertY(y) + (tickFontHeight / 2), tickColor, tickFont);
    }
    
    
    public void enforceGraphBounds(Graphics2D g) {
        enforceGraphBounds(g, Color.white);
    }
    
    public void enforceGraphBounds(Graphics2D g, Color color) {
        int height = canvas.getSize().height;
        int width = canvas.getSize().width;
        
        canvas.fillRect(g, 0, 0, width, preYSpacing, color);
        canvas.fillRect(g, 0, height - postYSpacing, width, postYSpacing, color);
        canvas.fillRect(g, 0, 0, preXSpacing, height, color);
        canvas.fillRect(g, width - postXSpacing, 0, postXSpacing, height, color);
    }
    
    public void drawTitle(Graphics2D g, String title, Color titleColor) {
        if(subTitle == null)
            canvas.drawString(g, title, canvas.getSize().width / 2 - (title.length() * titleFontWidth / 2), titleFontHeight * 2, titleColor, titleFont);
        else {
            canvas.drawString(g, title, canvas.getSize().width / 2 - (title.length() * titleFontWidth / 2), (int)(titleFontHeight * 1.5), titleColor, titleFont);
            canvas.drawString(g, subTitle, canvas.getSize().width / 2 - (subTitle.length() * titleFontWidth / 2), titleFontHeight * 3, titleColor, titleFont);           
        }
    }
    
    public void drawXLabel(Graphics2D g, String label, Color labelColor) {
        canvas.drawString(g, label, canvas.getSize().width / 2 - (label.length() * labelFontWidth / 2), convertY(graphstarty) + postYSpacing - labelFontHeight, labelColor, labelFont);
    }
    
    public void drawYLabel(Graphics2D g, String label, Color labelColor) {
        /*
        String[] headerStrings = new String[columnVector.size()];
        Font headerFont = new Font("System", Font.PLAIN, 12);
        g.setFont(headerFont);
        FontMetrics hfm = g.getFontMetrics();
        int fontHeight = hfm.getHeight();
        int nudge = (int) ((double) fontHeight * .5);
        int maxHeight = 0;
         
        for (int i = 0; i < columnVector.size(); i++)
            {
            headerStrings[i] = ((SlideData) columnVector.elementAt(i)).getSlideDataName();
            maxHeight = Math.max(maxHeight, hfm.stringWidth(headerStrings[i]));
            }
         
        setPreferredSize(new Dimension(canvas.getSize().width, maxHeight));
         
        g.rotate(- Math.PI / 2);
         
        for (int i = 0; i < headerStrings.length; i++)
            {
            g.drawString(headerStrings[i], postYSpacing - getSize().height, preXSpacing + nudge + (canvas.getXElementSize() / 2) + canvas.getXpos(i));
            }
         
        g.rotate(Math.PI / 2);
         */
        g.rotate(- Math.PI / 2);
        canvas.drawString(g, label, startx - postYSpacing + preXSpacing - (canvas.getSize().height / 2) - (label.length() * labelFontWidth / 2), labelFontHeight, labelColor, labelFont);
        g.rotate(Math.PI / 2);
    }
    
    public void drawPoint(Graphics2D g, GraphPoint graphPoint) {
        drawPointAt(g, graphPoint.getX(), graphPoint.getY(), graphPoint.getColor(), graphPoint.getPointSize());
    }
    
    public void drawPointAt(Graphics2D g, double x, double y, Color pointColor, int pointSize) {
        if ((x < graphstartx || x > graphstopx) || (y < graphstarty || y > graphstopy)) /*System.out.println("X/Y OOB")*/;
        else canvas.fillRect(g, convertX(x) - (pointSize / 2), convertY(y) - (pointSize / 2), pointSize, pointSize, pointColor);
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
            canvas.drawLine(g, convertX(e.getX1()), convertY(e.getY1()), convertX(e.getX2()), convertY(e.getY2()), e.getColor());
    }
    
    public void drawLine(Graphics2D g, GraphPoint graphPoint1, GraphPoint graphPoint2, Color lineColor) {
            canvas.drawLine(g, convertX(graphPoint1.getX()), convertY(graphPoint1.getY()),
            convertX(graphPoint2.getX()), convertY(graphPoint2.getY()), lineColor);
    }
    
    public void drawBar(Graphics2D g, GraphBar e) {
        if (e.getStyle() == GraphBar.VERTICAL) {
            drawVerticalHistogramBar(g, e.getLower(), e.getUpper(), e.getValue(), e.getColor(), e.getStyle());
        } else if (e.getStyle() == GraphBar.HORIZONTAL) {
            //Nothing yet
        }
    }
    
    public void drawVerticalHistogramBar(Graphics2D g, double low, double high, double value, Color barColor, int style) {
        if ((low < graphstartx || low > graphstopx) || (high < graphstartx || high > graphstopx)) /*System.out.println("Range OOB")*/;
        else if (value < graphstarty || value > graphstopy) /*System.out.println("Value OOB")*/;
        else {
            if (style == GraphBar.OUTLINE) {
                canvas.drawRect(g, convertX(low), convertY(value), (int) ((high - low) * getXScale()), (int) (value * getYScale()) - 1, barColor);
            } else if (style == GraphBar.SOLID) {
                canvas.fillRect(g, convertX(low), convertY(value), (int) ((high - low) * getXScale()) + 1, (int) (value * getYScale()) + 1, barColor);
            }
        }
    }
    
    public void close() {
        if (hasFrame()) frame.dispose();
    }
    
    public void toggleReferenceLines(){
        this.referenceLinesOn = (! this.referenceLinesOn);
    }
    
    public void setShowCoordinates(boolean showCoordinates){
        this.showCoordinates = showCoordinates;
    }
    
    private class EventListener implements ActionListener, KeyListener, MouseListener, MouseMotionListener, java.io.Serializable {
        public void actionPerformed(ActionEvent event) {
            popup.setVisible(false);
            if (event.getActionCommand() == "Close") getFrame().dispose();
            else if (event.getActionCommand() == "Set Boundaries") {
                BoundariesDialog bd = new BoundariesDialog(getFrame(), graphstartx, graphstopx, graphstarty, graphstopy);
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
}