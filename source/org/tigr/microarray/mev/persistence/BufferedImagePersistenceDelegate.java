/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.persistence;

import java.awt.image.BufferedImage;
import java.beans.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.tigr.microarray.mev.MultipleArrayViewer;


public class BufferedImagePersistenceDelegate extends PersistenceDelegate {
	
	public Expression instantiate(Object oldInstance, Encoder encoder) {
		BufferedImageWrapper biw = (BufferedImageWrapper) oldInstance;
		BufferedImage bi = biw.getBufferedImage();
		try {
			File outputFile = File.createTempFile("bufferedimage", ".jpg", new File(MultipleArrayViewer.CURRENT_TEMP_DIR));
	        outputFile.deleteOnExit();
	        DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile));
	        PersistenceObjectFactory.writeBufferedImage(dos, bi);
	        dos.close();
			return new Expression((BufferedImageWrapper) oldInstance, new PersistenceObjectFactory().getClass(), "readBufferedImage",
					new Object[]{outputFile.getName()});
		} catch (IOException ioe){
			System.out.println("Can't write to file to save BufferedImage");
			return null;
		}
	}

	public void initialize(Class type, Object oldInstance, Object newInstance, XMLEncoder encoder){
		;
	}
}










































 