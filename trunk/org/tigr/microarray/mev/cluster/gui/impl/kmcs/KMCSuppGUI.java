/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: KMCSuppGUI.java,v $
 * $Revision: 1.5 $
 * $Date: 2004-05-26 13:17:20 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.kmcs;

import java.awt.Color;
import java.util.Arrays;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.util.FloatMatrix;
import org.tigr.util.ConfMap;

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

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Monitor;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLInitDialog;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLViewer;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLGUI;

import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;


/**
 *
 * @author  nbhagaba
 * @version
 */
public class KMCSuppGUI implements IClusterGUI, IScriptGUI {
    
    
    private Algorithm algorithm;
    private Progress progress;
    //private Monitor monitor;
    
    private Experiment experiment;
    private int[][] clusters;
    private int k;
    private FloatMatrix means;
    private FloatMatrix variances;
    
    private IData data;
    
    private boolean unassignedExists;
    private boolean clusterGenes;
    
    
    /** Creates new KMCSuppGUI */
    public KMCSuppGUI() {
    }
    
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        // the default values
        int numClusters = 10;
        int iterations = 50;
        data = framework.getData();
        
        KMCSupportDialog kmcsDialog = new KMCSupportDialog((JFrame) framework.getFrame(), true);
        kmcsDialog.setVisible(true);
        
        if (!kmcsDialog.isOkPressed()) return null;
        int numReps = 0;
        double thresholdPercent = 0.0;
        try {
            numClusters = kmcsDialog.getNumClusters();
            iterations = kmcsDialog.getIterations();
            numReps = kmcsDialog.getNumReps();
            thresholdPercent = kmcsDialog.getThresholdPercent();
            clusterGenes = kmcsDialog.isClusterGenes();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Invalid input parameters!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        boolean isHierarchicalTree = kmcsDialog.isDrawTrees();
        boolean calculateMeans = kmcsDialog.meansChosen();
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        if (isHierarchicalTree) {
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
            algorithm = framework.getAlgorithmFactory().getAlgorithm("KMCS");
            algorithm.addAlgorithmListener(listener);
            
            int genes = experiment.getNumberOfGenes();
            //this.monitor = new Monitor(framework.getFrame(), "Reallocations", 25, 100, 210.0/genes);
            //this.monitor.setStepXFactor((int)Math.floor(245/iterations));
            //this.monitor.update(genes);
            //this.monitor.show();
            
            this.progress = new Progress(framework.getFrame(), "Calculating clusters", listener);
            this.progress.show();
            
            AlgorithmData data = new AlgorithmData();
            FloatMatrix matrix = experiment.getMatrix();
            if(!clusterGenes)
                matrix = matrix.transpose();
            data.addMatrix("experiment", matrix);
            data.addParam("kmc-cluster-genes", String.valueOf(clusterGenes));
            data.addParam("distance-factor", String.valueOf(1.0f));
            IDistanceMenu menu = framework.getDistanceMenu();
            data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
            int function = menu.getDistanceFunction();
            if (function == Algorithm.DEFAULT) {
                function = Algorithm.EUCLIDEAN;
            }
            data.addParam("distance-function", String.valueOf(function));
            data.addParam("number-of-desired-clusters", String.valueOf(numClusters));            
            data.addParam("number-of-iterations", String.valueOf(iterations));
            data.addParam("number-of-repetitions", String.valueOf(numReps));
            data.addParam("threshold-percent", String.valueOf(thresholdPercent));
            data.addParam("calculate-means", String.valueOf(calculateMeans));
            // hcl parameters
            if (isHierarchicalTree) {
                data.addParam("hierarchical-tree", String.valueOf(true));
                data.addParam("method-linkage", String.valueOf(hcl_method));
                data.addParam("calculate-genes", String.valueOf(hcl_genes));
                data.addParam("calculate-experiments", String.valueOf(hcl_samples));
            }
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(data);
            long time = System.currentTimeMillis() - start;
            // getting the results
            Cluster result_cluster = result.getCluster("cluster");
            NodeList nodeList = result_cluster.getNodeList();
            AlgorithmParameters resultMap = result.getParams();
            k = resultMap.getInt("number-of-clusters"); // NEED THIS TO GET THE VALUE OF NUMBER-OF-CLUSTERS
            unassignedExists = resultMap.getBoolean("unassigned-genes-exist");
            this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            
            GeneralInfo info = new GeneralInfo();
            if (unassignedExists) {
                info.clusters = k - 1;
            } else {
                info.clusters = k;
            }
            info.userNumClusters = numClusters;
            if (calculateMeans) {
                info.meansOrMedians = "K-Means";
            } else {
                info.meansOrMedians = "K-Medians";
            }
            //info.converged = result.getParams().getBoolean("converged");
            info.iterations = iterations;//result.getParams().getInt("iterations");
            info.time = time;
            info.numReps = numReps;
            info.thresholdPercent = thresholdPercent;
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
    }
    
    public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, Experiment experiment) throws AlgorithmException {
        this.experiment = experiment;
        this.data = framework.getData();
        this.clusterGenes = algData.getParams().getBoolean("kmc-cluster-genes");
        boolean calculateMeans = algData.getParams().getBoolean("calculate-means");
        
        Listener listener = new Listener();
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("KMCS");
            algorithm.addAlgorithmListener(listener);
            
            int genes = experiment.getNumberOfGenes();
            this.progress = new Progress(framework.getFrame(), "Calculating clusters", listener);
            this.progress.show();
            
            AlgorithmData data = new AlgorithmData();
            FloatMatrix matrix = experiment.getMatrix();
            if(!clusterGenes)
                matrix = matrix.transpose();
            algData.addMatrix("experiment", matrix);
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(algData);
            long time = System.currentTimeMillis() - start;
            // getting the results
            Cluster result_cluster = result.getCluster("cluster");
            NodeList nodeList = result_cluster.getNodeList();
            AlgorithmParameters resultMap = result.getParams();
            k = resultMap.getInt("number-of-clusters"); // NEED THIS TO GET THE VALUE OF NUMBER-OF-CLUSTERS
            unassignedExists = resultMap.getBoolean("unassigned-genes-exist");
            this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            AlgorithmParameters params = algData.getParams();
            
            GeneralInfo info = new GeneralInfo();
            if (unassignedExists) {
                info.clusters = k - 1;
            } else {
                info.clusters = k;
            }
            info.userNumClusters = params.getInt("number-of-desired-clusters");;
            if (calculateMeans) {
                info.meansOrMedians = "K-Means";
            } else {
                info.meansOrMedians = "K-Medians";
            }
            //info.converged = result.getParams().getBoolean("converged");
            info.iterations = params.getInt("number-of-iterations");
            info.time = time;              
            info.numReps = params.getInt("number-of-repetitions");
            info.thresholdPercent = params.getFloat("threshold-percent");
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
    
    
    public AlgorithmData getScriptParameters(IFramework framework) {
        // the default values
        int numClusters = 10;
        int iterations = 50;
        data = framework.getData();
        
        KMCSupportDialog kmcsDialog = new KMCSupportDialog((JFrame) framework.getFrame(), true);
        kmcsDialog.setVisible(true);
        
        if (!kmcsDialog.isOkPressed()) return null;
        int numReps = 0;
        double thresholdPercent = 0.0;
        try {
            numClusters = kmcsDialog.getNumClusters();
            iterations = kmcsDialog.getIterations();
            numReps = kmcsDialog.getNumReps();
            thresholdPercent = kmcsDialog.getThresholdPercent();
            clusterGenes = kmcsDialog.isClusterGenes();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Invalid input parameters!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        boolean isHierarchicalTree = kmcsDialog.isDrawTrees();
        boolean calculateMeans = kmcsDialog.meansChosen();
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        if (isHierarchicalTree) {
            HCLInitDialog hcl_dialog = new HCLInitDialog(framework.getFrame());
            if (hcl_dialog.showModal() != JOptionPane.OK_OPTION) {
                return null;
            }
            hcl_method = hcl_dialog.getMethod();
            hcl_samples = hcl_dialog.isClusterExperience();
            hcl_genes = hcl_dialog.isClusterGenes();
        }
        this.experiment = framework.getData().getExperiment();
        
        int genes = experiment.getNumberOfGenes();
        
        AlgorithmData data = new AlgorithmData();
        data.addParam("kmc-cluster-genes", String.valueOf(clusterGenes));
        data.addParam("distance-factor", String.valueOf(1.0f));
        IDistanceMenu menu = framework.getDistanceMenu();
        data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.EUCLIDEAN;
        }
        data.addParam("distance-function", String.valueOf(function));
        data.addParam("number-of-desired-clusters", String.valueOf(numClusters));
        data.addParam("number-of-iterations", String.valueOf(iterations));
        data.addParam("number-of-repetitions", String.valueOf(numReps));
        data.addParam("threshold-percent", String.valueOf(thresholdPercent));
        data.addParam("calculate-means", String.valueOf(calculateMeans));
        // hcl parameters
        if (isHierarchicalTree) {
            data.addParam("hierarchical-tree", String.valueOf(true));
            data.addParam("method-linkage", String.valueOf(hcl_method));
            data.addParam("calculate-genes", String.valueOf(hcl_genes));
            data.addParam("calculate-experiments", String.valueOf(hcl_samples));
        }
        //script control parameters
        
        // alg name
        data.addParam("name", "KMS");
        
        // alg type
        if(clusterGenes)
            data.addParam("alg-type", "cluster-genes");
        else
            data.addParam("alg-type", "cluster-experiments");
        
        // output class
        data.addParam("output-class", "multi-cluster-output");
        
        //output nodes
        String [] outputNodes = new String[1];
        outputNodes[0] = "Multi-cluster";
        data.addStringArray("output-nodes", outputNodes);
        
        return data;
    }
    
    
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    private DefaultMutableTreeNode createResultTree(Cluster result_cluster, GeneralInfo info) {
        DefaultMutableTreeNode root;
        if(this.clusterGenes)
            root = new DefaultMutableTreeNode("KMS - genes");
        else
            root = new DefaultMutableTreeNode("KMS - experiments");
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
        addTableViews(root);
        addClusterInfo(root);
        addGeneralInfo(root, info);
    }
    
    private void addTableViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table views");
        IViewer tabViewer;
        if(clusterGenes)
            tabViewer = new ClusterTableViewer(this.experiment, this.clusters, this.data);
        else
            tabViewer = new ExperimentClusterTableViewer(this.experiment, this.clusters, this.data);
        //return; // placeholder for ExptClusterTableViewer
        //expViewer = new KMCSuppExperimentClusterViewer(this.experiment, this.clusters);
        
        if (!unassignedExists) {
            for (int i=0; i<this.clusters.length; i++) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), tabViewer, new Integer(i))));
            }
        } else {
            for (int i=0; i<(this.clusters.length - 1); i++) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), tabViewer, new Integer(i))));
            }
            //if(clusterGenes)
            node.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned ", tabViewer, new Integer(clusters.length - 1))));
            //else
            //node.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned experiments ", expViewer, new Integer(clusters.length - 1))));
            
        }
        root.add(node);
    }
    
    /**
     * Adds nodes to display clusters data.
     */
    private void addExpressionImages(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer;
        if(clusterGenes)
            expViewer = new KMCSuppExperimentViewer(this.experiment, this.clusters);
        else
            expViewer = new KMCSuppExperimentClusterViewer(this.experiment, this.clusters);
        
        if (!unassignedExists) {
            for (int i=0; i<this.clusters.length; i++) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expViewer, new Integer(i))));
            }
        } else {
            for (int i=0; i<(this.clusters.length - 1); i++) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expViewer, new Integer(i))));
            }
            if(clusterGenes)
                node.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned genes ", expViewer, new Integer(clusters.length - 1))));
            else
                node.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned experiments ", expViewer, new Integer(clusters.length - 1))));
            
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
        int [][] clusters = null;
        int i;
        
        if(this.clusterGenes){
            if (!unassignedExists) {
                for (i=0; i<nodeList.getSize(); i++) {
                    node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), createHCLViewer(nodeList.getNode(i), info, clusters))));
                }
            } else {
                for (i=0; i<(nodeList.getSize() - 1); i++) {
                    node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), createHCLViewer(nodeList.getNode(i), info, clusters))));
                }
                node.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned genes ", createHCLViewer(nodeList.getNode(nodeList.getSize() - 1), info, clusters))));
            }
        }
        //clusterExperiments
        else {
            clusters = new int[k][];
            for (i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            if(info.hcl_samples)
                clusters = getOrderedIndices(nodeList, clusters, info.hcl_genes);
            if(!unassignedExists){
                for (i=0; i<nodeList.getSize(); i++) {
                    node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), createHCLViewer(nodeList.getNode(i), info, clusters), new Integer(i))));
                }
            } else {
                for (i=0; i<(nodeList.getSize() - 1); i++) {
                    node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), createHCLViewer(nodeList.getNode(i), info, clusters), new Integer(i))));
                }
                node.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned experiments ", createHCLViewer(nodeList.getNode(nodeList.getSize() - 1), info, clusters), new Integer(i))));
            }
        }
        root.add(node);
    }
    
    /**
     * Creates an <code>HCLViewer</code>.
     */
    private IViewer createHCLViewer(Node clusterNode, GeneralInfo info, int [][] sampleClusters) {
        HCLTreeData genes_result = info.hcl_genes ? getResult(clusterNode, 0) : null;
        HCLTreeData samples_result = info.hcl_samples ? getResult(clusterNode, info.hcl_genes ? 4 : 0) : null;
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
        if(clusterGenes)
            node.add(new DefaultMutableTreeNode(new LeafInfo("Genes in Clusters (#,%)", new KMCSuppInfoViewer(this.clusters, this.experiment.getNumberOfGenes(), this.unassignedExists, this.clusterGenes))));
        else
            node.add(new DefaultMutableTreeNode(new LeafInfo("Experiments in Clusters (#,%)", new KMCSuppInfoViewer(this.clusters, this.experiment.getNumberOfSamples(), this.unassignedExists, this.clusterGenes))));
        root.add(node);
    }
    
    /**
     * Adds nodes to display centroid charts.
     */
    private void addCentroidViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        int i;
        KMCSuppCentroidViewer centroidViewer;
        KMCSuppExperimentCentroidViewer expCentroidViewer;
        
        if(clusterGenes){
            centroidViewer = new KMCSuppCentroidViewer(this.experiment, clusters);
            centroidViewer.setMeans(this.means.A);
            centroidViewer.setVariances(this.variances.A);
            if(!unassignedExists){
                for (i=0; i<this.clusters.length; i++) {
                    centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                    expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
                }
            } else {
                for (i=0; i<this.clusters.length-1; i++) {
                    centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                    expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
                }
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned genes ", centroidViewer, new CentroidUserObject(this.clusters.length - 1, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned genes ", centroidViewer, new CentroidUserObject(this.clusters.length - 1, CentroidUserObject.VALUES_MODE))));
            }
            
            
            KMCSuppCentroidsViewer centroidsViewer = new KMCSuppCentroidsViewer(this.experiment, clusters);
            centroidsViewer.setMeans(this.means.A);
            centroidsViewer.setVariances(this.variances.A);
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
            
        }
        else{
            expCentroidViewer = new KMCSuppExperimentCentroidViewer(this.experiment, clusters);
            
            expCentroidViewer.setMeans(this.means.A);
            expCentroidViewer.setVariances(this.variances.A);
            if(!unassignedExists){
                for (i=0; i<this.clusters.length; i++) {
                    centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                    expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
                }
            } else {
                for (i=0; i<this.clusters.length-1; i++) {
                    centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                    expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
                }
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned Experiments ", expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned Experiments ", expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
                
            }
            
            KMCSuppExperimentCentroidsViewer expCentroidsViewer = new KMCSuppExperimentCentroidsViewer(this.experiment, clusters);
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
        node.add(new DefaultMutableTreeNode("K-Means or K-Medians: "+info.meansOrMedians));
        node.add(new DefaultMutableTreeNode("Number of consensus clusters: "+String.valueOf(info.clusters)));
        //node.add(new DefaultMutableTreeNode("Converged: "+String.valueOf(info.converged)));
        node.add(new DefaultMutableTreeNode("Clusters per K-means / K-medians run: "+String.valueOf(info.userNumClusters)));
        node.add(new DefaultMutableTreeNode("Iterations per K-means / K-Medians run: "+String.valueOf(info.iterations)));
        node.add(new DefaultMutableTreeNode("Number of K-Means / K-Medians runs: "+String.valueOf(info.numReps)));
        node.add(new DefaultMutableTreeNode("Threshold co-occurrence %: "+String.valueOf(info.thresholdPercent)));
        node.add(new DefaultMutableTreeNode("HCL: "+info.getMethodName()));
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time)+" ms"));
        node.add(new DefaultMutableTreeNode(info.function));
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
        public String meansOrMedians;
        //public boolean converged;
        public int iterations;
        public int userNumClusters;
        public long time;
        public String function;
        public int numReps;
        public double thresholdPercent;
        
        private boolean hcl;
        private int hcl_method;
        private boolean hcl_genes;
        private boolean hcl_samples;
        
        public String getMethodName() {
            return hcl ? HCLGUI.GeneralInfo.getMethodName(hcl_method) : "no linkage";
        }
        
    }
    
}
























