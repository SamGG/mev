/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Dec 14, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.awt.image.BufferedImage;
import java.beans.Expression;
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
	int exptID = 0;
	
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
	/**
	 * This method implemented only to satisfy IViewer interface
	 */
	public void setExperiment(Experiment e) {
		;
		
	}
	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExperimentID()
	 */
	public int getExperimentID() {
		return this.exptID;
	}
	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperimentID(int)
	 */
	public void setExperimentID(int id) {
		this.exptID = id;
		
	}
	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExpression()
	 */
	public Expression getExpression() {
		return new Expression(this, this.getClass(), "new", 
				new Object[]{});
	}

}//end class
