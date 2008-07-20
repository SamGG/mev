/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SOMGUI.java,v $
 * $Revision: 1.9 $
 * $Date: 2006-02-23 20:59:54 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.som;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValueList;
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
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLGUI;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLViewer;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;
import org.tigr.util.FloatMatrix;


public class SOMGUI implements IClusterGUI, IScriptGUI {
    
    private Experiment experiment;
    private IData data;
    private Algorithm algorithm;
    private Progress progress;
    
    private int[][] clusters;
    private FloatMatrix codes;
    private FloatMatrix u_matrix;
    private FloatMatrix means;
    private FloatMatrix variances;
    private boolean clusterGenes;
    
    /**
     * Initialize the algorithm's parameters and execute it.
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {

        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.EUCLIDEAN;
        } 
            
        this.data = framework.getData();
        SOMInitDialog som_dialog = new SOMInitDialog(framework.getFrame(), 3, 3, 2000, 0.05f, 3f, 1, 1, 0, menu.getFunctionName(function), menu.isAbsoluteDistance());
        if (som_dialog.showModal() != JOptionPane.OK_OPTION) {
            return null;
        }
        clusterGenes = som_dialog.isClusterGenes();
        boolean isHierarchicalTree = som_dialog.isHierarchicalTree();
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        int hcl_metric = 4;
        boolean hcl_absolute = false;
        if (isHierarchicalTree) {
                        
            HCLInitDialog hcl_dialog = new HCLInitDialog(framework.getFrame(), menu.getFunctionName(som_dialog.getDistanceMetric()), som_dialog.isAbsoluteDistance(), true);
            if (hcl_dialog.showModal() != JOptionPane.OK_OPTION) {
                return null;
            }
            hcl_method = hcl_dialog.getMethod();
            hcl_samples = hcl_dialog.isClusterExperiments();
            hcl_genes = hcl_dialog.isClusterGenes();
            hcl_metric = hcl_dialog.getDistanceMetric();
            hcl_absolute = hcl_dialog.getAbsoluteSelection();
        }
        
        this.experiment = framework.getData().getExperiment();
        GeneralInfo info = new GeneralInfo();
        info.alpha = som_dialog.getAlpha();
        info.radius = som_dialog.getRadius();
        if(clusterGenes)
            info.dimension   = this.experiment.getNumberOfSamples();
        else
            info.dimension = this.experiment.getNumberOfGenes();
        info.dimension_x = som_dialog.getDimensionX();
        info.dimension_y = som_dialog.getDimensionY();
        info.clusters    = info.dimension_x * info.dimension_y;
        info.iterations  = som_dialog.getIterations();
        info.neiborhood  = som_dialog.getNeighborhood() == 0 ? "bubble" : "gaussian";
        info.topology    = som_dialog.getTopology() == 0 ? "hexagonal" : "rectangular";
        info.init_type   = som_dialog.getInitType() == 0 ? "vector" : "genes";
        info.hcl = isHierarchicalTree;
        info.hcl_genes = hcl_genes;
        info.hcl_samples = hcl_samples;
        info.hcl_method = hcl_method;
        
        Listener listener = new Listener();
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("SOM");
            algorithm.addAlgorithmListener(listener);
            
            this.progress = new Progress(framework.getFrame(), "SOM Training", listener);
            this.progress.show();
            
            AlgorithmData data = new AlgorithmData();
            FloatMatrix matrix = framework.getData().getExperiment().getMatrix();
            if(!clusterGenes)
                matrix = matrix.transpose();
            data.addMatrix("experiment", matrix);
            data.addParam("distance-factor", String.valueOf(1f));
            data.addParam("distance-absolute", String.valueOf(som_dialog.isAbsoluteDistance()));
            
            function = som_dialog.getDistanceMetric();
            info.function = menu.getFunctionName(function);
            data.addParam("distance-function", String.valueOf(function));
            data.addParam("dimension-x", String.valueOf(info.dimension_x));
            data.addParam("dimension-y", String.valueOf(info.dimension_y));
            data.addParam("iterations", String.valueOf(info.iterations));
            data.addParam("topology", info.topology);
            data.addParam("is_neighborhood_bubble", String.valueOf(info.neiborhood.equals("bubble")));
            data.addParam("is_random_vector", String.valueOf(info.init_type.equals("vector")));
            data.addParam("radius", String.valueOf(info.radius));
            data.addParam("alpha", String.valueOf(info.alpha));
            data.addParam("som-cluster-genes", String.valueOf(clusterGenes));
            // hcl parameters
            if (isHierarchicalTree) {
                data.addParam("hierarchical-tree", String.valueOf(true));
                data.addParam("method-linkage", String.valueOf(hcl_method));
                data.addParam("calculate-genes", String.valueOf(hcl_genes));
                data.addParam("calculate-experiments", String.valueOf(hcl_samples));     
                data.addParam("hcl-distance-function", String.valueOf(hcl_metric));
                data.addParam("hcl-distance-absolute", String.valueOf(hcl_absolute));
            }
            
            long startTime = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(data);
            info.time = System.currentTimeMillis()-startTime;
            // obtain the clusters
            Cluster result_cluster = result.getCluster("cluster");
            NodeList nodeList = result_cluster.getNodeList();
            this.clusters = new int[nodeList.getSize()][];
            for (int i=0; i<this.clusters.length; i++) {
                this.clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            // obtain the codes
            this.codes = result.getMatrix("codes");
            // obtain the u-matrix
            this.u_matrix = result.getMatrix("u_matrix");
            // means, variances
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
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
    
    
    /**
     *  Script Support
     */
    
    public AlgorithmData getScriptParameters(IFramework framework) {
        
        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.EUCLIDEAN;
        } 
            
        this.data = framework.getData();
        SOMInitDialog som_dialog = new SOMInitDialog(framework.getFrame(), 3, 3, 2000, 0.05f, 3f, 1, 1, 0, menu.getFunctionName(function), menu.isAbsoluteDistance());
        if (som_dialog.showModal() != JOptionPane.OK_OPTION) {
            return null;
        }
        clusterGenes = som_dialog.isClusterGenes();
        boolean isHierarchicalTree = som_dialog.isHierarchicalTree();
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        int hcl_metric = 4;
        boolean hcl_absolute = false;        
        if (isHierarchicalTree) {
            HCLInitDialog hcl_dialog = new HCLInitDialog(framework.getFrame(), menu.getFunctionName(som_dialog.getDistanceMetric()), som_dialog.isAbsoluteDistance(), true);
            if (hcl_dialog.showModal() != JOptionPane.OK_OPTION) {
                return null;
            }
            hcl_method = hcl_dialog.getMethod();
            hcl_samples = hcl_dialog.isClusterExperiments();
            hcl_genes = hcl_dialog.isClusterGenes();
            hcl_metric = hcl_dialog.getDistanceMetric();
            hcl_absolute = hcl_dialog.getAbsoluteSelection();
        }
        
        this.experiment = framework.getData().getExperiment();
        GeneralInfo info = new GeneralInfo();
        info.alpha = som_dialog.getAlpha();
        info.radius = som_dialog.getRadius();
        if(clusterGenes)
            info.dimension   = this.experiment.getNumberOfSamples();
        else
            info.dimension = this.experiment.getNumberOfGenes();
        info.dimension_x = som_dialog.getDimensionX();
        info.dimension_y = som_dialog.getDimensionY();
        info.clusters    = info.dimension_x * info.dimension_y;
        info.iterations  = som_dialog.getIterations();
        info.neiborhood  = som_dialog.getNeighborhood() == 0 ? "bubble" : "gaussian";
        info.topology    = som_dialog.getTopology() == 0 ? "hexagonal" : "rectangular";
        info.init_type   = som_dialog.getInitType() == 0 ? "vector" : "genes";
        info.hcl = isHierarchicalTree;
        info.hcl_genes = hcl_genes;
        info.hcl_samples = hcl_samples;
        info.hcl_method = hcl_method;
        
        Listener listener = new Listener();
        
        AlgorithmData data = new AlgorithmData();
        FloatMatrix matrix = framework.getData().getExperiment().getMatrix();
        
        data.addParam("distance-factor", String.valueOf(1f));

        data.addParam("distance-absolute", String.valueOf(som_dialog.isAbsoluteDistance()));

        function = som_dialog.getDistanceMetric();
        info.function = menu.getFunctionName(function);
        data.addParam("distance-function", String.valueOf(function));        

        data.addParam("dimension-x", String.valueOf(info.dimension_x));
        data.addParam("dimension-y", String.valueOf(info.dimension_y));
        data.addParam("iterations", String.valueOf(info.iterations));
        data.addParam("topology", info.topology);
        data.addParam("is_neighborhood_bubble", String.valueOf(info.neiborhood.equals("bubble")));
        data.addParam("is_random_vector", String.valueOf(info.init_type.equals("vector")));
        data.addParam("radius", String.valueOf(info.radius));
        data.addParam("alpha", String.valueOf(info.alpha));
        data.addParam("som-cluster-genes", String.valueOf(clusterGenes));
        // hcl parameters
        if (isHierarchicalTree) {
            data.addParam("hierarchical-tree", String.valueOf(true));
            data.addParam("method-linkage", String.valueOf(hcl_method));
            data.addParam("calculate-genes", String.valueOf(hcl_genes));
            data.addParam("calculate-experiments", String.valueOf(hcl_samples));
            data.addParam("hcl-distance-function", String.valueOf(hcl_metric));
            data.addParam("hcl-distance-absolute", String.valueOf(hcl_absolute));
        }
        
        //script control parameters
        
        // alg name
        data.addParam("name", "SOM");
        
        // alg type
        if(clusterGenes)
            data.addParam("alg-type", "cluster-genes");
        else
            data.addParam("alg-type", "cluster-experiments");
        
        // output class
        if(clusterGenes)
            data.addParam("output-class", "multi-gene-cluster-output");
        else
            data.addParam("output-class", "multi-experiment-cluster-output");
        
        //output nodes
        String [] outputNodes = new String[1];
        outputNodes[0] = "Multi-cluster";
        data.addStringArray("output-nodes", outputNodes);
        
        return data;
    }
    
    public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, Experiment experiment) throws AlgorithmException {
        this.data = framework.getData();
        this.experiment = experiment;
        Listener listener = new Listener();
        this.clusterGenes = algData.getParams().getBoolean("som-cluster-genes");
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("SOM");
            algorithm.addAlgorithmListener(listener);
            
            this.progress = new Progress(framework.getFrame(), "SOM Training", listener);
            this.progress.show();
            
            FloatMatrix matrix = framework.getData().getExperiment().getMatrix();
            if(!clusterGenes)
                matrix = matrix.transpose();
            algData.addMatrix("experiment", matrix);
            
            long startTime = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(algData);
            GeneralInfo info = new GeneralInfo();
            info.time = System.currentTimeMillis()-startTime;
            // obtain the clusters
            Cluster result_cluster = result.getCluster("cluster");
            NodeList nodeList = result_cluster.getNodeList();
            this.clusters = new int[nodeList.getSize()][];
            for (int i=0; i<this.clusters.length; i++) {
                this.clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            // obtain the codes
            this.codes = result.getMatrix("codes");
            // obtain the u-matrix
            this.u_matrix = result.getMatrix("u_matrix");
            // means, variances
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            
            
            IDistanceMenu menu = framework.getDistanceMenu();
            
            AlgorithmParameters params = algData.getParams();
            
            info.alpha = params.getFloat("alpha");
            info.radius = params.getFloat("radius");
            if(clusterGenes)
                info.dimension   = this.experiment.getNumberOfSamples();
            else
                info.dimension = this.experiment.getNumberOfGenes();
            info.dimension_x = params.getInt("dimension-x");
            info.dimension_y = params.getInt("dimension-y");
            info.clusters    = info.dimension_x * info.dimension_y;
            info.iterations  = params.getInt("iterations");
            info.neiborhood  = params.getBoolean("is-neighborhood-bubble") ? "bubble" : "gaussian";
            info.topology    = params.getString("topology");
            info.init_type   = params.getBoolean("is-random-vector") ? "vector" : "genes";
            info.hcl = params.getBoolean("hierarchical-tree");
            info.hcl_genes = params.getBoolean("calculate-genes");
            info.hcl_samples = params.getBoolean("calculate-experiments");
            if(info.hcl_genes)
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
    
    
    /**
     * Creates the result tree.
     */
    private DefaultMutableTreeNode createResultTree(Cluster result_cluster, GeneralInfo info) {
        DefaultMutableTreeNode root;
        if(this.clusterGenes)
            root = new DefaultMutableTreeNode("SOM - genes");
        else
            root = new DefaultMutableTreeNode("SOM - samples");
        addResultNodes(root, result_cluster, info);
        return root;
    }
    
    /**
     * Adds nodes into the result tree root node.
     */
    private void addResultNodes(DefaultMutableTreeNode root, Cluster result_cluster, GeneralInfo info) {
        addExpressionImages(root, info);
        addHierarchicalTrees(root, result_cluster, info);
        addCentroidViews(root, info);
        addSOMViews(root, info);
        addTableViews(root, info);
        addClusterInfo(root);
        addGeneralInfo(root, info);
    }
    
    private void addTableViews(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table views");
        //SOMExperimentViewer expViewer;
        IViewer tabViewer;
        if(clusterGenes)
            tabViewer = new ClusterTableViewer(this.experiment, this.clusters, this.data);
        else
            tabViewer = new ExperimentClusterTableViewer(this.experiment, this.clusters, this.data);
        //return; //placeholder for ExptClusterTableViewer
        //expViewer = new SOMExperimentClusterViewer(this.experiment, this.clusters, "SOM Vector", this.codes);
        int cluster;
        for (int x=0; x<info.dimension_x; x++) {
            for (int y=0; y<info.dimension_y; y++) {
                cluster = x*info.dimension_y+y;
                node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(cluster+1)+" ("+String.valueOf(x+1)+","+String.valueOf(y+1)+")", tabViewer, new Integer(cluster))));
            }
        }
        root.add(node);
    }
    
    /**
     * Adds experiment viewer nodes.
     */
    private void addExpressionImages(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        //SOMExperimentViewer expViewer;
        IViewer expViewer;
        if(clusterGenes)
            expViewer = new SOMExperimentViewer(this.experiment, this.clusters, this.codes);
        else
            expViewer = new SOMExperimentClusterViewer(this.experiment, this.clusters, "SOM Vector", this.codes);
        int cluster;
        for (int x=0; x<info.dimension_x; x++) {
            for (int y=0; y<info.dimension_y; y++) {
                cluster = x*info.dimension_y+y;
                node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(cluster+1)+" ("+String.valueOf(x+1)+","+String.valueOf(y+1)+")", expViewer, new Integer(cluster))));
            }
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
        int [][] hclClusters = this.clusters;
        int x, y;
        int k = nodeList.getSize();
        
        if(!this.clusterGenes){
            hclClusters = new int[k][];
            for (int i=0; i<k; i++) {
                hclClusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            if(info.hcl_samples)
                hclClusters = getOrderedIndices(nodeList, hclClusters, info.hcl_genes);
        }
        for (int i=0; i<nodeList.getSize(); i++) {
            x = i/info.dimension_y;
            y = i%info.dimension_y;
            if(clusterGenes)
                node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1)+" ("+String.valueOf(x+1)+","+String.valueOf(y+1)+")", createHCLViewer(nodeList.getNode(i), info, hclClusters))));
            else
                node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1)+" ("+String.valueOf(x+1)+","+String.valueOf(y+1)+")", createHCLViewer(nodeList.getNode(i), info, hclClusters), new Integer(i))));
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
        if(this.clusterGenes)
            node.add(new DefaultMutableTreeNode(new LeafInfo("Genes in Clusters (#,%)", new SOMInfoViewer(this.clusters, this.experiment.getNumberOfGenes()))));
        else
            node.add(new DefaultMutableTreeNode(new LeafInfo("Samples in Clusters (#,%)", new SOMInfoViewer(this.clusters, this.experiment.getNumberOfSamples(), false))));
        root.add(node);
    }
    
    /**
     * Adds centroid and expression viewers nodes.
     */
    private void addCentroidViews(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        SOMCentroidViewer centroidViewer;
        SOMExperimentCentroidViewer expCentroidViewer;
        
        if(clusterGenes){
            centroidViewer = new SOMCentroidViewer(this.experiment, this.clusters);
            centroidViewer.setMeans(this.means.A);
            centroidViewer.setVariances(this.variances.A);
            centroidViewer.setCodes(this.codes.A);
            int cluster;
            for (int x=0; x<info.dimension_x; x++) {
                for (int y=0; y<info.dimension_y; y++) {
                    cluster = x*info.dimension_y+y;
                    centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(cluster+1)+" ("+String.valueOf(x+1)+","+String.valueOf(y+1)+")", centroidViewer, new CentroidUserObject(cluster, CentroidUserObject.VARIANCES_MODE))));
                    expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(cluster+1)+" ("+String.valueOf(x+1)+","+String.valueOf(y+1)+")", centroidViewer, new CentroidUserObject(cluster, CentroidUserObject.VALUES_MODE))));
                }
            }
            SOMCentroidsViewer centroidsViewer = new SOMCentroidsViewer(this.experiment, clusters);
            centroidsViewer.setMeans(this.means.A);
            centroidsViewer.setVariances(this.variances.A);
            centroidsViewer.setCodes(this.codes.A);
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        }
        else{
            expCentroidViewer = new SOMExperimentCentroidViewer(this.experiment, this.clusters);
            expCentroidViewer.setMeans(this.means.A);
            expCentroidViewer.setVariances(this.variances.A);
            expCentroidViewer.setCodes(this.codes.A);
            int cluster;
            for (int x=0; x<info.dimension_x; x++) {
                for (int y=0; y<info.dimension_y; y++) {
                    cluster = x*info.dimension_y+y;
                    centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(cluster+1)+" ("+String.valueOf(x+1)+","+String.valueOf(y+1)+")", expCentroidViewer, new CentroidUserObject(cluster, CentroidUserObject.VARIANCES_MODE))));
                    expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(cluster+1)+" ("+String.valueOf(x+1)+","+String.valueOf(y+1)+")", expCentroidViewer, new CentroidUserObject(cluster, CentroidUserObject.VALUES_MODE))));
                }
            }
            SOMExperimentCentroidsViewer centroidsViewer = new SOMExperimentCentroidsViewer(this.experiment, clusters);
            centroidsViewer.setMeans(this.means.A);
            centroidsViewer.setVariances(this.variances.A);
            centroidsViewer.setCodes(this.codes.A);
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        }
        root.add(centroidNode);
        root.add(expressionNode);
    }
    
    
    
    /**
     * Adds som visualization nodes.
     */
    private void addSOMViews(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode somNode = new DefaultMutableTreeNode("SOM Visualization");
        somNode.add(new DefaultMutableTreeNode(new LeafInfo("U-Matrix Color", new UMatrixColorViewer(this.clusters, this.u_matrix, info.dimension_x, info.dimension_y, info.topology))));
        somNode.add(new DefaultMutableTreeNode(new LeafInfo("U-Matrix Distance", new UMatrixDistanceViewer(this.clusters, this.u_matrix, info.dimension_x, info.dimension_y, info.topology))));
        root.add(somNode);
    }
    
    /**
     * Adds the general info node.
     */
    private void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        node.add(new DefaultMutableTreeNode("clusters: "+String.valueOf(info.clusters)));
        node.add(new DefaultMutableTreeNode("iterations: "+String.valueOf(info.iterations)));
        node.add(new DefaultMutableTreeNode("dimension: "+String.valueOf(info.dimension)));
        node.add(new DefaultMutableTreeNode("dimension x: "+String.valueOf(info.dimension_x)));
        node.add(new DefaultMutableTreeNode("dimension y: "+String.valueOf(info.dimension_y)));
        node.add(new DefaultMutableTreeNode("topology: "+info.topology));
        node.add(new DefaultMutableTreeNode("neiborhood: "+info.neiborhood));
        node.add(new DefaultMutableTreeNode("init type: "+info.init_type));
        node.add(new DefaultMutableTreeNode("alpha: "+String.valueOf(info.alpha)));
        node.add(new DefaultMutableTreeNode("radius: "+String.valueOf(info.radius)));
        node.add(new DefaultMutableTreeNode("HCL: "+info.getMethodName()));
        node.add(new DefaultMutableTreeNode("time: "+String.valueOf(info.time)+" ms"));
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
     * The class to listen to a dialog and algorithm events.
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
            }
        }
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("cancel-command")) {
                algorithm.abort();
                progress.dispose();
            }
        }
        
        public void windowClosing(WindowEvent e) {
            algorithm.abort();
            progress.dispose();
        }
    }
    
    /**
     * The general info structure.
     */
    private class GeneralInfo {
        public int clusters;
        public long iterations;
        public int dimension;
        public int dimension_x;
        public int dimension_y;
        public String topology;
        public String neiborhood;
        public String init_type;
        public float alpha;
        public float radius;
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
}
