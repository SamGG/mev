/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * KNNCGUI.java
 *
 * Created on September 2, 2003, 4:59 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.knnc;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JFileChooser;
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
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterCentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterTableViewer;
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
 */
public class KNNCGUI implements IClusterGUI, IScriptGUI {
    
    private Algorithm algorithm;
    private Progress progress;
    private Experiment experiment;
    private IData data;
    private int[][] clusters;
    private FloatMatrix means;
    private FloatMatrix variances;
    private int k, usedNumNeibs;
    KNNClassificationEditor kcEditor;
    boolean validate, classifyGenes, useVarianceFilter, useCorrelFilter;
    int numClasses, numVarFilteredVectors, numNeighbors, numPerms;
    double correlPValue;
    Vector[] classificationVector;
    
    int[] classIndices, classes, origNumInFiltTrgSetByClass, numberCorrectlyClassifiedByClass, numberIncorrectlyClassifiedByClass;
    
    /** Creates a new instance of KNNCGUI */
    public KNNCGUI() {
    }
    
    /** This method should return a tree with calculation results or
     * null, if analysis start was canceled.
     *
     * @param framework the reference to <code>IFramework</code> implementation,
     *        which is used to obtain an initial analysis data and parameters.
     * @throws AlgorithmException if calculation was failed.
     * @throws AbortException if calculation was canceled.
     * @see IFramework
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        this.data = framework.getData();
        this.experiment = framework.getData().getExperiment();
        //this.data = framework.getData();
        
        int number_of_samples = experiment.getNumberOfSamples();
        int number_of_genes = experiment.getNumberOfGenes();
        
        KNNClassifyOrValidateDialog kCOrVDialog = new KNNClassifyOrValidateDialog((JFrame)framework.getFrame(), true);
        kCOrVDialog.setVisible(true);
        validate = !(kCOrVDialog.classify());
        if (!kCOrVDialog.isOkPressed()) {
            return null;
        }
        
        if (!validate) {
            KNNCStatusDialog kStatDialog = new KNNCStatusDialog((JFrame)framework.getFrame(), false);
            kStatDialog.setVisible(true);
            KNNCFirstDialog kDialog = new KNNCFirstDialog((JFrame)framework.getFrame(), true, framework);
            kDialog.setVisible(true);
            
            if (!kDialog.isOkPressed()) {
                kStatDialog.dispose();
                return null;
            }
            
            classifyGenes = kDialog.classifyGenes();
            numClasses = kDialog.getNumClasses();
            numNeighbors = kDialog.getNumNeighbors();
            useVarianceFilter = kDialog.useVarianceFilter();
            if (useVarianceFilter) {
                numVarFilteredVectors = kDialog.getNumVectors();
            }
            useCorrelFilter = kDialog.useCorrelFilter();
            if (useCorrelFilter) {
                correlPValue = kDialog.getCorrPValue();
                numPerms = kDialog.getNumPerms();
            }
            k = numClasses*4 + 1;
            kcEditor = new KNNClassificationEditor(framework, classifyGenes, numClasses);
            if (kDialog.createNewTrgSet()) {
                kcEditor.showModal(true);
            } else {
                final JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("Data"));
                int returnVal = fc.showOpenDialog(framework.getFrame());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    kcEditor.loadFromFile(fc.getSelectedFile());
                } else {
                    kStatDialog.dispose();
                    return null;
                }
            }
            
            if (kcEditor.fileIsIncompatible()) {
                kStatDialog.dispose();
                return null;
            }
            
            while (!kcEditor.isNextPressed()) {
                continue;
            }
            
            //System.out.println("KNNCGUI: kcEditor.proceed() = " + kcEditor.proceed());
            
            if (!kcEditor.proceed()) {
                kStatDialog.dispose();
                return null;
            }
            
            // **** DONE UP TO HERE: 9_25_03, AT THIS POINT, NEED TO getClassificaiton() from kcEditor
            
            classificationVector = kcEditor.getClassification();
            classIndices = new int[classificationVector[0].size()];
            classes = new int[classificationVector[1].size()];
            
            for (int i = 0; i < classIndices.length; i++) {
                classIndices[i] = ((Integer)(classificationVector[0].get(i))).intValue();
                classes[i] = ((Integer)(classificationVector[1].get(i))).intValue();
            }
            
            boolean isHierarchicalTree = kDialog.drawTrees();

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
                //System.out.println("Proceeded to algorithm");
                algorithm = framework.getAlgorithmFactory().getAlgorithm("KNNC");
                algorithm.addAlgorithmListener(listener);
                
                this.progress = new Progress(framework.getFrame(), "KNN classification", listener);
                this.progress.show();
                
                AlgorithmData data = new AlgorithmData();
                
                data.addMatrix("experiment", experiment.getMatrix());
                data.addParam("distance-factor", String.valueOf(1.0f));

                data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
                
                data.addParam("distance-function", String.valueOf(function));
                
                data.addParam("validate", String.valueOf(validate));
                data.addParam("classifyGenes", String.valueOf(classifyGenes));
                if (classifyGenes) {
                    data.addMatrix("experiment", experiment.getMatrix());
                } else {
                    data.addMatrix("experiment", experiment.getMatrix().transpose());
                }
                data.addParam("useVarianceFilter", String.valueOf(useVarianceFilter));
                data.addParam("useCorrelFilter", String.valueOf(useCorrelFilter));
                data.addParam("numClasses", String.valueOf(numClasses));
                data.addParam("numNeighbors", String.valueOf(numNeighbors));
                if (useVarianceFilter) {
                    data.addParam("numVarFilteredVectors", String.valueOf(numVarFilteredVectors));
                }
                if (useCorrelFilter) {
                    data.addParam("correlPValue", String.valueOf((float)correlPValue));
                    data.addParam("numPerms", String.valueOf(numPerms));
                }
                data.addIntArray("classIndices", classIndices);
                data.addIntArray("classes", classes);
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
                this.clusters = new int[k][];
                for (int i=0; i<k; i++) {
                    clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
                }
                this.means = result.getMatrix("clusters_means");
                this.variances = result.getMatrix("clusters_variances");
                
                this.numberCorrectlyClassifiedByClass = result.getIntArray("numberCorrectlyClassifiedByClass");
                this.numberIncorrectlyClassifiedByClass = result.getIntArray("numberIncorrectlyClassifiedByClass");
                this.origNumInFiltTrgSetByClass = result.getIntArray("origNumInFiltTrgSetByClass");
                
                GeneralInfo info = new GeneralInfo();
                //info.clusters = k;
                info.time = time;
                info.hcl = isHierarchicalTree;
                info.hcl_genes = hcl_genes;
                info.hcl_samples = hcl_samples;
                info.hcl_method = hcl_method;
                info.numClasses = numClasses;
                info.numNeighbors = numNeighbors;
                info.usedVarFilter = useVarianceFilter;
                if (useVarianceFilter) {
                    info.numVarFiltered = this.numVarFilteredVectors;
                    info.postVarDataSetSize = (result.getParams()).getInt("postVarDataSetSize");
                    info.postVarClassSetSize = (result.getParams()).getInt("postVarClassSetSize");
                }
                info.usedCorrelFilter = this.useCorrelFilter;
                if (useCorrelFilter) {
                    info.correlPvalue = this.correlPValue;
                    info.numPerms = this.numPerms;
                    info.postCorrDataSetSize = (result.getParams()).getInt("postCorrDataSetSize");
                }
                info.usedNumNeibs = (result.getParams()).getInt("usedNumNeibs");
                info.origDataSetSize = (result.getParams()).getInt("origDataSetSize");
                info.origClassSetSize = (result.getParams()).getInt("origClassSetSize");
                
                return createResultTree(result_cluster, info);
                
            } finally {
                kStatDialog.dispose();
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
            
        } else {// if (validate)
            KNNCStatusDialog kStatDialog = new KNNCStatusDialog((JFrame)framework.getFrame(), false);
            kStatDialog.setVisible(true);
            KNNCValidationFirstDialog kvDialog = new KNNCValidationFirstDialog((JFrame)framework.getFrame(), true, framework);
            kvDialog.setVisible(true);
            
            if (!kvDialog.isOkPressed()) {
                kStatDialog.dispose();
                return null;
            }
            
            classifyGenes = kvDialog.classifyGenes();
            numClasses = kvDialog.getNumClasses();
            numNeighbors = kvDialog.getNumNeighbors();
            
            useCorrelFilter = kvDialog.useCorrelFilter();
            if (useCorrelFilter) {
                correlPValue = kvDialog.getCorrPValue();
                numPerms = kvDialog.getNumPerms();
            }
            k = numClasses*4 + 1;
            kcEditor = new KNNClassificationEditor(framework, classifyGenes, numClasses);
            if (kvDialog.createNewTrgSet()) {
                //kcEditor = new KNNClassificationEditor(framework, classifyGenes, numClasses);
                kcEditor.showModal(true);
            } else {
                final JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("Data"));
                int returnVal = fc.showOpenDialog(framework.getFrame());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    //kcEditor = new KNNClassificationEditor(framework, classifyGenes, numClasses);
                    //kcEditor.setVisible(true);
                    kcEditor.loadFromFile(fc.getSelectedFile());
                } else {
                    kStatDialog.dispose();
                    return null;
                }
            }
            
            if (kcEditor.fileIsIncompatible()) {
                kStatDialog.dispose();
                return null;
            }
            
            while (!kcEditor.isNextPressed()) {
                continue;
            }
            if (!kcEditor.proceed()) {
                kStatDialog.dispose();
                return null;
            }
            
            classificationVector = kcEditor.getClassification();
            classIndices = new int[classificationVector[0].size()];
            classes = new int[classificationVector[1].size()];
            
            for (int i = 0; i < classIndices.length; i++) {
                classIndices[i] = ((Integer)(classificationVector[0].get(i))).intValue();
                classes[i] = ((Integer)(classificationVector[1].get(i))).intValue();
            }
            
            boolean isHierarchicalTree = kvDialog.drawTrees();
            
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
                //System.out.println("Proceeded to algorithm");
                algorithm = framework.getAlgorithmFactory().getAlgorithm("KNNC");
                algorithm.addAlgorithmListener(listener);
                
                this.progress = new Progress(framework.getFrame(), "KNN classification", listener);
                this.progress.show();
                
                AlgorithmData data = new AlgorithmData();
                
                data.addMatrix("experiment", experiment.getMatrix());
                data.addParam("distance-factor", String.valueOf(1.0f));
                data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
                                
                data.addParam("distance-function", String.valueOf(function));
                
                data.addParam("validate", String.valueOf(validate));
                data.addParam("classifyGenes", String.valueOf(classifyGenes));
                if (classifyGenes) {
                    data.addMatrix("experiment", experiment.getMatrix());
                } else {
                    data.addMatrix("experiment", experiment.getMatrix().transpose());
                }
                //data.addParam("useVarianceFilter", String.valueOf(useVarianceFilter));
                data.addParam("useCorrelFilter", String.valueOf(useCorrelFilter));
                data.addParam("numClasses", String.valueOf(numClasses));
                data.addParam("numNeighbors", String.valueOf(numNeighbors));
                /*
                if (useVarianceFilter) {
                    data.addParam("numVarFilteredVectors", String.valueOf(numVarFilteredVectors));
                }
                 */
                if (useCorrelFilter) {
                    data.addParam("correlPValue", String.valueOf((float)correlPValue));
                    data.addParam("numPerms", String.valueOf(numPerms));
                }
                data.addIntArray("classIndices", classIndices);
                data.addIntArray("classes", classes);
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
                k = numClasses + 1;
                this.clusters = new int[k][];
                for (int i=0; i<k; i++) {
                    clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
                }
                this.means = result.getMatrix("clusters_means");
                this.variances = result.getMatrix("clusters_variances");
                
                this.numberCorrectlyClassifiedByClass = result.getIntArray("numberCorrectlyClassifiedByClass");
                this.numberIncorrectlyClassifiedByClass = result.getIntArray("numberIncorrectlyClassifiedByClass");
                this.origNumInFiltTrgSetByClass = result.getIntArray("origNumInFiltTrgSetByClass");
                
                GeneralInfo info = new GeneralInfo();
                //info.clusters = k;
                info.time = time;
                info.hcl = isHierarchicalTree;
                info.hcl_genes = hcl_genes;
                info.hcl_samples = hcl_samples;
                info.hcl_method = hcl_method;
                info.numClasses = numClasses;
                info.numNeighbors = numNeighbors;
                //info.usedVarFilter = useVarianceFilter;
                /*
                if (useVarianceFilter) {
                    info.numVarFiltered = this.numVarFilteredVectors;
                    info.postVarDataSetSize = (result.getParams()).getInt("postVarDataSetSize");
                    info.postVarClassSetSize = (result.getParams()).getInt("postVarClassSetSize");
                }
                 */
                info.usedCorrelFilter = this.useCorrelFilter;
                if (useCorrelFilter) {
                    info.correlPvalue = this.correlPValue;
                    info.numPerms = this.numPerms;
                    //info.postCorrDataSetSize = (result.getParams()).getInt("postCorrDataSetSize");
                }
                info.usedNumNeibs = (result.getParams()).getInt("usedNumNeibs");
                //info.origDataSetSize = (result.getParams()).getInt("origDataSetSize");
                //info.origClassSetSize = (result.getParams()).getInt("origClassSetSize");
                
                return createValidationResultTree(result_cluster, info);
                //return createResultTree(result_cluster, info);
                
            }  finally {
                kStatDialog.dispose();
                if (algorithm != null) {
                    algorithm.removeAlgorithmListener(listener);
                }
                if (progress != null) {
                    progress.dispose();
                }
            }
            
        } // end if (validate)
        
        //return null; //for now
    }
    
    
    
    
    
    
    
    public AlgorithmData getScriptParameters(IFramework framework) {
        
        AlgorithmData data = new AlgorithmData();
        
        this.data = framework.getData();
        this.experiment = framework.getData().getExperiment();
        //this.data = framework.getData();
        
        int number_of_samples = experiment.getNumberOfSamples();
        int number_of_genes = experiment.getNumberOfGenes();
        
        //KNNCStatusDialog kStatDialog = new KNNCStatusDialog((JFrame)framework.getFrame(), false);
        //kStatDialog.setVisible(true);
        KNNClassifyOrValidateDialog kCOrVDialog = new KNNClassifyOrValidateDialog((JFrame)framework.getFrame(), true);
        kCOrVDialog.setVisible(true);
        validate = !(kCOrVDialog.classify());
        if (!kCOrVDialog.isOkPressed()) {
            //kStatDialog.dispose();
            return null;
        }
        //KNNCInitDialog kDialog = new KNNCInitDialog((JFrame)framework.getFrame(), false, framework);
        
        if (!validate) {
            KNNCStatusDialog kStatDialog = new KNNCStatusDialog((JFrame)framework.getFrame(), false);
            kStatDialog.setVisible(true);
            KNNCFirstDialog kDialog = new KNNCFirstDialog((JFrame)framework.getFrame(), true, framework);
            kDialog.setVisible(true);
            
            if (!kDialog.isOkPressed()) {
                kStatDialog.dispose();
                return null;
            }
            
            classifyGenes = kDialog.classifyGenes();
            numClasses = kDialog.getNumClasses();
            numNeighbors = kDialog.getNumNeighbors();
            useVarianceFilter = kDialog.useVarianceFilter();
            if (useVarianceFilter) {
                numVarFilteredVectors = kDialog.getNumVectors();
            }
            useCorrelFilter = kDialog.useCorrelFilter();
            if (useCorrelFilter) {
                correlPValue = kDialog.getCorrPValue();
                numPerms = kDialog.getNumPerms();
            }
            k = numClasses*4 + 1;
            kcEditor = new KNNClassificationEditor(framework, classifyGenes, numClasses);
            if (kDialog.createNewTrgSet()) {
                kcEditor.showModal(true);
            } else {
                final JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("Data"));
                int returnVal = fc.showOpenDialog(framework.getFrame());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    kcEditor.loadFromFile(fc.getSelectedFile());
                } else {
                    kStatDialog.dispose();
                    return null;
                }
            }
            
            kcEditor.dispose();
            
            //    if (kcEditor.fileIsIncompatible()) {
            //       kStatDialog.dispose();
            //      return null;
            //  }
            
            //    while (!kcEditor.isNextPressed()) {
            //       continue;
            //   }
            
            //System.out.println("KNNCGUI: kcEditor.proceed() = " + kcEditor.proceed());
            
            //    if (!kcEditor.proceed()) {
            //       kStatDialog.dispose();
            //       return null;
            //    }
            
            // **** DONE UP TO HERE: 9_25_03, AT THIS POINT, NEED TO getClassificaiton() from kcEditor
            
            classificationVector = kcEditor.getClassification();
            classIndices = new int[classificationVector[0].size()];
            classes = new int[classificationVector[1].size()];
            
            for (int i = 0; i < classIndices.length; i++) {
                classIndices[i] = ((Integer)(classificationVector[0].get(i))).intValue();
                classes[i] = ((Integer)(classificationVector[1].get(i))).intValue();
            }
            
            boolean isHierarchicalTree = kDialog.drawTrees();

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
            
            data.addParam("validate", String.valueOf(validate));
            data.addParam("classifyGenes", String.valueOf(classifyGenes));
            
            data.addParam("useVarianceFilter", String.valueOf(useVarianceFilter));
            data.addParam("useCorrelFilter", String.valueOf(useCorrelFilter));
            data.addParam("numClasses", String.valueOf(numClasses));
            data.addParam("numNeighbors", String.valueOf(numNeighbors));
            if (useVarianceFilter) {
                data.addParam("numVarFilteredVectors", String.valueOf(numVarFilteredVectors));
            }
            if (useCorrelFilter) {
                data.addParam("correlPValue", String.valueOf((float)correlPValue));
                data.addParam("numPerms", String.valueOf(numPerms));
            }
            data.addIntArray("classIndices", classIndices);
            data.addIntArray("classes", classes);
            // hcl parameters
            if (isHierarchicalTree) {
                data.addParam("hierarchical-tree", String.valueOf(true));
                data.addParam("method-linkage", String.valueOf(hcl_method));
                data.addParam("calculate-genes", String.valueOf(hcl_genes));
                data.addParam("calculate-experiments", String.valueOf(hcl_samples));
                data.addParam("hcl-distance-function", String.valueOf(hcl_function));
                data.addParam("hcl-distance-absolute", String.valueOf(hcl_absolute));
            }
            // alg name
            data.addParam("name", "KNNC");
            
        // alg type
        if(classifyGenes)
            data.addParam("alg-type", "cluster-genes");
        else
            data.addParam("alg-type", "cluster-experiments");
            
            // output class
            data.addParam("output-class", "partition-output");
            
            //output nodes
            String [] outputNodes = new String[4*numClasses+1];
            String str;
            for(int i = 0; i < numClasses ; i++) {
                str = String.valueOf(i+1)+"  ";
                outputNodes[i] = "Used classifiers: Class "+str;
                outputNodes[numClasses+i] = "Unused classifiers: Class "+str;
                outputNodes[2*numClasses+i] = "Classified: Class "+str;
                outputNodes[3*numClasses+i] = "Used classifiers + classified: Class "+str;
            }
            outputNodes[4*numClasses] = "Unclassified";
            
            data.addStringArray("output-nodes", outputNodes);
            
            kStatDialog.dispose();
            
        } else {
                      
            KNNCStatusDialog kStatDialog = new KNNCStatusDialog((JFrame)framework.getFrame(), false);
            kStatDialog.setVisible(true);
            KNNCValidationFirstDialog kvDialog = new KNNCValidationFirstDialog((JFrame)framework.getFrame(), true, framework);
            kvDialog.setVisible(true);
            
            if (!kvDialog.isOkPressed()) {
                kStatDialog.dispose();
                return null;
            }
            
            classifyGenes = kvDialog.classifyGenes();
            numClasses = kvDialog.getNumClasses();
            numNeighbors = kvDialog.getNumNeighbors();
            
            useCorrelFilter = kvDialog.useCorrelFilter();
            if (useCorrelFilter) {
                correlPValue = kvDialog.getCorrPValue();
                numPerms = kvDialog.getNumPerms();
            }
            k = numClasses*4 + 1;
            kcEditor = new KNNClassificationEditor(framework, classifyGenes, numClasses);
            if (kvDialog.createNewTrgSet()) {
                //kcEditor = new KNNClassificationEditor(framework, classifyGenes, numClasses);
                kcEditor.showModal(true);
            } else {
                final JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("Data"));
                int returnVal = fc.showOpenDialog(framework.getFrame());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    kcEditor.loadFromFile(fc.getSelectedFile());
                } else {
                    kStatDialog.dispose();
                    return null;
                }
            }
            
            if (kcEditor.fileIsIncompatible()) {
                kStatDialog.dispose();
                return null;
            }
            
            while (!kcEditor.isNextPressed()) {
                continue;
            }
            if (!kcEditor.proceed()) {
                kStatDialog.dispose();
                return null;
            }
            
            classificationVector = kcEditor.getClassification();
            classIndices = new int[classificationVector[0].size()];
            classes = new int[classificationVector[1].size()];
            
            for (int i = 0; i < classIndices.length; i++) {
                classIndices[i] = ((Integer)(classificationVector[0].get(i))).intValue();
                classes[i] = ((Integer)(classificationVector[1].get(i))).intValue();
            }
            
            boolean isHierarchicalTree = kvDialog.drawTrees();
            

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
            
            data.addParam("validate", String.valueOf(validate));
            data.addParam("classifyGenes", String.valueOf(classifyGenes));
            
            //data.addParam("useVarianceFilter", String.valueOf(useVarianceFilter));
            data.addParam("useCorrelFilter", String.valueOf(useCorrelFilter));
            data.addParam("numClasses", String.valueOf(numClasses));
            data.addParam("numNeighbors", String.valueOf(numNeighbors));
                /*
                if (useVarianceFilter) {
                    data.addParam("numVarFilteredVectors", String.valueOf(numVarFilteredVectors));
                }
                 */
            if (useCorrelFilter) {
                data.addParam("correlPValue", String.valueOf((float)correlPValue));
                data.addParam("numPerms", String.valueOf(numPerms));
            }
            data.addIntArray("classIndices", classIndices);
            data.addIntArray("classes", classes);
            // hcl parameters
            
            if (isHierarchicalTree) {
                data.addParam("hierarchical-tree", String.valueOf(true));
                data.addParam("method-linkage", String.valueOf(hcl_method));
                data.addParam("calculate-genes", String.valueOf(hcl_genes));
                data.addParam("calculate-experiments", String.valueOf(hcl_samples));
                data.addParam("hcl-distance-function", String.valueOf(hcl_function));
                data.addParam("hcl-distance-absolute", String.valueOf(hcl_absolute));
            }
            
            k = numClasses + 1;
            
            // alg name
            data.addParam("name", "KNNC");
            
            // alg type
            if(classifyGenes)
                data.addParam("alg-type", "cluster-genes");
            else
                data.addParam("alg-type", "cluster-experiments");
            
            // output class
            data.addParam("output-class", "partition-output");
            
            //output nodes
            String [] outputNodes = new String[numClasses+1];
            
            for(int i = 0; i < numClasses; i++) {
                outputNodes[i] = "Training Class "+String.valueOf(i+1)+"  ";
            }
            outputNodes[numClasses] = "Not in Training Set  ";
            
            data.addStringArray("output-nodes", outputNodes);
            
            kStatDialog.dispose();
            
        }
        return data;
    }
    
    
    
    
    
    
    public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, Experiment experiment) throws AlgorithmException {
        AlgorithmParameters params = algData.getParams();
         Listener listener = new Listener();
                    
        this.data = framework.getData();
        this.experiment = experiment;
        this.classifyGenes = params.getBoolean("classifyGenes");
        this.numClasses = params.getInt("numClasses");
        this.numNeighbors = params.getInt("numNeighbors");
        this.useCorrelFilter = params.getBoolean("useCorrelFilter");
        this.validate = params.getBoolean("validate");
        int number_of_samples = experiment.getNumberOfSamples();
        int number_of_genes = experiment.getNumberOfGenes();
        
        if (useCorrelFilter) {
            this.correlPValue = params.getFloat("correlPValue");
            this.numPerms = params.getInt("numPerms");
        }
        
        if (!validate) {
            
            this.useVarianceFilter = params.getBoolean("useVarianceFilter");
            if(this.useVarianceFilter)
                this.numVarFilteredVectors = params.getInt("numVarFilteredVectors");
            
   
            try {
                algorithm = framework.getAlgorithmFactory().getAlgorithm("KNNC");
                algorithm.addAlgorithmListener(listener);
                
                this.progress = new Progress(framework.getFrame(), "KNN classification", listener);
                this.progress.show();
                
                if (classifyGenes) {
                    algData.addMatrix("experiment", experiment.getMatrix());
                } else {
                    algData.addMatrix("experiment", experiment.getMatrix().transpose());
                }
                
                long start = System.currentTimeMillis();
                AlgorithmData result = algorithm.execute(algData);
                long time = System.currentTimeMillis() - start;
                // getting the results
                Cluster result_cluster = result.getCluster("cluster");
                NodeList nodeList = result_cluster.getNodeList();
                k = numClasses*4 + 1;
                this.clusters = new int[k][];
                for (int i=0; i<k; i++) {
                    clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
                }
                this.means = result.getMatrix("clusters_means");
                this.variances = result.getMatrix("clusters_variances");
                
                this.numberCorrectlyClassifiedByClass = result.getIntArray("numberCorrectlyClassifiedByClass");
                this.numberIncorrectlyClassifiedByClass = result.getIntArray("numberIncorrectlyClassifiedByClass");
                this.origNumInFiltTrgSetByClass = result.getIntArray("origNumInFiltTrgSetByClass");
                
                GeneralInfo info = new GeneralInfo();
                //info.clusters = k;
                info.time = time;
                info.hcl = params.getBoolean("hierarchical-tree");
                info.hcl_genes = params.getBoolean("calculate-genes");
                info.hcl_samples = params.getBoolean("calculate-experiments");
                if(info.hcl)
                    info.hcl_method = params.getInt("method-linkage");
                info.numClasses = numClasses;
                info.numNeighbors = numNeighbors;
                info.usedVarFilter = useVarianceFilter;
                if (useVarianceFilter) {
                    info.numVarFiltered = this.numVarFilteredVectors;
                    info.postVarDataSetSize = (result.getParams()).getInt("postVarDataSetSize");
                    info.postVarClassSetSize = (result.getParams()).getInt("postVarClassSetSize");
                }
                info.usedCorrelFilter = this.useCorrelFilter;
                if (useCorrelFilter) {
                    info.correlPvalue = this.correlPValue;
                    info.numPerms = this.numPerms;
                    info.postCorrDataSetSize = (result.getParams()).getInt("postCorrDataSetSize");
                }
                info.usedNumNeibs = (result.getParams()).getInt("usedNumNeibs");
                info.origDataSetSize = (result.getParams()).getInt("origDataSetSize");
                info.origClassSetSize = (result.getParams()).getInt("origClassSetSize");
                
                return createResultTree(result_cluster, info);
                
            } finally {
                
                if (algorithm != null) {
                    algorithm.removeAlgorithmListener(listener);
                }
                if (progress != null) {
                    progress.dispose();
                }
            }
            
        } else {// if (validate)

            try {
                //System.out.println("Proceeded to algorithm");
                algorithm = framework.getAlgorithmFactory().getAlgorithm("KNNC");
                algorithm.addAlgorithmListener(listener);
                
                this.progress = new Progress(framework.getFrame(), "KNN classification", listener);
                this.progress.show();
                
                if (classifyGenes) {
                    algData.addMatrix("experiment", experiment.getMatrix());
                } else {
                    algData.addMatrix("experiment", experiment.getMatrix().transpose());
                }
                
                long start = System.currentTimeMillis();
                AlgorithmData result = algorithm.execute(algData);
                long time = System.currentTimeMillis() - start;
                // getting the results
                Cluster result_cluster = result.getCluster("cluster");
                NodeList nodeList = result_cluster.getNodeList();
                k = numClasses + 1;
                this.clusters = new int[k][];
                for (int i=0; i<k; i++) {
                    clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
                }
                this.means = result.getMatrix("clusters_means");
                this.variances = result.getMatrix("clusters_variances");
                
                this.numberCorrectlyClassifiedByClass = result.getIntArray("numberCorrectlyClassifiedByClass");
                this.numberIncorrectlyClassifiedByClass = result.getIntArray("numberIncorrectlyClassifiedByClass");
                this.origNumInFiltTrgSetByClass = result.getIntArray("origNumInFiltTrgSetByClass");
                
                GeneralInfo info = new GeneralInfo();
                //info.clusters = k;
                info.time = time;
                info.hcl = params.getBoolean("hierarchical-tree");
                info.hcl_genes = params.getBoolean("calculate-genes");
                info.hcl_samples = params.getBoolean("calculate-experiments");
                if(info.hcl)
                    info.hcl_method = params.getInt("method-linkage");
                info.numClasses = numClasses;
                info.numNeighbors = numNeighbors;
                info.usedCorrelFilter = this.useCorrelFilter;
                if (useCorrelFilter) {
                    info.correlPvalue = this.correlPValue;
                    info.numPerms = this.numPerms;
                }
                info.usedNumNeibs = (result.getParams()).getInt("usedNumNeibs");
                
                return createValidationResultTree(result_cluster, info);
            }  finally {
                if (algorithm != null) {
                    algorithm.removeAlgorithmListener(listener);
                }
                if (progress != null) {
                    progress.dispose();
                }
            }
        }
    }
    
    
    
    
    
    
    private DefaultMutableTreeNode createValidationResultTree(Cluster result_cluster, GeneralInfo info) {
        DefaultMutableTreeNode root;
        if(classifyGenes)
            root = new DefaultMutableTreeNode("KNNC Validation - genes");
        else
            root = new DefaultMutableTreeNode("KNNC Validation - samples");
        addValidationExpressionImages(root);
        addValidationHierarchicalTrees(root, result_cluster, info);
        addValidationCentroidViews(root);
        addValidationTableViews(root);
        addValidationInfo(root);
        addValidationGeneralInfo(root, info);
        return root;
    }
    
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    private DefaultMutableTreeNode createResultTree(Cluster result_cluster, GeneralInfo info) {
        DefaultMutableTreeNode root;
        if(classifyGenes)
            root = new DefaultMutableTreeNode("KNNC - genes");
        else
            root = new DefaultMutableTreeNode("KNNC - samples");
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
        addValidationInfo(root);
        addGeneralInfo(root, info);
    }
    
    
    private void addValidationTableViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table views");
        IViewer tabViewer;
        if (classifyGenes) {
            tabViewer = new ClusterTableViewer(this.experiment, this.clusters, this.data);
        } else {
            tabViewer = new ExperimentClusterTableViewer(this.experiment, this.clusters, this.data);
        }
        for (int i = 1; i < this.clusters.length; i++) {
            node.add(new DefaultMutableTreeNode(new LeafInfo("Class " + String.valueOf(i), tabViewer, new Integer(i))));
        }
        node.add(new DefaultMutableTreeNode(new LeafInfo("Not in training set ", tabViewer, new Integer(0))));
        root.add(node);
    }
    
    private void addTableViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table views");
        IViewer tabViewer;
        DefaultMutableTreeNode[] nodeArray = new DefaultMutableTreeNode[4];
        nodeArray[0] = new DefaultMutableTreeNode("Used classifiers");
        nodeArray[1] = new DefaultMutableTreeNode("Unused classifiers");
        nodeArray[2] = new DefaultMutableTreeNode("Classified");
        nodeArray[3] = new DefaultMutableTreeNode("Used classifiers + classified");
        //nodeArray[4] = new DefaultMutableTreeNode("Unclassified");
        if (classifyGenes) {
            tabViewer = new ClusterTableViewer(this.experiment, this.clusters, this.data);
        } else {
            tabViewer = new ExperimentClusterTableViewer(this.experiment, this.clusters, this.data);
            //return; //placeholder for ExptClusterTableViewer
            //expViewer = new KNNCExperimentClusterViewer(this.experiment, this.clusters);
        }
        
        for (int i =0; i < numClasses; i++) {
            nodeArray[0].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1), tabViewer, new Integer(i))));
        }
        for (int i = numClasses; i < 2*numClasses; i++) {
            nodeArray[1].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - numClasses), tabViewer, new Integer(i))));
        }
        for (int i =2*numClasses; i < 3*numClasses; i++) {
            nodeArray[2].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - 2*numClasses), tabViewer, new Integer(i))));
        }
        for (int i =3*numClasses; i < 4*numClasses; i++) {
            nodeArray[3].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - 3*numClasses), tabViewer, new Integer(i))));
        }
        
        //nodeArray[4].add(new DefaultMutableTreeNode(new LeafInfo("Unclassified ", expViewer, new Integer(4*numClasses))));
        
        for (int i = 0; i < nodeArray.length; i++) {
            node.add(nodeArray[i]);
        }
        
        node.add(new DefaultMutableTreeNode(new LeafInfo("Unclassified ", tabViewer, new Integer(4*numClasses))));
        
        root.add(node);
    }
    
    private void addValidationExpressionImages(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer;
        if (classifyGenes) {
            expViewer = new KNNCExperimentViewer(this.experiment, this.clusters);
        } else {
            expViewer = new KNNCExperimentClusterViewer(this.experiment, this.clusters);
        }
        
        for (int i = 1; i < this.clusters.length; i++) {
            node.add(new DefaultMutableTreeNode(new LeafInfo("Class " + String.valueOf(i), expViewer, new Integer(i))));
        }
        node.add(new DefaultMutableTreeNode(new LeafInfo("Not in training set ", expViewer, new Integer(0))));
        root.add(node);
    }
    
    /**
     * Adds nodes to display clusters data.
     */
    private void addExpressionImages(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer;
        DefaultMutableTreeNode[] nodeArray = new DefaultMutableTreeNode[4];
        nodeArray[0] = new DefaultMutableTreeNode("Used classifiers");
        nodeArray[1] = new DefaultMutableTreeNode("Unused classifiers");
        nodeArray[2] = new DefaultMutableTreeNode("Classified");
        nodeArray[3] = new DefaultMutableTreeNode("Used classifiers + classified");
        //nodeArray[4] = new DefaultMutableTreeNode("Unclassified");
        if (classifyGenes) {
            expViewer = new KNNCExperimentViewer(this.experiment, this.clusters);
        } else {
            expViewer = new KNNCExperimentClusterViewer(this.experiment, this.clusters);
        }
        
        for (int i =0; i < numClasses; i++) {
            nodeArray[0].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1), expViewer, new Integer(i))));
        }
        for (int i = numClasses; i < 2*numClasses; i++) {
            nodeArray[1].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - numClasses), expViewer, new Integer(i))));
        }
        for (int i =2*numClasses; i < 3*numClasses; i++) {
            nodeArray[2].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - 2*numClasses), expViewer, new Integer(i))));
        }
        for (int i =3*numClasses; i < 4*numClasses; i++) {
            nodeArray[3].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - 3*numClasses), expViewer, new Integer(i))));
        }
        
        //nodeArray[4].add(new DefaultMutableTreeNode(new LeafInfo("Unclassified ", expViewer, new Integer(4*numClasses))));
        
        for (int i = 0; i < nodeArray.length; i++) {
            node.add(nodeArray[i]);
        }
        
        node.add(new DefaultMutableTreeNode(new LeafInfo("Unclassified ", expViewer, new Integer(4*numClasses))));
        
        root.add(node);
    }
    
    
    private void addValidationHierarchicalTrees(DefaultMutableTreeNode root, Cluster result_cluster, GeneralInfo info) {
        if (!info.hcl) {
            return;
        }
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Hierarchical Trees");
        NodeList nodeList = result_cluster.getNodeList();
        int [][] clusters = null;
        
        if(!this.classifyGenes){
            clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            if(info.hcl_samples)
                clusters = getOrderedIndices(nodeList, clusters, info.hcl_genes);
        }
        for (int i=1; i<nodeList.getSize(); i++) {
            if(this.classifyGenes)
                node.add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i), createHCLViewer(nodeList.getNode(i), info, null))));
            else
                node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i), createHCLViewer(nodeList.getNode(i), info, clusters), new Integer(i))));
        }
        if (this.classifyGenes) {
            node.add(new DefaultMutableTreeNode(new LeafInfo("Not in training set", createHCLViewer(nodeList.getNode(0), info, null))));
        } else
            node.add(new DefaultMutableTreeNode(new LeafInfo("Not in training set", createHCLViewer(nodeList.getNode(0), info, clusters), new Integer(0))));
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
        
        DefaultMutableTreeNode[] nodeArray = new DefaultMutableTreeNode[4];
        nodeArray[0] = new DefaultMutableTreeNode("Used classifiers");
        nodeArray[1] = new DefaultMutableTreeNode("Unused classifiers");
        nodeArray[2] = new DefaultMutableTreeNode("Classified");
        nodeArray[3] = new DefaultMutableTreeNode("Used classifiers + classified");
        //nodeArray[4] = new DefaultMutableTreeNode("Unclassified");
        
        if(!this.classifyGenes){
            clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            if(info.hcl_samples)
                clusters = getOrderedIndices(nodeList, clusters, info.hcl_genes);
        }
        //for (int i=0; i<nodeList.getSize(); i++) {
        if(this.classifyGenes) {
            for (int i =0; i < numClasses; i++) {
                nodeArray[0].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1), createHCLViewer(nodeList.getNode(i), info, null))));
            }
            for (int i = numClasses; i < 2*numClasses; i++) {
                nodeArray[1].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - numClasses), createHCLViewer(nodeList.getNode(i), info, null))));
            }
            for (int i =2*numClasses; i < 3*numClasses; i++) {
                nodeArray[2].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - 2*numClasses), createHCLViewer(nodeList.getNode(i), info, null))));
            }
            for (int i =3*numClasses; i < 4*numClasses; i++) {
                nodeArray[3].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - 3*numClasses), createHCLViewer(nodeList.getNode(i), info, null))));
            }
            for (int i = 0; i < nodeArray.length; i++) {
                node.add(nodeArray[i]);
            }
            //nodeArray[4].add(new DefaultMutableTreeNode(new LeafInfo("Unclassified ", createHCLViewer(nodeList.getNode(4*numClasses), info, null))));
            node.add(new DefaultMutableTreeNode(new LeafInfo("Unclassified ", createHCLViewer(nodeList.getNode(4*numClasses), info, null))));
            //node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), createHCLViewer(nodeList.getNode(i), info, null))));
        }
        else {
            for (int i =0; i < numClasses; i++) {
                nodeArray[0].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1), createHCLViewer(nodeList.getNode(i), info, clusters), new Integer(i))));
            }
            for (int i = numClasses; i < 2*numClasses; i++) {
                nodeArray[1].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - numClasses), createHCLViewer(nodeList.getNode(i), info, clusters), new Integer(i))));
            }
            for (int i =2*numClasses; i < 3*numClasses; i++) {
                nodeArray[2].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - 2*numClasses), createHCLViewer(nodeList.getNode(i), info, clusters), new Integer(i))));
            }
            for (int i =3*numClasses; i < 4*numClasses; i++) {
                nodeArray[3].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - 3*numClasses), createHCLViewer(nodeList.getNode(i), info, clusters), new Integer(i))));
            }
            for (int i = 0; i < nodeArray.length; i++) {
                node.add(nodeArray[i]);
            }
            
            node.add(new DefaultMutableTreeNode(new LeafInfo("Unclassified ", createHCLViewer(nodeList.getNode(4*numClasses), info, clusters), new Integer(4*numClasses))));
            //nodeArray[4].add(new DefaultMutableTreeNode(new LeafInfo("Unclassified ", createHCLViewer(nodeList.getNode(4*numClasses), info, clusters), new Integer(4*numClasses))));
            
            
            //node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), createHCLViewer(nodeList.getNode(i), info, clusters), new Integer(i))));
        }
        /*
        for (int i = 0; i < nodeArray.length; i++) {
            node.add(nodeArray[i]);
        }
         */
        //}
        root.add(node);
    }
    
    /**
     * Creates an <code>HCLViewer</code>.
     */
    private IViewer createHCLViewer(Node clusterNode, GeneralInfo info, int [][] sampleClusters) {
        HCLTreeData genes_result = info.hcl_genes ? getResult(clusterNode, 0) : null;
        HCLTreeData samples_result = info.hcl_samples ? getResult(clusterNode, info.hcl_genes ? 4 : 0) : null;
        if(this.classifyGenes)
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
        if(this.classifyGenes)
            node.add(new DefaultMutableTreeNode(new LeafInfo("Genes in Classes (#,%)", new KNNCInfoViewer(this.clusters, this.experiment.getNumberOfGenes(), numClasses))));
        else
            node.add(new DefaultMutableTreeNode(new LeafInfo("Samples in Classes (#,%)", new KNNCInfoViewer(this.clusters, this.experiment.getNumberOfSamples(), false, numClasses))));
        root.add(node);
    }
    
    private void addValidationInfo(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new LeafInfo("Validation Information", new KNNCValidationInfoViewer(origNumInFiltTrgSetByClass, numberCorrectlyClassifiedByClass, numberIncorrectlyClassifiedByClass)));
        root.add(node);
    }
    
    
    private void addValidationCentroidViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        
        KNNCCentroidViewer centroidViewer;
        ExperimentClusterCentroidViewer expCentroidViewer;
        
        
        int[][] shuffledClusters = new int[clusters.length][];
        for (int i = 0; i < clusters.length - 1; i++) {
            shuffledClusters[i] = clusters[i + 1];
        }
        shuffledClusters[clusters.length - 1] = clusters[0];
        
        FloatMatrix shuffledMeans = new FloatMatrix(means.getRowDimension(), means.getColumnDimension());
        FloatMatrix shuffledVariances = new FloatMatrix(variances.getRowDimension(), variances.getColumnDimension());
        
        for (int i = 0; i  < clusters.length - 1; i++) {
            shuffledMeans.A[i] = means.A[i + 1];
            shuffledVariances.A[i] = variances.A[i + 1];
        }
        
        shuffledMeans.A[clusters.length - 1] = means.A[0];
        shuffledVariances.A[clusters.length - 1] = variances.A[0];
        
        if(classifyGenes){
            centroidViewer = new KNNCCentroidViewer(this.experiment, clusters);
            centroidViewer.setMeans(this.means.A);
            centroidViewer.setVariances(this.variances.A);
            for (int i=1; i<this.clusters.length; i++) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Not in training set ", centroidViewer, new CentroidUserObject(0, CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Not in training set ", centroidViewer, new CentroidUserObject(0, CentroidUserObject.VALUES_MODE))));
            
            
            KNNCCentroidsViewer centroidsViewer = new KNNCCentroidsViewer(this.experiment, shuffledClusters);
            centroidsViewer.setMeans(shuffledMeans.A);
            centroidsViewer.setVariances(shuffledVariances.A);
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
            
        }
        else{
            expCentroidViewer = new KNNCExperimentCentroidViewer(this.experiment, clusters);
            
            expCentroidViewer.setMeans(this.means.A);
            expCentroidViewer.setVariances(this.variances.A);
            for (int i=1; i<this.clusters.length; i++) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Not in training set ", expCentroidViewer, new CentroidUserObject(0, CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Not in training set ", expCentroidViewer, new CentroidUserObject(0, CentroidUserObject.VALUES_MODE))));
            
            KNNCExperimentCentroidsViewer expCentroidsViewer = new KNNCExperimentCentroidsViewer(this.experiment, shuffledClusters);
            expCentroidsViewer.setMeans(shuffledMeans.A);
            expCentroidsViewer.setVariances(shuffledVariances.A);
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", expCentroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", expCentroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
            
            
        }
        root.add(centroidNode);
        root.add(expressionNode);
    }
    
    /**
     * Adds nodes to display centroid charts.
     */
    private void addCentroidViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        
        KNNCCentroidViewer centroidViewer;
        ExperimentClusterCentroidViewer expCentroidViewer;
        
        DefaultMutableTreeNode[] centroidNodeArray = new DefaultMutableTreeNode[4];
        DefaultMutableTreeNode[] expressionNodeArray = new DefaultMutableTreeNode[4];
        
        centroidNodeArray[0] = new DefaultMutableTreeNode("Used classifiers");
        centroidNodeArray[1] = new DefaultMutableTreeNode("Unused classifiers");
        centroidNodeArray[2] = new DefaultMutableTreeNode("Classified");
        centroidNodeArray[3] = new DefaultMutableTreeNode("Used classifiers + classified");
        //centroidNodeArray[4] = new DefaultMutableTreeNode("Unclassified");
        //centroidNodeArray[5] = new DefaultMutableTreeNode("All");
        
        expressionNodeArray[0] = new DefaultMutableTreeNode("Used classifiers");
        expressionNodeArray[1] = new DefaultMutableTreeNode("Unused classifiers");
        expressionNodeArray[2] = new DefaultMutableTreeNode("Classified");
        expressionNodeArray[3] = new DefaultMutableTreeNode("Used classifiers + classified");
        //expressionNodeArray[4] = new DefaultMutableTreeNode("Unclassified");
        //expressionNodeArray[5] = new DefaultMutableTreeNode("All");
        
        if(classifyGenes){
            centroidViewer = new KNNCCentroidViewer(this.experiment, clusters);
            centroidViewer.setMeans(this.means.A);
            centroidViewer.setVariances(this.variances.A);
            
            for (int i =0; i < numClasses; i++) {
                centroidNodeArray[0].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNodeArray[0].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            for (int i = numClasses; i < 2*numClasses; i++) {
                centroidNodeArray[1].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - numClasses), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNodeArray[1].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - numClasses), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            for (int i =2*numClasses; i < 3*numClasses; i++) {
                centroidNodeArray[2].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - 2*numClasses), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNodeArray[2].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 -2*numClasses), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            for (int i =3*numClasses; i < 4*numClasses; i++) {
                centroidNodeArray[3].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - 3*numClasses), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNodeArray[3].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - 3*numClasses), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            
            //centroidNodeArray[4].add(new DefaultMutableTreeNode(new LeafInfo("Unclassified ", centroidViewer, new CentroidUserObject(4*numClasses, CentroidUserObject.VARIANCES_MODE))));
            //expressionNodeArray[4].add(new DefaultMutableTreeNode(new LeafInfo("Unclassified ", centroidViewer, new CentroidUserObject(4*numClasses, CentroidUserObject.VALUES_MODE))));
             /*
            for (int i=0; i<this.clusters.length; i++) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
              */
            
            KNNCCentroidsViewer centroidsViewer = new KNNCCentroidsViewer(this.experiment, clusters);
            centroidsViewer.setMeans(this.means.A);
            centroidsViewer.setVariances(this.variances.A);
            
            for (int i = 0; i < centroidNodeArray.length; i++) {
                centroidNode.add(centroidNodeArray[i]);
                expressionNode.add(expressionNodeArray[i]);
            }
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Unclassified ", centroidViewer, new CentroidUserObject(4*numClasses, CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Unclassified ", centroidViewer, new CentroidUserObject(4*numClasses, CentroidUserObject.VALUES_MODE))));
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All ", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All ", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
            
        }
        else{
            expCentroidViewer = new KNNCExperimentCentroidViewer(this.experiment, clusters);
            
            expCentroidViewer.setMeans(this.means.A);
            expCentroidViewer.setVariances(this.variances.A);
            for (int i =0; i < numClasses; i++) {
                centroidNodeArray[0].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNodeArray[0].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            for (int i = numClasses; i < 2*numClasses; i++) {
                centroidNodeArray[1].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - numClasses), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNodeArray[1].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - numClasses), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            for (int i =2*numClasses; i < 3*numClasses; i++) {
                centroidNodeArray[2].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - 2*numClasses), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNodeArray[2].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 -2*numClasses), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            for (int i =3*numClasses; i < 4*numClasses; i++) {
                centroidNodeArray[3].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - 3*numClasses), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNodeArray[3].add(new DefaultMutableTreeNode(new LeafInfo("Class "+String.valueOf(i+1 - 3*numClasses), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            
            //centroidNodeArray[4].add(new DefaultMutableTreeNode(new LeafInfo("Unclassified ", expCentroidViewer, new CentroidUserObject(4*numClasses, CentroidUserObject.VARIANCES_MODE))));
            //expressionNodeArray[4].add(new DefaultMutableTreeNode(new LeafInfo("Unclassified ", expCentroidViewer, new CentroidUserObject(4*numClasses, CentroidUserObject.VALUES_MODE))));
            
            /*
            for (int i=0; i<this.clusters.length; i++) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
             */
            KNNCExperimentCentroidsViewer expCentroidsViewer = new KNNCExperimentCentroidsViewer(this.experiment, clusters);
            expCentroidsViewer.setMeans(this.means.A);
            expCentroidsViewer.setVariances(this.variances.A);
            
            for (int i = 0; i < centroidNodeArray.length; i++) {
                centroidNode.add(centroidNodeArray[i]);
                expressionNode.add(expressionNodeArray[i]);
            }
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Unclassified ", expCentroidViewer, new CentroidUserObject(4*numClasses, CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Unclassified ", expCentroidViewer, new CentroidUserObject(4*numClasses, CentroidUserObject.VALUES_MODE))));
            
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All ", expCentroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All ", expCentroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
            
            
        }
        /*
        for (int i = 0; i < centroidNodeArray.length; i++) {
            centroidNode.add(centroidNodeArray[i]);
            expressionNode.add(expressionNodeArray[i]);
        }
         */
        root.add(centroidNode);
        root.add(expressionNode);
    }
    
    /**
     * Adds node with general iformation.
     */
    private void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        node.add(new DefaultMutableTreeNode("Original total data set size: " + info.origDataSetSize));
        node.add(new DefaultMutableTreeNode("Original training set size: " + info.origClassSetSize));
        
        node.add(new DefaultMutableTreeNode("Used variance filter: " + info.usedVarFilter));
        if (useVarianceFilter) {
            node.add(new DefaultMutableTreeNode("Total data set size after var. filtering: " + info.numVarFiltered));
            node.add(new DefaultMutableTreeNode("Var. filtered training set size: " + info.postVarClassSetSize));
            node.add(new DefaultMutableTreeNode("Size of set to classify after var. filtering: " + info.postVarDataSetSize));
        }
        node.add(new DefaultMutableTreeNode("Used correlation filter: " + info.usedCorrelFilter));
        if (useCorrelFilter) {
            node.add(new DefaultMutableTreeNode("Threshold p-value: " + info.correlPvalue));
            node.add(new DefaultMutableTreeNode("Number of permutations: " + info.numPerms));
            node.add(new DefaultMutableTreeNode("Size of set to classify after corr. filtering: " + info.postCorrDataSetSize));
        }
        node.add(new DefaultMutableTreeNode("Num. classes: " + info.numClasses));
        node.add(new DefaultMutableTreeNode("Input num. neighbors: " + info.numNeighbors));
        node.add(new DefaultMutableTreeNode("Num. neighbors used: " + info.usedNumNeibs));
        node.add(new DefaultMutableTreeNode("HCL: "+info.getMethodName()));
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time)+" ms"));
        //node.add(new DefaultMutableTreeNode(info.function));
        root.add(node);
    }
    
    private void addValidationGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        node.add(new DefaultMutableTreeNode("Used correlation filter: " + info.usedCorrelFilter));
        if (useCorrelFilter) {
            node.add(new DefaultMutableTreeNode("Threshold p-value: " + info.correlPvalue));
            node.add(new DefaultMutableTreeNode("Number of permutations: " + info.numPerms));
            //node.add(new DefaultMutableTreeNode("Size of set to classify after corr. filtering: " + info.postCorrDataSetSize));
        }
        node.add(new DefaultMutableTreeNode("Num. classes: " + info.numClasses));
        node.add(new DefaultMutableTreeNode("Input num. neighbors: " + info.numNeighbors));
        node.add(new DefaultMutableTreeNode("Num. neighbors used: " + info.usedNumNeibs));
        node.add(new DefaultMutableTreeNode("HCL: "+info.getMethodName()));
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time)+" ms"));
        //node.add(new DefaultMutableTreeNode(info.function));
        root.add(node);
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
    
    // the general info structure
    private class GeneralInfo {
        
        public long time;
        public String function;
        
        private boolean hcl;
        private int hcl_method;
        private boolean hcl_genes;
        private boolean hcl_samples;
        
        private int numClasses, numNeighbors, numVarFiltered, numPerms, usedNumNeibs, postVarClassSetSize, postVarDataSetSize, postCorrDataSetSize;
        private int origDataSetSize, origClassSetSize;
        private boolean usedVarFilter, usedCorrelFilter;
        private double correlPvalue;
        
        public String getMethodName() {
            return hcl ? HCLGUI.GeneralInfo.getMethodName(hcl_method) : "no linkage";
        }
        
    }
    
}




















