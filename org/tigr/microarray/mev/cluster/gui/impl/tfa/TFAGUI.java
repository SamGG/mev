/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
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
    int[] factorAAssignments, factorBAssignments;
    
    private Object[][] auxData;
    private String[] auxTitles;
    String[] clusterLabels;
    private boolean usePerms, drawSigTreesOnly;
    
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
        
        TFAInitBox2 t2Box  = new TFAInitBox2((JFrame)framework.getFrame(), true, exptNamesVector, factorNames, numFactorLevels);
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
        factorAAssignments = t2Box.getFactorAAssignments();
        factorBAssignments = t2Box.getFactorBAssignments();
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
            function = Algorithm.EUCLIDEAN;
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
        
        TFAInitBox2 t2Box  = new TFAInitBox2((JFrame)framework.getFrame(), true, exptNamesVector, factorNames, numFactorLevels);
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
        factorAAssignments = t2Box.getFactorAAssignments();
        factorBAssignments = t2Box.getFactorBAssignments();
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
            function = Algorithm.EUCLIDEAN;
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
    
    
    private String getPValueBasedOn(boolean isPerm) {
        String str = "";
        if (isPerm) {
            str = "permutation";
        } else {
            str = "F-distribution";
        }
        
        return str;
    }
    
    private String getAdjMethod(int adjMethod) {
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
    private DefaultMutableTreeNode createResultTree(Cluster result_cluster, GeneralInfo info) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Two-factor ANOVA");
        addResultNodes(root, result_cluster, info);
        return root;
    }
    
    /**
     * Adds result nodes into the tree root.
     */
    private void addResultNodes(DefaultMutableTreeNode root, Cluster result_cluster, GeneralInfo info) {
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
    private void addExpressionImages(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer = new TFAExperimentViewer(this.experiment, this.clusters, auxTitles, auxData);
        for (int i=0; i<this.clusters.length; i++) {
            node.add(new DefaultMutableTreeNode(new LeafInfo(clusterLabels[i], expViewer, new Integer(i))));
        }
        root.add(node);
    }
    
    private void addTableViews(DefaultMutableTreeNode root) {
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
    private void addHierarchicalTrees(DefaultMutableTreeNode root, Cluster result_cluster, GeneralInfo info) {
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
    private IViewer createHCLViewer(Node clusterNode, GeneralInfo info) {
        HCLTreeData genes_result = info.hcl_genes ? getResult(clusterNode, 0) : null;
        HCLTreeData samples_result = info.hcl_samples ? getResult(clusterNode, info.hcl_genes ? 4 : 0) : null;
        return new HCLViewer(this.experiment, clusterNode.getFeaturesIndexes(), genes_result, samples_result);
    }
    
    /**
     * Adds nodes to display centroid charts.
     */
    private void addCentroidViews(DefaultMutableTreeNode root) {
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
    private HCLTreeData getResult(Node clusterNode, int pos) {
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
    private void addClusterInfo(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Cluster Information");
        node.add(new DefaultMutableTreeNode(new LeafInfo("Results (#,%)", new TFAInfoViewer(this.clusters, this.experiment.getNumberOfGenes(), factorNames))));
        root.add(node);
    }
    
    /**
     * Adds node with general iformation.
     */
    private void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
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
    
    private DefaultMutableTreeNode getGroupAssignmentInfo() {
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
    private class Listener extends DialogListener implements AlgorithmListener {
        
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
    
    private class GeneralInfo {
        public int clusters;
        public String adjMethod;
        public String pValueBasedOn;
        public float alpha;
        public int numPerms;
        public long time;
        public String function;
        
        private boolean hcl;
        private int hcl_method;
        private boolean hcl_genes;
        private boolean hcl_samples;
        
        public String getMethodName() {
            return hcl ? HCLGUI.GeneralInfo.getMethodName(hcl_method) : "no linkage";
        }
        
    }
    
	
	// for pipeline, pass the normalized cell file entry-- multiple experiment
	// to
	// ttest window for user to choose for grouping and pairing
	public Vector getExperiment(ArrayList list) {
		Vector v = new Vector();// use vector to store files
		for (int i = 0; i < list.size(); i++) {
			String exp = (String) list.get(i);
			v.add(exp);

		}
		return v;

	}
	//CCC 6/2/06 for AMP
	public AlgorithmData execute(AlgorithmData algData, String dir)
			throws AlgorithmException {
		AlgorithmData result = null;
		Algorithm algorithm = new TFA();
		long start = System.currentTimeMillis();
		try {

			result = algorithm.execute(algData);

		} catch (Exception e) {
			throw new AlgorithmException(e.toString());
		}
		long time = System.currentTimeMillis() - start;
		algData.addParam("name", "2-FACT-ANOVA");
		this.clusterLabels = algData.getStringArray("output-nodes");
		this.factorNames = algData.getStringArray("factor-names");
		this.factorAAssignments = algData.getIntArray("factorAAssignments");
		this.factorBAssignments = algData.getIntArray("factorBAssignments");
		this.drawSigTreesOnly = algData.getParams().getBoolean(
				"draw-sig-trees-only");

		numFactorLevels = new int[2];

		for (int i = 0; i < factorNames.length; i++)
			factorNames[i] = factorNames[i].replace(' ', '-');

		try {

			// getting the results
			Cluster result_cluster = result.getCluster("cluster");
			NodeList nodeList = result_cluster.getNodeList();
			System.out.println("-->in TFAGUI--execute--nodeList==" + nodeList);
			AlgorithmParameters params = algData.getParams();
			result.setParams(params);
			int k = 7; // resultMap.getInt("number-of-clusters"); // NEED THIS
						// TO GET THE VALUE OF NUMBER-OF-CLUSTERS

			this.clusters = new int[k][];
			for (int i = 0; i < k; i++) {
				clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
			}
			this.means = result.getMatrix("clusters_means");
			this.variances = result.getMatrix("clusters_variances");
			FloatMatrix factorAFValuesMatrix = result
					.getMatrix("factorAFValuesMatrix");
			FloatMatrix factorBFValuesMatrix = result
					.getMatrix("factorBFValuesMatrix");
			FloatMatrix interactionFValuesMatrix = result
					.getMatrix("interactionFValuesMatrix");

			FloatMatrix factorADfValuesMatrix = result
					.getMatrix("factorADfValuesMatrix");
			FloatMatrix factorBDfValuesMatrix = result
					.getMatrix("factorBDfValuesMatrix");
			FloatMatrix interactionDfValuesMatrix = result
					.getMatrix("interactionDfValuesMatrix");
			FloatMatrix errorDfValuesMatrix = result
					.getMatrix("errorDfValuesMatrix");

			FloatMatrix origFactorAPValuesMatrix = result
					.getMatrix("origFactorAPValuesMatrix");
			FloatMatrix origFactorBPValuesMatrix = result
					.getMatrix("origFactorBPValuesMatrix");
			FloatMatrix origInteractionPValuesMatrix = result
					.getMatrix("origInteractionPValuesMatrix");

			FloatMatrix adjFactorAPValuesMatrix = result
					.getMatrix("adjFactorAPValuesMatrix");
			FloatMatrix adjFactorBPValuesMatrix = result
					.getMatrix("adjFactorBPValuesMatrix");
			FloatMatrix adjInteractionPValuesMatrix = result
					.getMatrix("adjInteractionPValuesMatrix");

			auxTitles = new String[13];
	
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

			result.addStringArray("titles", auxTitles);

			FloatMatrix exp = result.getMatrix("experiment");
			int[] columns = new int[exp.getColumnDimension()];

			for (int i = 0; i < exp.getColumnDimension(); i++)
				columns[i] = i;

			experiment = new Experiment(result.getMatrix("experiment"), columns);

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
				auxData[i][9] = new Integer(
						(int) (factorADfValuesMatrix.A[i][0]));
				auxData[i][10] = new Integer(
						(int) (factorBDfValuesMatrix.A[i][0]));
				auxData[i][11] = new Integer(
						(int) (interactionDfValuesMatrix.A[i][0]));
				auxData[i][12] = new Integer(
						(int) (errorDfValuesMatrix.A[i][0]));
			}

			GeneralInfo info = new GeneralInfo();
			info.time = time;
			// ADD MORE INFO PARAMETERS HERE
			info.alpha = params.getFloat("alpha");
			info.adjMethod = this.getAdjMethod(params
					.getInt("adjustmentMethod"));
			info.pValueBasedOn = getPValueBasedOn(params.getBoolean("usePerms"));
			if (usePerms) {
				// info.useAllCombs = useAllCombs;
				info.numPerms = params.getInt("numPerms");
			}
	
			info.hcl = params.getBoolean("hierarchical-tree");
			info.hcl_genes = params.getBoolean("calculate-genes");
			info.hcl_samples = params.getBoolean("calculate-experiments");
			if (info.hcl)
				info.hcl_method = params.getInt("method-linkage");
	
			String[] samples = algData.getStringArray("sample_annotation");
			
			String[] genes = algData.getStringArray("gene_annotation");
			
			result.addStringArray("output-nodes", clusterLabels);
			result.addObjectMatrix("auxData", auxData);

			result.addStringArray("sample_annotation", samples);// sample_annotation
			result.addStringArray("gene_annotation", genes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		writeClustersReports(result, dir);
		return result;

	}
	//CCC 4/3/06 for AMP
	public void writeClustersReports(AlgorithmData data, String outDir) {
		String path = outDir;
		try {
			if (auxTitles.length == 0) {
				ExperimentUtil
						.writeExperiment(path, experiment, clusters, data);
			} else {
				ExperimentUtil.writeAllGeneClustersWithAux(path, experiment,
						clusters, data);
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
	//CCC 3/4/06 for AMP extract parameters fro dialog
	public AlgorithmData getParams(ArrayList list) throws AlgorithmException {

		Vector exptNamesVector = getExperiment(list);
		JFrame frame = null;
		TFAInitBox1 t1Box = new TFAInitBox1(frame, true);
		t1Box.setVisible(true);
		if (!t1Box.isOkPressed())
			return null;

		factorNames = new String[2];
		numFactorLevels = new int[2];

		factorNames[0] = t1Box.getFactorAName();
		factorNames[1] = t1Box.getFactorBName();

		String[] localClustNames = {
				factorNames[0].replaceAll(" ", "") + "-Sig-Genes",
				factorNames[1].replaceAll(" ", "") + "-Sig-Genes",
				"Int-Sig-Genes",
				factorNames[0].replaceAll(" ", "") + "-Nonsig-Genes",
				factorNames[1].replaceAll(" ", "") + "-Nonsig-Genes",
				"Int-Nonsig-Genes", "Nonsig-for-All-Effects"};

		clusterLabels = new String[localClustNames.length];

		for (int i = 0; i < clusterLabels.length; i++) {
			clusterLabels[i] = localClustNames[i];
		}

		numFactorLevels[0] = t1Box.getNumFactorALevels();
		numFactorLevels[1] = t1Box.getNumFactorBLevels();

		TFAInitBox2 t2Box = new TFAInitBox2(frame, true, exptNamesVector,
				factorNames, numFactorLevels);
		t2Box.setVisible(true);

		if (!t2Box.isOkPressed())
			return null;

		boolean allCellsHaveOneSample = t2Box.allCellsHaveOneSample();
		boolean isHierarchicalTree = t2Box.drawTrees();
		drawSigTreesOnly = true;
		if (isHierarchicalTree) {
			drawSigTreesOnly = t2Box.drawSigTreesOnly();
		}
		int adjustmentMethod = t2Box.getAdjustmentMethod();
		float alpha = t2Box.getAlpha();
		factorAAssignments = t2Box.getFactorAAssignments();
		factorBAssignments = t2Box.getFactorBAssignments();
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

		int function = Algorithm.EUCLIDEAN;// for pipeline, this is default
											// because no access of viewer
		// hcl init
		int hcl_method = 0;
		boolean hcl_samples = false;
		boolean hcl_genes = false;
		int hcl_function = 4;
		boolean hcl_absolute = false;
		if (isHierarchicalTree) {
			// HCLInitDialog hcl_dialog = new HCLInitDialog(frame,
			// menu.getFunctionName(function), menu.isAbsoluteDistance(), true);
			HCLInitDialog hcl_dialog = new HCLInitDialog(frame,
					"Euclidean Distance", hcl_absolute, true);
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
		AlgorithmData data = new AlgorithmData();
		// hcl end
		try {
			algorithm = new AlgorithmFactoryImpl().getAlgorithm("TFA");
			algorithm.addAlgorithmListener(listener);

			data.addParam("distance-function", String.valueOf(function));
			data.addIntArray("numFactorLevels", numFactorLevels);
			data.addParam("allCellsHaveOneSample", String
					.valueOf(allCellsHaveOneSample));
			data.addParam("adjustmentMethod", String.valueOf(adjustmentMethod));
			data.addParam("alpha", String.valueOf(alpha));
			data.addIntArray("factorAAssignments", factorAAssignments);
			data.addIntArray("factorBAssignments", factorBAssignments);
			data
					.addObjectMatrix("bothFactorAssignments",
							bothFactorAssignments);
			data.addParam("isBalancedDesign", String.valueOf(isBalancedDesign));
			data.addParam("usePerms", String.valueOf(usePerms));
			data.addParam("numPerms", String.valueOf(numPerms));

			data.addStringArray("factor-names", factorNames);
			data.addStringArray("cluster-labels", clusterLabels);
			data.addStringArray("output-nodes", clusterLabels);
			// hcl parameters
			if (isHierarchicalTree) {
				data.addParam("hierarchical-tree", String.valueOf(true));
				data.addParam("draw-sig-trees-only", String
						.valueOf(drawSigTreesOnly));
				data.addParam("method-linkage", String.valueOf(hcl_method));
				data.addParam("calculate-genes", String.valueOf(hcl_genes));
				data.addParam("calculate-experiments", String
						.valueOf(hcl_samples));
				data.addParam("hcl-distance-function", String
						.valueOf(hcl_function));
				data.addParam("hcl-distance-absolute", String
						.valueOf(hcl_absolute));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		String[] sampleNames = new String[exptNamesVector.size()];
		for (int i = 0; i < exptNamesVector.size(); i++)
			sampleNames[i] = (String) exptNamesVector.get(i);

		data.addStringArray("sample_annotation", sampleNames);//
		if (algorithm != null) {
			algorithm.removeAlgorithmListener(listener);
		}
		// sendObject(data, "2020");
		return data;
	}
	//CCC 5/5/06 for AMP
	private void sendObject(AlgorithmData adata, String uid, String rid,
			String notes, String returnURL) {
		OutputStream out;
		ObjectOutputStream objectStream;

		try {
			HTTPObject http = new HTTPObject(returnURL);
			URLConnection connection = http.getConnectionToServlet();
			out = connection.getOutputStream();

			// now send the job object to the Servlet
			objectStream = new ObjectOutputStream(out);
			Vector v = new Vector();
			v.add(uid);
			v.add(rid);
			v.add("TFA");// to identify the pda that sends the object
			if (adata != null)
				System.out.println("adata=" + adata.toString());

			v.add(adata);
			v.add(notes);
			objectStream.writeObject(v); // don't read POST in doget or
											// you'll get responsecode 405
			objectStream.flush();
			objectStream.close();

			out.close();

			// get the inputstream (server response)
			System.out.println(http.readTextInputStream(connection
					.getInputStream(), connection));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String args[]) {

	
		String uid = args[0];
		String rid = args[1];
		String study = args[2];
		String notes = args[3];
		//The URL of the action handler that will take the parameters
		String returnURL = args[5];
		AlgorithmData adata = null;
		ArrayList al = new ArrayList();
		for (int i = 6; i < args.length; i++)// the rest of argument is the
												// experiment names
		{
			al.add(args[i]);
		}

		TFAGUI tfa = new TFAGUI();
		try {
			adata = (AlgorithmData) tfa.getParams(al);
		} catch (Exception e) {
			e.printStackTrace();
		}
		tfa.sendObject(adata, uid, rid, notes, returnURL);// pass the userid and rid back
												// to server
	
	}

}
