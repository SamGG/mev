package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;

import java.util.ArrayList;
import java.util.Vector;

import org.tigr.microarray.mev.ISlideDataElement;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.gui.GSEAExperiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.GSEAConstants;
import org.tigr.util.FloatMatrix;

/**
 * @author Sarita Nair
 * ProbetoGene, collapses multiple probes mapping to one gene.
 * This is done as per the criteria provided by the user. The available options for this being
 * MAX_PROBE: 
 * 			Sample1		Sample2		Sample3
 * Probe_1   10			20			30
 * Probe_2	 20			5			10 	
 * Gene_12   20			20			30
 * 
 * 
 * MEDIAN_PROBE: 
 * 			Sample1		Sample2		Sample3
 * Probe_1   10			20			30
 * Probe_2	 20			15			10
 * Probe_3	 20			5			10
 * Gene_123  20			15			10
 * 
 * STANDARD_DEVIATION: 
 * 			Sample1		Sample2		Sample3		SD(Calculated by MeV on the fly)  
 * Probe_1   10			20			30			3
 * Probe_2	 20			5			10			4
 * Probe_3	 20			15			10			2
 * Gene_123 will be represented by Probe_2, which has the MAX SD value across samples. So,
 * Gene_123   20	5	10
 * 
 * 
 * 
 * 
 */

public class ProbetoGene {
	private AlgorithmData aData;
	private IData data;
	private Vector<String> unique_genes_in_data = new Vector<String>();
	// GeneDataMatrix is a FloatMatrix containing the Gene expression values.
	// This would be used for downstream analysis (lmPerGene etc).
	private FloatMatrix geneDataMatrix;
	private GSEAExperiment gseaExperiment;
	private int[] columns;
	private FloatMatrix experiment_matrix;
	private GeneDataElement gde;
	private FloatGeneDataElement fgde;
	private IGeneData[] gene_data;

	
	
	public ProbetoGene(AlgorithmData algData, IData idata) {
		this.aData = algData;
		this.data = idata;
		experiment_matrix = aData.getMatrix("matrix");

	}

	/**
	 * 
	 */
	public IGeneData[] convertProbeToGene(String geneIdentifier,
			String conversionCriteria, String SDcutoff) {
		
		int count = 0;
		// Number of rows in the loaded expression data
		int arrayRows = data.getFeaturesSize();

		// Number of samples in the expression dataset
		int cols = data.getFeaturesCount();

		IGeneData[] gdata = new IGeneData[cols];

		/*
		 * Initialize the gene data and GeneDataElement. Each geneData
		 * should have this information and hence the loop
		 * 
		 */

		for (int index = 0; index < arrayRows; index++) {
			ISlideDataElement isde = data.getFeature(0).getSlideDataElement(
					index);

			MevAnnotation ann = (MevAnnotation) isde.getElementAnnotation();
			String[] temp = (ann.getAttribute(geneIdentifier));
			String currentID = temp[0];
			currentID = currentID.toUpperCase();
			//System.out.println("current id:"+currentID);
			if (!unique_genes_in_data.contains(currentID)
					&& !currentID.equalsIgnoreCase("NA")) {
				unique_genes_in_data.add(count, currentID);
				count = count + 1;
			}

		}

		setUniqueGenesinDataset(unique_genes_in_data);

		// Number of genes (unique) present in the expression data set
		int geneRows = unique_genes_in_data.size();

		geneDataMatrix = new FloatMatrix(geneRows, cols);
	
		
		for (int i = 0; i < cols; i++) {
			
			if(i==0){
				//The first element is always GeneData, rest are FloatGeneData
				gdata[0] = new GeneData();
			}
			else
				gdata[i] = new FloatGeneData();
			
			
			String slideName = data.getFeature(i).getSlideDataName();
			gdata[i].setSlideName(slideName);

			for (int index = 0; index < unique_genes_in_data.size(); index++) {
				String requiredID = (String) unique_genes_in_data.get(index);
				
				//If at the first slide, create GeneDataElement, else create FloatGeneDataElement
				if(i==0){
					gde = new GeneDataElement(index, requiredID);
					gdata[i].setGeneDataElement(gde, index);

				}else{
					fgde = new FloatGeneDataElement(index, requiredID);
					gdata[i].setGeneDataElement(fgde, index);

				}
				
				
				
			}

		}

		/**
		 * Loop to figure out which probes match to which gene. The name and
		 * position of the probes in the expression data is stored. This is done
		 * ONLY for the first GeneData. This information will be used to capture
		 * the expression values corresponding to the probes from the matrix.
		 * This expression matrix will be obtained from the Algorithm Data
		 * class.
		 * 
		 * 
		 * 
		 * 
		 */

		for (int index = 0; index < arrayRows; index++) {

			ISlideDataElement isde = data.getFeature(0).getSlideDataElement(
					index);
			MevAnnotation ann = (MevAnnotation) isde.getElementAnnotation();
			boolean found = false;

			String currentID = (ann.getAttribute(geneIdentifier))[0];
			String probeID = ann.getCloneID();

			count = 0;

			while (count < geneRows && found == false) {
				String requiredID = (String) unique_genes_in_data.get(count);
				
				if (currentID.equalsIgnoreCase(requiredID)) {
					gde = (GeneDataElement) gdata[0].getGeneDataElement(count);

					if (!gde.getProbeID().contains(probeID)) {
						
						gde.setProbeID(probeID);
						gde.setProbePosition(index);
					}
					// Get out of while loop as soon as a match is found and
					// advance to the next probe.
					// count=count+1;---Think about this. What if one probe
					// matches >1gene
					// break;
					found = true;
					if(gde.getProbeID().size()>((GeneData)gdata[0]).get_max_num_probes_mapping_to_gene()){
						((GeneData)gdata[0]).set_max_num_probes_mapping_to_gene(gde.getProbeID().size());
					}

				}
				// If probeID does not match current geneData entry, remain in
				// the while loop and advance to the next probe
				else {
					count = count + 1;
				}

			}// End of while loop

		}// End of for loop

		
			
		/**
		 * Use the float matrix contained in the AlgorithmData, to populate the
		 * expression values in GeneDataElement. The original values obtained
		 * from the floatMatrix will be stored in the variable
		 * "trueIntensities". The variable "current Intensities" would contain
		 * expression vales obtained after applying (MAX_PROBE, MEDIAN_PROBE or
		 * SD)
		 * 
		 */

		// If conversion criteria is Standard Deviation, choose the probe that
		// has the higher standard deviation across samples,
		// if multiple probes map to a gene
		if (conversionCriteria.equalsIgnoreCase(GSEAConstants.SD)) {

			gene_data = remove_lowVar_probes(gdata, SDcutoff);

		}// If SD loop ends

		else {
			for (int j = 0; j < geneRows; j++) {
				count = 0;
				int num_probes = (gdata[0].getGeneDataElement(j).getProbeID()
						.size());
				ArrayList probePos = (gdata[0].getGeneDataElement(j)
						.getProbePosition());

				while (count < cols) {
					// If only one probe maps to a gene

					if (num_probes == 1) {
						int probe_pos = Integer.parseInt(probePos.get(0)
								.toString());
						// System.out.println("matrix
						// value:"+matrix.get(probe_pos,count));

						gdata[count].getGeneDataElement(j).setCurrentIntensity(
								experiment_matrix.get(probe_pos, count));
						gdata[count].getGeneDataElement(j).setTrueIntensity(
								experiment_matrix.get(probe_pos, count));
						// Added Tuesday June 3, 08
						geneDataMatrix.set(j, count, experiment_matrix.get(
								probe_pos, count));
					} else {

						for (int k = 0; k < num_probes; k++) {
							int probe_pos = Integer.parseInt(probePos.get(k)
									.toString());
							gdata[count].getGeneDataElement(j)
									.setTrueIntensity(
											experiment_matrix.get(probe_pos,
													count));

						}

						ArrayList intensity = gdata[count].getGeneDataElement(j)
								.getTrueIntensity();
						// System.out.println("gene:"+gdata[0].getGeneDataElement(j).getGeneIdentifier());
						// System.out.println("Number of probes corr to the
						// gene:"+num_probes);
						if (conversionCriteria
								.equalsIgnoreCase(GSEAConstants.MAX_PROBE)) {
							float max_probe = getMaxProbe(intensity);
							gdata[count].getGeneDataElement(j)
									.setCurrentIntensity(max_probe);
							geneDataMatrix.set(j, count, max_probe);
							intensity = new ArrayList();
						}
						if (conversionCriteria
								.equalsIgnoreCase(GSEAConstants.MEDIAN_PROBE)) {
							float med_probe = getMedianProbe(intensity);
							gdata[count].getGeneDataElement(j)
									.setCurrentIntensity(med_probe);
							geneDataMatrix.set(j, count, med_probe);
							intensity = new ArrayList();
						}

					}

					count = count + 1;

				}// End of while loop

			}// End of for loop

			gene_data = gdata;
		}// End of ELSE

	
		gseaExperiment = new GSEAExperiment(geneDataMatrix, createColumns(data
				.getFeaturesCount()));
		this.aData.addGeneMatrix("gene-data-matrix", geneDataMatrix);
		this.aData.addVector("Unique-Genes-in-Expressionset",
				unique_genes_in_data);

		return gene_data;
	}// End of the function convertProbetoGene

	// Need to return this. The reason being, it would be useful to get the
	// information of what probes map
	// to what gene etc, while generating expressionviewer. SpotInformationBox
	// can contain this info.

	public GSEAExperiment returnGSEAExperiment() {
		return this.gseaExperiment;
	}

	/**
	 * remove_lowVar_probes does the following 
	 * 1. Goes through the Genes in the original GeneData, checks if it has one or multiple probes associated
	 * with it. 
	 * 2. Checks the variability of the probes across samples, by calculating SD. Checks if the SD is greater than the user specified
	 * cutoff 
	 * 3. If there are multiple probes associated with one genes, ONLY the probe with the maximum SD and exceeding the user specified cutoff is
	 * retained. 
	 * 4. If all the probes of a genes fail to make the cutoff, the gene is not included for further analysis
	 * 
	 *  
	 * @return
	 */

	public IGeneData[] remove_lowVar_probes(IGeneData[] gdata, String SDcutoff) {

		IGeneData[] gene_data;
		// List of genes (subset of the unique ones in the expression data) which
		// pass the SD cutoff
		Vector genes = new Vector();
		// List of probes which pass the cutoff AND also have the highest SD
		Vector probes_passing_cutoff = new Vector();
		// Tracks the num of genes passing the cutoff
		int num_genes_passing_cutoff = 0;

		// List of genes excluded from the analysis for failing to pass the SD
		// cutoff.
		Vector genes_with_low_variability = new Vector();

		// Number of samples in the experiment
		int cols = data.getFeaturesCount();

		FloatMatrix geneDataMatrix;

		// for testing
		Vector probe = new Vector();

		/**
		 * First step in removing the probes, is to go through the unique genes
		 * in the expression data. Check if the probes mapping to the genes pass
		 * the SD cut off. if so, add the gene to the Vector genes. Also add the
		 * position of the probe which has the Maximum SD to the vector
		 * probes_passing_cutoff. This can be used to populate the float matrix
		 * gene data matrix.
		 * 
		 * 
		 * 
		 */

		for (int index = 0; index < unique_genes_in_data.size(); index++) {
			// Captures the probe position with the maximum SD across samples
			int max_probeSD_pos = -1;
			// Max Value of SD for a gene
			double max_sd = 0;
			// SD of a probe across samples
			double current_sd = 0;
			// array containing the expression values of a probe across samples
			double[] matrixVals = new double[cols];

			// Extract the number of probes associated with the gene.
			int num_probes = (gdata[0].getGeneDataElement(index).getProbeID()
					.size());
			// Extract the exact position (corresponding to the expression data)
			// of the probes mapping to the current gene
			ArrayList probePos = (gdata[0].getGeneDataElement(index)
					.getProbePosition());
			// Extract the gene corresponding to this entry
			String Gene = gdata[0].getGeneDataElement(index)
					.getGeneIdentifier();
			// Local variable to keep a track of probe positions
			int probe_pos = 0;

			// If there is just one probe mapping to a gene, extract the probe
			// position.

			if (num_probes == 1) {
				probe_pos = Integer.parseInt(probePos.get(0).toString());

				// Calculate the SD of the probe across samples and see if it
				// exceeds the user cutoff
				for (int col = 0; col < cols; col++) {
					matrixVals = new double[cols];
					matrixVals[col] = (double) experiment_matrix.get(probe_pos,
							col);
				}
				current_sd = JSci.maths.ArrayMath.standardDeviation(matrixVals);
				if (current_sd >=Double.parseDouble(SDcutoff)) {

					// Keep a track of the probes passing SD cutoff. This can be
					// used to populate the Gene Data Matrix(FloatMatrix)
					probes_passing_cutoff.add(num_genes_passing_cutoff,
							probe_pos);

					// Set it as a unique gene
					genes.add(num_genes_passing_cutoff, Gene);
					num_genes_passing_cutoff = num_genes_passing_cutoff + 1;
				} else {
					genes_with_low_variability.add(Gene);
				}

			} else { //If there are more than one probes mapping to a gene 
				matrixVals = new double[cols];
				// Loops through the probes mapping to a gene
				for (int k = 0; k < num_probes; k++) {
					// Gets the position of that probe
					probe_pos = Integer.parseInt(probePos.get(k).toString());
					// Loop to fetch the expression value of that probe across
					// samples
					for (int val = 0; val < cols; val++) {
						matrixVals[val] = (double) experiment_matrix.get(
								probe_pos, val);
					}

					current_sd = JSci.maths.ArrayMath
							.standardDeviation(matrixVals);

					// current_sd MUST be greater than the user specified cutoff
					// and greater than the current
					// max_sd value
					if (current_sd > max_sd
							&& current_sd >= Double.parseDouble(SDcutoff)) {
						max_probeSD_pos = probe_pos;
					}

				}// End of inner for loop

				// Once we know which probe has the maximum SD across samples,
				// set unique gene in data set
				// Add the max probe position to the Vector
				if (max_probeSD_pos != -1) {
					genes.add(num_genes_passing_cutoff, Gene);
					// Keep a track of the probes passing SD cutoff. This can be
					// used to populate the Gene Data Matrix(FloatMatrix)
					probes_passing_cutoff.add(num_genes_passing_cutoff,
							max_probeSD_pos);
					num_genes_passing_cutoff = num_genes_passing_cutoff + 1;
				} else
					genes_with_low_variability.add(Gene);

			}// Else loop ends

		}// Unique genes for loop ends

		/**
		 * Populate the new GeneData array. This array would ONLY have GENES
		 * that PASSED the standard deviation cutoff. These would be used for all
		 * the downstream analysis
		 * 
		 * 
		 * 
		 * 
		 * 
		 */

		gene_data = new GeneData[cols];
		int geneRows = genes.size();
		geneDataMatrix = new FloatMatrix(geneRows, cols);

		for (int i = 0; i < cols; i++) {
			
			if(i==0){
			gene_data[i] = new GeneData();
			}else{
				gene_data[i]=new FloatGeneData();
				
			}
			String slideName = data.getFeature(i).getSlideDataName();
			String slideFile = data.getFeature(i).getSlideFileName();

			gene_data[i].setSlideName(slideName);

			for (int index = 0; index < geneRows; index++) {
				String Gene = (String) genes.get(index);
				int gIndex = unique_genes_in_data.indexOf(Gene);
				
				if(i==0){
					gde = new GeneDataElement(index, Gene);
					gene_data[i].setGeneDataElement(gde, index);
				}else{
					fgde = new FloatGeneDataElement(index, Gene);	
					gene_data[i].setGeneDataElement(fgde, index);
				}
				
				
			

			}
		}// col for loop ends
		/**
		 * Copies information about the probes mapping to the genes, from the
		 * original gene data object
		 * 
		 * 
		 * 
		 * 
		 * 
		 */
		for (int row = 0; row < geneRows; row++) {
			String Gene = (String) genes.get(row);
			int gIndex = unique_genes_in_data.indexOf(Gene);
			int maxProbePos = ((Integer) probes_passing_cutoff.elementAt(row))
					.intValue();

			ArrayList probe_id = (ArrayList) gdata[0].getGeneDataElement(gIndex)
					.getProbeID();
			ArrayList probe_pos = (ArrayList) gdata[0].getGeneDataElement(gIndex)
					.getProbePosition();

			for (int index = 0; index < probe_id.size(); index++) {
				String pid = (String) probe_id.get(index);
				int probepos = Integer
						.parseInt(probe_pos.get(index).toString());
				gene_data[0].getGeneDataElement(row).setProbeID(pid);
				gene_data[0].getGeneDataElement(row).setProbePosition(probepos);
			}
			// Set the true intensity across samples
			for (int col = 0; col < data.getFeaturesCount(); col++) {
				ArrayList trueIntensity = gdata[col].getGeneDataElement(gIndex)
						.getTrueIntensity();
				for (int j = 0; j < trueIntensity.size(); j++) {
					gene_data[col].getGeneDataElement(row).setTrueIntensity(
							((Float) trueIntensity.get(j)).floatValue());
				}
				// current intensity is equal to the intensity of the probe with
				// max SD
				gene_data[col].getGeneDataElement(row).setCurrentIntensity(
						experiment_matrix.get(maxProbePos, col));
				geneDataMatrix.set(row, col, experiment_matrix.get(maxProbePos,
						col));
			}

		}// row for loop ends

		// Set the Gene Data Matrix
		setGeneDataMatrix(geneDataMatrix);

		// Sets the unique genes in the data set. This time ONLY the genes
		// passing the SD cutoff are included.
		setUniqueGenesinDataset(genes);
		return gene_data;
	}

	/**
	 * 
	 * @param intensity
	 * @return Maximum of the intensity values
	 */

	public float getMaxProbe(ArrayList intensity) {
		float max = ((Float) intensity.get(0)).floatValue();

		if (intensity.size() == 1)
			return max;
		else {
			for (int i = 1; i < intensity.size(); i++) {
				float current = ((Float) intensity.get(i)).floatValue();
				max = Math.max(max, current);

			}
		}
		return max;
	}

	/**
	 * 
	 * @param intensity
	 * @return median intensity value
	 */

	public float getMedianProbe(ArrayList intensity) {

		ArrayList temp = new ArrayList();

		for (int i = 0; i < intensity.size(); i++) {
			if (!((Float) intensity.get(i)).isNaN())
				temp.add((Float) intensity.get(i));

		}

		float[] vals = new float[temp.size()];
		for (int i = 0; i < temp.size(); i++) {
			vals[i] = ((Float) temp.get(i)).floatValue();
		}

		java.util.Arrays.sort(vals);

		float median = 0;
		int count = vals.length;

		if (count % 2 == 0) {
			median = (vals[count / 2 - 1] + vals[count / 2]) / 2;

		}

		if (count % 2 != 0 && count != 1) {
			median = vals[count / 2];
		}

		if (count == 1)
			median = vals[0];

		return median;

	}

	public void setGeneDataMatrix(FloatMatrix gene_data_mat) {
		geneDataMatrix = new FloatMatrix(gene_data_mat.getRowDimension(),
				gene_data_mat.getColumnDimension());
		geneDataMatrix = gene_data_mat;
	}

	public FloatMatrix getGeneDataMatrix() {
		return this.geneDataMatrix;
	}

	/**
	 * setUniqueGenesinDataset sets the unique genes present in the expression
	 * data
	 * 
	 * @param genes
	 */
	public void setUniqueGenesinDataset(Vector genes) {
		unique_genes_in_data = new Vector();
		unique_genes_in_data = genes;

	}

	/**
	 * getUniqueGenesinDataset returns a vector of unique genes
	 * @return
	 */

	public Vector getUniqueGenesinDataset() {
		return unique_genes_in_data;
	}

	/**
	 * createColumns generates an integer array, that contains 
	 * the number of experiments(samples). Needed to populate 
	 * GSEAExperiment 
	 * 
	 * 
	 */
	public int[] createColumns(int count) {
		columns = new int[count];
		for (int i = 0; i < count; i++) {
			columns[i] = i;
		}
		return columns;
	}

}
