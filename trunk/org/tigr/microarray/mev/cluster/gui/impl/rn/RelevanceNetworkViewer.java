/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: RelevanceNetworkViewer.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.rn;

import java.util.Arrays;
import java.util.ArrayList;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import java.awt.event.*;

import java.awt.image.BufferedImage;

import javax.swing.*;

import org.tigr.microarray.mev.cluster.gui.*;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

public class RelevanceNetworkViewer extends JPanel implements IViewer, Scrollable {

    private static final String ZOOM_IN_CMD = "zoom-in-cmd";
    private static final String ZOOM_OUT_CMD = "zoom-out-cmd";
    private static final String SHOW_ALL_CMD = "show-all-cmd";
    private static final String SELECT_BY_DEGREE_CMD = "select-by-degree-cmd";
    private static final String SELECT_BY_GENE_ID_CMD = "select-by-gene-id-cmd";
    private static final String LINKS_COLOR_CMD     = "links-color-cmd";
    private static final String LINKS_THRESHOLD_CMD = "links-threshold-cmd";
    private static final String DESELECT_CMD = "deselect-cmd";
    private static final String SET_PUBLIC_CLUSTER_CMD = "set-public-cluster";
    private static final String RANDOM_LAYOUT_CMD = "random-layout-cmd";
    private static final String CIRCULAR_LAYOUT_CMD = "circular-layout-cmd";
    private static final String DEBUG_LAYOUT_CMD = "debug-layout-cmd";
    private static final String SET_LABEL_COLOR_CMD = "set-label-color-cmd";
    private static final String SET_SELECTION_COLOR_CMD = "set-selection-color-cmd";
    private static final String SET_BACKGROUND_COLOR_CMD = "set-background-color-cmd";
    private static final String SHAPE_RECT_CMD = "shape-rect-cmd";
    private static final String SHAPE_OVAL_CMD = "shape-oval-cmd";
    private static final String FIND_CLUSTER_CMD = "find-cluster-cmd";

    private static final int SHAPE_RECT = 0;
    private static final int SHAPE_OVAL = 1;

    private static final int MAX_CONTENT_SIZE = Integer.MAX_VALUE-1;
    private static final float ZOOM_COEFFICIENT = 2.0f;
    private Rectangle prevZoomRect = new Rectangle();

    private Experiment experiment;
    private IData data;
    private IFramework framework;
    private boolean isGenes;
    private int[][] clusters;
    private float[][] weights;
    private int[] indices;
    private float[][] coords; // x, y coordinaties
    private boolean[] selected; 
    private boolean[] draw; // used to prevent multiple drawing of a same spot
    private float links_threshold = 0f;
    private boolean isLinksColor = true; // use colors for the links?
    private static final int COLORS_DEEP = 100; // number of links palette colors
    private Color[] LINKS_PALETTE = createPalette(Color.blue, Color.red, COLORS_DEEP);
    private float weight_min = 0f;
    private float weight_scale; // scale to select color from the links palette

    private Color selectionColor = Color.green;
    private Color labelColor = Color.black;
    private Insets insets = new Insets(10, 10, 10, 10);
    private boolean isDrawBorders;
    private boolean isAntiAliasing = true;
    private Dimension elementSize = new Dimension(20, 5);
    private int labelIndex = -1;
    private String status;
    private int shape_type = SHAPE_RECT;

    private JPopupMenu popup;
    private JWindow tipWindow;

    public Color[] createPalette(Color color1, Color color2, int deep) {
        //BufferedImage image = new BufferedImage(deep, 1, BufferedImage.TYPE_3BYTE_BGR);
        //BufferedImage image = (BufferedImage)this.createImage(deep, 1);
        BufferedImage image = (BufferedImage)java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(deep,1);        
        
        Graphics2D graphics = image.createGraphics();
        GradientPaint gp = new GradientPaint(0, 0, color1, deep-1, 0, color2);
        graphics.setPaint(gp);
        graphics.drawRect(0, 0, deep-1, 1);
        Color[] colors = new Color[deep];
        for (int i=0; i<deep; i++)
            colors[i] = new Color(image.getRGB(i, 0));
        return colors;
    }

    public static float getWeightsMinValue(float[][] array) {
        if (array == null || array.length == 0)
            return Float.NaN;
        float min = Float.MAX_VALUE;
        for (int i=0; i<array.length; i++)
            for (int j=1; j<array[i].length; j++)
                min = Math.min(min, array[i][j]);
        return min;
    }

    public static float getWeightsMaxValue(float[][] array) {
        if (array == null || array.length == 0)
            return Float.NaN;
        float max = -Float.MAX_VALUE;
        for (int i=0; i<array.length; i++)
            for (int j=1; j<array[i].length; j++)
                max = Math.max(max, array[i][j]);
        return max;
    }

    /**
     * Constructs a <code>RelevanceNetworkViewer</code> for specified experiment,
     * clusters, weights and sorted indices.
     */
    public RelevanceNetworkViewer(boolean isGenes, Experiment experiment, int[][] clusters, float[][] weights, int[] indices) {
        setLayout(null);
        setBackground(Color.white);
        setFont(new Font("monospaced", Font.BOLD, this.elementSize.height));
        Listener listener = new Listener();
        this.popup = createJPopupMenu(listener);
        this.tipWindow = createTipWindow();
        getContentComponent().addMouseListener(listener);
        getContentComponent().addMouseMotionListener(listener);

        this.isGenes = isGenes;
        this.experiment = experiment;
        this.clusters = clusters;
        this.weights = weights;
        this.weight_min = getWeightsMinValue(weights);
        this.weight_scale = COLORS_DEEP/(getWeightsMaxValue(weights)-this.weight_min);
        this.indices = indices;
        RelevanceNetworkLayout layout = new RelevanceNetworkLayout();
        this.coords = layout.doLayout(clusters, weights, RelevanceNetworkLayout.CIRCULAR_LAYOUT);
        this.selected = createSelected(clusters);
        this.draw = new boolean[clusters.length];
        setPreferredSize(new Dimension(300, 300));
    }

    /**
     * Prepares an array of false values.
     */
    private boolean[] createSelected(int[][] cluster) {
        boolean[] selected = new boolean[cluster.length];
        clearSelected(selected);
        return selected;
    }

    /**
     * Fills in an array with false values.
     */
    private void clearSelected(boolean[] selected) {
        Arrays.fill(selected, false);
    }

    /**
     * Overriden to have a focus.  (deprecated as of 1.4, use isFocusable())
     */
    public boolean isFocusTraversable() {
        return true;
    }

     /**
     * Overriden to have a focus.  (deprecated as of 1.4, use isFocusable())
     */
    public boolean isFocusable() {
        return true;
    }
    
    /**
     * Paints relevance network into specified graphics.
     */
    public void paint(Graphics g1D) {
        super.paint(g1D);
        Graphics2D g = (Graphics2D)g1D;
        if (this.isAntiAliasing) {//Anti-aliasing is on
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        Rectangle bounds = g.getClipBounds();
        float scale = getScale();
        drawLines(g, bounds, scale);
        drawLabels(g, bounds, scale);
        drawShapes(g, bounds, scale);
    }

    /**
     * Calculates the viewer scale.
     */
    private final float getScale() {
        return Math.min(getPreferredSize().width-insets.left-insets.right, getPreferredSize().height-insets.top-insets.bottom);
    }

    /**
     * Checkes if a rectangle intersects with specified bounds.
     */
    private final boolean isRectIntersects(Rectangle rect, int x1, int y1, int x2, int y2) {
        if (x1 < rect.x && x2 < rect.x) {
            return false;
        }
        if (x1 > rect.x+rect.width && x2 > rect.x+rect.width) {
            return false;
        }
        if (y1 < rect.y && y2 < rect.y) {
            return false;
        }
        if (y1 > rect.y+rect.height && y2 > rect.y+rect.height) {
            return false;
        }
        return true;
    }

    /**
     * Checkes if (p1, p2) line intersects with vertical (x, y1, y2) one.
     * @return point the intersection coordinate.
     */
    private final boolean isIntersectVerticalLine(Point p1, Point p2, int x, int y1, int y2, Point point) {
        if ((p1.x < x && p2.x < x) || ((p1.x > x && p2.x > x))) {
            return false;
        }
        float tan = (float)(p2.y - p1.y)/(float)(p2.x - p1.x);
        float delta = tan*(float)(x - p1.x);
        float y = p1.y + delta;
        point.y = p1.y + Math.round(delta);
        point.x = x;
        return y > Math.min(y1, y2) && y < Math.max(y1, y2);
    }

    /**
     * Checkes if (p1, p2) line intersects with horizontal (y, x1, x2) one.
     * @return point the intersection coordinate.
     */
    private final boolean isIntersectHorizontalLine(Point p1, Point p2, int y, int x1, int x2, Point point) {
        if ((p1.y < y && p2.y < y) || ((p1.y > y && p2.y > y))) {
            return false;
        }
        float tan = (float)(p2.y - p1.y)/(float)(p2.x - p1.x);
        float delta = (float)(y - p1.y)/tan;
        float x = p1.x + delta;
        point.x = p1.x + Math.round(delta);
        point.y = y;
        return x > Math.min(x1, x2) && x < Math.max(x1, x2);
    }

    /**
     * Checkes if points p1 or p2 are an internal point of a rect.
     * @return n the coordinaties of an internal point.
     */
    private final boolean isInternalPoint(Rectangle rect, Point p1, Point p2, Point n) {
        boolean p1b = rect.contains(p1);
        boolean p2b = rect.contains(p2);
        if (p1b) {
            n.setLocation(p1);
            return true;
        }
        if (p2b) {
            n.setLocation(p2);
            return true;
        }
        return false;
    }

    /**
     * Checkes if (p1, p2) line intersects rect.
     * @return n1, n2 points which is intersection of the line and the rect.
     */
    private final boolean isLineIntersects(Rectangle rect, Point p1, Point p2, Point n1, Point n2) {
        n1.setLocation(p1);
        n2.setLocation(p2);
        if (rect.contains(p1) && rect.contains(p2)) {
            return true;
        }
        if (p1.x < rect.x && p2.x < rect.x) {
            return false;
        }
        if (p1.y < rect.y && p2.y < rect.y) {
            return false;
        }
        if (p1.x > rect.x+rect.width && p2.x > rect.x+rect.width) {
            return false;
        }
        if (p1.y > rect.y+rect.height && p2.y > rect.y+rect.height) {
            return false;
        }
        if (p1.x == p2.x) {
            // vertical line
            if (p1.y < rect.y) {
                n1.y = rect.y;
            } else if (p1.y > rect.y+rect.height) {
                n1.y = rect.y+rect.height;
            }
            if (p2.y < rect.y) {
                n2.y = rect.y;
            } else if (p2.y > rect.y+rect.height) {
                n2.y = rect.y+rect.height;
            }
            return true;
        }
        if (p1.y == p2.y) {
            // horizontal line
            if (p1.x < rect.x) {
                n1.x = rect.x;
            } else if (p1.x > rect.x+rect.width) {
                n1.x = rect.x+rect.width;
            }
            if (p2.x < rect.x) {
                n2.x = rect.x;
            } else if (p2.x > rect.x+rect.width) {
                n2.x = rect.x+rect.width;
            }
            return true;
        }
        if (isIntersectVerticalLine(p1, p2, rect.x, rect.y, rect.y+rect.height, n1)) {
            if (isIntersectVerticalLine(p1, p2, rect.x+rect.width, rect.y, rect.y+rect.height, n2)) {
            } else if (isIntersectHorizontalLine(p1, p2, rect.y, rect.x, rect.x+rect.width, n2)) {
            } else if (isIntersectHorizontalLine(p1, p2, rect.y+rect.height, rect.x, rect.x+rect.width, n2)) {
            } else if (isInternalPoint(rect, p1, p2, n2)) {
            } else {
                return false;
            }
            return true;
        } else if (isIntersectVerticalLine(p1, p2, rect.x+rect.width, rect.y, rect.y+rect.height, n1)) {
            if (isIntersectHorizontalLine(p1, p2, rect.y+rect.height, rect.x, rect.x+rect.width, n2)) {
            } else if (isIntersectHorizontalLine(p1, p2, rect.y, rect.x, rect.x+rect.width, n2)) {
            } else if (isInternalPoint(rect, p1, p2, n2)) {
            } else {
                return false;
            }
            return true;
        } else if (isIntersectHorizontalLine(p1, p2, rect.y, rect.x, rect.x+rect.width, n1)) {
            if (isIntersectHorizontalLine(p1, p2, rect.y+rect.height, rect.x, rect.x+rect.width, n2)) {
            } else if (isInternalPoint(rect, p1, p2, n2)) {
            } else {
                return false;
            }
            return true;
        } else if (isIntersectHorizontalLine(p1, p2, rect.y+rect.height, rect.x, rect.x+rect.width, n1)) {
            if (isInternalPoint(rect, p1, p2, n2)) {
            } else {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Draws a network links with specified bounds.
     */
    private final void drawLines(Graphics2D g, Rectangle bounds, float scale) {
        float x1, y1, x2, y2;
        Point p1 = new Point();
        Point p2 = new Point();
        Point n1 = new Point();
        Point n2 = new Point();
        g.setColor(Color.black);
        int spot_root, spot_child;
        Arrays.fill(this.draw, true);
        for (int i=0; i<this.clusters.length; i++) {
            if (this.clusters[i].length > 1) {
                spot_root = this.clusters[i][0];
                this.draw[spot_root] = false;
                x1 = (this.coords[spot_root][0])*scale+insets.left;
                y1 = (this.coords[spot_root][1])*scale+insets.top;
                for (int j=1; j<this.clusters[i].length; j++) {
                    if (this.weights != null && this.weights[i][j] < this.links_threshold)
                        continue;
                    if (this.isLinksColor)
                        g.setColor(LINKS_PALETTE[(int)((this.weights[i][j]-this.weight_min)*this.weight_scale-0.001f)]);

                    spot_child = this.clusters[i][j];
                    if (this.draw[spot_child]) {
                        x2 = (this.coords[spot_child][0])*scale+insets.left;
                        y2 = (this.coords[spot_child][1])*scale+insets.top;
                        p1.move((int)x1, (int)y1);
                        p2.move((int)x2, (int)y2);
                        if (isLineIntersects(bounds, p1, p2, n1, n2)) {
                            g.drawLine(n1.x, n1.y, n2.x, n2.y);
                        }
                    }
                }
            }
        }
    }

    /**
     * Checkes if an element with x and y coordinaties intersects a rect.
     */
    private final boolean isElementIntersects(Rectangle rect, int x, int y) {
        int X1 = x-this.elementSize.width/2;
        int Y1 = y-this.elementSize.height/2;
        int X2 = x+this.elementSize.width/2;
        int Y2 = y+this.elementSize.height/2;
        return isRectIntersects(rect, X1, Y1, X2, Y2);
    }

    /**
     * Fills in a shape with specified coordinaties.
     */
    private final void fillShape(Graphics g, int x, int y) {
        if (this.shape_type == SHAPE_RECT) {
            g.fillRect(x, y, this.elementSize.width, this.elementSize.height);
        } else {
            g.fillOval(x, y, this.elementSize.width, this.elementSize.height);
        }
    }

    /**
     * Draws a shape with specified coordinaties.
     */
    private final void drawShape(Graphics g, int x, int y) {
        drawShape(g, x, y, this.elementSize.width-1, this.elementSize.height-1);
    }

    /**
     * Draws a shape with specified bounds.
     */
    private final void drawShape(Graphics g, int x, int y, int width, int height) {
        if (this.shape_type == SHAPE_RECT) {
            g.drawRect(x, y, width, height);
        } else {
            g.drawOval(x, y, width, height);
        }
    }

    /**
     * Draws a node with specified index and coordinaties.
     */
    private final void drawNode(Graphics g, int index, int x, int y) {
        if (this.selected[index]) {
            g.setColor(this.selectionColor);
        } else {
            g.setColor(getProbeColor(index));
        }
        fillShape(g, x, y);
        if (this.isDrawBorders) {
            g.setColor(Color.black);
            drawShape(g, x, y);
        }
    }

    /**
     * Draws a network nodes with specified canvas bounds.
     */
    private final void drawShapes(Graphics2D g, Rectangle bounds, float scale) {
        float x, y;
        int spot;
        Arrays.fill(this.draw, true);
        for (int i=0; i<this.clusters.length; i++) {
            if (this.clusters[i].length > 1) {
                for (int j=0; j<this.clusters[i].length; j++) {
                    spot = this.clusters[i][j];
                    if (this.draw[spot]) {
                        this.draw[spot] = false;
                        x = this.coords[spot][0]*scale+insets.left;
                        y = this.coords[spot][1]*scale+insets.top;
                        if (isElementIntersects(bounds, (int)x, (int)y)) {
                            drawNode(g, spot, (int)x-this.elementSize.width/2, (int)y-this.elementSize.height/2);
                        }
                    }
                }

            }
        }
    }

    /**
     * Draws a node with specified index.
     */
    private final void drawSpot(Graphics g, int index) {
        float scale = getScale();
        float x = this.coords[index][0]*scale+insets.left;
        float y = this.coords[index][1]*scale+insets.top;
        drawNode(g, index, (int)x-this.elementSize.width/2, (int)y-this.elementSize.height/2);
    }

    /**
     * Draws a selected node with specified index.
     */
    private final void drawSelectedSpot(Graphics g, int index) {
        float scale = getScale();
        float x = this.coords[index][0]*scale+insets.left;
        float y = this.coords[index][1]*scale+insets.top;
        drawNode(g, index, (int)x-this.elementSize.width/2, (int)y-this.elementSize.height/2);
        g.setColor(Color.white);
        drawShape(g, (int)x-this.elementSize.width/2+1, (int)y-this.elementSize.height/2+1, this.elementSize.width-3, this.elementSize.height-3);
    }

    // store view port image
    private void onStartDrawZoom() {
        prevZoomRect.setBounds(0, 0, 0, 0);
    }

    /**
     * Draws zoom rectangle.
     */
    private void drawZoomRect(Rectangle rect) {
        Graphics2D g = (Graphics2D)getGraphics();
        g.setColor(Color.black);
        g.setXORMode(getBackground());
        g.drawRect(prevZoomRect.x, prevZoomRect.y, prevZoomRect.width-1, prevZoomRect.height-1);
        prevZoomRect.setBounds(rect);
        g.drawRect(rect.x, rect.y, rect.width-1, rect.height-1);
        g.setPaintMode();
        g.dispose();
    }

    /**
     * Checkes if a string intersects specified rectangle.
     */
    private final boolean isLabelIntersects(FontMetrics fm, Rectangle rect, String str, int x, int y) {
        int width  = fm.stringWidth(str);
        int height = fm.getHeight();
        return isRectIntersects(rect, x, y, x+width, y+height);
    }

    /**
     * Calculates max label width.
     */
    private int getMaxLabelWidth() {
        if (this.isGenes && this.labelIndex < 0)
            return 0;
        Graphics2D g = (Graphics2D)getGraphics();
        if (this.isAntiAliasing) {//Anti-aliasing is on
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        FontMetrics fm = g.getFontMetrics();
        String str;
        int max = 0;
        for (int i=0; i<this.clusters.length; i++) {
            if (this.clusters[i].length > 1) {
                for (int j=0; j<this.clusters[i].length; j++) {
                    str = isGenes ? this.data.getElementAttribute(this.clusters[i][j], this.labelIndex) : this.data.getSampleName(this.clusters[i][j]);
                    if (str != null) {
                        max = Math.max(max, fm.stringWidth(str));
                    }
                }
            }
        }
        return max;
    }

    /**
     * Draws nodes labels.
     */
    private final void drawLabels(Graphics2D g, Rectangle bounds, float scale) {
        if (this.isGenes && this.labelIndex < 0)
            return;
        float x, y;
        String str;
        g.setColor(this.labelColor);
        FontMetrics fm = g.getFontMetrics();
        int spot;
        Arrays.fill(this.draw, true);
        for (int i=0; i<this.clusters.length; i++) {
            if (this.clusters[i].length > 1) {
                for (int j=0; j<this.clusters[i].length; j++) {
                    spot = this.clusters[i][j];
                    if (this.draw[spot]) {
                        this.draw[spot] = false;
                        x = (this.coords[spot][0])*scale+this.insets.left+this.elementSize.width/2+10;
                        y = (this.coords[spot][1])*scale+this.insets.top+this.elementSize.height/2;
                        str = isGenes ? this.data.getElementAttribute(spot, this.labelIndex) : this.data.getSampleName(spot);
                        if (isLabelIntersects(fm, bounds, str, (int)x, (int)y))
                            g.drawString(str, (int)x, (int)y);
                    }
                }

            }
        }
    }

    /**
     * @return a color for a specified probe.
     */
    private final Color getProbeColor(int probe) {
        Color color = isGenes ? this.data.getProbeColor(probe) : this.data.getExperimentColor(probe);
        return color == null ? Color.gray : color;
    }

    /**
     * @return this component.
     */
    public JComponent getContentComponent() {
        return this;
    }

    /**
     * @return null.
     */
    public JComponent getHeaderComponent() {
        return null;
    }

    /**
     * Updates some attributies when the viewer is selected.
     */
    public void onSelected(IFramework framework) {
        this.framework = framework;
        this.data = framework.getData();
        IDisplayMenu menu = framework.getDisplayMenu();
        this.isDrawBorders = menu.isDrawingBorder();
        this.isAntiAliasing = menu.isAntiAliasing();
        this.labelIndex = menu.getLabelIndex();
        setElementSize(menu.getElementSize());
        updateSize();
    }

    /**
     * Sets the viewer data.
     */
    public void onDataChanged(IData data) {
        this.data = data;
    }

    /**
     * Updates some attributies when the display menu is changed.
     */
    public void onMenuChanged(IDisplayMenu menu) {
        this.isDrawBorders = menu.isDrawingBorder();
        this.isAntiAliasing = menu.isAntiAliasing();
        this.labelIndex = menu.getLabelIndex();
        setElementSize(menu.getElementSize());
        updateSize();
    }

    /**
     * Updates the viewer sizes.
     */
    private void updateSize() {
        int oldWidth  = getPreferredSize().width  - this.insets.right-this.insets.left;
        int oldHeight = getPreferredSize().height - this.insets.top-this.insets.bottom;
        this.insets.left = this.elementSize.width/2+10;
        this.insets.right = getMaxLabelWidth()+this.elementSize.width/2+10;
        this.insets.top = this.insets.bottom = this.elementSize.height/2+10;
        setSizes(oldWidth+this.insets.left+this.insets.right, oldHeight+this.insets.top+this.insets.bottom);
    }

    public void onDeselected() {}
    public void onClosed() {}

    /**
     * Sets a node size.
     */
    private void setElementSize(Dimension newSize) {
        if (newSize.equals(this.elementSize)) {
            return;
        }
        this.elementSize = new Dimension(newSize);
        setFont(new Font("monospaced", Font.BOLD, newSize.height));
    }

    /**
     * @return null.
     */
    public BufferedImage getImage() {
        return null;
    }

    /**
     * Creates a popup menu.
     */
    private JPopupMenu createJPopupMenu(Listener listener) {
        JPopupMenu popup = new JPopupMenu();
        addMenuItems(popup, listener);
        return popup;
    }

    /**
     * Adds the viewer specific menu items.
     */
    private void addMenuItems(JPopupMenu menu, Listener listener) {
        JMenuItem menuItem;
        menuItem = new JMenuItem("Zoom in", GUIFactory.getIcon("zoom_in.gif"));
        menuItem.setActionCommand(ZOOM_IN_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("Zoom out", GUIFactory.getIcon("zoom_out.gif"));
        menuItem.setActionCommand(ZOOM_OUT_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("Show all", GUIFactory.getIcon("show_all.gif"));
        menuItem.setActionCommand(SHOW_ALL_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menu.addSeparator();

        JMenu select_menu = new JMenu("Select");

        menuItem = new JMenuItem("Feature Degree...");
        menuItem.setActionCommand(SELECT_BY_DEGREE_CMD);
        menuItem.addActionListener(listener);
        select_menu.add(menuItem);

        menuItem = new JMenuItem("Element ID...");
        menuItem.setActionCommand(SELECT_BY_GENE_ID_CMD);
        menuItem.addActionListener(listener);
        select_menu.add(menuItem);

        menu.add(select_menu);

        JMenu links_menu = new JMenu("Links");

        menuItem = new JMenuItem("Threshold...");
        menuItem.setActionCommand(LINKS_THRESHOLD_CMD);
        menuItem.addActionListener(listener);
        links_menu.add(menuItem);

        menuItem = new JCheckBoxMenuItem("Color");
        menuItem.setSelected(true);
        menuItem.setActionCommand(LINKS_COLOR_CMD);
        menuItem.addActionListener(listener);
        links_menu.add(menuItem);

        menu.add(links_menu);

        menuItem = new JMenuItem("Deselect");
        menuItem.setActionCommand(DESELECT_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("Set Public Cluster...");
        menuItem.setActionCommand(SET_PUBLIC_CLUSTER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("Find Clusters");
        menuItem.setActionCommand(FIND_CLUSTER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menu.addSeparator();

        JMenu shape_menu = new JMenu("Shape");
        ButtonGroup group = new ButtonGroup();

        menuItem = new JRadioButtonMenuItem("Rectangle");
        menuItem.setSelected(true);
        menuItem.setActionCommand(SHAPE_RECT_CMD);
        menuItem.addActionListener(listener);
        shape_menu.add(menuItem);
        group.add(menuItem);

        menuItem = new JRadioButtonMenuItem("Oval");
        menuItem.setActionCommand(SHAPE_OVAL_CMD);
        menuItem.addActionListener(listener);
        shape_menu.add(menuItem);
        group.add(menuItem);

        menu.add(shape_menu);

        /*
        menuItem = new JMenuItem("Circular Layout");
        menuItem.setActionCommand(CIRCULAR_LAYOUT_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("Random Layout");
        menuItem.setActionCommand(RANDOM_LAYOUT_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("Debug Layout");
        menuItem.setActionCommand(DEBUG_LAYOUT_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);*/

        menu.addSeparator();

        menuItem = new JMenuItem("Set selection...");
        menuItem.setActionCommand(SET_SELECTION_COLOR_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("Set labels...");
        menuItem.setActionCommand(SET_LABEL_COLOR_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("Set background...");
        menuItem.setActionCommand(SET_BACKGROUND_COLOR_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
    }

    /**
     * Returns a menu item by specified action command.
     * @return null if no one item was found.
     */
    private JMenuItem getJMenuItem(JPopupMenu menu, String command) {
        Component[] components = menu.getComponents();
        for (int i=0; i<components.length; i++) {
            if (components[i] instanceof JMenu) {
                JMenuItem item = getJMenuItem(((JMenu)components[i]).getPopupMenu(), command);
                if (item != null)
                    return item;
            }
            if (components[i] instanceof JMenuItem) {
                if (((JMenuItem)components[i]).getActionCommand().equals(command))
                    return(JMenuItem)components[i];
            }
        }
        return null;
    }

    /**
     * Sets a menu item state.
     */
    private void setEnableMenuItem(String command, boolean enable) {
        JMenuItem item = getJMenuItem(this.popup, command);
        if (item == null) {
            return;
        }
        item.setEnabled(enable);
    }

    /**
     * Shows all network nodes.
     */
    private void onShowAll() {
        setSizes(300+insets.left+insets.right, 300+insets.top+insets.bottom);
        revalidate();
    }

    /**
     * Zoom in a network
     */
    private void onZoomIn() {
        zoom(ZOOM_COEFFICIENT);
    }

    /**
     * Zoom out a network
     */
    private void onZoomOut() {
        zoom(1/ZOOM_COEFFICIENT);
        if (getPreferredSize().width < getWidth() || getPreferredSize().height < getHeight()) {
            repaint();                                                                           
        }
    }

    /**
     * Zoom a network with specified coefficient.
     */
    private void zoom(float coefficient) {
        int newAreaWidth = (int)((getPreferredSize().width-insets.left-insets.right)*coefficient);
        int newAreaHeight = (int)((getPreferredSize().height-insets.top-insets.bottom)*coefficient);
        int viewWidth  = getParent().getWidth();
        int viewHeight = getParent().getHeight();
        int x = Math.abs(getX())-insets.left+(int)((float)viewWidth*(coefficient-1)/(coefficient*2f));
        int y = Math.abs(getY())-insets.top+(int)((float)viewHeight*(coefficient-1)/(coefficient*2f));
        setSizes(newAreaWidth+insets.left+insets.right, newAreaHeight+insets.top+insets.bottom);
        framework.setContentLocation((int)((float)x*coefficient+insets.left), (int)((float)y*coefficient+insets.top));
    }

    /**
     * Zoom a network to specified rectangle.
     */
    private void zoom(Rectangle rect) {
        if (rect == null || rect.width < 10 || rect.height < 10) {
            drawZoomRect(new Rectangle());
            return;
        }
        int viewWidth  = getParent().getWidth();
        int viewHeight = getParent().getHeight();
        float coefficient = Math.min((float)viewWidth/(float)rect.width, (float)viewHeight/(float)rect.height);
        float width = (getPreferredSize().width-insets.left-insets.right)*coefficient;
        float height = (getPreferredSize().height-insets.top-insets.bottom)*coefficient;
        if (width > MAX_CONTENT_SIZE || height > MAX_CONTENT_SIZE) {
            drawZoomRect(new Rectangle());
            return;
        }
        int newAreaWidth = (int)width;
        int newAreaHeight = (int)height;
        float x = rect.x-insets.left;
        float y = rect.y-insets.top;
        setSizes((int)(newAreaWidth+insets.left+insets.right), (int)(newAreaHeight+insets.top+insets.bottom));
        framework.setContentLocation((int)(x*coefficient+insets.left), (int)(y*coefficient+insets.top));
    }

    /**
     * Sets the viewer sizes.
     */
    private void setSizes(int width, int height) {
        setSize(width, height);
        setPreferredSize(new Dimension(width, height));
        revalidate();
    }

    /**
     * Selects nodes with specified degrees.
     */
    private void selectByDegree(String condition, int min_degree, int max_degree) {
        clearSelected(this.selected);
        int cond = 0;
        if (condition.equals(RelNetSelectionDlg.CONDITION_EQUAL_TO)) {
            cond = 1;
        } else if (condition.equals(RelNetSelectionDlg.CONDITION_LESS_THAN)) {
            cond = 2;
        } else if (condition.equals(RelNetSelectionDlg.CONDITION_BETWEEN)) {
            cond = 3;
        }
        for (int i=0; i<this.clusters.length; i++) {
            switch (cond) {
            case 0:
                if (this.clusters[i].length-1 > max_degree) {
                    this.selected[this.clusters[i][0]] = true;
                }
                break;
            case 1:
                if (this.clusters[i].length-1 == max_degree) {
                    this.selected[this.clusters[i][0]] = true;
                }
                break;
            case 2:
                if (this.clusters[i].length-1 < min_degree) {
                    this.selected[this.clusters[i][0]] = true;
                }
                break;
            case 3:
                int value = this.clusters[i].length-1;
                if (value < max_degree && value > min_degree) {
                    this.selected[this.clusters[i][0]] = true;
                }
                break;
            }
        }
        repaint();
    }

    /**
     * Selects nodes with specified gene id.
     */
    private void selectByGeneID(String condition, String ID) {
        clearSelected(this.selected);
        boolean like = condition.equals(RelNetSelectionDlg.CONDITION_LIKE);
        String str;
        boolean value;
        for (int i=0; i<this.clusters.length; i++) {
            if (this.clusters[i].length > 1) {
                for (int j=0; j<this.clusters[i].length; j++) {
                    str = isGenes ? this.data.getUniqueId(this.clusters[i][j]) : this.data.getSampleName(this.clusters[i][j]);
                    if (str != null) {
                        if (like) {
                            value = str.indexOf(ID) != -1;
                        } else {
                            value = str.equals(ID);
                        }
                        this.selected[this.clusters[i][j]] = value;
                    }
                }
            }
        }
        repaint();
    }

    /*private void onCircularLayout() {
        this.coords = RelevanceNetworkLayout.doLayout(clusters, null, RelevanceNetworkLayout.CIRCULAR_LAYOUT);
        repaint();
    }

    private void onRandomLayout() {
        this.coords = RelevanceNetworkLayout.doLayout(clusters, null, RelevanceNetworkLayout.RANDOM_LAYOUT);
        repaint();
    }

    private void onDebugLayout() {
        this.coords = RelevanceNetworkLayout.doLayout(clusters, null, RelevanceNetworkLayout.DEBUG_LAYOUT);
        repaint();
    }*/

    /**
     * Sets a background color.
     */
    private void onSetBackgroundColor() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        Color newColor = JColorChooser.showDialog(frame, "Choose color", getBackground());
        if (newColor != null) {
            setBackground(newColor);
        }
    }

    /**
     * Sets a selection color.
     */
    private void setSelectionColor(Color color) {
        this.selectionColor = color;
        repaint();
    }

    /**
     * Sets labels color.
     */
    private void setLabelColor(Color color) {
        this.labelColor = color;
        repaint();
    }

    /**
     * Sets a selection color.
     */
    private void onSetSelectionColor() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        Color newColor = JColorChooser.showDialog(frame, "Choose selection color", getBackground());
        if (newColor != null) {
            setSelectionColor(newColor);
        }
    }

    /**
     * Sets labels color.
     */
    private void onSetLabelColor() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        Color newColor = JColorChooser.showDialog(frame, "Choose label color", getBackground());
        if (newColor != null) {
            setLabelColor(newColor);
        }
    }

    /**
     * Selects nodes with the user defined degrees.
     */
    private void onDegreeSelection() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        RelNetSelectionDlg dialog = new RelNetSelectionDlg(frame, RelNetSelectionDlg.DEGREE_TYPE);
        if (dialog.showModal() == JOptionPane.OK_OPTION) {
            selectByDegree(dialog.getCondition(), dialog.getMinDegree(), dialog.getMaxDegree());
        }
    }

    /**
     * Selects nodes with the user defined gene id.
     */
    private void onGeneIDSelection() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        RelNetSelectionDlg dialog = new RelNetSelectionDlg(frame, RelNetSelectionDlg.GENEID_TYPE);
        if (dialog.showModal() == JOptionPane.OK_OPTION) {
            selectByGeneID(dialog.getCondition(), dialog.getGeneID());
        }
    }

    /**
     * Shows links with the user specified threshold.
     */
    private void onLinksThreshold() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        String value = (String)JOptionPane.showInputDialog(frame, "Enter in links threshold (from 0 to 1).", "Input", JOptionPane.OK_CANCEL_OPTION, null, null, String.valueOf(this.links_threshold));
        if (value == null || value.equals(""))
            return;
        try {
            float threshold = Float.parseFloat(value);
            if (threshold < 0 || threshold > 1)
                throw new NumberFormatException("value must be between 0 and 1.");
            this.links_threshold = threshold;
            repaint();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Illegal number: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onLinksColor() {
        JMenuItem item = getJMenuItem(this.popup, LINKS_COLOR_CMD);
        this.isLinksColor = item.isSelected();
        repaint();
    }

    /**
     * Deselects all nodes.
     */
    private void onDeselect() {
        clearSelected(this.selected);
        repaint();
    }

    // Scrollable interface implementation.

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.VERTICAL) {
            return this.elementSize.height;
        } else {
            return this.elementSize.width;
        }
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.VERTICAL) {
            return visibleRect.height;
        } else {
            return visibleRect.width;
        }
    }

    public boolean getScrollableTracksViewportWidth() {
        if (getParent() instanceof JViewport) {
            return(((JViewport)getParent()).getWidth() > getPreferredSize().width);
        }
        return false;
    }

    public boolean getScrollableTracksViewportHeight() {
        if (getParent() instanceof JViewport) {
            return(((JViewport)getParent()).getHeight() > getPreferredSize().height);
        }
        return false;
    }

    /**
     * Searches a node index by specified coordinaties.
     * @return -1 if no one node was found.
     */
    private int findSpot(float X, float Y, Rectangle2D.Float rect) {
        float scale  = getScale();
        // element rect
        float rectWidth = (float)elementSize.getWidth()/scale;
        float rectHeight = (float)elementSize.getHeight()/scale;
        for (int i=0; i<this.clusters.length; i++) {
            if (this.clusters[i].length > 1) {
                for (int j=0; j<this.clusters[i].length; j++) {
                    rect.setRect(coords[this.clusters[i][j]][0]-rectWidth/2f, coords[this.clusters[i][j]][1]-rectHeight/2f, rectWidth, rectHeight);
                    if (rect.contains(X, Y)) {
                        return this.clusters[i][j];
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Deselects a specified node.
     */
    private void unselectSpot(int spotIndex) {
        // restore status text
        this.framework.setStatusText(this.status);
        Graphics g = getContentComponent().getGraphics();
        drawSpot(g, spotIndex);
        g.dispose();
        hideTipWindow();
    }

    /**
     * Selects a specified node.
     */
    private void selectSpot(int spotIndex) {
        this.status = framework.getStatusText(); // remember original status
        if (this.isGenes)
            this.framework.setStatusText("Gene Id: "+data.getUniqueId(spotIndex)+" Gene Name: "+data.getGeneName(spotIndex));
        else
            this.framework.setStatusText("Experiment: "+data.getSampleName(spotIndex));
        Graphics g = getContentComponent().getGraphics();
        drawSelectedSpot(g, spotIndex);
        g.dispose();
        showTipWindow(spotIndex);
    }

    private void showTipWindow(int spot) {
        if (this.isGenes && this.labelIndex < 0)
            return;
        Component content = getContentComponent();
        String text = this.isGenes ? this.data.getElementAttribute(spot, this.labelIndex) : data.getSampleName(spot);

        JToolTip tooltip = new JToolTip();
        tooltip.setTipText(text);
        Dimension size = tooltip.getPreferredSize();

        float scale = getScale();
        float x = (this.coords[spot][0])*scale+this.insets.left+this.elementSize.width/2+10;
        float y = (this.coords[spot][1])*scale+this.insets.top+(float)(this.elementSize.height-size.getHeight())/2;
        
        Point screenLocation = content.getLocationOnScreen();

        this.tipWindow.getContentPane().add(tooltip, BorderLayout.CENTER);
        this.tipWindow.setLocation((int)(screenLocation.x+x), (int)(screenLocation.y+y));
        this.tipWindow.pack();
        this.tipWindow.setVisible(true);
    }

    private void hideTipWindow() {
        this.tipWindow.getContentPane().removeAll();
        this.tipWindow.setVisible(false);
    }

    private JWindow createTipWindow() {
        return new JWindow(JOptionPane.getFrameForComponent(getContentComponent()) );
    }

    /**
     * Sets rectangle shape type.
     */
    private void onShapeRect() {
        setShapeType(SHAPE_RECT);
    }

    /**
     * Sets oval shape type.
     */
    private void onShapeOval() {
        setShapeType(SHAPE_OVAL);
    }

    /**
     * Sets specified shape type.
     */
    private void setShapeType(int type) {
        this.shape_type = type;
        repaint();
    }

    /**
     * @return true, if there is a selected node.
     */
    private boolean hasSelected() {
        for (int i=0; i<this.selected.length; i++) {
            if (this.selected[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the user defined public color for selected nodes.
     */
    private void onSetPublicCluster() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        Color color = JColorChooser.showDialog(frame, "Choose color", Color.green);
        if (color == null) {
            return;
        }
        int count = 0;
        for (int i=0; i<this.selected.length; i++) {
            if (this.selected[i]) {
                count++;
            }
        }
        if (count == 0) {
            return;
        }
        int[] probes = new int[count];
        int pos = 0;
        for (int i=0; i<this.selected.length; i++) {
            if (this.selected[i]) {
                probes[pos] = i;
                pos++;
            }
        }
        if (this.isGenes)
            this.data.setProbesColor(probes, color);
        else
            this.data.setExperimentColor(probes, color);

        clearSelected(this.selected);
        repaint();
    }

    /**
     * Shows list of clusters for specified node.
     */
    private void onFindCluster(int index) {
        ArrayList info = new ArrayList();
        for (int i=0; i<this.clusters.length; i++) {
            if (this.clusters[indices[i]].length > 1) {
                for (int j=0; j<this.clusters[indices[i]].length; j++) {
                    if (index == this.clusters[indices[i]][j]) {
                        info.add(new String("Cluster "+String.valueOf(indices[i]+1)+" ("+clusters[indices[i]].length+")"));
                        continue;
                    }
                }
            }
        }
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        String nodeName = this.isGenes ? (this.data.getUniqueId(index)+ " ("+this.data.getGeneName(index)+")") : this.data.getSampleName(index);
        String title = "Clusters for node: "+nodeName;
        RelNetClusterList list = new RelNetClusterList(frame, title, info.toArray(new String[info.size()]));
        list.showModal();
    }

    /**
     * The class to listen to mouse and menu actions events.
     */
    private class Listener extends MouseAdapter implements ActionListener, MouseMotionListener {

        private boolean isSpotSelected = false;
        private int spotIndex = -1;
        private Rectangle2D.Float spotRect = new Rectangle2D.Float();

        private boolean isZoomStarted = false;
        private Rectangle zoomRect = new Rectangle();
        private Point zoomPoint = new Point(); // starting point to draw zoom rect

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals(SET_BACKGROUND_COLOR_CMD)) {
                onSetBackgroundColor();
            } else if (command.equals(SET_SELECTION_COLOR_CMD)) {
                onSetSelectionColor();
            } else if (command.equals(SET_LABEL_COLOR_CMD)) {
                onSetLabelColor();
            } else if (command.equals(ZOOM_IN_CMD)) {
                onZoomIn();
            } else if (command.equals(ZOOM_OUT_CMD)) {
                onZoomOut();
            } else if (command.equals(SHOW_ALL_CMD)) {
                onShowAll();
            } else if (command.equals(CIRCULAR_LAYOUT_CMD)) {
                //onCircularLayout();
            } else if (command.equals(RANDOM_LAYOUT_CMD)) {
                //onRandomLayout();
            } else if (command.equals(DEBUG_LAYOUT_CMD)) {
                //onDebugLayout();
            } else if (command.equals(SELECT_BY_DEGREE_CMD)) {
                onDegreeSelection();
            } else if (command.equals(SELECT_BY_GENE_ID_CMD)) {
                onGeneIDSelection();
            } else if (command.equals(LINKS_THRESHOLD_CMD)) {
                onLinksThreshold();
            } else if (command.equals(LINKS_COLOR_CMD)) {
                onLinksColor();
            } else if (command.equals(DESELECT_CMD)) {
                onDeselect();
            } else if (command.equals(SHAPE_OVAL_CMD)) {
                onShapeOval();
            } else if (command.equals(SHAPE_RECT_CMD)) {
                onShapeRect();
            } else if (command.equals(SET_PUBLIC_CLUSTER_CMD)) {
                onSetPublicCluster();
            } else if (command.equals(FIND_CLUSTER_CMD)) {
                onFindCluster(spotIndex);
            }
        }

        public void mouseDragged(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                return;
            }
            if (isZoomStarted) {
                processDrawZoom(e.getX(), e.getY());
            } else {
                startDrawZoom(e.getX(), e.getY());
            }
        }

        private void startDrawZoom(int x, int y) {
            isZoomStarted = true;
            zoomPoint.setLocation(x, y);
            onStartDrawZoom();
        }

        private void processDrawZoom(int x, int y) {
            int X = x;
            if (x > zoomPoint.x) {
                X = zoomPoint.x;
            }
            int Y = y;
            if (y > zoomPoint.y) {
                Y = zoomPoint.y;
            }
            zoomRect.setBounds(X, Y, Math.abs(x-zoomPoint.x), Math.abs(y-zoomPoint.y));
            drawZoomRect(zoomRect);
        }

        private void stopDrawZoom() {
            isZoomStarted = false;
            zoom(zoomRect);
        }

        public void mouseMoved(MouseEvent e) {
            if (popup.isVisible()) {
                return;
            }
            float scale  = getScale();
            float X = (float)(e.getX()-insets.left)/scale;
            float Y = (float)(e.getY()-insets.top)/scale;
            if (isSpotSelected) {
                if (spotRect.contains(X, Y)) {
                    return;
                } else {
                    isSpotSelected = false;
                    unselectSpot(spotIndex);
                    spotIndex = -1;
                }
            }
            if ((spotIndex = findSpot(X, Y, spotRect)) != -1) {
                selectSpot(spotIndex);
                isSpotSelected = true;
            }
        }

        public void mouseClicked(MouseEvent event) {
            if (SwingUtilities.isRightMouseButton(event)) {
                return;
            }
            if (event.isShiftDown()) {
                // zooming process
            } else {
                // clickable spot feature
                if (isSpotSelected) {
                    if (RelevanceNetworkViewer.this.isGenes)
                        framework.displaySlideElementInfo(0, spotIndex);
                    else 
                        framework.displaySingleArrayViewer(spotIndex);
                }
            }
        }

        public void mouseReleased(MouseEvent event) {
            if (SwingUtilities.isRightMouseButton(event)) {
                maybeShowPopup(event);
            } else {
                if (isZoomStarted) {
                    stopDrawZoom();
                }
            }
        }

        private void maybeShowPopup(MouseEvent e) {
            if (!e.isPopupTrigger()) {
                return;
            }
            setEnableMenuItem(ZOOM_OUT_CMD, getPreferredSize().width-insets.left-insets.right >= 300 || getPreferredSize().height-insets.top-insets.bottom >= 300);
            setEnableMenuItem(ZOOM_IN_CMD, Math.max(getWidth()+insets.left+insets.right, getHeight()+insets.top+insets.bottom) < MAX_CONTENT_SIZE/ZOOM_COEFFICIENT-1);
            setEnableMenuItem(SET_PUBLIC_CLUSTER_CMD, hasSelected());
            setEnableMenuItem(FIND_CLUSTER_CMD, isSpotSelected);
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
