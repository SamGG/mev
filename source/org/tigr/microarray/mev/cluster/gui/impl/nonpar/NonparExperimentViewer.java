/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: NonparExperimentViewer.java,v $
 * $Revision: 1.2 $
 * $Date: 2007-12-17 22:10:37 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.nonpar;

import java.awt.Color;
import java.awt.Frame;
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
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
//import org.tigr.microarray.mev.cluster.gui.impl.kmc.KMCExperimentViewer.Listener;

public class NonparExperimentViewer extends ExperimentViewer {

	private String [] auxTitles;
	private String [][] auxData;
		
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
     * @param auxData results in String form //supports cluster saves
     * @param auxTitles String array of titles //supports cluster saves
     */
    public NonparExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder,
    		Boolean drawAnnotations, String [] auxTitles, String [][] auxData){//, ExperimentHeader header, Insets insets) {
    	super(e, clusters, samplesOrder, drawAnnotations.booleanValue());//, header, insets);
    	this.auxData = auxData;
    	this.auxTitles = auxTitles;
    }

    public Expression getExpression() {
    	Expression e = super.getExpression();
    	Object[] args = e.getArguments();
    	Object[] temp = new Object[args.length+2];
    	int i = 0;
    	for(; i<args.length; i++) {
    		temp[i] = args[i];
    	}
    	temp[i] = auxTitles;
    	temp[i+1] = auxData;
    	return new Expression(this, this.getClass(), "new", temp);
    }
    
    /**
     * Constructs a <code>KMCExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public NonparExperimentViewer(Experiment experiment, int[][] clusters, String [] auxTitles, String [][] auxData) {
        super(experiment, clusters);
        Listener listener = new Listener();
        this.popup = createJPopupMenu(listener);
        getContentComponent().addMouseListener(listener);
        getHeaderComponent().addMouseListener(listener);
    	this.auxData = auxData;
    	this.auxTitles = auxTitles;
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
     * Saves all the clusters.
     */
    public void saveClusters(Frame frame) throws Exception {
        frame = frame == null ? JOptionPane.getFrameForComponent(this) : frame;
        ExperimentUtil.saveAllGeneClustersWithAux(frame, getExperiment(), getData(), getClusters(), auxTitles, auxData);
    }
    
    /**
     * Saves current cluster.
     */
    public void saveCluster(Frame frame) throws Exception {
        frame = frame == null ? JOptionPane.getFrameForComponent(this) : frame;
        ExperimentUtil.saveGeneClusterWithAux(frame, getExperiment(), getData(), getCluster(), auxTitles, auxData);
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
