/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: SOTAGUI.java,v $
 * $Revision: 1.4 $
 * $Date: 2004-05-06 15:32:06 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.sota;

import java.util.Arrays;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;

import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Monitor;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLGUI;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLViewer;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.kmc.KMCCentroidViewer;
import org.tigr.microarray.mev.cluster.gui.impl.kmc.KMCInfoViewer;
import org.tigr.microarray.mev.cluster.gui.impl.kmc.KMCCentroidsViewer;
import org.tigr.microarray.mev.cluster.gui.impl.kmc.KMCExperimentViewer;

import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;


public class SOTAGUI implements IClusterGUI, IScriptGUI {
    
    private Algorithm algorithm;
    private IData frameData;
    private AlgorithmData data;
    private AlgorithmData result;
    private SOTATreeData sotaTreeData;
    private Progress progress;
    private Monitor monitor;
    private Listener listener;
    
    private Experiment experiment;
    private int[][] clusters;
    private FloatMatrix means;
    private FloatMatrix variances;
    private IDistanceMenu menu;
    private boolean clusterGenes;
    private int k = 0;
    
    /** Creates new SOTAGUI */
    public SOTAGUI() {
        listener = new Listener();
    }
    
    /**
     * This method should return a tree with calculation results or
     * null, if analysis start was canceled.
     *
     * @param framework the reference to <code>IFramework</code> implementation,
     *       which is used to obtain an initial analysis data and parameters.
     * @throws AlgorithmException if calculation was failed.
     * @throws AbortException if calculation was canceled.
     * @see IFramework
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        // the default values
        
        
        int maxCycles = 10;
        int maxEpochsPerCycle = 1000;
        float epochStopCriteria = (float)0.0001;
        float maxTreeDiv  = (float) 0.01;
        float migFactor_w = (float)0.01;
        float migFactor_p = (float)0.005;
        float migFactor_s = (float)0.001;
        int neighborhoodLevel = 5;
        float pValue = (float)0.05;
        boolean useVariance;
        boolean runToMaxCycles;
        boolean setMaxClusterDiv;
        float maxClusterDiv;
        
        boolean calcClusterHCL;
        boolean calcFullTreeSampleHCL;
        
        frameData = framework.getData();
        
        menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        int distFactor = 1;
        
        if ((function==Algorithm.PEARSON)           ||
        (function==Algorithm.PEARSONUNCENTERED) ||
        (function==Algorithm.PEARSONSQARED)     ||
        (function==Algorithm.COSINE)            ||
        (function==Algorithm.COVARIANCE)        ||
        (function==Algorithm.DOTPRODUCT)        ||
        (function==Algorithm.SPEARMANRANK)      ||
        (function==Algorithm.KENDALLSTAU)) {
            distFactor = -1;
        } else {
            distFactor = 1;
        }
        SOTAInitDialog sota_dialog = new SOTAInitDialog(framework.getFrame(), distFactor);
        
        //   maxCycles, maxEpochsPerCycle, maxTreeDiv, epochDivFluxLimit, migFactor_w, migFactor_p, migFactor_s);
        
        if (sota_dialog.showModal() != JOptionPane.OK_OPTION) {
            return null;
        }
        
        try{
            clusterGenes = sota_dialog.getBoolean("clusterGenes");
            maxCycles = sota_dialog.getInt("maxCycles");
            maxEpochsPerCycle = sota_dialog.getInt("maxEpochsPerCycle");
            epochStopCriteria = sota_dialog.getFloat("epochStopCriteria");
            
            
            migFactor_w = sota_dialog.getFloat("migFactor_w");
            migFactor_p = sota_dialog.getFloat("migFactor_p");
            migFactor_s = sota_dialog.getFloat("migFactor_s");
            neighborhoodLevel = sota_dialog.getInt("neighborhood-level");
            useVariance = sota_dialog.getBoolean("useVariance");
            if(useVariance)
                pValue = sota_dialog.getFloat("pValue");
            else
                maxTreeDiv = sota_dialog.getFloat("maxTreeDiv");
            runToMaxCycles = sota_dialog.getBoolean("runToMaxCycles");
            setMaxClusterDiv = sota_dialog.getBoolean("setMaxClusterDiv");
            maxClusterDiv = sota_dialog.getFloat("maxClusterDiv");
            calcFullTreeSampleHCL = false;
            calcClusterHCL = sota_dialog.getBoolean("calcClusterHCL");
            
            if(migFactor_w <=0 || migFactor_w <=0 || migFactor_w <=0 ){
                JOptionPane.showMessageDialog(framework.getFrame(), "Migration weights should be > 0", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            
            if(pValue <= 0){
                JOptionPane.showMessageDialog(framework.getFrame(), "p-value should be > 0", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            
            
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(framework.getFrame(), "Invalid input parameters!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        if (maxCycles < 1) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Number of cycles (number of clusters) must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (maxEpochsPerCycle < 1) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Number of epochs per cycle must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        
        if (calcClusterHCL || calcFullTreeSampleHCL) {
            HCLInitDialog hcl_dialog = new HCLInitDialog(framework.getFrame());
            if (hcl_dialog.showModal() != JOptionPane.OK_OPTION) {
                return null;
            }
            hcl_method = hcl_dialog.getMethod();
            hcl_samples = hcl_dialog.isClusterExperience();
            hcl_genes = hcl_dialog.isClusterGenes();
        }
        
        this.experiment = framework.getData().getExperiment();
        Listener listener = new Listener();
        
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("SOTA");
            algorithm.addAlgorithmListener(listener);
            ;
          /*  DISABLE DIVERISTY MONITOR
           *
            this.monitor = new Monitor(framework.getFrame(), "Reallocations", 25, 100, 210.0/100, maxCycles);
            this.monitor.setStepXFactor((int)Math.floor(245/maxCycles));
            this.monitor.update(100);
            this.monitor.show();
           */
            this.progress = new Progress(framework.getFrame(), "Calculating clusters", listener);
            this.progress.show();
            data = new AlgorithmData();
            FloatMatrix matrix = experiment.getMatrix();
            if(!clusterGenes)
                matrix = matrix.transpose();
            data.addMatrix("experiment", matrix);
            data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
            if (function == Algorithm.DEFAULT) {
                function = Algorithm.EUCLIDEAN;
            }
            data.addParam("distance-function", String.valueOf(function));
            data.addParam("sota-cluster-genes", String.valueOf(clusterGenes));
            data.addParam("max-number-of-cycles", String.valueOf(maxCycles));
            data.addParam("max-epochs-per-cycle", String.valueOf(maxEpochsPerCycle));
            data.addParam("epoch-improvement-cutoff", String.valueOf(epochStopCriteria));
            data.addParam("end-training-diversity", String.valueOf(maxTreeDiv));
            data.addParam("use-cluster-variance", String.valueOf(useVariance));
            data.addParam("pValue", String.valueOf(pValue));
            data.addParam("mig_w", String.valueOf(migFactor_w));
            data.addParam("mig_p", String.valueOf(migFactor_p));
            data.addParam("mig_s", String.valueOf(migFactor_s));
            data.addParam("neighborhood-level", String.valueOf(neighborhoodLevel));
            data.addParam("run-to-max-cycles", String.valueOf(runToMaxCycles));
            data.addParam("set-max-cluster-div", String.valueOf(setMaxClusterDiv));
            data.addParam("maxClusterDiv", String.valueOf(maxClusterDiv));
            data.addParam("calc-full-tree-hcl", String.valueOf(calcFullTreeSampleHCL));
            data.addParam("calc-cluster-hcl", String.valueOf(calcClusterHCL));
            
            // hcl parameters
            if (calcClusterHCL || calcFullTreeSampleHCL) {
                data.addParam("calcClusterHCL", String.valueOf(calcClusterHCL));
                data.addParam("calcFullTreeHCL", String.valueOf(calcFullTreeSampleHCL));
                data.addParam("method-linkage", String.valueOf(hcl_method));
                data.addParam("calculate-genes", String.valueOf(hcl_genes));
                data.addParam("calculate-experiments", String.valueOf(hcl_samples));
            }
            
            long start = System.currentTimeMillis();
            result = algorithm.execute(data);
            long time = System.currentTimeMillis() - start;
            // getting the results
            Cluster result_cluster = result.getCluster("cluster");
            Cluster hcl_clusters = result.getCluster("hcl-result-clusters");
            Cluster hcl_sample_tree = result.getCluster("full-tree-sample-HCL");
            
            NodeList nodeList = result_cluster.getNodeList();
            k = nodeList.getSize();
            this.clusters = new int[k][];
            
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getProbesIndexes();
            }
            
            means = result.getMatrix("centroid-matrix");
            variances = result.getMatrix("cluster-variances");
            
            
            //gather parameters
            GeneralInfo info = new GeneralInfo();
            
            //results
            info.iterations = result.getParams().getInt("cycles")-1;
            info.clusters = info.iterations + 1;
            info.time = time;
            
            //menu param
            info.function = menu.getFunctionName(function);
            
            //Growth Term Crit.
            info.maxCycles = maxCycles;
            info.maxEpochsPerCycle = maxEpochsPerCycle;
            info.diversityCutoff = maxTreeDiv;
            info.epochStopCriteria = epochStopCriteria;
            info.runToMaxCycles = runToMaxCycles;
            
            //Cell Migration/Neighborhood Parmeters
            info.migW = migFactor_w;
            info.migP = migFactor_p;
            info.migS = migFactor_s;
            info.neighborhoodLevel = neighborhoodLevel;
            
            //Cell Division Criteria
            info.useCellDiversity = !useVariance;
            info.useCellVariability = useVariance;
            if(useVariance){
                info.pValue = pValue;
                info.computedVarCutoff = result.getParams().getFloat("computed-var-cutoff");
            }
            //HCL Options
            info.hcl_on_clusters = calcClusterHCL;
            info.hcl_on_samples_on_all_genes = calcFullTreeSampleHCL;
            info.hcl_genes_in_clusters = hcl_genes;
            info.hcl_samples_in_clusters = hcl_samples;
            info.hcl = (info.hcl_on_clusters || info.hcl_on_samples_on_all_genes);
            info.hcl_method = hcl_method;
            
            loadSotaTreeData();
            
            //return createResultTree(sota, result, result_cluster, hcl_clusters, hcl_sample_tree, info);
            return createResultTree(hcl_clusters, hcl_sample_tree, info);
            
        } finally {
            if (algorithm != null) {
                algorithm.removeAlgorithmListener(listener);
            }
            if (progress != null) {
                progress.dispose();
            }
            /*if (monitor != null) {  NOT USING MONITOR
                monitor.dispose();
            }
             */
        }
    }
    
    
    
    
    /*
     * Scripting Support
     */
    public AlgorithmData getScriptParameters(IFramework framework) {
        int maxCycles = 10;
        int maxEpochsPerCycle = 1000;
        float epochStopCriteria = (float)0.0001;
        float maxTreeDiv  = (float) 0.01;
        float migFactor_w = (float)0.01;
        float migFactor_p = (float)0.005;
        float migFactor_s = (float)0.001;
        int neighborhoodLevel = 5;
        float pValue = (float)0.05;
        boolean useVariance;
        boolean runToMaxCycles;
        boolean setMaxClusterDiv;
        float maxClusterDiv;
        
        boolean calcClusterHCL;
        boolean calcFullTreeSampleHCL;
        
        frameData = framework.getData();
        
        menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        int distFactor = 1;
        
        if ((function==Algorithm.PEARSON)           ||
        (function==Algorithm.PEARSONUNCENTERED) ||
        (function==Algorithm.PEARSONSQARED)     ||
        (function==Algorithm.COSINE)            ||
        (function==Algorithm.COVARIANCE)        ||
        (function==Algorithm.DOTPRODUCT)        ||
        (function==Algorithm.SPEARMANRANK)      ||
        (function==Algorithm.KENDALLSTAU)) {
            distFactor = -1;
        } else {
            distFactor = 1;
        }
        SOTAInitDialog sota_dialog = new SOTAInitDialog(framework.getFrame(), distFactor);
        
        if (sota_dialog.showModal() != JOptionPane.OK_OPTION) {
            return null;
        }
        
        try{
            clusterGenes = sota_dialog.getBoolean("clusterGenes");
            maxCycles = sota_dialog.getInt("maxCycles");
            maxEpochsPerCycle = sota_dialog.getInt("maxEpochsPerCycle");
            epochStopCriteria = sota_dialog.getFloat("epochStopCriteria");
            
            
            migFactor_w = sota_dialog.getFloat("migFactor_w");
            migFactor_p = sota_dialog.getFloat("migFactor_p");
            migFactor_s = sota_dialog.getFloat("migFactor_s");
            neighborhoodLevel = sota_dialog.getInt("neighborhood-level");
            useVariance = sota_dialog.getBoolean("useVariance");
            if(useVariance)
                pValue = sota_dialog.getFloat("pValue");
            else
                maxTreeDiv = sota_dialog.getFloat("maxTreeDiv");
            runToMaxCycles = sota_dialog.getBoolean("runToMaxCycles");
            setMaxClusterDiv = sota_dialog.getBoolean("setMaxClusterDiv");
            maxClusterDiv = sota_dialog.getFloat("maxClusterDiv");
            calcFullTreeSampleHCL = false;
            calcClusterHCL = sota_dialog.getBoolean("calcClusterHCL");
            
            if(migFactor_w <=0 || migFactor_w <=0 || migFactor_w <=0 ){
                JOptionPane.showMessageDialog(framework.getFrame(), "Migration weights should be > 0", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            
            if(pValue <= 0){
                JOptionPane.showMessageDialog(framework.getFrame(), "p-value should be > 0", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            
            
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(framework.getFrame(), "Invalid input parameters!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        if (maxCycles < 1) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Number of cycles (number of clusters) must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (maxEpochsPerCycle < 1) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Number of epochs per cycle must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        
        if (calcClusterHCL || calcFullTreeSampleHCL) {
            HCLInitDialog hcl_dialog = new HCLInitDialog(framework.getFrame());
            if (hcl_dialog.showModal() != JOptionPane.OK_OPTION) {
                return null;
            }
            hcl_method = hcl_dialog.getMethod();
            hcl_samples = hcl_dialog.isClusterExperience();
            hcl_genes = hcl_dialog.isClusterGenes();
        }
        
        this.experiment = framework.getData().getExperiment();
        Listener listener = new Listener();
        
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("SOTA");
            algorithm.addAlgorithmListener(listener);
            
            data = new AlgorithmData();
            
            data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
            
            if (function == Algorithm.DEFAULT) {
                function = Algorithm.EUCLIDEAN;
            }
            
            data.addParam("distance-function", String.valueOf(function));
            data.addParam("sota-cluster-genes", String.valueOf(clusterGenes));
            data.addParam("max-number-of-cycles", String.valueOf(maxCycles));
            data.addParam("max-epochs-per-cycle", String.valueOf(maxEpochsPerCycle));
            data.addParam("epoch-improvement-cutoff", String.valueOf(epochStopCriteria));
            data.addParam("end-training-diversity", String.valueOf(maxTreeDiv));
            data.addParam("use-cluster-variance", String.valueOf(useVariance));
            data.addParam("pValue", String.valueOf(pValue));
            data.addParam("mig_w", String.valueOf(migFactor_w));
            data.addParam("mig_p", String.valueOf(migFactor_p));
            data.addParam("mig_s", String.valueOf(migFactor_s));
            data.addParam("neighborhood-level", String.valueOf(neighborhoodLevel));
            data.addParam("run-to-max-cycles", String.valueOf(runToMaxCycles));
            data.addParam("set-max-cluster-div", String.valueOf(setMaxClusterDiv));
            data.addParam("maxClusterDiv", String.valueOf(maxClusterDiv));
            data.addParam("calc-full-tree-hcl", String.valueOf(calcFullTreeSampleHCL));
            data.addParam("calc-cluster-hcl", String.valueOf(calcClusterHCL));
            
            // hcl parameters
            if (calcClusterHCL || calcFullTreeSampleHCL) {
                data.addParam("calcClusterHCL", String.valueOf(calcClusterHCL));
                data.addParam("calcFullTreeHCL", String.valueOf(calcFullTreeSampleHCL));
                data.addParam("method-linkage", String.valueOf(hcl_method));
                data.addParam("calculate-genes", String.valueOf(hcl_genes));
                data.addParam("calculate-experiments", String.valueOf(hcl_samples));
            }
            
            // alg name
            data.addParam("name", "SOTA");
            
            // alg type
            data.addParam("alg-type", "cluster");
            
            // output class
            data.addParam("output-class", "multi-cluster-output");
            
            //output nodes
            String [] outputNodes = new String[1];
            outputNodes[0] = "Multi-cluster";
            data.addStringArray("output-nodes", outputNodes);
            
        } catch (Exception e) {  }
        return data;
    }
    
    
    public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, Experiment experiment) throws AlgorithmException {
        this.experiment = experiment;
        Listener listener = new Listener();
        
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("SOTA");
            algorithm.addAlgorithmListener(listener);
            algData.addMatrix("experiment", experiment.getMatrix());
            
            this.progress = new Progress(framework.getFrame(), "Calculating clusters", listener);
            this.progress.show();
            
            long start = System.currentTimeMillis();
            result = algorithm.execute(algData);
            long time = System.currentTimeMillis() - start;
            // getting the results
            Cluster result_cluster = result.getCluster("cluster");
            Cluster hcl_clusters = result.getCluster("hcl-result-clusters");
            Cluster hcl_sample_tree = result.getCluster("full-tree-sample-HCL");
            
            NodeList nodeList = result_cluster.getNodeList();
            k = nodeList.getSize();
            this.clusters = new int[k][];
            
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getProbesIndexes();
            }
            
            means = result.getMatrix("centroid-matrix");
            variances = result.getMatrix("cluster-variances");
            
            
            //gather parameters
            GeneralInfo info = new GeneralInfo();
            
            //results
            info.iterations = result.getParams().getInt("cycles")-1;
            info.clusters = info.iterations + 1;
            info.time = time;
            
            AlgorithmParameters params = algData.getParams();
            
            //menu param
            info.function = framework.getDistanceMenu().getFunctionName(params.getInt("distance-function"));
            
            //Growth Term Crit.
            info.maxCycles = params.getInt("max-number-of-cycles");
            info.maxEpochsPerCycle = params.getInt("max-epochs-per-cycle");
            info.diversityCutoff = params.getFloat("end-training-diversity");
            info.epochStopCriteria = params.getFloat("epoch-improvement-cutoff");
            info.runToMaxCycles = params.getBoolean("run-to-max-cycles");
            
            //Cell Migration/Neighborhood Parmeters
            info.migW = params.getFloat("mig_w");
            info.migP = params.getFloat("mig_p");
            info.migS = params.getFloat("mig_s");
            info.neighborhoodLevel = params.getInt("neighborhood-level");
            
            //Cell Division Criteria
            info.useCellDiversity = !params.getBoolean("use-cluster-variance");
            info.useCellVariability = params.getBoolean("use-cluster-variance");
            if(info.useCellVariability){
                info.pValue = params.getFloat("pValue");
                info.computedVarCutoff = result.getParams().getFloat("computed-var-cutoff");
            }
            
            //HCL Options
            info.hcl_on_clusters = params.getBoolean("calc-cluster-hcl");
            info.hcl_on_samples_on_all_genes = params.getBoolean("calc-full-tree-hcl");
            info.hcl_genes_in_clusters = params.getBoolean("calculate_genes");
            info.hcl_samples_in_clusters = params.getBoolean("calculate-experiments");
            info.hcl = (info.hcl_on_clusters || info.hcl_on_samples_on_all_genes);
            info.hcl_method = params.getInt("method-linkage");
            
            loadSotaTreeData();
            
            //return createResultTree(sota, result, result_cluster, hcl_clusters, hcl_sample_tree, info);
            return createResultTree(hcl_clusters, hcl_sample_tree, info);
            
        } finally {
            if (algorithm != null) {
                algorithm.removeAlgorithmListener(listener);
            }
            if (progress != null) {
                progress.dispose();
            }
            /*if (monitor != null) {  NOT USING MONITOR
                monitor.dispose();
            }
             */
        }
    }
    
    
    /**
     * Loads SOTATreeData result data structure
     */
    private void loadSotaTreeData(){
        sotaTreeData = new SOTATreeData();
        sotaTreeData.nodeHeights = result.getMatrix("node-heights").getRowPackedCopy();
        sotaTreeData.leftChild = result.getIntArray("left-child");
        sotaTreeData.rightChild = result.getIntArray("right-child");
        sotaTreeData.nodePopulation = result.getIntArray("node-population");
        
        sotaTreeData.centroidMatrix = result.getMatrix("centroid-matrix");
        sotaTreeData.clusterDiversity = result.getMatrix("cluster-diversity");
        sotaTreeData.clusterPopulation = result.getIntArray("cluster-population");
        sotaTreeData.function = result.getParams().getInt("distance-function");
        sotaTreeData.factor = result.getParams().getFloat("factor");
        sotaTreeData.absolute = result.getParams().getBoolean("distance-absolute", true);
    }
    
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    private DefaultMutableTreeNode createResultTree( Cluster hcl_clusters, Cluster hcl_sample_tree, GeneralInfo info) {
        DefaultMutableTreeNode root;
        if(this.clusterGenes)
            root = new DefaultMutableTreeNode("SOTA - genes");
        else
            root = new DefaultMutableTreeNode("SOTA - experiments");
        addResultNodes(root, hcl_clusters, hcl_sample_tree, info);
        return root;
    }
    
    /**
     * Adds result nodes into the tree root.
     */
    private void addResultNodes(DefaultMutableTreeNode root, Cluster hcl_clusters, Cluster hcl_sample_tree, GeneralInfo info) {
        
        SOTAGeneTreeViewer sotaViewer = null;
        SOTAExperimentTreeViewer sotaExpViewer;
        DefaultMutableTreeNode expressionImageNode = null;
        if(clusterGenes){
            sotaViewer = addSotaGeneViewer(root, hcl_sample_tree);
            expressionImageNode = addExpressionImages(root);
            sotaViewer.associateExpressionImageNode(expressionImageNode);  //allows viewr to have a node handle to exp images
        }
        else{
            sotaExpViewer = addSotaExperimentViewer(root, hcl_sample_tree);
            expressionImageNode = addExpressionImages(root);
            sotaExpViewer.associateExpressionImageNode(expressionImageNode);  //allows viewr to have a node handle to exp images
        }
        addHierarchicalTrees(root, hcl_clusters, info);
        addCentroidViews(root);
        addTableViews(root);
        addDiversityViewer(root);
        addClusterInfo(root);
        addGeneralInfo(root, info);
    }
    
    
    private void addTableViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table views");
        IViewer tabViewer;
        if (clusterGenes)
            tabViewer = new ClusterTableViewer(this.experiment, this.clusters, this.frameData);
        else
            tabViewer = new ExperimentClusterTableViewer(this.experiment, this.clusters, this.frameData);
        //return; //placeholder for ExptClusterTableViewer
        
        for (int i=0; i<this.clusters.length; i++) {
            node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), tabViewer, new Integer(i))));
        }
        root.add(node);
        //return node;
    }
    
    private SOTAGeneTreeViewer addSotaGeneViewer(DefaultMutableTreeNode root, Cluster hcl_sample_tree){
        SOTAGeneTreeViewer viewer = new SOTAGeneTreeViewer(experiment, sotaTreeData, hcl_sample_tree, this.clusters);
        if(viewer != null){
            root.add(new DefaultMutableTreeNode(  new LeafInfo("SOTA Dendogram", viewer)  ));
        }
        return viewer;
    }
    
    private SOTAExperimentTreeViewer addSotaExperimentViewer(DefaultMutableTreeNode root, Cluster hcl_sample_tree){
        SOTAExperimentTreeViewer viewer = new SOTAExperimentTreeViewer(experiment, sotaTreeData, this.clusters);
        
        if(viewer != null){
            root.add(new DefaultMutableTreeNode(  new LeafInfo("SOTA Dendogram", viewer)  ));
        }
        return viewer;
    }
    
    private void addDiversityViewer(DefaultMutableTreeNode root){
        if(result != null)
            root.add(new DefaultMutableTreeNode( new LeafInfo("SOTA Diversity History", new SOTADiversityViewer(result.getMatrix("cycle-diversity"))  )));
    }
    
    /**
     * Adds nodes to display clusters data.
     */
    private DefaultMutableTreeNode addExpressionImages(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer;
        if(clusterGenes)
            expViewer= new SOTAExperimentViewer(this.experiment, this.clusters, result.getMatrix("centroid-matrix"), result.getMatrix("cluster-diversity"), sotaTreeData);
        else
            expViewer= new SOTAExperimentViewer(this.experiment, this.clusters, result.getMatrix("centroid-matrix"), result.getMatrix("cluster-diversity"), sotaTreeData, this.clusterGenes);
        
        for (int i=0; i<this.clusters.length; i++) {
            node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expViewer, new Integer(i))));
        }
        root.add(node);
        return node;
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
        int [][] clusters = null;
        
        if(!this.clusterGenes){
            clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            if(info.hcl_samples_in_clusters)
                clusters = getOrderedIndices(nodeList, clusters, info.hcl_genes_in_clusters);
        }
        for (int i=0; i<nodeList.getSize(); i++) {
            if(this.clusterGenes)
                node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), createHCLViewer(nodeList.getNode(i), info, null))));
            else
                node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), createHCLViewer(nodeList.getNode(i), info, clusters), new Integer(i))));
        }
        root.add(node);
    }
    
    /**
     * Creates an <code>HCLViewer</code>.
     */
    private IViewer createHCLViewer(Node clusterNode, GeneralInfo info, int [][] sampleClusters) {
        HCLTreeData genes_result = info.hcl_genes_in_clusters ? getResult(clusterNode, 0) : null;
        HCLTreeData samples_result = info.hcl_samples_in_clusters ? getResult(clusterNode, info.hcl_genes_in_clusters ? 4 : 0) : null;
        if(this.clusterGenes)
            return new HCLViewer(this.experiment, clusterNode.getFeaturesIndexes(), genes_result, samples_result);
        else
            return new HCLViewer(this.experiment, clusterNode.getFeaturesIndexes(), genes_result, samples_result, sampleClusters, true);
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
        if(this.clusterGenes)
            node.add(new DefaultMutableTreeNode(new LeafInfo("Genes in Clusters (#,%)", new SOTAInfoViewer(this.clusters, this.experiment.getNumberOfGenes()))));
        else
            node.add(new DefaultMutableTreeNode(new LeafInfo("Experiments in Clusters (#,%)", new SOTAInfoViewer(this.clusters, this.experiment.getNumberOfSamples(), false))));
        root.add(node);
    }
    
    
    
    /**
     * Adds nodes to display centroid charts.
     */
    private void addCentroidViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        
        SOTACentroidViewer centroidViewer;
        SOTAExperimentCentroidViewer expCentroidViewer;
        if(clusterGenes){
            centroidViewer = new SOTACentroidViewer(this.experiment, clusters);
            centroidViewer.setMeans(this.means.A);
            centroidViewer.setVariances(this.variances.A);
            for (int i=0; i<this.clusters.length; i++) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            
            SOTACentroidsViewer centroidsViewer = new SOTACentroidsViewer(this.experiment, clusters);
            centroidsViewer.setMeans(this.means.A);
            centroidsViewer.setVariances(this.variances.A);
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        }
        else{
            expCentroidViewer = new SOTAExperimentCentroidViewer(this.experiment, clusters);
            
            expCentroidViewer.setMeans(this.means.A);
            expCentroidViewer.setVariances(this.variances.A);
            for (int i=0; i<this.clusters.length; i++) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            SOTAExperimentCentroidsViewer expCentroidsViewer = new SOTAExperimentCentroidsViewer(this.experiment, clusters);
            expCentroidsViewer.setMeans(this.means.A);
            expCentroidsViewer.setVariances(this.variances.A);
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", expCentroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", expCentroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        }
        root.add(centroidNode);
        root.add(expressionNode);
    }
    
    /**
     * Adds node with general iformation.
     */
    private void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        
        DefaultMutableTreeNode gtc = new DefaultMutableTreeNode("Growth Termination Criteria");
        gtc.add(new DefaultMutableTreeNode("Max. cycles: "+String.valueOf(info.maxCycles)));
        gtc.add(new DefaultMutableTreeNode("Max. epochs/cycle: "+String.valueOf(info.maxEpochsPerCycle)));
        if(info.useCellDiversity)
            gtc.add(new DefaultMutableTreeNode("Max. cell div: "+String.valueOf(info.diversityCutoff)));
        else
            gtc.add(new DefaultMutableTreeNode("Max. cell div: n/a (used variability and p value)"));
        
        
        gtc.add(new DefaultMutableTreeNode("Min. epoch error change: "+String.valueOf(info.epochStopCriteria)));
        gtc.add(new DefaultMutableTreeNode("Run max. cycles (unrestricted): "+String.valueOf(info.runToMaxCycles)));
        
        DefaultMutableTreeNode mp = new DefaultMutableTreeNode("Migration Parameters");
        mp.add(new DefaultMutableTreeNode("Winning cell mig. factor: "+String.valueOf(info.migW)));
        mp.add(new DefaultMutableTreeNode("Parent cell mig. factor: "+String.valueOf(info.migP)));
        mp.add(new DefaultMutableTreeNode("Sister cell mig. factor: "+String.valueOf(info.migS)));
        mp.add(new DefaultMutableTreeNode("Neighborhood level: "+String.valueOf(info.neighborhoodLevel)));
        
        DefaultMutableTreeNode cdc = new DefaultMutableTreeNode("Cell Division Criteria");
        
        cdc.add(new DefaultMutableTreeNode("Diversity division criteria: "+String.valueOf(info.useCellDiversity)));
        cdc.add(new DefaultMutableTreeNode("Variability division criteria: "+String.valueOf(info.useCellVariability)));
        if(info.useCellVariability){
            cdc.add(new DefaultMutableTreeNode("Variablity pValue: "+String.valueOf(info.pValue)));
            cdc.add(new DefaultMutableTreeNode("Computed Cell Var. Cutoff: "+String.valueOf(info.computedVarCutoff)));
        }
        else{
            cdc.add(new DefaultMutableTreeNode("Variablity pValue: n/a"));
            cdc.add(new DefaultMutableTreeNode("Computed cell var. cutoff: n/a"));
        }
        
        DefaultMutableTreeNode ho = new DefaultMutableTreeNode("HCL Options");
        if(info.hcl_on_samples_on_all_genes || info.hcl_on_clusters){
            ho.add(new DefaultMutableTreeNode("HCL on samples (over all genes): "+String.valueOf(info.hcl_on_samples_on_all_genes)));
            ho.add(new DefaultMutableTreeNode("HCL on samples in clusters: "+String.valueOf(info.hcl_samples_in_clusters)));
            ho.add(new DefaultMutableTreeNode("HCL on genes in clusters: "+String.valueOf(info.hcl_genes_in_clusters)));
            ho.add(new DefaultMutableTreeNode("Tree - "+info.getMethodName()));
        }
        
        node.add(gtc);
        node.add(mp);
        node.add(cdc);
        
        if(info.hcl_on_samples_on_all_genes || info.hcl_on_clusters)
            node.add(ho);
        
        //results
        node.add(new DefaultMutableTreeNode("number of clusters: "+String.valueOf(info.clusters)));
        node.add(new DefaultMutableTreeNode("Cycles run: "+String.valueOf(info.iterations)));
        node.add(new DefaultMutableTreeNode(info.function));
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time)+" ms"));
        
        root.add(node);
    }
    
    /***************************************************************************************
     * Code to order sample clustering results based on HCL runs.  sampleClusters contain an array
     * of sample indices for each experiment cluster.  Note that these indicies are ordered in
     * an order which matches HCL input matrix sample order so that HCL results (node-order) can
     * be used to order leaf indices to match HCL samples results
     */
    private int [][] getOrderedIndices(NodeList nodeList, int [][] sampleClusters, boolean calcGeneHCL){
        HCLTreeData result;
        for(int i = 0; i < sampleClusters.length ; i++){
            if(sampleClusters[i].length > 0){
                result = getResult(nodeList.getNode(i), calcGeneHCL ? 4 : 0);  //get sample Result
                sampleClusters[i] = getSampleOrder(result, sampleClusters[i]);
            }
        }
        return sampleClusters;
    }
    
    private int[] getSampleOrder(HCLTreeData result, int[] indices) {
        return getLeafOrder(result.node_order, result.child_1_array, result.child_2_array, indices);
    }
    
    private int[] getLeafOrder(int[] nodeOrder, int[] child1, int[] child2, int[] indices) {
        int[] leafOrder = new int[nodeOrder.length];
        Arrays.fill(leafOrder, -1);
        fillLeafOrder(leafOrder, child1, child2, 0, child1.length-2, indices);
        return leafOrder;
    }
    
    private int fillLeafOrder(int[] leafOrder, int[] child1, int[] child2, int pos, int index, int[] indices) {
        if (child1[index] != -1) {
            pos = fillLeafOrder(leafOrder, child1, child2, pos, child1[index], indices);
        }
        if (child2[index] != -1) {
            pos = fillLeafOrder(leafOrder, child1, child2, pos, child2[index], indices);
        } else {
            leafOrder[pos] = indices == null ? index : indices[index];
            pos++;
        }
        return pos;
    }
    
    /****************************************************************************************
     * End of Sample Cluster index ordering code
     */
    
    
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
                        monitor.dispose();
                    } else if(value < 245){
                        monitor.update(value);
                    }
                    break;
            }
        }
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("cancel-command")) {
                algorithm.abort();
                progress.dispose();
                monitor.dispose();
            }
        }
        
        public void windowClosing(WindowEvent e) {
            algorithm.abort();
            progress.dispose();
            monitor.dispose();
        }
    }
    
    // the general info structure
    private class GeneralInfo {
        
        //results
        public int clusters;
        public int iterations;
        public long time;
        
        //diatance metric
        public String function;
        
        //Growth Term Crit.
        public int maxCycles;
        public int maxEpochsPerCycle;
        public float diversityCutoff;
        public float epochStopCriteria;
        public boolean runToMaxCycles;
        
        //Cell Migration/Neighborhood Parmeters
        public float migW;
        public float migP;
        public float migS;
        public int neighborhoodLevel;
        
        //Cell Division Criteria
        public boolean useCellDiversity;
        public boolean useCellVariability;
        public float pValue;
        public float computedVarCutoff;
        
        //HCL Options
        public boolean hcl;
        public int hcl_method;
        public boolean hcl_on_clusters;
        public boolean hcl_on_samples_on_all_genes;
        public boolean hcl_genes_in_clusters;
        public boolean hcl_samples_in_clusters;
        
        
        /*
         *  Returns the linkage method if HCL trees were created for SOTA clusters
         */
        public String getMethodName() {
            return hcl ? HCLGUI.GeneralInfo.getMethodName(hcl_method) : "no linkage";
        }
        
    }
    
    
    
}


