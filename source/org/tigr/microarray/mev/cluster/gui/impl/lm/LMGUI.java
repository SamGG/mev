/*
 * Created on Aug 30, 2005
 * @author Raktim
 */
package org.tigr.microarray.mev.cluster.gui.impl.lm;
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
import org.tigr.microarray.mev.cluster.gui.impl.bn.CytoscapeWebstart;
import org.tigr.microarray.mev.cluster.gui.impl.bn.LMBNViewer;
import org.tigr.microarray.mev.cluster.gui.impl.bn.RunWekaProgressPanel;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;
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
		
		String basePath = dialog.getBaseFileLocation() + BNConstants.SEP;
		if(Useful.converter(dialog.getSelectedCluster(),framework,dialog.getBaseFileLocation()) == null) {
			JOptionPane.showMessageDialog(new JFrame(), "Error mapping Unique identifiers", "Error!", JOptionPane.ERROR_MESSAGE);
			LMGUI.done = false;
			runProgressPanel.dispose();
			return null;
		}
		
		String kegg_sp = dialog.getKeggSpecies();
		if(kegg_sp != null) kegg_sp = kegg_sp.trim();
		else kegg_sp = "na";
		
		//Build Property file for Weka Params
		//buildPropertyFile(dialog.isLit(),dialog.isPPI(),dialog.isBoth(),dialog.useGoTerm(),dialog.getBaseFileLocation());
		Useful.buildPropertyFile(dialog.isLit(),dialog.isPPI(),dialog.isKEGG(), dialog.isBoth(), dialog.isLitAndKegg(), dialog.isPpiAndKegg(), dialog.isAll(),dialog.useGoTerm(),dialog.getBaseFileLocation(),kegg_sp);
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


