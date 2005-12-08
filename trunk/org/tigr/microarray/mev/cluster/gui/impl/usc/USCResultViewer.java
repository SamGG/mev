/*
 * Created on Dec 14, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;

/**
 * @author vu
 */
public class USCResultViewer extends JPanel implements IViewer, Serializable {

	
	public JComponent getContentComponent() {
		return this;
	}
	public JComponent getHeaderComponent() {
		return null;
	}
	public BufferedImage getImage() {
		return null;
	}
	public JComponent getRowHeaderComponent() {
		return null;
	}
	public JComponent getCornerComponent(int cornerIndex) {
		return null;
	}
	public void onDataChanged(IData data) {
		//do nothing
	}

	
	public void onSelected(IFramework framework) {
		//
	}
	
	
	public void onMenuChanged(IDisplayMenu menu) {
		//
	}

	
	public void onDeselected() {
		//
	}

	
	public void onClosed() {
		//
	}

	
	public int[][] getClusters() {
		return null;
	}

	
	public Experiment getExperiment() {
		return null;
	}
    /* (non-Javadoc)
     * @see org.tigr.microarray.mev.cluster.gui.IViewer#getViewerType()
     */
    public int getViewerType() {
        return 0;
    }

}//end class
