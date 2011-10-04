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
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.GraphViewer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

public class CLVALID extends AbstractAlgorithm{
	private int progress;
	private FloatMatrix expMatrix;
	private boolean stop = false;
	private int[][] clusterArrays;

	private int numGenes, numExps, iteration;

	private AlgorithmEvent event;

	private String[] geneNames;
	private String[] sampleNames;
	private boolean isInternalV;
	private boolean isStabilityV;
	private boolean isBiologicalV;
	private int lowClusterRange;
	private int highClusterRange;
	private double[] measuresIntern;
	private double[] measuresStab;
	private double[] measuresBio;
	private String[] methodsArray;
	//private clvalidLogger logger;
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
		geneNames = data.getStringArray("geneLabels");
		sampleNames = data.getStringArray("sampleLabels");
		methodsArray = data.getStringArray("methodsArray");
		isInternalV = map.getBoolean("internal-validation");
		isStabilityV = map.getBoolean("stability-validation");
		isBiologicalV = map.getBoolean("biological-validation");
		lowClusterRange = map.getInt("cluster-range-low");
		highClusterRange = map.getInt("cluster-range-high");
		
//        data.addParam("validate", String.valueOf(dialog.isValidate()));
//        data.addParam("internal-validation", String.valueOf(dialog.isInternalV()));
//        data.addParam("stability-validation", String.valueOf(dialog.isStabilityV()));
//        data.addParam("biological-validation", String.valueOf(dialog.isBiologicalV()));
//        data.addParam("cluster-range-low", String.valueOf(dialog.getLowClusterRange()));
//        data.addParam("cluster-range-high", String.valueOf(dialog.getHighClusterRange()));

		numGenes= expMatrix.getRowDimension();
		numExps = expMatrix.getColumnDimension();
		progress=0;
		event = null;
		event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Calculating...");
		// set progress limit
		fireValueChanged(event);
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue(0);
		fireValueChanged(event);

		runRAlg();
		if (stop) {
			throw new AbortException();
		}

		AlgorithmData result = new AlgorithmData();
		Cluster result_cluster = new Cluster();
		NodeList nodeList = result_cluster.getNodeList();
		int[] features;        
		if (clusterArrays!=null){
			for (int i=0; i<clusterArrays.length; i++) {
				if (stop) {
					throw new AbortException();
				}
				features = clusterArrays[i];
				Node node = new Node(features);
				nodeList.addNode(node);
			}
		}

		// prepare the result
		result.addIntMatrix("sigGenesArrays", clusterArrays);
		result.addParam("iterations", String.valueOf(iteration-1));
		result.addCluster("cluster", result_cluster);
		result.addResultNode("validation-node", createResultNode());

		return result;   
	}



	private DefaultMutableTreeNode createResultNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Cluster Validation");
		int numClusters = this.highClusterRange-this.lowClusterRange+1;
		int numMeasures = 3;
		int numMethods = methodsArray.length;
		if (this.isInternalV){
			numMeasures = 3;//standard for Internal Validation
			String[] measures = new String[]{"Connectivity","Silhouette Width","Dunn Index"};
			double[][][] dataMatrices = createDataMatrices(measuresIntern, numClusters, numMeasures, numMethods);
			DefaultMutableTreeNode ivNode = new DefaultMutableTreeNode("Internal Validation");
			for (int i=0; i<measures.length; i++){
				ivNode.add(new DefaultMutableTreeNode(new LeafInfo(measures[i], 
	            		new GraphViewer("Internal Validation", 
	            				dataMatrices[i], 
	            				methodsArray, 
	            				"Number of Clusters",
	            				"Cluster ", 
	            				"Connectivity", 
	            				"",
	            				lowClusterRange))));
			}
			node.add(ivNode);
    	}
    	if (this.isStabilityV){
			numMeasures = 4;//standard for Stabilization Validation
			double[][][] dataMatrices = createDataMatrices(measuresStab, numClusters, numMeasures, numMethods);
        	node.add(new DefaultMutableTreeNode(new LeafInfo("Stability Validation", 
            		new GraphViewer("Stability Validation", 
            				dataMatrices[0], 
            				methodsArray, 
            				"Number of Clusters",
            				"Cluster ", 
            				"Connectivity", 
            				"",
            				lowClusterRange))));
    	}
    	if (this.isBiologicalV){
			double[][][] dataMatrices = createDataMatrices(measuresBio, numClusters, numMeasures, numMethods);
        	node.add(new DefaultMutableTreeNode(new LeafInfo("Biological Validation", 
            		new GraphViewer("Biological Validation", 
            				dataMatrices[0], 
            				methodsArray, 
            				"Number of Clusters",
            				"Cluster ", 
            				"Connectivity", 
            				"",
            				lowClusterRange))));
    	}
		return node;
	}
	private double[][][] createDataMatrices(double[] measures, int numClusters, int numMeasures, int numMethods) {
		//validate
		if (measures.length!=numClusters*numMeasures*numMethods){
			System.out.println("measures.length = "+measures.length+", numClusters = "+numClusters +", numMeasures = "+numMeasures+", numClusterings = "+numMethods);
			return null;
		}
		int index = 0;
		double[][][] res = new double[numMeasures][numMethods][numClusters];
		for (int i=0; i<numMethods; i++){
			for (int j=0; j<numClusters; j++){
				for (int k=0; k<numMeasures; k++){
					res[k][i][j] = measures[index];
					index++;
				}				
			}			
		}
		return res;
	}
	/**
	 * Function to create R session in memory and execute CLVALID
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
			RHook.log("Starting R Algorithim");
			
			String rCmd = "library(clValid)";
			RHook.evalR(rCmd);
	
			String fileLoc = System.getProperty("user.dir")+System.getProperty("file.separator")+"tmpfile.txt";
			fileLoc = fileLoc.replace("\\", "/");
			String filePath = writeMatrixToFile(fileLoc, expMatrix, geneNames);
			RHook.createRDataMatrixFromFile("y", filePath, true, sampleNames);
			String methodsString = getMethodsString();
			if (isInternalV){
		        rCmd = "intern <- clValid(y, "+lowClusterRange+":"+highClusterRange+", clMethods=c("+methodsString+"),  validation='internal')";
				RHook.evalR(rCmd);
				rCmd = "summary(intern)";
				RHook.evalR(rCmd);
				rCmd = "optimalScores(intern)";
				RHook.evalR(rCmd);
				rCmd = "intern@measures";
				measuresIntern = RHook.evalR(rCmd).asDoubleArray();
			}
			if (isStabilityV){
				rCmd = "stab <- clValid(y, 2:6, clMethods=c('hierarchical','kmeans','pam'),validation='stability')";
				RHook.evalR(rCmd);
				rCmd = "optimalScores(stab)";
				RHook.evalR(rCmd);
				rCmd = "stab@measures";
				measuresStab = RHook.evalR(rCmd).asDoubleArray();
			}
			if (isBiologicalV){
				String annotation = "hgu133plus2.db";
				rCmd = "if(require('Biobase') && require('annotate') && require('GO.db') && require('"+annotation+"')) {" +
						"bio2 <- clValid(y, 2:6, clMethods=c('hierarchical','kmeans','pam')," +
						"validation='biological',annotation='moe430a.db',GOcategory='all')}";
				RHook.evalR(rCmd);
				rCmd = "optimalScores(bio2)";
				RHook.evalR(rCmd);
				rCmd = "bio2@measures";
				measuresBio = RHook.evalR(rCmd).asDoubleArray();
			}
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

	private String getMethodsString() {
		String methodsString = "";
		for (int i=0; i<methodsArray.length; i++){
			methodsString = methodsString +"'"+methodsArray[i]+ "',";
		}
		if (methodsString.length()>0)
			methodsString = methodsString.substring(0, methodsString.length()-1);
		System.out.println("methods string = "+ methodsString);
		return methodsString;
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

		progress++;
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue((100*progress)/(progress+7));
	}

	private void removeTmps(String fileName) {
		File f = new File(fileName);
		f.delete();
	}

}
