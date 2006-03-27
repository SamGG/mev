/*
 * Created on Jul 11, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigr.microarray.mev.persistence;

import java.beans.*;
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
		if(oldInstance instanceof FloatSlideData){
		FloatSlideData fsd = (FloatSlideData) oldInstance;
		e = new Expression((FloatSlideData) oldInstance, oldInstance.getClass(), "new",
		new Object[]{fsd.getSlideDataKeys(), fsd.getSlideDataLabels(), fsd.getFullSlideFileName(), 
				fsd.getSlideDataName(), new Boolean(fsd.getIsNonZero()), new Integer(fsd.getNormalizedState()), 
				new Integer(fsd.getSortState()), fsd.getSpotInformationData(), new Integer(fsd.getDataType())});
		} else {
			SlideData sd = (SlideData) oldInstance;
			e = new Expression((SlideData)oldInstance, oldInstance.getClass(), "new",
					new Object[]{sd.getSlideDataName(), sd.getSlideDataKeys(), sd.getSampleLabelKey(),
					sd.getSlideDataLabels(), sd.getSlideFileName(), new Boolean(sd.isNonZero()), new Integer(sd.getRows()), new Integer(sd.getColumns()),
					new Integer(sd.getNormalizedState()), new Integer(sd.getSortState()), sd.getSpotInformationData(), sd.getFieldNames(), new Integer(sd.getDataType())
			});
		}
		
		return e;
	}
	public void initialize(Class type, Object oldInstance, Object newInstance, Encoder encoder) {
		return;
	}
}
