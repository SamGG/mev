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

import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;

/**
 * @author eleanora
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ExperimentHeaderPersistenceDelegate extends PersistenceDelegate {

	/* (non-Javadoc)
	 * @see java.beans.PersistenceDelegate#instantiate(java.lang.Object, java.beans.Encoder)
	 */
	protected Expression instantiate(Object oldInstance, Encoder encoder) {
		ExperimentHeader eh = (ExperimentHeader) oldInstance;
		Expression e = new Expression((ExperimentHeader) oldInstance, oldInstance.getClass(), "new",
				new Object[]{eh.getExperiment(), eh.getClusters(), eh.getSamplesOrder()});
		return e;
	}

}
