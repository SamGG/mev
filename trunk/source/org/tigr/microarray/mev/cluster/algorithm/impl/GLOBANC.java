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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import java.util.Vector;

public class GLOBANC extends AbstractAlgorithm{
	private int progress;
	private FloatMatrix expMatrix,collapsedExpMatrix;
	private boolean stop = false;
	private int[] groupAssignments;
	private int[][] geneLists;
	private int[] mapping, mapping2;
	private String nameA, nameB;
	private String geneSetFilePath = "C:\\Users\\Dan\\.mev\\repository\\org.tigr.microarray.mev.cluster.gui.impl.gsea.GeneSigDbGeneSets\\genesigdb_genesets3.txt";
	private int numGenes, numExps, numGroups, iteration, numAGroups, numBGroups;
	private float alpha;
	private boolean  drawSigTreesOnly;
	private int hcl_function;
	private boolean hcl_absolute;
	private boolean hcl_genes_ordered;  
	private boolean hcl_samples_ordered; 

	private int dataDesign;

	private AlgorithmEvent event;

	private String[] geneNames, collapsedGeneNames;
	private String[] sampleNames;
	private float[][] lfc;
	private float[][] t;
	private float[][] logOdds;
	private float[][] pValues;
	private float[][] adjPvalues;
	private float[] fValues;
	float[][] resultMatrix;
	int validN;
	private String[] geneListsNames;
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

		geneNames = data.getStringArray("geneLabels");
		sampleNames = data.getStringArray("sampleLabels");
		geneSetFilePath = map.getString("geneSetFilePath");
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
		event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Calculating...");
		// set progress limit
		fireValueChanged(event);
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue(0);
		fireValueChanged(event);

		// Start Logging
		//logger = new GlobancLogger(logFileName);
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
		for (int i=0; i<geneLists.length; i++) {
			if (stop) {
				throw new AbortException();
			}
			features = geneLists[i];
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
		int[][]sigReturn = new int[geneLists.length][];
		//System.out.println("sigGenesArrays.length "+sigGenesArrays.length);
		for (int i=0; i<geneLists.length; i++){
			//System.out.println("sga "+i);
			sigReturn[i]=new int[geneLists[i].length];
			for (int j=0; j<geneLists[i].length; j++){
				sigReturn[i][j]=mapping2[mapping[geneLists[i][j]]];
			}
		}

		// prepare the result
		result.addIntMatrix("geneListsMatrix", sigReturn);
		result.addStringArray("gene-list-names",this.geneListsNames);
		result.addParam("iterations", String.valueOf(iteration-1));
		result.addCluster("cluster", result_cluster);
		result.addParam("number-of-clusters", "1"); 
//		result.addMatrix("clusters_means", means);
//		result.addMatrix("clusters_variances", variances); 
		result.addMatrix("result-matrix", new FloatMatrix(resultMatrix));
		result.addMatrix("geneGroupMeansMatrix", getAllGeneGroupMeans());
		result.addMatrix("geneGroupSDsMatrix", getAllGeneGroupSDs());

//		result.addMatrix("pValues", getPValues());
//		result.addMatrix("adjPValues", getAdjPValues());
//		result.addMatrix("lfc", getLogFoldChanges());
//		result.addMatrix("logOdds", getLogOdds());
//		result.addMatrix("tStat", getTStatistic());
//		result.addMatrix("fValues", getFValues());
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
	 * Function to create R session in memory and execute GLOBANC
	 * @throws AbortException 
	 */
	public void runRAlg() throws AbortException {
		progress++;
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue(10);
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

		try {
		//System.out.println("Testing GLOBANC install");
		RHook.testPackage("globalanc");
		//System.out.println("Loading Lib GLOBANC");
		RHook.log("dataDesign = " + dataDesign);
		RHook.log("Starting R Algorithim");
		
		String rCmd = "library(GlobalAncova)";
		RHook.evalR(rCmd);

		int numProbes = expMatrix.getRowDimension();
		int numSamples = expMatrix.getColumnDimension();

		String fileLoc = System.getProperty("user.dir")+System.getProperty("file.separator")+"tmpfile.txt";
		//if(fileLoc.contains("\\"));
		fileLoc = fileLoc.replace("\\", "/");
		collapseProbesToGenes();
		String filePath = writeMatrixToFile(fileLoc, collapsedExpMatrix, collapsedGeneNames);
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

//		String phenodata = "";// = "design <- cbind(Grp1=1,Grp2vs1=c(rep(0," + grp1 + "),rep(1," + grp2 + ")))";
		//System.out.println("Study Design: " + design);

		if (dataDesign == 4||dataDesign == 5){
			String phenoData = "phenodata <-data.frame(cbind(Sample=1:" +numSamples+"), cbind(full=c(";
			for (int i=0; i<numSamples; i++){
				phenoData = phenoData + ((this.groupAssignments[i]-1)/numBGroups+1)+",";//+this.nameB+((groupAssignments[i]-1)%numBGroups+1)+"\",";
			}
			phenoData = phenoData.substring(0, phenoData.length()-1);
			
			phenoData = phenoData+")), cbind(grade = rep(1,"+numSamples+")),cbind(reduced=c(";
						
			for (int i=0; i<numSamples; i++){
				phenoData = phenoData + ((groupAssignments[i]-1)%numBGroups+1)+",";
			}
			phenoData = phenoData.substring(0, phenoData.length()-1);
			phenoData = phenoData+")))";
//			RHook.log(phenoData);
			RHook.evalR(phenoData);
			System.out.println("phenodata: " + phenoData);

			//phenodata <-data.frame(cbind(Sample=1:19), cbind(metastases=c(0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1)), cbind(grade=rep(1,19)),cbind(ERStatus=pon))
		}
		
//		String[] geneset = getPathwaysCMD_fast();// 
//		// Source the R File genesfile.R
//		RHook.evalR("source('" + geneset[0] + "')");
//		// Source the R File namesfile.R
//		RHook.evalR("source('" + geneset[1] + "')");		

		String[] geneset = getPathwaysCMD();// 
		// Source the R File genesfile.R
		RHook.evalR(geneset[0]);
		// Source the R File namesfile.R
		RHook.evalR(geneset[1]);
		
		// List objects created in R
		REXP e = RHook.evalR("ls()");
		String objs[] = e.asStringArray();
		for(int i=0; i < objs.length; i++) {
			System.out.println("\tR Obj name: " + objs[i]);
		}

		String runGA = "GA.obj <-GlobalAncova(xx = y, formula.full = ~full + reduced, formula.red = ~reduced, model.dat = phenodata, test.genes=genesvector, method='both', perm = 100)";
//		RHook.log(runGA);
		RHook.evalR(runGA);
		
		REXP x = RHook.evalR("GA.obj");
		double[][] matrix = x.asMatrix();
		resultMatrix = new float[matrix.length][matrix[0].length];
		for (int i=0; i<matrix.length; i++){
			for (int j=0; j<matrix[i].length; j++){
				resultMatrix[i][j] = (float)matrix[i][j];
				System.out.print(resultMatrix[i][j]+"\t");
			}
			System.out.println();
		}
		
//		phenodata = getStudyDesign();
//		RHook.log(phenodata);
//		RHook.evalR(phenodata);
		int a=0;
		RHook.endRSession();
		if (a==0)
			return;
		
		
		RHook.endRSession();
		removeTmps(filePath);
		} catch (Exception e) {
			RHook.log(e);
			try {
				RHook.endRSession();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * getPathwaysCMD_fast()
	 * 
	 * Raktim - faster version of getPathwaysCMD
	 * 
	 * can be improved further if gene set file is directly loaded into
	 * R and lists created there.
	 * @return
	 */
	private String[] getPathwaysCMD_fast() {
		String[] cmd = new String[2];
		String genesFile = System.getProperty("user.dir")+System.getProperty("file.separator")+"genesfile.R";
		String namesFile = System.getProperty("user.dir")+System.getProperty("file.separator")+"namesfile.R";
		
		
		
		cmd[0] = genesFile.replace("\\", "/"); //"genesvector <- list(";
		cmd[1] = namesFile.replace("\\", "/"); //"names(genesvector) <- c(";
		try {						
			PrintWriter genesout  = new PrintWriter(new BufferedWriter(new FileWriter(genesFile)));
			PrintWriter namesout  = new PrintWriter(new BufferedWriter(new FileWriter(namesFile)));
			BufferedReader br = new BufferedReader(new FileReader(geneSetFilePath));
			
			genesout.write("genesvector <- list(");
			namesout.write("names(genesvector) <- c(");
			
			String line;
			ArrayList<ArrayList> al = new ArrayList<ArrayList>();
			ArrayList<String> namesal = new ArrayList<String>();
			
			line = br.readLine();
			while( line != null){
				line.trim();
				String[] genes = line.split("\t");
				//String[] genes = line.split("\\s+");
				System.out.println("gene set gene count " + genes.length);
				
				if (genes.length<3)
					continue;
				//if enough genes...
				al.add(new ArrayList<Integer>());

				// cmd[0] = cmd[0] + "c(";
				String tmp ="c(";
				
				boolean first = true;
				
				for (int i=2; i<genes.length; i++){
					if (geneNameAL.contains(genes[i])){//TODO remove gene lists with 0 values
						al.get(al.size()-1).add(geneNameAL.indexOf(genes[i]));						
						//cmd[0] = cmd[0]+(first?"":",")+"'"+genes[i]+"'";
						if(tmp != null) {
							genesout.write(tmp);
							tmp = null;
						}
						genesout.write((first?"":",")+"'"+genes[i]+"'");
						first = false;
					}
				}
				if (first){  //checks to see if there were no matching indices
					//cmd[0] = cmd[0].substring(0, cmd[0].length()-2);
					al.remove(al.size()-1);
					line = br.readLine();
				} else {
					if ((line = br.readLine()) != null) {
						//cmd[0] = cmd[0] + "),";
						genesout.write("),");
						//cmd[1] = cmd[1] + "'"+genes[1].replace("'", "")+"',";
						namesout.write("'"+genes[1].replace("'", "")+"',");
					} else {
						genesout.write(")");
						namesout.write("'"+genes[1].replace("'", "")+"'");
					}
					namesal.add(genes[1]);
				}
			}
			//cmd[0] = cmd[0].substring(0, cmd[0].length()-1)+")";
			genesout.write(")");
			//cmd[1] = cmd[1].substring(0, cmd[1].length()-1)+")";
			namesout.write(")");
			
			this.geneLists = new int[al.size()][];
			this.geneListsNames = new String[namesal.size()];
			
			for (int i=0; i<al.size(); i++){
				geneLists[i]=new int[al.get(i).size()];
				for (int j=0; j<al.get(i).size(); j++){
					geneLists[i][j] = (Integer)al.get(i).get(j);
				}
			}
			for (int i=0; i<namesal.size(); i++){
				geneListsNames[i]=namesal.get(i);
			}
			
			br.close();
			genesout.flush(); genesout.close();
			namesout.flush(); namesout.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		return cmd;
	}
	
	private String[] getPathwaysCMD() {
		String[] cmd = new String[2];
		cmd[0] = "genesvector <- list(";
		cmd[1] = "names(genesvector) <- c(";
		try {						
			BufferedReader br = new BufferedReader(new FileReader(geneSetFilePath));
			String line;
			ArrayList<ArrayList> al = new ArrayList<ArrayList>();
			ArrayList<String> namesal = new ArrayList<String>();
			while( (line = br.readLine()) != null){
				line.trim();
				String[] genes = line.split("\t");
				
				if (genes.length<3)
					continue;
				//if enough genes...
				al.add(new ArrayList<Integer>());
//				namesal.add(genes[1]);
				cmd[0] = cmd[0] + "c(";
				boolean first = true;
				for (int i=2; i<genes.length; i++){
					if (geneNameAL.contains(genes[i])){//TODO remove gene lists with 0 values
						al.get(al.size()-1).add(geneNameAL.indexOf(genes[i]));						
						cmd[0] = cmd[0]+(first?"":",")+"'"+genes[i]+"'";
						first = false;
					}
				}
				if (first){  //checks to see if there were no matching indices
					cmd[0] = cmd[0].substring(0, cmd[0].length()-2);
					al.remove(al.size()-1);
				} else {
					cmd[0] = cmd[0] + "),";
					cmd[1] = cmd[1] + "'"+genes[1].replace("'", "")+"',";
					namesal.add(genes[1]);
				}
			}
			cmd[0] = cmd[0].substring(0, cmd[0].length()-1)+")";
			cmd[1] = cmd[1].substring(0, cmd[1].length()-1)+")";
			
			this.geneLists = new int[al.size()][];
			this.geneListsNames = new String[namesal.size()];
			
			for (int i=0; i<al.size(); i++){
				geneLists[i]=new int[al.get(i).size()];
				for (int j=0; j<al.get(i).size(); j++){
					geneLists[i][j] = (Integer)al.get(i).get(j);
				}
			}
			for (int i=0; i<namesal.size(); i++){
				geneListsNames[i]=namesal.get(i);
			}
			
			br.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		return cmd;
	}
	
	ArrayList<String> geneNameAL;
	private void collapseProbesToGenes() {
		geneNameAL = new ArrayList<String>();
		ArrayList<Integer> indicesMap = new ArrayList<Integer>();
		for (int i=0; i<expMatrix.getRowDimension(); i++){
			if (!geneNameAL.contains(geneNames[i])){
				geneNameAL.add(geneNames[i]);
				indicesMap.add(i);
			}
		}
		collapsedGeneNames = new String[indicesMap.size()];
		float[][] tempCollapsed = new float[indicesMap.size()][expMatrix.A[0].length];
		for (int i=0; i<indicesMap.size(); i++){
			collapsedGeneNames[i] = geneNames[indicesMap.get(i)].equals("NA")?"Unknown":geneNames[indicesMap.get(i)];
			for (int j=0; j<expMatrix.A[i].length; j++){
				tempCollapsed[i][j] = expMatrix.A[indicesMap.get(i)][j];
			}
		}
		collapsedExpMatrix = new FloatMatrix(tempCollapsed);
		
	}
	private String writeMatrixToFile(String fileLoc, FloatMatrix fm, String[] rowNames) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileLoc));

			int row = fm.getRowDimension();
			int col = fm.getColumnDimension();
			System.out.println("row="+row+"  col="+col);
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
