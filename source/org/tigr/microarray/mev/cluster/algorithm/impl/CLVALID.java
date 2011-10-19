/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/**
 * @author  dschlauch
 * @version
 */
package org.tigr.microarray.mev.cluster.algorithm.impl;

import org.rosuda.JRI.Rengine;
import org.tigr.rhook.RHook;
import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.GraphViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ResultDataTable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

public class CLVALID extends AbstractAlgorithm{
	private int progress;
	private FloatMatrix expMatrix;
	private boolean stop = false;

	private AlgorithmEvent event;

	private String[] geneNames,sampleNames;
	private boolean isInternalV,isStabilityV,isBiologicalV,isClusterGenes,isClusterSamples;
	private int lowClusterRange,highClusterRange;
	private double[] measuresIntern,measuresStab,measuresBio;
	private String[] methodsArray;
	private String linkageMethod,distanceMetric,bioCAnnotation;
	private HashMap<String, Object> optimalScoresIntern = new HashMap<String, Object>();
	private HashMap<String, Object> optimalScoresStab = new HashMap<String, Object>();
	private HashMap<String, Object> optimalScoresBio = new HashMap<String, Object>();
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
		expMatrix = data.getMatrix("experiment");
		geneNames = data.getStringArray("geneLabels");
		sampleNames = data.getStringArray("sampleLabels");
		methodsArray = data.getStringArray("methodsArray");
		AlgorithmParameters map = data.getParams();
		isClusterGenes = map.getBoolean("cluster-genes");
		isClusterSamples = map.getBoolean("cluster-samples");
		isInternalV = map.getBoolean("internal-validation");
		isStabilityV = map.getBoolean("stability-validation");
		isBiologicalV = map.getBoolean("biological-validation");
		lowClusterRange = map.getInt("cluster-range-low");
		highClusterRange = map.getInt("cluster-range-high");
		linkageMethod = map.getString("validation-linkage");
		distanceMetric = map.getString("validation-distance");
		bioCAnnotation = map.getString("bioC-annotation");
		
		progress=0;
		event = null;
		event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Calculating...");
		// set progress limit
		fireValueChanged(event);
		event.setId(AlgorithmEvent.MONITOR_VALUE);
		event.setDescription("description");
		fireValueChanged(event);
		event.setId(AlgorithmEvent.SET_VALUE);
		event.setDescription("description");
		fireValueChanged(event);
		event.setId(AlgorithmEvent.WARNING);
		event.setDescription("description");
		fireValueChanged(event);

		runRAlg();
		if (stop) 
			throw new AbortException();		

		data.addResultNode("validation-node", createResultNode());
		return data;   
	}



	private DefaultMutableTreeNode createResultNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Cluster Validation");
		int numClusters = this.highClusterRange-this.lowClusterRange+1;
		int numMeasures;
		int numMethods = methodsArray.length;
		if (this.isInternalV){
			DefaultMutableTreeNode ivNode = new DefaultMutableTreeNode("Internal Validation");
			String[] measures = new String[]{"Connectivity","Silhouette Width","Dunn Index"};
			numMeasures = measures.length;
			double[][][] dataMatrices = createDataMatrices(measuresIntern, numClusters, numMeasures, numMethods);
			ivNode.add(new DefaultMutableTreeNode(new LeafInfo("Optimal Cluster Scores", new ResultDataTable(optimalScoresIntern, measures)))); 
			for (int i=0; i<measures.length; i++){
				ivNode.add(new DefaultMutableTreeNode(new LeafInfo(measures[i], 
	            		new GraphViewer("Internal Validation", 
	            				dataMatrices[i], 
	            				methodsArray, 
	            				"Number of Clusters",
	            				"Cluster ", 
	            				measures[i], 
	            				"",
	            				lowClusterRange))));
			}
			node.add(ivNode);
    	}
    	if (this.isStabilityV){
			DefaultMutableTreeNode ivNode = new DefaultMutableTreeNode("Stability Validation");
			String[] measures = new String[]{"Avg. Proportion Non-Overlap","Avg. Distance","Avg. Distance Between Means","Figure of Merit"};
			numMeasures = measures.length;
			double[][][] dataMatrices = createDataMatrices(measuresStab, numClusters, numMeasures, numMethods);
			ivNode.add(new DefaultMutableTreeNode(new LeafInfo("Optimal Cluster Scores", new ResultDataTable(optimalScoresStab, measures)))); 
			for (int i=0; i<measures.length; i++){
				ivNode.add(new DefaultMutableTreeNode(new LeafInfo(measures[i],  
            		new GraphViewer("Stability Validation", 
            				dataMatrices[i], 
            				methodsArray, 
            				"Number of Clusters",
            				"Cluster ", 
            				measures[i], 
            				"",
            				lowClusterRange))));
			}
			node.add(ivNode);
    	}
    	if (this.isBiologicalV){
			DefaultMutableTreeNode ivNode = new DefaultMutableTreeNode("Biological Validation");
			String[] measures = new String[]{"Biological Homogeneity Index","Biological Stability Index"};
			numMeasures = measures.length;
			double[][][] dataMatrices = createDataMatrices(measuresBio, numClusters, numMeasures, numMethods);
			ivNode.add(new DefaultMutableTreeNode(new LeafInfo("Optimal Cluster Scores", new ResultDataTable(optimalScoresBio, measures)))); 
			for (int i=0; i<measures.length; i++){
				ivNode.add(new DefaultMutableTreeNode(new LeafInfo(measures[i],			
            		new GraphViewer("Biological Validation", 
            				dataMatrices[i], 
            				methodsArray, 
            				"Number of Clusters",
            				"Cluster ", 
            				measures[i], 
            				"",
            				lowClusterRange))));
			}
			node.add(ivNode);
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
				throw new AbortException();
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "REngine", JOptionPane.ERROR_MESSAGE);
			throw new AbortException();
		}

		try {
			RHook.testPackage("clvalid");
			RHook.log("Starting R Algorithim");
			
			String rCmd = "library(clValid)";
			RHook.evalR(rCmd);

			rCmd = "zz <- file('all.Rout', open='wt')";
			RHook.evalR(rCmd);
			rCmd = "sink(zz)";
			RHook.evalR(rCmd);
			rCmd = "sink(zz, type='message')";
			RHook.evalR(rCmd);
			
			String fileLoc = System.getProperty("user.dir")+System.getProperty("file.separator")+"tmpfile.txt";
			fileLoc = fileLoc.replace("\\", "/");
			
			String filePath;
			if (isClusterGenes){
				filePath = writeMatrixToFile(fileLoc, expMatrix, geneNames);
				RHook.createRDataMatrixFromFile("y", filePath, true, sampleNames);
			} else {
				filePath = writeMatrixToFile(fileLoc, expMatrix.transpose(), sampleNames);
				RHook.createRDataMatrixFromFile("y", filePath, true, geneNames);
			}
				
			String methodsString = getMethodsString();
			if (isInternalV)
				measuresIntern = runRScriptValidationStep(methodsString, optimalScoresIntern, "internal", measuresIntern);
			if (isStabilityV)
				measuresStab = runRScriptValidationStep(methodsString, optimalScoresStab, "stability", measuresStab);			
			if (isBiologicalV)
				runRScriptBioValidationStep(methodsString);			

			rCmd = "sink()";
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

	private void runRScriptBioValidationStep(String methodsString) throws Exception {
		String rCmd = "source('http://www.bioconductor.org/biocLite.R')";
		RHook.evalR(rCmd);
		rCmd = "biocLite('"+bioCAnnotation+"')";
		RHook.evalR(rCmd);
		rCmd = "library("+bioCAnnotation+")";
		RHook.evalR(rCmd);
		rCmd = "if(require('Biobase') && require('annotate') && require('GO.db') && require('"+bioCAnnotation+"')) {" +
				"bio2 <- clValid(y, "+lowClusterRange+":"+highClusterRange+", clMethods=c("+methodsString+
				"), metric = '"+distanceMetric+"', method = '"+linkageMethod+"', validation='biological', annotation='"+bioCAnnotation+"',GOcategory='all')}";
		RHook.evalR(rCmd);
		rCmd = "optimalScores(bio2)";
		RHook.evalR(rCmd);
		rCmd = "bio2@measures";
		measuresBio = RHook.evalR(rCmd).asDoubleArray();

		optimalScoresBio = new HashMap<String, Object>();
		rCmd = "as.matrix(optimalScores(bio2)[1])";
		optimalScoresBio.put("scores", RHook.evalR(rCmd).asDoubleArray());	
		rCmd = "as.matrix(optimalScores(bio2)[2])";
		optimalScoresBio.put("method", RHook.evalR(rCmd).asStringArray());
		rCmd = "as.matrix(optimalScores(bio2)[3])";
		optimalScoresBio.put("clusters", RHook.evalR(rCmd).asStringArray());	
		optimalScoresBio.put("numMeasures", 2);
		
	}
	private double[] runRScriptValidationStep(String methodsString, HashMap<String, Object> optimalScores, String validationType, double[] measuresIntern) throws Exception{

        String rCmd = "results <- clValid(y, "+lowClusterRange+":"+highClusterRange+", clMethods=c("+methodsString+
        	"), metric = '"+distanceMetric+"', method = '"+linkageMethod+"',  validation='"+validationType+"')";
		RHook.evalR(rCmd);
		rCmd = "summary(results)";
		RHook.evalR(rCmd);
		rCmd = "results@measures";
		measuresIntern = RHook.evalR(rCmd).asDoubleArray();
		System.out.println("measuresIntern1 = "+measuresIntern.length);
		
		rCmd = "as.matrix(optimalScores(results)[1])";
		optimalScores.put("scores", RHook.evalR(rCmd).asDoubleArray());	
		rCmd = "as.matrix(optimalScores(results)[2])";
		optimalScores.put("method", RHook.evalR(rCmd).asStringArray());
		rCmd = "as.matrix(optimalScores(results)[3])";
		optimalScores.put("clusters", RHook.evalR(rCmd).asStringArray());	
		optimalScores.put("numMeasures", 3);
		return measuresIntern;
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
