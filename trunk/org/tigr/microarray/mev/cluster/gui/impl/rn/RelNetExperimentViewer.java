/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: RelNetExperimentViewer.java,v $
 * $Revision: 1.5 $
 * $Date: 2006-03-24 15:51:24 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.rn;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JColorChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

public class RelNetExperimentViewer extends ExperimentViewer implements java.io.Serializable {

    private static final String SET_COLOR_CMD = "set-color-cmd";
    private static final String SET_DEF_COLOR_CMD = "set-def-color-cmd";
    private static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
    private static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";

    private JPopupMenu popup;

    /**
     * Constructs a <code>RelNetExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public RelNetExperimentViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
        Listener listener = new Listener();
        this.popup = createJPopupMenu(listener);
        getContentComponent().addMouseListener(listener);
        getHeaderComponent().addMouseListener(listener);
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
    public RelNetExperimentViewer(int[][] clusters, int[] samplesOrder, boolean drawAnnotations, ExperimentHeader header, Insets insets, Integer exptID) {
    	super(clusters, samplesOrder, drawAnnotations, header, insets, exptID);
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
    }
}
