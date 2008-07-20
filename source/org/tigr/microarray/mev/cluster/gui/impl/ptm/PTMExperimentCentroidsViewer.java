/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: PTMExperimentCentroidsViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-03-24 15:51:08 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.ptm;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Expression;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

public class PTMExperimentCentroidsViewer extends PTMExperimentSubCentroidsViewer {
    
    private static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";
    private static final String SET_Y_TO_EXPERIMENT_MAX_CMD = "set-y-to-exp-max-cmd";
    private static final String SET_Y_TO_CLUSTER_MAX_CMD = "set-y-to-cluster-max-cmd";
    
    private JPopupMenu popup;
    private JMenuItem setOverallMaxMenuItem;
    private JMenuItem setClusterMaxMenuItem;
    
    private String[] auxTitles;
    private Object[][] auxData;    
    
    /**
     * Constructs a <code>PTMCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public PTMExperimentCentroidsViewer(Experiment experiment, int[][] clusters, Vector template, String[] auxTitles, Object[][] auxData) {
        super(experiment, clusters, template, auxTitles, auxData);
        Listener listener = new Listener();
        this.auxTitles = auxTitles;
        this.auxData = auxData;         
        this.popup = createJPopupMenu(listener);
        getContentComponent().addMouseListener(listener);
    }
    public PTMExperimentCentroidsViewer(PTMExperimentCentroidViewer cv, String[] auxTitles, Object[][] auxData){
    	super(cv);
    	this.auxTitles = auxTitles;
    	this.auxData = auxData;
        Listener listener = new Listener();
        this.auxTitles = auxTitles;
        this.auxData = auxData;         
	this.popup = createJPopupMenu(listener);
	getContentComponent().addMouseListener(listener);
    }    
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{this.centroidViewer, this.auxTitles, this.auxData});
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
    private void onSaveClusters() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        try {
            ExperimentUtil.saveAllExperimentClustersWithAux(frame, getExperiment(), getData(), getClusters(), auxTitles, auxData);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * The class to listen to mouse and action events.
     */
    private class Listener extends MouseAdapter implements ActionListener {
        
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

