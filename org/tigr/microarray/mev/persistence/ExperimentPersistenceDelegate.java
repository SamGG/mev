
package org.tigr.microarray.mev.persistence;

import java.beans.*;

import org.tigr.microarray.mev.cluster.gui.Experiment;



public class ExperimentPersistenceDelegate extends PersistenceDelegate {
	public Expression instantiate(Object oldInstance, Encoder encoder) {
		Experiment e = (Experiment) oldInstance;
		return new Expression((Experiment) oldInstance, oldInstance.getClass(), "new",
				new Object[]{e.getColumns(), e.getRows(), new Integer(e.getId()), e.getMatrix()});
	}
}












































 