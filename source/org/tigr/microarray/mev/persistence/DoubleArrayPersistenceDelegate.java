/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.persistence;

import java.beans.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.util.BooleanArray;
import org.tigr.util.DoubleArray;


public class DoubleArrayPersistenceDelegate extends PersistenceDelegate {
	
	public Expression instantiate(Object oldInstance, Encoder encoder) {
		DoubleArray fm = (DoubleArray) oldInstance;
		try {
			File outputFile = File.createTempFile("doublearray", ".bin", new File(MultipleArrayViewer.CURRENT_TEMP_DIR));
	        outputFile.deleteOnExit();
	        writeDoubleArray(outputFile, fm);
	        return new Expression((DoubleArray) oldInstance, this.getClass(), "readDoubleArray",
					new Object[]{outputFile.getName()});
		} catch (IOException ioe){
			System.out.println("Can't write to file to save DoubleArray");
			return null;
		}
	}
	/**
	 * Reads a binary file written by writeArray(File outputFile, DoubleArray fm) and
	 * returns a DoubleArray object writeDoubleArray from the data in outputFile.  
	 * 
	 * @param inputFile The name of the file to be read from. Assumed to be found in the System
	 *   temp directory in a folder named by MultipleArrayViewer.CURRENT_TEMP_DIR
	 * @return a new FloatMatrix containing the data from inputFile
	 * @throws IOException
	 */
	public static DoubleArray readDoubleArray(String inputFile) throws IOException{
		File binFile = new File(MultipleArrayViewer.CURRENT_TEMP_DIR , inputFile);
		DoubleArray fm = new DoubleArray(readArray(binFile));
		return fm;
	}
	
    private static double[] readArray(File binFile) throws IOException{

    	DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(binFile)));
    	double[] matrix = new double[1];
		try {
			int numRows;
			numRows = dis.readInt();
			matrix = new double[numRows];
			for(int i=0; i<numRows; i++){
				matrix[i] = dis.readDouble();
			}    	
		} catch (IOException e) {
			System.out.println("Couldn't read doublearray from file " + binFile.getName());
			e.printStackTrace();
		} finally {
			dis.close();
		}
    	return matrix;
    }
    
	/**
	 * Writes a BooleanArray out to a binary file specified by the name outputFile.  
	 * @param outputFile This file is assumed to be located in the System temp directory
	 *   in the folder MultipleArrayViewer.CURRENT_TEMP_DIR
	 * @param fm the BooleanArray to be written to file
	 * @throws IOException
	 */
    public static void writeDoubleArray(File outputFile, DoubleArray fm) throws IOException {
    	
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        try {
        	double[] array = fm.toArray();
			int numRows = array.length;
			dos.writeInt(numRows);
			for(int i=0; i<numRows; i++){
				dos.writeDouble(array[i]);
			}
    	} catch (Exception e) {
    		System.out.println("Error in writing DoubleArray "+ outputFile.getName());
    		e.printStackTrace();

    	} finally {
    		dos.flush();
    		dos.close();
    	}
    }
}










































 