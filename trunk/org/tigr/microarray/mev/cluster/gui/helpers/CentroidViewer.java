/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: CentroidViewer.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:25 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Font;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JComponent;

import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

import org.tigr.microarray.mev.cluster.clusterUtil.*;


public class CentroidViewer extends JPanel implements IViewer {
    
    public static final Color DEF_CLUSTER_COLOR = Color.lightGray;
    protected static final Color bColor = new Color(0, 0, 128);
    
    //menu options
    protected JMenuItem setOverallMaxMenuItem;
    protected JMenuItem setClusterMaxMenuItem;
    protected static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
    protected static final String SET_DEF_COLOR_CMD = "set-def-color-cmd";
    protected static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
    protected static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";
    protected static final String SET_Y_TO_EXPERIMENT_MAX_CMD = "set-y-to-exp-max-cmd";
    protected static final String SET_Y_TO_CLUSTER_MAX_CMD = "set-y-to-cluster-max-cmd";
    protected static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";   
    protected static final String TOGGLE_REF_LINE_CMD = "toggle-ref-line-cmd";   
        
    protected Color centroidColor = Color.magenta;
    
    protected Experiment experiment;
    protected IFramework framework;
    protected IData data;
    protected int clusterIndex;
    protected int[][] clusters;
    protected float maxYValue;           //current max y range set for graph, from y = 0
    protected float maxClusterValue;     //max abs. value in current cluster
    protected float maxExperimentValue;  //max abs. value in all clusters
    protected boolean gradientToggle = true;
    
    protected int yRangeOption;
    public static int USE_EXPERIMENT_MAX = 0;
    public static int USE_CLUSTER_MAX = 1;
    
    protected boolean drawValues = true;
    protected boolean drawVariances = true;
    protected boolean drawCodes = true;
    private boolean drawMarks = true;
    protected boolean isAntiAliasing = false;
    protected boolean gradientColors;
    protected float[][] means;
    protected float[][] variances;
    protected float[][] codes;
    protected float minValue, maxValue;
    public static Color missingColor = new Color(128, 128, 128);
    public static BufferedImage posColorImage; // = createGradientImage(Color.black, Color.red);
    public static BufferedImage negColorImage; // = createGradientImage(Color.green, Color.black);
    
    protected boolean drawReferenceBlock = false;
    protected int xref = 0;
    protected int yref = 0;
    protected int currExpRefLine;
    protected boolean showRefLine = false;
    /**
     * Constructs a <code>CentroidViewer</code> for specified
     * experiment and clusters.
     *
     * @param experiment the data of an experiment.
     * @param clusters the array of clusters.
     */
    public CentroidViewer(Experiment experiment, int[][] clusters) {
        if (experiment == null) {
            throw new IllegalArgumentException("experiment == null");
        }
        this.experiment = experiment;
        this.clusters = clusters;
        setBackground(Color.white);
        setFont(new Font("monospaced", Font.BOLD, 10));
       // this.setGradient(this.checkGradient());
        this.maxExperimentValue = experiment.getMaxAbsValue();
        this.yRangeOption = this.USE_EXPERIMENT_MAX;
        this.addMouseMotionListener(new GraphListener());
        
    }
    
    /**
     * Sets means values.
     */
    public void setMeans(float[][] means) {
        this.means = means;
    }
    
    public void toggleGradient() {
        this.gradientToggle = !this.gradientToggle;
    }
    
    /**
     * Sets variances values.
     */
    public void setVariances(float[][] variances) {
        this.variances = variances;
    }
    
    /**
     * Sets codes.
     */
    public void setCodes(float[][] codes) {
        this.codes = codes;
    }
    
    /**
     * Sets the draw marks attribute.
     */
    public void setDrawMarks(boolean draw) {
        this.drawMarks = draw;
    }
    
    /**
     * Sets the draw variances attribute.
     */
    public void setDrawVariances(boolean draw) {
        this.drawVariances = draw;
    }
    
    /**
     * Sets the draw values attribute.
     */
    public void setDrawValues(boolean draw) {
        this.drawValues = draw;
    }
    
    /**
     * Sets the draw codes attribute.
     */
    public void setDrawCodes(boolean draw) {
        this.drawCodes = draw;
    }
    
    /**
     * Sets the anti-aliasing attribute.
     */
    public void setAntiAliasing(boolean value) {
        this.isAntiAliasing = value;
    }
    
    /**
     * Updates data, mode and some the viewer attributes.
     */
    public void onSelected(IFramework framework) {
        this.framework = framework;
        setData(framework.getData());
        setAntiAliasing(framework.getDisplayMenu().isAntiAliasing());
        Object userObject = framework.getUserObject();
        if (userObject instanceof CentroidUserObject) {
            setClusterIndex(((CentroidUserObject)userObject).getClusterIndex());
            setMode(((CentroidUserObject)userObject).getMode());
        } else {
            //	setClusterIndex(((Integer)userObject).intValue());
            setMode(((Integer)userObject).intValue());
        }
        updateValues(getCluster());
        this.maxValue = Math.abs(framework.getDisplayMenu().getMaxRatioScale());
        this.minValue = -Math.abs(framework.getDisplayMenu().getMinRatioScale());
        this.posColorImage = framework.getDisplayMenu().getPositiveGradientImage();
        this.negColorImage = framework.getDisplayMenu().getNegativeGradientImage();
        this.setGradient(framework.getDisplayMenu().getColorGradientState());
    }
    
    /**
     * Determines whether any clusters are set
     */
    public boolean checkGradient() {
        boolean temp = true;
        for (int i = 0; i<clusters.length; i++) {
            for (int j = 0; j<clusters[i].length; j++) {
                try {
                    if (this.data.getProbeColor(this.experiment.getGeneIndexMappedToData(clusters[i][j])) != null) {
                        temp = false;
                    }
                } catch (Exception e) {
                }
            }
        }
        return temp;
    }
    
    
    public void setGradient(boolean g) {
        this.gradientColors = g;
        this.gradientToggle = g;
    }
    
    /**
     * Calculates color for passed expression value.
     */
    private Color getColor(float value) {	// value is the log ratio used to set color cutoffs
        if (Float.isNaN(value)) {
            return missingColor;
        }
        float maximum = value < 0 ? this.minValue : this.maxValue;
        int colorIndex = (int)(255*value/maximum);
        colorIndex = colorIndex > 255 ? 255 : colorIndex;
        int rgb = value < 0 ? negColorImage.getRGB(255-colorIndex, 0) : posColorImage.getRGB(colorIndex, 0);
        return new Color(rgb);
    }
    
    
    /**
     * Sets data.
     */
    public void setData(IData data) {
        this.data = data;
    }
    
    /**
     * Sets a cluster index.
     */
    public void setClusterIndex(int clusterIndex) {
        this.clusterIndex = clusterIndex;
        updateValues(getCluster());
    }
    
    /**
     * Returns a current cluster.
     */
    public int[] getCluster() {
        return this.clusters[this.clusterIndex];
    }
    
    /**
     * Returns all clusters.
     */
    public int[][] getClusters() {
        return clusters;
    }
    
    /**
     * Returns index of a gene in the current cluster.
     */
    protected int getProbe(int row) {
        return this.clusters[this.clusterIndex][row];
    }
    
    /**
     * Sets the viewer mode.
     */
    public void setMode(int mode) {
        switch (mode) {
            case CentroidUserObject.VARIANCES_MODE:
                setDrawVariances(true);
                setDrawValues(false);
                break;
            case CentroidUserObject.VALUES_MODE:
                setDrawVariances(false);
                setDrawValues(true);
                break;
        }
    }
    
    /**
     * Updates max value.
     */
    private void updateValues(int[] cluster) {
        this.maxClusterValue = calculateMaxValue(cluster);
    }
    
    /**
     * Sets public color for the current cluster.
     */
    public void setClusterColor(Color color) {
        if(color ==null){  //indicates removal of cluster
            framework.removeCluster(getArrayMappedToData(), experiment, ClusterRepository.GENE_CLUSTER);
        }
    }
    
    /**
     *  Sets cluster color
     */
    public void storeCluster(){
        framework.storeCluster(getArrayMappedToData(), experiment, ClusterRepository.GENE_CLUSTER);       
        onDataChanged(this.data);
    }
    
    /**
     * Launches a new <code>MultipleExperimentViewer</code> containing the current cluster
     */
    public void launchNewSession(){
        framework.launchNewMAV(getArrayMappedToData(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.GENE_CLUSTER);
    }
    
    
    private int [] getArrayMappedToData(){
        int [] clusterIndices = getCluster();
        if(clusterIndices == null || clusterIndices.length < 1)
            return clusterIndices;
        
        int [] dataIndices = new int [clusterIndices.length];
        for(int i = 0; i < clusterIndices.length; i++){
            dataIndices[i] = this.experiment.getGeneIndexMappedToData(clusterIndices[i]);
        }
        return dataIndices;
    }
    
    
    /**
     * Sets Y range scaling option
     */
    public void setYRangeOption(int option){
        if(option != this.USE_EXPERIMENT_MAX && option != this.USE_CLUSTER_MAX)
            this.yRangeOption = USE_EXPERIMENT_MAX;
        else
            yRangeOption = option;
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
     * Paints chart into specified graphics.
     */
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
     */
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
        if(this.yRangeOption == this.USE_EXPERIMENT_MAX)
            maxYValue = this.maxExperimentValue;
        else if(this.yRangeOption == this.USE_CLUSTER_MAX)
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
            g.setColor(Color.gray);
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
           framework.setStatusText("Experiment = "+data.getSampleName(experiment.getSampleIndex(currExpRefLine))+",   mean = "+ this.means[this.clusterIndex][currExpRefLine]+",   sd = "+ this.variances[this.clusterIndex][currExpRefLine]);
        }
        g.setColor(bColor);
        if (getCluster() == null || getCluster().length == 0) {
            g.drawString("No Genes", left+10, top+20);
        } else {
            g.drawString(getCluster().length+" Genes", left+10, top+20);
        }
    }
    
    /**
     * @return null
     */
    public JComponent getHeaderComponent() {
        return null;
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
        setAntiAliasing(menu.isAntiAliasing());
        this.maxValue = Math.abs(menu.getMaxRatioScale());
        this.minValue = -Math.abs(menu.getMinRatioScale());
        this.posColorImage = menu.getPositiveGradientImage();
        this.negColorImage = menu.getNegativeGradientImage();
        this.setGradient(menu.getColorGradientState());
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
        
        menuItem = new JMenuItem("Launch new session", GUIFactory.getIcon("launch_new_mav.gif"));
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
        
        menu.addSeparator();
        
        setOverallMaxMenuItem = new JMenuItem("Set Y to overall max...", GUIFactory.getIcon("Y_range_expand.gif"));
        setOverallMaxMenuItem.setActionCommand(SET_Y_TO_EXPERIMENT_MAX_CMD);
        setOverallMaxMenuItem.addActionListener(listener);
        setOverallMaxMenuItem.setEnabled(false);
        menu.add(setOverallMaxMenuItem);
        
        setClusterMaxMenuItem = new JMenuItem("Set Y to cluster max...", GUIFactory.getIcon("Y_range_expand.gif"));
        setClusterMaxMenuItem.setActionCommand(SET_Y_TO_CLUSTER_MAX_CMD);
        setClusterMaxMenuItem.addActionListener(listener);
        menu.add(setClusterMaxMenuItem);
        
        menu.addSeparator();

        menuItem = new JMenuItem("Toggle reference line...");
        menuItem.setActionCommand(TOGGLE_REF_LINE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
    }
    
    
    /**
     * Handles mouse event in viewer
     */
    public class GraphListener extends java.awt.event.MouseAdapter implements java.awt.event.MouseMotionListener{
        int x = 0, y = 0;
        
        public void mouseDragged(java.awt.event.MouseEvent mouseEvent) {
            
        }
        
        public void mouseMoved(java.awt.event.MouseEvent me) {
            if(!showRefLine){
                framework.setStatusText("TIGR MultiExperiment Viewer");
                return;
            }
            int newX = me.getX();            
            int newY = me.getY();
            
            int numberOfSamples  = experiment.getNumberOfSamples();
            if(newX < 40 || newX > getWidth()- 40 || numberOfSamples <= 1){
                drawReferenceBlock = false;
                framework.setStatusText("TIGR MultiExperiment Viewer");
                repaint();
                return;
            }
            drawReferenceBlock = true;           
            currExpRefLine = Math.round((numberOfSamples - 1) * ((newX-40f)/(getWidth()-80f)));
            xref = newX;
            yref = newY;
            repaint();        
        }
    } 
}
