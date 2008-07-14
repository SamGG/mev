/*
 * Created on Aug 30, 2005
 * @author Raktim
 */
package org.tigr.microarray.mev.cluster.gui.impl.lm;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.impl.bn.BNConstants;
import org.tigr.microarray.mev.cluster.gui.impl.bn.BNGUI;
import org.tigr.microarray.mev.cluster.gui.impl.bn.CytoscapeWebstart;
import org.tigr.microarray.mev.cluster.gui.impl.bn.LMBNViewer;
import org.tigr.microarray.mev.cluster.gui.impl.bn.RunWekaProgressPanel;
import org.tigr.microarray.mev.cluster.gui.impl.bn.BNGUI.GeneralInfo;
import org.tigr.microarray.mev.cluster.gui.impl.bn.getInteractions.GetInteractionsModule;
import org.tigr.microarray.mev.cluster.gui.impl.bn.prepareXMLBif.PrepareXMLBifModule;

public class LMGUI implements IClusterGUI {
	public static final int GENE_CLUSTER = 0;
	public static boolean done = false;
	public static boolean cancelRun = false;
	public static boolean prior = true;
	RunWekaProgressPanel runProgressPanel;
	LMBNViewer fileViewer;
	Vector<String> networkFiles = new Vector<String>();
	
	public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
		done = false;
		cancelRun = false; 
		prior = true;
		DefaultMutableTreeNode root = new DefaultMutableTreeNode( "LM" );
		IData data = framework.getData();
		Experiment exp = data.getExperiment();
		ClusterRepository repository = framework.getClusterRepository(Cluster.GENE_CLUSTER);
		final LiteratureMiningDialog dialog = new LiteratureMiningDialog(framework, repository, framework.getData().getFieldNames());
		if(dialog.showModal() != JOptionPane.OK_OPTION)
			return null;
		
		if(dialog.isNone()){
			prior = false;
			done = true;
		}

		if(dialog.isNone()){
			return null;
		}
		
		runProgressPanel = new RunWekaProgressPanel();
		runProgressPanel.setIndeterminate(true);
		runProgressPanel.setLocationRelativeTo(framework.getFrame());
		//pgPanel.setLocation((screenSize.width-framework.getFrame().getSize().width)/2,(screenSize.height-framework.getFrame().getSize().height)/2);
		runProgressPanel.setVisible(true);
		
		converter(dialog.getSelectedCluster(),framework,dialog.getBaseFileLocation());
		String kegg_sp = dialog.getKeggSpecies();
		if(kegg_sp != null) kegg_sp = kegg_sp.trim();
		else kegg_sp = "na";
		
		String basePath = dialog.getBaseFileLocation() + BNConstants.SEP;
		converter(dialog.getSelectedCluster(),framework,dialog.getBaseFileLocation());
		
		//Build Property file for Weka Params
		//buildPropertyFile(dialog.isLit(),dialog.isPPI(),dialog.isBoth(),dialog.useGoTerm(),dialog.getBaseFileLocation());
		buildPropertyFile(dialog.isLit(),dialog.isPPI(),dialog.isKEGG(), dialog.isBoth(), dialog.isLitAndKegg(), dialog.isPpiAndKegg(), dialog.isAll(),dialog.useGoTerm(),dialog.getBaseFileLocation(),kegg_sp);
		Thread thread1 = new Thread( new Runnable(){
			public void run(){	
				//literatureMining(dialog.isLit(),dialog.isPPI(),dialog.isBoth(),dialog.getBaseFileLocation());
				int status = -1;
				try {
					status = literatureMining(dialog.isLit(), dialog.isPPI(), dialog.isKEGG(), dialog.isBoth(), dialog.isLitAndKegg(), dialog.isPpiAndKegg(), dialog.isAll(),dialog.getBaseFileLocation());
				} 
				catch(OutOfMemoryError ofm){
					runProgressPanel.dispose();
					LMGUI.done = false;
					System.out.println("Out of Memory. Aborting...");
					JOptionPane.showMessageDialog(new JFrame(), ofm.getMessage() + "\n Out of Memory", "Error - Out of Memory. Aborting!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
					LMGUI.done = false;
					runProgressPanel.dispose();
					return;
				}
				System.out.println("Interaction count: " + status);
				LMGUI.done = true;
				runProgressPanel.dispose();
			}
		});
		thread1.start();
		
		//Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		//runProgressPanel = new RunWekaProgressPanel();
		//runProgressPanel.setIndeterminate(true);
		//runProgressPanel.setLocation((screenSize.width-framework.getFrame().getSize().width)/2,(screenSize.height-framework.getFrame().getSize().height)/2);
		//runProgressPanel.setVisible(true);
		
		while(!LMGUI.done){
			try{				Thread.sleep(500);	
			}catch(InterruptedException x){
				//ignore;
			}
		}
		runProgressPanel.dispose();
		
		if(cancelRun)
			return null;

		done = false;
		
		//Create Viewers on a successfull run
		GeneralInfo info = new GeneralInfo();
		if(dialog.isBoth()){
			info.prior = "LM & PPI";
		}else if (dialog.isKEGG()) {
			info.prior = "KEGG";
		}else if(dialog.isAll()){
			info.prior = "LM, PPI & KEGG";
		}else if(dialog.isLitAndKegg()){
			info.prior = "LM & KEGG";
		}else if(dialog.isPpiAndKegg()){ 
			info.prior = "PPI & KEGG";
		}else if(dialog.isPPI()){
			info.prior = "PPI";
		}else if(dialog.isLit()){
			info.prior = "Literature Mining";
		}
		if(dialog.useGoTerm()){
			info.useGoTerms="Use GO Terms";
		}
		info.numGene = (dialog.getSelectedCluster()).getIndices().length;

		//Call Webstart wuth File(s)
		String lmFile = basePath + BNConstants.RESULT_DIR + BNConstants.SEP + System.getProperty("LM_ONLY");
		networkFiles.add(lmFile);
		CytoscapeWebstart.onWebstartCytoscape(networkFiles);

		return createResultTree(exp, info);

	}
	
	/**
	 * 
	 * @param experiment
	 * @param info
	 * @return
	 */
	private DefaultMutableTreeNode createResultTree(Experiment experiment, GeneralInfo info) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("LM");
		fileViewer = new LMBNViewer(this.networkFiles);
		root.add(new DefaultMutableTreeNode(new LeafInfo("Networks", fileViewer)));
		addGeneralInfo(root, info);
		return root;
	}
	
	/**
	 * 
	 * @param root
	 * @param info
	 */
	private void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
		node.add(new DefaultMutableTreeNode("Number of Genes: "+info.numGene));
		node.add(new DefaultMutableTreeNode("Prior: "+info.prior));
		root.add(node);
	}
	
	/**
	 * TODO Raktim
	 * Description:
	 * @param lit
	 * @param ppi
	 * @param both
	 * @param goTerms
	 * @param path
	 */
	private void buildPropertyFile(boolean lit,boolean ppi,boolean kegg, boolean LitPpi, boolean LitKegg, boolean KeggPpi, boolean LitPpiKegg,boolean goTerms,String path, String keggSpecies){
		//String sep= System.getProperty("file.separator");    
		final int fileSize = 8;
		String[] propFile = new String[fileSize];
		String[] outFile = new String[fileSize-1];
		
		propFile[0]= path+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP+BNConstants.LIT_INTER_MODULE_FILE;
		propFile[1]= path+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP+BNConstants.PPI_INTER_MODULE_DIRECT_FILE;
		propFile[2]= path+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP+BNConstants.BOTH_INTER_MODULE_FILE;
		propFile[3]= path+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP+BNConstants.KEGG_INTER_MODULE_FILE;
		propFile[4]= path+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP+BNConstants.LIT_KEGG_INTER_MODULE_FILE;
		propFile[5]= path+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP+BNConstants.PPI_KEGG_INTER_MODULE_FILE;
		propFile[6]= path+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP+BNConstants.LIT_PPI_KEGG_INTER_MODULE_FILE;
		propFile[7]= path+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP+BNConstants.XML_BIF_MODULE_FILE; 
		outFile[0] = BNConstants.LIT_INTER_FILE;
		outFile[1] = BNConstants.PPI_INTER_FILE; 
		outFile[2] = BNConstants.LIT_PPI_INTER_FILE;
		outFile[3] = BNConstants.KEGG_INTER_FILE;
		outFile[4] = BNConstants.LIT_KEGG_INTER_FILE;
		outFile[5] = BNConstants.PPI_KEGG_INTER_FILE;
		outFile[6] = BNConstants.LIT_PPI_KEGG_INTER_FILE;

		PrintWriter out = null;
		try{ 	 
			if(lit){
				out= new PrintWriter(new FileOutputStream(new File(propFile[0])));	 
				out.println(BNConstants.RES_FILE_NAME + "=" + BNConstants.RESOURCERER_FILE);
				out.println(BNConstants.GB_ACC_FILE_NAME + "=" + BNConstants.OUT_ACCESSION_FILE);
				out.println(BNConstants.SYM_ARTICLES_FRM_PUBMED + "=" + BNConstants.PUBMED_DB_FILE);
				out.println(BNConstants.SYM_ARTICLES_FRM_GENEDB + "=" + BNConstants.GENE_DB_FILE);
				out.println(BNConstants.ART_REM_THRESH + "=" + BNConstants.ART_REM_THRESH_VAL);		 
				out.println(BNConstants.FRM_LIT + "=true");
				out.println(BNConstants.FRM_PPI + "=false");
				out.println(BNConstants.FRM_KEGG + "=false");
				out.println(BNConstants.OUT_INTER_FILE_NAME + "=" +outFile[0]);
				out.flush();
				out.close();
			}
			if(ppi){
				out= new PrintWriter(new FileOutputStream(new File(propFile[1])));	 
				out.println(BNConstants.RES_FILE_NAME + "=" + BNConstants.RESOURCERER_FILE);
				out.println(BNConstants.GB_ACC_FILE_NAME + "=" + BNConstants.OUT_ACCESSION_FILE);
				out.println(BNConstants.SYM_ARTICLES_FRM_PUBMED + "=" + BNConstants.PUBMED_DB_FILE);
				out.println(BNConstants.SYM_ARTICLES_FRM_GENEDB + "=" + BNConstants.GENE_DB_FILE);
				out.println(BNConstants.ART_REM_THRESH + "=" + BNConstants.ART_REM_THRESH_VAL);		 	  
				out.println(BNConstants.FRM_LIT + "=false");
				out.println(BNConstants.FRM_PPI + "=true");
				out.println(BNConstants.FRM_KEGG + "=false");
				out.println(BNConstants.OUT_INTER_FILE_NAME + "=" + outFile[1]);
				out.println(BNConstants.USE_PPI_DIRECT + "=true");
				//out.println("usePpiOnlyWithin=true");
				out.println(BNConstants.PPI_FILE_NAME + "=" + BNConstants.PPI_FILE);
				out.flush();
				out.close();

			}
			if(LitPpi){
				out= new PrintWriter(new FileOutputStream(new File(propFile[2])));	 
				out.println(BNConstants.RES_FILE_NAME + "=" + BNConstants.RESOURCERER_FILE);
				out.println(BNConstants.GB_ACC_FILE_NAME + "=" + BNConstants.OUT_ACCESSION_FILE);
				out.println(BNConstants.SYM_ARTICLES_FRM_PUBMED + "=" + BNConstants.PUBMED_DB_FILE);
				out.println(BNConstants.SYM_ARTICLES_FRM_GENEDB + "=" + BNConstants.GENE_DB_FILE);
				out.println(BNConstants.ART_REM_THRESH + "=" + BNConstants.ART_REM_THRESH_VAL);		 	  
				out.println(BNConstants.FRM_LIT + "=true");
				out.println(BNConstants.FRM_PPI + "=true");
				out.println(BNConstants.FRM_KEGG + "=false");
				out.println(BNConstants.OUT_INTER_FILE_NAME + "=" + outFile[2]);
				out.println(BNConstants.PPI_FILE_NAME + "=" + BNConstants.PPI_FILE);
				out.flush();
				out.close();
			}
			if(kegg){
				out= new PrintWriter(new FileOutputStream(new File(propFile[3])));	 
				out.println(BNConstants.RES_FILE_NAME + "=" + BNConstants.RESOURCERER_FILE);
				out.println(BNConstants.GB_ACC_FILE_NAME + "=" + BNConstants.OUT_ACCESSION_FILE);
				out.println(BNConstants.KEGG_SPECIES + "=" + keggSpecies);
				out.println(BNConstants.SYM_ARTICLES_FRM_PUBMED + "=" + BNConstants.PUBMED_DB_FILE);
				out.println(BNConstants.SYM_ARTICLES_FRM_GENEDB + "=" + BNConstants.GENE_DB_FILE);
				//out.println(BNConstants.ART_REM_THRESH + "=" + BNConstants.ART_REM_THRESH_VAL);		 	  
				out.println(BNConstants.FRM_LIT + "=false");
				out.println(BNConstants.FRM_PPI + "=false");
				out.println(BNConstants.FRM_KEGG + "=true");
				out.println(BNConstants.OUT_INTER_FILE_NAME + "=" + outFile[3]);
				out.flush();
				out.close();
			}
			if(LitKegg){
				out= new PrintWriter(new FileOutputStream(new File(propFile[4])));	 
				out.println(BNConstants.RES_FILE_NAME + "=" + BNConstants.RESOURCERER_FILE);
				out.println(BNConstants.GB_ACC_FILE_NAME + "=" + BNConstants.OUT_ACCESSION_FILE);
				out.println(BNConstants.KEGG_SPECIES + "=" + keggSpecies);
				out.println(BNConstants.SYM_ARTICLES_FRM_PUBMED + "=" + BNConstants.PUBMED_DB_FILE);
				out.println(BNConstants.SYM_ARTICLES_FRM_GENEDB + "=" + BNConstants.GENE_DB_FILE);
				out.println(BNConstants.ART_REM_THRESH + "=" + BNConstants.ART_REM_THRESH_VAL);		 	  
				out.println(BNConstants.FRM_LIT + "=true");
				out.println(BNConstants.FRM_PPI + "=false");
				out.println(BNConstants.FRM_KEGG + "=true");
				out.println(BNConstants.OUT_INTER_FILE_NAME + "=" + outFile[4]);
				out.flush();
				out.close();
			}
			if(KeggPpi){
				out= new PrintWriter(new FileOutputStream(new File(propFile[5])));	 
				out.println(BNConstants.RES_FILE_NAME + "=" + BNConstants.RESOURCERER_FILE);
				out.println(BNConstants.GB_ACC_FILE_NAME + "=" + BNConstants.OUT_ACCESSION_FILE);
				out.println(BNConstants.KEGG_SPECIES + "=" + keggSpecies);
				out.println(BNConstants.SYM_ARTICLES_FRM_PUBMED + "=" + BNConstants.PUBMED_DB_FILE);
				out.println(BNConstants.SYM_ARTICLES_FRM_GENEDB + "=" + BNConstants.GENE_DB_FILE);
				out.println(BNConstants.ART_REM_THRESH + "=" + BNConstants.ART_REM_THRESH_VAL);		 	  
				out.println(BNConstants.FRM_LIT + "=false");
				out.println(BNConstants.FRM_PPI + "=true");
				out.println(BNConstants.FRM_KEGG + "=true");
				out.println(BNConstants.OUT_INTER_FILE_NAME + "=" + outFile[5]);
				out.println(BNConstants.PPI_FILE_NAME + "=" + BNConstants.PPI_FILE);
				out.flush();
				out.close();
			}
			if(LitPpiKegg){
				out= new PrintWriter(new FileOutputStream(new File(propFile[6])));	 
				out.println(BNConstants.RES_FILE_NAME + "=" + BNConstants.RESOURCERER_FILE);
				out.println(BNConstants.GB_ACC_FILE_NAME + "=" + BNConstants.OUT_ACCESSION_FILE);
				out.println(BNConstants.KEGG_SPECIES + "=" + keggSpecies);
				out.println(BNConstants.SYM_ARTICLES_FRM_PUBMED + "=" + BNConstants.PUBMED_DB_FILE);
				out.println(BNConstants.SYM_ARTICLES_FRM_GENEDB + "=" + BNConstants.GENE_DB_FILE);
				out.println(BNConstants.ART_REM_THRESH + "=" + BNConstants.ART_REM_THRESH_VAL);		 	  
				out.println(BNConstants.FRM_LIT + "=true");
				out.println(BNConstants.FRM_PPI + "=true");
				out.println(BNConstants.FRM_KEGG + "=true");
				out.println(BNConstants.OUT_INTER_FILE_NAME + "=" + outFile[2]);
				out.println(BNConstants.PPI_FILE_NAME + "=" + BNConstants.PPI_FILE);
				out.flush();
				out.close();
			}

			out= new PrintWriter(new FileOutputStream(new File(propFile[fileSize-1])));
			if(goTerms){
				System.out.println("Use GO Terms");
				out.println(BNConstants.USE_GO + "=" + "true");
				out.println(BNConstants.GB_GO_FILE_NAME + "=" + BNConstants.GB_GO_FILE); //"gbGOs.txt"
			}
			out.println(BNConstants.NAMES_FILE_NAME + "=" + BNConstants.OUT_ACCESSION_FILE);
			out.println(BNConstants.DISTRIBUTION_FRM_WEIGHTS + "=" + "true");
			out.println(BNConstants.OUT_XML_BIF_FILE_NAME + "=" + BNConstants.BIF_RESULT_FILE);
			if(lit){
				out.println(BNConstants.SIF_FILE_NAME + "=" + outFile[0]);
			}else if(ppi){
				out.println(BNConstants.SIF_FILE_NAME + "=" + outFile[1]);
			} else if(LitPpi){
				out.println(BNConstants.SIF_FILE_NAME + "=" + outFile[2]);
			} else if(kegg){
				out.println(BNConstants.SIF_FILE_NAME + "=" + outFile[3]);
			} else if(LitKegg){
				out.println(BNConstants.SIF_FILE_NAME + "=" + outFile[4]);
			} else if(KeggPpi){
				out.println(BNConstants.SIF_FILE_NAME + "=" + outFile[5]);
			} else if(LitPpiKegg){
				out.println(BNConstants.SIF_FILE_NAME + "=" + outFile[6]);
			}
			out.flush();
			out.close();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Older version - Unused
	 * @param lit
	 * @param ppi
	 * @param both
	 * @param goTerms
	 * @param path
	 */
	private void buildPropertyFile(boolean lit,boolean ppi,boolean both,boolean goTerms,String path){
		//String sep= System.getProperty("file.separator");    
		final int fileSize=4;
		String[] propFile=new String[fileSize];
		String[] outFile=new String[fileSize-1];
		//String datPath=path+sep+"bn"+sep;		//	 Raktim - USe Tmp dir
		/*
	 propFile[0]= path+sep+"getInterModLit.props";
	 propFile[1]= path+sep+"getInterModPPIDirectly.props";
	 propFile[2]= path+sep+"getInterModBoth.props";
	 propFile[3]= path+sep+"prepareXMLBifMod.props";
	 outFile[0]="outInteractionsLit.txt";
	 outFile[1]="outInteractionsPPI.txt"; 
	 outFile[2]="outInteractionsBoth.txt";		 */
		propFile[0]= path+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP+BNConstants.LIT_INTER_MODULE_FILE;
		propFile[1]= path+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP+BNConstants.PPI_INTER_MODULE_DIRECT_FILE;
		propFile[2]= path+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP+BNConstants.BOTH_INTER_MODULE_FILE;
		propFile[3]= path+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP+BNConstants.XML_BIF_MODULE_FILE; 
		outFile[0] = BNConstants.LIT_INTER_FILE;
		outFile[1] = BNConstants.PPI_INTER_FILE; 
		outFile[2] = BNConstants.LIT_PPI_INTER_FILE;

		PrintWriter out=null;
		try{ 	 
			if(lit){
				out= new PrintWriter(new FileOutputStream(new File(propFile[0])));	 
				out.println(BNConstants.RES_FILE_NAME + "=" + BNConstants.RESOURCERER_FILE);
				out.println(BNConstants.GB_ACC_FILE_NAME + "=" + BNConstants.OUT_ACCESSION_FILE);
				out.println(BNConstants.SYM_ARTICLES_FRM_PUBMED + "=" + BNConstants.PUBMED_DB_FILE);
				out.println(BNConstants.SYM_ARTICLES_FRM_GENEDB + "=" + BNConstants.GENE_DB_FILE);
				out.println(BNConstants.ART_REM_THRESH + "=" + BNConstants.ART_REM_THRESH_VAL);		 
				out.println(BNConstants.FRM_LIT + "=true");
				out.println(BNConstants.FRM_PPI + "=false");
				out.println(BNConstants.OUT_INTER_FILE_NAME + "=" +outFile[0]);
				out.flush();
				out.close();
			}
			if(ppi){
				out= new PrintWriter(new FileOutputStream(new File(propFile[1])));	 
				out.println(BNConstants.RES_FILE_NAME + "=" + BNConstants.RESOURCERER_FILE);
				out.println(BNConstants.GB_ACC_FILE_NAME + "=" + BNConstants.OUT_ACCESSION_FILE);
				out.println(BNConstants.SYM_ARTICLES_FRM_PUBMED + "=" + BNConstants.PUBMED_DB_FILE);
				out.println(BNConstants.SYM_ARTICLES_FRM_GENEDB + "=" + BNConstants.GENE_DB_FILE);
				out.println(BNConstants.ART_REM_THRESH + "=" + BNConstants.ART_REM_THRESH_VAL);		 	  
				out.println(BNConstants.FRM_LIT + "=false");
				out.println(BNConstants.FRM_PPI + "=true");
				out.println(BNConstants.OUT_INTER_FILE_NAME + "=" + outFile[1]);
				out.println(BNConstants.USE_PPI_DIRECT + "=true");
				//out.println("usePpiOnlyWithin=true");
				out.println(BNConstants.PPI_FILE_NAME + "=" + BNConstants.PPI_FILE);
				out.flush();
				out.close();

			}
			if(both){
				out= new PrintWriter(new FileOutputStream(new File(propFile[2])));	 
				out.println(BNConstants.RES_FILE_NAME + "=" + BNConstants.RESOURCERER_FILE);
				out.println(BNConstants.GB_ACC_FILE_NAME + "=" + BNConstants.OUT_ACCESSION_FILE);
				out.println(BNConstants.SYM_ARTICLES_FRM_PUBMED + "=" + BNConstants.PUBMED_DB_FILE);
				out.println(BNConstants.SYM_ARTICLES_FRM_GENEDB + "=" + BNConstants.GENE_DB_FILE);
				out.println(BNConstants.ART_REM_THRESH + "=" + BNConstants.ART_REM_THRESH_VAL);		 	  
				out.println(BNConstants.FRM_LIT + "=true");
				out.println(BNConstants.FRM_PPI + "=true");
				out.println(BNConstants.OUT_INTER_FILE_NAME + "=" + outFile[2]);
				out.println(BNConstants.PPI_FILE_NAME + "=" + BNConstants.PPI_FILE);
				out.flush();
				out.close();
			}
			out= new PrintWriter(new FileOutputStream(new File(propFile[fileSize-1])));
			if(goTerms){
				System.out.println("Use GO Terms");
				out.println(BNConstants.USE_GO + "=" + "true");
				out.println(BNConstants.GB_GO_FILE_NAME + "=" + BNConstants.GB_GO_FILE); //"gbGOs.txt"
			}
			out.println(BNConstants.NAMES_FILE_NAME + "=" + BNConstants.OUT_ACCESSION_FILE);
			out.println(BNConstants.DISTRIBUTION_FRM_WEIGHTS + "=" + "true");
			out.println(BNConstants.OUT_XML_BIF_FILE_NAME + "=" + BNConstants.BIF_RESULT_FILE);
			if(lit){
				out.println(BNConstants.SIF_FILE_NAME + "=" + outFile[0]);
			}else if(ppi){
				out.println(BNConstants.SIF_FILE_NAME + "=" + outFile[1]);
			} else if(both){
				out.println(BNConstants.SIF_FILE_NAME + "=" + outFile[2]);
			}
			out.flush();
			out.close();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Function to read a cluster. 
	 * It reads the UID(probe id, ref seq etc) 
	 * and maps them to Genbank acc and writes them to a tmp file- list.txt 
	 * @param cl
	 * @param framework
	 * @param path
	 */
	public void converter(Cluster cl,IFramework framework,String path){
		int genes=cl.getIndices().length;		//System.out.print(genes);
		IData data=framework.getData();
		int[] rows = new int[genes];
		rows=cl.getIndices();
		String[] affyId = new String[genes];
		String[] accList =new String[genes];
		HashMap accHash = new HashMap();
		String lineRead = "";
		//String sep=System.getProperty("file.separator");
		System.out.println(0);
		for (int i=0; i<rows.length; i++) {
			affyId[i] = data.getSlideDataElement(0,rows[i]).getFieldAt(0);
			//System.out.println("affyid :"+affyId[i] );
		}
		System.out.println(1);
		try {
			File file = new File(path,BNConstants.ACCESSION_FILE);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String[] fields;
			//PrintWriter out = new PrintWriter (new FileOutputStream(new File(path+sep+"list.txt")));
			br.readLine();
			br.readLine();
			while((lineRead = br.readLine()) != null) {
				//System.out.println("lineRead :"+lineRead );
				fields = lineRead.split("\t");
				accHash.put(fields[0].trim(), fields[1].trim());
				//System.out.println(fields[1] );
			}
			System.out.println(2);
			for (int i = 0; i < accList.length; i++) {
				accList[i] = (String)accHash.get((String)affyId[i].trim());
			}
			writeAccToFile(accList,path);
		} catch(FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
			System.out.println("File Write Error ");
		}
	}
	/**
 	Function to match a subset of ProbeIDs to their corresponding Acc Numbers
 	Return a list of Acc numbers
	 */
	private String[] matchSet (String[] accs, HashMap accHash) {
		String[] accList = new String[accs.length];
		for (int i = 0; i < accs.length; i++) {
			accList[i] = (String)accHash.get((String)accs[i].trim());
		}
		return accList;
	}
	private void writeAccToFile (String[] accList, String path) {
		//String sep=System.getProperty("file.separator");		//String outFile = path + sep+"list.txt";
		//	 Raktim - Use tmp Dir
		String outFile = path + BNConstants.SEP+ BNConstants.TMP_DIR + BNConstants.SEP + BNConstants.OUT_ACCESSION_FILE;
		System.out.println(outFile);
		BufferedWriter out = null;
		int nRows = accList.length;
		try {
			out = new BufferedWriter (new FileWriter(outFile));
			for (int row = 0; row < nRows; row++) {
				out.write(accList[row]);
				out.newLine();
				//System.out.println(accList[row]);
			}
			out.flush();
			out.close();
		} catch (IOException e){
			e.printStackTrace();
			//System.out.println("File Write Error " + errorStrings[FILE_IO_ERROR]);
			//return FILE_IO_ERROR;
		}
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
	public int literatureMining(boolean lit,boolean ppi, boolean kegg, boolean LitPpi, boolean LitKegg, boolean KeggPpi, boolean LitPpiKegg, String path){
		//System.out.print(sep);
		GetInteractionsModule getModule = new GetInteractionsModule(path);
		if(lit){
			//getModule.test(path+sep+"getInterModLit.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path + 
					BNConstants.SEP + 
					BNConstants.TMP_DIR + 
					BNConstants.SEP + 
					BNConstants.LIT_INTER_MODULE_FILE);
		}
		if(ppi){
			//getModule.test(path+sep+"getInterModPPIDirectly.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.PPI_INTER_MODULE_DIRECT_FILE); 
		}
		if(LitPpi){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.BOTH_INTER_MODULE_FILE);
		}
		if(kegg){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.KEGG_INTER_MODULE_FILE);
		}
		if(LitKegg){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.LIT_KEGG_INTER_MODULE_FILE);
		}
		if(KeggPpi){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.PPI_KEGG_INTER_MODULE_FILE);
		}
		if(LitPpiKegg){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			return GetInteractionsModule.test(path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.PPI_KEGG_LIT_INTER_MODULE_FILE);
		}
		return -1;
	}

	/**
	 * Older version - Unused
	 * @param lit
	 * @param ppi
	 * @param both
	 * @param path
	 */
	public void literatureMining(boolean lit,boolean ppi,boolean both,String path){
		//System.out.print(sep);
		GetInteractionsModule getModule=new GetInteractionsModule(path);
		if(lit && !both){
			System.out.print("run");
			//getModule.test(path+sep+"getInterModLit.props"); 	//Raktim - USe tmp dir
			GetInteractionsModule.test(
					path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.LIT_INTER_MODULE_FILE);
			return;
		}
		if(ppi && !both){
			//getModule.test(path+sep+"getInterModPPIDirectly.props"); //Raktim - USe tmp dir
			GetInteractionsModule.test(
					path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.PPI_INTER_MODULE_DIRECT_FILE); 
			return;
		}
		if(both){
			//getModule.test(path+sep+"getInterModBoth.props"); //Raktim - USe tmp dir
			GetInteractionsModule.test(
					path +
					BNConstants.SEP +
					BNConstants.TMP_DIR +
					BNConstants.SEP +
					BNConstants.BOTH_INTER_MODULE_FILE);
		}

	}
	public void prepareXMLBifFile(String path){
		PrepareXMLBifModule getModule=new PrepareXMLBifModule();		//getModule.test(path+sep+"prepareXMLBifMod.props"); //Raktim - USe tmp dir
		PrepareXMLBifModule.test(
				path +
				BNConstants.SEP +
				BNConstants.TMP_DIR +
				BNConstants.SEP +
				BNConstants.XML_BIF_MODULE_FILE);
	}
	
	/**
	 * General info structure.
	 */
	public static class GeneralInfo {
		String prior = "No Priors";
		String useGoTerms = "Use modification of DFS";
		int numGene;
	}
	/**
	 * Displays an error dialog
	 * @param message
	 */
	public void error( String message ) {
		JOptionPane.showMessageDialog( new JFrame(), message, "Input Error", JOptionPane.ERROR_MESSAGE );
	}//end error()
	
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
	
}//end class


