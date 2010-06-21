package org.tigr.microarray.mev.cluster.gui.impl.attract;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.impl.attract.AttractAlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.GSEAUtils;
import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.GeneData;
import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.GeneDataElement;
import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.GeneSetElement;
import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.Geneset;
import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.IGeneData;
import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.IGeneSetElement;
import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.ProbetoGene;
import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.ReadGeneSet;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.GSEAExperiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Logger;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.GSEACentroidViewer;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.GSEAConstants;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.GSEAExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.GSEATableViewer;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.GenesetMembership;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.LeadingEdgeSubsetViewer;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.LeadingEdgeTableViewer;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.StepsPanel;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.TestStatisticTableViewer;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.TestStatisticViewer;

import org.tigr.util.FloatMatrix;


public class ATTRACTGUI implements IClusterGUI{

	private IData idata;
    private  AlgorithmData algData = new AlgorithmData();
	private Algorithm gsea;
	private Algorithm attract;
	private Experiment experiment;
	private GSEAExperiment gseaExperiment;
	private Logger logger;
	private Listener listener;
	private boolean stop=false;
	private String[][]geneToProbeMapping;
	//max_columns decides the number of columns to display in the
	//table viewer
	int max_columns;
	
    private HashMap<String, LinkedHashMap<String, Float>>orderedTestStats=new HashMap<String, LinkedHashMap<String, Float>>();
    private HashMap<String, LinkedHashMap<String, Float>>descendingSortedTStats=new HashMap<String, LinkedHashMap<String, Float>>();
    private Geneset[]geneset=null;
	private IGeneData[]gData=null;
	private AlgorithmParameters param;  
	private ArrayList<String>sorted_gene_names=new ArrayList<String>();
	private int topPathways;
	private int[] factorLevels;
	private int[][]grpAssignments;
	
	
	
	
	public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
		
		// Temp code to disable launch in Mac & Linux
		// to be reverted when Mac R issue is resolved
		if (!System.getProperty("os.name").toLowerCase().contains("win")) {
			System.out.println("ATTARCT module not supported on Mac or Linux OS yet.");
			JOptionPane.showMessageDialog(framework.getFrame(), "ATTARCT module not supported on Mac or Linux OS yet.", "OS not supported", JOptionPane.INFORMATION_MESSAGE);
			return null;
		}
		 DefaultMutableTreeNode resultNode = null;
		 JFrame mainFrame = (JFrame)(framework.getFrame());
		 
		//Need the "." after the step names, to keep track of the highlighting
		String [] steps = {"Data Selection.", "Parameter Selection.", "Execute."};		
		this.idata = framework.getData();
		this.experiment = framework.getData().getExperiment();
		FloatMatrix matrix = experiment.getMatrix();
		algData.addMatrix("matrix",matrix);
	
		
		ArrayList<String>sampleLabels = new ArrayList<String>();
		int number_of_samples=experiment.getNumberOfSamples();
		
        for (int i = 0; i < number_of_samples; i++) {
             sampleLabels.add(framework.getData().getFullSampleName(i)); 
        }
		algData.addStringArray("sampleLabels", sampleLabels.toArray(new String[sampleLabels.size()]));
		
		Geneset[]gset=null;
		
		
		AttractInitWizard wiz=new AttractInitWizard(idata, mainFrame, "Attract Initialization", true, algData, steps,  1, new StepsPanel(), framework.getClusterRepository(1), framework);
		wiz.setVisible(true);
		logger = new Logger(framework.getFrame(), "Attract Package", listener);
		
		
		if(wiz.showModal() == JOptionPane.OK_OPTION) {
			logger.show();
					
			//Multiple files can be uploaded ONLY if they are in the same directory.
			String genesetFilePath=algData.getParams().getString("gene-set-directory");
			
			//Assuming there are multiple file uploads, need to check the extensions of each uploaded file
			//and make sure they have same gene identifiers
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
			
			if(stop){
				logger.dispose();
				return null;
			}
			
			grpAssignments=algData.getIntMatrix("factor-assignments");
			factorLevels=algData.getIntArray("factor-levels");
			topPathways=Integer.parseInt(algData.getParams().getString("pathway-cutoff"));
			
			//Get the gene identifier from AlgorithmData and use it to collapse the probes
			gData=ptg.convertProbeToGene(algData.getParams().getString("gene-identifier"), collapsemode, cutoff);
			
			
			gseaExperiment=ptg.returnGSEAExperiment();
			Vector genesInExpressionData=algData.getVector("Unique-Genes-in-Expressionset");

			//String array containing Gene to Probe mapping, which will be used in the table viewers	
			geneToProbeMapping=((GeneData)gData[0]).getProbetoGeneMapping(gData);
			
			//Decides the number of columns in the table viewer. 
			//The reason being one Gene may map to one probe and another to ten. 
			max_columns=((GeneData)gData[0]).get_max_num_probes_mapping_to_gene();
			
			//Second step is to read the Gene Set file itself. Once this is done, the gene sets will have to be further
			//processed to remove the genes, which are present in the gene set but NOT in GeneData (expressiondata). 
			//Gene set object is also processed to remove the gene sets which do not have the minimum number of genes
			//as specified by the user.
			
			logger.append("Reading gene set files \n");
			
			if(stop){
				logger.dispose();
				return null;
			}
			
			ReadGeneSet rgset=new ReadGeneSet(extension, genesetFilePath);
			try{
				
				gset=rgset.readMultipleFiles(algData.getStringArray("gene-set-files"), genesetFilePath);
				//System.out.println("gene set size after raedMultipleFiles in GSEAGUI:"+gset.length);
				Geneset[] gene_set=rgset.removeGenesNotinExpressionData(gset, genesInExpressionData);
				 
				

			//Third step is to generate Association Matrix. The Association Matrix generated, does not include gene set
			//which do not satisfy the minimum number of genes criteria
			logger.append("Creating Association Matrix \n");
			
			if(stop){
				logger.dispose();
				return null;
			}
			
			FloatMatrix amat=rgset.createAssociationMatrix(gene_set, genesInExpressionData, min_genes);
			algData.addGeneMatrix("association-matrix", amat);
					
			
			//Create a gene set array to hold the gene sets after excluding ones which do not pass cutoff
			geneset=new Geneset[gene_set.length-rgset.getExcludedGeneSets().size()];
			
			logger.append("Removing gene sets that do not pass the minimum genes criteria \n");
			geneset=rgset.removeGenesetsWithoutMinimumGenes(rgset.getExcludedGeneSets(), gene_set);
			System.out.println("ATTRACTGUI geneset.length after filters" + geneset.length);
			if (geneset.length == 0) {
				logger.dispose();
				return null;
			}
			
			//Add to excluded genes to Algorithm Data
			algData.addVector("excluded-gene-sets", rgset.getExcludedGeneSets());
			
			//Add the Gene set names to AlgorithmData
			algData.addVector("gene-set-names", new GSEAUtils().getGeneSetNames(geneset));
			
					
						
			}catch(Exception e){
				e.printStackTrace();
			}
			gsea = framework.getAlgorithmFactory().getAlgorithm("GSEA");
			gsea.addAlgorithmListener(listener);
			
			//Run GSEA
			AlgorithmData result = gsea.execute(algData);	
			logger.append("GSEA step completed..\n");
			
			attract = framework.getAlgorithmFactory().getAlgorithm("ATTRACT");
			attract.addAlgorithmListener(listener);
			param=new AttractAlgorithmParameters(geneset);
			logger.append("Bundling parameters for ATTRACT..\n");
						
		    algData.addAlgorithmParameters("attract", param);	
		    
		    logger.append("Algorithm execution begins... \n");
			AlgorithmData attractResult=attract.execute(algData);
			logger.append("Algorithm excecution ends...\n");
			
			//Get gene sets sorted by over enrichment pvalues and size
			geneset=((AttractAlgorithmParameters)result.getAlgorithmParameters("attract")).getGenesets();
			System.out.println("ATTRACTGUI geneset.length afterexecute()" + geneset.length);
			if (geneset.length == 0) {
				logger.dispose();
				return null;
			}
			if(stop) {
				logger.dispose();
				return null;
			}
			logger.append("Generating Viewers...\n");
			
		try {
			resultNode=createResultNode(attractResult, idata, gseaExperiment);
			logger.append("Generating Viewers ends...\n");
			logger.dispose();
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
		}
		
		 return resultNode;
		
	}
	
	
	
	
	 /** creates an empty result if the result is null.
	    * @param result
	    * @return  
	    * 
	    * */
	   protected DefaultMutableTreeNode createEmptyResultNode(AlgorithmData result){
	   	return createEmptyResultNode(result, "Attract");
	   }
	   
	   /** creates an empty result with label label if the result is null.
	    * @param result
	    * @param label the label to apply to the treenode
	    * @return 
	    */
	   protected DefaultMutableTreeNode createEmptyResultNode(AlgorithmData result, String label){
		   DefaultMutableTreeNode root = new DefaultMutableTreeNode(label);
	       root.add(new DefaultMutableTreeNode("No results found"));
	       addGeneralInfo(root, result);
	       return root;
	   }
	   
	   private DefaultMutableTreeNode createResultNode(AlgorithmData result, IData idata, GSEAExperiment experiment) {
			DefaultMutableTreeNode node = null;
			
			node = new DefaultMutableTreeNode("Attract-Significant Gene sets");
			addTableViews(node, result);
			addSynExpressionImages(node, result, experiment);	
	
			return node;
		}
	   
	   /**
	    * Loops through the significant gene sets, creates synExpression graphs and regular MeV viewers
	    * @param root
	    * @param result
	    * @param experiment
	    */
	   private void addSynExpressionImages(DefaultMutableTreeNode root,  AlgorithmData result, Experiment experiment) {
		   DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
		   DefaultMutableTreeNode clusterNode;
		
		   
		 //Loop generates a folder for every gene set. Each folder/geneset has an synexpression viewer, experiment viewer, centroid viewer and table viewer
			  for (int i=0; i<topPathways; i++) {
				  clusterNode = new DefaultMutableTreeNode((String)geneset[i].getGeneSetName());
			
				  //Call addSynExpressionGraph here
				   addSynExpressionGraph(geneset[i].getSynExpressionProfiles(),geneset[i].getSimilarGeneExpressionProfile(),geneset[i], clusterNode);
			 
				  //Call addExpressionViewers here
				//  addExpressionImages(clusterNode, result, experiment, (String)geneset[i].getGeneSetName());
				  
				  node.add(clusterNode);
			  }
			  
			
			  root.add(node);	  
		   
	   }
	   
	   //Go through individual gene sets and plot all associated synexpression groups.
	   //Add to clusterNode. will be called for each geneset
	   
	   private void addSynExpressionGraph(FloatMatrix profile, FloatMatrix corProfile,Geneset geneset ,DefaultMutableTreeNode clusterNode) {
		   int numProfiles=profile.getRowDimension();
		   
		  		  
		   for(int j=0; j<numProfiles; j++) {
			  clusterNode.add(new DefaultMutableTreeNode(new LeafInfo("Syn Expression Group"+(j+1), new SynExpressionViewer(profile.getMatrix(j, j, 0, profile.getColumnDimension()-1),corProfile.getMatrix(j, j, 0, corProfile.getColumnDimension()-1),grpAssignments,factorLevels))));
			  addSynExpressionTableViews(clusterNode, geneset.getSynExpressionGroup("Group"+(j+1)), "Syn Expression Group"+(j+1)+"Genes Table View");
			  addSynExpressionTableViews(clusterNode,geneset.getSimilarGenes("Group"+(j+1)), "Correlated genes Group"+(j+1)+" Table View");
		   }
		   
		   
	   }
	   
	   
	   /**
	    * Adds a table viewer for each synexpression group. The viewer contains a list of genes 
	    * present in the synexpression group.
	    * @param root
	    * @param result
	    */
	   private void addSynExpressionTableViews(DefaultMutableTreeNode clusterNode, String[]genes, String tableName) {
		 
		   String[] header1 = { tableName+""+"Genes" };
		   String[][] dummyString=new String[genes.length][1];
		   for(int k=0; k<dummyString.length; k++) {
				dummyString[k][0]=genes[k];
			 }
		   
		   clusterNode.add(new DefaultMutableTreeNode(new LeafInfo(tableName, new SynExpressionTableViewer(header1,dummyString ))));
		  
		   
		   
	   }
	   
	   
	   /**
	    * Creates and adds Expression images to the node
	    * @param clusterNode
	    * @param result
	    * @param experiment
	    * @param geneSetName
	    */
	   
	   private void addExpressionImages(DefaultMutableTreeNode clusterNode,  AlgorithmData result, Experiment experiment, String geneSetName) {
	 
		   //For every gene set, create an experiment viewer, centroid viewer and table viewer
		   int[][]clusters=new int[1][];
		   clusters=GenesettoProbeMapping(gData, geneset, geneSetName);
		   GSEAUtils utils=new GSEAUtils();
		   FloatMatrix clusterMeans =utils.getMeans(experiment.getMatrix(), clusters);
		   FloatMatrix clusterVars = utils.getVariances(experiment.getMatrix(), clusterMeans, clusters);
		   GSEACentroidViewer centroidViewer=new GSEACentroidViewer(experiment, clusters);
		   centroidViewer.setMeans(clusterMeans.A);
		   centroidViewer.setVariances(clusterVars.A);

		   clusterNode.add(new DefaultMutableTreeNode(new LeafInfo("Expression Image", new GSEAExperimentViewer(experiment, clusters))));
		   clusterNode.add(new DefaultMutableTreeNode(new LeafInfo("Centroid Graph",centroidViewer , new CentroidUserObject(new Integer(0),CentroidUserObject.VARIANCES_MODE))));
		   clusterNode.add(new DefaultMutableTreeNode(new LeafInfo("Expression Graph", centroidViewer, new CentroidUserObject(new Integer(0), CentroidUserObject.VALUES_MODE))));

					
		}
	   
	 
	   
	   
	   
	   /**
	    * Adds a table viewer to the result node
	    * 
	    * @param root
	    * @param result
	    * @param experiment
	    * @param data
	    */
	   private void addTableViews(DefaultMutableTreeNode root, AlgorithmData result) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table Views");
		GSEATableViewer tabViewer;
		
		//Display genes removed by LIMMA
		String[][] dummyString=new String[result.getStringArray("excluded-genes").length][1];
		String[] tempArray=result.getStringArray("excluded-genes");
		
		for(int k=0; k<dummyString.length; k++) {
			dummyString[k][0]=tempArray[k];
		}
		
		String[] header1 = { "Excluded Genes " };
		tabViewer = new GSEATableViewer(header1, dummyString);

		node.add(new DefaultMutableTreeNode(new LeafInfo("Excluded Genes",
				tabViewer, new Integer(0))));


		// Display Excluded Gene sets
		Vector temp = result.getVector("excluded-gene-sets");
	
		String[][] _dummy = new String[temp.size()][1];

		for (int i = 0; i < temp.size(); i++) {
			_dummy[i][0] = (String) temp.get(i);

		}
		String[] header2 = { "Excluded Gene Sets" };
		tabViewer = new GSEATableViewer(header2, _dummy);

		node.add(new DefaultMutableTreeNode(new LeafInfo("Excluded Gene Sets",
				tabViewer, new Integer(0))));

		// Display Probe to Gene mapping
		String[] header3 = new String[max_columns + 1];
		header3[0] = "Gene";
		for (int i = 0; i < max_columns; i++) {
			header3[i + 1] = "Probes";
		}

		tabViewer = new GSEATableViewer(header3, geneToProbeMapping);
		node.add(new DefaultMutableTreeNode(new LeafInfo(
				"Probe to Gene Mapping", tabViewer, new Integer(0))));

		root.add(node);
		}

	
	   /** Adds general algorithm information.
	    * @param root root node
	    * @param result
	    */
	   protected void addGeneralInfo(DefaultMutableTreeNode root, AlgorithmData result){
		   DefaultMutableTreeNode generalInfo = new DefaultMutableTreeNode("General Information");

		   DefaultMutableTreeNode newNode;


		   newNode = new DefaultMutableTreeNode("Analysis Options");
		   DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode("Selected Annotation and Geneset Files");

		   String annotationFileName = result.getParams().getString("annotation-file");
		   fileNode.add(new DefaultMutableTreeNode("Annotation file selected: "+annotationFileName));
		   DefaultMutableTreeNode genesetFileNode = new DefaultMutableTreeNode("Geneset Files");
		   String [] genesetFiles = this.algData.getStringArray("gene-set-files");
		   if(genesetFiles != null)
			   for(int i = 0; i < genesetFiles.length; i++)
				   genesetFileNode.add(new DefaultMutableTreeNode("File: "+genesetFiles[i]));
		   fileNode.add(genesetFileNode);
		   newNode.add(fileNode);
		   AlgorithmParameters params = this.algData.getParams();

		   DefaultMutableTreeNode statNode = new DefaultMutableTreeNode("Algorithm Parameters");

		   statNode.add(new DefaultMutableTreeNode("Probe to Gene collapse mode:"+  params.getString("probe_value")));
		   if(params.getString("probe_value").equals(GSEAConstants.SD))
			   statNode.add(new DefaultMutableTreeNode("Standard Deviation Cutoff:"+ params.getString("standard-deviation-cutoff")));
		   statNode.add(new DefaultMutableTreeNode("Minimum number of genes that must be presen in a geneset: " + params.getString("gene-number")));
		   statNode.add(new DefaultMutableTreeNode("Number of permutations: "+params.getString("permutations")));
		   statNode.add(new DefaultMutableTreeNode("Identifier used to annotate genes in the geneset: "+params.getString("gene-identifier")));
		   
		   String[]factorNames=this.algData.getStringArray("factor-names");
		   for(int index=0; index<factorNames.length; index++){
			statNode.add(new DefaultMutableTreeNode("Factorname:"+ factorNames[index]));   
		   }
		   
		   int[]factorLevels=this.algData.getIntArray("factor-levels");
		   for(int i=0; i<factorLevels.length; i++){
			   statNode.add(new DefaultMutableTreeNode("FactorLevel:"+factorLevels[i]));
		   }
		   
		   newNode.add(statNode);

		   generalInfo.add(newNode);

		   root.add(generalInfo);
	   }
	
	   
	  /**
	   * Returns a 2 d array containing indices of genes present in a gene set.
	   * These indices correspond to their positions in the experiment data matrix
	   * 
	   *@return 2 d int array
	   */
	   private int[][]GenesettoProbeMapping(IGeneData[] gData, Geneset[]gset, String name){
		   
		int genesetIndex = 0;

		int geneDataElementIndex = 0;

		// The size of geneset_clusters would be equal to the number of gene
		// sets
		int[][] geneset_clusters = new int[1][];

		// Vector containing the indices of probes mapping to a gene
		Vector probe_mappings;

		// integer array of probe_mappings
		int[] probe_mappings_array;

		// Iterate over gene sets till you find the desired one
		for (int index = 0; index < gset.length; index++) {
			if (gset[index].getGeneSetName().equalsIgnoreCase(name)) {
				genesetIndex = index;
				break;
			}
		}

		int genesetElementIndex = 0;
		// Fetch the array list containing gene set elements
		ArrayList<IGeneSetElement> gsElementList = gset[genesetIndex]
				.getGenesetElements();
		probe_mappings = new Vector();
		// Iterate over the elements in the gene sets
		while (genesetElementIndex < gsElementList.size()) {
			// Retrieve the gene name from the gene set
			GeneSetElement gselement = (GeneSetElement) gsElementList
					.get(genesetElementIndex);
			String Gene = (String) gselement.getGene();
			// System.out.println("Gene:"+Gene);
			// Retrieve the index of this gene from Gene Data Element
			GeneDataElement gde = (GeneDataElement) ((GeneData) gData[0])
					.getGeneDataElement(Gene);

			// Populate the probe_mappings vector here
			for (int index = 0; index < gde.getProbePosition().size(); index++) {
				probe_mappings.add((Integer) gde.getProbePosition().get(index));
				// System.out.print(gde.getProbePosition().get(index));
				// System.out.print('\t');
			}
			// System.out.println();
			genesetElementIndex = genesetElementIndex + 1;

		} // Gene set elements while loop ends
		// Populate the probe_mappings_array
		probe_mappings_array = new int[probe_mappings.size()];
		geneset_clusters = new int[1][probe_mappings.size()];
		for (int index = 0; index < probe_mappings.size(); index++) {
			geneset_clusters[0][index] = ((Integer) probe_mappings.get(index))
					.intValue();
		}

		return geneset_clusters;
	   }
	   
	
	/*
	 *  The class to listen to progress, monitor and algorithms events.
    */
   private class Listener extends DialogListener implements AlgorithmListener {
       
             
       public void actionPerformed(ActionEvent e) {
           String command = e.getActionCommand();
           if (command.equals("cancel-command")) {
        	   stop = true;
        	   if(gsea!=null)
        		   gsea.abort();
               logger.dispose();
           }
       }
       
       public void valueChanged(AlgorithmEvent event) {
           
       }
       
       public void windowClosing(WindowEvent e) {
           gsea.abort();
           
       }
   }
	
   /**
	 * checkFileNameExtension returns the extension of the file.
	 * @param fileName
	 * @return
	 */
	
	public String checkFileNameExtension(String fileName){
		String extension=fileName.substring(fileName.lastIndexOf('.')+1, fileName.length());
		//System.out.println("Extension:"+extension);	
		return extension;
	}
	
	
}
