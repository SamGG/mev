/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.persistence;

import java.beans.*;
import java.io.File;
import java.io.IOException;

import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.util.FloatMatrix;


public class FloatMatrixPersistenceDelegate extends PersistenceDelegate {
	
	public Expression instantiate(Object oldInstance, Encoder encoder) {
		FloatMatrix fm = (FloatMatrix) oldInstance;
		try {
			File outputFile = File.createTempFile("floatmatrix", ".bin", new File(MultipleArrayViewer.CURRENT_TEMP_DIR));
	        outputFile.deleteOnExit();
	        PersistenceObjectFactory.writeMatrix(outputFile, fm);
			return new Expression((FloatMatrix) oldInstance, new PersistenceObjectFactory().getClass(), "readFloatMatrix",
					new Object[]{outputFile.getName()});
		} catch (IOException ioe){
			System.out.println("Can't write to file to save FloatMatrix");
			return null;
		}
	}

}










































 