/* This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/* Boostrap.java
 * Copyright (C) 2005 Amira Djebbari
 */
//package edu.harvard.dfci.compbio.bnUsingLit.prepareArrayData.bootstrap;
package org.tigr.microarray.mev.cluster.gui.impl.bn;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Random;
/**
 * The class <code>Bootstrap</code> is given a dataset in CSV format, creates a given number of bootstrapped datasets
 * in CSV format as well
 *
 * @author <a href="mailto:amirad@jimmy.harvard.edu"></a>
 */
public class Bootstrap {
    /**
     * The variable <code>MAX_ITERATIONS</code> corresponds to the max number of iterations for bootstrap
     */
    public static int MAX_ITERATIONS = 500; 
    /**
     * The <code>readDataSet</code> method is given an input file name containing the original dataset in CSV format
     * and returns a 2 D array of Strings representation of the given dataset
     *
     * @param dataSetFileName a <code>String</code> denoting the name of the input file containing the original dataset
     * in CSV format
     * @return a <code>String[][]</code> corresponding to a 2D array of <code>String</code>s representation 
     * of the given dataset
     */
    public static String[][] readDataSet(String dataSetFileName){
		// initialize 2D array that will contain the dataset of size max iterations in either dimension
		String[][] dataSet = new String[MAX_ITERATIONS][MAX_ITERATIONS];
		for(int i = 0; i < MAX_ITERATIONS; i++){
		    for(int j = 0; j < MAX_ITERATIONS; j++){
		    	dataSet[i][j] = null;
		    }
		}
		int trueSizeX = 0;
		try {
		    // read the given input file
		    FileReader fr = new FileReader(dataSetFileName);
		    LineNumberReader lnr = new LineNumberReader(fr);
		    String s = null;
		    int count = 0;	    
		    while((s = lnr.readLine())!= null){
		    	s = s.trim();
		        dataSet[count] = s.split(",");
		        trueSizeX = dataSet[count].length;
		        count++;
		    }
		    String[][] result = new String[count][trueSizeX];
		    for(int i = 0; i < count; i++){
		    	for(int j = 0; j < trueSizeX; j++){		    
		    		result[i][j] = dataSet[i][j];
		    	}
		    }     
		    return result;
		}
		catch(IOException ioe){
		    System.out.println(ioe);
		}
		return null;
    }

    /**
     * The <code>printDataSet</code> method is given a PrintWriter and a 2D array of Strings representation of a dataset
     * and prints the dataset to the given PrintWriter in csv format
     *
     * @param pw a <code>PrintWriter</code> corresponding to a PrintWriter where the dataset is to be written
     * @param dataSet a <code>String[][]</code> corresponding to a 2D array of Strings representation of a dataset
     */
    public static void printDataSet(PrintWriter pw, String[][] dataSet){
		for(int i = 0; i < dataSet.length; i++){
		    for(int j = 0; j < dataSet[i].length-1; j++){
		    	pw.print(dataSet[i][j]+",");
		    }
		    pw.print(dataSet[i][(dataSet[i].length-1)]);
		    pw.println();
		}
    }

    /**
     * The <code>createDataSets</code> method is given the original dataset file name, the root output file name, 
     * and the number of iterations for which to run the boostrap and creates a number of datasets (number of iterations) 
     * by resampling the original dataset with replacement and writes each of them to the given root output file name
     * appended with the iteration number in csv format
     * @param origDataSetFileName a <code>String</code> corresponding to the name of the file containing the original dataset in CSV format
     * @param rootOutFileName a <code>String</code> corresponding to the root name of the file where each bootstrapped
     * dataset iteration is to be written. For example, for iteration 0, the bootstrapped dataset will be written 
     * in rootOutFileName0
     * @param numIterations an <code>int</code> corresponding the number of iterations for which the bootstrap
     * will be performed by resampling with replacement instances from the original dataset.
     * @param seed a <code>long</code> corresponding to the seed of the random number generator 
     * used to randomly resample the original dataset with replacement
     */
    public static void createDataSets(String origDataSetFileName, String rootOutFileName, int numIterations, long seed){
		try { 
		    String[][] origDataSet = readDataSet(origDataSetFileName);
		    String[][] dataSet_i = null;
		    FileOutputStream fos = null;
		    PrintWriter pw = null;
		    Random rand = new Random(seed);
		    for(int i = 0; i < numIterations; i++){
			fos = new FileOutputStream(rootOutFileName+i);
			pw = new PrintWriter(fos, true);
			// Construct a dataSet_i by resampling with replacement instances from origDataSet
			dataSet_i = reSampleDataSet(rand, origDataSet);
			printDataSet(pw, dataSet_i);
			fos.close();
			pw.close();
		    }
		}
		catch(IOException ioe){
		    System.out.println(ioe);
		}
    }

    /**
     * The <code>reSampleDataSet</code> method is given an instance of Random number generator
     * and the 2D array of Strings representation of the original dataset
     * and returns a bootstrapped dataset by resampling with replacement the original dataset
     *
     * @param rand a <code>Random</code> corresponding to a random number generator
     * @param origDataSet a <code>String[][]</code> corresponding to the given original dataset
     * @return a <code>String[][]</code> corresponding to a bootstrapped dataset obtained by
     * resampling with replacement the original dataset
     */
    public static String[][] reSampleDataSet(Random rand, String[][] origDataSet){
		// Initialize dataset to original dataset
		String[][] dataSet = new String[origDataSet.length][origDataSet[0].length];
		int randomNumber = 0;
		for(int k = 0; k < dataSet[0].length; k++){
		    dataSet[0][k] = origDataSet[0][k];
		}
		// resample with replacement original dataset
		for(int i = 1; i < dataSet.length; i++) {
		    randomNumber = rand.nextInt(origDataSet.length-1);
		    randomNumber++;
		    for(int j = 0; j < dataSet[1].length; j++) {
			dataSet[i][j] = origDataSet[randomNumber][j];
		    }
		}
		return dataSet;
    }

    /**
     * The <code>usage</code> method displays the usage.
     *
     */
    public static void usage(){
		System.out.println("Usage: java Bootstrap dataCSVFileName rootOutFileName numBootstrapIterations seed\nExample: java Bootstrap data.csv bootstrap/boot 100 1");
		System.exit(0);
    }
	
    public static void main(String[] argv){
		if(argv.length != 4){
		    usage();
		}
		String origDataSetFileName = argv[0];
		String rootOutFileName = argv[1];
		int numBootstrapIterations = Integer.parseInt(argv[2]);
		long seed = (long)Integer.parseInt(argv[3]);
		createDataSets(origDataSetFileName, rootOutFileName, numBootstrapIterations, seed);
    }
}







