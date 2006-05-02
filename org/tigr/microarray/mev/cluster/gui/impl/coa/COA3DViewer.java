/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * COA3DViewer.java
 *
 * Created on September 20, 2004, 1:20 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.coa;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.Expression;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;
import org.tigr.util.FloatMatrix;

/**
 *
 * @author  nbhagaba
 */
public class COA3DViewer  extends ViewerAdapter {  
    
    private static final String RESET_CMD   = "reset-cmd";
    private static final String OPTIONS_CMD = "options-cmd";
    private static final String SELECTION_AREA_CMD = "select-cmd";
    private static final String SAVE_CMD    = "save-cmd";
    private static final String SAVE_GENE_CLUSTER_CMD    = "save-genes-cmd";
    private static final String SAVE_EXPT_CLUSTER_CMD    = "save-expts-cmd";
    private static final String SHOW_SELECTION_CMD = "show-selection-cmd";
    private static final String HIDE_SELECTION_BOX_CMD = "hide-selection-box-cmd";
    private static final String SHOW_SPHERES_CMD = "show-spheres-cmd";
    private static final String SHOW_TEXT_CMD = "show-text-cmd";
    private static final String SHOW_GENE_TEXT_FROM_BOTH_CMD = "show-gene-test-from-both-cmd";
    private static final String SHOW_EXPT_TEXT_FROM_BOTH_CMD = "show-expt-test-from-both-cmd";
    private static final String WHITE_CMD = "white-cmd";
    private static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
    private static final String STORE_GENE_CLUSTER_CMD = "store-gene-cluster-cmd";
    private static final String STORE_EXPT_CLUSTER_CMD = "store-expt-cluster-cmd";
    private static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";
    private static final String LAUNCH_NEW_GENE_SESSION_CMD = "launch-new-gene-session-cmd";
    private static final String LAUNCH_NEW_EXPT_SESSION_CMD = "launch-new-expt-session-cmd";
    
    private IData data;
    private Experiment experiment;
    private COAContent3D content;
    //private JPanel content;
    private JPopupMenu popup, popup2;
    private Frame frame;
    //private boolean geneViewer;
    private int geneOrExpt;
    private int xAxis, yAxis, zAxis;    
    private int labelIndex = -1;    
    private IFramework framework;
    
    private FloatMatrix geneUMatrix, exptUMatrix, U;
    private COASelectionAreaDialog dlg;
    //private int mode;    
    private int exptID = 0;
    
    /** Creates a new instance of COA3DViewer */
    public COA3DViewer(Frame frame, FloatMatrix U, Experiment experiment, int geneOrExpt) {
        this.frame = frame;
        this.experiment = experiment;
        this.exptID = experiment.getId();
        this.geneOrExpt = geneOrExpt;
        this.U = U;
        //this.mode = mode;
        content = createContent(U, experiment, geneOrExpt);
        dlg = new COASelectionAreaDialog(content, frame, content.getPositionX(), content.getPositionY(), content.getPositionZ(), content.getSizeX(), content.getSizeY(), content.getSizeZ(), content.getMaxValue());
        //content = new JPanel();
        popup = createJPopupMenu();    
         
        Listener listener2 = new Listener(); // this was an attempt to get the pop up menu to show up over the 3D viewer
        popup2 = createJPopupMenu(listener2); //didn't work because of the native mouse response behavior of the 3D API, but left it in for possible future use  
	getContentComponent().addMouseListener(listener2);        
    }
    
    public COA3DViewer(Frame frame, FloatMatrix U, Experiment experiment, int geneOrExpt, int xAxis, int yAxis, int zAxis) {
        this.frame = frame;
        this.experiment = experiment;
        this.exptID = experiment.getId();
        this.geneOrExpt = geneOrExpt;
        this.U = U;
        this.xAxis = xAxis;
        this.yAxis= yAxis;
        this.zAxis = zAxis;        
        //this.mode = mode;
        content = createContent(U, experiment, geneOrExpt, xAxis, yAxis, zAxis);
        dlg = new COASelectionAreaDialog(content, frame, content.getPositionX(), content.getPositionY(), content.getPositionZ(), content.getSizeX(), content.getSizeY(), content.getSizeZ(), content.getMaxValue());
        //content = new JPanel();
        popup = createJPopupMenu();    
         
        Listener listener2 = new Listener(); // this was an attempt to get the pop up menu to show up over the 3D viewer
        popup2 = createJPopupMenu(listener2); //didn't work because of the native mouse response behavior of the 3D API, but left it in for possible future use  
	getContentComponent().addMouseListener(listener2);        
    }    
    
    public COA3DViewer(Frame frame, FloatMatrix geneUMatrix, FloatMatrix exptUMatrix, Experiment experiment, int geneOrExpt) {
        this.frame = frame;
        this.experiment = experiment;
        this.exptID = experiment.getId();
        this.geneOrExpt = geneOrExpt;
        this.geneUMatrix = geneUMatrix;
        this.exptUMatrix = exptUMatrix;
        content = createContent(geneUMatrix, exptUMatrix, experiment, geneOrExpt);
        dlg = new COASelectionAreaDialog(content, frame, content.getPositionX(), content.getPositionY(), content.getPositionZ(), content.getSizeX(), content.getSizeY(), content.getSizeZ(), content.getMaxValue());        
        popup = createJPopupMenu(); 
        
        Listener listener2 = new Listener(); // this was an attempt to get the pop up menu to show up over the 3D viewer
        popup2 = createJPopupMenu(listener2); //didn't work because of the native mouse response behavior of the 3D API, but left it in for possible future use  
	getContentComponent().addMouseListener(listener2);        
    }
    
    public COA3DViewer(Frame frame, FloatMatrix geneUMatrix, FloatMatrix exptUMatrix, Experiment experiment, int geneOrExpt, int xAxis, int yAxis, int zAxis) {
        this.frame = frame;
        this.experiment = experiment;
        this.exptID = experiment.getId();
        this.geneOrExpt = geneOrExpt;
        this.geneUMatrix = geneUMatrix;
        this.exptUMatrix = exptUMatrix;
        this.xAxis = xAxis;
        this.yAxis= yAxis;
        this.zAxis = zAxis;        
        content = createContent(geneUMatrix, exptUMatrix, experiment, geneOrExpt, xAxis, yAxis, zAxis);
        dlg = new COASelectionAreaDialog(content, frame, content.getPositionX(), content.getPositionY(), content.getPositionZ(), content.getSizeX(), content.getSizeY(), content.getSizeZ(), content.getMaxValue());        
        //content = new JPanel();
        popup = createJPopupMenu(); 
        
        Listener listener2 = new Listener(); // this was an attempt to get the pop up menu to show up over the 3D viewer
        popup2 = createJPopupMenu(listener2); //didn't work because of the native mouse response behavior of the 3D API, but left it in for possible future use  
        getContentComponent().addMouseListener(listener2);        
    }    
    
    
    public COA3DViewer(Experiment e, FloatMatrix geneUMatrix, FloatMatrix exptUMatrix, Integer geneOrExpt, FloatMatrix U, Integer xAxis, Integer yAxis, Integer zAxis) {
	    this.geneOrExpt = geneOrExpt.intValue();
        this.geneUMatrix = geneUMatrix;
        this.exptUMatrix = exptUMatrix;
        this.U = U;
        this.xAxis = xAxis.intValue();
        this.yAxis = yAxis.intValue();
        this.zAxis = zAxis.intValue();   
        setExperiment(e);
	}
    /**
     * @inheritDoc
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{this.experiment, geneUMatrix, exptUMatrix, new Integer(geneOrExpt), U, new Integer(xAxis), new Integer(yAxis), new Integer(zAxis)});
    }
    /**
     * @inheritDoc
     */
    public void setExperiment(Experiment e){
    	this.experiment = e;
    	this.exptID = experiment.getId();
	    if (this.geneOrExpt == COAGUI.BOTH) {
	        content = createContent(geneUMatrix, exptUMatrix, experiment, geneOrExpt, xAxis, yAxis, zAxis);
	        dlg = new COASelectionAreaDialog(content, frame, content.getPositionX(), content.getPositionY(), content.getPositionZ(), content.getSizeX(), content.getSizeY(), content.getSizeZ(), content.getMaxValue());            
	    } else {
	        content = createContent(U, experiment, geneOrExpt, xAxis, yAxis, zAxis);
	        dlg = new COASelectionAreaDialog(content, frame, content.getPositionX(), content.getPositionY(), content.getPositionZ(), content.getSizeX(), content.getSizeY(), content.getSizeZ(), content.getMaxValue());
	    }   
	    popup = createJPopupMenu(); 
        
    }
    public Experiment getExperiment(){return this.experiment;}
    public int getExperimentID(){return this.exptID;}
    
    /**
     * Updates the viewer data and its content.
     */
    public void onSelected(IFramework framework) {
        this.framework = framework;
        this.frame = framework.getFrame();
        this.data = framework.getData();
        IDisplayMenu menu = framework.getDisplayMenu();        
        labelIndex = menu.getLabelIndex();        
        content.setData(this.data);
        content.setGeneLabelIndex(labelIndex);
        onMenuChanged(menu);
        content.updateScene();
        
        //In case it is viewed after serialization
        if(popup == null){
            popup = createJPopupMenu(); 
            DefaultMutableTreeNode node = framework.getCurrentNode();
            if(node != null){
                if(node.getUserObject() instanceof LeafInfo){
                    LeafInfo leafInfo = (LeafInfo) node.getUserObject();
                    leafInfo.setPopupMenu(this.popup);
                }
            }
        }    
    }  
    
    public void onMenuChanged(IDisplayMenu menu) {
        labelIndex = menu.getLabelIndex();  
        content.setGeneLabelIndex(labelIndex);  
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
    private COAContent3D createContent(FloatMatrix U, Experiment experiment, int geneOrExpt) {
        return new COAContent3D(U, experiment, geneOrExpt);
    }    
    
    private COAContent3D createContent(FloatMatrix geneUMatrix, FloatMatrix exptUMatrix, Experiment experiment, int geneOrExpt) {
        return new COAContent3D(geneUMatrix, exptUMatrix, experiment, geneOrExpt);
    }    
    
    private COAContent3D createContent(FloatMatrix U, Experiment experiment, int geneOrExpt, int xAxis, int yaxis, int zAxis) {
        return new COAContent3D(U, experiment, geneOrExpt, xAxis, yAxis, zAxis);
    }    
    
    private COAContent3D createContent(FloatMatrix geneUMatrix, FloatMatrix exptUMatrix, Experiment experiment, int geneOrExpt, int xAxis, int yaxis, int zAxis) {
        return new COAContent3D(geneUMatrix, exptUMatrix, experiment, geneOrExpt, xAxis, yAxis, zAxis);
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
        
        menu.addSeparator();
        
        if (this.geneOrExpt == COAGUI.BOTH) {
            menuItem = new JMenuItem("Store gene cluster...", GUIFactory.getIcon("new16.gif"));
            menuItem.setEnabled(false);
            menuItem.setActionCommand(STORE_GENE_CLUSTER_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
            
            menuItem = new JMenuItem("Launch new session with selected genes", GUIFactory.getIcon("launch_new_mav.gif"));
            menuItem.setEnabled(false);
            menuItem.setActionCommand(LAUNCH_NEW_GENE_SESSION_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
            
            menuItem = new JMenuItem("Save gene cluster...", GUIFactory.getIcon("save16.gif"));
            menuItem.setEnabled(false);
            menuItem.setActionCommand(SAVE_GENE_CLUSTER_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
            
            menuItem = new JCheckBoxMenuItem("Show gene text");
            menuItem.setEnabled(true);
            menuItem.setActionCommand(SHOW_GENE_TEXT_FROM_BOTH_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);          
            menu.addSeparator(); 
            
            menuItem = new JMenuItem("Store sample cluster...", GUIFactory.getIcon("new16.gif"));
            menuItem.setEnabled(false);
            menuItem.setActionCommand(STORE_EXPT_CLUSTER_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
            
            menuItem = new JMenuItem("Launch new session with selected samples", GUIFactory.getIcon("launch_new_mav.gif"));
            menuItem.setEnabled(false);
            menuItem.setActionCommand(LAUNCH_NEW_EXPT_SESSION_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
            
            menuItem = new JMenuItem("Save sample cluster...", GUIFactory.getIcon("save16.gif"));
            menuItem.setEnabled(false);
            menuItem.setActionCommand(SAVE_EXPT_CLUSTER_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
            
            menuItem = new JCheckBoxMenuItem("Show sample text");
            menuItem.setEnabled(true);
            menuItem.setActionCommand(SHOW_EXPT_TEXT_FROM_BOTH_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);            
            menu.addSeparator();            
        }
        else {
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
            
            menuItem = new JCheckBoxMenuItem("Show text");
            menuItem.setEnabled(true);
            menuItem.setActionCommand(SHOW_TEXT_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);            
            menu.addSeparator();
        }
        
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
        
        /*
        menuItem = new JCheckBoxMenuItem("Show text");
        menuItem.setEnabled(true);
        menuItem.setActionCommand(SHOW_TEXT_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        */
        menuItem = new JCheckBoxMenuItem("White background");
        menuItem.setActionCommand(WHITE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
    }   
    
    /**
     * Adds the viewer specific menu items.
     */
    private void addMenuItems(JPopupMenu menu, ActionListener listener) {
        //Listener listener = new Listener();
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
        
        menu.addSeparator();
        
        if (this.geneOrExpt == COAGUI.BOTH) {
            menuItem = new JMenuItem("Store gene cluster...", GUIFactory.getIcon("new16.gif"));
            menuItem.setEnabled(false);
            menuItem.setActionCommand(STORE_GENE_CLUSTER_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
            
            menuItem = new JMenuItem("Launch new session with selected genes", GUIFactory.getIcon("launch_new_mav.gif"));
            menuItem.setEnabled(false);
            menuItem.setActionCommand(LAUNCH_NEW_GENE_SESSION_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
            
            menuItem = new JMenuItem("Save gene cluster...", GUIFactory.getIcon("save16.gif"));
            menuItem.setEnabled(false);
            menuItem.setActionCommand(SAVE_GENE_CLUSTER_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
            
            menuItem = new JCheckBoxMenuItem("Show gene text");
            menuItem.setEnabled(true);
            menuItem.setActionCommand(SHOW_GENE_TEXT_FROM_BOTH_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);          
            menu.addSeparator();            
            
            menuItem = new JMenuItem("Store sample cluster...", GUIFactory.getIcon("new16.gif"));
            menuItem.setEnabled(false);
            menuItem.setActionCommand(STORE_EXPT_CLUSTER_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
            
            menuItem = new JMenuItem("Launch new session with selected samples", GUIFactory.getIcon("launch_new_mav.gif"));
            menuItem.setEnabled(false);
            menuItem.setActionCommand(LAUNCH_NEW_EXPT_SESSION_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
            
            menuItem = new JMenuItem("Save sample cluster...", GUIFactory.getIcon("save16.gif"));
            menuItem.setEnabled(false);
            menuItem.setActionCommand(SAVE_EXPT_CLUSTER_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);
            
            menuItem = new JCheckBoxMenuItem("Show sample text");
            menuItem.setEnabled(true);
            menuItem.setActionCommand(SHOW_EXPT_TEXT_FROM_BOTH_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);          
            menu.addSeparator();            
                      
        }
        else {
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
            
            menuItem = new JCheckBoxMenuItem("Show text");
            menuItem.setEnabled(true);
            menuItem.setActionCommand(SHOW_TEXT_CMD);
            menuItem.addActionListener(listener);
            menu.add(menuItem);            
            menu.addSeparator();
        }
        
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
        /*
        menuItem = new JCheckBoxMenuItem("Show text");
        menuItem.setEnabled(true);
        menuItem.setActionCommand(SHOW_TEXT_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        */
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
        COAResultConfigDialog dlg = new COAResultConfigDialog(frame,
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
        //COASelectionAreaDialog dlg = new COASelectionAreaDialog(frame,
        //content.getPositionX(), content.getPositionY(), content.getPositionZ(),
        //content.getSizeX(), content.getSizeY(), content.getSizeZ());
        //COASelectionAreaDialog dlg = new COASelectionAreaDialog(content, frame, content.getPositionX(), content.getPositionY(), content.getPositionZ(), content.getSizeX(), content.getSizeY(), content.getSizeZ(), content.getMaxValue());
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
            if(geneOrExpt == COAGUI.GENES)
                ExperimentUtil.saveExperiment(frame, experiment, data, content.getSelectedGenes());
            else if (geneOrExpt == COAGUI.EXPTS)
                ExperimentUtil.saveExperimentCluster(frame, experiment, data, content.getSelectedGenes());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save matrix!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void onSaveGenesFromBoth() {
        try {
            ExperimentUtil.saveExperiment(frame, experiment, data, content.getSelectedGenesFromBoth());            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save matrix!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void onSaveExptsFromBoth() {
        try {
            ExperimentUtil.saveExperimentCluster(frame, experiment, data, content.getSelectedExptsFromBoth());            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save matrix!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }    
    
    /**
     * Stores the selected cluster
     */
    private void storeCluster(){
        if(geneOrExpt == COAGUI.GENES)
            framework.storeSubCluster( content.getSelectedGenes(), experiment, Cluster.GENE_CLUSTER);
        else if (geneOrExpt == COAGUI.EXPTS)
            framework.storeSubCluster( content.getSelectedGenes(), experiment, Cluster.EXPERIMENT_CLUSTER);
        content.setSelection(false);
        //onHideSelection(); 
        this.onDataChanged(this.data);
        content.updateScene();        
    }
    
    private void storeGeneClusterFromBoth() {
        framework.storeSubCluster( content.getSelectedGenesFromBoth(), experiment, Cluster.GENE_CLUSTER);
        content.setSelection(false);
        //onHideSelection(); 
        this.onDataChanged(this.data);
        content.updateScene();        
    }
    
    private void storeExptClusterFromBoth() {
        framework.storeSubCluster( content.getSelectedExptsFromBoth(), experiment, Cluster.EXPERIMENT_CLUSTER);     
        content.setSelection(false);
        //onHideSelection(); 
        this.onDataChanged(this.data);
        content.updateScene();        
    }
    
    
    /**
     * Launches a new MultipleArrayViewer using selected elements
     */
    private void launchNewSession(){
        if(geneOrExpt == COAGUI.GENES)
            framework.launchNewMAV(content.getSelectedGenes(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.GENE_CLUSTER);
        else if (geneOrExpt == COAGUI.EXPTS)
            framework.launchNewMAV(content.getSelectedGenes(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.EXPERIMENT_CLUSTER);        
    }
    
    private void launchNewGeneSessionFromBoth() {
        framework.launchNewMAV(content.getSelectedGenesFromBoth(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.GENE_CLUSTER);
    }
    
    private void launchNewExptSessionFromBoth() {
       framework.launchNewMAV(content.getSelectedExptsFromBoth(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.EXPERIMENT_CLUSTER); 
    }
    
    /**
     * Handles the selection box state.
     */
    private void onShowSelection() {
        JMenuItem selectionItem = getJMenuItem(SHOW_SELECTION_CMD);
        JMenuItem hideBoxItem = getJMenuItem(HIDE_SELECTION_BOX_CMD);
        JMenuItem selectionAreaItem = getJMenuItem(SELECTION_AREA_CMD);
        if ((geneOrExpt == COAGUI.GENES) || (geneOrExpt == COAGUI.EXPTS)) {
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
        } else {
            JMenuItem saveGeneClusterItem = getJMenuItem(SAVE_GENE_CLUSTER_CMD);
            JMenuItem storeGeneClusterItem = getJMenuItem(STORE_GENE_CLUSTER_CMD);
            JMenuItem launchNewGeneItem = getJMenuItem(LAUNCH_NEW_GENE_SESSION_CMD);
            JMenuItem saveExptClusterItem = getJMenuItem(SAVE_EXPT_CLUSTER_CMD);
            JMenuItem storeExptClusterItem = getJMenuItem(STORE_EXPT_CLUSTER_CMD);
            JMenuItem launchNewExptItem = getJMenuItem(LAUNCH_NEW_EXPT_SESSION_CMD);
            if (selectionItem.isSelected()) {
                content.setSelection(true);
                content.setSelectionBox(!hideBoxItem.isSelected());
                selectionAreaItem.setEnabled(true);
                saveGeneClusterItem.setEnabled(true);
                saveExptClusterItem.setEnabled(true);
                hideBoxItem.setEnabled(true);
                storeGeneClusterItem.setEnabled(true);
                storeExptClusterItem.setEnabled(true);
                launchNewGeneItem.setEnabled(true);  
                launchNewExptItem.setEnabled(true);
            } else {
                content.setSelection(false);
                content.setSelectionBox(false);
                selectionAreaItem.setEnabled(false);
                saveGeneClusterItem.setEnabled(false);
                saveExptClusterItem.setEnabled(false);
                hideBoxItem.setEnabled(false);
                storeGeneClusterItem.setEnabled(false);
                storeExptClusterItem.setEnabled(false);
                launchNewGeneItem.setEnabled(false);  
                launchNewExptItem.setEnabled(false);                
            }
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
        if ((geneOrExpt == COAGUI.GENES) || (geneOrExpt == COAGUI.EXPTS)) {
            JMenuItem sphereItem = getJMenuItem(SHOW_SPHERES_CMD);
            JMenuItem textItem = getJMenuItem(SHOW_TEXT_CMD);
            if (sphereItem.isSelected()) {
                content.setShowSpheres(true);
                content.setShowText(textItem.isSelected());
                textItem.setEnabled(true);
            } else {
                content.setShowSpheres(false);
                //content.setShowText(false);
                content.setShowText(textItem.isSelected());
                textItem.setEnabled(true);
            }            
        } else {
            JMenuItem sphereItem = getJMenuItem(SHOW_SPHERES_CMD);
            JMenuItem geneTextItem = getJMenuItem(SHOW_GENE_TEXT_FROM_BOTH_CMD);
            JMenuItem exptTextItem = getJMenuItem(SHOW_EXPT_TEXT_FROM_BOTH_CMD);
            if (sphereItem.isSelected()) {
                content.setShowSpheres(true);
                content.setShowGeneTextFromBoth(geneTextItem.isSelected());
                geneTextItem.setEnabled(true);
                content.setShowExptTextFromBoth(exptTextItem.isSelected());
                exptTextItem.setEnabled(true);                
            } else {
                content.setShowSpheres(false);
                //content.setShowText(false);
                content.setShowGeneTextFromBoth(geneTextItem.isSelected());
                geneTextItem.setEnabled(true);
                content.setShowExptTextFromBoth(exptTextItem.isSelected());
                exptTextItem.setEnabled(true);                
            }            
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
    
    private void onShowGeneTextFromBoth() {
        content.setShowGeneTextFromBoth(!content.isShowGeneTextFromBoth());
        content.updateScene();
    }    
    
    private void onShowExptTextFromBoth() {
        content.setShowExptTextFromBoth(!content.isShowExptTextFromBoth());
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
    private class Listener extends MouseAdapter implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (command.equals(RESET_CMD)) {
                onReset();
            } else if (command.equals(OPTIONS_CMD)) {
                onOptions();
            } else if (command.equals(SELECTION_AREA_CMD)) {
                onSelectionArea();
            } else if (command.equals(SAVE_GENE_CLUSTER_CMD)) {
                onSaveGenesFromBoth();
            } else if (command.equals(SAVE_EXPT_CLUSTER_CMD)) {
                onSaveExptsFromBoth();
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
            } else if (command.equals(SHOW_GENE_TEXT_FROM_BOTH_CMD)) {
                onShowGeneTextFromBoth();
            } else if (command.equals(SHOW_EXPT_TEXT_FROM_BOTH_CMD)) {
                onShowExptTextFromBoth();
            } else if (command.equals(WHITE_CMD)) {
                onWhiteBackground();
            } else if (command.equals(STORE_CLUSTER_CMD)){
                storeCluster();
            } else if (command.equals(STORE_GENE_CLUSTER_CMD)) {
                storeGeneClusterFromBoth();
            } else if (command.equals(STORE_EXPT_CLUSTER_CMD)) {
                storeExptClusterFromBoth();
            } else if (command.equals(LAUNCH_NEW_SESSION_CMD)){
                launchNewSession();
            } else if (command.equals(LAUNCH_NEW_GENE_SESSION_CMD)) {
                launchNewGeneSessionFromBoth();
            } else if (command.equals(LAUNCH_NEW_EXPT_SESSION_CMD)) {
                launchNewExptSessionFromBoth();
            }
        }     
        
	public void mouseReleased(MouseEvent event) {
	    maybeShowPopup(event);
	}
	
	public void mousePressed(MouseEvent event) {
	    maybeShowPopup(event);
	}
	
	private void maybeShowPopup(MouseEvent e) {
            //System.out.println("maybeShowPopup");
	    //if (!e.isPopupTrigger() || getCluster() == null || getCluster().length == 0) {
           if (!e.isPopupTrigger()) { 
		return;
	    }
	    popup2.show(e.getComponent(), e.getX(), e.getY());
	}        
        
    }    
    
}
