/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: RelNetExperimentViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-05-02 16:57:04 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.rn;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

public class RelNetExperimentViewer extends ExperimentViewer {

    private static final String SET_COLOR_CMD = "set-color-cmd";
    private static final String SET_DEF_COLOR_CMD = "set-def-color-cmd";
    private static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
    private static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";

    /**
     * Constructs a <code>RelNetExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public RelNetExperimentViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }
    /**
     * This constructor is used to re-create an ExperimentViewer from information
     * stored in a saved analysis file by XMLEncoder.  
     * 
     * @param experiment
     * @param clusters
     * @param samplesOrder
     * @param drawAnnotations
     * @param header
     * @param insets
     */
    public RelNetExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder, boolean drawAnnotations, ExperimentHeader header, Insets insets) {
    	super(e, clusters, samplesOrder, drawAnnotations, header, insets);
    }
    public RelNetExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder, boolean drawAnnotations) {
    	super(e, clusters, samplesOrder, drawAnnotations, new ExperimentHeader(e, clusters), new Insets(0,10,0,0));
    }
    
	/**
	 * Creates a popup menu.
	 *
	protected JPopupMenu createJPopupMenu(ActionListener listener) {
	    JPopupMenu popup = new JPopupMenu();
	    this.addMenuItems(popup, listener);
	    return popup;
	}
	
    /**
     * Adds the viewer specific menu items.
     *
    protected void addMenuItems(JPopupMenu menu, ActionListener listener) {
        JMenuItem menuItem;

        menuItem = new JMenuItem("Set public cluster...", GUIFactory.getIcon("new16.gif"));
        menuItem.setActionCommand(SET_COLOR_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("Delete public cluster", GUIFactory.getIcon("delete16.gif"));
        menuItem.setActionCommand(SET_DEF_COLOR_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

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
     * The class to listen to mouse and action events.
     *
    private class RNPopupListener extends MouseAdapter implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals(SAVE_CLUSTER_CMD)) {
                onSaveCluster();
            } else if (command.equals(SAVE_ALL_CLUSTERS_CMD)) {
                onSaveClusters();
            } else if (command.equals(SET_COLOR_CMD)) {
                onSetColor();
            } else if (command.equals(SET_DEF_COLOR_CMD)) {
                onSetDefaultColor();
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
    }*/
}
