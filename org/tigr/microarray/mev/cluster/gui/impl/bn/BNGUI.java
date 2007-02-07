/*
 * Created on Aug 30, 2005
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterTable;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.gui.impl.bn.prepareXMLBif.PrepareXMLBifModule;
import org.tigr.microarray.mev.cluster.gui.impl.bn.getInteractions.GetInteractionsModule;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLNodeHeightGraph;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLGUI.GeneralInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
//import java.util.Hashtable;
import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.BufferedWriter;

public class BNGUI implements IClusterGUI {
	String sep = System.getProperty("file.separator");
	public static final int GENE_CLUSTER = 0;
	public static boolean done=false;
	public static boolean prior=true;
	public DefaultMutableTreeNode execute(IFramework framework)
			throws AlgorithmException {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode( "BN" );
		IData data = framework.getData();
		Experiment exp =data.getExperiment();
		ClusterRepository repository = framework.getClusterRepository(Cluster.GENE_CLUSTER);
		/*
		if(exp.getNumberOfGenes()>200 && repository.isEmpty()){
		    BNPreDialog b=new BNPreDialog(framework.getJFrame(),true);
                    b.setVisible(true);        
                    return null;
		}else {	
	        */  
		final BNInitDialog dialog = new BNInitDialog(framework.getFrame(), repository, framework.getData().getFieldNames());
        if(dialog.showModal() != JOptionPane.OK_OPTION)
             return null;
		if(dialog.isNone()){
			prior=false;
			done=true;
		}
		 converter(dialog.getSelectedCluster(),framework,dialog.getBaseFileLocation());
         buildPropertyFile(dialog.isLit(),dialog.isPPI(),dialog.isBoth(),dialog.useGoTerm(),dialog.getBaseFileLocation());
	     Thread thread = new Thread( new Runnable(){
		    public void run(){	
		         if(!dialog.isNone()){
		         		 
		         literatureMining(dialog.isLit(),dialog.isPPI(),dialog.isBoth(),dialog.getBaseFileLocation());
		         //literatureMining(true,false,false,dialog.getBaseFileLocation());
			     prepareXMLBifFile(dialog.getBaseFileLocation());
		         BNGUI.done=true;
			 }
	             }
	           });
	    thread.start();
	    BNClassificationEditor bnEditor=new BNClassificationEditor(framework,false,dialog.getSelectedCluster(),(new Integer(dialog.getNumberBin())).toString(),dialog.getNumberClass(),dialog.numParents(),dialog.getAlgorithm(),dialog.getScoreType(),dialog.useArcRev(), dialog.getBaseFileLocation());
		bnEditor.showModal(true);
		GeneralInfo info = new GeneralInfo();
		if(dialog.isBoth()){
		info.prior="Literature Mining and PPI";
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
		return createResultTree(exp, info);
	    //return root;
	}
	
	private DefaultMutableTreeNode createResultTree(Experiment experiment, GeneralInfo info) {
	        DefaultMutableTreeNode root = new DefaultMutableTreeNode("BN");
	        addGeneralInfo(root, info);
	        return root;
	    }
	
	private void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        node.add(new DefaultMutableTreeNode("Number of Genes: "+info.numGene));
        node.add(new DefaultMutableTreeNode("Prior: "+info.prior));
        node.add(new DefaultMutableTreeNode("Number of Discretizing Expression Values: "+info.numBin));
        node.add(new DefaultMutableTreeNode("Number of Sample Classes: "+info.numClass));
        node.add(new DefaultMutableTreeNode("Number of Parents: "+info.numParents));
        node.add(new DefaultMutableTreeNode("Algorithm: "+info.algorithm));
        root.add(node);
    }
	private void buildPropertyFile(boolean lit,boolean ppi,boolean both,boolean goTerms,String path){
	 String sep= System.getProperty("file.separator");    
	 final int fileSize=4;
	 String[] propFile=new String[fileSize];
	 String[] outFile=new String[fileSize-1];
	 //String datPath=path+sep+"bn"+sep;
	 propFile[0]= path+sep+"getInterModLit.props";
	 propFile[1]= path+sep+"getInterModPPIDirectly.props";
	 propFile[2]= path+sep+"getInterModBoth.props";
	 propFile[3]= path+sep+"prepareXMLBifMod.props";
	 outFile[0]="outInteractionsLit.txt";
	 outFile[1]="outInteractionsPPI.txt"; 
	 outFile[2]="outInteractionsBoth.txt";
	  
	 PrintWriter out=null;
	 try{ 	 
		 if(lit){
		out= new PrintWriter(new FileOutputStream(new File(propFile[0])));	 
	    out.println("resourcererFileName=res.txt");
		 out.println("gbAccessionsFileName=list.txt");
		 out.println("symbolsArticlesFromPubmedFileName=symArtsPubmed.txt");
		 out.println("symbolsArticlesFromGeneDbFileName=symArtsGeneDb.txt");
		 out.println("articleRemovalThreshold=2");		 
		 out.println("fromLiterature=true");
		 out.println("fromPpi=false");
		 out.println("outInteractionsFileName="+outFile[0]);
		 out.flush();
         out.close();
		 }
		  if(ppi){
		out= new PrintWriter(new FileOutputStream(new File(propFile[1])));	 
	    out.println("resourcererFileName=res.txt");
		 out.println("gbAccessionsFileName=list.txt");
		 out.println("symbolsArticlesFromPubmedFileName=symArtsPubmed.txt");
		 out.println("symbolsArticlesFromGeneDbFileName=symArtsGeneDb.txt");
		 out.println("articleRemovalThreshold=2");		 	  
		 out.println("fromLiterature=false");
		 out.println("fromPpi=true");
		 out.println("outInteractionsFileName="+outFile[1]);
		 out.println("usePpiDirectly=true");
		 //out.println("usePpiOnlyWithin=true");
		 out.println("ppiFileName=all_ppi.txt");
		 out.flush();
         out.close();
		 
		 }
		  if(both){
	     out= new PrintWriter(new FileOutputStream(new File(propFile[2])));	 
	     out.println("resourcererFileName=res.txt");
		 out.println("gbAccessionsFileName=list.txt");
		 out.println("symbolsArticlesFromPubmedFileName=symArtsPubmed.txt");
		 out.println("symbolsArticlesFromGeneDbFileName=symArtsGeneDb.txt");
		 out.println("articleRemovalThreshold=2");		 	  
	     out.println("fromLiterature=true");
		 out.println("fromPpi=true");
		 out.println("outInteractionsFileName="+outFile[2]);
		  out.println("ppiFileName=all_ppi.txt");
		  out.flush();
          out.close();
	     }
	     out= new PrintWriter(new FileOutputStream(new File(propFile[fileSize-1])));
	     if(goTerms){
	    	 out.println("useGo=true");
		     out.println("gbGOsFileName=gbGOs.txt"); 
	     }
	     out.println("namesFileName=list.txt");
	     out.println("distributionFromWeights=true");
	     out.println("outXMLBifFileName=resultBif.xml");
	     if(lit){
	       out.println("sifFileName="+outFile[0]);
	     }else if(ppi){
                 out.println("sifFileName="+outFile[1]);
	     } else if(both){
	      out.println("sifFileName="+outFile[2]);
	     }
	     out.flush();
	     out.close();
	}catch (Exception e){
            e.printStackTrace();
        }
	}
	
	//from cluster to generate gene list file automatically 
	public void converter(Cluster cl,IFramework framework,String path){
         int genes=cl.getIndices().length;
    	//System.out.print(genes);
    	IData data=framework.getData();
    	int[] rows = new int[genes];
    	rows=cl.getIndices();
	String[] affyId=new String[genes];
	String[] accList =new String[genes];
	HashMap accHash = new HashMap();
	String lineRead = "";
	String sep=System.getProperty("file.separator");
	for (int i=0; i<rows.length; i++) {
    	  affyId[i]=data.getSlideDataElement(0,rows[i]).getFieldAt(0);
	  //System.out.println("affyid :"+affyId[i] );
	 }
	
	 try {
	    File file = new File(path,"affyID_accession.txt");
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
	 String sep=System.getProperty("file.separator");
	 String outFile = path + sep+"list.txt";
	// System.out.println(outFile);
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
	public void literatureMining(boolean lit,boolean ppi,boolean both,String path){
		//System.out.print(sep);
		GetInteractionsModule getModule=new GetInteractionsModule(path);
		if(lit){
		  getModule.test(path+sep+"getInterModLit.props"); 	
		}
		if(ppi){
		  getModule.test(path+sep+"getInterModPPIDirectly.props");
		}
		if(both){
		  getModule.test(path+sep+"getInterModBoth.props");
		}
		
	}
	public void prepareXMLBifFile(String path){
	PrepareXMLBifModule getModule=new PrepareXMLBifModule();
	//getModule.test("prepareXMLBifMod.props");
	getModule.test(path+sep+"prepareXMLBifMod.props");
	}
	
	 /**
     * General info structure.
     */
    public static class GeneralInfo {
    	String prior="NO Priors";
    	String useGoTerms="Use modification of DFS";
    	String numParents;
    	int numClass;
    	int numBin;
    	int numGene;
    	String algorithm;
    }
	/**
	 * Displays an error dialog
	 * @param message
	 */
	public void error( String message ) {
		JOptionPane.showMessageDialog( new JFrame(), 
				message, "Input Error", JOptionPane.ERROR_MESSAGE );
	}//end error()
}//end class


