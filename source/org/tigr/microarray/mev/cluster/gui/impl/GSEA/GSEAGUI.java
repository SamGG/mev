package org.tigr.microarray.mev.cluster.gui.impl.GSEA;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;


import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.GeneAnnotationImportDialog;
import org.tigr.microarray.mev.ISlideDataElement;
import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.annotation.AnnotationFileReader;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.impl.GSEA.GeneData;
import org.tigr.microarray.mev.cluster.algorithm.impl.GSEA.GeneSetElement;
import org.tigr.microarray.mev.cluster.algorithm.impl.GSEA.Geneset;
import org.tigr.microarray.mev.cluster.algorithm.impl.GSEA.ProbetoGene;
import org.tigr.microarray.mev.cluster.algorithm.impl.GSEA.ReadGeneSet;

import org.tigr.microarray.mev.cluster.algorithm.impl.GSEA.ProcessGroupAssignments;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.GSEAExperiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Logger;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;


import org.tigr.microarray.mev.cluster.gui.impl.nonpar.NonparConstants;
import org.tigr.microarray.mev.cluster.gui.impl.nonpar.NonparExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.impl.util.MatrixFunctions;

import org.tigr.util.FloatMatrix;

public class GSEAGUI implements IClusterGUI {
	 	
		private Algorithm gsea;
		private Progress progress;
	    private Experiment experiment;
	    private GSEAExperiment gseaExperiment;
	    private IData idata;
	    private Logger logger;
	    protected Listener listener;
	  
	    
	   

	
	public DefaultMutableTreeNode execute(IFramework framework)	throws AlgorithmException {
		
		this.experiment = framework.getData().getExperiment();
        this.idata = framework.getData();
        FloatMatrix matrix = experiment.getMatrix();
		int number_of_samples = experiment.getNumberOfSamples();
		
        AlgorithmData algData = new AlgorithmData();
        DefaultMutableTreeNode resultNode = null;
		
		
		Geneset[]gset=null;
		GeneData[]gData=null;
		
		algData.addMatrix("matrix",matrix);
					
		JFrame mainFrame = (JFrame)(framework.getFrame());
		//Need the "." after the step names, to keep track of the highlighting
		String [] steps = {"Data Selection.","Parameter Selection.", "Execute."};		
		
		GSEAInitWizard wiz=new GSEAInitWizard(idata, mainFrame, "GSEA Initialization", true, algData,steps,  2, new StepsPanel(), framework.getClusterRepository(1));
		
		 listener = new Listener();
	     logger = new Logger(framework.getFrame(), "Gene Set Enrichment Analysis", listener);
	     //progress = new Progress(framework.getFrame(), " Progress", listener);
	       
		
		
		if(wiz.showModal() == JOptionPane.OK_OPTION) {
			logger.show();
			//progress.show();
				
			
			String genesetFilePath=algData.getParams().getString("gene-set-file");
			String extension=checkFileNameExtension(genesetFilePath);
			
			//Get the collapse mode; MAX_PROBE OR MEDIAN_PROBE
			String collapsemode=algData.getParams().getString("probe_value");
			//Get the minimum number of genes per gene set
			int min_genes=Integer.parseInt(algData.getParams().getString("gene-number"));
			
			//Get the SD cutoff
			
			String cutoff=algData.getParams().getString("standard-deviation-cutoff");
			
			//First step is to convert the expression data into GeneData object. Depending on the type of 
			//gene set file loaded, the gene identifier to use will vary.
			
			ProbetoGene ptg=new ProbetoGene(algData, idata);
			logger.append("Collapsing probes to genes \n");
			
			//If extension is gmt or gmx, the gene identifier defaults to GENE_SYMBOL.
			if(extension.equalsIgnoreCase("gmt")||extension.equalsIgnoreCase("gmx")){
				gData=ptg.convertProbeToGene(AnnotationFieldConstants.GENE_SYMBOL, collapsemode, cutoff);
							
			}
			//If extension is .txt, gene identifier is ENTREZ_ID as of now. 
			else if(extension.equalsIgnoreCase("txt")){
				gData=ptg.convertProbeToGene(AnnotationFieldConstants.ENTREZ_ID, collapsemode, cutoff);
			}
			
			gseaExperiment=ptg.returnGSEAExperiment();
			Vector genesInExpressionData=algData.getVector("Unique-Genes-in-Expressionset");
			System.out.println("Number of unique genes in data set:"+genesInExpressionData.size());
			
			//Second step is to read the Gene Set file itself. Once this is done, the gene sets will have to be further
			//processed to remove the genes, which are present in the gene set but NOT in GeneData (expressiondata). 
			//Gene set object is also processed to remove the gene sets which do not have the minimum number of genes
			//as specified by the user.
			
			logger.append("Reading gene set files \n");
			
			ReadGeneSet rgset=new ReadGeneSet(extension, genesetFilePath);
			try{
				if(extension.equalsIgnoreCase("gmx"))
					gset=rgset.read_GMXformatfile(genesetFilePath);
				else if(extension.equalsIgnoreCase("gmt"))
					gset=rgset.read_GMTformatfile(genesetFilePath);
				else if(extension.equalsIgnoreCase("txt"))
					gset=rgset.read_TXTformatfile(genesetFilePath);
			
			//	Geneset[] gene_set=rgset.removeGenesNotinExpressionData(gset, genesInExpressionData);//--commented for testing
				Geneset[] gene_set=gset;//Added for Testing to see, if removeGenes is screwing up 
			   

			//Third step is to generate Association Matrix. The Association Matrix generated, does not include gene set
			//which do not satisfy the minimum number of genes criteria
			logger.append("Creating Association Matrix \n");
				
			FloatMatrix amat=rgset.createAssociationMatrix(gene_set, genesInExpressionData, min_genes);
			algData.addGeneMatrix("association-matrix", amat);
				
			
			
			//System.out.println("size of excluded gene set is:"+rgset.getExcludedGeneSets().size());
			Geneset[]geneset=new Geneset[gene_set.length-rgset.getExcludedGeneSets().size()];
			
			logger.append("Removing gene sets that do not pass the minimum genes criteria \n");
			geneset=rgset.removeGenesetsWithoutMinimumGenes(rgset.getExcludedGeneSets(), gene_set);
			
			//Add the Gene set names to AlgorithmData
			algData.addVector("gene-set-names", geneset[0].getAllGenesetNames());
			
						
			//Add to Algorithm Data, so that can access from viewers
			algData.addVector("excluded-gene-sets", rgset.getExcludedGeneSets());
			
			
		/*	System.out.println("Amat in GSEAGUI:"+amat.getRowDimension()+":"+amat.getColumnDimension());
			for(int i=0; i<amat.getRowDimension();i++){
				System.out.print("Gene set Name:"+geneset[0].getAllGenesetNames().get(i));
				System.out.print('\t');
				for(int j=0; j<amat.getColumnDimension(); j++){
					System.out.print(amat.get(i,j));
					System.out.print('\t');
				}
				System.out.println();
			}*/
			
			
			
			
			
			
			//System.out.println("excluded-gene-set size:"+algData.getVector("excluded-gene-sets").size());
			//Add to algorithmdata, the genes removed from gene sets
			//Both these would be displayed in the viewer
			
			/****
			 * I think i may need to add a function to probetoGene that returns GSEAExperiment.
			 * No way of passing it around otherwise. ?
			 */
			
			
			
			
			
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
		
		
		gsea = framework.getAlgorithmFactory().getAlgorithm("GSEA");
		gsea.addAlgorithmListener(listener);
		logger.append("Algorithm execution begins... \n");
		AlgorithmData result = gsea.execute(algData);//--commented for testing viewer	
		logger.append("Algorithm excecution ends...\n");
		logger.dispose();
		
		//resultNode = createResultNode(null, algData, idata, null, experiment);
		resultNode = createResultNode(null, result, idata, null, experiment);//--commented for Testing
			
		//temporarily commented for testing and building gsea
		//algData.addGeneMatrix("association-matrix", createAssociationMatrix(gset, algData.getGeneData("gene-data-array")));
		
		
		
		
		
		return resultNode;
			
		}
				
			
		
		
			
	
	/*
	 *  The class to listen to progress, monitor and algorithms events.
    */
   private class Listener extends DialogListener implements AlgorithmListener {
       
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
           }
       }
       
       public void actionPerformed(ActionEvent e) {
           String command = e.getActionCommand();
           if (command.equals("cancel-command")) {
               gsea.abort();
               progress.dispose();
           }
       }
       
       public void windowClosing(WindowEvent e) {
           gsea.abort();
           progress.dispose();
       }
   }
	
	
   private DefaultMutableTreeNode createResultNode(String mode, AlgorithmData result, IData idata, int [][] clusters, Experiment experiment) {
		DefaultMutableTreeNode node = null;
		
		//if(mode.equals(NonparConstants.MODE_WILCOXON_MANN_WHITNEY)) {
			node = new DefaultMutableTreeNode("GSEA-Significant Gene sets");
			addTableViews(mode, node, result, experiment, idata, clusters);
			//Does not seem to do it's job...shows wierd expression values
			addExpressionImages(node,  result, clusters, this.gseaExperiment);
			
		//} 		
		return node;
	}
	

   private void addExpressionImages(DefaultMutableTreeNode root,  AlgorithmData result, int [][] clusters, GSEAExperiment experiment) {

		GSEAExperimentViewer viewer;	
			
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
		viewer = new GSEAExperimentViewer(experiment, clusters);
	    DefaultMutableTreeNode viewerNode = new DefaultMutableTreeNode(new LeafInfo("Gene Matrix", viewer, new Integer(0)));
		node.add(viewerNode);			
		 		
		root.add(node);
	}

   
   
   
   
   
   
   
   
   private void addTableViews(String mode, DefaultMutableTreeNode root, AlgorithmData result, Experiment experiment, IData data, int [][] clusters) {
   	DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table Views");
   	GSEATableViewer tabViewer;
   	String[][]pVals =(String[][]) result.getObjectMatrix("geneset-pvals");
   	String[]headernames={"Gene Set", "Lower-pValues", "Upper-pValues"};
   	
   	
   //DISPLAY SIGNIFICANT GENE SETS
   	tabViewer = new GSEATableViewer(headernames,pVals);
   	node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Gene Sets", tabViewer, new Integer(0))));
   
   //Display Excluded Gene sets
   	Vector temp=result.getVector("excluded-gene-sets");
   	String[][]_dummy=new String[temp.size()][1];
   	
   	for(int i=0; i<temp.size(); i++){
   		
   			_dummy[i][0]=(String)temp.get(i);
   			//System.out.println("Excluded gene set name:"+(String)temp.get(i));
   		
   	}
    String[]header={"Excluded Gene Sets"};  	
  	tabViewer = new GSEATableViewer(header,_dummy);
   	node.add(new DefaultMutableTreeNode(new LeafInfo("Excluded Gene Sets", tabViewer, new Integer(0))));
 
   	
   	
  //Display Genes excluded from gene sets
   	
   	
   	
   	root.add(node);
   }

   
		
	
	/**
	 * checkFileNameExtension returns the extension of the file.
	 * @param fileName
	 * @return
	 */
	
	public String checkFileNameExtension(String fileName){
		String extension=fileName.substring(fileName.indexOf('.')+1, fileName.length());
			
		return extension;
	}
	
	public static void main(String[] args){
		
		
	}
	
	
	}	


