/*
 * Created on Jul 11, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigr.microarray.mev.persistence;

import java.beans.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.tigr.microarray.mev.*;

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
				
				File outputFile = File.createTempFile(MultipleArrayViewer.CURRENT_TEMP_DIR + System.getProperty("file.separator") + "slidedata", ".bin");
				outputFile.deleteOnExit();
				dos = new DataOutputStream(new FileOutputStream(outputFile));
				PersistenceObjectFactory.writeSlideDataIntensities(dos, sd);
				dos.close();
				
				e = new Expression((SlideData)oldInstance, new PersistenceObjectFactory().getClass(), "makeSlideData",
						new Object[]{sd.getSlideDataName(), sd.getSlideDataKeys(), sd.getSampleLabelKey(),
							sd.getSlideDataLabels(), sd.getSlideFileName(), new Boolean(sd.isNonZero()), new Integer(sd.getRows()), new Integer(sd.getColumns()),
							new Integer(sd.getNormalizedState()), new Integer(sd.getSortState()), sd.getSpotInformationData(), sd.getFieldNames(), new Integer(sd.getDataType()),
							annotationFile.getName(), outputFile.getName()/*, progBar*/});

			}
		} catch (IOException ioe){
			System.out.println("Can't write to file to save FloatMatrix");
			return null;
		}
		return e;
	}
	public void initialize(Class type, Object oldInstance, Object newInstance, Encoder encoder) {
		return;
	}
}
