/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Jul 11, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigr.microarray.mev.persistence;

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import org.tigr.microarray.mev.AffySlideDataElement;
import org.tigr.microarray.mev.FloatSlideData;
import org.tigr.microarray.mev.ISlideDataElement;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.annotation.AnnotationStateSavingParser;
import org.tigr.microarray.mev.annotation.IAnnotation;

/**
 * @author eleanora
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ISlideDataPersistenceDelegate extends
		PersistenceDelegate {

	/* (non-Javadoc)
	 * @see java.beans.PersistenceDelegate#instantiate(java.lang.Object, java.beans.Encoder)
	 */
	protected Expression instantiate(Object oldInstance, Encoder encoder) {
		Expression e;
		try {
			if(oldInstance instanceof FloatSlideData){
				FloatSlideData fsd = (FloatSlideData) oldInstance;
				
				File outputFile = File.createTempFile("floatslidedata", ".bin", new File(MultipleArrayViewer.CURRENT_TEMP_DIR));
		        outputFile.deleteOnExit();
		        DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile));
		        PersistenceObjectFactory.writeFloatSlideDataIntensities(dos, fsd);
		        dos.close();
		        
				e = new Expression((FloatSlideData) oldInstance, new PersistenceObjectFactory().getClass(), "makeFloatSlideData",
						new Object[]{fsd.getSlideDataKeys(), fsd.getSlideDataLabels(), fsd.getFullSlideFileName(), 
							fsd.getSlideDataName(), new Boolean(fsd.getIsNonZero()), new Integer(fsd.getNormalizedState()), 
							new Integer(fsd.getSortState()), fsd.getSpotInformationData(), new Integer(fsd.getDataType()), fsd.getSlideMetaData(),
							outputFile.getName(), fsd.getSampleAnnotation()});
			} else {
				SlideData sd = (SlideData) oldInstance;
				
				File annotationFile = File.createTempFile("slidedataannotation", ".bin", new File(MultipleArrayViewer.CURRENT_TEMP_DIR));
				annotationFile.deleteOnExit();
				DataOutputStream dos = new DataOutputStream(new FileOutputStream(annotationFile));
				PersistenceObjectFactory.writeSlideDataAnnotation(dos, sd);
				dos.close();

				File iAnnotationFile = File.createTempFile("iannotation", ".txt", new File(MultipleArrayViewer.CURRENT_TEMP_DIR));
				annotationFile.deleteOnExit();
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(iAnnotationFile)));
							
				PersistenceObjectFactory.writeSlideDataIAnnotation(pw, sd);
				pw.close();
				

				File outputFile = File.createTempFile("slidedata", ".bin", new File(MultipleArrayViewer.CURRENT_TEMP_DIR));
				outputFile.deleteOnExit();
				dos = new DataOutputStream(new FileOutputStream(outputFile));
				PersistenceObjectFactory.writeSlideDataIntensities(dos, sd);
				dos.close();
				
				
				e = new Expression((SlideData)oldInstance, new PersistenceObjectFactory().getClass(), "makeSlideData",
						new Object[]{sd.getSlideDataName(), sd.getSlideDataKeys(), sd.getSampleLabelKey(),
							sd.getSlideDataLabels(), sd.getSlideFileName(), new Boolean(sd.isNonZero()), new Integer(sd.getRows()), new Integer(sd.getColumns()),
							new Integer(sd.getNormalizedState()), new Integer(sd.getSortState()), sd.getSpotInformationData(), sd.getFieldNames(), new Integer(sd.getDataType()),
							annotationFile.getName(), outputFile.getName(), iAnnotationFile.getName(), sd.getSampleAnnotation()/*, progBar*/});
//				System.out.println("annotation file name: " + iAnnotationFile.getName() + "\nExpression: " + e.toString());
			}
		} catch (Exception ioe){
			ioe.printStackTrace();
			System.out.println("Can't write to file to save FloatMatrix");
			return null;
		}
		return e;
	}
	public void initialize(Class type, Object oldInstance, Object newInstance, Encoder encoder) {
		return;
	}
}
