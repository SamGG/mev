/*
 Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
 All rights reserved.
 */
/*
 * $RCSfile: SOMExperimentViewer.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-05-02 16:57:04 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.som;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.util.FloatMatrix;

public class SOMExperimentViewer implements IViewer {    
	
	private JPopupMenu popup;
	private ExperimentViewer expViewer;
	private CentroidExperimentHeader header;
	
	protected static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
	protected static final String SET_DEF_COLOR_CMD = "set-def-color-cmd";
	protected static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
	protected static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";
	protected static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";
	
	//EH stored for state-saving
	private FloatMatrix codes;
	private Experiment experiment;
	
	/**
	 * Constructs a <code>SOMExperimentViewer</code> with specified
	 * experiment, clusters and codes.
	 */
	public SOMExperimentViewer(Experiment experiment, int[][] clusters, FloatMatrix codes) {
		Listener listener = new Listener();
		this.popup = createJPopupMenu(listener);
		
		this.expViewer = new ExperimentViewer(experiment, clusters);
		this.expViewer.getContentComponent().addMouseListener(listener);
		
		this.header = new CentroidExperimentHeader(this.expViewer.getHeaderComponent(), codes, clusters, "SOM Vector");
		this.codes = codes;
		this.experiment = experiment; 
		
		//this.header = new SOMExperimentHeader(expViewer.getHeaderComponent(), codes, clusters);
		//this.header.setColorImages(expViewer.getPosColorImage(), expViewer.getNegColorImage());
		this.header.setNegAndPosColorImages(expViewer.getPosColorImage(), expViewer.getNegColorImage());
		this.header.setMissingColor(expViewer.getMissingColor());
		this.header.addMouseListener(listener);
		this.header.setBackground(Color.white);
		
	}

	public Expression getExpression(){
		return new Expression(this, this.getClass(), "new", 
				new Object[]{this.experiment, this.getClusters(), this.codes});
	}   
    
    public void setExperiment(Experiment e){
    	expViewer.setExperiment(e);
    }
    public void setExperimentID(int id){
    	expViewer.setExperimentID(id);
    }
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
		header.setNegAndPosColorImages(menu.getNegativeGradientImage(), menu.getPositiveGradientImage());
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
		header.setNegAndPosColorImages(menu.getNegativeGradientImage(), menu.getPositiveGradientImage());
		header.setValues(menu.getMinRatioScale(), menu.getMidRatioValue() ,menu.getMaxRatioScale());
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
			expViewer.saveClusters(frame);
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
			expViewer.saveCluster(frame);
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
		return expViewer.getClusters();
	}    
	
	/**  Returns the viewer's experiment or null
	 */
	public Experiment getExperiment() {
		return expViewer.getExperiment();
	}    
	
	/** Returns int value indicating viewer type
	 * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
	 */
	public int getViewerType() {
		return expViewer.getViewerType();        
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
			}  else if (command.equals(STORE_CLUSTER_CMD)) {
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
