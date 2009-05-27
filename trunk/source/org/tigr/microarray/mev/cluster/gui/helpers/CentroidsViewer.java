/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: CentroidsViewer.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.Expression;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

/**
 * This class is used to draw set of centroid charts.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public class CentroidsViewer extends JPanel implements IViewer, java.io.Serializable {
    protected static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";

	protected static final String SET_Y_TO_EXPERIMENT_MAX_CMD = "set-y-to-exp-max-cmd";

	protected static final String SET_Y_TO_CLUSTER_MAX_CMD = "set-y-to-cluster-max-cmd";

	private int exptID = 0;
    
    /** Wrapped centroid viewer */
    protected CentroidViewer centroidViewer;

	protected JMenuItem setOverallMaxMenuItem;

	protected JMenuItem setClusterMaxMenuItem;

	protected JPopupMenu popup;
    
    /**
     * Constructs a <code>CentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public CentroidsViewer(Experiment experiment, int[][] clusters) {
		this(new CentroidViewer(experiment,clusters));
        PopupListener listener = new PopupListener();
        this.popup = createJPopupMenu(listener);
        getContentComponent().addMouseListener(listener);
    }
	
    /**
     * Constructs a <code>CentroidsViewer</code> for specified experiment
     * and clusters.
     */    
	public CentroidsViewer(CentroidViewer cv) {
		this.centroidViewer = cv;
		setBackground(Color.white);
		setFont(new Font("monospaced", Font.BOLD, 10));

        PopupListener listener = new PopupListener();
        this.popup = createJPopupMenu(listener);
        getContentComponent().addMouseListener(listener);
	}
	
	/**
	 * 
	 */
	public Expression getExpression(){
		return new Expression(this, this.getClass(), "new",
				new Object[]{getCentroidViewer()});
	}
	public void setExperiment(Experiment e) {
		this.centroidViewer.setExperiment(e);
	}
	
    public CentroidViewer getCentroidViewer(){
    	return this.centroidViewer;
    }
    
	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExperimentID()
	 */
	public int getExperimentID() {
		return this.centroidViewer.getExperimentID();
	}
	
	public void setExperimentID(int exptID){
		this.exptID = exptID;
	}
	//EH end additions for new state-saving
	
    /**
     * Returns component to be inserted into the framework scroll pane.
     */
    public JComponent getContentComponent() {
		return this;
    }
    
    /**
     * There is no a header.
     * @return null
     */
    public JComponent getHeaderComponent() {
		return null;
    }
    
    /**
     * Updates data, drawing mode and some attributes of the wrapped viewer.
     */
    public void onSelected(IFramework framework) {
		this.centroidViewer.setData(framework.getData());
		this.centroidViewer.setMode(((Integer)framework.getUserObject()).intValue());
		this.centroidViewer.setAntiAliasing(framework.getDisplayMenu().isAntiAliasing());
		this.centroidViewer.onSelected(framework);
    }
    
    /**
     * Updates data of the wrapped viewer.
     */
    public void onDataChanged(IData data) {
		this.centroidViewer.setData(data);
    }
    
    /**
     * Sets mean values to the wrapped viewer.
     */
    public void setMeans(float[][] means) {
		this.centroidViewer.setMeans(means);
    }
    
    /**
     * Sets variances values to the wrapped viewer.
     */
    public void setVariances(float[][] variances) {
		this.centroidViewer.setVariances(variances);
    }
    
    /**
     * Sets codes to the wrapped viewer.
     */
    public void setCodes(float[][] codes) {
		this.centroidViewer.setCodes(codes);
    }
    
    /**
     * Returns the experiment.
     */
    public Experiment getExperiment() {
		return this.centroidViewer.getExperiment();
    }
    
    /**
     * Returns the data.
     */
    protected IData getData() {
		return this.centroidViewer.getData();
    }
    
    /**
     * Returns clusters.
     */
    public int[][] getClusters() {
		return this.centroidViewer.getClusters();
    }
    
    /**
     * Updates some attributes of the wrapped viewer.
     */
    public void onMenuChanged(IDisplayMenu menu) {
		this.centroidViewer.onMenuChanged(menu);
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
     * Paints centroid charts into specified graphics.
     */
    public void paint(Graphics g) {
		super.paint(g);
		
		final int gap = 10;
		final int imagesX = (int)Math.ceil(Math.sqrt(getClusters().length));
		final int imagesY = (int)Math.ceil((float)getClusters().length/(float)imagesX);
		
		final float stepX = (float)(getWidth()-gap)/(float)imagesX;
		final float stepY = (float)(getHeight()-gap)/(float)imagesY;
		int cluster;
		Rectangle rect = new Rectangle();
		
		for (int y=0; y<imagesY; y++) {
		    for (int x=0; x<imagesX; x++) {
				cluster = y*imagesX+x;
				if (cluster >= getClusters().length) {
				    break;
				}
				this.centroidViewer.setClusterIndex(cluster);
				rect.setBounds((int)Math.round(gap+x*stepX), (int)Math.round(gap+y*stepY), (int)Math.round(stepX-gap), (int)Math.round(stepY-gap));
				this.centroidViewer.paint((Graphics2D)g, rect, false);
		    }
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
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return org.tigr.microarray.mev.cluster.clusterUtil.Cluster.GENE_CLUSTER;
    }

	/**
	 * Creates a popup menu.
	 */
	protected JPopupMenu createJPopupMenu(PopupListener listener) {
	    JPopupMenu popup = new JPopupMenu();
	    addMenuItems(popup, listener);
	    return popup;
	}

	/**
	 * Adds the viewer specific menu items.
	 */
	private void addMenuItems(JPopupMenu menu, PopupListener listener) {
	    JMenuItem menuItem;
	    menuItem = new JMenuItem("Save all clusters", GUIFactory.getIcon("save16.gif"));
	    menuItem.setActionCommand(SAVE_ALL_CLUSTERS_CMD);
	    menuItem.addActionListener(listener);
	    menu.add(menuItem);
	    
	    setOverallMaxMenuItem = new JMenuItem("Set Y to overall max...", GUIFactory.getIcon("Y_range_expand.gif"));
	    setOverallMaxMenuItem.setActionCommand(SET_Y_TO_EXPERIMENT_MAX_CMD);
	    setOverallMaxMenuItem.addActionListener(listener);
	    setOverallMaxMenuItem.setEnabled(false);
	    menu.add(setOverallMaxMenuItem);
	    
	    setClusterMaxMenuItem = new JMenuItem("Set Y to cluster max...", GUIFactory.getIcon("Y_range_expand.gif"));
	    setClusterMaxMenuItem.setActionCommand(SET_Y_TO_CLUSTER_MAX_CMD);
	    setClusterMaxMenuItem.addActionListener(listener);
	    menu.add(setClusterMaxMenuItem);
	}

	/**
	 * Saves all clusters.
	 */
	protected void onSaveClusters() {
	    Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
	    try {
	        ExperimentUtil.saveExperiment(frame, getExperiment(), getData(), getClusters());
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
	        e.printStackTrace();
	    }
	}


	/**
	 * The class to listen to mouse and action events.
	 */
	public class PopupListener extends MouseAdapter implements ActionListener {
	    
	    public void actionPerformed(ActionEvent e) {
	        String command = e.getActionCommand();
	        if (command.equals(SAVE_ALL_CLUSTERS_CMD)) {
	            onSaveClusters();
	        } else if(command.equals(SET_Y_TO_EXPERIMENT_MAX_CMD)){
	            setAllYRanges(CentroidViewer.USE_EXPERIMENT_MAX);
	            setClusterMaxMenuItem.setEnabled(true);
	            setOverallMaxMenuItem.setEnabled(false);
	            repaint();
	        } else if(command.equals(SET_Y_TO_CLUSTER_MAX_CMD)){
	            setAllYRanges(CentroidViewer.USE_CLUSTER_MAX);
	            setClusterMaxMenuItem.setEnabled(false);
	            setOverallMaxMenuItem.setEnabled(true);
	            repaint();
	        }
	    }
	    
	    private void setAllYRanges(int yRangeOption){
	        int numClusters = getClusters().length;
	        for(int i = 0; i < numClusters; i++){
	            centroidViewer.setClusterIndex(i);
	            centroidViewer.setYRangeOption(yRangeOption);
	        }
	    }
	    
	    public void mouseReleased(MouseEvent event) {
	        maybeShowPopup(event);
	    }
	    
	    public void mousePressed(MouseEvent event) {
	        maybeShowPopup(event);
	    }
	    
	    private void maybeShowPopup(MouseEvent e) {
	        if (!e.isPopupTrigger()) {
	            return;
	        }
	        popup.show(e.getComponent(), e.getX(), e.getY());
	    }
	}
}

