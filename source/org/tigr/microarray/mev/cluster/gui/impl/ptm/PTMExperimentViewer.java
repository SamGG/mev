/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: PTMExperimentViewer.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.ptm;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;


public class PTMExperimentViewer extends ExperimentViewer implements IViewer {
	
	protected static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
	protected static final String SET_DEF_COLOR_CMD = "set-def-color-cmd";
	protected static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
	protected static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";
	protected static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";
	
	private ExperimentViewer expViewer;
	private PTMExperimentHeader header;
	private String[] auxTitles;
	private Object[][] auxData;    
	private int[][] clusters;
	private Vector templateVector;
	
	/**
     * State-saving constructor for MeV v4.4.
	 * Constructs a <code>PTMExperimentViewer</code> with specified
	 * experiment, clusters and templateVector.
	 */
	public PTMExperimentViewer(Experiment experiment, ClusterWrapper clusters, Vector templateVector, String[] auxTitles, Object[][] auxData) {
		this(experiment, clusters.getClusters(), templateVector, auxTitles, auxData);
	}
	/**
     * State-saving constructor used to load saved analysis files from MeV v4.0-4.3.
	 * @param experiment
	 * @param clusters
	 * @param templateVector
	 * @param auxTitles
	 * @param auxData
	 * @deprecated
	 */
	public PTMExperimentViewer(Experiment experiment, int[][] clusters, Vector templateVector, String[] auxTitles, Object[][] auxData) {
		
		this.expViewer = new ExperimentViewer(experiment, clusters);
		this.auxTitles = auxTitles;
		this.auxData = auxData;
		this.clusters = clusters;
		this.templateVector = templateVector;
		this.header = new PTMExperimentHeader(expViewer.getHeaderComponent(), templateVector);
		this.header.setColorImages(expViewer.getNegColorImage(), expViewer.getPosColorImage());
		this.header.setMissingColor(expViewer.getMissingColor());
	}
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new",
    			new Object[]{this.expViewer.getExperiment(), ClusterWrapper.wrapClusters(this.clusters), this.templateVector, this.auxTitles, this.auxData});
    }
	public void setExperiment(Experiment e){expViewer.setExperiment(e);}
	public void setExperimentID(int i){expViewer.setExperimentID(i);}
	public int getExperimentID(){return expViewer.getExperimentID();}

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
		return expViewer.getContentComponent();
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
	
	public BufferedImage getImage() {
		return expViewer.getImage();
	}
	
	/**
	 * Updates header and contents attributes when the viewer is selected.
	 */
	public void onSelected(IFramework framework) {
		expViewer.onSelected(framework);
		header.setCurrentCluster(((Integer)framework.getUserObject()).intValue());
		IDisplayMenu menu = framework.getDisplayMenu();
		header.setUseDoubleGradient(menu.getUseDoubleGradient());    
		header.setColorImages(menu.getPositiveGradientImage(), menu.getNegativeGradientImage());
		header.setValues(menu.getMinRatioScale(), menu.getMidRatioValue(), menu.getMaxRatioScale());
		header.setAntiAliasing(menu.isAntiAliasing());
		header.setDrawBorders(menu.isDrawingBorder());
		header.updateSize(menu.getElementSize());
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
		expViewer.onMenuChanged(menu);
		header.setUseDoubleGradient(menu.getUseDoubleGradient());
		header.setColorImages(menu.getPositiveGradientImage(), menu.getNegativeGradientImage());
		header.setValues(menu.getMinRatioScale(), menu.getMidRatioValue(), menu.getMaxRatioScale());
		header.setAntiAliasing(menu.isAntiAliasing());
		header.setDrawBorders(menu.isDrawingBorder());
		header.updateSize(menu.getElementSize());
	}
	
	public void onDeselected() {}
	public void onClosed() {}
	
	
	
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
	
	/**
	 * Saves clusters.
	 */
	protected void onSaveClusters() {
		Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
		try {
			//expViewer.saveClusters(frame);
			ExperimentUtil.saveAllGeneClustersWithAux(frame, expViewer.getExperiment(), expViewer.getData(), expViewer.getClusters(), auxTitles, auxData);            
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
			//expViewer.saveCluster(frame);
			ExperimentUtil.saveGeneClusterWithAux(frame, expViewer.getExperiment(), expViewer.getData(), expViewer.getCluster(), auxTitles, auxData);            
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	/**
	 * Removes a public color.
	 */
	protected void onSetDefaultColor() {
		expViewer.setClusterColor(null);
	}
	
	public int[][] getClusters() {
		return this.expViewer.getClusters();
	}
	
	public Experiment getExperiment() {
		return this.expViewer.getExperiment();
	}
	
	/** Returns int value indicating viewer type
	 * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
	 */
	public int getViewerType() {
		return this.expViewer.getViewerType();
	}
	
	/** //TODO may need this
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
				expViewer.storeCluster();
			} else if(command.equals(LAUNCH_NEW_SESSION_CMD)){
				expViewer.launchNewSession();
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
		
		public void mousePressed(MouseEvent event) {
			maybeShowPopup(event);
		}
		
		private void maybeShowPopup(MouseEvent e) {
			
			if (!e.isPopupTrigger() || expViewer.getCluster() == null || expViewer.getCluster().length == 0) {
				return;
			}
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	
}
