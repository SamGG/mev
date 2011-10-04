/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/**
 * @author  dschlauch
 * @author  raktim
 * @version
 */
package org.tigr.microarray.mev.cluster.algorithm.impl;


import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;
import org.tigr.rhook.RHook;
import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;
import java.util.Vector;

public class ATTRACT extends AbstractAlgorithm{
	private FloatMatrix expMatrix;
	private boolean stop = false;
	private int[] groupAssignments;
	private int[][] keggGenesSynArrays;
	private int[][] keggGenesCorArrays;

	private int numGenes, numExps, numGroups, iteration;
	private float alpha;
	private boolean  drawSigTreesOnly;
	private int hcl_function;
	private boolean hcl_absolute;
	private boolean hcl_genes_ordered;  
	private boolean hcl_samples_ordered; 


	private AlgorithmEvent event;

	private String[] sampleNames;
	private String[] probeIDs;
	private Object[][] synResultMatrix;
//	private Object[][] corResultMatrix;
	float[] sortedMapping;

	int validN;
	private String chipTypeName;
	private String[] resultColumns;
	private int resultMatrixColumns;
	private String[] nodeTitlesSyn;
	private String[] nodeTitlesCor;

	/**
	 * This method should interrupt the calculation.
	 */
	public void abort() {
		stop = true;        
	}
	/**
	 * This method execute calculation and return result,
	 * stored in <code>AlgorithmData</code> class.
	 *
	 * @param data the data to be calculated.
	 */
	public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
		AlgorithmParameters map = data.getParams();
		expMatrix = data.getMatrix("experiment");
		groupAssignments = data.getIntArray("group_assignments");
		alpha = map.getFloat("alpha"); 
		numGroups = map.getInt("numGroups");
		chipTypeName = map.getString("chipName", "hgu133plus2.db");
		probeIDs = data.getStringArray("probeIDs");		
		sampleNames = data.getStringArray("sampleLabels");

		hcl_function = map.getInt("hcl-distance-function", EUCLIDEAN);
		hcl_absolute = map.getBoolean("hcl-distance-absolute", false);     
		hcl_genes_ordered = map.getBoolean("hcl-genes-ordered", false);  
		hcl_samples_ordered = map.getBoolean("hcl-samples-ordered", false);    
		boolean hierarchical_tree = map.getBoolean("hierarchical-tree", false);
		if (hierarchical_tree) {
			drawSigTreesOnly = map.getBoolean("draw-sig-trees-only");
		}        
		int method_linkage = map.getInt("method-linkage", 0);
		boolean calculate_genes = map.getBoolean("calculate-genes", false);
		boolean calculate_experiments = map.getBoolean("calculate-experiments", false);

		event = null;
		event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Calculating...");
		// set progress limit
		fireValueChanged(event);
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue(0);
		fireValueChanged(event);

		// REngine
		runRAlg();
		// Stop Logging
		//logger.stop();

		if (stop) {
			throw new AbortException();
		}
		numGenes= expMatrix.getRowDimension();
		numExps = expMatrix.getColumnDimension();

		AlgorithmData result = new AlgorithmData();
		Cluster result_cluster = new Cluster();
		NodeList nodeList = result_cluster.getNodeList();
		int[] features;        
		for (int i=0; i<keggGenesSynArrays.length; i++) {
			if (stop) {
				throw new AbortException();
			}
			features = keggGenesSynArrays[i];
			Node node = new Node(features);
			nodeList.addNode(node);
			if (hierarchical_tree) {
				if (drawSigTreesOnly) {
					if (i == 0) {
						node.setValues(calculateHierarchicalTree(features, method_linkage, calculate_genes, calculate_experiments));
						event.setIntValue(i+1);
						fireValueChanged(event);                       
					}                    
				} else {                
					node.setValues(calculateHierarchicalTree(features, method_linkage, calculate_genes, calculate_experiments));
					event.setIntValue(i+1);
					fireValueChanged(event);
				}                
			}
		}

		// prepare the result
		result.addIntMatrix("keggSynArrays", keggGenesSynArrays);
		result.addIntMatrix("keggCorArrays", keggGenesCorArrays);
		result.addParam("iterations", String.valueOf(iteration-1));
		result.addCluster("cluster", result_cluster);
		result.addParam("number-of-clusters", "1"); 
		result.addObjectMatrix("synResultMatrix", synResultMatrix);
//		result.addObjectMatrix("corResultMatrix", corResultMatrix);
		result.addStringArray("resultColumns", resultColumns);
		result.addStringArray("nodeTitlesSyn", nodeTitlesSyn);
		result.addStringArray("nodeTitlesCor", nodeTitlesCor);
		result.addMatrix("clusters_means", getMeans(keggGenesSynArrays));
		result.addMatrix("clusters_variances", getVariances(keggGenesSynArrays, getMeans(keggGenesSynArrays))); 
		result.addMatrix("geneGroupMeansMatrix", getAllGeneGroupMeans());
		result.addMatrix("geneGroupSDsMatrix", getAllGeneGroupSDs());

		return result;   
	}



	private FloatMatrix getSubExperiment(FloatMatrix experiment, int[] features) {
		FloatMatrix subExperiment = new FloatMatrix(features.length, experiment.getColumnDimension());
		for (int i=0; i<features.length; i++) {
			subExperiment.A[i] = experiment.A[features[i]];
		}
		return subExperiment;
	}
	/**
	 * Checking the result of hcl algorithm calculation.
	 * @throws AlgorithmException, if the result is incorrect.
	 */
	private void validate(AlgorithmData result) throws AlgorithmException {
		if (result.getIntArray("child-1-array") == null) {
			throw new AlgorithmException("parameter 'child-1-array' is null");
		}
		if (result.getIntArray("child-2-array") == null) {
			throw new AlgorithmException("parameter 'child-2-array' is null");
		}
		if (result.getIntArray("node-order") == null) {
			throw new AlgorithmException("parameter 'node-order' is null");
		}
		if (result.getMatrix("height") == null) {
			throw new AlgorithmException("parameter 'height' is null");
		}
	}
	private NodeValueList calculateHierarchicalTree(int[] features, int method, boolean genes, boolean experiments) throws AlgorithmException {
		NodeValueList nodeList = new NodeValueList();
		AlgorithmData data = new AlgorithmData();
		FloatMatrix experiment = getSubExperiment(expMatrix, features);
		data.addMatrix("experiment", experiment);
		data.addParam("hcl-distance-function", String.valueOf(this.hcl_function));
		data.addParam("hcl-distance-absolute", String.valueOf(this.hcl_absolute));
		data.addParam("method-linkage", String.valueOf(method));
		HCL hcl = new HCL();
		AlgorithmData result;
		if (genes) {
			data.addParam("calculate-genes", String.valueOf(true));
			data.addParam("optimize-gene-ordering", String.valueOf(hcl_genes_ordered));
			result = hcl.execute(data);
			validate(result);
			addNodeValues(nodeList, result);
		}
		if (experiments) {
			data.addParam("calculate-genes", String.valueOf(false));
			data.addParam("optimize-sample-ordering", String.valueOf(hcl_samples_ordered));
			result = hcl.execute(data);
			validate(result);
			addNodeValues(nodeList, result);
		}
		return nodeList;
	}

	private void addNodeValues(NodeValueList target_list, AlgorithmData source_result) {
		target_list.addNodeValue(new NodeValue("child-1-array", source_result.getIntArray("child-1-array")));
		target_list.addNodeValue(new NodeValue("child-2-array", source_result.getIntArray("child-2-array")));
		target_list.addNodeValue(new NodeValue("node-order", source_result.getIntArray("node-order")));
		target_list.addNodeValue(new NodeValue("height", source_result.getMatrix("height").getRowPackedCopy()));
	}

	private FloatMatrix getMeans(int[][] clusters) {
		FloatMatrix means = new FloatMatrix(clusters.length, numExps);
		FloatMatrix mean;
		for (int i=0; i<clusters.length; i++) {
			mean = getMean(clusters[i]);
			means.A[i] = mean.A[0];
		}
		return means;
	}
	private FloatMatrix getMean(int[] cluster) {
		FloatMatrix mean = new FloatMatrix(1, numExps);
		float currentMean;
		int n = cluster.length;
		int denom = 0;
		float value;
		for (int i=0; i<numExps; i++) {
			currentMean = 0f;
			denom = 0;
			for (int j=0; j<n; j++) {
				value = expMatrix.get(((Integer) cluster[j]).intValue(), i);
				if (!Float.isNaN(value)) {
					currentMean += value;
					denom++;
				}
			}
			mean.set(0, i, currentMean/(float)denom);
		}

		return mean;
	}
	private FloatMatrix getVariances(int[][] clusters, FloatMatrix means) {
		final int rows = means.getRowDimension();
		final int columns = means.getColumnDimension();
		FloatMatrix variances = new FloatMatrix(rows, columns);
		for (int row=0; row<rows; row++) {
			for (int column=0; column<columns; column++) {
				variances.set(row, column, getSampleVariance(clusters[row], column, means.get(row, column)));
			}
		}
		return variances;
	}


	private float getSampleVariance(int[] cluster, int column, float mean) {
		return(float)Math.sqrt(getSampleNormalizedSum(cluster, column, mean)/(float)(validN-1));

	}  

	private float getSampleNormalizedSum(int[] cluster, int column, float mean) {
		final int size = cluster.length;
		float sum = 0f;
		float value;
		validN = 0;
		for (int i=0; i<size; i++) {
			value = expMatrix.get(((Integer) cluster[i]).intValue(), column);
			if (!Float.isNaN(value)) {
				sum += Math.pow(value-mean, 2);
				validN++;
			}
		}
		return sum;
	}

	private float[] getGeneGroupMeans(int gene) {
		float[] geneValues = new float[numExps];
		for (int i = 0; i < numExps; i++) {
			geneValues[i] = expMatrix.A[gene][i];
		} 

		float[][] geneValuesByGroups = new float[numGroups][];

		for (int i = 0; i < numGroups; i++) {
			geneValuesByGroups[i] = getGeneValuesForGroup(geneValues, i+1);
		} 

		float[] geneGroupMeans = new float[numGroups];
		for (int i = 0; i < numGroups; i++) {
			geneGroupMeans[i] = getMean(geneValuesByGroups[i]);
		}
		return geneGroupMeans;
	}

	private float[] getGeneGroupSDs(int gene) {
		float[] geneValues = new float[numExps];        
		for (int i = 0; i < numExps; i++) {
			geneValues[i] = expMatrix.A[gene][i];
		}   

		float[][] geneValuesByGroups = new float[numGroups][];

		for (int i = 0; i < numGroups; i++) {
			geneValuesByGroups[i] = getGeneValuesForGroup(geneValues, i+1);
		}  

		float[] geneGroupSDs = new float[numGroups];
		for (int i = 0; i < numGroups; i++) {
			geneGroupSDs[i] = getStdDev(geneValuesByGroups[i]);
		}

		return geneGroupSDs;        
	}

	private float[] getGeneValuesForGroup(float[] geneValues, int group) {
		Vector<Float> groupValuesVector = new Vector<Float>();

		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] == group) {
				groupValuesVector.add(new Float(geneValues[i]));
			}
		}

		float[] groupGeneValues = new float[groupValuesVector.size()];

		for (int i = 0; i < groupValuesVector.size(); i++) {
			groupGeneValues[i] = ((Float)(groupValuesVector.get(i))).floatValue();
		}

		return groupGeneValues;
	}

	private FloatMatrix getAllGeneGroupMeans() {
		FloatMatrix means = new FloatMatrix(numGenes, numGroups);
		for (int i = 0; i < means.getRowDimension(); i++) {
			means.A[i] = getGeneGroupMeans(i);
		}
		return means;
	}
	private FloatMatrix getAllGeneGroupSDs() {
		FloatMatrix sds = new FloatMatrix(numGenes, numGroups);
		for (int i = 0; i < sds.getRowDimension(); i++) {
			sds.A[i] = getGeneGroupSDs(i);
		}
		return sds;        
	}

	private float getMean(float[] group) {
		float sum = 0;
		int n = 0;

		for (int i = 0; i < group.length; i++) {
			if (!Float.isNaN(group[i])) {
				sum = sum + group[i];
				n++;
			}
		}

		if (n == 0) {
			return Float.NaN;
		}
		float mean =  sum / (float)n;

		if (Float.isInfinite(mean)) {
			return Float.NaN;
		}

		return mean;
	}

	private float getStdDev(float[] group) {
		float mean = getMean(group);
		int n = 0;

		float sumSquares = 0;

		for (int i = 0; i < group.length; i++) {
			if (!Float.isNaN(group[i])) {
				sumSquares = (float)(sumSquares + Math.pow((group[i] - mean), 2));
				n++;
			}
		}

		if (n < 2) {
			return Float.NaN;
		}

		float var = sumSquares / (float)(n - 1);
		if (Float.isInfinite(var)) {
			return Float.NaN;
		}
		return (float)(Math.sqrt((double)var));
	}    
	/**
	 * Function to create R session in memory and execute ATTRACT
	 * @throws AbortException 
	 */
	public void runRAlg() throws AbortException {
		event.setDescription("Creating R Engine...");
		fireValueChanged(event);

		Rengine re;
		try {
			re = RHook.startRSession();
			if(re == null) {
				JOptionPane.showMessageDialog(null, "Error creating R Engine",  "REngine", JOptionPane.ERROR_MESSAGE);
				//logger.writeln("Could not get REngine");
				throw new AbortException();
				//return;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "REngine", JOptionPane.ERROR_MESSAGE);
			//logger.writeln("Could not get REngine");
			throw new AbortException();
			//return;
		}

		event.setDescription("Loading packages...");
		fireValueChanged(event);
		try {
			RHook.testPackage("attract");
			RHook.log("Starting R Algorithim");
			
			String rCmd = "library(attract)";
			RHook.evalR(rCmd);			

			rCmd = "source('http://www.bioconductor.org/biocLite.R')";
			RHook.evalR(rCmd);
			rCmd = "biocLite('"+chipTypeName+"')";
			RHook.evalR(rCmd);
			rCmd = "library("+chipTypeName+")";
			RHook.evalR(rCmd);
					

			event.setDescription("Generating script and running Attract module...");
			fireValueChanged(event);
			
			String fileLoc = System.getProperty("user.dir")+System.getProperty("file.separator")+"tmpfile.txt";
			//if(fileLoc.contains("\\"));
			fileLoc = fileLoc.replace("\\", "/");
			String filePath = writeMatrixToFile(fileLoc, expMatrix, probeIDs);
			//Create data matrix in R from a file
			RHook.createRDataFrameFromFile("y", filePath, true, sampleNames);
			

			rCmd = "sampleAssignments <- c(";
			for (int i=0; i<sampleNames.length; i++){
				rCmd = rCmd + "'group"+this.groupAssignments[i] + "',";				
			}
			rCmd = rCmd.substring(0, rCmd.length()-1)+")";
			RHook.evalR(rCmd);

			rCmd = "chipname <- c(";
			for (int i=0; i<sampleNames.length; i++){				
				rCmd = rCmd + "'"+this.sampleNames[i] + "',";
			}
			rCmd = rCmd.substring(0, rCmd.length()-1)+")";
			RHook.evalR(rCmd);
			
			rCmd = "sample.info = data.frame(chipname = chipname, grps = sampleAssignments)";
			RHook.evalR(rCmd);
			
			
			rCmd = "eset<-new('ExpressionSet')";
			RHook.evalR(rCmd);
			rCmd = "eset@assayData<-new.env()";
			RHook.evalR(rCmd);
			rCmd = "assign('exprs', y, eset@assayData)";
			RHook.evalR(rCmd);
			rCmd = "p.eset<-new('AnnotatedDataFrame', data=sample.info)";
			RHook.evalR(rCmd);
			rCmd = "eset@phenoData<-p.eset";
			RHook.evalR(rCmd);

			rCmd = "attract_out <- findAttractors(eset, colnames(pData(eset))[2], annotation = '"+chipTypeName+"')";
			RHook.evalR(rCmd);
			rCmd = "removeTheseGenes<-removeFlatGenes(eset, colnames(pData(eset))[2], contrasts=NULL, limma.cutoff="+alpha+")";
			RHook.evalR(rCmd);
			rCmd = "keepTheseGenes<-setdiff(featureNames(eset), removeTheseGenes)";
			RHook.evalR(rCmd);
	

			REXP x;
			rCmd = "as.matrix(attract_out@rankedPathways[1])";
			x = RHook.evalR(rCmd);
			String[] keggIDs = x.asStringArray();
			int keggGroupCount = keggIDs.length;			

			rCmd = "as.matrix(attract_out@rankedPathways[2])";
			x = RHook.evalR(rCmd);
			String[] keggNames = x.asStringArray();

			rCmd = "as.matrix(attract_out@rankedPathways[3])";
			x = RHook.evalR(rCmd);
			double[] keggPValues = x.asDoubleArray();

			rCmd = "as.matrix(attract_out@rankedPathways[4])";
			x = RHook.evalR(rCmd);
			double[] keggNumGenes = x.asDoubleArray();
			
			resultMatrixColumns = 6;
			synResultMatrix = new Object[resultMatrixColumns+sampleNames.length][];
			synResultMatrix[0] = keggIDs;
			synResultMatrix[1] = keggNames;
			synResultMatrix[2] = new Object[keggGroupCount];
			synResultMatrix[3] = new Object[keggGroupCount];
			synResultMatrix[4] = new Object[keggGroupCount];
			synResultMatrix[5] = new Object[keggGroupCount];
			for (int i=resultMatrixColumns; i<synResultMatrix.length; i++){
				synResultMatrix[i] = new Object[keggGroupCount];
			}
			for (int i=0; i<keggGroupCount; i++){
				synResultMatrix[2][i] = keggPValues[i];
				synResultMatrix[3][i] = keggNumGenes[i];	
				synResultMatrix[4][i] = 0;		
				synResultMatrix[5][i] = 0;	
				
			}

//			corResultMatrix = new Object[resultMatrixColumns+sampleNames.length][];
//			corResultMatrix[0] = keggIDs;
//			corResultMatrix[1] = keggNames;
//			corResultMatrix[2] = new Object[keggGroupCount];
//			corResultMatrix[3] = new Object[keggGroupCount];
//			corResultMatrix[4] = new Object[keggGroupCount];
//			corResultMatrix[5] = new Object[keggGroupCount];
//			for (int i=4; i<corResultMatrix.length; i++){
//				corResultMatrix[i] = new Object[keggGroupCount];
//			}
//			for (int i=0; i<keggGroupCount; i++){
//				corResultMatrix[2][i] = keggPValues[i];
//				corResultMatrix[3][i] = keggNumGenes[i];				             
//			}
			resultColumns = new String[resultMatrixColumns+sampleNames.length];
			resultColumns[0] = "KEGG ID";
			resultColumns[1] = "KEGG Name";
			resultColumns[2] = "Adjusted p-value";
			resultColumns[3] = "Number of detected genes";
			resultColumns[4] = "Number of Synexpression groups";
			resultColumns[5] = "Number of Correlated Partners groups";
			for (int i=0; i<sampleNames.length; i++){
				resultColumns[i+resultMatrixColumns] = sampleNames[i];
			}
						

			fireValueChanged(event);
			rCmd = "zz <- file('all.Rout', open='wt')";
			RHook.evalR(rCmd);
			rCmd = "sink(zz)";
			RHook.evalR(rCmd);
			rCmd = "sink(zz, type='message')";
			RHook.evalR(rCmd);

			String[]probeIDclone = probeIDs.clone();
			sortedMapping = new float[probeIDclone.length];
			for (int i=0; i<sortedMapping.length; i++){
				sortedMapping[i]=i;
			}
			ExperimentUtil.sort3(probeIDclone, sortedMapping);

			ArrayList<int[]> keggGenesSynArraysList = new ArrayList<int[]>();
			ArrayList<String> nodeTitlesSynList = new ArrayList<String>();
			ArrayList<int[]> keggGenesCorArraysList = new ArrayList<int[]>();
			ArrayList<String> nodeTitlesCorList = new ArrayList<String>();
			
			for (int keggIndex=0; keggIndex<keggGroupCount; keggIndex++){
				int percent = (int) (((float)keggIndex*100f)/((float)keggGroupCount));
				event.setDescription("Gathering results... ("+percent+"%)");
				fireValueChanged(event);
				
				x = null;
				rCmd = "synExpression"+keggIndex+"<-findSynexprs('"+keggIDs[keggIndex]+"', attract_out, removeTheseGenes)";
				RHook.evalR(rCmd);
				
				rCmd = "length(synExpression"+keggIndex+"@groups)";
				x = RHook.evalR(rCmd);
				int lengthSyn; 
				if (x==null)
					lengthSyn = 0;
				else
					lengthSyn = x.asInt();

				String[][] passSynGenes = null;
				String[][] passCorGenes = null;
				if (lengthSyn!=0){
					rCmd = "corPartners"+keggIndex+"<-findCorrPartners(synExpression"+keggIndex+", eset, removeTheseGenes)";
					RHook.evalR(rCmd);
					rCmd = "length(corPartners"+keggIndex+"@groups)";
					x = RHook.evalR(rCmd);
					int lengthCor; 
					if (x==null)
						lengthCor = 0;
					else
						lengthCor = x.asInt();
					synResultMatrix[4][keggIndex] = lengthSyn;
					synResultMatrix[5][keggIndex] = lengthCor;
					passSynGenes = new String[lengthSyn][];
					passCorGenes = new String[lengthCor][];
					for (int groupIndex = 0; groupIndex<lengthSyn; groupIndex++){
						rCmd = "asMatrixSyn"+keggIndex+"<-t(as.matrix(unlist(synExpression"+keggIndex+"@groups[["+(groupIndex+1)+"]]), nrow=length((synExpression"+keggIndex+"@groups[[1]])), ncol=length(unlist(synExpression"+keggIndex+"@groups[[1]])), byrow=true))";
						RHook.evalR(rCmd);
						rCmd = "asVectorSyn"+keggIndex+"<-as.vector(asMatrixSyn"+keggIndex+")";
						RHook.evalR(rCmd);		
						rCmd = "asVectorSyn"+keggIndex;
						x = RHook.evalR(rCmd);
						try{
							passSynGenes[groupIndex] = x.asStringArray();
						} catch (Exception e){
							passSynGenes[groupIndex] = new String[0];
							System.out.println("error in pathway "+(keggIndex+1));
						}
					}
					for (int groupIndex = 0; groupIndex<lengthCor; groupIndex++){
						rCmd = "asMatrixCor"+keggIndex+"<-t(as.matrix(unlist(corPartners"+keggIndex+"@groups[["+(groupIndex+1)+"]]), nrow=length((corPartners"+keggIndex+"@groups[[1]])), ncol=length(unlist(corPartners"+keggIndex+"@groups[[1]])), byrow=true))";
						RHook.evalR(rCmd);
						rCmd = "asVectorCor"+keggIndex+"<-as.vector(asMatrixCor"+keggIndex+")";
						RHook.evalR(rCmd);	
						rCmd = "asVectorCor"+keggIndex;
						x = RHook.evalR(rCmd);
						try{
							passCorGenes[groupIndex] = x.asStringArray();
						} catch (Exception e){
							passCorGenes[groupIndex] = new String[0];
							System.out.println("error in pathway "+(keggIndex+1));
						}
					}
					
					rCmd = "synExpression"+keggIndex+"@profiles";
					x = RHook.evalR(rCmd);				
					try{
						double[][] synMatrix;
						if (x!=null&&x.asMatrix()!=null){
							synMatrix = x.asMatrix();
							for (int i=0; i<synMatrix[0].length; i++)
								synResultMatrix[i+resultMatrixColumns][keggIndex]=synMatrix[0][i];
						}
						rCmd = "corPartners"+keggIndex+"@profiles";
						x = RHook.evalR(rCmd);
//						double[][] corMatrix;
//						if (x!=null&&x.asMatrix()!=null){
//							corMatrix = x.asMatrix();
//							for (int i=0; i<corMatrix[0].length; i++)
//								corResultMatrix[i+resultMatrixColumns][keggIndex]=corMatrix[0][i];					
//						}				
					} catch (Exception e){
						System.out.println("Error in kegg pathway #"+keggIndex);
					}
				} 
				
				if (passSynGenes==null){ //none or not enough genes
					passSynGenes = new String[0][0];
				}
				

				if (passCorGenes==null){ //none or not enough genes
					passCorGenes = new String[0][0];
				}


				for (int groupIndex=0; groupIndex<passSynGenes.length; groupIndex++){
					int[] genesInGroup = new int[passSynGenes[groupIndex].length]; //member genes
					for (int i=0; i<genesInGroup.length; i++){
						try{
							genesInGroup[i] = (int)sortedMapping[Arrays.binarySearch(probeIDclone, passSynGenes[groupIndex][i])];
						} catch (Exception e){
							System.out.println("gene not found in Probes: "+passSynGenes[groupIndex][i]+", "+i);
							e.printStackTrace();
						}
					}
					keggGenesSynArraysList.add(genesInGroup);
					nodeTitlesSynList.add(((String) synResultMatrix[1][keggIndex] + ", (Group "+(groupIndex+1)+")"));
				}	
								
				
				for (int groupIndex=0; groupIndex<passCorGenes.length; groupIndex++){
					int[] genesInGroup = new int[passCorGenes[groupIndex].length]; //member genes
					for (int i=0; i<genesInGroup.length; i++){
						try{
							genesInGroup[i] = (int)sortedMapping[Arrays.binarySearch(probeIDclone, passCorGenes[groupIndex][i])];
						} catch (Exception e){
							System.out.println("gene not found in Probes: "+passCorGenes[groupIndex][i]+", "+i);
							e.printStackTrace();
						}
					}
					keggGenesCorArraysList.add(genesInGroup);
					nodeTitlesCorList.add(((String) synResultMatrix[1][keggIndex] + ", (Group "+(groupIndex+1)+")"));
				}	
			}		

			keggGenesSynArrays = new int[keggGenesSynArraysList.size()][];
			nodeTitlesSyn = new String[keggGenesSynArrays.length];
			for (int i=0; i<keggGenesSynArrays.length; i++){
				keggGenesSynArrays[i] = new int[keggGenesSynArraysList.get(i).length];
				for (int j=0; j<keggGenesSynArrays[i].length; j++){
					keggGenesSynArrays[i][j] = keggGenesSynArraysList.get(i)[j];
				}
				nodeTitlesSyn[i] = nodeTitlesSynList.get(i);				
			}

			keggGenesCorArrays = new int[keggGenesCorArraysList.size()][];
			nodeTitlesCor = new String[keggGenesCorArrays.length];
			for (int i=0; i<keggGenesCorArrays.length; i++){
				keggGenesCorArrays[i] = new int[keggGenesCorArraysList.get(i).length];
				for (int j=0; j<keggGenesCorArrays[i].length; j++){
					keggGenesCorArrays[i][j] = keggGenesCorArraysList.get(i)[j];
				}
				nodeTitlesCor[i] = nodeTitlesCorList.get(i);
			}
			
			event.setDescription("Generating viewers...");
			fireValueChanged(event);
			rCmd = "sink()";
			RHook.evalR(rCmd);
			
			RHook.endRSession();
//			removeTmps(filePath);
		} catch (Exception e) {
			RHook.log(e);
			try {
				RHook.endRSession();
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(), "REngine", JOptionPane.ERROR_MESSAGE);
				//throw new AlgorithmException(e);
				throw new AbortException();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private String writeMatrixToFile(String fileLoc, FloatMatrix fm, String[] rowNames) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileLoc));

			int row = fm.getRowDimension();
			int col = fm.getColumnDimension();
			String srtVector = "";

			for(int iRow = 0; iRow < row; iRow++) {
				srtVector = rowNames[iRow] + "\t";
				for(int jCol = 0; jCol < col; jCol++) {
					if(jCol == col-1)
						srtVector += fm.get(iRow, jCol) + "\n";
					else 
						srtVector += fm.get(iRow, jCol) + "\t";
				}
				out.write(srtVector);
				srtVector = "";
			}
			out.close();
		} catch(IOException e) {
			return null;
		}
		return fileLoc;
	}


	public void updateProgressBar(){

	}

	private void removeTmps(String fileName) {
		File f = new File(fileName);
		f.delete();
	}

}
