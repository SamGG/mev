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
import org.tigr.rhook.RConstants;
import org.tigr.rhook.RHook;
import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.gui.impl.deseq.DESEQInitBox;
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
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

import javax.swing.JOptionPane;

public class DESEQ extends AbstractAlgorithm {
	public static final int FALSE_NUM = 12;
	public static final int FALSE_PROP = 13;  

	private int progress;
	private int[][] countMatrix;
	//private FloatMatrix experimentData;

	private boolean stop = false;
	private int[] groupAssignments;
	private int[] librarySize;
	boolean calcLibSize = true;
	//private int[] transcriptLen;

	private int numGenes, numExps;
	private boolean debug = false;
	private int testDesign;


	private String[] geneNames;
	private String[] sampleNames;
	private String methodName = null;


	// results
	String[] rownames;
	float[][] resultMatrix;
	
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
		librarySize = data.getIntArray("libSize");
		//transcriptLen = data.getIntArray("transcriptLen");
		groupAssignments = data.getIntArray("group_assignments");

		methodName = map.getString("methodName");
		testDesign = map.getInt("dataDesign");
		countMatrix = data.getIntMatrix("experiment");

		numGenes = map.getInt("numGenes");
		numExps  = map.getInt("numExps");

		progress=0;
		event = null;
		event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Running DESeq...");
		// set progress limit
		fireValueChanged(event);
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue(0);
		fireValueChanged(event);

		// Run R cmds
		runRAlg();

		AlgorithmData result = new AlgorithmData();
		result.addStringArray("rownames", rownames);
		// order of ROWS in resultMAtrix:
		// Row 1 foldChange;
		// Row 2 log2FoldChange;
		// Row 2 PValue;
		// Row 3 adjPValue;
		// Row 4 baseMean;
		// Row 5 baseMeanA;
		// Row 6 baseMeanB;
		System.out.println("resultMatrix: " + resultMatrix[0].length);
		result.addMatrix("result", new FloatMatrix(resultMatrix));
		updateProgressBar();
		return result;   
	}



	/**
	 * Function to create R session in memory and execute DESEQ
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
			//System.out.println("Testing DESEQ install");
			if (RHook.getOS() == RConstants.MAC_OS ||
					RHook.getOS() == RConstants.WINDOWS_OS) {
				RHook.testPackage("deseq");
			} else {
				RHook.installModule("DESeq");
			}

			RHook.log("Starting R Algorithim");

			String rCmd = "library(DESeq)";
			RHook.evalR(rCmd);

			String fileLoc = System.getProperty("user.dir")+System.getProperty("file.separator")+"tmpfile.txt";
			fileLoc = fileLoc.replace("\\", "/");
			//String filePath = writeMatrixToFile(fileLoc, countMatrix, geneNames, transcriptLen);
			String filePath = writeMatrixToFile(fileLoc, countMatrix, geneNames, sampleNames);

			//RHook.createRDataMatrixFromFile("raw.data", filePath, true, sampleNames);
			rCmd = "countsTable <- read.delim('" + filePath + "', header=TRUE, stringsAsFactors=TRUE)";
			RHook.evalR(rCmd);
			updateProgressBar();

			int grp1 = 0;
			int grp2 = 0;
			for(int i=0; i<this.groupAssignments.length; i++){
				if(groupAssignments[i] == 1)
					grp1++;
				if(groupAssignments[i] == 2)
					grp2++;
			}
			// assign gene names as rownames
			rCmd = "rownames( countsTable ) <- countsTable$gene";
			RHook.evalR(rCmd);
			// drop gene name col
			rCmd = "countsTable <- countsTable[ , -1 ]";
			RHook.evalR(rCmd);
			// encode condition vector
			rCmd = "conds <- c(rep(1,"+grp1+"), rep(2," +grp2+"))";
			RHook.evalR(rCmd);
			// instantiate a CountDataSet
			rCmd = "cds <- newCountDataSet( countsTable, conds )";
			RHook.evalR(rCmd);

			// LibSize vector
			if(calcLibSize) {
				rCmd = "libsizes <- c(";
				for(int i=0; i<sampleNames.length-1; i++){
					rCmd += sampleNames[i] + "=" + librarySize[i] + ",";
				}
				rCmd += sampleNames[sampleNames.length-1] + "=" + librarySize[sampleNames.length-1] + ")";
				RHook.evalR(rCmd);
				
				rCmd = "sizeFactors(cds) <- libsizes";
				RHook.evalR(rCmd);
			}
			// Recommended way to calc from data
			else {
				rCmd = "cds <- estimateSizeFactors( cds )";
				RHook.evalR(rCmd);
			}
			
			// Variance estimation
			rCmd = "cds <- estimateVarianceFunctions( cds )";
			RHook.evalR(rCmd);
			updateProgressBar();
			// TODO
			// Make available the pooled variance estimation option later
			
			// Calling differential expression
			rCmd = "res <- nbinomTest( cds, 1, 2 )";
			RHook.evalR(rCmd);
			updateProgressBar();

			// Write result file Demo only
				//fileLoc = System.getProperty("user.dir")+System.getProperty("file.separator")+"DESeq_Resfile.txt";
				//fileLoc = fileLoc.replace("\\", "/");
				//rCmd = "write.table(res,'" + fileLoc + "', row.names = FALSE)";
				//RHook.evalR(rCmd);
			// End demo code
			
			// Get result columns back to java
			REXP res = RHook.evalR("res$id");
			this.rownames = res.asStringArray();
			
			resultMatrix = new float[7][rownames.length];
			// Gt "logConc" 
			res = RHook.evalR("res$foldChange");
			double[] _tmp = res.asDoubleArray();
			resultMatrix[0] = doubletoFloat(_tmp);
			// Get "logFC"   
			res = RHook.evalR("res$log2FoldChange");
			_tmp = res.asDoubleArray();
			resultMatrix[1] = doubletoFloat(_tmp);
			// Get "PValue"  
			res = RHook.evalR("res$pval");
			_tmp = res.asDoubleArray();
			resultMatrix[2] = doubletoFloat(_tmp);
			// Get "FDR"
			res = RHook.evalR("res$padj");
			_tmp = res.asDoubleArray();
			resultMatrix[3] = doubletoFloat(_tmp);
			//
			res = RHook.evalR("res$baseMean");
			_tmp = res.asDoubleArray();
			resultMatrix[4] = doubletoFloat(_tmp);
			//
			res = RHook.evalR("res$baseMeanA");
			_tmp = res.asDoubleArray();
			resultMatrix[5] = doubletoFloat(_tmp);
			//
			res = RHook.evalR("res$baseMeanB");
			_tmp = res.asDoubleArray();
			resultMatrix[6] = doubletoFloat(_tmp);

			updateProgressBar();
			RHook.endRSession();
			
			removeTmps(filePath);
			updateProgressBar();
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

	private float[] doubletoFloat(double[] _tmp) {
		float[] tmp = new float[_tmp.length];
		for(int i=0; i<_tmp.length; i++){
			tmp[i] = (float)_tmp[i];
		}
		return tmp;
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
	private String writeMatrixToFile(String fileLoc, int[][] fm, String[] rowNames, String[] samples) throws IOException {
		//System.out.println("Matrix to write: " + Arrays.deepToString(fm));
		BufferedWriter out = new BufferedWriter(new FileWriter(fileLoc));

		int row = this.numGenes;
		int col = this.numExps;
		String srtVector = "gene" + "\t";

		if(debug) {
			System.out.println("row, col, rowName " + row);
			System.out.println("row, col, rowName " + col);
			System.out.println("row, col, rowName " + rowNames[1]);
		}

		//write sample names
		int iRow;
		for(iRow = 0; iRow < samples.length-1; iRow++) {
			srtVector += samples[iRow]+ "\t";
		}
		out.write(srtVector + samples[samples.length-1] + "\n");

		srtVector = "";
		for(iRow = 0; iRow < row; iRow++) {
			srtVector = rowNames[iRow] + "\t";
			for(int jCol = 0; jCol < col; jCol++) {
				if(jCol == col-1)
					srtVector += fm[iRow][jCol] + "\n";
				else 
					srtVector += fm[iRow][jCol] + "\t";
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
	 * main() test
	 * @param args
	 */
	public static void main(String[] args){
		int[]array = {0,1};
		for (int i=0; i<array.length;i++){
			System.out.print(array[i]+"\t");
		}
		System.out.println(":"+array.length);
		//int[] result = getPermutedValues(array.length, array);
		//for (int i=0; i<result.length;i++){
		//System.out.print(result[i]+"\t");
		//}
	}
}