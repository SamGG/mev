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
import org.tigr.microarray.mev.cluster.gui.impl.edger.EDGERInitBox;
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

public class EDGER extends AbstractAlgorithm {
	public static final int FALSE_NUM = 12;
	public static final int FALSE_PROP = 13;  

	private int progress;
	private int[][] countMatrix;
	//private FloatMatrix experimentData;

	private boolean stop = false;
	private int[] groupAssignments;
	private int[] librarySize;
	private int[] transcriptLen;
	
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
		transcriptLen = data.getIntArray("transcriptLen");
		groupAssignments = data.getIntArray("group_assignments");
		
		methodName = map.getString("methodName");
		testDesign = map.getInt("dataDesign");
		countMatrix = data.getIntMatrix("experiment");
		
		numGenes = map.getInt("numGenes");
		numExps  = map.getInt("numExps");

		progress=0;
		event = null;
		event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Running edgeR...");
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
		// Row 1 logConc;
		// Row 2 logFC;
		// Row 2 PValue;
		// Row 3 FDR;
		System.out.println("resultMatrix: " + resultMatrix[0].length);
		result.addMatrix("result", new FloatMatrix(resultMatrix));
		
		// end debug
		updateProgressBar();
		return result;   
	}

	

	/**
	 * Function to create R session in memory and execute EDGER
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
			//System.out.println("Testing EDGER install");
			RHook.testPackage("edger");
			
			RHook.log("Starting R Algorithim");

			String rCmd = "library(edgeR)";
			RHook.evalR(rCmd);
			
			updateProgressBar();
			
			String fileLoc = System.getProperty("user.dir")+System.getProperty("file.separator")+"tmpfile.txt";
			fileLoc = fileLoc.replace("\\", "/");
			String filePath = writeMatrixToFile(fileLoc, countMatrix, geneNames, transcriptLen);
			
			//RHook.createRDataMatrixFromFile("raw.data", filePath, true, sampleNames);
			rCmd = "raw.data <- read.delim('" + filePath + "', header=TRUE)";
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
			int cols = this.numExps+1;
			rCmd = "d <- raw.data[, 2:"+cols+"]";
			RHook.evalR(rCmd);
			rCmd = "rownames(d) <- raw.data[, 1]";
			RHook.evalR(rCmd);
			rCmd = "group <- c(rep(1,"+grp1+"), rep(2," +grp2+"))";
			RHook.evalR(rCmd);
			rCmd = "d <- DGEList(counts = d, group = group)";
			RHook.evalR(rCmd);
			
			rCmd = "names(d)";
			RHook.evalR(rCmd);
			
			rCmd = "d <- estimateCommonDisp(d)";
			RHook.evalR(rCmd);
			
			if(this.methodName.equalsIgnoreCase("Common dispersion")) {				
				rCmd = "de <- exactTest(d)";
				RHook.evalR(rCmd);
			}
			// tag wise dispersion method
			else {
				rCmd = "d <- estimateTagwiseDisp(d, prior.n = 10, grid.length = 500)";
				RHook.evalR(rCmd);
				
				rCmd = "de <- exactTest(d, common.disp = FALSE)";
				RHook.evalR(rCmd);
			}
			
			rCmd = "res <- topTags(de, n=" +this.numGenes+")$table";
			REXP res = RHook.evalR(rCmd);
			updateProgressBar();
			
			// Get result columns back to java
			res = RHook.evalR("rownames(res)");
			this.rownames = res.asStringArray();
			//System.out.println("rownames(res) len & vals " + this.rownames.length + " " + Arrays.toString(this.rownames));
			
			resultMatrix = new float[4][rownames.length];
			
			// Get "logConc" 
			res = RHook.evalR("res$logConc");
			double[] _tmp = res.asDoubleArray();
			System.out.println("res$logConc length " + _tmp.length);
			this.resultMatrix[0] = doubletoFloat(_tmp);
			// Get "logFC"   
			res = RHook.evalR("res$logFC");
			_tmp = res.asDoubleArray();
			this.resultMatrix[1] = doubletoFloat(_tmp);
			// Get "PValue"  
			res = RHook.evalR("res$PValue");
			_tmp = res.asDoubleArray();
			this.resultMatrix[2] = doubletoFloat(_tmp);
			// Get "FDR"
			res = RHook.evalR("res$FDR");
			_tmp = res.asDoubleArray();
			this.resultMatrix[3] = doubletoFloat(_tmp);
			
			// Write result file 
			// Demo only
			/*
				fileLoc = System.getProperty("user.dir")+System.getProperty("file.separator")+"edgeR_Resfile.txt";
				fileLoc = fileLoc.replace("\\", "/");
				BufferedWriter out = new BufferedWriter(new FileWriter(fileLoc));
				out.write("geneIndex\tlogConc\tlogFC\tPValue\tFDR\n");
				for(int iRow = 0; iRow < this.rownames.length; iRow++) {
					out.write(
							this.rownames[iRow] + "\t" + 
							this.resultMatrix[0][iRow] + "\t" + 
							this.resultMatrix[1][iRow] + "\t" + 
							this.resultMatrix[2][iRow] + "\t" + 
							this.resultMatrix[3][iRow] + "\n"
						);
				}
				out.close();
			*/
			// End demo code
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
	private String writeMatrixToFile(String fileLoc, int[][] fm, String[] rowNames, int[] transLen) throws IOException {
		//System.out.println("Matrix to write: " + Arrays.deepToString(fm));
		BufferedWriter out = new BufferedWriter(new FileWriter(fileLoc));

		int row = this.numGenes;
		int col = this.numExps;
		String srtVector = "";

		if(debug) {
			System.out.println("row, col, rowName " + row);
			System.out.println("row, col, rowName " + col);
			System.out.println("row, col, rowName " + rowNames[1]);
		}
		
		//Write Sample Names
		srtVector = "Index\t";
		for(int iSam = 0; iSam < this.sampleNames.length; iSam++){
			if(iSam == col-1)
				srtVector += this.sampleNames[iSam] + "\n";
			else 
				srtVector += this.sampleNames[iSam] + "\t";
		}
		out.write(srtVector);
		
		//Write data
		srtVector = "";
		for(int iRow = 0; iRow < row; iRow++) {
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
		//int[] result = getPermutedValues(array.length, array);
		//for (int i=0; i<result.length;i++){
			//System.out.print(result[i]+"\t");
		//}
	}
}