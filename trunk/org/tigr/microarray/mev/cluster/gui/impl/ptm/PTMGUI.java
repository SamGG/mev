/*
Copyright @ 1999-2006, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: PTMGUI.java,v $
 * $Revision: 1.11 $
 * $Date: 2006-02-14 15:31:10 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.ptm;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JFrame;
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
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterList;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
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
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLGUI;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLViewer;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;
import org.tigr.util.FloatMatrix;


public class PTMGUI implements IClusterGUI, IScriptGUI {
    
    private Algorithm algorithm;
    private Experiment experiment;
    private int[][] clusters;
    private FloatMatrix means;
    private FloatMatrix variances;
    private IData data;
    Vector templateVector;
    private float[] pValues, rValues;
    private boolean clusterGenes = false;
    private Object[][] auxData;
    private String[] auxTitles;
    private boolean drawSigTreesOnly;    
    
    /**
     * Default constructor.
     */
    public PTMGUI() {
        
    }
    
    
    
    /**
     * Initialize the algorithm's parameters and execute it.
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        int k = 2; //number of clusters
        boolean modality = true; // HOPE THIS IS OK; DID THIS TO CALL UP THE DIALOG BOX
        boolean useAbsolute = false;
        boolean useR = false;
        FloatMatrix templateMatrix;
        float threshold = 0.8f;
        
        this.experiment = framework.getData().getExperiment();
        this.data = framework.getData();
        Vector sampleNamesVector = new Vector();
        int number_of_samples = experiment.getNumberOfSamples();
        int number_of_genes = experiment.getNumberOfGenes();
        
        for (int i = 0; i < number_of_samples; i++) {
            sampleNamesVector.add(framework.getData().getFullSampleName(experiment.getSampleIndex(i)));
        }
        
        Vector uniqueIDs = new Vector();
        int labelIndex = framework.getDisplayMenu().getLabelIndex();
        for (int i = 0; i < number_of_genes; i++) {
            uniqueIDs.add(framework.getData().getElementAttribute(experiment.getGeneIndexMappedToData(i), labelIndex));
            //uniqueIDs.add(framework.getData().getUniqueId(experiment.getGeneIndexMappedToData(i)));
        }
        
        Vector clusterVector = new Vector();
        
        Color clusterColors[] = framework.getData().getColors();

        boolean assignedToACluster[] = new boolean[number_of_genes];
        
        for (int i = 0; i < number_of_genes; i++) {
            assignedToACluster[i] = false;
        }
        
        //for (int i = 0; i < clusterColors.length; i++) {
          //  Vector currentCluster = new Vector();
            
            /*
            for (int j = 0; j < number_of_genes; j++) {
                if (!assignedToACluster[j]) {
                    if (clusterColors[i].equals(framework.getData().getProbeColor(experiment.getGeneIndexMappedToData(j)))) {
                        currentCluster.add(new Integer(j));
                        assignedToACluster[j] = true;
                    }
                }
            }
             */
        
        //can't assume that a null cluster color means that a gene is not in a cluster.
        ClusterRepository geneClusterRepository = framework.getClusterRepository(org.tigr.microarray.mev.cluster.clusterUtil.Cluster.GENE_CLUSTER);

        //need to get total cluster count
        int numClusters = 0;
        for(int i = 0; i < geneClusterRepository.size(); i++) {
        	numClusters += geneClusterRepository.getClusterList(i).size();
        } 

        clusterColors = new Color[numClusters];
        int clusterIndex = 0;
        
        for(int i = 0; i < geneClusterRepository.size(); i++) {
            ClusterList list = geneClusterRepository.getClusterList(i);
            Vector currentCluster;
            
            //clusterColors = new Color[list.size()];
            
            for(int j = 0; j < list.size(); j++) {
                //make a vector     
                currentCluster = new Vector();
                                
                //get cluster
                org.tigr.microarray.mev.cluster.clusterUtil.Cluster mevCluster = list.getClusterAt(j);
                clusterColors[clusterIndex] = mevCluster.getClusterColor();
                clusterIndex++;
                
                //check genes for membership and add to cluster vector
                for(int m = 0 ; m < number_of_genes; m++) {
                    if(mevCluster.isMember(experiment.getGeneIndexMappedToData(m)))
                        currentCluster.add(new Integer(m));
                        assignedToACluster[m] = true;
                }

                clusterVector.add(currentCluster);
                
            }  
        }
    
        
        //Experiment clustering
        Vector expClusterVector = new Vector();
        Color expClusterColors[] = framework.getData().getExperimentColors();
        boolean expAssignedToACluster[] = new boolean[number_of_samples];
        
        for(int i = 0; i < number_of_samples; i++) {
            expAssignedToACluster[i] = false;
        }

        //11.04.2005 JCB, bug fix, GENE_CLUSTER->EXPERIMENT_CLUSTER
        ClusterRepository expClusterRepository = framework.getClusterRepository(org.tigr.microarray.mev.cluster.clusterUtil.Cluster.EXPERIMENT_CLUSTER);
        
        //need to get total cluster count
        numClusters = 0;
        for(int i = 0; i < expClusterRepository.size(); i++) {
        	numClusters += expClusterRepository.getClusterList(i).size();
        } 

        expClusterColors = new Color[numClusters];
        clusterIndex = 0;        
        
        for(int i = 0; i < expClusterRepository.size(); i++) {
            ClusterList list = expClusterRepository.getClusterList(i);
            Vector currentCluster;
                        
            for(int j = 0; j < list.size(); j++) {
                //make a vector     
                currentCluster = new Vector();
                
                //get cluster
                org.tigr.microarray.mev.cluster.clusterUtil.Cluster mevCluster = list.getClusterAt(j);
                expClusterColors[clusterIndex] = mevCluster.getClusterColor();
                clusterIndex++;
                
                //check genes for membership and add to cluster vector
                for(int m = 0 ; m < number_of_samples; m++) {
                    if(mevCluster.isMember(experiment.getSampleIndex(m)))
                        currentCluster.add(new Integer(m));
                        expAssignedToACluster[m] = true;
                }
                
                expClusterVector.add(currentCluster);                
            }  
        }
      /*  for (int i = 0; i < expClusterColors.length; i++) {
            Vector currentCluster = new Vector();
            
            for (int j = 0; j < number_of_samples; j++) {
                if (!expAssignedToACluster[j]) {
                    if (expClusterColors[i].equals(framework.getData().getExperimentColor(experiment.getSampleIndex(j)))) {
                        currentCluster.add(new Integer(j));
                        expAssignedToACluster[j] = true;
                    }
                }
            }
            expClusterVector.add(currentCluster);
        }
       */

        
        PTMInitDialog  ptmInitBox = new PTMInitDialog((JFrame)framework.getFrame(), modality, experiment.getMatrix(), uniqueIDs, sampleNamesVector, clusterVector, expClusterVector, clusterColors, expClusterColors);
        
        boolean isHierarchicalTree;
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        
        ptmInitBox.setVisible(true);
        
        
        if (! ptmInitBox.isOkPressed()){
            return null;
        }
        clusterGenes = ptmInitBox.isGeneTemplate();
        
        if(clusterGenes){
            templateMatrix = ptmInitBox.convertTemplateVectorToFloatMatrix();
            templateVector = ptmInitBox.getTemplate();
            useAbsolute = ptmInitBox.isUseAbsolute();
            useR = ptmInitBox.isUseR();
            threshold = (float) ptmInitBox.getThresholdR();
            isHierarchicalTree = ptmInitBox.isDrawTrees();
        }
        else{
            templateMatrix = ptmInitBox.convertTemplateVectorToFloatMatrix();
            templateVector = ptmInitBox.getTemplate();
            useAbsolute = ptmInitBox.isUseAbsolute();
            useR = ptmInitBox.isUseR();
            threshold = (float) ptmInitBox.getThresholdR();
            isHierarchicalTree = ptmInitBox.isDrawTrees();
        }
        
        drawSigTreesOnly = true;
        if (isHierarchicalTree) {
            drawSigTreesOnly = ptmInitBox.drawSigTreesOnly();
        }        

        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.EUCLIDEAN;
        }
        
        int hcl_function = 4;
        boolean hcl_absolute = false;        
        
        if (isHierarchicalTree) {
            HCLInitDialog hcl_dialog = new HCLInitDialog(new JFrame(), menu.getFunctionName(function), menu.isAbsoluteDistance(), true);
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
            algorithm = framework.getAlgorithmFactory().getAlgorithm("PTM");
            algorithm.addAlgorithmListener(listener);
            FloatMatrix matrix = experiment.getMatrix();
            int genes;
            if(!clusterGenes){
                matrix = matrix.transpose();
                genes = matrix.getRowDimension();
            }
            genes = experiment.getNumberOfGenes();
            
            AlgorithmData data = new AlgorithmData();
            
            data.addMatrix("experiment", matrix);
            data.addParam("ptm-cluster-genes", String.valueOf(clusterGenes));
            data.addMatrix("templateVectorMatrix", templateMatrix);
            data.addParam("distance-factor", String.valueOf(1.0f));

            data.addParam("use-absolute", String.valueOf(useAbsolute));
            data.addParam("useR", String.valueOf(useR));
            data.addParam("threshold", String.valueOf(threshold));

            //function = Algorithm.PEARSON;
            data.addParam("distance-function", String.valueOf(function));
            
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
            
            this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            
            FloatMatrix rValuesMatrix = result.getMatrix("rValuesMatrix");
            FloatMatrix pValuesMatrix = result.getMatrix("pValuesMatrix");
            
            pValues = new float[pValuesMatrix.getRowDimension()];
            rValues = new float[rValuesMatrix.getRowDimension()];
            
            for (int i = 0; i < pValues.length; i++) {
                pValues[i] = pValuesMatrix.A[i][0];
                rValues[i] = rValuesMatrix.A[i][0];
            }
            
            auxTitles = new String[2];
            auxTitles[0] = "R values";
            auxTitles[1] = "p Values";
            
            auxData = new Object[pValues.length][2];
            
            for (int i = 0; i < auxData.length; i++) {
                auxData[i][0] = new Float(rValues[i]);
                auxData[i][1] = new Float(pValues[i]);
            }
            
            GeneralInfo info = new GeneralInfo();
            info.clusters = k;
            info.time = time;
            info.function = menu.getFunctionName(function);
            info.isR = useR;
            info.isAbsolute = useAbsolute;
            info.threshold = threshold;
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
        int k = 2; //number of clusters
        boolean modality = true; // HOPE THIS IS OK; DID THIS TO CALL UP THE DIALOG BOX
        boolean useAbsolute = false;
        boolean useR = false;
        FloatMatrix templateMatrix;
        float threshold = 0.8f;
        
        this.experiment = framework.getData().getExperiment();
        this.data = framework.getData();
        Vector sampleNamesVector = new Vector();
        int number_of_samples = experiment.getNumberOfSamples();
        int number_of_genes = experiment.getNumberOfGenes();
        
        for (int i = 0; i < number_of_samples; i++) {
            sampleNamesVector.add(framework.getData().getFullSampleName(experiment.getSampleIndex(i)));
        }
        
        Vector uniqueIDs = new Vector();
        int labelIndex = framework.getDisplayMenu().getLabelIndex();
        for (int i = 0; i < number_of_genes; i++) {
            uniqueIDs.add(framework.getData().getElementAttribute(experiment.getGeneIndexMappedToData(i), labelIndex));
            //uniqueIDs.add(framework.getData().getUniqueId(experiment.getGeneIndexMappedToData(i)));
        }
        
        Vector clusterVector = new Vector();
        
        Color clusterColors[] = framework.getData().getColors();
        boolean assignedToACluster[] = new boolean[number_of_genes];
        
        for (int i = 0; i < number_of_genes; i++) {
            assignedToACluster[i] = false;
        }
        
        for (int i = 0; i < clusterColors.length; i++) {
            Vector currentCluster = new Vector();
            
            for (int j = 0; j < number_of_genes; j++) {
                if (!assignedToACluster[j]) {
                    if (clusterColors[i].equals(framework.getData().getProbeColor(experiment.getGeneIndexMappedToData(j)))) {
                        currentCluster.add(new Integer(j));
                        assignedToACluster[j] = true;
                    }
                }
            }
            
            clusterVector.add(currentCluster);
        }
        
        //Experiment clustering
        Vector expClusterVector = new Vector();
        Color expClusterColors[] = framework.getData().getExperimentColors();
        boolean expAssignedToACluster[] = new boolean[number_of_samples];
        
        for (int i = 0; i < number_of_samples; i++) {
            expAssignedToACluster[i] = false;
        }
        
        for (int i = 0; i < expClusterColors.length; i++) {
            Vector currentCluster = new Vector();
            
            for (int j = 0; j < number_of_samples; j++) {
                if (!expAssignedToACluster[j]) {
                    if (expClusterColors[i].equals(framework.getData().getExperimentColor(experiment.getSampleIndex(j)))) {
                        currentCluster.add(new Integer(j));
                        expAssignedToACluster[j] = true;
                    }
                }
            }
            expClusterVector.add(currentCluster);
        }
        
        PTMInitDialog  ptmInitBox = new PTMInitDialog((JFrame)framework.getFrame(), modality, experiment.getMatrix(), uniqueIDs, sampleNamesVector, clusterVector, expClusterVector, clusterColors, expClusterColors);
        
        boolean isHierarchicalTree;
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        
        ptmInitBox.setVisible(true);
        
        
        if (! ptmInitBox.isOkPressed()){
            return null;
        }
        clusterGenes = ptmInitBox.isGeneTemplate();
        
        if(clusterGenes){
            templateMatrix = ptmInitBox.convertTemplateVectorToFloatMatrix();
            templateVector = ptmInitBox.getTemplate();
            useAbsolute = ptmInitBox.isUseAbsolute();
            useR = ptmInitBox.isUseR();
            threshold = (float) ptmInitBox.getThresholdR();
            isHierarchicalTree = ptmInitBox.isDrawTrees();
        }
        else{
            templateMatrix = ptmInitBox.convertTemplateVectorToFloatMatrix();
            templateVector = ptmInitBox.getTemplate();
            useAbsolute = ptmInitBox.isUseAbsolute();
            useR = ptmInitBox.isUseR();
            threshold = (float) ptmInitBox.getThresholdR();
            isHierarchicalTree = ptmInitBox.isDrawTrees();
        }
        
        drawSigTreesOnly = true;
        if (isHierarchicalTree) {
            drawSigTreesOnly = ptmInitBox.drawSigTreesOnly();
        }        
        
        IDistanceMenu menu = framework.getDistanceMenu();        
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.EUCLIDEAN;
        }
        
        int hcl_function = 4;
        boolean hcl_absolute = false;        
        
        if (isHierarchicalTree) {
            HCLInitDialog hcl_dialog = new HCLInitDialog(new JFrame(), menu.getFunctionName(function), menu.isAbsoluteDistance(), true);
            if (hcl_dialog.showModal() != JOptionPane.OK_OPTION) {
                return null;
            }
            hcl_method = hcl_dialog.getMethod();
            hcl_samples = hcl_dialog.isClusterExperiments();
            hcl_genes = hcl_dialog.isClusterGenes();
            hcl_function = hcl_dialog.getDistanceMetric();
            hcl_absolute = hcl_dialog.getAbsoluteSelection();            
        }
                
        AlgorithmData data = new AlgorithmData();
        data.addParam("ptm-cluster-genes", String.valueOf(clusterGenes));
        data.addMatrix("templateVectorMatrix", templateMatrix);
        data.addParam("distance-factor", String.valueOf(1.0f));
        data.addParam("use-absolute", String.valueOf(useAbsolute));
        data.addParam("useR", String.valueOf(useR));
        data.addParam("threshold", String.valueOf(threshold));

        //function = Algorithm.PEARSON;
        data.addParam("distance-function", String.valueOf(function));
        
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
        data.addParam("name", "PTM");
        
        // alg type
        if(clusterGenes)
            data.addParam("alg-type", "cluster-genes");
        else
            data.addParam("alg-type", "cluster-experiments");
        
        // output class
        data.addParam("output-class", "partition-output");
        
        //output nodes
        String [] outputNodes = new String[2];
        outputNodes[0] = "Significant Genes";
        outputNodes[1] = "Non-significant Genes";
        
        data.addStringArray("output-nodes", outputNodes);
        
        return data;
    }
    
    public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, Experiment experiment) throws AlgorithmException {
        int k = 2;
        this.experiment = experiment;
        this.data = framework.getData();
        AlgorithmParameters params = algData.getParams();
        this.drawSigTreesOnly = algData.getParams().getBoolean("draw-sig-trees-only");        
        this.clusterGenes = params.getBoolean("ptm-cluster-genes");
        FloatMatrix templateMatrix = algData.getMatrix("templateVectorMatrix");
        this.templateVector = new Vector();
        int cols = templateMatrix.getColumnDimension();
        for(int i = 0; i < cols; i++) {
            templateVector.add(new Float(templateMatrix.A[0][i]));
        }
        
        Listener listener = new Listener();
        
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("PTM");
            algorithm.addAlgorithmListener(listener);
            FloatMatrix matrix = experiment.getMatrix();
            int genes;
            if(!clusterGenes){
                matrix = matrix.transpose();
                genes = matrix.getRowDimension();
            }
            genes = experiment.getNumberOfGenes();
       
            algData.addMatrix("experiment", matrix);
  
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(algData);
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
            
            FloatMatrix rValuesMatrix = result.getMatrix("rValuesMatrix");
            FloatMatrix pValuesMatrix = result.getMatrix("pValuesMatrix");
            
            pValues = new float[pValuesMatrix.getRowDimension()];
            rValues = new float[rValuesMatrix.getRowDimension()];
            
            for (int i = 0; i < pValues.length; i++) {
                pValues[i] = pValuesMatrix.A[i][0];
                rValues[i] = rValuesMatrix.A[i][0];
            }
            
            auxTitles = new String[2];
            auxTitles[0] = "R values";
            auxTitles[1] = "p Values";
            
            auxData = new Object[pValues.length][2];
            
            for (int i = 0; i < auxData.length; i++) {
                auxData[i][0] = new Float(rValues[i]);
                auxData[i][1] = new Float(pValues[i]);
            }
            
            GeneralInfo info = new GeneralInfo();
            info.clusters = k;
            info.time = time;
            int function = params.getInt("distance-function");
            info.function = framework.getDistanceMenu().getFunctionName(function);
            info.isR = params.getBoolean("useR");
            info.isAbsolute = params.getBoolean("use-absolute");
            info.threshold = params.getFloat("threshold");
            info.hcl = params.getBoolean("hierarchical-tree");
            info.hcl_genes = params.getBoolean("calculate-genes");
            info.hcl_samples = params.getBoolean("calculate-experiments");
            if(info.hcl)
                info.hcl_method = params.getInt("method-linkage") ;
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
            root = new DefaultMutableTreeNode("PTM - genes");
        else
            root = new DefaultMutableTreeNode("PTM - samples");
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
        addStatsTables(root);
        addClusterInfo(root);
        addGeneralInfo(root, info);
    }
    
    /**
     * Adds nodes to display clusters data.
     */
    private void addExpressionImages(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer;
        
        if(clusterGenes){
            expViewer = new PTMExperimentViewer(this.experiment, this.clusters, this.templateVector, auxTitles, auxData);
            for (int i=0; i<this.clusters.length; i++) {
                if (i < this.clusters.length - 1) {
                    node.add(new DefaultMutableTreeNode(new LeafInfo("Matched Genes ", expViewer, new Integer(i))));
                } else if (i == this.clusters.length - 1) {
                    node.add(new DefaultMutableTreeNode(new LeafInfo("Unmatched Genes ", expViewer, new Integer(i))));
                }
            }
        }
        else {
            
            expViewer = new PTMExperimentClusterViewer(this.experiment, this.clusters, "Template", this.templateVector, auxTitles, auxData);
            for (int i=0; i<this.clusters.length; i++) {
                if (i < this.clusters.length - 1) {
                    node.add(new DefaultMutableTreeNode(new LeafInfo("Matched Experiments ", expViewer, new Integer(i))));
                } else if (i == this.clusters.length - 1) {
                    node.add(new DefaultMutableTreeNode(new LeafInfo("Unmatched Experiments ", expViewer, new Integer(i))));
                }
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
        int n = nodeList.getSize();
        int [][] clusters = null;
        
        if (!drawSigTreesOnly) {
            if(!this.clusterGenes){
                clusters = new int[n][];
                for (int i=0; i<clusters.length; i++) {
                    clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
                }
                if(info.hcl_samples)
                    clusters = getOrderedIndices(nodeList, clusters, info.hcl_genes);
            }
            for (int i=0; i<n; i++) {
                if(this.clusterGenes){
                    if(i == 0)
                        node.add(new DefaultMutableTreeNode(new LeafInfo("Matched Genes", createHCLViewer(nodeList.getNode(i), info, null))));
                    else
                        node.add(new DefaultMutableTreeNode(new LeafInfo("Unmatched Genes", createHCLViewer(nodeList.getNode(i), info, null))));
                }
                else{
                    if(i == 0)
                        node.add(new DefaultMutableTreeNode(new LeafInfo("Matched Experiments", createHCLViewer(nodeList.getNode(i), info, clusters), new Integer(i))));
                    else
                        node.add(new DefaultMutableTreeNode(new LeafInfo("UnMatched Experiments", createHCLViewer(nodeList.getNode(i), info, clusters), new Integer(i))));
                }
            }
        
        } else { // if (drawSigTreesOnly)
            if(!this.clusterGenes){
                //clusters = new int[n][];
                clusters = new int[1][];
                for (int i=0; i<clusters.length; i++) {
                    clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
                }
                if(info.hcl_samples)
                    clusters = getOrderedIndices(nodeList, clusters, info.hcl_genes);
            }
            for (int i=0; i<n; i++) {
                if(this.clusterGenes){
                    if(i == 0)
                        node.add(new DefaultMutableTreeNode(new LeafInfo("Matched Genes", createHCLViewer(nodeList.getNode(i), info, null))));                    
                }
                else{
                    if(i == 0)
                        node.add(new DefaultMutableTreeNode(new LeafInfo("Matched Experiments", createHCLViewer(nodeList.getNode(i), info, clusters), new Integer(i))));                    
                }
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
     * Adds node with cluster information.
     */
    private void addClusterInfo(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Cluster Information");
        if(this.clusterGenes)
            node.add(new DefaultMutableTreeNode(new LeafInfo("Matching Results (#,%)", new PTMInfoViewer(this.clusters, this.experiment.getNumberOfGenes()))));
        else
            node.add(new DefaultMutableTreeNode(new LeafInfo("Matching Results (#,%)", new PTMInfoViewer(this.clusters, this.experiment.getNumberOfSamples(), this.clusterGenes))));
        
        root.add(node);
    }
    
    
    /**
     * Adds nodes to display centroid charts.
     */
    private void addCentroidViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        
        PTMCentroidViewer centroidViewer;
        PTMExperimentCentroidViewer expCentroidViewer;
        if(clusterGenes){
            centroidViewer = new PTMCentroidViewer(this.experiment, clusters, templateVector, auxTitles, auxData);
            centroidViewer.setMeans(this.means.A);
            centroidViewer.setVariances(this.variances.A);
            for (int i=0; i<this.clusters.length; i++) {
                if(i == 0){
                    centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Matched Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                    expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Matched Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
                }
                else{
                    centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Unmatched Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                    expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Unmatched Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
                    
                }
            }
            
            PTMCentroidsViewer centroidsViewer = new PTMCentroidsViewer(this.experiment, clusters, templateVector, auxTitles, auxData);
            centroidsViewer.setMeans(this.means.A);
            centroidsViewer.setVariances(this.variances.A);
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        }
        else{
            expCentroidViewer = new PTMExperimentCentroidViewer(this.experiment, clusters, templateVector, auxTitles, auxData);
            float [][] codes = getCodes(this.templateVector);
            expCentroidViewer.setMeans(this.means.A);
            expCentroidViewer.setVariances(this.variances.A);
            // expCentroidViewer.setCodes(codes);
            //  expCentroidViewer.setDrawCodes(true);
            
            for (int i=0; i<this.clusters.length; i++) {
                if(i == 0){
                    centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Matched Experiments ", expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                    expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Matched Experiments ", expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
                }
                else {
                    centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Unmatched Experiments ", expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                    expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Unmatched Experiments ", expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
                }
            }
            PTMExperimentCentroidsViewer expCentroidsViewer = new PTMExperimentCentroidsViewer(this.experiment, clusters, templateVector, auxTitles, auxData);
            expCentroidsViewer.setMeans(this.means.A);
            expCentroidsViewer.setVariances(this.variances.A);
            // expCentroidsViewer.setCodes(codes);
            // expCentroidsViewer.setDrawCodes(true);
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", expCentroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", expCentroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        }
        root.add(centroidNode);
        root.add(expressionNode);
    }
    
    private void addStatsTables(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode tablesNode = new DefaultMutableTreeNode("Table views");
        if (clusterGenes) {
            IViewer tabViewer = new ClusterTableViewer(this.experiment, this.clusters, this.data, this.auxTitles, this.auxData);
            for (int i=0; i<this.clusters.length; i++) {
                if (i < this.clusters.length - 1) {
                    tablesNode.add(new DefaultMutableTreeNode(new LeafInfo("Matched Genes ", tabViewer, new Integer(i))));
                } else if (i == this.clusters.length - 1) {
                    tablesNode.add(new DefaultMutableTreeNode(new LeafInfo("Unmatched Genes ", tabViewer, new Integer(i))));
                }
            }
            //IViewer nonSigTableViewer = new PTMGeneStatsTableViewer(this.experiment, this.clusters, this.data, this.auxTitles, this.auxData, false);
            //tablesNode.add(new DefaultMutableTreeNode(new LeafInfo("Matched Genes", sigTableViewer)));
            //tablesNode.add(new DefaultMutableTreeNode(new LeafInfo("Unmatched Genes", nonSigTableViewer)));
        } else {
            IViewer tabViewer = new ExperimentClusterTableViewer(this.experiment, this.clusters, this.data, this.auxTitles, this.auxData);
            for (int i=0; i<this.clusters.length; i++) {
                if (i < this.clusters.length - 1) {
                    tablesNode.add(new DefaultMutableTreeNode(new LeafInfo("Matched Experiments ", tabViewer, new Integer(i))));
                } else if (i == this.clusters.length - 1) {
                    tablesNode.add(new DefaultMutableTreeNode(new LeafInfo("Unmatched Experiments ", tabViewer, new Integer(i))));
                }
            }
        }
        /*else {
            IViewer sigTableViewer = new PTMExpStatsTableViewer(this.experiment, this.clusters, this.data, this.auxTitles, this.auxData, true);
            IViewer nonSigTableViewer = new PTMExpStatsTableViewer(this.experiment, this.clusters, this.data, this.auxTitles, this.auxData, false);
            tablesNode.add(new DefaultMutableTreeNode(new LeafInfo("Matched experiments", sigTableViewer)));
            tablesNode.add(new DefaultMutableTreeNode(new LeafInfo("Unmatched experiments", nonSigTableViewer)));
        }
         */
        
        root.add(tablesNode);
    }
    
    /**
     * Gets template in form for viewer codes
     */
    public float[][] getCodes(Vector template){
        int length = template.size();
        float [][] codes = new float[2][length];
        for(int i = 0; i < codes.length; i++){
            for(int j = 0; j < codes[i].length; j++){
                codes[i][j] = ((Float)(template.elementAt(i))).intValue();
            }
        }
        return codes;
    }
    
    /**
     * Adds node with general iformation.
     */
    private void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        // node.add(new DefaultMutableTreeNode("Clusters: "+String.valueOf(info.clusters)));
        node.add(new DefaultMutableTreeNode("Absolute: "+String.valueOf(info.isAbsolute)));
        if (info.isR) {
            node.add(new DefaultMutableTreeNode("Threshold Pearson R: "+String.valueOf(info.threshold)));
        } else {
            node.add(new DefaultMutableTreeNode("Threshold prob. of R: "+String.valueOf(info.threshold)));
            
        }
        // node.add(new DefaultMutableTreeNode("Converged: "+String.valueOf(info.converged)));
        // node.add(new DefaultMutableTreeNode("Iterations: "+String.valueOf(info.iterations)));
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
        public long time;
        public String function;
        public boolean isAbsolute;
        public boolean isR;
        public float threshold;
        
        private boolean hcl;
        private int hcl_method;
        private boolean hcl_genes;
        private boolean hcl_samples;
        
        public String getMethodName() {
            return hcl ? HCLGUI.GeneralInfo.getMethodName(hcl_method) : "no linkage";
        }
        
    }
    
}
