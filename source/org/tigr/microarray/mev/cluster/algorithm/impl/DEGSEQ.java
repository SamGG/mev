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
import org.tigr.microarray.mev.cluster.gui.impl.deseq.DESEQInitBox;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.file.StringSplitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

import javax.swing.JOptionPane;

public class DEGSEQ extends AbstractAlgorithm {
	public static final int FALSE_NUM = 12;
	public static final int FALSE_PROP = 13;  

	private int progress;
	private int[][] countMatrix;
	//private FloatMatrix experimentData;

	private boolean stop = false;
	private int[] groupAssignments;
	private String grp1ColIndStr, grp2ColIndStr;
	private int grp1, grp2;
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
		groupAssignments = data.getIntArray("group_assignments");
		grp1ColIndStr = map.getString("grp1ColIndStr");
		grp2ColIndStr = map.getString("grp2ColIndStr");
		grp1 = map.getInt("grp1") + 1; //adj for gene col
		grp2 = map.getInt("grp2") + 1;
		methodName = map.getString("infMethod");
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
		// order of Cols in resultMAtrix:
		// Col 1 log2(Fold_change);
		// Col 2 log2(Fold_change) normalized;
		// Col 3 z-score;
		// Col 4 p-value;
		// Col 5 q-value(Benjamini et al. 1995);
		// Col 6 q-value(Storey et al. 2003);
		System.out.println("resultMatrix: " + resultMatrix[0].length);
		result.addMatrix("result", new FloatMatrix(resultMatrix));
		updateProgressBar();
		return result;   
	}



	/**
	 * Function to create R session in memory and execute DEGSEQ
	 * @throws AbortException 
	 */
	public void runRAlg() throws AbortException {
		progress++;
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue(10);
		fireValueChanged(event);

		try {
			if(RHook.startRSession() == null) {
				JOptionPane.showMessageDialog(null, "Error creating R Engine",  "REngine", JOptionPane.ERROR_MESSAGE);
				throw new AbortException();
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "REngine", JOptionPane.ERROR_MESSAGE);
			throw new AbortException();
		}
		updateProgressBar();

		try {
			//System.out.println("Testing DEGSEQ install");
			RHook.testPackage("degseq");

			RHook.log("Starting R Algorithim");

			String rCmd = "library(DEGseq)";
			RHook.evalR(rCmd);

			String geneExpFile  = System.getProperty("user.dir")+System.getProperty("file.separator")+"geneExpFile.txt";
			geneExpFile = geneExpFile.replace("\\", "/");
			//String filePath = writeMatrixToFile(fileLoc, countMatrix, geneNames, transcriptLen);
			String filePath = writeMatrixToFile(geneExpFile, countMatrix, geneNames, sampleNames);

			String outDir = System.getProperty("user.dir");
			outDir = outDir.replace("\\", "/");

			rCmd = "geneExpMatrix1 <- readGeneExp(" +
			"file='"+geneExpFile+"', geneCol=1, " +
			"valCol=c("+grp1ColIndStr+"))";
			RHook.evalR(rCmd);

			rCmd = "geneExpMatrix2 <- readGeneExp(" +
			"file='"+geneExpFile+"', geneCol=1, " +
			"valCol=c("+grp2ColIndStr+"))";
			RHook.evalR(rCmd);
			updateProgressBar();

			if (methodName.equals("LRT") || methodName.equals("FET") || methodName.equals("MARS")) {
				rCmd = "DEGexp(" +
				"geneExpMatrix1=geneExpMatrix1, " +
				"geneCol1=1, " +
				"expCol1=c(seq(from=2,to="+grp1+")), " +
				"groupLabel1='Grp1'," +
				"geneExpMatrix2=geneExpMatrix2, " +
				"geneCol2=1, " +
				"expCol2=c(seq(from=2,to="+grp2+")), " +
				"groupLabel2='Grp2'," +
				"method='"+methodName+"', " + 
				"outputDir='"+outDir+"')";
			} else {
				// MATR grp1 and grp2 are treated as technical replicates
				rCmd = "DEGexp(" +
				"geneExpMatrix1=geneExpMatrix1, " +
				"geneCol1=1, " +
				//"expCol1=c(seq(from=2,to="+grp1+")), " +
				"expCol1=c(2), " +
				"groupLabel1='Grp1'," +
				"geneExpMatrix2=geneExpMatrix2, " +
				"geneCol2=1, " +
				//"expCol2=c(seq(from=2,to="+grp2+")), " +
				"expCol2=c(2), " +
				"groupLabel2='Grp2'," +
				"method='"+methodName+"', " + 
				"replicateExpMatrix1=geneExpMatrix1, " +
				"geneColR1=1, " +
				"expColR1=c(seq(from=3,to="+grp1+")), " +
				"replicateExpMatrix2=geneExpMatrix2, " +
				"geneColR2=1, " +
				"expColR2=c(seq(from=3,to="+grp2+")), " +
				"outputDir='"+outDir+"')";
			}
			RHook.evalR(rCmd);
			updateProgressBar();

			String resFile = processResults(outDir);
			updateProgressBar();

			RHook.endRSession();

			removeTmps(filePath);
			removeTmps(resFile);

			updateProgressBar();
		} catch (Exception e) {
			RHook.log(e);
			try {
				RHook.endRSession();
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(), "REngine", JOptionPane.ERROR_MESSAGE);
				//throw new AlgorithmException(e.getMessage());
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

	// Output File Name, Col and order 
	// #0 GeneNames (Gene Index in our case)
	// #1 value1		Unused
	// #2 value2		Unused
	// #3 log2(Fold_change)
	// #4 log2(Fold_change) normalized
	// #5 z-score
	// #6 p-value
	// #7 q-value(Benjamini et al. 1995)
	// #8 q-value(Storey et al. 2003)
	// #9 Signature(p-value < 0.001)		Unused
	private String processResults(String outDir) throws IOException {
		String outFile = outDir + "/output_score.txt";

		// Read result into data structures
		this.rownames = new String[countMatrix.length];
		this.resultMatrix = new float[rownames.length][6];

		int col = 0;
		int counter = 0;
		BufferedReader reader = new BufferedReader(new FileReader(outFile));
		StringSplitter ss = new StringSplitter((char) 0x09);
		String currentLine;

		reader.readLine(); //skip header
		while ((currentLine = reader.readLine()) != null) {
			ss.init(currentLine);
			//System.out.println("Token Count " + ss.countTokens());
			col = 0;
			for(int token = 0; token < ss.countTokens(); token++) {
				if(token == 0) {
					this.rownames[counter] = ss.nextToken();
					continue;
				}
				if (token == 1 || token == 2 || token == 9) {
					ss.nextToken();
					continue;
				}
				resultMatrix[counter][col++] = ss.nextFloatToken(Float.NaN);
			}
			counter++;
		}
		reader.close();
		return outFile;
	}

	/**
	 * 
	 * @param fileName
	 */
	private void removeTmps(String fileName) {
		if (fileName != null){
			File f = new File(fileName);
			if(f.exists())
				f.delete();
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