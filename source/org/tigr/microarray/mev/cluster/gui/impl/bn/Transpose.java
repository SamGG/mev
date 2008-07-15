/* This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/* Transpose.java
 * Copyright (C) 2005 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn;import java.io.*;

import org.tigr.microarray.mev.TMEV;
/**
 * The class <code>Transpose</code>  contains methods to transpose takes in a tab delimited file
 * and outputs the transpose of it
 *
 * @author <a href="mailto:amirad@jimmy.harvard.edu"></a>
 * modified by Raktim
 */
public class Transpose {
	/**
	 * The variable <code>MAX_LENGTH</code> corresponds to the max length of matrix in either dimension
	 */
	//public static int MAX_LENGTH = 300; 

	/**
	 * The <code>initializeMatrix</code> method returns a 2D array of Strings of size max length 
	 * in either dimension and initializes each element in the array to the null string
	 *
	 * @return a <code>String[][]</code> of size max length in either dimension 
	 * and with each element in the array initialized to the null string
	 */
	public static String[][] initializeMatrix(){
		String[][] matrix = new String[BNConstants.MAX_GENES][BNConstants.MAX_GENES];
		for(int i = 0; i < BNConstants.MAX_GENES; i++){
			for(int j = 0; j < BNConstants.MAX_GENES; j++){
				matrix[i][j] = null;
			}
		}
		return matrix;
	}


	/**
	 * The <code>readMatrix</code> method is given an input file name containing Strings in a tab delimited format
	 * and returns a 2D String array containing the Strings in the input file name in tab delimited format
	 * @param inFileName a <code>String</code> denoting the name of the input file containing Strings
	 * in a tab delimited format
	 * @return a <code>String[][]</code>  containing the <code>String</code>s in the input file name in tab delimited format
	 */
	public static String[][] readMatrix(String inFileName){
		try {
			/*
		String dataPath = TMEV.getDataPath();
    	File pathFile = TMEV.getFile("data/");
    	if(dataPath != null) {
            pathFile = new File(dataPath);
            if(!pathFile.exists())
                pathFile = TMEV.getFile("data/");
        }
			 */
			FileReader fr = new FileReader(new File(inFileName));
			LineNumberReader lnr = new LineNumberReader(fr);
			String s = null;
			String[] tokens = null;	    
			int i = 0;
			String[][] result = initializeMatrix();
			while((s = lnr.readLine())!=null){
				s = s.trim();
				tokens = s.split("\t");
				for(int j = 0; j < tokens.length; j++){
					result[i][j] = new String(tokens[j]);
				}
				i++;
			}
			lnr.close();
			fr.close();
			return result;
		}
		catch(IOException ioe){
			System.out.println(ioe);
		}
		return null;
	}


	/**
	 * The <code>writeMatrix</code> method is given a 2D array of Strings and an output file name
	 * and writes the matrix to the output file name in tab delimited format
	 *
	 * @param matrix a <code>String[][]</code> corresponding to the given data
	 * @param outFileName a <code>String</code> denoting the name of the output file where the matrix
	 * is written in tab delimited format
	 */
	public static void writeMatrix(String[][] matrix, String outFileName){
		try {
			FileOutputStream fos = new FileOutputStream(new File(outFileName));
			PrintWriter pw = new PrintWriter(fos, true);
			boolean toggle = false;
			for(int i = 0; i < BNConstants.MAX_GENES; i++){
				toggle = false;
				for(int j = 0; j < BNConstants.MAX_GENES; j++){
					if(matrix[i][j] != null){
						toggle = true;
						if(j == 0){
							pw.print(matrix[i][j]);
						}
						else {
							pw.print("\t"+matrix[i][j]);
						}
					}
				}
				if(toggle){
					pw.println();
				}
			}
		}
		catch(IOException ioe){
			System.out.println(ioe);
		}
	}

	/**
	 * The <code>transposeMatrix</code> method is given a 2D array of Strings
	 * returns a new 2D array of Strings that is the transpose of the given array
	 *
	 * @param matrix a <code>String[][]</code> corresponding to the given data
	 * @return a <code>String[][]</code> which is a new 2D array of Strings that is the transpose of the given array
	 */
	public static String[][] transposeMatrix(String[][] matrix){
		String[][] transposedMatrix = initializeMatrix();
		for(int i = 0; i < BNConstants.MAX_GENES; i++){
			for(int j = 0; j < BNConstants.MAX_GENES; j++){
				transposedMatrix[i][j] = matrix[j][i];
			}
		}
		return transposedMatrix;
	}

	/**
	 * The <code>readAndWriteTranspose</code> method is given an input file name containing Strings in a tab delimited format
	 * writes to the output file name the transpose of the 2D array contained in the input file in tab delimited format
	 * @param inFileName a <code>String</code> denoting the name of the input file containing Strings 
	 * in a tab delimited format
	 * @param outFileName a <code>String</code> denoting the name of the output file where the transpose of the 2D array
	 * contained in the input file in tab delimited format
	 */
	public static void readAndWriteTranspose(String inFileName, String outFileName){
		String[][] matrix = readMatrix(inFileName);
		String[][] transposedMatrix = transposeMatrix(matrix);
		writeMatrix(transposedMatrix,outFileName);
	}

	/**
	 * The <code>usage</code> method displays the usage.
	 *
	 */
	public static void usage(){
		System.out.println("Usage: java Transpose inFileNameTabDelimited outFileNameTabDelimited\nExample: java Transpose expr_data_orig expr_data_transposed");
		System.exit(0);	
	}

	/**
	 * The <code>printMatrix</code> method is given a 2D array of Strings 
	 * and displays the contents of the given 2D array of Strings to the standard output
	 *
	 * @param matrix a <code>String[][]</code> corresponding to the given data
	 */

	public static void printMatrix(String[][] matrix) {
		boolean toggle = false;
		for(int i = 0; i < BNConstants.MAX_GENES; i++){
			toggle = false;
			for(int j = 0; j < BNConstants.MAX_GENES; j++) {
				if(matrix[i][j]!=null){
					toggle = true;
					System.out.print(matrix[i][j]+"\t");
				}		
			}
			if(toggle){
				System.out.println();
			}
		}
	}

	/**
	 * The <code>test</code> method tests the <code>readMatrix</code>, <code>writeMatrix</code>
	 * and the <code>transposeMatrix</code> methods
	 *
	 * @param inFileName a <code>String</code> corresponding to the name of the input file containing 
	 * the given data in tab-delimited format
	 * @param outFileName a <code>String</code> corresponding to the name of the output file
	 * where the transpose of the given data is to be written in tab-delimited format
	 */
	public static void test(String inFileName, String outFileName){
		String[][] matrix = readMatrix(inFileName);
		System.out.println("test read");
		//printMatrix(matrix);
		System.out.println("test write");
		writeMatrix(matrix, "another_"+inFileName);
		System.out.println("test tranpose");
		String[][] transposedMatrix = transposeMatrix(matrix);
		//printMatrix(transposedMatrix);
		System.out.println("test read and write transpose");
	}
}
