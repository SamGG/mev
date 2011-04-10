/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: GOSEQGUI.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-11-07 17:27:40 $
 * $Author: dschlauch $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.goseq;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.IRNASeqSlide;
import org.tigr.microarray.mev.RNASeqElement;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.impl.ease.EaseAlgorithmData;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.ease.EASEGUI;
import org.tigr.microarray.mev.cluster.gui.impl.deseq.DESEQGUI;
import org.tigr.microarray.mev.cluster.gui.impl.degseq.DEGseqGUI;
import org.tigr.microarray.mev.cluster.gui.impl.edger.EDGERGUI;
import org.tigr.microarray.mev.cluster.gui.impl.goseq.gotree.GOTreeViewer;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;
import org.tigr.util.FloatMatrix;

/**
 * @author  dschlauch
 * @version
 */
public class GOSEQGUI implements IClusterGUI, IScriptGUI {
    
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
    protected float[][] geneGroupMeans, geneGroupSDs;
    Vector<String> exptNamesVector;
    protected IData data;
    protected float alpha;
    protected int iterations;
    protected ArrayList<String> geneLabels;
    protected ArrayList<String> sampleLabels;
	private FloatMatrix resultMatrix;
	private String[] geneListNames;
	private String[] geneSetFilePath;
	private int geneSetOrigin;
	private int[] diffGeneCluster;
	private int numPerms;
	private String annotChosen;
	private float[][] pwfBinData;
	private int numGenesPerBin;
	private float[][] geneSetSigs;
	private String[] categoryNames;
	EaseAlgorithmData result;
	private boolean isGO;
	private String biasString;
	private boolean isRunDE;
	private String deAnalysis;
	private int[][] geneLists;
    
    /** Creates new GOSEQGUI */
    public GOSEQGUI() {
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
    @SuppressWarnings("unchecked")
	public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
		//Before anything check for Mac OS and throw appropriate msg
		if(sysMsg() != JOptionPane.OK_OPTION)
			return null;
        this.experiment = framework.getData().getExperiment();        
        this.data = framework.getData();
        exptNamesVector = new Vector<String>();
        int number_of_samples = experiment.getNumberOfSamples();
        
        int [] columnIndices = experiment.getColumnIndicesCopy(); 
        
        GOSEQInitBox GOSEQDialog = new GOSEQInitBox((JFrame)framework.getFrame(), true, exptNamesVector, framework.getData().getAllFilledAnnotationFields(), framework.getClusterRepository(0));
        GOSEQDialog.setVisible(true);
        
        sampleLabels = new ArrayList<String>();
        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(framework.getData().getFullSampleName(columnIndices[i]));
            sampleLabels.add(framework.getData().getFullSampleName(columnIndices[i])); //Raktim
        }
        
        annotChosen = GOSEQDialog.getSelectedAnnotation();
        geneLabels = new ArrayList<String>();
        for (int i = 0; i < experiment.getNumberOfGenes(); i++) {
        	geneLabels.add(framework.getData().getElementAnnotation(i, annotChosen)[0]);
        }
        
        
        if (!GOSEQDialog.isOkPressed()) return null;
        isGO = GOSEQDialog.isGOAnalysis();
        isRunDE = GOSEQDialog.isRunDEAnalysis();
        deAnalysis = GOSEQDialog.getDEAnalysis();
        result = new EaseAlgorithmData();
        if (isGO){
        	try{
	        	EASEGUI gui = new EASEGUI();
	        	gui.execute(framework);
	        	result = gui.getAlgData();
        	} catch (Exception e){
        		e.printStackTrace();
        		JOptionPane.showMessageDialog(null, "Error running EASE.", "Error", JOptionPane.ERROR_MESSAGE);
        		return null;
			}
        } else if (isRunDE){
        	try{
	        	if (deAnalysis.equalsIgnoreCase("EDGER")){
	            	EDGERGUI gui = new EDGERGUI();
	            	gui.execute(framework);
	            	diffGeneCluster = gui.getResultCluster();	            	
	        	} else if (deAnalysis.equalsIgnoreCase("DEGSEQ")){
	        		DEGseqGUI gui = new DEGseqGUI();
	            	gui.execute(framework);
	            	diffGeneCluster = gui.getResultCluster();
	        	} else if (deAnalysis.equalsIgnoreCase("DESEQ")){
	            	DESEQGUI gui = new DESEQGUI();
	            	gui.execute(framework);
	            	diffGeneCluster = gui.getResultCluster();
	        	} else {
	        		JOptionPane.showMessageDialog(null, "Could not find "+deAnalysis+".", "Error", JOptionPane.ERROR_MESSAGE);
	        		return null;
				}

		        geneSetFilePath = GOSEQDialog.getDEGeneSetFilePath();
		        geneSetOrigin = GOSEQDialog.getDEGeneSetOrigin();
        	} catch (Exception e){
        		e.printStackTrace();
        		JOptionPane.showMessageDialog(null, "Error running "+deAnalysis+".", "Error", JOptionPane.ERROR_MESSAGE);
        		return null;
			}
        	
        } else { //running just cluster
	        geneSetFilePath = GOSEQDialog.getClusterGeneSetFilePath();
	        geneSetOrigin = GOSEQDialog.getClusterGeneSetOrigin();
        }
        alpha = GOSEQDialog.getAlpha();
        numPerms = GOSEQDialog.getNumPerms();
        numGenesPerBin = GOSEQDialog.getNumBins();
        if (!this.isRunDE)
        	diffGeneCluster = GOSEQDialog.getDiffGeneSet();
        int[] nonDiffGeneCluster = new int[experiment.getNumberOfGenes()-diffGeneCluster.length];
        ArrayList<Integer> diffAL = new ArrayList<Integer>();
        for (int i=0; i<diffGeneCluster.length; i++)
        	diffAL.add(diffGeneCluster[i]);
        int ind = 0;
        for (int i=0; i<experiment.getNumberOfGenes(); i++){
        	if (!diffAL.contains(i)){
        		nonDiffGeneCluster[ind]=i;
        		ind++;
        	}       		
        }
        clusters = new int[2][];
        clusters[0]=diffGeneCluster;
        clusters[1]=nonDiffGeneCluster;
        
        Listener listener = new Listener();
        
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("GOSEQ");
            algorithm.addAlgorithmListener(listener);
            
            this.progress = new Progress(framework.getFrame(), "Running GOSEQ Analysis", listener);
            this.progress.setIndeterminate(true);
            this.progress.setIndeterminantString("");
            this.progress.show();
            int[] transInt= new int[experiment.getNumberOfGenes()];
            biasString = GOSEQDialog.getBias();
            try{
	            if (biasString.equals("RNASeq Transcript Length")){
		        	ArrayList<IRNASeqSlide> temp = (ArrayList<IRNASeqSlide>)data.getFeaturesList();
		            for (int i=0; i<experiment.getNumberOfGenes(); i++){
		            	transInt[i] = ((RNASeqElement)(temp.get(0).getSlideDataElement(i))).getTranscriptLength();
		            }
	            } else if (biasString.equals("Total Expression")){
	            	for (int i=0; i<experiment.getNumberOfGenes(); i++){
	            		float sum = 0f;
	            		for (int j=0; j<experiment.getNumberOfSamples(); j++){
	            			 sum = sum + experiment.get(i, j);
	            		}
	            		transInt[i] = (int)sum;
		            }
	            } else {
	                String[] trans = framework.getData().getAnnotationList(GOSEQDialog.getBias());
	                for (int i=0; i<trans.length; i++){
	                	transInt[i] = Integer.parseInt(trans[i]);                	
	                }
	            }
            } catch (Exception e){
            	e.printStackTrace();
	    		JOptionPane.showMessageDialog(null, "Error reading values in "+GOSEQDialog.getBias()+".", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
	    	}
            result.addMatrix("experiment", experiment.getMatrix());
            result.addParam("isGO", String.valueOf(isGO));
            result.addParam("geneSetOrigin",String.valueOf(geneSetOrigin));
            result.addStringArray("geneSetFilePaths", geneSetFilePath);
            result.addIntArray("diffGeneCluster", diffGeneCluster);
            result.addIntArray("transcriptLengths", transInt);
            result.addParam("alpha", String.valueOf(alpha));
            result.addParam("numPerms", String.valueOf(numPerms));
            result.addParam("numGenesPerBin", String.valueOf(numGenesPerBin));

            result.addStringArray("geneLabels", geneLabels.toArray(new String[geneLabels.size()]));
            result.addStringArray("sampleLabels", sampleLabels.toArray(new String[sampleLabels.size()]));
                        
            long start = System.currentTimeMillis();
            algorithm.execute(result);
            long time = System.currentTimeMillis() - start;
            
            // getting the results
            
            Cluster result_cluster = result.getCluster("cluster");

            this.geneListNames = result.getStringArray("gene-list-names");
            this.geneLists = result.getIntMatrix("gene-lists");
            this.resultMatrix = result.getMatrix("resultsMatrix");    
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
//          this.pwf = result.getMatrix("pwf").A[0];
            pwfBinData = result.getMatrix("pwfM2").A;
            geneSetSigs = result.getMatrix("geneSetSigs").A;
            categoryNames = result.getStringArray("category-names");
            FloatMatrix geneGroupMeansMatrix = result.getMatrix("geneGroupMeansMatrix");
            
            FloatMatrix geneGroupSDsMatrix = result.getMatrix("geneGroupSDsMatrix");
            
            iterations = result.getParams().getInt("iterations");
            
            geneGroupMeans = new float[geneGroupMeansMatrix.getRowDimension()][geneGroupMeansMatrix.getColumnDimension()];
            geneGroupSDs = new float[geneGroupSDsMatrix.getRowDimension()][geneGroupSDsMatrix.getColumnDimension()];
            for (int i = 0; i < geneGroupMeans.length; i++) {
                for (int j = 0; j < geneGroupMeans[i].length; j++) {
                    geneGroupMeans[i][j] = geneGroupMeansMatrix.A[i][j];
                    geneGroupSDs[i][j] = geneGroupSDsMatrix.A[i][j];
                }
            }
            
            
            auxTitles = new String[1];
            auxTitles[0] = "PWF";
            auxData = new Object[experiment.getNumberOfGenes()][auxTitles.length];
            for (int i = 0; i < auxData.length; i++) {
            	auxData[i][0] = "NA";//pwf[i];
            }
            
            
            GeneralInfo info = new GeneralInfo();
            info.time = time;
            //ADD MORE INFO PARAMETERS HERE
            info.alpha = alpha;
            if (this.isGO)
            	replaceResultClusterResults();
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
    
    private void replaceResultClusterResults() {
    	for (int i=0; i<result.getResultMatrix().length; i++){
    		result.getResultMatrix()[i][8] = String.valueOf(resultMatrix.A[i][1]);
    	}
	}

	public AlgorithmData getScriptParameters(IFramework framework) {
        
        this.experiment = framework.getData().getExperiment();
        exptNamesVector = new Vector<String>();
        int number_of_samples = experiment.getNumberOfSamples();
        
        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(framework.getData().getFullSampleName(experiment.getSampleIndex(i)));
        }
        
        GOSEQInitBox GOSEQDialog = new GOSEQInitBox((JFrame)framework.getFrame(), true, exptNamesVector, framework.getData().getAllFilledAnnotationFields(), framework.getClusterRepository(1));
        GOSEQDialog.setVisible(true);
        
        if (!GOSEQDialog.isOkPressed()) return null;
        
        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.EUCLIDEAN;
        }
        
        AlgorithmData data = new AlgorithmData();
        
        data.addParam("alpha-value", String.valueOf(alpha));
        
        // alg name
        data.addParam("name", "GOSEQ");
        
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
        return null;
    }
    
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    protected DefaultMutableTreeNode createResultTree(Cluster result_cluster, GeneralInfo info) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("GOSEQ");
        addResultNodes(root, result_cluster, info);
        return root;
    }
    
    /**
     * Adds result nodes into the tree root.
     */
    protected void addResultNodes(DefaultMutableTreeNode root, Cluster result_cluster, GeneralInfo info) {
    	addInputViewers(root);
//        addCentroidViews(root);
    	
        addExpressionImages(root);
        addTableViews(root);
        addGeneSetInfo(root);
        addPWFGraphs(root);
        if (isGO)
        	addGOTree(root, (EaseAlgorithmData)result, result.getHeaderNames());
        addGeneralInfo(root, info);
    }
    
    private void addInputViewers(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode inNode = new DefaultMutableTreeNode("Input Viewers");
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer = new GOSEQExperimentViewer(this.experiment, clusters, null, null, null, null, null, null, null, null, null);
    	node.add(new DefaultMutableTreeNode(new LeafInfo("Differentially Expressed Cluster", expViewer, new Integer(0))));
    	node.add(new DefaultMutableTreeNode(new LeafInfo("Non-Differentially Expressed Cluster", expViewer, new Integer(1))));
    	inNode.add(node);

        DefaultMutableTreeNode nodeT = new DefaultMutableTreeNode("Table Views");
        IViewer tabViewer = new ClusterTableViewer(this.experiment, this.clusters, this.data, this.auxTitles, this.auxData);
        nodeT.add(new DefaultMutableTreeNode(new LeafInfo("Differentially Expressed Cluster", tabViewer, new Integer(0))));
        nodeT.add(new DefaultMutableTreeNode(new LeafInfo("Non-Differentially Expressed Cluster", tabViewer, new Integer(1))));
        inNode.add(nodeT);
    	
        root.add(inNode);		
	}

	protected void addGOTree(DefaultMutableTreeNode root, EaseAlgorithmData data, String[] headerNames) {
        
        String categories = new String("");
        
        for(int i = 0; i < categoryNames.length; i++)
            categories += categoryNames[i];
        
        if(categories.indexOf("GO Biological Process") != -1) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode();
            String category = "GO Biological Process";
            GOTreeViewer viewer = new GOTreeViewer(category, headerNames, data, root);
            node.setUserObject(new LeafInfo("GO Hierarchy -- Biological Process", viewer));
            root.add(node);
        }
        
        if(categories.indexOf("GO Cellular Component") != -1) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode();
            String category = "GO Cellular Component";
            GOTreeViewer viewer = new GOTreeViewer(category, headerNames, data, root);
            node.setUserObject(new LeafInfo("GO Hierarchy -- Cellular Component", viewer));
            root.add(node);
        }
        
        if(categories.indexOf("GO Molecular Function") != -1) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode();
            String category = "GO Molecular Function";
            GOTreeViewer viewer = new GOTreeViewer(category, headerNames, data, root);
            node.setUserObject(new LeafInfo("GO Hierarchy -- Molecular Function", viewer));
            root.add(node);
        }
    }
    private void addPWFGraphs(DefaultMutableTreeNode root) {
    	root.add(new DefaultMutableTreeNode(new LeafInfo("Probability Weighting Function", new GOSEQPlotViewer(pwfBinData, biasString))));		
	}

	private void addGeneSetInfo(DefaultMutableTreeNode root) {
      Object[][] results = new Object[this.resultMatrix.A.length][this.resultMatrix.A[0].length+1+geneSetSigs.length];
      for (int i=0; i<results.length; i++){
    	  results[i][0] = geneListNames[i]; //names
    	  results[i][1] = resultMatrix.A[i][0]; //total genes
    	  results[i][2] = geneSetSigs[1][i]; //significant
    	  results[i][3] = geneSetSigs[0][i]; //expected
    	  results[i][4] = resultMatrix.A[i][1]; //p-value    	  
      }
      
      String[] columns = {"Gene List","Gene Count", "Significant Genes", "Expected Sig. Genes", "p-value (permutation)"};
      
      IViewer tabViewer = new GOSEQResultTable(results,columns);
  	root.add(new DefaultMutableTreeNode(new LeafInfo("Results Table", tabViewer, new Integer(0))));
		
	}
    protected void addTableViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table Views");
        DefaultMutableTreeNode sigNode = new DefaultMutableTreeNode("Significant Gene Sets");
        DefaultMutableTreeNode nonSigNode = new DefaultMutableTreeNode("Non-significant Gene Sets");
        IViewer expViewer = new ClusterTableViewer(this.experiment, geneLists, this.data, this.auxTitles, this.auxData);
        for (int i=0; i<geneLists.length; i++){
        	if (this.resultMatrix.A[i][1]<this.alpha)
        		sigNode.add(new DefaultMutableTreeNode(new LeafInfo(this.geneListNames[i], expViewer, new Integer(i))));
        	else
        		nonSigNode.add(new DefaultMutableTreeNode(new LeafInfo(this.geneListNames[i], expViewer, new Integer(i))));        		
        }
        if (sigNode.getLeafCount()!=1)
        	node.add(sigNode);
        if (nonSigNode.getLeafCount()!=1)
        	node.add(nonSigNode);
        
        root.add(node);
    }
    
    /**
     * Adds nodes to display clusters data.
     */
    protected void addExpressionImages(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode sigNode = new DefaultMutableTreeNode("Significant Gene Sets");
        DefaultMutableTreeNode nonSigNode = new DefaultMutableTreeNode("Non-significant Gene Sets");
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer = new GOSEQExperimentViewer(this.experiment, geneLists, null, null, null, null, null, null, null, null, null);
        for (int i=0; i<geneLists.length; i++){
        	if (this.resultMatrix.A[i][1]<this.alpha)
        		sigNode.add(new DefaultMutableTreeNode(new LeafInfo(this.geneListNames[i], expViewer, new Integer(i))));
        	else
        		nonSigNode.add(new DefaultMutableTreeNode(new LeafInfo(this.geneListNames[i], expViewer, new Integer(i))));        		
        }
        if (sigNode.getLeafCount()!=1)
        	node.add(sigNode);
        if (nonSigNode.getLeafCount()!=1)
        	node.add(nonSigNode);
        root.add(node);
    }
        
    /**
     * Adds node with general information.
     */
    protected void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        node.add(new DefaultMutableTreeNode(this.isGO ? "GO Analysis": (this.isRunDE ? this.deAnalysis : "Cluster Analysis")));
        node.add(new DefaultMutableTreeNode("Iterations: " + this.iterations));
        node.add(new DefaultMutableTreeNode("Bias: " + this.biasString));
        node.add(new DefaultMutableTreeNode("Alpha: " + this.alpha));
        if (!isGO){
        	String gsfp = "";
        	for (int i=0; i<geneSetFilePath.length; i++){
        		gsfp = gsfp + geneSetFilePath;
        		if (i<geneSetFilePath.length-1)
        			gsfp = gsfp + ", ";
        	}
        	node.add(new DefaultMutableTreeNode("Gene Set: " + gsfp.toString()));
        }
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time-1)+" ms"));
        root.add(node);
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
            }
        }
        
        public void windowClosing(WindowEvent e) {
            algorithm.abort();
            progress.dispose();
        }
    }
    
	private int sysMsg() {
		String os = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");
		String ver = System.getProperty("os.version");

		String message = "System Config:\n";
		message += "OS: " + os + " | Architecture: " + arch + " | Version: " + ver + "\n";
		message += "Please note:\n";
		if(arch.toLowerCase().contains("64") && os.toLowerCase().contains("mac")) {
			message += "You need to have 32Bit JVM as default for GOSEQ\n";
			message += "Please contact MeV Support if you need help.\n";
			message += "You also need to have R 2.9.x installed for GOSEQ\n";
			message += "Cancel if either is not installed. Ok to continue.";
			return JOptionPane.showConfirmDialog(null, message, "R Engine Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		}
		if(arch.toLowerCase().contains("64")) {
			message += "You need to have 32Bit JVM as default for GOSEQ\n";
			message += "Please contact MeV Support if you need help.\n";
			message += "Cancel if 32 Bit JVM is not installed. Ok to continue.";
			return JOptionPane.showConfirmDialog(null, message, "R Engine Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		}
		if (os.toLowerCase().contains("mac")) {
			message += "You need to have R 2.9.x installed for GOSEQ\n";
			message += "Cancel if R is not installed. Ok to continue.";
			return JOptionPane.showConfirmDialog(null, message, "R Engine Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		}
		return JOptionPane.OK_OPTION;
	}

    protected class GeneralInfo {

        public int clusters;
        public String correctionMethod;
        public float alpha;
        public long time;
        public String function;
        
        protected boolean hcl, usePerms;
        protected int hcl_method, numPerms;
        protected boolean hcl_genes;
        protected boolean hcl_samples;
    	//EH constructor added so AMP could extend
        protected GeneralInfo(){
    		super();
    	}        
        
    }
    
}
