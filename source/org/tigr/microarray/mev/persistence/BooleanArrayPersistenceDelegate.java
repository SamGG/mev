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


public class BooleanArrayPersistenceDelegate extends PersistenceDelegate {
	
	public Expression instantiate(Object oldInstance, Encoder encoder) {
		BooleanArray fm = (BooleanArray) oldInstance;
		try {
			File outputFile = File.createTempFile("booleanarray", ".bin", new File(MultipleArrayViewer.CURRENT_TEMP_DIR));
	        outputFile.deleteOnExit();
	        writeBooleanArray(outputFile, fm);
	        return new Expression((BooleanArray) oldInstance, this.getClass(), "readBooleanArray",
					new Object[]{outputFile.getName()});
		} catch (IOException ioe){
			System.out.println("Can't write to file to save FloatMatrix");
			return null;
		}
	}
	/**
	 * Reads a binary file written by writeMatrix(File outputFile, FloatMatrix fm) and
	 * returns a FloatMatrix object created from the data in outputFile.  
	 * 
	 * @param inputFile The name of the file to be read from. Assumed to be found in the System
	 *   temp directory in a folder named by MultipleArrayViewer.CURRENT_TEMP_DIR
	 * @return a new FloatMatrix containing the data from inputFile
	 * @throws IOException
	 */
	public static BooleanArray readBooleanArray(String inputFile) throws IOException{
		File binFile = new File(MultipleArrayViewer.CURRENT_TEMP_DIR , inputFile);
		BooleanArray fm = new BooleanArray(readArray(binFile));
		return fm;
	}
	
    private static boolean[] readArray(File binFile) throws IOException{

    	DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(binFile)));
		boolean[] matrix = new boolean[1];
		try {
			int numRows;
			numRows = dis.readInt();
			matrix = new boolean[numRows];
			for(int i=0; i<numRows; i++){
					matrix[i] = dis.readBoolean();
			}    	
		} catch (IOException e) {
			System.out.println("Couldn't read booleanarray from file " + binFile.getName());
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
    public static void writeBooleanArray(File outputFile, BooleanArray fm) throws IOException {
    	
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        try {
        	boolean[] array = fm.toArray();
			int numRows = array.length;
			dos.writeInt(numRows);
			for(int i=0; i<numRows; i++){
				dos.writeBoolean(array[i]);
			}
    	} catch (Exception e) {
    		System.out.println("Error in writing BooleanArray "+ outputFile.getName());
    		e.printStackTrace();

    	} finally {
    		dos.flush();
    		dos.close();
    	}
    }
}










































 