/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SOTAExperimentViewer.java,v $
 * $Revision: 1.9 $
 * $Date: 2006-05-02 16:57:35 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.sota;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.Expression;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.LineBorder;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.util.FloatMatrix;

/**
 * Class to display expression images with a <code>CentroidExperimentHeader</code>
 * and cluster information.
 */
public class SOTAExperimentViewer extends ExperimentViewer implements IViewer {
    
    protected static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
    private static final String SET_DEF_COLOR_CMD = "set-def-color-cmd";
    private static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
    private static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";
    protected static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";
    
    //panel components
    private IViewer expViewer;
    private JComponent header;
    private InfoPanel infoPanel;
    private JPanel viewPanel;
    
    //data matricies and parameters
    private int [][] clusters;
    private FloatMatrix clusterDivFM;
    private FloatMatrix centroidDataFM;
    private int numberOfCells;
    private float factor;
    private int function;
    private boolean geneClusterViewer = true;
    private boolean useDoubleGradient = true;
    private FloatMatrix codes;
    private SOTATreeData sotaTreeData;
    
    /**
     * Constructs a <code>SOTAExperimentViewer</code> with specified
     * experiment, clusters (gene indices) and codes (centroid data)
     * SOTAExperimentViewers created with this constructor always display gene clusters
     */
    public SOTAExperimentViewer(Experiment experiment, int[][] clusters, FloatMatrix codes, FloatMatrix clusterDiv, SOTATreeData sotaTreeData) {
        setLayout(new GridBagLayout());
        Listener listener = new Listener();
        this.codes = codes;
        this.popup = createJPopupMenu(listener);
        this.clusters = clusters;
        this.clusterDivFM = clusterDiv;
        this.numberOfCells = 0;
        this.exptID = experiment.getId();
        if(this.clusterDivFM != null)
            this.numberOfCells = clusterDivFM.getRowDimension();
        this.centroidDataFM = codes;
        this.sotaTreeData = sotaTreeData;
        this.factor = sotaTreeData.factor;  //from SOTA, factor sets polarity of 'displayed' distances in viewer based on metric
        this.function = sotaTreeData.function; //distance metric
        this.expViewer = new ExperimentViewer(experiment, clusters);
        this.expViewer.getContentComponent().addMouseListener(listener);
        setInsets(new Insets(0,0,0,0));
        this.header = new CentroidExperimentHeader(this.expViewer.getHeaderComponent(), codes, clusters,"SOTA Centroid Vector");
        ((CentroidExperimentHeader)this.header).setNegAndPosColorImages(((ExperimentViewer)expViewer).getNegColorImage(), ((ExperimentViewer)expViewer).getPosColorImage());
        this.infoPanel = new InfoPanel();
        this.infoPanel.addMouseListener(listener);
        ((CentroidExperimentHeader)this.header).setNegAndPosColorImages(((ExperimentViewer)expViewer).getNegColorImage(), ((ExperimentViewer)expViewer).getPosColorImage());
        ((CentroidExperimentHeader)this.header).setMissingColor(((ExperimentViewer)expViewer).getMissingColor());
        ((CentroidExperimentHeader)this.header).addMouseListener(listener);
        viewPanel = new JPanel();
        viewPanel.setLayout(new GridBagLayout());
        viewPanel.add(((JComponent)expViewer), new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        viewPanel.add(infoPanel, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        add(viewPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }
    public SOTAExperimentViewer(Experiment experiment, int[][] clusters, FloatMatrix codes, FloatMatrix clusterDiv, SOTATreeData sotaTreeData, Boolean clusterGenes) {
    	this(experiment, clusters, codes, clusterDiv, sotaTreeData, clusterGenes.booleanValue());
    }   
    /**
     * Constructs a <code>SOTAExperimentViewer</code> with specified
     * experiment, clusters (gene indices) and codes (centroid data)
     */
    public SOTAExperimentViewer(Experiment experiment, int[][] clusters, FloatMatrix codes, FloatMatrix clusterDiv, SOTATreeData sotaTreeData, boolean clusterGenes) {
        setLayout(new GridBagLayout());
    	this.codes = codes;
        this.geneClusterViewer = clusterGenes;
        this.exptID = experiment.getId();
        Listener listener = new Listener();
        this.popup = createJPopupMenu(listener);
        this.clusters = clusters;
        this.clusterDivFM = clusterDiv;
        this.numberOfCells = 0;
        if(this.clusterDivFM != null)
            this.numberOfCells = clusterDivFM.getRowDimension();
        this.centroidDataFM = codes;
        this.sotaTreeData = sotaTreeData;
        this.factor = sotaTreeData.factor;  //from SOTA, factor sets polarity of 'displayed' distances in viewer based on metric
        this.function = sotaTreeData.function; //distance metric
        if(!clusterGenes){
            this.expViewer = new ExperimentClusterViewer(experiment, clusters, "Sota Centroid Vector", codes.getArrayCopy());
            this.header = (ExperimentClusterHeader)(expViewer.getHeaderComponent());
        } else {
            this.expViewer = new ExperimentViewer(experiment, clusters);
            this.header = new CentroidExperimentHeader(expViewer.getHeaderComponent(), codes, this.clusters, "SOTA Centroid Vector");
        }
        this.expViewer.getContentComponent().addMouseListener(listener);
        this.infoPanel = new InfoPanel();
        this.infoPanel.addMouseListener(listener);
        setInsets(new Insets(0,0,0,0));
        viewPanel = new JPanel();
        viewPanel.setLayout(new GridBagLayout());
        viewPanel.add(((JComponent)expViewer), new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        viewPanel.add(infoPanel, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        add(viewPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }
    
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new",
    			new Object[]{this.expViewer.getExperiment(), this.clusters, this.codes, this.clusterDivFM, this.sotaTreeData, new Boolean(this.geneClusterViewer)});
    }
    
    public SOTAExperimentViewer(IViewer exptViewer, Float factor, Integer function,
    		Integer numberOfCells, Boolean geneClusterViewer, Boolean useDoubleGradient, 
			FloatMatrix clusterDivFM, FloatMatrix centroidDataFM, int[][] clusters, 
			JComponent header, 
			Insets insets, Integer exptID, FloatMatrix codes, JPanel viewPanel) {
    	setLayout(new GridBagLayout());

    	this.expViewer = exptViewer;
        this.factor = factor.floatValue();
        this.function = function.intValue();
        this.numberOfCells = numberOfCells.intValue();
        this.geneClusterViewer = geneClusterViewer.booleanValue();
        this.useDoubleGradient = useDoubleGradient.booleanValue();
        this.clusterDivFM = clusterDivFM;
        this.centroidDataFM = centroidDataFM;
    	this.clusters = clusters;
    	this.header = header;
    	setInsets(insets);
    	this.exptID = exptID.intValue();
    	this.codes = codes;
    	this.viewPanel = viewPanel;
    }
    public void setExperiment(Experiment e) {
    	super.setExperiment(e);
        expViewer.setExperiment(e);
        Listener listener = new Listener();
        this.expViewer.getContentComponent().addMouseListener(listener);
        this.infoPanel = new InfoPanel();
        this.infoPanel.addMouseListener(listener);
    	this.header.addMouseListener(listener);
        this.popup = createJPopupMenu(listener);
        if(!geneClusterViewer){ //Experiment clusters (expViewer is an ExperimentClusterViewer)
           this.header = (ExperimentClusterHeader)(expViewer.getHeaderComponent());
           ((ExperimentClusterHeader)header).setExperiment(e);
        } else { // gene clusters
            this.header = new CentroidExperimentHeader(expViewer.getHeaderComponent(), codes, this.clusters, "SOTA Centroid Vector");
            ((CentroidExperimentHeader)header).setExperiment(e);
            ((CentroidExperimentHeader)this.header).setNegAndPosColorImages(((ExperimentViewer)expViewer).getNegColorImage(), ((ExperimentViewer)expViewer).getPosColorImage());
            ((CentroidExperimentHeader)this.header).setNegAndPosColorImages(((ExperimentViewer)expViewer).getNegColorImage(), ((ExperimentViewer)expViewer).getPosColorImage());
    	    ((CentroidExperimentHeader)this.header).setNegAndPosColorImages(((ExperimentViewer)expViewer).getNegColorImage(), ((ExperimentViewer)expViewer).getPosColorImage());
    	    ((CentroidExperimentHeader)this.header).setMissingColor(((ExperimentViewer)expViewer).getMissingColor());
    	}
        viewPanel.add(((JComponent)expViewer), new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        viewPanel.add(infoPanel, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        add(viewPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }
    
    
    /**
     *  Adds components to viewer
     */
    private void addComponents(JComponent header, ExperimentViewer expImageViewer, InfoPanel info){
        add(expImageViewer, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 0, 0, 0), 0, 0));
        add(info,  new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }
    
    /**
     * Returns the header component.
     */
    public JComponent getHeaderComponent() {
        return header;
    }
    
    /**
     * Returns the wrapped experiment viewer.
     */
    public JComponent getContentComponent() {
        return this;
    }
    
    public BufferedImage getImage() {
        return expViewer.getImage();
    }
    
    /**
     * Updates header and contents attributes when the viewer is selected.
     */
    public void onSelected(IFramework framework) {
    	setFramework(framework);
        if(this.geneClusterViewer)
            ((ExperimentViewer) expViewer).onSelected(framework);
        else
            ((ExperimentClusterViewer) expViewer).onSelected(framework);
        Integer userObject = ((Integer)framework.getUserObject());
        infoPanel.setCurrentCluster(userObject == null ? 0 : userObject.intValue());
        infoPanel.onSelected();
        IDisplayMenu menu = framework.getDisplayMenu();
        useDoubleGradient = menu.getUseDoubleGradient();
        if(geneClusterViewer){                        
            ((CentroidExperimentHeader)this.header).setCurrentCluster(userObject == null ? 0 : userObject.intValue());
            ((CentroidExperimentHeader)this.header).setNegAndPosColorImages(menu.getNegativeGradientImage(), menu.getPositiveGradientImage());
            ((CentroidExperimentHeader)this.header).setValues(menu.getMinRatioScale(), menu.getMidRatioValue(), menu.getMaxRatioScale());
            ((CentroidExperimentHeader)this.header).setAntiAliasing(menu.isAntiAliasing());            
            ((CentroidExperimentHeader)this.header).setDrawBorders(menu.isDrawingBorder());
            ((CentroidExperimentHeader)this.header).updateSize(menu.getElementSize());
            ((CentroidExperimentHeader)this.header).setUseDoubleGradient(useDoubleGradient);
            int height = ((CentroidExperimentHeader)this.header).getCurrHeight();
            this.header.setSize(getContentWidth(), height);
            this.header.setPreferredSize(new Dimension(getContentWidth(), height));
        }
        else{
            ((ExperimentClusterHeader)(this.header)).updateSizes(getContentWidth(), menu.getElementSize().width);
            ((ExperimentClusterHeader)(this.header)).setUseDoubleGradient(useDoubleGradient);        
            ((ExperimentClusterHeader)this.header).setValues(menu.getMinRatioScale(), menu.getMidRatioValue(), menu.getMaxRatioScale());
        }
        repaint();
    }
    
    
    public int getContentWidth(){
        int width;
        if(this.geneClusterViewer)
            width = ((ExperimentViewer)this.expViewer).getContentWidth();
        else
            width = ((ExperimentClusterViewer)this.expViewer).getContentWidth();
        width += this.infoPanel.INFO_PANEL_WIDTH;
        return width;
    }
    
    
    /**
     * Updates experiment data.
     */
    public void onDataChanged(IData data) {
        expViewer.onDataChanged(data);
    }
    
    /**
     * Updates header and contents attributes when the display menu is changed.
     */
    public void onMenuChanged(IDisplayMenu menu) {
        
        if(this.geneClusterViewer)
            ((ExperimentViewer) expViewer).onMenuChanged(menu);
        else
            ((ExperimentClusterViewer) expViewer).onMenuChanged(menu);
        
        useDoubleGradient = menu.getUseDoubleGradient();
        if(geneClusterViewer){
            ((CentroidExperimentHeader)this.header).setNegAndPosColorImages(menu.getNegativeGradientImage(), menu.getPositiveGradientImage());
            //jcb.. end point values were set in the wrong orientation changed to correct min,max order
            ((CentroidExperimentHeader)this.header).setValues(-Math.abs(menu.getMinRatioScale()), Math.abs(menu.getMaxRatioScale()));
            ((CentroidExperimentHeader)this.header).setAntiAliasing(menu.isAntiAliasing());
            ((CentroidExperimentHeader)this.header).setDrawBorders(menu.isDrawingBorder());
            ((CentroidExperimentHeader)this.header).updateSize(menu.getElementSize());
            ((CentroidExperimentHeader)this.header).setUseDoubleGradient(useDoubleGradient);
            this.header.setSize(getContentWidth(), this.header.getHeight());
            this.header.setPreferredSize(new Dimension(getContentWidth(), this.header.getHeight()));
        }
        else {
            ((ExperimentClusterHeader)(this.header)).updateSizes(getContentWidth(), menu.getElementSize().width);
            ((ExperimentClusterHeader)(this.header)).setUseDoubleGradient(useDoubleGradient);
        }
        repaint();
    }
    
    public void onDeselected() {}
    public void onClosed() {}
    
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

        //EH Gaggle testing
        menuItem = new JMenuItem("Broadcast Matrix to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
        menuItem.setActionCommand(BROADCAST_MATRIX_GAGGLE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Broadcast Gene List to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
        menuItem.setActionCommand(BROADCAST_NAMELIST_GAGGLE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        //end Gaggle testing
    }
    
    /**
     * Saves clusters.
     */
    protected void onSaveClusters() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        try {
            if(expViewer instanceof ExperimentViewer)
                ((ExperimentViewer)expViewer).saveClusters(frame);
            else
                ((ExperimentClusterViewer)expViewer).saveClusters(frame);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save clusters!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Save the viewer cluster.
     */
    protected void onSaveCluster() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        try {
            if(expViewer instanceof ExperimentViewer)
                ((ExperimentViewer)expViewer).saveCluster(frame);
            else{
                ((ExperimentClusterViewer)expViewer).saveCluster(frame);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Sets a public color.
     */
    protected void onSetColor() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        Color newColor = JColorChooser.showDialog(frame, "Choose color", CentroidViewer.DEF_CLUSTER_COLOR);
        if (newColor != null) {
            if(expViewer instanceof ExperimentViewer)
                ((ExperimentViewer)expViewer).setClusterColor(newColor);
            else
                ((ExperimentClusterViewer)expViewer).setClusterColor(newColor);
        }
    }
    
    /**
     * Sets public color for the current cluster.
     */
    public void setClusterColor(Color color) {
        if(color ==null){  //indicates removal of cluster
            if(expViewer instanceof ExperimentViewer)
                ((ExperimentViewer)expViewer).setClusterColor(null);
            else
                ((ExperimentClusterViewer)expViewer).setClusterColor(null);
        }
    }
    
    /**
     * Launches a new session
     */
    public void launchNewSession(){
        if(expViewer instanceof ExperimentViewer)
            ((ExperimentViewer)expViewer).launchNewSession();
        else
            ((ExperimentClusterViewer)expViewer).launchNewSession();
    }
    
    /**
     *  Sets cluster color
     */
    public void storeCluster(){
        if(expViewer instanceof ExperimentViewer)
            ((ExperimentViewer)expViewer).storeCluster();
        else
            ((ExperimentClusterViewer)expViewer).storeCluster();
    }
    
    /**
     * Removes a public color.
     */
    protected void onSetDefaultColor() {
        if(expViewer instanceof ExperimentViewer)
            ((ExperimentViewer)expViewer).setClusterColor(null);
        else
            ((ExperimentClusterViewer)expViewer).setClusterColor(null);
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
    
    /** Returns the viewer's clusters or null
     */
    public int[][] getClusters() {
        return this.expViewer.getClusters();
    }    
    
    /**  Returns the viewer's experiment or null
     */
    public Experiment getExperiment() {
        return this.expViewer.getExperiment();
    }
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {       
        return this.expViewer.getViewerType();
    }    
    
    //EH gaggle test
    public int[] getCluster() {
    	
        if(expViewer instanceof ExperimentViewer)
            return ((ExperimentViewer)expViewer).getCluster();
        else
            return ((ExperimentClusterViewer)expViewer).getCluster();
    }
    
    /**
     * The class to listen to mouse and action events.
     */
    private class Listener extends MouseAdapter implements ActionListener {
        
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
            //EH Gaggle test
            } else if (command.equals(BROADCAST_MATRIX_GAGGLE_CMD)) {
                broadcastClusterGaggle();
            } else if (command.equals(BROADCAST_NAMELIST_GAGGLE_CMD)) {
                broadcastNamelistGaggle();
            }
        }
        
        public void mouseReleased(MouseEvent event) {
            maybeShowPopup(event);
        }
        
        private void maybeShowPopup(MouseEvent e) {
            int [] cluster = null;
            //EH
            cluster = getCluster();
//            if(expViewer instanceof ExperimentViewer)
//                cluster = ((ExperimentViewer)expViewer).getCluster();
//            else
//                cluster = ((ExperimentClusterViewer)expViewer).getCluster();
            
            if (!e.isPopupTrigger() || cluster == null || cluster.length == 0) {
                return;
            }
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    
    /**
     * Displays information about the currently displayed cluster
     *
     */
    private class InfoPanel extends JPanel {
        
        private int currCluster;
        public int INFO_PANEL_WIDTH = 300;
        
        private javax.swing.JSplitPane jSplitPane1;
        private javax.swing.JScrollPane viewerPane;
        private javax.swing.JPanel infoPanel;
        private javax.swing.JLabel jLabel10;
        private javax.swing.JLabel jLabel11;
        private javax.swing.JLabel jLabel12;
        private javax.swing.JLabel jLabel13;
        private javax.swing.JLabel jLabel14;
        private javax.swing.JLabel jLabel15;
        private javax.swing.JLabel jLabel16;
        private javax.swing.JLabel jLabel17;
        private javax.swing.JLabel c1Label;
        private javax.swing.JLabel c1PopLabel;
        private javax.swing.JLabel c1DivLabel;
        private javax.swing.JLabel distLabel;
        private javax.swing.JLabel c2Label;
        private javax.swing.JLabel c2DivLabel;
        private javax.swing.JLabel c2PopLabel;
		private SOTAInfoStats infoStats = new SOTAInfoStats();
        
        /**
         * Constructs a new InfoPanel.
         *
         */
        private InfoPanel(){
            initComponents();
            currCluster = 0;
            
            this.setSize(INFO_PANEL_WIDTH , 350);
            this.setPreferredSize( new Dimension(INFO_PANEL_WIDTH , 350));
            this.setVisible(true);
            this.setBackground(java.awt.Color.white);
            super.setBackground(java.awt.Color.white);
        }
        
        private void initComponents() {
            
        	
            jLabel10 = new javax.swing.JLabel();
            jLabel11 = new javax.swing.JLabel();
            jLabel12 = new javax.swing.JLabel();
            jLabel13 = new javax.swing.JLabel();
            jLabel14 = new javax.swing.JLabel();
            jLabel15 = new javax.swing.JLabel();
            jLabel16 = new javax.swing.JLabel();
            jLabel17 = new javax.swing.JLabel();
            c1Label = new javax.swing.JLabel();
            c1PopLabel = new javax.swing.JLabel();
            c1DivLabel = new javax.swing.JLabel();
            distLabel = new javax.swing.JLabel();
            c2Label = new javax.swing.JLabel();
            c2DivLabel = new javax.swing.JLabel();
            c2PopLabel = new javax.swing.JLabel();
            
            setBackground(Color.white);
            this.setLayout(null);
            
            this.setBackground(java.awt.Color.white);
            super.setBackground(java.awt.Color.white);
            this.setBorder(new LineBorder(Color.black, 1));
            
            setAlignmentY(1.0F);
            setAlignmentX(1.0F);
            setOpaque(false);
            jLabel10.setText("Cluster ID#:");
            jLabel10.setForeground(java.awt.Color.black);
            add(jLabel10);
            jLabel10.setBounds(20, 30, 65, 17);
            
            jLabel11.setText("Cluster Population:");
            jLabel11.setForeground(java.awt.Color.black);
            add(jLabel11);
            jLabel11.setBounds(20, 60, 107, 17);
            
            jLabel12.setText("Cluster Diversity:");
            jLabel12.setForeground(java.awt.Color.black);
            add(jLabel12);
            jLabel12.setBounds(20, 90, 96, 17);
            
            jLabel13.setText("Distance to");
            jLabel13.setForeground(java.awt.Color.black);
            add(jLabel13);
            jLabel13.setBounds(20, 130, 64, 17);
            
            jLabel14.setText("Closest Neighbor:");
            jLabel14.setForeground(java.awt.Color.black);
            add(jLabel14);
            jLabel14.setBounds(20, 150, 100, 17);
            
            jLabel15.setText("Neighbor ID#:");
            jLabel15.setForeground(java.awt.Color.black);
            add(jLabel15);
            jLabel15.setBounds(20, 190, 75, 17);
            
            jLabel16.setText("Neighbor Population:");
            jLabel16.setForeground(java.awt.Color.black);
            add(jLabel16);
            jLabel16.setBounds(20, 220, 117, 17);
            
            jLabel17.setText("Neighbor Diversity:");
            jLabel17.setForeground(java.awt.Color.black);
            add(jLabel17);
            jLabel17.setBounds(20, 250, 106, 17);
            
            c1Label.setForeground(java.awt.Color.black);
            add(c1Label);
            c1Label.setBounds(150, 30, 70, 20);
            
            c1PopLabel.setForeground(java.awt.Color.black);
            add(c1PopLabel);
            c1PopLabel.setBounds(150, 60, 70, 20);
            
            c1DivLabel.setForeground(java.awt.Color.black);
            add(c1DivLabel);
            c1DivLabel.setBounds(150, 90, 110, 20);
            
            distLabel.setForeground(java.awt.Color.black);
            add(distLabel);
            distLabel.setBounds(150, 150, 110, 20);
            
            c2Label.setForeground(java.awt.Color.black);
            add(c2Label);
            c2Label.setBounds(150, 190, 70, 20);
            
            c2DivLabel.setForeground(java.awt.Color.black);
            add(c2DivLabel);
            c2DivLabel.setBounds(150, 250, 110, 20);
            
            c2PopLabel.setForeground(java.awt.Color.black);
            add(c2PopLabel);
            c2PopLabel.setBounds(150, 220, 70, 20);
        }
        
        /**
         * Sets viewable data into panel
         * EH changed this method to take the bean SOTAInfoPanel instead of a long list of parameters
         */
       	private void setData1(SOTAInfoStats infoStats) {
        	this.infoStats = infoStats;
        	this.c1Label.setText(String.valueOf(infoStats.getC1()+1));
            this.c1PopLabel.setText(String.valueOf(infoStats.getClusterPop1()));
            this.c1DivLabel.setText(String.valueOf(infoStats.getDiv1()));
            this.distLabel.setText(String.valueOf(infoStats.getDist()*factor));  //factor sets polarity based on distance metric
            this.c2Label.setText(String.valueOf(infoStats.getC2()+1));
            this.c2PopLabel.setText(String.valueOf(infoStats.getClusterPop2()));
            this.c2DivLabel.setText(String.valueOf(infoStats.getDiv2()));
           
            repaint();
        }
        
        /**
         * Clears data entries
         */
        private void clearData(int clusterNum){
            this.c1Label.setText(String.valueOf(clusterNum+1));
            this.c1PopLabel.setText("");
            this.c1DivLabel.setText("");
            this.distLabel.setText("");
            this.c2Label.setText("");
            this.c2PopLabel.setText("");
            this.c2DivLabel.setText("");
            repaint();
        }
        
        /**
         * Returns index of closest neighbor
         */
        private int getClosestCentroid(int centroidNum){
            
            float minDist = Float.POSITIVE_INFINITY;
            float currDist;
            int closestCentroid = centroidNum;
            for(int i = 0; i < numberOfCells ;i++){
                currDist = org.tigr.microarray.mev.cluster.algorithm.impl.ExperimentUtil.geneDistance(centroidDataFM,
                null, centroidNum, i, function, (float)1.0, false);
                
                if(currDist < minDist && i != centroidNum){
                    minDist = currDist;
                    closestCentroid = i;
                }
            }
            return closestCentroid;
        }
        
        /**
         *  sets the cluster index (and associated data) to display
         */
        public void setCurrentCluster(int clusterIndex){
            currCluster = clusterIndex;
        }
        
        
        /**
         *  Triggers preparation to display data on current cluster
         */
        public void onSelected(){
            float neighborDist;
            int neighbor = getClosestCentroid(currCluster);
            
            if(neighbor == currCluster) return;
            
            neighborDist = org.tigr.microarray.mev.cluster.algorithm.impl.ExperimentUtil.geneDistance(centroidDataFM,
            null, currCluster, neighbor, function, (float)1.0, false);
            
            if(neighborDist == Float.POSITIVE_INFINITY || neighborDist == 0 || neighbor >= numberOfCells || clusterDivFM == null || clusters[currCluster].length <=0)
                clearData(currCluster);
            else {
            	//int c1, int clusterPop1, float div1, float dist, int c2, int clusterPop2, float div2){
            	infoStats.setC1(currCluster);
            	infoStats.setClusterPop1(clusters[currCluster].length);
            	infoStats.setDiv1(clusterDivFM.get(currCluster, 0));
            	infoStats.setDist(neighborDist);
            	infoStats.setC2(neighbor);
            	infoStats.setClusterPop2(clusters[neighbor].length);
            	infoStats.setDiv2(clusterDivFM.get(neighbor, 0));
            	setData1(infoStats);
            }
        }
    }
}



