/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: EASEGUI.java,v $
 * $Revision: 1.3 $
 * $Date: 2004-05-26 13:13:39 $
 * $Author: braisted $
 * $State: Exp $
 */
/*
 * EASEGUI.java
 *
 * Created on August 22, 2003, 1:28 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.ease;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.clusterUtil.*;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Logger;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;


import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;


/** The <CODE>EASEGUI</CODE> class contains code to gather parameters
 * for EASE annotation analysis, to run the analysis, and to display
 * various results from the analysis.
 */
public class EASEGUI implements IClusterGUI, IScriptGUI {
    
    /** The algorithm class for execution of EASE.
     */
    private Algorithm algorithm;
    /** The <CODE>AlgorithmData<\CODE> object to encapsulate parameters, input data, and results
     */
    private AlgorithmData algorithmData;
    /** The <CODE>Experiment</CODE> data wrapper class.
     */
    private Experiment experiment;
    /** The input <CODE>Cluster</CODE> object for cluster analysis
     */
    private Cluster cluster;
    /** Encapsulates the indices of clusters created by the analysis.
     */
    private int [][] clusters;
    /** Names of the theme categories found in the gene list.
     */
    private String [] categoryNames;
    /** The main result matrix for table display.
     */
    private String [][] resultMatrix;
    /** Indicates if accession numbers were appended.
     */
    private boolean haveAccessionNumbers;
    /** Indicates if the mode is cluster analysis (or if not then a survey)
     */
    private boolean isClusterAnalysis;
    /** Verbose progress dialog
     */
    private Logger logger;
    /** Optional progress bar.
     */
    private Progress progress;
    /** Algorithm event listener.
     */
    private Listener listener;
    
    boolean stop = false;
    String annotationKeyType;
    
    /** Indicates if the algorithm run is via a script execution
     */    
    boolean isScripting = false;
    
    /** Creates a new instance of EASEGUI */
    public EASEGUI() {
    }
    
    /** Accumulates parameters for execution of the EASE analysis
     * calls algorithm class and coordinates result viewer
     * creation.
     * @param framework The framework object
     * @throws AlgorithmException
     * @return
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        
        algorithmData = new AlgorithmData();
        
        ClusterRepository repository = framework.getClusterRepository(Cluster.GENE_CLUSTER);
        
        EASEInitDialog dialog = new EASEInitDialog(framework.getFrame(), repository, framework.getData().getFieldNames());
        
        if(dialog.showModal() != JOptionPane.OK_OPTION)
            return null;
        
        listener = new Listener();
        logger = new Logger(framework.getFrame(), "EASE Analysis", listener);
        logger.show();
        progress = new Progress(framework.getFrame(), "Probability Analysis Resampling Progress", listener);
        
        isClusterAnalysis = dialog.isClusterModeSelected();
        String converterFileName = dialog.getConverterFileName();
        annotationKeyType = dialog.getAnnotationKeyType();
        String [] annotationFileList = dialog.getAnnToGOFileList();
        int minClusterSize = dialog.getMinClusterSize();
        int [] indices;
        boolean isPvalueCorrectionSelected;
        experiment = framework.getData().getExperiment();
        
        if(isClusterAnalysis){
            cluster = dialog.getSelectedCluster();
            experiment = cluster.getExperiment();   //asign proper experiment object
            indices = cluster.getIndices();  //**These map to IDATA**
            algorithmData.addParam("report-ease-score", String.valueOf(dialog.isEaseScoreSelected()));
            isPvalueCorrectionSelected = dialog.isCorrectPvaluesSelected();
            algorithmData.addParam("p-value-corrections", String.valueOf(isPvalueCorrectionSelected));
            if(isPvalueCorrectionSelected){
                algorithmData.addParam("bonferroni-correction", String.valueOf(dialog.isBonferroniSelected()));
                algorithmData.addParam("bonferroni-step-down-correction", String.valueOf(dialog.isStepDownBonferroniSelected()));
                algorithmData.addParam("sidak-correction", String.valueOf(dialog.isSidakSelected()));
            }
            
            algorithmData.addParam("run-permutation-analysis", String.valueOf(dialog.isPermutationAnalysisSelected()));
            if(dialog.isPermutationAnalysisSelected())
                algorithmData.addParam("permutation-count", String.valueOf(dialog.getPermutationCount()));
            
            logger.append("Extracting Annotation Key Lists\n");
            String [] clusterKeys = framework.getData().getAnnotationList(annotationKeyType, indices);
            if(clusterKeys == null)
                System.out.println("NULL CLUSTER KEYS!!!!!!!!!!!!!!!!!!!!!!!!!");
            algorithmData.addStringArray("sample-list", clusterKeys);
            algorithmData.addIntArray("sample-indices", cluster.getExperimentIndices());  //drop in experiment indices
        }
        
        //Use file or IData for population, only permit file use for cluster analysis
        String [] populationKeys;
        if(isClusterAnalysis && dialog.isPopFileModeSelected()) {
            try {
                populationKeys = getPopulationKeysFromFile(dialog.getPopulationFileName());
                algorithmData.addParam("population-file-name", dialog.getPopulationFileName());
                if(populationKeys == null) {
                    return null;
                }
            } catch (IOException ioe) {
                //Bad file format
                JOptionPane.showMessageDialog(framework.getFrame(), "Error loading population file.", "Population File Load Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } else {
            populationKeys = framework.getData().getAnnotationList(annotationKeyType, experiment.getRowMappingArrayCopy());
        }
        
        algorithmData.addParam("perform-cluster-analysis", String.valueOf(isClusterAnalysis));
        algorithmData.addStringArray("population-list", populationKeys);
        if(converterFileName != null)
            algorithmData.addParam("converter-file-name", converterFileName);
        algorithmData.addStringArray("annotation-file-list", annotationFileList);
        algorithmData.addMatrix("expression", experiment.getMatrix());
        
        //Trim options
        String [] trimOptions = dialog.getTrimOptions();
        algorithmData.addParam("trim-option", trimOptions[0]);
        algorithmData.addParam("trim-value", trimOptions[1]);
        
        algorithm = framework.getAlgorithmFactory().getAlgorithm("EASE");
        algorithm.addAlgorithmListener(listener);
        algorithm.execute(algorithmData);
        
        if(stop)
            return null;
        
        progress.dispose();
        categoryNames = algorithmData.getStringArray("category-names");
        
        clusters = algorithmData.getIntMatrix("cluster-matrix");
        resultMatrix = (String [][])algorithmData.getObjectMatrix("result-matrix");
        haveAccessionNumbers = algorithmData.getParams().getBoolean("have-accession-numbers", false);
        
        DefaultMutableTreeNode node;
        logger.append("Creating Result Viewers\n");
        
        if(resultMatrix == null)
            node = createEmptyResultNode(algorithmData);
        else
            node = createResultNode(algorithmData, clusters);
        
        if (algorithm != null) {
            algorithm.removeAlgorithmListener(listener);
        }
        if (logger != null) logger.dispose();
        
        return node;
    }
    
    
    
    public AlgorithmData getScriptParameters(IFramework framework) {
        algorithmData = new AlgorithmData();
        
        //  ClusterRepository repository = framework.getClusterRepository(Cluster.GENE_CLUSTER);
        
        EASEInitDialog dialog = new EASEInitDialog(framework.getFrame(), framework.getData().getFieldNames());
        
        if(dialog.showModal() != JOptionPane.OK_OPTION)
            return null;
        
        isClusterAnalysis = dialog.isClusterModeSelected();
        String converterFileName = dialog.getConverterFileName();
        annotationKeyType = dialog.getAnnotationKeyType();
        algorithmData.addParam("annotation-key-type", annotationKeyType);
        String [] annotationFileList = dialog.getAnnToGOFileList();
        int minClusterSize = dialog.getMinClusterSize();
        int [] indices;
        boolean isPvalueCorrectionSelected;
        experiment = framework.getData().getExperiment();
        
        if(isClusterAnalysis){
            //cluster = dialog.getSelectedCluster();
            //experiment = cluster.getExperiment();   //asign proper experiment object
            //indices = cluster.getIndices();  //**These map to IDATA**
            algorithmData.addParam("report-ease-score", String.valueOf(dialog.isEaseScoreSelected()));
            isPvalueCorrectionSelected = dialog.isCorrectPvaluesSelected();
            algorithmData.addParam("p-value-corrections", String.valueOf(isPvalueCorrectionSelected));
            if(isPvalueCorrectionSelected){
                algorithmData.addParam("bonferroni-correction", String.valueOf(dialog.isBonferroniSelected()));
                algorithmData.addParam("bonferroni-step-down-correction", String.valueOf(dialog.isStepDownBonferroniSelected()));
                algorithmData.addParam("sidak-correction", String.valueOf(dialog.isSidakSelected()));
            }
            
            algorithmData.addParam("run-permutation-analysis", String.valueOf(dialog.isPermutationAnalysisSelected()));
            if(dialog.isPermutationAnalysisSelected())
                algorithmData.addParam("permutation-count", String.valueOf(dialog.getPermutationCount()));
            
            //  logger.append("Extracting Annotation Key Lists\n");
            //  String [] clusterKeys = framework.getData().getAnnotationList(annotationKeyType, indices);
            //   if(clusterKeys == null)
            //      System.out.println("NULL CLUSTER KEYS!!!!!!!!!!!!!!!!!!!!!!!!!");
            //  algorithmData.addStringArray("sample-list", clusterKeys);
            // algorithmData.addIntArray("sample-indices", cluster.getExperimentIndices());  //drop in experiment indices
        }
        
        //Use file or IData for population, only permit file use for cluster analysis
        String [] populationKeys;
        if(isClusterAnalysis && dialog.isPopFileModeSelected()) {
            // try {
            // populationKeys = getPopulationKeysFromFile(dialog.getPopulationFileName());
            algorithmData.addParam("population-file-name", dialog.getPopulationFileName());
            //     if(populationKeys == null) {
            //        return null;
            //   }
            //  } catch (IOException ioe) {
            //Bad file format
            //       JOptionPane.showMessageDialog(framework.getFrame(), "Error loading population file.", "Population File Load Error", JOptionPane.ERROR_MESSAGE);
            //      return null;
            //  }
        } //else {
        //   populationKeys = framework.getData().getAnnotationList(annotationKeyType, experiment.getRowMappingArrayCopy());
        // }
        
        algorithmData.addParam("perform-cluster-analysis", String.valueOf(isClusterAnalysis));
        // algorithmData.addStringArray("population-list", populationKeys);
        if(converterFileName != null)
            algorithmData.addParam("converter-file-name", converterFileName);
        algorithmData.addStringArray("annotation-file-list", annotationFileList);
        //  algorithmData.addMatrix("expression", experiment.getMatrix());
        
        //Trim options
        String [] trimOptions = dialog.getTrimOptions();
        algorithmData.addParam("trim-option", trimOptions[0]);
        algorithmData.addParam("trim-value", trimOptions[1]);
        
        //script control parameters
        
        // alg name
        algorithmData.addParam("name", "EASE");
        
        // alg type
        algorithmData.addParam("alg-type", "cluster-genes");
        
        // output class
        algorithmData.addParam("output-class", "multi-cluster-output");
        
        //output nodes
        String [] outputNodes = new String[1];
        outputNodes[0] = "Multi-cluster";
        algorithmData.addStringArray("output-nodes", outputNodes);
        return algorithmData;
    }
    
    
    
    
    public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, Experiment experiment) throws AlgorithmException {
        this.isScripting = true;
        this.algorithmData = algData;
        this.experiment = experiment;
        algData.addMatrix("expression", framework.getData().getExperiment().getMatrix());
        
        AlgorithmParameters params = algData.getParams();
        
        this.isClusterAnalysis = params.getBoolean("perform-cluster-analysis");
        this.annotationKeyType = params.getString("annotation-key-type");
        
        
        
        listener = new Listener();
        logger = new Logger(framework.getFrame(), "EASE Analysis", listener);
        logger.show();
        progress = new Progress(framework.getFrame(), "Probability Analysis Resampling Progress", listener);
        
        if(this.isClusterAnalysis) {
            //cluster keys
            int indices [] = experiment.getRowMappingArrayCopy();
            String [] clusterKeys = framework.getData().getAnnotationList(annotationKeyType, indices);
            //   if(clusterKeys == null)
            //      System.out.println("NULL CLUSTER KEYS!!!!!!!!!!!!!!!!!!!!!!!!!");
              algData.addStringArray("sample-list", clusterKeys);
             algData.addIntArray("sample-indices", indices);  //drop in experiment indices
            
        }
        
        
        
        
        // population keys
        String popFileName = params.getString("population-file-name");
        String [] populationKeys;
        if(isClusterAnalysis && popFileName != null){// && dialog.isPopFileModeSelected()) {
            try {
                populationKeys = getPopulationKeysFromFile(params.getString("population-file-name"));
                if(populationKeys == null) {
                    return null;
                }
            } catch (IOException ioe) {
                //Bad file format
                JOptionPane.showMessageDialog(framework.getFrame(), "Error loading population file.", "Population File Load Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } else {
            populationKeys = framework.getData().getAnnotationList(annotationKeyType, experiment.getRowMappingArrayCopy());
        }
                algData.addStringArray("population-list", populationKeys);
        
        
        
        
        
        
        algorithm = framework.getAlgorithmFactory().getAlgorithm("EASE");
        algorithm.addAlgorithmListener(listener);
        algorithm.execute(algorithmData);
        
        if(stop)
            return null;
        
        progress.dispose();
        categoryNames = algorithmData.getStringArray("category-names");
        
        clusters = algorithmData.getIntMatrix("cluster-matrix");
        resultMatrix = (String [][])algorithmData.getObjectMatrix("result-matrix");
        haveAccessionNumbers = algorithmData.getParams().getBoolean("have-accession-numbers", false);
        
        DefaultMutableTreeNode node;
        logger.append("Creating Result Viewers\n");
        
        if(resultMatrix == null)
            node = createEmptyResultNode(algorithmData);
        else
            node = createResultNode(algorithmData, clusters);
        
        if (algorithm != null) {
            algorithm.removeAlgorithmListener(listener);
        }
        if (logger != null) logger.dispose();
        
        return node;
    }
    
    
    private String [] getPopulationKeysFromFile(String fileName) throws IOException {
        File file = new File(fileName);
        if(file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            Vector ann = new Vector();
            String key;
            while( (key = reader.readLine()) != null ) {
                ann.add(key);
            }
            String [] annot = new String [ann.size()];
            for(int i = 0; i < annot.length; i++) {
                annot[i] = (String)(ann.elementAt(i));
            }
            return annot;
        }
        return null;
    }
    
    
    /** Creates the result node.
     * @param result result matrix
     * @param clusters cluster indices
     * @return returns the result node
     */
    private DefaultMutableTreeNode createResultNode(AlgorithmData result, int [][] clusters){
        DefaultMutableTreeNode root;
        if(this.isClusterAnalysis)
            root = new DefaultMutableTreeNode("EASE Analysis");
        else
            root = new DefaultMutableTreeNode("EASE Survey");
        addTableViewer(root, result);
        addExpressionViewers(root, result);
        addGeneralInfo(root, result);
        return root;
    }
    
    /** creates an empty result if the result is null.
     * @param result
     * @return  */
    private DefaultMutableTreeNode createEmptyResultNode(AlgorithmData result){
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("EASE");
        root.add(new DefaultMutableTreeNode("No Annotation Hits"));
        addGeneralInfo(root, result);
        return root;
    }
    
    /** Adds nodes to display cluster data.
     * @param root root node
     * @param result result matrix
     */
    private void addExpressionViewers(DefaultMutableTreeNode root, AlgorithmData result) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Viewers");
        
        IViewer expViewer = new EASEExperimentViewer(this.experiment, this.clusters);
        EASECentroidViewer graphViewer = new EASECentroidViewer(this.experiment, this.clusters);
        //set means and variances in the graph viewer
        graphViewer.setMeans(result.getMatrix("means").A);
        graphViewer.setVariances(result.getMatrix("variances").A);
        DefaultMutableTreeNode clusterNode, annotNode, popNode;
        for (int i=0; i<this.clusters.length; i++) {
            clusterNode = new DefaultMutableTreeNode("Term "+String.valueOf(i+1));
            clusterNode.add(new DefaultMutableTreeNode(new LeafInfo("Expression Image", expViewer, new Integer(i))));
            clusterNode.add(new DefaultMutableTreeNode(new LeafInfo("Centroid Graph", graphViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
            clusterNode.add(new DefaultMutableTreeNode(new LeafInfo("Expression Graph", graphViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            clusterNode.add(new DefaultMutableTreeNode(resultMatrix[i][1]));
            if(this.haveAccessionNumbers)
                clusterNode.add(new DefaultMutableTreeNode(resultMatrix[i][2]));
            clusterNode.add(new DefaultMutableTreeNode("Number of Genes: "+this.clusters[i].length));
            
            node.add(clusterNode);
        }
        root.add(node);
    }
    
    
    /** Adds the table viewer
     * @param root root node
     * @param result
     */
    private void addTableViewer(DefaultMutableTreeNode root, AlgorithmData result){
        Object [][] data = result.getObjectMatrix("result-matrix");
        String [] headerNames = result.getStringArray("header-names");
        
        if(data == null || data.length < 1)
            return;
        
        EASETableViewer tv = new EASETableViewer(headerNames, data, root, experiment, clusters, haveAccessionNumbers, this.isClusterAnalysis);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new LeafInfo("Table Viewer", tv));
        root.add(node);
    }
    
    /** Returns the indices mapped to IData indices.  Appropriate indices for
     * direct access to IData encapsulated annotation.
     * @param indices
     * @return
     */
    private int [] getDataIndices(int [] indices){
        int [] dataIndices = new int[indices.length];
        for(int i = 0; i < dataIndices.length; i++){
            dataIndices[i] = this.experiment.getGeneIndexMappedToData(indices[i]);
        }
        return dataIndices;
    }
    
    /** Adds general algorithm information.
     * @param root root node
     * @param result
     */
    private void addGeneralInfo(DefaultMutableTreeNode root, AlgorithmData result){
        DefaultMutableTreeNode generalInfo = new DefaultMutableTreeNode("General Information");
        String converterFileName = result.getParams().getString("converter-file-name");
        DefaultMutableTreeNode newNode;
        
        if(this.isClusterAnalysis && !isScripting){
            newNode = new DefaultMutableTreeNode("Input Cluster Info");
            newNode.add(new DefaultMutableTreeNode("Cluster Serial # :"+String.valueOf(this.cluster.getSerialNumber())));
            newNode.add(new DefaultMutableTreeNode("Cluster Source: "+String.valueOf(this.cluster.getSource())));
            newNode.add(new DefaultMutableTreeNode("Cluster Analysis Node: "+String.valueOf(this.cluster.getAlgorithmName())));
            newNode.add(new DefaultMutableTreeNode("Cluster Cluster Node: "+String.valueOf(this.cluster.getClusterID())));
            newNode.add(new DefaultMutableTreeNode("Cluster Label: "+String.valueOf(this.cluster.getClusterLabel())));
            newNode.add(new DefaultMutableTreeNode("Cluster Size: "+String.valueOf(this.cluster.getSize())));
            generalInfo.add(newNode);
        }
        
        if(this.isScripting) {
            newNode = new DefaultMutableTreeNode("Input Data: Script Data Input");            
        }
        
        newNode = new DefaultMutableTreeNode("Analysis Options");
        DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode("Selected Index and Files");
        
        String popFileName = result.getParams().getString("population-file-name");
        if(popFileName == null)
            fileNode.add(new DefaultMutableTreeNode("Population Selection: Data in Current Viewer"));
        else
            fileNode.add(new DefaultMutableTreeNode("Population Selection: File Input ("+popFileName+")"));
        
        fileNode.add(new DefaultMutableTreeNode("MeV Index: "+this.annotationKeyType));
        AlgorithmParameters params = this.algorithmData.getParams();
        fileNode.add(new DefaultMutableTreeNode("Conversion File: "+params.getString("converter-file-name", "Not Selected")));
        DefaultMutableTreeNode annFileNode = new DefaultMutableTreeNode("Annotation-to-Theme Files");
        String [] annFiles = this.algorithmData.getStringArray("annotation-file-list");
        if(annFiles != null)
            for(int i = 0; i < annFiles.length; i++)
                annFileNode.add(new DefaultMutableTreeNode("File: "+annFiles[i]));
        fileNode.add(annFileNode);
        newNode.add(fileNode);
        
        if(this.isClusterAnalysis || !(params.getString("trim-option").equals("NO_TRIM"))){
            DefaultMutableTreeNode statNode = new DefaultMutableTreeNode("Stat Parameters");
            if(this.isClusterAnalysis){
                statNode.add(new DefaultMutableTreeNode("Reported Statistic:"+  ((params.getBoolean("report-ease-score"))?"EASE Score":"Fisher's Exact")));
                if(params.getBoolean("bonferroni-correction", false))
                    statNode.add(new DefaultMutableTreeNode("Mult.-Correct.: Bonferroni"));
                if(params.getBoolean("bonferroni-step-down-correction", false))
                    statNode.add(new DefaultMutableTreeNode("Mult.-Correct.: Bonferroni Step Down"));
                if(params.getBoolean("sidak-correction", false))
                    statNode.add(new DefaultMutableTreeNode("Mult.-Correct.: Sidak Method"));
            }
            if(!(params.getString("trim-option").equals("NO_TRIM"))){
                if(params.getString("trim-option").equals("N_TRIM"))
                    statNode.add(new DefaultMutableTreeNode("Trim out if hit number < "+params.getInt("trim-value")));
                else
                    statNode.add(new DefaultMutableTreeNode("Trim out if hit percent < "+params.getInt("trim-value")));
            }
            newNode.add(statNode);
        }
        generalInfo.add(newNode);
        
        root.add(generalInfo);
    }
    
    
    
    /** Listens to algorithm events and updates the logger.
     */
    private class Listener extends DialogListener implements AlgorithmListener{
        String eventDescription;
        /** Handles algorithm events.
         * @param actionEvent event object
         */
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            String command = actionEvent.getActionCommand();
            if (command.equals("cancel-command")) {
                System.out.println("abort execution");
                stop = true;
                algorithm.abort();
                progress.dispose();
                logger.dispose();
            }
        }
        
        /** Invoked when an algorithm progress value was changed.
         *
         * @param event a <code>AlgorithmEvent</code> object.
         */
        public void valueChanged(AlgorithmEvent event) {
            if(event.getId() == AlgorithmEvent.MONITOR_VALUE){
                logger.append( event.getDescription() );
            } else {  //event to progress
                
                eventDescription = event.getDescription();
                
                if(eventDescription.equals("SET_VALUE")){
                    progress.setValue(event.getIntValue());
                    return;
                } else if(eventDescription.equals("SET_UNITS")){
                    progress.setDescription("Resampling Analysis Iterations");
                    progress.setValue(0);
                    progress.setUnits(event.getIntValue());
                    progress.show();
                    java.awt.Point p = progress.getLocation();
                    java.awt.Point loggerP = logger.getLocation();
                    progress.setLocation(p.x, loggerP.y-progress.getHeight());
                    return;
                } else {  //default dispose
                    progress.setVisible(false);
                    progress.dispose();
                }
            }
        }
    }
}
