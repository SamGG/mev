/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * TFAExperimentViewer.java
 *
 * Created on February 27, 2004, 11:10 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.tfa;

/**
 *
 * @author  nbhagaba
 */

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
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

public class TFAExperimentViewer extends ExperimentViewer {

    private JPopupMenu popup;
    String[] auxTitles;
    Object[][] auxData;
    
    /** Creates a new instance of TFAExperimentViewer */
    public TFAExperimentViewer(Experiment experiment, int[][] clusters, String[] auxTitles, Object[][] auxData) {
	super(experiment, clusters);
    	initialize(auxTitles, auxData);
    }
    
    /**
     * @inheritDoc
     */ 
    public TFAExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder, boolean drawAnnotations, 
    		String[] auxTitles, Object[][] auxData) {
    	super(e, clusters, samplesOrder, drawAnnotations);
    	initialize(auxTitles, auxData);
    } 
    
	public Expression getExpression(){
		Object[] parentConstructorArgs = super.getExpression().getArguments();
		Object[] temp = new Object[parentConstructorArgs.length + 2];
		int i=0;
		for(i=0; i<parentConstructorArgs.length; i++){
			temp[i] = parentConstructorArgs[i];
		}
		temp[i] = auxTitles;
		temp[i+1] = auxData;
		return new Expression(this, this.getClass(), "new", temp);
	}
	
    private void initialize(String[] auxTitles, Object[][] auxData){
        this.auxTitles = auxTitles;
        this.auxData = auxData;
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
	    //saveClusters(frame);
            ExperimentUtil.saveAllGeneClustersWithAux(frame, this.getExperiment(), this.getData(), this.getClusters(), auxTitles, auxData);            
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
            ExperimentUtil.saveGeneClusterWithAux(frame, this.getExperiment(), this.getData(), this.getCluster(), auxTitles, auxData);
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
