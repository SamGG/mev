/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: KMCExperimentViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-08-22 18:01:26 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.kmc;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Expression;

import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.util.StringSplitter;
//import org.tigr.microarray.mev.cluster.gui.impl.kmc.KMCExperimentViewer.Listener;

public class KMCExperimentViewer extends ExperimentViewer {
    
    private JPopupMenu popup; 
    
    /**
     * Reconstitute a saved instance of this class from an XML file.
     * 
     * TODO 
     * Save clusters as a long, tab-delimited string rather than an int[][].  Same for samplesOrder.
     * 
     * @param experiment
     * @param clusters
     * @param samplesOrder
     * @param drawAnnotations
     * @param header
     * @param insets
     */
    public KMCExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder,
    		Boolean drawAnnotations){
    	super(e, clusters, samplesOrder, drawAnnotations.booleanValue());
        Listener listener = new Listener();
        this.popup = createJPopupMenu(listener);
        getContentComponent().addMouseListener(listener);
        getHeaderComponent().addMouseListener(listener);
        
    } 

    /**
     * Constructs a <code>KMCExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public KMCExperimentViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
        Listener listener = new Listener();
        this.popup = createJPopupMenu(listener);
        getContentComponent().addMouseListener(listener);
        getHeaderComponent().addMouseListener(listener);
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
     * Removes a public color.
     */
    private void onSetDefaultColor() {
        setClusterColor(null);
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
            } else if (command.equals(STORE_CLUSTER_CMD)) {
                storeCluster();
            } else if (command.equals(SET_DEF_COLOR_CMD)) {
                onSetDefaultColor();
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
}
