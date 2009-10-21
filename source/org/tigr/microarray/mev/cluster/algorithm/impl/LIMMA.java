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
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import java.util.Vector;

public class LIMMA extends AbstractAlgorithm{
	private int progress;
	private FloatMatrix expMatrix;
	private boolean stop = false;
	private int[] groupAssignments;
	private int[][] sigGenesArrays;
	private int[] mapping, mapping2;
	private String nameA, nameB;

	private int numGenes, numExps, numGroups, iteration, numAGroups, numBGroups;
	private float alpha;
	private boolean  drawSigTreesOnly;
	private int hcl_function;
	private boolean hcl_absolute;
	private boolean hcl_genes_ordered;  
	private boolean hcl_samples_ordered; 

	private int dataDesign;

	private AlgorithmEvent event;

	//Raktim
	private String[] geneNames;
	private String[] sampleNames;
	private float[][] lfc;
	private float[][] t;
	private float[][] logOdds;
	private float[][] pValues;
	private float[][] adjPvalues;
	private float[] fValues;
	//private LimmaLogger logger;
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
		dataDesign = map.getInt("dataDesign");
		alpha = map.getFloat("alpha"); 
		numGroups = map.getInt("numGroups");
		nameA = map.getString("nameA");
		nameB = map.getString("nameB");
		numAGroups = map.getInt("numAGroups");
		numBGroups = map.getInt("numBGroups");
		//logFileName = map.getString("logfile");

		//Raktim
		geneNames = data.getStringArray("geneLabels");
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

		mapping = new int[expMatrix.getRowDimension()];
		for (int i=0; i<mapping.length; i++){
			mapping[i]=i;
		}
		mapping2 = new int[expMatrix.getRowDimension()];
		for (int i=0; i<mapping2.length; i++){
			mapping2[i]=i;
		}

		progress=0;
		event = null;
		event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Initializing...");
		// set progress limit
		fireValueChanged(event);
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue(0);
		fireValueChanged(event);

		// Start Logging
		//logger = new LimmaLogger(logFileName);
		//logger.start();
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
//		FloatMatrix means = getMeans(sigGenesArrays);       
//		FloatMatrix variances = getVariances(sigGenesArrays, means); 
		Cluster result_cluster = new Cluster();
		NodeList nodeList = result_cluster.getNodeList();
		int[] features;        
		for (int i=0; i<sigGenesArrays.length; i++) {
			if (stop) {
				throw new AbortException();
			}
			features = sigGenesArrays[i];
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
		//remap genes to expmatrix
		int[][]sigReturn = new int[sigGenesArrays.length][];
		//System.out.println("sigGenesArrays.length "+sigGenesArrays.length);
		for (int i=0; i<sigGenesArrays.length; i++){
			//System.out.println("sga "+i);
			sigReturn[i]=new int[sigGenesArrays[i].length];
			for (int j=0; j<sigGenesArrays[i].length; j++){
				sigReturn[i][j]=mapping2[mapping[sigGenesArrays[i][j]]];
			}
		}

		// prepare the result
		result.addIntMatrix("sigGenesArrays", sigReturn);
		result.addParam("iterations", String.valueOf(iteration-1));
		result.addCluster("cluster", result_cluster);
		result.addParam("number-of-clusters", "1"); 
//		result.addMatrix("clusters_means", means);
//		result.addMatrix("clusters_variances", variances); 
		result.addMatrix("geneGroupMeansMatrix", getAllGeneGroupMeans());
		result.addMatrix("geneGroupSDsMatrix", getAllGeneGroupSDs());

		result.addMatrix("pValues", getPValues());
		result.addMatrix("adjPValues", getAdjPValues());
		result.addMatrix("lfc", getLogFoldChanges());
		result.addMatrix("logOdds", getLogOdds());
		result.addMatrix("tStat", getTStatistic());
		result.addMatrix("fValues", getFValues());
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

//	private FloatMatrix getMeans(int[][] clusters) {
//		FloatMatrix means = new FloatMatrix(clusters.length, numExps);
//		FloatMatrix mean;
//		for (int i=0; i<clusters.length; i++) {
//			mean = getMean(clusters[i]);
//			means.A[i] = mean.A[0];
//		}
//		return means;
//	}
//	private FloatMatrix getMean(int[] cluster) {
//		FloatMatrix mean = new FloatMatrix(1, numExps);
//		float currentMean;
//		int n = cluster.length;
//		int denom = 0;
//		float value;
//		for (int i=0; i<numExps; i++) {
//			currentMean = 0f;
//			denom = 0;
//			for (int j=0; j<n; j++) {
//				value = expMatrix.get(((Integer) cluster[j]).intValue(), i);
//				if (!Float.isNaN(value)) {
//					currentMean += value;
//					denom++;
//				}
//			}
//			mean.set(0, i, currentMean/(float)denom);
//		}
//
//		return mean;
//	}
//	private FloatMatrix getVariances(int[][] clusters, FloatMatrix means) {
//		final int rows = means.getRowDimension();
//		final int columns = means.getColumnDimension();
//		FloatMatrix variances = new FloatMatrix(rows, columns);
//		for (int row=0; row<rows; row++) {
//			for (int column=0; column<columns; column++) {
//				variances.set(row, column, getSampleVariance(clusters[row], column, means.get(row, column)));
//			}
//		}
//		return variances;
//	}

	int validN;

//	private float getSampleVariance(int[] cluster, int column, float mean) {
//		return(float)Math.sqrt(getSampleNormalizedSum(cluster, column, mean)/(float)(validN-1));
//
//	}  
//
//	private float getSampleNormalizedSum(int[] cluster, int column, float mean) {
//		final int size = cluster.length;
//		float sum = 0f;
//		float value;
//		validN = 0;
//		for (int i=0; i<size; i++) {
//			value = expMatrix.get(((Integer) cluster[i]).intValue(), column);
//			if (!Float.isNaN(value)) {
//				sum += Math.pow(value-mean, 2);
//				validN++;
//			}
//		}
//		return sum;
//	}

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

	private FloatMatrix getPValues(){
		FloatMatrix pvals = new FloatMatrix(numGenes, pValues.length);
		for (int i=0; i<pValues.length; i++){
			for (int j=0; j<mapping.length; j++){
				pvals.A[j][i] = pValues[i][j]; 
			}
		}
		return pvals;
	}

	private FloatMatrix getFValues(){
		FloatMatrix pvals = new FloatMatrix(numGenes, 1);
		for (int j=0; j<mapping.length; j++){
			pvals.A[j][0] = fValues[j]; 
		}
		
		return pvals;
	}
	
	private FloatMatrix getAdjPValues() {
		FloatMatrix adjpvals = new FloatMatrix(numGenes, adjPvalues.length);
		for (int i=0; i<adjPvalues.length; i++){
			for (int j=0; j<mapping.length; j++){
				adjpvals.A[j][i] = adjPvalues[i][j]; 
			}
		}
		return adjpvals;
	}

	private FloatMatrix getLogFoldChanges() {
		FloatMatrix lfcs = new FloatMatrix(numGenes, lfc.length);
		
		for (int i=0; i<lfc.length; i++){
			for (int j=0; j<mapping.length; j++){
				lfcs.A[j][i] = lfc[i][j]; 
			}
		}
		return lfcs;
	}

	private FloatMatrix getLogOdds() {
		FloatMatrix lodds = new FloatMatrix(numGenes, logOdds.length);
		//System.out.println("numGenes " + numGenes);
		//System.out.println("mapping.length " + mapping.length);

		for (int i=0; i<logOdds.length; i++){
			for (int j=0; j<mapping.length; j++){
				lodds.A[j][i] = logOdds[i][j]; 
			}
		}
		return lodds;
	}

	private FloatMatrix getTStatistic() {
		FloatMatrix tstat = new FloatMatrix(numGenes, t.length);
		for (int i=0; i<t.length; i++){
			for (int j=0; j<mapping.length; j++){
				tstat.A[j][i] = t[i][j]; 
			}
		}
		return tstat;
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
	 * Function to create R session in memory and execute LIMMA
	 */
	public void runRAlg() {
		progress++;
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue(10);
		fireValueChanged(event);

		Rengine re;
		try {
			re = RHook.startRSession();
			if(re == null) {
				JOptionPane.showMessageDialog(null, "Error creating R Engine", "REngine", JOptionPane.ERROR_MESSAGE);
				//logger.writeln("Could not get REngine");
				return;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error creating R Engine", "REngine", JOptionPane.ERROR_MESSAGE);
			//logger.writeln("Could not get REngine");
			return;
		}

		try {
		//System.out.println("Testing LIMMA install");
		RHook.testPackage("limma");
		//System.out.println("Loading Lib LIMMA");
		RHook.log("dataDesign = " + dataDesign);
		RHook.log("Starting R Algorithim");
		
		String rCmd = "library(limma)";
		RHook.evalR(rCmd);

		int numProbes = expMatrix.getRowDimension();
		int numSamples = expMatrix.getColumnDimension();

		String fileLoc = System.getProperty("user.dir")+System.getProperty("file.separator")+"tmpfile.txt";
		//if(fileLoc.contains("\\"));
		fileLoc = fileLoc.replace("\\", "/");
		String filePath = writeMatrixToFile(fileLoc, expMatrix, geneNames);
		//Create data matrix in R from a file
		//logger.writeln("RHook.createRDataMatrixFromFile(\"y\","+ filePath+", true,"+ sampleNames+")");
		RHook.createRDataMatrixFromFile("y", filePath, true, sampleNames);
		//Create data matrix in R, in memory - Inefficient
		//RHook.createRDataMatrix("yy", expMatrix, geneNames, sampleNames);

		//TODO
		//define design vector based on experiment. COMPLICATED

		//System.out.println("design <- cbind(Grp1=1,Grp2vs1=c(rep(0,dim(y)[2]/2),rep(1,dim(y)[2]/2)))");
		int grp1, grp2;
		if(numSamples % 2 == 0) grp1 = numSamples/2;
		else grp1 = (numSamples-1)/2;
		grp2 = numSamples - grp1;

		String design = "design <- cbind(Grp1=1,Grp2vs1=c(rep(0," + grp1 + "),rep(1," + grp2 + ")))";
		//System.out.println("Study Design: " + design);

		if (dataDesign == 4||dataDesign == 5){
			RHook.log(design);
			String ts = "TS <- c(";
			for (int i=0; i<numSamples; i++){
				ts = ts + "\""+this.nameA+((this.groupAssignments[i]-1)/numBGroups+1)+"."+this.nameB+((groupAssignments[i]-1)%numBGroups+1)+"\",";
			}
			ts = ts.substring(0, ts.length()-1);
			ts = ts+")";
			RHook.evalR(ts);
			//System.out.println("ts: " + ts);

			String levels = "c(";
			for (int i=0; i<this.numAGroups; i++){
				for (int j=0; j<this.numBGroups; j++){
					levels = levels + "\""+this.nameA+(i+1)+"."+this.nameB+(j+1)+"\",";
				}
			}
			levels = levels.substring(0, levels.length()-1);
			levels = levels+"))";
			rCmd = "TS <-factor(TS, levels = "+levels;
			RHook.evalR(rCmd);
			//System.out.println("asdasd: " + rCmd);
		}
		
		design = getStudyDesign();
		RHook.log(design);
		RHook.evalR(design);

		if (dataDesign == 3){
			// fit with contrasts
			rCmd = "fit <- lmFit(y,design)";
			RHook.evalR(rCmd);
			//System.out.println("fit <- lmFit(y,design)");
			
			rCmd = "contrast.matrix <- makeContrasts(";
			for (int i=0; i<numGroups; i++){
				for (int j=i+1; j<numGroups; j++){
					rCmd = rCmd + "Grp"+(i+1)+"-Grp"+(j+1)+",";
				}
			}
			rCmd = rCmd + "levels = design)";
			RHook.evalR(rCmd);
			//System.out.println("contrasts comm: " + rCmd);
			
			rCmd = "fit2 <- contrasts.fit(fit, contrast.matrix)";
			RHook.evalR(rCmd);
			//System.out.println(rCmd);
			
			rCmd = "fit2 <- eBayes(fit2)";
			RHook.evalR(rCmd);
			//System.out.println(rCmd);
		} else if (dataDesign == 4){
			rCmd = "colnames(design) <-levels(TS)";
			RHook.evalR(rCmd);
			

//			rCmd = "design <- design[c(1:"+groupAssignments.length+"),c(1:"+(numAGroups*numBGroups)+")]";
//			RHook.evalR(rCmd);
			rCmd = "y <- y[,c(as.numeric(rownames(design)))]";
			RHook.evalR(rCmd);
			
			
			rCmd = "fit <- lmFit(y,design)";
			RHook.evalR(rCmd);

			rCmd = "cont.matrix <- makeContrasts(";
			rCmd += "FactorA1.B1vsB2 = "+this.nameA+"1."+this.nameB+"1 - "+this.nameA+"1."+this.nameB+"2,";
			rCmd += "FactorA2.B1vsB2 = "+this.nameA+"2."+this.nameB+"1 - "+this.nameA+"2."+this.nameB+"2,";
			rCmd += "Diff = ("+this.nameA+"2."+this.nameB+"1 - "+this.nameA+"2."+this.nameB+"2) - ("+this.nameA+"1."+this.nameB+"1 - "+this.nameA+"1."+this.nameB+"2),levels = design)";
			RHook.evalR(rCmd);
			//System.out.println(rCmd);
			
			rCmd = "fit2 <- contrasts.fit(fit, cont.matrix)";
			RHook.evalR(rCmd);
			//System.out.println(rCmd);
			
			rCmd = "fit2 <- eBayes(fit2)";
			RHook.evalR(rCmd);
			//System.out.println(rCmd);
		} else if (dataDesign == 5){
			rCmd = "colnames(design) <-levels(TS)";
			RHook.evalR(rCmd);
			
//			rCmd = "design <- design[c(1:"+groupAssignments.length+"),c(1:"+(numAGroups*numBGroups)+")]";
//			RHook.evalR(rCmd);
			rCmd = "y <- y[,c(as.numeric(rownames(design)))]";
			RHook.evalR(rCmd);
			
			
			rCmd = "fit <- lmFit(y,design)";
			RHook.evalR(rCmd);

			rCmd = "cont1.matrix <- makeContrasts(";
			for (int i=0; i<numGroups-1; i++){
				rCmd = rCmd + "\"Condition1."+nameB+(i+2)+"-"+"Condition1."+nameB+(i+1)+"\",";
			}
			rCmd = rCmd + "levels = design)";
			RHook.evalR(rCmd);
			//System.out.println(rCmd);

			rCmd = "fit2a <- contrasts.fit(fit, cont1.matrix)";
			RHook.evalR(rCmd);
			//System.out.println(rCmd);
			
			rCmd = "fit2a <- eBayes(fit2a)";
			RHook.evalR(rCmd);
			//System.out.println(rCmd);
			
			rCmd = "cont2.matrix <- makeContrasts(";
			for (int i=0; i<numGroups-1; i++){
				rCmd = rCmd + "\"Condition2."+nameB+(i+2)+"-"+"Condition2."+nameB+(i+1)+"\",";
			}
			rCmd = rCmd + "levels = design)";
			RHook.evalR(rCmd);
			//System.out.println(rCmd);

			rCmd = "fit2b <- contrasts.fit(fit, cont2.matrix)";
			RHook.evalR(rCmd);
			//System.out.println(rCmd);
			
			rCmd = "fit2b <- eBayes(fit2b)";
			RHook.evalR(rCmd);
			//System.out.println(rCmd);

			rCmd = "cont3.matrix <- makeContrasts(";
			for (int i=0; i<numGroups-1; i++){
				rCmd = rCmd + "Dif"+i+" = (Condition1."+nameB+(i+2)+"-"+"Condition1."+nameB+(i+1)+") - " +
					"(Condition2."+nameB+(i+2)+"-"+"Condition2."+nameB+(i+1)+"),";
			}
			rCmd = rCmd + "levels = design)";
			RHook.evalR(rCmd);
			//System.out.println(rCmd);

			rCmd = "fit2c <- contrasts.fit(fit, cont3.matrix)";
			RHook.evalR(rCmd);
			//System.out.println(rCmd);
			
			rCmd = "fit2c <- eBayes(fit2c)";
			RHook.evalR(rCmd);
			//System.out.println(rCmd);
			
		}else {
			// Ordinary fit
			rCmd = "fit <- lmFit(y,design)";
			//System.out.println(rCmd);
			RHook.evalR(rCmd);
			rCmd = "fit <- eBayes(fit)";
			//System.out.println(rCmd);
			RHook.evalR(rCmd);
		}

		// Various ways of summarizing or plotting the results
		//TODO maybe?
		//Extract data based on study design
		//System.out.println("Summarizing LIMMA result");
		//System.out.println("res <- topTable(fit,coef=2)");
//		System.out.println("res <- toptable(fit,coef=c(2),number="+ numProbes +",genelist=fit$genes,adjust.method='fdr',sort.by='B',p.value=1,lfc=0)");

		REXP x;
		String[] topTabString;
		switch (dataDesign){
			case 1:{ //one-class design
				topTabString = new String[1];
				topTabString[0] = "res0 <- toptable(fit,number="+ numProbes +",genelist=fit$genes,adjust.method='fdr',sort.by='B',p.value=1,lfc=0)"; //List all genes with lfc
				//System.out.println(" one class : "+topTabString[0]);
				sigGenesArrays = new int[2][]; //sig genes	
				break;
			}
			case 2:{ //two-class design
				topTabString = new String[1];
				topTabString[0] = "res0 <- toptable(fit, coef = 2, number="+ numProbes +",genelist=fit$genes,adjust.method='fdr',sort.by='B',p.value=1,lfc=0)"; //List all genes with lfc
				//System.out.println(" two class : "+topTabString[0]);
				sigGenesArrays = new int[2][]; //sig genes	
				break;
			}
			case 3: {//multiple-class design 
				int ttCases = 0;
				for (int i=0; i<numGroups; i++)
					ttCases = ttCases+i;
				topTabString = new String[ttCases];
				for (int i=0; i<ttCases; i++){
					topTabString[i] = "res"+i+" <- toptable(fit2,coef="+(i+1)+",number="+ numProbes +",genelist=fit$genes,adjust.method='fdr',sort.by='B',p.value=1,lfc=0)"; //List all genes with lfc
					//System.out.println(" multi class ["+i+"] : "+topTabString[i]);
				}
				sigGenesArrays = new int[topTabString.length*2+2][]; //sig genes	
				break;
			}
			case 4:{ //two-factor design
				topTabString = new String[3];
				for (int i=0; i<3; i++){
					topTabString[i] = "res"+i+" <- toptable(fit2,coef="+(i+1)+",number="+ numProbes +",genelist=fit$genes,adjust.method='fdr',sort.by='B',p.value=1,lfc=0)"; //List all genes with lfc
					//System.out.println(" 2-factor ["+i+"] : "+topTabString[i]);
				}
				sigGenesArrays = new int[topTabString.length*2+2][]; //sig genes	
				break;
				}
			case 5:{ //time-course design
				topTabString = new String[2*(numGroups-1)];
				for (int i=0; i<numGroups-1; i++){
					topTabString[i] = "res"+i+" <- toptable(fit2a,coef="+(i+1)+",number="+ numProbes +",genelist=fit$genes,adjust.method='fdr',sort.by='B',p.value=1,lfc=0)"; //List all genes with lfc
					//System.out.println(" timecourse ["+i+"] : "+topTabString[i]);
				}
				for (int i=numGroups-1; i<2*(numGroups-1); i++){
					topTabString[i] = "res"+i+" <- toptable(fit2b,coef="+(i-numGroups+2)+",number="+ numProbes +",genelist=fit$genes,adjust.method='fdr',sort.by='B',p.value=1,lfc=0)"; //List all genes with lfc
					//System.out.println(" timecourse ["+i+"] : "+topTabString[i]);
				}
				sigGenesArrays = new int[topTabString.length*2+6][]; //sig genes	
				break;
				}
			default:{ //no design
				topTabString = new String[1];
				topTabString[0] = "default";	
				sigGenesArrays = new int[topTabString.length*2+2][]; //sig genes	
			}
		}
		int grpPairs = topTabString.length;
		lfc = new float[grpPairs][numProbes];
		t = new float[grpPairs][numProbes];
		logOdds = new float[grpPairs][numProbes];
		pValues = new float[grpPairs][numProbes];
		adjPvalues = new float[grpPairs][numProbes];
		fValues = new float[numProbes];
		for (int interax=0; interax<grpPairs; interax++){
			RHook.evalR(topTabString[interax]);
			rCmd = "as.numeric(rownames(res"+interax+"))-1";
			x = RHook.evalR(rCmd);
			double rowIndices[] = x.asDoubleArray();
	
			//System.out.println("rowIndices[] "+ rowIndices[0] + " " + rowIndices[numProbes-1]);
			//System.out.println("res$ID");
			rCmd = "res"+interax+"$ID";
			x = RHook.evalR(rCmd);
			String sigGenes[]=x.asStringArray(); //Basically all genes 
			
			rCmd = "res"+interax+"$logFC";
			x = RHook.evalR(rCmd);
			double tmp[]=x.asDoubleArray();
			for(int i=0; i < tmp.length; i++) {
				lfc[interax][(int)rowIndices[i]] = (float)tmp[i];
			}
			
			rCmd = "res"+interax+"$t";
			x = RHook.evalR(rCmd);
			tmp=x.asDoubleArray();
			for(int i=0; i < tmp.length; i++) {
				t[interax][(int)rowIndices[i]] = (float)tmp[i];
			}
			
			rCmd = "res"+interax+"$P.Value";
			x = RHook.evalR(rCmd);
			tmp=x.asDoubleArray();
			for(int i=0; i < tmp.length; i++) {
				pValues[interax][(int)rowIndices[i]] = (float)tmp[i];
			}
			
			rCmd = "res"+interax+"$adj.P.Val";
			x = RHook.evalR(rCmd);
			tmp=x.asDoubleArray();
			for(int i=0; i < tmp.length; i++) {
				adjPvalues[interax][(int)rowIndices[i]] = (float)tmp[i];
				//System.out.print(tmp[i]+"\t");
			}
			
			rCmd = "res"+interax+"$B";
			x = RHook.evalR(rCmd);
			tmp=x.asDoubleArray();
			for(int i=0; i < tmp.length; i++) {
				logOdds[interax][(int)rowIndices[i]] = (float)tmp[i];
			}
	
			updateProgressBar();
	
			//Record sig and non-sig gene indices based on user defined alpha
			ArrayList<Integer> sig = new ArrayList<Integer>();
			ArrayList<Integer> nonsig = new ArrayList<Integer>();
			for(int i = 0; i < adjPvalues[interax].length; i++) {
				if(adjPvalues[interax][i] <= alpha)
					sig.add(new Integer(i));
				else
					nonsig.add(new Integer(i));
			}
			//System.out.println("sig# = "+sig.size()+ "  non-sig# = "+ nonsig.size());
			sigGenesArrays[interax*2] = new int[sig.size()]; //sig genes
			sigGenesArrays[interax*2+1] = new int[numProbes - sig.size()]; //non-sig genes
	
			//Hashtable _tmpsigGenesTable = new Hashtable();
			for (int i=0; i<sigGenesArrays[interax*2].length; i++){
				sigGenesArrays[interax*2][i] = sig.get(i);
			}
			//System.out.println(sigGenesArrays[interax*2+1].length);
			//System.out.println(nonsig.size());
			for (int i=0; i<sigGenesArrays[interax*2+1].length; i++){
				sigGenesArrays[interax*2+1][i] = nonsig.get(i);
			}
		}

		if (dataDesign==3||dataDesign==4){
			rCmd = "fit2$F.p.value";
			x = RHook.evalR(rCmd);
			//System.out.println(rCmd);
			double[] tmp=x.asDoubleArray();
			for(int i=0; i < tmp.length; i++) {
				fValues[i] = (float)tmp[i];
			}
			ArrayList<Integer> sig = new ArrayList<Integer>();
			ArrayList<Integer> nonsig = new ArrayList<Integer>();
			for(int i = 0; i < fValues.length; i++) {
				if(fValues[i] <= alpha)
					sig.add(new Integer(i));
				else
					nonsig.add(new Integer(i));
			}
			sigGenesArrays[grpPairs*2] = new int[sig.size()];
			sigGenesArrays[grpPairs*2+1] = new int[nonsig.size()];
			for (int i=0; i<sigGenesArrays[grpPairs*2].length; i++){
				sigGenesArrays[grpPairs*2][i] = sig.get(i);
			}
			for (int i=0; i<sigGenesArrays[grpPairs*2+1].length; i++){
				sigGenesArrays[grpPairs*2+1][i] = nonsig.get(i);
			}
		}
		if (dataDesign==5){
			rCmd = "fit2a$F.p.value";
			x = RHook.evalR(rCmd);
			//System.out.println(rCmd);
			double[] tmp=x.asDoubleArray();
			for(int i=0; i < tmp.length; i++) {
				fValues[i] = (float)tmp[i];
			}
			ArrayList<Integer> sig = new ArrayList<Integer>();
			ArrayList<Integer> nonsig = new ArrayList<Integer>();
			for(int i = 0; i < fValues.length; i++) {
				if(fValues[i] <= alpha)
					sig.add(new Integer(i));
				else
					nonsig.add(new Integer(i));
			}
			sigGenesArrays[grpPairs*2+0] = new int[sig.size()];
			sigGenesArrays[grpPairs*2+1] = new int[nonsig.size()];
			for (int i=0; i<sigGenesArrays[grpPairs*2+0].length; i++){
				sigGenesArrays[grpPairs*2+0][i] = sig.get(i);
			}
			for (int i=0; i<sigGenesArrays[grpPairs*2+1].length; i++){
				sigGenesArrays[grpPairs*2+1][i] = nonsig.get(i);
			}

			rCmd = "fit2b$F.p.value";
			x = RHook.evalR(rCmd);
			//System.out.println(rCmd);
			tmp=x.asDoubleArray();
			for(int i=0; i < tmp.length; i++) {
				fValues[i] = (float)tmp[i];
			}
			sig = new ArrayList<Integer>();
			nonsig = new ArrayList<Integer>();
			for(int i = 0; i < fValues.length; i++) {
				if(fValues[i] <= alpha)
					sig.add(new Integer(i));
				else
					nonsig.add(new Integer(i));
			}
			sigGenesArrays[grpPairs*2+2] = new int[sig.size()];
			sigGenesArrays[grpPairs*2+3] = new int[nonsig.size()];
			for (int i=0; i<sigGenesArrays[grpPairs*2+2].length; i++){
				sigGenesArrays[grpPairs*2+2][i] = sig.get(i);
			}
			for (int i=0; i<sigGenesArrays[grpPairs*2+3].length; i++){
				sigGenesArrays[grpPairs*2+3][i] = nonsig.get(i);
			}

			rCmd = "fit2c$F.p.value";
			x = RHook.evalR(rCmd);
			//System.out.println(rCmd);
			tmp=x.asDoubleArray();
			for(int i=0; i < tmp.length; i++) {
				fValues[i] = (float)tmp[i];
			}
			sig = new ArrayList<Integer>();
			nonsig = new ArrayList<Integer>();
			for(int i = 0; i < fValues.length; i++) {
				if(fValues[i] <= alpha)
					sig.add(new Integer(i));
				else
					nonsig.add(new Integer(i));
			}
			sigGenesArrays[grpPairs*2+4] = new int[sig.size()];
			sigGenesArrays[grpPairs*2+5] = new int[nonsig.size()];
			for (int i=0; i<sigGenesArrays[grpPairs*2+4].length; i++){
				sigGenesArrays[grpPairs*2+4][i] = sig.get(i);
			}
			for (int i=0; i<sigGenesArrays[grpPairs*2+5].length; i++){
				sigGenesArrays[grpPairs*2+5][i] = nonsig.get(i);
			}
		}
		RHook.endRSession();
		removeTmps(filePath);
		} catch (Exception e) {
			RHook.log(e);
			try {
				RHook.endRSession();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
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

	private String getStudyDesign(){
		switch (dataDesign){
		case 1:
			return getOneClassDesign();
		case 2:
			return getTwoClassDesign();
		case 3:
			return getMultiClassDesign();
		case 4:
			return getTwoFactorDesign();
		case 5:
			return getTimeCourseDesign();
		default: return "";
		}
	}

	private String getOneClassDesign(){
		String str = "design <- c(";
		for (int i=0; i<groupAssignments.length; i++){
			str = str + Integer.toString(groupAssignments[i]);
			if (i<groupAssignments.length-1)
				str = str  + ", ";
		}
		str = str + ")";
		//System.out.println("str design: " + str);
		return str;
	}

	private String getTwoClassDesign(){
		String grp1 = "";
		String grp2 = "";
		for (int i=0; i<groupAssignments.length; i++){
			if (groupAssignments[i]==1){
				grp1 = grp1 + "1";
				grp2 = grp2 + "0";
			} else if (groupAssignments[i]==2){
				grp1 = grp1 + "1";
				grp2 = grp2 + "1";
			} else {
				grp1 = grp1 + "0";
				grp2 = grp2 + "0";
			}
			if (i<groupAssignments.length-1){
				grp1 = grp1 + ", ";
				grp2 = grp2 + ", ";
			}
		}
		String str = "design <- cbind(Grp1=c(" + grp1 + "),Grp2=c(" + grp2 + "))";
		//System.out.println("str design: " + str);
		return str;
	}

	private String getMultiClassDesign(){
		String[] grpArray = new String[numGroups];
		for (int i=0; i<numGroups; i++){
			grpArray[i] = "";
		}
		for (int i=0; i<groupAssignments.length; i++){
			for (int j=0; j<numGroups; j++){
				if (groupAssignments[i]==j+1)
					grpArray[j] = grpArray[j] + "1";
				else
					grpArray[j] = grpArray[j] + "0";
			}

			if (i<groupAssignments.length-1){
				for (int j=0; j<numGroups; j++){
					grpArray[j] = grpArray[j] + ", ";
				}
			}
		}
		String str = "design <- cbind(";
		for (int i=0; i<numGroups; i++){
			str = str + "Grp"+(i+1)+"=c("+grpArray[i]+")";
			if (i<numGroups-1){
				str = str + ", ";
			}
		}
		str = str +")";
		//System.out.println("str design: " + str);
		return str;
	}

	private String getTwoFactorDesign(){
		String str = "design <- model.matrix(~0 + TS)";

		return str;		
	}

	private String getTimeCourseDesign(){
		String str = "design <- model.matrix(~0 + TS)";

		return str;		
	}


	public void updateProgressBar(){

		progress++;
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue((100*progress)/(progress+7));
	}

	private void removeTmps(String fileName) {
		File f = new File(fileName);
		f.delete();
	}

}
