/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: KMCGUI.java,v $
 * $Revision: 1.4 $
 * $Date: 2004-04-29 17:31:42 $
 * $Author: nbhagaba $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.kmc;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;

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
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidsViewer;

//import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;

public class KMCGUI implements IClusterGUI/*, IScriptGUI*/ {
    
    private Algorithm algorithm;
    private Progress progress;
    private Monitor monitor;
    
    private Experiment experiment;
    private IData data;
    private int[][] clusters;
    private FloatMatrix means;
    private FloatMatrix variances;
    private boolean clusterGenes;
    private int k;
    /**
     * Default constructor.
     */
    public KMCGUI() {
    }
    
    /**
     * Initialize the algorithm's parameters and execute it.
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        // the default values
        k = 10;
        int iterations = 50;
        boolean calcMeans = true;
        data = framework.getData();
        KMCInitDialog kmc_dialog = new KMCInitDialog(new JFrame(), k, iterations);
        if (kmc_dialog.showModal() != JOptionPane.OK_OPTION) {
            return null;
        }
        k = kmc_dialog.getClusters();
        iterations = kmc_dialog.getIterations();
        calcMeans = kmc_dialog.calculateMeans();
        clusterGenes = kmc_dialog.isClusterGenesSelected();
        
        if (k < 1) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Number of clusters must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (iterations < 1) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Number of iterations must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        boolean isHierarchicalTree = kmc_dialog.isHierarchicalTree();
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        if (isHierarchicalTree) {
            HCLInitDialog hcl_dialog = new HCLInitDialog(new JFrame());
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
            algorithm = framework.getAlgorithmFactory().getAlgorithm("KMC");
            algorithm.addAlgorithmListener(listener);
            
            int genes = experiment.getNumberOfGenes();
            this.monitor = new Monitor(framework.getFrame(), "Reallocations", 25, 100, 210.0/genes);
            this.monitor.setStepXFactor((int)Math.floor(245/iterations));
            this.monitor.update(genes);
            this.monitor.show();
            
            this.progress = new Progress(framework.getFrame(), "Calculating clusters", listener);
            this.progress.show();
            
            AlgorithmData data = new AlgorithmData();
            if(clusterGenes){
                data.addMatrix("experiment", experiment.getMatrix());
                data.addParam("kmc-cluster-genes", String.valueOf(true));
            }
            else{
                data.addMatrix("experiment", experiment.getMatrix().transpose());
                data.addParam("kmc-cluster-genes", String.valueOf(false));
            }
            data.addParam("distance-factor", String.valueOf(1.0f));
            IDistanceMenu menu = framework.getDistanceMenu();
            data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
            int function = menu.getDistanceFunction();
            if (function == Algorithm.DEFAULT) {
                function = Algorithm.EUCLIDEAN;
            }
            data.addParam("distance-function", String.valueOf(function));
            data.addParam("number-of-clusters", String.valueOf(k));
            data.addParam("number-of-iterations", String.valueOf(iterations));
            data.addParam("calculate-means", String.valueOf(calcMeans));
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
            this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            
            GeneralInfo info = new GeneralInfo();
            info.clusters = k;
            info.converged = result.getParams().getBoolean("converged");
            info.iterations = result.getParams().getInt("iterations");
            info.calculate_means = calcMeans;
            info.time = time;
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
            if (monitor != null) {
                monitor.dispose();
            }
        }
    }
    
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    private DefaultMutableTreeNode createResultTree(Cluster result_cluster, GeneralInfo info) {
        DefaultMutableTreeNode root;
        if(clusterGenes)
            root = new DefaultMutableTreeNode("KMC - genes");
        else
            root = new DefaultMutableTreeNode("KMC - experiments");
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
        addClusterTableViews(root);
        addClusterInfo(root);
        addGeneralInfo(root, info);
    }
    
    
    /**
     * Adds nodes to display clusters data.
     */
    private void addExpressionImages(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer;
        if(clusterGenes)
            expViewer = new KMCExperimentViewer(this.experiment, this.clusters);
        else
            expViewer = new KMCExperimentClusterViewer(this.experiment, this.clusters);
        
        for (int i=0; i<this.clusters.length; i++) {
            node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expViewer, new Integer(i))));
        }
        root.add(node);
    }
    
    private void addClusterTableViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table views"); 
        IViewer tableViewer;
        if (clusterGenes) {
            tableViewer = new ClusterTableViewer(this.experiment, this.clusters, data);
        } else {// placeholder for ExperimentClusterTableViewer
            tableViewer = new ExperimentClusterTableViewer(this.experiment, this.clusters, data);
            //return;
        }
        
        for (int i=0; i<this.clusters.length; i++) {
            node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), tableViewer, new Integer(i))));
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
        
        if(!this.clusterGenes){
            clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            if(info.hcl_samples)
                clusters = getOrderedIndices(nodeList, clusters, info.hcl_genes);
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
     * Adds node with cluster information.
     */
    private void addClusterInfo(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Cluster Information");
        if(this.clusterGenes)
            node.add(new DefaultMutableTreeNode(new LeafInfo("Genes in Clusters (#,%)", new KMCInfoViewer(this.clusters, this.experiment.getNumberOfGenes()))));
        else
            node.add(new DefaultMutableTreeNode(new LeafInfo("Experiments in Clusters (#,%)", new KMCInfoViewer(this.clusters, this.experiment.getNumberOfSamples(), false))));
        root.add(node);
    }
    
    
    /**
     * Adds nodes to display centroid charts.
     */
    private void addCentroidViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        
        KMCCentroidViewer centroidViewer;
        ExperimentClusterCentroidViewer expCentroidViewer;
        if(clusterGenes){
            centroidViewer = new KMCCentroidViewer(this.experiment, clusters);
            centroidViewer.setMeans(this.means.A);
            centroidViewer.setVariances(this.variances.A);
            for (int i=0; i<this.clusters.length; i++) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            
            KMCCentroidsViewer centroidsViewer = new KMCCentroidsViewer(this.experiment, clusters);
            centroidsViewer.setMeans(this.means.A);
            centroidsViewer.setVariances(this.variances.A);
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
            
        }
        else{
            expCentroidViewer = new KMCExperimentCentroidViewer(this.experiment, clusters);
            
            expCentroidViewer.setMeans(this.means.A);
            expCentroidViewer.setVariances(this.variances.A);
            for (int i=0; i<this.clusters.length; i++) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            KMCExperimentCentroidsViewer expCentroidsViewer = new KMCExperimentCentroidsViewer(this.experiment, clusters);
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
        if(info.calculate_means)
            node.add(new DefaultMutableTreeNode("KMC mode: Calculated Means"));
        else
            node.add(new DefaultMutableTreeNode("KMC mode: Calculated Medians"));
        node.add(new DefaultMutableTreeNode("Clusters: "+String.valueOf(info.clusters)));
        node.add(new DefaultMutableTreeNode("Converged: "+String.valueOf(info.converged)));
        node.add(new DefaultMutableTreeNode("Iterations: "+String.valueOf(info.iterations)));
        node.add(new DefaultMutableTreeNode("HCL: "+info.getMethodName()));
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time)+" ms"));
        node.add(new DefaultMutableTreeNode(info.function));
        root.add(node);
    }
    
    /***
     *      Script Support
     *
     */                  
    public AlgorithmData getScriptParameters(IFramework framework) {
        k = 10;
        int iterations = 50;
        boolean calcMeans = true;        
        
        KMCInitDialog kmc_dialog = new KMCInitDialog(new JFrame(), k, iterations);
        if (kmc_dialog.showModal() != JOptionPane.OK_OPTION) {
            return null;
        }
        k = kmc_dialog.getClusters();
        iterations = kmc_dialog.getIterations();
        calcMeans = kmc_dialog.calculateMeans();
        clusterGenes = kmc_dialog.isClusterGenesSelected();
        
        if (k < 1) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Number of clusters must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (iterations < 1) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Number of iterations must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        boolean isHierarchicalTree = kmc_dialog.isHierarchicalTree();
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        if (isHierarchicalTree) {
            HCLInitDialog hcl_dialog = new HCLInitDialog(new JFrame());
            if (hcl_dialog.showModal() != JOptionPane.OK_OPTION) {
                return null;
            }
            hcl_method = hcl_dialog.getMethod();
            hcl_samples = hcl_dialog.isClusterExperience();
            hcl_genes = hcl_dialog.isClusterGenes();
        }
        
        this.experiment = framework.getData().getExperiment();
        Listener listener = new Listener();
   
        AlgorithmData data = new AlgorithmData();
        if(clusterGenes){
            data.addParam("kmc-cluster-genes", String.valueOf(true));
        }
        else{
            data.addParam("kmc-cluster-genes", String.valueOf(false));
        }
        data.addParam("distance-factor", String.valueOf(1.0f));
        IDistanceMenu menu = framework.getDistanceMenu();
        data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.EUCLIDEAN;
        }
        data.addParam("distance-function", String.valueOf(function));
        data.addParam("number-of-clusters", String.valueOf(k));
        data.addParam("number-of-iterations", String.valueOf(iterations));
        data.addParam("calculate-means", String.valueOf(calcMeans));
        // hcl parameters
        if (isHierarchicalTree) {
            data.addParam("hierarchical-tree", String.valueOf(true));
            data.addParam("method-linkage", String.valueOf(hcl_method));
            data.addParam("calculate-genes", String.valueOf(hcl_genes));
            data.addParam("calculate-experiments", String.valueOf(hcl_samples));
        }
        
        //script control parameters

        // alg name
        data.addParam("name", "KMC");
        
        // alg type
        data.addParam("alg-type", "cluster");
        
        // output class
        data.addParam("output-class", "multi-cluster-output");
        
        //output nodes                
        String [] outputNodes = new String[1];
        outputNodes[0] = "Multi-cluster";
        data.addStringArray("output-nodes", outputNodes);
        
        
        return data;
    }
    
    public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, Experiment experiment) throws AlgorithmException {
        AlgorithmData data = algData;
        
            //extract some key paramters
            AlgorithmParameters params = data.getParams();
            k = params.getInt("number-of-clusters", 10);
            this.clusterGenes = params.getBoolean("kmc-cluster-genes");
            int iterations = params.getInt("number-of-iterations");
            
            if(clusterGenes){
                data.addMatrix("experiment", experiment.getMatrix());

            }
            else{
                data.addMatrix("experiment", experiment.getMatrix().transpose());

            }
        
        this.experiment = experiment;
        Listener listener = new Listener();
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("KMC");
            algorithm.addAlgorithmListener(listener);
            
            int genes = experiment.getNumberOfGenes();
            this.monitor = new Monitor(framework.getFrame(), "Reallocations", 25, 100, 210.0/genes);
            this.monitor.setStepXFactor((int)Math.floor(245/iterations));
            this.monitor.update(genes);
            this.monitor.show();
            
            this.progress = new Progress(framework.getFrame(), "Calculating clusters", listener);
            this.progress.show();
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(data);
            long time = System.currentTimeMillis() - start;
            // getting the results
            Cluster result_cluster = result.getCluster("cluster"); 
            NodeList nodeList = result_cluster.getNodeList();
            this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            
            GeneralInfo info = new GeneralInfo();
            info.clusters = k;
            info.converged = result.getParams().getBoolean("converged");
            info.iterations = result.getParams().getInt("iterations");
            info.calculate_means = params.getBoolean("calculate-means");
            info.time = time;
            info.function = framework.getDistanceMenu().getFunctionName(params.getInt("distance-function"));
            info.hcl = params.getBoolean("hierarchical-tree");

            info.hcl_genes = params.getBoolean("calculate-genes");
            info.hcl_samples = params.getBoolean("calculate-experiments");
            if(info.hcl)
                    info.hcl_method = params.getInt("method-linkage");
            else
                info.hcl_method = 0;
            
            return createResultTree(result_cluster, info);
            
        } finally {
            if (algorithm != null) {
                algorithm.removeAlgorithmListener(listener);
            }
            if (progress != null) {
                progress.dispose();
            }
            if (monitor != null) {
                monitor.dispose();
            }
        }
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
                        monitor.dispose();
                    } else {
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
        public int clusters;
        public boolean converged;
        public int iterations;
        public long time;
        public String function;
        public boolean calculate_means;
        
        private boolean hcl;
        private int hcl_method;
        private boolean hcl_genes;
        private boolean hcl_samples;
        
        public String getMethodName() {
            return hcl ? HCLGUI.GeneralInfo.getMethodName(hcl_method) : "no linkage";
        }
        
    }
}
