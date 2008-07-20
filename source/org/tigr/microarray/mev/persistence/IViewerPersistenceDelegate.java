/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
   
package org.tigr.microarray.mev.persistence;

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;

import org.tigr.microarray.mev.cluster.gui.IViewer;

/**
 * A PersistenceDelegate class used for saving the state of 
 * implementations of the {@link IViewer} interface.
 * 
 * @author eleanora
 * @see org.tigr.microarray.mev.cluster.gui.IViewer
 * @see XMLEncoderFactory
 */
public class IViewerPersistenceDelegate extends PersistenceDelegate {
	
	/**
	 * Creates an {@link Expression} 
	 * @inheritDoc
	 */
	public Expression instantiate(Object oldInstance, Encoder encoder) {
		//default expression
		Expression e = new Expression(oldInstance, oldInstance.getClass(), "new", new Object[]{});

		e = ((IViewer)oldInstance).getExpression();

		return e;
		
	}
	public void initialize(Class type, Object oldInstance, Object newInstance, Encoder encoder) {
		return;
	}
}
 