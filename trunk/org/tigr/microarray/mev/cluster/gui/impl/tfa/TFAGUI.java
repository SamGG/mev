/*
 * TFAGUI.java
 *
 * Created on February 12, 2004, 10:44 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.tfa;

import java.io.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.*;

import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.util.FloatMatrix;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Monitor;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLInitDialog;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLViewer;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLGUI;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidsViewer;


/**
 *
 * @author  nbhagaba
 */
public class TFAGUI implements IClusterGUI {

    private Algorithm algorithm;
    private Progress progress;    
    private Experiment experiment;
    private IData data;    
    private int[][] clusters;
    private FloatMatrix means;
    private FloatMatrix variances; 

    Vector exptNamesVector;    
    String[] factorNames;
    int[] numFactorLevels;
    
    /** Creates a new instance of TFAGUI */
    public TFAGUI() {
    }
    
    /**
     * This method should return a tree with calculation results or
     *
     * null, if analysis start was canceled.
     *
     *
     *
     * @param framework the reference to <code>IFramework</code> implementation,
     *
     *        which is used to obtain an initial analysis data and parameters.
     *
     * @throws AlgorithmException if calculation was failed.
     *
     * @throws AbortException if calculation was canceled.
     *
     * @see IFramework
     *
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
	this.experiment = framework.getData().getExperiment();
        this.data = framework.getData();
	
	int number_of_samples = experiment.getNumberOfSamples();
	int number_of_genes = experiment.getNumberOfGenes();   
        
        exptNamesVector = new Vector();
	for (int i = 0; i < number_of_samples; i++) {
	    exptNamesVector.add(framework.getData().getFullSampleName(i));
	}        
        
        TFAInitBox1 t1Box = new TFAInitBox1((JFrame)framework.getFrame(), true);
        t1Box.setVisible(true);
        if (!t1Box.isOkPressed()) return null;
        
        factorNames = new String[2];
        numFactorLevels = new int[2];
        
        factorNames[0] = t1Box.getFactorAName();
        factorNames[1] = t1Box.getFactorBName();
        
        numFactorLevels[0] = t1Box.getNumFactorALevels();
        numFactorLevels[1] = t1Box.getNumFactorBLevels();
        
        TFAInitBox2 t2Box  = new TFAInitBox2((JFrame)framework.getFrame(), true, exptNamesVector, factorNames, numFactorLevels);
        t2Box.setVisible(true);
        
        return null; // for now
    }
    
}
