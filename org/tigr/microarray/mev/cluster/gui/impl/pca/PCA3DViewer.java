/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: PCA3DViewer.java,v $
 * $Revision: 1.2 $
 * $Date: 2003-12-08 18:16:07 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.pca;

import java.awt.Frame;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JOptionPane;
import javax.swing.JCheckBoxMenuItem;

import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.gui.Experiment;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;

public class PCA3DViewer extends ViewerAdapter {
    
    private static final String RESET_CMD   = "reset-cmd";
    private static final String OPTIONS_CMD = "options-cmd";
    private static final String SELECTION_AREA_CMD = "select-cmd";
    private static final String SAVE_CMD    = "save-cmd";
    private static final String SHOW_SELECTION_CMD = "show-selection-cmd";
    private static final String HIDE_SELECTION_BOX_CMD = "hide-selection-box-cmd";
    private static final String SHOW_SPHERES_CMD = "show-spheres-cmd";
    private static final String SHOW_TEXT_CMD = "show-text-cmd";
    private static final String WHITE_CMD = "white-cmd";
    private static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
    private static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";
    
    private IData data;
    private Experiment experiment;
    private Content3D content;
    private JPopupMenu popup;
    private Frame frame;
    private boolean geneViewer;
    private IFramework framework;
    
    /**
     * Constructs a <code>PCA3DViewer</code> with specified mode,
     * U-matrix and an experiment data.
     */
    public PCA3DViewer(Frame frame, int mode, FloatMatrix U, Experiment experiment, boolean geneViewer) {
        this.frame = frame;
        this.experiment = experiment;
        this.geneViewer = geneViewer;
        content = createContent(mode, U, experiment, geneViewer);
        popup = createJPopupMenu();
    }
    
    /**
     * Updates the viewer data and its content.
     */
    public void onSelected(IFramework framework) {
        this.framework = framework;
        this.data = framework.getData();
        content.setData(this.data);
        content.updateScene();
    }
    
    /**
     * Updates the viewer data and its content.
     */
    public void onDataChanged(IData data) {
        this.data = data;
        content.setData(data);
        content.updateScene();
    }
    
    /**
     * Returns a content of the viewer.
     */
    public JComponent getContentComponent() {
        return content;
    }
    
    /**
     * Returns a content image.
     */
    public BufferedImage getImage() {
        return content.createImage();
    }
    
    /**
     * Creates a 3D content with specified mode, u-matrix and experiment.
     */
    private Content3D createContent(int mode, FloatMatrix U, Experiment experiment, boolean geneViewer) {
        return new Content3D(mode, U, experiment, geneViewer);
    }
    
    /**
     * Returns the viewer popup menu.
     */
    public JPopupMenu getJPopupMenu() {
        return popup;
    }
    
    /**
     * Creates the viewer popup menu.
     */
    private JPopupMenu createJPopupMenu() {
        JPopupMenu popup = new JPopupMenu();
        addMenuItems(popup);
        return popup;
    }
    
    /**
     * Adds the viewer specific menu items.
     */
    private void addMenuItems(JPopupMenu menu) {
        Listener listener = new Listener();
        JMenuItem menuItem;
        menuItem = new JMenuItem("Reset", GUIFactory.getIcon("refresh16.gif"));
        menuItem.setActionCommand(RESET_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Options...", GUIFactory.getIcon("edit16.gif"));
        menuItem.setActionCommand(OPTIONS_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Selection area...", GUIFactory.getIcon("edit16.gif"));
        menuItem.setEnabled(false);
        menuItem.setActionCommand(SELECTION_AREA_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Store cluster...", GUIFactory.getIcon("new16.gif"));
        menuItem.setEnabled(false);
        menuItem.setActionCommand(STORE_CLUSTER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Launch new session", GUIFactory.getIcon("launch_new_mav.gif"));
        menuItem.setEnabled(false);
        menuItem.setActionCommand(LAUNCH_NEW_SESSION_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Save cluster...", GUIFactory.getIcon("save16.gif"));
        menuItem.setEnabled(false);
        menuItem.setActionCommand(SAVE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        menu.addSeparator();
        
        menuItem = new JCheckBoxMenuItem("Show selection area");
        menuItem.setActionCommand(SHOW_SELECTION_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JCheckBoxMenuItem("Hide selection box");
        menuItem.setEnabled(false);
        menuItem.setActionCommand(HIDE_SELECTION_BOX_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JCheckBoxMenuItem("Show spheres");
        menuItem.setActionCommand(SHOW_SPHERES_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JCheckBoxMenuItem("Show text");
        menuItem.setEnabled(true);
        menuItem.setActionCommand(SHOW_TEXT_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JCheckBoxMenuItem("White background");
        menuItem.setActionCommand(WHITE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
    }
    
    /**
     * Returns a menu item by specified action command.
     */
    private JMenuItem getJMenuItem(String command) {
        JMenuItem item;
        Component[] components = popup.getComponents();
        for (int i=0; i<components.length; i++) {
            if (components[i] instanceof JMenuItem) {
                if (((JMenuItem)components[i]).getActionCommand().equals(command))
                    return(JMenuItem)components[i];
            }
        }
        return null;
    }
    
    /**
     * Sets a menu item state.
     */
    private void setEnableMenuItem(String command, boolean enable) {
        JMenuItem item = getJMenuItem(command);
        if (item == null) {
            return;
        }
        item.setEnabled(enable);
    }
    
    /**
     * Reset the viewer content.
     */
    private void onReset() {
        content.reset();
    }
    
    /**
     * Sets the user specified content parameters.
     */
    private void onOptions() {
        PCAResultConfigDialog dlg = new PCAResultConfigDialog(frame,
        content.getPointSize(), content.getSelectedPointSize(),
        content.getScaleAxisX(), content.getScaleAxisY(), content.getScaleAxisZ());
        if (dlg.showModal() == JOptionPane.OK_OPTION) {
            content.setPointSize(dlg.getPointSize());
            content.setSelectedPointSize(dlg.getSelectedPointSize());
            content.setScale(dlg.getScaleAxisX(), dlg.getScaleAxisY(), dlg.getScaleAxisZ());
            content.updateScene();
        }
    }
    
    /**
     * Sets the user specified selection area parameters.
     */
    private void onSelectionArea() {
        PCASelectionAreaDialog dlg = new PCASelectionAreaDialog(frame,
        content.getPositionX(), content.getPositionY(), content.getPositionZ(),
        content.getSizeX(), content.getSizeY(), content.getSizeZ());
        if (dlg.showModal() == JOptionPane.OK_OPTION) {
            content.setBoxPosition(dlg.getPositionX(), dlg.getPositionY(), dlg.getPositionZ());
            content.setBoxSize(dlg.getSizeX(), dlg.getSizeY(), dlg.getSizeZ());
            content.updateScene();
        }
    }
    
    /**
     * Saves selected genes.
     */
    private void onSave() {
        try {
            if(geneViewer)
                ExperimentUtil.saveExperiment(frame, experiment, data, content.getSelectedGenes());
            else
                ExperimentUtil.saveExperimentCluster(frame, experiment, data, content.getSelectedGenes());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save matrix!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Stores the selected cluster
     */
    private void storeCluster(){
        if(geneViewer)
            framework.storeCluster( content.getSelectedGenes(), experiment, Cluster.GENE_CLUSTER);
        else
            framework.storeCluster( content.getSelectedGenes(), experiment, Cluster.EXPERIMENT_CLUSTER);
        content.setSelection(false);
        onHideSelection(); 
        this.onDataChanged(this.data);
        content.updateScene();
        
    }
    
    
    /**
     * Launches a new MultipleArrayViewer using selected elements
     */
    private void launchNewSession(){
        if(geneViewer)
            framework.launchNewMAV(content.getSelectedGenes(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.GENE_CLUSTER);
        else
            framework.launchNewMAV(content.getSelectedGenes(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.EXPERIMENT_CLUSTER);        
    }
    
    /**
     * Handles the selection box state.
     */
    private void onShowSelection() {
        JMenuItem selectionItem = getJMenuItem(SHOW_SELECTION_CMD);
        JMenuItem hideBoxItem = getJMenuItem(HIDE_SELECTION_BOX_CMD);
        JMenuItem selectionAreaItem = getJMenuItem(SELECTION_AREA_CMD);
        JMenuItem saveClusterItem = getJMenuItem(SAVE_CMD);
        JMenuItem storeClusterItem = getJMenuItem(STORE_CLUSTER_CMD);
        JMenuItem launchNewItem = getJMenuItem(LAUNCH_NEW_SESSION_CMD);
        if (selectionItem.isSelected()) {
            content.setSelection(true);
            content.setSelectionBox(!hideBoxItem.isSelected());
            selectionAreaItem.setEnabled(true);
            saveClusterItem.setEnabled(true);
            hideBoxItem.setEnabled(true);
            storeClusterItem.setEnabled(true);
            launchNewItem.setEnabled(true);
        } else {
            content.setSelection(false);
            content.setSelectionBox(false);
            selectionAreaItem.setEnabled(false);
            saveClusterItem.setEnabled(false);
            hideBoxItem.setEnabled(false);
            storeClusterItem.setEnabled(false);
            launchNewItem.setEnabled(false);
        }
        content.updateScene();
    }
    
    /**
     * Hides a content selection box.
     */
    private void onHideSelection() {
        content.setSelectionBox(!content.isSelectionBox());
        content.updateScene();
    }
    
    /**
     * Shows or hides spheres.
     */
    private void onShowSphere() {
        content.setShowSpheres(!content.isShowSpheres());
        content.updateScene();
        
        JMenuItem sphereItem = getJMenuItem(SHOW_SPHERES_CMD);
        JMenuItem textItem = getJMenuItem(SHOW_TEXT_CMD);
        if (sphereItem.isSelected()) {
            content.setShowSpheres(true);
            content.setShowText(textItem.isSelected());
            textItem.setEnabled(true);
        } else {
            content.setShowSpheres(false);
            content.setShowText(false);
            textItem.setEnabled(true);
        }
        content.updateScene();
    }
    
    /**
     * Shows or hide content text.
     */
    private void onShowText() {
        content.setShowText(!content.isShowText());
        content.updateScene();
    }
    
    /**
     * Sets content background.
     */
    private void onWhiteBackground() {
        content.setWhiteBackround(!content.isWhiteBackground());
        content.updateScene();
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }
    
    /**
     * The listener to listen to menu items events.
     */
    private class Listener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (command.equals(RESET_CMD)) {
                onReset();
            } else if (command.equals(OPTIONS_CMD)) {
                onOptions();
            } else if (command.equals(SELECTION_AREA_CMD)) {
                onSelectionArea();
            } else if (command.equals(SAVE_CMD)) {
                onSave();
            } else if (command.equals(SHOW_SELECTION_CMD)) {
                onShowSelection();
            } else if (command.equals(HIDE_SELECTION_BOX_CMD)) {
                onHideSelection();
            } else if (command.equals(SHOW_SPHERES_CMD)) {
                onShowSphere();
            } else if (command.equals(SHOW_TEXT_CMD)) {
                onShowText();
            } else if (command.equals(WHITE_CMD)) {
                onWhiteBackground();
            } else if (command.equals(STORE_CLUSTER_CMD)){
                storeCluster();
            } else if (command.equals(LAUNCH_NEW_SESSION_CMD)){
                launchNewSession();
            }
        }
    }
}
