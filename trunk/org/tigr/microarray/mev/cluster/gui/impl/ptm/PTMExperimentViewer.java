/*
 Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
 All rights reserved.
 */
/*
 * $RCSfile: PTMExperimentViewer.java,v $
 * $Revision: 1.9 $
 * $Date: 2006-03-24 15:51:08 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.ptm;

import java.awt.Color;
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

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;


public class PTMExperimentViewer extends ExperimentViewer implements IViewer {
	
	protected static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
	protected static final String SET_DEF_COLOR_CMD = "set-def-color-cmd";
	protected static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
	protected static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";
	protected static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";
	
	private JPopupMenu popup;
	private ExperimentViewer expViewer;
	private PTMExperimentHeader header;
	private String[] auxTitles;
	private Object[][] auxData;    
	
	/**
	 * Constructs a <code>PTMExperimentViewer</code> with specified
	 * experiment, clusters and templateVector.
	 */
	public PTMExperimentViewer(Experiment experiment, int[][] clusters, Vector templateVector, String[] auxTitles, Object[][] auxData) {
		Listener listener = new Listener();
		this.popup = createJPopupMenu(listener);
		
		this.expViewer = new ExperimentViewer(experiment, clusters);
		this.expViewer.getContentComponent().addMouseListener(listener);
		this.auxTitles = auxTitles;
		this.auxData = auxData;        
		this.header = new PTMExperimentHeader(expViewer.getHeaderComponent(), templateVector);
		this.header.setColorImages(expViewer.getNegColorImage(), expViewer.getPosColorImage());
		this.header.setMissingColor(expViewer.getMissingColor());
		this.header.addMouseListener(listener);
	}
	public void setExperiment(Experiment e){expViewer.setExperiment(e);}
	public void setExperimentID(int i){expViewer.setExperimentID(i);}
	public int getExperimentID(){return expViewer.getExperimentID();}
	
    /**
     * 
     */ 
    public PTMExperimentViewer(ExperimentViewer exptViewer, PTMExperimentHeader exptHeader, String[] auxTitles, Object[][] auxData) {
    	this.expViewer = exptViewer;
    	this.header = exptHeader;
    	this.auxTitles = auxTitles;
    	this.auxData = auxData;
		Listener listener = new Listener();	
		this.popup = createJPopupMenu(listener);
		this.expViewer.getContentComponent().addMouseListener(listener);
		this.header.setColorImages(expViewer.getNegColorImage(), expViewer.getPosColorImage());
		this.header.setMissingColor(expViewer.getMissingColor());
		this.header.addMouseListener(listener);
	
    }
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new",
    			new Object[]{this.expViewer, this.header, this.auxTitles, this.auxData});
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
	 * Creates a popup menu.
	 */
	private JPopupMenu createJPopupMenu(Listener listener) {
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
	
	/**
	 * Saves clusters.
	 */
	private void onSaveClusters() {
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
	private void onSaveCluster() {
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
	 * Sets a public color.
	 */
	private void onSetColor() {
		Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
		Color newColor = JColorChooser.showDialog(frame, "Choose color", CentroidViewer.DEF_CLUSTER_COLOR);
		if (newColor != null) {
			expViewer.setClusterColor(newColor);
		}
	}
	
	/**
	 * Removes a public color.
	 */
	private void onSetDefaultColor() {
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
				expViewer.storeCluster();
			} else if(command.equals(LAUNCH_NEW_SESSION_CMD)){
				expViewer.launchNewSession();
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
