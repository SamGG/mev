/*
 * Created on Jun 18, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;

/**
 * @author vu
 */
public class USCExperimentClusterViewer extends ExperimentClusterViewer {
	private JPopupMenu popup;
	

	/**
	 * @param experiment
	 * @param clusters
	 * @param centroidName
	 * @param centroids
	 */
	public USCExperimentClusterViewer(Experiment experiment, int[][] clusters, String centroidName, float[][] centroids) {
		super(experiment, clusters, centroidName, centroids);
		Listener listener = new Listener();
		this.popup = createJPopupMenu(listener);
		getContentComponent().addMouseListener(listener);
		getHeaderComponent().addMouseListener(listener);
	}//end constructor
	
	
	public USCExperimentClusterViewer( Experiment experiment, int[][] clusters ) {
		super( experiment, clusters );
		Listener listener = new Listener();
		this.popup = createJPopupMenu( listener );
		this.getContentComponent().addMouseListener( listener );
		this.getHeaderComponent().addMouseListener( listener );
	}
    /**
     * @inheritDoc
     */
    public USCExperimentClusterViewer(int[][] clusters, int[] genesOrder, Boolean drawAnnotations, 
    		Integer offset, ExperimentClusterHeader header, Boolean hasCentroid, float[][] centroids, 
			Dimension elementSize, Integer labelIndex, Integer exptID) {
    		super(clusters, genesOrder, drawAnnotations, offset, header, hasCentroid, centroids, elementSize, labelIndex, exptID);
    		Listener listener = new Listener();
    		this.popup = createJPopupMenu( listener );
    		this.getContentComponent().addMouseListener( listener );
    		this.getHeaderComponent().addMouseListener( listener );
    }
	
	/**
	 * @param listener
	 * @return
	 */
	private JPopupMenu createJPopupMenu(Listener listener) {
		JPopupMenu popup = new JPopupMenu();
		addMenuItems(popup, listener);
		return popup;
	}

	/**
	 * The class to listen to mouse and action events.
	 */
	private class Listener extends MouseAdapter implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals(SAVE_CLUSTER_CMD)) {
				//onSaveCluster();
			} else if (command.equals(SAVE_ALL_CLUSTERS_CMD)) {
				//onSaveClusters();
			} else if (command.equals(SET_DEF_COLOR_CMD)) {
				//onSetDefaultColor();
			} else if (command.equals(STORE_CLUSTER_CMD)) {
				//storeCluster();
			} else if(command.equals(LAUNCH_NEW_SESSION_CMD)){
				//launchNewSession();
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
	}//end Listener class
	
	
	
}//end class
