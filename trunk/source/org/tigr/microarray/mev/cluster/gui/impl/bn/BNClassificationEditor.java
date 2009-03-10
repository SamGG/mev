/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * BNClassificationEditor.java
 *
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDialogs.ExampleFileFilter;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.bn.prepareXMLBif.PrepareXMLBifModule;
import org.tigr.util.StringSplitter;
import org.xml.sax.SAXException;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.estimators.Estimator;
import weka.gui.GUIChooser;
import weka.classifiers.bayes.net.search.SearchAlgorithm;
import weka.classifiers.bayes.net.estimate.BayesNetEstimator;
/**
 *
 */
public class BNClassificationEditor extends javax.swing.JDialog {// JFrame {

	final IFramework framework;
	IData data;
	boolean classifyGenes;
	private boolean stopHere = true;
	private boolean nextPressed = false;
	private boolean incompatible = false;
	private boolean fileSaved = false;
	public static GUIChooser m_chooser;
	JTextArea textArea;
	JScrollPane evalScrollPane;
	String evalStr = null;
	String XmlBifStr = null;
	RunWekaProgressPanel runProgressPanel;
	int numClasses;
	JButton showInCytoButton,showLitCytoButton, showBootInCytoButton, showAllNetworks;
	Vector<String> networkFiles = new Vector<String>();
	String[] fieldNames;
	int numGenes, numExps;
	JTable BNClassTable;
	BNClassTableModel kModel;
	JMenuBar menuBar;
	JMenu fileMenu, editMenu, toolsMenu, assignSubMenu, sortAscMenu, sortDescMenu;
	JMenuItem saveItem, closeItem, fileMenuItem,selectAllItem, searchItem, sortByClassItem, origOrderItem;
	JMenuItem[] classItem, labelsAscItem, labelsDescItem;
	JRadioButton saveButton, doNotSaveButton;
	JButton nextButton, cancelButton, loadButton, saveSettingsButton, updateNetwork;
	JTextField confThreshField;
	JCheckBox finalThreshBox;
	JFrame mainFrame;
	JDialog resultFrame;
	String numBin, numParents, sAlgorithm, sType;
	boolean useArc=true;
	//SortListener sorter;
	String[] label;
	Object[][] origData;
	final String basePath;
	//Raktim 
	//String fileName;
	String evalStrs[] = null;
	String XmlBifStrs[] = null;
	Properties props = null;
	boolean isBootstraping = false;
	String bootNetFile = null;
	String finalBootFile = null;
	int numIterations = 100;
	float confThreshold = 0.07f;
	int kfold = 10;
	Hashtable<String, Integer> edgesTable = new Hashtable<String, Integer>();    
	File labelFile = null;
	Cluster clust;
	HashMap<String, String> probeIndexAssocHash;
	Vector<String> interactionsfinal = null;

	/** Creates a new instance of BNClassificationEditor */
	public BNClassificationEditor(final IFramework framework, boolean classifyGenes, final Cluster cl,String num,int numClasses,String parents,String algorithm,String scoreType,boolean uAr, boolean bootstrap, int iteration, float threshold, int kfolds, String path, HashMap<String, String> probeIndexHash) {
		super(framework.getFrame(), true);
		this.setTitle("Classification Editor: Assign Samples to group(s)");
		mainFrame = (JFrame)(framework.getFrame());
		//setBounds(0, 0, 550, 800);
		int width = 300;
		int height = 300;

		setBackground(Color.white);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.framework = framework;
		this.data = framework.getData();
		this.numGenes = data.getFeaturesSize();
		this.numExps = data.getFeaturesCount();
		label=new String[numExps];
		this.fieldNames = data.getFieldNames();
		this.classifyGenes = classifyGenes;
		this.numClasses = numClasses;
		this.clust = cl;
		if(numClasses <= 1)
			width = 360;
		else if (numClasses > 1 && numClasses <= 2)
			width = 390;
		else if (numClasses > 2 && numClasses <= 3)
			width = 450;
		else if (numClasses > 3 && numClasses <= 5)
			width = 550;
		else
			width = 600;

		if(this.numExps <= 5)
			height = 200;
		else if (this.numExps > 5 && this.numExps <= 10)
			height = 250;
		else if (this.numExps > 10 && this.numExps <= 15)
			height = 300;
		else if (this.numExps > 15 && this.numExps < 20)
			height = 350;
		else 
			height = 450;

		setBounds(0,0,width,height);

		numBin=num;
		numParents=parents;
		sAlgorithm=algorithm;
		sType=scoreType;
		useArc=uAr;
		basePath=path+System.getProperty("file.separator");
		this.isBootstraping = bootstrap;
		this.numIterations = iteration;
		this.confThreshold = threshold;
		this.kfold = kfolds;
		this.probeIndexAssocHash = probeIndexHash;
		//menuBar = new JMenuBar();
		//this.setJMenuBar(menuBar);  

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;

		JPanel pane = new JPanel();
		pane.setLayout(gridbag);

		JPanel tablePanel = new JPanel();
		GridBagLayout grid1 = new GridBagLayout();
		tablePanel.setLayout(grid1);

		kModel = new BNClassTableModel();
		BNClassTable = new JTable(kModel);
		BNClassTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableColumn column = null;
		for (int i = 0; i < kModel.getColumnCount(); i++) {
			column = BNClassTable.getColumnModel().getColumn(i);
			if(i == (kModel.getColumnCount()-1))
				//Resize the last column to make it bigger for sample names.
				column.setMinWidth(190);
			else
				column.setMinWidth(30);
		}
		BNClassTable.setColumnModel(new BNClassTableColumnModel(BNClassTable.getColumnModel()));
		BNClassTable.getModel().addTableModelListener(new ClassSelectionListener());

		// searchDialog = new KNNCSearchDialog(this, BNClassTable, numClasses, false); //persistent search dialog     
		//JOptionPane.getFrameForComponent(this)
		JScrollPane scroll = new JScrollPane(BNClassTable);
		buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
		grid1.setConstraints(scroll, constraints);
		tablePanel.add(scroll);

		buildConstraints(constraints, 0, 0, 2, 1, 100, 90);
		gridbag.setConstraints(tablePanel, constraints);
		pane.add(tablePanel);

		final JFileChooser fc1 = new JFileChooser();
		final JFileChooser fc2 = new JFileChooser();
		ExampleFileFilter filter = new ExampleFileFilter("txt");
		fc2.setFileFilter(filter);

		//TODO The following block may not be needed
		String dataPath = TMEV.getDataPath();
		File pathFile = TMEV.getFile("data/bn");
		if(dataPath != null) {
			pathFile = new File(dataPath);
			if(!pathFile.exists())
				pathFile = TMEV.getFile("data/bn");
		}
		//End unnecessary Block

		//fc1.setCurrentDirectory(new File(pathFile.getAbsolutePath()));
		fc1.setCurrentDirectory(new File(basePath));
		fc1.setDialogTitle("Open Classification");

		//fc2.setCurrentDirectory(new File(pathFile.getAbsolutePath()));
		fc2.setCurrentDirectory(new File(basePath));
		fc2.setDialogTitle("Save Classification");

		JPanel bottomPanel = new JPanel();
		bottomPanel.setBorder(new EtchedBorder());
		bottomPanel.setBackground(Color.white);
		GridBagLayout grid2 = new GridBagLayout();
		bottomPanel.setLayout(grid2);
		loadButton=new JButton("Load Settings");
		loadButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt) {
				int returnVal = fc1.showOpenDialog(BNClassificationEditor.this);  
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc1.getSelectedFile();  
					loadFromFile(file);
				}
			}
		});
		saveSettingsButton=new JButton("Save Settings");
		saveSettingsButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt) {
				int returnVal = fc2.showOpenDialog(BNClassificationEditor.this);  
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					labelFile = fc2.getSelectedFile();
					//saveToFile(labelFile);
					//fileOpened = true;
					//nextPressed = false;                        

				}
			}
		});
		cancelButton=new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt) {
				dispose();
			}
		});
		nextButton = new JButton("OK");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				onOk(cl);
			}
		});
		cleanUpFile();
		constraints.fill = GridBagConstraints.HORIZONTAL; 

		buildConstraints(constraints, 0, 0, 1, 1, 0, 0);
		grid2.setConstraints(loadButton, constraints);
		bottomPanel.add(loadButton);

		buildConstraints(constraints, 1, 0, 1, 1, 0, 0);
		grid2.setConstraints(saveSettingsButton, constraints);
		bottomPanel.add(saveSettingsButton);
		buildConstraints(constraints, 2, 0, 1, 1, 0, 0);
		grid2.setConstraints(cancelButton, constraints);
		bottomPanel.add(cancelButton); 

		buildConstraints(constraints, 3, 0, 1, 1, 0, 0);
		grid2.setConstraints(nextButton, constraints);
		bottomPanel.add(nextButton);        

		constraints.fill = GridBagConstraints.HORIZONTAL;
		buildConstraints(constraints, 0, 1, 1, 1, 0, 0);
		gridbag.setConstraints(bottomPanel, constraints);
		pane.add(bottomPanel);

		this.setContentPane(pane);

		if (classifyGenes) {
			labelsAscItem = new JMenuItem[fieldNames.length];
			labelsDescItem =  new JMenuItem[fieldNames.length];
			for (int i = 0; i < fieldNames.length; i++) {
				labelsAscItem[i] = new JMenuItem(fieldNames[i]);
				labelsDescItem[i] = new JMenuItem(fieldNames[i]);
			}
		} else {
			labelsAscItem = new JMenuItem[1];
			labelsAscItem[0] = new JMenuItem("Sample Name");
			labelsDescItem = new JMenuItem[1];
			labelsDescItem[0] = new JMenuItem("Sample Name");
		}

		for (int i = 0; i < labelsAscItem.length; i++) {
			labelsAscItem[i].addActionListener(new SortListener(true, false));
			labelsDescItem[i].addActionListener(new SortListener(false, false));
		}

		classItem = new JMenuItem[numClasses + 1];

		for (int i = 0; i < numClasses; i++) {
			classItem[i] = new JMenuItem("Class " + (i + 1));
		}

		classItem[numClasses] = new JMenuItem("Neutral");

		for (int i = 0; i < classItem.length; i++) {
			classItem[i].addActionListener(new AssignListener());
		}
	}
	/** Closes the dialog */
	private void closeDialog(WindowEvent evt) {
		setVisible(false);
		//cancelPressed = true;                        
		dispose();
	}

	public void displayScrollPane(JPanel panel){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		//JFrame.setDefaultLookAndFeelDecorated(true);
		//Create and set up the window.

		//JDialog frame = new JDialog(new JFrame(), "Results from Weka", true);
		resultFrame = new JDialog(mainFrame, "Results from Weka", false);
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame
		resultFrame.getContentPane().add(panel);
		resultFrame.pack();
		resultFrame.setLocation((screenSize.width-getSize().width)/2,(screenSize.height-getSize().height)/2);
		resultFrame.setVisible(true);
	}

	/**
	 * Panel to create nets for different user given thresholds
	 * @return
	 */
	public JPanel getScrollPanePanel(){
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

		// Call Webstart wuth Files
		CytoscapeWebstart.onWebstartCytoscape(networkFiles);

		final JPanel evalPanel = new JPanel();
		evalPanel.setLayout(new BorderLayout());
		evalPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		// The evalStr is now shown in the Viewer
		if(isBootstraping){
			updateNetwork.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					onUpdateNetwork();
					/* Try Cytoscape Broadcast */
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
	 * Core function to run BN with weka on the selected cluster
	 * @param cl
	 */
	protected void onOk(Cluster cl) {
		BNClassificationEditor.this.dispose(); 
		saveToFile(basePath+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP+"label");
		if(labelFile != null) {
			saveToFile(labelFile);
		}
		saveWekaData(cl,framework,basePath+BNConstants.SEP+BNConstants.TMP_DIR);
		props = tranSaveWeka(numBin,basePath+BNConstants.SEP+BNConstants.TMP_DIR, isBootstraping, numIterations);

		Thread thread = new Thread( new Runnable(){
			public void run(){
				try{     
					Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
					runProgressPanel = new RunWekaProgressPanel();
					runProgressPanel.setString("Running Network Search");
					runProgressPanel.setIndeterminate(true);
					runProgressPanel.setLocation((screenSize.width-getSize().width)/2,(screenSize.height-getSize().height)/2);
					runProgressPanel.setVisible(true);

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
					String lmNetFile = basePath + BNConstants.RESULT_DIR + BNConstants.SEP + System.getProperty("LM_ONLY");
					networkFiles.add(0,lmNetFile);

					// BN Observed
					String obsNetFile = basePath + BNConstants.RESULT_DIR + BNConstants.SEP + Useful.getUniqueFileID()+ sAlgorithm + "_" + sType + "_";
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
						FromWekaToSif.fromWekaBifToXgmml(XmlBifStr, obsNetFile, probeIndexAssocHash, data);
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
							// evalStr = Evaluation.evaluateModel(new BayesNet(), argsWeka);
							bnNetOrg = new BayesNet();
							evalStrs[i] = Evaluation.evaluateModel(bnNetOrg, argsWeka);
							bnNetOrg.estimateCPTs();
							XmlBifStrs[i] = bnNetOrg.toXMLBIF03();
							//evalStr = evalStrs[i];
							System.out.println("Bootstrap Itr: " + i);
							//if(BNGUI.cancelRun)
							//break;
						}
						//if(!BNGUI.cancelRun)
						bootNetFile = createNetworkFromBootstraps(XmlBifStrs, evalStrs, numIterations, outarffbase);
					}
				} catch(OutOfMemoryError ofm){
					runProgressPanel.dispose();
					System.out.println("Error: Out of Memory..");
					ofm.printStackTrace();
					JOptionPane.showMessageDialog(new JFrame(), ofm.getMessage() + "\n Out of Memory", "Error - Out of Memory. Cannot Continue!", JOptionPane.ERROR_MESSAGE);
					BNGUI.run = true;
					BNGUI.cancelRun = true;
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
					BNGUI.run = true;
					BNGUI.cancelRun = true;
				}
				runProgressPanel.dispose();
				//if(!BNGUI.cancelRun)
				displayScrollPane(getScrollPanePanel());
				BNGUI.run = true;
			}
		});
		thread.start();
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
	private String createNetworkFromBootstraps(String[] xmlBifStrs, String[] evalStrs, int numItr, String outarffbase) {
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
			bifCpt = learnCPTFromFixedNetwork(interactions);
		} catch (Exception e1) {
			// TODO Create JOptionPane Msg ??
			e1.printStackTrace();
		}
		
		//End Code block to learn CPTs
		String bootNetFile = basePath+BNConstants.SEP+BNConstants.RESULT_DIR+BNConstants.SEP
							+Useful.getUniqueFileID()+ sAlgorithm + "_" + sType + "_" 
							+ "boot_result_" + numIterations + "_" + confThreshold;
		String bootCptFile = bootNetFile + "_CPT.xml";
		
		//Write CPT file for confidence threshold
		FileOutputStream fos;
		PrintWriter pw;
		try {
			fos = new FileOutputStream(bootCptFile);
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
			bootNetFile += ".sif";
			try {
				fos = new FileOutputStream(bootNetFile);
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
			bootNetFile += ".xgmml";
			
			try {
				//TODO Move from edgesTable method
				//FromWekaToSif.fromWekaToXgmml(edgesTable, numItr, confThreshold, bootNetFile, probeIndexAssocHash, data);
				FromWekaToSif.fromWekaBifToXgmml(bifCpt, bootNetFile, probeIndexAssocHash, data);
			} catch (Exception e) {
				//throw new Exception("Error creating XGML File from Bootstrap");
				e.printStackTrace();
			}
		}
		return bootNetFile;
	}

	private void writeCPTFromFixedNetwork(Vector<String> interactions) throws Exception {
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
		String outCPTBifXML = basePath+BNConstants.SEP+BNConstants.RESULT_DIR+BNConstants.SEP + "FixedNetWithCPT.xml";
		FileOutputStream fos = new FileOutputStream(outCPTBifXML);
		PrintWriter pw = new PrintWriter(fos, true);
		pw.print(bnNet.toXMLBIF03());
		pw.close();
		fos.close();
		
	}
	
	/**
	 * Learns the CPT of a fixed network
	 * @param interactions
	 * @return
	 * @throws Exception
	 */
	private String learnCPTFromFixedNetwork(Vector<String> interactions) throws Exception {
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
	 * Called when Update Network button is clicked
	 * Creates interactions & network files
	 *
	 */
	protected void onUpdateNetwork() {
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
				bifCpt = learnCPTFromFixedNetwork(interactionsfinal);
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
				FromWekaToSif.fromWekaBifToXgmml(bifCpt, _bootNetFile, probeIndexAssocHash, data);
			}

			
			if(finalThreshBox.isSelected()) {
				//Create File & interaction adges
				
				System.out.println("Boot threshold: " + confThres);
				System.out.println("Boot file: " + _bootNetFile);

				//store the final thresh and the file name
				finalBootFile = _bootNetFile;
				networkFiles.add(0,finalBootFile);
				
				if(interactionsfinal.size() == 0) {
					JOptionPane.showMessageDialog(this, "No valid network for selected threshold\n Will try to use last selected threshold.", "Warning", JOptionPane.INFORMATION_MESSAGE);
					if(interactionsPrefinal != null) {
						if(interactionsPrefinal.size() > 0) {
							JOptionPane.showMessageDialog(this, "Last network not avaialble. Aborting.", "Warning", JOptionPane.INFORMATION_MESSAGE);
							return;
						}
					} else {
						JOptionPane.showMessageDialog(this, "Last network not avaialble. Aborting.", "Warning", JOptionPane.INFORMATION_MESSAGE);
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
			directionals.add(false);
			interactions.add(fromTo);
		}
		framework.broadcastNetwork(interactions, types, directionals);
	}

	/**
	 * Cleans up the tmp directory
	 *
	 */
	private void cleanUpFile(){
		File file;
		String path = basePath + BNConstants.TMP_DIR;
		System.out.println("Cleaning UP temporary files at: " + path);
		try {
			file = new File(path);
		} catch (Exception e) {
			file = null;
			e.printStackTrace();
		}
		if (file != null) {
			String files[] = file.list();
			for(int x = 0; x < files.length ; x ++){
				File f = new File (files[x]);
				if(f.isFile()){
					f.delete();
				}else{
					//System.out.println("Not a file to delete");
				}
			} 
		} else {
			System.out.println("No such Location");
		}
		System.out.println("Done !!");
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
	 * Writes out observations in terms of UID & class groups
	 * E.g. Ref-Seq class1 class1 class2 class2
	 * @param cl
	 * @param frame
	 * @param path
	 */
	public void saveWekaData(Cluster cl, IFramework frame,String path) {
		int genes=cl.getIndices().length;
		//System.out.print(genes);
		IData data=frame.getData();
		int[] rows = new int[genes];
		rows=cl.getIndices();
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
			accList=convertFromFile(path);
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
	public Properties tranSaveWeka(String binNum,String path, boolean bootStrap, int numIter){
		return PrepareArrayDataModule.prepareArrayData(path+BNConstants.SEP+"wekaData", binNum, bootStrap, numIter, this.numClasses); 
	}

	/**
	 * 
	 * @param gbc
	 * @param gx
	 * @param gy
	 * @param gw
	 * @param gh
	 * @param wx
	 * @param wy
	 */
	void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy) {
		gbc.gridx = gx;
		gbc.gridy = gy;
		gbc.gridwidth = gw;
		gbc.gridheight = gh;
		gbc.weightx = wx;
		gbc.weighty = wy;
	}

	/**
	 * 
	 * @param visible
	 */
	public void showModal(boolean visible) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
		//showWarningMessage();     
		super.setVisible(visible);        
	}

	class BNClassifierTable extends JTable {}

	class BNClassTableModel extends AbstractTableModel {
		String[] columnNames;
		Object tableData[][];
		int indexLastClass;

		public BNClassTableModel() {
			indexLastClass = numClasses;
			if (classifyGenes) {
				columnNames = new String[fieldNames.length + numClasses + 2];
				columnNames[0] = "Index";
				for (int i = 0; i < numClasses; i++) {
					columnNames[i + 1] = "Class " + (i+1);
				}
				columnNames[numClasses + 1] = "Neutral";

				for (int i = 0; i < fieldNames.length; i++) {
					columnNames[numClasses + 2 + i] = fieldNames[i];
				}

				tableData = new Object[numGenes][columnNames.length];

				for (int i = 0; i < tableData.length; i++) {
					for (int j = 0; j < columnNames.length; j++) {
						if (j == 0) {
							tableData[i][j] = new Integer(i);
						} else if ((j > 0) && (j < (numClasses + 1))) {
							tableData[i][j] = new Boolean(false);
						} else if (j == numClasses + 1) {
							tableData[i][j] = new Boolean(true);
						} else {
							tableData[i][j] = data.getElementAttribute(i, j - (numClasses + 2));
						}
					}
				}

			} else { // (!classifyGenes)
				columnNames = new String[numClasses + 3];
				columnNames[0] = "Index";
				for (int i = 0; i < numClasses; i++) {
					columnNames[i + 1] = "Class " + (i+1);
				}
				columnNames[numClasses + 1] = "Neutral";
				columnNames[numClasses + 2] = "Sample Name";
				tableData = new Object[numExps][columnNames.length];

				for (int i = 0; i < tableData.length; i++) {
					for (int j = 0; j < columnNames.length; j++) {
						if (j == 0) {
							tableData[i][j] = new Integer(i);
						} else if ((j > 0) && (j < (numClasses + 1))) {
							tableData[i][j] = new Boolean(false);
						} else if (j == numClasses + 1) {
							tableData[i][j] = new Boolean(true);
						} else if (j == numClasses + 2) {
							tableData[i][j] = data.getFullSampleName(i);
						}
					}
				}
			}

			origData = new Object[tableData.length][tableData[0].length];

			for (int i = 0; i < tableData.length; i++) {
				for (int j = 0; j < tableData[0].length; j++) {
					origData[i][j] = tableData[i][j];
				}
			}
		}


		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return tableData.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public int getColumnIndex(String name) {
			int i;
			for (i = 0; i < columnNames.length; i++) {
				if (columnNames[i].equals(name)) {
					break;
				}
			}
			if (i < columnNames.length) {
				return i;
			} else {
				return -1;
			}
		}

		public Object getValueAt(int row, int col) {
			return tableData[row][col];
		}

		public void setValueAt(Object value, int row, int col) {
			tableData[row][col] = value;
			//fireTableCellUpdated(row, col);
			this.fireTableChanged(new TableModelEvent(this, row, row, col));
		}


		public Class getColumnClass(int c) {
			if (c == 0) {
				return java.lang.Integer.class;
			} else if ((c > 0) && (c <= (numClasses + 1))) {
				return java.lang.Boolean.class;
			} else {
				return getValueAt(0, c).getClass();
			}
		}


		public boolean isCellEditable(int row, int col) {
			//Note that the data/cell address is constant,
			//no matter where the cell appears onscreen.
			if ((col > 0) && (col <= (numClasses + 1))) {
				return true;
			} else {
				return false;
			}
		}
	}


	class BNClassTableColumnModel implements TableColumnModel {

		TableColumnModel tcm;

		public BNClassTableColumnModel(TableColumnModel TCM) {
			this.tcm = TCM;
		}

		public void addColumn(javax.swing.table.TableColumn tableColumn) {
			tcm.addColumn(tableColumn);
		}

		public void addColumnModelListener(javax.swing.event.TableColumnModelListener tableColumnModelListener) {
			tcm.addColumnModelListener(tableColumnModelListener);
		}

		public javax.swing.table.TableColumn getColumn(int param) {
			return tcm.getColumn(param);
		}

		public int getColumnCount() {
			return tcm.getColumnCount();
		}

		public int getColumnIndex(Object obj) {
			return tcm.getColumnIndex(obj);
		}

		public int getColumnIndexAtX(int param) {
			return tcm.getColumnIndexAtX(param);
		}

		public int getColumnMargin() {
			return tcm.getColumnMargin();
		}

		public boolean getColumnSelectionAllowed() {
			return tcm.getColumnSelectionAllowed();
		}

		public java.util.Enumeration getColumns() {
			return tcm.getColumns();
		}

		public int getSelectedColumnCount() {
			return tcm.getSelectedColumnCount();
		}

		public int[] getSelectedColumns() {
			return tcm.getSelectedColumns();
		}

		public javax.swing.ListSelectionModel getSelectionModel() {
			return tcm.getSelectionModel();
		}

		public int getTotalColumnWidth() {
			return tcm.getTotalColumnWidth();
		}

		public void moveColumn(int from, int to) {
			if (from <= (numClasses + 1) || to <= (numClasses + 1)) {
				return;
			} else {
				tcm.moveColumn(from, to);
			}
		}

		public void removeColumn(javax.swing.table.TableColumn tableColumn) {
			tcm.removeColumn(tableColumn);
		}

		public void removeColumnModelListener(javax.swing.event.TableColumnModelListener tableColumnModelListener) {
			tcm.removeColumnModelListener(tableColumnModelListener);
		}

		public void setColumnMargin(int param) {
			tcm.setColumnMargin(param);
		}

		public void setColumnSelectionAllowed(boolean param) {
			tcm.setColumnSelectionAllowed(param);
		}

		public void setSelectionModel(javax.swing.ListSelectionModel listSelectionModel) {
			tcm.setSelectionModel(listSelectionModel);
		}

	}    


	class ClassSelectionListener implements TableModelListener {

		public void tableChanged(TableModelEvent tme) {
			//TableModel tabMod = (TableModel)tme.getSource();
			int selectedCol = tme.getColumn(); //
			int selectedRow = tme.getFirstRow(); //

			if ((selectedCol < 1) || (selectedCol > (numClasses + 1) )) {
				return;
			}

			if( verifySelected(selectedRow, selectedCol)){
				changeNeighbors(selectedRow, selectedCol);
			}

			int origDataRow = ((Integer)(kModel.getValueAt(selectedRow, 0))).intValue();

			origData[origDataRow][selectedCol] = new Boolean(true);

			for (int i = 1; i <= (numClasses + 1); i++) {
				if (i != selectedCol) {
					origData[origDataRow][i] = new Boolean(false);
				}
			}
		}

		private void changeNeighbors(int first, int col){
			for (int i = 1; i <= (numClasses + 1); i++) {
				if (i != col) {
					BNClassTable.setValueAt(new Boolean(false), first, i);
					//origData[first][i] = new Boolean(false); 
				}
			}
		}

		private boolean verifySelected(int row, int col){

			boolean selVal = ((Boolean)BNClassTable.getValueAt(row,col)).booleanValue();
			//boolean value1, value2;

			if(selVal == true){
				return true;
			} else {
				Vector truthValues = new Vector();
				for (int i = 1; i <=(numClasses + 1); i++) {
					if (i != col) {
						boolean value = ((Boolean)(BNClassTable.getValueAt(row,i))).booleanValue();
						truthValues.add(new Boolean(value));
					}
				}
				boolean val1 = true;
				for (int i = 0; i < truthValues.size(); i++) {
					boolean val2 = ((Boolean)(truthValues.get(i))).booleanValue();
					if (val2 == true) {
						val1 = false;
						break;
					}
				}

				if (val1 == true) {
					BNClassTable.setValueAt(new Boolean(true), row, col);
					//origData[row][col] = new Boolean(true);
				}

			}
			return false;

			/*else {
                BNClassTable.setValueAt(new Boolean(true), selectedRow, selectedCol);

                for (int i = 1; i <= (numClasses + 1); i++) {
                    if (i != selectedCol) {
                        BNClassTable.setValueAt(new Boolean(false), selectedRow, i);
                    }
                }

            }
			 */

		}

	}

	public void sortByColumn(int column, boolean ascending, boolean originalOrder) {
		if (originalOrder) {
			//double[] indices = new int[kModel.getRowCount()];
			//for (int i = 0; i < kModel.getRowCount(); i++) {
			//indices[i] = ((Integer)(kModel.getValueAt(i, 0))).doubleValue();
			/*
                QSort sortIndices = new QSort(indices);
                int[] sorted = sortIndices.getOrigIndx();
			 */
			Object[][] sortedData = new Object[kModel.getRowCount()][kModel.getColumnCount()];

			for (int i = 0; i < sortedData.length; i++) {
				for (int j = 0; j < sortedData[0].length; j++) {
					sortedData[i][j] = origData[i][j];
				}
			}

			for (int i = 0; i < sortedData.length; i++) {
				for (int j = 0; j < sortedData[0].length; j++) {
					kModel.setValueAt(sortedData[i][j], i, j);
				}
				validateTable(sortedData, i);
			}
			return;
			//}
			/*
            for (int i = 0; i < kModel.getRowCount(); i++) {
                for (int j = 0; j < kModel.getColumnCount(); j++) {
                    kModel.setValueAt(origData[i][j], i, j);
                }
                validateTable(origData, i);
            }
            return;
			 */
		}
		if ((column < 0)|| (column > kModel.getColumnCount())) {
			return;
		}
		Object[][] sortedData = new Object[kModel.getRowCount()][kModel.getColumnCount()];
		//float[] origArray = new float[kModel.getRowCount()];
		SortableField[] sortFields = new SortableField[kModel.getRowCount()];

		for (int i = 0; i < sortFields.length; i++) {
			int origDataRow = ((Integer)(kModel.getValueAt(i, 0))).intValue();
			sortFields[i] = new SortableField(origDataRow, column);
		}
		Arrays.sort(sortFields);
		int[] sortedIndices = new int[sortFields.length];
		for (int i = 0; i < sortedIndices.length; i++) {
			sortedIndices[i] = sortFields[i].getIndex();
		}
		if (!ascending) {
			sortedIndices = reverse(sortedIndices);
		}

		for (int i = 0; i < sortedData.length; i++) {
			for (int j = 0; j < sortedData[i].length; j++) {
				//sortedData[i][j] = tModel.getValueAt(sortedMeansAIndices[i], j);
				sortedData[i][j] = origData[sortedIndices[i]][j];
			}
		}

		for (int i = 0; i < sortedData.length; i++) {
			for (int j = 0; j < sortedData[i].length; j++) {
				kModel.setValueAt(sortedData[i][j], i, j);
			}
			validateTable(sortedData, i);
		}

		BNClassTable.removeRowSelectionInterval(0, BNClassTable.getRowCount() - 1);
	}

	private int[] reverse(int[] arr) {
		int[] revArr = new int[arr.length];
		int  revCount = 0;
		int count = arr.length - 1;
		for (int i=0; i < arr.length; i++) {
			revArr[revCount] = arr[count];
			revCount++;
			count--;
		}
		return revArr;
	}

	private void sortByClassification() {
		Vector[] classVectors = new Vector[numClasses + 1];
		for (int i = 0; i < classVectors.length; i++) {
			classVectors[i] = new Vector();
		}

		for (int i = 0; i < kModel.getRowCount(); i++) {
			for (int j = 1; (j <= numClasses + 1); j++) {
				boolean b = ((Boolean)(kModel.getValueAt(i, j))).booleanValue();
				if (b) {
					classVectors[j - 1].add(new Integer(i));
					break;
				}
			}
		}

		int[] sortedIndices = new int[kModel.getRowCount()];
		int counter = 0;

		for (int i = 0; i < classVectors.length; i++) {
			for (int j = 0; j < classVectors[i].size(); j++) {
				sortedIndices[counter] = ((Integer)(classVectors[i].get(j))).intValue();
				counter++;
			}
		}

		Object sortedData[][] = new Object[kModel.getRowCount()][kModel.getColumnCount()];

		for (int i = 0; i < sortedData.length; i++) {
			for (int j = 0; j < sortedData[0].length; j++) {
				sortedData[i][j] = kModel.getValueAt(sortedIndices[i], j);
			}
		}

		for (int i = 0; i < kModel.getRowCount(); i++) {
			for (int j = 0; j < kModel.getColumnCount(); j++) {
				kModel.setValueAt(sortedData[i][j], i, j);
			}
			validateTable(sortedData, i);
		}

		BNClassTable.removeRowSelectionInterval(0, BNClassTable.getRowCount() - 1);        
	}

	private void validateTable(Object[][] tabData, int row) {
		for (int i = 1; i <= (numClasses + 1); i++) {
			boolean check = ((Boolean)(tabData[row][i])).booleanValue();
			if (check) {
				kModel.setValueAt(new Boolean(true), row, i);
				break;
			}
		}
	}

	private void searchTable(){

		//searchDialog.setVisible(true);
		//searchDialog.toFront();
		//searchDialog.requestFocus();
		//searchDialog.setLocation(this.getLocation().x + 100, this.getLocation().y +100);

	}    

	private void saveToFile(String file) {
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream(new File(file)));
			for (int i = 0; i < kModel.getRowCount(); i++) {
				out.print(((Integer)(kModel.getValueAt(i, 0))).intValue());
				out.print("\t");
				for (int j = 1; j <= numClasses; j++) {
					if (((Boolean)(kModel.getValueAt(i, j))).booleanValue()) {
						out.print(j);
						label[i]=(new Integer(j)).toString();
						break;
					}
				}
				if (((Boolean)(kModel.getValueAt(i, numClasses + 1))).booleanValue()) {
					label[i]=(new Integer(-1)).toString();
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

	private void saveToFile(File file) {
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream(file));
			for (int i = 0; i < kModel.getRowCount(); i++) {
				out.print(((Integer)(kModel.getValueAt(i, 0))).intValue());
				out.print("\t");
				for (int j = 1; j <= numClasses; j++) {
					if (((Boolean)(kModel.getValueAt(i, j))).booleanValue()) {
						out.print(j);
						label[i]=(new Integer(j)).toString();
						break;
					}
				}
				if (((Boolean)(kModel.getValueAt(i, numClasses + 1))).booleanValue()) {
					label[i]=(new Integer(-1)).toString();
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

	public void loadFromFile (File file) {
		Vector indicesVector = new Vector();
		Vector classVector = new Vector();
		try {
			BufferedReader buff = new BufferedReader(new FileReader(file)); 
			String line = new String();
			StringSplitter st;           

			while ((line = buff.readLine()) != null) {
				st = new StringSplitter('\t');
				st.init(line);
				String currIndex = st.nextToken();
				indicesVector.add(new Integer(currIndex));
				String currClass = st.nextToken();
				classVector.add(new Integer(currClass));
			}

			for (int i = 0; i < indicesVector.size(); i++) {
				int currInd = ((Integer)(indicesVector.get(i))).intValue();
				int currCl = ((Integer)(classVector.get(i))).intValue();

				if (currCl == (-1)) {
					kModel.setValueAt(new Boolean(true), currInd, (numClasses + 1));
				} else {
					kModel.setValueAt(new Boolean(true), currInd, currCl);
				}
			}  
			BNClassificationEditor.this.showModal(true);

			//BNClassificationEditor.this.setVisible(true);
			// showWarningMessage();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(framework.getFrame(), "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
			incompatible = true;
			//BNClassificationEditor.this.dispose();
			//e.printStackTrace();
		}
		/*
        for (int i = 0; i < indicesVector.size(); i++) {
            int currInd = ((Integer)(indicesVector.get(i))).intValue();
            int currCl = ((Integer)(classVector.get(i))).intValue();
        }
		 */
	}

	public Vector[] getClassification() {
		Vector indicesVector = new Vector();
		Vector classVector = new Vector();
		Vector[] vectArray = new Vector[2];

		for (int i = 0; i < kModel.getRowCount(); i++) {
			if (((Boolean)(kModel.getValueAt(i, numClasses + 1))).booleanValue()) {
				continue;
			} else {
				indicesVector.add((Integer)(kModel.getValueAt(i, 0)));
				classVector.add(new Integer(getClass(i)));
			}
		}

		vectArray[0] = indicesVector;
		vectArray[1] = classVector;
		return vectArray;
	}

	public boolean isNextPressed() {
		return nextPressed;
	}

	private int getClass(int row) {
		int i;
		for (i = 1; i <= numClasses + 1; i++) {
			if (((Boolean)(kModel.getValueAt(row, i))).booleanValue()) {
				break;
			}
		}

		return i;
	}

	public boolean proceed() {
		return !(stopHere);
	}

	public boolean fileIsIncompatible() {
		return incompatible;
	}

	private class SortableField implements Comparable {
		private String field;
		private int index;

		SortableField(int index, int column) {
			this.index = index;
			this.field = (String)(origData[index][column]);
			//System.out.println("SortableField[" + index + "][" + column + "]: index = " + index + ", field = " + field);
		}

		public int compareTo(Object other) {
			SortableField otherField = (SortableField)other;
			return this.field.compareTo(otherField.getField());
		}

		public int getIndex() {
			return this.index;
		}
		public String getField() {
			return this.field;
		}
	}

	public class AssignListener implements ActionListener {

		public void actionPerformed(ActionEvent evt) {
			Object source = evt.getSource();

			if (source instanceof JMenuItem) {
				String key = ((JMenuItem)source).getText();
				int classCol = kModel.getColumnIndex(key);
				int[] selectedRows = BNClassTable.getSelectedRows();
				int[] selectedIndices = new int[selectedRows.length];

				for (int i = 0; i < selectedRows.length; i++) {
					kModel.setValueAt(new Boolean(true), selectedRows[i], classCol);
					//int currIndex = ((Integer)(kModel.getValueAt(selectedRows[i], 0))).intValue();
					//origData[currIndex][classCol] = new Boolean(true);
				}
			}
		}

	}

	public class SortListener implements ActionListener {
		boolean asc, origOrd;
		public SortListener(boolean asc, boolean origOrd) {
			this.asc = asc;
			this.origOrd = origOrd;
		}

		public void actionPerformed(ActionEvent evt) {
			Object source = evt.getSource();

			if (source instanceof JMenuItem) {
				String key = ((JMenuItem)source).getText();
				int colToSort = kModel.getColumnIndex(key);
				sortByColumn(colToSort, asc, origOrd);
			}
		}
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
}
