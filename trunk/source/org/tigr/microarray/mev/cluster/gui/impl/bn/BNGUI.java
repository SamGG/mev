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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.ParserConfigurationException;

import org.tigr.microarray.mev.HistoryViewer;
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
import org.tigr.microarray.mev.cluster.gui.impl.bn.getInteractions.GetInteractionsModule;
import org.tigr.microarray.mev.cluster.gui.impl.bn.prepareXMLBif.PrepareXMLBifModule;
import org.tigr.microarray.mev.cluster.gui.impl.lm.LMGUI;
import org.tigr.microarray.mev.resources.AvailableAnnotationsFileDefinition;
import org.tigr.microarray.mev.resources.ISupportFileDefinition;
import org.tigr.microarray.mev.resources.SupportFileAccessError;
import org.xml.sax.SAXException;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;

public class BNGUI implements IClusterGUI {
	//private static final int GENE_CLUSTER = 0;
	//private static boolean done = false;
	//private static boolean run = false;
	//private static boolean cancelRun = false;
	//private static boolean prior = true;

	HashMap<String, String> probeIndexAssocHash = new HashMap<String, String>();
	HistoryViewer wekaOutputViewer;
	LMBNViewer fileViewer;
	IData data;
	IFramework framework;
	private JDialog resultFrame;
	private JFrame mainFrame;
	private JCheckBox finalThreshBox;
	private JTextField confThreshField;
	private JButton updateNetwork;
	//New vars
	//private File labelFile = null;
	//JTable BNClassTable;
	//private BNClassTableModel kModel;
	private String basePath;
	//private Properties props = null;
	//private boolean isBootstraping = false;
	//private String numBin;
	//private int numClasses;
	//private String[] label;
	//private String sType;
	//private String sAlgorithm;
	//private String numParents;
	//private boolean useArc;
	//private int numIterations = 100;
	//private float confThreshold = 0.07f;
	//private int kfold = 10;
	private String evalStr = null;
	private String XmlBifStr = null;
	private String evalStrs[] = null;
	private String XmlBifStrs[] = null;
	private Vector<String> networkFiles = new Vector<String>();
	private String bootNetFile = null;
	private Vector<String> interactionsfinal = null;
	private String finalBootFile = null;
	//private Hashtable<String, Integer> edgesTable = new Hashtable<String, Integer>();
	//End New vars
	public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
		
		//done = false;
		//run = false;
		//cancelRun = false;
		//prior = true;
		this.framework = framework;
		mainFrame = (JFrame)(framework.getFrame());
		data = framework.getData();
		Experiment exp = data.getExperiment();
		//exp.getGeneIndexMappedToData(row);
		ClusterRepository repository = framework.getClusterRepository(Cluster.GENE_CLUSTER);
		// String easeFileLocation = null;
		String chipType = null;
		String species = null;
		Vector<ISupportFileDefinition> defs = new Vector<ISupportFileDefinition>();

		BNSupportDataFile bnSuppFileHandle = null;
		
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
		
		Hashtable<String, Vector<String>> speciestoarrays = null;
		AvailableAnnotationsFileDefinition aafd = new AvailableAnnotationsFileDefinition();
		defs.add(aafd);
        try {
        	Hashtable<ISupportFileDefinition, File> supportFiles = framework.getSupportFiles(defs, true);
        	
	        File speciesarraymapping = supportFiles.get(aafd);
	        try {
	        	speciestoarrays = aafd.parseAnnotationListFile(speciesarraymapping);
	        } catch (IOException ioe) {
	        	speciestoarrays = null;
	        }
        } catch (SupportFileAccessError sfae) {

        }
		//
		//final BNInitDialog dialog = new BNInitDialog(framework.getFrame(), repository, framework.getData().getFieldNames());
		final BNInitDialog dialog = new BNInitDialog(framework, repository, framework.getData().getFieldNames(),
				framework.getResourceManager(),
        		species, 
        		chipType, 
        		speciestoarrays);
		if(dialog.showModal() != JOptionPane.OK_OPTION)
			return null;
		if(dialog.isNone()){
			//prior = false;
			//done = true;
		}

		if(dialog.isNone()){
			return null;
		}

		if(dialog.getSelectedCluster().getIndices().length > BNConstants.MAX_GENES) {
			JOptionPane.showMessageDialog(framework.getFrame(), "Cluster size exceeds max gene limit of " + BNConstants.MAX_GENES + ". Please reduce cluster size.", "Error!", JOptionPane.ERROR_MESSAGE);
			LMGUI.done = false;
			return null;
		}
		
		RunWekaProgressPanel pgPanel = new RunWekaProgressPanel();
		pgPanel.setString("Learning Network priors");
		pgPanel.setIndeterminate(true);
		pgPanel.setLocationRelativeTo(framework.getFrame());
		//pgPanel.setLocation((screenSize.width-framework.getFrame().getSize().width)/2,(screenSize.height-framework.getFrame().getSize().height)/2);
		pgPanel.setVisible(true);

		try {
			this.probeIndexAssocHash = Useful.converter(dialog.getSelectedCluster(),framework,dialog.getBaseFileLocation());
			if(this.probeIndexAssocHash == null){
				throw new Exception("Error in mapping Unique identifiers to Accession");
			}
		} catch(Exception e) {
			//JOptionPane.showMessageDialog(framework.getFrame(), "Error mapping Unique identifiers", "Error!", JOptionPane.ERROR_MESSAGE);
			JOptionPane.showMessageDialog(framework.getFrame(), e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
			LMGUI.done = false;
			pgPanel.dispose();
			return null;
		}
		
		String kegg_sp = dialog.getKeggSpecies();
		if(kegg_sp != null) kegg_sp = kegg_sp.trim();
		else kegg_sp = "na";

		//Build Property file for Weka Params
		Useful.buildPropertyFile(dialog.isLit(),dialog.isPPI(),dialog.isKEGG(), dialog.isBoth(), dialog.isLitAndKegg(), dialog.isPpiAndKegg(), dialog.isAll(),dialog.useGoTerm(),dialog.getBaseFileLocation(),kegg_sp);
		
		//Thread thread = new Thread( new Runnable(){
		//public void run(){	
		//if(!dialog.isNone()){		System.out.println(dialog.getBaseFileLocation());
		int status = -1;
		try {
			status = literatureMining(dialog.isLit(), dialog.isPPI(), dialog.isKEGG(), dialog.isBoth(), dialog.isLitAndKegg(), dialog.isPpiAndKegg(), dialog.isAll(),dialog.getBaseFileLocation(), this.data);
		} 
		catch(OutOfMemoryError ofm){
			pgPanel.dispose();
			//BNGUI.done = false;
			System.out.println("Out of Memory. Aborting...");
			JOptionPane.showMessageDialog(framework.getFrame(), ofm.getMessage() + "\n Out of Memory", "Error - Out of Memory. Aborting!", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(framework.getFrame(), e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
			//BNGUI.done = false;
			pgPanel.dispose();
			return null;
		}
		//literatureMining(true,false,false,dialog.getBaseFileLocation());
		System.out.println("Interaction count: " + status);
		if(status > 0) {
			//Display warning if too many interactions are found.
			if(status > 50) {
				if (JOptionPane.showConfirmDialog(framework.getFrame(),
						"Too many interactions found. \n The process might run out of memory! \n Do you want to continue ? ", "Interaction found: " + status + "!",
						JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
					//BNGUI.done = false;
					pgPanel.dispose();
					return null;
				} 
			}
			//Generates prior info for Weka
			prepareXMLBifFile(dialog.getBaseFileLocation());
			//BNGUI.done = true;
			//pgPanel.dispose();
		} else {
			//BNGUI.done = false;
			pgPanel.dispose();
			return null;
		}
		
		BNClassificationEditor bnEditor = new BNClassificationEditor(framework, false, dialog.getNumberClass(), dialog.getBaseFileLocation());
		pgPanel.dispose();
		//bnEditor.showModal(true);
		
		if(bnEditor.showModal(true) != JOptionPane.OK_OPTION)
			return null;
		
		//Start new structure
		AlgorithmData algData = new AlgorithmData();
		
		File labelFile = bnEditor.getLabelFile();
		BNClassTableModel kModel = bnEditor.getClassTableModel();
		this.basePath = dialog.getBaseFileLocation();
		boolean isBootstraping = dialog.isBootstrapping();
		int numIterations = dialog.getNumIterations();
		String numBin = String.valueOf(dialog.getNumberBin());
		String sType = dialog.getScoreType();
		String sAlgorithm = dialog.getAlgorithm();
		String numParents = dialog.numParents();
		int kfold = dialog.getKFolds();
		boolean useArc = dialog.useArcRev();
		int numClasses = dialog.getNumberClass();
		float confThreshold = dialog.getConfThreshold();
		//this.label = new String[data.getFeaturesCount()];
		onOk(dialog.getSelectedCluster(), labelFile, kModel, isBootstraping, numBin, numClasses, sType, sAlgorithm, numParents, kfold, useArc, numIterations, confThreshold);
		//End New Struct

		/*
		while(!BNGUI.run){
			try{
				Thread.sleep(500);	
			}catch(InterruptedException x){
				//ignore;
			}
		}
		*/
		//if(BNGUI.cancelRun)
			//return null;
		
		//Added to record the Weka output for Observed BN analysis
		wekaOutputViewer = new HistoryViewer();
		String wekaResult = evalStr;
		wekaOutputViewer.addHistory(wekaResult);

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

		//String lmFile = bnEditor.basePath + BNConstants.RESULT_DIR + BNConstants.SEP + System.getProperty("LM_ONLY");
		//String bnFile = bnEditor.getBootNetworkFile();
		//Vector files = bnEditor.getNetworkFiles();
		fileViewer = createLMBNViewer(this.networkFiles);
		return createResultTree(exp, fileViewer, wekaOutputViewer, info);
		//return root;
	}

	//New Support Funct
	/**
	 * Core function to run BN with weka on the selected cluster
	 * @param cl
	 */
	protected void onOk(Cluster cl, File labelFile, BNClassTableModel kModel, boolean isBootstraping, String numBin, int numClasses, String sType, String sAlgorithm, String numParents, int kfold, boolean useArc, int numIterations, float confThreshold) {
		//result = JOptionPane.OK_OPTION;
		//BNClassificationEditor.this.dispose(); 
		RunWekaProgressPanel runProgressPanel;
		//Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		runProgressPanel = new RunWekaProgressPanel();
		runProgressPanel.setString("Running Network Search");
		runProgressPanel.setIndeterminate(true);
		//runProgressPanel.setLocation((screenSize.width)/2,(screenSize.height)/2);
		runProgressPanel.setLocationRelativeTo(framework.getFrame());
		runProgressPanel.setVisible(true);
		
		String[] label = saveToFile(kModel, numClasses, data.getFeaturesCount(), basePath+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP+"label");
		if(labelFile != null) {
			saveToFile(kModel, numClasses, labelFile);
		}
		saveWekaData(cl, framework, label, basePath+BNConstants.SEP+BNConstants.TMP_DIR);
		Properties props = tranSaveWeka(isBootstraping, numBin, numClasses, numIterations, basePath+BNConstants.SEP+BNConstants.TMP_DIR);

		//Thread thread = new Thread( new Runnable(){
			//public void run(){
				try{     
					//String sep = System.getProperty("file.separator"); 
					String path = basePath+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP;
					String outarff = "outExpression.arff";
					
					//	WEKA on observed Data
					String arguments = Useful.getWekaArgs(path, outarff, sAlgorithm, useArc, numParents, sType, kfold);
					System.out.println("calling weka On Observed Data,  with arguments: \n"+arguments);
					String[] argsWeka = arguments.split(" ");
					BayesNet bnNetOrg = new BayesNet();
					evalStr = Evaluation.evaluateModel(bnNetOrg, argsWeka);
					bnNetOrg.estimateCPTs();
					XmlBifStr = bnNetOrg.toXMLBIF03();
					System.out.println("\t\t ***** Start Eval and Bif Strs");
					System.out.println("XmlBifStr\n" + XmlBifStr);
					System.out.println("evalStr\n" + evalStr);
					System.out.println("\t\t End ***** Eval and Bif Strs");
					
					//TODO Start
					//LM Network
					String lmNetFile = basePath + BNConstants.SEP + BNConstants.RESULT_DIR + BNConstants.SEP + System.getProperty("LM_ONLY");
					networkFiles.add(0,lmNetFile);

					// BN Observed
					String obsNetFile = basePath + BNConstants.SEP + BNConstants.RESULT_DIR + BNConstants.SEP + Useful.getUniqueFileID()+ sAlgorithm + "_" + sType + "_";
					String obsCptFile = obsNetFile + "CPT.xml";
					
					//Write CPT file
					FileOutputStream fos;
					PrintWriter pw;	
					fos = new FileOutputStream(obsCptFile);
					pw = new PrintWriter(fos, true);
					pw.print(bnNetOrg.toXMLBIF03());
					pw.close();
					fos.close();
					
					if(!data.isAnnotationLoaded()) {
						//Create sif file
						obsNetFile += ".sif";
						fos = new FileOutputStream(obsNetFile);
						pw = new PrintWriter(fos, true);
						//Move from evalStr to CPT Bif
						//FromWekaToSif.fromWekaToSif(evalStr, pw, false);
						FromWekaToSif.fromWekaBifToSif(XmlBifStr, pw);
						fos.flush();fos.close();pw.close();
					}
					else {
						//create xgmml file
						obsNetFile += ".xgmml";
						//Should move completely to Bif CPT format
						//FromWekaToSif.fromWekaToXgmml(evalStr, fileName, false, probeIndexAssocHash, data);
						FromWekaToSif.fromWekaBifToXgmml(true, XmlBifStr, obsNetFile, obsCptFile, probeIndexAssocHash, data);
					}					
					networkFiles.add(0,obsNetFile);
					//TODO End
					
					if(isBootstraping) {	
						//WEKA On bootstrapped data
						String outarffbase = props.getProperty("rootOutputFileName");
						String outarffext = ".arff";
						evalStrs = new String[numIterations];
						XmlBifStrs = new String[numIterations];
						for(int i=0; i < numIterations; i++){
							//Previously created .arff files for bootstrap
							outarff = outarffbase + i + outarffext;
							arguments = Useful.getWekaArgs(path, outarff, sAlgorithm, useArc, numParents, sType, kfold);
							System.out.println("calling weka On Bootstrap Data, arguments: \n"+arguments);
							argsWeka = arguments.split(" ");
							
							bnNetOrg = new BayesNet();
							evalStrs[i] = Evaluation.evaluateModel(bnNetOrg, argsWeka);
							bnNetOrg.estimateCPTs();
							XmlBifStrs[i] = bnNetOrg.toXMLBIF03();
							
							System.out.println("Bootstrap Itr: " + i);
						}
						//if(!BNGUI.cancelRun)
						Hashtable<String, Integer> edgesTable = new Hashtable<String, Integer>();
						bootNetFile = createNetworkFromBootstraps(XmlBifStrs, evalStrs, numIterations, outarffbase, sType, sAlgorithm, kfold, numIterations, confThreshold, edgesTable);
						displayScrollPane(getScrollPanePanel(isBootstraping, sType, sAlgorithm, kfold, numIterations, edgesTable));
					}
				} catch(OutOfMemoryError ofm){
					runProgressPanel.dispose();
					System.out.println("Error: Out of Memory..");
					ofm.printStackTrace();
					JOptionPane.showMessageDialog(new JFrame(), ofm.getMessage() + "\n Out of Memory", "Error - Out of Memory. Cannot Continue!", JOptionPane.ERROR_MESSAGE);
					//BNGUI.run = true;
					//BNGUI.cancelRun = true;
				} catch(IOException ioE){
					ioE.printStackTrace(); 
					JOptionPane.showMessageDialog(null, ioE.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				} catch(NullArgumentException nae){
					nae.printStackTrace(); 
					JOptionPane.showMessageDialog(null, nae.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				} catch (SAXException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Error processing Weka Bif DOM", "Error", JOptionPane.ERROR_MESSAGE);
				} catch(Exception ex){
					runProgressPanel.dispose();
					System.out.println("Weka exception..");
					ex.printStackTrace();
					JOptionPane.showMessageDialog(new JFrame(), ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
					//BNGUI.run = true;
					//BNGUI.cancelRun = true;
				}
				runProgressPanel.dispose();
				//if(!BNGUI.cancelRun)
				//displayScrollPane(getScrollPanePanel());
				//Call Webstart with Files
				CytoscapeWebstart.onWebstartCytoscape(networkFiles);
				//BNGUI.run = true;
			//}
		//});
		//thread.start();
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
		resultFrame.setLocation((screenSize.width-panel.getSize().width)/2,(screenSize.height-panel.getSize().height)/2);
		resultFrame.setVisible(true);
	}
	
	/**
	 * Panel to create nets for different user given thresholds
	 * @return
	 */
	public JPanel getScrollPanePanel(boolean isBootstraping, final String sType, final String sAlgorithm, final int kfold, final int numIterations, final Hashtable<String, Integer> edgesTable){
		//JButton updateNetwork;
		//JTextField confThreshField;
		//JCheckBox finalThreshBox;
		// BN Bootstrap Network
		if(isBootstraping){
			//System.out.println("BN BootStrap: " + bootNetFile);
			networkFiles.add(0,bootNetFile);
			updateNetwork = new JButton("Update Network");
			confThreshField = new JTextField("0.8");
			confThreshField.setPreferredSize(new Dimension(35, 10));

			finalThreshBox = new JCheckBox("Final");
			finalThreshBox.setBackground(Color.white);
			finalThreshBox.setFocusPainted(false);
		}

		// Debug Print File Names
		System.out.println("Files to Show: " + networkFiles.size());
		for(int i=0; i < networkFiles.size(); i++) {
			System.out.println("File: " + networkFiles.get(i));
		}
		//End Debug

		final JPanel evalPanel = new JPanel();
		evalPanel.setLayout(new BorderLayout());
		evalPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		// The evalStr is now shown in the Viewer
		if(isBootstraping){
			updateNetwork.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					onUpdateNetwork(sType, sAlgorithm, kfold, numIterations, edgesTable);
					// Try Cytoscape Broadcast
					if(!framework.isGaggleConnected()) {
						if(framework.requestGaggleConnect()) {
							try {
								broadcastNetworkGaggle(interactionsfinal);
							} catch (Exception e) {
								JOptionPane.showMessageDialog(null, "Error Using Gaggle Broadcast", "Error", JOptionPane.ERROR_MESSAGE);
								e.printStackTrace();
								if(finalThreshBox.isSelected())
									resultFrame.dispose();
								else
									resultFrame.show();
							}
						} else {
							//TODO just write and file and display msg
							JOptionPane.showMessageDialog(null, "Could not connect to Gaggle", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});
		}
		if(isBootstraping) {
			//evalPanel.add(showBootInCytoButton, BorderLayout.LINE_START);
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
	 *
	 */
	protected void onUpdateNetwork(String sType, String sAlgorithm, int kfold, int numIterations, Hashtable<String, Integer> edgesTable) {
		try {
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
			try {
				bifCpt = learnCPTFromFixedNetwork(interactionsfinal, kfold);
			} catch (Exception e1) {
				//TODO Create JOptionPane Msg ??
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, "Error Learning CPT from Weka", "Error", JOptionPane.ERROR_MESSAGE);
			}
			
			String _bootNetFile = basePath+BNConstants.SEP+BNConstants.RESULT_DIR+BNConstants.SEP+
									Useful.getUniqueFileID()+ sAlgorithm + "_" + sType + "_" +
									"boot_result_"+numIterations+"_"+confThres;
			
			String outCPTBifXML = _bootNetFile + "_CPT.xml";
			
			//Write network File
			if(!data.isAnnotationLoaded()) {
				//Create interactions & sif file
				_bootNetFile += ".sif";
				FileOutputStream fos;
				PrintWriter pw;
				try {
					fos = new FileOutputStream(_bootNetFile);
					pw = new PrintWriter(fos, true);
					FromWekaToSif.fromWekaBifToSif(bifCpt, pw);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				} catch (SAXException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				//xgmml File
				_bootNetFile += ".xgmml";
				//FromWekaToSif.fromWekaToXgmml(edgesTable, numIterations, confThres, _bootNetFile, probeIndexAssocHash, data);
				FromWekaToSif.fromWekaBifToXgmml(true, bifCpt, _bootNetFile, outCPTBifXML, probeIndexAssocHash, data);
			}

			
			if(finalThreshBox.isSelected()) {
				//Create File & interaction adges
				
				System.out.println("Boot threshold: " + confThres);
				System.out.println("Boot file: " + _bootNetFile);

				//store the final thresh and the file name
				finalBootFile = _bootNetFile;
				networkFiles.add(0,finalBootFile);
				
				if(interactionsfinal.size() == 0) {
					JOptionPane.showMessageDialog(mainFrame, "No valid network for selected threshold\n Will try to use last selected threshold.", "Warning", JOptionPane.INFORMATION_MESSAGE);
					if(interactionsPrefinal != null) {
						if(interactionsPrefinal.size() > 0) {
							JOptionPane.showMessageDialog(mainFrame, "Last network not avaialble. Aborting.", "Warning", JOptionPane.INFORMATION_MESSAGE);
							return;
						}
					} else {
						JOptionPane.showMessageDialog(mainFrame, "Last network not avaialble. Aborting.", "Warning", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
				}
				
				//Distinct name for final CPT XML File
				outCPTBifXML = basePath+BNConstants.SEP+BNConstants.RESULT_DIR+BNConstants.SEP + "FinalNetWithCPT.xml";
				resultFrame.hide();
			}
			
			//Write CPT File final or otherwise
			FileOutputStream fos = new FileOutputStream(outCPTBifXML);
			PrintWriter pw = new PrintWriter(fos, true);
			pw.print(bifCpt);
			pw.close();fos.flush();fos.close();
			//JOptionPane.showMessageDialog(this, "BIF File with CPTs, written here:\n" + outCPTBifXML, "CPTs File", JOptionPane.PLAIN_MESSAGE);
		}catch(Exception ex){
			JOptionPane.showMessageDialog(null, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}

	private void saveToFile(BNClassTableModel kModel, int numClasses, File file) {
		try {
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
				if (((Boolean)(kModel.getValueAt(i, numClasses + 1))).booleanValue()) {
					//label[i]=(new Integer(-1)).toString();
					out.print(-1);
				}
				//out.print("\t");
				for (int j = numClasses + 2; j < kModel.getColumnCount(); j++) {
					out.print("\t");
					out.print(kModel.getValueAt(i, j));
				}
				out.print("\n");
			}
			out.flush();
			out.close();            

		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
	
	/*
	 * Saves sample classification info in a file
	 */
	private String[] saveToFile(BNClassTableModel kModel, int numClasses, int numLabels, String fileName) {
		System.out.println("saveToFile kModel Row# " + kModel.getRowCount());
		System.out.println("numClasses " + numClasses);
		String[] label = new String[numLabels];
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream(new File(fileName)));
			for (int i = 0; i < kModel.getRowCount(); i++) {
				System.out.println("label Row " + i+1);
				out.print(((Integer)(kModel.getValueAt(i, 0))).intValue());
				out.print("\t");
				for (int j = 1; j <= numClasses; j++) {
					if (((Boolean)(kModel.getValueAt(i, j))).booleanValue()) {
						out.print(j);
						label[i]=(new Integer(j)).toString();
						System.out.println("label at " + i + " is " + label[i]);
						break;
					}
				}
				if (((Boolean)(kModel.getValueAt(i, numClasses + 1))).booleanValue()) {
					label[i]=(new Integer(-1)).toString();
					System.out.println("label at " + i + " is " + label[i]);
					out.print(-1);
				}
				//out.print("\t");
				for (int j = numClasses + 2; j < kModel.getColumnCount(); j++) {
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
			JOptionPane.showMessageDialog(framework.getFrame(), "Error saving sample labels.  Labels not saved.", "Save Error", JOptionPane.WARNING_MESSAGE);
			return null;
		}
	}
	
	/**
	 * Writes out observations in terms of UID & class groups
	 * E.g. Ref-Seq class1 class1 class2 class2
	 * @param cl
	 * @param frame
	 * @param path
	 */
	private void saveWekaData(Cluster cl, IFramework frame, String[] label, String path) {
		int genes=cl.getIndices().length;
		//System.out.print(genes);
		IData data = frame.getData();
		int[] rows = new int[genes];
		rows = cl.getIndices();
		String[] accList=new String[genes];
		try{ 
			//PrintWriter out = new PrintWriter(new FileOutputStream(new File(basePath+"wekaData"))); // Raktim - USe Tmp dir
			//String sep= System.getProperty("file.separator");    
			PrintWriter out = new PrintWriter(new FileOutputStream(new File(basePath+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP+"wekaData")));

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

			//for (int i=0; i<genes; i++) {
			//  rows[i] = i;
			//}
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
		}catch (Exception e){
			JOptionPane.showMessageDialog(framework.getFrame(), "Error saving cluster.  Cluster not saved.", "Save Error", JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
			return;
		}
	}

	/**
	 * 
	 * @param binNum
	 * @param path
	 * @param bootStrap
	 * @param numIter
	 * @return
	 */
	public Properties tranSaveWeka(boolean isBootstraping, String numBin, int numClasses, int numIterations, String path){
		return PrepareArrayDataModule.prepareArrayData(path+BNConstants.SEP+"wekaData", numBin, isBootstraping, numIterations, numClasses); 
	}

	
	/**
	 * From an array of Weka evaluation string it creates a network that exceeds a confidence threshold
	 * Writes the netwrok to a file and retirn the file name
	 * @param xmlBifStrs
	 * @param evalStrs 
	 * @param numItr
	 * @param outarffbase
	 * @return
	 */
	private String createNetworkFromBootstraps(String[] xmlBifStrs, String[] evalStrs, int numItr, String outarffbase, String sType, String sAlgorithm, int kfold, int numIterations, float confThreshold, Hashtable<String, Integer> edgesTable) {
		String path = basePath+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP;
		String fileName = path + outarffbase;

		//Create sif files for every output of the resampled WEKA evaluation
		//This can be done in memory but avoided by disk writes
		try {
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
		} catch (Exception e){
			e.printStackTrace();
		}

		//Count occurrence of each edge across all the iterations of the bootstrap network sif files
		try {
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
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//Remove Edges below threshold
		Vector<String> interactions = new Vector<String>();
		interactions = createInteractions(edgesTable, confThreshold, numItr);
		
		//Fix the network learn the CPT
		String bifCpt = "";
		try {
			bifCpt = learnCPTFromFixedNetwork(interactions, kfold);
		} catch (Exception e1) {
			// TODO Create JOptionPane Msg ??
			e1.printStackTrace();
		}
		
		//End Code block to learn CPTs
		String _bootNetFile = basePath+BNConstants.SEP+BNConstants.RESULT_DIR+BNConstants.SEP
							+Useful.getUniqueFileID()+ sAlgorithm + "_" + sType + "_" 
							+ "boot_result_" + numIterations + "_" + confThreshold;
		String _bootCptFile = _bootNetFile + "_CPT.xml";
		
		//Write CPT file for confidence threshold
		FileOutputStream fos;
		PrintWriter pw;
		try {
			fos = new FileOutputStream(_bootCptFile);
			pw = new PrintWriter(fos, true);
			pw.print(bifCpt);
			pw.close();
			fos.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Create Cytoscape File
		if(!data.isAnnotationLoaded()) {
			//Create sif file
			_bootNetFile += ".sif";
			try {
				fos = new FileOutputStream(_bootNetFile);
				pw = new PrintWriter(fos, true);
				FromWekaToSif.fromWekaBifToSif(bifCpt, pw);
				fos.flush();fos.close();pw.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			//create xgmml file
			_bootNetFile += ".xgmml";
			
			try {
				//TODO Move from edgesTable method
				//FromWekaToSif.fromWekaToXgmml(edgesTable, numItr, confThreshold, bootNetFile, probeIndexAssocHash, data);
				FromWekaToSif.fromWekaBifToXgmml(true, bifCpt, _bootNetFile, _bootCptFile, probeIndexAssocHash, data);
			} catch (Exception e) {
				//throw new Exception("Error creating XGML File from Bootstrap");
				e.printStackTrace();
			}
		}
		return _bootNetFile;
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	private String[] convertFromFile(String path){
		//String sep= System.getProperty("file.separator");    
		String filePath = path + BNConstants.SEP + BNConstants.OUT_ACCESSION_FILE; // Raktim - path incls tmp dir
		//String filePath = path+sep+"tmp"+sep+"list.txt";
		System.out.println("convertFromFile(): " + filePath);
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
		String propsFile = BNConstants.getBaseFileLocation() + BNConstants.SEP + BNConstants.TMP_DIR + BNConstants.SEP + BNConstants.XML_BIF_MODULE_FILE;
		PrepareXMLBifModule.createXMLBifFromList(propsFile, interactions);
		String bifFileFinal = BNConstants.getBaseFileLocation() + BNConstants.SEP + BNConstants.TMP_DIR + BNConstants.SEP + BNConstants.OUT_XML_BIF_FILE_FINAL;
		BayesNet bnNet = new BayesNet();
		//Dataset
		String outarff = basePath+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP + "outExpression.arff";
		//Specify Data set K for kfold validation in weka
        String modelArgs = "-t " + outarff + " -c 1 -x " + kfold;
        //Specify Fixed Netwrok Classifier from File
        modelArgs += " -Q weka.classifiers.bayes.net.search.fixed.FromFile -- -B ";
        //Specify Fixed Netwrok Bif File
        modelArgs += bifFileFinal;
        //Specify Weka Estimator with inital alpha
        modelArgs += " -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5";
        //Run Weka Model
        Evaluation.evaluateModel(bnNet, modelArgs.split(" "));
        //Estimator myEstm [][] = bnNet.getDistributions();
		bnNet.estimateCPTs();
		//Write The network with CPts in BIF File format
		//System.out.println(bnNet.toXMLBIF03());
		//String outCPTBifXML = basePath+BNConstants.SEP+BNConstants.RESULT_DIR+BNConstants.SEP + "FixedNetWithCPT.xml";
		return bnNet.toXMLBIF03();
	}
	
	/**
	 * Broadcasts a list of edges as a network to Cytoscape using Gaggle
	 * @param interacts a list of edges encoded with node labels and probe index id
	 */
	protected void broadcastNetworkGaggle(Vector<String> interacts) {
		Vector<int[]> interactions = new Vector<int[]>();
		Vector<String> types = new Vector<String>();
		Vector<Boolean> directionals = new Vector<Boolean>();
		for(int j=0; j<interacts.size(); j++) {
			//String uid = this.data.getSlideDataElement(0,rows[j]).getFieldAt(0);
			// Of the form XXXXXX pp XXXXXX
			String[] edgeLabels = interacts.get(j).split(" ");
			System.out.println("Encoding edge: " + edgeLabels[0] + " - " + edgeLabels[2]);
			int[] fromTo = new int[2];
			//Get indx from hash map encoded int the form NM_23456 to 1-Afy_X1234 where 1 is the probe index
			String tmp[] = probeIndexAssocHash.get(edgeLabels[0]).split("-");
			fromTo[0] = Integer.parseInt(tmp[0]);
			tmp = probeIndexAssocHash.get(edgeLabels[2]).split("-");
			fromTo[1] = Integer.parseInt(tmp[0]);
			types.add("pd");
			directionals.add(true);
			interactions.add(fromTo);
		}
		framework.broadcastNetwork(interactions, types, directionals);
	}

	//End New Support Function
	
	private DefaultMutableTreeNode createResultTree(Experiment experiment, LMBNViewer fileViewer, HistoryViewer out, GeneralInfo info) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("BN");
		root.add(new DefaultMutableTreeNode(new LeafInfo("Networks", fileViewer)));
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
	 */
	public int literatureMining(boolean lit,boolean ppi, boolean kegg, boolean LitPpi, boolean LitKegg, boolean KeggPpi, boolean LitPpiKegg, String path, IData data){
		//System.out.print(sep);
		GetInteractionsModule getModule = new GetInteractionsModule(path, this.probeIndexAssocHash);
		if(LitPpiKegg){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.PPI_KEGG_LIT_INTER_MODULE_FILE, data);
		}
		else if(LitKegg){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.LIT_KEGG_INTER_MODULE_FILE, data);
		}
		else if(KeggPpi){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.PPI_KEGG_INTER_MODULE_FILE, data);
		}
		else if(LitPpi){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.BOTH_INTER_MODULE_FILE, data);
		}
		else if(lit){			//getModule.test(path+sep+"getInterModLit.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path + 
					BNConstants.SEP + 
					BNConstants.TMP_DIR + 
					BNConstants.SEP + 
					BNConstants.LIT_INTER_MODULE_FILE, data);
		}
		else if(ppi){			//getModule.test(path+sep+"getInterModPPIDirectly.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.PPI_INTER_MODULE_DIRECT_FILE, data); 
		}
		else if(kegg){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.KEGG_INTER_MODULE_FILE, data);
		}
		
		return -1;
	}
	// The bif file is for Weka
	public void prepareXMLBifFile(String path){
		PrepareXMLBifModule getModule = new PrepareXMLBifModule();
		PrepareXMLBifModule.test(path +
				BNConstants.SEP +
				BNConstants.TMP_DIR +
				BNConstants.SEP +
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
}//end class
