/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.persistence;

import java.beans.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;


import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.cluster.gui.impl.ease.EASETableViewer;


public class EASETableViewerPersistenceDelegate extends PersistenceDelegate {
	protected Expression instantiate(Object oldInstance, Encoder encoder) {
		Expression e;
		try {
			EASETableViewer etv = (EASETableViewer) oldInstance;
			
			File outputFile = File.createTempFile("easetableviewer", ".txt", new File(MultipleArrayViewer.CURRENT_TEMP_DIR));
	        outputFile.deleteOnExit();
	        PrintWriter pw = new PrintWriter(new FileOutputStream(outputFile));
	        e = new Expression((EASETableViewer) oldInstance, new PersistenceObjectFactory().getClass(), "makeEASETableViewer",
		    		etv.getExpression(pw, outputFile.getName()).getArguments()
		    		
		    	);
		    	pw.close();

		} catch (Exception ioe){
			ioe.printStackTrace();
			System.out.println("Can't write to file to save EASETableViewer");
			return null;
		}
		return e;
	}
	public void initialize(Class type, Object oldInstance, Object newInstance, Encoder encoder) {
		return;
	}
}


