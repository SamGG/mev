/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
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