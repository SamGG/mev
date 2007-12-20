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
				
				File outputFile = File.createTempFile(MultipleArrayViewer.CURRENT_TEMP_DIR + System.getProperty("file.separator") + "floatslidedata", ".bin");
		        outputFile.deleteOnExit();
		        DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile));
		        PersistenceObjectFactory.writeFloatSlideDataIntensities(dos, fsd);
		        dos.close();
		        
				e = new Expression((FloatSlideData) oldInstance, new PersistenceObjectFactory().getClass(), "makeFloatSlideData",
						new Object[]{fsd.getSlideDataKeys(), fsd.getSlideDataLabels(), fsd.getFullSlideFileName(), 
							fsd.getSlideDataName(), new Boolean(fsd.getIsNonZero()), new Integer(fsd.getNormalizedState()), 
							new Integer(fsd.getSortState()), fsd.getSpotInformationData(), new Integer(fsd.getDataType()), fsd.getSlideMetaData(),
							outputFile.getName()});
			} else {
				SlideData sd = (SlideData) oldInstance;
				
				File annotationFile = File.createTempFile(MultipleArrayViewer.CURRENT_TEMP_DIR + System.getProperty("file.separator") + "slidedataannotation", ".bin");
				annotationFile.deleteOnExit();
				DataOutputStream dos = new DataOutputStream(new FileOutputStream(annotationFile));
				PersistenceObjectFactory.writeSlideDataAnnotation(dos, sd);
				dos.close();
////////////////////////////////////////////////////////
				File iAnnotationFile = File.createTempFile(MultipleArrayViewer.CURRENT_TEMP_DIR + System.getProperty("file.separator") + "iannotation", ".txt");
				annotationFile.deleteOnExit();
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(iAnnotationFile)));
							
				PersistenceObjectFactory.writeSlideDataIAnnotation(pw, sd);
				pw.close();
				/**
				 * Trial IAnnotation state saving
				 
				
				String iAnnotationFileName=null;
				if(sd.getAllElements().get(0) instanceof AffySlideDataElement) {
					AnnotationStateSavingParser asp=new AnnotationStateSavingParser();
					Vector <IAnnotation>annotationVector=new Vector();
					Vector allslideData=sd.getAllElements();

					for(int i=0; i<allslideData.size(); i++) {
						//We need to create a function getIAnnotation in ISlideDataElement/AffySlideDataElement
						ISlideDataElement sde=(ISlideDataElement)allslideData.get(i);
						IAnnotation annot;
						if( ( annot=sde.getElementAnnotation())!=null) {
							annotationVector.add(annot);
						}
					}

					if(annotationVector.size()>0) {
						File iAnnotationFile = File.createTempFile(MultipleArrayViewer.CURRENT_TEMP_DIR + System.getProperty("file.separator") + "iannotation", ".txt");
						iAnnotationFileName=iAnnotationFile.getName();
						//iAnnotationFile.deleteOnExit();
						asp.writeAnnotationFile(annotationVector, iAnnotationFile); 
						System.out.println("ISlideDataPersistenceDelegate:"+iAnnotationFile.length());
						System.out.println("ISlideDataPersistenceDelegate:"+iAnnotationFile.length());
						
					}

				}*/
		//////////////////////////////////////////////		
				File outputFile = File.createTempFile(MultipleArrayViewer.CURRENT_TEMP_DIR + System.getProperty("file.separator") + "slidedata", ".bin");
				outputFile.deleteOnExit();
				dos = new DataOutputStream(new FileOutputStream(outputFile));
				PersistenceObjectFactory.writeSlideDataIntensities(dos, sd);
				dos.close();
				
				e = new Expression((SlideData)oldInstance, new PersistenceObjectFactory().getClass(), "makeSlideData",
						new Object[]{sd.getSlideDataName(), sd.getSlideDataKeys(), sd.getSampleLabelKey(),
							sd.getSlideDataLabels(), sd.getSlideFileName(), new Boolean(sd.isNonZero()), new Integer(sd.getRows()), new Integer(sd.getColumns()),
							new Integer(sd.getNormalizedState()), new Integer(sd.getSortState()), sd.getSpotInformationData(), sd.getFieldNames(), new Integer(sd.getDataType()),
							annotationFile.getName(), outputFile.getName(), iAnnotationFile.getName()/*, progBar*/});
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
