/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SAMGUI.java,v $
 * $Revision: 1.8 $
 * $Date: 2005-03-10 20:21:59 $
 * $Author: braistedj $
 * $State: Exp $
 */

/*
 * SAMGUI.java
 *
 * Created on December 16, 2002, 11:13 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.sam;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Vector;

import javax.swing.JFileChooser;
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
public class SAMGUI implements IClusterGUI, IScriptGUI {
    
    private Algorithm algorithm;
    private Progress progress;
    //private Monitor monitor;
    private IData data;
    
    private Experiment experiment;
    private int[][] clusters;
    private FloatMatrix means;
    private FloatMatrix variances;
    private float[] dValues, rValues, qLowestFDR, foldChangeArray;
    private double[] xArray, yArray;
    private float delta, oneClassMean;
    private double[] deltaGrid, medNumFalse, false90th, FDRMedian, FDR90th;
    private int[] numSig;
    private String[] auxTitles;
    private float[][] auxData;
    //private String[] allTitles;
    
    int[] groupAssignments;
    int numMultiClassGroups;
    int studyDesign;
    boolean[] inSurvivalAnalysis, censored;
    private boolean drawSigTreesOnly;    
    double[] survivalTimes;
    public static JFrame SAMFrame;
    boolean calculateQLowestFDR;
    Vector exptNamesVector;
    Vector geneNamesVector;
    Vector pairedGroupAExpts, pairedGroupBExpts;
    /** Creates new SAMGUI */
    public SAMGUI() {
    }
    
    
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        
        SAMGUI.SAMFrame = (JFrame) framework.getFrame();
        this.experiment = framework.getData().getExperiment();
        this.data = framework.getData();
        exptNamesVector = new Vector();
        geneNamesVector = new Vector();
        int number_of_samples = experiment.getNumberOfSamples();
        int number_of_genes = experiment.getNumberOfGenes();
        int [] columnIndices = experiment.getColumnIndicesCopy();
        
        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(framework.getData().getFullSampleName(columnIndices[i]));
        }
        for (int i = 0; i < number_of_genes; i++) {
            geneNamesVector.add(framework.getData().getGeneName(i));
        }
        
        SAMInitDialog sDialog;
        studyDesign = 0;
        int numCombs = 0;
        int numUniquePerms = 0;
        int numNeighbors = 0;
        numMultiClassGroups = 0;
        
        boolean useKNearest = true;
        boolean isHierarchicalTree = false;
        drawSigTreesOnly = true;
        boolean usePreviousGraph = false;
        boolean saveImputedMatrix = false;
        boolean useTusherEtAlS0 = false;
        boolean useAllUniquePerms = false;
        
        double userPercentile = 0;
        
        //SAMState.fieldNames = framework.getData().getFieldNames();
        
        if (!SAMState.firstRun) {
            SAMPreDialog spDialog = new SAMPreDialog((JFrame)framework.getFrame(), true);
            spDialog.setVisible(true);
            if (!spDialog.isOkPressed()) return null;
            
            if (spDialog.usePrevious()) {
                usePreviousGraph = true;
                groupAssignments = SAMState.groupAssignments;
                studyDesign = SAMState.studyDesign;
                if (studyDesign == SAMInitDialog.MULTI_CLASS) {
                    numMultiClassGroups = SAMState.numMultiClassGroups;
                }
                if (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) {
                    pairedGroupAExpts = SAMState.pairedGroupAExpts;
                    pairedGroupBExpts = SAMState.pairedGroupBExpts;
                }
                if (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) {
                    inSurvivalAnalysis = SAMState.inSurvivalAnalysis;
                    survivalTimes = SAMState.survivalTimes;
                    censored = SAMState.censored;
                } else if (studyDesign == SAMInitDialog.ONE_CLASS) {
                    oneClassMean = (float)(SAMState.oneClassMean);
                }
                useAllUniquePerms = SAMState.useAllUniquePerms;
                if (useAllUniquePerms) {
                    numUniquePerms = SAMState.numUniquePerms;
                }
                numCombs = SAMState.numCombs;
                numNeighbors = SAMState.numNeighbors;
                useKNearest = SAMState.useKNearest;
                isHierarchicalTree = spDialog.drawTrees();
                if (isHierarchicalTree) {
                    drawSigTreesOnly = spDialog.drawSigTreesOnly();
                }           
                useTusherEtAlS0 = SAMState.useTusherEtAlS0;
                calculateQLowestFDR = SAMState.calculateQLowestFDR;
                
            } else {
                usePreviousGraph = false;
                sDialog = new SAMInitDialog((JFrame) framework.getFrame(), true, exptNamesVector, number_of_genes, framework.getClusterRepository(1));
                sDialog.setVisible(true);
                
                if (!sDialog.isOkPressed()) {
                    return null;
                } else {
                    SAMState.firstRun = false;
                }
                
                //SAMState.firstRun = false;
                studyDesign = sDialog.getStudyDesign();
                SAMState.studyDesign = studyDesign;
                if (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) {
                    pairedGroupAExpts = sDialog.getPairedAExpts();
                    pairedGroupBExpts = sDialog.getPairedBExpts();
                    SAMState.pairedGroupAExpts = pairedGroupAExpts;
                    SAMState.pairedGroupBExpts = pairedGroupBExpts;
                }
                if (studyDesign == SAMInitDialog.MULTI_CLASS) {
                    numMultiClassGroups = sDialog.getMultiClassNumGroups();
                    SAMState.numMultiClassGroups = numMultiClassGroups;
                }
                if (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) {
                    inSurvivalAnalysis = sDialog.isInSurvivalAnalysis();
                    SAMState.inSurvivalAnalysis = inSurvivalAnalysis;
                    censored = sDialog.isCensored();
                    SAMState.censored = censored;
                    survivalTimes =sDialog.getSurvivalTimes();
                    SAMState.survivalTimes = survivalTimes;
                }
                
                if (studyDesign == SAMInitDialog.ONE_CLASS) {
                    oneClassMean = (float)(sDialog.getOneClassMean());
                    SAMState.oneClassMean = (double)oneClassMean;
                }
                
                groupAssignments = sDialog.getGroupAssignments();
                
                SAMState.groupAssignments = groupAssignments;
                //boolean useAllCombs = sDialog.useAllCombs();
                //if (!useAllCombs) {
                numCombs = sDialog.getUserNumCombs();
                SAMState.numCombs = numCombs;
                useAllUniquePerms = sDialog.useAllUniquePerms();
                SAMState.useAllUniquePerms = useAllUniquePerms;
                if (useAllUniquePerms) {
                    numUniquePerms = sDialog.getNumUniquePerms();
                    SAMState.numUniquePerms = numUniquePerms;
                }
                //}
                useKNearest = sDialog.useKNearest();
                SAMState.useKNearest = useKNearest;
                //numNeighbors = 10;
                if (useKNearest) {
                    numNeighbors = sDialog.getNumNeighbors();
                    SAMState.numNeighbors = numNeighbors;
                }
                saveImputedMatrix = sDialog.isSaveMatrix();
                
                userPercentile = sDialog.getPercentile();
                useTusherEtAlS0 = sDialog.useTusherEtAlS0();
                SAMState.useTusherEtAlS0 = useTusherEtAlS0;
                
                calculateQLowestFDR = sDialog.calculateQLowestFDR();
                SAMState.calculateQLowestFDR = calculateQLowestFDR;
                
                isHierarchicalTree = sDialog.drawTrees();
                if (isHierarchicalTree) {
                    drawSigTreesOnly = sDialog.drawSigTreesOnly();
                }             
                //SAMState.isHierarchicalTree = isHierarchicalTree;
            }
            
        } else { //if (SAMState.firstRun)
            usePreviousGraph = false;
            sDialog = new SAMInitDialog((JFrame) framework.getFrame(), true, exptNamesVector, number_of_genes, framework.getClusterRepository(1));
            sDialog.setVisible(true);
            
            if (!sDialog.isOkPressed()) {
                return null;
            } else {
                SAMState.firstRun = false;
            }
            
            //SAMState.firstRun = false;
            studyDesign = sDialog.getStudyDesign();
            SAMState.studyDesign = studyDesign;
            if (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) {
                pairedGroupAExpts = sDialog.getPairedAExpts();
                pairedGroupBExpts = sDialog.getPairedBExpts();
                SAMState.pairedGroupAExpts = pairedGroupAExpts;
                SAMState.pairedGroupBExpts = pairedGroupBExpts;
            }
            if (studyDesign == SAMInitDialog.MULTI_CLASS) {
                numMultiClassGroups = sDialog.getMultiClassNumGroups();
                SAMState.numMultiClassGroups = numMultiClassGroups;
            }
            if (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) {
                inSurvivalAnalysis = sDialog.isInSurvivalAnalysis();
                SAMState.inSurvivalAnalysis = inSurvivalAnalysis;
                censored = sDialog.isCensored();
                SAMState.censored = censored;
                survivalTimes =sDialog.getSurvivalTimes();
                SAMState.survivalTimes = survivalTimes;
            }
            
            if (studyDesign == SAMInitDialog.ONE_CLASS) {
                oneClassMean = (float)(sDialog.getOneClassMean());
                SAMState.oneClassMean = (double)oneClassMean;
            }
            groupAssignments = sDialog.getGroupAssignments();
            
            SAMState.groupAssignments = groupAssignments;
            //boolean useAllCombs = sDialog.useAllCombs();
            //if (!useAllCombs) {
            numCombs = sDialog.getUserNumCombs();
            SAMState.numCombs = numCombs;
            
            useAllUniquePerms = sDialog.useAllUniquePerms();
            SAMState.useAllUniquePerms = useAllUniquePerms;
            if (useAllUniquePerms) {
                numUniquePerms = sDialog.getNumUniquePerms();
                SAMState.numUniquePerms = numUniquePerms;
            }
            //}
            useKNearest = sDialog.useKNearest();
            SAMState.useKNearest = useKNearest;
            //numNeighbors = 10;
            if (useKNearest) {
                numNeighbors = sDialog.getNumNeighbors();
                SAMState.numNeighbors = numNeighbors;
            }
            isHierarchicalTree = sDialog.drawTrees();
            if (isHierarchicalTree) {
                drawSigTreesOnly = sDialog.drawSigTreesOnly();
            }            
            //SAMState.isHierarchicalTree = isHierarchicalTree;
            saveImputedMatrix = sDialog.isSaveMatrix();
            
            userPercentile = sDialog.getPercentile();
            useTusherEtAlS0 = sDialog.useTusherEtAlS0();
            SAMState.useTusherEtAlS0 = useTusherEtAlS0;
            
            calculateQLowestFDR = sDialog.calculateQLowestFDR();
            SAMState.calculateQLowestFDR = calculateQLowestFDR;
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
        
        Listener listener = new Listener();
        
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("SAM");
            //System.out.println("SAMGUI: getting algorithm");
            algorithm.addAlgorithmListener(listener);
            
            //this.progress = new Progress(framework.getFrame(), "Finding significant genes", listener); // **** MAKE PROGRESS BARS LATER
            //this.progress.show();
            
            this.progress = new Progress(framework.getFrame(), "SAM Execution", listener);
            this.progress.show();
            
            AlgorithmData data = new AlgorithmData();
            
            data.addMatrix("experiment", experiment.getMatrix());
            data.addParam("distance-factor", String.valueOf(1.0f));
            data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
            
            data.addParam("distance-function", String.valueOf(function));
            data.addIntArray("group-assignments", groupAssignments);
            data.addParam("study-design", String.valueOf(studyDesign));
            if (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) {
                FloatMatrix pairedAExptsMatrix = new FloatMatrix(pairedGroupAExpts.size(), 1);
                FloatMatrix pairedBExptsMatrix = new FloatMatrix(pairedGroupBExpts.size(), 1);
                
                for (int i = 0; i < pairedGroupAExpts.size(); i++) {
                    pairedAExptsMatrix.A[i][0] = ((Integer)(pairedGroupAExpts.get(i))).floatValue();
                    pairedBExptsMatrix.A[i][0] = ((Integer)(pairedGroupBExpts.get(i))).floatValue();
                }
                data.addMatrix("pairedAExptsMatrix", pairedAExptsMatrix);
                data.addMatrix("pairedBExptsMatrix", pairedBExptsMatrix);
            }
            
            if (studyDesign == SAMInitDialog.MULTI_CLASS) {
                data.addParam("numMultiClassGroups", String.valueOf(numMultiClassGroups));
            }
            
            if (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) {
                FloatMatrix inAnalysisMatrix, isCensoredMatrix, survivalTimesMatrix;
                inAnalysisMatrix = new FloatMatrix(inSurvivalAnalysis.length, 1);
                isCensoredMatrix = new FloatMatrix(inSurvivalAnalysis.length, 1);
                survivalTimesMatrix = new FloatMatrix(inSurvivalAnalysis.length, 1);
                for (int i = 0; i < inSurvivalAnalysis.length; i++) {
                    if (!inSurvivalAnalysis[i]) {
                        inAnalysisMatrix.A[i][0] = 0.0f;
                    } else {
                        inAnalysisMatrix.A[i][0] = 1.0f;
                    }
                    if (!censored[i]) {
                        isCensoredMatrix.A[i][0] = 0.0f;
                    } else {
                        isCensoredMatrix.A[i][0] = 1.0f;
                    }
                    survivalTimesMatrix.A[i][0] = (float)survivalTimes[i];
                }
                data.addMatrix("inAnalysisMatrix", inAnalysisMatrix);
                data.addMatrix("isCensoredMatrix", isCensoredMatrix);
                data.addMatrix("survivalTimesMatrix", survivalTimesMatrix);
            }
            
            if (studyDesign == SAMInitDialog.ONE_CLASS) {
                data.addParam("oneClassMean", String.valueOf(oneClassMean));
            }
            data.addParam("useAllUniquePerms", String.valueOf(useAllUniquePerms));
            
            if (useAllUniquePerms) {
                data.addParam("numUniquePerms", String.valueOf(numUniquePerms));
            }
            
            data.addParam("num-combs", String.valueOf(numCombs));
            //data.addParam("use-all-combs", String.valueOf(useAllCombs));
            data.addParam("use-k-nearest", String.valueOf(useKNearest));
            data.addParam("num-neighbors", String.valueOf(numNeighbors));
            data.addParam("saveImputedMatrix", String.valueOf(saveImputedMatrix));
            data.addParam("use-previous-graph", String.valueOf(usePreviousGraph));
            data.addParam("userPercentile", String.valueOf(userPercentile));
            data.addParam("useTusherEtAlS0", String.valueOf(useTusherEtAlS0));
            data.addParam("calculateQLowestFDR", String.valueOf(calculateQLowestFDR));
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
            //System.out.println("After algorithm.execute()");
            long time = System.currentTimeMillis() - start;
            // getting the results
            Cluster result_cluster = result.getCluster("cluster");
            NodeList nodeList = result_cluster.getNodeList();
            AlgorithmParameters resultMap = result.getParams();
            int k = 0;
            if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) || (studyDesign == SAMInitDialog.ONE_CLASS)) {
                k = 4; //resultMap.getInt("number-of-clusters"); // NEED THIS TO GET THE VALUE OF NUMBER-OF-CLUSTERS
            } else {
                k = 2;
            }
            
            this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            
            delta = resultMap.getFloat("delta");
            String numSigGenes = resultMap.getString("numSigGenes");
            String numFalseSigMed = resultMap.getString("numFalseSigMed");
            String numFalseSig90th = resultMap.getString("numFalseSig90th");
            String FDRMedianString = resultMap.getString("FDRMedian");
            String FDR90thString = resultMap.getString("FDR90th");
            float sNought = resultMap.getFloat("sNought");
            float s0Percentile = resultMap.getFloat("s0Percentile");
            float pi0Hat = resultMap.getFloat("pi0Hat");
            float upperCutoff;
            try {
                upperCutoff = resultMap.getFloat("upperCutoff");
            } catch (NumberFormatException nfe) {
                upperCutoff = Float.POSITIVE_INFINITY;
            }
            float lowerCutoff;
            try {
                lowerCutoff = resultMap.getFloat("lowerCutoff");
            } catch (NumberFormatException nfe) {
                lowerCutoff = Float.NEGATIVE_INFINITY;
            }
            boolean useFoldChange = resultMap.getBoolean("useFoldChange");
            float foldChangeValue = resultMap.getFloat("foldChangeValue");
            FloatMatrix dValuesMatrix = result.getMatrix("dValuesMatrix");
            FloatMatrix rValuesMatrix = result.getMatrix("rValuesMatrix");
            FloatMatrix qLowestFDRMatrix = result.getMatrix("qLowestFDRMatrix");
            FloatMatrix foldChangeMatrix = result.getMatrix("foldChangeMatrix");
            dValues = new float[dValuesMatrix.getRowDimension()];
            rValues = new float[rValuesMatrix.getRowDimension()];
            foldChangeArray =new float[foldChangeMatrix.getRowDimension()];
            qLowestFDR = new float[qLowestFDRMatrix.getRowDimension()];
            for (int i = 0; i < dValues.length; i++) {
                dValues[i] = dValuesMatrix.A[i][0];
                rValues[i] = rValuesMatrix.A[i][0];
                qLowestFDR[i] = qLowestFDRMatrix.A[i][0];
                foldChangeArray[i] = foldChangeMatrix.A[i][0];
            }
            FloatMatrix dBarMatrixX = result.getMatrix("dBarMatrixX");
            FloatMatrix sortedDMatrixY = result.getMatrix("sortedDMatrixY");
            xArray = new double[dBarMatrixX.getRowDimension()];
            yArray = new double[sortedDMatrixY.getRowDimension()];
            for (int i = 0; i < xArray.length; i++) {
                xArray[i] = (double)(dBarMatrixX.A[i][0]);
                yArray[i] = (double)(sortedDMatrixY.A[i][0]);
            }
            
            FloatMatrix deltaGridMatrix = result.getMatrix("deltaGridMatrix");
            FloatMatrix medNumFalseMatrix = result.getMatrix("medNumFalseMatrix");
            FloatMatrix false90thMatrix = result.getMatrix("false90thMatrix");
            FloatMatrix numSigMatrix = result.getMatrix("numSigMatrix");
            FloatMatrix FDRMedianMatrix = result.getMatrix("FDRMedianMatrix");
            FloatMatrix FDR90thMatrix = result.getMatrix("FDR90thMatrix");
            
            deltaGrid = new double[deltaGridMatrix.getRowDimension()];
            medNumFalse = new double[medNumFalseMatrix.getRowDimension()];
            false90th = new double[false90thMatrix.getRowDimension()];
            numSig = new int[numSigMatrix.getRowDimension()];
            FDRMedian = new double[FDRMedianMatrix.getRowDimension()];
            FDR90th = new double[FDR90thMatrix.getRowDimension()];
            
            
            for (int i= 0; i < deltaGrid.length; i++) {
                deltaGrid[i] = (double)(deltaGridMatrix.A[i][0]);
                medNumFalse[i] = (double)(medNumFalseMatrix.A[i][0]);
                false90th[i] = (double)(false90thMatrix.A[i][0]);
                numSig[i] = (int)(numSigMatrix.A[i][0]);
                FDRMedian[i] = (double)(FDRMedianMatrix.A[i][0]);
                FDR90th[i] = (double)(FDR90thMatrix.A[i][0]);
            }
            //int studyDesign = resultMap.getInt("studyDesign");
            
            if ((!usePreviousGraph) && (saveImputedMatrix)) {
                FloatMatrix imputedMatrix = result.getMatrix("imputedMatrix");
                final JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Save imputed matrix");
                fc.setCurrentDirectory(new File("Data"));
                int returnVal = fc.showSaveDialog((JFrame) framework.getFrame());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    try {
                        PrintWriter out = new PrintWriter(new FileOutputStream(file));
                        String[] fieldNames = framework.getData().getFieldNames();
                        //out.print("Original row");
                        //out.print("\t");
                        for (int i = 0; i < fieldNames.length; i++) {
                            out.print(fieldNames[i]);
                            if (i < fieldNames.length - 1) {
                                out.print("\t");
                            }
                        }
                        for (int i=0; i<experiment.getNumberOfSamples(); i++) {
                            out.print("\t");
                            out.print(framework.getData().getFullSampleName(experiment.getSampleIndex(i)));
                        }
                        out.print("\n");
                        for (int i=0; i<imputedMatrix.getRowDimension(); i++) {
                            //out.print(Integer.toString(experiment.getGeneIndexMappedToData(rows[i]) + 1));  //handles cutoffs
                            //out.print(data.getUniqueId(rows[i]));
                            //out.print("\t");
                            //out.print(data.getGeneName(rows[i]));
                            for (int f = 0; f < fieldNames.length; f++) {
                                out.print(framework.getData().getElementAttribute(experiment.getGeneIndexMappedToData(i), f));
                                if (f < fieldNames.length - 1) {
                                    out.print("\t");
                                }
                            }
                            for (int j=0; j<imputedMatrix.getColumnDimension(); j++) {
                                out.print("\t");
                                out.print(Float.toString(imputedMatrix.A[i][j]));
                            }
                            out.print("\n");
                        }
                        //int[] groupAssgn = getGroupAssignments();
                        /*
                        for (int i = 0; i < groupAssgn.length; i++) {
                            out.print(groupAssgn[i]);
                            if (i < groupAssgn.length - 1) {
                                out.print("\t");
                            }
                        }
                        out.println();
                         */
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                    //this is where a real application would save the file.
                    //log.append("Saving: " + file.getName() + "." + newline);
                } else {
                    //log.append("Save command cancelled by user." + newline);
                }
            }
            
            GeneralInfo info = new GeneralInfo();
            
            info.time = time;
            //ADD MORE INFO PARAMETERS HERE
            info.delta = delta;
            info.upperCutoff = upperCutoff;
            info.lowerCutoff = lowerCutoff;
            info.useAllUniquePerms = useAllUniquePerms;
            info.numUniquePerms = numUniquePerms;
            if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED)) {
                if (useFoldChange) {
                    info.useFoldChange = "Yes";
                    info.foldChangeValue = foldChangeValue;
                } else {
                    info.useFoldChange = "No";
                }
            } else {
                info.useFoldChange = "N/A";
            }
            if (studyDesign == SAMInitDialog.MULTI_CLASS) {
                info.numMultiClassGroups = numMultiClassGroups;
            }
            
            if (studyDesign == SAMInitDialog.ONE_CLASS) {
                info.oneClassMean = oneClassMean;
            }
            info.numSigGenes = numSigGenes;
            info.numFalseSigMed = numFalseSigMed;
            info.numFalseSig90th = numFalseSig90th;
            info.FDRMedian = FDRMedianString;
            info.FDR90th = FDR90thString;
            info.studyDesign = studyDesign;
            if (useKNearest) {
                info.imputationEngine = "K-Nearest Neighbors";
                info.numNeighbors = numNeighbors;
            } else {
                info.imputationEngine = "Row Average";
            }
            info.numCombs = numCombs;
            info.sNought = sNought;
            info.s0Percentile = s0Percentile;
            info.pi0Hat = pi0Hat;
            //info.function = menu.getFunctionName(function);
            info.hcl = isHierarchicalTree;
            info.hcl_genes = hcl_genes;
            info.hcl_samples = hcl_samples;
            info.hcl_method = hcl_method;
            
            Vector allFields = new Vector();
            
            allFields.add("Expected score (dExp)");
            allFields.add(" Observed score(d)");
            allFields.add("Numerator(r)");
            allFields.add("Denominator (s+s0)");
            if ((studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED)) {
                allFields.add("Fold change(Unlogged)");
            }
            if (calculateQLowestFDR) {
                allFields.add("q-value (%)");
            }
            
            auxTitles = new String[allFields.size()];
            for (int i = 0; i < auxTitles.length; i++) {
                auxTitles[i] = (String)(allFields.get(i));
            }
            
            auxData = new float[experiment.getNumberOfGenes()][auxTitles.length];
            for (int i = 0; i < auxData.length; i++) {
                int counter = 0;
                auxData[i][counter++] = new Float(dBarMatrixX.A[i][0]);
                auxData[i][counter++] = new Float(dValues[i]);
                auxData[i][counter++] = new Float(rValues[i]);
                auxData[i][counter++] = new Float((float)(rValues[i]/dValues[i]));
                if ((studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED)) {
                    auxData[i][counter++] = new Float(foldChangeArray[i]);
                }
                if (calculateQLowestFDR) {
                    auxData[i][counter++] = new Float(qLowestFDR[i]);
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
        
        //return null; //for now
        
    }
    
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    private DefaultMutableTreeNode createResultTree(Cluster result_cluster, GeneralInfo info) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("SAM");
        addResultNodes(root, result_cluster, info);
        return root;
    }
    
    /**
     * Adds result nodes into the tree root.
     */
    private void addResultNodes(DefaultMutableTreeNode root, Cluster result_cluster, GeneralInfo info) {
        addSAMGraph(root);
        addSAMDeltaInfo(root);
        addExpressionImages(root);
        addHierarchicalTrees(root, result_cluster, info);
        addCentroidViews(root);
        addTableViews(root);
        addClusterInfo(root);
        addGeneralInfo(root, info);
    }
    
    private void addSAMGraph(DefaultMutableTreeNode root) {
        //DefaultMutableTreeNode node = new DefaultMutableTreeNode("SAM Graph");
        IViewer sgViewer = new SAMGraphViewer(xArray, yArray, studyDesign, (double)delta);
        root.add(new DefaultMutableTreeNode(new LeafInfo("SAM Graph", sgViewer)));
    }
    
    private void addSAMDeltaInfo(DefaultMutableTreeNode root) {
        IViewer sdInfoViewer = new SAMDeltaInfoViewer(deltaGrid, medNumFalse, false90th, numSig, FDRMedian, FDR90th);
        root.add(new DefaultMutableTreeNode(new LeafInfo("Delta table", sdInfoViewer)));
    }
    
    /**
     * Adds nodes to display clusters data.
     */
    private void addExpressionImages(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer = new SAMExperimentViewer(this.experiment, this.clusters, studyDesign, /*geneNamesVector,*/ dValues, rValues, foldChangeArray, qLowestFDR, calculateQLowestFDR);
        
        if ((studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) || (studyDesign == SAMInitDialog.ONE_CLASS)) {
            for (int i=0; i<this.clusters.length; i++) {
                if (i == 0) {
                    node.add(new DefaultMutableTreeNode(new LeafInfo("Positive Significant Genes ", expViewer, new Integer(i))));
                } else if (i == 1) {
                    node.add(new DefaultMutableTreeNode(new LeafInfo("Negative Significant Genes ", expViewer, new Integer(i))));
                    
                } else if (i == 2) {
                    node.add(new DefaultMutableTreeNode(new LeafInfo("All Significant Genes ", expViewer, new Integer(i))));
                    
                }  else if (i == 3) {
                    node.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", expViewer, new Integer(i))));
                    
                }
            }
        } else {
            for (int i=0; i<this.clusters.length; i++) {
                if (i == 0) {
                    node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", expViewer, new Integer(i))));
                } else if (i == 1) {
                    node.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", expViewer, new Integer(i))));
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
        if (!drawSigTreesOnly) {
            if ((studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) || (studyDesign == SAMInitDialog.ONE_CLASS)) {
                for (int i=0; i<nodeList.getSize(); i++) {
                    if (i == 0) {
                        node.add(new DefaultMutableTreeNode(new LeafInfo("Positive Significant Genes ", createHCLViewer(nodeList.getNode(i), info))));
                    } else if (i == 1) {
                        node.add(new DefaultMutableTreeNode(new LeafInfo("Negative Significant Genes ", createHCLViewer(nodeList.getNode(i), info))));
                    } else if (i == 2) {
                        node.add(new DefaultMutableTreeNode(new LeafInfo("All Significant Genes ", createHCLViewer(nodeList.getNode(i), info))));
                    } else if (i == 3) {
                        node.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", createHCLViewer(nodeList.getNode(i), info))));
                    }
                }
            } else {
                for (int i=0; i<nodeList.getSize(); i++) {
                    if (i == 0) {
                        node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", createHCLViewer(nodeList.getNode(i), info))));
                    } else if (i == 1) {
                        node.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", createHCLViewer(nodeList.getNode(i), info))));
                    }
                }
            }
            
        } else {//if (drawSigTreesOnly)
            if ((studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) || (studyDesign == SAMInitDialog.ONE_CLASS)) {
                for (int i=0; i<nodeList.getSize(); i++) {
                    if (i == 0) {
                        node.add(new DefaultMutableTreeNode(new LeafInfo("Positive Significant Genes ", createHCLViewer(nodeList.getNode(i), info))));
                    } else if (i == 1) {
                        node.add(new DefaultMutableTreeNode(new LeafInfo("Negative Significant Genes ", createHCLViewer(nodeList.getNode(i), info))));
                    } else if (i == 2) {
                        node.add(new DefaultMutableTreeNode(new LeafInfo("All Significant Genes ", createHCLViewer(nodeList.getNode(i), info))));
                    } 
                }
                
            } else {
                for (int i=0; i<nodeList.getSize(); i++) {
                    if (i == 0) {
                        node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", createHCLViewer(nodeList.getNode(i), info))));
                    } 
                }
            }            
        }
        root.add(node);
    }
    
    private IViewer createHCLViewer(Node clusterNode, GeneralInfo info) {
        HCLTreeData genes_result = info.hcl_genes ? getResult(clusterNode, 0) : null;
        HCLTreeData samples_result = info.hcl_samples ? getResult(clusterNode, info.hcl_genes ? 4 : 0) : null;
        return new HCLViewer(this.experiment, clusterNode.getFeaturesIndexes(), genes_result, samples_result);
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
        //System.out.println("SAMGUI.addClusterInfo(): studyDesign = " + studyDesign);
        node.add(new DefaultMutableTreeNode(new LeafInfo("Results (#,%)", new SAMInfoViewer(this.clusters, this.experiment.getNumberOfGenes(), studyDesign))));
        root.add(node);
    }
    
    private void addTableViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode tabNode = new DefaultMutableTreeNode("Table Views");
        IViewer tabViewer = new SAMClusterTableViewer(this.experiment, this.clusters, this.data, this.auxTitles, this.auxData);
        if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) || (studyDesign == SAMInitDialog.ONE_CLASS)) {
            for (int i=0; i<this.clusters.length; i++) {
                if (i == 0) {
                    tabNode.add(new DefaultMutableTreeNode(new LeafInfo("Positive Significant Genes ", tabViewer, new Integer(i))));
                } else if (i == 1) {
                    tabNode.add(new DefaultMutableTreeNode(new LeafInfo("Negative Significant Genes ", tabViewer, new Integer(i))));
                } else if (i == 2) {
                    tabNode.add(new DefaultMutableTreeNode(new LeafInfo("All Significant Genes ", tabViewer, new Integer(i))));
                } else if (i == 3) {
                    tabNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", tabViewer, new Integer(i))));
                }
            }
        } else {
            for (int i=0; i<this.clusters.length; i++) {
                if (i == 0) {
                    tabNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", tabViewer, new Integer(i))));
                } else if (i == 1) {
                    tabNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", tabViewer, new Integer(i))));
                }
            }
        }
        root.add(tabNode);
    }
    
    /**
     * Adds nodes to display centroid charts.
     */
    private void addCentroidViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        SAMCentroidViewer centroidViewer = new SAMCentroidViewer(this.experiment, clusters, studyDesign, /*geneNamesVector,*/ dValues, rValues, foldChangeArray, qLowestFDR, calculateQLowestFDR);
        centroidViewer.setMeans(this.means.A);
        centroidViewer.setVariances(this.variances.A);
        if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) || (studyDesign == SAMInitDialog.ONE_CLASS)) {
            for (int i=0; i<this.clusters.length; i++) {
                
                if (i == 0) {
                    centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Positive Significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                    expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Positive Significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
                } else if (i == 1) {
                    centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Negative Significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                    expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Negative Significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
                    
                } else if (i == 2) {
                    centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                    expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
                    
                } else if (i == 3) {
                    centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                    expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
                    
                }
            }
        } else {
            for (int i=0; i<this.clusters.length; i++) {
                
                if (i == 0) {
                    centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                    expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
                } else if (i == 1) {
                    centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                    expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
                    
                }
            }
        }
        
        SAMCentroidsViewer centroidsViewer = new SAMCentroidsViewer(this.experiment, clusters, studyDesign, /*geneNamesVector,*/ dValues, rValues, foldChangeArray, qLowestFDR, calculateQLowestFDR);
        centroidsViewer.setMeans(this.means.A);
        centroidsViewer.setVariances(this.variances.A);
        
        centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
        expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        
        root.add(centroidNode);
        root.add(expressionNode);
    }
    
    /**
     * Adds node with general iformation.
     */
    private void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        DefaultMutableTreeNode inputSubNode = new DefaultMutableTreeNode("Input Parameters");
        DefaultMutableTreeNode computedSubNode = new DefaultMutableTreeNode("Computed Quantities");
        inputSubNode.add(new DefaultMutableTreeNode("Study Design: " + info.getStudyDesign()));
        if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.MULTI_CLASS) || (studyDesign == SAMInitDialog.ONE_CLASS)) {
            inputSubNode.add(getGroupAssignmentInfo(info.studyDesign));
        }
        if (info.studyDesign == SAMInitDialog.MULTI_CLASS) {
            inputSubNode.add(new DefaultMutableTreeNode("Number of classes: " + info.numMultiClassGroups));
        }
        if (info.studyDesign == SAMInitDialog.ONE_CLASS) {
            inputSubNode.add(new DefaultMutableTreeNode("Hypothesized one-class mean: " + info.oneClassMean));
        }
        if (info.studyDesign == SAMInitDialog.CENSORED_SURVIVAL) {
            inputSubNode.add(getSampleSurvivalInfo());
        }
        inputSubNode.add(new DefaultMutableTreeNode("Imputation Engine: " + info.imputationEngine));
        if (info.imputationEngine == "K-Nearest Neighbors") {
            inputSubNode.add(new DefaultMutableTreeNode("Number of K-Nearest Neighbors: " + info.numNeighbors));
        }
        inputSubNode.add(new DefaultMutableTreeNode("Delta: " + info.delta));
        inputSubNode.add(new DefaultMutableTreeNode("Upper Cutoff: " + info.upperCutoff));
        inputSubNode.add(new DefaultMutableTreeNode("Lower Cutoff: " + info.lowerCutoff));
        inputSubNode.add(new DefaultMutableTreeNode("All permutations unique? " + info.useAllUniquePerms));
        if (info.useAllUniquePerms) {
            inputSubNode.add(new DefaultMutableTreeNode("Number of unique permutations " + info.numUniquePerms));
        } else {
            inputSubNode.add(new DefaultMutableTreeNode("Number of Permutations: " + info.numCombs));
        }
        inputSubNode.add(new DefaultMutableTreeNode("Fold Change Criterion Used: " + info.useFoldChange));
        if (info.useFoldChange == "Yes") {
            inputSubNode.add(new DefaultMutableTreeNode("Fold Change Value: " + info.foldChangeValue));
        }
        //ADD MORE INFO HERE
        inputSubNode.add(new DefaultMutableTreeNode("HCL: "+info.getMethodName()));
        inputSubNode.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time)+" ms"));
        
        computedSubNode.add(new DefaultMutableTreeNode("Computed Exchangeability Factor s0: " + info.sNought));
        computedSubNode.add(new DefaultMutableTreeNode("s0 Percentile: " + info.s0Percentile));
        computedSubNode.add(new DefaultMutableTreeNode("Pi0Hat: " + info.pi0Hat));
        //computedSubNode.add(new DefaultMutableTreeNode("Number of Significant Genes: " + info.numSigGenes));
        computedSubNode.add(new DefaultMutableTreeNode("Num. False Sig. Genes (Median): " + info.numFalseSigMed));
        computedSubNode.add(new DefaultMutableTreeNode("Num. False Sig. Genes (90th %ile): " + info.numFalseSig90th));
        computedSubNode.add(new DefaultMutableTreeNode("False Discovery Rate (Median): " + info.FDRMedian + " %"));
        computedSubNode.add(new DefaultMutableTreeNode("False Discovery Rate (90th %ile): " + info.FDR90th + " %"));
        //node.add(new DefaultMutableTreeNode(info.function));
        node.add(inputSubNode);
        node.add(computedSubNode);
        root.add(node);
    }
    
    private DefaultMutableTreeNode getSampleSurvivalInfo() {
        DefaultMutableTreeNode sampleSurvivalInfo = new DefaultMutableTreeNode("Sample information ");
        DefaultMutableTreeNode notInAnalysisNode = new DefaultMutableTreeNode("Not in analysis ");
        for (int i = 0; i < inSurvivalAnalysis.length; i++) {
            DefaultMutableTreeNode sampleNode = new DefaultMutableTreeNode((String)(exptNamesVector.get(i)));
            if (inSurvivalAnalysis[i]) {
                sampleNode.add(new DefaultMutableTreeNode("Time: " + survivalTimes[i]));
                sampleNode.add(new DefaultMutableTreeNode("State: " + (censored[i]?"Censored":"Dead")));
                sampleSurvivalInfo.add(sampleNode);
            } else {
                notInAnalysisNode.add(sampleNode);
            }
        }
        
        if (notInAnalysisNode.getChildCount() > 0) {
            sampleSurvivalInfo.add(notInAnalysisNode);
        }
        return sampleSurvivalInfo;
    }
    
    private DefaultMutableTreeNode getGroupAssignmentInfo(int studyDesign) {
        DefaultMutableTreeNode groupAssignmentInfo = new DefaultMutableTreeNode("Group assigments ");
        if (studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) {
            DefaultMutableTreeNode groupA = new DefaultMutableTreeNode("Group A ");
            DefaultMutableTreeNode groupB = new DefaultMutableTreeNode("Group B ");
            DefaultMutableTreeNode neitherGroup = new DefaultMutableTreeNode("Neither group ");
            
            int neitherGroupCounter = 0;
            
            for (int i = 0; i < groupAssignments.length; i++) {
                if (groupAssignments[i] == SAMInitDialog.GROUP_A) {
                    groupA.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
                } else if (groupAssignments[i] == SAMInitDialog.GROUP_B) {
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
        } else if (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) {
            groupAssignmentInfo = new DefaultMutableTreeNode("Pairings ");
            boolean paired[] = new boolean[exptNamesVector.size()];
            for (int i = 0; i < paired.length; i++) {
                paired[i] = false;
            }
            DefaultMutableTreeNode pairs = new DefaultMutableTreeNode("Sample Pairs");
            DefaultMutableTreeNode nonPairs = new DefaultMutableTreeNode("Unpaired Experiments");
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
            
        } else if (studyDesign == SAMInitDialog.MULTI_CLASS) {
            DefaultMutableTreeNode notInGroups = new DefaultMutableTreeNode("Not in groups");
            DefaultMutableTreeNode[] groups = new DefaultMutableTreeNode[numMultiClassGroups];
            for (int i = 0; i < numMultiClassGroups; i++) {
                groups[i] = new DefaultMutableTreeNode("Group " + (i + 1));
                
            }
            
            for (int i = 0; i < groupAssignments.length; i++) {
                int currentGroup = groupAssignments[i];
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
        } else if (studyDesign == SAMInitDialog.ONE_CLASS) {
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
        }
        //
        return groupAssignmentInfo;
    }
    
    
    
    
    
    
    public AlgorithmData getScriptParameters(IFramework framework) {
        AlgorithmData data = new AlgorithmData();
        SAMGUI.SAMFrame = (JFrame) framework.getFrame();
        this.experiment = framework.getData().getExperiment();
        this.data = framework.getData();
        exptNamesVector = new Vector();
        geneNamesVector = new Vector();
        int number_of_samples = experiment.getNumberOfSamples();
        int number_of_genes = experiment.getNumberOfGenes();
        
        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(framework.getData().getFullSampleName(experiment.getSampleIndex(i)));
        }
        for (int i = 0; i < number_of_genes; i++) {
            geneNamesVector.add(framework.getData().getGeneName(i));
        }
        
        SAMInitDialog sDialog;
        studyDesign = 0;
        int numCombs = 0;
        int numUniquePerms = 0;
        int numNeighbors = 0;
        numMultiClassGroups = 0;
        
        boolean useKNearest = true;
        boolean isHierarchicalTree = false;
        boolean usePreviousGraph = false;
        boolean saveImputedMatrix = false;
        boolean useTusherEtAlS0 = false;
        boolean useAllUniquePerms = false;
        
        double userPercentile = 0;
        
        //Always launch as first run
        
        usePreviousGraph = false;
        sDialog = new SAMInitDialog((JFrame) framework.getFrame(), true, exptNamesVector, number_of_genes, framework.getClusterRepository(1));
        sDialog.setVisible(true);
        
        if (!sDialog.isOkPressed()) {
            return null;
        } else {
            SAMState.firstRun = false;
        }
        
        //get delta value
        SAMScriptDeltaValueInitDialog deltaDialog = new SAMScriptDeltaValueInitDialog((JFrame)framework.getFrame());
        if(deltaDialog.showModal() != JOptionPane.OK_OPTION)
            return null;
        
        boolean graphInteraction = deltaDialog.interactWithGraph();       
        if(!graphInteraction)
            delta = deltaDialog.getDeltaValue();

        data.addParam("permit-graph-interaction", String.valueOf(graphInteraction));
        data.addParam("delta", String.valueOf(delta));
        
        //SAMState.firstRun = false;
        studyDesign = sDialog.getStudyDesign();
        SAMState.studyDesign = studyDesign;
        if (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) {
            pairedGroupAExpts = sDialog.getPairedAExpts();
            pairedGroupBExpts = sDialog.getPairedBExpts();
            SAMState.pairedGroupAExpts = pairedGroupAExpts;
            SAMState.pairedGroupBExpts = pairedGroupBExpts;
        }
        if (studyDesign == SAMInitDialog.MULTI_CLASS) {
            numMultiClassGroups = sDialog.getMultiClassNumGroups();
            SAMState.numMultiClassGroups = numMultiClassGroups;
        }
        if (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) {
            inSurvivalAnalysis = sDialog.isInSurvivalAnalysis();
            SAMState.inSurvivalAnalysis = inSurvivalAnalysis;
            censored = sDialog.isCensored();
            SAMState.censored = censored;
            survivalTimes =sDialog.getSurvivalTimes();
            SAMState.survivalTimes = survivalTimes;
        }
        
        if (studyDesign == SAMInitDialog.ONE_CLASS) {
            oneClassMean = (float)(sDialog.getOneClassMean());
            SAMState.oneClassMean = (double)oneClassMean;
        }
        groupAssignments = sDialog.getGroupAssignments();
        
        SAMState.groupAssignments = groupAssignments;
        //boolean useAllCombs = sDialog.useAllCombs();
        //if (!useAllCombs) {
        numCombs = sDialog.getUserNumCombs();
        SAMState.numCombs = numCombs;
        
        useAllUniquePerms = sDialog.useAllUniquePerms();
        SAMState.useAllUniquePerms = useAllUniquePerms;
        if (useAllUniquePerms) {
            numUniquePerms = sDialog.getNumUniquePerms();
            SAMState.numUniquePerms = numUniquePerms;
        }
        //}
        useKNearest = sDialog.useKNearest();
        SAMState.useKNearest = useKNearest;
        //numNeighbors = 10;
        if (useKNearest) {
            numNeighbors = sDialog.getNumNeighbors();
            SAMState.numNeighbors = numNeighbors;
        }
        isHierarchicalTree = sDialog.drawTrees();
        drawSigTreesOnly = true;
        if (isHierarchicalTree) {
            drawSigTreesOnly = sDialog.drawSigTreesOnly();
        }        
        //SAMState.isHierarchicalTree = isHierarchicalTree;
        saveImputedMatrix = sDialog.isSaveMatrix();
        
        userPercentile = sDialog.getPercentile();
        useTusherEtAlS0 = sDialog.useTusherEtAlS0();
        SAMState.useTusherEtAlS0 = useTusherEtAlS0;
        
        calculateQLowestFDR = sDialog.calculateQLowestFDR();
        SAMState.calculateQLowestFDR = calculateQLowestFDR;
        
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
        
        
        data.addParam("distance-factor", String.valueOf(1.0f));
        data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
                
        data.addParam("distance-function", String.valueOf(function));
        data.addIntArray("group-assignments", groupAssignments);
        data.addParam("study-design", String.valueOf(studyDesign));
        if (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) {
            FloatMatrix pairedAExptsMatrix = new FloatMatrix(pairedGroupAExpts.size(), 1);
            FloatMatrix pairedBExptsMatrix = new FloatMatrix(pairedGroupBExpts.size(), 1);
            
            for (int i = 0; i < pairedGroupAExpts.size(); i++) {
                pairedAExptsMatrix.A[i][0] = ((Integer)(pairedGroupAExpts.get(i))).floatValue();
                pairedBExptsMatrix.A[i][0] = ((Integer)(pairedGroupBExpts.get(i))).floatValue();
            }
            data.addMatrix("pairedAExptsMatrix", pairedAExptsMatrix);
            data.addMatrix("pairedBExptsMatrix", pairedBExptsMatrix);
        }
        
        if (studyDesign == SAMInitDialog.MULTI_CLASS) {
            data.addParam("numMultiClassGroups", String.valueOf(numMultiClassGroups));
        }
        
        if (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) {
            FloatMatrix inAnalysisMatrix, isCensoredMatrix, survivalTimesMatrix;
            inAnalysisMatrix = new FloatMatrix(inSurvivalAnalysis.length, 1);
            isCensoredMatrix = new FloatMatrix(inSurvivalAnalysis.length, 1);
            survivalTimesMatrix = new FloatMatrix(inSurvivalAnalysis.length, 1);
            for (int i = 0; i < inSurvivalAnalysis.length; i++) {
                if (!inSurvivalAnalysis[i]) {
                    inAnalysisMatrix.A[i][0] = 0.0f;
                } else {
                    inAnalysisMatrix.A[i][0] = 1.0f;
                }
                if (!censored[i]) {
                    isCensoredMatrix.A[i][0] = 0.0f;
                } else {
                    isCensoredMatrix.A[i][0] = 1.0f;
                }
                survivalTimesMatrix.A[i][0] = (float)survivalTimes[i];
            }
            data.addMatrix("inAnalysisMatrix", inAnalysisMatrix);
            data.addMatrix("isCensoredMatrix", isCensoredMatrix);
            data.addMatrix("survivalTimesMatrix", survivalTimesMatrix);
        }
        
        if (studyDesign == SAMInitDialog.ONE_CLASS) {
            data.addParam("oneClassMean", String.valueOf(oneClassMean));
        }
        data.addParam("useAllUniquePerms", String.valueOf(useAllUniquePerms));
        
        if (useAllUniquePerms) {
            data.addParam("numUniquePerms", String.valueOf(numUniquePerms));
        }
        
        data.addParam("num-combs", String.valueOf(numCombs));
        //data.addParam("use-all-combs", String.valueOf(useAllCombs));
        data.addParam("use-k-nearest", String.valueOf(useKNearest));
        data.addParam("num-neighbors", String.valueOf(numNeighbors));
        data.addParam("saveImputedMatrix", String.valueOf(saveImputedMatrix));
        data.addParam("use-previous-graph", String.valueOf(usePreviousGraph));
        data.addParam("userPercentile", String.valueOf(userPercentile));
        data.addParam("useTusherEtAlS0", String.valueOf(useTusherEtAlS0));
        data.addParam("calculateQLowestFDR", String.valueOf(calculateQLowestFDR));
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
        data.addParam("name", "SAM");
        
        // alg type
        data.addParam("alg-type", "cluster-genes");
        
        // output class
        data.addParam("output-class", "partition-output");
        
        //output nodes
        String [] outputNodes;
        if(studyDesign == SAMInitDialog.MULTI_CLASS) {
            outputNodes = new String[2];
            outputNodes[0] = "Significant Genes";
            outputNodes[1] = "Non-significant Genes";
        } else {
            outputNodes = new String[4];
            outputNodes[0] = "Positive Significant Genes";
            outputNodes[1] = "Negative Significant Genes";
            outputNodes[2] = "All Significant Genes";
            outputNodes[3] = "Non-significant Genes";
        }
        
        data.addStringArray("output-nodes", outputNodes);
        return data;
    }
    
    
    public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, Experiment experiment) throws AlgorithmException {
     
        Listener listener = new Listener();
        this.experiment = experiment;
        algData.addMatrix("experiment", experiment.getMatrix());
        this.data = framework.getData();
        AlgorithmParameters params = algData.getParams();
        this.drawSigTreesOnly = algData.getParams().getBoolean("draw-sig-trees-only");        
        this.studyDesign = params.getInt("study-design");
        
        if(this.studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED || this.studyDesign == SAMInitDialog.ONE_CLASS ||
            this.studyDesign == SAMInitDialog.MULTI_CLASS) {
            this.groupAssignments = algData.getIntArray("group-assignments");
            if(this.studyDesign == SAMInitDialog.MULTI_CLASS)
                this.numMultiClassGroups = params.getInt("numMultiClassGroups");
        } else if(this.studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) {
            FloatMatrix fm = algData.getMatrix("pairedAExptsMatrix");
            this.pairedGroupAExpts = new Vector();
            for(int i = 0; i < fm.getRowDimension(); i++)
                this.pairedGroupAExpts.addElement(new Integer((int)(fm.get(i,0))));

            fm = algData.getMatrix("pairedBExptsMatrix");           
            this.pairedGroupBExpts = new Vector();
            for(int i = 0; i < fm.getRowDimension(); i++)
                this.pairedGroupBExpts.addElement(new Integer((int)(fm.get(i,0))));
        }
        
        int number_of_samples = experiment.getNumberOfSamples();
        this.exptNamesVector = new Vector();       
        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(framework.getData().getFullSampleName(i));
        }

        
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("SAM");
            //System.out.println("SAMGUI: getting algorithm");
            algorithm.addAlgorithmListener(listener);
            
            //this.progress = new Progress(framework.getFrame(), "Finding significant genes", listener); // **** MAKE PROGRESS BARS LATER
            //this.progress.show();
            
            this.progress = new Progress(framework.getFrame(), "SAM Execution", listener);
            this.progress.show();
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(algData);
            //System.out.println("After algorithm.execute()");
            long time = System.currentTimeMillis() - start;
            // getting the results
            Cluster result_cluster = result.getCluster("cluster");
            NodeList nodeList = result_cluster.getNodeList();
            AlgorithmParameters resultMap = result.getParams();
            int k = 0;
            if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) || (studyDesign == SAMInitDialog.ONE_CLASS)) {
                k = 4; //resultMap.getInt("number-of-clusters"); // NEED THIS TO GET THE VALUE OF NUMBER-OF-CLUSTERS
            } else {
                k = 2;
            }
            
            this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            
            delta = resultMap.getFloat("delta");
            String numSigGenes = resultMap.getString("numSigGenes");
            String numFalseSigMed = resultMap.getString("numFalseSigMed");
            String numFalseSig90th = resultMap.getString("numFalseSig90th");
            String FDRMedianString = resultMap.getString("FDRMedian");
            String FDR90thString = resultMap.getString("FDR90th");
            float sNought = resultMap.getFloat("sNought");
            float s0Percentile = resultMap.getFloat("s0Percentile");
            float pi0Hat = resultMap.getFloat("pi0Hat");
            float upperCutoff;
            try {
                upperCutoff = resultMap.getFloat("upperCutoff");
            } catch (NumberFormatException nfe) {
                upperCutoff = Float.POSITIVE_INFINITY;
            }
            float lowerCutoff;
            try {
                lowerCutoff = resultMap.getFloat("lowerCutoff");
            } catch (NumberFormatException nfe) {
                lowerCutoff = Float.NEGATIVE_INFINITY;
            }
            boolean useFoldChange = resultMap.getBoolean("useFoldChange");
            float foldChangeValue = resultMap.getFloat("foldChangeValue");
            FloatMatrix dValuesMatrix = result.getMatrix("dValuesMatrix");
            FloatMatrix rValuesMatrix = result.getMatrix("rValuesMatrix");
            FloatMatrix qLowestFDRMatrix = result.getMatrix("qLowestFDRMatrix");
            FloatMatrix foldChangeMatrix = result.getMatrix("foldChangeMatrix");
            dValues = new float[dValuesMatrix.getRowDimension()];
            rValues = new float[rValuesMatrix.getRowDimension()];
            foldChangeArray =new float[foldChangeMatrix.getRowDimension()];
            qLowestFDR = new float[qLowestFDRMatrix.getRowDimension()];
            for (int i = 0; i < dValues.length; i++) {
                dValues[i] = dValuesMatrix.A[i][0];
                rValues[i] = rValuesMatrix.A[i][0];
                qLowestFDR[i] = qLowestFDRMatrix.A[i][0];
                foldChangeArray[i] = foldChangeMatrix.A[i][0];
            }
            FloatMatrix dBarMatrixX = result.getMatrix("dBarMatrixX");
            FloatMatrix sortedDMatrixY = result.getMatrix("sortedDMatrixY");
            xArray = new double[dBarMatrixX.getRowDimension()];
            yArray = new double[sortedDMatrixY.getRowDimension()];
            for (int i = 0; i < xArray.length; i++) {
                xArray[i] = (double)(dBarMatrixX.A[i][0]);
                yArray[i] = (double)(sortedDMatrixY.A[i][0]);
            }
            
            FloatMatrix deltaGridMatrix = result.getMatrix("deltaGridMatrix");
            FloatMatrix medNumFalseMatrix = result.getMatrix("medNumFalseMatrix");
            FloatMatrix false90thMatrix = result.getMatrix("false90thMatrix");
            FloatMatrix numSigMatrix = result.getMatrix("numSigMatrix");
            FloatMatrix FDRMedianMatrix = result.getMatrix("FDRMedianMatrix");
            FloatMatrix FDR90thMatrix = result.getMatrix("FDR90thMatrix");
            
            deltaGrid = new double[deltaGridMatrix.getRowDimension()];
            medNumFalse = new double[medNumFalseMatrix.getRowDimension()];
            false90th = new double[false90thMatrix.getRowDimension()];
            numSig = new int[numSigMatrix.getRowDimension()];
            FDRMedian = new double[FDRMedianMatrix.getRowDimension()];
            FDR90th = new double[FDR90thMatrix.getRowDimension()];
            
            
            for (int i= 0; i < deltaGrid.length; i++) {
                deltaGrid[i] = (double)(deltaGridMatrix.A[i][0]);
                medNumFalse[i] = (double)(medNumFalseMatrix.A[i][0]);
                false90th[i] = (double)(false90thMatrix.A[i][0]);
                numSig[i] = (int)(numSigMatrix.A[i][0]);
                FDRMedian[i] = (double)(FDRMedianMatrix.A[i][0]);
                FDR90th[i] = (double)(FDR90thMatrix.A[i][0]);
            }
            //int studyDesign = resultMap.getInt("studyDesign");
            
           /*
            *  Scripting will not support saving the imputed matrix to file
            * on the first pass
            *
            *
            if ((!usePreviousGraph) && (saveImputedMatrix)) {
                FloatMatrix imputedMatrix = result.getMatrix("imputedMatrix");
                final JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Save imputed matrix");
                fc.setCurrentDirectory(new File("Data"));
                int returnVal = fc.showSaveDialog((JFrame) framework.getFrame());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    try {
                        PrintWriter out = new PrintWriter(new FileOutputStream(file));
                        String[] fieldNames = framework.getData().getFieldNames();
                        //out.print("Original row");
                        //out.print("\t");
                        for (int i = 0; i < fieldNames.length; i++) {
                            out.print(fieldNames[i]);
                            if (i < fieldNames.length - 1) {
                                out.print("\t");
                            }
                        }
                        for (int i=0; i<experiment.getNumberOfSamples(); i++) {
                            out.print("\t");
                            out.print(framework.getData().getFullSampleName(experiment.getSampleIndex(i)));
                        }
                        out.print("\n");
                        for (int i=0; i<imputedMatrix.getRowDimension(); i++) {
                            //out.print(Integer.toString(experiment.getGeneIndexMappedToData(rows[i]) + 1));  //handles cutoffs
                            //out.print(data.getUniqueId(rows[i]));
                            //out.print("\t");
                            //out.print(data.getGeneName(rows[i]));
                            for (int f = 0; f < fieldNames.length; f++) {
                                out.print(framework.getData().getElementAttribute(experiment.getGeneIndexMappedToData(i), f));
                                if (f < fieldNames.length - 1) {
                                    out.print("\t");
                                }
                            }
                            for (int j=0; j<imputedMatrix.getColumnDimension(); j++) {
                                out.print("\t");
                                out.print(Float.toString(imputedMatrix.A[i][j]));
                            }
                            out.print("\n");
                        }
                        //int[] groupAssgn = getGroupAssignments();
            *
            */
                        /*
                        for (int i = 0; i < groupAssgn.length; i++) {
                            out.print(groupAssgn[i]);
                            if (i < groupAssgn.length - 1) {
                                out.print("\t");
                            }
                        }
                        out.println();
                         */
              /*
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                    //this is where a real application would save the file.
                    //log.append("Saving: " + file.getName() + "." + newline);
              
                } else {
                    //log.append("Save command cancelled by user." + newline);
                }
            }
            
            */
           

            
            GeneralInfo info = new GeneralInfo();

            info.time = time;
            //ADD MORE INFO PARAMETERS HERE
            info.delta = delta;
            info.upperCutoff = upperCutoff;
            info.lowerCutoff = lowerCutoff;
            info.useAllUniquePerms = params.getBoolean("useAllUniquePerms");
            if(info.useAllUniquePerms)
                info.numUniquePerms = params.getInt("numUniquePerms");
            if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED)) {
                if (params.getBoolean("useFoldChange")) {
                    info.useFoldChange = "Yes";
                    info.foldChangeValue = params.getFloat("foldChangeValue");
                } else {
                    info.useFoldChange = "No";
                }
            } else {
                info.useFoldChange = "N/A";
            }
            if (studyDesign == SAMInitDialog.MULTI_CLASS) {
                info.numMultiClassGroups = params.getInt("numMultiClassGroups");
            }
            
            if (studyDesign == SAMInitDialog.ONE_CLASS) {
                info.oneClassMean = params.getFloat("oneClassMean");
            }
            
            info.numSigGenes = numSigGenes;
            info.numFalseSigMed = numFalseSigMed;
            info.numFalseSig90th = numFalseSig90th;
            info.FDRMedian = FDRMedianString;
            info.FDR90th = FDR90thString;
            info.studyDesign = studyDesign;
            
            if (params.getBoolean("useKNearest")) {
                info.imputationEngine = "K-Nearest Neighbors";
                info.numNeighbors = params.getInt("numNeighbors");
            } else {
                info.imputationEngine = "Row Average";
            }
            info.numCombs = params.getInt("num-combs");
            info.sNought = sNought;
            info.s0Percentile = s0Percentile;
            info.pi0Hat = pi0Hat;
            //info.function = menu.getFunctionName(function);
            
            info.hcl = params.getBoolean("hierarchical-tree");
            info.hcl_genes = params.getBoolean("calculate-genes");
            info.hcl_samples = params.getBoolean("calculate-experiments");
            if(info.hcl)
                info.hcl_method = params.getInt("method-linkage");
            
            Vector allFields = new Vector();
            
            allFields.add("Score(d)");
            allFields.add("Numerator(r)");
            allFields.add("Denominator (s+s0)");
            if ((studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED)) {
                allFields.add("Fold change (Unlogged)");
            }
            if (calculateQLowestFDR) {
                allFields.add("q-value (%)");
            }
            
            auxTitles = new String[allFields.size()];
            for (int i = 0; i < auxTitles.length; i++) {
                auxTitles[i] = (String)(allFields.get(i));
            }
            
            auxData = new float[experiment.getNumberOfGenes()][auxTitles.length];
            for (int i = 0; i < auxData.length; i++) {
                int counter = 0;
                auxData[i][counter++] = new Float(dValues[i]);
                auxData[i][counter++] = new Float(rValues[i]);
                auxData[i][counter++] = new Float((float)(rValues[i]/dValues[i]));
                if ((studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED)) {
                    auxData[i][counter++] = new Float(foldChangeArray[i]);
                }
                if (calculateQLowestFDR) {
                    auxData[i][counter++] = new Float(qLowestFDR[i]);
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
    
    private class GeneralInfo {
        public int clusters;
        public String sigMethod, useFoldChange;
        
        //public boolean converged;
        //public int iterations;
        //public int userNumClusters;
        public long time;
        //public String function;
        //public int numReps;
        //public double thresholdPercent;
        private float delta, sNought, s0Percentile, pi0Hat, foldChangeValue, upperCutoff, lowerCutoff, oneClassMean;
        private String numSigGenes, numFalseSigMed, numFalseSig90th, FDRMedian, FDR90th;
        private int studyDesign;
        private int numCombs, numUniquePerms;
        private String imputationEngine;
        private int numNeighbors;
        private int numMultiClassGroups;
        private boolean hcl;
        private int hcl_method;
        private boolean hcl_genes;
        private boolean hcl_samples;
        private boolean useAllUniquePerms;
        
        public String getMethodName() {
            return hcl ? HCLGUI.GeneralInfo.getMethodName(hcl_method) : "no linkage";
        }
        
        public String getStudyDesign() {
            String study = "None";
            if (studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) {
                study = "Two Class Unpaired";
            } else if (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) {
                study = "Two Class Paired";
            } else if (studyDesign == SAMInitDialog.MULTI_CLASS) {
                study = "Multi Class";
            } else if (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) {
                study = "Censored Survival";
            } else if (studyDesign == SAMInitDialog.ONE_CLASS) {
                study = "One Class";
            }
            return study;
        }
        
    }
    
}
