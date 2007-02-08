/*
 * Created on Sep 1, 2005
 */
package org.tigr.microarray.mev.cluster.gui.impl.bridge;

import java.util.Vector;

/**
 * Set of BridgeHyb objects
 * @author iVu
 */
public class BridgeHybSet {
	private Vector vHyb;
	

	/**
	 * @param vector
	 */
	public BridgeHybSet( Vector v ) {
		this.vHyb = v;
	}
	
	
	public Vector getVHyb() {
		return this.vHyb;
	}
}