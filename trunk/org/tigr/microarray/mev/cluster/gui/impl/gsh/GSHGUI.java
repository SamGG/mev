/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: GSHGUI.java,v $
 * $Revision: 1.7 $
 * $Date: 2005-02-24 20:24:05 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.gsh;

import java.util.Arrays;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

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
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterTableViewer;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLInitDialog;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLViewer;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLGUI;

import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;

public class GSHGUI implements IClusterGUI, IScriptGUI {
    
    private Algorithm algorithm;
    private IData data;
    private Experiment experiment;
    private int[][] clusters;
    private FloatMatrix means;
    private FloatMatrix variances;
    private boolean clusterGenes;
    
    /**
     * Default constructor.
     */
    public GSHGUI() {
    }
    
    /**
     * Initialize the algorithm's parameters and execute it.
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        this.data = framework.getData();
         
        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();        
        if (function == Algorithm.DEFAULT) {        
            function = Algorithm.EUCLIDEAN;            
        }
        
        // the default values
        int k = 10;
        int f = 20;
        int s = 5;
        
        GSHInitDialog gsh_dialog = new GSHInitDialog(framework.getFrame(), k, f, s);
        if (gsh_dialog.showModal() != JOptionPane.OK_OPTION) {
            return null;
        }
        k = gsh_dialog.getClusters();
        f = gsh_dialog.getFM();
        s = gsh_dialog.getST();
        clusterGenes = gsh_dialog.isClusterGenesSelected();
        if (k < 1) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Number of clusters must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (f < 1) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Number of faked matrix must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (s < 1) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Number of swap must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        boolean isHierarchicalTree = gsh_dialog.isHierarchicalTree();
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        int hcl_function = 4;
        boolean hcl_absolute = false;
        if (isHierarchicalTree) {
            
            HCLInitDialog hcl_dialog = new HCLInitDialog(framework.getFrame(), menu.getFunctionName(menu.getDistanceFunction()), menu.isAbsoluteDistance(), true);
            if (hcl_dialog.showModal() != JOptionPane.OK_OPTION) {
                return null;
            }
            hcl_method = hcl_dialog.getMethod();
            hcl_samples = hcl_dialog.isClusterExperiments();
            hcl_genes = hcl_dialog.isClusterGenes();
            hcl_function = hcl_dialog.getDistanceMetric();
            hcl_absolute = hcl_dialog.getAbsoluteSelection();
        }
        
        this.experiment = framework.getData().getExperiment();
        Listener listener = new Listener();
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("GSH");
            algorithm.addAlgorithmListener(listener);
            
            int genes = experiment.getNumberOfGenes();
            
            AlgorithmData data = new AlgorithmData();
            FloatMatrix matrix = experiment.getMatrix();
            if(!clusterGenes)
                matrix = matrix.transpose();
            
            data.addMatrix("experiment", matrix);
            data.addParam("distance-factor", String.valueOf(1.0f));

            data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
            data.addParam("gsh-cluster-genes", String.valueOf(this.clusterGenes));

            data.addParam("distance-function", String.valueOf(function));
            data.addParam("number-of-clusters", String.valueOf(k));
            data.addParam("number-of-fakedMatrix", String.valueOf(f));
            data.addParam("number-of-swap", String.valueOf(s));
            // hcl parameters
            if (isHierarchicalTree) {
                data.addParam("hierarchical-tree", String.valueOf(true));
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
            
            AlgorithmParameters resultMap = result.getParams();
            k = resultMap.getInt("number-of-clusters"); // NEED THIS TO GET THE VALUE OF NUMBER-OF-CLUSTERS
            
            this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            this.means = result.getMatrix("clusters-means");
            this.variances = result.getMatrix("clusters-variances");
            
            GeneralInfo info = new GeneralInfo();
            info.clusters = k;
            info.time = time;
            info.FM = f;
            info.ST = s;
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
        }
    }
    
    
    
    public AlgorithmData getScriptParameters(IFramework framework) {
        this.data = framework.getData();
        // the default values
        int k = 10;
        int f = 20;
        int s = 5;

        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();        
        if (function == Algorithm.DEFAULT) {        
            function = Algorithm.EUCLIDEAN;            
        }        
                
        GSHInitDialog gsh_dialog = new GSHInitDialog(framework.getFrame(), k, f, s);
        if (gsh_dialog.showModal() != JOptionPane.OK_OPTION) {
            return null;
        }
        k = gsh_dialog.getClusters();
        f = gsh_dialog.getFM();
        s = gsh_dialog.getST();
        clusterGenes = gsh_dialog.isClusterGenesSelected();
        if (k < 1) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Number of clusters must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (f < 1) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Number of faked matrix must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (s < 1) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Number of swap must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        boolean isHierarchicalTree = gsh_dialog.isHierarchicalTree();
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        int hcl_function = 4;
        boolean hcl_absolute = false;
        if (isHierarchicalTree) {
            
            HCLInitDialog hcl_dialog = new HCLInitDialog(framework.getFrame(), menu.getFunctionName(menu.getDistanceFunction()), menu.isAbsoluteDistance(), true);           
            if (hcl_dialog.showModal() != JOptionPane.OK_OPTION) {
                return null;
            }
            hcl_method = hcl_dialog.getMethod();
            hcl_samples = hcl_dialog.isClusterExperiments();
            hcl_genes = hcl_dialog.isClusterGenes();
            hcl_function = hcl_dialog.getDistanceMetric();
            hcl_absolute = hcl_dialog.getAbsoluteSelection();
        }
        
        this.experiment = framework.getData().getExperiment();
        Listener listener = new Listener();

        int genes = experiment.getNumberOfGenes();
        
        AlgorithmData data = new AlgorithmData();
        data.addParam("distance-factor", String.valueOf(1.0f));

        data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
        data.addParam("gsh-cluster-genes", String.valueOf(this.clusterGenes));
        
        data.addParam("distance-function", String.valueOf(function));
        data.addParam("number-of-clusters", String.valueOf(k));
        data.addParam("number-of-fakedMatrix", String.valueOf(f));
        data.addParam("number-of-swap", String.valueOf(s));
        // hcl parameters
        if (isHierarchicalTree) {
            data.addParam("hierarchical-tree", String.valueOf(true));
            data.addParam("method-linkage", String.valueOf(hcl_method));
            data.addParam("calculate-genes", String.valueOf(hcl_genes));
            data.addParam("calculate-experiments", String.valueOf(hcl_samples));
            data.addParam("hcl-distance-function", String.valueOf(hcl_function));
            data.addParam("hcl-distance-absolute", String.valueOf(hcl_absolute));
        }
        
        //script control parameters
        
        // alg name
        data.addParam("name", "GSH");
        
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
        
        this.clusterGenes = algData.getParams().getBoolean("gsh-cluster-genes");
        
        Listener listener = new Listener();
        try {
            
            FloatMatrix matrix = experiment.getMatrix();
            if(!clusterGenes)
                matrix = matrix.transpose();
            algData.addMatrix("experiment", matrix);
            
            algorithm = framework.getAlgorithmFactory().getAlgorithm("GSH");
            algorithm.addAlgorithmListener(listener);
            int genes = experiment.getNumberOfGenes();            
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(algData);
            long time = System.currentTimeMillis() - start;
            // getting the results
            Cluster result_cluster = result.getCluster("cluster");
            NodeList nodeList = result_cluster.getNodeList();
            
            AlgorithmParameters resultMap = result.getParams();
            int k = resultMap.getInt("number-of-clusters"); // NEED THIS TO GET THE VALUE OF NUMBER-OF-CLUSTERS
            
            this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            this.means = result.getMatrix("clusters-means");
            this.variances = result.getMatrix("clusters-variances");
            
            AlgorithmParameters params = algData.getParams();
            
            GeneralInfo info = new GeneralInfo();
            info.clusters = k;
            info.time = time;
            info.FM = params.getInt("number-of-fakedMatrix");
            info.ST = params.getInt("number-of-swap");
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
        }
        
    }
    
    
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    private DefaultMutableTreeNode createResultTree(Cluster result_cluster, GeneralInfo info) {
        DefaultMutableTreeNode root;
        if(this.clusterGenes)
            root = new DefaultMutableTreeNode("GSH - genes");
        else
            root = new DefaultMutableTreeNode("GSH - samples");
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
        int i;
        
        if(clusterGenes)
            tabViewer = new ClusterTableViewer(this.experiment, this.clusters, this.data);
        else
            tabViewer = new ExperimentClusterTableViewer(this.experiment, this.clusters, this.data);
        //return; // placeholder for ExptClusterTableViewer
        //expViewer = new GSHExperimentClusterViewer(this.experiment, this.clusters);
        
        for (i=0; i<this.clusters.length-1; i++) {
            node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), tabViewer, new Integer(i))));
        }
        //if(clusterGenes)
        node.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned ", tabViewer, new Integer(i))));
        //else
        //node.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned Experiments", expViewer, new Integer(i))));
        
        root.add(node);
    }
    
    /**
     * Adds nodes to display clusters data.
     */
    private void addExpressionImages(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer;
        int i;
        
        if(clusterGenes)
            expViewer = new GSHExperimentViewer(this.experiment, this.clusters);
        else
            expViewer = new GSHExperimentClusterViewer(this.experiment, this.clusters);
        
        for (i=0; i<this.clusters.length-1; i++) {
            node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expViewer, new Integer(i))));
        }
        if(clusterGenes)
            node.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned Genes", expViewer, new Integer(i))));
        else
            node.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned Experiments", expViewer, new Integer(i))));
        
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
        int k = this.clusters.length;
        
        if(!this.clusterGenes){
            clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            if(info.hcl_samples)
                clusters = getOrderedIndices(nodeList, clusters, info.hcl_genes);
        }
        for (int i=0; i<nodeList.getSize()-1; i++) {
            if(this.clusterGenes)
                node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), createHCLViewer(nodeList.getNode(i), info, null))));
            else
                node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), createHCLViewer(nodeList.getNode(i), info, clusters), new Integer(i))));
        }
        if(this.clusterGenes)
            node.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned Genes", createHCLViewer(nodeList.getNode(nodeList.getSize()-1), info, null))));
        else
            node.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned Experiments", createHCLViewer(nodeList.getNode(nodeList.getSize()-1), info, clusters), new Integer(nodeList.getSize()-1))));
        
        
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
            node.add(new DefaultMutableTreeNode(new LeafInfo("Genes in Clusters (#,%)", new GSHInfoViewer(this.clusters, this.experiment.getNumberOfGenes()))));
        else
            node.add(new DefaultMutableTreeNode(new LeafInfo("Samples in Clusters (#,%)", new GSHInfoViewer(this.clusters, this.experiment.getNumberOfSamples(), false))));
        root.add(node);
    }
    
    /**
     * Adds nodes to display centroid charts.
     */
    private void addCentroidViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        
        GSHCentroidViewer centroidViewer;
        GSHExperimentCentroidViewer expCentroidViewer;
        if(clusterGenes){
            centroidViewer = new GSHCentroidViewer(this.experiment, clusters);
            centroidViewer.setMeans(this.means.A);
            centroidViewer.setVariances(this.variances.A);
            for (int i=0; i<(this.clusters.length - 1); i++) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned Genes ", centroidViewer, new CentroidUserObject((this.clusters.length - 1), CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned Genes ", centroidViewer, new CentroidUserObject((this.clusters.length - 1), CentroidUserObject.VALUES_MODE))));
            
            GSHCentroidsViewer centroidsViewer = new GSHCentroidsViewer(this.experiment, clusters);
            centroidsViewer.setMeans(this.means.A);
            centroidsViewer.setVariances(this.variances.A);
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
            
        }
        else{
            expCentroidViewer = new GSHExperimentCentroidViewer(this.experiment, clusters);
            
            expCentroidViewer.setMeans(this.means.A);
            expCentroidViewer.setVariances(this.variances.A);
            for (int i=0; i<this.clusters.length-1; i++) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned Experiments", expCentroidViewer, new CentroidUserObject(this.clusters.length-1, CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Unassigned Experiments", expCentroidViewer, new CentroidUserObject(this.clusters.length-1, CentroidUserObject.VALUES_MODE))));
            
            GSHExperimentCentroidsViewer expCentroidsViewer = new GSHExperimentCentroidsViewer(this.experiment, clusters);
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
        node.add(new DefaultMutableTreeNode("Clusters: "+String.valueOf(info.clusters)));
        node.add(new DefaultMutableTreeNode("Faked_Matrix: " + String.valueOf(info.FM)));
        node.add(new DefaultMutableTreeNode("Swap_Time: " + String.valueOf(info.ST)));
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
                    break;
                case AlgorithmEvent.PROGRESS_VALUE:
                    break;
                case AlgorithmEvent.MONITOR_VALUE:
                    break;
            }
        }
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("cancel-command")) {
                algorithm.abort();
            }
        }
        
        public void windowClosing(WindowEvent e) {
            algorithm.abort();
        }
    }
    
    // the general info structure
    private class GeneralInfo {
        public int clusters;
        public long time;
        public String function;
        
        public int FM;
        public int ST;
        
        private boolean hcl;
        private int hcl_method;
        private boolean hcl_genes;
        private boolean hcl_samples;
        
        public String getMethodName() {
            return hcl ? HCLGUI.GeneralInfo.getMethodName(hcl_method) : "no linkage";
        }
        
    }
}
