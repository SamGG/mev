/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SVMExperimentViewer.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-05-02 16:57:36 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

public class SVMExperimentViewer extends ExperimentViewer {
    
    /**
     * Constructs a <code>SVMExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public SVMExperimentViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
	Listener listener = new Listener();
	this.popup = createJPopupMenu(listener);
	getContentComponent().addMouseListener(listener);
	getHeaderComponent().addMouseListener(listener);
    }
    /**
     * @inheritDoc
     */ 
    public SVMExperimentViewer(Experiment e, int[][] clusters, int[] samplesOrder, boolean drawAnnotations, ExperimentHeader header, Insets insets) {
    	super(e, clusters, samplesOrder, drawAnnotations, header, insets);
        Listener listener = new Listener();
        this.popup = createJPopupMenu(listener);
        getContentComponent().addMouseListener(listener);
        getHeaderComponent().addMouseListener(listener);
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
	    } else if (command.equals(SET_DEF_COLOR_CMD)) {
		onSetDefaultColor();
	    } else if (command.equals(STORE_CLUSTER_CMD)) {
		storeCluster();
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
