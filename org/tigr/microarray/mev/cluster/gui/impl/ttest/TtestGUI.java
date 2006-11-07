/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: TtestGUI.java,v $
 * $Revision: 1.13 $
 * $Date: 2006-11-07 17:27:40 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.ttest;

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
 * @author  nbhagaba
 * @version
 */
public class TtestGUI implements IClusterGUI, IScriptGUI {
    
    protected Algorithm algorithm;
    protected Progress progress;
    //protected Monitor monitor;
    
    protected Experiment experiment;
    protected int[][] clusters;
    protected FloatMatrix means;
    protected FloatMatrix variances;
    
    protected String[] auxTitles;
    protected Object[][] auxData;
    
    //protected Vector sigTValues, nonSigTValues, sigPValues, nonSigPValues, additionalHeaders, additionalSigOutput, additionalNonSigOutput;
    protected Vector tValues, rawPValues, adjPValues, dfValues, meansA, meansB, sdA, sdB, oneClassMeans, oneClassSDs;
    protected Vector pairedGroupAExpts, pairedGroupBExpts;
    protected IData data;
    protected Vector exptNamesVector;
    protected int[] groupAssignments;
    public int tTestDesign, falseNum;
    protected double oneClassMean, falseProp;
    protected boolean isPermutations, useWelchDf, drawSigTreesOnly, doFastFDRApprox, calculateAdjFDRPVals;
    protected boolean[] isSig;
    protected double[] diffMeansBA, negLog10PValues;
    //JFrame tTestFrame;
    public static JFrame TtestFrame;
    public TtestGUI() {
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
        //int k = 2;
        
        this.experiment = framework.getData().getExperiment();
        //tTestFrame = (JFrame)framework.getFrame();
        exptNamesVector = new Vector();
        this.data = framework.getData();
        int number_of_samples = experiment.getNumberOfSamples();
        int number_of_genes = experiment.getNumberOfGenes();
        
        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(framework.getData().getSampleName(experiment.getSampleIndex(i)));
        }
        
        TtestInitDialog ttDialog = new TtestInitDialog((JFrame) framework.getFrame(), true, exptNamesVector);
        ttDialog.setVisible(true);
        
        if (!ttDialog.isOkPressed()) return null;
        
        double alpha = 0.01d;
        try {
            alpha = ttDialog.getAlphaValue();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Invalid alpha value!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        tTestDesign = ttDialog.getTestDesign();
        oneClassMean = 0;
        if (tTestDesign == TtestInitDialog.ONE_CLASS) {
            groupAssignments = ttDialog.getOneClassAssignments();
            oneClassMean = ttDialog.getOneClassMean();
        } else if (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) {
            groupAssignments = ttDialog.getGroupAssignments();
        } else if (tTestDesign == TtestInitDialog.PAIRED) {
            pairedGroupAExpts = ttDialog.getPairedAExpts();
            pairedGroupBExpts = ttDialog.getPairedBExpts();
        }
        int significanceMethod = ttDialog.getSignificanceMethod();
        if (significanceMethod == TtestInitDialog.FALSE_NUM) {
            falseNum = ttDialog.getFalseNum();
            calculateAdjFDRPVals = ttDialog.calculateFDRPVals();
            doFastFDRApprox = ttDialog.doFastFDRApprox();
        }
        if (significanceMethod == TtestInitDialog.FALSE_PROP) {
            falseProp = ttDialog.getFalseProp();
            calculateAdjFDRPVals = ttDialog.calculateFDRPVals();
            doFastFDRApprox = ttDialog.doFastFDRApprox();
        }        
        boolean isHierarchicalTree = ttDialog.isDrawTrees();
        drawSigTreesOnly = true;
        if (isHierarchicalTree) {
            drawSigTreesOnly = ttDialog.drawSigTreesOnly();
        }
        boolean isPermut = ttDialog.isPermut();
        isPermutations = isPermut;
        int numCombs = ttDialog.getUserNumCombs();
        boolean useAllCombs = ttDialog.useAllCombs();
        useWelchDf = ttDialog.useWelchDf();
        
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        
        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.EUCLIDEAN;
        }
        
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
        
        Listener listener = new Listener();
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("TTEST");
            algorithm.addAlgorithmListener(listener);
            
            int genes = experiment.getNumberOfGenes();
            //this.monitor = new Monitor(framework.getFrame(), "Reallocations", 25, 100, 210.0/genes);
            //this.monitor.setStepXFactor((int)Math.floor(245/iterations));
            //this.monitor.update(genes);
            //this.monitor.show();
            
            this.progress = new Progress(framework.getFrame(), "Finding significant genes", listener);
            this.progress.show();
            
            AlgorithmData data = new AlgorithmData();
            
            data.addMatrix("experiment", experiment.getMatrix());
            data.addParam("distance-factor", String.valueOf(1.0f));
            data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
            
            data.addParam("distance-function", String.valueOf(function));
            data.addParam("tTestDesign", String.valueOf(tTestDesign));
            if ((tTestDesign == TtestInitDialog.ONE_CLASS) || (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS)) {
                data.addIntArray("group-assignments", groupAssignments);
            }
            if (tTestDesign == TtestInitDialog.ONE_CLASS) {
                data.addParam("oneClassMean", String.valueOf(oneClassMean));
            }
            if (tTestDesign == TtestInitDialog.PAIRED) {
                FloatMatrix pairedAExptsMatrix = new FloatMatrix(pairedGroupAExpts.size(), 1);
                FloatMatrix pairedBExptsMatrix = new FloatMatrix(pairedGroupBExpts.size(), 1);
                
                for (int i = 0; i < pairedGroupAExpts.size(); i++) {
                    pairedAExptsMatrix.A[i][0] = ((Integer)(pairedGroupAExpts.get(i))).floatValue();
                    pairedBExptsMatrix.A[i][0] = ((Integer)(pairedGroupBExpts.get(i))).floatValue();
                }
                data.addMatrix("pairedAExptsMatrix", pairedAExptsMatrix);
                data.addMatrix("pairedBExptsMatrix", pairedBExptsMatrix);                
            }            
            data.addParam("alpha", String.valueOf(alpha));
            data.addParam("significance-method", String.valueOf(significanceMethod));
            if (significanceMethod == TtestInitDialog.FALSE_NUM) {
                data.addParam("falseNum", String.valueOf(falseNum));
                data.addParam("calculateAdjFDRPVals", String.valueOf(calculateAdjFDRPVals));
                data.addParam("useFastFDRApprox", String.valueOf(doFastFDRApprox));
            }
            if (significanceMethod == TtestInitDialog.FALSE_PROP) {
                data.addParam("falseProp", String.valueOf((float)falseProp));
                data.addParam("calculateAdjFDRPVals", String.valueOf(calculateAdjFDRPVals));
                data.addParam("useFastFDRApprox", String.valueOf(doFastFDRApprox));
            }            
            data.addParam("is-permut", String.valueOf(isPermut));
            data.addParam("num-combs", String.valueOf(numCombs));
            data.addParam("use-all-combs", String.valueOf(useAllCombs));
            data.addParam("useWelchDf", String.valueOf(useWelchDf));
            
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
            AlgorithmParameters resultMap = result.getParams();
            int k = 2; //resultMap.getInt("number-of-clusters"); // NEED THIS TO GET THE VALUE OF NUMBER-OF-CLUSTERS
            
            this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            FloatMatrix sigPValuesMatrix = result.getMatrix("sigPValues");
            FloatMatrix sigTValuesMatrix = result.getMatrix("sigTValues");
            FloatMatrix nonSigPValuesMatrix = result.getMatrix("nonSigPValues");
            FloatMatrix nonSigTValuesMatrix = result.getMatrix("nonSigTValues");
            FloatMatrix rawPValuesMatrix = result.getMatrix("rawPValues");
            FloatMatrix adjPValuesMatrix = result.getMatrix("adjPValues");
            FloatMatrix tValuesMatrix = result.getMatrix("tValues");
            FloatMatrix dfMatrix = result.getMatrix("dfValues");
            FloatMatrix meansAMatrix = result.getMatrix("meansAMatrix");
            FloatMatrix meansBMatrix = result.getMatrix("meansBMatrix");
            FloatMatrix sdAMatrix = result.getMatrix("sdAMatrix");
            FloatMatrix sdBMatrix = result.getMatrix("sdBMatrix");
            FloatMatrix isSigMatrix = result.getMatrix("isSigMatrix");
            FloatMatrix oneClassMeansMatrix = result.getMatrix("oneClassMeansMatrix");
            FloatMatrix oneClassSDsMatrix = result.getMatrix("oneClassSDsMatrix");
            
            rawPValues = new Vector();
            adjPValues = new Vector();
            tValues = new Vector();
            dfValues = new Vector();
            meansA = new Vector();
            meansB = new Vector();
            sdA = new Vector();
            sdB = new Vector();
            oneClassMeans = new Vector();
            oneClassSDs = new Vector();
            
            for (int i = 0; i < rawPValuesMatrix.getRowDimension(); i++) {
                rawPValues.add(new Float(rawPValuesMatrix.A[i][0]));
                adjPValues.add(new Float(adjPValuesMatrix.A[i][0]));
            }
            
            for (int i = 0; i < tValuesMatrix.getRowDimension(); i++) {
                tValues.add(new Float(tValuesMatrix.A[i][0]));
            }
            
            for (int i = 0; i < dfMatrix.getRowDimension(); i++) {
                dfValues.add(new Float(dfMatrix.A[i][0]));
            }
            
            if (tTestDesign == TtestInitDialog.ONE_CLASS) {
                for (int i = 0; i < oneClassMeansMatrix.getRowDimension(); i++) {
                    oneClassMeans.add(new Float(oneClassMeansMatrix.A[i][0]));
                }
                
                for (int i = 0; i < oneClassSDsMatrix.getRowDimension(); i++) {
                    oneClassSDs.add(new Float(oneClassSDsMatrix.A[i][0]));
                }
            }
            
            if ((tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) || (tTestDesign == TtestInitDialog.PAIRED)) {
                
                for (int i = 0; i < meansAMatrix.getRowDimension(); i++) {
                    meansA.add(new Float(meansAMatrix.A[i][0]));
                    meansB.add(new Float(meansBMatrix.A[i][0]));
                    sdA.add(new Float(sdAMatrix.A[i][0]));
                    sdB.add(new Float(sdBMatrix.A[i][0]));
                }
                
                isSig = new boolean[isSigMatrix.getRowDimension()];
                
                for (int i = 0; i < isSig.length; i++) {
                    if (isSigMatrix.A[i][0] == 1.0f) {
                        isSig[i] = true;
                    } else {
                        isSig[i] = false;
                    }
                }
                
                diffMeansBA = new double[isSigMatrix.getRowDimension()];
                for (int i = 0; i < diffMeansBA.length; i++) {
                    diffMeansBA[i] = (double)(meansBMatrix.A[i][0]) - (double)(meansAMatrix.A[i][0]);
                }
                
                negLog10PValues = new double[isSigMatrix.getRowDimension()];
                
                double log10BaseE = Math.log(10);
                
                for (int i = 0; i < negLog10PValues.length; i++) {
                    double currentP = (double)(adjPValuesMatrix.A[i][0]);
                    negLog10PValues[i] = (-1)*((Math.log(currentP))/log10BaseE);
                    //System.out.println("i = " + i + ", currentP = " + currentP + ", negLog10P = " + negLog10PValues[i]);
                }
            }
            
            if (tTestDesign == TtestInitDialog.ONE_CLASS) {
                
                isSig = new boolean[isSigMatrix.getRowDimension()];
                
                for (int i = 0; i < isSig.length; i++) {
                    if (isSigMatrix.A[i][0] == 1.0f) {
                        isSig[i] = true;
                    } else {
                        isSig[i] = false;
                    }
                }
                
                diffMeansBA = new double[isSigMatrix.getRowDimension()];
                for (int i = 0; i < diffMeansBA.length; i++) {
                    diffMeansBA[i] = (double)((oneClassMeansMatrix.A[i][0]) - oneClassMean);
                }
                
                negLog10PValues = new double[isSigMatrix.getRowDimension()];
                
                double log10BaseE = Math.log(10);
                
                for (int i = 0; i < negLog10PValues.length; i++) {
                    double currentP = (double)(adjPValuesMatrix.A[i][0]);
                    negLog10PValues[i] = (-1)*((Math.log(currentP))/log10BaseE);
                    //System.out.println("i = " + i + ", currentP = " + currentP + ", negLog10P = " + negLog10PValues[i]);
                }                
            }            
            
            /*
            if (tTestDesign == TtestInitDialog.PAIRED) {
                return null; //for now
            }
             */
            
            
            GeneralInfo info = new GeneralInfo();
            
            info.time = time;
            //ADD MORE INFO PARAMETERS HERE
            info.alpha = alpha;
            info.sigMethod = getSigMethod(significanceMethod);
            info.pValueBasedOn = getPValueBasedOn(isPermut);
            if (isPermut) {
                info.useAllCombs = useAllCombs;
                info.numCombs = numCombs;
            }
            info.function = menu.getFunctionName(function);
            info.hcl = isHierarchicalTree;
            info.hcl_genes = hcl_genes;
            info.hcl_samples = hcl_samples;
            info.hcl_method = hcl_method;
            
            Vector titlesVector = new Vector();
            if ((tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) || (tTestDesign == TtestInitDialog.PAIRED)) {
                titlesVector.add("GroupA mean");
                titlesVector.add("GroupA std.dev.");
                titlesVector.add("GroupB mean");
                titlesVector.add("GroupB std.dev.");
                titlesVector.add("Absolute t value");
            } else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
                titlesVector.add("Gene mean");
                titlesVector.add("Gene std.dev.");
                titlesVector.add("t value");
            }
            titlesVector.add("Degrees of freedom");
            titlesVector.add("Raw p value");
            //titlesVector.add("Adj p value");
            if ((significanceMethod == TtestInitDialog.FALSE_NUM)||(significanceMethod == TtestInitDialog.FALSE_PROP)) {
                if (calculateAdjFDRPVals)
                    titlesVector.add("Adj p value");
            } else {
                titlesVector.add("Adj p value");
            }            
            
            auxTitles = new String[titlesVector.size()];
            for (int i = 0; i < auxTitles.length; i++) {
                auxTitles[i] = (String)(titlesVector.get(i));
            }
            
            auxData = new Object[experiment.getNumberOfGenes()][auxTitles.length];
            for (int i = 0; i < auxData.length; i++) {
                int counter = 0;
                if ((tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) || (tTestDesign == TtestInitDialog.PAIRED)) {
                    auxData[i][counter++] = meansA.get(i);
                    auxData[i][counter++] = sdA.get(i);
                    auxData[i][counter++] = meansB.get(i);
                    auxData[i][counter++] = sdB.get(i);
                } else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
                    auxData[i][counter++] = oneClassMeans.get(i);
                    auxData[i][counter++] = oneClassSDs.get(i);
                }
                auxData[i][counter++] = tValues.get(i);
                auxData[i][counter++] = dfValues.get(i);
                auxData[i][counter++] = rawPValues.get(i);
                if ((significanceMethod == TtestInitDialog.FALSE_NUM)||(significanceMethod == TtestInitDialog.FALSE_PROP)) {
                    if (calculateAdjFDRPVals)
                        auxData[i][counter++] = adjPValues.get(i);
                } else {
                    auxData[i][counter++] = adjPValues.get(i);
                }
            }
            
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
        
        //return null; //FOR NOW
    }
    
    
    /*********
     *
     *  Script Implementation
     *
     */
    
    public AlgorithmData getScriptParameters(IFramework framework) {
        this.experiment = framework.getData().getExperiment();
        exptNamesVector = new Vector();
        this.data = framework.getData();
        int number_of_samples = experiment.getNumberOfSamples();
        int number_of_genes = experiment.getNumberOfGenes();
        
        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(framework.getData().getSampleName(experiment.getSampleIndex(i)));
        }
        
        TtestInitDialog ttDialog = new TtestInitDialog((JFrame) framework.getFrame(), true, exptNamesVector);
        ttDialog.setVisible(true);
        
        if (!ttDialog.isOkPressed()) return null;
        
        double alpha = 0.01d;
        try {
            alpha = ttDialog.getAlphaValue();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Invalid alpha value!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        tTestDesign = ttDialog.getTestDesign();
        oneClassMean = 0;
        if (tTestDesign == TtestInitDialog.ONE_CLASS) {
            groupAssignments = ttDialog.getOneClassAssignments();
            oneClassMean = ttDialog.getOneClassMean();
        } else if (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) {
            groupAssignments = ttDialog.getGroupAssignments();
        } else if (tTestDesign == TtestInitDialog.PAIRED) {
            pairedGroupAExpts = ttDialog.getPairedAExpts();
            pairedGroupBExpts = ttDialog.getPairedBExpts();
        }
        int significanceMethod = ttDialog.getSignificanceMethod();
        if (significanceMethod == TtestInitDialog.FALSE_NUM) {
            falseNum = ttDialog.getFalseNum();
            System.out.println("getScriptParams: falseNum = " + falseNum);
            calculateAdjFDRPVals = ttDialog.calculateFDRPVals();
            doFastFDRApprox = ttDialog.doFastFDRApprox();
        }
        if (significanceMethod == TtestInitDialog.FALSE_PROP) {
            falseProp = ttDialog.getFalseProp();
            System.out.println("getScriptParams: falseProp = " + falseProp);
            calculateAdjFDRPVals = ttDialog.calculateFDRPVals();
            doFastFDRApprox = ttDialog.doFastFDRApprox();
        }        
        boolean isHierarchicalTree = ttDialog.isDrawTrees();
        drawSigTreesOnly = true;
        if (isHierarchicalTree) {
            drawSigTreesOnly = ttDialog.drawSigTreesOnly();
        }        
        boolean isPermut = ttDialog.isPermut();
        useWelchDf = ttDialog.useWelchDf();
        isPermutations = isPermut;
        int numCombs = ttDialog.getUserNumCombs();
        boolean useAllCombs = ttDialog.useAllCombs();
        

        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.EUCLIDEAN;
        }
        
        // hcl init
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

        
        int genes = experiment.getNumberOfGenes();
        
        AlgorithmData data = new AlgorithmData();
        
        data.addParam("distance-factor", String.valueOf(1.0f));
        data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));                
        data.addParam("distance-function", String.valueOf(function));
        data.addParam("tTestDesign", String.valueOf(tTestDesign));
        if ((tTestDesign == TtestInitDialog.ONE_CLASS) || (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS)) {
            data.addIntArray("group-assignments", groupAssignments);
        }
  
        if (tTestDesign == TtestInitDialog.ONE_CLASS) {
            data.addParam("oneClassMean", String.valueOf(oneClassMean));
        }
        
        if (tTestDesign == TtestInitDialog.PAIRED) {
            FloatMatrix pairedAExptsMatrix = new FloatMatrix(pairedGroupAExpts.size(), 1);
            FloatMatrix pairedBExptsMatrix = new FloatMatrix(pairedGroupBExpts.size(), 1);
            
            for (int i = 0; i < pairedGroupAExpts.size(); i++) {
                pairedAExptsMatrix.A[i][0] = ((Integer)(pairedGroupAExpts.get(i))).floatValue();
                pairedBExptsMatrix.A[i][0] = ((Integer)(pairedGroupBExpts.get(i))).floatValue();
            }
            data.addMatrix("pairedAExptsMatrix", pairedAExptsMatrix);
            data.addMatrix("pairedBExptsMatrix", pairedBExptsMatrix);
        }
        data.addParam("alpha", String.valueOf(alpha));
        data.addParam("significance-method", String.valueOf(significanceMethod));
        if (significanceMethod == TtestInitDialog.FALSE_NUM) {
            data.addParam("falseNum", String.valueOf(falseNum));
            data.addParam("calculateAdjFDRPVals", String.valueOf(calculateAdjFDRPVals));
            data.addParam("useFastFDRApprox", String.valueOf(doFastFDRApprox));
        }
        if (significanceMethod == TtestInitDialog.FALSE_PROP) {
            data.addParam("falseProp", String.valueOf((float)falseProp));
            data.addParam("calculateAdjFDRPVals", String.valueOf(calculateAdjFDRPVals));
            data.addParam("useFastFDRApprox", String.valueOf(doFastFDRApprox));
        }        
        data.addParam("is-permut", String.valueOf(isPermut));
        data.addParam("num-combs", String.valueOf(numCombs));
        data.addParam("use-all-combs", String.valueOf(useAllCombs));
        data.addParam("useWelchDf", String.valueOf(useWelchDf));        
        
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
        data.addParam("name", "TTEST");
        
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
        
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("TTEST");
            algorithm.addAlgorithmListener(listener);
            
            this.experiment = experiment;
            this.data = framework.getData();
            
            this.groupAssignments = algData.getIntArray("group-assignments");
            this.isPermutations = algData.getParams().getBoolean("is-permut");
            this.useWelchDf = algData.getParams().getBoolean("useWelchDf");
            this.drawSigTreesOnly = algData.getParams().getBoolean("draw-sig-trees-only");
            this.exptNamesVector = new Vector();
            int number_of_samples = experiment.getNumberOfSamples();
            for (int i = 0; i < number_of_samples; i++) {
                exptNamesVector.add(this.data.getSampleName(i));
            }
      
            int significanceMethod = algData.getParams().getInt("significance-method");
            algData.addMatrix("experiment", experiment.getMatrix());
            
            this.tTestDesign = algData.getParams().getInt("tTestDesign");            
            System.out.println("reached upto here");
            if (significanceMethod == TtestInitDialog.FALSE_NUM) {
                this.falseNum = algData.getParams().getInt("falseNum");
                System.out.println("executeScript: fasleNum = " + falseNum);
            } else if (significanceMethod == TtestInitDialog.FALSE_PROP) {
                this.falseProp = algData.getParams().getFloat("falseProp");
                System.out.println("executeScript: falseProp = " + falseProp);
            }
                       
            int genes = experiment.getNumberOfGenes();
            
            //get global ttest design
            
            if (this.tTestDesign == TtestInitDialog.PAIRED) {
                FloatMatrix pairedAExptsMatrix = algData.getMatrix("pairedAExptsMatrix");
                FloatMatrix pairedBExptsMatrix = algData.getMatrix("pairedBExptsMatrix");
                pairedGroupAExpts = new Vector();
                pairedGroupBExpts = new Vector();
                
                for (int i = 0; i < pairedAExptsMatrix.A.length; i++) {
                    pairedGroupAExpts.add(new Integer((int)pairedAExptsMatrix.A[i][0]));
                    pairedGroupBExpts.add(new Integer((int)pairedBExptsMatrix.A[i][0]));
                }
            }
            
            this.progress = new Progress(framework.getFrame(), "Finding significant genes", listener);
            this.progress.show();
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(algData);
            long time = System.currentTimeMillis() - start;
            // getting the results
            Cluster result_cluster = result.getCluster("cluster");
            NodeList nodeList = result_cluster.getNodeList();
            AlgorithmParameters resultMap = result.getParams();
            int k = 2; //resultMap.getInt("number-of-clusters"); // NEED THIS TO GET THE VALUE OF NUMBER-OF-CLUSTERS
            
            this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            FloatMatrix sigPValuesMatrix = result.getMatrix("sigPValues");
            FloatMatrix sigTValuesMatrix = result.getMatrix("sigTValues");
            FloatMatrix nonSigPValuesMatrix = result.getMatrix("nonSigPValues");
            FloatMatrix nonSigTValuesMatrix = result.getMatrix("nonSigTValues");
            FloatMatrix rawPValuesMatrix = result.getMatrix("rawPValues");
            FloatMatrix adjPValuesMatrix = result.getMatrix("adjPValues");
            FloatMatrix tValuesMatrix = result.getMatrix("tValues");
            FloatMatrix dfMatrix = result.getMatrix("dfValues");
            FloatMatrix meansAMatrix = result.getMatrix("meansAMatrix");
            FloatMatrix meansBMatrix = result.getMatrix("meansBMatrix");
            FloatMatrix sdAMatrix = result.getMatrix("sdAMatrix");
            FloatMatrix sdBMatrix = result.getMatrix("sdBMatrix");
            FloatMatrix isSigMatrix = result.getMatrix("isSigMatrix");
            FloatMatrix oneClassMeansMatrix = result.getMatrix("oneClassMeansMatrix");
            FloatMatrix oneClassSDsMatrix = result.getMatrix("oneClassSDsMatrix");
            
            rawPValues = new Vector();
            adjPValues = new Vector();
            tValues = new Vector();
            dfValues = new Vector();
            meansA = new Vector();
            meansB = new Vector();
            sdA = new Vector();
            sdB = new Vector();
            oneClassMeans = new Vector();
            oneClassSDs = new Vector();
            
            for (int i = 0; i < rawPValuesMatrix.getRowDimension(); i++) {
                rawPValues.add(new Float(rawPValuesMatrix.A[i][0]));
                adjPValues.add(new Float(adjPValuesMatrix.A[i][0]));
            }
            
            for (int i = 0; i < tValuesMatrix.getRowDimension(); i++) {
                tValues.add(new Float(tValuesMatrix.A[i][0]));
            }
            
            for (int i = 0; i < dfMatrix.getRowDimension(); i++) {
                dfValues.add(new Float(dfMatrix.A[i][0]));
            }
            
            if (tTestDesign == TtestInitDialog.ONE_CLASS) {
                for (int i = 0; i < oneClassMeansMatrix.getRowDimension(); i++) {
                    oneClassMeans.add(new Float(oneClassMeansMatrix.A[i][0]));
                }
                
                for (int i = 0; i < oneClassSDsMatrix.getRowDimension(); i++) {
                    oneClassSDs.add(new Float(oneClassSDsMatrix.A[i][0]));
                }
            }
            
            if ((tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) ||(tTestDesign == TtestInitDialog.PAIRED)) {
                
                for (int i = 0; i < meansAMatrix.getRowDimension(); i++) {
                    meansA.add(new Float(meansAMatrix.A[i][0]));
                    meansB.add(new Float(meansBMatrix.A[i][0]));
                    sdA.add(new Float(sdAMatrix.A[i][0]));
                    sdB.add(new Float(sdBMatrix.A[i][0]));
                }
                
                isSig = new boolean[isSigMatrix.getRowDimension()];
                
                for (int i = 0; i < isSig.length; i++) {
                    if (isSigMatrix.A[i][0] == 1.0f) {
                        isSig[i] = true;
                    } else {
                        isSig[i] = false;
                    }
                }
                
                diffMeansBA = new double[isSigMatrix.getRowDimension()];
                for (int i = 0; i < diffMeansBA.length; i++) {
                    diffMeansBA[i] = (double)(meansBMatrix.A[i][0]) - (double)(meansAMatrix.A[i][0]);
                }
                
                negLog10PValues = new double[isSigMatrix.getRowDimension()];
                
                double log10BaseE = Math.log(10);
                
                for (int i = 0; i < negLog10PValues.length; i++) {
                    double currentP = (double)(adjPValuesMatrix.A[i][0]);
                    negLog10PValues[i] = (-1)*((Math.log(currentP))/log10BaseE);
                    //System.out.println("i = " + i + ", currentP = " + currentP + ", negLog10P = " + negLog10PValues[i]);
                }
            }
            
            if (tTestDesign == TtestInitDialog.ONE_CLASS) {
                
                isSig = new boolean[isSigMatrix.getRowDimension()];
                
                for (int i = 0; i < isSig.length; i++) {
                    if (isSigMatrix.A[i][0] == 1.0f) {
                        isSig[i] = true;
                    } else {
                        isSig[i] = false;
                    }
                }
                
                diffMeansBA = new double[isSigMatrix.getRowDimension()];
                for (int i = 0; i < diffMeansBA.length; i++) {
                    diffMeansBA[i] = (double)((oneClassMeansMatrix.A[i][0]) - oneClassMean);
                }
                
                negLog10PValues = new double[isSigMatrix.getRowDimension()];
                
                double log10BaseE = Math.log(10);
                
                for (int i = 0; i < negLog10PValues.length; i++) {
                    double currentP = (double)(adjPValuesMatrix.A[i][0]);
                    negLog10PValues[i] = (-1)*((Math.log(currentP))/log10BaseE);
                    //System.out.println("i = " + i + ", currentP = " + currentP + ", negLog10P = " + negLog10PValues[i]);
                }                
            }
            
            
            GeneralInfo info = new GeneralInfo();
            
            info.time = time;
            
            AlgorithmParameters params = algData.getParams();
            
            
            /*
             *        data.addParam("distance-function", String.valueOf(function));
        data.addParam("tTestDesign", String.valueOf(tTestDesign));
        data.addIntArray("group-assignments", groupAssignments);
        if (tTestDesign == TtestInitDialog.ONE_CLASS) {
            data.addParam("oneClassMean", String.valueOf(oneClassMean));
        }
        data.addParam("alpha", String.valueOf(alpha));
        data.addParam("significance-method", String.valueOf(significanceMethod));
        data.addParam("is-permut", String.valueOf(isPermut));
        data.addParam("num-combs", String.valueOf(numCombs));
        data.addParam("use-all-combs", String.valueOf(useAllCombs));
             
        // hcl parameters
        if (isHierarchicalTree) {
            data.addParam("hierarchical-tree", String.valueOf(true));
            data.addParam("method-linkage", String.valueOf(hcl_method));
            data.addParam("calculate-genes", String.valueOf(hcl_genes));
            data.addParam("calculate-experiments", String.valueOf(hcl_samples));
        }
             */
            
            
            //ADD MORE INFO PARAMETERS HERE
            info.alpha = params.getFloat("alpha");
            info.sigMethod = getSigMethod(params.getInt("significance-method"));
            boolean isPermut = params.getBoolean("is-permut");
            info.pValueBasedOn = getPValueBasedOn(isPermut);
            if (isPermut) {
                info.useAllCombs = params.getBoolean("use-all-combs");
                info.numCombs = params.getInt("num-combs");
            }
          
            info.function = framework.getDistanceMenu().getFunctionName(params.getInt("distance-function"));
            info.hcl = params.getBoolean("hierarchical-tree");
            if(info.hcl) {
                info.hcl_genes = params.getBoolean("calculate-genes");
                info.hcl_samples = params.getBoolean("calculate-experiments");
                info.hcl_method = params.getInt("method-linkage");
            }
            Vector titlesVector = new Vector();
            if ((tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) || (tTestDesign == TtestInitDialog.PAIRED)) {
                titlesVector.add("GroupA mean");
                titlesVector.add("GroupA std.dev.");
                titlesVector.add("GroupB mean");
                titlesVector.add("GroupB std.dev.");
                titlesVector.add("Absolute t value");
            } else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
                titlesVector.add("Gene mean");
                titlesVector.add("Gene std.dev.");
                titlesVector.add("t value");
            }
            titlesVector.add("Degrees of freedom");
            titlesVector.add("Raw p value");
            //int significanceMethod = params.getInt("significance-method");
            if ((significanceMethod == TtestInitDialog.FALSE_NUM)||(significanceMethod == TtestInitDialog.FALSE_PROP)) {
                if (calculateAdjFDRPVals)
                    titlesVector.add("Adj p value");
            } else {
                titlesVector.add("Adj p value");
            }
            
            auxTitles = new String[titlesVector.size()];
            for (int i = 0; i < auxTitles.length; i++) {
                auxTitles[i] = (String)(titlesVector.get(i));
            }
            
            auxData = new Object[experiment.getNumberOfGenes()][auxTitles.length];
            for (int i = 0; i < auxData.length; i++) {
                int counter = 0;
                if ((tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) || (tTestDesign == TtestInitDialog.PAIRED)) {
                    auxData[i][counter++] = meansA.get(i);
                    auxData[i][counter++] = sdA.get(i);
                    auxData[i][counter++] = meansB.get(i);
                    auxData[i][counter++] = sdB.get(i);
                } else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
                    auxData[i][counter++] = oneClassMeans.get(i);
                    auxData[i][counter++] = oneClassSDs.get(i);
                }
                auxData[i][counter++] = tValues.get(i);
                auxData[i][counter++] = dfValues.get(i);
                auxData[i][counter++] = rawPValues.get(i);
                if ((significanceMethod == TtestInitDialog.FALSE_NUM)||(significanceMethod == TtestInitDialog.FALSE_PROP)) {
                    if (calculateAdjFDRPVals)
                        auxData[i][counter++] = adjPValues.get(i);
                } else {
                    auxData[i][counter++] = adjPValues.get(i);
                }
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
    
    
    
    
    protected String getPValueBasedOn(boolean isPerm) {
        String str = "";
        if (isPerm) {
            str = "permutation";
        } else {
            str = "t-distribution";
        }
        
        return str;
    }
    
    protected String getSigMethod(int sigMethod) {
        String methodName = "";
        
        if (sigMethod == TtestInitDialog.JUST_ALPHA) {
            methodName = "Just alpha";
        } else if (sigMethod == TtestInitDialog.STD_BONFERRONI) {
            methodName = "Standard Bonferroni correction";
        } else if (sigMethod == TtestInitDialog.ADJ_BONFERRONI) {
            methodName = "Adjusted Bonferroni correction";
        } else if (sigMethod == TtestInitDialog.MIN_P) {
            methodName = "Step-down Westfall Young: Min P";
        } else if (sigMethod == TtestInitDialog.MAX_T) {
            methodName = "Step-down Westfall Young: Max T";
        } else if (sigMethod == TtestInitDialog.FALSE_NUM) {
            methodName = "False significant number: " + falseNum + " or less";
        } else if (sigMethod == TtestInitDialog.FALSE_PROP) {
            methodName = "False significant proportion: " + falseProp + " or less";
        }
        
        return methodName;
    }
    
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    protected DefaultMutableTreeNode createResultTree(Cluster result_cluster, GeneralInfo info) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("T Tests");
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
        //addTStatsViews(root);
        //if ((tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) || (tTestDesign == TtestInitDialog.PAIRED)) {
            addVolcanoPlot(root);
        //}
        addGeneralInfo(root, info);
    }
    
    protected void addTableViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table Views");
        IViewer tabViewer = new ClusterTableViewer(this.experiment, this.clusters, this.data, this.auxTitles, this.auxData);
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
        IViewer expViewer = new TtestExperimentViewer(this.experiment, this.clusters, tTestDesign, oneClassMeans, oneClassSDs, meansA, meansB, sdA, sdB, rawPValues, adjPValues, tValues, dfValues);
        for (int i=0; i<this.clusters.length; i++) {
            if (i < this.clusters.length - 1) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", expViewer, new Integer(i))));
            } else if (i == this.clusters.length - 1) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", expViewer, new Integer(i))));
                
            }
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
        node.add(new DefaultMutableTreeNode(new LeafInfo("Results (#,%)", new TtestInfoViewer(this.clusters, this.experiment.getNumberOfGenes()))));
        root.add(node);
    }
    
    /**
     * Adds nodes to display centroid charts.
     */
    protected void addCentroidViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        TtestCentroidViewer centroidViewer = new TtestCentroidViewer(this.experiment, clusters, tTestDesign, oneClassMeans, oneClassSDs, meansA, meansB, sdA, sdB, rawPValues, adjPValues, tValues, dfValues);
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
        
        TtestCentroidsViewer centroidsViewer = new TtestCentroidsViewer(this.experiment, clusters, tTestDesign, oneClassMeans, oneClassSDs, meansA, meansB, sdA, sdB, rawPValues, adjPValues, tValues, dfValues);
        centroidsViewer.setMeans(this.means.A);
        centroidsViewer.setVariances(this.variances.A);
        
        centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
        expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        
        root.add(centroidNode);
        root.add(expressionNode);
    }
    /*
    protected void addTStatsViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode tStatsNode = new DefaultMutableTreeNode("Gene Statistics");
        IViewer tSigViewer = new TStatsTableViewer(this.experiment, this.clusters, this.data, tTestDesign, oneClassMeans, oneClassSDs, meansA, meansB, sdA, sdB, pValues, tValues, dfValues, true);
        IViewer tNonSigViewer = new TStatsTableViewer(this.experiment, this.clusters, this.data, tTestDesign, oneClassMeans, oneClassSDs, meansA, meansB, sdA, sdB, pValues, tValues, dfValues, false);
        
        tStatsNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes", tSigViewer)));
        tStatsNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes", tNonSigViewer)));
        
        root.add(tStatsNode);
    }
     */
    
    protected void addVolcanoPlot(DefaultMutableTreeNode root) {
        //DefaultMutableTreeNode vNode = new DefaultMutableTreeNode("Volcano plot");
        IViewer volcanoPlotViewer = new TTestVolcanoPlotViewer(this.experiment, diffMeansBA, negLog10PValues, isSig,  tTestDesign, oneClassMean, oneClassMeans, oneClassSDs, meansA, meansB, sdA, sdB, rawPValues, adjPValues, tValues, dfValues);
        root.add(new DefaultMutableTreeNode(new LeafInfo("Volcano Plot", volcanoPlotViewer)));
    }
    
    /**
     * Adds node with general iformation.
     */
    protected void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        node.add(new DefaultMutableTreeNode("Test design: " + info.getTestDesign()));
        node.add(getGroupAssignmentInfo());
        if (tTestDesign == TtestInitDialog.ONE_CLASS) {
            node.add(new DefaultMutableTreeNode("Mean tested against: " + oneClassMean));
        }
        if (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) {
            if (useWelchDf)
                node.add(new DefaultMutableTreeNode("used Welch approximation"));
            else 
                node.add(new DefaultMutableTreeNode("assumed equal variances"));
        }
        if (info.sigMethod.startsWith("False")) {
            node.add(new DefaultMutableTreeNode("Confidence (1 - alpha) : "+(1d - info.alpha)*100 + " %"));
        } else {
            node.add(new DefaultMutableTreeNode("Alpha (overall threshold p-value): "+info.alpha));
        }
        node.add(new DefaultMutableTreeNode("P-values based on: "+info.pValueBasedOn));
        if (isPermutations) {
            node.add(new DefaultMutableTreeNode("All permutations used: " + info.useAllCombs));
            node.add(new DefaultMutableTreeNode("Number of permutations per gene: " + info.numCombs));
        }
        if (info.sigMethod.startsWith("False")) {
           node.add(new DefaultMutableTreeNode(info.sigMethod)); 
        } else {
            node.add(new DefaultMutableTreeNode("Significance determined by: "+info.sigMethod));
        }
        if (info.sigMethod.startsWith("False")) {
            if (doFastFDRApprox)
                node.add(new DefaultMutableTreeNode("False discovery calculation: fast approximation"));
            else
                node.add(new DefaultMutableTreeNode("False discovery calculation: exhaustive computation"));
        }
        node.add(new DefaultMutableTreeNode("HCL: "+info.getMethodName()));
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time)+" ms"));
        if (info.hcl) {
            node.add(new DefaultMutableTreeNode(info.function));
        }
        root.add(node);
    }
    
    
    protected DefaultMutableTreeNode getGroupAssignmentInfo() {
        DefaultMutableTreeNode groupAssignmentInfo = new DefaultMutableTreeNode();
        if (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) {
            groupAssignmentInfo = new DefaultMutableTreeNode("Group assigments ");
            DefaultMutableTreeNode groupA = new DefaultMutableTreeNode("Group A ");
            DefaultMutableTreeNode groupB = new DefaultMutableTreeNode("Group B ");
            DefaultMutableTreeNode neitherGroup = new DefaultMutableTreeNode("Neither group ");
            
            int neitherGroupCounter = 0;
            
            for (int i = 0; i < groupAssignments.length; i++) {
                if (groupAssignments[i] == TtestInitDialog.GROUP_A) {
                    groupA.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
                } else if (groupAssignments[i] == TtestInitDialog.GROUP_B) {
                    groupB.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
                } else {
                    neitherGroup.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
                    neitherGroupCounter++;
                }
            }
            
            groupAssignmentInfo.add(groupA);
            groupAssignmentInfo.add(groupB);
            if (neitherGroupCounter > 0) {
                groupAssignmentInfo.add(neitherGroup);
            }
        } else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
            groupAssignmentInfo = new DefaultMutableTreeNode("Sample details");
            DefaultMutableTreeNode in = new DefaultMutableTreeNode("In analysis ");
            DefaultMutableTreeNode out = new DefaultMutableTreeNode("Out of analysis ");
            int outCounter = 0;
            for (int i = 0; i < groupAssignments.length; i++) {
                if (groupAssignments[i] == 1) {
                    in.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
                } else {
                    out.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
                    outCounter++;
                }
            }
            
            if (outCounter == 0) {
                out.add(new DefaultMutableTreeNode("None"));
            }
            groupAssignmentInfo.add(in);
            groupAssignmentInfo.add(out);
        } else if (tTestDesign == TtestInitDialog.PAIRED) {
            groupAssignmentInfo = new DefaultMutableTreeNode("Pairings ");
            boolean paired[] = new boolean[exptNamesVector.size()];
            for (int i = 0; i < paired.length; i++) {
                paired[i] = false;
            }
            DefaultMutableTreeNode pairs = new DefaultMutableTreeNode("Sample Pairs");
            DefaultMutableTreeNode nonPairs = new DefaultMutableTreeNode("Unpaired Samples");
            for (int i = 0; i < pairedGroupAExpts.size(); i++) {
                int currentA = ((Integer)(pairedGroupAExpts.get(i))).intValue();
                int currentB = ((Integer)(pairedGroupBExpts.get(i))).intValue();
                pairs.add(new DefaultMutableTreeNode("A: " + (String)(exptNamesVector.get(currentA)) + " - B: " + (String)(exptNamesVector.get(currentB)) ));
                paired[currentA] = true;
                paired[currentB] = true;
            }
            for (int i = 0 ; i < paired.length; i++) {
                if (!paired[i]) {
                    nonPairs.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
                }
            }
            groupAssignmentInfo.add(pairs);
            if (!nonPairs.isLeaf()) {
                groupAssignmentInfo.add(nonPairs);
            }
            
        }
        
        return groupAssignmentInfo;
    }
    
    
    
    
    
    /**
     * The class to listen to progress, monitor and algorithms events.
     */
    protected class Listener extends DialogListener implements AlgorithmListener {
    	//EH added so AMP could subclass
    	public Listener(){
        	super();
        }
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
        public String design;
        public String sigMethod;
        public String pValueBasedOn;
        public double alpha;
        public int numCombs;
        public boolean useAllCombs;
        public long time;
        public String function, fdrFastOrSlow;
        
        protected boolean hcl;
        protected int hcl_method;
        protected boolean hcl_genes;
        protected boolean hcl_samples;
        
        public String getMethodName() {
            return hcl ? HCLGUI.GeneralInfo.getMethodName(hcl_method) : "no linkage";
        }
        
        public String getTestDesign() {
            String design = "";
            
            if (tTestDesign == TtestInitDialog.ONE_CLASS) {
                design = "One-class";
            } else if (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) {
                design = "Between-subjects";
            } else if (tTestDesign == TtestInitDialog.PAIRED) {
                design = "Paired";
            }
            
            return design;
        }
        
    }
}