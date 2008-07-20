/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: EASEGUI.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-11-07 17:27:40 $
 * $Author: eleanorahowe $
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
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.annotation.AnnotationFileReader;
import org.tigr.microarray.mev.annotation.IChipAnnotation;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.cluster.algorithm.*;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.*;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Logger;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.ease.gotree.GOTreeViewer;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;


/** The <CODE>EASEGUI</CODE> class contains code to gather parameters
 * for EASE annotation analysis, to run the analysis, and to display
 * various results from the analysis.
 */
public class EASEGUI implements IClusterGUI, IScriptGUI {
    
    /** The algorithm class for execution of EASE.
     */
    protected Algorithm algorithm;
    /** The <CODE>AlgorithmData<\CODE> object to encapsulate parameters, input data, and results
     */
    protected AlgorithmData algorithmData;
    /** The <CODE>Experiment</CODE> data wrapper class.
     */
    protected Experiment experiment;
    /** The input <CODE>Cluster</CODE> object for cluster analysis
     */
    protected Cluster cluster;
    /** Encapsulates the indices of clusters created by the analysis.
     */
    protected int [][] clusters;
    /** Names of the theme categories found in the gene list.
     */
    protected String [] categoryNames;
    /** The main result matrix for table display.
     */
    protected String [][] resultMatrix;
    /** Indicates if accession numbers were appended.
     */
    protected boolean haveAccessionNumbers;
    /** Indicates if the mode is cluster analysis (or if not then a survey)
     */
    protected boolean isClusterAnalysis;
    /** Verbose progress dialog
     */
    protected Logger logger;
    /** Optional progress bar.
     */
    protected Progress progress;
    /** Algorithm event listener.
     */
    protected Listener listener;
    
    protected boolean stop = false;
    /** Annotation type to use as a key (annotation field name)
     */
    protected String annotationKeyType;
    /** Indicates path of EASE base file system for this analysis
     */
    protected String baseFileSystem;
    /** Indicates if the algorithm run is via a script execution
     */
    protected boolean isScripting = false;
    protected File annotationFile;
    
    public static final String LAST_EASE_FILE_LOCATION = "last-ease-file-location";
    
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
        String sep = System.getProperty("file.separator");
        algorithmData = new AlgorithmData();
        
        ClusterRepository repository = framework.getClusterRepository(Cluster.GENE_CLUSTER);
        
        //check for existance of annotation file. If there is annotation loaded and the annotation file it came
        //from can be read, save a filehandle for it. 
    	String easeFileLocation = null;
        if(framework.getData().isAnnotationLoaded()) {
        	String chipType = framework.getData().getChipAnnotation().getChipType();
        	String filename = framework.getData().getChipAnnotation().getAnnFileName();

        	annotationFile = new File(filename);
	        if(annotationFile.canRead()) {
	        	easeFileLocation = "./data/ease" + sep + "ease_" + chipType;
	        } else {
	        	annotationFile = new File("./data/Annotation/" + chipType + ".txt");
	        	easeFileLocation = "./data/ease" + sep + "ease_" + chipType;
	        	if(!annotationFile.canRead()) {
	        		easeFileLocation = null;
	        	}
	        } 
        }
        
        EASEInitDialog dialog = new EASEInitDialog(
        		framework.getFrame(), 
        		repository, 
        		framework.getData().getAllFilledAnnotationFields(), 
        		easeFileLocation);
        
        if(dialog.showModal() != JOptionPane.OK_OPTION)
            return null;
        
        listener = new Listener();
        logger = new Logger(framework.getFrame(), "EASE Analysis", listener);
        logger.show();
        progress = new Progress(framework.getFrame(), "Probability Analysis Resampling Progress", listener);
        
        baseFileSystem = dialog.getBaseFileLocation();
        algorithmData.addParam("base-file-system", baseFileSystem);
        isClusterAnalysis = dialog.isClusterModeSelected();
        String converterFileName = dialog.getConverterFileName();
        annotationKeyType = dialog.getAnnotationKeyType();
        String [] annotationFileList = dialog.getAnnToGOFileList();
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

            algorithmData.addStringArray("sample-list", clusterKeys);
            algorithmData.addIntArray("sample-indices", cluster.getExperimentIndices());  //drop in experiment indices
        }
        
        //Use file or IData for population, only permit file use for cluster analysis
        //populationKeys contains the list of IDs (usually EntrezGene IDs) that represent the background 
        //population of the EASE run. This can be taken from the current viewer, a list or an annotation file.
        String [] populationKeys;
        if(isClusterAnalysis && dialog.isPreloadedAnnotationSelected()) {
            try {
        		populationKeys = loadGeneIDs();
            } catch (IOException ioe) {
                //Bad file format
                JOptionPane.showMessageDialog(framework.getFrame(), "Error loading population file.", "Population File Load Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if(populationKeys == null) {
            	JOptionPane.showMessageDialog(framework.getFrame(), "Error loading population file.", "Population File Load Error", JOptionPane.ERROR_MESSAGE);
            	return null;
            }
        } else {
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
	            populationKeys = framework.getData().getAnnotationList(annotationKeyType, framework.getData().getExperiment().getRowMappingArrayCopy());            
	        }
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
        
        EASEInitDialog dialog = new EASEInitDialog(framework.getFrame(), framework.getData().getFieldNames());
        
        if(dialog.showModal() != JOptionPane.OK_OPTION)
            return null;

        baseFileSystem = dialog.getBaseFileLocation();
        algorithmData.addParam("base-file-system", baseFileSystem);
        isClusterAnalysis = dialog.isClusterModeSelected();
        String converterFileName = dialog.getConverterFileName();
        annotationKeyType = dialog.getAnnotationKeyType();
        algorithmData.addParam("annotation-key-type", annotationKeyType);
        String [] annotationFileList = dialog.getAnnToGOFileList();
        boolean isPvalueCorrectionSelected;
        experiment = framework.getData().getExperiment();
        
        if(isClusterAnalysis){
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
        }
        
        //Use file or IData for population, only permit file use for cluster analysis
//        String [] populationKeys;
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
        algorithmData.addParam("output-class", "multi-gene-cluster-output");
        
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
            
            algData.addStringArray("sample-list", clusterKeys);

            //since we have an experiment containing only genes in cluster we can make
            //default indices
            int [] tempArray = new int[indices.length];
            for(int i = 0; i < indices.length; i++) {
                tempArray[i]= i;
            }
            
            algData.addIntArray("sample-indices", tempArray);  //drop in experiment indices
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
            populationKeys = framework.getData().getAnnotationList(annotationKeyType, framework.getData().getExperiment().getRowMappingArrayCopy());
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
    
    
    protected String [] getPopulationKeysFromFile(String fileName) throws IOException {
        File file = new File(fileName);
        if(file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            Vector<String> ann = new Vector<String>();
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
    
    /**
     * Loads Unique Gene IDs from a standard-formatted Resourcerer file stored in 
     * MeV's Annotation directory, with the name slideType.
     *
     * @param slideType
     * @return Gene id keys for the background population listed in annotationFile.
     * @throws IOException
     */
    protected String [] loadGeneIDs() throws IOException {
        if(annotationFile != null && annotationFile.exists()) {
        	try {
	        	AnnotationFileReader afr = AnnotationFileReader.createAnnotationFileReader(annotationFile);
	            Hashtable<String, MevAnnotation> annotations = afr.getAffyAnnotation();
	            IChipAnnotation icAnnotation = afr.getAffyChipAnnotation();
	            String[] annot = new String[annotations.size()];
	            java.util.Enumeration<String> allAnnotations = annotations.keys();
	            int i=0;
	            while(allAnnotations.hasMoreElements()) {
	            	String thisKey = allAnnotations.nextElement();
	            	org.tigr.microarray.mev.annotation.IAnnotation thisAnnotation = annotations.get(thisKey);
	            	annot[i] = thisAnnotation.getTgiTC();
	            	i++;
	            }
	            return annot;
        	} catch (IOException ioe) {
        		//TODO handle this!
        	}
        }
        return null;
    }
    
    /** Creates the result node.
     * @param result result matrix
     * @param clusters cluster indices
     * @return returns the result node
     */
    protected DefaultMutableTreeNode createResultNode(AlgorithmData result, int [][] clusters){
        DefaultMutableTreeNode root;
        if(this.isClusterAnalysis)
            root = new DefaultMutableTreeNode("EASE Analysis");
        else
            root = new DefaultMutableTreeNode("EASE Survey");
        addTableViewer(root, result);
        addExpressionViewers(root, result);
        addGeneralInfo(root, result);
        if(isClusterAnalysis)
            addGOTree(root, result);
        return root;
    }
    
    protected void addGOTree(DefaultMutableTreeNode root, AlgorithmData result) {
        String [][] data = (String [][]) (result.getObjectMatrix("result-matrix"));
        String [] headerNames = result.getStringArray("header-names");
        
        String categories = new String("");
        
        for(int i = 0; i < categoryNames.length; i++)
            categories += categoryNames[i];
        
        if(categories.indexOf("GO Biological Process") != -1) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode();
            GOTreeViewer viewer = new GOTreeViewer("GO Biological Process", headerNames, data, root, this.baseFileSystem);
            node.setUserObject(new LeafInfo("GO Hierarchy -- Biological Process", viewer));
            root.add(node);
        }
        
        if(categories.indexOf("GO Cellular Component") != -1) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode();
            GOTreeViewer viewer = new GOTreeViewer("GO Cellular Component", headerNames, data, root, this.baseFileSystem);
            node.setUserObject(new LeafInfo("GO Hierarchy -- Cellular Component", viewer));
            root.add(node);
        }
        
        if(categories.indexOf("GO Molecular Function") != -1) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode();
            GOTreeViewer viewer = new GOTreeViewer("GO Molecular Function", headerNames, data, root, this.baseFileSystem);
            node.setUserObject(new LeafInfo("GO Hierarchy -- Molecular Function", viewer));
            root.add(node);
        }
    }
    
    /** creates an empty result if the result is null.
     * @param result
     * @return  */
    protected DefaultMutableTreeNode createEmptyResultNode(AlgorithmData result){
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("EASE");
        root.add(new DefaultMutableTreeNode("No Annotation Hits"));
        addGeneralInfo(root, result);
        return root;
    }
    
    /** Adds nodes to display cluster data.
     * @param root root node
     * @param result result matrix
     */
    protected void addExpressionViewers(DefaultMutableTreeNode root, AlgorithmData result) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Viewers");
        
        IViewer expViewer = new EASEExperimentViewer(this.experiment, this.clusters);
        EASECentroidViewer graphViewer = new EASECentroidViewer(this.experiment, this.clusters);
        //set means and variances in the graph viewer
        graphViewer.setMeans(result.getMatrix("means").A);
        graphViewer.setVariances(result.getMatrix("variances").A);
        DefaultMutableTreeNode clusterNode;//, annotNode, popNode;
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
    protected void addTableViewer(DefaultMutableTreeNode root, AlgorithmData result){
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
    protected int [] getDataIndices(int [] indices){
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
    protected void addGeneralInfo(DefaultMutableTreeNode root, AlgorithmData result){
        DefaultMutableTreeNode generalInfo = new DefaultMutableTreeNode("General Information");
//        String converterFileName = result.getParams().getString("converter-file-name");
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
    protected class Listener extends DialogListener implements AlgorithmListener{

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
