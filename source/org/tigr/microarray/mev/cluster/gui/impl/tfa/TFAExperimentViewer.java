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

import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Expression;

import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;

public class TFAExperimentViewer extends ExperimentViewer {

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
    }
    
    /**
     * Saves clusters.
     */
    protected void onSaveClusters() {
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

    protected void onSaveCluster() {
	Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
	try {
	    //expViewer.saveCluster(frame);
            ExperimentUtil.saveGeneClusterWithAux(frame, this.getExperiment(), this.getData(), this.getCluster(), auxTitles, auxData);
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
	    e.printStackTrace();
	}
    }

    
}
