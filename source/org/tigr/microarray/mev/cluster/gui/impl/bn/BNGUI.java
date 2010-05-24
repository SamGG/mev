/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Aug 30, 2005
 * @author Others
 * Modified by
 * @author Raktim
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.systemsbiology.gaggle.core.datatypes.Interaction;
import org.systemsbiology.gaggle.core.datatypes.Network;
import org.tigr.microarray.mev.HistoryViewer;
import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.annotation.IAnnotation;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.impl.bn.BNClassificationEditor.BNClassTableModel;
import org.tigr.microarray.mev.cluster.gui.impl.bn.algs.Cyclic;
import org.tigr.microarray.mev.cluster.gui.impl.bn.getInteractions.GetInteractionsModule;
import org.tigr.microarray.mev.cluster.gui.impl.bn.prepareXMLBif.PrepareXMLBifModule;
import org.tigr.microarray.mev.cluster.gui.impl.bn.prepareXMLBif.SifToXMLBif;
import org.tigr.microarray.mev.cluster.gui.impl.lm.LMGUI;
import org.tigr.microarray.mev.resources.PipelinedAnnotationsFileDefinition;
import org.tigr.microarray.mev.resources.ISupportFileDefinition;
import org.tigr.microarray.mev.resources.SupportFileAccessError;
import org.tigr.util.StringSplitter;
import org.xml.sax.SAXException;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;

public class BNGUI implements IClusterGUI, ActionListener {

	HashMap<String, String> probeIndexAssocHash = new HashMap<String, String>();
	HistoryViewer wekaOutputViewer;
	LMBNViewer fileViewer, badNetSeedViewer;
	IData data;
	IFramework framework;
	private JDialog resultFrame;
	private JFrame mainFrame;
	private JCheckBox finalThreshBox;
	private JTextField confThreshField;
	private JButton updateNetwork;

	private String basePath, tmpPath, resultPath;
	private String evalStr = null;
	private String XmlBifStr = null;
	private String evalStrs[] = null;
	private String XmlBifStrs[] = null;
	private Vector<String> networkFiles = new Vector<String>();
	private String bootNetFile = null;
	private Vector<String> interactionsfinal = null;
	private String finalBootFile = null;
	private ArrayList<SimpleGeneEdge> networkSeedEdgeList;
	private ArrayList<String> unMappedNetworkSeedEdgeList;

	private static ProgressMonitor pbar;
	private static int counter = 0;
	private static String pBarMsg = "Bn Module Initializing";

	//Start new structures
	private File labelFile;
	private BNClassTableModel kModel;
	private boolean isBootstraping;
	private int numIterations;
	private String numBin;
	private int numStates;
	private String sType;
	private String sAlgorithm;
	private String numParents;
	private int kfold;
	private boolean useArc;
	private int numClasses;
	private float confThreshold;
	private static boolean cancel = false;
	//TODO
	// add function in GUI to get value
	//private boolean netSeedasFixedNet = false;
	//End New structures

	public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {

		this.framework = framework;
		mainFrame = (JFrame)(framework.getFrame());
		data = framework.getData();
		Experiment exp = data.getExperiment();
		//exp.getGeneIndexMappedToData(row);
		ClusterRepository repository = framework.getClusterRepository(Cluster.GENE_CLUSTER);

		String chipType = null;
		String species = null;
		Vector<ISupportFileDefinition> defs = new Vector<ISupportFileDefinition>();

		BNSupportDataFile bnSuppFileHandle = null;

		// If annotations are not loaded all bets are off
		if (framework.getData().isAnnotationLoaded()) {
			chipType = framework.getData().getChipAnnotation().getChipType();
			species = framework.getData().getChipAnnotation().getSpeciesName();
			bnSuppFileHandle = new BNSupportDataFile(species, chipType);
			defs.add(bnSuppFileHandle);
		} else if (chipType == null || species == null)  {
			JOptionPane.showMessageDialog(
					framework.getFrame(), 
					"Organism and/or Array information unavailable",
					"Aborting execution...", JOptionPane.ERROR_MESSAGE);
			return null;
		} else {
			JOptionPane.showMessageDialog(
					framework.getFrame(), 
					"Annotation unavailable",
					"Aborting execution...", JOptionPane.ERROR_MESSAGE);
			return null;
		}

		// Eleanor code ??
		Hashtable<String, Vector<String>> speciestoarrays = null;
		PipelinedAnnotationsFileDefinition aafd = new PipelinedAnnotationsFileDefinition();
		defs.add(aafd);
		try {
			Hashtable<ISupportFileDefinition, File> supportFiles = framework.getSupportFiles(defs, true);

			File speciesarraymapping = supportFiles.get(aafd);
			speciestoarrays = aafd.parseAnnotationListFile(speciesarraymapping);

		} catch (SupportFileAccessError sfae) {
			sfae.printStackTrace();
			return null;
		} catch (IOException ioe) {
			speciestoarrays = null;
			ioe.printStackTrace();
			return null;
		}

		final BNInitDialog dialog = new BNInitDialog(
				framework, repository, 
				framework.getData().getFieldNames(),
				framework.getResourceManager(),
				species, 
				chipType, 
				speciestoarrays);
		if(dialog.showModal() != JOptionPane.OK_OPTION)
			return null;

		if(dialog.isNone()){
			return null;
		}

		if(dialog.getSelectedCluster().getIndices().length > BNConstants.MAX_GENES) {
			JOptionPane.showMessageDialog(framework.getFrame(), 
					"Cluster size exceeds max gene limit of " + 
					BNConstants.MAX_GENES + 
					". Please reduce cluster size.", "Error!", 
					JOptionPane.ERROR_MESSAGE);
			LMGUI.done = false;
			return null;
		}

		// Collect Class Info
		BNClassificationEditor bnEditor = new BNClassificationEditor(
				framework, false, dialog.getNumberClass(), 
				dialog.getBaseFileLocation());

		if (dialog.getNumberClass() > 1)
			if(bnEditor.showModal(true) != JOptionPane.OK_OPTION)
				return null;

		//init new structures
		this.labelFile = bnEditor.getLabelFile();
		this.kModel = bnEditor.getClassTableModel();
		this.basePath = dialog.getBaseFileLocation();
		this.tmpPath = this.basePath+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP;
		this.resultPath = this.basePath+BNConstants.SEP+BNConstants.RESULT_DIR+BNConstants.SEP;
		this.isBootstraping = dialog.isBootstrapping();
		this.numIterations = dialog.getNumIterations();
		this.numBin = String.valueOf(dialog.getNumberBin());
		this.numStates = dialog.getNumberBin();
		this.sType = dialog.getScoreType();
		this.sAlgorithm = dialog.getAlgorithm();
		this.numParents = dialog.numParents();
		this.kfold = dialog.getKFolds();
		this.useArc = dialog.useArcRev();
		this.numClasses = dialog.getNumberClass();
		this.confThreshold = dialog.getConfThreshold();
		//this.label = new String[data.getFeaturesCount()];
		//End New structures

		// Class dialog is closed.
		bnEditor.dispose();

		// Init Progress Bar
		pBarInit("Bayesian Network search", "Initializing . . .");
		try {
			Thread.sleep(900);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Map cluster genes to annotation, writes mapped Gb Acc to list.txt
		try {
			updatePBarProgress("Checking tmp folder", counter+5);
			Useful.cleanUpDir(this.tmpPath);

			updatePBarProgress("Mapping Genes to UID", counter+10);
			this.probeIndexAssocHash = Useful.converter(
					dialog.getSelectedCluster(),
					framework,
					dialog.getBaseFileLocation());
			if(cancel) return null;

			if(this.probeIndexAssocHash == null){
				throw new Exception("Error in mapping Unique identifiers to Accession");
			}
			if(this.probeIndexAssocHash.size() == 0){
				throw new Exception("Error in mapping Unique identifiers to Accession");
			}
		} catch(Exception e) {
			JOptionPane.showMessageDialog(
					framework.getFrame(), 
					e.getMessage(), 
					"Error!", JOptionPane.ERROR_MESSAGE);
			LMGUI.done = false;
			disposePBar();
			return null;
		}

		updatePBarCounter(counter+10);

		try {

			///////////////////////////////////////////////////
			//  * Network seed option used, load and validate
			//  * nodes and edges
			//////////////////////////////////////////////////
			if (dialog.useNetworkSeed() && 
					(dialog.useNetworkSeedWithoutPrior() 
							|| dialog.useNetworkSeedForCptOnly()
							|| dialog.useNetworkSeedWithPrior())
			) {

				updatePBarMsg("Mapping and validating Network seed");
				updatePBarCounter(counter+10);

				// Load Seed File
				ArrayList<String> netEdges;
				netEdges = loadNetSeedFile(dialog.getNetSeedUID(), dialog.getNetSeedFileLoc());
				System.out.println("Netseed file loaded");
				// Validate Seed File by Mapping. Gives a list of SimpleGeneEdges
				networkSeedEdgeList = mapNetSeedEdgesToSimpleEdges(
						dialog.getNetSeedUID(), 
						netEdges, 
						dialog.getSelectedCluster().getIndices());
				System.out.println("Netseed interactions mapped and validated");
				System.out.println(networkSeedEdgeList.size() + " Netseed edges mapped");
				System.out.println(unMappedNetworkSeedEdgeList.size() + " Netseed edges un-mapped");

				//Check for DAG and not Cyclic network see
				if (Cyclic.isCyclic(networkSeedEdgeList)) {
					throw new NotDAGException("Cycles detected in the loaded network!\n" +
							dialog.getNetSeedFileLoc());
				}

				// create the property file
				String propFile = this.tmpPath+BNConstants.XML_BIF_MODULE_FILE;
				Useful.createXmlBifPropFile(propFile, numClasses, numStates);
			}
			if(cancel) return null;
			///////////////////////////////////////////////////
			//  * Bypass LM Prior altogether when using 
			//  * exclusive network seed
			//  * if not used as priors just learn CPT for the net
			//////////////////////////////////////////////////
			if (dialog.useNetworkSeedWithoutPrior()) {
				System.out.println("<-------- Net witn Net seed Only ------->");
				do_NetworkSeedWithoutPrior(dialog.getSelectedCluster(), false);
				if(cancel) return null;
			} 
			else if (dialog.useNetworkSeedForCptOnly()) {
				System.out.println("<-------- Net For CPT Only ------->");
				System.out.println("CPT only status " + true);
				do_NetworkSeedWithoutPrior(dialog.getSelectedCluster(), true);
				if(cancel) return null;
			}
			///////////////////////////////////////////////////
			//  * Only LM Priors OR
			//  * Full Network seed with LM priors
			//////////////////////////////////////////////////
			else 
			{
				ArrayList<SimpleGeneEdge> interactions;
				/////////////////////////////////////////////
				// * All other options include LM so do that 
				// * common part here
				/////////////////////////////////////////////

				//pbar.setNote("Doing Literature Search..");
				updatePBarMsg("Initiating Literature Search");

				interactions = do_lit_miningNetwork(dialog);

				///////////////////////////////////////////////////
				//  * Create net seed edges and merge with LM edges
				//  * prepare the Priors bif file
				//////////////////////////////////////////////////
				if (dialog.useNetworkSeedWithPrior()) {
					//TODO
					System.out.println("<-------- Net seed With Priors ------->");
					pbar.setMaximum(pbar.getMaximum() + (3*5));
					do_NetworkSeedWithPrior(interactions);
					if(cancel) return null;
				}
				else {	
					//plain old LM based prior
					//Generates prior info for Weka
					System.out.println("<-------- Net With Priors ------->");
					pbar.setProgress(3);
					pbar.setNote("Generating Priors");
					//Thread.sleep(3000);
					prepareXMLBifFile(dialog.getBaseFileLocation());
					if(cancel) return null;
				}

				updatePBarCounter(counter+2);
				updatePBarProgress("Network Search", counter+10);
				///////////////////////////////////////////////////
				//  * Common code for LM only or LM with Net Seed
				//  * 
				//////////////////////////////////////////////////
				onOk(
						dialog.getSelectedCluster(), 
						labelFile, kModel, isBootstraping, 
						numBin, numClasses, sType, 
						sAlgorithm, numParents, kfold, 
						useArc, numIterations, confThreshold
				);
				if(cancel) return null;
				// About to create viewers
				updatePBarProgress("Creating Viewers", counter+5);
			}
		} 
		catch(OutOfMemoryError ofm){
			disposePBar();
			System.out.println("Out of Memory. Aborting...");
			JOptionPane.showMessageDialog(
					framework.getFrame(), 
					ofm.getMessage() + "\n Out of Memory", "Error - Out of Memory. Aborting!", 
					JOptionPane.ERROR_MESSAGE
			);
			return null;
		} catch (NullArgumentException e) {
			disposePBar();
			JOptionPane.showMessageDialog(
					framework.getFrame(), 
					e.getMessage() , 
					"Bad Arg(s). Aborting!", 
					JOptionPane.ERROR_MESSAGE
			);
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			disposePBar();
			JOptionPane.showMessageDialog(
					framework.getFrame(), 
					e.getMessage() , 
					"IO Error. Aborting!", 
					JOptionPane.ERROR_MESSAGE
			);
			e.printStackTrace();
			return null;
		} catch (InterruptedException e) {
			disposePBar();
			JOptionPane.showMessageDialog(
					framework.getFrame(), 
					e.getMessage() , 
					"Error, Aborting!", 
					JOptionPane.ERROR_MESSAGE
			);
			e.printStackTrace();
			return null;
		} catch (SAXException e) {
			disposePBar();
			JOptionPane.showMessageDialog(
					framework.getFrame(), 
					"Error processing Weka Bif DOM", 
					"XML parsing Error", 
					JOptionPane.ERROR_MESSAGE
			);
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			disposePBar();
			JOptionPane.showMessageDialog(
					framework.getFrame(), 
					e.getMessage() , 
					"Error, Aborting!", 
					JOptionPane.ERROR_MESSAGE
			);
			e.printStackTrace();
			return null;
		}

		//Added to record the Weka output for Observed BN analysis
		wekaOutputViewer = new HistoryViewer();
		wekaOutputViewer.addHistory(evalStr);

		//Common to all options
		GeneralInfo info = new GeneralInfo();
		if(dialog.isPPI()){
			info.prior="PPI";
		}
		if(dialog.isLit()){
			info.prior="Literature Mining";
		}
		if (dialog.isKEGG()) {
			info.prior="KEGG";
		}
		if(dialog.isBoth()){
			info.prior="LM & PPI";
		}
		if(dialog.isLitAndKegg()){
			info.prior="LM & KEGG";
		}
		if(dialog.isPpiAndKegg()){ 
			info.prior="PPI & KEGG";
		}
		if(dialog.isAll()){
			info.prior="LM, PPI & KEGG";
		}

		if(dialog.useGoTerm()){
			info.useGoTerms="Use GO Terms";
		}

		info.algorithm=dialog.getAlgorithm();
		info.numBin=dialog.getNumberBin();
		info.numClass=dialog.getNumberClass();
		info.numParents=dialog.numParents();
		info.numGene=(dialog.getSelectedCluster()).getIndices().length;
		info.kFolds = dialog.getKFolds();
		info.score = dialog.getScoreType();

		fileViewer = createLMBNViewer(this.networkFiles);
		DefaultMutableTreeNode node;
		if (unMappedNetworkSeedEdgeList.size() > 0) {
			badNetSeedViewer = new LMBNViewer(unMappedNetworkSeedEdgeList);
			node = createResultTree(exp, fileViewer, badNetSeedViewer, wekaOutputViewer, info);
		} else {
			node = createResultTree(exp, fileViewer, wekaOutputViewer, info);
		}
		disposePBar();
		return node;
	}

	/* Raktim - Network Seed Related Functions */

	ArrayList<SimpleGeneEdge> do_lit_miningNetwork(BNInitDialog dialog) 
	throws OutOfRangeException, IOException, NullArgumentException, InterruptedException {

		String kegg_sp = dialog.getKeggSpecies();
		if(kegg_sp != null) kegg_sp = kegg_sp.trim();
		else kegg_sp = "na";

		//Build Property file for Weka Params
		updatePBarProgress("Building Property File", counter+5);

		Useful.buildPropertyFile(dialog.isLit(),dialog.isPPI(),dialog.isKEGG(), 
				dialog.isBoth(), dialog.isLitAndKegg(), 
				dialog.isPpiAndKegg(), dialog.isAll(),
				dialog.useGoTerm(),dialog.getBaseFileLocation(),kegg_sp,
				numClasses, numStates);

		//System.out.println(dialog.getBaseFileLocation());
		updatePBarProgress("Getting LM interactions", counter+5);
		ArrayList<SimpleGeneEdge> interactions = null;
		interactions = literatureMining(
				dialog.isLit(), dialog.isPPI(), dialog.isKEGG(), 
				dialog.isBoth(), dialog.isLitAndKegg(), 
				dialog.isPpiAndKegg(), dialog.isAll(),
				dialog.getBaseFileLocation(), this.data
		);
		if(interactions != null) {
			//Display warning if too many interactions are found.
			if(interactions.size() > 50) {
				if (JOptionPane.showConfirmDialog(framework.getFrame(),
						"Too many interactions found. \n " +
						"The process might run out of memory! \n " +
						"Do you want to continue ? ", 
						"Interaction found: " + interactions.size() + "!",
						JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION
				) {
					//pgPanel.dispose();
					//pbar.setProgress(4);
					disposePBar();
					return null;
				} 
			}
		} else {
			disposePBar();
			return null;
		}
		return interactions;
	}

	/**
	 * 
	 * @param interactions
	 * @throws NullArgumentException
	 * @throws NotDAGException
	 * @throws InterruptedException 
	 * @throws IOException 
	 * 
	 * UNUSED
	 */
	void do_usePartialNetworkSeed(ArrayList<SimpleGeneEdge> interactions) 
	throws NullArgumentException, NotDAGException, InterruptedException, IOException {

		// Get DAG from LM edges
		updatePBarProgress("Creating DAG from LM interactions", counter+5);
		ArrayList<SimpleGeneEdge> lmDirEdges = PrepareXMLBifModule.getDAGFromUndirectedGraph(interactions);
		System.out.println("DAG created from LM edges");

		// Write a function to merge netEdges & lmDirEdges
		updatePBarProgress("Merging LM and seed Network edges", counter+5);
		ArrayList<SimpleGeneEdge> mergedInteractions = UsefulInteractions.mergeInteractions(
				networkSeedEdgeList, lmDirEdges, true);
		System.out.println("Net Seed edges merged with LM edges");

		// Write into Bif file for weka
		updatePBarProgress("Creating Priors Bif File", counter+5);
		ArrayList<String> nodeNames = Useful.readNamesFromFile(this.tmpPath+BNConstants.OUT_ACCESSION_FILE);
		SifToXMLBif.createXMLBifGivenSifFile(
				mergedInteractions, nodeNames, false, 
				numStates, numClasses, 
				this.tmpPath+BNConstants.BIF_RESULT_FILE
		);

		updatePBarProgress("Writing Seed Network", counter+5);
		String fname_seed = writeUserSeedNetwork();
		networkFiles.add(0, fname_seed);
	}

	/**
	 * Network seed is used as the prior along with LM prior to
	 * construct the starting network. In case there is conflict
	 * in interactions between the 2 sources (same interaction in
	 * both but diff direction etc), the net seed overrides the LM
	 * interaction.
	 * 
	 * @param interactions
	 * @throws NullArgumentException
	 * @throws NotDAGException
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	void do_NetworkSeedWithPrior(ArrayList<SimpleGeneEdge> interactions) 
	throws NullArgumentException, NotDAGException, InterruptedException, IOException {

		// Get DAG from LM edges
		updatePBarProgress("Generating DAG from LM interactions", counter+5);
		ArrayList<SimpleGeneEdge> lmDirEdges = PrepareXMLBifModule.getDAGFromUndirectedGraph(interactions);
		System.out.println("DAG created from LM edges");

		// a function to merge netEdges & lmDirEdges
		updatePBarProgress("Merging LM and Seed Network interactions", counter+5);
		ArrayList<SimpleGeneEdge> mergedInteractions = UsefulInteractions.mergeInteractions(
				networkSeedEdgeList, lmDirEdges, true);
		System.out.println("Net Seed edges merged with LM edges");

		// Write into Bif file for weka
		updatePBarProgress("Generating XML Bif File", counter+5);
		ArrayList<String> nodeNames = Useful.readNamesFromFile(this.tmpPath+BNConstants.OUT_ACCESSION_FILE);
		SifToXMLBif.createXMLBifGivenSifFile(
				mergedInteractions, nodeNames, false, 
				numStates, numClasses, 
				this.tmpPath+BNConstants.BIF_RESULT_FILE
		);

		updatePBarProgress("Writing Seed Network", counter+5);
		String fname_seed = writeUserSeedNetwork();
		networkFiles.add(0, fname_seed);
	}

	/**
	 * Use net seed either without prior where the supplied net would
	 * be used as the starting net to learn the final structure 
	 * 
	 * OR
	 * 
	 * If CPT only option is used, the network is not leaned and used as
	 * the final network and only the CPT is leaner for the given net.
	 * 
	 * @param cl
	 * @param asFixedNetForCptOnly
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws NullArgumentException 
	 * @throws Exception
	 */
	void do_NetworkSeedWithoutPrior(Cluster cl, boolean asFixedNetForCptOnly) 
	throws InterruptedException, NullArgumentException, IOException, Exception  {
		// Learn structure from Fixed File
		String[] label = saveToFile(kModel, numClasses, data.getFeaturesCount(), this.tmpPath+"label");
		if(labelFile != null) {
			saveToFile(kModel, numClasses, labelFile);
		}

		//Writes out observations in terms of UID & class groups
		// * E.g. Ref-Seq class1 class1 class2 class2
		updatePBarProgress("Mapping and encoding groups", counter+10);
		saveWekaData(cl, framework, label, this.tmpPath);
		// The following function does the following
		// (1). Discretize (2). Replace NAs (3). create ARFF file for Weka
		updatePBarProgress("Dicretizing Data and making ARFF file", counter+10);
		Properties props = PrepareArrayDataModule.prepareArrayData(
				this.tmpPath+"wekaData", 
				numBin, 
				isBootstraping, 
				numIterations, 
				numClasses);
		//Properties props = tranSaveWeka(isBootstraping, numBin, numClasses, numIterations, basePath+BNConstants.SEP+BNConstants.TMP_DIR);
		String bifFile = this.tmpPath+BNConstants.BIF_RESULT_FILE;
		String outArffFileName = this.tmpPath+"outExpression.arff";
		String namesFileName = this.tmpPath+BNConstants.OUT_ACCESSION_FILE;

		// Create new bif File from net seed
		updatePBarProgress("Generating XML Bif File", counter+10);
		PrepareXMLBifModule.createXMLBifFromSimpleGeneEdge(
				namesFileName, 
				bifFile,
				this.tmpPath+BNConstants.XML_BIF_MODULE_FILE, 
				networkSeedEdgeList);

		// Run Weka Model
		updatePBarProgress("Evaluating Baysian Model", counter+10);
		BayesNet bnNet = new BayesNet();	
		// TODO 
		if(asFixedNetForCptOnly) {
			// From fixed file
			evalStr = Evaluation.evaluateModel(
					bnNet, 
					Useful.getWekaArgsArrayForFixedFile(outArffFileName, bifFile, kfold)
			);
		} else {
			// or use network as priors to learn structure
			evalStr = Evaluation.evaluateModel(
					bnNet, 
					Useful.getWekaArgsArray(
							outArffFileName, bifFile, sAlgorithm, 
							useArc, numParents, sType, kfold
					)
			);
		}
		bnNet.estimateCPTs();
		XmlBifStr = bnNet.toXMLBIF03();

		// Write network from Network seed
		updatePBarProgress("Generating Cytoscape File", counter+10);
		String fname_seed = writeUserSeedNetwork();

		// Write network & Cpt file leaned from Weka
		updatePBarProgress("Writing Network Files", counter+10);
		networkFiles.add(0, fname_seed);

		// BN Observed
		String obsNetFile = this.resultPath + Useful.getUniqueFileID()+ sAlgorithm + "_" + sType + "_";
		String obsCptFile = obsNetFile + "CPT.xml";
		writeCptFile(XmlBifStr, obsCptFile);
		obsNetFile = writeWekaNetwork(obsNetFile, XmlBifStr, obsCptFile);
		networkFiles.add(0,obsNetFile);

		// If Bootstrap
		if(isBootstraping) {
			do_bootStrapNetworks(props);
		}
		if(cancel) return;
		updatePBarProgress("Launching Cytoscape", pbar.getMaximum()-1);
		//disposePBar();
		pbar.close();
		CytoscapeWebstart.onWebstartCytoscapeBN(networkFiles);
	}


	/**
	 * Reads a seed file and creates edges list
	 * Expected format: A	\t	B
	 * User has already selected the UID type (probe_id or gene symbol or acc# etc) 
	 * @param netSeedUID
	 * @param netSeedFileLoc
	 * @return
	 * @throws Exception 
	 */
	private ArrayList<String> loadNetSeedFile(String netSeedUID, String netSeedFileLoc) 
	throws IOException {
		File netSeedFile = new File(netSeedFileLoc);
		if (!netSeedFile.exists()) 
			throw new IOException("File not found: " + netSeedFileLoc);

		BufferedReader buff = new BufferedReader(new FileReader(netSeedFile)); 
		String line = new String();   

		ArrayList<String> edges = new ArrayList<String>();
		while ((line = buff.readLine()) != null) {
			String tmp[] = line.split("\t");
			if (tmp.length != 2) {
				edges = null;
				throw new IOException(line + "\n in network seed file, is in invalid format");
			}
			edges.add(tmp[0].trim()+"-"+tmp[1].trim());
		}
		return edges;
	}

	/**
	 * 
	 * @return
	 * @throws NullArgumentException
	 * @throws IOException
	 */
	private String writeUserSeedNetwork() throws NullArgumentException, IOException {
		String fname_seed = "";
		if(!data.isAnnotationLoaded()) {
			fname_seed = Useful.getUniqueFileID() +"_"+ "Seed.sif";
			//System.out.println("fname_cyto " + fname_cyto);
			//System.setProperty("LM_ONLY", fname_seed);
			UsefulInteractions.writeSifFileUndir(
					networkSeedEdgeList, 
					this.resultPath + fname_seed);
		} else {
			fname_seed = Useful.getUniqueFileID() +"_"+ "Seed.xgmml";
			//System.setProperty("LM_ONLY", fname_seed);
			if(probeIndexAssocHash != null ) {
				if (probeIndexAssocHash.size() <= 0) {
					//System.out.println("probeIndexAssocHash Size: " + probeIndexAssocHash.size());
					//System.out.println("First Entry : " + probeIndexAssocHash.entrySet().toArray()[0]);
					throw new NullArgumentException("Given Probe-Index Hash was empty!");
				}
			} else {
				throw new NullArgumentException("Given Probe-Index Hash was null!");
			}
			UsefulInteractions.writeXgmmlFileUndir(
					networkSeedEdgeList, 
					this.resultPath + fname_seed, 
					probeIndexAssocHash, 
					data);
		}
		return this.resultPath + fname_seed;
	}

	/**
	 * 
	 * @param obsNetFile
	 * @param cptString
	 * @param obsCptFile
	 * @throws SAXException
	 * @throws IOException
	 * @throws NullArgumentException
	 */
	private String writeWekaNetwork(String obsNetFile, String cptString, String obsCptFile) throws SAXException, IOException, NullArgumentException {
		if(!data.isAnnotationLoaded()) {
			//Create sif file
			obsNetFile += ".sif";
			FileOutputStream fos = new FileOutputStream(obsNetFile);
			PrintWriter pw = new PrintWriter(fos, true);
			//Move from evalStr to CPT Bif
			FromWekaToSif.fromWekaBifToSif(cptString, pw);
			fos.flush();fos.close();pw.close();
		}
		else {
			//create xgmml file
			obsNetFile += ".xgmml";
			//Should move completely to Bif CPT format
			FromWekaToSif.fromWekaBifToXgmml(
					true, cptString, obsNetFile, 
					obsCptFile, probeIndexAssocHash, data
			);
		}
		return obsNetFile;
	}

	/**
	 * 
	 * @param XmlBifStr
	 * @param obsCptFile
	 * @throws IOException
	 */
	private void writeCptFile(String XmlBifStr, String obsCptFile) throws IOException {
		FileOutputStream fos;
		PrintWriter pw;	
		fos = new FileOutputStream(obsCptFile);
		pw = new PrintWriter(fos, true);
		pw.print(XmlBifStr);
		pw.close();
		fos.close();
	}

	/**
	 * 
	 * @param netSeedUID
	 * @param netEdges
	 * @param probeIndices 
	 * @return
	 * @throws Exception 
	 */
	private ArrayList<SimpleGeneEdge> mapNetSeedEdgesToSimpleEdges(String netSeedUID,
			ArrayList<String> netEdges, int[] probeIndices) throws Exception {

		unMappedNetworkSeedEdgeList = new ArrayList<String>();
		//MevAnnotation annotation;
		Hashtable<String, MevAnnotation> _tmpTable = new Hashtable<String, MevAnnotation>();
		ArrayList<SimpleGeneEdge> edgeList = new ArrayList<SimpleGeneEdge>();
		Iterator<String> it = netEdges.iterator();
		for(int row = 0; it.hasNext(); row++){
			String _tmp = (String)it.next();
			String __tmp[] = _tmp.split("-");

			MevAnnotation annotationFrm = null, annotationTo = null;
			if(_tmpTable.get(__tmp[0]) == null) {
				if ((annotationFrm = (MevAnnotation) mapAnnotationGivenIdices(
						__tmp[0], probeIndices, netSeedUID)
				) != null) {
					_tmpTable.put(__tmp[0], annotationFrm);
				} else {
					// Un-mappable node hence un-usable edge
					// record and move on;
					unMappedNetworkSeedEdgeList.add(_tmp);
					continue;
				}
			}
			else
				annotationFrm = (MevAnnotation) _tmpTable.get(__tmp[0]);

			if(_tmpTable.get(__tmp[1]) == null)
				if ((annotationTo = (MevAnnotation) mapAnnotationGivenIdices(
						__tmp[1], probeIndices, netSeedUID)
				) != null) {
					_tmpTable.put(__tmp[1], annotationTo);
				} else {
					// Un-mappable node hence un-usable edge
					// record and move on;
					unMappedNetworkSeedEdgeList.add(_tmp);
					continue;
				}
			else
				annotationTo = (MevAnnotation) _tmpTable.get(__tmp[1]);

			// Default from and to labels are GB Acc#
			// edgeList.add(new SimpleGeneEdge(__tmp[0], __tmp[1], annotationFrm, annotationTo));
			edgeList.add(new SimpleGeneEdge(
					annotationFrm.getGenBankAcc(), 
					annotationTo.getGenBankAcc(), 
					annotationFrm, annotationTo)
			);
		}
		//System.out.println("Mapped NetSeed Interactions: " + edgeList.size());
		//System.out.println("Un-Mapped NetSeed Interactions: " + unMappedNetworkSeedEdgeList.size());
		_tmpTable = null;
		if (edgeList == null)
			throw new Exception("No seed network nodes mapped to selected cluster nodes." +
					"\nUID used: " + netSeedUID);
		if (edgeList.size() == 0)
			throw new Exception("No seed network nodes mapped to selected cluster nodes." +
					"\nUID used: " + netSeedUID);
		return edgeList; 
	}

	/**
	 * 
	 * @param value
	 * @param probeIndices
	 * @param netSeedUID
	 * @return
	 */
	private IAnnotation mapAnnotationGivenIdices(String value, int[] probeIndices, String netSeedUID) {
		for(int i=0; i < probeIndices.length; i++) {
			MevAnnotation annotation = 
				(MevAnnotation) data.getSlideDataElement(0,probeIndices[i]).getElementAnnotation();
			String _tmp[] = annotation.getAttribute(netSeedUID);
			if (_tmp == null)  {
				System.out.println("MevAnnotation not found for: "+ value);
				return null;
			}
			for(int ii=0; ii < _tmp.length; ii++) {
				if(_tmp[ii].trim().toUpperCase().equals(value.trim().toUpperCase())) {
					System.out.println("MevAnnotation found for: "+ value + " Gene Name: " + 
							annotation.getGeneSymbol());
					return annotation;
				}
			}
		}
		return null;
	}

	//New Support Function
	/**
	 * Core function to run BN with weka on the selected cluster
	 * @param cl
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws NullArgumentException 
	 */
	protected void onOk(Cluster cl, File labelFile, BNClassTableModel kModel, 
			boolean isBootstraping, String numBin, int numClasses, 
			String sType, String sAlgorithm, String numParents, 
			int kfold, boolean useArc, int numIterations, 
			float confThreshold) 
	throws InterruptedException, IOException, SAXException, NullArgumentException, Exception {

		String[] label = saveToFile(kModel, numClasses, data.getFeaturesCount(), this.tmpPath+"label");
		if(labelFile != null) {
			saveToFile(kModel, numClasses, labelFile);
		}

		updatePBarProgress("Encoding matrix by class labels", counter+5);
		updatePBarProgress("Discretizing Data and creating ARFF file", counter+5);

		saveWekaData(cl, framework, label, this.tmpPath);
		if(cancel) return;

		updatePBarProgress("Transposing matrix", counter+5);
		Properties props = tranSaveWeka(isBootstraping, numBin, numClasses, numIterations, this.tmpPath);
		if(cancel) return;

		String outarff = "outExpression.arff";
		//	WEKA on observed Data
		updatePBarProgress("Evaluating Model on Observed data", counter+5);

		String[] argsWeka = Useful.getWekaArgsArray(
				this.tmpPath+outarff, 
				this.tmpPath+BNConstants.BIF_RESULT_FILE,
				sAlgorithm, useArc, 
				numParents, sType, kfold
		);
		BayesNet bnNetOrg = new BayesNet();
		evalStr = Evaluation.evaluateModel(bnNetOrg, argsWeka);
		if(cancel) return;

		updatePBarProgress("Learning CPT for Observed Network", counter+5);

		bnNetOrg.estimateCPTs();
		XmlBifStr = bnNetOrg.toXMLBIF03();
		
		//LM Network
		String lmNetFile = this.resultPath + System.getProperty("LM_ONLY");
		networkFiles.add(0,lmNetFile);

		// BN Observed
		String obsNetFile = this.resultPath + Useful.getUniqueFileID()+ sAlgorithm + "_" + sType + "_";
		String obsCptFile = obsNetFile + "CPT.xml";

		//Write CPT file
		updatePBarProgress("Writing CPT file", counter+5);
		writeCptFile(bnNetOrg.toXMLBIF03(), obsCptFile);
		if(cancel) return;
		
		updatePBarProgress("Writting Network Files", counter+5);
		obsNetFile = writeWekaNetwork(obsNetFile, XmlBifStr, obsCptFile);
		if(cancel) return;
		
		networkFiles.add(0,obsNetFile);
		
		if(isBootstraping) {
			do_bootStrapNetworks(props);
		}
		if(cancel) return;
		disposePBar();
		CytoscapeWebstart.onWebstartCytoscapeBN(networkFiles);
	}

	/**
	 * 
	 * @param props
	 * @throws InterruptedException
	 * @throws Exception
	 */
	private void do_bootStrapNetworks(Properties props) throws InterruptedException, Exception{

		String outarff = "outExpression.arff";

		pbar.setMaximum(pbar.getMaximum() + (2*numIterations));
		updatePBarProgress("Starting Bootstrap", counter+2);
		
		//WEKA On bootstrapped data
		String outarffbase = props.getProperty("rootOutputFileName");
		String outarffext = ".arff";
		evalStrs = new String[numIterations];
		XmlBifStrs = new String[numIterations];
		int ctr=0;
		String[] argsWeka;
		BayesNet bnNetOrg;
		for(int i=0; i < numIterations; i++){
			//Previously created .arff files for bootstrap
			ctr++;
			updatePBarProgress("Bootstrap " + ctr + " of " + numIterations, counter+2);
			outarff = outarffbase + i + outarffext;
			argsWeka = Useful.getWekaArgsArray(
					this.tmpPath+outarff, 
					this.tmpPath+BNConstants.BIF_RESULT_FILE, 
					sAlgorithm, useArc, numParents, sType, kfold
			);

			bnNetOrg = new BayesNet();
			evalStrs[i] = Evaluation.evaluateModel(bnNetOrg, argsWeka);
			bnNetOrg.estimateCPTs();
			XmlBifStrs[i] = bnNetOrg.toXMLBIF03();

			if(pbar.isCanceled()) return;
			pbar.setProgress(5 + i + 1);
			//System.out.println("Bootstrap Itr: " + i);
			if(cancel) return;
		}
		updatePBarProgress("Creating Network from Bootstrap Models", counter+5);

		Hashtable<String, Integer> edgesTable = new Hashtable<String, Integer>();
		bootNetFile = createNetworkFromBootstraps(
				XmlBifStrs, evalStrs, numIterations, 
				outarffbase, sType, sAlgorithm, 
				kfold, numIterations, 
				confThreshold, edgesTable
		);
		if(cancel) return;
		
		updatePBarProgress("Launching Cytoscape..", pbar.getMaximum()-1);
		updatePBarCounter(pbar.getMaximum()+1);
		displayScrollPane(
				getScrollPanePanel(
						isBootstraping, sType, sAlgorithm, 
						kfold, numIterations, edgesTable)
		);

	}

	/**
	 * Window that allows iteration over bootstrap network
	 * @param panel
	 */
	public void displayScrollPane(JPanel panel){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		resultFrame = new JDialog(mainFrame, "Results from Weka", false);

		resultFrame.getContentPane().add(panel);
		resultFrame.pack();
		resultFrame.setLocation(
				(screenSize.width-panel.getSize().width)/2,
				(screenSize.height-panel.getSize().height)/2
		);
		resultFrame.setVisible(true);
	}

	/**
	 * Panel to create nets for different user given thresholds
	 * @return
	 */
	public JPanel getScrollPanePanel(
			boolean isBootstraping, final String sType, 
			final String sAlgorithm, final int kfold, 
			final int numIterations, 
			final Hashtable<String, Integer> edgesTable) 
	throws SAXException, IOException, NullArgumentException, Exception {
		if(isBootstraping){
			//System.out.println("BN BootStrap: " + bootNetFile);
			networkFiles.add(0, bootNetFile);
			updateNetwork = new JButton("Update Network");
			confThreshField = new JTextField("0.8");
			confThreshField.setPreferredSize(new Dimension(35, 10));

			finalThreshBox = new JCheckBox("Final");
			finalThreshBox.setBackground(Color.white);
			finalThreshBox.setFocusPainted(false);
		}

		// Debug Print File Names
		//System.out.println("Files to Show: " + networkFiles.size());
		//for(int i=0; i < networkFiles.size(); i++) {
		//System.out.println("File: " + networkFiles.get(i));
		//}
		//End Debug

		final JPanel evalPanel = new JPanel();
		evalPanel.setLayout(new BorderLayout());
		evalPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		// The evalStr is now shown in the Viewer
		if(isBootstraping){
			updateNetwork.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					String cptFile;
					try {
						cptFile = onUpdateNetwork(sType, sAlgorithm, kfold, numIterations, edgesTable);
					} catch (SAXException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(
								null, "Could not update network. Error in pasing XML doc", 
								"SAX parsing Error", 
								JOptionPane.ERROR_MESSAGE
						);
						resultFrame.dispose();
						return;
					} catch (IOException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(
								null, "Could not update network. Error in reading/writing file", 
								"IO Error", JOptionPane.ERROR_MESSAGE
						);
						resultFrame.dispose();
						return;
					} catch (NullArgumentException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(
								null, "Could not update network.", 
								"Error", JOptionPane.ERROR_MESSAGE
						);
						resultFrame.dispose();
						return;
					} catch (Exception e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(
								null, "Could not update network.", 
								"Error", JOptionPane.ERROR_MESSAGE
						);
						resultFrame.dispose();
						return;
					}
					// Try Cytoscape Broadcast
					if(!framework.isGaggleConnected()) {
						framework.requestGaggleConnect();					
					}
					if(!framework.isGaggleConnected()) {//if still not connected give up
						//TODO just write and file and display msg
						JOptionPane.showMessageDialog(
								null, "Could not connect to Gaggle", 
								"Error", JOptionPane.ERROR_MESSAGE
						);
						resultFrame.dispose();
						return;
					}
					try {
						broadcastNetworkGaggle(interactionsfinal, cptFile);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(
								null, "Error Using Gaggle Broadcast", 
								"Error", JOptionPane.ERROR_MESSAGE
						);
						e.printStackTrace();
						System.out.println("Dumping Content of probeIndexAssocHash");
						Set<String> sit = probeIndexAssocHash.keySet();
						Iterator it = sit.iterator();
						while(it.hasNext()) {
							String key = (String) it.next();
							System.out.println(
									"Key: "+ key + 
									" Value: " +probeIndexAssocHash.get(key)
							);	
						}
						if(finalThreshBox.isSelected())
							resultFrame.dispose();
						else
							resultFrame.show();
					}
				}
			});
		}
		if(isBootstraping) {
			evalPanel.add(finalThreshBox, BorderLayout.WEST);
			evalPanel.add(confThreshField, BorderLayout.CENTER);
			evalPanel.add(updateNetwork, BorderLayout.EAST);
		} else {
			//resultFrame.dispose();
		}
		return evalPanel;
	}

	/**
	 * Called when Update Network button is clicked
	 * Creates interactions & network files
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws NullArgumentException 
	 *
	 */
	protected String onUpdateNetwork(
			String sType, String sAlgorithm, 
			int kfold, int numIterations, 
			Hashtable<String, Integer> edgesTable) 
	throws SAXException, IOException, NullArgumentException, Exception {
		String outCPTBifXML = "";

		//To track last created network from bootstrap data starts with null
		Vector<String> interactionsPrefinal = null;
		//For lookup during gaggle broadcast and for creating final list of edges
		interactionsPrefinal = interactionsfinal;
		interactionsfinal = new Vector<String>();
		//To Remove edges below threshold
		float confThres = Float.parseFloat(confThreshField.getText().trim());

		//Remove Edges below threshold
		interactionsfinal = createInteractions(edgesTable, confThres, numIterations);

		//Fix the network learn the CPT
		String bifCpt = "";
		bifCpt = learnCPTFromFixedNetwork(interactionsfinal, kfold);

		String _bootNetFile = this.resultPath +
		Useful.getUniqueFileID()+ sAlgorithm + "_" + sType + "_" +
		"boot_result_"+numIterations+"_"+confThres;

		outCPTBifXML = _bootNetFile + "_CPT.xml";

		//Write network File
		_bootNetFile = writeWekaNetwork(_bootNetFile, bifCpt, outCPTBifXML);

		if(finalThreshBox.isSelected()) {
			//Create File & interaction adges
			//store the final thresh and the file name
			finalBootFile = _bootNetFile;
			networkFiles.add(0,finalBootFile);

			if(interactionsfinal.size() == 0) {
				JOptionPane.showMessageDialog(
						mainFrame, 
						"No valid network for selected threshold.\n " +
						"Will try to use last selected threshold.", 
						"Warning", JOptionPane.INFORMATION_MESSAGE
				);
				if(interactionsPrefinal != null) {
					if(interactionsPrefinal.size() > 0) {
						JOptionPane.showMessageDialog(
								mainFrame, "Last network not avaialble. Aborting.", 
								"Warning", JOptionPane.INFORMATION_MESSAGE
						);
						return null;
					}
				} else {
					JOptionPane.showMessageDialog(
							mainFrame, "Last network not avaialble. Aborting.", 
							"Warning", JOptionPane.INFORMATION_MESSAGE
					);
					return null;
				}
			}
			resultFrame.hide();
		}

		//Write CPT File final or otherwise
		writeCptFile(bifCpt, outCPTBifXML);
		return outCPTBifXML;
	}

	/**
	 * 
	 * @param kModel
	 * @param numClasses
	 * @param file
	 * @throws FileNotFoundException 
	 */
	private void saveToFile(BNClassTableModel kModel, int numClasses, File file) 
	throws FileNotFoundException {
		PrintWriter out = new PrintWriter(new FileOutputStream(file));
		for (int i = 0; i < kModel.getRowCount(); i++) {
			out.print(((Integer)(kModel.getValueAt(i, 0))).intValue());
			out.print("\t");
			for (int j = 1; j <= numClasses; j++) {
				if (((Boolean)(kModel.getValueAt(i, j))).booleanValue()) {
					out.print(j);
					//label[i]=(new Integer(j)).toString();
					break;
				}
			}
			//if (((Boolean)(kModel.getValueAt(i, numClasses + 1))).booleanValue()) {
			if (((Boolean)(kModel.getValueAt(i, numClasses))).booleanValue()) {
				//label[i]=(new Integer(-1)).toString();
				out.print(-1);
			}
			//out.print("\t");
			//for (int j = numClasses + 2; j < kModel.getColumnCount(); j++) {
			for (int j = numClasses + 1; j < kModel.getColumnCount(); j++) {
				out.print("\t");
				out.print(kModel.getValueAt(i, j));
			}
			out.print("\n");
		}
		out.flush();
		out.close();
	}

	/*
	 * Saves sample classification info in a file
	 */
	private String[] saveToFile(BNClassTableModel kModel, int numClasses, int numLabels, String fileName) {
		//System.out.println("saveToFile kModel Row# " + kModel.getRowCount());
		//System.out.println("numClasses " + numClasses);
		String[] label = new String[numLabels];
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream(new File(fileName)));
			for (int i = 0; i < kModel.getRowCount(); i++) {
				//System.out.println("label Row " + i+1);
				out.print(((Integer)(kModel.getValueAt(i, 0))).intValue());
				out.print("\t");
				for (int j = 1; j <= numClasses; j++) {
					if (((Boolean)(kModel.getValueAt(i, j))).booleanValue()) {
						out.print(j);
						label[i]=(new Integer(j)).toString();
						//System.out.println("label at " + i + " is " + label[i]);
						break;
					}
				}
				//if (((Boolean)(kModel.getValueAt(i, numClasses + 1))).booleanValue()) {
				if (((Boolean)(kModel.getValueAt(i, numClasses))).booleanValue()) {
					label[i]=(new Integer(-1)).toString();
					//System.out.println("label at " + i + " is " + label[i]);
					out.print(-1);
				}
				//out.print("\t");
				//for (int j = numClasses + 2; j < kModel.getColumnCount(); j++) {
				for (int j = numClasses + 1; j < kModel.getColumnCount(); j++) {
					out.print("\t");
					out.print(kModel.getValueAt(i, j));
				}
				out.print("\n");
			}
			out.flush();
			out.close();    
			return label;

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(
					framework.getFrame(), 
					"Error saving sample labels.  Labels not saved.", 
					"Save Error", JOptionPane.WARNING_MESSAGE
			);
			return null;
		}
	}

	/**
	 * Writes out observations in terms of UID & class groups
	 * E.g. Ref-Seq class1 class1 class2 class2
	 * @param cl
	 * @param frame
	 * @param path
	 * @throws FileNotFoundException 
	 */
	private void saveWekaData(Cluster cl, IFramework frame, String[] label, String path) 
	throws FileNotFoundException {
		int genes=cl.getIndices().length;
		//System.out.print(genes);
		IData data = frame.getData();
		int[] rows = new int[genes];
		rows = cl.getIndices();
		String[] accList=new String[genes]; 
		//PrintWriter out = new PrintWriter(new FileOutputStream(new File(basePath+"wekaData")));
		PrintWriter out = new PrintWriter(new FileOutputStream(new File(this.tmpPath+"wekaData")));

		String[] fieldNames = data.getFieldNames();
		String key;
		String val = new String(); 
		if(fieldNames == null)
			return;

		out.print("CLASS");
		out.print("\t");

		for (int i=0; i<data.getFeaturesCount(); i++) {  
			if(new Integer(label[i]).intValue()!=-1){
				out.print("class"+label[i]);
			}else
				out.print("-class");  
			out.print("\t");
		}
		out.print("\n");

		accList = convertFromFile(path);
		for (int i=0; i<rows.length; i++) {
			String s=data.getSlideDataElement(0,rows[i]).getFieldAt(0);
			if(s=="")
				out.print("gene"+(i+1));
			else
				out.print(accList[i]);
			for (int j=0; j<data.getFeaturesCount(); j++) {      		
				out.print("\t");
				out.print(Float.toString(data.getRatio(j,rows[i],IData.LOG)));
			}
			out.print("\n");
		}
		out.flush();
		out.close();
	}

	/**
	 * Discretize data for Weka evaluation
	 * @param binNum
	 * @param path
	 * @param bootStrap
	 * @param numIter
	 * @return
	 */
	public Properties tranSaveWeka(
			boolean isBootstraping, 
			String numBin, int numClasses, 
			int numIterations, 
			String path){
		return PrepareArrayDataModule.prepareArrayData(
				path+"wekaData", numBin, isBootstraping, 
				numIterations, numClasses
		); 
	}


	/**
	 * From an array of Weka evaluation string it creates a network that exceeds a confidence threshold
	 * Writes the network to a file and return the file name
	 * @param xmlBifStrs
	 * @param evalStrs 
	 * @param numItr
	 * @param outarffbase
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws NullArgumentException 
	 */
	private String createNetworkFromBootstraps(
			String[] xmlBifStrs, String[] evalStrs, 
			int numItr, String outarffbase, String sType, 
			String sAlgorithm, int kfold, int numIterations, 
			float confThreshold, Hashtable<String, Integer> edgesTable) 
	throws SAXException, IOException, NullArgumentException, Exception {

		String path = this.tmpPath;
		String fileName = path + outarffbase;

		//Create sif files for every output of the resampled WEKA evaluation
		//This can be done in memory but avoided by disk writes
		for(int i = 0; i < numItr; i++) {
			//System.out.println("Creating file: " + fileName+i+outarffext);
			FileOutputStream fos = new FileOutputStream(fileName+i+".sif");
			PrintWriter pw = new PrintWriter(fos, true);
			//FromWekaToSif.fromWekaToSif(evalStrs[i], pw);	
			//No* need to convert to xgmml as these files are just used to select
			//a netwrok from the bootstrap network for a threshold.
			//TODO Move to Bif CPT format from evalStr
			//FromWekaToSif.fromWekaToSif(evalStrs[i], pw, false);
			FromWekaToSif.fromWekaBifToSif(xmlBifStrs[i], pw);
			pw.flush();
			pw.close();
		}

		for(int i = 0; i < numItr; i++) {
			//System.out.println("Reading file: " + fileName+i+".sif");
			BufferedReader br = new BufferedReader(new FileReader(fileName+i+".sif"));
			String line = br.readLine();
			while (line != null) {
				//System.out.println(line);
				Integer count = (Integer)edgesTable.get(line.trim());
				if(count != null) {
					edgesTable.remove(line.trim());
					edgesTable.put(line.trim(), count + new Integer(1));
				}
				else {
					edgesTable.put(line.trim(), new Integer(1));
				}
				line = br.readLine();
			}
			br.close();
		}

		//Remove Edges below threshold
		Vector<String> interactions = new Vector<String>();
		interactions = createInteractions(edgesTable, confThreshold, numItr);

		//Fix the network learn the CPT
		String bifCpt = "";
		bifCpt = learnCPTFromFixedNetwork(interactions, kfold);

		//End Code block to learn CPTs
		String _bootNetFile = this.resultPath
		+ Useful.getUniqueFileID()+ sAlgorithm + "_" + sType + "_" 
		+ "boot_result_" + numIterations + "_" + confThreshold;
		String _bootCptFile = _bootNetFile + "_CPT.xml";

		//Write CPT file for confidence threshold
		writeCptFile(bifCpt, _bootCptFile);

		//Create Cytoscape File
		_bootNetFile = writeWekaNetwork(_bootNetFile, bifCpt, _bootCptFile);
		return _bootNetFile;
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	private String[] convertFromFile(String path){
		//String sep= System.getProperty("file.separator");    
		String filePath = path + BNConstants.OUT_ACCESSION_FILE; // Raktim - path incls tmp dir
		//String filePath = path+sep+"tmp"+sep+"list.txt";
		//System.out.println("convertFromFile(): " + filePath);
		String lineRead = "";
		Vector<String> store=new Vector<String>();
		String[] accList=null;
		try {
			File file = new File(filePath);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			while((lineRead = br.readLine()) != null) {
				store.add(lineRead);
			}
			accList=new String[store.size()];
			for(int i=0;i<store.size();i++){
				accList[i]=(String)store.get(i);
			}

		} catch(FileNotFoundException e){
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		}
		return accList;
	}

	/**
	 * Given a list of edges and its count in a hashtable along with a cut-off
	 * this return a list of edges that exceeds the cut-off over n iterations
	 * @param edges
	 * @param thresh
	 * @param itr
	 * @return
	 */
	protected Vector<String> createInteractions(Hashtable edges, float thresh, int itr) {
		Vector<String> _tmp = new Vector<String>();
		Enumeration enumerate = edges.keys();
		while(enumerate.hasMoreElements()){
			String edge = (String)enumerate.nextElement();
			Integer count = (Integer)edges.get(edge);
			float presence = count.floatValue()/itr;
			if(presence >= thresh){
				_tmp.add(edge);
			}
		}
		return _tmp;
	}

	/**
	 * Learns the CPT of a fixed network
	 * @param interactions
	 * @return
	 * @throws Exception
	 */
	private String learnCPTFromFixedNetwork(Vector<String> interactions, int kfold) throws Exception {
		String propsFile = this.tmpPath + BNConstants.XML_BIF_MODULE_FILE;
		PrepareXMLBifModule.createXMLBifFromList(propsFile, interactions);
		String bifFileFinal = this.tmpPath + BNConstants.OUT_XML_BIF_FILE_FINAL;
		BayesNet bnNet = new BayesNet();
		//Dataset
		String outarff = this.tmpPath + "outExpression.arff";

		//Run Weka Model
		Evaluation.evaluateModel(
				bnNet, 
				Useful.getWekaArgsArrayForFixedFile(outarff, bifFileFinal, kfold)
		);
		bnNet.estimateCPTs();
		return bnNet.toXMLBIF03();
	}

	/**
	 * Broadcasts a list of edges as a network to Cytoscape using Gaggle
	 * @param interacts a list of edges encoded with node labels and probe index id
	 * @param cptFile 
	 */
	protected void broadcastNetworkGaggle(Vector<String> interacts, String cptFile) {
		Vector<int[]> interactions = new Vector<int[]>();

		for(int j=0; j<interacts.size(); j++) {
			// Of the form XXXXXX pp XXXXXX
			String[] edgeLabels = interacts.get(j).split(" ");
			System.out.println("Encoding edge: " + edgeLabels[0] + " - " + edgeLabels[2]);
			int[] fromTo = new int[2];
			//Get index from hash map encoded into the form 
			//NM_23456 -> 1-Afy_X1234 where 1 is the probe index
			String tmp[] = probeIndexAssocHash.get(edgeLabels[0]).split("-");
			fromTo[0] = Integer.parseInt(tmp[0]);
			tmp = probeIndexAssocHash.get(edgeLabels[2]).split("-");
			fromTo[1] = Integer.parseInt(tmp[0]);
			interactions.add(fromTo);
		}
		Network nt = getGaggleNetwork(interactions, "pd", true, cptFile);
		System.out.println("Broadcasting " + nt.getName());
		framework.broadcastNet(nt);
	}

	/**
	 * Funtion to create Network for Gaggle Broadcast
	 * @author raktim
	 * @param interactions contains index of 2 probes representing source and target nodes
	 * @param type interaction type - pd, pp etc.
	 * @param directionals - true if graph is directed else otherwise
	 * @param title - name of the network, hacked to pass CPT file name
	 */
	public Network getGaggleNetwork(
			Vector<int[]> interactions, 
			String type, 
			boolean directionals, 
			String title
	) {

		Network nt = new Network();

		nt.setSpecies("");

		String[] allFields = data.getFieldNames();
		Hashtable<String, String[]> nodeAnnotations = new Hashtable<String, String[]>();

		for(int i=0; i<interactions.size(); i++) {
			String source, target;
			// if Class var
			if (interactions.get(i)[0] == 9999)
				source = "CLASS";
			else {
				source = data.getAnnotationList(
						AnnotationFieldConstants.GENBANK_ACC, 
						new int[]{interactions.get(i)[0]}
				)[0];
			}
			// if Class var
			if (interactions.get(i)[1] == 9999)
				target = "CLASS";
			else {
				target = data.getAnnotationList(
						AnnotationFieldConstants.GENBANK_ACC, 
						new int[]{interactions.get(i)[1]}
				)[0];
			}

			Interaction tempInt = new Interaction(source, target, type, directionals);

			nt.add(tempInt);
			
			// If source not yet in list
			if(!nodeAnnotations.containsKey(source)) {
				nodeAnnotations.put(source, new String[0]);
				// if Class var
				if(source.equals("CLASS")) {
					for(String field: allFields) {
						nt.addNodeAttribute(
								source, 
								field, 
								"CLASS"
						);
						//System.out.println(target + " " + field + " " + data.getElementAnnotation(interactions.get(i)[1], field)[0]);
					}
				} else {
					for(String field: allFields) {
						nt.addNodeAttribute(
								source, 
								field, 
								data.getElementAnnotation(
										interactions.get(i)[0], 
										field)[0]
						);
						//System.out.println(source + " " + field + " " + data.getElementAnnotation(interactions.get(i)[0], field)[0]);
					}
				}
			}
			
			// If target not yet in list
			if(!nodeAnnotations.containsKey(target)) {
				nodeAnnotations.put(target, new String[0]);
				// if Class var
				if(target.equals("CLASS")) {
					for(String field: allFields) {
						nt.addNodeAttribute(
								target, 
								field, 
								"CLASS"
						);
						//System.out.println(target + " " + field + " " + data.getElementAnnotation(interactions.get(i)[1], field)[0]);
					}
				} else {
					for(String field: allFields) {
						nt.addNodeAttribute(
								target, 
								field, 
								data.getElementAnnotation(
										interactions.get(i)[1], 
										field)[0]
						);
						//System.out.println(target + " " + field + " " + data.getElementAnnotation(interactions.get(i)[1], field)[0]);
					}
				}
			}
		}

		nt.setName(title);
		return nt;
	}
	//End New Support Function

	private DefaultMutableTreeNode createResultTree(
			Experiment experiment, 
			LMBNViewer fileViewer, 
			HistoryViewer out, 
			GeneralInfo info
	) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("BN");
		root.add(new DefaultMutableTreeNode(new LeafInfo("Networks", fileViewer)));
		root.add(new DefaultMutableTreeNode(new LeafInfo("BN Details", out)));
		addGeneralInfo(root, info);
		return root;
	}
	
	private DefaultMutableTreeNode createResultTree(
			Experiment experiment, 
			LMBNViewer fileViewer, 
			LMBNViewer badEntries,
			HistoryViewer out, 
			GeneralInfo info
	) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("BN");
		root.add(new DefaultMutableTreeNode(new LeafInfo("Networks", fileViewer)));
		root.add(new DefaultMutableTreeNode(new LeafInfo("Error Net Seed", badEntries)));
		root.add(new DefaultMutableTreeNode(new LeafInfo("BN Details", out)));
		addGeneralInfo(root, info);
		return root;
	}

	private LMBNViewer createLMBNViewer(Vector files) {
		LMBNViewer viewer = new LMBNViewer(files);
		return viewer;

	}
	private void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
		node.add(new DefaultMutableTreeNode("Number of Genes: "+info.numGene));
		node.add(new DefaultMutableTreeNode("Prior: "+info.prior));
		node.add(new DefaultMutableTreeNode("Number of Discretizing Expression Values: "+info.numBin));
		node.add(new DefaultMutableTreeNode("Number of Sample Classes: "+info.numClass));
		node.add(new DefaultMutableTreeNode("Number of Parents: "+info.numParents));
		node.add(new DefaultMutableTreeNode("Algorithm: "+info.algorithm));
		node.add(new DefaultMutableTreeNode("Score: "+info.score));
		node.add(new DefaultMutableTreeNode("K-Folds: "+info.kFolds));
		root.add(node);
	}

	/**
	 * New LM Module with KEGG priors
	 * @param lit
	 * @param ppi
	 * @param kegg
	 * @param LitPpi
	 * @param LitKegg
	 * @param KeggPpi
	 * @param LitPpiKegg
	 * @param path
	 * @return
	 * @throws NullArgumentException 
	 * @throws IOException 
	 * @throws OutOfRangeException 
	 */
	public ArrayList literatureMining(
			boolean lit,
			boolean ppi, 
			boolean kegg, 
			boolean LitPpi, 
			boolean LitKegg, 
			boolean KeggPpi, 
			boolean LitPpiKegg, 
			String path, 
			IData data
	) throws OutOfRangeException, IOException, NullArgumentException{
		//System.out.print(sep);
		GetInteractionsModule getModule = new GetInteractionsModule(path, this.probeIndexAssocHash);
		if(LitPpiKegg){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.getInteractions(this.tmpPath +
					BNConstants.PPI_KEGG_LIT_INTER_MODULE_FILE, data);
		}
		else if(LitKegg){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.getInteractions(this.tmpPath +
					BNConstants.LIT_KEGG_INTER_MODULE_FILE, data);
		}
		else if(KeggPpi){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.getInteractions(this.tmpPath +
					BNConstants.PPI_KEGG_INTER_MODULE_FILE, data);
		}
		else if(LitPpi){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.getInteractions(this.tmpPath +
					BNConstants.BOTH_INTER_MODULE_FILE, data);
		}
		else if(lit){
			//getModule.test(path+sep+"getInterModLit.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.getInteractions(this.tmpPath + 
					BNConstants.LIT_INTER_MODULE_FILE, data);
		}
		else if(ppi){
			//getModule.test(path+sep+"getInterModPPIDirectly.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.getInteractions(this.tmpPath +
					BNConstants.PPI_INTER_MODULE_DIRECT_FILE, data); 
		}
		else if(kegg){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.getInteractions(this.tmpPath +
					BNConstants.KEGG_INTER_MODULE_FILE, data);
		}

		return null;
	}

	// The bif file is for Weka
	public void prepareXMLBifFile(String path){
		PrepareXMLBifModule getModule = new PrepareXMLBifModule();
		PrepareXMLBifModule.test(this.tmpPath +
				BNConstants.XML_BIF_MODULE_FILE);
	}

	/**
	 * General info structure.
	 */
	public static class GeneralInfo {
		public String score;
		String prior="NO Priors";
		String useGoTerms="Use modification of DFS";
		String numParents;
		int numClass;
		int numBin;
		int numGene;
		String algorithm;
		int kFolds;

	}

	/**
	 * State Saving Function
	 * @param LMBNViewer
	 */
	public void setLMBNViewer(LMBNViewer fileViewer){
		this.fileViewer = fileViewer;
	}

	/**
	 * Sate SAving Function
	 * @return
	 */
	public LMBNViewer getLMBNViewerViewer(){
		return fileViewer;
	}

	/**
	 * State Saving Function
	 * @param histViewer
	 */
	public void setHistoryViewer(HistoryViewer histViewer){
		wekaOutputViewer = histViewer;
	}

	/**
	 * Sate SAving Function
	 * @return
	 */
	public HistoryViewer getHistoryViewer(){
		return wekaOutputViewer;
	}

	public String getWekaEvalString() {
		return evalStr;
	}

	public String getBootNetworkFile() {
		return this.bootNetFile;
	}

	public Vector getNetworkFiles() {
		return this.networkFiles;
	}

	/**
	 * For State Saving
	 */
	public void setWekaEvalString(String s) {
		evalStr = s;
	}

	public void setBootNetworkFile(String file) {
		this.bootNetFile = file;
	}

	public void setNetworkFiles(Vector netFiles) {
		this.networkFiles = netFiles;
	}

	//////////////////////////////////////////
	// * Progress Monitor functions
	// * Progress updater thread class
	//////////////////////////////////////////

	/**
	 * 
	 * @param msg
	 * @param note
	 */
	void pBarInit(String msg, String note) {
		pbar = new ProgressMonitor(
				framework.getFrame(), 
				msg, 
				note, 
				0, 100
		);
		pbar.setMillisToDecideToPopup(0);
		pbar.setMillisToPopup(0);
		pbar.setProgress(0);

		// Fire a timer every once in a while to update the progress.
		Timer timer = new Timer(500, this);
		timer.start();
	}

	/**
	 * Update both msg & counter
	 * @param msg
	 * @param ctr
	 * @throws InterruptedException 
	 */
	void updatePBarProgress(String msg, int ctr) throws InterruptedException {
		updatePBarMsg(msg);
		updatePBarCounter(ctr);
		Thread.sleep(1000);
	}

	/**
	 * Update PBar message
	 * @param msg
	 * @throws InterruptedException 
	 */
	void updatePBarMsg(String msg) throws InterruptedException {
		pBarMsg = msg;
		Thread.sleep(1000);
	}

	/**
	 * Update PBar counter
	 * @param ctr
	 */
	void updatePBarCounter(int ctr) {
		counter = ctr;
	}

	void disposePBar(){
		if (pbar == null)
			return;
		pbar.setProgress(pbar.getMaximum());
		pbar.close();
		pbar = null;
	}

	/**
	 * BNGUI ActionListener for PBar timer
	 */
	public void actionPerformed(ActionEvent e) {
		// Invoked by the timer every half second. Simply place
		// the progress monitor update on the event queue.
		SwingUtilities.invokeLater(new UpdatePBar());
	}

	/**
	 * implements run for PBar timer function in a thread
	 * @author raktim
	 *
	 */
	class UpdatePBar implements Runnable {
		public void run() {
			if (pbar == null) return;
			if (pbar.isCanceled()) {
				disposePBar();
				//System.exit(1);
				cancel = true;
				return;
			}
			pbar.setProgress(counter);
			pbar.setNote(pBarMsg); 
		}
	}
}//end class

