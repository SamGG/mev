/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: BETRGUI.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-11-07 17:27:40 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.BETR;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
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
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLGUI;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLViewer;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;
import org.tigr.util.FloatMatrix;

/**
 *
 * @author  dschlauch
 * @version
 */
public class BETRGUI implements IClusterGUI, IScriptGUI {
    
    protected Algorithm algorithm;
    protected Progress progress;
    protected Experiment experiment;
    protected int[][] clusters;
    protected int[][] errorGenesArray = new int[1][];
    protected FloatMatrix means;
    protected FloatMatrix variances;
    
    protected int[][] sigGenesArrays;
    
    
    protected String[] auxTitles;
    protected Object[][] auxData;
    
    protected Vector<Float> fValues, rawPValues, adjPValues, dfNumValues, dfDenomValues, ssGroups, ssError;
    protected float[][] geneTimeMeans, geneTimeSDs;
    protected float[][] geneConditionMeans, geneConditionSDs;
    protected boolean drawSigTreesOnly;
    
    //protected boolean usePerms;
    
    Vector<String> exptNamesVector;
    protected int[] timeAssignments;
    protected int[] conditionAssignments;
    protected int falseNum, correctionMethod;
    protected double falseProp;
    protected IData data;
    protected int numTimePoints, numPerms, dataDesign;
    protected boolean errorGenes;
    /** Creates new BETRGUI */
    public BETRGUI() {
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
        this.experiment = framework.getData().getExperiment();        
        this.data = framework.getData();
        exptNamesVector = new Vector<String>();
        int number_of_samples = experiment.getNumberOfSamples();
        //int number_of_genes = experiment.getNumberOfGenes();
        
        int [] columnIndices = experiment.getColumnIndicesCopy(); 
        
        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(framework.getData().getFullSampleName(columnIndices[i]));
        }
        
        BETRInitBox BETRDialog = new BETRInitBox((JFrame)framework.getFrame(), true, exptNamesVector, framework.getClusterRepository(1));
        BETRDialog.setVisible(true);
        
        if (!BETRDialog.isOkPressed()) return null;
        
        //double alpha = BETRDialog.getPValue();
        float alpha = BETRDialog.getAlpha();
        dataDesign = BETRDialog.getDataDesign();
        numTimePoints = BETRDialog.getNumTimePoints();

        if (BETRDialog.getTestDesign()==BETRInitBox.CLUSTER_SELECTION){
        	timeAssignments =BETRDialog.getClusterTimeAssignments();
        	if (dataDesign==2)
        		conditionAssignments=BETRDialog.getClusterConditionAssignments();
        }
        if (BETRDialog.getTestDesign()==BETRInitBox.BUTTON_SELECTION){
        	timeAssignments=BETRDialog.getTimeAssignments();
        	if (dataDesign==2)
        		conditionAssignments=BETRDialog.getConditionAssignments();
        } 
        if (timeAssignments == null)
        	return null;
        boolean usePerms = BETRDialog.usePerms();
        int numPerms = 0;
        if (usePerms) {
            numPerms = BETRDialog.getNumPerms();
        }
        correctionMethod = BETRDialog.getCorrectionMethod();
        if (correctionMethod == BETRInitBox.FALSE_NUM) {
            falseNum = BETRDialog.getFalseNum();
        }
        if (correctionMethod == BETRInitBox.FALSE_PROP) {
            falseProp = BETRDialog.getFalseProp();
        }
        boolean isHierarchicalTree = BETRDialog.drawTrees();
        drawSigTreesOnly = true;
        if (isHierarchicalTree) {
            drawSigTreesOnly = BETRDialog.drawSigTreesOnly();
        }      
        
        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.EUCLIDEAN;
        }
        
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        int hcl_function = 4;
        boolean hcl_absolute = false;
        boolean hcl_samples_ordered=false;
        boolean hcl_genes_ordered=false;
        if (isHierarchicalTree) {
            HCLInitDialog hcl_dialog = new HCLInitDialog(framework.getFrame(), menu.getFunctionName(function), menu.isAbsoluteDistance(), true);
            if (hcl_dialog.showModal() != JOptionPane.OK_OPTION) {
                return null;
            }
            hcl_method = hcl_dialog.getMethod();
            hcl_samples = hcl_dialog.isClusterExperiments();
            hcl_genes = hcl_dialog.isClusterGenes();
            hcl_function = hcl_dialog.getDistanceMetric();
            hcl_absolute = hcl_dialog.getAbsoluteSelection();
            hcl_genes_ordered = hcl_dialog.isGeneOrdering();
            hcl_samples_ordered = hcl_dialog.isSampleOrdering();
        }
        
        Listener listener = new Listener();
        
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("BETR");
            algorithm.addAlgorithmListener(listener);
            
            this.progress = new Progress(framework.getFrame(), "Finding significant genes", listener);
            this.progress.show();
            
            AlgorithmData data = new AlgorithmData();
            
            data.addMatrix("experiment", experiment.getMatrix());
            data.addParam("distance-factor", String.valueOf(1.0f));
            data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
            
            data.addParam("distance-function", String.valueOf(function));
            data.addParam("dataDesign", String.valueOf(dataDesign));
            data.addIntArray("time_assignments", timeAssignments);
            data.addIntArray("condition_assignments", conditionAssignments);
            data.addIntMatrix("time-matrix", BETRDialog.getTimeMatrix());
        	data.addIntMatrix("conditions-matrix", null);
            if (dataDesign==2)
            	data.addIntMatrix("conditions-matrix", BETRDialog.getConditionsMatrix());
            data.addParam("usePerms", String.valueOf(usePerms));
            data.addParam("numPerms", String.valueOf(numPerms));
            data.addParam("alpha", String.valueOf(alpha));
            data.addParam("correction-method", String.valueOf(correctionMethod));
            data.addParam("numTimePoints", String.valueOf(numTimePoints));
            data.addParam("alpha-value", String.valueOf(BETRDialog.mPanel.alpha));
            if (correctionMethod == BETRInitBox.FALSE_NUM) {
                data.addParam("falseNum", String.valueOf(falseNum));
            }
            if (correctionMethod == BETRInitBox.FALSE_PROP) {
                data.addParam("falseProp", String.valueOf((float)falseProp));
            }
            // hcl parameters
            if (isHierarchicalTree) {
                data.addParam("hierarchical-tree", String.valueOf(true));
                data.addParam("draw-sig-trees-only", String.valueOf(drawSigTreesOnly));                
                data.addParam("method-linkage", String.valueOf(hcl_method));
                data.addParam("calculate-genes", String.valueOf(hcl_genes));
                data.addParam("calculate-experiments", String.valueOf(hcl_samples));
                data.addParam("hcl-distance-function", String.valueOf(hcl_function));
                data.addParam("hcl-distance-absolute", String.valueOf(hcl_absolute));
                data.addParam("hcl-genes-ordered", String.valueOf(hcl_genes_ordered));
                data.addParam("hcl-samples-ordered", String.valueOf(hcl_samples_ordered));
            }
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(data);
            long time = System.currentTimeMillis() - start;
            
            // getting the results
            Cluster result_cluster = result.getCluster("cluster");
            //NodeList nodeList = result_cluster.getNodeList();
            //AlgorithmParameters resultMap = result.getParams();
            //int k = 2; //resultMap.getInt("number-of-clusters"); // NEED THIS TO GET THE VALUE OF NUMBER-OF-CLUSTERS
            
       /*     this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }*/
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            
            this.clusters = result.getIntMatrix("sigGenesArrays");
            errorGenesArray[0] = result.getIntArray("error-genes");
            int errorGenesLength = result.getParams().getInt("error-length");
            if (errorGenesLength>0){
            	errorGenes = true;
            }
            /*//FloatMatrix pValuesMatrix = result.getMatrix("pValues");
            FloatMatrix rawPValuesMatrix = result.getMatrix("rawPValues");
            FloatMatrix adjPValuesMatrix = result.getMatrix("adjPValues");
            //FloatMatrix pValuesMatrix = result.getMatrix("pValues");
            FloatMatrix fValuesMatrix = result.getMatrix("fValues");
            FloatMatrix dfNumMatrix = result.getMatrix("dfNumMatrix");
            FloatMatrix dfDenomMatrix = result.getMatrix("dfDenomMatrix");
            FloatMatrix ssGroupsMatrix = result.getMatrix("ssGroupsMatrix");
            FloatMatrix ssErrorMatrix = result.getMatrix("ssErrorMatrix");*/
            FloatMatrix geneTimeMeansMatrix = result.getMatrix("geneTimeMeansMatrix");
            FloatMatrix geneTimeSDsMatrix = result.getMatrix("geneTimeSDsMatrix");
            FloatMatrix geneConditionMeansMatrix=null;
            FloatMatrix geneConditionSDsMatrix=null;
            if (dataDesign == 2){
            	geneConditionMeansMatrix = result.getMatrix("geneConditionMeansMatrix");
            	geneConditionSDsMatrix = result.getMatrix("geneConditionSDsMatrix");
            }
            FloatMatrix pValues = result.getMatrix("pValues");
            
            rawPValues = new Vector<Float>();
            adjPValues = new Vector<Float>();
            fValues = new Vector<Float>();
            ssGroups = new Vector<Float>();
            ssError = new Vector<Float>();
            
            geneTimeMeans = new float[geneTimeMeansMatrix.getRowDimension()][geneTimeMeansMatrix.getColumnDimension()];
            geneTimeSDs = new float[geneTimeSDsMatrix.getRowDimension()][geneTimeSDsMatrix.getColumnDimension()];
            if (dataDesign == 2){
            	geneConditionMeans = new float[geneConditionMeansMatrix.getRowDimension()][geneConditionMeansMatrix.getColumnDimension()];
            	geneConditionSDs = new float[geneConditionSDsMatrix.getRowDimension()][geneConditionSDsMatrix.getColumnDimension()];
            }
            for (int i = 0; i < geneTimeMeans.length; i++) {
                for (int j = 0; j < geneTimeMeans[i].length; j++) {
                    geneTimeMeans[i][j] = geneTimeMeansMatrix.A[i][j];
                    geneTimeSDs[i][j] = geneTimeSDsMatrix.A[i][j];
                }
            }
            if (dataDesign == 2){
	            for (int i = 0; i < geneConditionMeans.length; i++) {
	                for (int j = 0; j < geneConditionMeans[i].length; j++) {
	                    geneConditionMeans[i][j] = geneConditionMeansMatrix.A[i][j];
	                    geneConditionSDs[i][j] = geneConditionSDsMatrix.A[i][j];
	                }
	            }
            }
            
            
            /*
            for (int i = 0; i < rawPValuesMatrix.getRowDimension(); i++) {
                rawPValues.add(new Float(rawPValuesMatrix.A[i][0]));
                adjPValues.add(new Float(adjPValuesMatrix.A[i][0]));
            }
            
            for (int i = 0; i < fValuesMatrix.getRowDimension(); i++) {
                fValues.add(new Float(fValuesMatrix.A[i][0]));
            }
            
            dfNumValues = new Vector();
            dfDenomValues = new Vector();
            
            for (int i = 0; i < dfNumMatrix.getRowDimension(); i++) {
                dfNumValues.add(new Float(dfNumMatrix.A[i][0]));
                dfDenomValues.add(new Float(dfDenomMatrix.A[i][0]));
                ssGroups.add(new Float(ssGroupsMatrix.A[i][0]));
                ssError.add(new Float(ssErrorMatrix.A[i][0]));
            }*/
            
            GeneralInfo info = new GeneralInfo();
            info.time = time;
            //ADD MORE INFO PARAMETERS HERE
            info.alpha = alpha;
            info.usePerms = usePerms;
            info.numPerms = numPerms;
            info.correctionMethod = getSigMethod(correctionMethod);
            /*
            info.pValueBasedOn = getPValueBasedOn(isPermut);
            if (isPermut) {
                info.useAllCombs = useAllCombs;
                info.numCombs = numCombs;
            }
             */
            info.function = menu.getFunctionName(function);
            info.hcl = isHierarchicalTree;
            info.hcl_genes = hcl_genes;
            info.hcl_samples = hcl_samples;
            info.hcl_method = hcl_method;
            
            Vector<String> titlesVector = new Vector<String>();
            for (int i = 0; i < geneTimeMeans[0].length; i++) {
                titlesVector.add("Time " + (i) + " mean");
                titlesVector.add("Time " + (i) + " std.dev");
            }
            if (dataDesign==2){
                for (int i = 0; i < geneConditionMeans[0].length; i++) {
                    titlesVector.add("Condition " + (i+1) + " mean");
                    titlesVector.add("Condition " + (i+1) + " std.dev");
                }
            }
            titlesVector.add("p-Values");
            /*
            titlesVector.add("F ratio");
            titlesVector.add("SS(Groups)");
            titlesVector.add("SS(Error)");
            titlesVector.add("df (Groups)");
            titlesVector.add("df (Error)");
            titlesVector.add("Raw p value");
            if (!((correctionMethod == BETRInitBox.FALSE_NUM)||(correctionMethod == BETRInitBox.FALSE_PROP))) {            
                titlesVector.add("Adj. p value");
            }*/
            
            auxTitles = new String[titlesVector.size()];
            for (int i = 0; i < auxTitles.length; i++) {
                auxTitles[i] = (String)(titlesVector.get(i));
            }
            
            auxData = new Object[experiment.getNumberOfGenes()][auxTitles.length];
            for (int i = 0; i < auxData.length; i++) {
                int counter = 0;
                for (int j = 0; j < geneTimeMeans[i].length; j++) {
                    auxData[i][counter++] = new Float(geneTimeMeans[i][j]);
                    auxData[i][counter++] = new Float(geneTimeSDs[i][j]);
                }
                if (dataDesign==2){
                	for (int j = 0; j < geneConditionMeans[i].length; j++) {
                		auxData[i][counter++] = new Float(geneConditionMeans[i][j]);
                		auxData[i][counter++] = new Float(geneConditionSDs[i][j]);
                	}
                }
                auxData[i][counter++] = pValues.get(i, 0);
                /*
                auxData[i][counter++] = fValues.get(i);
                auxData[i][counter++] = ssGroups.get(i);
                auxData[i][counter++] = ssError.get(i);
                auxData[i][counter++] = dfNumValues.get(i);
                auxData[i][counter++] = dfDenomValues.get(i);
                auxData[i][counter++] = rawPValues.get(i);
                if (!((correctionMethod == BETRInitBox.FALSE_NUM)||(correctionMethod == BETRInitBox.FALSE_PROP))) {
                    auxData[i][counter++] = adjPValues.get(i);
                }*/
            }
            
            
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
        
        this.experiment = framework.getData().getExperiment();
        exptNamesVector = new Vector<String>();
        int number_of_samples = experiment.getNumberOfSamples();
       // int number_of_genes = experiment.getNumberOfGenes();
        
        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(framework.getData().getFullSampleName(experiment.getSampleIndex(i)));
        }
        
        BETRInitBox BETRDialog = new BETRInitBox((JFrame)framework.getFrame(), true, exptNamesVector,framework.getClusterRepository(1));
        BETRDialog.setVisible(true);
        
        if (!BETRDialog.isOkPressed()) return null;
        
        double alpha = BETRDialog.getPValue();
        int dataDesign = BETRDialog.getDataDesign();
        numTimePoints = BETRDialog.getNumTimePoints();

        if (BETRDialog.getTestDesign()==BETRInitBox.CLUSTER_SELECTION){
        	timeAssignments =BETRDialog.getClusterTimeAssignments();
        	if (dataDesign==2)
        		conditionAssignments=BETRDialog.getClusterConditionAssignments();
        }
        if (BETRDialog.getTestDesign()==BETRInitBox.BUTTON_SELECTION){
        	timeAssignments=BETRDialog.getTimeAssignments();
        	if (dataDesign==2)
        		conditionAssignments=BETRDialog.getConditionAssignments();
        }
        if (timeAssignments ==null)
        	return null;
        boolean usePerms = BETRDialog.usePerms();     
        int numPerms = 0;
        if (usePerms) {
            numPerms = BETRDialog.getNumPerms();           
        }
        correctionMethod = BETRDialog.getCorrectionMethod();
        if (correctionMethod == BETRInitBox.FALSE_NUM) {
            falseNum = BETRDialog.getFalseNum();
        }
        if (correctionMethod == BETRInitBox.FALSE_PROP) {
            falseProp = BETRDialog.getFalseProp();
        }        
        boolean isHierarchicalTree = BETRDialog.drawTrees();
        drawSigTreesOnly = true;
        if (isHierarchicalTree) {
            drawSigTreesOnly = BETRDialog.drawSigTreesOnly();
        }         
        
        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.EUCLIDEAN;
        }
        
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        int hcl_function = 4;
        boolean hcl_absolute = false;
        if (isHierarchicalTree) {
            HCLInitDialog hcl_dialog = new HCLInitDialog(framework.getFrame(), menu.getFunctionName(function), menu.isAbsoluteDistance(), true);
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
        
        data.addParam("distance-factor", String.valueOf(1.0f));
        data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
        
        data.addParam("distance-function", String.valueOf(function));
        data.addIntArray("time_assignments", timeAssignments);
        data.addIntArray("condition_assignments", conditionAssignments);
        data.addParam("usePerms", String.valueOf(usePerms));   
        data.addParam("numPerms", String.valueOf(numPerms));
        data.addParam("alpha", String.valueOf(alpha));
        data.addParam("correction-method", String.valueOf(correctionMethod));
        data.addParam("numTimePoints", String.valueOf(numTimePoints));
        if (correctionMethod == BETRInitBox.FALSE_NUM) {
            data.addParam("falseNum", String.valueOf(falseNum));
        }
        if (correctionMethod == BETRInitBox.FALSE_PROP) {
            data.addParam("falseProp", String.valueOf((float)falseProp));
        }       
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
        data.addParam("name", "BETR");
        
        // alg type
        data.addParam("alg-type", "cluster-genes");
        
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
        
        Listener listener = new Listener();
        this.experiment = experiment;
        this.data = framework.getData();
        this.timeAssignments = algData.getIntArray("time_assignments");
        this.conditionAssignments = algData.getIntArray("condition_assignments");
        this.correctionMethod = algData.getParams().getInt("correction-method");
        if (correctionMethod == BETRInitBox.FALSE_NUM) {
            falseNum = algData.getParams().getInt("falseNum");
        }
        if (correctionMethod == BETRInitBox.FALSE_PROP) {
            falseProp = algData.getParams().getFloat("falseProp");
        }        
        this.drawSigTreesOnly = algData.getParams().getBoolean("draw-sig-trees-only");        
        this.rawPValues = new Vector<Float>();
        this.adjPValues=  new Vector<Float>();
        
        exptNamesVector = new Vector<String>();
        int number_of_samples = experiment.getNumberOfSamples();

        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(this.data.getFullSampleName(i));
        }
 
        try {
            algData.addMatrix("experiment", experiment.getMatrix());
            algorithm = framework.getAlgorithmFactory().getAlgorithm("BETR");
            algorithm.addAlgorithmListener(listener);
            
            this.progress = new Progress(framework.getFrame(), "Finding significant genes", listener);
            this.progress.show();
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(algData);
            long time = System.currentTimeMillis() - start;
            
            // getting the results
            Cluster result_cluster = result.getCluster("cluster");
            NodeList nodeList = result_cluster.getNodeList();
            //AlgorithmParameters resultMap = result.getParams();
            int k = 2; //resultMap.getInt("number-of-clusters"); // NEED THIS TO GET THE VALUE OF NUMBER-OF-CLUSTERS
                       
            this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
//            FloatMatrix rawPValuesMatrix = result.getMatrix("rawPValues");
//            FloatMatrix adjPValuesMatrix = result.getMatrix("adjPValues");
//            FloatMatrix fValuesMatrix = result.getMatrix("fValues");
//            FloatMatrix dfNumMatrix = result.getMatrix("dfNumMatrix");
//            FloatMatrix dfDenomMatrix = result.getMatrix("dfDenomMatrix");
//            FloatMatrix ssGroupsMatrix = result.getMatrix("ssGroupsMatrix");
//            FloatMatrix ssErrorMatrix = result.getMatrix("ssErrorMatrix");
//            FloatMatrix geneGroupMeansMatrix = result.getMatrix("geneGroupMeansMatrix");
//            FloatMatrix geneGroupSDsMatrix = result.getMatrix("geneGroupSDsMatrix");
            
            //pValues = new Vector();
            fValues = new Vector<Float>();
            ssGroups = new Vector<Float>();
            ssError = new Vector<Float>();
/*            
            geneGroupMeans = new float[geneGroupMeansMatrix.getRowDimension()][geneGroupMeansMatrix.getColumnDimension()];
            geneGroupSDs = new float[geneGroupSDsMatrix.getRowDimension()][geneGroupSDsMatrix.getColumnDimension()];
            
            for (int i = 0; i < geneGroupMeans.length; i++) {
                for (int j = 0; j < geneGroupMeans[i].length; j++) {
                    geneGroupMeans[i][j] = geneGroupMeansMatrix.A[i][j];
                    geneGroupSDs[i][j] = geneGroupSDsMatrix.A[i][j];
                }
            }
            
            for (int i = 0; i < rawPValuesMatrix.getRowDimension(); i++) {
                rawPValues.add(new Float(rawPValuesMatrix.A[i][0]));
                adjPValues.add(new Float(adjPValuesMatrix.A[i][0]));
            }
            
            for (int i = 0; i < fValuesMatrix.getRowDimension(); i++) {
                fValues.add(new Float(fValuesMatrix.A[i][0]));
            }
            
            dfNumValues = new Vector();
            dfDenomValues = new Vector();
            
            for (int i = 0; i < dfNumMatrix.getRowDimension(); i++) {
                dfNumValues.add(new Float(dfNumMatrix.A[i][0]));
                dfDenomValues.add(new Float(dfDenomMatrix.A[i][0]));
                ssGroups.add(new Float(ssGroupsMatrix.A[i][0]));
                ssError.add(new Float(ssErrorMatrix.A[i][0]));
            }*/
            
            AlgorithmParameters params = algData.getParams();
            
            GeneralInfo info = new GeneralInfo();
            info.time = time;
            //ADD MORE INFO PARAMETERS HERE
            info.alpha = params.getFloat("alpha");
            numTimePoints = params.getInt("numTimePoints");
            info.correctionMethod = getSigMethod(params.getInt("correction-method"));
            info.usePerms = params.getBoolean("usePerms");
            info.numPerms = params.getInt("numPerms");
            /*
            info.pValueBasedOn = getPValueBasedOn(isPermut);
            if (isPermut) {
                info.useAllCombs = useAllCombs;
                info.numCombs = numCombs;
            }
             */
            info.function = framework.getDistanceMenu().getFunctionName(params.getInt("distance-function"));
            info.hcl = params.getBoolean("hierarchical-tree");
            info.hcl_genes = params.getBoolean("calculate-genes");
            info.hcl_samples = params.getBoolean("calculate-experiments");
            if(info.hcl)
                info.hcl_method = params.getInt("method-linkage") ;
            
            Vector<String> titlesVector = new Vector<String>();
            for (int i = 0; i < geneTimeMeans[0].length; i++) {
                titlesVector.add("Group" + (i+1) + " mean");
                titlesVector.add("Group" + (i + 1) + " std.dev");
            }/*
            titlesVector.add("F ratio");
            titlesVector.add("SS(Groups)");
            titlesVector.add("SS(Error)");
            titlesVector.add("df (Groups)");
            titlesVector.add("df (Error)");
            titlesVector.add("Raw p value");
            if (!((correctionMethod == BETRInitBox.FALSE_NUM)||(correctionMethod == BETRInitBox.FALSE_PROP))) {            
                titlesVector.add("Adj. p value");
            }*/
            
            auxTitles = new String[titlesVector.size()];
            for (int i = 0; i < auxTitles.length; i++) {
                auxTitles[i] = (String)(titlesVector.get(i));
            }
            
            auxData = new Object[experiment.getNumberOfGenes()][auxTitles.length];
            for (int i = 0; i < auxData.length; i++) {
                int counter = 0;
                for (int j = 0; j < geneTimeMeans[i].length; j++) {
                    auxData[i][counter++] = new Float(geneTimeMeans[i][j]);
                    auxData[i][counter++] = new Float(geneTimeSDs[i][j]);
                }
                for (int j = 0; j < geneConditionMeans[i].length; j++) {
                    auxData[i][counter++] = new Float(geneConditionMeans[i][j]);
                    auxData[i][counter++] = new Float(geneConditionSDs[i][j]);
                }
                
                /*
                
                auxData[i][counter++] = fValues.get(i);
                auxData[i][counter++] = ssGroups.get(i);
                auxData[i][counter++] = ssError.get(i);
                auxData[i][counter++] = dfNumValues.get(i);
                auxData[i][counter++] = dfDenomValues.get(i);
                auxData[i][counter++] = rawPValues.get(i);
                if (!((correctionMethod == BETRInitBox.FALSE_NUM)||(correctionMethod == BETRInitBox.FALSE_PROP))) {
                    auxData[i][counter++] = adjPValues.get(i);
                }*/
            }
            
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
    
    
    protected String getSigMethod(int sigMethod) {
        String methodName = "";
        
        if (sigMethod == BETRInitBox.JUST_ALPHA) {
            methodName = "Just alpha (uncorrected)";
        } else if (sigMethod == BETRInitBox.STD_BONFERRONI) {
            methodName = "Standard Bonferroni correction";
        } else if (sigMethod == BETRInitBox.ADJ_BONFERRONI) {
            methodName = "Adjusted Bonferroni correction";
        } else if (sigMethod == BETRInitBox.MAX_T) {
            methodName = "Westfall Young stepdown - MaxT";
        } else if (sigMethod == BETRInitBox.FALSE_NUM) {
            methodName = "False significant number: " + falseNum + " or less";
        } else if (sigMethod == BETRInitBox.FALSE_PROP) {
            methodName = "False significant proportion: " + falseProp + " or less";
        }
        
        return methodName;
    }
    
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    protected DefaultMutableTreeNode createResultTree(Cluster result_cluster, GeneralInfo info) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("BETR");
        addResultNodes(root, result_cluster, info);
        return root;
    }
    
    /**
     * Adds result nodes into the tree root.
     */
    protected void addResultNodes(DefaultMutableTreeNode root, Cluster result_cluster, GeneralInfo info) {
        addExpressionImages(root);
        addHierarchicalTrees(root, result_cluster, info);
        addCentroidViews(root);
        addTableViews(root);
        addClusterInfo(root);
        //addFRatioInfoViews(root);
        addGeneralInfo(root, info);
    }
    
    protected void addTableViews(DefaultMutableTreeNode root) {

        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table Views");
        IViewer tabViewer = new ClusterTableViewer(this.experiment, this.clusters, this.data, this.auxTitles, this.auxData);
        //IViewer tabViewer = new ClusterTableViewer(this.experiment, this.clusters, this.data, null, null);
        for (int i=0; i<this.clusters.length; i++) {
            if (i < this.clusters.length - 1) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", tabViewer, new Integer(i))));
            } else if (i == this.clusters.length - 1) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", tabViewer, new Integer(i))));
                
            }
        }
        root.add(node);
    }
    
    /**
     * Adds nodes to display clusters data.
     */
    protected void addExpressionImages(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer = new BETRExperimentViewer(this.experiment, clusters, null, null, null, null, null, null, null, null, null);
        //IViewer expViewer = new BETRExperimentViewer(this.experiment, this.clusters, geneGroupMeans, geneGroupSDs, rawPValues, adjPValues, fValues, ssGroups, ssError, dfNumValues, dfDenomValues);
        for (int i=0; i<this.clusters.length; i++) {
            if (i < this.clusters.length - 1) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", expViewer, new Integer(i))));
            } else if (i == this.clusters.length - 1) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", expViewer, new Integer(i))));
                
            }
        }
        if (errorGenes){
        	IViewer errorGenesIViewer = new BETRExperimentViewer(this.experiment, errorGenesArray, null, null, null, null, null, null, null, null, null);
        	node.add(new DefaultMutableTreeNode(new LeafInfo("Invalid Genes ", errorGenesIViewer, new Integer(0))));
        }
        root.add(node);
    }
    
    /**
     * Adds nodes to display hierarchical trees.
     */
    protected void addHierarchicalTrees(DefaultMutableTreeNode root, Cluster result_cluster, GeneralInfo info) {
        if (!info.hcl) {
            return;
        }
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Hierarchical Trees");
        NodeList nodeList = result_cluster.getNodeList();
        if (!drawSigTreesOnly) {        
            for (int i=0; i<nodeList.getSize(); i++) {
                if (i < nodeList.getSize() - 1 ) {
                    node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", createHCLViewer(nodeList.getNode(i), info))));
                } else if (i == nodeList.getSize() - 1) {
                    node.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", createHCLViewer(nodeList.getNode(i), info))));
                }
            }
        } else {
            node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", createHCLViewer(nodeList.getNode(0), info))));            
        }
        root.add(node);
    }
    
    /**
     * Creates an <code>HCLViewer</code>.
     */
    protected IViewer createHCLViewer(Node clusterNode, GeneralInfo info) {
        HCLTreeData genes_result = info.hcl_genes ? getResult(clusterNode, 0) : null;
        HCLTreeData samples_result = info.hcl_samples ? getResult(clusterNode, info.hcl_genes ? 4 : 0) : null;
        return new HCLViewer(this.experiment, clusterNode.getFeaturesIndexes(), genes_result, samples_result);
    }
    
    /**
     * Returns a hcl tree data from the specified cluster node.
     */
    protected HCLTreeData getResult(Node clusterNode, int pos) {
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
    protected void addClusterInfo(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Cluster Information");
        node.add(new DefaultMutableTreeNode(new LeafInfo("Results (#,%)", new BETRInfoViewer(this.clusters, this.experiment.getNumberOfGenes()))));
        root.add(node);
    }
    
    /**
     * Adds nodes to display centroid charts.
     */
    protected void addCentroidViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        //BETRCentroidViewer centroidViewer = new BETRCentroidViewer(this.experiment, clusters, geneGroupMeans, geneGroupSDs, rawPValues, adjPValues, fValues, ssGroups, ssError, dfNumValues, dfDenomValues);
        BETRCentroidViewer centroidViewer = new BETRCentroidViewer(this.experiment, clusters, null, null, null, null, null, null, null, null, null);
        centroidViewer.setMeans(this.means.A);
        centroidViewer.setVariances(this.variances.A);
        for (int i=0; i<this.clusters.length; i++) {
            
            if (i == 0) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            } else if (i == 1) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
                
            }
        }
        
        BETRCentroidsViewer centroidsViewer = new BETRCentroidsViewer(this.experiment, clusters, geneTimeMeans, geneTimeSDs, rawPValues, adjPValues, fValues, ssGroups, ssError, dfNumValues, dfDenomValues);

        centroidsViewer.setMeans(this.means.A);
        centroidsViewer.setVariances(this.variances.A);
        
        centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
        expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        /*if (errorGenes){
        	IViewer errorGenesIViewer = new BETRExperimentViewer(this.experiment, errorGenesArray, null, null, null, null, null, null, null, null, null);
        	centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Invalid Genes ", errorGenesIViewer, new Integer(0))));
        }
        if (errorGenes){
        	IViewer errorGenesIViewer = new BETRExperimentViewer(this.experiment, errorGenesArray, null, null, null, null, null, null, null, null, null);
        	expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Invalid Genes ", errorGenesIViewer, new Integer(0))));
        }*/
        root.add(centroidNode);
        root.add(expressionNode);
    }
    
    /*
    protected void addFRatioInfoViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode fRatioInfoNode = new DefaultMutableTreeNode("F-Ratio information");
        IViewer fSigViewer = new FStatsTableViewer(this.experiment, this.clusters, this.data, geneGroupMeans, geneGroupSDs, pValues, fValues, ssGroups, ssError, dfNumValues, dfDenomValues, true);
        IViewer fNonSigViewer = new FStatsTableViewer(this.experiment, this.clusters, this.data, geneGroupMeans, geneGroupSDs, pValues, fValues, ssGroups, ssError, dfNumValues, dfDenomValues, false);
        
        fRatioInfoNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes", fSigViewer)));
        fRatioInfoNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes", fNonSigViewer)));
        
        root.add(fRatioInfoNode);
    }
     */
    
    /**
     * Adds node with general iformation.
     */
    protected void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        node.add(getTimeAssignmentInfo());
        if (dataDesign==2)
        	node.add(getConditionAssignmentInfo());
        //node.add(new DefaultMutableTreeNode("Alpha (overall threshold p-value): "+info.alpha));
        if (info.correctionMethod.startsWith("False")) {
            node.add(new DefaultMutableTreeNode("Confidence (1 - alpha) : "+(1d - info.alpha)*100 + " %"));
        } else {
            node.add(new DefaultMutableTreeNode("Alpha (overall threshold p-value): "+info.alpha));
        }        
        //node.add(new DefaultMutableTreeNode("Used permutation test? " + info.usePerms));
        if (info.usePerms) {
            //node.add(new DefaultMutableTreeNode("Number of permutations " + info.numPerms));
        }
        /*
        node.add(new DefaultMutableTreeNode("P-values based on: "+info.pValueBasedOn));
        if (isPermutations) {
            node.add(new DefaultMutableTreeNode("All permutations used: " + info.useAllCombs));
            node.add(new DefaultMutableTreeNode("Number of permutations per gene: " + info.numCombs));
        }
         */
        if (info.correctionMethod.startsWith("False")) {
           node.add(new DefaultMutableTreeNode(info.correctionMethod)); 
        } else {
            node.add(new DefaultMutableTreeNode("Significance determined by: "+info.correctionMethod));
        }        
        //node.add(new DefaultMutableTreeNode("Significance determined by: "+info.correctionMethod));
        node.add(new DefaultMutableTreeNode("HCL: "+info.getMethodName()));
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time-1)+" ms"));
        node.add(new DefaultMutableTreeNode(info.function));
        root.add(node);
    }
    
    protected DefaultMutableTreeNode getTimeAssignmentInfo() {
        DefaultMutableTreeNode groupAssignmentInfo = new DefaultMutableTreeNode("Time assignments ");
        DefaultMutableTreeNode notInGroups = new DefaultMutableTreeNode("Not in groups");
        DefaultMutableTreeNode[] groups = new DefaultMutableTreeNode[numTimePoints];
        for (int i = 0; i < numTimePoints; i++) {
            groups[i] = new DefaultMutableTreeNode("Time " + (i));
            
        }
        
        for (int i = 0; i < timeAssignments.length; i++) {
            int currentGroup = timeAssignments[i];
            if (currentGroup == 0) {
                notInGroups.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
            } else {
                groups[currentGroup - 1].add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
            }
        }
        
        for (int i = 0; i < groups.length; i++) {
            groupAssignmentInfo.add(groups[i]);
        }
        if (notInGroups.getChildCount() > 0) {
            groupAssignmentInfo.add(notInGroups);
        }
        return groupAssignmentInfo;
    }
    
    protected DefaultMutableTreeNode getConditionAssignmentInfo() {
        DefaultMutableTreeNode groupAssignmentInfo = new DefaultMutableTreeNode("Condition assignments ");
        DefaultMutableTreeNode notInGroups = new DefaultMutableTreeNode("Not in groups");
        DefaultMutableTreeNode[] groups = new DefaultMutableTreeNode[2];
        for (int i = 0; i < 2; i++) {
            groups[i] = new DefaultMutableTreeNode("Condition " + (i + 1));
            
        }
        
        for (int i = 0; i < conditionAssignments.length; i++) {
            int currentGroup = conditionAssignments[i];
            if (currentGroup == 0) {
                notInGroups.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
            } else {
                groups[currentGroup - 1].add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
            }
        }
        
        for (int i = 0; i < groups.length; i++) {
            groupAssignmentInfo.add(groups[i]);
        }
        if (notInGroups.getChildCount() > 0) {
            groupAssignmentInfo.add(notInGroups);
        }
        return groupAssignmentInfo;
    }
    
    
    /**
     * The class to listen to progress, monitor and algorithms events.
     */
    protected class Listener extends DialogListener implements AlgorithmListener {
    	//EH added so AMP could extend this class
        protected Listener(){super();}
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
    
    protected class GeneralInfo {

        public int clusters;
        public String correctionMethod;
        //public String pValueBasedOn;
        public float alpha;
        //public int numCombs;
        //public boolean useAllCombs;
        public long time;
        public String function;
        //public int numReps;
        //public double thresholdPercent;
        
        protected boolean hcl, usePerms;
        protected int hcl_method, numPerms;
        protected boolean hcl_genes;
        protected boolean hcl_samples;
    	//EH constructor added so AMP could extend
        protected GeneralInfo(){
    		super();
    	}        
        public String getMethodName() {
            return hcl ? HCLGUI.GeneralInfo.getMethodName(hcl_method) : "no linkage";
        }
        
    }
    
}
