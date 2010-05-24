/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: RP.java,v $
 * $Revision: 1.8 $
 * $Date: 2008-8-28 17:27:39 $
 * $Author: dschlauch $
 * $State: Exp $
 */


package org.tigr.microarray.mev.cluster.algorithm.impl;


import org.rosuda.JRI.REXP;
import org.tigr.rhook.RHook;
import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.gui.impl.minet.MINETInitBox;
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
import java.util.Random;
import java.util.Vector;

import javax.swing.JOptionPane;

public class MINET extends AbstractAlgorithm{
	public static final int FALSE_NUM = 12;
	public static final int FALSE_PROP = 13;  

	private int progress;
	private FloatMatrix expMatrix;
	private FloatMatrix experimentData;

	private boolean stop = false;
	private int[] inGroupAssignments;

	private int numGenes, numExps;
	private boolean debug = false;
	private int testDesign;
	

	private String[] geneNames;
	private String[] sampleNames;
	private String methodName = null;
	private String estimatorName = null;
	private String discretizationName = null;
	private int bins = 0;

	// results
	protected FloatMatrix netAdjMatrix;
	protected String[] rowColIndices;

	private AlgorithmEvent event;
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

		geneNames = data.getStringArray("geneLabels");
		sampleNames = data.getStringArray("sampleLabels");

		methodName = map.getString("methodName");
		estimatorName = map.getString("estimatorName");
		discretizationName = map.getString("discretizationName");
		discretizationName = "equalwidth";
		bins = map.getInt("bins");
		testDesign = map.getInt("dataDesign");
		expMatrix = data.getMatrix("experiment");
		if (testDesign==MINETInitBox.ONE_CLASS){
			inGroupAssignments = data.getIntArray("group_assignments");
			int q =0;
			for (int i=0; i<inGroupAssignments.length; i++){
				//System.out.println("Grp ind " + inGroupAssignments[i]);
				if (inGroupAssignments[i]!=0)
					q++;
			}
			int[] groupAssignments=new int[q];
			q=0;
			for (int i=0; i<inGroupAssignments.length; i++){
				if (inGroupAssignments[i]!=0){
					groupAssignments[q]=i;
					q++;
				}
			}

			groupAssignments=new int[expMatrix.getColumnDimension()];
			for (int i=0; i<groupAssignments.length; i++){
				groupAssignments[i]=i;
			}

			System.out.println(expMatrix.getRowDimension()-1 + ", " + groupAssignments.length);
			experimentData = expMatrix.getMatrix(0, expMatrix.getRowDimension()-1, groupAssignments);
		}

		numGenes= experimentData.getRowDimension();
		numExps = expMatrix.getColumnDimension();

		progress=0;
		event = null;
		event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Running MINET...");
		// set progress limit
		fireValueChanged(event);
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue(0);
		fireValueChanged(event);

		// Run R cmds
		runRAlg();
		//runR_AttractAlg();

		AlgorithmData result = new AlgorithmData();
		result.addMatrix("network", netAdjMatrix);
		result.addStringArray("rowColIndices", rowColIndices);

		// the matrix along the diagonal as it is symmetric
		if (debug){
			for(int i=0; i < 10.; i++){
				for(int ii=i; ii < 10.; ii++){
					System.out.print(netAdjMatrix.get(i, ii) + ", ");
				}
				System.out.println();
			}

			System.out.println("Row Index ");
			for(int i=0; i < 10; i++){
				System.out.println("\tRow Index "+i+": "+ rowColIndices[i]);
			}
		}
		// end debug
		updateProgressBar();
		return result;   
	}

	

	/**
	 * Function to create R session in memory and execute MINET
	 * @throws AbortException 
	 */
	public void runRAlg() throws AbortException {
		progress++;
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue(10);
		fireValueChanged(event);

		try {
			//re = RHook.startRSession();
			if(RHook.startRSession() == null) {
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
		updateProgressBar();
		
		try {
			//System.out.println("Testing MINET install");
			RHook.testPackage("minet");
			//System.out.println("Loading Lib LIMMA");
			//RHook.log("dataDesign = " + dataDesign);
			RHook.log("Starting R Algorithim");

			String rCmd = "library(infotheo)";
			RHook.evalR(rCmd);

			rCmd = "library(minet)";
			RHook.evalR(rCmd);

			rCmd = "data(syn.data)";
			RHook.evalR(rCmd);
			
			updateProgressBar();
			
			String fileLoc = System.getProperty("user.dir")+System.getProperty("file.separator")+"tmpfile.txt";
			fileLoc = fileLoc.replace("\\", "/");
			String filePath = writeMatrixToFile(fileLoc, expMatrix, geneNames);
			//Create data matrix in R from a file
			//logger.writeln("RHook.createRDataMatrixFromFile(\"y\","+ filePath+", true,"+ sampleNames+")");
			RHook.createRDataMatrixFromFile("y", filePath, true, sampleNames);
			//Create data matrix in R, in memory - Inefficient
			//RHook.createRDataMatrix("yy", expMatrix, geneNames, sampleNames);
			updateProgressBar();
			
			rCmd = "t_y <- as.data.frame(t(y))";
			RHook.evalR(rCmd);
			//rCmd = "dimnames(t_y)[[1]] <- paste('SAMPLE', dimnames(t_y)[[1]], sep='_')";
			//RHook.evalR(rCmd);
			//rCmd = "dimnames(t_y)[[2]] <- paste('VAR', dimnames(t_y)[[2]], sep='_')";
			//RHook.evalR(rCmd);
			rCmd = "res<-minet(t_y,\"" + methodName + "\",\""  + estimatorName + "\",\"" + discretizationName + "\"," + bins + ")";
			REXP res = RHook.evalR(rCmd);
			updateProgressBar();
			
			double network[][] = res.asMatrix();
			//cast double to float
			float netadjmat[][] = new float[network.length][network.length];
			for(int row = 0; row < network.length; row++) {
				for(int cow = 0; cow < network.length; cow++) {
					//netadjmat[row][cow] = Double.valueOf(network[row][cow]).floatValue();
					netadjmat[row][cow] = (float)network[row][cow];
				}
			}
			netAdjMatrix = new FloatMatrix(netadjmat);

			// Get row indices/names as str		
			// Get col indices/names as str (actually the same as row as it is a matrix of edges)
			// e.g. "1"
			res = RHook.evalR("colnames(res)");
			rowColIndices = res.asStringArray();
			updateProgressBar();

			RHook.endRSession();
			removeTmps(filePath);
			updateProgressBar();
		} catch (Exception e) {
			RHook.log(e);
			try {
				RHook.endRSession();
				e.printStackTrace();
				throw new AlgorithmException(e);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}


	/**
	 * TODO for permutation based thresholding
	 * @param inputMatrix
	 * @return
	 */
	protected FloatMatrix getPermutedMatrix(FloatMatrix inputMatrix) {
		int[] validArray = new int[numGenes];
		int[] permGenes;
		for (int j = 0; j < validArray.length; j++) {
			validArray[j] = j;
		}
		FloatMatrix permutedMatrix = new FloatMatrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());
		for (int i = 0; i < inputMatrix.getColumnDimension(); i++) {

			permGenes = getPermutedValues(numGenes, validArray); //returns an int array of size "numExps", with the valid values permuted

			for (int j = 0; j < inputMatrix.getRowDimension(); j++) {
				permutedMatrix.A[j][i] = inputMatrix.A[permGenes[j]][i];
			}
		}
		return permutedMatrix;
	} 
	
	/**
	 * TODO for permutation based thresholding
	 * @param arrayLength
	 * @param validArray
	 * @return
	 */
	protected static int[] getPermutedValues(int arrayLength, int[] validArray) {//returns an integer array of length "arrayLength", with the valid values (the currently included experiments) permuted
		int[] permutedValues = new int[arrayLength];
		for (int i = 0; i < permutedValues.length; i++) {
			permutedValues[i] = i;
		}

		int[] permutedValidArray = new int[validArray.length];
		for (int i = 0; i < validArray.length; i++) {
			permutedValidArray[i] = validArray[i];
		}

		for (int i = permutedValidArray.length; i > 1; i--) {
			Random generator2 =new Random();
			int randVal = generator2.nextInt(i - 1);
			int temp = permutedValidArray[randVal];
			permutedValidArray[randVal] = permutedValidArray[i - 1];
			permutedValidArray[i - 1] = temp;
		}  

		for (int i = 0; i < validArray.length; i++) {
			permutedValues[validArray[i]] = permutedValidArray[i];
		}
		return permutedValues;
	}

	private void updateProgressBar(){
		progress++;
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue((100*progress)/5);
		fireValueChanged(event);
	}
	
	/**
	 * 
	 * @param fileLoc
	 * @param fm
	 * @param rowNames
	 * @return
	 * @throws IOException
	 */
	private String writeMatrixToFile(String fileLoc, FloatMatrix fm, String[] rowNames) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(fileLoc));

		int row = fm.getRowDimension();
		int col = fm.getColumnDimension();
		String srtVector = "";

		if(debug) {
			System.out.println("row, col, rowName " + row);
			System.out.println("row, col, rowName " + col);
			System.out.println("row, col, rowName " + rowNames[1]);
		}
		
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
		return fileLoc;
	}
	
	/**
	 * 
	 * @param fileName
	 */
	private void removeTmps(String fileName) {
		File f = new File(fileName);
		f.delete();
	}
	
	/**
	 * DUMMY
	 * @throws AbortException
	 */
	public void runR_AttractAlg() throws AbortException {
		progress++;
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue(10);
		fireValueChanged(event);

		try {
			if(RHook.startRSession() == null) {
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

			String libPath = System.getenv("R_HOME");
			libPath = libPath.replace("\\", "/");
			libPath += "/library";
			String rCmd = ".libPaths('" + libPath + "')";
			System.out.println("libPath cmd " + rCmd);
			RHook.evalR(rCmd);

			rCmd = ".libPaths()";
			REXP rx = RHook.evalR(rCmd);
			System.out.println("Curr libPath " + rx.asStringArray()[0]);

			RHook.testPackage("attract");

			String srcLoc = "C:/Projects/ATTRACT/Mev-Attract_Code/";

			rCmd = "loc='" + srcLoc + "'";
			RHook.evalR(rCmd);

			String scriptR = srcLoc + "MeV-RCode.R";
			rCmd = "source('" + scriptR + "')";
			RHook.evalR(rCmd);

			rCmd = "length(remove_these_genes)";
			rx = RHook.evalR(rCmd);
			int len = rx.asInt();

			RHook.endRSession();

			System.out.println("Length of vector remove.these.genes " + len);

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
	/**
	 * main() test
	 * @param args
	 */
	public static void main(String[] args){
		int[]array = {0,1};
		for (int i=0; i<array.length;i++){
			System.out.print(array[i]+"\t");
		}
		System.out.println(":"+array.length);
		int[] result = getPermutedValues(array.length, array);
		for (int i=0; i<result.length;i++){
			System.out.print(result[i]+"\t");
		}
	}
}