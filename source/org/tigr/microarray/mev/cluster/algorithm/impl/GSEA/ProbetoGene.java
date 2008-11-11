package org.tigr.microarray.mev.cluster.algorithm.impl.GSEA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.ISlideDataElement;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.annotation.AnnotationConstants;
import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.annotation.IAnnotation;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.gui.GSEAExperiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.GSEA.GSEAConstants;
import org.tigr.util.FloatMatrix;
import JSci.maths.ArrayMath;

public class ProbetoGene {
	private AlgorithmData aData;
	private IData data;
	private Vector unique_genes_in_data=new Vector();
	//GeneDataMatrix is a FloatMatrix containing the Gene expression values. This would be used for downstream analysis (lmPerGene etc).
	private FloatMatrix geneDataMatrix;
	private GSEAExperiment gseaExperiment;
	private int[]columns;
	private FloatMatrix experiment_matrix;
	private GeneDataElement gde;
	private GeneData[]gene_data;
	
	
	/**
	 * 
	 * @param algData
	 * @param idata
	 * @param gset
	 * While calling the ProbetoGene constructor, pass the first Geneset element.
	 * 
	 */
	public ProbetoGene(AlgorithmData algData, IData idata){
		this.aData=algData;
		this.data=idata;
		
		experiment_matrix = aData.getMatrix("matrix");
		
	
	}
	
	/**
	 * 
	 */
	public GeneData[] convertProbeToGene(String geneIdentifier, String conversionCriteria, String SDcutoff){
		// System.out.println("gene identifier:"+geneIdentifier);
		 int count=0;
		 //Number of rows in the loaded expression data
		 int arrayRows=data.getFeaturesSize();
	
	
		 //Number of samples in the expression dataset
		 int cols=data.getFeaturesCount();
		
		
	
		 GeneData[]gdata =new GeneData[cols];
		
		 
		 /*
		  * Initialize the gene data and GeneDataElement. Each of the geneData should have this information and hence the loop
		  * 
		  */
		 
		 for(int index=0;index<arrayRows; index++){
			 ISlideDataElement isde=data.getFeature(0).getSlideDataElement(index);
			 
			 MevAnnotation ann=(MevAnnotation)isde.getElementAnnotation();
			 String[] temp=(ann.getAttribute(geneIdentifier));
			 String currentID=temp[0];
			 currentID=currentID.toUpperCase();
			 
			 
			 
			 if(!unique_genes_in_data.contains(currentID) && !currentID.equalsIgnoreCase("NA")){
				 unique_genes_in_data.add(count, currentID);
				 count=count+1;
			 }
	
		 }
		 
		 setUniqueGenesinDataset(unique_genes_in_data);
		 
		 //Number of genes (unique) present in the expression data set
		 int geneRows=unique_genes_in_data.size();
		 
		 geneDataMatrix=new FloatMatrix(geneRows, cols);
		
		 
		 for(int i=0; i<cols; i++){
			
			 gdata[i]=new GeneData(geneRows,1);
			 String slideName=data.getFeature(i).getSlideDataName();
			 String slideFile=data.getFeature(i).getSlideFileName();
			 
			 gdata[i].setSlideName(slideName);
			 
			 for(int index=0; index<unique_genes_in_data.size();index++){
				 String requiredID=(String)unique_genes_in_data.get(index);
				 gde=new GeneDataElement(index, requiredID);
				 gdata[i].setGeneDataElement(gde, index);
				 
			 }
			 
		 }
			
		 /**
		  * Loop to figure out which probes match to which gene. The name and position of the probes in the
		  * expression data is stored. This is done ONLY for the first GeneData. This information
		  * will be used to capture the expression values corresponding to the probes from the matrix.
		  * This expression matrix will be obtained from the Algorithm Data class.   
		  * 
		  * 
		  * 
		  * 
		  */
		
		 
		 for(int index=0; index<arrayRows; index++){

			 ISlideDataElement isde=data.getFeature(0).getSlideDataElement(index);
			 MevAnnotation ann=(MevAnnotation)isde.getElementAnnotation();
			 boolean found=false;
			 
			 String currentID=(ann.getAttribute(geneIdentifier))[0];
			 String probeID=ann.getCloneID();
			
			 
			 count=0;


			 while(count < geneRows && found==false){
				 String requiredID=(String)unique_genes_in_data.get(count);
				
				 if(currentID.equalsIgnoreCase(requiredID)){
					 gde=(GeneDataElement)gdata[0].getGeneDataElement(count);
					
					 if(!gde.getProbeID().contains(probeID)){
						// System.out.println("probeID:"+probeID);
						 //System.out.println("probePosition:"+index);
						 gde.setProbeID(probeID);
						 gde.setProbePosition(index);
					 }
					 //Get out of while loop as soon as a match is found and advance to the next probe.
					 //count=count+1;---Think about this. What if one probe matches >1gene
					// break;
					 found=true;

				 }
				 //If probeID does not match current geneData entry, remain in the while loop
				 // and advance to the next probe
				 else{
					 count=count+1;
				 }



			 }//End of while loop
			 
			 
			 
		 }//End of for loop
		 
		 
		 /**
		  * For testing
		  * Write out GeneData to a file
		  * 
		  *
		 File probetogeneFile;
		try {
			probetogeneFile = new File("c:/MeV-GSEA-Devel/ProbetoGene.txt");
			 PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(probetogeneFile)));
			 int tcount=0;
			 while(tcount<unique_genes_in_data.size()){
				 gde=(GeneDataElement)gdata[0].getGeneDataElement(tcount);
				 String id=gde.getGeneIdentifier();
				 pw.write(id);
				 pw.write('\t');
				 Vector _temp=gdata[0].getGeneDataElement(tcount).getProbeID();
				 for(int i=0; i<_temp.size(); i++){
					 pw.write((String)_temp.get(i));
					 pw.write('\t');
				 }
				 pw.write('\n');
				 tcount=tcount+1;
				 
			 }
			 
			 
			 pw.close();
			 
			 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	
		 
		 
		 
		 
		 
		 
		 
		/**
		 * Use the float matrix contained in the AlgorithmData, to populate the expression values
		 * in GeneDataElement. The original values obtained from the floatMatrix will be stored in the
		 * variable "trueIntensities". The variable "current Intensities" would contain expression vales
		 * obtained after applying (MAX_PROBE, MEDIAN_PROBE or SD) 
		 * 
		 */
		 
	
		
		//If conversion criteria is Standard Deviation, choose the probe that has the higher standard deviation across samples,
		//if multiple probes map to a gene
		 if(conversionCriteria.equalsIgnoreCase(GSEAConstants.SD)){

			 gene_data=remove_lowVar_probes(gdata, SDcutoff);//---commented for testing
			

		 }//If loop (conversion criteria==SD)ends
		
		else{	
			for(int j=0; j<geneRows; j++){
				count=0;
				int num_probes=(gdata[0].getGeneDataElement(j).getProbeID().size());
				Vector probePos=(gdata[0].getGeneDataElement(j).getProbePosition());
			
				while(count <cols){
					//If only one probe maps to a gene

					if(num_probes ==1){
						int probe_pos=Integer.parseInt(probePos.get(0).toString());
						//System.out.println("matrix value:"+matrix.get(probe_pos,count));

						gdata[count].getGeneDataElement(j).setCurrentIntensity(experiment_matrix.get(probe_pos,count));
						gdata[count].getGeneDataElement(j).setTrueIntensity(experiment_matrix.get(probe_pos,count));
						//Added Tuesday June 3, 08
						geneDataMatrix.set(j, count, experiment_matrix.get(probe_pos, count));
					}else{

						for(int k=0; k<num_probes;k++){
							int probe_pos=Integer.parseInt(probePos.get(k).toString());
							gdata[count].getGeneDataElement(j).setTrueIntensity(experiment_matrix.get(probe_pos, count));
							
						}
					
						Vector intensity=gdata[count].getGeneDataElement(j).getTrueIntensity();
					//	System.out.println("gene:"+gdata[0].getGeneDataElement(j).getGeneIdentifier());
						//System.out.println("Number of probes corr to the gene:"+num_probes);
						if(conversionCriteria.equalsIgnoreCase(GSEAConstants.MAX_PROBE)){
							float max_probe=getMaxProbe(intensity);
							gdata[count].getGeneDataElement(j).setCurrentIntensity(max_probe);
							geneDataMatrix.set(j, count,max_probe);
							intensity=new Vector();
						}
						if(conversionCriteria.equalsIgnoreCase(GSEAConstants.MEDIAN_PROBE)){
							float med_probe=getMedianProbe(intensity);
							gdata[count].getGeneDataElement(j).setCurrentIntensity(med_probe);
							geneDataMatrix.set(j, count,med_probe );
							intensity=new Vector();
						}


					}

					count=count+1;


				}//End of while loop



			}//End of for loop
			
			gene_data=gdata;
		}//End of ELSE 
		 
			 
		 

		/*********************** testing BEGINS********************************/
		 /**
		  * For testing
		  * Write out GeneData to a file
		  * File will conatin gene identifier followed by tab delimited expression vales(after max/median or sd )has
		  * been applied
		  * 
		  * 
		  *
		 File GeneDataFile;
		try {
			GeneDataFile = new File("C:/Users/sarita/Desktop/GSEA-TestData/GeneData.txt");
			 PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(GeneDataFile)));
			 int tcount=0;
			 while(tcount<unique_genes_in_data.size()){
				 gde=(GeneDataElement)gdata[0].getGeneDataElement(tcount);
				 String id=gde.getGeneIdentifier();
				 pw.write(id);
				 pw.write('\t');
				
				 for(int i=0; i<data.getFeaturesCount(); i++){
					 float _temp=gdata[i].getGeneDataElement(tcount).getCurrentIntensity();
					 pw.write(Float.toString(_temp));
					 pw.write('\t');
				 }
				 pw.write('\n');
				 tcount=tcount+1;
				 
			 }
			 
			 
			 pw.close();
			 
			 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		 
		 
		 /***********************For testing ends********************************/
		 /**********For testing*******************************************	
		 File expMatrixFile;
			try {
				expMatrixFile = new File("C:/Users/sarita/Desktop/GSEA-TestData/JavaMatrix_after_MEDIAN.txt");
				 PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(expMatrixFile)));
				 int tcount=0;
				 while(tcount<geneDataMatrix.getRowDimension()){
					 
					 gde=(GeneDataElement)gene_data[0].getGeneDataElement(tcount);
					 String id=gde.getGeneIdentifier();
				
					 
					 pw.write(id);
					 pw.write('\t');
					
					 for(int i=0; i<geneDataMatrix.getColumnDimension(); i++){
						 pw.write(((Float)geneDataMatrix.get(tcount, i)).toString());
						 pw.write('\t');
					 }
					 pw.write('\n');
					 tcount=tcount+1;
					 
				 }
				 
				 
				 pw.close();
				 
				 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/**********For testing*******************************************/
	
	
		
	
	
		 /**********For testing****************************************
		  * Writes out the true intensity of the probes which MeV recorded.
		  * Format: Gene ID \t expr(probe1)\t exprs(probe2)
		  * 		Gene ID2 \t exprs(probe3)\t
		  * 
		  * 
		  * 
		  * 
		  * ***	
		 File GeneDataFile;
			try {
				GeneDataFile = new File("C:/Users/sarita/Desktop/GSEA-TestData/GeneDataMatrix.txt");
				 PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(GeneDataFile)));
				 int tcount=0;
				 while(tcount<geneDataMatrix.getRowDimension()){
					 gde=(GeneDataElement)gene_data[0].getGeneDataElement(tcount);
					 String id=gde.getGeneIdentifier();
					 pw.write(id);
					 pw.write('\t');
					
					 for(int i=0; i<10; i++){
						 gde=(GeneDataElement)gene_data[i].getGeneDataElement(tcount);
						 Vector intensity=gde.getTrueIntensity();
						 for(int k=0; k<intensity.size(); k++){
							 pw.write(((Float)intensity.get(k)).toString());
							 pw.write('\t');
						 }
						 pw.write("||");
						 pw.write('\t');
					 }
					 pw.write('\n');
					 tcount=tcount+1;
					 
				 }
				 
				 
				 pw.close();
				 
				 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/**********For testing*******************************************/
	
		 
		 
		 
		 
		 
		
		
		gseaExperiment=new GSEAExperiment(geneDataMatrix, createColumns(data.getFeaturesCount()));
		this.aData.addGeneMatrix("gene-data-matrix", geneDataMatrix);
		this.aData.addVector("Unique-Genes-in-Expressionset", unique_genes_in_data);
		
		return gene_data;
	}//End of the function convertProbetoGene
	
	//Need to return this. The reason being, it would be useful to get the information of what probes map
	//to what gene etc, while generating expressionviewer. SpotInformationBox can contain this info.

	
	public GSEAExperiment returnGSEAExperiment(){
		return this.gseaExperiment;
	}
	
	/**
	 * remove_lowVar_probes does the following
	 * 1. Goes through the Genes in the original GeneData, checks if it has one or multiple probes
	 * associated with it. 
	 * 2. Checks the variability of the probes across samples, by calculating SD. Checks if the SD is greater than the user specified cutoff 
	 * 3. If there are multiple probes associated with one genes, ONLY the probe with the maximum SD and exceeding the user specified 
	 * cutoff is retained.
	 * 4. If all the probes of a genes fail to make the cutoff, the gene is not included for further analysis   
	 * 
	 * 
	 * 
	 * @return
	 */
	
	public GeneData[]remove_lowVar_probes(GeneData[]gdata, String SDcutoff){
		
		GeneData[]gene_data;
		//List of genes (subset of the unique ones in th expression data) which pass the SD cutoff
		Vector genes=new Vector();
		//List of probes which pass the cutoff AND also have the highest SD
		Vector probes_passing_cutoff=new Vector();
		//Tracks the num of genes passing the cutoff
		int num_genes_passing_cutoff=0;
		
		//List of genes excluded from the analysis for failing to pass the SD cutoff.
		Vector genes_with_low_variability=new Vector();
		 
		//Number of samples in the experiment
		int cols=data.getFeaturesCount();
		
		FloatMatrix geneDataMatrix;
		
		//for testing
		Vector probe=new Vector();

		
		/**
		 * First step in removing the probes, is to go through the unique genes
		 * in the expression data. Check if the probes mapping to the genes pass the
		 * SD cut off. if so, add the gene to the Vector genes. Also add the position of the probe
		 * which has the Maximum  SD to the vector probes_passing_cutoff. This can be used to populate 
		 * the float matrix gene data matrix.
		 * 
		 * 
		 * 
		 */

		for(int index=0; index<unique_genes_in_data.size(); index++){
			//Captures the probe position with the maximum SD across samples
			int max_probeSD_pos=-1;
			//Max Value of SD for a gene 
			double max_sd=0;
			//SD of a probe across samples
			double current_sd=0;
			//array containing the expression values of a probe across samples
			double[]matrixVals=new double[cols];


			//Extract the number of probes associated with the gene.
			int num_probes=(gdata[0].getGeneDataElement(index).getProbeID().size());
			//Extract the exact position (corresponding to the expression data) of the probes mapping to the current gene
			Vector probePos=(gdata[0].getGeneDataElement(index).getProbePosition());
			//Extract the gene corresponding to this entry
			String Gene=gdata[0].getGeneDataElement(index).getGeneIdentifier();
			//Local variable to keep a track of probe positions
			int probe_pos=0;


			//If there is just one probe mapping to a gene, extract the probe position. 

			if(num_probes==1){
				probe_pos=Integer.parseInt(probePos.get(0).toString());

				//Calculate the SD of the probe across samples and see if it exceeds the user cutoff
				for(int col=0; col<cols;col++){
					matrixVals=new double[cols];
					matrixVals[col]=(double)experiment_matrix.get(probe_pos, col);
				}
				current_sd=JSci.maths.ArrayMath.standardDeviation(matrixVals);
				if(current_sd > Double.parseDouble(SDcutoff)){
					 
					//Keep a track of the probes passing SD cutoff. This can be used to populate the Gene Data Matrix(FloatMatrix)
					probes_passing_cutoff.add(num_genes_passing_cutoff, probe_pos);
					
					//Set it as a unique gene
					genes.add(num_genes_passing_cutoff, Gene);
					num_genes_passing_cutoff=num_genes_passing_cutoff+1;
				}else{
					genes_with_low_variability.add(Gene);
				}

			}else{
				matrixVals=new double[cols];
				//Loops through the probes mapping to a gene
				for(int k=0; k<num_probes;k++){
					//Gets the position of that probe
					probe_pos=Integer.parseInt(probePos.get(k).toString());
					//Loop to fetch the expression value of that probe across samples
					for(int val=0; val<cols; val++){
						matrixVals[val]=(double)experiment_matrix.get(probe_pos, val);
					}


					current_sd=JSci.maths.ArrayMath.standardDeviation(matrixVals);

					//current_sd MUST be greater than the user specified cutoff and greater than the current 
					//max_sd value
					if(current_sd>max_sd && current_sd>Double.parseDouble(SDcutoff)){
						max_probeSD_pos=probe_pos;
					}


				}//End of inner for loop

				//Once we know which probe has the maximum SD across samples, set unique gene in data set
				//Add the max probe position to the Vector
				if(max_probeSD_pos!=-1){
					genes.add(num_genes_passing_cutoff, Gene);
					//Keep a track of the probes passing SD cutoff. This can be used to populate the Gene Data Matrix(FloatMatrix)
					probes_passing_cutoff.add(num_genes_passing_cutoff,max_probeSD_pos);
					num_genes_passing_cutoff=num_genes_passing_cutoff+1;
				}else
					genes_with_low_variability.add(Gene);
				
				
			}//Else loop ends


		}//Unique genes for loop ends
		
		
		/**
		 * Populate the new GeneData array. This array would ONLY have GENES that PASSED the standard deviation cutoff 
		 * This would be used for all the downstream analysis 
		 * 
		 * 
		 * 
		 * 
		 * 
		 */
		
		gene_data=new GeneData[cols];
		int geneRows=genes.size();
		geneDataMatrix=new FloatMatrix(geneRows, cols);
		
		for(int i=0; i<cols; i++){

			gene_data[i]=new GeneData(geneRows,1);
			String slideName=data.getFeature(i).getSlideDataName();
			String slideFile=data.getFeature(i).getSlideFileName();

			gene_data[i].setSlideName(slideName);

			for(int index=0; index<geneRows;index++){
				String Gene=(String)genes.get(index);
				int gIndex=unique_genes_in_data.indexOf(Gene);

				gde=new GeneDataElement(index, Gene);
				gene_data[i].setGeneDataElement(gde, index);

			}
		}//col for loop ends
			/**
			 * Copies information about the probes mapping to the genes, from the original gene data object
			 * 
			 * 
			 * 
			 * 
			 * 
			 */
			for(int row=0; row<geneRows; row++){
				String Gene=(String)genes.get(row);
				int gIndex=unique_genes_in_data.indexOf(Gene);
				int maxProbePos=((Integer)probes_passing_cutoff.elementAt(row)).intValue();

				Vector probe_id=(Vector)gdata[0].getGeneDataElement(gIndex).getProbeID();
				Vector probe_pos=(Vector)gdata[0].getGeneDataElement(gIndex).getProbePosition();
				
				for(int index=0; index<probe_id.size(); index++){
					String pid=(String)probe_id.get(index);
					int probepos=Integer.parseInt(probe_pos.get(index).toString());
					gene_data[0].getGeneDataElement(row).setProbeID(pid);
					gene_data[0].getGeneDataElement(row).setProbePosition(probepos);
				}
				//Set the true intensity across samples
				for(int col=0; col<data.getFeaturesCount(); col++){
					Vector trueIntensity=gdata[col].getGeneDataElement(gIndex).getTrueIntensity();
					for(int j=0; j<trueIntensity.size(); j++){
						gene_data[col].getGeneDataElement(row).setTrueIntensity(((Float)trueIntensity.elementAt(j)).floatValue());
					}
					//current intensity is equal to the intensity of the probe with max SD
					gene_data[col].getGeneDataElement(row).setCurrentIntensity(experiment_matrix.get(maxProbePos, col));
					geneDataMatrix.set(row, col, experiment_matrix.get(maxProbePos, col));
				}
				
				
				
				
			}//row for loop ends
			
			
			
			
			
			/*for testing only
			System.out.println("Printing genes with low variablility Begins");
			for(int i=0; i<genes_with_low_variability.size(); i++){
				
				System.out.println("Gene:"+(String)genes_with_low_variability.get(i));
			}
			System.out.println("##############################################");*/
			
		//Set the Gene Data Matrix
		setGeneDataMatrix(geneDataMatrix);
			 
		
				
		//Sets the unique genes in the data set. This time ONLY the genes passing the SD cutoff are included.
		setUniqueGenesinDataset(genes);
		return gene_data;
	}

	
	
	
	
	/**
	 * 
	 * @param intensity
	 * @return Maximum of the intensity values
	 */
	
	public float getMaxProbe(Vector intensity){
		float max=((Float)intensity.get(0)).floatValue();

		if(intensity.size()==1)
			return max;
		else{
			for (int i=1; i<intensity.size();i++){
				float current=((Float)intensity.get(i)).floatValue();
				max=Math.max(max, current);

			}
		}
		return max;
	}
	
	
	/**
	 * 
	 * @param intensity
	 * @return median intensity value 
	 */
	
	public float getMedianProbe(Vector intensity){
		
		Vector temp=new Vector();
		
		for(int i=0; i<intensity.size(); i++){
			if(	!((Float)intensity.get(i)).isNaN())
				temp.add((Float)intensity.get(i));
			
		}
		
		float[]vals=new float[temp.size()];
		for(int i=0; i<temp.size(); i++){
			vals[i]=((Float)temp.get(i)).floatValue();
		}
		
		java.util.Arrays.sort(vals);
		
		float median=0;
		int count=vals.length;
		
		if(count%2==0){
			 median = (vals[count/2-1] + vals[count/2]) / 2;
			
		}
		
		if(count%2!=0 && count !=1){
	      median = vals[count/2];
	     }
		
		if(count==1)
			median=vals[0];
	  	
		
		
		return median;
	
	
	}
	
	
	public void setGeneDataMatrix(FloatMatrix gene_data_mat){
		geneDataMatrix=new FloatMatrix(gene_data_mat.getRowDimension(),gene_data_mat.getColumnDimension());
		geneDataMatrix=gene_data_mat;
	}
	
	public FloatMatrix getGeneDataMatrix(){
		return this.geneDataMatrix;
	}
	
	
	
	
	
	/**
	 * setUniqueGenesinDataset sets the unique genes present in the expression data
	 * @param genes
	 */
	public void setUniqueGenesinDataset(Vector genes){
		unique_genes_in_data=new Vector();
		unique_genes_in_data=genes;
		
	}
	
	/**
	 * getUniqueGenesinDataset returns a vector of unique genes
	 * @return
	 */
	
	
	public Vector getUniqueGenesinDataset(){
		return unique_genes_in_data;
	}
	
	
	
	
	/**
	 * createColumns generates an integer array, that contains 
	 * the number of experiments(samples). Needed to populate 
	 * GSEAExperiment 
	 * 
	 * 
	 */
	public int[]createColumns(int count){
		columns=new int[count];
		for(int i=0; i<count; i++){
			columns[i]=i;
		}
		return columns;
	}
	

}
