/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * TFAGUI.java
 *
 * Created on February 12, 2004, 10:44 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.tfa;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLGUI;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLViewer;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;
import org.tigr.util.FloatMatrix;

/**
 *
 * @author  nbhagaba
 */
public class TFAGUI implements IClusterGUI, IScriptGUI {
    
    public static final int JUST_ALPHA = 4;
    public static final int STD_BONFERRONI = 5;
    public static final int ADJ_BONFERRONI = 6;
    public static final int MAX_T = 9;
    public static final int MIN_P = 10;
    
    protected Algorithm algorithm;
    protected Progress progress;
    protected Experiment experiment;
    protected IData data;
    protected int[][] clusters;
    protected FloatMatrix means;
    protected FloatMatrix variances;
    
    protected Vector exptNamesVector;
    protected String[] factorNames;
    protected int[] numFactorLevels;
    protected int[] factorAAssignments, factorBAssignments;
    
    protected Object[][] auxData;
    protected String[] auxTitles;
    protected String[] clusterLabels;
    protected boolean usePerms, drawSigTreesOnly;
    
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
            exptNamesVector.add(framework.getData().getFullSampleName(experiment.getSampleIndex(i)));
        }
        
        TFAInitBox1 t1Box = new TFAInitBox1((JFrame)framework.getFrame(), true);
        t1Box.setVisible(true);
        if (!t1Box.isOkPressed()) return null;
        
        factorNames = new String[2];
        numFactorLevels = new int[2];
        
        factorNames[0] = t1Box.getFactorAName();
        factorNames[1] = t1Box.getFactorBName();
        
        String[] localClustNames =  {factorNames[0] + " significant", factorNames[1] + " significant", "Interaction signficant", factorNames[0] + " non-significant", factorNames[1] + " non-significant", "Interaction non-signficant", "Non-significant for all effects"};
        clusterLabels = new String[localClustNames.length];
        
        for (int i = 0; i < clusterLabels.length; i++) {
            clusterLabels[i] = localClustNames[i];
        }
        
        numFactorLevels[0] = t1Box.getNumFactorALevels();
        numFactorLevels[1] = t1Box.getNumFactorBLevels();
        
        TFAInitBox2 t2Box  = new TFAInitBox2((JFrame)framework.getFrame(), true, exptNamesVector, factorNames, numFactorLevels, framework.getClusterRepository(1));
        t2Box.setVisible(true);
        
        if (!t2Box.isOkPressed()) return null;
        
        boolean allCellsHaveOneSample = t2Box.allCellsHaveOneSample();
        boolean isHierarchicalTree = t2Box.drawTrees();
        drawSigTreesOnly = true;
        if (isHierarchicalTree) {
            drawSigTreesOnly = t2Box.drawSigTreesOnly();
        }        
        int adjustmentMethod = t2Box.getAdjustmentMethod();
        float alpha = t2Box.getAlpha();
        if (t2Box.isButtonSelectionMethod()){
	        factorAAssignments = t2Box.getFactorAAssignments();
	        factorBAssignments = t2Box.getFactorBAssignments();
        } else {
	        factorAAssignments = t2Box.getFactorAClusterAssignments();
	        factorBAssignments = t2Box.getFactorBClusterAssignments();
        }
        Vector[][] bothFactorAssignments = t2Box.getBothFactorAssignments();
        boolean isBalancedDesign = false;
        if (!allCellsHaveOneSample) {
            isBalancedDesign = t2Box.isBalancedDesign();
        }
        usePerms = t2Box.usePerms();
        int numPerms = 0;
        if (usePerms) {
            numPerms = t2Box.getNumPerms();
        }

        IDistanceMenu menu = framework.getDistanceMenu();        
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.PEARSON;
        }
    
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        int hcl_function = 4;
        boolean hcl_absolute = false;
        if (isHierarchicalTree) {
            HCLInitDialog hcl_dialog = new HCLInitDialog(framework.getFrame(), menu.getFunctionName(function), menu.isAbsoluteDistance(), true);
            if (hcl_dialog.showModal() != JOptionPane.OK_OPTION) {
                return null;
            }
            hcl_method = hcl_dialog.getMethod();
            hcl_samples = hcl_dialog.isClusterExperiments();
            hcl_genes = hcl_dialog.isClusterGenes();
            hcl_function = hcl_dialog.getDistanceMetric();
            hcl_absolute = hcl_dialog.getAbsoluteSelection();
        }
        
        Listener listener = new Listener();
        
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("TFA");
            algorithm.addAlgorithmListener(listener);
            
            int genes = experiment.getNumberOfGenes();
            
            this.progress = new Progress(framework.getFrame(), "Finding significant genes", listener);
            this.progress.show();
            
            AlgorithmData data = new AlgorithmData();
            
            data.addMatrix("experiment", experiment.getMatrix());
            data.addParam("distance-factor", String.valueOf(1.0f));
            data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
                        
            data.addParam("distance-function", String.valueOf(function));
            data.addIntArray("numFactorLevels", numFactorLevels);
            data.addParam("allCellsHaveOneSample", String.valueOf(allCellsHaveOneSample));
            data.addParam("adjustmentMethod", String.valueOf(adjustmentMethod));
            data.addParam("alpha", String.valueOf(alpha));
            data.addIntArray("factorAAssignments", factorAAssignments);
            data.addIntArray("factorBAssignments", factorBAssignments);
            data.addObjectMatrix("bothFactorAssignments", bothFactorAssignments);
            data.addParam("isBalancedDesign", String.valueOf(isBalancedDesign));
            data.addParam("usePerms", String.valueOf(usePerms));
            data.addParam("numPerms", String.valueOf(numPerms));
            // hcl parameters
            if (isHierarchicalTree) {
                data.addParam("hierarchical-tree", String.valueOf(true));
                data.addParam("draw-sig-trees-only", String.valueOf(drawSigTreesOnly));                
                data.addParam("method-linkage", String.valueOf(hcl_method));
                data.addParam("calculate-genes", String.valueOf(hcl_genes));
                data.addParam("calculate-experiments", String.valueOf(hcl_samples));
                data.addParam("hcl-distance-function", String.valueOf(hcl_function));
                data.addParam("hcl-distance-absolute", String.valueOf(hcl_absolute));
            }
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(data);
            long time = System.currentTimeMillis() - start;
            
            // getting the results
            Cluster result_cluster = result.getCluster("cluster");
            NodeList nodeList = result_cluster.getNodeList();
            //AlgorithmParameters resultMap = result.getParams();
            int k = 7; //resultMap.getInt("number-of-clusters"); // NEED THIS TO GET THE VALUE OF NUMBER-OF-CLUSTERS
            
            this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            FloatMatrix factorAFValuesMatrix = result.getMatrix("factorAFValuesMatrix");
            FloatMatrix factorBFValuesMatrix = result.getMatrix("factorBFValuesMatrix");
            FloatMatrix interactionFValuesMatrix = result.getMatrix("interactionFValuesMatrix");
            
            FloatMatrix factorADfValuesMatrix = result.getMatrix("factorADfValuesMatrix");
            FloatMatrix factorBDfValuesMatrix = result.getMatrix("factorBDfValuesMatrix");
            FloatMatrix interactionDfValuesMatrix = result.getMatrix("interactionDfValuesMatrix");
            FloatMatrix errorDfValuesMatrix = result.getMatrix("errorDfValuesMatrix");
            
            FloatMatrix origFactorAPValuesMatrix = result.getMatrix("origFactorAPValuesMatrix");
            FloatMatrix origFactorBPValuesMatrix = result.getMatrix("origFactorBPValuesMatrix");
            FloatMatrix origInteractionPValuesMatrix = result.getMatrix("origInteractionPValuesMatrix");
            
            FloatMatrix adjFactorAPValuesMatrix = result.getMatrix("adjFactorAPValuesMatrix");
            FloatMatrix adjFactorBPValuesMatrix = result.getMatrix("adjFactorBPValuesMatrix");
            FloatMatrix adjInteractionPValuesMatrix = result.getMatrix("adjInteractionPValuesMatrix");
            
            auxTitles = new String[13];
            //auxTitles = {"Adj. p-values (" + factorNames[0] + ")", "Adj. p-values (" + factorNames[1] + ")",  "Adj. p-values (interaction)", factorName[0] + " Orig. p-values", factorNames[0] + " F-ratio", factorNames[1] + "F-Ratio", "Interaction F-Ratio" };
            auxTitles[0] = "Adj. p-values (" + factorNames[0] + ")";
            auxTitles[1] = "Adj. p-values (" + factorNames[1] + ")";
            auxTitles[2] = "Adj. p-values (interaction)";
            auxTitles[3] = "Orig. p-values (" + factorNames[0] + ")";
            auxTitles[4] = "Orig. p-values (" + factorNames[1] + ")";
            auxTitles[5] = "Orig. p-values (interaction)";
            auxTitles[6] = "F-ratio (" + factorNames[0] + ")";
            auxTitles[7] = "F-ratio (" + factorNames[1] + ")";
            auxTitles[8] = "F-ratio (interaction)";
            auxTitles[9] = "df (" + factorNames[0] + ")";
            auxTitles[10] = "df (" + factorNames[1] + ")";
            auxTitles[11] = "df (interaction)";
            auxTitles[12] = "df (error)";
            
            auxData = new Object[factorAFValuesMatrix.A.length][13];
            
            for (int i = 0; i < auxData.length; i++) {
                auxData[i][0] = new Float(adjFactorAPValuesMatrix.A[i][0]);
                auxData[i][1] = new Float(adjFactorBPValuesMatrix.A[i][0]);
                auxData[i][2] = new Float(adjInteractionPValuesMatrix.A[i][0]);
                auxData[i][3] = new Float(origFactorAPValuesMatrix.A[i][0]);
                auxData[i][4] = new Float(origFactorBPValuesMatrix.A[i][0]);
                auxData[i][5] = new Float(origInteractionPValuesMatrix.A[i][0]);
                auxData[i][6] = new Float(factorAFValuesMatrix.A[i][0]);
                auxData[i][7] = new Float(factorBFValuesMatrix.A[i][0]);
                auxData[i][8] = new Float(interactionFValuesMatrix.A[i][0]);
                auxData[i][9] = new Integer((int)(factorADfValuesMatrix.A[i][0]));
                auxData[i][10] = new Integer((int)(factorBDfValuesMatrix.A[i][0]));
                auxData[i][11] = new Integer((int)(interactionDfValuesMatrix.A[i][0]));
                auxData[i][12] = new Integer((int)(errorDfValuesMatrix.A[i][0]));
            }
            
            
            
            GeneralInfo info = new GeneralInfo();
            info.time = time;
            //ADD MORE INFO PARAMETERS HERE
            info.alpha = alpha;
            info.adjMethod = getAdjMethod(adjustmentMethod);
            info.pValueBasedOn = getPValueBasedOn(usePerms);
            if (usePerms) {
                //info.useAllCombs = useAllCombs;
                info.numPerms = numPerms;
            }
            info.function = menu.getFunctionName(function);
            info.hcl = isHierarchicalTree;
            info.hcl_genes = hcl_genes;
            info.hcl_samples = hcl_samples;
            info.hcl_method = hcl_method;
            return createResultTree(result_cluster, info);
            
        } finally {
            if (algorithm != null) {
                algorithm.removeAlgorithmListener(listener);
            }
            if (progress != null) {
                progress.dispose();
            }
            /*
            if (monitor != null) {
                monitor.dispose();
            }
             */
        }
        
        //return null; // for now
    }
    
    
    
    public AlgorithmData getScriptParameters(IFramework framework) {
        this.experiment = framework.getData().getExperiment();
        this.data = framework.getData();
        
        int number_of_samples = experiment.getNumberOfSamples();
        int number_of_genes = experiment.getNumberOfGenes();
        
        exptNamesVector = new Vector();
        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(framework.getData().getFullSampleName(experiment.getSampleIndex(i)));            
        }
        
        TFAInitBox1 t1Box = new TFAInitBox1((JFrame)framework.getFrame(), true);
        t1Box.setVisible(true);
        if (!t1Box.isOkPressed()) return null;
        
        factorNames = new String[2];
        numFactorLevels = new int[2];
        
        factorNames[0] = t1Box.getFactorAName();
        factorNames[1] = t1Box.getFactorBName();
        
        String[] localClustNames =  {factorNames[0] + " significant", factorNames[1] + " significant", "Interaction signficant", factorNames[0] + " non-significant", factorNames[1] + " non-significant", "Interaction non-signficant", "Non-significant for all effects"};
        clusterLabels = new String[localClustNames.length];
        
        for (int i = 0; i < clusterLabels.length; i++) {
            clusterLabels[i] = localClustNames[i];
        }
        
        numFactorLevels[0] = t1Box.getNumFactorALevels();
        numFactorLevels[1] = t1Box.getNumFactorBLevels();
        
        TFAInitBox2 t2Box  = new TFAInitBox2((JFrame)framework.getFrame(), true, exptNamesVector, factorNames, numFactorLevels, framework.getClusterRepository(1));
        t2Box.setVisible(true);
        
        if (!t2Box.isOkPressed()) return null;
        
        boolean allCellsHaveOneSample = t2Box.allCellsHaveOneSample();
        boolean isHierarchicalTree = t2Box.drawTrees();
        drawSigTreesOnly = true;
        if (isHierarchicalTree) {
            drawSigTreesOnly = t2Box.drawSigTreesOnly();
        }        
        
        int adjustmentMethod = t2Box.getAdjustmentMethod();
        float alpha = t2Box.getAlpha();
        if (t2Box.isButtonSelectionMethod()){
	        factorAAssignments = t2Box.getFactorAAssignments();
	        factorBAssignments = t2Box.getFactorBAssignments();
        } else {
	        factorAAssignments = t2Box.getFactorAClusterAssignments();
	        factorBAssignments = t2Box.getFactorBClusterAssignments();
        }
        Vector[][] bothFactorAssignments = t2Box.getBothFactorAssignments();
        boolean isBalancedDesign = false;
        if (!allCellsHaveOneSample) {
            isBalancedDesign = t2Box.isBalancedDesign();
        }
        usePerms = t2Box.usePerms();
        int numPerms = 0;
        if (usePerms) {
            numPerms = t2Box.getNumPerms();
        }
        
        IDistanceMenu menu = framework.getDistanceMenu();        
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.PEARSON;
        }
        
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        int hcl_function = 4;
        boolean hcl_absolute = false;
        if (isHierarchicalTree) {
            HCLInitDialog hcl_dialog = new HCLInitDialog(framework.getFrame(), menu.getFunctionName(function), menu.isAbsoluteDistance(), true);
            if (hcl_dialog.showModal() != JOptionPane.OK_OPTION) {
                return null;
            }
            hcl_method = hcl_dialog.getMethod();
            hcl_samples = hcl_dialog.isClusterExperiments();
            hcl_genes = hcl_dialog.isClusterGenes();
            hcl_function = hcl_dialog.getDistanceMetric();
            hcl_absolute = hcl_dialog.getAbsoluteSelection();
        }
        
        int genes = experiment.getNumberOfGenes();
        
        AlgorithmData data = new AlgorithmData();
        data.addStringArray("cluster-labels", this.clusterLabels);
        data.addStringArray("factor-names", this.factorNames);
        data.addParam("distance-factor", String.valueOf(1.0f));

        data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
        
        data.addParam("distance-function", String.valueOf(function));
        data.addIntArray("numFactorLevels", numFactorLevels);
        data.addParam("allCellsHaveOneSample", String.valueOf(allCellsHaveOneSample));
        data.addParam("adjustmentMethod", String.valueOf(adjustmentMethod));
        data.addParam("alpha", String.valueOf(alpha));
        data.addIntArray("factorAAssignments", factorAAssignments);
        data.addIntArray("factorBAssignments", factorBAssignments);
        data.addObjectMatrix("bothFactorAssignments", bothFactorAssignments);
        data.addParam("isBalancedDesign", String.valueOf(isBalancedDesign));
        data.addParam("usePerms", String.valueOf(usePerms));
        data.addParam("numPerms", String.valueOf(numPerms));
        // hcl parameters
        if (isHierarchicalTree) {
            data.addParam("hierarchical-tree", String.valueOf(true));
            data.addParam("draw-sig-trees-only", String.valueOf(drawSigTreesOnly));            
            data.addParam("method-linkage", String.valueOf(hcl_method));
            data.addParam("calculate-genes", String.valueOf(hcl_genes));
            data.addParam("calculate-experiments", String.valueOf(hcl_samples));
            data.addParam("hcl-distance-function", String.valueOf(hcl_function));
            data.addParam("hcl-distance-absolute", String.valueOf(hcl_absolute));            
        }
        
        // alg name
        data.addParam("name", "2 Fact. ANOVA");
        
        // alg type
        data.addParam("alg-type", "cluster-genes");
        
        // output class
        data.addParam("output-class", "partition-output");
        
        //output nodes
        data.addStringArray("output-nodes", clusterLabels);
        return data;
    }
    
    
    public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, Experiment experiment) throws AlgorithmException {
        this.experiment = experiment;
        this.data = framework.getData();
        exptNamesVector = new Vector();
        for (int i = 0; i < data.getFeaturesCount(); i++) {
            exptNamesVector.add(framework.getData().getFullSampleName(i));
        }        
        this.clusterLabels = algData.getStringArray("cluster-labels");
        this.factorNames = algData.getStringArray("factor-names");
        this.factorAAssignments = algData.getIntArray("factorAAssignments");
        this.factorBAssignments = algData.getIntArray("factorBAssignments");
        this.drawSigTreesOnly = algData.getParams().getBoolean("draw-sig-trees-only");        
        algData.addMatrix("experiment", experiment.getMatrix());
        Listener listener = new Listener();
        
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("TFA");
            algorithm.addAlgorithmListener(listener);
            
            int genes = experiment.getNumberOfGenes();
            
            this.progress = new Progress(framework.getFrame(), "Finding significant genes", listener);
            this.progress.show();
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(algData);
            long time = System.currentTimeMillis() - start;
            
            // getting the results
            Cluster result_cluster = result.getCluster("cluster");
            NodeList nodeList = result_cluster.getNodeList();
            //AlgorithmParameters resultMap = result.getParams();
            int k = 7; //resultMap.getInt("number-of-clusters"); // NEED THIS TO GET THE VALUE OF NUMBER-OF-CLUSTERS
            
            this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            FloatMatrix factorAFValuesMatrix = result.getMatrix("factorAFValuesMatrix");
            FloatMatrix factorBFValuesMatrix = result.getMatrix("factorBFValuesMatrix");
            FloatMatrix interactionFValuesMatrix = result.getMatrix("interactionFValuesMatrix");
            
            FloatMatrix factorADfValuesMatrix = result.getMatrix("factorADfValuesMatrix");
            FloatMatrix factorBDfValuesMatrix = result.getMatrix("factorBDfValuesMatrix");
            FloatMatrix interactionDfValuesMatrix = result.getMatrix("interactionDfValuesMatrix");
            FloatMatrix errorDfValuesMatrix = result.getMatrix("errorDfValuesMatrix");
            
            FloatMatrix origFactorAPValuesMatrix = result.getMatrix("origFactorAPValuesMatrix");
            FloatMatrix origFactorBPValuesMatrix = result.getMatrix("origFactorBPValuesMatrix");
            FloatMatrix origInteractionPValuesMatrix = result.getMatrix("origInteractionPValuesMatrix");
            
            FloatMatrix adjFactorAPValuesMatrix = result.getMatrix("adjFactorAPValuesMatrix");
            FloatMatrix adjFactorBPValuesMatrix = result.getMatrix("adjFactorBPValuesMatrix");
            FloatMatrix adjInteractionPValuesMatrix = result.getMatrix("adjInteractionPValuesMatrix");
            
            auxTitles = new String[13];
            //auxTitles = {"Adj. p-values (" + factorNames[0] + ")", "Adj. p-values (" + factorNames[1] + ")",  "Adj. p-values (interaction)", factorName[0] + " Orig. p-values", factorNames[0] + " F-ratio", factorNames[1] + "F-Ratio", "Interaction F-Ratio" };
            auxTitles[0] = "Adj. p-values (" + factorNames[0] + ")";
            auxTitles[1] = "Adj. p-values (" + factorNames[1] + ")";
            auxTitles[2] = "Adj. p-values (interaction)";
            auxTitles[3] = "Orig. p-values (" + factorNames[0] + ")";
            auxTitles[4] = "Orig. p-values (" + factorNames[1] + ")";
            auxTitles[5] = "Orig. p-values (interaction)";
            auxTitles[6] = "F-ratio (" + factorNames[0] + ")";
            auxTitles[7] = "F-ratio (" + factorNames[1] + ")";
            auxTitles[8] = "F-ratio (interaction)";
            auxTitles[9] = "df (" + factorNames[0] + ")";
            auxTitles[10] = "df (" + factorNames[1] + ")";
            auxTitles[11] = "df (interaction)";
            auxTitles[12] = "df (error)";
            
            auxData = new Object[factorAFValuesMatrix.A.length][13];
            
            for (int i = 0; i < auxData.length; i++) {
                auxData[i][0] = new Float(adjFactorAPValuesMatrix.A[i][0]);
                auxData[i][1] = new Float(adjFactorBPValuesMatrix.A[i][0]);
                auxData[i][2] = new Float(adjInteractionPValuesMatrix.A[i][0]);
                auxData[i][3] = new Float(origFactorAPValuesMatrix.A[i][0]);
                auxData[i][4] = new Float(origFactorBPValuesMatrix.A[i][0]);
                auxData[i][5] = new Float(origInteractionPValuesMatrix.A[i][0]);
                auxData[i][6] = new Float(factorAFValuesMatrix.A[i][0]);
                auxData[i][7] = new Float(factorBFValuesMatrix.A[i][0]);
                auxData[i][8] = new Float(interactionFValuesMatrix.A[i][0]);
                auxData[i][9] = new Integer((int)(factorADfValuesMatrix.A[i][0]));
                auxData[i][10] = new Integer((int)(factorBDfValuesMatrix.A[i][0]));
                auxData[i][11] = new Integer((int)(interactionDfValuesMatrix.A[i][0]));
                auxData[i][12] = new Integer((int)(errorDfValuesMatrix.A[i][0]));
            }
            
            AlgorithmParameters params = algData.getParams();
            
            
            GeneralInfo info = new GeneralInfo();
            info.time = time;
            //ADD MORE INFO PARAMETERS HERE
            info.alpha = params.getFloat("alpha");
            info.adjMethod = this.getAdjMethod(params.getInt("adjustmentMethod"));
            info.pValueBasedOn = getPValueBasedOn(params.getBoolean("usePerms"));
            if (usePerms) {
                //info.useAllCombs = useAllCombs;
                info.numPerms = params.getInt("numPerms");
            }
            int function = params.getInt("distance-function");
            info.function = framework.getDistanceMenu().getFunctionName(function);
            info.hcl = params.getBoolean("hierarchical-tree");
            info.hcl_genes = params.getBoolean("calculate-genes");
            info.hcl_samples = params.getBoolean("calculate-experiments");
            if(info.hcl)
                info.hcl_method = params.getInt("method-linkage");
            return createResultTree(result_cluster, info);
            
        } finally {
            if (algorithm != null) {
                algorithm.removeAlgorithmListener(listener);
            }
            if (progress != null) {
                progress.dispose();
            }
        }
    }
    
    
    protected String getPValueBasedOn(boolean isPerm) {
        String str = "";
        if (isPerm) {
            str = "permutation";
        } else {
            str = "F-distribution";
        }
        
        return str;
    }
    
    protected String getAdjMethod(int adjMethod) {
        String methodName = "";
        
        if (adjMethod == JUST_ALPHA) {
            methodName = "None";
        } else if (adjMethod == STD_BONFERRONI) {
            methodName = "Standard Bonferroni correction";
        } else if (adjMethod == ADJ_BONFERRONI) {
            methodName = "Adjusted Bonferroni correction";
        } else if (adjMethod == MIN_P) {
            methodName = "Step-down Westfall Young: Min P";
        } else if (adjMethod == MAX_T) {
            methodName = "Step-down Westfall Young: Max T";
        }
        
        return methodName;
    }
    
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    protected DefaultMutableTreeNode createResultTree(Cluster result_cluster, GeneralInfo info) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Two-factor ANOVA");
        addResultNodes(root, result_cluster, info);
        return root;
    }
    
    /**
     * Adds result nodes into the tree root.
     */
    protected void addResultNodes(DefaultMutableTreeNode root, Cluster result_cluster, GeneralInfo info) {
        addExpressionImages(root);
        addHierarchicalTrees(root, result_cluster, info);
        addCentroidViews(root);
        addClusterInfo(root);
        addTableViews(root);
        addGeneralInfo(root, info);
    }
    
    /**
     * Adds nodes to display clusters data.
     */
    protected void addExpressionImages(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer = new TFAExperimentViewer(this.experiment, this.clusters, auxTitles, auxData);
        for (int i=0; i<this.clusters.length; i++) {
            node.add(new DefaultMutableTreeNode(new LeafInfo(clusterLabels[i], expViewer, new Integer(i))));
        }
        root.add(node);
    }
    
    protected void addTableViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table views");
        IViewer tableViewer = new ClusterTableViewer(this.experiment, this.clusters, data, auxTitles, auxData);
        //IViewer tableViewer = new ClusterTableViewer(this.experiment, this.clusters, data);
        for (int i=0; i<this.clusters.length; i++) {
            node.add(new DefaultMutableTreeNode(new LeafInfo(clusterLabels[i], tableViewer, new Integer(i))));
        }
        root.add(node);
    }
    
    /**
     * Adds nodes to display hierarchical trees.
     */
    protected void addHierarchicalTrees(DefaultMutableTreeNode root, Cluster result_cluster, GeneralInfo info) {
        if (!info.hcl) {
            return;
        }
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Hierarchical Trees");
        NodeList nodeList = result_cluster.getNodeList();
        
        if (!drawSigTreesOnly) {
            for (int i=0; i<nodeList.getSize(); i++) {
                node.add(new DefaultMutableTreeNode(new LeafInfo(clusterLabels[i], createHCLViewer(nodeList.getNode(i), info))));
            }
        } else {
            for (int i=0; i<nodeList.getSize(); i++) {
                if (i <= 2) {
                node.add(new DefaultMutableTreeNode(new LeafInfo(clusterLabels[i], createHCLViewer(nodeList.getNode(i), info))));
                }
            }            
        }
        root.add(node);
    }
    
    /**
     * Creates an <code>HCLViewer</code>.
     */
    protected IViewer createHCLViewer(Node clusterNode, GeneralInfo info) {
        HCLTreeData genes_result = info.hcl_genes ? getResult(clusterNode, 0) : null;
        HCLTreeData samples_result = info.hcl_samples ? getResult(clusterNode, info.hcl_genes ? 4 : 0) : null;
        return new HCLViewer(this.experiment, clusterNode.getFeaturesIndexes(), genes_result, samples_result);
    }
    
    /**
     * Adds nodes to display centroid charts.
     */
    protected void addCentroidViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        TFACentroidViewer centroidViewer = new TFACentroidViewer(this.experiment, clusters, auxTitles, auxData);
        centroidViewer.setMeans(this.means.A);
        centroidViewer.setVariances(this.variances.A);
        for (int i=0; i<this.clusters.length; i++) {
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo(clusterLabels[i], centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo(clusterLabels[i], centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
        }
        
        TFACentroidsViewer centroidsViewer = new TFACentroidsViewer(this.experiment, clusters, auxTitles, auxData);
        centroidsViewer.setMeans(this.means.A);
        centroidsViewer.setVariances(this.variances.A);
        
        centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
        expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        
        root.add(centroidNode);
        root.add(expressionNode);
    }
    
    /**
     * Returns a hcl tree data from the specified cluster node.
     */
    protected HCLTreeData getResult(Node clusterNode, int pos) {
        HCLTreeData data = new HCLTreeData();
        NodeValueList valueList = clusterNode.getValues();
        data.child_1_array = (int[])valueList.getNodeValue(pos).value;
        data.child_2_array = (int[])valueList.getNodeValue(pos+1).value;
        data.node_order = (int[])valueList.getNodeValue(pos+2).value;
        data.height = (float[])valueList.getNodeValue(pos+3).value;
        return data;
    }
    
    /**
     * Adds node with cluster information.
     */
    protected void addClusterInfo(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Cluster Information");
        node.add(new DefaultMutableTreeNode(new LeafInfo("Results (#,%)", new TFAInfoViewer(this.clusters, this.experiment.getNumberOfGenes(), factorNames))));
        root.add(node);
    }
    
    /**
     * Adds node with general iformation.
     */
    protected void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        //node.add(new DefaultMutableTreeNode("Test design: " + info.getTestDesign()));
        node.add(getGroupAssignmentInfo());
        
        node.add(new DefaultMutableTreeNode("Alpha (overall threshold p-value): "+info.alpha));
        node.add(new DefaultMutableTreeNode("P-values based on: "+info.pValueBasedOn));
        if (usePerms) {
            node.add(new DefaultMutableTreeNode("Number of permutations per gene: " + info.numPerms));
        }
        node.add(new DefaultMutableTreeNode("P-value adjustment: "+info.adjMethod));
        node.add(new DefaultMutableTreeNode("HCL: "+info.getMethodName()));
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time)+" ms"));
        node.add(new DefaultMutableTreeNode(info.function));
        root.add(node);
    }
    
    protected DefaultMutableTreeNode getGroupAssignmentInfo() {
        DefaultMutableTreeNode groupAssignmentInfo = new DefaultMutableTreeNode("Factor Assignments");
        DefaultMutableTreeNode factorANode = new DefaultMutableTreeNode(factorNames[0]);
        DefaultMutableTreeNode factorBNode = new DefaultMutableTreeNode(factorNames[1]);
        for (int i = 0; i < exptNamesVector.size(); i++) {
            if (factorAAssignments[i] != 0) {
                factorANode.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i)) + ": Group " + factorAAssignments[i]));
            } else {
                factorANode.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i)) + ": Unassigned"));
            }
            if (factorBAssignments[i] != 0) {
                factorBNode.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i)) + ": Group " + factorBAssignments[i]));
            } else {
                factorBNode.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i)) + ": Unassigned"));
            }
        }
        groupAssignmentInfo.add(factorANode);
        groupAssignmentInfo.add(factorBNode);
        
        return groupAssignmentInfo;
    }
    
    
    
    /**
     * The class to listen to progress, monitor and algorithms events.
     */
    protected class Listener extends DialogListener implements AlgorithmListener {
        
    	//EH added so AMP could subclass
    	protected Listener(){
    		super();
    	}
        public void valueChanged(AlgorithmEvent event) {
            switch (event.getId()) {
                case AlgorithmEvent.SET_UNITS:
                    progress.setUnits(event.getIntValue());
                    progress.setDescription(event.getDescription());
                    break;
                case AlgorithmEvent.PROGRESS_VALUE:
                    progress.setValue(event.getIntValue());
                    progress.setDescription(event.getDescription());
                    break;
                case AlgorithmEvent.MONITOR_VALUE:
                    int value = event.getIntValue();
                    if (value == -1) {
                        //monitor.dispose();
                    } else {
                        //monitor.update(value);
                    }
                    break;
            }
        }
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("cancel-command")) {
                algorithm.abort();
                progress.dispose();
                //monitor.dispose();
            }
        }
        
        public void windowClosing(WindowEvent e) {
            algorithm.abort();
            progress.dispose();
            //monitor.dispose();
        }
    }
    
    protected class GeneralInfo {
        public int clusters;
        public String adjMethod;
        public String pValueBasedOn;
        public float alpha;
        public int numPerms;
        public long time;
        public String function;
        
        protected boolean hcl;
        protected int hcl_method;
        protected boolean hcl_genes;
        protected boolean hcl_samples;
        
        public String getMethodName() {
            return hcl ? HCLGUI.GeneralInfo.getMethodName(hcl_method) : "no linkage";
        }
        
    }
    
}
