/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: PCA3DViewer.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-12-20 15:48:00 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.pca;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.ShowThrowableDialog;
import org.tigr.microarray.mev.TMEV;
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

public class PCA3DViewer extends ViewerAdapter {
    
    private static final String RESET_CMD   = "reset-cmd";
    private static final String OPTIONS_CMD = "options-cmd";
    private static final String SELECTION_AREA_CMD = "select-cmd";
    private static final String SAVE_CMD    = "save-cmd";
    private static final String SAVE_3D_CMD    = "save-3d-cmd";    
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
    
    private FloatMatrix U;
    private int mode;
    private int xAxis, yAxis, zAxis;
    private int labelIndex = -1;    
    private PCASelectionAreaDialog dlg;
    private int exptID = 0;
    private boolean enabled3D = false;
    private JComponent renderContent;
    
    /**
     * Constructs a <code>PCA3DViewer</code> with specified mode,
     * U-matrix and an experiment data.
     */
    public PCA3DViewer(Frame frame, int mode, FloatMatrix U, Experiment experiment, boolean geneViewer) {
        this.frame = frame;
        this.experiment = experiment;
        this.exptID = experiment.getId();
        this.geneViewer = geneViewer;
        this.U = U;
        this.mode = mode;
        try {
	        content = createContent(mode, U, experiment, geneViewer);
	        dlg = new PCASelectionAreaDialog(content, frame, content.getPositionX(), content.getPositionY(), content.getPositionZ(), content.getSizeX(), content.getSizeY(), content.getSizeZ(), content.getMaxValue());
	        popup = createJPopupMenu();
        	enabled3D = true;
        	renderContent = content;
        } catch (UnsatisfiedLinkError ule) {
        	ShowThrowableDialog.show(new Frame(), "No Java 3D detected", new Exception("Java3D is not installed. The 3D viewer cannot be created."));
        	//TODO create blank screen with error message. 
        	enabled3D = false;
        	renderContent = getJ3DErrorPlaceholderContent();
        } catch (java.lang.NoClassDefFoundError ncdfe) {
        	ShowThrowableDialog.show(new Frame(), "No Java 3D detected", new Exception("Java3D is not installed. The 3D viewer cannot be created."));
        	//TODO create blank screen with error message. 
        	enabled3D = false;
        	renderContent = getJ3DErrorPlaceholderContent();
        	//create new content for viewer
        }
    }
    private JTextArea getJ3DErrorPlaceholderContent() {
        JTextArea area = new JTextArea(20, 20);
        area.setEditable(false);
        area.setMargin(new Insets(0, 10, 0, 0));

        area.setText("No 3D viewer is available. To view the results of this analysis, please install Java3D, available at java.sun.com. \n" +
        		"Use the File -> Save Analysis As option to save your results. \n" +
        		"After installing Java3D, restart MeV and load the saved analysis file to view these results in an interactive form. \n");
        area.setCaretPosition(0);
        return area;
    }
    public PCA3DViewer(Frame frame, int mode, FloatMatrix U, Experiment experiment, boolean geneViewer, int xAxis, int yAxis, int zAxis) {
        this.frame = frame;
        this.experiment = experiment;
        this.exptID = experiment.getId();
        this.geneViewer = geneViewer;
        this.U = U;
        this.mode = mode;
        this.xAxis = xAxis;
        this.yAxis= yAxis;
        this.zAxis = zAxis;
        try {
	        content = createContent(mode, U, experiment, geneViewer, xAxis, yAxis, zAxis);
	        dlg = new PCASelectionAreaDialog(content, frame, content.getPositionX(), content.getPositionY(), content.getPositionZ(), content.getSizeX(), content.getSizeY(), content.getSizeZ(), content.getMaxValue());
	        popup = createJPopupMenu();
	    	enabled3D = true;
	    	renderContent = content;
	    } catch (UnsatisfiedLinkError ule) {
	    	ShowThrowableDialog.show(new Frame(), "No Java 3D detected", new Exception("Java3D is not installed. The 3D viewer cannot be created."));
	    	//TODO create blank screen with error message. 
	    	enabled3D = false;
	    	renderContent = getJ3DErrorPlaceholderContent();
	    } catch (java.lang.NoClassDefFoundError ncdfe) {
	    	ShowThrowableDialog.show(new Frame(), "No Java 3D detected", new Exception("Java3D is not installed. The 3D viewer cannot be created."));
	    	//TODO create blank screen with error message. 
	    	enabled3D = false;
	    	renderContent = getJ3DErrorPlaceholderContent();
	    	//create new content for viewer
	    }
    }   
    /**
     * State-saving constructor.
     * @param e
     * @param geneViewer
     * @param U
     * @param mode
     * @param xAxis
     * @param yAxis
     * @param zAxis
     */
    public PCA3DViewer(Experiment e, Boolean geneViewer, FloatMatrix U, Integer mode, Integer xAxis, Integer yAxis, Integer zAxis){
        this.geneViewer = geneViewer.booleanValue();
        this.U = U;
        this.mode = mode.intValue();
        this.xAxis = xAxis.intValue();
        this.yAxis = yAxis.intValue();
        this.zAxis = zAxis.intValue();
        this.experiment = e;
    	this.exptID = experiment.getId();
        try {
	    	content = createContent(mode, U, experiment, geneViewer, xAxis, yAxis, zAxis);  
	        dlg = new PCASelectionAreaDialog(content, frame, content.getPositionX(), content.getPositionY(), content.getPositionZ(), content.getSizeX(), content.getSizeY(), content.getSizeZ(), content.getMaxValue());
	        popup = createJPopupMenu();
	        enabled3D = true;
	    	renderContent = content;
        } catch (UnsatisfiedLinkError ule) {
	    	enabled3D = false;
	    	renderContent = getJ3DErrorPlaceholderContent();
	    } catch (java.lang.NoClassDefFoundError ncdfe) {
	    	enabled3D = false;
	    	renderContent = getJ3DErrorPlaceholderContent();
	    }
    }
    /**
     * @inheritDoc
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{this.experiment, new Boolean(geneViewer), U, new Integer(mode), new Integer(xAxis), new Integer(yAxis), new Integer(zAxis)});
    }

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
        if(enabled3D) {
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
        return renderContent;
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
    
    private Content3D createContent(int mode, FloatMatrix U, Experiment experiment, boolean geneViewer, int x, int y, int z) {
        return new Content3D(mode, U, experiment, geneViewer, x, y, z);
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

        menuItem = new JMenuItem("Save 3D coordinates...", GUIFactory.getIcon("save16.gif"));
        menuItem.setEnabled(true);
        menuItem.setActionCommand(SAVE_3D_CMD);
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
        //PCASelectionAreaDialog dlg = new PCASelectionAreaDialog(frame,
        //content.getPositionX(), content.getPositionY(), content.getPositionZ(),
        //content.getSizeX(), content.getSizeY(), content.getSizeZ());
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
            framework.storeSubCluster( content.getSelectedGenes(), experiment, Cluster.GENE_CLUSTER);
        else
            framework.storeSubCluster( content.getSelectedGenes(), experiment, Cluster.EXPERIMENT_CLUSTER);
        content.setSelection(false);
        //onHideSelection(); 
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
            //content.setShowText(false);
            content.setShowText(textItem.isSelected());
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
     * outputs the 3D coordinates
     * @param outputGenes true if output gene coords
     */
    private void output3DCoords() {
    	
    	JFileChooser fileChooser;
    	
    	fileChooser = new JFileChooser(TMEV.getDataPath());
    	
    	String [] annFields;
    	Vector sampleAnnFields;
    	FloatMatrix coordMatrix = U;
    	
    	boolean outputGenes = this.geneViewer;
    	
    	
    	if(outputGenes) {
    		annFields = data.getFieldNames();
    	} else {
    		sampleAnnFields = data.getSampleAnnotationFieldNames();
    		
    		annFields = new String[sampleAnnFields.size()];
    		for(int i = 0; i < annFields.length; i++) {
    			annFields[i] = (String)(sampleAnnFields.get(i));
    		}
    	}
    	
    	try {
    		if(fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
    			File file = fileChooser.getSelectedFile();
    			
    			PrintWriter pw = new PrintWriter(new FileWriter(file));
    			
    			for(int i = 0; i < annFields.length; i++) {
    				pw.print(annFields[i]+"\t");
    			}    		
    			pw.println("X\tY\tZ");
    			
    			int nRows = coordMatrix.getRowDimension();
    			
    			if(outputGenes) {
    				for(int i = 0; i < nRows; i++) {
    					for(int j = 0; j < annFields.length; j++) {
    						pw.print(data.getElementAttribute(experiment.getGeneIndexMappedToData(i),j)+"\t");
    					}    				
    					pw.print(coordMatrix.get(i,0)+"\t");
    					pw.print(coordMatrix.get(i,1)+"\t");
    					pw.println(coordMatrix.get(i,2));    				
    				}
    			} else {    			
    				for(int i = 0; i < nRows; i++) {
    					for(int j = 0; j < annFields.length; j++) {
    						pw.print(data.getSampleAnnotation(i, annFields[j])+"\t");
    					}    				
    					pw.print(coordMatrix.get(i,0)+"\t");
    					pw.print(coordMatrix.get(i,1)+"\t");
    					pw.println(coordMatrix.get(i,2));
    				}
    			}
    			pw.flush();
    			pw.close();
    		}
    	} catch (IOException ioe) {
    		JOptionPane.showMessageDialog(this.frame, "Error opening or saving to file", "Coordinate ouput Error", JOptionPane.ERROR_MESSAGE);
    	}
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
            } else if(command.equals(SAVE_3D_CMD)) {
            	output3DCoords();
            }
            
        }        
    }
}
