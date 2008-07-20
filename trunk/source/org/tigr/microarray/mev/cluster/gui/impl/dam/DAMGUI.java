/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.cluster.gui.impl.dam;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
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
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Logger;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.util.FloatMatrix;

public class DAMGUI implements IClusterGUI {
    
    private Experiment experiment;
    private Experiment unusedExperiment;
    private Experiment usedExperiment;
    
    private Algorithm algorithm;
    private Progress progress;
    private IFramework framework;
    private float minDist;
    private float maxDist;
    private int num_genes;
    private int mode;
    
    public static int A0 = 0;
    public static int A1 = 1;
    public static int A2 = 2;
    public static int A3 = 3;
    
    private FloatMatrix matrix3D;
    private FloatMatrix matrixS;
    private Logger logger;
    
    private int algorithmSelection=0;
    private int classificationSelection=0;
    private boolean isPDA;
    private boolean preSelectGenes;
    private int numberOfClasses;
    private int kValue;
    private double alpha;
    
    private IData iData;
    
    DAMClassificationEditor damClassEditor;
    
    Vector[] classificationVector;
    int[] trainingIndices;  // array for experiments indices that are selected into the classes
    int[] testIndices;  // array for experiments indices that are selected into the classes
    int[] classes;     // array for class numbers that contain experiments
    
    int[] columns;
    int[] rows;
    int[] usedGeneIndices;
    int[] unusedGeneIndices;
    
    boolean classifyGenes = false;
    
    private int[][] clusters;
    private int[][] geneClusters;
    
    private FloatMatrix means, means_used, means_unused;
    private FloatMatrix variances, variances_used, variances_unused;
    
    /**
     * Initialize the algorithm's parameters and execute it.
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        this.framework = framework;
        this.experiment = framework.getData().getExperiment();
        this.iData = framework.getData();
        
        Listener listener = new Listener();
        
        try {
            
            DAMInitDialog damInitDialog = new DAMInitDialog(framework.getFrame(), true);
            
            if (damInitDialog.showModal() != JOptionPane.OK_OPTION) {
                return null;
            }
            
            classifyGenes = damInitDialog.isEvaluateGenesSelected();
            algorithmSelection = damInitDialog.getAssessmentSelection();
            isPDA = damInitDialog.isPDASelected();
            numberOfClasses = damInitDialog.getNumClasses();
            kValue = damInitDialog.getKValue();
            alpha = damInitDialog.getAlphaValue();
            
            preSelectGenes = !(damInitDialog.getSkipGeneSelectionValue());
            
            
            DAMClassificationEditor damClassEditor = new DAMClassificationEditor(framework, classifyGenes, numberOfClasses);
            damClassEditor.setVisible(true);
            
            while (!damClassEditor.isNextPressed()) {
                if (damClassEditor.isCancelPressed()) {
                    return null;
                } else {
                    continue;
                }
            }
            
            AlgorithmData data = new AlgorithmData();
            
            boolean useGenes = damInitDialog.isEvaluateGenesSelected();
            if (useGenes) {
                mode = 1;
                data.addParam("dam-mode", "1");
            } else {
                mode = 3;
                data.addParam("dam-mode", "3");
            }
            
            classificationVector = damClassEditor.getClassification();
            trainingIndices = new int[classificationVector[0].size()];
            classes = new int[classificationVector[1].size()];
            testIndices = new int[classificationVector[2].size()];
            
            for (int i = 0; i < trainingIndices.length; i++) {
                trainingIndices[i] = ((Integer)(classificationVector[0].get(i))).intValue();
                classes[i] = ((Integer)(classificationVector[1].get(i))).intValue();
            }
            
            for (int i = 0; i < testIndices.length; i++) {
                testIndices[i] = ((Integer)(classificationVector[2].get(i))).intValue();
            }
            
            algorithm = framework.getAlgorithmFactory().getAlgorithm("DAM");
            algorithm.addAlgorithmListener(listener);
            
            logger = new Logger(framework.getFrame(), "DAM Log Window", listener);
            logger.show();
            logger.append("Starting DAM calculation\n");
            
            FloatMatrix Cov;
            Experiment experiment = framework.getData().getExperiment();
            
            if(classifyGenes) {
            	//Problem here: if the program has a gene cluster (maybe also if it
            	//has an experiment cluster) the matrix returned is null.  
                FloatMatrix temp;
                temp = (experiment.getMatrix()).transpose();
//                System.out.println("floatmatrix size: " + temp.m + ", " + temp.n);
            	data.addMatrix("experiment", temp);
                
            } else {
                data.addMatrix("experiment", experiment.getMatrix());
            }
            
            data.addParam("distance-factor", String.valueOf(1.0f));
            IDistanceMenu menu = framework.getDistanceMenu();
            data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
            
            data.addParam("algorithmSelection", String.valueOf(algorithmSelection));
            data.addParam("isPDA", String.valueOf(isPDA));
            data.addParam("preSelectGenes", String.valueOf(preSelectGenes));
            data.addParam("numberOfClasses", String.valueOf(numberOfClasses));
            data.addParam("kValue", String.valueOf(kValue));
            data.addParam("alpha", String.valueOf(alpha));
            
            data.addIntArray("trainingIndices", trainingIndices);
            data.addIntArray("classes", classes);
            data.addIntArray("testIndices", testIndices);
            
            int function = menu.getDistanceFunction();
            if (function == Algorithm.DEFAULT) {
                function = Algorithm.COVARIANCE;
            }
            data.addParam("distance-function", String.valueOf(function));
            data.addParam("dam-mode", String.valueOf(mode));
            
            AlgorithmData result = null;
            DefaultMutableTreeNode node = null;
            long start = System.currentTimeMillis();
            switch (mode) {
                case 1: // Spots
                    data.addParam("distance-function", String.valueOf(function));
                    result = algorithm.execute(data);
                    matrixS = result.getMatrix("S");
                    matrix3D = result.getMatrix("matrix3D");
                    usedGeneIndices = result.getIntArray("usedGeneIndices");
                    unusedGeneIndices = result.getIntArray("unusedGeneIndices");
                    node = new DefaultMutableTreeNode("DAM - genes");
                    break;
                case 3: // Experiments
                    result = algorithm.execute(data);
                    matrixS = result.getMatrix("S");
                    matrix3D = result.getMatrix("matrix3D");
                    usedGeneIndices = result.getIntArray("usedGeneIndices");
                    unusedGeneIndices = result.getIntArray("unusedGeneIndices");
                    
/*
             if (preSelectGenes) {
                 System.out.println("DAMGUI.java: usedGeneIndices size: " + usedGeneIndices.length);
                 for(int i=0; i< usedGeneIndices.length; i++) {
                     System.out.print(usedGeneIndices[i] + ", ");
                 }
                 System.out.println(" ");
                 System.out.println(" ");
                 System.out.println("DAMGUI.java: unusedGeneIndices size: " + unusedGeneIndices.length);
                 for(int i=0; i< unusedGeneIndices.length; i++) {
                     System.out.print(unusedGeneIndices[i] + ", ");
                 }
                 System.out.println(" ");
                 System.out.println(" ");
             }
 */
                    
                    node = new DefaultMutableTreeNode("DAM - samples");
                    break;
                default:
                    break;
            }
            
            Cluster result_cluster = result.getCluster("cluster");
            NodeList nodeList = result_cluster.getNodeList();
            
            int k = numberOfClasses*3;
            this.clusters = new int[k][];
            
  //          System.out.println(" ");
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
                
            }
            
            Cluster gene_cluster = result.getCluster("geneCluster");
            nodeList = gene_cluster.getNodeList();
            
            this.geneClusters = new int[2][];
            
  //          System.out.println(" ");
            for (int i=0; i<2; i++) {
                geneClusters[i] = nodeList.getNode(i).getFeaturesIndexes();
                
            }
            
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            
            this.means_used = result.getMatrix("clusters_means_used");
            this.variances_used = result.getMatrix("clusters_variances_used");
            
            this.means_unused = result.getMatrix("clusters_means_unused");
            this.variances_unused = result.getMatrix("clusters_variances_unused");
            
            columns = new int[(experiment.getMatrix()).getColumnDimension()];
            for(int i=0; i< columns.length; i++) {
                columns[i] = i;
            }
            
            rows = new int[(experiment.getMatrix()).getRowDimension()];
            for(int i=0; i< rows.length; i++) {
                rows[i] = i;
            }
            
            
            if (classifyGenes) {
                usedExperiment = new Experiment((experiment.getMatrix()).getMatrix(rows, usedGeneIndices), usedGeneIndices, rows);
                unusedExperiment = new Experiment((experiment.getMatrix()).getMatrix(rows, unusedGeneIndices), unusedGeneIndices, rows);
            } else {
                usedExperiment = new Experiment((experiment.getMatrix()).getMatrix(usedGeneIndices, columns), columns, usedGeneIndices);
                unusedExperiment = new Experiment((experiment.getMatrix()).getMatrix(unusedGeneIndices, columns), columns, unusedGeneIndices);
            }
            
/*
                System.out.println("DAMGUI.java - means: " + means.getRowDimension() + " X " + means.getColumnDimension());
                System.out.println("DAMGUI.java - variances: " + variances.getRowDimension() + " X " + variances.getColumnDimension());
 
                System.out.println("DAMGUI.java - matrix3D " + matrix3D.getRowDimension() + " X " + matrix3D.getColumnDimension());
                for(int i=0; i< matrix3D.getRowDimension(); i++) {
                    for(int j=0; j< matrix3D.getColumnDimension(); j++) {
                        System.out.print(matrix3D.get(i, j) + ", ");
                    }
                    System.out.println(" ");
                }
 */
            
            logger.append("Creating the result viewers\n");
            long time = System.currentTimeMillis() - start;
            if(algorithmSelection == A3) //only classification
                addClassificationResultNodes(framework.getFrame(), node, time, menu.getFunctionName(function), experiment);
            else
                addValidationResultNodes(framework.getFrame(), node, time, menu.getFunctionName(function), experiment);
            
            
            return node;
        } finally {
            if (algorithm != null) {
                algorithm.removeAlgorithmListener(listener);
            }
            if (logger != null) {
                logger.dispose();
            }
        }
    }
    
    /**
     * Adds classification nodes into a result tree root.
     */
    private void addClassificationResultNodes(Frame frame, DefaultMutableTreeNode node, long time, String function, Experiment experiment) {
        addExpressionImages(node);
        addCentroidViews(node);
        addTableViews(node);
        add3DViewNode(frame, node, experiment);
        addClusterInfo(node);
        addGeneralInfoNode(node, time, function);
    }
    
    /**
     * Adds validation nodes into a result tree root.
     */
    private void addValidationResultNodes(Frame frame, DefaultMutableTreeNode node, long time, String function, Experiment experiment) {
        addExpressionImages(node);
        addCentroidViews(node);
        addTableViews(node);
        add3DViewNode(frame, node, experiment);
        addClusterInfo(node);
        addGeneralInfoNode(node, time, function);
    }
    
    
    private void addTableViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table views");
        IViewer tabViewer, tabViewer1;
        DefaultMutableTreeNode[] nodeArray = new DefaultMutableTreeNode[3];
        nodeArray[0] = new DefaultMutableTreeNode("Classifiers");
        
        if(this.algorithmSelection != A3) { //has validation
            nodeArray[1] = new DefaultMutableTreeNode("Initial Classification");
            nodeArray[2] = new DefaultMutableTreeNode("Validation Classification");
        } else {  //just classification
            nodeArray[1] = new DefaultMutableTreeNode("Classified");
            nodeArray[2] = new DefaultMutableTreeNode("Classifiers + Classified");
        }
        DefaultMutableTreeNode usedNode;
        DefaultMutableTreeNode unusedNode;
        
        if (classifyGenes) {
            tabViewer = new ClusterTableViewer(this.experiment, this.clusters, this.iData);
        } else {
            tabViewer = new ExperimentClusterTableViewer(this.experiment, this.clusters, this.iData);
        }
        
        for (int i =0; i < numberOfClasses; i++) {
            nodeArray[0].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1), tabViewer, new Integer(i))));
        }
        for (int i = numberOfClasses; i < 2*numberOfClasses; i++) {
            nodeArray[1].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - numberOfClasses), tabViewer, new Integer(i))));
        }
        for (int i =2*numberOfClasses; i < 3*numberOfClasses; i++) {
            nodeArray[2].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - 2*numberOfClasses), tabViewer, new Integer(i))));
        }
        
                
        for (int i = 0; i < nodeArray.length; i++) {
            node.add(nodeArray[i]);
        }
        
        
        if (classifyGenes) {
            tabViewer = new ClusterTableViewer(this.experiment, this.clusters, this.iData);
        } else {
            tabViewer = new ExperimentClusterTableViewer(this.experiment, this.clusters, this.iData);
        }
        
        
        if (preSelectGenes) {
            if (classifyGenes) {
                tabViewer1 = new ExperimentClusterTableViewer(this.experiment, this.geneClusters, this.iData);
                usedNode = new DefaultMutableTreeNode(new LeafInfo("Used Exp Table View", tabViewer1, new Integer(0)));
                unusedNode = new DefaultMutableTreeNode(new LeafInfo("Unused Exp Table View", tabViewer1, new Integer(1)));
            } else {
                tabViewer1 = new ClusterTableViewer(this.experiment, this.geneClusters, this.iData);
                usedNode = new DefaultMutableTreeNode(new LeafInfo("Used Gene Table View", tabViewer1, new Integer(0)));
                unusedNode = new DefaultMutableTreeNode(new LeafInfo("Unused Gene Table View", tabViewer1, new Integer(1)));
            }
            node.add(usedNode);
            node.add(unusedNode);
        }
        
        root.add(node);
    }
    
    
    
    /**
     * Adds node with 3D viewer.
     */
    private void add3DViewNode(Frame frame, DefaultMutableTreeNode node, Experiment experiment) {
        
        if (matrix3D == null || matrix3D.getColumnDimension() < 3) {
            return;
        }
        
        DAM3DViewer dam3DViewer;
        if(mode == 1) {
            dam3DViewer = new DAM3DViewer(frame, mode, matrix3D, experiment, true);
        }
        else {
            dam3DViewer = new DAM3DViewer(frame, mode, matrix3D, experiment, false);
        }
        
        node.add(new DefaultMutableTreeNode(new LeafInfo("Gene Component 3D view", dam3DViewer, dam3DViewer.getJPopupMenu())));
    }
    
    /**
     * Adds node with a general information.
     */
    private void addGeneralInfoNode(DefaultMutableTreeNode node, long time, String function) {
        DefaultMutableTreeNode gNode = new DefaultMutableTreeNode("General Information");
        if (matrixS != null) {
            gNode.add(new DefaultMutableTreeNode("Components: "+matrixS.getColumnDimension()));
        }
        gNode.add(new DefaultMutableTreeNode("Time: "+String.valueOf(time)+" ms"));
        gNode.add(new DefaultMutableTreeNode(function));
        node.add(gNode);
    }
    
    
    /**
     * Adds nodes to display centroid charts.
     */
    private void addCentroidViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        
        DAMCentroidViewer centroidViewer;
        ExperimentClusterCentroidViewer expCentroidViewer ;
        DAMCentroidViewer geneCentroidViewer;
        
        DefaultMutableTreeNode[] centroidNodeArray = new DefaultMutableTreeNode[3];
        DefaultMutableTreeNode[] expressionNodeArray = new DefaultMutableTreeNode[3];
        
        centroidNodeArray[0] = new DefaultMutableTreeNode("Classifiers");
        centroidNodeArray[1] = new DefaultMutableTreeNode("Classified");
        centroidNodeArray[2] = new DefaultMutableTreeNode("Classifiers + Classified");
        
        expressionNodeArray[0] = new DefaultMutableTreeNode("Classifiers");
        expressionNodeArray[1] = new DefaultMutableTreeNode("Classified");
        expressionNodeArray[2] = new DefaultMutableTreeNode("Classifiers + Classified");
        
        
        DefaultMutableTreeNode unusedGeneCentroidNode;
        DefaultMutableTreeNode unusedGeneExpressionNode;
        
        DefaultMutableTreeNode usedGeneCentroidNode;
        DefaultMutableTreeNode usedGeneExpressionNode;
        
        if(classifyGenes){
            centroidViewer = new DAMCentroidViewer(this.experiment, clusters);
            centroidViewer.setMeans(this.means.A);
            centroidViewer.setVariances(this.variances.A);
            
            for (int i =0; i < numberOfClasses; i++) {
                centroidNodeArray[0].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNodeArray[0].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            for (int i = numberOfClasses; i < 2*numberOfClasses; i++) {
                centroidNodeArray[1].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - numberOfClasses), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNodeArray[1].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - numberOfClasses), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            for (int i =2*numberOfClasses; i < 3*numberOfClasses; i++) {
                centroidNodeArray[2].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - 2*numberOfClasses), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNodeArray[2].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 -2*numberOfClasses), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            
            DAMCentroidsViewer centroidsViewer = new DAMCentroidsViewer(this.experiment, clusters);
            centroidsViewer.setMeans(this.means.A);
            centroidsViewer.setVariances(this.variances.A);
            
            
            for (int i = 0; i < centroidNodeArray.length; i++) {
                centroidNode.add(centroidNodeArray[i]);
                expressionNode.add(expressionNodeArray[i]);
            }
            
            expCentroidViewer = new DAMExperimentCentroidViewer(this.experiment, geneClusters);
            expCentroidViewer.setMeans(this.means_used.A);
            expCentroidViewer.setVariances(this.variances_used.A);
            
            if (preSelectGenes) {
                usedGeneCentroidNode = new DefaultMutableTreeNode(new LeafInfo("Used Exp Centroid Graph", expCentroidViewer, new CentroidUserObject(0, CentroidUserObject.VARIANCES_MODE)));
                usedGeneExpressionNode = new DefaultMutableTreeNode(new LeafInfo("Used Exp Expression Graph ", expCentroidViewer, new CentroidUserObject(0, CentroidUserObject.VALUES_MODE)));
                
                unusedGeneCentroidNode = new DefaultMutableTreeNode(new LeafInfo("Unused Exp Centroid Graph", expCentroidViewer, new CentroidUserObject(1, CentroidUserObject.VARIANCES_MODE)));
                unusedGeneExpressionNode = new DefaultMutableTreeNode(new LeafInfo("Unused Exp Expression Graph ", expCentroidViewer, new CentroidUserObject(1, CentroidUserObject.VALUES_MODE)));
                
                centroidNode.add(usedGeneCentroidNode);
                expressionNode.add(usedGeneExpressionNode);
                
                centroidNode.add(unusedGeneCentroidNode);
                expressionNode.add(unusedGeneExpressionNode);
            }
            
        }
        else{
            expCentroidViewer = new DAMExperimentCentroidViewer(this.experiment, clusters);
            
            expCentroidViewer.setMeans(this.means.A);
            expCentroidViewer.setVariances(this.variances.A);
            
            for (int i =0; i < numberOfClasses; i++) {
                centroidNodeArray[0].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNodeArray[0].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            for (int i = numberOfClasses; i < 2*numberOfClasses; i++) {
                centroidNodeArray[1].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - numberOfClasses), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNodeArray[1].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - numberOfClasses), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            for (int i =2*numberOfClasses; i < 3*numberOfClasses; i++) {
                centroidNodeArray[2].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - 2*numberOfClasses), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNodeArray[2].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 -2*numberOfClasses), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            
            DAMExperimentCentroidsViewer expCentroidsViewer = new DAMExperimentCentroidsViewer(this.experiment, clusters);
            
            expCentroidsViewer.setMeans(this.means.A);
            expCentroidsViewer.setVariances(this.variances.A);
            
            for (int i = 0; i < centroidNodeArray.length; i++) {
                centroidNode.add(centroidNodeArray[i]);
                expressionNode.add(expressionNodeArray[i]);
            }
            
            geneCentroidViewer = new DAMCentroidViewer(this.experiment, geneClusters);
            geneCentroidViewer.setMeans(this.means_used.A);
            geneCentroidViewer.setVariances(this.variances_used.A);
            
            if (preSelectGenes) {
                usedGeneCentroidNode = new DefaultMutableTreeNode(new LeafInfo("Used Gene Centroid Graph", geneCentroidViewer, new CentroidUserObject(0, CentroidUserObject.VARIANCES_MODE)));
                usedGeneExpressionNode = new DefaultMutableTreeNode(new LeafInfo("Used Gene Expression Graph ", geneCentroidViewer, new CentroidUserObject(0, CentroidUserObject.VALUES_MODE)));
                
                unusedGeneCentroidNode = new DefaultMutableTreeNode(new LeafInfo("Unused Gene Centroid Graph", geneCentroidViewer, new CentroidUserObject(1, CentroidUserObject.VARIANCES_MODE)));
                unusedGeneExpressionNode = new DefaultMutableTreeNode(new LeafInfo("Unused Gene Expression Graph ", geneCentroidViewer, new CentroidUserObject(1, CentroidUserObject.VALUES_MODE)));
                
                centroidNode.add(usedGeneCentroidNode);
                expressionNode.add(usedGeneExpressionNode);
                
                centroidNode.add(unusedGeneCentroidNode);
                expressionNode.add(unusedGeneExpressionNode);
            }
            
        }
        
        root.add(centroidNode);
        root.add(expressionNode);
    }
    
    
    /**
     * Adds node with cluster information.
     */
    
    private void addClusterInfo(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Cluster Information");
        if(this.classifyGenes)
            node.add(new DefaultMutableTreeNode(new LeafInfo("Genes in Classes (#,%)", new DAMInfoViewer(this.clusters, this.experiment.getNumberOfGenes(), numberOfClasses, usedGeneIndices.length, unusedGeneIndices.length, alpha, algorithmSelection, isPDA, preSelectGenes))));
        else
            node.add(new DefaultMutableTreeNode(new LeafInfo("Samples in Classes (#,%)", new DAMInfoViewer(this.clusters, this.experiment.getNumberOfSamples(), false, numberOfClasses, usedGeneIndices.length, unusedGeneIndices.length, alpha, algorithmSelection, isPDA, preSelectGenes))));
        
        root.add(node);
    }
    
    
    /**
     * Adds nodes to display clusters data.
     */
    private void addExpressionImages(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer, unusedGeneExpViewer, usedGeneExpViewer;
        DefaultMutableTreeNode[] nodeArray = new DefaultMutableTreeNode[3];
        
        nodeArray[0] = new DefaultMutableTreeNode("Classifiers");
        nodeArray[1] = new DefaultMutableTreeNode("Classified");
        nodeArray[2] = new DefaultMutableTreeNode("Classifiers + Classified");
        
        if (classifyGenes) {
            expViewer = new DAMExperimentViewer(this.experiment, this.clusters);
        } else {
            expViewer = new DAMExperimentClusterViewer(this.experiment, this.clusters);
        }
        
        for (int i =0; i < numberOfClasses; i++) {
            nodeArray[0].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1), expViewer, new Integer(i))));
        }
        for (int i = numberOfClasses; i < 2*numberOfClasses; i++) {
            nodeArray[1].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - numberOfClasses), expViewer, new Integer(i))));
        }
        for (int i =2*numberOfClasses; i < 3*numberOfClasses; i++) {
            nodeArray[2].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - 2*numberOfClasses), expViewer, new Integer(i))));
        }
        
        for (int i = 0; i < nodeArray.length; i++) {
            node.add(nodeArray[i]);
        }
        
        unusedGeneExpViewer = new DAMExperimentViewer(unusedExperiment, null);
        usedGeneExpViewer = new DAMExperimentViewer(usedExperiment, null);
        
        if (preSelectGenes) {
            if (classifyGenes) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Used Exp Expression", usedGeneExpViewer)));
                node.add(new DefaultMutableTreeNode(new LeafInfo("Unused Exp Expression", unusedGeneExpViewer)));
            } else {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Used Gene Expression", usedGeneExpViewer)));
                node.add(new DefaultMutableTreeNode(new LeafInfo("Unused Gene Expression", unusedGeneExpViewer)));
            }
        }
        
        root.add(node);
        
    }
    
    
    
    /**
     * The class to listen to dialog and algorithm events.
     */
    private class Listener extends DialogListener implements AlgorithmListener {
        
        public void valueChanged(AlgorithmEvent event) {
            logger.append(event.getDescription());
        }
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("cancel-command")) {
                algorithm.abort();
                logger.dispose();
            }
        }
        
        public void windowClosing(WindowEvent e) {
            algorithm.abort();
            logger.dispose();
        }
    }
    
}
