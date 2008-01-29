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

import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTree;

/**
 * @author eleanora
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class HCLTreePersistenceDelegate extends PersistenceDelegate {

	/* (non-Javadoc)
	 * @see java.beans.PersistenceDelegate#instantiate(java.lang.Object, java.beans.Encoder)
	 */
	protected Expression instantiate(Object oldInstance, Encoder encoder) {
		HCLTree eh = (HCLTree) oldInstance;
		Expression e = new Expression((HCLTree) oldInstance, oldInstance.getClass(), "new",
				new Object[]{eh.getTreeData(), new Integer(eh.getOrientation())
		});
		return e;
	}

}
