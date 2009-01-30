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
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.ParserConfigurationException;

import org.tigr.microarray.mev.HistoryViewer;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.impl.bn.getInteractions.GetInteractionsModule;
import org.tigr.microarray.mev.cluster.gui.impl.bn.prepareXMLBif.PrepareXMLBifModule;
import org.tigr.microarray.mev.cluster.gui.impl.lm.LMGUI;
import org.tigr.microarray.mev.resources.AvailableAnnotationsFileDefinition;
import org.tigr.microarray.mev.resources.ISupportFileDefinition;
import org.tigr.microarray.mev.resources.SupportFileAccessError;

public class BNGUI implements IClusterGUI {
	public static final int GENE_CLUSTER = 0;
	public static boolean done = false;
	public static boolean run = false;
	public static boolean cancelRun = false;
	public static boolean prior = true;

	HashMap<String, String> probeIndexAssocHash = new HashMap<String, String>();
	HistoryViewer wekaOutputViewer;
	LMBNViewer fileViewer;
	IData data;
	public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
		
		//Test Code
		if(1==1) {
			try {
				BifDOMBuilder bdb = new BifDOMBuilder();
				bdb.build("C:/Projects/Mev/MeV_SVN/data/BN_files/affy_HG-U133_Plus_2_BN/results/FixedNetWithCPT.xml");
				System.out.println("Testing Complete BifDOMBuilder");
			} catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		//End Test
		done = false;
		run = false;
		cancelRun = false;
		prior = true;

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
			prior = false;
			done = true;
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
			BNGUI.done = false;
			System.out.println("Out of Memory. Aborting...");
			JOptionPane.showMessageDialog(framework.getFrame(), ofm.getMessage() + "\n Out of Memory", "Error - Out of Memory. Aborting!", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(framework.getFrame(), e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
			BNGUI.done = false;
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
					BNGUI.done = false;
					pgPanel.dispose();
					return null;
				} 
			}
			prepareXMLBifFile(dialog.getBaseFileLocation());
			BNGUI.done = true;
			pgPanel.dispose();
		} else {
			BNGUI.done = false;
			pgPanel.dispose();
			return null;
		}
		
		BNClassificationEditor bnEditor = new BNClassificationEditor(framework,false,dialog.getSelectedCluster(),(new Integer(dialog.getNumberBin())).toString(),dialog.getNumberClass(),dialog.numParents(),dialog.getAlgorithm(),dialog.getScoreType(),dialog.useArcRev(), dialog.isBootstrapping(), dialog.getNumIterations(), dialog.getConfThreshold(), dialog.getKFolds(), dialog.getBaseFileLocation(), probeIndexAssocHash);
		bnEditor.showModal(true);

		while(!BNGUI.run){
			try{
				Thread.sleep(500);	
			}catch(InterruptedException x){
				//ignore;
			}
		}
		
		if(BNGUI.cancelRun)
			return null;
		
		//Added to record the Weka output for Observed BN analysis
		wekaOutputViewer = new HistoryViewer();
		String wekaResult = bnEditor.getWekaEvalString();
		wekaOutputViewer.addHistory(wekaResult);

		GeneralInfo info = new GeneralInfo();
		if(dialog.isBoth()){
			info.prior="LM & PPI";
		}else if (dialog.isKEGG()) {
			info.prior="KEGG";
		}else if(dialog.isAll()){
			info.prior="LM, PPI & KEGG";
		}else if(dialog.isLitAndKegg()){
			info.prior="LM & KEGG";
		}else if(dialog.isPpiAndKegg()){ 
			info.prior="PPI & KEGG";
		}else if(dialog.isPPI()){
			info.prior="PPI";
		}else if(dialog.isLit()){
			info.prior="Literature Mining";
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
		fileViewer = createLMBNViewer(bnEditor.getNetworkFiles());
		return createResultTree(exp, fileViewer, wekaOutputViewer, info);
		//return root;
	}

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
		if(lit){			//getModule.test(path+sep+"getInterModLit.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path + 
					BNConstants.SEP + 
					BNConstants.TMP_DIR + 
					BNConstants.SEP + 
					BNConstants.LIT_INTER_MODULE_FILE, data);
		}
		if(ppi){			//getModule.test(path+sep+"getInterModPPIDirectly.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.PPI_INTER_MODULE_DIRECT_FILE, data); 
		}
		if(LitPpi){			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.BOTH_INTER_MODULE_FILE, data);
		}
		if(kegg){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.KEGG_INTER_MODULE_FILE, data);
		}
		if(LitKegg){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.LIT_KEGG_INTER_MODULE_FILE, data);
		}
		if(KeggPpi){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.PPI_KEGG_INTER_MODULE_FILE, data);
		}
		if(LitPpiKegg){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.PPI_KEGG_LIT_INTER_MODULE_FILE, data);
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
	
}//end class
