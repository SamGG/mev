/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * TFACentroidViewer.java
 *
 * Created on February 27, 2004, 11:43 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.tfa;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Expression;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;

/**
 *
 * @author  nbhagaba
 */
public class TFACentroidViewer extends CentroidViewer {
    
    private JPopupMenu popup;  
    String[] auxTitles;
    Object[][] auxData;    
    
    /** Creates a new instance of TFACentroidViewer */
    public TFACentroidViewer(Experiment experiment, int[][] clusters, String[] auxTitles, Object[][] auxData) {
	super(experiment, clusters);
    	initialize(auxTitles, auxData);
    }
    /**
     * @inheritDoc
     */
    public TFACentroidViewer(Experiment e, int[][] clusters, float[][] variances, float[][] means, float[][] codes, String[] auxTitles, Object[][] auxData) {
    	super(e, clusters, variances, means, codes);
    	initialize(auxTitles, auxData);
    }
	public Expression getExpression(){
		Object[] parentConstructorArgs = super.getExpression().getArguments();
		return new Expression(this, this.getClass(), "new", 
				new Object[]{parentConstructorArgs[0], parentConstructorArgs[1], parentConstructorArgs[2], parentConstructorArgs[3], parentConstructorArgs[4], 
				auxTitles, auxData});
	}
    public void initialize(String[] auxTitles, Object[][] auxData){	        
        this.auxTitles = auxTitles;
        this.auxData = auxData;        
	Listener listener = new Listener();
	this.popup = createJPopupMenu(listener);
	getContentComponent().addMouseListener(listener);        
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
     * Saves all clusters.
     */
    private void onSaveClusters() {
	Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
	try {
	    //ExperimentUtil.saveExperiment(frame, getExperiment(), getData(), getClusters());
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
	    //ExperimentUtil.saveExperiment(frame, getExperiment(), getData(), getCluster());
            ExperimentUtil.saveGeneClusterWithAux(frame, this.getExperiment(), this.getData(), this.getCluster(), auxTitles, auxData);            
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
	    e.printStackTrace();
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
	    } else if(command.equals(SET_Y_TO_EXPERIMENT_MAX_CMD)){
                yRangeOption = CentroidViewer.USE_EXPERIMENT_MAX;
                setClusterMaxMenuItem.setEnabled(true);
                setOverallMaxMenuItem.setEnabled(false);
                repaint();
            } else if(command.equals(SET_Y_TO_CLUSTER_MAX_CMD)){
                yRangeOption = CentroidViewer.USE_CLUSTER_MAX;
                setClusterMaxMenuItem.setEnabled(false);
                setOverallMaxMenuItem.setEnabled(true);
                repaint();
            } else if(command.equals(LAUNCH_NEW_SESSION_CMD)){
                launchNewSession();
            } else if(command.equals(TOGGLE_REF_LINE_CMD)){
                showRefLine = !showRefLine;
                repaint();
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
