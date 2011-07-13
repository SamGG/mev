package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
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
import org.tigr.util.FloatMatrix;

public class GSEAGUI implements IClusterGUI {
	 	
		private Algorithm gsea;
		
	    private Experiment experiment;
	    private IData idata;
	    private Logger logger;
	    private Listener listener;
	    private String[][]geneToProbeMapping;
	    //max_columns decides the number of columns to display in the
	    //table viewer
	    int max_columns;
	   
	    private  AlgorithmData algData = new AlgorithmData();
	    private HashMap<String, LinkedHashMap<String, Float>>orderedTestStats=new HashMap<String, LinkedHashMap<String, Float>>();
	    private HashMap<String, LinkedHashMap<String, Float>>descendingSortedTStats=new HashMap<String, LinkedHashMap<String, Float>>();
	    private Geneset[]geneset=null;
		private IGeneData[]gData=null;
		private ArrayList<String>sorted_gene_names=new ArrayList<String>();
		private boolean stop=false;
		
	public DefaultMutableTreeNode execute(IFramework framework)	throws AlgorithmException {
	
		this.experiment = framework.getData().getExperiment();
        this.idata = framework.getData();
        FloatMatrix matrix = experiment.getMatrix();
		DefaultMutableTreeNode resultNode = null;
		
		
		Geneset[]gset=null;
		algData.addMatrix("matrix",matrix);
					
		JFrame mainFrame = (JFrame)(framework.getFrame());
		//Need the "." after the step names, to keep track of the highlighting
		String [] steps = {"Data Selection.", "Parameter Selection.", "Execute."};		
		
		//Create the initialization wizard.
		GSEAInitWizard wiz=new GSEAInitWizard(idata, mainFrame, "GSEA Initialization", true, algData,steps,  1, new StepsPanel(), framework.getClusterRepository(1), framework);
		wiz.setVisible(true);
		listener = new Listener();
	    logger = new Logger(framework.getFrame(), "Gene Set Enrichment Analysis", listener);
	         
		//If execute button was clicked
		
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
			
			if(stop)
				return null;
			
			//Get the gene identifier from AlgorithmData and use it to collapse the probes
			gData=ptg.convertProbeToGene(algData.getParams().getString("gene-identifier"), collapsemode, cutoff);
			
			
			ptg.returnGSEAExperiment();
			Vector genesInExpressionData=algData.getVector("Unique-Genes-in-Expressionset");

			
			//Second step is to read the Gene Set file itself. Once this is done, the gene sets will have to be further
			//processed to remove the genes, which are present in the gene set but NOT in GeneData (expressiondata). 
			//Gene set object is also processed to remove the gene sets which do not have the minimum number of genes
			//as specified by the user.
			
			logger.append("Reading gene set files \n");
			
			if(stop)
				return null;
			
			ReadGeneSet rgset=new ReadGeneSet(extension, genesetFilePath);
			try{
				
				gset=rgset.readMultipleFiles(algData.getStringArray("gene-set-files"), genesetFilePath);
				//System.out.println("gene set size after raedMultipleFiles in GSEAGUI:"+gset.length);
				Geneset[] gene_set=rgset.removeGenesNotinExpressionData(gset, genesInExpressionData);
				 
				

			//Third step is to generate Association Matrix. The Association Matrix generated, does not include gene set
			//which do not satisfy the minimum number of genes criteria
			logger.append("Creating Association Matrix \n");
			
			if(stop)
				return null;
			
			FloatMatrix amat=rgset.createAssociationMatrix(gene_set, genesInExpressionData, min_genes);
			algData.addGeneMatrix("association-matrix", amat);
					
			
			//Create a gene set array to hold the gene sets after excluding ones which do not pass cutoff
			geneset=new Geneset[gene_set.length-rgset.getExcludedGeneSets().size()];
			
			logger.append("Removing gene sets that do not pass the minimum genes criteria \n");
			geneset=rgset.removeGenesetsWithoutMinimumGenes(rgset.getExcludedGeneSets(), gene_set);
			
			//Add the Gene set names to AlgorithmData
			algData.addVector("gene-set-names", new GSEAUtils().getGeneSetNames(geneset));
			
						
			//Add to Algorithm Data, so that can access from viewers
			algData.addVector("excluded-gene-sets", rgset.getExcludedGeneSets());
			
		
			
			}catch(Exception e){
				e.printStackTrace();
			}
			gsea = framework.getAlgorithmFactory().getAlgorithm("GSEA");
			gsea.addAlgorithmListener(listener);
			logger.append("Algorithm execution begins... \n");
		    
		    AlgorithmData result = gsea.execute(algData);	
			
			logger.append("Algorithm execution ends...\n");
		
			if(stop)
				return null;
			
			logger.append("Generating Viewers...\n");
				
			//Populate the test statistic in to gene sets
			GSEAUtils utils=new GSEAUtils();
			geneset=utils.populateTestStatistic(gData, geneset, algData.getGeneMatrix("lmPerGene-coefficients"));
			
			orderedTestStats=utils.getSortedTestStats(geneset);
			this.sorted_gene_names=utils.getSorted_gene_names();
			
			
			descendingSortedTStats=utils.getDescendingSortedTestStats(geneset);
			
			
			
			//String array containing Gene to Probe mapping, which will be used in the table viewers	
			geneToProbeMapping=((GeneData)gData[0]).getProbetoGeneMapping(gData);
		
			//Decides the number of columns in the table viewer. 
			//The reason being one Gene may map to one probe and another to ten. 
			 
			this.max_columns=((GeneData)gData[0]).get_max_num_probes_mapping_to_gene();
	
						
			if(result.getMappings("over-enriched") == null)
	            resultNode = createEmptyResultNode(result);
			else
			resultNode = createResultNode(result, idata, null);
			logger.append("Generating Viewers ends...\n");
			logger.dispose();
			return resultNode;	
		
		}
		
		return null;
			
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
	
   
   /** creates an empty result if the result is null.
    * @param result
    * @return  
    * 
    */
   protected DefaultMutableTreeNode createEmptyResultNode(AlgorithmData result){
   	return createEmptyResultNode(result, "GSEA");
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
		
			node = new DefaultMutableTreeNode("GSEA");
			addPValueGraphImage(node, result);
			addGenesetMembershipPlot(node, result);
			addTableViews(node, result, experiment, idata);
			addResultImages(node, result, this.experiment);
			
			
			
		return node;
	}
   /**
    * Creates GenesetMembership plot and adds it to the result tree	
    * @param root
    * @param result
    */
   private void addGenesetMembershipPlot(DefaultMutableTreeNode root, AlgorithmData result){
	  
	   Object[]mappingKey=result.getMappings("over-enriched").keySet().toArray();
	   ArrayList<String>genesetNames=new ArrayList<String>();
	   for(int index=0; index<mappingKey.length; index++) {
		   genesetNames.add((String)mappingKey[index]);
	   }
	   
	
	  root.add(new DefaultMutableTreeNode(new LeafInfo("Geneset Membership Plot", new GenesetMembership(this.sorted_gene_names, genesetNames, this.geneset))));
	   
   }
   
   /**
    * Creates p value graph and adds it to the result tree
    * @param root
    * @param result
    */
   private void addPValueGraphImage(DefaultMutableTreeNode root, AlgorithmData result){
	   
	   LinkedHashMap overenriched=result.getMappings("over-enriched");
	   PValueGraphViewer pvg=new PValueGraphViewer("P Value graph","Genesets", "p-Values", overenriched);
	   root.add(new DefaultMutableTreeNode(new LeafInfo("Geneset p-value graph", pvg)));
	   
   }
   
   /**
    * Creates expression viewer, centroid viewer, test statistic graph and leading edge graph 
    * and adds it to the result tree
    * @param root
    * @param result
    * @param experiment
    */

   private void addResultImages(DefaultMutableTreeNode root,  AlgorithmData result, Experiment experiment) {

		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Gene Set Results");
					
    	String[]header1= {"Gene set", "Incremental J-G score"};
    	String[]header2= {"Gene set", "Test Statistic"};
		DefaultMutableTreeNode clusterNode;
		GSEAUtils util=new GSEAUtils();
		
		//Generate a 2 d string array from the over and under enriched linked hashmaps
		Object[]gene_set_names=result.getMappings("over-enriched").keySet().toArray();
		//Loop generates a folder for every gene set. Each folder/geneset has an experiment viewer, centroid viewer and table viewer
		  for (int i=0; i<gene_set_names.length; i++) {
			  int[][]clusters=new int[1][];
			  clusters=GenesettoProbeMapping(gData, geneset, (String)gene_set_names[i]);
			
				FloatMatrix clusterMeans = util.getMeans(experiment.getMatrix(), clusters);
				FloatMatrix clusterVars = util.getVariances(experiment.getMatrix(), clusterMeans, clusters);
				GSEACentroidViewer centroidViewer=new GSEACentroidViewer(experiment, clusters);
				centroidViewer.setMeans(clusterMeans.A);
				centroidViewer.setVariances(clusterVars.A);
	            clusterNode = new DefaultMutableTreeNode("Set "+(i+1)+": " + (String)gene_set_names[i]);
	            clusterNode.add(new DefaultMutableTreeNode(new LeafInfo("Expression Image", new GSEAExperimentViewer(experiment, clusters))));
	            clusterNode.add(new DefaultMutableTreeNode(new LeafInfo("Centroid Graph",centroidViewer , new CentroidUserObject(new Integer(0),CentroidUserObject.VARIANCES_MODE))));
	            clusterNode.add(new DefaultMutableTreeNode(new LeafInfo("Expression Graph", centroidViewer, new CentroidUserObject(new Integer(0), CentroidUserObject.VALUES_MODE))));
	            clusterNode.add(new DefaultMutableTreeNode(new LeafInfo("Test statistics graph", new TestStatisticViewer(getOrderedTestStats().get((String)gene_set_names[i])))));
	            TestStatisticTableViewer testStatTabView=new TestStatisticTableViewer(header2, getOrderedTestStatasStringArray(getOrderedTestStats().get((String)gene_set_names[i])));
	            clusterNode.add(new DefaultMutableTreeNode(new LeafInfo("Test statistics table view", testStatTabView)));
	            
	            clusterNode.add(new DefaultMutableTreeNode(new LeafInfo("Leading Edge Graph", new LeadingEdgeSubsetViewer(getDescendingSortedTStats().get((String)gene_set_names[i])))));
	            String[][] temp=new LeadingEdgeSubsetViewer(getDescendingSortedTStats().get((String)gene_set_names[i])).getLeadingEdgeGenes();
	            LeadingEdgeTableViewer tabViewer=new LeadingEdgeTableViewer(header1,temp );
	            clusterNode.add(new DefaultMutableTreeNode(new LeafInfo("Leading edge genes", tabViewer)));
	            node.add(clusterNode);
	            
	        }
	 	 		
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
   
 
   
 
   
   public HashMap<String,LinkedHashMap<String, Float> > getOrderedTestStats(){
	   return orderedTestStats;
   }
   
   
   public String[][]getOrderedTestStatasStringArray(LinkedHashMap<String, Float>testStat){
	   String[][]orderedTStat=new String[testStat.size()][2];
	   Iterator<String> temp=testStat.keySet().iterator();
	   int index=0;
	   
	   while(temp.hasNext()) {
		   String key=temp.next();
		   orderedTStat[index][0]=key;
		   orderedTStat[index][1]=Float.toString(testStat.get(key));
		   index=index+1;
		
		   
	   }
	
	   return orderedTStat;
	   
   }
   
   
   public HashMap<String,LinkedHashMap<String, Float> > getDescendingSortedTStats(){
	   return descendingSortedTStats;
   }
   
   
   /**
    * Returns a 2 d integer array containing mappings of probes to genes.
    * Mappings are the actual position of the probes on the expression array. Each row
    * corresponds to a gene set.
    * 
    */
   private int[][]GenesettoProbeMapping(IGeneData[] gData, Geneset[]gset, String name){
	   
	   int genesetIndex=0;
	 
	   int geneDataElementIndex=0;
	 
	   //The size of geneset_clusters would be equal to the number of gene sets 
	   int[][]geneset_clusters=new int[1][];
	   
	   //Vector containing the indices of probes mapping to a gene
	   Vector probe_mappings;
	   
	   //integer array of probe_mappings
	   int[]probe_mappings_array;
	   
	   //Iterate over gene sets till you find the desired one
	   for(int index=0; index<gset.length; index++) {
		   if(gset[index].getGeneSetName().equalsIgnoreCase(name)) {
			   genesetIndex=index;
		   		break;
		   }
	   }
	    
	    int genesetElementIndex=0;
		   //Fetch the array list containing  gene set elements
		   ArrayList<IGeneSetElement> gsElementList=gset[genesetIndex].getGenesetElements();
		   probe_mappings=new Vector();
		   //Iterate over the elements in the gene sets
		   while(genesetElementIndex<gsElementList.size()){
			   //Retrieve the gene name from the gene set
			   GeneSetElement gselement=(GeneSetElement)gsElementList.get(genesetElementIndex);
			   String Gene=(String)gselement.getGene();
		//	   System.out.println("Gene:"+Gene);
			   //Retrieve the index of this gene from Gene Data Element 
			   GeneDataElement gde=(GeneDataElement)((GeneData)gData[0]).getGeneDataElement(Gene);
			   
			   //Populate the probe_mappings vector here
			   for(int index=0; index<gde.getProbePosition().size(); index++){
				   probe_mappings.add((Integer)gde.getProbePosition().get(index));
			//	   System.out.print(gde.getProbePosition().get(index));
			//	   System.out.print('\t');
			   }
			  // System.out.println();
			  genesetElementIndex=genesetElementIndex+1;
			   
		   } //Gene set elements while loop ends
		   //Populate the probe_mappings_array
		   probe_mappings_array=new int[probe_mappings.size()];
		   geneset_clusters=new int[1][probe_mappings.size()];
		   for(int index=0; index<probe_mappings.size(); index++){
			   geneset_clusters[0][index]=((Integer)probe_mappings.get(index)).intValue();
		   }
	
	   return geneset_clusters;
   }
   
   
   
   private void addTableViews(DefaultMutableTreeNode root, AlgorithmData result, GSEAExperiment experiment, IData data) {
   	DefaultMutableTreeNode node = new DefaultMutableTreeNode("Results");
   	GSEATableViewer tabViewer;
   	
   	LinkedHashMap overenriched=result.getMappings("over-enriched");
   	LinkedHashMap underenriched=result.getMappings("under-enriched");
   	
	String[][]pVals =new String[overenriched.size()][4];
   	
   	//Generate a 2 d string array from the over and under enriched linked hashmaps
	Object[]gene_sets=overenriched.keySet().toArray();
   	
	for(int i=0; i<overenriched.size(); i++){
		pVals[i][0]=Integer.toString(i+1);
		pVals[i][1]=(String)gene_sets[i];
		pVals[i][2]=((Float)underenriched.get(gene_sets[i])).toString();
		pVals[i][3]=((Float)overenriched.get(gene_sets[i])).toString();
   	}
   	
   	
   
   	String[]headernames={"Index", "Gene Set", "Lower-pValues (Under-Enriched)", "Upper-pValues (Over-Enriched)"};
   	
   	
   //Display Significant Gene Sets
   	tabViewer = new GSEATableViewer(headernames,pVals, root,experiment);
   	node.add(new DefaultMutableTreeNode(new LeafInfo("Included Gene Sets", tabViewer, new Integer(0))));
   
   //Display Excluded Gene sets
   	Vector temp=result.getVector("excluded-gene-sets");
   	String[][]_dummy=new String[temp.size()][1];
   	
   	for(int i=0; i<temp.size(); i++){
   		
   			_dummy[i][0]=(String)temp.get(i);
   			//System.out.println("Excluded gene set name:"+(String)temp.get(i));
   		
   	}
    String[]header1={"Excluded Gene Sets"};  	
  	tabViewer = new GSEATableViewer(header1,_dummy);
 
   	node.add(new DefaultMutableTreeNode(new LeafInfo("Excluded Gene Sets", tabViewer, new Integer(0))));
 
   	
   	
  //Display Collapse Probe to Gene 
   //System.out.println("max columns:"+this.max_columns);
   String[]header2=new String[this.max_columns+1];
    header2[0]="Gene";
   	for(int i=0; i<max_columns; i++){
   		header2[i+1]="Probes";
   	}
   	
   	
    //tabViewer=new GSEATableViewer(header2,geneToProbeMapping, root, experiment);
   	for (int i=0; i<geneToProbeMapping.length; i++){
   		for (int j=0; j<geneToProbeMapping[i].length; j++){
   			if (geneToProbeMapping[i][j]==null||geneToProbeMapping[i][j].equals("null"))
   					geneToProbeMapping[i][j]="";
   		}
   	}
   	tabViewer=new GSEATableViewer(header2,geneToProbeMapping);
    node.add(new DefaultMutableTreeNode(new LeafInfo("Probe to Gene Mapping", tabViewer, new Integer(0))));
   	
   	
   	root.add(node);
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
	
		
	
	
	public static void main(String[] args){
		
		
	}
	
	
	}	


