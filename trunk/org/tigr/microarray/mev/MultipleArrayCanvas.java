/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: MultipleArrayCanvas.java,v $
 * $Revision: 1.2 $
 * $Date: 2003-12-08 18:47:59 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.AlphaComposite;

import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IViewer;

public class MultipleArrayCanvas extends JPanel implements IViewer {
    
    private static final int TRACE_SPACE = 50;
    
    private MultipleArrayHeader header;
    private Thumbnail thumbnail;
    private boolean isShowThumbnail = false;
    
    private IFramework framework;
    private IData data;
    private Insets insets;
    
    private int maxLabelWidth = 0;
    
    // display options
    private int paletteStyle;
    private boolean isGRScale;
    private boolean isDrawBorders;
    private boolean isTracing;
    private boolean isAntiAliasing = true;
    private Dimension elementSize = new Dimension();
    private int labelIndex = -1;
    // chain members
    private boolean drawChain = false;
    private int[] chain; // probe indices
    
    //micro array parameters
    private int features;
    private int probes;
    private float maxCY3;
    private float maxCY5;
    private float maxRatio;
    private float minRatio;
    
    public static Color missingColor = Color.gray;
    private BufferedImage negColorImage;
    private BufferedImage posColorImage;
    private int xOffset = 10;
    
    /**
     * Constructs a <code>MultipleArrayCanvas</code> with specified
     * reference to a framework and insets.
     */
    public MultipleArrayCanvas(IFramework framework, Insets insets) {
	this.framework = framework;
	setBackground(Color.white);
	this.insets = insets;
	header = new MultipleArrayHeader(insets, TRACE_SPACE);
	Listener listener = new Listener();
	addMouseListener(listener);
	addMouseMotionListener(listener);
	addKeyListener(listener);
	this.thumbnail = new Thumbnail(framework, listener);
        this.negColorImage = framework.getDisplayMenu().getNegativeGradientImage();
        this.posColorImage = framework.getDisplayMenu().getPositiveGradientImage();
        this.header.setNegativeAndPositiveColorImages(this.negColorImage, this.posColorImage);
    }
    
    /**
     * Overriden to have focus. (deprecated as of 1.4, use isFocusable()
     */
    public boolean isFocusTraversable() {
	return true;
    }
    
        /**
     * Overriden to have focus.
     */
    public boolean isFocusable() {
	return true;
    }
    
    /**
     * @return this component.
     */
    public JComponent getContentComponent() {
	return this;
    }
    
    /**
     * Returns the viewer header.
     */
    public JComponent getHeaderComponent() {
	return header;
    }
    
    /**
     * Updates the viewer when it has been selected.
     */
    public void onSelected(IFramework framework) {
	this.framework = framework;
	this.data = framework.getData();
	IDisplayMenu menu = framework.getDisplayMenu();
	this.maxCY3 = menu.getMaxCY3Scale();
	this.maxCY5 = menu.getMaxCY5Scale();
	this.maxRatio = menu.getMaxRatioScale();
	this.minRatio = menu.getMinRatioScale(); 
        this.header.setMinAndMaxRatios(this.minRatio, this.maxRatio);
	header.setData(data);
	onMenuChanged(menu);
	onDataChanged(data);
	if (this.isShowThumbnail) {
	    this.thumbnail.show();
	}
    }
    
    /**
     * Updates the viewer when data is changed.
     */
    public void onDataChanged(IData data) {
	this.data = data;
	features = data.getFeaturesCount();
	probes   = data.getFeaturesSize();
	setDrawChain(false);
	updateSize();
	header.setData(data);
//        header.updateSize();
	//header.setContentWidth(getSize().width);
     //   header.updateSize();
	thumbnail.onDataChanged(data);
    }
    
    /**
     * Updates the viewer when the framework display menu is changed.
     */
    public void onMenuChanged(IDisplayMenu menu) {
	paletteStyle = menu.getPaletteStyle();
	isGRScale = menu.isGRScale();
	isDrawBorders = menu.isDrawingBorder();
        isTracing = menu.isTracing();
	Dimension newSize = menu.getElementSize();
        elementSize = new Dimension(newSize);
	this.maxCY3 = menu.getMaxCY3Scale();
	this.maxCY5 = menu.getMaxCY5Scale();
	this.maxRatio = menu.getMaxRatioScale();
	this.minRatio = menu.getMinRatioScale();
        this.header.setMinAndMaxRatios(this.minRatio, this.maxRatio);
        this.negColorImage = menu.getNegativeGradientImage();
        this.posColorImage = menu.getPositiveGradientImage();
        this.header.setNegativeAndPositiveColorImages(this.negColorImage, this.posColorImage);
        updateSize();  //first set size of viewer
        isAntiAliasing = menu.isAntiAliasing();
	labelIndex = menu.getLabelIndex();
	setFont(new Font("monospaced", Font.BOLD, elementSize.height));
	updateSize();
        header.setTracing(isTracing);
        header.setElementWidth(elementSize.width);
    //    header.setContentWidth(getSize().width);//pass current width
    //    header.updateSize();  // NOW update header size
	this.thumbnail.onMenuChanged(menu);
    }
    
    /**
     * Hides a thumbnail when the viewer is deselected.
     */
    public void onDeselected() {
	this.thumbnail.hide();
    }
    
    /**
     * Disposes a thumbnail when the framework is going to be closed.
     */
    public void onClosed() {
	this.thumbnail.dispose();
    }
    
    /**
     * Shows a thumbnail.
     */
    public void onShowThumbnail() {
	this.isShowThumbnail = true;
	this.thumbnail.show();
    }
    
    /**
     * @return true if it's possible to show a thumbnail.
     */
    public boolean isThumbnailEnabled() {
	return this.data.getFeaturesCount() > 0;
    }
    
    /**
     * @return null.
     */
    public BufferedImage getImage() {
	return null;
    }
    
    /**
     * Updates the viewer size.
     */
    private void updateSize() {
	updateMaxLabelWidth();
	int width = insets.left + getXSize() + insets.right + getMaxLabelWidth() + 20;
	int height = insets.top + getYSize() + insets.bottom;
	setSize(width, height);
	setPreferredSize(new Dimension(width, height));
        this.header.setContentWidth(this.getSize().width);
    }
    
    /**
     * Paints the viewer content into specified graphics.
     */
    public void paint(Graphics g1D) {
        g1D.setColor(Color.white);
	super.paint(g1D);
        if(this.elementSize.getHeight() < 1)
            return;
        g1D.setColor(Color.black);
	if (features == 0 || framework == null) { // empty data
	    return;
	}
	Graphics2D g2D = (Graphics2D)g1D;
	if (isAntiAliasing) {//Anti-aliasing is on
	    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	    g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}
	drawColumns(g2D);
    }
    
    /**
     * Draws microarrays columns into a specified graphics.
     */
    private void drawColumns(Graphics2D g) {
	
	Rectangle bounds = g.getClipBounds();
	final int top = getTopIndex(bounds.y);
	final int bottom = getBottomIndex(bounds.y+bounds.height, probes);
	final int left = getLeftIndex(bounds.x);
	final int right = getRightIndex(bounds.x+bounds.width, features);

	for (int column = left; column < right; column++) {
	    drawColumn(g, column, top, bottom);
	}
	if (isDrawChain()) {
	    drawChain(g, Color.magenta, getChain(), left, right);
	}
	if ((right >= features) && (labelIndex >= 0)) {
	    drawLabels(g, top, bottom);
	}
    }
    
    /**
     * Draws a specified column.
     */
    private void drawColumn(Graphics2D g, int column, final int top, final int bottom) {
	int[] indices = data.getSortedIndices(column);
	for (int row = top; row < bottom; row++) {
	    drawSlideDataElement(g, row, column, indices);
	}
    }
    
    private boolean isMissingValue(int row, int column) {
	boolean missing = false;
	FloatMatrix matrix = data.getExperiment().getMatrix();
	if (Float.isNaN(matrix.get(row, column))) {
	    missing = true;
	}
	return missing;
    }
    
    /**
     * Draws an element rectangle to specified row and column.
     */
    private void drawSlideDataElement(Graphics g, final int row, final int column, int[] indices) {
	
	float cy3 = data.getCY3(column, indices[row]);
	float cy5 = data.getCY5(column, indices[row]);
	
	int x, y, width, height;
	
	switch (paletteStyle) {
	    case IDisplayMenu.OVERLAY:
		x = insets.left + column*(elementSize.width + getSpacing());
		y = insets.top + row*elementSize.height;
		Color color = getOverlayColor(cy3, cy5);//new Color(getRedValue(cy5), getGreenValue(cy3), 0);
                g.setColor(color);
                g.fillRect(x, y, elementSize.width, elementSize.height);
                break;
	    case IDisplayMenu.RATIOSPLIT:
		float ratio = data.getRatio(column, indices[row], IData.LOG);
		Color holdColor;
		g.setColor(getColor(ratio));
		g.fillRect(insets.left+column*(elementSize.width+getSpacing()),
		insets.top + row*elementSize.height,
		elementSize.width, elementSize.height);
		break;
	    case IDisplayMenu.GREENRED:
		if (cy3 <= 0 && cy5 <= 0 ) {
		    g.setColor(missingColor);
		    g.fillRect(insets.left+column*(elementSize.width+getSpacing()),
		    insets.top + row*elementSize.height,
		    elementSize.width, elementSize.height);
		} else {
		    int adjust = (int)(elementSize.width*cy3/(cy3+cy5));
		    //g.setColor(new Color(0, getGreenValue(cy3), 0));
		    g.setColor(this.getNegativeColor(cy3));
                    g.fillRect(insets.left+column*(elementSize.width+getSpacing()),
		    insets.top + row*elementSize.height,
		    adjust, elementSize.height);
                    g.setColor(this.getPositiveColor(cy5));
		    g.fillRect(insets.left+column*(elementSize.width+getSpacing())+adjust,
		    insets.top + row*elementSize.height,
		    elementSize.width-adjust, elementSize.height);
		}
		break;
	}
	if (isDrawBorders) {
	    g.setColor(Color.black);
	    g.drawRect(getXPos(column), getYPos(row) + insets.top, elementSize.width -1, elementSize.height -1);
	}
    }
    
    /**
     * Calculates color for passed value.
     */
    private Color getColor(float value) {
        if (Float.isNaN(value)) {
            return missingColor;
        }
        float maximum = value < 0 ? this.minRatio : this.maxRatio;
        int colorIndex = (int)(255*value/maximum);
        colorIndex = colorIndex > 255 ? 255 : colorIndex;
        int rgb = value < 0 ? negColorImage.getRGB(255-colorIndex, 0) : posColorImage.getRGB(colorIndex, 0);
        return new Color(rgb);
    }
    
    
    /**
     *  Returns the current negative color for given cy3
     */
    private Color getNegativeColor(float cy3){
        float fraction = (float)cy3/maxCY3;
        int rgb = 0;
        int colorIndex = (int)(255*cy3/this.maxCY3);
        colorIndex = Math.min(colorIndex, 255);
        if(isGRScale){
           rgb = negColorImage.getRGB(255-colorIndex, 0);
        }
        else{
            rgb = negColorImage.getRGB(0, 0);            
        }
        return new Color(rgb);
    }
    
    /**
     * Returns the current negative color for given cy3
     */
    private Color getPositiveColor(float cy5){
        float fraction = (float)cy5/maxCY3;
        int rgb = 0;
        int colorIndex = (int)(255*cy5/this.maxCY5);
        colorIndex = Math.min(colorIndex, 255);
        if(isGRScale){
           rgb = posColorImage.getRGB(colorIndex, 0);
        }
        else{
            rgb = posColorImage.getRGB(255, 0);            
        }
        return new Color(rgb);
    }
    
    /**
     * Returns the current overlay color for passed cy3 and cy5
     */
    private Color getOverlayColor(float cy3, float cy5){
        Color negColor = getNegativeColor(cy3);
        Color posColor = getPositiveColor(cy5);
        double negFraction = cy3/(cy3 + cy5);
        double posFraction = cy5/(cy3 + cy5);
        int r, g, b;
        
        r = (int)(negColor.getRed()+ posColor.getRed());
        r = r < 256 ? r : 255;
        g = (int)(negColor.getGreen() + posColor.getGreen());
        g = g < 256 ? g : 255;
        b = (int)(negColor.getBlue() + posColor.getBlue());
        b = b < 256 ? b : 255;
        
        return new Color(r, g, b);
    }
    
        /**
     * Returns the current overlay color for passed cy3 and cy5
     */
    private BufferedImage getOverlayImage(float cy3, float cy5){
        Color negColor = getNegativeColor(cy3);
        Color posColor = getPositiveColor(cy5);
        
        BufferedImage bi = new BufferedImage(elementSize.width, elementSize.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D)bi.createGraphics();
        g2.setColor(Color.white);
        g2.fillRect(0,0, elementSize.width, elementSize.height);   
      // g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)(cy3/(cy3+cy5))));
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)(0.5)));
        g2.setColor(negColor);
        g2.fillRect(0,0, elementSize.width, elementSize.height);
       // g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)(cy5/(cy3+cy5))));
        g2.setColor(posColor);
        g2.fillRect(0, 0, elementSize.width, elementSize.height);
                
        return bi;
    }
    
    /**
     * Sets the draw chain attribute.
     */
    private void setDrawChain(boolean drawChain) {
	this.drawChain = drawChain;
	repaint();
    }
    
    /**
     * Returns the draw chain attribute value.
     */
    private boolean isDrawChain() {
	return drawChain;
    }
    
    /**
     * Sets a chain array.
     */
    private void setChain(int[] chain) {
	this.chain = chain;
    }
    
    /**
     * Creates and sets a chain for specified row and column.
     */
    private void doChain(int row, int column) {
	setChain(createChain(row, column));
    }
    
    /**
     * Returns a chain.
     */
    private int[] getChain() {
	return chain;
    }
    
    /**
     * Creates a chain for specified row and column.
     */
    private int[] createChain(int row, int column) {
	int[] indices = data.getSortedIndices(column);
	int trueColumn = data.getProbeColumn(column, indices[row]);
	int trueRow    = data.getProbeRow(column, indices[row]);
	int[] positions = new int[data.getFeaturesCount()];
	final int size = data.getFeaturesSize();
	for (int feature = 0; feature < positions.length; feature++) {
	    if (feature == column) {
		positions[column] = row;
	    } else {
		indices = data.getSortedIndices(feature);
		for (int probe = 0; probe < size; probe++) {
		    if ((data.getProbeColumn(feature, indices[probe]) == trueColumn) &&
		    (data.getProbeRow(feature, indices[probe]) == trueRow)) {
			positions[feature] = probe;
			break;
		    }
		}
	    }
	}
	return positions;
    }
    
    /**
     * Returns width of tracing space.
     */
    private int getSpacing() {
	if (isTracing) {
	    return TRACE_SPACE;
	}
	return 0;
    }
    
    /**
     * Returns a width of the viewer.
     */
    private int getXSize() {
	return(features*elementSize.width)+((features-1)*getSpacing());
    }
    
    /**
     * Returns a height of the viewer.
     */
    private int getYSize() {
	return probes*elementSize.height;
    }
    
    /**
     * Updates max label width value.
     */
    private void updateMaxLabelWidth() {
	Graphics2D g = (Graphics2D)getGraphics();
	if (g == null) {
	    return;
	}
	if (isAntiAliasing) {//Anti-aliasing is on
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}
	FontMetrics fm = g.getFontMetrics();
	if (labelIndex < 0) {
	    maxLabelWidth = fm.stringWidth("");
	    return;
	}
	String label;
	double maxWidth = 0;
	final int SIZE = data.getFeaturesSize();
	for (int i = 0; i < SIZE; i++) {
	    label = data.getElementAttribute(i, labelIndex);
	    maxWidth = Math.max(maxWidth, fm.stringWidth(label));
	}
	maxLabelWidth = (int)maxWidth;
    }
    
    /**
     * Returns max label width.
     */
    private int getMaxLabelWidth() {
	return maxLabelWidth;
    }
    
    /**
     * Draws annotations into specified graphics.
     */
    private void drawLabels(Graphics2D g, final int top, final int bottom) {
	String label;
	g.setColor(Color.black);
	int[] indices = data.getSortedIndices(0);
	for (int probe = top; probe < bottom; probe++) {
	    label = data.getElementAttribute(indices[probe], labelIndex);
	    if (label != null) {
		g.drawString(label, insets.left + getXSize() + insets.right, insets.top + ((probe +1)*elementSize.height) -1);
	    }
	}
    }
    
    /**
     * Returns index of top row.
     */
    private int getTopIndex(int top) {
	if (top<0) {
	    return 0;
	}
	return top/elementSize.height;
    }
    
    /**
     * Returns index of left column.
     */
    private int getLeftIndex(int left) {
	if (left<insets.left) {
	    return 0;
	}
	return (left - insets.left)/(elementSize.width+getSpacing());
    }
    
    /**
     * Returns index of right column.
     */
    private int getRightIndex(int right, int limit) {
	if (right<0) {
	    return 0;
	}
	int result = right/(elementSize.width+getSpacing())+1;
	return result > limit ? limit : result;
    }
    
    /**
     * Returns index of bottom row.
     */
    private int getBottomIndex(int bottom, int limit) {
	if (bottom<0) {
	    return 0;
	}
	int result = bottom/elementSize.height+1;
	return result > limit ? limit : result;
    }
    
    /**
     * Draws link for specified spots.
     */
    private void drawLink(Graphics2D g, int row1, int col1, int row2, int col2, Color color) {
	double yPos1 = row1+0.5;
	double yPos2 = row2+0.5;
	g.setColor(color);
	g.drawLine(insets.left + elementSize.width + col1*(elementSize.width+getSpacing()),
	insets.top + (int)(yPos1*elementSize.height),
	insets.left + col2*(elementSize.width+getSpacing())-1,
	insets.top + (int)(yPos2*elementSize.height));
    }
    
    /**
     * Calculates color for CY3 value.
     */
    private float getGreenValue(float cy3) {
	if (isGRScale) {
	    float color = Math.abs(cy3/maxCY3);
	    return color > 1f ? 1f : color;
	} else {
	    if (cy3 == 0f)
		return 0f;
	    else
		return 1f;
	}
    }
    
    /**
     * Calculates color for CY5 value.
     */
    private float getRedValue(float cy5) {
	if (isGRScale) {
	    float color = Math.abs(cy5/maxCY5);
	    return color > 1f ? 1f : color;
	} else {
	    if (cy5 == 0f)
		return 0f;
	    else
		return 1f;
	}
    }
    
    /**
     * @return true, if specified row and column are exists.
     */
    private boolean isLegalPosition(int row, int column) {
	if (isLegalRow(row) && isLegalColumn(column))
	    return true;
	return false;
    }
    
    /**
     * @return true, if specified column is exists.
     */
    private boolean isLegalColumn(int column) {
	if (column < 0 || column > features -1)
	    return false;
	return true;
    }
    
    /**
     * @return true, if specified row is exists.
     */
    private boolean isLegalRow(int row) {
	if (row < 0 || row > probes -1)
	    return false;
	return true;
    }
    
    /**
     * Finds column by specified x coordinate.
     * @return -1 if column was not found.
     */
    private int findColumn(int targetx) {
	//targetx -= insets.left;
	int columnSize = elementSize.width + getSpacing();
	if (targetx >= features*columnSize - getSpacing() + insets.left || targetx < insets.left) {
	    return -1;
	}
      //  if(targetx < insets.left)
       //     return 0;
        else
            return ((targetx - insets.left)/columnSize);
    }
    
    /**
     * Finds row by specified y coordinate.
     * @return -1 if row was not found.
     */
    private int findRow(int targety) {
	targety -= insets.top;
	int ySize = probes*elementSize.height;
	if (targety >= ySize || targety < 0)
	    return -1;
	return targety/elementSize.height;
    }
    
    /**
     * Returns x coordinate of a column.
     */
    private int getXPos(int column) {
	return column*(elementSize.width + getSpacing()) + insets.left;
    }
    
    /**
     * Returns y coordinate of a row.
     */
    private int getYPos(int row) {
	return row * elementSize.height;
    }
    
    /**
     * Draws a rectangle with specified color.
     */
    private void drawColoredBoxAt(Graphics g, int row, int column, Color color) {
	g.setColor(color);
	g.drawRect(insets.left + column*(elementSize.width+getSpacing()), insets.top + row*elementSize.height, elementSize.width-1, elementSize.height-1);
    }
    
    /**
     * Draws a chain.
     */
    private void drawChain(Graphics2D g, Color color, int[] chain, final int left, final int right) {
	for (int column = left; column < right; column++) {
	    drawColoredBoxAt(g, chain[column], column, color);
	}
	final int last = right == chain.length ? right-1 : right;
	for (int column = left; column < last; column++) {
	    drawLink(g, chain[column], column, chain[column+1], column+1, color);
	}
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
    
    /**
     * The listener to listen to mouse, keyboard and window events.
     */
    private class Listener extends MouseAdapter implements MouseMotionListener, KeyListener, WindowListener {
	
	private int oldRow = -1;
	private int oldColumn = -1;
	
	public void mousePressed(MouseEvent event) {
	    requestFocus();
	}
	
	public void mouseClicked(MouseEvent event) {
	    int column = findColumn(event.getX());
	    int row = findRow(event.getY());
	    if (!isLegalPosition(row, column)) {
		return;
	    }
	    if (event.isControlDown()) { // single array viewer
		framework.displaySingleArrayViewer(column);
		return;
	    }
	    if (!event.isShiftDown()) { // element info
		int[] indices = data.getSortedIndices(column);
		framework.displaySlideElementInfo(column, indices[row]);
		return;
	    }
	    // start chain
	    doChain(row, column);
	    setDrawChain(true);
	}
	
	public void mouseMoved(MouseEvent event) {
	    if (features == 0 || event.isShiftDown())
		return;
	    int column = findColumn(event.getX());
	    int row = findRow(event.getY());
	    if (isCurrentPosition(row, column)) {
		return;
	    }
	    Graphics g = null;
	    if (isLegalPosition(row, column)) {
		g = getGraphics();
		drawColoredBoxAt(g, row, column, Color.white);
	    }
	    if (isLegalPosition(oldRow, oldColumn)) {
		g = g != null ? g : getGraphics();
		drawSlideDataElement(g, oldRow, oldColumn, data.getSortedIndices(oldColumn));
	    }
	    setOldPosition(row, column);
	    if (g != null) {
		g.dispose();
	    }
	}
	
	public void mouseExited(MouseEvent event) {
	    if (isLegalPosition(oldRow, oldColumn)) {
		Graphics g = getGraphics();
		drawSlideDataElement(g, oldRow, oldColumn, data.getSortedIndices(oldColumn));
		g.dispose();
	    }
	    setOldPosition(-1, -1);
	}
	
	public void mouseDragged(MouseEvent event) {
	}
	
	public void keyReleased(KeyEvent event) {
	    if ((event.getKeyCode() == KeyEvent.VK_SHIFT) && (isDrawChain() == true)) {
		setDrawChain(false);
	    }
	}
	
	public void keyPressed(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	
	private void setOldPosition(int row, int column) {
	    oldColumn = column;
	    oldRow = row;
	}
	
	private boolean isCurrentPosition(int row, int column) {
	    return(row == oldRow && column == oldColumn);
	}
	
	public void windowClosing(WindowEvent e) {
	    MultipleArrayCanvas.this.isShowThumbnail = false;
	    MultipleArrayCanvas.this.thumbnail.hide();
	}
	
	public void windowOpened(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
    }
    
}
