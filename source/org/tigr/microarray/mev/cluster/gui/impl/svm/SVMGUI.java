/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SVMGUI.java,v $
 * $Revision: 1.9 $
 * $Date: 2006-03-24 15:51:53 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;
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
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Logger;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Monitor;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLViewer;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;
import org.tigr.util.FloatMatrix;

public class SVMGUI implements IClusterGUI, IScriptGUI {
    
    // GUI
    protected Frame parentFrame;
    private IFramework framework;
    protected IDistanceMenu menu;
    protected Logger logger;
    protected Monitor monitor;
    protected Listener listener;
    
    // Calculation stuff
    protected IData experiment;
    protected Algorithm algorithm;
    protected SVMData data = new SVMData();
    protected GeneralInfo info = new GeneralInfo();
    protected Experiment experimentMap;
    
    //Training data members
    private File SVMFile;
    private float[] Weights;
    private FloatMatrix trainingMatrix;
    private FloatMatrix kernelMatrix;
    private int[] classes;
    
    public static int TRAIN_AND_CLASSIFY = 0;
    public static int TRAIN_ONLY = 1;
    public static int CLASSIFY_ONLY = 2;
    public static int ONE_OUT_VALIDATION = 3;
    
    
    private int SVMMode = 0;
    private boolean classifyGenes;
    private FloatMatrix discriminantMatrix;
    private boolean stop = false;
    
    private boolean scripting = false;
    
    // Generic protocol for both SVM Train and SVM classify algorithms
    /** Creates an SVM algorithm
     * @param factory algorithm factory
     * @throws AlgorithmException
     */
    protected void createAlgorithm( AlgorithmFactory factory ) throws AlgorithmException {
        listener = new Listener();
        this.algorithm = factory.getAlgorithm("SVM");
        this.algorithm.addAlgorithmListener(listener);
    }
    
    /** Binds common parameters.
     * @param data AlgorithmData to mainitain pased parameters
     */
    protected void bindParams( AlgorithmData data ) {
        if(!scripting) {
            FloatMatrix matrix = this.experiment.getExperiment().getMatrix();
            if(!this.data.classifyGenes)
                matrix = matrix.transpose();
            data.addMatrix("experiment", matrix);
        }        
       
        data.addParam("distance-factor", String.valueOf(1.0f)  );
        data.addParam("hcl-distance-absolute", String.valueOf( this.data.absoluteDistance ) );
        data.addParam("hcl-distance-function", String.valueOf( this.data.distanceFunction ) );
        
        data.addParam("constant", String.valueOf(this.data.constant));
        data.addParam("coefficient", String.valueOf( this.data.coefficient));
        data.addParam("power", String.valueOf( this.data.power ) );
    }
    
    
    /** Executes SVM and returns SVM result
     * @param framework Main IFramework
     * @throws AlgorithmException
     * @return Returns SVM result node
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        //get basic objects for data and framework
        this.parentFrame = framework.getFrame();
        this.experiment = framework.getData();
        this.experimentMap = experiment.getExperiment();
        this.menu = framework.getDistanceMenu();
        this.framework = framework;
        
        DefaultMutableTreeNode svmNode = null;  //Result Node
        
        try {
            //create an algorithm, assigns algorithm to this.algorithm
            createAlgorithm( framework.getAlgorithmFactory() );
            
            //Make a new data structure for binding data and params
            AlgorithmData data = new AlgorithmData();
            
            //Determine sample set (exp or genes) and SVM process
            // (train and classify, just train, or just classify), sets SVMMode
            if(!selectSVMProcedure())
                return null;
            
            if(SVMMode == TRAIN_AND_CLASSIFY){
                //Set get data into float, run init dialog, read svc file or use class. editor
                if (!initTrainingParams())
                    return null;
                //bind training parameters into data
                bindTrainingParams(data);
                //Time start and execution of training
                long start = System.currentTimeMillis();
                AlgorithmData trainingResult = this.algorithm.execute(data);
                long time = System.currentTimeMillis() - start;
                //build training result, attach to node
                getTrainingResults( trainingResult );
                if(this.classifyGenes)
                    svmNode = new DefaultMutableTreeNode("SVM - genes");
                else
                    svmNode = new DefaultMutableTreeNode("SVM - samples");
                svmNode.add( createTrainingGUIResult());  //add traing result viewer
                
                //classify
                createAlgorithm( framework.getAlgorithmFactory() );  //get a fresh SVM
                
                //classification parameters already set, weights and data and primary params for kernel
                bindClassificationParams(data);

                start = System.currentTimeMillis();
                AlgorithmData classificationResult = this.algorithm.execute(data);
                time += System.currentTimeMillis() - start;
                getClassificationResults( classificationResult );
                info.time = time;
                info.function = menu.getFunctionName( this.data.distanceFunction ); //SVMData 
                
                svmNode.add( createClassificationGUIResult() );   //add class. viewer
                svmNode.add( createSVMExpressionViews( classificationResult, classes));  //add expression image viewer
                if(this.data.calculateHCL)
                    svmNode.add(createHierarchicalTreeViews(classificationResult.getCluster("cluster"), classificationResult));
                createSVMCentroidViews(classificationResult, svmNode); //add centroid and expression graphs
                createInfoView(classificationResult, svmNode);
                addSVMParameterNode(svmNode);
            }
            
            else if(SVMMode == TRAIN_ONLY){
                if (!initTrainingParams())
                    return null;
                bindTrainingParams(data);
                long start = System.currentTimeMillis();
                AlgorithmData trainingResult = this.algorithm.execute(data);
                long time = System.currentTimeMillis() - start;
                getTrainingResults( trainingResult );
                if(this.classifyGenes)
                    svmNode = new DefaultMutableTreeNode("SVM - genes");
                else
                    svmNode = new DefaultMutableTreeNode("SVM - samples");
                svmNode.add( createTrainingGUIResult() );
                info.time = time;
                int function = this.data.distanceFunction;
                info.function = menu.getFunctionName( function );
                addSVMParameterNode(svmNode);
            }
            
            else if(SVMMode == CLASSIFY_ONLY){
                if (!initClassificationParams())
                    return null;
                bindClassificationParams(data);
                long start = System.currentTimeMillis();
                AlgorithmData classificationResult = this.algorithm.execute(data);
                long time = System.currentTimeMillis() - start;
                getClassificationResults( classificationResult );
                if(this.classifyGenes)
                    svmNode = new DefaultMutableTreeNode("SVM - genes");
                else
                    svmNode = new DefaultMutableTreeNode("SVM - samples");
                info.time = time;
                int function = this.data.distanceFunction;
                info.function = menu.getFunctionName( function );
                svmNode.add( createClassificationGUIResult() );
                svmNode.add( createViewers(classificationResult) );     //Experiment viewers based on pos/neg without prior knowledge of init. class.
                if(this.data.calculateHCL)
                    svmNode.add(createHierarchicalTreeViews(classificationResult.getCluster("cluster"), classificationResult));
                createSVMCentroidViews(classificationResult, svmNode);  //append centroid and exp. viewers
                createInfoView(classificationResult, svmNode);
                addSVMParameterNode(svmNode);
            }
            
            else if(SVMMode == ONE_OUT_VALIDATION){
                int iter;
                int n;
                int initClass;
                int numberOfNonNeutrals;
                FloatMatrix cumDiscriminantMatrix;
                int [] iterationScores;
                int [] elementScores;
                
                if (!initTrainingParams())
                    return null;
                
                if(this.classifyGenes)
                    n = this.experimentMap.getNumberOfGenes();
                else
                    n = this.experimentMap.getNumberOfSamples();
                
                cumDiscriminantMatrix = new FloatMatrix(n,2);
                numberOfNonNeutrals = getNumberOfNonNeutrals();
                iterationScores = new int[n];
                elementScores = new int[n];
                for(int i = 0; i < n; i++)
                    elementScores[i] = 0;
                
                if(this.classifyGenes)
                    svmNode = new DefaultMutableTreeNode("SVM Val. - genes");
                else
                    svmNode = new DefaultMutableTreeNode("SVM Val. - samples");
                
                for(iter = 0; iter < n; iter++){
                    initClass = classes[iter];
                    classes[iter] = 0;
                    
                    //bind training parameters into data
                    bindTrainingParams(data);
                    
                    //Time start and execution of training
                    long start = System.currentTimeMillis();
                    AlgorithmData trainingResult = this.algorithm.execute(data);
                    long time = System.currentTimeMillis() - start;
                    //build training result, attach to node
                    getTrainingResults( trainingResult );
                    
                    //    svmNode.add( createTrainingGUIResult());  //add traing result viewer
                    
                    //classify
                    createAlgorithm( framework.getAlgorithmFactory() );  //get a fresh SVM
                    
                    //classification parameters already set, weights and data and primary params for kernel
                    bindClassificationParams(data);
                    start = System.currentTimeMillis();
                    AlgorithmData classificationResult = this.algorithm.execute(data);
                    time += System.currentTimeMillis() - start;
                    getClassificationResults( classificationResult );
                    
                    accumulateResult(cumDiscriminantMatrix, iter); // get cumulative result
                    
                    classes[iter] = initClass;  //restore initial classification
                    
                    getNumberOfCorrectPlacements(iterationScores, elementScores, iter);
                }
                                
                DefaultMutableTreeNode root = new DefaultMutableTreeNode(new LeafInfo("SVM One-out Validation",
                new SVMOneOutViewer(experiment.getExperiment(), cumDiscriminantMatrix, this.data.classifyGenes, this.classes, elementScores, iterationScores, numberOfNonNeutrals)));
                svmNode.add(root);
                
                AlgorithmData cumulativeData = constructCumulativeResult(cumDiscriminantMatrix);                
                
                svmNode.add( createSVMExpressionViews( cumulativeData, classes));  //add expression image viewer
                if(this.data.calculateHCL){
                    try{
                        calculateHCL(cumulativeData, cumDiscriminantMatrix);
                        svmNode.add(createHierarchicalTreeViews(cumulativeData.getCluster("cluster"), cumulativeData));
                    }catch (Exception e){ }
                }
                createSVMCentroidViews(cumulativeData, svmNode); //add centroid and expression graphs
                createInfoView(cumulativeData, svmNode);
                addSVMParameterNode(svmNode);
                
            }
            
            return svmNode;
        } finally {
            if (algorithm != null) {
                algorithm.removeAlgorithmListener(listener);
            }
            if (monitor != null) monitor.dispose();
            if (logger != null) logger.dispose();
        }
    }
    
    
    
    public AlgorithmData getScriptParameters(IFramework framework) {
        //get basic objects for data and framework
        this.scripting = true;
        this.parentFrame = framework.getFrame();
        this.experiment = framework.getData();
        this.experimentMap = experiment.getExperiment();
        this.menu = framework.getDistanceMenu();
        this.framework = framework;
        
        //Make a new data structure for binding data and params
        AlgorithmData data = new AlgorithmData();
        
        //Determine sample set (exp or genes) and SVM process
        // (train and classify, just train, or just classify), sets SVMMode
        if(!selectSVMProcedure())
            return null;
        
        if(SVMMode == TRAIN_AND_CLASSIFY){
            //Set get data into float, run init dialog, read svc file or use class. editor
            if (!initTrainingParams())
                return null;
            //bind training parameters into data
            bindTrainingParams(data);
            //classification parameters already set, weights and data and primary params for kernel
            bindClassificationParams(data);
            
            data.addParam("mode", String.valueOf(TRAIN_AND_CLASSIFY));
            
            // alg name
            data.addParam("name", "SVM");
            
            // alg type
            if(classifyGenes)
                data.addParam("alg-type", "cluster-genes");
            else
                data.addParam("alg-type", "cluster-experiments");
            
            // output class
            data.addParam("output-class", "partition-output");
            
            //output nodes
            String [] outputNodes = new String[2];
            outputNodes[0] = "Positives";
            outputNodes[1] = "Negatives";
            
            data.addStringArray("output-nodes", outputNodes);
            
            return data;
        }
        
        else if(SVMMode == TRAIN_ONLY){
            if (!initTrainingParams())
                return null;
            bindTrainingParams(data);
            
            data.addParam("mode", String.valueOf(TRAIN_ONLY));
            
            // alg name
            data.addParam("name", "SVM");
            
            // alg type
            if(classifyGenes)
                data.addParam("alg-type", "cluster-genes");
            else
                data.addParam("alg-type", "cluster-experiments");
            
            // output class
            data.addParam("output-class", "partition-output");
            
            //output nodes
            String [] outputNodes = new String[1];
            outputNodes[0] = "Training Weights Output";
            
            data.addStringArray("output-nodes", outputNodes);
            
            return data;
        }
        
        else if(SVMMode == CLASSIFY_ONLY){
            if (!initClassificationParams())
                return null;
            bindClassificationParams(data);
            
            data.addParam("mode", String.valueOf(CLASSIFY_ONLY));
            
            // alg name
            data.addParam("name", "SVM");
            
            // alg type
            if(classifyGenes)
                data.addParam("alg-type", "cluster-genes");
            else
                data.addParam("alg-type", "cluster-experiments");
            
            // output class
            data.addParam("output-class", "partition-output");
            
            //output nodes
            String [] outputNodes = new String[2];
            outputNodes[0] = "Positives";
            outputNodes[1] = "Negatives";
            
            data.addStringArray("output-nodes", outputNodes);
            
            return data;
        }
        
        else if(SVMMode == ONE_OUT_VALIDATION){
            if (!initTrainingParams())
                return null;
            
            //bind training parameters into data
            bindTrainingParams(data);
            bindClassificationParams(data);
            
            data.addParam("mode", String.valueOf(ONE_OUT_VALIDATION));
            
            // alg name
            data.addParam("name", "SVM");
            
            // alg type
            if(classifyGenes)
                data.addParam("alg-type", "cluster-genes");
            else
                data.addParam("alg-type", "cluster-experiments");
            
            // output class
            data.addParam("output-class", "partition-output");
            
            //output nodes
            String [] outputNodes = new String[2];
            outputNodes[0] = "Positives";
            outputNodes[1] = "Negatives";
            
            data.addStringArray("output-nodes", outputNodes);
            
            return data;
        }
        return data;
    }
    
    public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, Experiment experiment) throws AlgorithmException {
        
        //get basic objects for data and framework
        this.SVMMode = algData.getParams().getInt("mode");
        
        //set fields
        rebuildSVMData(algData);
        
        this.parentFrame = framework.getFrame();
        this.experiment = framework.getData();
        this.experimentMap = experiment;
        this.menu = framework.getDistanceMenu();
        this.framework = framework;
        
        this.data.classifyGenes = algData.getParams().getBoolean("classify-genes");
        this.classifyGenes = this.data.classifyGenes;
        
        trainingMatrix = experiment.getMatrix();
        if(this.data.classifyGenes)
            algData.addMatrix("training", trainingMatrix);
        else
            algData.addMatrix("training", trainingMatrix.transpose());
        
        
        FloatMatrix matrix = experiment.getMatrix();
        if(!this.data.classifyGenes)
            matrix = matrix.transpose();
        algData.addMatrix("experiment", matrix);
        this.classes = algData.getIntArray("classes");
        
        DefaultMutableTreeNode svmNode = null;  //Result Node
        
        showLogger("SVM Log Window");
        
        try {
            //create an algorithm, assigns algorithm to this.algorithm
            createAlgorithm( framework.getAlgorithmFactory() );
            
            if(SVMMode == TRAIN_AND_CLASSIFY){
                bindParams(algData);
                algData.addParam("is-classify", String.valueOf(false));
                
                //Time start and execution of training
                long start = System.currentTimeMillis();
                AlgorithmData trainingResult = this.algorithm.execute(algData);
                long time = System.currentTimeMillis() - start;
                //build training result, attach to node
                getTrainingResults( trainingResult );
                if(this.classifyGenes)
                    svmNode = new DefaultMutableTreeNode("SVM - genes");
                else
                    svmNode = new DefaultMutableTreeNode("SVM - samples");
                svmNode.add( createTrainingGUIResult());  //add traing result viewer
                
                //classify
                createAlgorithm( framework.getAlgorithmFactory() );  //get a fresh SVM
                
                //classification parameters already set, weights and data and primary params for kernel
                bindClassificationParams(algData);
                algData.addParam("is-classify", String.valueOf(true));
                
                start = System.currentTimeMillis();
                AlgorithmData classificationResult = this.algorithm.execute(algData);
                time += System.currentTimeMillis() - start;
                getClassificationResults( classificationResult );
                info.time = time;
                
                info.function = framework.getDistanceMenu().getFunctionName( this.data.distanceFunction );
                
                svmNode.add( createClassificationGUIResult() );   //add class. viewer
                svmNode.add( createSVMExpressionViews( classificationResult, classes));  //add expression image viewer
                if(this.data.calculateHCL)
                    svmNode.add(createHierarchicalTreeViews(classificationResult.getCluster("cluster"), classificationResult));
                createSVMCentroidViews(classificationResult, svmNode); //add centroid and expression graphs
                createInfoView(classificationResult, svmNode);
                addSVMParameterNode(svmNode);
            }
            
            else if(SVMMode == TRAIN_ONLY){
              //  if (!initTrainingParams())
              //      return null;
                bindParams(algData);
                algData.addParam("is-classify", String.valueOf(false));
                
                long start = System.currentTimeMillis();
                AlgorithmData trainingResult = this.algorithm.execute(algData);
                long time = System.currentTimeMillis() - start;
                getTrainingResults( trainingResult );
                if(this.classifyGenes)
                    svmNode = new DefaultMutableTreeNode("SVM - genes");
                else
                    svmNode = new DefaultMutableTreeNode("SVM - samples");
                svmNode.add( createTrainingGUIResult() );
                info.time = time;
                int function = menu.getDistanceFunction();
                info.function = menu.getFunctionName( function );
                addSVMParameterNode(svmNode);
            }
            
            else if(SVMMode == CLASSIFY_ONLY){
              //  if (!initClassificationParams())
               //     return null;
                this.Weights = algData.getMatrix("weights").A[0];
                bindClassificationParams(algData);
                long start = System.currentTimeMillis();
                AlgorithmData classificationResult = this.algorithm.execute(algData);
                long time = System.currentTimeMillis() - start;
                getClassificationResults( classificationResult );
                if(this.classifyGenes)
                    svmNode = new DefaultMutableTreeNode("SVM - genes");
                else
                    svmNode = new DefaultMutableTreeNode("SVM - samples");
                info.time = time;
                int function = menu.getDistanceFunction();
                info.function = menu.getFunctionName( function );
                svmNode.add( createClassificationGUIResult() );
                svmNode.add( createViewers(classificationResult) );     //Experiment viewers based on pos/neg without prior knowledge of init. class.
                if(this.data.calculateHCL)
                    svmNode.add(createHierarchicalTreeViews(classificationResult.getCluster("cluster"), classificationResult));
                createSVMCentroidViews(classificationResult, svmNode);  //append centroid and exp. viewers
                createInfoView(classificationResult, svmNode);
                addSVMParameterNode(svmNode);
            }
            
            else if(SVMMode == ONE_OUT_VALIDATION){
                int iter;
                int n;
                int initClass;
                int numberOfNonNeutrals;
                FloatMatrix cumDiscriminantMatrix;
                int [] iterationScores;
                int [] elementScores;
                
                if (!initTrainingParams())
                    return null;
                
                if(this.classifyGenes)
                    n = this.experimentMap.getNumberOfGenes();
                else
                    n = this.experimentMap.getNumberOfSamples();
                
                cumDiscriminantMatrix = new FloatMatrix(n,2);
                numberOfNonNeutrals = getNumberOfNonNeutrals();
                iterationScores = new int[n];
                elementScores = new int[n];
                for(int i = 0; i < n; i++)
                    elementScores[i] = 0;
                
                if(this.classifyGenes)
                    svmNode = new DefaultMutableTreeNode("SVM Val. - genes");
                else
                    svmNode = new DefaultMutableTreeNode("SVM Val. - samples");
                
                for(iter = 0; iter < n; iter++){
                    initClass = classes[iter];
                    classes[iter] = 0;
                    
                    //bind training parameters into data
                    bindTrainingParams(algData);
                    
                    //Time start and execution of training
                    long start = System.currentTimeMillis();
                    AlgorithmData trainingResult = this.algorithm.execute(algData);
                    long time = System.currentTimeMillis() - start;
                    //build training result, attach to node
                    getTrainingResults( trainingResult );
                    
                    //    svmNode.add( createTrainingGUIResult());  //add traing result viewer
                    
                    //classify
                    createAlgorithm( framework.getAlgorithmFactory() );  //get a fresh SVM
                    
                    //classification parameters already set, weights and data and primary params for kernel
                    bindClassificationParams(algData);
                    start = System.currentTimeMillis();
                    AlgorithmData classificationResult = this.algorithm.execute(algData);
                    time += System.currentTimeMillis() - start;
                    getClassificationResults( classificationResult );
                    
                    accumulateResult(cumDiscriminantMatrix, iter); // get cumulative result
                    
                    classes[iter] = initClass;  //restore initial classification
                    
                    getNumberOfCorrectPlacements(iterationScores, elementScores, iter);
                }
                                     
                DefaultMutableTreeNode root = new DefaultMutableTreeNode(new LeafInfo("SVM One-out Validation",
                //new SVMOneOutViewer( framework, this.experiment, this.data, cumDiscriminantMatrix, info, this.data.classifyGenes, this.classes, elementScores, iterationScores, numberOfNonNeutrals)));                
                new SVMOneOutViewer(this.experiment.getExperiment(), cumDiscriminantMatrix, this.data.classifyGenes, this.classes, elementScores, iterationScores, numberOfNonNeutrals)));                
                svmNode.add(root);
                 
                AlgorithmData cumulativeData = constructCumulativeResult(cumDiscriminantMatrix);
                
                svmNode.add( createSVMExpressionViews( cumulativeData, classes));  //add expression image viewer
                if(this.data.calculateHCL){
                    try{
                        calculateHCL(cumulativeData, cumDiscriminantMatrix);
                        svmNode.add(createHierarchicalTreeViews(cumulativeData.getCluster("cluster"), cumulativeData));
                    }catch (Exception e){ }
                }
                createSVMCentroidViews(cumulativeData, svmNode); //add centroid and expression graphs
                createInfoView(cumulativeData, svmNode);
                addSVMParameterNode(svmNode);
                
            }
            
            return svmNode;
        } finally {
            if (algorithm != null) {
                algorithm.removeAlgorithmListener(listener);
            }
            if (monitor != null) monitor.dispose();
            if (logger != null) logger.dispose();
        }
    }
    
    private void rebuildSVMData(AlgorithmData algData) {
        AlgorithmParameters params = algData.getParams();
        
        this.data.constant = params.getFloat("constant");
        this.data.coefficient = params.getFloat("coefficient"); 
        this.data.power = params.getFloat("power");

        //skip training params if classification from file, params won't be in algData
        if(this.SVMMode != SVMGUI.CLASSIFY_ONLY) {
            this.data.diagonalFactor = params.getFloat("diagonal-factor");
            this.data.convergenceThreshold = params.getFloat("convergence-threshold"); 
            this.data.radial = params.getBoolean("radial");
            this.data.normalize = params.getBoolean("normalize");
            this.data.widthFactor = params.getFloat("width-factor");        
            this.data.constrainWeights = params.getBoolean("constrain-weights");
            this.data.positiveConstraint = params.getFloat("positive-constraint");
            this.data.negativeConstraint = params.getFloat("negative-constraint");
        }
        this.data.useEditor = params.getBoolean("used-classification-editor"); 
        if(!this.data.useEditor) { 
            String fileName = params.getString("classification-file-name");
            if(fileName != null)
                this.data.classificationFile = new File(fileName);
        }
        
        this.data.classifyGenes = params.getBoolean("classify-genes"); 
        this.data.calculateHCL = params.getBoolean("calculate-hcl");
        if(this.data.calculateHCL) {
            this.data.calcSampleHCL = params.getBoolean("calculate-samples-hcl");
            this.data.calcGeneHCL = params.getBoolean("calculate-genes-hcl");
            this.data.hclMethod = params.getInt("linkage-method");
        }
        this.data.distanceFunction = params.getInt("hcl-distance-function");
        this.data.absoluteDistance = params.getBoolean("hcl-absolute-distance");
    }
    
    
    private AlgorithmData constructCumulativeResult(FloatMatrix discriminantMatrix){
        AlgorithmData result = new AlgorithmData();
        result.addMatrix("experiment", this.experimentMap.getMatrix());
        result.addMatrix("discriminant", discriminantMatrix);
        result.addIntArray("positives", getPositives(discriminantMatrix));
        result.addIntArray("negatives", getNegatives(discriminantMatrix));
        FloatMatrix means = getMeans(discriminantMatrix);
        result.addMatrix("means", means);
        result.addMatrix("variances", getVariance(discriminantMatrix, means));
        return result;
    }
    
    private void getNumberOfCorrectPlacements(int [] placements, int [] elementAccuracy, int index){
        int clazz;
        int n = 0;
        
        for(int i = 0; i < discriminantMatrix.getRowDimension(); i++){
            clazz = (int)discriminantMatrix.get(i,0);
            if(clazz == classes[i]){
                elementAccuracy[i]++;
                n++;
            }
        }
        placements[index] = n;
    }
    
    
    /**
     * Selection of working on genes or experiments, and SVMMode, train, classify, or both.
     * @return Returns true if SVM is classifying genes
     */
    private boolean selectSVMProcedure(){
        SVMProcessInitDialog dialog = new SVMProcessInitDialog( this.parentFrame, true );
        if (dialog.showModal() != JOptionPane.OK_OPTION)
            return false;
        else{
            classifyGenes = dialog.isEvaluateGenesSelected();
            this.data.classifyGenes = classifyGenes;
            SVMMode = dialog.getSVMProcessSelection();
            this.data.calculateHCL = dialog.getHCLSelection();
            
            if(this.data.calculateHCL){
                IDistanceMenu menu = framework.getDistanceMenu();
                
                HCLInitDialog hcl_dialog = new HCLInitDialog(framework.getFrame(), menu.getFunctionName(menu.getDistanceFunction()), menu.isAbsoluteDistance(), true);
                
                if (hcl_dialog.showModal() == JOptionPane.OK_OPTION) {
                    this.data.hclMethod = hcl_dialog.getMethod();
                    this.data.calcSampleHCL = hcl_dialog.isClusterExperiments();
                    this.data.calcGeneHCL = hcl_dialog.isClusterGenes();
                    this.data.distanceFunction = hcl_dialog.getDistanceMetric();
                    this.data.absoluteDistance = hcl_dialog.getAbsoluteSelection();
                }
                else
                    this.data.calculateHCL = false;  //changed mind about HCL
            }
            return true;
        }
    }
    
    
    /***************************************************************************************************
     *
     *    Training Init code.
     */
    
    /**
     * Parameters initialization.
     * @throws AlgorithmException
     * @return Returns true if successful
     */
    protected boolean initTrainingParams() {//throws AlgorithmException {
        trainingMatrix =  experiment.getExperiment().getMatrix();
        kernelMatrix = null;
        //data.distanceFunction =  menu.getDistanceFunction();
        //if (data.distanceFunction == Algorithm.DEFAULT)
       //     data.distanceFunction  = Algorithm.EUCLIDEAN; //this applies to HCL on SVM result
        //SVM kernal uses dot product on normalized vectors
        
        SVMInitDialog dialog = new SVMInitDialog( this.parentFrame, this.data  );
        
        if (dialog.showModal() != JOptionPane.OK_OPTION)
            return false;
        this.data = dialog.getData();
        if(classifyGenes)
            classes = new int[trainingMatrix.getRowDimension()];
        else{
            classes = new int[trainingMatrix.getColumnDimension()];
        }
        
        if(!scripting)
            showLogger("SVM Log Window");
        if(this.data.useEditor){
            SVMClassificationEditor editor = new SVMClassificationEditor(framework, this.data.classifyGenes);
    
            if(!scripting)
                logger.append("Using Classification Editor\n");//            editor.show();
            
            editor.setVisible(true);
            //editor.show();
            //  while(editor.isVisible()){
            // wait until done
            //  }
            if(editor.formCanceled()){
                System.out.println("form canceled!!!");
                return false;
            }
            classes = editor.getClassification();
        }
        else{
            logger.append("Reading classification file\n");
//            if (!readSVCFile())
//                return false;
            loadTableSVC();
        }
        return true;
    }
    
    
    
    
    
    
	/**
	 * Loads file based assignments
	 */
	private void loadTableSVC(){
		
//		File file;		
//		JFileChooser fileChooser = new JFileChooser("./data");
//		
//	
//		file = fileChooser.getSelectedFile();
		
		try {						
			//first grab the data and close the file
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(data.classificationFile.getPath())));
			Vector<String> data = new Vector<String>();
			String line;
			while( (line = br.readLine()) != null)
				data.add(line.trim());
			
			br.close();
				
			//build structures to capture the data for assingment information and for *validation
			
			//factor names
			Vector<String> groupNames = new Vector<String>();
			
			
			Vector<Integer> sampleIndices = new Vector<Integer>();
			Vector<String> sampleNames = new Vector<String>();
			Vector<String> groupAssignments = new Vector<String>();		
			
			//parse the data in to these structures
			String [] lineArray;
			//String status = "OK";
			for(int row = 0; row < data.size(); row++) {
				line = (String)(data.get(row));

				//if not a comment line, and not the header line
				if(!(line.startsWith("#")) && !(line.startsWith("SampleIndex"))) {
					
					lineArray = line.split("\t");
					
					//check what module saved the file
					if(lineArray[0].startsWith("Module:")) {
						if (!lineArray[1].equals("SVM")){
							Object[] optionst = { "Continue", "Cancel" };
							if (JOptionPane.showOptionDialog(null, 
		    						"The saved file was saved using a different module, "+lineArray[1]+". \n Would you like MeV to try to load it anyway?", 
		    						"File type warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
		    						optionst, optionst[0])==0)
								continue;
							return;
						}
						continue;
					}
					
					//pick up group names
					if(lineArray[0].startsWith("Group ") && lineArray[0].endsWith("Label:")) {
						groupNames.add(lineArray[1]);
						continue;
					}
						

					//non-comment line, non-header line and not a group label line
					
					try {
						Integer.parseInt(lineArray[0]);
					} catch ( NumberFormatException nfe) {
						//if not parsable continue
						continue;
					}
					
					sampleIndices.add(new Integer(lineArray[0]));
					sampleNames.add(lineArray[1]);
					groupAssignments.add(lineArray[2]);	
				}				
			}
			
			//we have the data parsed, now validate, assign current data


		        int numRows;
		        if (this.classifyGenes)
		        	numRows=this.experimentMap.getNumberOfGenes(); 
		        else
		        	numRows=this.experimentMap.getNumberOfSamples(); 
			if( numRows != sampleNames.size()) {
				System.out.println(numRows+"  "+sampleNames.size());
				//status = "number-of-samples-mismatch";
				System.out.println(numRows+ " s length " + sampleNames.size());
				//warn and prompt to continue but omit assignments for those not represented				

				JOptionPane.showMessageDialog(null, "<html>Error -- number of samples designated in assignment file ("+String.valueOf(sampleNames.size())+")<br>" +
						                                   "does not match the number of samples loaded in MeV ("+numRows+").<br>" +
						                                   	"Assignments are not set.</html>", "File Compatibility Error", JOptionPane.ERROR_MESSAGE);
				
				return;
			}
			
			int fileSampleIndex = 0;
			int groupIndex = 0;
			String groupName;
			
			for(int sample = 0; sample < numRows; sample++) {
				boolean doIndex = false;
				for (int i=0;i<numRows; i++){
					if (i==sample)
						continue;
					if (framework.getData().getSampleAnnotation(i, framework.getData().getCurrentSampleLabelKey()).equals(framework.getData().getSampleAnnotation(sample, framework.getData().getCurrentSampleLabelKey()))){
						doIndex=true;
					}
				}
				fileSampleIndex = sampleNames.indexOf(framework.getData().getSampleAnnotation(sample, framework.getData().getCurrentSampleLabelKey()));
				if (fileSampleIndex==-1){
					doIndex=true;
				}
				if (doIndex){
					setStateBasedOnIndex(groupAssignments,groupNames);
					break;
				}
				
				groupName = (String)(groupAssignments.get(fileSampleIndex));
				groupIndex = groupNames.indexOf(groupName);
				
				
                classes[sample]=-1;
                
				//set state
				try{
    				if (groupIndex==0)
    					classes[sample]=-1;
    				if (groupIndex==1)
    					classes[sample]=1;
    				if (groupIndex==2||groupIndex==-1)
    					classes[sample]=0;
				}catch (Exception e){
					classes[sample]=0;  //set to last state... excluded
				}
			}
					
			//need to clear assignments, clear assignment booleans in sample list and re-init
			//maybe a specialized inti for the sample list panel.
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "<html>The file format cannot be read.</html>", "File Compatibility Error", JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
	private void setStateBasedOnIndex(Vector<String>groupAssignments,Vector<String>groupNames){
		Object[] optionst = { "Continue", "Cancel" };
		if (JOptionPane.showOptionDialog(null, 
				"The saved file was saved using a different sample annotation or has duplicate annotation. \n Would you like MeV to try to load it by index order?", 
				"File type warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
				optionst, optionst[0])==1)
			return;
		int numRows;
        if (this.classifyGenes)
        	numRows=this.experimentMap.getNumberOfGenes(); 
        else
        	numRows=this.experimentMap.getNumberOfSamples(); 
		for(int sample = 0; sample < numRows; sample++) {
	
			//set state
			try{
				if (groupNames.indexOf(groupAssignments.get(sample))==0)
					classes[sample]=-1;
				if (groupNames.indexOf(groupAssignments.get(sample))==1)
					classes[sample]=1;
				if (groupNames.indexOf(groupAssignments.get(sample))==2||groupNames.indexOf(groupAssignments.get(sample))==-1)
					classes[sample]=0;
			}catch (Exception e){
				e.printStackTrace();
				classes[sample]=0;
			}
		}
	}
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * Reads an SVC classification file. Sets classes.
     * @return Returns true if read is successful and correct
     */
    private boolean readSVCFile() {
        String WorkString="";
        String Dummy;
        String Value;
        int Position=0;
        int CurrentGene;
        boolean ReturnValue=false;
        BufferedReader in=null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(data.classificationFile.getPath())));
            Dummy=in.readLine();
            if (Dummy!=null) {
                WorkString=Dummy;
                Position=WorkString.indexOf(9);
                WorkString=WorkString.substring(Position+1,WorkString.length());
                Value=WorkString;
                //int i=0;
            }
            CurrentGene=0;
            Dummy=in.readLine();
            while (Dummy != null) {
                WorkString=Dummy;
                WorkString = WorkString.substring(WorkString.indexOf(9)+1); //chop index
                Position=WorkString.indexOf(9); //WorkString starts with -1 or 1 (init. class.)
                Value=WorkString.substring(0,Position);
                WorkString=WorkString.substring(Position+1,WorkString.length());
                CurrentGene++;
                /* TODO
                if (Value.compareTo( experiment.getMatrix().GetUniqueID(CurrentGene-1))!=0) {
                    JOptionPane.showMessageDialog(parentFrame, "Unique ID does not fit in line "+CurrentGene,"Error", JOptionPane.ERROR_MESSAGE);
                    break;
                }
                 */
                if ((Value.compareTo("1")*Value.compareTo("-1"))!=0) {
                    JOptionPane.showMessageDialog(parentFrame, "Value not Element of [1,-1] in line "+CurrentGene,"Error", JOptionPane.ERROR_MESSAGE);
                    break;
                } else {
                    classes[CurrentGene-1]=Integer.parseInt(Value);
                }
                Dummy=in.readLine();
            }
            if (this.classifyGenes && CurrentGene !=  this.experimentMap.getNumberOfGenes()) {
                JOptionPane.showMessageDialog(parentFrame, "Number of Genes to classify does not match current data set!","Error", JOptionPane.ERROR_MESSAGE);
            }
            else if(!this.classifyGenes && CurrentGene != this.experimentMap.getNumberOfSamples()){
                JOptionPane.showMessageDialog(parentFrame, "Number of Experiments, "+CurrentGene+", to classify does not match current data set, "+this.experimentMap.getNumberOfSamples()+"!","Error", JOptionPane.ERROR_MESSAGE);
            }
            else {
                ReturnValue=true;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parentFrame, "Can not read file "+data.classificationFile.getPath()+"!","Error", JOptionPane.ERROR_MESSAGE);
        };
        return ReturnValue;
    }
    
    
    /**
     * Binds parameters required for SVM Training
     * @param data Holds parameters for SVM training
     */
    protected void bindTrainingParams( AlgorithmData data ) {
        bindParams( data );
        data.addParam("is-classify", String.valueOf(false));
        data.addParam("hcl-distance-function", String.valueOf( this.data.distanceFunction ) );
        data.addIntArray("classes", classes);
        data.addParam("seed", String.valueOf( this.data.seed));
        data.addParam("normalize", String.valueOf(this.data.normalize));
        data.addParam("radial", String.valueOf(this.data.radial));
        data.addParam("width-factor", String.valueOf(this.data.widthFactor));
        data.addParam("positive-diagonal", String.valueOf(this.data.positiveDiagonal));
        data.addParam("negative-diagonal", String.valueOf(this.data.negativeDiagonal));
        data.addParam("diagonal-factor", String.valueOf(this.data.diagonalFactor));
        data.addParam("positive-constraint", String.valueOf(this.data.positiveConstraint));
        data.addParam("negative-constraint", String.valueOf(this.data.negativeConstraint));
        data.addParam("convergence-threshold", String.valueOf(this.data.convergenceThreshold));
        data.addParam("constrain-weights", String.valueOf(this.data.constrainWeights));
        
        data.addParam("used-classification-editor", String.valueOf(this.data.useEditor));
        if(!this.data.useEditor && this.data.classificationFile != null)
            data.addParam("classification-file-name", this.data.classificationFile.getName());
        
        data.addParam("classify-genes", String.valueOf(classifyGenes));
    }
    
    /**
     * Extracts results from traing run. (Weights)
     * @param result Holds SVM training results
     */
    protected void getTrainingResults( AlgorithmData result ) {
        FloatMatrix weightsMatrix = result.getMatrix("weights");
        Weights = weightsMatrix.getColumnPackedCopy();
    }
    
    
    /**
     * Creates svm train result.
     */
    protected DefaultMutableTreeNode createTrainingGUIResult() {
        DefaultMutableTreeNode root =  new DefaultMutableTreeNode(new LeafInfo("SVM Training Result",
      //new SVMTrainViewer( framework, experiment, experimentMap, data, Weights, info, this.data.classifyGenes )));
        new SVMTrainViewer( experiment.getExperiment(), Weights, this.data.classifyGenes, data)));
        return root;
    }
    
    
    /**
     *
     * Code for Classification
     * @throws AlgorithmException
     * @return Returns true if initialization of parameters was successful
     */
    
    protected boolean initClassificationParams() {//throws AlgorithmException {
       // data.distanceFunction =  menu.getDistanceFunction();
     //   if (data.distanceFunction == Algorithm.DEFAULT)
     //      data.distanceFunction  = Algorithm.EUCLIDEAN; //this applies to HCL on SVM result
        //SVM kernal uses dot product on normalized vectors
        
        final JFileChooser fc = new JFileChooser(TMEV.getFile("data/svm"));
        fc.addChoosableFileFilter(new SVMFileFilter());
        fc.setFileView(new SVMFileView());
        int returnVal = fc.showOpenDialog( parentFrame );
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            SVMFile = fc.getSelectedFile();
            if(!scripting)
                showLogger("SVM Classify Log Window");
        } else
            return false;

        if(!scripting)
            logger.append("Reading SVM file\n");
        
        try {
            if(!ReadSVMFile())
                return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    
    /**
     * Reads svm file.  Populates Weights array
     * @throws Exception
     * @return Returns true if read is successful
     */
    public boolean ReadSVMFile() throws Exception {
        BufferedReader in;
        String Dummy=new String();
        String WorkString=new String();
        String Value=new String();
        int Position;
        int NumberOfGenes;
        int NumberOfSamples;
        
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(SVMFile)));
            
            NumberOfGenes = this.experimentMap.getNumberOfGenes();
            NumberOfSamples = this.experimentMap.getNumberOfSamples();
            in.readLine();
            
            Dummy=in.readLine();
            Position=Dummy.indexOf(":");
            Value=Dummy.substring(Position+2,Dummy.length());
            data.constant=Float.valueOf(Value).floatValue();
            
            Dummy=in.readLine();
            Position=Dummy.indexOf(":");
            Value=Dummy.substring(Position+2,Dummy.length());
            data.coefficient=Float.valueOf(Value).floatValue();
            
            Dummy=in.readLine();
            Position=Dummy.indexOf(":");
            Value=Dummy.substring(Position+2,Dummy.length());
            data.power=Float.valueOf(Value).floatValue();
            
            Dummy=in.readLine();
            Position=Dummy.indexOf(":");
            Value=Dummy.substring(Position+2,Dummy.length());
            data.diagonalFactor=Float.valueOf(Value).floatValue();
            
            Dummy=in.readLine();
            Position=Dummy.indexOf(":");
            Value=Dummy.substring(Position+2,Dummy.length());
            data.convergenceThreshold=Float.valueOf(Value).floatValue();
            
            //in.readLine(); //normalize bool
            //Dummy=in.readLine();
            //Position=Dummy.indexOf(":");
            //Value=Dummy.substring(Position+2,Dummy.length());
            //data.normalize=Boolean.valueOf(Value).booleanValue();
            
            //in.readLine(); //radial bool
            Dummy=in.readLine();
            Position=Dummy.indexOf(":");
            Value=Dummy.substring(Position+2,Dummy.length());
            data.radial=Boolean.valueOf(Value).booleanValue();
            
            Dummy=in.readLine();
            Position=Dummy.indexOf(":");
            Value=Dummy.substring(Position+2,Dummy.length());
            data.widthFactor=Float.valueOf(Value).floatValue();
            
            Dummy=in.readLine();
            Position=Dummy.indexOf(":");
            Value=Dummy.substring(Position+2,Dummy.length());
            data.constrainWeights=Boolean.valueOf(Value).booleanValue();
            Dummy=in.readLine();
            Position=Dummy.indexOf(":");
            Value=Dummy.substring(Position+2,Dummy.length());
            data.positiveConstraint=Float.valueOf(Value).floatValue();
            
            Dummy=in.readLine();
            Position=Dummy.indexOf(":");
            Value=Dummy.substring(Position+2,Dummy.length());
            data.negativeConstraint=Float.valueOf(Value).floatValue();
            
            in.readLine();
            //in.readLine(); //skip execution time
            
            Dummy=in.readLine();
            Position=Dummy.indexOf(":");
            Value=Dummy.substring(Position+2,Dummy.length());
            data.objective1=Float.valueOf(Value).floatValue();
            
            in.readLine();    //read blank row
            in.readLine(); //read data header
            
            if(this.classifyGenes)
                Weights=new float[NumberOfGenes];
            else
                Weights=new float[NumberOfSamples];
            
            //ready to read weights
            
            for (int i=0; i<Weights.length; i++) {
                Dummy=in.readLine();
                WorkString=Dummy;
                Position=WorkString.indexOf(9);
                Value=WorkString.substring(0,Position);
                Weights[i]=Float.valueOf(Value).floatValue();
            }
            
            if(in.readLine() != null){
                JOptionPane.showMessageDialog(parentFrame, SVMFile.getPath()+" has an incorrect number of weight values for current data set.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
        } catch (Exception e2) {
            e2.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "Can not read file "+SVMFile.getPath()+"!","Error", JOptionPane.ERROR_MESSAGE);
            throw e2;
        }
        return true;
    }
    
    
    
    /**
     * Binds parameters from the specified data.
     * @param data Maintains parameters for SVM classification
     */
    protected void bindClassificationParams( AlgorithmData data ) {
        bindParams( data );     //already done
        data.addParam("is-classify", String.valueOf(true));
        if(!scripting) {
            if(this.SVMMode == SVMGUI.CLASSIFY_ONLY)
                trainingMatrix = this.experiment.getExperiment().getMatrix();
            if(this.data.classifyGenes)
                data.addMatrix("training", trainingMatrix);
            else
                data.addMatrix("training", trainingMatrix.transpose());
        }
        if(this.Weights != null)
            data.addMatrix("weights", new FloatMatrix(Weights, 1));  //weights already set by Training
        data.addParam("classify-genes", String.valueOf(classifyGenes));
        if(this.data.calculateHCL){
            data.addParam("calculate-hcl", String.valueOf(this.data.calculateHCL));
            data.addParam("calculate-genes-hcl", String.valueOf(this.data.calcGeneHCL));
            data.addParam("calculate-samples-hcl", String.valueOf(this.data.calcSampleHCL));
            data.addParam("linkage-method", String.valueOf(this.data.hclMethod));
            data.addParam("hcl-distance-fucntion", String.valueOf(this.data.distanceFunction));
            data.addParam("hcl-absolute-distance", String.valueOf(this.data.absoluteDistance));
        }
    }
    
    /**
     * Extracts classification result
     * @param result Holds SVM classification results
     */
    protected void getClassificationResults( AlgorithmData result ) {
        discriminantMatrix = result.getMatrix("discriminant");
    }
    
    
    /**
     * Creates analysis result to be inserted into the framework tree node.
     * @return Node containing <CODE>SVMClassifyViewer</CODE>
     */
    protected DefaultMutableTreeNode createClassificationGUIResult() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new LeafInfo("SVM Classification Result",
       // new SVMClassifyViewer( framework, experiment, data, discriminantMatrix, info, this.data.classifyGenes )));
        		new SVMClassifyViewer(experiment.getExperiment(), discriminantMatrix, this.data.classifyGenes)));
        return root;
    }
    
    /**
     * Creates viewers folowing classification
     * @param result Holds svm classification result
     * @return contains result viewers
     */
    protected DefaultMutableTreeNode createViewers(AlgorithmData result){
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        int [][] classIndices = getClassificationIndices(result.getIntArray("positives"), result.getIntArray("negatives"));
        IViewer expViewer;
        if(this.classifyGenes)
            expViewer = new SVMExperimentViewer(this.experimentMap, classIndices);
        else
            expViewer = new SVMExperimentClusterViewer(this.experimentMap, classIndices);
        node.add(new DefaultMutableTreeNode(new LeafInfo("Positives", expViewer, new Integer(0))));
        node.add(new DefaultMutableTreeNode(new LeafInfo("Negatives", expViewer, new Integer(1))));
        return node;
    }
    
    
    /**
     *  Creates viewers following svm train and classify.
     */
    protected DefaultMutableTreeNode createSVMExpressionViews(AlgorithmData result, int [] classification){
        int [] pos = result.getIntArray("positives");
        int [] neg = result.getIntArray("negatives");
        
        int [][] truePositives = getTruePositives(pos, classification);
        int [][] falseNegatives = getFalseNegatives(pos, classification);
        int [][] falsePositives = getFalsePositives(neg, classification);
        int [][] trueNegatives = getTrueNegatives(neg, classification);
        
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        
        if(this.classifyGenes){
            SVMDiscriminantExperimentViewer viewer = new SVMDiscriminantExperimentViewer(experimentMap, getOrderedClassIndices(truePositives[0], falseNegatives[0], falsePositives[0], trueNegatives[0]),
            truePositives != null ? truePositives[0].length : 0,
            falsePositives != null ? falsePositives[0].length : 0,
            getDiscriminants(pos, neg), null, this.classifyGenes);
            
            node.add(new DefaultMutableTreeNode(new LeafInfo("Positives", viewer, new Integer(0))));
            node.add(new DefaultMutableTreeNode(new LeafInfo("Negatives", viewer, new Integer(1))));
        }
        else{
            //Classify Experiments
            int [] posSamplesOrder = result.getIntArray("positve-samples-order");
            int [] negSamplesOrder = result.getIntArray("negative-samples-order");
            SVMExperimentClusterViewer viewer = new SVMExperimentClusterViewer(experimentMap, getClassificationIndices(pos, neg));
            
            node.add(new DefaultMutableTreeNode(new LeafInfo("Positive Experiments", viewer, new Integer(0))));
            node.add(new DefaultMutableTreeNode(new LeafInfo("Negative Experiments", viewer, new Integer(1))));
        }
        
        return node;
    }
    
    protected  void createInfoView(AlgorithmData result, DefaultMutableTreeNode node){
        int [] pos = result.getIntArray("positives");
        int [] neg = result.getIntArray("negatives");
        SVMInfoViewer viewer;
        if(SVMMode == SVMGUI.CLASSIFY_ONLY){
            viewer = new SVMInfoViewer(pos.length, neg.length, this.classifyGenes, this.SVMMode);
            
        }
        
        else{
            int [][] truePositives = getTruePositives(pos, classes);
            int [][] falseNegatives = getFalseNegatives(pos, classes);
            int [][] falsePositives = getFalsePositives(neg, classes);
            int [][] trueNegatives = getTrueNegatives(neg, classes);
            
            int numPosRecruitedFromNeutrals = 0;
            int numNegRecruitedFromNeutrals = 0;
            int numPosExamples = getNumberOfPositiveExamples(classes);
            int numNegExamples = getNumberOfNegativeExamples(classes);
            
            //If neutrals exist
            if((classes.length - numPosExamples) - numNegExamples > 0){
                numPosRecruitedFromNeutrals = getNumPosRecFromNeutrals(pos, classes);
                numNegRecruitedFromNeutrals = getNumNegRecFromNeutrals(neg, classes);
            }
            
            viewer = new SVMInfoViewer( numPosExamples, numNegExamples, (classes.length - numPosExamples) - numNegExamples,
            pos.length, truePositives[0].length, falseNegatives[0].length, neg.length, trueNegatives[0].length, falsePositives[0].length,
            numPosRecruitedFromNeutrals, numNegRecruitedFromNeutrals, this.classifyGenes, this.SVMMode);
        }
        
        node.add(new DefaultMutableTreeNode(new LeafInfo("Classification Information", viewer)));
    }
    
    /** Creates SVM Centroid viewes
     */
    private DefaultMutableTreeNode createSVMCentroidViews(AlgorithmData result, DefaultMutableTreeNode root){
        DefaultMutableTreeNode centNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expNode = new DefaultMutableTreeNode("Expression Graphs");
        float [][] means;
        float [][] vars;
        
        int [][] clusters = this.getClassificationIndices(result.getIntArray("positives"), result.getIntArray("negatives"));
        if(this.classifyGenes){
            SVMCentroidViewer viewer = new SVMCentroidViewer(this.experimentMap, clusters);
            means = result.getMatrix("means").A;
            vars = result.getMatrix("variances").A;
            viewer.setMeans(means);
            viewer.setVariances(vars);
            SVMCentroidsViewer multiviewer = new SVMCentroidsViewer(this.experimentMap, clusters);
            multiviewer.setMeans(means);
            multiviewer.setVariances(vars);
            expNode.add(new DefaultMutableTreeNode(new LeafInfo("Positives", viewer, new CentroidUserObject(0, CentroidUserObject.VALUES_MODE))));
            expNode.add(new DefaultMutableTreeNode(new LeafInfo("Negatives", viewer, new CentroidUserObject(1, CentroidUserObject.VALUES_MODE))));
            expNode.add(new DefaultMutableTreeNode(new LeafInfo("Both Groups", multiviewer, new Integer(CentroidUserObject.VALUES_MODE))));
            centNode.add(new DefaultMutableTreeNode(new LeafInfo("Positives", viewer, new CentroidUserObject(0, CentroidUserObject.VARIANCES_MODE))));
            centNode.add(new DefaultMutableTreeNode(new LeafInfo("Negatives", viewer, new CentroidUserObject(1, CentroidUserObject.VARIANCES_MODE))));
            centNode.add(new DefaultMutableTreeNode(new LeafInfo("Both Groups", multiviewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            root.add(centNode);
            root.add(expNode);
            return root;
        } else{
            SVMExperimentClusterCentroidViewer viewer = new SVMExperimentClusterCentroidViewer(this.experimentMap, clusters);
            means = result.getMatrix("means").A;
            vars = result.getMatrix("variances").A;
            viewer.setMeans(means);
            viewer.setVariances(vars);
            SVMExperimentClusterCentroidsViewer multiviewer = new SVMExperimentClusterCentroidsViewer(this.experimentMap, clusters);
            multiviewer.setMeans(means);
            multiviewer.setVariances(vars);
            expNode.add(new DefaultMutableTreeNode(new LeafInfo("Positives", viewer, new CentroidUserObject(0, CentroidUserObject.VALUES_MODE))));
            expNode.add(new DefaultMutableTreeNode(new LeafInfo("Negatives", viewer, new CentroidUserObject(1, CentroidUserObject.VALUES_MODE))));
            expNode.add(new DefaultMutableTreeNode(new LeafInfo("Both Groups", multiviewer, new Integer(CentroidUserObject.VALUES_MODE))));
            centNode.add(new DefaultMutableTreeNode(new LeafInfo("Positives", viewer, new CentroidUserObject(0, CentroidUserObject.VARIANCES_MODE))));
            centNode.add(new DefaultMutableTreeNode(new LeafInfo("Negatives", viewer, new CentroidUserObject(1, CentroidUserObject.VARIANCES_MODE))));
            centNode.add(new DefaultMutableTreeNode(new LeafInfo("Both Groups", multiviewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            root.add(centNode);
            root.add(expNode);
            return root;
        }
    }
    
    
    /**
     * Adds nodes to display hierarchical trees.
     */
    private DefaultMutableTreeNode createHierarchicalTreeViews(Cluster result_cluster, AlgorithmData result) {
        if (!this.data.calculateHCL) {
            return null;
        }
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Hierarchical Trees");
        NodeList nodeList = result_cluster.getNodeList();
        String nodeString = "Positives";
        int [][] classIndices = null;
        
        if(!this.classifyGenes){  //if classifying experiments
            //get classification indices
            classIndices = getClassificationIndices(result.getIntArray("positives"), result.getIntArray("negatives"));;
            if(this.data.calcSampleHCL){
                //if have sample HCL then indices need to be ordered
                classIndices = getOrderedIndices(nodeList, classIndices, this.data.calcGeneHCL);
            }
        }
        for (int i=0; i<nodeList.getSize(); i++) {
            if(i > 0)
                nodeString = "Negatives";
            if(this.classifyGenes)
                node.add(new DefaultMutableTreeNode(new LeafInfo(nodeString, createHCLViewer(nodeList.getNode(i), result, classIndices))));
            else
                node.add(new DefaultMutableTreeNode(new LeafInfo(nodeString, createHCLViewer(nodeList.getNode(i), result, classIndices), new Integer(i))));
        }
        return node;
    }
    
    
    /**
     * Creates an <code>HCLViewer</code>.
     */
    private IViewer createHCLViewer(Node clusterNode, AlgorithmData result, int [][] classIndices) {
        HCLTreeData genes_result = this.data.calcGeneHCL ? getResult(clusterNode, 0) : null;
        HCLTreeData samples_result = this.data.calcSampleHCL ? getResult(clusterNode, this.data.calcGeneHCL ? 4 : 0) : null;
        if(this.classifyGenes){
            return new HCLViewer(this.experimentMap, clusterNode.getFeaturesIndexes(), genes_result, samples_result);
        }
        else{
            return new HCLViewer(this.experimentMap, clusterNode.getFeaturesIndexes(), genes_result, samples_result, classIndices, true);
        }
    }
    
    /**
     * Returns a hcl tree data from the specified cluster node.
     * @param clusterNode Holds result nodes
     * @param pos Holds indices of elements in positive classification
     * @return
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
     * Creates and adds an SVM parameter information node
     */
    private void addSVMParameterNode(DefaultMutableTreeNode root){
        String value;
        DefaultMutableTreeNode genInfo = new DefaultMutableTreeNode("General Information");
        DefaultMutableTreeNode node;
        DefaultMutableTreeNode childNode;
        
        if(this.SVMMode == SVMGUI.TRAIN_ONLY)
            value = "Train SVM Only";
        else if(this.SVMMode == SVMGUI.CLASSIFY_ONLY)
            value = "Classify Only";
        else if(this.SVMMode == SVMGUI.TRAIN_AND_CLASSIFY)
            value = "Train SVM and Classify";
        else
            value = "One-out Validation";
        
        node = new DefaultMutableTreeNode("SVM Mode: "+value);
        genInfo.add(node);
        node = new DefaultMutableTreeNode("Kernel Parameters");
        node.add(new DefaultMutableTreeNode("Metric: Dot Product on Normalized Vectors"));
        
        if(!data.radial){
            childNode = new DefaultMutableTreeNode("Kernel Function: Polynomial");
            childNode.add(new DefaultMutableTreeNode("Constant: "+String.valueOf(data.constant)));
            childNode.add(new DefaultMutableTreeNode("Coefficient: "+String.valueOf(data.coefficient)));
            childNode.add(new DefaultMutableTreeNode("Power: "+String.valueOf(data.power)));
            node.add(childNode);
        }
        else{
            childNode = new DefaultMutableTreeNode("Kernel Function: Radial Basis (Gausian)");
            childNode.add(new DefaultMutableTreeNode("Width Factor: "+String.valueOf(data.widthFactor)));
            node.add(childNode);
        }
        genInfo.add(node);
        
        node = new DefaultMutableTreeNode("Training Parameters");
        childNode = new DefaultMutableTreeNode("Constrain Weights: "+String.valueOf(data.constrainWeights) );
        if(data.constrainWeights){
            childNode.add(new DefaultMutableTreeNode("Positive Constraint: "+String.valueOf(data.positiveConstraint)));
            childNode.add(new DefaultMutableTreeNode("Negative Constraint: "+String.valueOf(data.negativeConstraint)));
        }
        node.add(childNode);
        node.add(new DefaultMutableTreeNode("Diagonal Factor: "+String.valueOf(data.diagonalFactor)));
        node.add(new DefaultMutableTreeNode("Threshold: "+String.valueOf(data.convergenceThreshold)));
        genInfo.add(node);
        
        if(data.calculateHCL){
            node = new DefaultMutableTreeNode("HCL Parameters");
            value = AbstractAlgorithm.getDistanceName(data.distanceFunction);
            node.add(new DefaultMutableTreeNode("HCL distance metric: "+value));
            String method;
            if(data.hclMethod == 0)
                method = "Average Linkage";
            else if(data.hclMethod == 1)
                method = "Complete Linkage";
            else
                method = "Single Linkage";
            node.add(new DefaultMutableTreeNode("Linkage Method: "+method));
            genInfo.add(node);
        }
        root.add(genInfo);
    }
    
    /**
     * Returns a default ordered gene cluster array
     * @return cluster indices
     */
    private int [][] getDefaultGeneCluster(){
        int n = this.experimentMap.getNumberOfGenes();
        int [][] c = new int [1][n];
        for(int i = 0; i < n; i++){
            c[0][i] = i;
        }
        return c;
    }
    
    /**
     * Returns the discriminants for positive and negative elements
     * @param pos Positive element indices
     * @param neg Negative element indices
     * @return
     */
    private float [][] getDiscriminants(int [] pos, int [] neg){
        //discriminat is in second column of discriminantMatrix
        int n = discriminantMatrix.getRowDimension();
        float [][] A = discriminantMatrix.A;
        float disc [] = new float [n];
        for(int i=0; i < n; i++){
            disc[i] = A[i][1];
        }
        float [][] result = new float[2][];
        result[0] = new float[pos.length];
        result[1] = new float[neg.length];
        
        for(int i = 0; i < pos.length ; i++){
            result[0][i] = disc[pos[i]];
        }
        
        for(int i = 0; i < neg.length ; i++){
            result[1][i] = disc[neg[i]];
        }
        return result;
    }
    
    /**
     * Orders the classification indicies into positive and negative element arrays
     *
     * @param a Positive group 1
     * @param b Positive group 2
     * @param c Negative group 1
     * @param d Negative group 2
     * @return
     */
    private int [][] getOrderedClassIndices(int [] a, int [] b, int [] c, int [] d){
        //        int [][] result = new int[a.length + b.length][c.length + d.length];
        
        int [][] result = new int[2][];
        result[0] = new int[a.length + b.length];
        result[1] = new int[c.length + d.length];
        
        
        System.arraycopy(a, 0, result[0], 0, a.length);
        System.arraycopy(b, 0, result[0], a.length, b.length);
        System.arraycopy(c, 0, result[1], 0, c.length);
        System.arraycopy(d, 0, result[1], c.length, d.length);
        return result;
    }
    
    
    /**
     * Extracts the indices of retained and recruited positives
     * @param pos Positive element indices
     * @param classification classification indices
     * @return
     */
    private int [][] getTruePositives(int [] pos, int [] classification){
        int [][] result = null;
        Vector pVector = new Vector();
        for(int i= 0; i < pos.length; i++){
            if(classification[pos[i]] == 1){
                pVector.add(new Integer(pos[i]));
            }
        }
        result = new int[1][pVector.size()];
        for(int i = 0; i < result[0].length ; i++){
            result[0][i] = ((Integer)pVector.elementAt(i)).intValue();
        }
        return result;
    }
    
    /**
     *  Extracts the indices of recuited positives
     */
    private int [][] getFalseNegatives(int [] pos,  int [] classification){
        int [][] result = null;
        Vector pVector = new Vector();
        for(int i= 0; i < pos.length; i++){
            if(classification[pos[i]] <= 0){
                pVector.add(new Integer(pos[i]));
            }
        }
        result = new int[1][pVector.size()];
        for(int i = 0; i < result[0].length ; i++){
            result[0][i] = ((Integer)pVector.elementAt(i)).intValue();
        }
        return result;
    }
    
    /**
     * Extracts recruitred negatives
     */
    private int [][] getFalsePositives(int [] neg,  int [] classification){
        int [][] result = null;
        Vector pVector = new Vector();
        for(int i= 0; i < neg.length; i++){
            
            if(classification[neg[i]] == 1){
                pVector.add(new Integer(neg[i]));
            }
        }
        result = new int[1][pVector.size()];
        for(int i = 0; i < result[0].length ; i++){
            result[0][i] = ((Integer)pVector.elementAt(i)).intValue();
        }
        return result;
    }
    
    /**
     *  Extracts retained negatives
     */
    private int [][] getTrueNegatives(int [] neg,  int [] classification){
        int [][] result = null;
        Vector pVector = new Vector();
        for(int i= 0; i < neg.length; i++){
            if(classification[neg[i]] <= 0){
                pVector.add(new Integer(neg[i]));
            }
        }
        result = new int[1][pVector.size()];
        for(int i = 0; i < result[0].length ; i++){
            result[0][i] = ((Integer)pVector.elementAt(i)).intValue();
        }
        return result;
    }
    
    
    /**
     * Returns the number of positive training examples
     */
    private int getNumberOfPositiveExamples(int [] c){
        int cnt = 0;
        for(int i = 0; i < c.length; i++){
            if(c[i] == 1)
                cnt++;
        }
        return cnt;
    }
    
    /**
     * Returns the number of negative training examples
     */
    private int getNumberOfNegativeExamples(int [] c){
        int cnt = 0;
        for(int i = 0; i < c.length; i++){
            if(c[i] == -1)
                cnt++;
        }
        return cnt;
    }
    
    private int  getNumPosRecFromNeutrals(int [] pos, int [] c){
        int cnt = 0;
        for(int i = 0; i < pos.length; i++){
            if(c[pos[i]] == 0 ) //initially neutral gene
                cnt++;
        }
        return cnt;
    }
    
    private int  getNumNegRecFromNeutrals(int [] neg, int [] c){
        int cnt = 0;
        for(int i = 0; i < neg.length; i++){
            if(c[neg[i]] == 0 )  //initially neutral gene
                cnt++;
        }
        return cnt;
    }
    
    /**
     * Returns the result classification as element indices
     * (merges pos and neg into 2D array)
     * @param pos
     * @param neg
     * @return holds merged positive and negative
     */
    private int [][] getClassificationIndices(int [] pos, int [] neg){
        int [][] results = new int[2][];
        results[0] = pos;
        results[1] = neg;
        return results;
    }
    
    private void accumulateResult(FloatMatrix cumMatrix, int index){
        for(int i = 0; i < this.discriminantMatrix.getColumnDimension(); i++){
            cumMatrix.set(index, i, discriminantMatrix.get(index, i));
        }
    }
    
    private float [] toFloatArray(int [] array){
        float [] result = new float[array.length];
        for(int i = 0; i < result.length; i++){
            result[i] = (float)array[i];
        }
        return result;
    }
    
    private int getNumberOfNonNeutrals(){
        int n = 0;
        for(int i = 0; i < classes.length; i++){
            if(classes[i] != 0)
                n++;
        }
        return n;
    }
    
    // GUI helpers
    protected void showLogger( String caption ) {
        if (logger == null) {
            logger = new Logger(parentFrame, caption , listener );
            logger.show();
        }
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
     * Returns positive element index list
     */
    private int [] getPositives(FloatMatrix matrix){
        int cnt = 0;
        
        for(int i = 0; i < matrix.getRowDimension(); i++){
            if( matrix.get( i, 0 ) == 1.0 )
                cnt++;
        }
        
        int [] pos = new int[cnt];
        cnt = 0;
        
        for(int i = 0; i < matrix.getRowDimension(); i++){
            if( matrix.get( i, 0 ) == 1.0 ){
                pos[cnt] = i;
                cnt++;
            }
        }
        return pos;
    }
    
    
    
    /**
     * Returns negative element index list
     */
    private int [] getNegatives(FloatMatrix matrix){
        int cnt = 0;
        for(int i = 0; i < matrix.getRowDimension(); i++){
            if( matrix.get( i, 0 ) <= 0 )
                cnt++;
        }
        
        int [] neg = new int[cnt];
        cnt = 0;
        
        for(int i = 0; i < matrix.getRowDimension(); i++){
            if( matrix.get( i, 0 ) <= 0 ){
                neg[cnt] = i;
                cnt++;
            }
        }
        return neg;
    }
    
    
    
    /**
     *  Retuns means values for each column within positives and negatives
     */
    private FloatMatrix getMeans(FloatMatrix discMatrix){
        FloatMatrix expMatrix = experiment.getExperiment().getMatrix();
        
        if(!this.classifyGenes)
            expMatrix = expMatrix.transpose();
        
        int numSamples = expMatrix.getColumnDimension();
        int numGenes = expMatrix.getRowDimension();
        
        FloatMatrix means = new FloatMatrix(2, numSamples);
        float posMean = 0;
        float negMean = 0;
        float value;
        int posCnt = 0;
        int negCnt = 0;
        float c;
        
        for(int j = 0; j < numSamples; j++){
            for(int i = 0; i < numGenes; i++){
                
                c = discMatrix.get(i,0);
                if(c == 1){
                    value = expMatrix.get(i,j);
                    if(!Float.isNaN(value)){
                        posCnt++;
                        posMean += value;
                    }
                }
                else{
                    
                    value = expMatrix.get(i,j);
                    if(!Float.isNaN(value)){
                        negCnt++;
                        negMean += value;
                    }
                }
            }
            means.set( 0, j, (float)(posCnt != 0 ? posMean/posCnt : 0.0f));
            means.set( 1, j, (float)(negCnt != 0 ? negMean/negCnt : 0.0f));
            posCnt = 0;
            negCnt = 0;
            posMean = 0;
            negMean = 0;
        }
        return means;
    }
    
    
    /**
     *  Retuns variance values for each column within positives and negatives
     */
    private FloatMatrix getVariance(FloatMatrix discMatrix, FloatMatrix means){
        FloatMatrix expMatrix = experiment.getExperiment().getMatrix();
        
        if(!this.classifyGenes)
            expMatrix = expMatrix.transpose();
        
        int numSamples = expMatrix.getColumnDimension();
        int numGenes = expMatrix.getRowDimension();
        FloatMatrix vars = new FloatMatrix(2, numSamples);
        float value;
        float c;
        float mean;
        float ssePos = 0;
        int posCnt = 0;
        float sseNeg = 0;
        int negCnt = 0;
        for(int i = 0; i < numSamples; i++){
            
            for(int j = 0; j < numGenes; j++){
                c = discMatrix.get(j, 0);
                
                if(c == 1){
                    value = expMatrix.get(j,i);
                    if(!Float.isNaN(value)){
                        ssePos += Math.pow(value - means.get(0, i), 2);
                        posCnt++;
                    }
                }
                else{
                    value = expMatrix.get(j,i);
                    if(!Float.isNaN(value)){
                        sseNeg += Math.pow(value - means.get(1, i), 2);
                        negCnt++;
                    }
                }
            }
            vars.set( 0, i, (float)(posCnt > 1 ? Math.sqrt(ssePos/(posCnt - 1)) : 0.0f));
            vars.set( 1, i, (float)(negCnt > 1 ? Math.sqrt(sseNeg/(negCnt - 1)) : 0.0f));
            posCnt = 0;
            negCnt = 0;
            ssePos = 0;
            sseNeg = 0;
        }
        return vars;
    }
    
    
    private void calculateHCL(AlgorithmData data, FloatMatrix discriminantMatrix) throws AbortException, AlgorithmException{
        
        int [][] clusters = new int[2][];
        clusters[0] = getPositives(discriminantMatrix);
        clusters[1] = getNegatives(discriminantMatrix);
        
        if(this.data.calculateHCL){
            //preparation for HCL
            Cluster result_cluster = new Cluster();
            NodeList nodeList = result_cluster.getNodeList();
            int[] features;
            for (int i=0; i<clusters.length; i++) {
                if (stop) {
                    throw new AbortException();
                }
                features = clusters[i];
                Node node = new Node(features);
                nodeList.addNode(node);
                node.setValues(calculateHierarchicalTree(features, this.data.hclMethod, this.data.calcGeneHCL, this.data.calcSampleHCL));
            }
            data.addCluster("cluster", result_cluster);
        }
    }
    
    
    /**
     * Creates HCL results
     */
    private NodeValueList calculateHierarchicalTree(int[] features, int method, boolean genes, boolean experiments) throws AlgorithmException {
        NodeValueList nodeList = new NodeValueList();
        AlgorithmData data = new AlgorithmData();
        FloatMatrix experiment;
        FloatMatrix expMatrix = this.experimentMap.getMatrix();
        if(this.data.classifyGenes){
            experiment = getSubExperiment(expMatrix, features);
        }
        else{
            expMatrix = expMatrix.transpose();
            experiment = getSubExperimentReducedCols(expMatrix, features);
        }
        data.addMatrix("experiment", experiment);
        data.addParam("hcl-distance-function", String.valueOf(this.data.distanceFunction));
        data.addParam("hcl-distance-absolute", String.valueOf(this.data.absoluteDistance));
        data.addParam("method-linkage", String.valueOf(this.data.hclMethod));
        
        Algorithm hcl = framework.getAlgorithmFactory().getAlgorithm("HCL");
        
        //        HCL hcl = new HCL();
        
        AlgorithmData result;
        
        if (genes) {
            data.addParam("calculate-genes", String.valueOf(true));
            result = hcl.execute(data);
            validate(result);
            addNodeValues(nodeList, result);
        }
        if (experiments) {
            data.addParam("calculate-genes", String.valueOf(false));
            result = hcl.execute(data);
            int [] nodes = result.getIntArray("node-order");
            validate(result);
            addNodeValues(nodeList, result);
        }
        return nodeList;
    }
    
    
    /**
     * Accumulates hcl results
     */
    private void addNodeValues(NodeValueList target_list, AlgorithmData source_result) {
        target_list.addNodeValue(new NodeValue("child-1-array", source_result.getIntArray("child-1-array")));
        target_list.addNodeValue(new NodeValue("child-2-array", source_result.getIntArray("child-2-array")));
        target_list.addNodeValue(new NodeValue("node-order", source_result.getIntArray("node-order")));
        target_list.addNodeValue(new NodeValue("height", source_result.getMatrix("height").getRowPackedCopy()));
    }
    
    
    /**
     *  Gets sub experiment (cluster membership only, dictated by features)
     */
    private FloatMatrix getSubExperiment(FloatMatrix experiment, int[] features) {
        FloatMatrix subExperiment = new FloatMatrix(features.length, experiment.getColumnDimension());
        for (int i=0; i<features.length; i++) {
            subExperiment.A[i] = experiment.A[features[i]];
        }
        return subExperiment;
    }
    
    /**
     *  Creates a matrix with reduced columns (samples) as during experiment classification
     */
    private FloatMatrix getSubExperimentReducedCols(FloatMatrix experiment, int[] features) {
        FloatMatrix copyMatrix = experiment.copy();
        FloatMatrix subExperiment = new FloatMatrix(features.length, copyMatrix.getColumnDimension());
        for (int i=0; i<features.length; i++) {
            subExperiment.A[i] = copyMatrix.A[features[i]];
        }
        subExperiment = subExperiment.transpose();
        return subExperiment;
    }
    
    /**
     * Checks the result of hcl algorithm calculation.
     * @throws AlgorithmException, if the result is incorrect.
     */
    private void validate(AlgorithmData result) throws AlgorithmException {
        if (result.getIntArray("child-1-array") == null) {
            throw new AlgorithmException("parameter 'child-1-array' is null");
        }
        if (result.getIntArray("child-2-array") == null) {
            throw new AlgorithmException("parameter 'child-2-array' is null");
        }
        if (result.getIntArray("node-order") == null) {
            throw new AlgorithmException("parameter 'node-order' is null");
        }
        if (result.getMatrix("height") == null) {
            throw new AlgorithmException("parameter 'height' is null");
        }
    }
    
    private int[] convert2int(ArrayList source) {
        int[] int_matrix = new int[source.size()];
        for (int i=0; i<int_matrix.length; i++) {
            int_matrix[i] = (int)((Float)source.get(i)).floatValue();
        }
        return int_matrix;
    }
    
    
    private class Listener extends DialogListener implements AlgorithmListener {
        
        /**
         * Handles algorithm events
         * @param event
         */
        public void valueChanged(AlgorithmEvent event) {
            int id = event.getId();
            switch (id) {
                case AlgorithmEvent.SET_UNITS:
                    break;
                case AlgorithmEvent.PROGRESS_VALUE:
                    logger.append( event.getDescription() );
                    break;
                case AlgorithmEvent.MONITOR_VALUE:
                    if (monitor == null) {
                        monitor = new Monitor(parentFrame, "Convergence",75,125,(0.1/data.convergenceThreshold));
                        monitor.show();
                    } else {
                        monitor.update( event.getFloatValue() );
                    }
                    break;
            }
        }
        
        /**
         * Handles action events during run
         * @param e
         */
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("cancel-command")) {
                algorithm.abort();
                if (monitor != null)
                    monitor.dispose();
                if (logger != null)
                    logger.dispose();
            }
        }
        
        /**
         * Handles dialog closing events
         * @param e
         */
        public void windowClosing(WindowEvent e) {
            algorithm.abort();
            if (monitor != null)
                monitor.dispose();
            if (logger != null)
                logger.dispose();
            
        }
    }
}
