/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: EDGERGUI.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-11-07 17:27:40 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.edger;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.IRNASeqSlide;
import org.tigr.microarray.mev.RNASeqElement;
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
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLGUI;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;
import org.tigr.util.FloatMatrix;

//import cern.colt.Arrays;

/**
 *
 * @author  raktim
 * @version
 */
public class EDGERGUI implements IClusterGUI, IScriptGUI {

	protected Algorithm algorithm;
	protected Progress progress;
	protected Experiment experiment;

	boolean debug = true;
	protected String methodName = null;
	protected String estimatorName = null;
	//protected String discretizationName = null;
	protected int bins = 0;

	protected ArrayList<String> geneLabels;
	protected ArrayList<String> sampleLabels;
	Vector<String> exptNamesVector;
	protected int[] groupAssignments;

	protected IData data;
	protected int dataDesign;

	FloatMatrix resultMatrix;
	String resultRowNames[];

	float sigCutOff = 0.05f;
	String sigMethod = "fdr";
	Object[][] auxData;
	int[][] clusters;
	String[] headerNames;

	/** Creates new EDGERGUI */
	public EDGERGUI() {
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
		// Temp bail out
		//if (true) {
		//JOptionPane.showMessageDialog(framework.getFrame(),"Coming Soon ...");
		//return null;
		//}
		// End 
		this.data = framework.getData();
		exptNamesVector = new Vector<String>();
		for (int i = 0; i < this.data.getFeaturesCount(); i++) {
			exptNamesVector.add(framework.getData().getFullSampleName(i));
		}

		EDGERInitBox EDGERDialog = new EDGERInitBox(
				(JFrame)framework.getFrame(), 
				true, 
				exptNamesVector
				//framework.getClusterRepository(
						//org.tigr.microarray.mev.cluster.clusterUtil.Cluster.EXPERIMENT_CLUSTER), 
				//framework.getClusterRepository(
				//org.tigr.microarray.mev.cluster.clusterUtil.Cluster.GENE_CLUSTER)
		);
		EDGERDialog.setVisible(true);

		if (!EDGERDialog.isOkPressed()) return null;

		methodName = EDGERDialog.methodsPanel.getMethodName();
		//estimatorName = EDGERDialog.estimatorPanel.getEstimatorName();
		//discretizationName = EDGERDialog.discretizationPanel.getDiscretizationMethodName();
		sigMethod = EDGERDialog.getCutOffField();
		sigCutOff = EDGERDialog.getPValue();
		dataDesign=EDGERDialog.getTestDesign();
		//if (EDGERDialog.getTestDesign()==EDGERInitBox.ONE_CLASS){
		//if (EDGERDialog.getSelectionDesign()==EDGERInitBox.CLUSTER_SELECTION){
		//groupAssignments=EDGERDialog.getClusterOneClassAssignments();
		//}
		if (EDGERDialog.getTestDesign()==EDGERInitBox.TWO_CLASS){
			groupAssignments=EDGERDialog.getTwoClassAssignments();
		}
		//}

		// count # of samples used in analysis
		int samplesUsed = 0;
		for(int i = 0; i < groupAssignments.length; i++) {
			if(groupAssignments[i] != 0)
				samplesUsed++;
		}
		// get samples indices used
		int sampleIndices[] = new int[samplesUsed];
		for(int i = 0, ii = 0; i < groupAssignments.length; i++) {
			if(groupAssignments[i] != 0)
				sampleIndices[ii++] = i;
		}

		// set up the group struct for algo
		int[] twoClassGrps = new int[samplesUsed]; 
		for(int i = 0, ii = 0; i < groupAssignments.length; i++) {
			if(groupAssignments[i] != 0)
				twoClassGrps[ii++] = groupAssignments[i];
		}
		//System.out.println("groupAssignments: " + Arrays.toString(groupAssignments));
		//System.out.println(samplesUsed + " out of " + groupAssignments.length + " used. Sample indices: " + Arrays.toString(sampleIndices));

		this.experiment = framework.getData().getExperiment();        
		//int number_of_samples = this.data.getFeaturesCount();//experiment.getNumberOfSamples();
		//int [] columnIndices = experiment.getColumnIndicesCopy(); 

		sampleLabels = new ArrayList<String>();
		geneLabels = new ArrayList<String>();
		for (int i = 0; i < samplesUsed; i++) {
			sampleLabels.add(framework.getData().getFullSampleName(sampleIndices[i])); //Raktim
		}

		//Raktim Use probe index as the gene labels in R
		for (int i = 0; i < this.data.getFeaturesSize();/*experiment.getNumberOfGenes();*/ i++) {
			//geneLabels.add(framework.getData().getElementAnnotation(i, AnnotationFieldConstants.PROBE_ID)[0]); //Raktim
			geneLabels.add(String.valueOf(i));
		}

		// Make Count Matrix  based on data
		int numGenes = this.data.getFeaturesSize();
		System.out.println("data.getFeaturesCount(): " + this.data.getFeaturesCount() + 
				" data.getFeaturesSize(): " + this.data.getFeaturesSize());
		ArrayList<IRNASeqSlide> temp = (ArrayList<IRNASeqSlide>)data.getFeaturesList();
		// TEst Code from EH
		/*
	        RNASeqChipAnnotation chipAnnotation = (RNASeqChipAnnotation)data.getChipAnnotation();
	    	System.out.println("Library size, first sample: " + temp.get(0).getLibrarySize());
	    	System.out.println("Library size, last sample: " + temp.get(temp.size()-1).getLibrarySize());
	    	System.out.println("\n");

	    	System.out.println("Read length: " + chipAnnotation.getReadLength());
	    	System.out.println("\n");

	    	System.out.println("first count value for first slide: " + temp.get(0).getCount(0));
	    	System.out.println("last count value for first slide: " + temp.get(0).getCount(temp.get(0).getSize()-1));
	    	System.out.println("\n");

	    	System.out.println("first count value for last slide: " + temp.get(temp.size()-1).getCount(0));
	    	System.out.println("last count value for last slide: " + temp.get(temp.size()-1).getCount(temp.get(0).getSize()-1));
	    	System.out.println("\n");

	    	System.out.println("transcript length 0,0: " + ((RNASeqElement)(temp.get(0).getSlideDataElement(0))).getTranscriptLength());
	    	System.out.println("classcode for  0,0: " + ((RNASeqElement)(temp.get(0).getSlideDataElement(0))).getClasscode());
		 */
		// End EH Test Code

		// My test code
		/*
	    	System.out.println("transcript length 0,0: " + ((RNASeqElement)(temp.get(0).getSlideDataElement(9))).getTranscriptLength());
	    	System.out.println("classcode for  0,0: " + ((RNASeqElement)(temp.get(0).getSlideDataElement(9))).getClasscode());
		 */
		// End my test code

		/*
        	ArrayList<IRNASeqSlide> temp = (ArrayList<IRNASeqSlide>)data.getFeaturesList();
		 */

		int[][]	countMatrix 	= new int[numGenes][samplesUsed];
		int[]	transcriptLen 	= new int[numGenes];
		int[]	libSize			= new int[samplesUsed];
		for(int row=0; row < numGenes; row++) {
			for(int col=0; col < sampleIndices.length; col++) {
				// get count value
				countMatrix[row][col] = temp.get(sampleIndices[col]).getCount(row);
			}
			// get transcript len for each gene
			//transcriptLen[row] = ((RNASeqElement)(temp.get(row).getSlideDataElement(row))).getTranscriptLength();
			transcriptLen[row] = ((RNASeqElement)(temp.get(0).getSlideDataElement(row))).getTranscriptLength();
		}

		// get lib size for each sample used
		for(int col=0; col < sampleIndices.length; col++) {
			// get count value
			libSize[col] = temp.get(sampleIndices[col]).getLibrarySize();
		}

		//System.out.println("CountMatrix: " + Arrays.deepToString(countMatrix));
		//System.out.println("transcriptLen: " + Arrays.toString(transcriptLen));
		//System.out.println("libSize: " + Arrays.toString(libSize));

		Listener listener = new Listener();

		try {
			algorithm = framework.getAlgorithmFactory().getAlgorithm("EDGER");
			algorithm.addAlgorithmListener(listener);

			this.progress = new Progress(framework.getFrame(), "Running edgeR ...", listener);
			this.progress.show();

			AlgorithmData data = new AlgorithmData();

			//data.addMatrix("experiment", experiment.getMatrix());
			data.addIntMatrix("experiment", countMatrix);

			data.addParam("dataDesign", String.valueOf(dataDesign));
			data.addIntArray("group_assignments", twoClassGrps);
			data.addIntArray("libSize", libSize);
			data.addIntArray("transcriptLen", transcriptLen);
			//data.addIntArray("geneIndices", geneIndices);
			data.addParam("numGenes", String.valueOf(numGenes));
			data.addParam("numExps", String.valueOf(samplesUsed));
			data.addParam("methodName", methodName);
			//data.addParam("bins", String.valueOf(bins)); //sqrt(sample size)
			//data.addParam("classes", String.valueOf(EDGERDialog.getTestDesign()));

			data.addStringArray("geneLabels", geneLabels.toArray(new String[geneLabels.size()]));
			data.addStringArray("sampleLabels", sampleLabels.toArray(new String[sampleLabels.size()]));

			// run algorithm
			long start = System.currentTimeMillis();
			AlgorithmData result = algorithm.execute(data);
			long time = System.currentTimeMillis() - start;

			// getting the results
			resultMatrix = result.getMatrix("result");
			resultRowNames = result.getStringArray("rownames");

			// Process results     
			createHeaderNames();
			createAuxData();
			createResultClusters();

			GeneralInfo info = new GeneralInfo();
			info.time = time;

			System.out.println("Creating Viewers for edgeR...");
			return createResultTree(info);

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
	 * order of ROWS in resultMAtrix:
	 * Row 1 logConc;
	 * Row 2 logFC;
	 * Row 2 PValue;
	 * Row 3 FDR;
	 */
	private void createHeaderNames() {
		this.headerNames = new String[4];
		this.headerNames[0]="logConc";
		this.headerNames[1]="logFC";
		this.headerNames[2]="PValue";
		this.headerNames[3]="FDR";
	}

	/**
	 * order of ROWS in resultMAtrix:
	 * Row 0 logConc;
	 * Row 1 logFC;
	 * Row 2 PValue;
	 * Row 3 FDR;
	 */
	private void createAuxData() {
		this.auxData = new Object[this.resultRowNames.length][headerNames.length];
		for(int i=0; i < this.resultRowNames.length; i++) {
			int j=0;
			int ind = Integer.parseInt(this.resultRowNames[i]);
			/*
    		auxData[i][j++] = this.data.getElementAnnotation(ind, headerNames[0])[0];
    		auxData[i][j++] = this.data.getElementAnnotation(ind, headerNames[1])[0];
    		auxData[i][j++] = this.data.getElementAnnotation(ind, headerNames[2])[0];
    		auxData[i][j++] = this.data.getElementAnnotation(ind, headerNames[3])[0];
			 */
			//this.resultRowNames has indexes as "1", "2" etc
			this.auxData[ind][j++] = Float.valueOf(this.resultMatrix.get(0, i));
			this.auxData[ind][j++] = Float.valueOf(this.resultMatrix.get(1, i));
			this.auxData[ind][j++] = Float.valueOf(this.resultMatrix.get(2, i));
			this.auxData[ind][j++] = Float.valueOf(this.resultMatrix.get(3, i));
		}
	}

	private void createResultClusters() {
		this.clusters = new int[2][];
		// choose FDR (default) or PValue
		int methodInd = 3;
		if(this.sigMethod.equals("pvalue"))
			methodInd = 2;
		// figure out how many are sig and non-sig by running thru the array
		int sigCnt = 0;
		for(int i=0; i < this.resultMatrix.getColumnDimension();i++){
			if (this.resultMatrix.get(methodInd, i) <= this.sigCutOff)
				sigCnt++;
		}
		// allocate sig array
		this.clusters[0] = new int[sigCnt];
		// allocate non-sig array
		this.clusters[1] = new int[this.resultMatrix.getColumnDimension()-sigCnt];
		//map indices of sig & non-sig
		for(int i=0, j=0, k=0; i < this.resultMatrix.getColumnDimension();i++){
			int ind = Integer.parseInt(this.resultRowNames[i]);
			if (this.resultMatrix.get(methodInd, i) <= this.sigCutOff)
				this.clusters[0][k++] = ind;
			else
				this.clusters[1][j++] = ind;
		}

	}
	public AlgorithmData getScriptParameters(IFramework framework) {

		this.experiment = framework.getData().getExperiment();
		exptNamesVector = new Vector<String>();
		int number_of_samples = experiment.getNumberOfSamples();
		//int number_of_genes = experiment.getNumberOfGenes();

		for (int i = 0; i < number_of_samples; i++) {
			exptNamesVector.add(framework.getData().getFullSampleName(experiment.getSampleIndex(i)));
		}

		EDGERInitBox EDGERDialog = new EDGERInitBox(
				(JFrame)framework.getFrame(), 
				true, 
				exptNamesVector//,
				//framework.getClusterRepository(1),
				//framework.getClusterRepository(0)
		);
		EDGERDialog.setVisible(true);

		if (!EDGERDialog.isOkPressed()) return null;

		//float alpha = EDGERDialog.getPValue();
		//numTimePoints = EDGERDialog.getNumTimePoints();

		//if (EDGERDialog.getTestDesign()==EDGERInitBox.ONE_CLASS){
		//if (EDGERDialog.getTestDesign()==EDGERInitBox.CLUSTER_SELECTION){
		//groupAssignments=EDGERDialog.getClusterOneClassAssignments();
		//}
		if (EDGERDialog.getTestDesign()==EDGERInitBox.TWO_CLASS){
			groupAssignments=EDGERDialog.getTwoClassAssignments();
		}
		//}

		AlgorithmData data = new AlgorithmData();       

		// alg name
		data.addParam("name", "edgeR");

		// alg type
		data.addParam("alg-type", "data-visualization");

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
		//this.timeAssignments = algData.getIntArray("time_assignments");
		this.groupAssignments = algData.getIntArray("condition_assignments");

		exptNamesVector = new Vector<String>();
		int number_of_samples = experiment.getNumberOfSamples();

		for (int i = 0; i < number_of_samples; i++) {
			exptNamesVector.add(this.data.getFullSampleName(i));
		}

		try {
			algData.addMatrix("experiment", experiment.getMatrix());
			algorithm = framework.getAlgorithmFactory().getAlgorithm("MINET");
			algorithm.addAlgorithmListener(listener);

			this.progress = new Progress(framework.getFrame(), "Running MINET Analysis...", listener);
			this.progress.show();

			long start = System.currentTimeMillis();
			AlgorithmData result = algorithm.execute(algData);
			long time = System.currentTimeMillis() - start;

			AlgorithmParameters params = algData.getParams();

			GeneralInfo info = new GeneralInfo();            
			return createResultTree(info);

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

		if (sigMethod == EDGERInitBox.JUST_ALPHA) {
			methodName = "Just alpha (uncorrected)";
		} else if (sigMethod == EDGERInitBox.STD_BONFERRONI) {
			methodName = "Standard Bonferroni correction";
		} else if (sigMethod == EDGERInitBox.ADJ_BONFERRONI) {
			methodName = "Adjusted Bonferroni correction";
		} else if (sigMethod == EDGERInitBox.MAX_T) {
			methodName = "Westfall Young stepdown - MaxT";
		} else if (sigMethod == EDGERInitBox.FALSE_NUM) {
			//methodName = "False significant number: " + falseNum + " or less";
		} else if (sigMethod == EDGERInitBox.FALSE_PROP) {
			//methodName = "False significant proportion: " + falseProp + " or less";
		}

		return methodName;
	}

	/**
	 * Creates a result tree to be inserted into the framework analysis node.
	 */
	protected DefaultMutableTreeNode createResultTree(GeneralInfo info) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("edgeR");
		addResultNodes(root, info);
		return root;
	}

	/**
	 * Adds result nodes into the tree root.
	 */
	protected void addResultNodes(DefaultMutableTreeNode root, GeneralInfo info) {
		addExpressionImages(root);
		addTableViews(root);
		addGeneralInfo(root, info);
	}

	/**
	 * Adds node with general information.
	 */
	protected void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");

		node.add(getConditionAssignmentInfo());

		node.add(new DefaultMutableTreeNode("Inference Algorithm: "+methodName));
		node.add(new DefaultMutableTreeNode("Mutual Info Estimator: "+estimatorName));       
		node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time-1)+" ms"));
		root.add(node);
	}


	protected DefaultMutableTreeNode getConditionAssignmentInfo() {
		DefaultMutableTreeNode groupAssignmentInfo = new DefaultMutableTreeNode("Group assignments ");
		DefaultMutableTreeNode notInGroups = new DefaultMutableTreeNode("Samples Excluded");
		DefaultMutableTreeNode inGroup;
		inGroup = new DefaultMutableTreeNode("Samples Included");
		groupAssignmentInfo.add(inGroup);

		for (int i = 0; i < groupAssignments.length; i++) {
			int currentGroup = groupAssignments[i];
			if (currentGroup == 0)
				notInGroups.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
			else 
				inGroup.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
		}

		if (notInGroups.getChildCount() > 0) {
			groupAssignmentInfo.add(notInGroups);
		}
		return groupAssignmentInfo;
	}

	/**
	 * Adds nodes to display clusters data.
	 */
	protected void addExpressionImages(DefaultMutableTreeNode root) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
		IViewer expViewer = new ExperimentViewer(this.experiment, clusters);
		node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", expViewer, new Integer(0))));
		node.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", expViewer, new Integer(1))));
		root.add(node);
	}

	protected void addTableViews(DefaultMutableTreeNode root) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table Views");

		IViewer tabViewer = new ClusterTableViewer(data.getExperiment(), (int[][])null, data, headerNames, auxData);
		node.add(new DefaultMutableTreeNode(new LeafInfo("Gene List", tabViewer, new Integer(0))));

		// Create sig and non-sig table views    	
		tabViewer = new ClusterTableViewer(this.experiment, clusters, data, headerNames, auxData);
		node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Gene List", tabViewer, new Integer(0))));
		node.add(new DefaultMutableTreeNode(new LeafInfo("Non-Significant Gene List", tabViewer, new Integer(1))));
		root.add(node);
	}

	public int[] getResultCluster(){
		return clusters[0];
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
		public float alpha;
		public long time;
		public String function;

		protected boolean hcl, usePerms;
		protected int hcl_method, numPerms;
		protected boolean hcl_genes;
		protected boolean hcl_samples;
		protected GeneralInfo(){
			super();
		}        
		public String getMethodName() {
			return hcl ? HCLGUI.GeneralInfo.getMethodName(hcl_method) : "no linkage";
		}

	}

	private static String getUniqueFileID() {
		Date now = new Date();
		String dateString = now.toString();

		SimpleDateFormat formatDt = new SimpleDateFormat("MMM_dd_yy_HHmmssSSS");
		dateString = formatDt.format(now);
		//System.out.println(" 2. " + dateString);
		return dateString;
	}
}
