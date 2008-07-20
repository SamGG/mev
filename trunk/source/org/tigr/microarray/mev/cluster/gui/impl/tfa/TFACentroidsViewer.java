/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * TFACentroidsViewer.java
 *
 * Created on February 27, 2004, 11:56 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.tfa;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Expression;

import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;

/**
 *
 * @author  nbhagaba
 */
public class TFACentroidsViewer extends CentroidsViewer {
    
    String[] auxTitles;
    Object[][] auxData;    
    
    /** Creates a new instance of TFACentroidsViewer */
    public TFACentroidsViewer(Experiment experiment, int[][] clusters, String[] auxTitles, Object[][] auxData) {
        super(experiment, clusters);
		initialize(auxTitles, auxData);
    }
	/**
	 * @inheritDoc
	 */
	public TFACentroidsViewer(CentroidViewer cv, String[] auxTitles, Object[][] auxData) {
		super(cv);
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
        CentroidsViewer.PopupListener listener = new CentroidsViewer.PopupListener();
        this.popup = createJPopupMenu(listener);
        this.auxTitles = auxTitles;
        this.auxData = auxData;        
        getContentComponent().addMouseListener(listener);         
    }
    
    /**
     * Saves all clusters.
     */
    protected void onSaveClusters() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        try {
            //ExperimentUtil.saveExperiment(frame, getExperiment(), getData(), getClusters());
            ExperimentUtil.saveAllGeneClustersWithAux(frame, this.getExperiment(), this.getData(), this.getClusters(), auxTitles, auxData);            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }    
    
}
