/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: NonparCentroidViewer.java,v $
 * $Revision: 1.2 $
 * $Date: 2007-12-17 22:10:37 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.nonpar;

import java.awt.Frame;
import java.beans.Expression;

import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;

public class NonparCentroidViewer extends CentroidViewer {
    
    private String [] auxTitles;
    private String [][] auxData;
    
    /**
     * Construct a <code>KMCCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public NonparCentroidViewer(Experiment experiment, int[][] clusters, String [] auxTitles, String [][] auxData) {
		super(experiment, clusters);
	    this.auxTitles = auxTitles;
	    this.auxData = auxData;
    }
    /**
     * Used by XMLDecoder when reconstructing this class from saved file.  Signature
     * specified by CentroidViewer.getPersistenceDelegateArgs().
     * 
     * @param experiment
     * @param clusters
     * @param variances
     */
    public NonparCentroidViewer(Experiment e, int[][] clusters, float[][] variances, float[][] means, float[][] codes, String [] auxTitles, String [][] auxData) {
    	super(e, clusters, variances, means, codes);
        this.auxTitles = auxTitles;
        this.auxData = auxData;
    }
    public Expression getExpression() {
    	return new Expression(this, this.getClass(), "new",
				new Object[]{experiment, clusters, variances, means, codes, auxTitles, auxData});
    }
       
    /**
     * Saves all clusters.
     */
    protected void onSaveClusters() {
	Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
	try {
	    ExperimentUtil.saveAllGeneClustersWithAux(frame, getExperiment(), getData(), getClusters(), auxTitles, auxData);
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
	    ExperimentUtil.saveGeneClusterWithAux(frame, getExperiment(), getData(), getCluster(), auxTitles, auxData);
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
	    e.printStackTrace();
	}
    }
    
}
