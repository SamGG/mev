/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: SVMDiscriminantExperimentViewer.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-05-02 16:57:36 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.Expression;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.tigr.microarray.mev.cluster.clusterUtil.*;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;



public class SVMDiscriminantExperimentViewer extends JPanel implements IViewer {
    
    private int numRetainedPos;
    private int numRecruitedNeg;
    private Color RecruitColor = Color.red;
    private boolean classifyGenes = true;
    
    private ExperimentHeader header;
    private Experiment experiment;
    private IFramework framework;
    private IData data;
    protected int clusterIndex;
    private int[][] clusters;
    private float [][] discriminants;
    protected int[] samplesOrder;
    protected Dimension elementSize = new Dimension(20, 5);
    protected int labelIndex = -1;
    protected boolean isAntiAliasing = true;
    protected boolean isDrawBorders = true;
    protected boolean isDrawAnnotations = true;
    public static Color missingColor = new Color(128, 128, 128);
    public static Color maskColor = new Color(255, 255, 255, 128);
    private float maxValue = INITIAL_MAX_VALUE;
    private float minValue = INITIAL_MIN_VALUE;
    private float midValue = 0.0f;
    protected int firstSelectedRow = -1;
    protected int lastSelectedRow  = -1;
    public BufferedImage posColorImage = createGradientImage(Color.red, Color.black);
    public BufferedImage negColorImage = createGradientImage(Color.black, Color.green);
    private int annotationWidth;
    private Insets insets = new Insets(0, 10, 0, 0);
    
    private static final int RECT_HEIGHT = 15;
    private static final float INITIAL_MAX_VALUE = 3f;
    private static final float INITIAL_MIN_VALUE = -3f;
    private static final String NO_GENES_STR = "No Genes in Cluster!";
    private static final String NO_EXPERIMENTS_STR = "No Experiments In Cluster!";
    private static final Font ERROR_FONT = new Font("monospaced", Font.BOLD, 20);
    
    protected static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
    protected static final String SET_DEF_COLOR_CMD = "set-def-color-cmd";
    protected static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
    protected static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";
    protected static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";
    private boolean haveColorBar;
    private boolean useDoubleGradient = true;
    
    private JPopupMenu popup;
    private int exptID;
    public SVMDiscriminantExperimentViewer(Experiment experiment, int [][] clusters, Integer NumRetainedPos, Integer NumRecruitedNeg, float [][] discriminants, int [] samplesOrder, Boolean classifyGenes) {
    	this(experiment, clusters, NumRetainedPos.intValue(), NumRecruitedNeg.intValue(), discriminants, samplesOrder, classifyGenes.booleanValue());
    }
    /**
     * Creates new SVMDiscriminantExperimentViewer
     */
    public SVMDiscriminantExperimentViewer(Experiment experiment, int [][] clusters, int NumRetainedPos, int NumRecruitedNeg, float [][] discriminants, int [] samplesOrder, boolean classifyGenes) {
        if (experiment == null) {
            throw new IllegalArgumentException("experiment == null");
        }
        this.experiment = experiment;
        this.exptID = experiment.getId();
        this.clusters = clusters == null ? defGenesOrder(experiment.getNumberOfGenes()) : clusters;
        this.discriminants = discriminants;
        this.samplesOrder = samplesOrder == null ? defSamplesOrder(experiment.getNumberOfSamples()) : samplesOrder;
        this.classifyGenes = classifyGenes;
        this.isDrawAnnotations = true;
        this.header = new ExperimentHeader(this.experiment, this.clusters, this.samplesOrder);
        setBackground(Color.white);
        Listener listener = new Listener();
        addMouseListener(listener);
        addMouseMotionListener(listener);
        
        SVMExperimentActionListener actionListener = new SVMExperimentActionListener();
        addMouseListener(actionListener);
        this.popup = createJPopupMenu(actionListener);
        numRetainedPos= NumRetainedPos;
        numRecruitedNeg= NumRecruitedNeg;
    }
    /**
     * @inheritDoc
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{this.experiment, clusters, new Integer(numRetainedPos), new Integer(numRecruitedNeg), discriminants, samplesOrder, new Boolean(classifyGenes)});
    }
    
    /**
     * Creates a popup menu.
     */
    private JPopupMenu createJPopupMenu(SVMExperimentActionListener listener) {
        JPopupMenu popup = new JPopupMenu();
        addMenuItems(popup, listener);
        return popup;
    }
    
    
    /**
     * Adds viewer specific menu items.
     */
    protected void addMenuItems(JPopupMenu menu, ActionListener listener) {
        JMenuItem menuItem;
        menuItem = new JMenuItem("Store cluster", GUIFactory.getIcon("new16.gif"));
        menuItem.setActionCommand(STORE_CLUSTER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Launch new session", GUIFactory.getIcon("analysis16.gif"));
        menuItem.setActionCommand(LAUNCH_NEW_SESSION_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Delete public cluster", GUIFactory.getIcon("delete16.gif"));
        menuItem.setActionCommand(SET_DEF_COLOR_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Save cluster...", GUIFactory.getIcon("save16.gif"));
        menuItem.setActionCommand(SAVE_CLUSTER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Save all clusters...", GUIFactory.getIcon("save16.gif"));
        menuItem.setActionCommand(SAVE_ALL_CLUSTERS_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
    }
    
    
    
    private static int[] defSamplesOrder(int size) {
        int[] order = new int[size];
        for (int i=0; i<order.length; i++) {
            order[i] = i;
        }
        return order;
    }
    
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
        IDisplayMenu menu = framework.getDisplayMenu();
        useDoubleGradient = menu.getUseDoubleGradient();
        header.setUseDoubleGradient(useDoubleGradient);
        Integer userObject = (Integer)framework.getUserObject();
        setClusterIndex(userObject == null ? 0 : userObject.intValue());
        labelIndex = menu.getLabelIndex();
        this.maxValue = menu.getMaxRatioScale();
        this.minValue = menu.getMinRatioScale();
        this.midValue = menu.getMidRatioValue();
        setElementSize(menu.getElementSize());
        setAntialiasing(menu.isAntiAliasing());
        setDrawBorders(menu.isDrawingBorder());
        updateSize();
        header.setData(data);
        onMenuChanged(menu);
        //header.setValues(maxValue, minValue);
        header.setClusterIndex(clusterIndex);
        header.setValues(minValue, midValue, maxValue);
        header.setAntiAliasing(menu.isAntiAliasing());
        header.updateSizes(getSize().width, elementSize.width);        
    }
    
    /**
     * Updates appropriate attributes of this viewer and its header.
     * @see IViewer#onMenuChanged
     */
    public void onMenuChanged(IDisplayMenu menu) {
    	useDoubleGradient = menu.getUseDoubleGradient();
    	header.setUseDoubleGradient(useDoubleGradient);
        setDrawBorders(menu.isDrawingBorder());
        this.maxValue = menu.getMaxRatioScale();
        this.minValue = menu.getMinRatioScale();
        this.midValue = menu.getMidRatioValue();
        this.negColorImage = menu.getNegativeGradientImage();
        this.posColorImage = menu.getPositiveGradientImage();
        header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
        //header.setValues(maxValue, minValue);
        header.setValues(minValue, midValue, maxValue);
        if (this.elementSize.equals(menu.getElementSize()) &&
        labelIndex == menu.getLabelIndex() &&
        this.isAntiAliasing == menu.isAntiAliasing()) {
            return;
        }
        setElementSize(menu.getElementSize());
        setAntialiasing(menu.isAntiAliasing());
        labelIndex = menu.getLabelIndex();
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
        this.haveColorBar = areProbesColored();
        this.header.setData(data);
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
    
    /**
     *	Returns the row (index) within the main iData which corresponds to
     *  the passed index to the clusters array
     */
    protected int getMultipleArrayDataRow(int clusterArrayRow) {
        return experiment.getGeneIndexMappedToData(this.clusters[this.clusterIndex][clusterArrayRow]);
    }
    
    /**
     * Converts cluster indicies from the experiment to IData rows which could be different
     */
    private int [] getIDataRowIndices(int [] expIndices){
        int [] dataIndices = new int[expIndices.length];
        for(int i = 0; i < expIndices.length; i++){
            dataIndices[i] = this.getMultipleArrayDataRow(i);
        }
        return dataIndices;
    }
    
    /**
     *	Returns the row index in the experiment's <code>FloatMatrix<\code>
     *  corresponding to the passed index to the clusters array
     */
    protected int getExperimentRow(int row){
        return this.clusters[this.clusterIndex][row];
    }
    
    
    
    private int getColumn(int column) {
        return samplesOrder[column];
    }
    
    /**
     * Returns wrapped experiment.
     */
    public Experiment getExperiment() {
        return experiment;
    }
    
    /**
     * Returns the data.
     */
    protected IData getData() {
        return data;
    }
    
    /**
     * Launches a new <code>MultipleExperimentViewer</code> containing the current cluster
     */
    public void launchNewSession(){
        framework.launchNewMAV(getIDataRowIndices(getCluster()), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.GENE_CLUSTER);
    }
    
    /**
     * Sets public color for the current cluster.
     */
    public void setClusterColor(Color color) {
        if(color ==null){  //indicates removal of cluster
            framework.removeCluster(getIDataRowIndices(getCluster()), experiment, ClusterRepository.GENE_CLUSTER);
        }
    }
    
    /**
     *  Sets cluster color
     */
    public void storeCluster(){
        framework.storeCluster(getIDataRowIndices(getCluster()), experiment, ClusterRepository.GENE_CLUSTER);
        onDataChanged(this.data);
        updateSize();
    }
    
    /**
     * Saves all the clusters.
     */
    public void saveClusters(Frame frame) throws Exception {
        frame = frame == null ? JOptionPane.getFrameForComponent(this) : frame;
        ExperimentUtil.saveExperiment(frame, getExperiment(), getData(), getClusters());
    }
    
    /**
     * Saves current cluster.
     */
    public void saveCluster(Frame frame) throws Exception {
        frame = frame == null ? JOptionPane.getFrameForComponent(this) : frame;
        ExperimentUtil.saveExperiment(frame, getExperiment(), getData(), getCluster());
    }
    
    /**
     * Saves clusters.
     */
    private void onSaveClusters() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        try {
            saveClusters(frame);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save clusters!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Save the viewer cluster.
     */
    private void onSaveCluster() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        try {
            saveCluster(frame);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Sets a public color.
     */
    private void onSetColor() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        Color newColor = JColorChooser.showDialog(frame, "Choose color", CentroidViewer.DEF_CLUSTER_COLOR);
        if (newColor != null) {
            setClusterColor(newColor);
        }
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
        int width = elementSize.width*samplesOrder.length; //experiment.getNumberOfSamples()+1;
        if (isDrawAnnotations) {
            this.annotationWidth = getMaxWidth(g);
            width += 20+this.annotationWidth;
        }
        if(haveColorBar)
            width += this.elementSize.width + 10;
        int height = elementSize.height*getCluster().length+1;
        setSize(width, height);
        setPreferredSize(new Dimension(width, height));
    }
    
    /**
     * Returns max width of annotation strings.
     */
    private int getMaxWidth(Graphics2D g) {
        if (g == null || data == null || getCluster() == null) {
            return 0;
        }
        if (isAntiAliasing) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        FontMetrics fm = g.getFontMetrics();
        int max = 0;
        String str;
        for (int i=0; i<getCluster().length; i++) {
            str = data.getElementAttribute(getMultipleArrayDataRow(i), labelIndex);
            // str = genename ? data.getGeneName(getMultipleArrayDataRow(i)) : data.getUniqueId(getMultipleArrayDataRow(i));
            max = Math.max(max, fm.stringWidth(str));
        }
        return max;
    }
    
    /**
     * Calculates color for passed value.
     */
    private Color getColor(float value) {
        if (Float.isNaN(value)) {
            return missingColor;
        }
        
        float maximum;
        int colorIndex, rgb;
        
        if(useDoubleGradient) {
        	maximum = value < midValue ? this.minValue : this.maxValue;
			colorIndex = (int) (255 * (value-midValue) / (maximum - midValue));
			colorIndex = colorIndex > 255 ? 255 : colorIndex;
			rgb = value < midValue ? negColorImage.getRGB(255 - colorIndex, 0)
					: posColorImage.getRGB(colorIndex, 0);
        } else {
        	float span = this.maxValue - this.minValue;
        	if(value <= minValue)
        		colorIndex = 0;
        	else if(value >= maxValue)
        		colorIndex = 255;
        	else
        		colorIndex = (int)(((value - this.minValue)/span) * 255);
         	
        	rgb = posColorImage.getRGB(colorIndex,0);
        }
        return new Color(rgb);
    }
    
    
    /**
     * Removes a public color.
     */
    private void onSetDefaultColor() {
        setClusterColor(null);
    }
    
    /**
     * Paint component into specified graphics.
     */
    public void paint(Graphics g) {
        super.paint(g);
        if (this.data == null) {
            return;
        }
        
        setBackground(Color.white);
        DecimalFormat format = new DecimalFormat();
        format.setMinimumFractionDigits(4);
        format.setMaximumFractionDigits(4);
        format.setPositivePrefix("+");
        
        final int samples = samplesOrder.length; //experiment.getNumberOfSamples();
        
        if (this.clusters == null || getCluster().length == 0) {
            g.setColor(new Color(0, 0, 128));
            g.setFont(ERROR_FONT);
            g.drawString(NO_GENES_STR, 10, 30);
            return;
        }
        
        if (this.samplesOrder.length == 0){
            g.setColor(new Color(0, 0, 128));
            g.setFont(ERROR_FONT);
            g.drawString(NO_EXPERIMENTS_STR, 10, 30);
            return;
            
        }
        
        Rectangle bounds = g.getClipBounds();
        final int top = getTopIndex(bounds.y);
        final int bottom = getBottomIndex(bounds.y+bounds.height, getCluster().length);
        final int left = getLeftIndex(bounds.x);
        final int right = getRightIndex(bounds.x+bounds.width, samples);
        
        int x, y, breakPoint;
        // draw rectangles
        for (int column=left; column<right; column++) {
            for (int row=top; row<bottom; row++) {
                fillRectAt(g, row, column);
            }
        }
        
        // add color bar
        if(haveColorBar){
            for (int row=top; row<bottom; row++) {
                fillClusterRectAt(g, row, samples * this.elementSize.width + 5);
            }
        }
        
        // draw annotations
        if (this.isDrawAnnotations) {
            if (this.isAntiAliasing) {
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
            if (right >= samples) {
                String label = "";
                g.setColor(Color.black);
                
                if(clusterIndex == 0)
                    breakPoint = this.numRetainedPos;
                else
                    breakPoint = this.numRecruitedNeg;
                
                int uniqX = elementSize.width*samples+10;
                int annY;
                
                for (int row=top; row<bottom; row++) {
                    if (labelIndex >= 0) {
                        if(classifyGenes && ((clusterIndex == 0 && row >= breakPoint ) || (clusterIndex == 1 && row < breakPoint)))
                            g.setColor(this.RecruitColor);
                        else
                            g.setColor(Color.black);
                        
                        label = data.getElementAttribute(getMultipleArrayDataRow(row), labelIndex);
                    }
                    annY = (row+1)*elementSize.height;
                    if(discriminants.length >= getCluster().length)
                        g.drawString(format.format(discriminants[clusterIndex][row])+"   " +label, uniqX + insets.left, annY);
                    else
                        g.drawString(label, uniqX + insets.left, annY);
                }
            }
        }
        g.setColor(Color.black);
    }
    
    /**
     * Fills rect with specified row and colunn.
     */
    private void fillRectAt(Graphics g, int row, int column) {
        int x = column*elementSize.width + insets.left;
        int y = row*elementSize.height;
        boolean mask = this.firstSelectedRow >= 0 && this.lastSelectedRow >= 0 && (row < this.firstSelectedRow || row > this.lastSelectedRow);
        g.setColor(getColor(this.experiment.get(getExperimentRow(row), getColumn(column))));
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
        g.drawRect(column*elementSize.width + insets.left, row*elementSize.height, elementSize.width-1, elementSize.height-1);
    }
    
    /**
     * fills cluster colors
     */
    private void fillClusterRectAt(Graphics g, int row, int xLoc) {
        Color geneColor = data.getProbeColor(getMultipleArrayDataRow(row));
        if(geneColor == null)
            geneColor = Color.white;
        
        g.setColor(geneColor);
        g.fillRect(xLoc + insets.left, row*elementSize.height, elementSize.width-1, elementSize.height);
    }
    
    private int getTopIndex(int top) {
        if (top < 0) {
            return 0;
        }
        return top/elementSize.height;
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
        int result = bottom/elementSize.height+1;
        return result > limit ? limit : result;
    }
    
    /**
     * Finds column for specified x coordinate.
     * @return -1 if column was not found.
     */
    private int findColumn(int targetx) {
        int xSize = samplesOrder.length*elementSize.width;
        if (targetx >= (xSize + insets.left) || targetx < insets.left) {
            return -1;
        }
        return (targetx - insets.left)/elementSize.width;
    }
    
    /**
     * Finds row for specified y coordinate.
     * @return -1 if row was not found.
     */
    private int findRow(int targety) {
        int ySize = getCluster().length*elementSize.height;
        if (targety >= ySize || targety < 0)
            return -1;
        return targety/elementSize.height;
    }
    
    private boolean isLegalPosition(int row, int column) {
        if (isLegalRow(row) && isLegalColumn(column))
            return true;
        return false;
    }
    
    private boolean isLegalColumn(int column) {
        if (column < 0 || column > samplesOrder.length -1)
            return false;
        return true;
    }
    
    private boolean isLegalRow(int row) {
        if (row < 0 || row > getCluster().length -1)
            return false;
        return true;
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
        if(this.classifyGenes)
            return Cluster.GENE_CLUSTER;
        return Cluster.EXPERIMENT_CLUSTER;
    }
    
    /**
     * The class to listen to mouse events.
     */
    private class Listener extends MouseAdapter implements MouseMotionListener {
        
        private String oldStatusText;
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
            if (event.isControlDown()) { // single array viewer
                framework.displaySingleArrayViewer(experiment.getSampleIndex(getColumn(column)));
                return;
            }
            if (!event.isShiftDown()) { // element info
                framework.displaySlideElementInfo(experiment.getSampleIndex(getColumn(column)), getMultipleArrayDataRow(row));
                return;
            }
        }
        
        public void mouseMoved(MouseEvent event) {
            if (experiment.getNumberOfSamples() == 0 || event.isShiftDown())
                return;
            int column = findColumn(event.getX());
            int row = findRow(event.getY());
            if (isCurrentPosition(row, column)) {
                return;
            }
            Graphics g = null;
            if (isLegalPosition(row, column)) {
                g = getGraphics();
                drawRectAt(g, row, column, Color.white);
                framework.setStatusText("Gene: "+data.getUniqueId(getMultipleArrayDataRow(row))+" Sample: "+data.getSampleName(experiment.getSampleIndex(getColumn(column)))+" Value: "+experiment.get(getExperimentRow(row), getColumn(column)));
            } else {
                framework.setStatusText(oldStatusText);
            }
            if (isLegalPosition(oldRow, oldColumn)) {
                g = g != null ? g : getGraphics();
                fillRectAt(g, oldRow, oldColumn);
            }
            setOldPosition(row, column);
            if (g != null) {
                g.dispose();
            }
        }
        
        public void mouseEntered(MouseEvent event) {
            oldStatusText = framework.getStatusText();
        }
        
        public void mouseExited(MouseEvent event) {
            if (isLegalPosition(oldRow, oldColumn)) {
                Graphics g = getGraphics();
                fillRectAt(g, oldRow, oldColumn);
                g.dispose();
            }
            setOldPosition(-1, -1);
            framework.setStatusText(oldStatusText);
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
     * The class to listen to mouse and action events.
     */
    private class SVMExperimentActionListener extends MouseAdapter implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals(SAVE_CLUSTER_CMD)) {
                onSaveCluster();
            } else if (command.equals(SAVE_ALL_CLUSTERS_CMD)) {
                onSaveClusters();
            } else if (command.equals(SET_DEF_COLOR_CMD)) {
                onSetDefaultColor();
            } else if (command.equals(STORE_CLUSTER_CMD)) {
                storeCluster();
            } else if(command.equals(LAUNCH_NEW_SESSION_CMD)){
                launchNewSession();
            }
        }
        
        public void mouseReleased(MouseEvent event) {
            maybeShowPopup(event);
        }
        
        public void mousePressed(MouseEvent event) {
            maybeShowPopup(event);
        }
        
        private void maybeShowPopup(MouseEvent e) {
            
            if (!e.isPopupTrigger() || getCluster() == null || getCluster().length == 0) {
                return;
            }
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    
	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperiment(org.tigr.microarray.mev.cluster.gui.Experiment)
	 */
	public void setExperiment(Experiment e) {
		this.experiment = e;
		this.exptID = e.getId();
		this.header.setExperiment(e);
	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExperimentID()
	 */
	public int getExperimentID() {
		return exptID;
	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperimentID(int)
	 */
	public void setExperimentID(int id) {
		this.exptID = id;
	}
   
}
