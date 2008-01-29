/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: RNGUI.java,v $
 * $Revision: 1.7 $
 * $Date: 2005-03-10 20:39:04 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.rn;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.NodeList;
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
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.util.IntSorter;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;
import org.tigr.util.FloatMatrix;

public class RNGUI implements IClusterGUI, IScriptGUI {
    
    private Algorithm algorithm;
    private Progress progress;
    private FloatMatrix means;
    private FloatMatrix variances;
    private IData data;
    /**
     * Initialize the algorithm's parameters and execute it.
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        
        RelNetInitDialog dlg = new RelNetInitDialog(framework.getFrame());
        if (dlg.showModal() != JOptionPane.OK_OPTION) {
            return null;
        }
        
        boolean use_permutation = dlg.usePermutation();
        float min_threshold = dlg.getMinThreshold();
        float max_threshold = dlg.getMaxThreshold();
        boolean use_entropy = dlg.useEntropy();
        float entropy = use_entropy ? dlg.getEntropy() : 100f;
        boolean clusterGenes = dlg.isClusterGenes();
        this.data = framework.getData();
        
        Listener listener = new Listener();
        try {
            this.algorithm = framework.getAlgorithmFactory().getAlgorithm("RN");
            this.algorithm.addAlgorithmListener(listener);
            
            this.progress = new Progress(framework.getFrame(), "Calculating Relevance Network", listener);
            this.progress.show();
            
            AlgorithmData data = new AlgorithmData();
            Experiment experiment = framework.getData().getExperiment();
            data.addMatrix("experiment", clusterGenes ? experiment.getMatrix() : experiment.getMatrix().transpose());
            data.addParam("distance-factor", String.valueOf(1.0f));
            IDistanceMenu menu = framework.getDistanceMenu();
            data.addParam("distance-absolute", String.valueOf(true)); // is always absolute
            int function = Algorithm.PEARSON; // is always pearson
            data.addParam("distance-function", String.valueOf(function));
            data.addParam("use-permutation", String.valueOf(use_permutation));
            data.addParam("min-threshold", String.valueOf(min_threshold));
            data.addParam("max-threshold", String.valueOf(max_threshold));
            data.addParam("filter-by-entropy", String.valueOf(use_entropy));
            data.addParam("top-n-percent", String.valueOf(entropy));
            // stub: to test PVM version
            data.addParam("threshold", String.valueOf(min_threshold));
            
            long start = System.currentTimeMillis();
            AlgorithmData result = this.algorithm.execute(data);
            long time = System.currentTimeMillis() - start;
            int[][] clusters = convert2int(result.getCluster("cluster"));
            float[][] weights = convert2float(result.getCluster("weights"));
            this.means = result.getMatrix("means");
            this.variances = result.getMatrix("variances");
            AlgorithmParameters params = result.getParams();
            result = null; //gc
            int[] indices = getSortedIndices(clusters);
            
            GeneralInfo info = new GeneralInfo();
            info.time = time;
            info.links = params.getInt("links", 0);
            info.min_threshold = params.getFloat("min_threshold");
            info.max_threshold = max_threshold;
            info.entropy = entropy;
            info.function = menu.getFunctionName(function);
            info.absolute = true;
            return createResultTree(experiment, clusters, weights, indices, info, clusterGenes);
            
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
        
        RelNetInitDialog dlg = new RelNetInitDialog(framework.getFrame());
        if (dlg.showModal() != JOptionPane.OK_OPTION) {
            return null;
        }
        
        boolean use_permutation = dlg.usePermutation();
        float min_threshold = dlg.getMinThreshold();
        float max_threshold = dlg.getMaxThreshold();
        boolean use_entropy = dlg.useEntropy();
        float entropy = use_entropy ? dlg.getEntropy() : 100f;
        boolean clusterGenes = dlg.isClusterGenes();
        this.data = framework.getData();
        
        AlgorithmData data = new AlgorithmData();
        Experiment experiment = framework.getData().getExperiment();
        data.addParam("rn-cluster-genes", String.valueOf(clusterGenes));
        data.addParam("distance-factor", String.valueOf(1.0f));
        IDistanceMenu menu = framework.getDistanceMenu();
        data.addParam("distance-absolute", String.valueOf(true)); // is always absolute
        int function = Algorithm.PEARSON; // is always pearson
        data.addParam("distance-function", String.valueOf(function));
        data.addParam("use-permutation", String.valueOf(use_permutation));
        data.addParam("min-threshold", String.valueOf(min_threshold));
        data.addParam("max-threshold", String.valueOf(max_threshold));
        data.addParam("filter-by-entropy", String.valueOf(use_entropy));
        data.addParam("top-n-percent", String.valueOf(entropy));
        // stub: to test PVM version
        data.addParam("threshold", String.valueOf(min_threshold));
        
        //script control parameters
        
        // alg name
        data.addParam("name", "RN");

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
                                                             
        boolean clusterGenes = algData.getParams().getBoolean("rn-cluster-genes");
        System.out.println("cluster genes = "+clusterGenes);
        this.data = framework.getData();
        
        Listener listener = new Listener();
 
        try {
            this.algorithm = framework.getAlgorithmFactory().getAlgorithm("RN");
            this.algorithm.addAlgorithmListener(listener);
            
            this.progress = new Progress(framework.getFrame(), "Calculating Relevance Network", listener);
            this.progress.show();
                        
            algData.addMatrix("experiment", clusterGenes ? experiment.getMatrix() : experiment.getMatrix().transpose());            
            IDistanceMenu menu = framework.getDistanceMenu();            
            int function = Algorithm.PEARSON; // is always pearson
            
            
            long start = System.currentTimeMillis();
            AlgorithmData result = this.algorithm.execute(algData);
            long time = System.currentTimeMillis() - start;
            int[][] clusters = convert2int(result.getCluster("cluster"));
            float[][] weights = convert2float(result.getCluster("weights"));
            this.means = result.getMatrix("means");
            this.variances = result.getMatrix("variances");
            AlgorithmParameters params = result.getParams();
            result = null; //gc
            int[] indices = getSortedIndices(clusters);
            
            System.out.println("cluster length ="+clusters.length);
            
            AlgorithmParameters algDataParams = algData.getParams();
            function = algDataParams.getInt("distance-function");
            
            GeneralInfo info = new GeneralInfo();
            info.time = time;
            info.links = params.getInt("links", 0);
            info.min_threshold = algDataParams.getFloat("min-threshold");
            info.max_threshold = algDataParams.getFloat("max-threshold");
            info.entropy = algDataParams.getFloat("top-n-percent");
            info.function = menu.getFunctionName(function);
            info.absolute = true;
            return createResultTree(experiment, clusters, weights, indices, info, clusterGenes);
            
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
     * Converts a passed cluster into a two dimensional int array.
     */
    private int[][] convert2int(Cluster cluster) throws AlgorithmException {
        NodeList nodeList = cluster.getNodeList();
        final int nodeListSize = nodeList.getSize();
        int[][] result = new int[nodeListSize][];
        for (int i=0; i<nodeListSize; i++) {
            result[i] = nodeList.getNode(i).getFeaturesIndexes();
            if (result[i] == null) {
                throw new AlgorithmException("Cluster "+i+" does not contain indices.");
            }
        }
        return result;
    }
    
    private static float[] int2float(int[] ints) {
        if (ints == null)
            return null;
        float[] floats = new float[ints.length];
        for (int i=0; i<floats.length; i++)
            floats[i] = Float.intBitsToFloat(ints[i]);
        return floats;
    }
    
    /**
     * Converts a passed cluster into a two dimensional float array
     */
    private float[][] convert2float(Cluster cluster) {
        if (cluster == null) {
            return null;
        }
        NodeList nodeList = cluster.getNodeList();
        float[][] result = new float[nodeList.getSize()][];
        for (int i=0; i<nodeList.getSize(); i++)
            result[i] = int2float(nodeList.getNode(i).getFeaturesIndexes());
        return result;
    }
    
    /**
     * Sort the order of specified clusters.
     * @return array of sorted indices.
     */
    private int[] getSortedIndices(int[][] clusters) {
        int[] indices = new int[clusters.length];
        for (int i = indices.length; --i >= 0;) {
            indices[i] = i;
        }
        IntSorter.sort(indices, new RelNetComparator(clusters));
        
        return indices;
    }
    
    /**
     * Creates the relnet analysis result tree.
     */
    private DefaultMutableTreeNode createResultTree(Experiment experiment, int[][] clusters, float[][] weights, int[] indices, GeneralInfo info, boolean clusterGenes) {        
        DefaultMutableTreeNode root;
        if(clusterGenes)
            root = new DefaultMutableTreeNode("RelNet - genes");
        else
            root = new DefaultMutableTreeNode("RelNet - samples");
            
        addResultNodes(root, experiment, clusters, weights, indices, info, clusterGenes);
        return root;
    }
    
    /**
     * Adds a result nodes.
     */
    private void addResultNodes(DefaultMutableTreeNode root, Experiment experiment, int[][] clusters, float[][] weights, int[] indices, GeneralInfo info, boolean clusterGenes) {
        
        DefaultMutableTreeNode elementClusterNode = new DefaultMutableTreeNode("Element Seed Clusters");
        addExpressionImages(elementClusterNode, experiment, clusters, indices, clusterGenes);
        addCentroidViews(elementClusterNode, experiment, clusters, indices, clusterGenes);
        addTableViews(elementClusterNode, experiment, clusters, indices, clusterGenes);
        addElementClusterInfo(elementClusterNode, experiment, clusters, indices, clusterGenes);
        
        DefaultMutableTreeNode subnetNode = new DefaultMutableTreeNode("Subnets");
        addSubnets(subnetNode, experiment, clusters, clusterGenes);  //adds expression images AND graphs
        
        addRelevanceNetworkViewer(root, experiment, clusters, weights, indices, clusterGenes);
        root.add(elementClusterNode);
        root.add(subnetNode);
        addGeneralInfo(root, info);
    }
    
    private void addElementClusterInfo(DefaultMutableTreeNode root, Experiment experiment, int [][] clusters, int [] orderedIndices, boolean clusterGenes){
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Cluster Information");
        if(clusterGenes)
            node.add(new DefaultMutableTreeNode(new LeafInfo("Genes in Clusters (#,%)", new RNElementSeedInfoViewer(clusters, experiment, orderedIndices, experiment.getNumberOfGenes(), true))));
        else
            node.add(new DefaultMutableTreeNode(new LeafInfo("Samples in Clusters (#,%)", new RNElementSeedInfoViewer(clusters, experiment, orderedIndices, experiment.getNumberOfSamples(), false))));
        root.add(node);
    }
    
    private void addSubnetClusterInfo(DefaultMutableTreeNode root, Experiment experiment, int [][] clusters, int orderedIndices [], boolean clusterGenes){
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Cluster Information");
        if(clusterGenes)
            node.add(new DefaultMutableTreeNode(new LeafInfo("Genes in Subnets (#,%)", new RNSubnetInfoViewer(clusters, orderedIndices, experiment.getNumberOfGenes(), true))));
        else
            node.add(new DefaultMutableTreeNode(new LeafInfo("Samples in Subnets (#,%)", new RNSubnetInfoViewer(clusters, orderedIndices, experiment.getNumberOfSamples(), false))));
        root.add(node);
    }
    
    
    private void addTableViews(DefaultMutableTreeNode root, Experiment experiment, int[][] clusters, int[] indices, boolean clusterGenes) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table views");
        IViewer viewer;
        if (clusterGenes)
            viewer = new ClusterTableViewer(experiment, clusters, this.data);
        else
            viewer = new ExperimentClusterTableViewer(experiment, clusters, this.data);
        //return; //placeholder for ExptClusterTableViewer
        //viewer = new RelNetExperimentClusterViewer(experiment, clusters);
        for (int i=0; i<clusters.length; i++)
            if (clusters[indices[i]].length > 1)
                node.add(new DefaultMutableTreeNode(new LeafInfo("Element index "+String.valueOf(experiment.getGeneIndexMappedToData(clusters[i][0])+1)+" ("+clusters[indices[i]].length+")", viewer, new Integer(indices[i]))));
        root.add(node);
    }
    
    /**
     * Adds nodes to display expression images.
     */
    private void addExpressionImages(DefaultMutableTreeNode root, Experiment experiment, int[][] clusters, int[] indices, boolean clusterGenes) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer viewer;
        if (clusterGenes)
            viewer = new RelNetExperimentViewer(experiment, clusters);
        else
            viewer = new RelNetExperimentClusterViewer(experiment, clusters);
        for (int i=0; i<clusters.length; i++)
            if (clusters[indices[i]].length > 1)
                node.add(new DefaultMutableTreeNode(new LeafInfo("Element index "+String.valueOf(experiment.getGeneIndexMappedToData(clusters[i][0])+1)+" ("+clusters[indices[i]].length+")", viewer, new Integer(indices[i]))));
        root.add(node);
    }
    
    /**
     * Adds nodes to display centroid charts.
     */
    private void addCentroidViews(DefaultMutableTreeNode root, Experiment experiment, int [][] clusters, int [] indices, boolean clusterGenes) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        
        RNCentroidViewer centroidViewer;
        ExperimentClusterCentroidViewer expCentroidViewer;
        if(clusterGenes){
            centroidViewer = new RNCentroidViewer(experiment, clusters);
            centroidViewer.setMeans(this.means.A);
            centroidViewer.setVariances(this.variances.A);
            for (int i=0; i< clusters.length; i++) {
                if (clusters[indices[i]].length > 1){
                    centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Element index "+String.valueOf(experiment.getGeneIndexMappedToData(clusters[i][0])+1)+" ("+clusters[indices[i]].length+")", centroidViewer, new CentroidUserObject(indices[i], CentroidUserObject.VARIANCES_MODE))));
                    expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Element index "+String.valueOf(experiment.getGeneIndexMappedToData(clusters[i][0])+1)+" ("+clusters[indices[i]].length+")", centroidViewer, new CentroidUserObject(indices[i], CentroidUserObject.VALUES_MODE))));
                }
            }
            
            //    RNCentroidsViewer centroidsViewer = new RNCentroidsViewer(experiment, clusters);
            //    centroidsViewer.setMeans(this.means.A);
            //    centroidsViewer.setVariances(this.variances.A);
            
            //    centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            //    expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
            
        }
        else{
            expCentroidViewer = new RNExperimentCentroidViewer(experiment, clusters);
            
            expCentroidViewer.setMeans(this.means.A);
            expCentroidViewer.setVariances(this.variances.A);
            for (int i=0; i<clusters.length; i++) {
                if (clusters[indices[i]].length > 1){
                    centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Element index "+String.valueOf(indices[i]+1)+" ("+clusters[indices[i]].length+")", expCentroidViewer, new CentroidUserObject(indices[i], CentroidUserObject.VARIANCES_MODE))));
                    expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Element index "+String.valueOf(indices[i]+1)+" ("+clusters[indices[i]].length+")", expCentroidViewer, new CentroidUserObject(indices[i], CentroidUserObject.VALUES_MODE))));
                }
            }
            
            RNExperimentCentroidsViewer expCentroidsViewer = new RNExperimentCentroidsViewer(experiment, clusters);
            expCentroidsViewer.setMeans(this.means.A);
            expCentroidsViewer.setVariances(this.variances.A);
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", expCentroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", expCentroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        }
        root.add(centroidNode);
        root.add(expressionNode);
    }
    
    /**
     * Adds nodes to display relevance subnets.
     */
    private void addSubnets(DefaultMutableTreeNode root, Experiment experiment, int[][] clusters, boolean clusterGenes) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        RelevanceNetworkLayout layout = new RelevanceNetworkLayout();
        int[][] subnets = layout.formRelevanceNetworks(clusters);
        int[] indices = getSortedIndices(subnets);
        IViewer viewer;
        if (clusterGenes)
            viewer = new RelNetExperimentViewer(experiment, subnets);
        else
            viewer = new RelNetExperimentClusterViewer(experiment, subnets);
        
        for (int i=0; i<subnets.length; i++)
            node.add(new DefaultMutableTreeNode(new LeafInfo("Subnet "+String.valueOf(i+1)+" ("+subnets[indices[i]].length+")", viewer, new Integer(indices[i]))));
        root.add(node);
        addSubnetCentroidViews(root, experiment, subnets, indices, clusterGenes);
        addSubnetTableViews(root, experiment, subnets, indices, clusterGenes);
        addSubnetClusterInfo(root, experiment, subnets, indices, clusterGenes);
    }
    
    private void addSubnetTableViews(DefaultMutableTreeNode root, Experiment experiment, int [][] subnets, int [] indices, boolean clusterGenes) {
        DefaultMutableTreeNode tabNode = new DefaultMutableTreeNode("Table Views");
        IViewer viewer;
        if (clusterGenes)
            viewer = new ClusterTableViewer(experiment, subnets, this.data);
        else
           // viewer = new ExperimentClusterTableViewer(experiment, subnets, this.data);
        return; //placeholder for ExptClusterTableViewer
        for (int i=0; i<subnets.length; i++)
            tabNode.add(new DefaultMutableTreeNode(new LeafInfo("Subnet "+String.valueOf(i+1)+" ("+subnets[indices[i]].length+")", viewer, new Integer(indices[i]))));
        root.add(tabNode);
    }
    
    private void addSubnetCentroidViews(DefaultMutableTreeNode root, Experiment experiment, int [][] subnets, int [] indices, boolean clusterGenes){
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        
        RNCentroidViewer centroidViewer;
        ExperimentClusterCentroidViewer expCentroidViewer;
        FloatMatrix subnetMeans = getMeans(subnets, experiment, clusterGenes);
        FloatMatrix subnetVars = getVariances(subnetMeans, experiment, subnets, clusterGenes);
        if(clusterGenes){
            centroidViewer = new RNCentroidViewer(experiment, subnets);
            centroidViewer.setMeans(subnetMeans.A);
            centroidViewer.setVariances(subnetVars.A);
            for (int i=0; i< subnets.length; i++) {
                
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Subnet "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(indices[i], CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Subnet "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(indices[i], CentroidUserObject.VALUES_MODE))));
                
            }
            
            RNCentroidsViewer centroidsViewer = new RNCentroidsViewer(experiment, subnets);
            centroidsViewer.setMeans(subnetMeans.A);
            centroidsViewer.setVariances(subnetVars.A);
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
            
        }
        else{
            expCentroidViewer = new RNExperimentCentroidViewer(experiment, subnets);
            
            expCentroidViewer.setMeans(subnetMeans.A);
            expCentroidViewer.setVariances(subnetVars.A);
            for (int i=0; i<subnets.length; i++) {
                
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Subnet "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(indices[i], CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Subnet "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(indices[i], CentroidUserObject.VALUES_MODE))));
                
            }
            
            RNExperimentCentroidsViewer expCentroidsViewer = new RNExperimentCentroidsViewer(experiment, subnets);
            expCentroidsViewer.setMeans(subnetMeans.A);
            expCentroidsViewer.setVariances(subnetVars.A);
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", expCentroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", expCentroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        }
        root.add(centroidNode);
        root.add(expressionNode);
    }
    
    
    /**
     *  Retuns means values for each column within positives and negatives
     */
    private FloatMatrix getMeans(int [][] subnets, Experiment experiment, boolean classifyGenes){
        FloatMatrix expMatrix = experiment.getMatrix();
        
        if(!classifyGenes)
            expMatrix = expMatrix.transpose();
        
        int numSamples = expMatrix.getColumnDimension();
        int numGenes = expMatrix.getRowDimension();
        
        FloatMatrix means = new FloatMatrix(subnets.length, numSamples);
        
        float value;
        float cumVal = 0;
        int n = 0;
        int index = 0;
        float [] currMeans;
        for(int i = 0; i < subnets.length; i++){
            currMeans = new float[numSamples];
            for(int j = 0; j < numSamples; j++){
                for(int k = 0; k < subnets[i].length; k++){
                    index = subnets[i][k];
                    value = expMatrix.get(index, j);
                    if(!Float.isNaN(value)){
                        n++;
                        cumVal += value;
                    }
                }
                if(n > 0){
                    currMeans[j] = cumVal/n;
                } else {
                    currMeans[j] = 0;
                }
                n = 0;
                cumVal = 0;
            }
            means.A[i] = currMeans;
        }
        return means;
    }
    
    private FloatMatrix getVariances(FloatMatrix means, Experiment experiment, int [][] subnets, boolean clusterGenes){
        FloatMatrix expMatrix = experiment.getMatrix();
        
        if(!clusterGenes)
            expMatrix = expMatrix.transpose();
        
        int numSamples = expMatrix.getColumnDimension();
        int numGenes = expMatrix.getRowDimension();
        
        FloatMatrix vars = new FloatMatrix(subnets.length, numSamples);
        
        float mean = 0;
        float value;
        float cumVal = 0;
        int n = 0;
        int index = 0;
        float [] currVars;
        for(int i = 0; i < subnets.length; i++){
            currVars = new float[numSamples];
            for(int j = 0; j < numSamples; j++){
                for(int k = 0; k < subnets[i].length; k++){
                    index = subnets[i][k];
                    value = expMatrix.get(index, j);
                    if(!Float.isNaN(value)){
                        n++;
                        cumVal += Math.pow((value-means.get(i,j)), 2.0);
                    }
                }
                if(n > 1)
                    currVars[j] = (float) Math.sqrt(cumVal/(n-1));
                else
                    currVars[j] = 0;
                n = 0;
                cumVal = 0;
            }
            vars.A[i] = currVars;
        }
        return vars;
    }
    
    
    /**
     * Adds the node with a <code>RelevanceNetworkViewer</code>.
     */
    private void addRelevanceNetworkViewer(DefaultMutableTreeNode root, Experiment experiment, int[][] clusters, float[][] weights, int[] indices, boolean clusterGenes) {
        root.add(new DefaultMutableTreeNode(new LeafInfo("Network", new RelevanceNetworkViewer(clusterGenes, experiment, clusters, weights, indices))));
    }
    
    /**
     * Adds the node with a general info.
     */
    private void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        node.add(new DefaultMutableTreeNode("Links: "+String.valueOf(info.links)));
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time)+" ms"));
        node.add(new DefaultMutableTreeNode("Min Threshold: "+String.valueOf(info.min_threshold)));
        node.add(new DefaultMutableTreeNode("Max Threshold: "+String.valueOf(info.max_threshold)));
        node.add(new DefaultMutableTreeNode("Highest Entropy Filter: "+String.valueOf(info.entropy)+" %"));
        node.add(new DefaultMutableTreeNode(info.function));
        node.add(new DefaultMutableTreeNode("Absolute: "+String.valueOf(info.absolute)));
        root.add(node);
    }
    
    
    /**
     * The class to listen to progress and algorithms events.
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
        private long time;
        private int links;
        private float min_threshold;
        private float max_threshold;
        private float entropy;
        private String function;
        private boolean absolute;
    }
}
