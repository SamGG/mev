/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: CASTGUI.java,v $
 * $Revision: 1.3 $
 * $Date: 2004-04-29 18:51:41 $
 * $Author: nbhagaba $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.cast;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.util.ConfMap;
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

public class CASTGUI implements IClusterGUI {
    
    private Algorithm algorithm;
    
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
    public CASTGUI() {
    }
    
    /**
     * Initialize the algorithm's parameters and execute it.
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        this.data = framework.getData();
        // the default values
        float threshold = 0.5f;
        boolean modal = true; //I SET MODAL TO TRUE TO CALL THE HJC DIALOG BOX, HOPE THIS IS OK
        
        CASTInitDialog cast_dialog = new CASTInitDialog((JFrame) framework.getFrame(), modal);
        cast_dialog.setVisible(true);
        
        if (! cast_dialog.isOkPressed()) return null;
        
        clusterGenes = cast_dialog.isClusterGenes();
        threshold = Float.parseFloat(cast_dialog.thresholdTextField.getText());
        boolean isHierarchicalTree = cast_dialog.isHCLSelected();
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
            algorithm = framework.getAlgorithmFactory().getAlgorithm("CAST");
            algorithm.addAlgorithmListener(listener);
            
            int genes = experiment.getNumberOfGenes();
            
            AlgorithmData data = new AlgorithmData();
            FloatMatrix matrix = experiment.getMatrix();
            
            if(!clusterGenes)
                matrix = matrix.transpose();
            
            data.addMatrix("experiment", matrix);
            data.addParam("cast-cluster-genes", String.valueOf(clusterGenes));
            data.addParam("distance-factor", String.valueOf(1.0f));
            data.addParam("threshold", String.valueOf(threshold));
            IDistanceMenu menu = framework.getDistanceMenu();
            data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
            int function = menu.getDistanceFunction();
            int usedFunction = Algorithm.EUCLIDEAN;
            
            if (function == Algorithm.DEFAULT) {
                function = Algorithm.EUCLIDEAN;
            }
            
            
            
            data.addParam("distance-function", String.valueOf(function));
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
            k = resultMap.getInt("number-of-clusters"); // I USED THIS TO GET THE VALUE OF NUMBER-OF-CLUSTERS
            
            this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            
            GeneralInfo info = new GeneralInfo();
            info.clusters = k;
            info.time = time;
            info.threshold = threshold;
            info.isAbsolute = menu.isAbsoluteDistance();
            
            
            if((function == Algorithm.DEFAULT)||(function == Algorithm.COSINE)
            ||(function == Algorithm.COVARIANCE)||(function == Algorithm.EUCLIDEAN)
            ||(function == Algorithm.DOTPRODUCT)||(function == Algorithm.MANHATTAN)
            ||(function == Algorithm.SPEARMANRANK)||(function == Algorithm.KENDALLSTAU)
            ||(function == Algorithm.MUTUALINFORMATION)){
                // ONLY EUCLIDEAN OR PEARSON IS USED IN THE CALCULATION!!!!
                usedFunction = Algorithm.EUCLIDEAN;
            }
            else if((function == Algorithm.PEARSON)||(function == Algorithm.PEARSONUNCENTERED)
            ||(function == Algorithm.PEARSONSQARED)) {
                
                usedFunction = Algorithm.PEARSON;
                
            }
            info.function = menu.getFunctionName(usedFunction);
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
     * Creates a result tree to be inserted into the framework analysis node.
     */
    private DefaultMutableTreeNode createResultTree(Cluster result_cluster, GeneralInfo info) {
        DefaultMutableTreeNode root;
        if(this.clusterGenes)
            root = new DefaultMutableTreeNode("CAST - genes");
        else
            root = new DefaultMutableTreeNode("CAST - experiments");
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
            tabViewer= new ClusterTableViewer(this.experiment, this.clusters, this.data);
        else
            tabViewer= new ExperimentClusterTableViewer(this.experiment, this.clusters, this.data);
            //return; // placeholder for ExptClusterTableViewer
            //expViewer = new CASTExperimentClusterViewer(this.experiment, this.clusters);
        
        for (int i=0; i<this.clusters.length; i++) {
            node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), tabViewer, new Integer(i))));
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
            expViewer= new CASTExperimentViewer(this.experiment, this.clusters);
        else
            expViewer = new CASTExperimentClusterViewer(this.experiment, this.clusters);
        
        for (int i=0; i<this.clusters.length; i++) {
            node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expViewer, new Integer(i))));
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
     * Adds node with cluster information.
     */
    private void addClusterInfo(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Cluster Information");
        if(this.clusterGenes)
            node.add(new DefaultMutableTreeNode(new LeafInfo("Genes in Clusters (#,%)", new CASTInfoViewer(this.clusters, this.experiment.getNumberOfGenes()))));
        else
            node.add(new DefaultMutableTreeNode(new LeafInfo("Experiments in Clusters (#,%)", new CASTInfoViewer(this.clusters, this.experiment.getNumberOfSamples(), false))));
        root.add(node);
    }
    
    /**
     * Adds nodes to display centroid charts.
     */
    private void addCentroidViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        
        CASTCentroidViewer centroidViewer;
        CASTExperimentCentroidViewer expCentroidViewer;
        if(clusterGenes){
            centroidViewer = new CASTCentroidViewer(this.experiment, clusters);
            centroidViewer.setMeans(this.means.A);
            centroidViewer.setVariances(this.variances.A);
            for (int i=0; i<this.clusters.length; i++) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            
            CASTCentroidsViewer centroidsViewer = new CASTCentroidsViewer(this.experiment, clusters);
            centroidsViewer.setMeans(this.means.A);
            centroidsViewer.setVariances(this.variances.A);
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
            
        }
        else{
            expCentroidViewer = new CASTExperimentCentroidViewer(this.experiment, clusters);
            
            expCentroidViewer.setMeans(this.means.A);
            expCentroidViewer.setVariances(this.variances.A);
            for (int i=0; i<this.clusters.length; i++) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            CASTExperimentCentroidsViewer expCentroidsViewer = new CASTExperimentCentroidsViewer(this.experiment, clusters);
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
        node.add(new DefaultMutableTreeNode("Threshold: "+String.valueOf(info.threshold)));
        node.add(new DefaultMutableTreeNode("Absolute distance: "+String.valueOf(info.isAbsolute)));
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
            
        }
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("cancel-command")) {
                algorithm.abort();
                cleanUp();
            }
        }
        
        public void windowClosing(WindowEvent e) {
            algorithm.abort();
            cleanUp();
        }
        
        public void cleanUp() {
        }
    }
    
    // the general info structure
    private class GeneralInfo {
        public int clusters;
        //public boolean converged;
        //public int iterations;
        public long time;
        public float threshold;
        public boolean isAbsolute;
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