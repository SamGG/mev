/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.persistence;

import java.beans.*;

import org.tigr.graph.GraphViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.impl.kmc.*;
import org.tigr.util.awt.Viewer;


public class ViewerPersistenceDelegate extends PersistenceDelegate {
	public Expression instantiate(Object oldInstance, Encoder encoder) {
		Viewer iv = (Viewer) oldInstance;
		Expression e;
		
		
		if (iv instanceof GraphViewer) {
			GraphViewer ev = (GraphViewer)iv;
			e = new Expression((GraphViewer) oldInstance, oldInstance.getClass(), "new",
					new Object[]{
						ev.getFrame(), 
						new Integer(new Double(ev.getStartX()).intValue()), 
						new Integer(ev.getStopX()), 
						new Integer(new Double(ev.getStartY()).intValue()),
						new Integer(ev.getStopY()),
						new Double(ev.getGraphStartX()),
						new Double(ev.getGraphStopX()),
						new Double(ev.getGraphStartY()), 
						new Double(ev.getGraphStopY()),
						new Integer(ev.getPreXSpacing()),
						new Integer(ev.getPostXSpacing()),
						new Integer(ev.getPreYSpacing()), 
						new Integer(ev.getPostYSpacing()),
						ev.getTitle(),
						ev.getXLabel(),
						ev.getYLabel()});
		} else {
			//No matching class found.
			System.out.println("No Expression available for object " + oldInstance.toString());
			e = new Expression(oldInstance, oldInstance.getClass(), "new", new Object[]{});
		}
		return e;
	}
}












































 