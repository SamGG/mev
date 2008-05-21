/*
 * Created on Aug 30, 2005
 */
package org.tigr.microarray.mev.cluster.gui.impl.lm;
import java.awt.Dimension;import java.awt.EventQueue;import java.awt.Toolkit;import java.io.File;import java.io.FileWriter;import java.io.IOException;import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JFrame;import javax.swing.JOptionPane;import javax.swing.tree.DefaultMutableTreeNode;import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;import org.tigr.microarray.mev.cluster.clusterUtil.ClusterTable;
import org.tigr.microarray.mev.cluster.gui.Experiment;import org.tigr.microarray.mev.cluster.gui.IClusterGUI;import org.tigr.microarray.mev.cluster.gui.IData;import org.tigr.microarray.mev.cluster.gui.IFramework;import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.gui.impl.bn.prepareXMLBif.PrepareXMLBifModule;
//import org.tigr.microarray.mev.cluster.gui.impl.bn.BNGUI.GeneralInfo;
import org.tigr.microarray.mev.cluster.gui.impl.bn.getInteractions.GetInteractionsModule;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
//import org.tigr.microarray.mev.cluster.gui.impl.bn.BNGUI;
import org.tigr.microarray.mev.cluster.gui.impl.lm.LiteratureMiningDialog;
import org.tigr.microarray.mev.cluster.gui.impl.bn.BNConstants;
import org.tigr.microarray.mev.cluster.gui.impl.bn.RunWekaProgressPanel;//import java.util.Hashtable;
import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.lang.reflect.InvocationTargetException;

public class LMGUI implements IClusterGUI {
	//String sep = System.getProperty("file.separator");
	public static final int GENE_CLUSTER = 0;
	public static boolean done=false;
	//public static boolean cancelRun=false;
	public static boolean prior=true;
	RunWekaProgressPanel runProgressPanel;
	public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
		done=false;
		//cancelRun=false;
		prior=true;
		DefaultMutableTreeNode root = new DefaultMutableTreeNode( "LM" );
		IData data = framework.getData();
		Experiment exp =data.getExperiment();
		ClusterRepository repository = framework.getClusterRepository(Cluster.GENE_CLUSTER);
		final LiteratureMiningDialog dialog = new LiteratureMiningDialog(framework.getFrame(), repository, framework.getData().getFieldNames());
        if(dialog.showModal() != JOptionPane.OK_OPTION)
              return null;
        String basePath = dialog.getBaseFileLocation() + BNConstants.SEP;
        converter(dialog.getSelectedCluster(),framework,dialog.getBaseFileLocation());
        buildPropertyFile(dialog.isLit(),dialog.isPPI(),dialog.isBoth(),dialog.useGoTerm(),dialog.getBaseFileLocation());		         		 
        Thread thread1 = new Thread( new Runnable(){
		    public void run(){	
		    	literatureMining(dialog.isLit(),dialog.isPPI(),dialog.isBoth(),dialog.getBaseFileLocation());
		    	done=true;
		    }
          });
       thread1.start();
       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	   runProgressPanel=new RunWekaProgressPanel();
       runProgressPanel.setIndeterminate(true);
	   runProgressPanel.setLocation((screenSize.width-framework.getFrame().getSize().width)/2,(screenSize.height-framework.getFrame().getSize().height)/2);
       runProgressPanel.setVisible(true);
       while(!LMGUI.done){
		     try{		        Thread.sleep(10000);	
		     }catch(InterruptedException x){
		     //ignore;
		     }
		}
       runProgressPanel.dispose();
       //if(cancelRun)
    	   //return null;
       
       final String[] argv = new String[6];
	   //argv[0] = "-i";
	   argv[0] = "-N";
       //argv[1] = System.getProperty("user.dir")+"/data/bn/liter_mining_alone_network.sif"; //Raktim - Old Way
       //Raktim - Modified
	   //argv[1] = System.getProperty("user.dir")+"/data/bn/results/"+System.getProperty("LM_ONLY");
	   argv[1] = basePath + BNConstants.RESULT_DIR + BNConstants.SEP + System.getProperty("LM_ONLY");
	   argv[2] = "-p";	   //argv[3] = System.getProperty("user.dir")+"/plugins/core/yLayouts.jar";
	   argv[3] = System.getProperty("user.dir")+"/plugins/yLayouts.jar";
	   // Raktim - For Edge properties file
		 argv[4] = "-V";
		 argv[5] = System.getProperty("user.dir")+"/plugins/vizmap.props";
	   
	   done=false;
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
	    info.numGene=(dialog.getSelectedCluster()).getIndices().length;
	    
	    Runnable runnable=new Runnable(){
	    	public void run(){
	    		try{
	                  cytoscape.CyMain.main(argv);
				    }catch(Exception ex){
				    ex.printStackTrace();
				    }
	    	}
	    };
	    try {
			EventQueue.invokeAndWait(runnable);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
		return createResultTree(exp, info);
	    
	}
	private DefaultMutableTreeNode createResultTree(Experiment experiment, GeneralInfo info) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("LM");
        addGeneralInfo(root, info);
        return root;
    }
private void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
    node.add(new DefaultMutableTreeNode("Number of Genes: "+info.numGene));
    node.add(new DefaultMutableTreeNode("Prior: "+info.prior));
    root.add(node);
}
	private void buildPropertyFile(boolean lit,boolean ppi,boolean both,boolean goTerms,String path){
	 //String sep= System.getProperty("file.separator");    
	 final int fileSize=4;
	 String[] propFile=new String[fileSize];
	 String[] outFile=new String[fileSize-1];
	 //String datPath=path+sep+"bn"+sep;	 //	 Raktim - USe Tmp dir
	 /*
	 propFile[0]= path+sep+"getInterModLit.props";
	 propFile[1]= path+sep+"getInterModPPIDirectly.props";
	 propFile[2]= path+sep+"getInterModBoth.props";
	 propFile[3]= path+sep+"prepareXMLBifMod.props";
	 outFile[0]="outInteractionsLit.txt";
	 outFile[1]="outInteractionsPPI.txt"; 
	 outFile[2]="outInteractionsBoth.txt";	  */
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
        int genes=cl.getIndices().length;        //System.out.print(genes);
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
	 //String sep=System.getProperty("file.separator");	 //String outFile = path + sep+"list.txt";
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
    	String prior="NO Priors";
    	String useGoTerms="Use modification of DFS";
    	int numGene;
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


